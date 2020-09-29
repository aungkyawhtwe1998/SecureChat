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

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.example.securechat.Chat;
import com.example.securechat.ActivityUi.MessageActivity;
import com.example.securechat.Crypto.ENCUtils;
import com.example.securechat.R;
import com.example.securechat.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    public Context mContext;
    public List<User> mUser;
    private boolean ischat;
    String thelastmsg;
    Date d;
    String thelastdate;

    public UserAdapter(Context mContext, List<User> mUser, boolean ischat) {
        this.mUser = mUser;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_items, parent, false);
        return new UserAdapter.ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUser.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURl().equals("default")) {
            holder.profile_image.setImageResource(R.drawable.ic_user_profile);
        } else {
            Glide.with(mContext).load(user.getImageURl()).into(holder.profile_image);
        }

        if (ischat) {
            lastMessage(user.getId(), holder.last_msg, holder.last_date);

        } else {
            holder.last_msg.setVisibility(View.GONE);
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
        holder.rippleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        private TextView last_msg;
        private TextView last_date;
        private MaterialRippleLayout rippleLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.chat_user);
            profile_image = itemView.findViewById(R.id.chat_profileImg);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_message);
            last_date = itemView.findViewById(R.id.chat_date);
            rippleLayout=itemView.findViewById(R.id.ripple_user);

        }

    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, final TextView last_date) {
        thelastmsg = "default";
        thelastdate = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        assert chat != null;
                        assert firebaseUser != null;
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            thelastdate = chat.getDate();
                            //thelastmsg = chat.getMessage();
                        }
                    }
                    switch (thelastmsg) {
                        case "default":
                            last_msg.setText("No Message");
                            break;
                        default:
                            last_msg.setText(thelastmsg);
                    }
                    thelastmsg = "default";

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    SimpleDateFormat dfout = new SimpleDateFormat("hh:mm");
                    try {
                        Date d = df.parse(thelastdate);
                        switch (thelastdate) {
                            case "default":
                                last_date.setText("");
                                break;
                            default:
                                last_date.setText(dfout.format(d));
                        }
                        thelastdate = "default";

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
