package chien.nc.com.appchat.activity;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import chien.nc.com.appchat.model.AlertDialog;
import chien.nc.com.appchat.model.BottomSheetComment;
import chien.nc.com.appchat.model.Post;
import chien.nc.com.appchat.model.User;
import chien.nc.com.appchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class Fragment_Account extends Fragment {
    CircleImageView imageProfile;
    TextView txtStory;
    TextView txtName;
    DatabaseReference reference, reference1;
    ImageView imgCover;
    FirebaseUser fuser;
    Toolbar toolbar;
    List<Post> listPost, listPostUser;
    FloatingActionButton fab;
    RecyclerView recyclerView;
    StorageReference storageReference;
    private static final int IMG_REQUEST = 1;
    private static final int IMG_COVER_REQUEST = 2;
    private Uri imgUri;
    private Uri imgUriCover;
    private StorageTask uploadTask;
    private String story;
    private String editName;
    private String imageAvatar;
    private String imageCover;

    FirebaseRecyclerAdapter<Post, MyViewHolder> postAdapter;
    FirebaseRecyclerOptions<Post> postOptions;
    DatabaseReference likes,referencePost,reference2;
    Query query;
    String mId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("??ang t???i");
        progressDialog.show();
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (fuser!=null){
            mId = fuser.getUid();
        }
        this.txtStory = view.findViewById(R.id.profile_story);
        this.fab = view.findViewById(R.id.fab_edit);
        this.recyclerView = view.findViewById(R.id.profile_recyclerView);
        this.imgCover = view.findViewById(R.id.profile_image_cover);
        this.imageProfile = view.findViewById(R.id.profile_image);
        this.txtName = view.findViewById(R.id.profile_txtName);
        this.toolbar = view.findViewById(R.id.profile_toolbar);
        ((HomeActivity) Objects.requireNonNull(getContext())).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        storageReference = FirebaseStorage.getInstance().getReference("upload");

        likes = FirebaseDatabase.getInstance().getReference().child("Likes");
        referencePost = FirebaseDatabase.getInstance().getReference().child("Posts");
        query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uId").equalTo(mId);
        reference2 = FirebaseDatabase.getInstance().getReference("Users").child(mId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        listPost = new ArrayList<>();
        listPostUser = new ArrayList<>();
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
                                    Glide.with(getContext()).load(uAvatar).into(holder.avatar);
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
                        Glide.with(getContext()).load(pImage).into(holder.pImage);
                    } else {
                        holder.pImage.setVisibility(View.GONE);
                    }

                    // onClick
                    holder.pImage.setOnClickListener(v -> {
                        Intent i = new Intent(getContext(), ShowImageActivity.class);
                        i.putExtra("urlImage", pImage);
                        i.putExtra("time", pTime);
                        startActivity(i);
                    });
                    holder.countLikes(pId, mId, likes);
                    holder.countComments(pId, mId, referencePost);
                    holder.pLikes.setText(model.getpLikes() + " l?????t th??ch");


                    holder.btnLike.setOnClickListener(v -> {
                        String postId = model.getpId();

                        likes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child(postId).hasChild(mId)) {
                                    likes.child(postId).child(mId).removeValue();
//                                holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
//                                holder.btnLike.setTextColor(Color.parseColor("#000000"));
                                } else {
                                    likes.child(postId).child(mId).setValue("like");
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
                        BottomSheetComment bottomSheet = new BottomSheetComment(R.layout.show_comment_post,mId,pId);
                        bottomSheet.show(getActivity().getSupportFragmentManager(), "");
                    });
                    holder.btnMore.setOnClickListener(v -> {
                        showMoreOptions(holder.btnMore, uId, mId, pId, pImage, pContent);
                    });
                    holder.avatar.setOnClickListener(v -> {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        assert firebaseUser != null;
                        if (!uId.equals(firebaseUser.getUid())) {
                            Intent intent = new Intent(getContext(), UserProfile.class);
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
                            Intent intent = new Intent(getContext(), UserProfile.class);
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
                            Intent intent = new Intent(getContext(), UserProfile.class);
                            intent.putExtra("uid", uId);
                            if (getContext() == null) {
                                return;
                            }
                            startActivity(intent);
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
            postAdapter.startListening();
            recyclerView.setAdapter(postAdapter);




        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);
                assert user != null;
                txtName.setText(user.getName());
                txtStory.setText(user.getStory());
                story = user.getStory();
                editName = user.getName();
                imageCover = user.getImageCover();
                imageAvatar = user.getImageURL();
                if (!user.getImageURL().equals("default")) {
                    if (getContext() != null) {
                        Glide.with(getContext()).load(user.getImageURL()).into(imageProfile);
                    }
                } else {
                    imageProfile.setImageResource(R.drawable.ic_baseline_person_pin_24);
                }
                if (!user.getImageCover().equals("default")) {
                    if (getContext() != null) {
                        Glide.with(getContext()).load(user.getImageCover()).into(imgCover);
                    }
                } else {
                    imgCover.setImageResource(R.drawable.cover);
                }

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(v -> {
            showEditProfile();
        });

        imgCover.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
            RelativeLayout layout = new RelativeLayout(getContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, RelativeLayout.MarginLayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            ImageView imgShowCover = new ImageView(getContext());
            imgShowCover.setLayoutParams(params);
            if (!imageCover.equals("default")) {
                if (getContext() != null) {
                    Glide.with(getContext()).load(imageCover).into(imgShowCover);
                }
            } else {
                imgShowCover.setImageResource(R.drawable.cover);
            }
            layout.addView(imgShowCover);
            builder.setView(layout);
            builder.create().show();

        });

        imageProfile.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
            RelativeLayout layout = new RelativeLayout(getContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            ImageView imgShowAvatar = new ImageView(getContext());
            imgShowAvatar.setLayoutParams(params);
            if (!imageAvatar.equals("default")) {
                if (getContext() != null) {
                    Glide.with(getContext()).load(imageAvatar).into(imgShowAvatar);
                }
            } else {
                imgShowAvatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
            }
            layout.addView(imgShowAvatar);
            builder.setView(layout);
            builder.create().show();

        });

        return view;
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
        PopupMenu popupMenu = new PopupMenu(getContext(), btnMore, Gravity.END);
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
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Ch???nh s???a b??i vi???t");
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(15, 15, 15, 15);
        EditText edtName = new EditText(getContext());
        edtName.setText(pContent);
        linearLayout.addView(edtName);
        builder.setView(linearLayout);
        builder.setPositiveButton("L??u", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
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
        ProgressDialog progressDialog = new ProgressDialog(getContext());
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
                    Toast.makeText(getContext(), "X??a th??nh c??ng !", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), "X??a th??nh c??ng !", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "C?? l???i x???y ra !", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showEditProfile() {
        String[] option = {"Thay ?????i ???nh ?????i di???n", "X??a ???nh ?????i di???n", "Thay ?????i ???nh b??a", "X??a ???nh b??a", "?????i t??n", "Ch???nh s???a ti???u s???", "Thay ?????i m???t kh???u"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Th???c hi???n");
        builder.setItems(option, (dialog, which) -> {
            if (which == 0) {
                openImage();
            } else if (which == 1) {
                if (imageAvatar.equals("default")){
                    return;
                }
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageAvatar);
                storageReference.delete().addOnSuccessListener(aVoid -> {
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageURL", "default");
                    reference.updateChildren(hashMap);
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "C?? l???i x???y ra !", Toast.LENGTH_SHORT).show();
                });

            } else if (which == 2) {
                openImageCover();
            } else if (which == 3) {
                if (imageCover.equals("default")){
                    return;
                }
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageCover);
                storageReference.delete().addOnSuccessListener(aVoid -> {
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageCover", "default");
                    reference.updateChildren(hashMap);
                })
                        .addOnFailureListener(e -> {

                        });

            } else if (which == 4) {
                editName();
            } else if (which == 5) {
                openDialog(story);
            } else if (which == 6) {
                changePassword();
            }
        });
        builder.create().show();
    }

    private void changePassword() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        EditText edtCurrentPass = view.findViewById(R.id.edtCurrentPass);
        EditText edtNewPass = view.findViewById(R.id.edtNewPass);
        Button btnSave = view.findViewById(R.id.btnSaveChange);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setView(view);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        btnSave.setOnClickListener(v -> {
            String currentPass = edtCurrentPass.getText().toString().trim();
            String newPass = edtNewPass.getText().toString().trim();
            if (TextUtils.isEmpty(currentPass)) {
                Toast.makeText(getContext(), "Kh??ng ???????c b??? tr???ng m???t kh???u c??", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 8) {
                Toast.makeText(getContext(), "M???t kh???u m???i ph???i 8 k?? t??? tr??? l??n", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            updatePass(currentPass, newPass);

        });


    }

    private void updatePass(String currentPass, String newPass) {
        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("??ang th???c hi???n");
        pd.show();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(firebaseUser.getEmail()), currentPass);
        firebaseUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseUser.updatePassword(newPass)
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(getContext(), "M???t kh???u ???? ???????c thay ?????i", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "L???i : " + e.toString(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "M???t kh???u c?? kh??ng ch??nh x??c", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

    private void editName() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("?????i t??n");
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(15, 15, 15, 15);
        EditText edtName = new EditText(getContext());
        edtName.setText(editName);
        linearLayout.addView(edtName);
        builder.setView(linearLayout);
        builder.setPositiveButton("L??u", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("??ang th???c hi???n");
            progressDialog.show();
            if (!TextUtils.isEmpty(edtName.getText())) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("name", edtName.getText().toString());
                reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                reference.updateChildren(hashMap);
                progressDialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Kh??ng ???????c b??? tr???ng !", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
        builder.setNegativeButton("H???y", (dialog, which) -> {

        });
        builder.create().show();

    }

    private void openDialog(String story) {
        AlertDialog alertDialog = new AlertDialog(story);
        assert getFragmentManager() != null;
        alertDialog.show(getFragmentManager(), "Ch???nh s???a ti???u s???");

    }

    private void openImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, IMG_REQUEST);
    }

    private void openImageCover() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, IMG_COVER_REQUEST);
    }

    private String getFile(Uri uri) {
        ContentResolver contentResolver = Objects.requireNonNull(getContext()).getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("??ang t???i ???nh l??n...");
        progressDialog.show();
        if (imgUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFile(imgUri));
            uploadTask = fileReference.putFile(imgUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    Uri dUri = task.getResult();
                    assert dUri != null;
                    String mUri = dUri.toString();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageURL", mUri);
                    reference.updateChildren(hashMap);
                    progressDialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "T???i l??n th???t b???i !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "L???i server ! ", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "Kh??ng c?? ???nh n??o ???????c ch???n ! ", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageCover() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("??ang t???i ???nh l??n...");
        progressDialog.show();
        if (imgUriCover != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFile(imgUriCover));
            uploadTask = fileReference.putFile(imgUriCover);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    Uri dUri = task.getResult();
                    assert dUri != null;
                    String mUri = dUri.toString();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageCover", mUri);
                    reference.updateChildren(hashMap);
                    progressDialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "T???i l??n th???t b???i !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "L???i server ! ", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        } else {
            Toast.makeText(getContext(), "Kh??ng c?? ???nh n??o ???????c ch???n ! ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "??ang t???i", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
        if (requestCode == IMG_COVER_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUriCover = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "??ang t???i", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageCover();
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logout, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_logout) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "offline");
            reference.updateChildren(hashMap);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), MainActivity.class));
            if (getActivity() == null) {
                return true;
            }
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
