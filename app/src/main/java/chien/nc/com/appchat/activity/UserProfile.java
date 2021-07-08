package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.BottomSheetComment;
import chien.nc.com.appchat.model.Comment;
import chien.nc.com.appchat.model.Post;
import chien.nc.com.appchat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

public class UserProfile extends AppCompatActivity {
    TextView name, story;
    ImageView cover;
    Button btnChat, btnUnFriend, btnAddFriend, btnCancelAddFriend, btnAccept, btnCancelPending;
    CircleImageView avatar;
    String uId;
    FirebaseUser fuser;
    DatabaseReference reference, reference1, dbFriends, dbFriends1, dbFriends2, requestRef,referencePost,likes;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    List<Post> listPostUser, listPost;
    List<User> listCheckFriend;
    Toolbar toolbar;
    LinearLayout containerFriend, containerPending;
    RelativeLayout containerNotFriend;
    boolean checkFriend = false;
    String myId,avatarURL,coverURL;

    User friend, myInfor;

    ConstraintLayout overlay;
    Query query ;
    FirebaseRecyclerAdapter<Post, MyViewHolder> postAdapter;
    FirebaseRecyclerOptions<Post> postOptions;
    FirebaseRecyclerAdapter<Comment, CommentViewHolder> commentAdapter;
    FirebaseRecyclerOptions<Comment> commentOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Intent intent = getIntent();
        uId = intent.getStringExtra("uid");
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getMyInfor();
        requestRef = FirebaseDatabase.getInstance().getReference().child("RequestFriend");
        referencePost = FirebaseDatabase.getInstance().getReference().child("Posts");

        query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uId").equalTo(uId);
        mapping();
        likes = FirebaseDatabase.getInstance().getReference().child("Likes");
        initToolbar();
        loadStatusFriend();
        checkFriendRequest();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        listPostUser = new ArrayList<>();
        listPost = new ArrayList<>();
        getData();
        loadUserPost();

