package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chien.nc.com.appchat.model.User;
import chien.nc.com.appchat.model.VNCharacterUtils;
import chien.nc.com.appchat.R;

public class RegisterActivity extends AppCompatActivity {
    EditText edtName, edtUsername, edtEmail, edtPassword;
    Button btnRegister;
    TextView txtDangNhap;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    AlertDialog.Builder builder1;
    List<String> checkEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        builder1 = new AlertDialog.Builder(RegisterActivity.this);
        mapping();
        checkEmail = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkEmail.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    checkEmail.add(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        onClick();
        firebaseAuth = FirebaseAuth.getInstance();

    }

    private void onClick() {
        txtDangNhap.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String username = edtUsername.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if (edtPassword.getText().length() < 8 || TextUtils.isEmpty(edtPassword.getText().toString()) || TextUtils.isEmpty(edtEmail.getText().toString()) || TextUtils.isEmpty(edtName.getText().toString()) || TextUtils.isEmpty(edtUsername.getText().toString())) {
                builder1.setMessage("Bạn cần đảm bảo :\n - Nhập đầy đủ thông tin\n - Mật khẩu có ít nhất 8 kí tự\n - Email chưa có người đăng ký ");
                builder1.setCancelable(true);
                builder1.setTitle("Đăng ký không thành công");
                builder1.setPositiveButton(
                        "OK",
                        (dialog, id) -> dialog.cancel());
                AlertDialog alert11 = builder1.create();
                alert11.show();
            } else {
                Pattern pattern;
                Matcher matcher;
                String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
                pattern = Pattern.compile(EMAIL_PATTERN);
                matcher = pattern.matcher(edtEmail.getText().toString());
                if (!matcher.matches()) {
                    builder1.setMessage("Định dạng email chưa đúng !");
                    builder1.setCancelable(true);
                    builder1.setTitle("Đăng ký không thành công");
                    builder1.setPositiveButton(
                            "OK",
                            (dialog, id) -> dialog.cancel());
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else {
                    ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
                    progressDialog.setMessage("Đang đăng ký");
                    progressDialog.show();
                    firebaseAuth.fetchSignInMethodsForEmail(edtEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.getResult().getSignInMethods().isEmpty()) {
                                        register(name, username, email, password);
                                        progressDialog.dismiss();
                                    } else {
                                        builder1.setMessage("Email đã tồn tại !");
                                        builder1.setCancelable(true);
                                        builder1.setTitle("Đăng ký không thành công");
                                        builder1.setPositiveButton(
                                                "OK",
                                                (dialog, id) -> dialog.cancel());
                                        AlertDialog alert11 = builder1.create();
                                        alert11.show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                }
//
            }
        });
    }

    private void mapping() {
        this.txtDangNhap = findViewById(R.id.txtDangNhap);
        this.edtName = findViewById(R.id.editTextName);
        this.edtEmail = findViewById(R.id.editTextEmail);
        this.edtPassword = findViewById(R.id.editTextPassword);
        this.edtUsername = findViewById(R.id.editTextUsername);
        this.btnRegister = findViewById(R.id.btnRegister);
    }

    private void register(String name, String username, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                    String time = String.valueOf(System.currentTimeMillis());
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("name", name);
                    hashMap.put("username", username);
                    hashMap.put("imageURL", "default");
                    hashMap.put("status", "offline");
                    hashMap.put("imageCover", "default");
                    hashMap.put("story", "Hãy quay về hướng mặt trời, và bạn sẽ không thấy bóng tối");
                    hashMap.put("lastOnline", time);

                    databaseReference.setValue(hashMap).addOnCompleteListener(task1 -> {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công ! Mời bạn đăng nhập ", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });

                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký không thành công ! ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}