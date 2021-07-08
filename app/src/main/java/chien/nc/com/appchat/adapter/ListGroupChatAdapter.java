package chien.nc.com.appchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.activity.GroupMessActivity;
import chien.nc.com.appchat.model.GroupChat;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListGroupChatAdapter extends RecyclerView.Adapter<ListGroupChatAdapter.ViewHolder> {
    private Context context;
    private List<GroupChat> list;

    public ListGroupChatAdapter(Context context, List<GroupChat> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupChat groupChat = list.get(position);
        holder.name.setText(groupChat.getGroupname());

        try{
            if (groupChat.getAvatar().equals("default")){
                holder.avatar.setImageResource(R.drawable.groupchat);
            }else {
                Glide.with(context).load(groupChat.getAvatar()).into(holder.avatar);
            }
        }catch (Exception e){
            holder.avatar.setImageResource(R.drawable.loading);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, GroupMessActivity.class);
            i.putExtra("groupId",groupChat.getGroupid());
            context.startActivity(i);
        });
        loadLastMess(groupChat,holder);
    }

    private void loadLastMess(GroupChat groupChat, ViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupChat.getGroupid()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String message = ds.child("message").getValue()+"";
                            String idSender = ds.child("sender").getValue()+"";
                            String mTime = ds.child("time").getValue()+"";
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            calendar.setTimeInMillis(Long.parseLong(mTime));
                            String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            String mTimeAgo = caculatorTimeAgo(time);
                            holder.lastTime.setText(mTimeAgo);
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                    reference.orderByChild("id").equalTo(idSender).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds1:snapshot.getChildren()){
                                                String name = ds1.child("name").getValue()+"";
                                                holder.lastMess.setText(name+" : "+message);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView avatar;
        private TextView name;
        private TextView lastMess,lastTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.itemListGroupChatAvatar);
            name = itemView.findViewById(R.id.itemListGroupChatName);
            lastMess = itemView.findViewById(R.id.itemListGroupChatLastMess);
            lastTime = itemView.findViewById(R.id.itemListGroupChatTime);
        }
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
}
