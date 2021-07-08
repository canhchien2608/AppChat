package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.User;

public class AddParticipantsActivity extends AppCompatActivity {
    EditText search;
    TextView done, notify;
    RecyclerView recyclerView;
    ImageView back;
    FirebaseRecyclerOptions<User> options;
    FirebaseRecyclerAdapter<User, AddPartHolder> adapter;
    DatabaseReference referenceFriend;
    FirebaseUser firebaseUser;
    List<String> listUId;
    List<String> listUIdPart;
    String gid;
    boolean a = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);
        mapping();
        gid = getIntent().getStringExtra("groupid");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceFriend = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("friends");
        listUId = new ArrayList<>();
        listUIdPart = new ArrayList<>();
        getData();
        onClick();
    }

    private void onClick() {
        back.setOnClickListener(v -> finish());
        done.setOnClickListener(v -> {
            if (listUId.size()==0){
                finish();
            }else {
                String currentTime = System.currentTimeMillis() + "";
                for (int i = 0; i < listUId.size(); i++) {
                    HashMap<String, String> hashMap1 = new HashMap<>();
                    hashMap1.put("uid", listUId.get(i));
                    hashMap1.put("role", "member");
                    hashMap1.put("timestamp", currentTime);
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                    ref1.child(gid).child("Participants").child(listUId.get(i))
                            .setValue(hashMap1)
                            .addOnSuccessListener(aVoid1 -> {

                            })
                            .addOnFailureListener(e -> {

                            });
                }
                Toast.makeText(this, "Thêm thành công !", Toast.LENGTH_SHORT).show();
                finish();
            }

        });
    }

    private void getData() {
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(referenceFriend, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, AddPartHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AddPartHolder holder, int position, @NonNull User model) {
                a = false;
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(gid);
                reference.child("Participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listUIdPart.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String a = ds.child("uid").getValue()+"";
                            listUIdPart.add(a);
                        }
                        for (int i = 0; i <listUIdPart.size() ; i++) {
                            if (model.getId().equals(listUIdPart.get(i))){
                                holder.cb.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                    String uId = model.getId();
                    notify.setVisibility(View.GONE);
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(uId);
                    reference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User u = snapshot.getValue(User.class);
                            holder.name.setText(u.getName());
                            try {
                                if (u.getImageURL().equals("default")) {
                                    holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                                } else {
                                    Glide.with(AddParticipantsActivity.this).load(u.getImageURL()).into(holder.avatar);
                                }
                            } catch (Exception e) {
                                holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    holder.cb.setOnClickListener(v -> {
                        if (holder.cb.isChecked()) {
                            listUId.add(uId);
                        } else {
                            for (int i = 0; i < listUId.size(); i++) {
                                if (listUId.get(i).equals(uId)) {
                                    listUId.remove(uId);
                                }
                            }
                        }
                    });
            }

            @NonNull
            @Override
            public AddPartHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(AddParticipantsActivity.this).inflate(R.layout.row_add_part, parent, false);
                return new AddPartHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void mapping() {
        this.search = findViewById(R.id.addPartSearch);
        this.done = findViewById(R.id.addPartDone);
        this.notify = findViewById(R.id.addPartNotify);
        this.recyclerView = findViewById(R.id.addPartRV);
        this.back = findViewById(R.id.addPartBack);
    }
}