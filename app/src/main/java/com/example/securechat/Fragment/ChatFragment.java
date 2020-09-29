package com.example.securechat.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securechat.Adapter.UserAdapter;
import com.example.securechat.Chatlist;
import com.example.securechat.Notification.Token;
import com.example.securechat.R;
import com.example.securechat.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<User> mUser;

    FirebaseUser fuser;
    UserAdapter userAdapter;
    DatabaseReference reference;
    private List<Chatlist> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_chat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        userList = new ArrayList<>();

        /*reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 userList.clear();
                 for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                     Chat chat=snapshot.getValue(Chat.class);
                     assert chat != null;
                     if(chat.getSender().equals(fuser.getUid())){
                         Log.i("TAG", "Sender"+chat.getSender());
                         Log.i("TAG", "Receiver"+chat.getReceiver());
                         userList.add(chat.getReceiver());
                     }
                     if(chat.getReceiver().equals(fuser.getUid())){
                         userList.add(chat.getSender());
                     }
                 }
                 readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    userList.add(chatlist);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }
    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void chatList() {
        mUser = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("User");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : userList) {
                        if (user.getId().equals(chatlist.getId())) {
                            mUser.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUser, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*private void readChats(){
        mUser=new CopyOnWriteArrayList<User>();
        reference=FirebaseDatabase.getInstance().getReference("User");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User user=snapshot.getValue(User.class);
                    for(String id: userList){
                        if(user.getId().equals(id)){
                            if(mUser.size()!=0){
                                for(User user1 : mUser){
                                    if(!user.getId().equals(user1.getId())){
                                        mUser.add(user);
                                    }
                                }
                            }else {
                                mUser.add(user);
                            }
                        }
                    }
                }
                userAdapter=new UserAdapter(getContext(), mUser, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
