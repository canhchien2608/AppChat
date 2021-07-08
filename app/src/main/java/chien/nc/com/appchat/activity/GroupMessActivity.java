package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import chien.nc.com.appchat.adapter.GroupChatAdapter;
import chien.nc.com.appchat.model.BottomSheetMoreGroupChat;
import chien.nc.com.appchat.model.MessageGroupChat;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessActivity extends AppCompatActivity {
    ImageView btnBack,btnMore,btnAddImg,btnSend,avatar;
    EditText edtContent;
    TextView name;
    String groupId;
    FirebaseUser firebaseUser;
    RecyclerView recyclerView;
    List<MessageGroupChat> list;
    GroupChatAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_mess);
        groupId = getIntent().getStringExtra("groupId");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mapping();
        onClick();
        loadInforGroup();
        getData();
    }

    private void getData() {
        list = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    MessageGroupChat messageGroupChat = ds.getValue(MessageGroupChat.class);
                    list.add(messageGroupChat);
                }
                adapter = new GroupChatAdapter(GroupMessActivity.this,list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadInforGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupid").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String groupName = ds.child("groupname").getValue()+"";
                            String urlAvatar = ds.child("avatar").getValue().toString();
                            name.setText(groupName);
                            try {
                                if(urlAvatar.equals("default")){
                                    avatar.setImageResource(R.drawable.groupchat);
                                }else {
                                    Glide.with(GroupMessActivity.this).load(urlAvatar).into(avatar);
                                }
                            }catch (Exception e){
                                avatar.setImageResource(R.drawable.groupchat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void onClick() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> {
            String message = edtContent.getText().toString().trim();
            if (TextUtils.isEmpty(message)){
                return;
            }
            sendMessage(message);
        });
        btnMore.setOnClickListener(v->{
            BottomSheetMoreGroupChat bottomSheetMoreGroupChat = new BottomSheetMoreGroupChat(R.layout.bottom_sheet_group_chat,groupId);
            bottomSheetMoreGroupChat.show(getSupportFragmentManager(), "");
        });
    }

    private void sendMessage(String message) {
        String timeCurrent = System.currentTimeMillis()+"";
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",firebaseUser.getUid());
        hashMap.put("message",message);
        hashMap.put("time",timeCurrent);
        hashMap.put("type","text");
        hashMap.put("status","none");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timeCurrent).setValue(hashMap)
                .addOnSuccessListener(aVoid -> edtContent.setText("")).addOnFailureListener(e -> Toast.makeText(GroupMessActivity.this, "Có lỗi xảy ra, không thể gửi tin nhắn !", Toast.LENGTH_SHORT).show());
    }

    private void mapping() {
        this.btnAddImg = findViewById(R.id.groupMessAddImg);
        this.btnBack = findViewById(R.id.groupMessBtnBack);
        this.btnMore = findViewById(R.id.groupMessBtnMore);
        this.btnSend = findViewById(R.id.groupMessBtnSend);
        this.edtContent = findViewById(R.id.groupMessContent);
        this.name = findViewById(R.id.groupMessName);
        this.avatar = findViewById(R.id.groupMessAvatar);
        this.recyclerView = findViewById(R.id.groupMessRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }
}