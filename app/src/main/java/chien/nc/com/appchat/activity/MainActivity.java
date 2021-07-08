package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chien.nc.com.appchat.R;

public class MainActivity extends AppCompatActivity {
    TextView txtDangKy, txtForgetPass;
    EditText edtEmail, edtPassword;
    Button btnLogin;
    FirebaseAuth firebaseAuth;
    Pattern pattern;
    Matcher matcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mapping();
        onClick();
        init();
        
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void onClick() {
        txtDangKy.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        txtForgetPass.setOnClickListener(v -> {
            showForgetPass();
        });
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if (edtPassword.getText().toString().equals("") || edtEmail.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "Không được bỏ trống thông tin ! ", Toast.LENGTH_SHORT).show();
            } else {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Đang đăng nhập");
                progressDialog.show();
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            progressDialog.dismiss();
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                            builder1.setMessage("Email hoặc mật khẩu không chính xác !");
                            builder1.setCancelable(true);
                            builder1.setTitle("Thông báo");
                            builder1.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                    }
                });
            }
        });
    }

    private void showForgetPass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lấy lại mật khẩu");

        LinearLayout layout = new LinearLayout(this);
        EditText emailForgetPass = new EditText(this);
        emailForgetPass.setHint("Email");
        emailForgetPass.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(emailForgetPass);
        layout.setPadding(10, 10, 10, 10);
        builder.setView(layout);
        builder.setPositiveButton("Lấy mật khẩu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailForgetPass.getText().toString().trim();
                if (email.equals("")) {
                    Toast.makeText(MainActivity.this, "Không được bỏ trống email", Toast.LENGTH_SHORT).show();
                } else {
                    String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
                    pattern = Pattern.compile(EMAIL_PATTERN);
                    matcher = pattern.matcher(emailForgetPass.getText().toString());
                    if (!matcher.matches()) {
                        Toast.makeText(MainActivity.this, "Định dạng email chưa đúng !", Toast.LENGTH_SHORT).show();
                    } else {
                        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Vui lòng đợi");
                        progressDialog.show();
                        firebaseAuth = FirebaseAuth.getInstance();
                        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Mời bạn check email để lấy lại mật khẩu", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(MainActivity.this, "Lỗi : " + error, Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                }
                            }
                        });
                    }

                }
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void mapping() {
        this.txtDangKy = findViewById(R.id.txtDangKy);
        this.txtForgetPass = findViewById(R.id.txtForgetPass);
        this.edtEmail = findViewById(R.id.edtEmail);
        this.edtPassword = findViewById(R.id.edtPassword);
        this.btnLogin = findViewById(R.id.btnLogin);
    }

}