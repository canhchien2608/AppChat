package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.BottomSheetComment;
import chien.nc.com.appchat.model.Post;
import chien.nc.com.appchat.model.User;

public class SearchPostActivity extends AppCompatActivity {
    EditText edtContent;
    RecyclerView recyclerView;
    ImageView btnBack,btnSearch;
    DatabaseReference referencePost, likes, reference;
    FirebaseUser fuser;
    FirebaseRecyclerAdapter<Post, MyViewHolder> fadapter;
    FirebaseRecyclerOptions<Post> options;

    String myId;
    boolean checkLike = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_post);
        this.edtContent = findViewById(R.id.searchPostEdtContent);
        this.recyclerView = findViewById(R.id.searchPostListView);
        this.btnSearch = findViewById(R.id.searchPostBtnSearch);
        this.btnBack = findViewById(R.id.searchPostBtnBack);
        btnBack.setOnClickListener(v->finish());
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchPostActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        referencePost = FirebaseDatabase.getInstance().getReference().child("Posts");
        likes = FirebaseDatabase.getInstance().getReference().child("Likes");
        btnSearch.setOnClickListener(v->{
            String key = edtContent.getText().toString();
            if (TextUtils.isEmpty(key)){
                Toast.makeText(this, "Không được bỏ trống nội dung tìm kiếm", Toast.LENGTH_SHORT).show();
                return;
            }
            loadPost(key);
        });
    }

    private void loadPost(String key) {
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pContent")
                .startAt(key)
                .endAt(key + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        fadapter = new FirebaseRecyclerAdapter<Post, MyViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Post model) {
                String pTimeStamp = model.getpTime();
                String pContent = model.getpContent();
                String pImage = model.getpImage();
                String pId = getRef(position).getKey();
                String uId = model.getuId();
                String pComments = model.getpComments();
                if (pContent.toLowerCase().contains(key.toLowerCase())) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uId);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User u = snapshot.getValue(User.class);
                            if (u == null) {
                                return;
                            }
                            String uAvatar = u.getImageURL();
                            holder.txtName.setText(u.getName());
                            if (!uAvatar.equals("default")) {
                                try {
                                    Glide.with(SearchPostActivity.this).load(uAvatar).into(holder.avatar);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                    String pTimeAgo = caculatorTimeAgo(pTime);
                    holder.txtTime.setText(pTimeAgo);
                    holder.pComments.setText(pComments + " bình luận");

                    holder.txtContent.setText(pContent);
                    if (!pImage.equals("noImage")) {
                        holder.pImage.setVisibility(View.VISIBLE);
                        Glide.with(SearchPostActivity.this).load(pImage).into(holder.pImage);
                    } else {
                        holder.pImage.setVisibility(View.GONE);
                    }

                    // onClick
                    holder.pImage.setOnClickListener(v -> {
                        Intent i = new Intent(SearchPostActivity.this, ShowImageActivity.class);
                        i.putExtra("urlImage", pImage);
                        i.putExtra("time", pTime);
                        startActivity(i);
                    });
                    holder.countLikes(pId, myId, likes);
                    holder.countComments(pId, myId, referencePost);
                    holder.pLikes.setText(model.getpLikes() + " lượt thích");


                    holder.btnLike.setOnClickListener(v -> {
                        checkLike = true;
                        String postId = model.getpId();

                        likes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child(postId).hasChild(myId)) {
                                    likes.child(postId).child(myId).removeValue();
                                    checkLike = false;
//                                holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
//                                holder.btnLike.setTextColor(Color.parseColor("#000000"));
                                } else {
                                    likes.child(postId).child(myId).setValue("like");
                                    checkLike = false;
//                                holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked, 0, 0, 0);
//                                holder.btnLike.setTextColor(Color.parseColor("#FB0404"));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    });


                    holder.btnComment.setOnClickListener(v -> {
                        BottomSheetComment bottomSheet = new BottomSheetComment(R.layout.show_comment_post, myId, pId);
                        bottomSheet.show(getSupportFragmentManager(), "");
                    });
                    holder.btnMore.setOnClickListener(v -> {
                        showMoreOptions(holder.btnMore, uId, myId, pId, pImage, pContent);
                    });
                    holder.avatar.setOnClickListener(v -> {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        assert firebaseUser != null;
                        if (!uId.equals(firebaseUser.getUid())) {
                            Intent intent = new Intent(SearchPostActivity.this, UserProfile.class);
                            intent.putExtra("uid", uId);
                            startActivity(intent);
                        }
                    });
                    holder.txtName.setOnClickListener(v -> {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        assert firebaseUser != null;
                        if (!uId.equals(firebaseUser.getUid())) {
                            Intent intent = new Intent(SearchPostActivity.this, UserProfile.class);
                            intent.putExtra("uid", uId);
                            startActivity(intent);
                        }

                    });
                    holder.avatar.setOnClickListener(v -> {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        assert firebaseUser != null;
                        if (!uId.equals(firebaseUser.getUid())) {
                            Intent intent = new Intent(SearchPostActivity.this, UserProfile.class);
                            intent.putExtra("uid", uId);
                            startActivity(intent);
                        }
                    });
                }


                }

                @NonNull
                @Override
                public MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent,int viewType){
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
                    return new MyViewHolder(view);
                }

        };
        fadapter.startListening();
        recyclerView.setAdapter(fadapter);
    }

    private void showMoreOptions(ImageButton btnMore, String uId, String myId, String pId, String pImage, String pContent) {
        PopupMenu popupMenu = new PopupMenu(SearchPostActivity.this, btnMore, Gravity.END);
        if (uId.equals(myId)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Chỉnh sửa");
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                deletePost(pId, pImage);
            } else if (item.getItemId() == 1) {
                editPost(pId, pContent);
            }
            return false;
        });
        popupMenu.show();
    }

    private void editPost(String pId, String pContent) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(pId);


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa bài viết");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(15, 15, 15, 15);
        EditText edtName = new EditText(this);
        edtName.setText(pContent);
        linearLayout.addView(edtName);
        builder.setView(linearLayout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang thực hiện");
            progressDialog.show();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("pContent", edtName.getText().toString());
            reference.updateChildren(hashMap);
            progressDialog.dismiss();

        });
        builder.setNegativeButton("Hủy", (dialog, which) -> {

        });
        builder.create().show();

    }

    private void deletePost(String pId, String pImage) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xóa . . .");
        if (pImage.equals("noImage")) {
            progressDialog.show();
            Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        dataSnapshot.getRef().removeValue();
                    }
                    Toast.makeText(SearchPostActivity.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            progressDialog.show();
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
            storageReference.delete()
                    .addOnSuccessListener(aVoid -> {
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    dataSnapshot.getRef().removeValue();
                                }
                                Toast.makeText(SearchPostActivity.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(SearchPostActivity.this, "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String caculatorTimeAgo(String pTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        try {
            if (sdf.parse(pTime) == null) {
                return "";
            }
            long time = sdf.parse(pTime).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
    private void checkstatus(String status){
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkstatus("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkstatus("offline");
    }
}