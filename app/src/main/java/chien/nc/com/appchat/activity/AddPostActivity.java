package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.User;

public class AddPostActivity extends AppCompatActivity {
    TextView btnCancel, btnPost, btnAddImg;
    EditText edtContent;
    ImageView imgImage;
    ImageButton btnCancelImage;
    private static final int CAMERA_REQ_CODE = 1;
    private static final int GALLERY_REQ_CODE = 2;
    private static final int IMAGE_PICK_CAMERA = 3;
    private static final int IMAGE_PICK_GALLERY = 4;

    String[] cameraPermissions;
    String[] galleryPermissions;

    Uri img_Uri = null;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    String id, name;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        mapping();
        init();
        onClick();
    }

    private void init() {
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        galleryPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        pd = new ProgressDialog(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        id = firebaseUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                name = user.getName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    private void onClick() {
        btnCancel.setOnClickListener(v -> {
            finish();
        });

        btnPost.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtContent.getText()) && img_Uri==null) {
                Toast.makeText(this, "Nội dung đang để trống !", Toast.LENGTH_SHORT).show();
                return;
            }
            String content = edtContent.getText().toString().trim();
            if (img_Uri == null) {
                uploadPost(content, "noImage");
            } else {
                uploadPost(content, String.valueOf(img_Uri));
            }
        });
        btnAddImg.setOnClickListener(v -> {
            showImagePick();
        });
        btnCancelImage.setOnClickListener(v -> {
            imgImage.setImageURI(null);
            btnCancelImage.setVisibility(View.GONE);
            img_Uri = null;
        });
    }

    private void uploadPost(String content, String image) {
        pd.setMessage("Đang tải lên ...");
        pd.show();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_in_" + timeStamp;

        if (!image.equals("noImage")) {
            StorageReference sr = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            sr.putFile(Uri.parse(image))
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("uId", id);
                            hashMap.put("uName", name);
                            hashMap.put("pId", timeStamp);
                            hashMap.put("pContent", content);
                            hashMap.put("pImage", downloadUri);
                            hashMap.put("pTime", timeStamp);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");
                            DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Posts");
                            dr.child(timeStamp).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        pd.dismiss();
                                        Toast.makeText(this, "Tải bài viết thành công !", Toast.LENGTH_SHORT).show();
                                        edtContent.setText("");
                                        imgImage.setImageURI(null);
                                        img_Uri = null;
                                        btnCancelImage.setVisibility(View.GONE);

                                    })
                                    .addOnFailureListener(e -> {
                                        pd.dismiss();
                                        Toast.makeText(this, "Lỗi tải lên", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(this, "Lỗi : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uId", id);
            hashMap.put("uName", name);
            hashMap.put("pId", timeStamp);
            hashMap.put("pContent", content);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");
            DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Posts");
            dr.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        pd.dismiss();
                        Toast.makeText(this, "Tải bài viết thành công !", Toast.LENGTH_SHORT).show();
                        edtContent.setText("");
                        imgImage.setImageURI(null);
                        img_Uri = null;
                    })
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(this, "Lỗi tải lên", Toast.LENGTH_SHORT).show();
                    });

        }
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

    private void mapping() {
        this.btnAddImg = findViewById(R.id.addPost_btnAddImage);
        this.btnCancel = findViewById(R.id.addPost_btnCancel);
        this.btnPost = findViewById(R.id.addPost_btnPost);
        this.edtContent = findViewById(R.id.addPost_edtContent);
        this.imgImage = findViewById(R.id.addPost_image);
        this.btnCancelImage = findViewById(R.id.addPost_btnCancelImage);
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
                imgImage.setImageURI(img_Uri);
                btnCancelImage.setVisibility(View.VISIBLE);
            } else if (requestCode == IMAGE_PICK_GALLERY) {
                img_Uri = data.getData();
                imgImage.setImageURI(img_Uri);
                btnCancelImage.setVisibility(View.VISIBLE);
            }
        }
    }


    //check status
    private void checkstatus(String status) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
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
    protected void onDestroy() {
        super.onDestroy();
        checkstatus("offline");
    }
}