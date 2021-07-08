package chien.nc.com.appchat.activity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import chien.nc.com.appchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView avatar;
    public TextView name,content,time;
    public LinearLayout layoutRowComment;
    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        avatar = itemView.findViewById(R.id.listCommentAvatar);
        name = itemView.findViewById(R.id.listCommentName);
        content = itemView.findViewById(R.id.listCommentContent);
        time = itemView.findViewById(R.id.listCommentTime);
        layoutRowComment = itemView.findViewById(R.id.layoutRowComment);
    }
}
