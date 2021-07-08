package chien.nc.com.appchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.activity.UserProfile;
import chien.nc.com.appchat.model.User;

public class ListMemberAdapter extends RecyclerView.Adapter<ListMemberAdapter.ViewHolder> {
    Context context;
    List<String> list;
    String groupId;
    FirebaseUser firebaseUser;
    String admin,role, creator;

    public ListMemberAdapter(Context context, List<String> list, String groupId) {
        this.context = context;
        this.list = list;
        this.groupId = groupId;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        role = "none";
    }

    public ListMemberAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_list_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String id = list.get(position);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                holder.txtName.setText(u.getName());
                try {
                    if (u.getImageURL().equals("default")) {
                        holder.imgAvatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                    } else {
                        Glide.with(context).load(u.getImageURL()).into(holder.imgAvatar);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference1.child("Participants").child(id).child("role").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role2 = snapshot.getValue() + "";
                if (role2.equals("creator")) {
                    holder.txtRole.setText("Người lập nhóm");
                    creator = id;
                } else if (role2.equals("member")) {
                    holder.txtRole.setText("Thành viên");
                    role = id;
                } else {
                    holder.txtRole.setText("Quản trị viên");
                    admin = id;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.imgMore.setOnClickListener(v -> {
            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
            reference2.child("Participants").child(firebaseUser.getUid()).child("role").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role1 = snapshot.getValue() + "";
                    if (role1.equals("admin")) {
                        if (!id.equals(firebaseUser.getUid()) && role.equals(id)) {
                                ShowMoreOption(id, holder);
                        }
                    } else if (role1.equals("creator")){
                        if (!id.equals(firebaseUser.getUid())) {
                                ShowMoreOption(id, holder);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        holder.imgAvatar.setOnClickListener(v -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            assert firebaseUser != null;
            if (!id.equals(firebaseUser.getUid())) {
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra("uid", id);
                if (context == null) {
                    return;
                }
                context.startActivity(intent);
            }
        });
        holder.txtName.setOnClickListener(v -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            assert firebaseUser != null;
            if (!id.equals(firebaseUser.getUid())) {
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra("uid", id);
                if (context == null) {
                    return;
                }
                context.startActivity(intent);
            }
        });
    }

    private void ShowMoreOption(String id, ViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.imgMore, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Xóa khỏi nhóm");
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Gỡ quyền quản trị viên");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                removeRole(id);
            } else if (item.getItemId() == 1) {
                deleteMember(id);
            }
            return false;
        });
        PopupMenu popupMenu1 = new PopupMenu(context, holder.imgMore, Gravity.END);
        popupMenu1.getMenu().add(Menu.NONE, 1, 0, "Xóa khỏi nhóm");
        popupMenu1.getMenu().add(Menu.NONE, 0, 0, "Thăng cấp quyền quản trị viên");
        popupMenu1.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                updateRole(id);
            } else if (item.getItemId() == 1) {
                deleteMember(id);
            }
            return false;
        });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference1.child("Participants").child(id).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue() + "";
                if (role.equals("admin")) {
                    popupMenu.show();
                } else {
                    popupMenu1.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void removeRole(String id) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "member");
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference1.child("Participants").child(id).updateChildren(hashMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Gỡ quyền thành công !", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteMember(String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference.child("Participants").child(id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Xóa thành công !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRole(String id) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference1.child("Participants").child(id).updateChildren(hashMap)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(context, "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgMore;
        TextView txtName, txtRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.rowListMemberAvatar);
            imgMore = itemView.findViewById(R.id.rowListMemberMore);
            txtName = itemView.findViewById(R.id.rowListMemberName);
            txtRole = itemView.findViewById(R.id.rowListMemberRole);
        }
    }
}
