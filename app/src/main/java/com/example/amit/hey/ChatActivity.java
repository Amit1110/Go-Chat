package com.example.amit.hey;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private String mChatUser;
    private android.support.v7.widget.Toolbar mChatToolbar;

    private DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    String currentUserId;

    private TextView titleView;
    private TextView lastSeenView;
    private CircleImageView profileImage;

    private ImageView chatAddView;
    private ImageView chatSendView;
    private EditText chatMessageInput;

    private RecyclerView messagesList;
    private SwipeRefreshLayout mRefreshLatout;

    private final List<Messages> mMessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private static final  int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPageNumber = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        rootReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("userId");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        View action_bar_view = inflater.inflate(R.layout.chat_custom_layout, null);

        actionBar.setCustomView(action_bar_view);

        titleView = findViewById(R.id.custom_chat_bar_name);
        lastSeenView = findViewById(R.id.custom_chat_bar_latseen);
        profileImage = findViewById(R.id.custom_chat_bar_image);
        chatAddView = findViewById(R.id.chat_page_add_view);
        chatSendView = findViewById(R.id.chat_page_send_button);
        chatMessageInput = findViewById(R.id.chat_page_message_view);
        messagesList = findViewById(R.id.messages_list);
        mRefreshLatout = findViewById(R.id.swipeRefreshLayout);

        messageAdapter = new MessageAdapter(mMessagesList);

        linearLayoutManager = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayoutManager);
        messagesList.setAdapter(messageAdapter);

        loadMessages();


        rootReference.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatUserName = dataSnapshot.child("name").getValue().toString();
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thuumb_image").getValue().toString();
                if(online.equals("true")){
                    lastSeenView.setText("Online");
                }
                else {


                    //20 Mar 2019
                    GetTime getTime = new GetTime();
                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTime.getTimeAgo(lastTime, getApplicationContext());

                    lastSeenView.setText(lastSeenTime);
                }
                titleView.setText(chatUserName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //20 Mar 2019
        rootReference.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map ChatAddMap = new HashMap();
                    ChatAddMap.put("seen",false);
                    ChatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map ChatUserMap = new HashMap();
                    ChatUserMap.put("Chat/" + currentUserId + "/" + mChatUser, ChatAddMap);
                    ChatUserMap.put("Chat/" + mChatUser + "/" + currentUserId  , ChatAddMap);

                    rootReference.updateChildren(ChatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.i("Amit Chat Log", databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Sending Message
        chatSendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }

        });

        mRefreshLatout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPageNumber++;

                mMessagesList.clear();

                loadMessages();
            }
        });


    }

    private void loadMessages(){

        DatabaseReference messageRef = rootReference.child("messages").child(currentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(currentPageNumber*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messagesRetrievd = dataSnapshot.getValue(Messages.class);

                mMessagesList.add(messagesRetrievd);
                messageAdapter.notifyDataSetChanged();
                messagesList.scrollToPosition(mMessagesList.size() - 1);

                mRefreshLatout.setRefreshing(false);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //20 Mar 2019
    private void sendMessage() {

        String message = chatMessageInput.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String currentUserRef = "messages/" + currentUserId + "/" + mChatUser;
            String chatUserRef = "messages/" + mChatUser + "/" + currentUserId;

            DatabaseReference user_message_push = rootReference.child("messages").child(currentUserId).child(mChatUser).push();

            String pushId = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

            chatMessageInput.setText("");

            rootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.i("Amit Message Log", databaseError.getMessage().toString());
                    }
                }
            });

        }
    }
}





































