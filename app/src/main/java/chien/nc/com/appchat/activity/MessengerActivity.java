package chien.nc.com.appchat.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.adapter.MessengerAdapter;
import chien.nc.com.appchat.model.BottomSheetMess;
import chien.nc.com.appchat.model.ChatContents;
import chien.nc.com.appchat.model.User;

public class MessengerActivity extends AppCompatActivity {
    ImageView imgBack, imgAvatar, imgMore, imgImage, imgSend;
    RecyclerView recyclerView;
    EditText edtContent;
    TextView txtName, txtStatus,txtNotify;
    FirebaseUser fuser;
    String userid, myName, myId;
    DatabaseReference reference, userRefForSeen, referenceSendMess;
    MessengerAdapter messengerAdapter;
    List<ChatContents> chatContentsList;
    ValueEventListener seenListener;

    private static final int CAMERA_REQ_CODE = 1;
    private static final int GALLERY_REQ_CODE = 2;
    private static final int IMAGE_PICK_CAMERA = 3;
    private static final int IMAGE_PICK_GALLERY = 4;

    String[] cameraPermissions;
    String[] galleryPermissions;

    Uri img_Uri = null;

    String URL = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;

    User userFriend;
    boolean checkStatusUser =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        mapping();
        getMyInfor();
        getData();
        checkStatusUser();
        init();
        onClick();
    }

    private void checkStatusUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(myId).child("block").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    checkStatusUser = true;
                    if (checkStatusUser){
                        edtContent.setVisibility(View.GONE);
                        imgSend.setVisibility(View.GONE);
                        imgImage.setVisibility(View.GONE);
                        txtNotify.setVisibility(View.VISIBLE);
                    }else {
                        edtContent.setVisibility(View.VISIBLE);
                        imgSend.setVisibility(View.VISIBLE);
                        imgImage.setVisibility(View.VISIBLE);
                        txtNotify.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        reference1.child(userid).child("block").child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    checkStatusUser = true;
                    if (checkStatusUser){
                        edtContent.setVisibility(View.GONE);
                        imgSend.setVisibility(View.GONE);
                        imgImage.setVisibility(View.GONE);
                        txtNotify.setVisibility(View.VISIBLE);
                    }else {
                        edtContent.setVisibility(View.VISIBLE);
                        imgSend.setVisibility(View.VISIBLE);
                        imgImage.setVisibility(View.VISIBLE);
                        txtNotify.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getMyInfor() {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (fuser == null) {
            return;
        }
        myId = fuser.getUid();
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                myName = u.getName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void init() {
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        galleryPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        requestQueue = Volley.newRequestQueue(this);
        referenceSendMess = FirebaseDatabase.getInstance().getReference("ContentChats");

    }

    private void onClick() {
        imgBack.setOnClickListener(v -> finish());
        imgSend.setOnClickListener(v -> {
            String mess = edtContent.getText().toString();
            if (TextUtils.isEmpty(mess)) {
                return;
            }
            sendMessenger(fuser.getUid(), userid, mess);
//            String timeStamp = String.valueOf(System.currentTimeMillis());
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("sender", fuser.getUid());
//            hashMap.put("receiver", userid);
//            hashMap.put("mess", mess);
//            hashMap.put("time", timeStamp);
//            hashMap.put("seen", false);
//            hashMap.put("type", "text");
//            hashMap.put("mid", timeStamp);
//            hashMap.put("status", "none");
//            try {
//                referenceSendMess.child(timeStamp).setValue(hashMap).addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        sendNotification(mess);
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        });
        imgMore.setOnClickListener(v -> {
            BottomSheetMess bottomSheet = new BottomSheetMess(R.layout.bottomsheetmess,userid);
            bottomSheet.show(getSupportFragmentManager(), "");
        });
        imgImage.setOnClickListener(v -> {
            showImagePick();
        });

    }

    private void showImagePick() {
        String[] options = {"Máy ảnh", "Thư viện"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh từ");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            }
            if (which == 1) {
                if (!checkGalleryPermission()) {
                    requestGalleryPermission();
                } else {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY);

        // chọn nhiều ảnh
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent,"Select Picture"), IMAGE_PICK_GALLERY);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Chọn tạm thời");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Mô tả tạm thời");
        img_Uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, img_Uri);
        startActivityForResult(i, IMAGE_PICK_CAMERA);
    }

    private boolean checkGalleryPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, galleryPermissions, GALLERY_REQ_CODE);
    }

    private boolean checkCameraPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED)) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED));
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQ_CODE) {
            if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted && storageAccepted) {
                    pickFromCamera();
                } else {
                    Toast.makeText(this, "Bạn cần cho phép truy cập!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == GALLERY_REQ_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Bạn cần cho phép truy cập!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA) {
                sendImageMess(img_Uri);
            } else if (requestCode == IMAGE_PICK_GALLERY) {
                img_Uri = data.getData();
                sendImageMess(img_Uri);

            }
        }
    }

    private void sendImageMess(Uri img_uri) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Đang gửi...");
        dialog.show();

        String timeStamp = System.currentTimeMillis() + "";
        String fileNameAndPath = "MessImage/" + "post_" + timeStamp;

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), img_Uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray(); //convert image to byte
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
            storageReference.putBytes(data).addOnSuccessListener(taskSnapshot -> {
                dialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ContentChats");
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", fuser.getUid());
                    hashMap.put("receiver", userid);
                    hashMap.put("mess", downloadUri);
                    hashMap.put("time", timeStamp);
                    hashMap.put("seen", false);
                    hashMap.put("type", "image");
                    hashMap.put("mid", timeStamp);
                    hashMap.put("status", "none");

                    reference.child(timeStamp).setValue(hashMap);
                }
            }).addOnFailureListener(e -> {

            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getData() {
        Intent i = getIntent();
        userid = i.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userFriend = user;
                assert user != null;
                txtName.setText(user.getName());
                if (user.getStatus().equals("online")) {
                    txtStatus.setText("online");
                    txtStatus.setTextColor(Color.parseColor("#05E80F"));
                } else {
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(user.getLastOnline()));
                    String mTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                    txtStatus.setText("truy cập " + mTime);
                    txtStatus.setTextColor(Color.parseColor("#000000"));
                }
                if (!user.getImageURL().equals("default")) {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                }
                try {
                    getMess(fuser.getUid(), userid, user.getImageURL());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMess(userid);
    }

    private void mapping() {
        this.imgBack = findViewById(R.id.messenger_imgBack);
        this.imgAvatar = findViewById(R.id.messenger_imgAvatar);
        this.imgMore = findViewById(R.id.messenger_imgMore);
        this.imgImage = findViewById(R.id.messenger_imgImage);
        this.imgSend = findViewById(R.id.messenger_imgSend);
        this.txtName = findViewById(R.id.messenger_txtName);
        this.txtStatus = findViewById(R.id.messenger_txtStatus);
        this.edtContent = findViewById(R.id.messenger_edtContent);
        this.recyclerView = findViewById(R.id.messenger_recyclerView);
        this.txtNotify = findViewById(R.id.messenger_txtNotify);
    }

    private void seenMess(String userid) {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("ContentChats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatContents chat = ds.getValue(ChatContents.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen", true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessenger(String send, String receive, String mess) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
//        chatContentsList.add(new ChatContents(send,receive,mess,timeStamp,false,"text",timeStamp,"none"));
//        messengerAdapter.notifyDataSetChanged();
//        recyclerView.setAdapter(messengerAdapter);
        edtContent.setText("");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ContentChats");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", send);
        hashMap.put("receiver", receive);
        hashMap.put("mess", mess);
        hashMap.put("time", timeStamp);
        hashMap.put("seen", false);
        hashMap.put("type", "text");
        hashMap.put("mid", timeStamp);
        hashMap.put("status", "none");
        reference.child(timeStamp).setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotification(mess);
            }
        });
    }

    private void sendNotification(String mess) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics/" + userid);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("title", "" + myName);
            jsonObject1.put("body", mess);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("userID", userid);
            jsonObject2.put("type", "sms");

            jsonObject.put("notification", jsonObject1);
            jsonObject.put("data", jsonObject2);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonObject, response -> {

            }, error -> {

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("content-type", "application/json");
                    map.put("authorization", "key=AAAAbfKF8fM:APA91bH9eIBtik6xSvCYMW_Kc0KbI4AXjgYI1TD1u1W0oQNloDhiBaZnyT4J6A8LIAKF3pJchwSKMJD_1u0csmQM5z6ZjqmR5BpPNa4YrYEHEDPO5vNubum-gu36CO8lj0LPV4gMmdHK");
                    return map;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getMess(String myId, String userId, String imgURL) {
        chatContentsList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ContentChats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatContentsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatContents chat = dataSnapshot.getValue(ChatContents.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        chatContentsList.add(chat);
                    }
                    messengerAdapter = new MessengerAdapter(MessengerActivity.this, chatContentsList, imgURL);
                    recyclerView.setAdapter(messengerAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //check status
    private void checkstatus(String status) {
        if (fuser == null) {
            return;
        }
        fuser.getUid();
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
        if (fuser == null) {
            return;
        }
        fuser.getUid();
        checkstatus("offline");
        userRefForSeen.removeEventListener(seenListener);
    }


}