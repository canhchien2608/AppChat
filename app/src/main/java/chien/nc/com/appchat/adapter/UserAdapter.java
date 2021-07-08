package chien.nc.com.appchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import chien.nc.com.appchat.activity.Fragment_Friends;
import chien.nc.com.appchat.activity.UserProfile;
import chien.nc.com.appchat.model.User;
import chien.nc.com.appchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends BaseAdapter {
    private List<User> listUser;
    private Context context;
    private int layout;
    public UserAdapter(List<User> listUser, Context context, int layout) {
        this.listUser = listUser;
        this.context = context;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return listUser.size();
    }

    @Override
    public Object getItem(int position) {
        return listUser.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        CircleImageView imageView;
        TextView txtUsername;
        ConstraintLayout overLay;
        RelativeLayout itemSearchUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            viewHolder.imageView = convertView.findViewById(R.id.listSearch_image);
            viewHolder.txtUsername = convertView.findViewById(R.id.listSearch_username);
            viewHolder.overLay = convertView.findViewById(R.id.search_il_overlay);
            viewHolder.itemSearchUser = convertView.findViewById(R.id.itemSearchUser);
            viewHolder.overLay.setVisibility(View.GONE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.txtUsername.setText(listUser.get(position).getName());
        if (!listUser.get(position).getImageURL().equals("default")){
            Glide.with(context).load(listUser.get(position).getImageURL()).into(viewHolder.imageView);
        }else {
            viewHolder.imageView.setImageResource(R.drawable.ic_baseline_person_pin_24);
        }
        viewHolder.itemSearchUser.setOnClickListener(v -> {
            Intent i = new Intent(context, UserProfile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("uid", listUser.get(position).getId());
            context.startActivity(i);
        });
        return convertView;
    }

}
