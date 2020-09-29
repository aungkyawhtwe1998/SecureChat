package com.example.securechat.ActivityUi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.securechat.Crypto.ENCUtils;
import com.example.securechat.Fragment.ApiService;
import com.example.securechat.Notification.Client;
import com.example.securechat.R;
import com.example.securechat.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestProfile extends AppCompatActivity {

    private CircleImageView profile;
    private TextView username;
    private Button btnrequest, btncancel, chatnow;
    private DatabaseReference userdb;
    private String mCurrent_state;
    private ProgressDialog mProgressDia;
    private DatabaseReference mFriendReqDb;
    private DatabaseReference mNotificationDb;
    private DatabaseReference frinedDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser muser;
    String user_id;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friend Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.status));




        apiService = Client.getCient("https://fcm.googleapis.com/").create(ApiService.class);
        user_id = getIntent().getStringExtra("userid");

        userdb = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        mFriendReqDb = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        frinedDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDb = FirebaseDatabase.getInstance().getReference().child("notification");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        muser = FirebaseAuth.getInstance().getCurrentUser();

        profile = findViewById(R.id.request_profile);
        username = findViewById(R.id.request_username);
        btnrequest = findViewById(R.id.btn_request);
        btncancel = findViewById(R.id.btn_cancel_request);
        chatnow = findViewById(R.id.btn_chatnow);

        mCurrent_state = "not_friends";
        mProgressDia = new ProgressDialog(this);
        mProgressDia.setTitle("Loading User Data");
        mProgressDia.setMessage("Please wait while we load the user data");
        mProgressDia.setCanceledOnTouchOutside(false);
        mProgressDia.show();

        userdb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (!user.getImageURl().toString().equals("default")) {
                    // Glide.with(getApplicationContext()).load(user.getImageURl()).into(profile);
                    Picasso.get()
                            .load(user.getImageURl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_user_profile)
                            .into(profile, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(user.getImageURl()).into(profile);
                                }
                            });
                } else {
                    Glide.with(getApplicationContext()).load(R.drawable.ic_user_profile).into(profile);
                }

                //Friend List/ Request feauture

                mProgressDia.dismiss();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendReqDb.child(muser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendReqDb.child(user_id).child(muser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                btnrequest.setEnabled(true);
                                mCurrent_state = "not_friends";
                                btnrequest.setText("Send Request");
                                btncancel.setVisibility(View.VISIBLE);
                                btncancel.setEnabled(false);
                            }
                        });
                    }
                });
            }
        });

        mFriendReqDb.child(muser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                    if (req_type.equals("received")) {
                        mCurrent_state = "req_received";
                        btnrequest.setText("Accept");
                        btncancel.setVisibility(View.VISIBLE);
                        btncancel.setEnabled(true);
                        chatnow.setVisibility(View.GONE);
                    } else if (req_type.equals("sent")) {
                        mCurrent_state = "req_sent";
                        btnrequest.setText("Cancel Request");
                        btnrequest.setEnabled(true);
                        btncancel.setVisibility(View.GONE);
                        btncancel.setEnabled(false);
                        chatnow.setVisibility(View.GONE);
                    } else {
                        frinedDatabase.child(muser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(user_id)) {
                                    mCurrent_state = "friends";
                                    btnrequest.setText("Unfriend");
                                    btncancel.setVisibility(View.GONE);
                                    btncancel.setEnabled(false);
                                    chatnow.setVisibility(View.GONE);
                                }
                                mProgressDia.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                mProgressDia.dismiss();
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        frinedDatabase.child(muser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //
                if (dataSnapshot.hasChild(user_id)) {
                    Log.i("RequestProfile", "Key" + dataSnapshot.getChildren());
                    mCurrent_state = "friends";
                    btnrequest.setText("Unfriend");
                    btncancel.setVisibility(View.GONE);
                    btncancel.setEnabled(false);
                    chatnow.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RequestProfile.this, MessageActivity.class);
                intent.putExtra("userid", user_id);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        btnrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnrequest.setEnabled(false);
                //friends Request state
                if (mCurrent_state.equals("not_friends")) {
                    Map requMap = new HashMap();
                    requMap.put("Friend_req/" + muser.getUid() + "/" + user_id + "/request_type", "sent");
                    requMap.put("Friend_req/" + user_id + "/" + muser.getUid() + "/request_type", "received");

                    mRootRef.updateChildren(requMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(RequestProfile.this, "Request Sent", Toast.LENGTH_SHORT).show();
                            }
                            mCurrent_state = "req_sent";
                            btnrequest.setText("Cancel Request");
                            btncancel.setVisibility(View.GONE);
                            btncancel.setEnabled(true);
                            chatnow.setVisibility(View.GONE);

                        }
                    });
                }
                //cancel the sent
                if (mCurrent_state.equals("req_sent")) {
                    mFriendReqDb.child(muser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDb.child(user_id).child(muser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btnrequest.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    btnrequest.setText("Send Request");
                                    btncancel.setVisibility(View.VISIBLE);
                                    btncancel.setEnabled(false);
                                }
                            });
                        }
                    });

                }

                //received state
                if (mCurrent_state.equals("req_received")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + muser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + muser.getUid() + "/date", currentDate);
                    friendsMap.put("Friend_req/" + muser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + muser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                btnrequest.setEnabled(true);
                                mCurrent_state = "friends";
                                btnrequest.setText("Unfriend");
                                btncancel.setVisibility(View.GONE);
                                btncancel.setEnabled(false);
                                chatnow.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(RequestProfile.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                //Unfriend State
                if (mCurrent_state.equals("friends")) {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + muser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + muser.getUid(), null);
                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                btnrequest.setEnabled(true);
                                mCurrent_state = "not_friends";
                                btnrequest.setText("Send Request");
                                btncancel.setVisibility(View.GONE);
                                btncancel.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RequestProfile.this, HomeActivity.class));

    }

}