package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import chien.nc.com.appchat.adapter.UserAdapter;
import chien.nc.com.appchat.model.User;
import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.VNCharacterUtils;

public class SearchAcitvity extends AppCompatActivity {
    EditText edtSearch;
    ListView listView;
    TextView txtNone, txtBack;
    List<User> listSearchUser;
    List<User> test;
    UserAdapter userAdapter;
    DatabaseReference reference;
    FirebaseUser fuser;
    String userIdNow = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_acitvity);
        mapping();
        init();
        loadData();
        txtBack.setOnClickListener(v -> finish());
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search2(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void search2(String input) {
        if (TextUtils.isEmpty(input)) {
            listView.setVisibility(View.INVISIBLE);
            txtNone.setVisibility(View.VISIBLE);
            return;
        }
        listSearchUser.clear();
        for (int i = 0; i < test.size(); i++) {
            User user = test.get(i);
            String a1 = VNCharacterUtils.removeAccent(user.getName()).toLowerCase();
            String b = input.toLowerCase();
            if (a1.contains(b)) {
                if (!user.getId().equals(fuser.getUid())) {
                    listSearchUser.add(user);
                }
            }
        }
        for (int i = 0; i < Fragment_Friends.listUser.size(); i++) {
            User userFriend = Fragment_Friends.listUser.get(i);
            for (int j = 0; j < listSearchUser.size(); j++) {
                User searchUser = listSearchUser.get(j);
                if (userFriend.getId().equals(searchUser.getId())) {
                    System.out.println(listSearchUser.remove(j));
                }
            }

        }
        userAdapter.notifyDataSetChanged();
        if (listSearchUser.size() > 0) {
            listView.setVisibility(View.VISIBLE);
            txtNone.setVisibility(View.INVISIBLE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            txtNone.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                test.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    if (!user.getId().equals(userIdNow)) {
                        test.add(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

//        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search").equalTo(edtSearch.getText().toString());
//        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
//                .startAt(input)
//                .endAt(input + "\uf8ff");
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                listSearchUser.clear();
//                if (snapshot.exists()) {
//                    listView.setVisibility(View.VISIBLE);
//                    txtNone.setVisibility(View.INVISIBLE);
//
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Log.d("chiennc","dong1");
//                        User user = dataSnapshot.getValue(User.class);
//                        if (!user.getId().equals(userIdNow)) {
//                                listSearchUser.add(user);
//                            }
//
//                        }
//                    for (int i = 0; i < Fragment_Friends.listFriends.size(); i++) {
//                        User userFriend = Fragment_Friends.listFriends.get(i);
//                        for (int j = 0; j <listSearchUser.size() ; j++) {
//                            User searchUser = listSearchUser.get(j);
//                            if (userFriend.getId().equals(searchUser.getId())){
//                                listSearchUser.remove(j);
//                            }
//                        }
//
//                    }
//                    userAdapter.notifyDataSetChanged();
//                } else {
//                    listView.setVisibility(View.INVISIBLE);
//                    txtNone.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(SearchAcitvity.this, "Lỗi server !", Toast.LENGTH_SHORT).show();
//            }
//        });

    private void init() {
        txtNone.setVisibility(View.INVISIBLE);
        listSearchUser = new ArrayList<>();
        test = new ArrayList<>();
        userAdapter = new UserAdapter(listSearchUser, SearchAcitvity.this, R.layout.item_search_user);
        listView.setAdapter(userAdapter);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        assert fuser != null;
        userIdNow = fuser.getUid();
    }


    private void mapping() {
        this.edtSearch = findViewById(R.id.search_editText);
        this.listView = findViewById(R.id.search_listView);
        this.txtNone = findViewById(R.id.search_txtNone);
        this.txtBack = findViewById(R.id.search_txtBack);
    }

    //check status
//    private void checkstatus(String status){
//        fuser = FirebaseAuth.getInstance().getCurrentUser();
//        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
//        HashMap<String , Object> hashMap = new HashMap<>();
//        hashMap.put("status",status);
//        reference.updateChildren(hashMap);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        checkstatus("online");
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        checkstatus("offline");
//    }
    //check status
    private void checkstatus(String status){
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
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