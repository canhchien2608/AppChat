package chien.nc.com.appchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import chien.nc.com.appchat.model.User;
import chien.nc.com.appchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends BaseAdapter {
    private final List<User> list;
    private final Context context;
    private final int layout;

    public FriendAdapter(List<User> list, Context context, int layout) {
        this.list = list;
        this.context = context;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        CircleImageView friend_Img;
        TextView friend_txtName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            viewHolder.friend_Img = convertView.findViewById(R.id.listFriends_image);
            viewHolder.friend_txtName = convertView.findViewById(R.id.listFriends_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(list.size()>0){
            User user = list.get(position);
            viewHolder.friend_txtName.setText(list.get(position).getName());
            if (!user.getImageURL().equals("default")){
                Glide.with(context).load(user.getImageURL()).into(viewHolder.friend_Img);
            }else {
                viewHolder.friend_Img.setImageResource(R.drawable.ic_baseline_person_pin_24);
            }
        }



        return convertView;
    }
}
