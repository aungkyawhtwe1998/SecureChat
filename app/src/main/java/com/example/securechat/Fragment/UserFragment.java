package com.example.securechat.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securechat.Adapter.FriendRequestAdapter;
import com.example.securechat.Adapter.FriendSearchAdapter;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendSearchAdapter userAdapter;
    private FriendRequestAdapter friendAdapter;
    private List<User> mUser;
    String mCurrent_user_id;
    private List<String> request_user;
    EditText edt_search;
    private Button findfriedn;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Friend_req");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        edt_search = view.findViewById(R.id.edt_search);
        recyclerView = view.findViewById(R.id.recyler_view);
        findfriedn = view.findViewById(R.id.btn_findfriend);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        // recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUser = new ArrayList<>();
        request_user = new ArrayList<>();
        findfriedn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edt_search.getText().equals("")) {
                    searchUsers(edt_search.getText().toString());
                } else {
                    Toast.makeText(getContext(), "Please fill email address", Toast.LENGTH_SHORT).show();
                }
            }
        });
        readUser();

        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.equals("")) {
                    recyclerView.notify();
                }
            }
        });

        return view;

    }

    private void searchUsers(String s) {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("User").orderByChild("email")
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
                        mUser.add(user);
                    } else {
                        Toast.makeText(getActivity(), "It is you, just Find others", Toast.LENGTH_SHORT).show();
                    }
                }
                userAdapter = new FriendSearchAdapter(getContext(), mUser, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUser() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("Userfrag", "User " + firebaseUser.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Friend_req").child(firebaseUser.getUid().toString());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                request_user.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    request_user.add(uid);
                }
                friendAdapter = new FriendRequestAdapter(getContext(), request_user);
                recyclerView.setAdapter(friendAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    /*private void readUser(){
        //
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(edt_search.getText().toString().equals("")){
                    mUser.clear();
                    for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                        User user=snapshot.getValue(User.class);
                        assert user != null;
                        if(!user.getId().equals(firebaseUser.getUid())){
                            mUser.add(user);
                        }
                    }
                    userAdapter=new People_adapter(getContext(),mUser,true);
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
