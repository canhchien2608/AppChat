package chien.nc.com.appchat.model;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.adapter.ListMemberAdapter;

public class BottomShetListMember extends BottomSheetDialogFragment {
    int layout;
    String gid;
    List<String> list;
    ListMemberAdapter adapter;
    public BottomShetListMember(int layout, String gid) {
        this.layout = layout;
        this.gid = gid;
    }

    public BottomShetListMember() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_list_member,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.bottomSheetListMemberRV);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(gid);
        reference.child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String a = ds.child("uid").getValue()+"";
                    list.add(a);
                }
                adapter = new ListMemberAdapter(getContext(),list,gid);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }
    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }
}