        onClick();

    }

    private void getMyInfor() {
        DatabaseReference my = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        my.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myInfor = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadStatusFriend() {
        listCheckFriend = new ArrayList<>();
        dbFriends = FirebaseDatabase.getInstance().getReference("Users").child(myId).child("friends");
        dbFriends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listCheckFriend.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User u = dataSnapshot.getValue(User.class);
                    listCheckFriend.add(u);
                }
                for (int i = 0; i < listCheckFriend.size(); i++) {
                    if (uId.equals(listCheckFriend.get(i).getId())) {
                        checkFriend = true;
                    }
                }
                if (checkFriend) {
                    containerFriend.setVisibility(View.VISIBLE);
                    containerNotFriend.setVisibility(View.GONE);
                    containerPending.setVisibility(View.GONE);
                } else {
                    dbFriends2 = FirebaseDatabase.getInstance().getReference().child("RequestFriend").child(myId).child(uId);
                    dbFriends2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                containerFriend.setVisibility(View.GONE);
                                containerNotFriend.setVisibility(View.GONE);
                                containerPending.setVisibility(View.VISIBLE);
                            } else {
                                containerFriend.setVisibility(View.GONE);
                                containerNotFriend.setVisibility(View.VISIBLE);
                                containerPending.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                overlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void checkFriendRequest(){
        dbFriends1 = FirebaseDatabase.getInstance().getReference().child("RequestFriend").child(uId).child(myId);
        dbFriends1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadStatusFriend();
                if (snapshot.exists()) {
                    btnCancelAddFriend.setVisibility(View.VISIBLE);
                    btnAddFriend.setVisibility(View.GONE);
                } else {
                    btnCancelAddFriend.setVisibility(View.GONE);
                    btnAddFriend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void onClick() {
        cover.setOnClickListener(v->{
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(UserProfile.this);
            RelativeLayout layout = new RelativeLayout(UserProfile.this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, RelativeLayout.MarginLayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            ImageView imgShowCover = new ImageView(UserProfile.this);
            imgShowCover.setLayoutParams(params);
            if (!coverURL.equals("default")) {
                if (getContext() != null) {
                    Glide.with(this).load(coverURL).into(imgShowCover);
                }
            } else {
                imgShowCover.setImageResource(R.drawable.cover);
            }
            layout.addView(imgShowCover);
            builder.setView(layout);
            builder.create().show();
        });
        avatar.setOnClickListener(v->{
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            RelativeLayout layout = new RelativeLayout(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            ImageView imgShowAvatar = new ImageView(this);
            imgShowAvatar.setLayoutParams(params);
            if (!avatarURL.equals("default")) {
                if (getContext() != null) {
                    Glide.with(this).load(avatarURL).into(imgShowAvatar);
                }
            } else {
                imgShowAvatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
            }
            layout.addView(imgShowAvatar);
            builder.setView(layout);
            builder.create().show();
        });
        btnChat.setOnClickListener(v -> {
            Intent i = new Intent(UserProfile.this, MessengerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("userid", uId);
            startActivity(i);
        });
        btnAddFriend.setOnClickListener(v -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid",fuser.getUid()+"");
            hashMap.put("status", "pending");
            requestRef.child(uId).child(fuser.getUid()).updateChildren(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UserProfile.this, "Lời mời kết bạn đã được gửi đi", Toast.LENGTH_SHORT).show();
                    btnAddFriend.setVisibility(View.GONE);
                    btnCancelAddFriend.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(UserProfile.this, "có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                }
            });

        });
        btnCancelAddFriend.setOnClickListener(v -> requestRef.child(uId).child(fuser.getUid()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UserProfile.this, "Bạn đã hủy lời mời", Toast.LENGTH_SHORT).show();
                btnAddFriend.setVisibility(View.VISIBLE);
                btnCancelAddFriend.setVisibility(View.GONE);
            } else {
                Toast.makeText(UserProfile.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
            }
        }));
        btnAccept.setOnClickListener(v -> {
            DatabaseReference mData = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friends");
            mData.child(uId).setValue(friend).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UserProfile.this, "Thêm bạn thành công !", Toast.LENGTH_SHORT).show();
                }

            });
            DatabaseReference mData1 = FirebaseDatabase.getInstance().getReference("Users").child(uId).child("friends");
            mData1.child(myId).setValue(myInfor).addOnCompleteListener(task1 -> requestRef.child(myId).child(uId).removeValue().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful()) {
                    containerPending.setVisibility(View.GONE);
                    containerFriend.setVisibility(View.VISIBLE);
                    containerNotFriend.setVisibility(View.GONE);
                } else {
                    Toast.makeText(UserProfile.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                }
            }));
        });
        btnCancelPending.setOnClickListener(v -> requestRef.child(myId).child(uId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UserProfile.this, "Bạn đã hủy lời mời", Toast.LENGTH_SHORT).show();
                containerPending.setVisibility(View.GONE);
                containerFriend.setVisibility(View.GONE);
                btnAddFriend.setVisibility(View.VISIBLE);
                btnCancelAddFriend.setVisibility(View.GONE);
            } else {
                Toast.makeText(UserProfile.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
            }
        }));
        btnUnFriend.setOnClickListener(v -> {
            DatabaseReference myFriend = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friends");
            DatabaseReference uFriend = FirebaseDatabase.getInstance().getReference("Users").child(uId).child("friends");
            myFriend.child(uId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    uFriend.child(myId).removeValue().addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            Toast.makeText(UserProfile.this, "Đã xóa bạn !", Toast.LENGTH_SHORT).show();
                            containerPending.setVisibility(View.GONE);
                            containerFriend.setVisibility(View.GONE);
                            containerNotFriend.setVisibility(View.VISIBLE);
                            btnAddFriend.setVisibility(View.VISIBLE);
                            btnCancelAddFriend.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(this, "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }


    private void loadUserPost() {

        ProgressDialog dialog = new ProgressDialog(UserProfile.this);
        dialog.setMessage("Đang tải . . .");
        dialog.show();
        postOptions = new FirebaseRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        postAdapter = new FirebaseRecyclerAdapter<Post, MyViewHolder>(postOptions) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Post model) {
                String pTimeStamp = model.getpTime();
                String pContent = model.getpContent();
                String pImage = model.getpImage();
                String pId = getRef(position).getKey();
                String uId = model.getuId();
                String pComments = model.getpComments();
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
                            if (getContext() == null) {
                                return;
                            }
                            try {
                                Glide.with(UserProfile.this).load(uAvatar).into(holder.avatar);
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
                    Glide.with(UserProfile.this).load(pImage).into(holder.pImage);
                } else {
                    holder.pImage.setVisibility(View.GONE);
                }

                // onClick
                holder.pImage.setOnClickListener(v -> {
                    Intent i = new Intent(UserProfile.this, ShowImageActivity.class);
                    i.putExtra("urlImage", pImage);
                    i.putExtra("time", pTime);
                    startActivity(i);
                });
                holder.countLikes(pId, myId, likes);
                holder.countComments(pId, myId, referencePost);
                holder.pLikes.setText(model.getpLikes() + " lượt thích");


                holder.btnLike.setOnClickListener(v -> {
                    String postId = model.getpId();

                    likes.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.child(postId).hasChild(myId)) {
                                likes.child(postId).child(myId).removeValue();
//                                holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
//                                holder.btnLike.setTextColor(Color.parseColor("#000000"));
                            } else {
                                likes.child(postId).child(myId).setValue("like");
//                                holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked, 0, 0, 0);
//                                holder.btnLike.setTextColor(Color.parseColor("#FB0404"));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });


//                holder.btnComment.setOnClickListener(v -> {
//                    @SuppressLint("InflateParams") View view = LayoutInflater.from(UserProfile.this).inflate(R.layout.show_comment_post, null);
//                    ImageView btnSend = view.findViewById(R.id.cmt_btnSend);
//                    EditText edtContent = view.findViewById(R.id.cmt_edtContent);
//                    CircleImageView civAvatar = view.findViewById(R.id.cmt_avatar);
//                    reference1.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            User u = snapshot.getValue(User.class);
//                            if (u == null) {
//                                return;
//                            }
//                            if (!u.getImageURL().equals("default")) {
//                                if (getContext() == null) {
//                                    return;
//                                }
//                                try {
//                                    Glide.with(UserProfile.this).load(u.getImageURL()).into(civAvatar);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//                            } else {
//                                civAvatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                    RecyclerView rvListComment = view.findViewById(R.id.cmt_rvListCmt);
//                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(UserProfile.this);
//                    builder.setView(view);
//                    androidx.appcompat.app.AlertDialog dialog = builder.create();
//                    dialog.show();
//
//                    btnSend.setOnClickListener(v1 -> {
//                        ProgressDialog progressDialog = new ProgressDialog(UserProfile.this);
//                        progressDialog.setMessage("đang tải lên...");
//                        String comment = edtContent.getText().toString().trim();
//                        if (TextUtils.isEmpty(edtContent.getText())) {
//                            Toast.makeText(UserProfile.this, "không được để trống nội dung !", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        String timeStamp = String.valueOf(System.currentTimeMillis());
//                        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Posts").child(pId).child("Comments");
//                        HashMap<String, String> hashMap = new HashMap<>();
//                        hashMap.put("cId", timeStamp);
//                        hashMap.put("comment", comment);
//                        hashMap.put("timestamp", timeStamp);
//                        hashMap.put("uid", myId);
//                        hashMap.put("pid", pId);
//                        progressDialog.show();
//                        dr.child(timeStamp).setValue(hashMap)
//                                .addOnSuccessListener(aVoid -> {
//                                    progressDialog.dismiss();
//                                    edtContent.setText("");
//                                })
//                                .addOnFailureListener(e -> {
//                                    Toast.makeText(UserProfile.this, "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
//                                    progressDialog.dismiss();
//                                });
//
//                    });
//                    LinearLayoutManager layoutManager = new LinearLayoutManager(UserProfile.this);
//                    rvListComment.setLayoutManager(layoutManager);
//                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Posts").child(pId).child("Comments");
//                    commentOptions = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(reference2, Comment.class).build();
//                    commentAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(commentOptions) {
//                        @Override
//                        protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
//                            String uid = model.getUid();
//                            String cid = model.getcId();
//                            String comment = model.getComment();
//                            String time = model.getTimestamp();
//                            String pid = model.getPid();
//
//                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
//                            calendar.setTimeInMillis(Long.parseLong(time));
//                            String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
//                            String pTimeAgo = caculatorTimeAgo(pTime);
//                            holder.time.setText(pTimeAgo);
//                            holder.content.setText(comment);
//                            holder.name.setOnClickListener(v -> {
//                                if (myId == null) {
//                                    return;
//                                }
//                                if (!uid.equals(myId)) {
//                                    Intent intent = new Intent(UserProfile.this, UserProfile.class);
//                                    intent.putExtra("uid", uid);
//                                    startActivity(intent);
//                                }
//                            });
//
//                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
//                            reference.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    User u = snapshot.getValue(User.class);
//                                    assert u != null;
//                                    String uAvatar = u.getImageURL();
//                                    holder.name.setText(u.getName());
//                                    if (!uAvatar.equals("default")) {
//                                        if (getContext() != null) {
//                                            try {
//                                                Glide.with(UserProfile.this).load(uAvatar).into(holder.avatar);
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//
//                                    } else {
//                                        holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//                            holder.layoutRowComment.setOnLongClickListener(v -> {
//                                PopupMenu popupMenu = new PopupMenu(UserProfile.this, holder.layoutRowComment, Gravity.END);
//                                if (model.getUid().equals(myId)) {
//                                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
//                                }
//                                popupMenu.setOnMenuItemClickListener(item -> {
//                                    if (item.getItemId() == 0) {
//                                        ProgressDialog progressDialog = new ProgressDialog(UserProfile.this);
//                                        progressDialog.setMessage("Đang xóa . . .");
//                                        progressDialog.show();
//                                        DatabaseReference query = FirebaseDatabase.getInstance().getReference("Posts").child(pid);
//                                        query.child("Comments").child(cid).removeValue();
//                                        progressDialog.dismiss();
//                                        Toast.makeText(UserProfile.this, "Đã xóa", Toast.LENGTH_SHORT).show();
//
//                                    }
//                                    return false;
//                                });
//                                popupMenu.show();
//
//                                return false;
//                            });
//                        }
//
//                        @NonNull
//                        @Override
//                        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                            View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment, parent, false);
//                            return new CommentViewHolder(view1);
//                        }
//                    };
//                    commentAdapter.startListening();
//                    rvListComment.setAdapter(commentAdapter);
//
//                });
                holder.btnComment.setOnClickListener(v -> {
                    BottomSheetComment bottomSheet = new BottomSheetComment(R.layout.show_comment_post,myId,pId);
                    bottomSheet.show(getSupportFragmentManager(), "");
                });
                holder.btnMore.setOnClickListener(v -> {
                    showMoreOptions(holder.btnMore, uId, myId, pId, pImage, pContent);
                });
                holder.avatar.setOnClickListener(v -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    if (!uId.equals(firebaseUser.getUid())) {
                        Intent intent = new Intent(UserProfile.this, UserProfile.class);
                        intent.putExtra("uid", uId);
                        if (getContext() == null) {
                            return;
                        }
                        startActivity(intent);
                    }
                });
                holder.txtName.setOnClickListener(v -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    if (!uId.equals(firebaseUser.getUid())) {
                        Intent intent = new Intent(UserProfile.this, UserProfile.class);
                        intent.putExtra("uid", uId);
                        if (getContext() == null) {
                            return;
                        }
                        startActivity(intent);
                    }

                });
                holder.avatar.setOnClickListener(v -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    if (!uId.equals(firebaseUser.getUid())) {
                        Intent intent = new Intent(UserProfile.this, UserProfile.class);
                        intent.putExtra("uid", uId);
                        if (getContext() == null) {
                            return;
                        }
                        startActivity(intent);
                    }
                });
                dialog.dismiss();
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
                return new MyViewHolder(view);
            }

        };
        postAdapter.startListening();
        recyclerView.setAdapter(postAdapter);
        dialog.dismiss();
        progressDialog.dismiss();
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
    private void showMoreOptions(ImageButton btnMore, String uId, String myId, String pId, String pImage, String pContent) {
        PopupMenu popupMenu = new PopupMenu(UserProfile.this, btnMore, Gravity.END);
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


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(UserProfile.this);
        builder.setTitle("Chỉnh sửa bài viết");
        LinearLayout linearLayout = new LinearLayout(UserProfile.this);
        linearLayout.setPadding(15, 15, 15, 15);
        EditText edtName = new EditText(UserProfile.this);
        edtName.setText(pContent);
        linearLayout.addView(edtName);
        builder.setView(linearLayout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(UserProfile.this);
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
        ProgressDialog progressDialog = new ProgressDialog(UserProfile.this);
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
                    Toast.makeText(UserProfile.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(UserProfile.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(UserProfile.this, "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void getData() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải");
        progressDialog.show();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(uId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                friend = user;
                name.setText(user.getName());
                toolbar.setTitle(user.getName());
                story.setText(user.getStory());
                if (!user.getImageURL().equals("default")) {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(avatar);
                } else {
                    avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                }
                if (!user.getImageCover().equals("default")) {
                    Glide.with(getApplicationContext()).load(user.getImageCover()).into(cover);
                } else {
                    cover.setImageResource(R.drawable.cover);
                }
                avatarURL = user.getImageURL();
                coverURL = user.getImageCover();
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
        this.btnChat = findViewById(R.id.userProfileBtnChat);
        this.btnAccept = findViewById(R.id.userProfileBtnAccept);
        this.btnCancelPending = findViewById(R.id.userProfileBtnCancelPending);
        this.btnUnFriend = findViewById(R.id.userProfileBtnUnFriend);
        this.btnAddFriend = findViewById(R.id.userProfileBtnAddFriend);
        this.btnCancelAddFriend = findViewById(R.id.userProfileBtnCancelAddFriend);
        this.toolbar = findViewById(R.id.userProfileToolbar);
        this.name = findViewById(R.id.userProfileName);
        this.story = findViewById(R.id.userProfileStory);
        this.cover = findViewById(R.id.userProfileImgCover);
        this.avatar = findViewById(R.id.userProfileImgAvatar);
        this.recyclerView = findViewById(R.id.userProfilePost);
        this.containerFriend = findViewById(R.id.container_friend);
        this.containerPending = findViewById(R.id.container_pending);
        this.containerNotFriend = findViewById(R.id.container_notFriend);
        this.overlay = findViewById(R.id.userProfileOverlay);
        overlay.setVisibility(View.VISIBLE);
    }

    private void checkstatus(String status) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkstatus("online");
    }


    @Override
    protected void onPause() {
        super.onPause();
        checkstatus("offline");
    }

}