package chien.nc.com.appchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.internal.LifecycleFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.activity.HomeActivity;
import chien.nc.com.appchat.activity.MessengerActivity;
import chien.nc.com.appchat.model.ChatContents;
import chien.nc.com.appchat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListMessAdapter extends RecyclerView.Adapter<ListMessAdapter.ViewHolder> {
    private final Context context;
    private final List<User> listMess;
    private String send;
    private String receive;
    private boolean checkWho = true;


    public ListMessAdapter(Context context, List<User> listMess) {
        this.context = context;
        this.listMess = listMess;
    }

    @NonNull
    @Override
    public ListMessAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_mess, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListMessAdapter.ViewHolder holder, int position) {
        User u = listMess.get(position);
        holder.txtName.setText(u.getName());
        if (!u.getImageURL().equals("default")) {
            Glide.with(context).load(u.getImageURL()).into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
        }
        if (u.getStatus().equals("online")) {
            holder.status_on.setVisibility(View.VISIBLE);
            holder.status_off.setVisibility(View.GONE);
        } else {
            holder.status_on.setVisibility(View.GONE);
            holder.status_off.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, MessengerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("userid", listMess.get(position).getId());
            context.startActivity(i);
        });
        lastMess(u.getId(), holder.txtLastMess, holder.txtLastMessMe, holder.txtName);



    }

    @Override
    public int getItemCount() {
        return listMess.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar, status_on, status_off;
        TextView txtName, txtLastMess, txtLastMessMe;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.listMess_avatar);
            status_off = itemView.findViewById(R.id.listMess_status_off);
            status_on = itemView.findViewById(R.id.listMess_status_on);
            txtName = itemView.findViewById(R.id.listMess_name);
            txtLastMess = itemView.findViewById(R.id.listMess_lastMess);
            txtLastMessMe = itemView.findViewById(R.id.listMess_me);
        }
    }

    private void lastMess(final String userid, final TextView last_mess, final TextView last_mess_me, final TextView txtName) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ContentChats");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatContents chat = dataSnapshot.getValue(ChatContents.class);
                    if (chat==null){
                        return;
                    }
                    if (firebaseUser==null){
                        return;
                    }
                    if (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        last_mess_me.setVisibility(View.VISIBLE);
                        last_mess.setVisibility(View.GONE);
                        if (chat.getType().equals("image")) {
                            last_mess_me.setText("bạn đã gửi một ảnh");
                        } else {
                            last_mess_me.setText("bạn : " + chat.getMess());
                        }

                    }
                    if (chat.getSender().equals(userid) && chat.getReceiver().equals(firebaseUser.getUid())) {
                        last_mess_me.setVisibility(View.GONE);
                        last_mess.setVisibility(View.VISIBLE);
                        if (chat.getType().equals("image")) {
                            last_mess.setText("đã gửi một ảnh");
                        } else {
                            last_mess.setText(chat.getMess());
                        }
                        checkWho = chat.isSeen();
                        if (!chat.isSeen()){
                            last_mess.setTextColor(Color.parseColor("#000000"));
                            last_mess.setTypeface(last_mess.getTypeface(), Typeface.BOLD);
                            txtName.setTypeface(last_mess.getTypeface(), Typeface.BOLD);
                            txtName.setTextColor(Color.parseColor("#000000"));}
//                        }else {
//                            last_mess.setTextColor(Color.parseColor("#938C8C"));
//                            last_mess.setTypeface(last_mess.getTypeface(), Typeface.NORMAL);
//                            txtName.setTypeface(last_mess.getTypeface(), Typeface.NORMAL);
//                            txtName.setTextColor(Color.parseColor("#938C8C"));
//                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}
