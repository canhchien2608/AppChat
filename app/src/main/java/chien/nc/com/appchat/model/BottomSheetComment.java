package chien.nc.com.appchat.model;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.activity.CommentViewHolder;
import chien.nc.com.appchat.activity.UserProfile;
import de.hdodenhof.circleimageview.CircleImageView;

public class BottomSheetComment extends BottomSheetDialogFragment {
    int layout;
    String myId, pId, totalComments;
    DatabaseReference reference1, reference2;
    FirebaseRecyclerAdapter<Comment, CommentViewHolder> commentAdapter;
    FirebaseRecyclerOptions<Comment> commentOptions;

    public BottomSheetComment(int layout, String myId, String pId) {
        this.layout = layout;
        this.myId = myId;
        this.pId = pId;
        reference1 = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        reference2 = FirebaseDatabase.getInstance().getReference("Posts").child(pId).child("Comments");
    }

    public BottomSheetComment() {
    }


    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layout, container, false);
        TextView totalComment = view.findViewById(R.id.totalComment);
        ImageView btnSend = view.findViewById(R.id.cmt_btnSend);
        EditText edtContent = view.findViewById(R.id.cmt_edtContent);
        CircleImageView civAvatar = view.findViewById(R.id.cmt_avatar);
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    totalComments = String.valueOf(snapshot.getChildrenCount());
                    totalComment.setText(totalComments + " bình luận");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                if (u == null) {
                    return;
                }
                if (!u.getImageURL().equals("default")) {
                    if (getActivity() == null) {
                        return;
                    }
                    try {
                        Glide.with(getActivity()).load(u.getImageURL()).into(civAvatar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    civAvatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        RecyclerView rvListComment = view.findViewById(R.id.cmt_rvListCmt);
        btnSend.setOnClickListener(v1 -> {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("đang tải lên...");
            String comment = edtContent.getText().toString().trim();
            if (TextUtils.isEmpty(edtContent.getText())) {
                Toast.makeText(getActivity(), "không được để trống nội dung !", Toast.LENGTH_SHORT).show();
                return;
            }
            String timeStamp = String.valueOf(System.currentTimeMillis());
            DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Posts").child(pId).child("Comments");
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("cId", timeStamp);
            hashMap.put("comment", comment);
            hashMap.put("timestamp", timeStamp);
            hashMap.put("uid", myId);
            hashMap.put("pid", pId);
            progressDialog.show();
            dr.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        edtContent.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Có lỗi xảy ra !", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });

        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvListComment.setLayoutManager(layoutManager);
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Posts").child(pId).child("Comments");
        commentOptions = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(reference2, Comment.class).build();
        commentAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(commentOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
                String uid = model.getUid();
                String cid = model.getcId();
                String comment = model.getComment();
                String time = model.getTimestamp();
                String pid = model.getPid();

                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTimeInMillis(Long.parseLong(time));
                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                String pTimeAgo = caculatorTimeAgo(pTime);
                holder.time.setText(pTimeAgo);
                holder.content.setText(comment);
                holder.name.setOnClickListener(v -> {
                    if (myId == null) {
                        return;
                    }
                    if (!uid.equals(myId)) {
                        Intent intent = new Intent(getActivity(), UserProfile.class);
                        intent.putExtra("uid", uid);
                        getActivity().startActivity(intent);
                    }
                });

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User u = snapshot.getValue(User.class);
                        assert u != null;
                        String uAvatar = u.getImageURL();
                        holder.name.setText(u.getName());
                        if (!uAvatar.equals("default")) {
                            if (getActivity() != null) {
                                try {
                                    Glide.with(getActivity()).load(uAvatar).into(holder.avatar);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } else {
                            holder.avatar.setImageResource(R.drawable.ic_baseline_person_pin_24);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.layoutRowComment.setOnLongClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(getActivity(), holder.layoutRowComment, Gravity.END);
                    if (model.getUid().equals(myId)) {
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
                    }
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 0) {
                            ProgressDialog progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setMessage("Đang xóa . . .");
                            progressDialog.show();
                            DatabaseReference query = FirebaseDatabase.getInstance().getReference("Posts").child(pid);
                            query.child("Comments").child(cid).removeValue();
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Đã xóa", Toast.LENGTH_SHORT).show();

                        }
                        return false;
                    });
                    popupMenu.show();

                    return false;
                });
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment, parent, false);
                return new CommentViewHolder(view1);
            }
        };
        commentAdapter.startListening();
        rvListComment.setAdapter(commentAdapter);


        return view;
    }

    private String caculatorTimeAgo(String pTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        try {
            if (sdf.parse(pTime) == null) {
                return "";
            }
            long time = sdf.parse(pTime).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        View view = getView();
        view.post(() -> {
            View parent = (View) view.getParent();
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) (parent).getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();
            BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;
            bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());

            parent.setBackgroundColor(Color.TRANSPARENT);
        });
    }
}
