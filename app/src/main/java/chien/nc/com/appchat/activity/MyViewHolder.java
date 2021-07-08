package chien.nc.com.appchat.activity;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import chien.nc.com.appchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    CircleImageView avatar;
    TextView txtName, txtTime, txtContent, pLikes, pComments;
    ImageView pImage;
    Button btnLike, btnComment;
    ImageButton btnMore;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        avatar = itemView.findViewById(R.id.pUAvatar);
        txtName = itemView.findViewById(R.id.pUName);
        txtTime = itemView.findViewById(R.id.pTime);
        txtContent = itemView.findViewById(R.id.pContent);
        pLikes = itemView.findViewById(R.id.pLikes);
        pComments = itemView.findViewById(R.id.pComments);
        pImage = itemView.findViewById(R.id.pImage);
        btnLike = itemView.findViewById(R.id.btnLike);
        btnComment = itemView.findViewById(R.id.btnComment);
        btnMore = itemView.findViewById(R.id.btnMore);
    }

    public void countLikes(String pId, String myId, DatabaseReference likes) {
        likes.child(pId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalLikes = (int) snapshot.getChildrenCount();
//                    pLikes.setText(totalLikes + " lượt thích");
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(pId);
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("pLikes",totalLikes+"");
                    reference.updateChildren(hashMap);
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(pId);
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("pLikes","0");
                    reference.updateChildren(hashMap);
//                    pLikes.setText("0 lượt thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        likes.child(pId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    int totalLikes = (int) snapshot.getChildrenCount();
//                    pLikes.setText(totalLikes + " lượt thích");
//                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(pId);
//                    HashMap<String,Object> hashMap = new HashMap<>();
//                    hashMap.put("pLikes",totalLikes+"");
//                    reference.updateChildren(hashMap);
//                } else {
//                    pLikes.setText("0 lượt thích");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        likes.child(pId).child(myId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    //btnLike.setBackgroundColor("#FB0404"); //it would be something like this but only set the color of the Heart icon
//                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked, 0, 0, 0);
//                    btnLike.setTextColor(Color.parseColor("#FB0404"));
////                    btnLike.setBackgroundColor(Color.parseColor("#FB0404"));
//
//                } else {
//                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
//                    btnLike.setTextColor(Color.parseColor("#000000"));
////                    btnLike.setBackgroundColor(Color.parseColor("#000000"));
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        likes.child(pId).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked, 0, 0, 0);
                    btnLike.setTextColor(Color.parseColor("#FB0404"));

                } else {
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
                    btnLike.setTextColor(Color.parseColor("#000000"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void countComments(String pId, String myId, DatabaseReference reference) {
        reference.child(pId).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalComments = (int) snapshot.getChildrenCount();
                    pComments.setText(totalComments + " bình luận");
                } else {
                    pComments.setText("0 bình luận");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
