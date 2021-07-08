package chien.nc.com.appchat.model;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import java.util.HashMap;

import chien.nc.com.appchat.R;

public class BottomSheetMess extends BottomSheetDialogFragment {
    int layout;
    boolean checkStatusUser;
    String userid;
    FirebaseUser firebaseUser;
    public BottomSheetMess(int layout,String userid) {
        this.layout = layout;
//        this.check = check;
        this.userid = userid;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        checkStatusUser = false;
    }

    public BottomSheetMess() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layout,container,false);
        Button btnDelete = view.findViewById(R.id.btnBSDeleteMess);
        Button btnBlock = view.findViewById(R.id.btnBSBlock);
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        reference1.child(firebaseUser.getUid()).child("block").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    checkStatusUser = true;
                }else {
                    checkStatusUser = false;
                }
                if (checkStatusUser){
                    btnBlock.setText("Bỏ chặn người dùng này");
                }else {
                    btnBlock.setText("Chặn tin nhắn từ người này");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btnDelete.setOnClickListener(v->{

        });
        btnBlock.setOnClickListener(v->{
            if (checkStatusUser){
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseUser.getUid()).child("block").child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            try {
                                Toast.makeText(getContext(), "Đã bỏ chặn tin nhắn từ người này!", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }else {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("id",userid);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseUser.getUid()).child("block").child(userid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            try {
                                Toast.makeText(getContext(), "Đã chặn tin nhắn từ người này!", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        return view;
    }
}
