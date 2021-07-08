package chien.nc.com.appchat.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import chien.nc.com.appchat.activity.ShowImageActivity;
import chien.nc.com.appchat.model.ChatContents;
import chien.nc.com.appchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessengerAdapter extends RecyclerView.Adapter<MessengerAdapter.ViewHolder> {
    public static final int MESS_LEFT = 0;
    public static final int MESS_RIGHT = 1;
    private final Context context;
    private final List<ChatContents> list;
    private final String imgURL;
    FirebaseUser firebaseUser;
    boolean checkClick = true;

    public MessengerAdapter(Context context, List<ChatContents> list, String imgURL) {
        this.context = context;
        this.list = list;
        this.imgURL = imgURL;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MessengerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MESS_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatContents chatContents = list.get(position);
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(chatContents.getTime()));
        String mTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (chatContents.getType().equals("text")) {
            if (chatContents.getStatus().equals("deleted")) {
                holder.messenger.setTypeface(holder.messenger.getTypeface(), Typeface.ITALIC);
                holder.messenger.setTextColor(Color.parseColor("#D3CECE"));
            }
            holder.messenger.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.GONE);
            holder.messenger.setText(chatContents.getMess());
        } else if (chatContents.getType().equals("image")) {
            holder.messenger.setVisibility(View.GONE);
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(context).load(chatContents.getMess()).into(holder.image);
        }


        if (!imgURL.equals("default")) {
            Glide.with(context).load(imgURL).into(holder.img_profile1);
        } else {
            holder.img_profile1.setImageResource(R.drawable.ic_baseline_person_pin_24);
        }
        if (!imgURL.equals("default")) {
            Glide.with(context).load(imgURL).into(holder.seen);
        } else {
            holder.seen.setImageResource(R.drawable.ic_baseline_person_pin_24);
        }
        if (position == list.size() - 1) {
            if (chatContents.getSender().equals(firebaseUser.getUid())) {
                if (chatContents.isSeen()) {
                    holder.seen.setVisibility(View.VISIBLE);
                } else {
                    holder.seen.setVisibility(View.GONE);
                }
            }

        } else {
            holder.seen.setVisibility(View.GONE);
        }
        holder.messenger.setOnClickListener(v -> {
            if (checkClick) {
                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(mTime);
                checkClick = false;
            } else {
                holder.time.setVisibility(View.GONE);
                checkClick = true;
            }

        });
        holder.image.setOnClickListener(v -> {
            Intent i = new Intent(context, ShowImageActivity.class);
            i.putExtra("urlImage", chatContents.getMess());
            i.putExtra("time", mTime);
            context.startActivity(i);
        });
        holder.image.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.image, Gravity.END);
            if (chatContents.getSender().equals(firebaseUser.getUid())) {
                if (chatContents.getStatus().equals("deleted")) {
                    return false;
                }
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Thu hồi");
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Bạn có chắc chắn thu hồi tin nhắn này ?");
                        builder.setPositiveButton("Có", (dialog, which) -> {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(list.get(position).getMess());
                            storageReference.delete().addOnSuccessListener(aVoid -> {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("type", "text");
                                hashMap.put("mess", "Tin nhắn đã bị thu hồi");
                                hashMap.put("status", "deleted");
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ContentChats").child(list.get(position).getMid());
                                reference.updateChildren(hashMap);
                            })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "xảy ra lỗi !", Toast.LENGTH_SHORT).show();
                                    });

                        });
                        builder.setNegativeButton("Không", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    }
                    return false;
                });
            }

            return false;
        });
        holder.messenger.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.messenger, Gravity.END);
            if (chatContents.getSender().equals(firebaseUser.getUid())) {
                if (chatContents.getStatus().equals("deleted")) {
                    return false;
                }
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Thu hồi");
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Bạn có chắc chắn thu hồi tin nhắn này ?");
                        builder.setPositiveButton("Có", (dialog, which) -> {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("mess", "Tin nhắn đã bị thu hồi");
                            hashMap.put("status", "deleted");
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ContentChats").child(list.get(position).getMid());
                            reference.updateChildren(hashMap);
                        });
                        builder.setNegativeButton("Không", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    }
                    return false;
                });
            }

            return false;
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messenger, time;
        CircleImageView img_profile1, seen;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messenger = itemView.findViewById(R.id.item_content_mess);
            image = itemView.findViewById(R.id.item_content_mess_image);
            time = itemView.findViewById(R.id.txtChatTime);
            img_profile1 = itemView.findViewById(R.id.item_profile_image_leftright);
            seen = itemView.findViewById(R.id.seenMess);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (list.get(position).getSender().equals(firebaseUser.getUid())) {
            return MESS_RIGHT;
        } else {
            return MESS_LEFT;
        }
    }
}
