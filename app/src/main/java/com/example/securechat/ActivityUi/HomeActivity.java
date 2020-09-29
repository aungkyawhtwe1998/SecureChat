package com.example.securechat.ActivityUi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.securechat.Fragment.ChatFragment;
import com.example.securechat.Fragment.FriendFragment;
import com.example.securechat.Fragment.UserFragment;
import com.example.securechat.R;
import com.example.securechat.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
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

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    CircleImageView profile_image;
    DrawerLayout drawer;
    CircleImageView img_profile;
    TextView txt_username,txt_email;
    TextView username;
    private FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    NavigationView navigationView;

    @Override
    protected void onStart() {
        super.onStart();
    }

    private int[] tabIcons = {
            R.drawable.ic_multimedia,
            R.drawable.ic_men,
            R.drawable.ic_queue_black_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.status));

        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        username = findViewById(R.id.user_name);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //drawer = findViewById(R.id.drawer_layout);
        txt_username=findViewById(R.id.txt_username_nav);
        txt_email=findViewById(R.id.txt_email_nav);
        img_profile=findViewById(R.id.imgProfile_nav);


        if(firebaseUser!=null){
            reference = FirebaseDatabase.getInstance().getReference("User").child(firebaseUser.getUid());
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Log.i(String.valueOf(HomeActivity.this), "Retrieve :" + user.getId());
                    username.setText(user.getUsername());
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
        }
        /*navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.users_id) {
                    Intent intent=new Intent(HomeActivity.this,ProfileActivity.class);
                    startActivity(intent);
                    finish();

                }
                if (id == R.id.about_id) {

                }
                if (id == R.id.setting_id) {

                }
                if (id == R.id.logout_id) {
                    //logOut();
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("User").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
//                    Log.i("Profile", "ImageURL: " + user.getImageURl());
//                    if (user.getImageURl().equals("default")) {
//                        img_profile.setImageResource(R.drawable.ic_user_profile);
//                    } else {
//                        Picasso.get()
//                                .load(user.getImageURl())
//                                .networkPolicy(NetworkPolicy.OFFLINE)
//                                .placeholder(R.drawable.ic_user_profile)
//                                .into(img_profile, new Callback() {
//                                    @Override
//                                    public void onSuccess() {
//
//                                    }
//
//                                    @Override
//                                    public void onError(Exception e) {
//
//                                        Picasso.get().load(user.getImageURl()).placeholder(R.drawable.ic_user_profile).into(img_profile);
//                                    }
//                                });
//                    }
                    txt_username.setText(user.getUsername());
                    txt_email.setText(user.getEmail());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("ECKeyPair",MODE_PRIVATE);
        String privateKey = preferences.getString("private_key",null);
        Log.d("HomeActi", "SharedPre_Private_Key: "+privateKey);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatFragment(), "Chats");
        viewPagerAdapter.addFragment(new FriendFragment(), "Friends");
        viewPagerAdapter.addFragment(new UserFragment(), "Request");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
//        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
//        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
//        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("User").child(firebaseUser.getUid());
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
        status("offline");
    }
}
