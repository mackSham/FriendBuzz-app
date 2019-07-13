package com.example.macksham.friendbuzz;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvoList;

    private DatabaseReference mConvoDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUserDatabase;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;


    private FirebaseRecyclerAdapter<Conversation, ChatsFragment.ConvoViewHolder> firebaseRecyclerAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView =  inflater.inflate(R.layout.fragment_chats, container, false);

        mConvoList = mMainView.findViewById(R.id.convo_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mConvoDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mConvoDatabase.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrentUserId);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvoList.setHasFixedSize(true);
        mConvoList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvoDatabase.orderByChild("timestamp");
        FirebaseRecyclerOptions convoOptions = new FirebaseRecyclerOptions.Builder<Conversation>().setQuery(conversationQuery,Conversation.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conversation, ConvoViewHolder>(convoOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ConvoViewHolder holder, int position, @NonNull final Conversation model) {
                final String list_user_id = getRef(position).getKey();
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data);
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

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumbnail_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            Long userOnline = (Long)dataSnapshot.child("online").getValue();
                            holder.setUserOnline(userOnline);

                        }

                        holder.setName(userName);
                        holder.setUserImage(userThumb, getContext());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);

                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ConvoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chats_single_layout, parent, false);

                return new ConvoViewHolder(view);
            }

        };

        mConvoList.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }

    private static class ConvoViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ConvoViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public  void setMessage(String message){
            TextView convoMsgView = mView.findViewById(R.id.convo_messages);
            convoMsgView.setText(message);
        }

        public void setName(String name){
            TextView convoNameView = mView.findViewById(R.id.convo_name);
            convoNameView.setText(name);
        }
        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.convo_profile_pic);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

        }

        public void setUserOnline(long online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.convo_online);

            if(online_status == 1){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        firebaseRecyclerAdapter.stopListening();
    }
}
