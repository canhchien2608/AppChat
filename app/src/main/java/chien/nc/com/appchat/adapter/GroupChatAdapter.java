package chien.nc.com.appchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import chien.nc.com.appchat.R;
import chien.nc.com.appchat.model.MessageGroupChat;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private Context context;
    private List<MessageGroupChat> list;
    private FirebaseUser fuser;

    public GroupChatAdapter(Context context, List<MessageGroupChat> list) {
        this.context = context;
        this.list = list;
        fuser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public GroupChatAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_groupchat_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_groupchat_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageGroupChat messageGroupChat = list.get(position);
        String message = messageGroupChat.getMessage();
        String senderId = messageGroupChat.getSender();
        holder.txtGroupChatMessText.setText(message);
        setUserName(messageGroupChat, holder);
    }

    private void setUserName(MessageGroupChat messageGroupChat, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("id").equalTo(messageGroupChat.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            holder.txtGroupChatMessName.setText(name);
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

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (list.get(position).getSender().equals(fuser.getUid())) {
            return RIGHT;
        } else {
            return LEFT;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtGroupChatTime, txtGroupChatMessName, txtGroupChatMessText;
        ImageView imgGroupChatMessImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtGroupChatMessName = itemView.findViewById(R.id.txtGroupChatMessName);
            txtGroupChatTime = itemView.findViewById(R.id.txtGroupChatTime);
            txtGroupChatMessText = itemView.findViewById(R.id.txtGroupChatMessText);
            imgGroupChatMessImage = itemView.findViewById(R.id.imgGroupChatMessImage);
        }
    }

}
