package com.example.macksham.friendbuzz;

import android.app.WallpaperManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;

    //Layout References
    private String mChatUser;
    private Toolbar mChatToolbar;
    private TextView mTitleView;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessageLists;
    private SwipeRefreshLayout mRefreshLayout;

    private String currentUserid;

    private final List<Messages> messageList= new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;


    //Solution
    private int itemPos =0;
    private String mLastkey;
    private String mPrevKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //Firebase References
        mChatUser = getIntent().getStringExtra("user_id");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //Layout Reference
        mChatToolbar = findViewById(R.id.chat_app_bar);
        mChatMessageView = findViewById(R.id.chat_message_view);
        mChatSendBtn = findViewById(R.id.chat_send_btn);

        mAdapter = new MessageAdapter(messageList);
        mMessageLists = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);

        mLinearLayout = new LinearLayoutManager(this);
        mMessageLists.setHasFixedSize(true);
        mMessageLists.setLayoutManager(mLinearLayout);

        mMessageLists.setAdapter(mAdapter);

        //Setting the default Action Bar
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        //getSupportActionBar().setTitle(chat_user_name);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        //Log.d("nothing","hello mayank"+ chat_user_name);
        actionBar.setCustomView(action_bar_view);

        //Action Bar Reference
        mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeen = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);

        //setting for database for notification
        currentUserid = currentUser.getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        loadMessages();
        //Retrieving data for appbar
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String chat_user_name = dataSnapshot.child("name").getValue().toString();
                Long online = (Long) dataSnapshot.child("online").getValue();
                final String thumb_image = dataSnapshot.child("thumbnail_image").getValue().toString();
                if(online == 1){
                    mLastSeen.setText("Online");
                }else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    String lastSeenTime = getTimeAgo.getTimeAgo(online,ChatActivity.this);
                    mLastSeen.setText(String.valueOf(lastSeenTime));
                }

                mTitleView.setText(chat_user_name);
                Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.default_avatar).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(thumb_image).placeholder(R.mipmap.default_avatar).into(mProfileImage);
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        //Clicking the send button
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });


    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("Messages").child(currentUserid).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastkey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String message_key = dataSnapshot.getKey();
                if(itemPos==0){
                    mLastkey = message_key;
                }
                if(!mPrevKey.equals(message_key)){
                    messageList.add(itemPos++,message);
                }else{
                    mPrevKey = mLastkey;
                }
                if((mLastkey == mPrevKey)&&(mPrevKey == message_key)){
                    mRefreshLayout.setEnabled(false);
                }
                Log.d("Total Keys","Last Key : "+mLastkey+" | Prev Key : "+mPrevKey+ " | Message Key : "+message_key);
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPosition(itemPos);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("Messages").child(currentUserid).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEM_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                String message_key = dataSnapshot.getKey();
                if(itemPos==1){
                    mLastkey = message_key;
                    mPrevKey = message_key;
                }
                messageList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessageLists.scrollToPosition(messageList.size()-1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Sending the msg Method
    private void sendMessage(){
        final String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String currentUserRef = "Messages/"+currentUserid+"/"+mChatUser;
            String chatUserRef = "Messages/"+mChatUser+"/"+currentUserid;

            DatabaseReference userMessagePush = mRootRef.child("messages").child(currentUserid).child(mChatUser).push();
            String push_id = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserid);
            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef+"/"+push_id,messageMap);
            messageUserMap.put(chatUserRef+"/"+push_id,messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("Chat_Log",databaseError.getMessage().toString());
                    }else{
                        mChatMessageView.setText("");
                            Map chatAddMap = new HashMap();
                            chatAddMap.put("timestamp",ServerValue.TIMESTAMP);
                            chatAddMap.put("message",message);
                            chatAddMap.put("from",currentUserid);
                            Map chatUserMap = new HashMap();
                            chatUserMap.put("Chat/"+currentUserid+"/"+mChatUser,chatAddMap);
                            chatUserMap.put("Chat/"+mChatUser+"/"+currentUserid,chatAddMap);

                            mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if(databaseError!=null){
                                        Log.d("Chat Log",databaseError.getMessage().toString());
                                    }
                                }
                            });
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef.child("online").setValue(1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
