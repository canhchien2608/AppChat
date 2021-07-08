package chien.nc.com.appchat.activity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import chien.nc.com.appchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class RequstFriendHolder extends RecyclerView.ViewHolder {
    CircleImageView avatar;
    TextView name,notify;
    LinearLayout itemListRequest;
    AppCompatButton btnAccept,btnCancel;
    public RequstFriendHolder(@NonNull View itemView) {
        super(itemView);
        avatar = itemView.findViewById(R.id.itemListRequestAvatar);
        name = itemView.findViewById(R.id.itemListRequestName);
        itemListRequest = itemView.findViewById(R.id.itemListRequest);
        notify = itemView.findViewById(R.id.itemListRequestNotify);
        btnAccept = itemView.findViewById(R.id.itemListRequestAccept);
        btnCancel = itemView.findViewById(R.id.itemListRequestCancel);
    }
}
