package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import chien.nc.com.appchat.R;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class ShowImageActivity extends AppCompatActivity {
    ImageView img, download, back;
    TextView time;
    FirebaseUser fuser;
    DatabaseReference reference;
    Bitmap bitmap;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        this.img = findViewById(R.id.showImage);
        this.download = findViewById(R.id.showImageDownload);
        this.time = findViewById(R.id.showImageTime);
        this.back = findViewById(R.id.showImageBack);
        Intent i = getIntent();
        String urlImage = i.getStringExtra("urlImage");
        String timee = i.getStringExtra("time");
        Glide.with(this).load(urlImage).into(img);
        time.setText(timee);
        back.setOnClickListener(v -> finish());



        download.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    String [] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,};
                    requestPermissions(permission,WRITE_EXTERNAL_STORAGE_CODE);
                }else {
                    saveImage();
                }
            }
        });
//        download.setOnClickListener(v->{
//            Uri uri = Uri.parse(urlImage);
//            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//            DownloadManager.Request request = new DownloadManager.Request(uri);
//            request.setVisibleInDownloadsUi(true);
//            String time = System.currentTimeMillis()+"";
//            request.setDestinationInExternalFilesDir(this,DIRECTORY_DOWNLOADS,time+".jpg");
//            downloadManager.enqueue(request);
//        });
    }

    private void saveImage() {
        bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
        String time = System.currentTimeMillis()+"";
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path+"/DCIM");
        dir.mkdirs();
        String imageName = time+".PNG";
        File file = new File(dir,imageName);
        OutputStream os ;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
            os.flush();
            os.close();
            Toast.makeText(this, "Ảnh đã được lưu về máy !", Toast.LENGTH_SHORT).show();
        }catch (Exception e ){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case WRITE_EXTERNAL_STORAGE_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    saveImage();
                }else {
                    Toast.makeText(this, "Bạn cần cấp quyền để lưu ảnh về máy !", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
