package chien.nc.com.appchat.model;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import chien.nc.com.appchat.R;

public class AlertDialog extends AppCompatDialogFragment {
    EditText editText;
    private final String text;
    DatabaseReference reference;
    FirebaseUser fuser;

    public AlertDialog(String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_editstory,null);
        editText = view.findViewById(R.id.edt_editStory);
        editText.setText(text);
        builder.setView(view)
                .setTitle("Chỉnh sửa tiểu sử (tối đa 120 kí tự)")
                .setNegativeButton("Hủy", (dialog, which) -> {

                })
                .setNegativeButton("Lưu", (dialog, which) -> {
                    fuser = FirebaseAuth.getInstance().getCurrentUser();
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String , Object> hashMap = new HashMap<>();
                    hashMap.put("story",editText.getText().toString());
                    reference.updateChildren(hashMap);
                });

        return builder.create();
    }
}
