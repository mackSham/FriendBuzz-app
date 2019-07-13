package com.example.macksham.friendbuzz;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class AllUserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUserLists;
    private Contacts mContacts;
    //Firebase
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        //Layout Reference
        mToolbar = (Toolbar) findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //FireBase
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        mUserLists = (RecyclerView) findViewById(R.id.users_list);
        //Toast.makeText(this,"All Activity Activity Create",Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance().getReference().child("Users");

        mContacts = new Contacts();
        mUserLists.hasFixedSize();
        mUserLists.setLayoutManager(new LinearLayoutManager(AllUserActivity.this));

        FirebaseRecyclerOptions userOptions = new FirebaseRecyclerOptions.Builder<Users>().setQuery(query,Users.class).build();

        firebaseRecyclerAdapter  = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(userOptions) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_layout, parent, false);

                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                if(mContacts.contactExists(AllUserActivity.this,model.getPhone_number())){
                    holder.setName(model.getName());
                    holder.setStatus(model.getStatus());
                    holder.setUserImage(model.getThumbnail_image());
                    holder.setOnline(model.getOnline());
                    holder.setVisible();
                    final String user_id = getRef(position).getKey();

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent profileIntent = new Intent(AllUserActivity.this,ProfileActivity.class);
                            profileIntent.putExtra("user_id",user_id);
                            startActivity(profileIntent);
                        }
                    });
                }else{
                   Log.d("Contacts lists","Reached Here");
                    //firebaseRecyclerAdapter.getRef(position).removeValue();
                    holder.setGone();

                }

            }
        };
        mUserLists.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }
        if(currentUser != null){
            mUserRef.child("online").setValue(1);
        }

        //Toast.makeText(this,"All Activity Activity Start",Toast.LENGTH_LONG).show();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setVisible(){
            RelativeLayout userSingleRow = mView.findViewById(R.id.user_single_row);
            userSingleRow.setVisibility(View.VISIBLE);
        }
        public void setGone(){
            RelativeLayout userSingleRow = mView.findViewById(R.id.user_single_row);
            userSingleRow.setVisibility(View.GONE);
        }
        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumbnail_image) {
            CircleImageView usersImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumbnail_image).placeholder(R.drawable.default_avatar).into(usersImageView);
        }
        public void setOnline(Long online){
            ImageView userOnlineView = mView.findViewById(R.id.user_single_online);
            if(online == 1){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        firebaseRecyclerAdapter.stopListening();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

        //Toast.makeText(this,"All Activity Stop",Toast.LENGTH_LONG).show();
    }
}
