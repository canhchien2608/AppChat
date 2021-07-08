package chien.nc.com.appchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.adapter.ListMessAdapter;
import chien.nc.com.appchat.adapter.ViewPagerAdapter;
import chien.nc.com.appchat.model.ChatContents;
import chien.nc.com.appchat.model.User;

public class HomeActivity extends AppCompatActivity {
    public static BottomNavigationView bottomNavigationView;
    DatabaseReference reference;
    FirebaseUser fuser;
    ViewPager viewPager;
    List<User> listMess;
    List<String> usersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        init();
        FirebaseMessaging.getInstance().subscribeToTopic(fuser.getUid());
        setUpViewPager();
        checkNewMess();

    }

    private void checkNewMess() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ContentChats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatContents chat = dataSnapshot.getValue(ChatContents.class);
                    if (chat==null){
                        return;
                    }
                    if (chat.getSender().equals(fuser.getUid())) {
                        usersList.add(chat.getReceiver());
                    }
                    if (chat.getReceiver().equals(fuser.getUid())) {
                        usersList.add(chat.getSender());
                    }
                }
                showListChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showListChat() {
        listMess = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listMess.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User u = dataSnapshot.getValue(User.class);
                    for (String id : usersList) {
                        assert u != null;
                        if (u.getId().equals(id)) {
                            if (listMess.size() > 0) {
                                int j = 0;
                                for (int i = 0; i < listMess.size(); i++) {
                                    User user = listMess.get(i);
                                    if (!u.getId().equals(user.getId())) {
                                        j++;
                                    }
                                }
                                if (j == listMess.size()) {
                                    listMess.add(u);
                                    j = 0;
                                }
                            } else {
                                listMess.add(u);
                            }

                        }
                    }

                }
                for (int i = 0; i <listMess.size() ; i++) {
                    changedNewMess(listMess.get(i).getId());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void changedNewMess(String userid) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("ContentChats");
        reference1.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatContents chat = dataSnapshot.getValue(ChatContents.class);
                    if (chat==null){
                        return;
                    }
                    if (fuser==null){
                        return;
                    }
                    if (chat.getSender().equals(userid) && chat.getReceiver().equals(fuser.getUid())) {
                        if (chat.isSeen()) {
                            bottomNavigationView.getOrCreateBadge(R.id.menu_mess).setAlpha(0);
                        }else {
                            bottomNavigationView.getOrCreateBadge(R.id.menu_mess).setAlpha(1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void init() {
        this.viewPager = findViewById(R.id.viewPager);
        this.bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_friend:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.menu_mess:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.menu_account:
                    viewPager.setCurrentItem(3);
                    break;
                default:
                    viewPager.setCurrentItem(0);
                    break;
            }
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectFragment).commit();
            return true;
        });
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_Home()).commit();
    }
private void setUpViewPager(){
    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    viewPager.setAdapter(viewPagerAdapter);

    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position){
                case 0:
                    bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                    break;
                case 1:
                    bottomNavigationView.getMenu().findItem(R.id.menu_friend).setChecked(true);
                    break;
                case 2:
                    bottomNavigationView.getMenu().findItem(R.id.menu_mess).setChecked(true);
                    break;
                case 3:
                    bottomNavigationView.getMenu().findItem(R.id.menu_account).setChecked(true);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    });
}

    //check status
    private void checkstatus(String status) {
        if(fuser==null){
            return;
        }
        fuser.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkstatus("online");
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
}