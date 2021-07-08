package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.adapter.ListGroupChatAdapter;
import chien.nc.com.appchat.model.GroupChat;

public class GroupChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView notify;
    RecyclerView recyclerView;
    ConstraintLayout overLay;
    FirebaseAuth firebaseAuth;
    List<GroupChat> list;
    ListGroupChatAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        firebaseAuth = FirebaseAuth.getInstance();
        mapping();
        initToolbar();
        loadData();
    }

    private void loadData() {
        list = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        GroupChat chat = ds.getValue(GroupChat.class);
                        list.add(chat);
                    }
                }
                adapter = new ListGroupChatAdapter(GroupChatActivity.this,list);
                recyclerView.setAdapter(adapter);
                overLay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void mapping() {
        this.toolbar = findViewById(R.id.listGroupChatToolbar);
        this.notify = findViewById(R.id.listGroupChatNotify);
        this.recyclerView = findViewById(R.id.listGroupChatRecyclerView);
        this.overLay = findViewById(R.id.listGroupChatOverLay);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
    }
}