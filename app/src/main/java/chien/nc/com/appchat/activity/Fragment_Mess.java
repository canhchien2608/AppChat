package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import chien.nc.com.appchat.R;
import chien.nc.com.appchat.adapter.ListMessAdapter;
import chien.nc.com.appchat.model.ChatContents;
import chien.nc.com.appchat.model.GroupChat;
import chien.nc.com.appchat.model.User;

public class Fragment_Mess extends Fragment {
    private RecyclerView recyclerView;
    private List<User> listMess;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    ConstraintLayout mess_il_overlay;
    ListMessAdapter listMessAdapter;
    private List<String> usersList;
    ImageView btnMore;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mess, container, false);
        this.mess_il_overlay = view.findViewById(R.id.mess_il_overlay);
        mess_il_overlay.setVisibility(View.GONE);
        this.btnMore = view.findViewById(R.id.mess_btnMore);
        btnMore.setOnClickListener(v -> moreOption());
        recyclerView = view.findViewById(R.id.mess_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ContentChats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatContents chat = dataSnapshot.getValue(ChatContents.class);
                    if (chat == null) {
                        return;
                    }
                    if (chat.getSender().equals(firebaseUser.getUid())) {
                        usersList.add(chat.getReceiver());
                    }
                    if (chat.getReceiver().equals(firebaseUser.getUid())) {
                        usersList.add(chat.getSender());
                    }
                }
                showListChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void moreOption() {
        PopupMenu popupMenu = new PopupMenu(getContext(), btnMore, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Nhóm chat");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Tạo nhóm chat");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                showGroupChat();
            } else if (item.getItemId() == 1) {
                createGroupChat();
            }
            return false;
        });
        popupMenu.show();
    }

    private void createGroupChat() {
        if (getContext()==null){
            return;
        }
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Tạo nhóm chat");
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(15, 15, 15, 15);
        EditText edtName = new EditText(getContext());
        edtName.setHint("Tên nhóm chat (tối đa 124 ký tự)        ");
        edtName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(124)});
        linearLayout.addView(edtName);
        builder.setView(linearLayout);
        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String nameGroup = edtName.getText().toString().trim();
            String currentTime = System.currentTimeMillis()+"";
            if (TextUtils.isEmpty(nameGroup)){
                Toast.makeText(getContext(), "Không thể bỏ trống tên nhóm ! ", Toast.LENGTH_SHORT).show();
                return;
            }
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Đang thực hiện");
            progressDialog.show();
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("groupid",currentTime);
            hashMap.put("groupname",nameGroup);
            hashMap.put("timestamp",currentTime);
            hashMap.put("creator",firebaseUser.getUid());
            hashMap.put("avatar","default");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(currentTime).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        HashMap<String,String> hashMap1 = new HashMap<>();
                        hashMap1.put("uid",firebaseUser.getUid());
                        hashMap1.put("role","creator");
                        hashMap1.put("timestamp",currentTime);
                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                        ref1.child(currentTime).child("Participants").child(firebaseUser.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(getContext(), "Tạo nhóm thành công !", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Có lỗi xảy ra "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Có lỗi xảy ra"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });


        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showGroupChat() {
        startActivity(new Intent(getContext(), GroupChatActivity.class));
    }

    private void showListChat() {
        listMess = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listMess.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User u = dataSnapshot.getValue(User.class);
                    for (String id : usersList) {
                        assert u != null;
                        if (u.getId().equals(id)) {
                            if (listMess.size() > 0) {
                                int j = 0;
                                for (int i = 0; i < listMess.size(); i++) {
                                    User user = listMess.get(i);
                                    if (!u.getId().equals(user.getId())) {
                                        j++;
                                    }
                                }
                                if (j == listMess.size()) {
                                    listMess.add(u);
                                    j = 0;
                                }
                            } else {
                                listMess.add(u);
                            }

                        }
                    }

                }

                listMessAdapter = new ListMessAdapter(getContext(), listMess);
                recyclerView.setAdapter(listMessAdapter);
                mess_il_overlay.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        showListChat();
    }
}
