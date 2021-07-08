package chien.nc.com.appchat.activity;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import chien.nc.com.appchat.R;

public class AddPartHolder extends RecyclerView.ViewHolder {
    ImageView avatar;
    TextView name;
    CheckBox cb;
    public AddPartHolder(@NonNull View itemView) {
        super(itemView);
        avatar = itemView.findViewById(R.id.rowAddPartAvatar);
        name = itemView.findViewById(R.id.rowAddPartName);
        cb = itemView.findViewById(R.id.rowAddPartCb);
    }
}
