package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.RequestFriend;
import chien.nc.com.appchat.model.User;

public class ListRequestFriend extends AppCompatActivity {
    Toolbar toolBar;
    RecyclerView recyclerView;
    ConstraintLayout overLay;
    FirebaseRecyclerAdapter<RequestFriend,RequstFriendHolder> requestFriendAdapter;
    FirebaseRecyclerOptions<RequestFriend> requestFriendOptions;
    DatabaseReference referenceRequestFriend,reference,requestRef;
    FirebaseUser fuser;
    User friend,myInfor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_request_friend);
        mapping();
        initToolbar();
        init();
        loadListRequest();
    }

    private void initToolbar() {
        setSupportActionBar(toolBar);
        if (getSupportActionBar()==null){
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolBar.setNavigationOnClickListener(v -> finish());
    }

    private void init() {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        referenceRequestFriend = FirebaseDatabase.getInstance().getReference("RequestFriend").child(fuser.getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(ListRequestFriend.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        overLay.setVisibility(View.VISIBLE);
        requestRef = FirebaseDatabase.getInstance().getReference().child("RequestFriend");
    }

    private void loadListRequest() {
        requestFriendOptions = new FirebaseRecyclerOptions.Builder<RequestFriend>().setQuery(referenceRequestFriend, RequestFriend.class).build();
        requestFriendAdapter = new FirebaseRecyclerAdapter<RequestFriend, RequstFriendHolder>(requestFriendOptions) {

            @NonNull
            @Override
            public RequstFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_request_friend, parent, false);
                return new RequstFriendHolder(view);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull RequstFriendHolder holder, int position, @NonNull RequestFriend model) {
                holder.avatar.setOnClickListener(v -> {
                    Intent i = new Intent(ListRequestFriend.this,UserProfile.class);
                    i.putExtra("uid",model.getUid());
                    startActivity(i);
                });
                holder.name.setOnClickListener(v -> {
                    Intent i = new Intent(ListRequestFriend.this,UserProfile.class);
                    i.putExtra("uid",model.getUid());
                    startActivity(i);
                });
                String uid = model.getUid();
                reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User u = snapshot.getValue(User.class);
                        if (u==null){
                            return;
                        }
                        holder.name.setText(u.getName());
                        if (u.getImageURL().equalsIgnoreCase("default")){
                            holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                        }else {
                            try {
                                Glide.with(ListRequestFriend.this).load(u.getImageURL()).into(holder.avatar);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                DatabaseReference loadInforFriend = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid());
                loadInforFriend.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        friend = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                DatabaseReference loadMyInfor = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                loadMyInfor.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myInfor = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.btnAccept.setOnClickListener(v -> {

                    DatabaseReference mData = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friends");
                    mData.child(model.getUid()).setValue(friend).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ListRequestFriend.this, "Thêm bạn thành công !", Toast.LENGTH_SHORT).show();
                        }

                    });
                    DatabaseReference mData1 = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid()).child("friends");
                    mData1.child(fuser.getUid()).setValue(myInfor).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            requestRef.child(fuser.getUid()).child(model.getUid()).removeValue().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    holder.btnCancel.setVisibility(View.GONE);
                                    holder.btnAccept.setVisibility(View.GONE);
                                    holder.notify.setVisibility(View.VISIBLE);
                                    holder.notify.setText("Bạn đã chấp nhận lời mời kết bạn !");
                                } else {
                                    Toast.makeText(ListRequestFriend.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                });
                holder.btnCancel.setOnClickListener(v->{
                    requestRef.child(fuser.getUid()).child(model.getUid()).removeValue().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            holder.btnCancel.setVisibility(View.GONE);
                            holder.btnAccept.setVisibility(View.GONE);
                            holder.notify.setVisibility(View.VISIBLE);
                            holder.notify.setText("Bạn đã từ chối lời mời kết bạn !");
                        } else {
                            Toast.makeText(ListRequestFriend.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                holder.itemListRequest.setOnClickListener(v->{

                });
            }
        };
        requestFriendAdapter.startListening();
        recyclerView.setAdapter(requestFriendAdapter);
        overLay.setVisibility(View.GONE);

    }

    private void mapping() {
        this.toolBar = findViewById(R.id.listRequestFriendToolbar);
        this.recyclerView = findViewById(R.id.listRequestFriendRecyclerView);
        this.overLay = findViewById(R.id.listRequestFriendOverlay);
    }
}