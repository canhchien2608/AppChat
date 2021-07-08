package chien.nc.com.appchat.model;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.activity.AddParticipantsActivity;

public class BottomSheetMoreGroupChat extends BottomSheetDialogFragment {
    int layout;
    String gid;
    FirebaseUser firebaseUser;
    private static final int CAMERA_REQ_CODE = 1;
    private static final int GALLERY_REQ_CODE = 2;
    private static final int IMAGE_PICK_CAMERA = 3;
    private static final int IMAGE_PICK_GALLERY = 4;
    String[] cameraPermissions;
    String[] galleryPermissions;
    Uri img_Uri = null;
    String gavatar;
    String groupname;
    DatabaseReference referenceGetName;
    public BottomSheetMoreGroupChat(int layout, String gid) {
        this.layout = layout;
        this.gid = gid;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        galleryPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    public BottomSheetMoreGroupChat() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layout, container, false);
        ImageView avatar;
        TextView name, showMember, addMember, leave, changeAvatar, deleteAvatar,changeName;
        changeName = view.findViewById(R.id.bottomSheetGroupChatChangeName);
        avatar = view.findViewById(R.id.bottomSheetGroupChatAvatar);
        name = view.findViewById(R.id.bottomSheetGroupChatName);
        showMember = view.findViewById(R.id.bottomSheetGroupChatShowMember);
        addMember = view.findViewById(R.id.bottomSheetGroupChatAddMember);
        leave = view.findViewById(R.id.bottomSheetGroupChatLeave);
        changeAvatar = view.findViewById(R.id.bottomSheetGroupChatChangeAvatar);
        deleteAvatar = view.findViewById(R.id.bottomSheetGroupChatDeleteAvatar);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupid").equalTo(gid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    gavatar = ds.child("avatar").getValue() + "";
                    groupname = ds.child("groupname").getValue() + "";
                    name.setText(groupname);
                    try {
                        if (gavatar.equals("default")) {
                            avatar.setImageResource(R.drawable.groupchat);
                        } else {
                            Glide.with(getContext()).load(gavatar).into(avatar);
                        }
                    } catch (Exception e) {
                        avatar.setImageResource(R.drawable.groupchat);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        changeName.setOnClickListener(v->{
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setTitle("Chỉnh sửa tên nhóm");
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setPadding(15, 15, 15, 15);
            EditText edtName = new EditText(getContext());
            edtName.setText(groupname);
            linearLayout.addView(edtName);
            builder.setView(linearLayout);
            builder.setPositiveButton("Lưu", (dialog, which) -> {
                if (!TextUtils.isEmpty(edtName.getText())) {
                    ProgressDialog progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage("Đang thực hiện");
                    progressDialog.show();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("groupname", edtName.getText()+"");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                    ref.child(gid).updateChildren(hashMap).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getContext(), "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                        }
                    });
                    progressDialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Không được bỏ trống !", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.create().show();
        });
        addMember.setOnClickListener(v -> {
            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Groups").child(gid);
            reference2.child("Participants").child(firebaseUser.getUid()).child("role").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role1 = snapshot.getValue() + "";
                    if (role1.equals("admin") || role1.equals("creator")) {
                        Intent i = new Intent(getContext(), AddParticipantsActivity.class);
                        i.putExtra("groupid", gid);
                        startActivity(i);
                    } else {
                        Toast.makeText(getContext(), "Chức năng chỉ dành cho quản trị viên", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });
        deleteAvatar.setOnClickListener(v->{
            if (gavatar.equals("default")){
                return;
            }
            ProgressDialog pd;
            pd = new ProgressDialog(getContext());
            pd.setMessage("Đang thực hiện ...");
            pd.show();
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(gavatar);
            storageReference.delete().addOnSuccessListener(aVoid -> {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("avatar", "default");
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(gid).updateChildren(hashMap).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getContext(), "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                    }
                });
                pd.dismiss();
            }).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(getContext(), "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
            });

        });
        showMember.setOnClickListener(v -> {
            BottomShetListMember bottomShetListMember = new BottomShetListMember(R.layout.bottom_sheet_list_member, gid);
            bottomShetListMember.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "");
        });
        leave.setOnClickListener(v -> {
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups").child(gid);
            reference1.child("Participants").child(firebaseUser.getUid()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã rời nhóm", Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).finish();
                } else {
                    Toast.makeText(getContext(), "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                }
            });
        });
        changeAvatar.setOnClickListener(v -> {
            String[] options = {"Máy ảnh", "Thư viện"};
            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        });
        return view;
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY);
    }

    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), galleryPermissions, GALLERY_REQ_CODE);
    }

    private boolean checkGalleryPermission() {
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Chọn tạm thời");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Mô tả tạm thời");
        img_Uri = Objects.requireNonNull(getActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, img_Uri);
        startActivityForResult(i, IMAGE_PICK_CAMERA);
    }

    private boolean checkCameraPermission() {
        return (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED)) &&
                (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED));
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), cameraPermissions, CAMERA_REQ_CODE);
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
                    Toast.makeText(getContext(), "Bạn cần cho phép truy cập!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == GALLERY_REQ_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(getContext(), "Bạn cần cho phép truy cập!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA) {
                upLoadAvatar(String.valueOf(img_Uri));
            } else if (requestCode == IMAGE_PICK_GALLERY) {
                if (data!=null){
                    img_Uri = data.getData();
                    upLoadAvatar(String.valueOf(img_Uri));
                }
            }
        }
    }

    private void upLoadAvatar(String image) {
        ProgressDialog pd;
        pd = new ProgressDialog(getContext());
        pd.setMessage("Đang tải lên ...");
        pd.show();
        String timeStamp = System.currentTimeMillis() + "";
        String filePathAndName = "GroupChatAvatar/" + "post_in_" + timeStamp;
        StorageReference sr = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        sr.putFile(Uri.parse(image))
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();
                    if (uriTask.isSuccessful()) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("avatar", downloadUri);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(gid).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    pd.dismiss();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(getContext(), "Lỗi : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }
}
