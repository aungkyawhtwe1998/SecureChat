package com.example.securechat.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.securechat.ActivityUi.MainActivity;
import com.example.securechat.ActivityUi.MessageActivity;
import com.example.securechat.Chat;
import com.example.securechat.Crypto.Cryptor;
import com.example.securechat.Crypto.ENCUtils;
import com.example.securechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public Context mContext;
    public List<Chat> mChat;
    private String imageurl;
    private SecretKey secretKey;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    FirebaseUser fUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl,SecretKey secretKey) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;
        this.secretKey = secretKey;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.txt_date.setText(chat.getDate());
        holder.show_message.setText(chat.getMessage());
        //holder.show_message.setText(chat.getMessage());

        try {
            Log.d("TAG", "onBindViewHolder: Secrect"+secretKey);
            //holder.show_message.setText(ENCUtils.decryptString(secretKey,chat.getMessage()));
            holder.show_message.setText(ENCUtils.AESDescryptionMethod(secretKey,chat.getMessage()));

        }catch (Exception e) {
            e.printStackTrace();
        }
        //holder.profile_image.setImageResource(R.drawable.ic_user_profile);
        if (imageurl.equals("default")) {
            //holder.profile_image.setImageResource(R.drawable.ic_user_profile);
        } else {
            Log.i("TAG", "Adapter"+imageurl);
            //Glide.with(mContext.).load(imageurl).into(holder.profile_image);
        }

        if (position == mChat.size() - 1) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.txt_date.setVisibility(View.VISIBLE);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                SimpleDateFormat dfout = new SimpleDateFormat("mm/dd/yy hh:mm");
                try {
                    Date d = df.parse(chat.getDate());
                    holder.txt_date.setText(dfout.format(d));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        });
    }


    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public TextView txt_seen;
        CircleImageView profile_image;
        public TextView txt_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image =(CircleImageView) itemView.findViewById(R.id.chat_profile_fri);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            txt_date = itemView.findViewById(R.id.message_date);
            txt_date.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
