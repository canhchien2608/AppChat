package chien.nc.com.appchat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.activity.UserProfile;
import chien.nc.com.appchat.model.Comment;
import chien.nc.com.appchat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    Context context;
    List<Comment> list;
    FirebaseUser firebaseUser;
    public CommentAdapter(Context context, List<Comment> list) {
        this.context = context;
        this.list = list;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String uid = list.get(position).getUid();
        String cid = list.get(position).getcId();
        String comment = list.get(position).getComment();
        String time = list.get(position).getTimestamp();
        String pid = list.get(position).getPid();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(time));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.time.setText(pTime);
        holder.content.setText(comment);
        holder.name.setOnClickListener(v -> {
            assert firebaseUser != null;
            if (!uid.equals(firebaseUser.getUid())) {
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                assert u != null;
                String uAvatar = u.getImageURL();
                holder.name.setText(u.getName());
                if (!uAvatar.equals("default")) {
                    if (context != null) {
                        try {
                            Glide.with(context).load(uAvatar).into(holder.avatar);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.layoutRowComment.setOnLongClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, holder.layoutRowComment, Gravity.END);
            if (list.size()>0){
                if (list.get(position).getUid().equals(firebaseUser.getUid())){
                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Chỉnh sửa");
                }
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage("Đang xóa . . .");
                        progressDialog.show();
                        DatabaseReference query = FirebaseDatabase.getInstance().getReference("Posts").child(pid);
                        query.child("Comments").child(cid).removeValue();
                        progressDialog.dismiss();
                        Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();

                    } else if (item.getItemId() == 1) {
                        Toast.makeText(context, "chỉnh sửa để sau đi !!!", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                });
                popupMenu.show();

            }
            return false;
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView avatar;
        TextView name,content,time;
        LinearLayout layoutRowComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.listCommentAvatar);
            name = itemView.findViewById(R.id.listCommentName);
            content = itemView.findViewById(R.id.listCommentContent);
            time = itemView.findViewById(R.id.listCommentTime);
            layoutRowComment = itemView.findViewById(R.id.layoutRowComment);
        }
    }
}
