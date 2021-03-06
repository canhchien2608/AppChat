package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import chien.nc.com.appchat.model.Post;
import chien.nc.com.appchat.model.User;

public class Fragment_Home extends Fragment {
    FloatingActionButton btnAdd;
    RecyclerView recyclerView;
    TextView totalFriendRequest;
    ImageView imgListRequestFriend;
    SwipeRefreshLayout swipeRefreshLayout;
    List<Post> list;
    FirebaseRecyclerAdapter<Post, MyViewHolder> fadapter;
    FirebaseRecyclerOptions<Post> options;

    DatabaseReference referencePost, likes, reference1, friendRequest;
    String myId;
    boolean checkLike = false;

    ImageView btnSearch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        this.btnAdd = view.findViewById(R.id.home_btnAdd);
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddPostActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        this.swipeRefreshLayout = view.findViewById(R.id.home_SwipeRefreshLayout);
        recyclerView = view.findViewById(R.id.post_rvPosts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        list = new ArrayList<>();
        referencePost = FirebaseDatabase.getInstance().getReference().child("Posts");
        likes = FirebaseDatabase.getInstance().getReference().child("Likes");
        myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        reference1 = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        loadPostWithFirebase();
        swipeRefreshLayout.setOnRefreshListener(this::loadPostWithFirebase);
        //loadPost();
        //check friend request
        this.totalFriendRequest = view.findViewById(R.id.home_totalFriendRequest);

        this.imgListRequestFriend = view.findViewById(R.id.home_notiFriend);
        this.imgListRequestFriend.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(),ListRequestFriend.class));
        });
        friendRequest = FirebaseDatabase.getInstance().getReference().child("RequestFriend").child(myId);
        friendRequest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    totalFriendRequest.setVisibility(View.VISIBLE);
                    totalFriendRequest.setText(snapshot.getChildrenCount() + "");
                } else {
                    totalFriendRequest.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        this.btnSearch = view.findViewById(R.id.home_btnSearch);
        btnSearch.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getContext(),SearchPostActivity.class));
        });
        return view;
    }

    private void loadPostWithFirebase() {
//        ProgressDialog dialog = new ProgressDialog(getActivity());
//        dialog.setMessage("??ang t???i . . .");
//        dialog.show();
        swipeRefreshLayout.setRefreshing(true);
        options = new FirebaseRecyclerOptions.Builder<Post>().setQuery(referencePost, Post.class).build();
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
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uId);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        swipeRefreshLayout.setRefreshing(false);
                        User u = snapshot.getValue(User.class);
                        if (u == null) {
                            return;
                        }
                        String uAvatar = u.getImageURL();
                        holder.txtName.setText(u.getName());
                        if (!uAvatar.equals("default")) {
                            if (getActivity() == null) {
                                return;
                            }
                            try {
                                Glide.with(getActivity()).load(uAvatar).into(holder.avatar);
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
                holder.pComments.setText(pComments + " b??nh lu???n");

                holder.txtContent.setText(pContent);
                if (!pImage.equals("noImage")) {
                    holder.pImage.setVisibility(View.VISIBLE);
                    Glide.with(getActivity()).load(pImage).into(holder.pImage);
                } else {
                    holder.pImage.setVisibility(View.GONE);
                }

                // onClick
                holder.pImage.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), ShowImageActivity.class);
                    i.putExtra("urlImage", pImage);
                    i.putExtra("time", pTime);
                    Objects.requireNonNull(getActivity()).startActivity(i);
                });
                holder.countLikes(pId, myId, likes);
                holder.countComments(pId, myId, referencePost);
                holder.pLikes.setText(model.getpLikes() + " l?????t th??ch");


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
                    BottomSheetComment bottomSheet = new BottomSheetComment(R.layout.show_comment_post,myId,pId);
                    bottomSheet.show(getActivity().getSupportFragmentManager(), "");
                });
                holder.btnMore.setOnClickListener(v -> {
                    showMoreOptions(holder.btnMore, uId, myId, pId, pImage, pContent);
                });
                holder.avatar.setOnClickListener(v -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    if (!uId.equals(firebaseUser.getUid())) {
                        Intent intent = new Intent(getActivity(), UserProfile.class);
                        intent.putExtra("uid", uId);
                        if (getActivity() == null) {
                            return;
                        }
                        getActivity().startActivity(intent);
                    }
                });
                holder.txtName.setOnClickListener(v -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    if (!uId.equals(firebaseUser.getUid())) {
                        Intent intent = new Intent(getActivity(), UserProfile.class);
                        intent.putExtra("uid", uId);
                        if (getActivity() == null) {
                            return;
                        }
                        getActivity().startActivity(intent);
                    }

                });
                holder.avatar.setOnClickListener(v -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    if (!uId.equals(firebaseUser.getUid())) {
                        Intent intent = new Intent(getActivity(), UserProfile.class);
                        intent.putExtra("uid", uId);
                        if (getActivity() == null) {
                            return;
                        }
                        getActivity().startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
                return new MyViewHolder(view);
            }
        };
        fadapter.startListening();
        recyclerView.setAdapter(fadapter);
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
        PopupMenu popupMenu = new PopupMenu(getActivity(), btnMore, Gravity.END);
        if (uId.equals(myId)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "X??a");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Ch???nh s???a");
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


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle("Ch???nh s???a b??i vi???t");
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setPadding(15, 15, 15, 15);
        EditText edtName = new EditText(getActivity());
        edtName.setText(pContent);
        linearLayout.addView(edtName);
        builder.setView(linearLayout);
        builder.setPositiveButton("L??u", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("??ang th???c hi???n");
            progressDialog.show();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("pContent", edtName.getText().toString());
            reference.updateChildren(hashMap);
            progressDialog.dismiss();

        });
        builder.setNegativeButton("H???y", (dialog, which) -> {

        });
        builder.create().show();

    }

    private void deletePost(String pId, String pImage) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("??ang x??a . . .");
        if (pImage.equals("noImage")) {
            progressDialog.show();
            Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        dataSnapshot.getRef().removeValue();
                    }
                    Toast.makeText(getActivity(), "X??a th??nh c??ng !", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getActivity(), "X??a th??nh c??ng !", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "C?? l???i x???y ra !", Toast.LENGTH_SHORT).show();
                    });
        }
    }



    @Override
    public void onResume() {
        fadapter.notifyDataSetChanged();
        super.onResume();
    }
}
