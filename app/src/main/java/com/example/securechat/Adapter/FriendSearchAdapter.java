package com.example.securechat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.securechat.ActivityUi.RequestProfile;
import com.example.securechat.R;
import com.example.securechat.User;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendSearchAdapter extends RecyclerView.Adapter<FriendSearchAdapter.ViewHolder> {
    public Context mContext;
    public List<User> mUser;
    private boolean ischat;
    DatabaseReference mFriendDb;

    public FriendSearchAdapter(Context mContext, List<User> mUser, boolean ischat) {
        this.mContext = mContext;
        this.mUser = mUser;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public FriendSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_items, parent, false);
        return new FriendSearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendSearchAdapter.ViewHolder holder, int position) {
        final User user = mUser.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURl().equals("default")) {
            holder.profile_image.setImageResource(R.drawable.ic_user_profile);
        } else {
            Glide.with(mContext).load(user.getImageURl()).into(holder.profile_image);
        }
        if (ischat) {
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, RequestProfile.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
                ((Activity) mContext).finish();
            }

        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public CircleImageView profile_image;
        public CircleImageView img_on, img_off;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.chat_user);
            profile_image = itemView.findViewById(R.id.chat_profileImg);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
        }
    }
}
