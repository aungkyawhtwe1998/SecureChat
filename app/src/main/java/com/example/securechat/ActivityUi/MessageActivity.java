package com.example.securechat.ActivityUi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.securechat.Adapter.MessageAdapter;
import com.example.securechat.Chat;
import com.example.securechat.Crypto.Cryptor;
import com.example.securechat.Crypto.ENCUtils;
import com.example.securechat.Fragment.ApiService;
import com.example.securechat.Notification.Client;
import com.example.securechat.Notification.Data;
import com.example.securechat.Notification.MyResponse;
import com.example.securechat.Notification.Sender;
import com.example.securechat.Notification.Token;
import com.example.securechat.R;
import com.example.securechat.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jcajce.provider.symmetric.ARC4;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView username;
    FirebaseAuth mAuth;
    FirebaseUser fuser;
    DatabaseReference reference;
    FirebaseDatabase db;
    EditText txtKey;
    ImageButton btn_send;
    EditText text_send;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    String userid;
    ApiService apiService;
    boolean notify = false;
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpace;
    KeyGenerator generator;
    public static PublicKey other_public;

    String otherPublicKey;
    public static SecretKey secretKey;

    ValueEventListener seenListener;
    KeyPair kp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        profile_image = findViewById(R.id.message_profile);
        username = findViewById(R.id.message_username);
        text_send = findViewById(R.id.txt_send);
        btn_send = findViewById(R.id.btn_send);
        txtKey = findViewById(R.id.txtKey);
        Toolbar toolbar = findViewById(R.id.message_toolbar);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FFFFFF"));

        // Set BackgroundDrawable
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.status));

        apiService = Client.getCient("https://fcm.googleapis.com/").create(ApiService.class);

        recyclerView = findViewById(R.id.chat_recview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        final Intent intent = getIntent();
        try {
            cipher = Cipher.getInstance("AES");
            //decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {

            kp=ENCUtils.geneateECKey(getApplicationContext());
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("User").child(userid);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                otherPublicKey=user.getPublicKey();
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("ECKeyPair",MODE_PRIVATE);
                String privateKeyString = preferences.getString("private_key", null);
                Log.d("MessageActivity", "Shared_Private_Key: "+privateKeyString);
                byte[] pub=Base64.decode(otherPublicKey,Base64.DEFAULT);
                //byte[] pri= new byte[0];

                KeyFactory factory = null;
                try {
                    byte[] pri = Base64.decode(privateKeyString.getBytes("UTF-8"),Base64.DEFAULT);
                    factory = KeyFactory.getInstance("ECDH", "SC");
                    other_public = (PublicKey) factory.generatePublic(new X509EncodedKeySpec(pub));
                    kp=ENCUtils.geneateECKey(getApplicationContext());
                    //PrivateKey privateKey = kp.getPrivate();
                    PrivateKey myprivateKey = (PrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(pri));
                    Log.d("MessageActi", "MyPublic: "+other_public);
                    Log.d("MessageActi", "MyPrivate:"+myprivateKey);
                    secretKey=ENCUtils.generateSharedSecret(myprivateKey,other_public);
                    String value = preferences.getString(userid,null);
                    //txtKey.setText(Base64.encodeToString(secretKey.getEncoded(),Base64.DEFAULT));
                    secretKeySpace = new SecretKeySpec(secretKey.getEncoded(), "AES/GCM/NoPadding");
                    Log.d("TAG", "SecrectKey:"+Base64.encodeToString(secretKeySpace.getEncoded(), Base64.DEFAULT));
                    readMessage(fuser.getUid(), userid, user.getImageURl(),secretKey);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (user.getImageURl().equals("default")) {
                    profile_image.setImageResource(R.drawable.ic_user_profile);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURl()).into(profile_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                Calendar c = Calendar.getInstance();
                System.out.println("Current time => " + c.getTime());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String formattedDate = df.format(c.getTime());
                String msg = text_send.getText().toString().trim();
                if (!msg.equals("")) {
                    //sendMessage(fuser.getUid(), userid, AESEncryptionMethod(msg), formattedDate);
                    Log.d("MessageActivity", "onClick: SecrectKeySpec:"+secretKeySpace);
                    sendMessage(fuser.getUid(), userid,ENCUtils.AESEncryptionMethod(secretKeySpace,msg), formattedDate);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send Message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");


            }
        });
    }


    private void sendMessage(String sender, String receiver, String message, String date) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("date", date);
        reference.child("Chats").push().setValue(hashMap);

        //add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid()).child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("User").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, String username, String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher_new_round, username + ": " + message, "New Message", userid);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.snedNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.keepSynced(true);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String myid, final String userid, final String imageurl,SecretKey sk) {
        mchat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchat.add(chat);
                    }
                    //Log.i("TAG", "Message user id"+imageurl);
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl,sk);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("User").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));

    }
}
