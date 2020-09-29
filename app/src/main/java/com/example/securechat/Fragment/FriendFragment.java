package com.example.securechat.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securechat.Adapter.FriendAdapter;
import com.example.securechat.Adapter.FriendSearchAdapter;
import com.example.securechat.Chatlist;
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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment {

    EditText edt_search;
    private RecyclerView recyclerView;
    private List<String> mUser;
    private List<User> listuser;

    FirebaseUser fuser;
    FriendAdapter fadapter;
    FriendSearchAdapter people_adapter;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    private String mCurrent_user_id;
    private View mMainView;
    private List<Chatlist> userList;
    private DatabaseReference userdb;
    private DatabaseReference friendb;
    private FirebaseAuth mAth;

    public FriendFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        userdb = FirebaseDatabase.getInstance().getReference().child("User");
        friendb = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        //edt_search=view.findViewById(R.id.edt_search);
        recyclerView = mMainView.findViewById(R.id.recycler_request);
        edt_search = mMainView.findViewById(R.id.edt_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUser = new ArrayList<String>();
        readUser();
        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return mMainView;
    }

    private void readUser() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("Userfrag", "User " + firebaseUser.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Friends").child(mCurrent_user_id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    mUser.add(uid);
                }
                fadapter = new FriendAdapter(getContext(), mUser);
                recyclerView.setAdapter(fadapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(String s) {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("User").orderByChild("name")
                .startAt(s)
                .endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    assert fuser != null;
                    if (!user.getId().equals(fuser.getUid())) {
                        listuser.add(user);
                    }
                }
                people_adapter = new FriendSearchAdapter(getContext(), listuser, false);
                recyclerView.setAdapter(people_adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
