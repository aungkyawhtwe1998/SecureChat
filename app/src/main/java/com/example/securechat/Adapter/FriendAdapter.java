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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    public Context mContext;
    public List<String> mfriend;
    private DatabaseReference userdb;
    private DatabaseReference friendb;
    private FirebaseAuth mAth;
    private String fuser;

    public FriendAdapter(Context mContext, List<String> mfriend) {
        this.mContext = mContext;
        this.mfriend = mfriend;
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_items, parent, false);
        return new FriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {


        fuser = FirebaseAuth.getInstance().getUid();
        //friendb=FirebaseDatabase.getInstance().getReference("Friends").child(fuser);
        userdb = FirebaseDatabase.getInstance().getReference("User");

        final String user = mfriend.get(position);
        userdb.child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user1 = dataSnapshot.getValue(User.class);
                holder.username.setText(user1.getUsername());
                if (user1.getImageURl().equals("default")) {
                    holder.profile_image.setImageResource(R.drawable.ic_user_profile);
                } else {
                    Glide.with(mContext).load(user1.getImageURl()).into(holder.profile_image);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, RequestProfile.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                        intent.putExtra("userid", user1.getId());
                        mContext.startActivity(intent);
                        ((Activity) mContext).finish();
                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    public int getItemCount() {
        return mfriend.size();
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
