package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import chien.nc.com.appchat.adapter.FriendAdapter;
import chien.nc.com.appchat.model.User;
import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.VNCharacterUtils;

public class Fragment_Friends extends Fragment {
    ListView listView;
    TextView txtNone;
    EditText edtSearch;
    @SuppressLint("StaticFieldLeak")
    public static FriendAdapter adapter;
    public static List<User> listUser, listFriends;
    List<User> listSearchFriend;
    ImageView imgAdd;
    FirebaseUser fuser;
    DatabaseReference dbFriends, dbUsers;
    String idUser = "";
    ConstraintLayout clOverlay;

    @Override
    public void onResume() {
        super.onResume();
        edtSearch.setText("");
        show();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        this.listView = view.findViewById(R.id.friend_listView);
        this.imgAdd = view.findViewById(R.id.friend_imgViewAdd);
        this.txtNone = view.findViewById(R.id.friend_txtNone);
        this.edtSearch = view.findViewById(R.id.listFriends_search);
        clOverlay = view.findViewById(R.id.il_overlay);
        clOverlay.setVisibility(View.VISIBLE);
        txtNone.setVisibility(View.INVISIBLE);
        setHasOptionsMenu(true);
        listUser = new ArrayList<>();
        listFriends = new ArrayList<>();
        adapter = new FriendAdapter(listUser, getContext(), R.layout.item_list_friend);
        listView.setAdapter(adapter);
        show();
        onClick();
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchFriend(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick() {
        imgAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), SearchAcitvity.class)));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(getContext(), UserProfile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("uid", listUser.get(position).getId());
            startActivity(i);
        });
    }

    private void searchFriend(String input) {
        if (TextUtils.isEmpty(input)) {
            show();
            return;
        }
//        Query query = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friends").child("search")
//                .startAt(input)
//                .endAt(input + "\uf8ff");
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                listFriends.clear();
//                if (snapshot.exists()) {
//                    listView.setVisibility(View.VISIBLE);
//                    txtNone.setVisibility(View.INVISIBLE);
//
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        User user = dataSnapshot.getValue(User.class);
//                        Toast.makeText(getContext(), ""+user.getSearch(), Toast.LENGTH_SHORT).show();
//                        if (!user.getId().equals(fuser.getUid())) {
//                                listFriends.add(user);
//                            }
//
//                        }
//                    adapter.notifyDataSetChanged();
//                } else {
//                    listView.setVisibility(View.INVISIBLE);
//                    txtNone.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Lỗi server !", Toast.LENGTH_SHORT).show();
//            }
//        });
        listSearchFriend = new ArrayList<>();
        listSearchFriend.clear();
        if (listUser.size() > 0) {
            for (int i = 0; i < listUser.size(); i++) {
                User u = listUser.get(i);
                System.out.println(listSearchFriend.add(u));
            }
            listUser.clear();
        }

        for (int i = 0; i < listSearchFriend.size(); i++) {
            User user = listSearchFriend.get(i);
            String a1 = VNCharacterUtils.removeAccent(user.getName()).toLowerCase();
            String b = input.toLowerCase();
            if (a1.contains(b)) {
                if (!user.getId().equals(fuser.getUid())) {
                    listUser.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
//        adapter = new FriendAdapter(listFriends, getContext(), R.layout.item_list_friend, true);
//        listView.setAdapter(adapter);
    }

    private void show() {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        assert fuser != null;
        idUser = fuser.getUid();
        dbFriends = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friends");
        dbFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listFriends.clear();
                if (edtSearch.getText().toString().equals("")) {
                    if (snapshot.exists()) {
                        listView.setVisibility(View.VISIBLE);
                        txtNone.setVisibility(View.INVISIBLE);
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            listFriends.add(user);
                        }
                        if (listFriends.size() > 0) {
                            dbUsers = FirebaseDatabase.getInstance().getReference("Users");
                            dbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    listUser.clear();
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        User u = dataSnapshot.getValue(User.class);
                                        for (int i = 0; i < listFriends.size(); i++) {
                                            if (u.getId().equals(listFriends.get(i).getId())) {
                                                listUser.add(u);
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    } else {
                        listView.setVisibility(View.INVISIBLE);
                        txtNone.setVisibility(View.VISIBLE);
                    }
                    clOverlay.setVisibility(View.GONE);
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi sever !", Toast.LENGTH_SHORT).show();
            }
        });


    }


}
