package com.example.macksham.friendbuzz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfilePhoneNumber;
    private Button mProfileBlockBtn;
    private Button mProfileMsgBtn;
    //FireBase
    private DatabaseReference mUserDatabase;
    private DatabaseReference mBlockDatabase;
    private DatabaseReference mCurrentUserReference;
    private FirebaseUser mCurrentUser;
    private  int mBlockState;

    //progress dialog
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        //Reference Layout
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_name);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileBlockBtn = findViewById(R.id.profile_block_btn);
        mProfilePhoneNumber = findViewById(R.id.profile_phone_number);
        mProfileMsgBtn = findViewById(R.id.profile_send_message);


        //Firebase
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mBlockDatabase = FirebaseDatabase.getInstance().getReference().child("Block");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mCurrentUser!=null){
            mCurrentUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        }

        mBlockState = 0; //implies users is not blocked;

        //Progress Dialog before rertiving the data for user.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user Data");
        mProgressDialog.setMessage("Please Wait while we Load User Data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        //Loading the user data
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                String display_phone_number = dataSnapshot.child("phone_number").getValue().toString();
                String display_image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(display_status);
                mProfilePhoneNumber.setText(display_phone_number);
                Picasso.get().load(display_image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                //------Block State Feature ---------
                mBlockDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot blockStateSnapshot) {
                        if(blockStateSnapshot.hasChild(user_id)){
                            String block_type = blockStateSnapshot.child(user_id).child("BlockType").getValue().toString();
                            if(block_type.equals("blocked_to")){
                                mBlockState = 1;
                                mProfileBlockBtn.setText("UnBlock");
                                mProfileBlockBtn.setEnabled(true);
                            }else{
                                mBlockState = 2;
                                mProfileBlockBtn.setText("You are Blocked");
                                mProfileBlockBtn.setEnabled(false);
                            }

                        }
                        mProgressDialog.dismiss();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(ProfileActivity.this,ChatActivity.class);
                chatIntent.putExtra("user_id",user_id);
                startActivity(chatIntent);
            }
        });
        mProfileBlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog = new ProgressDialog(ProfileActivity.this);
                mProgressDialog.setMessage("Please Wait while We Do your work");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                mProfileBlockBtn.setEnabled(false);

                //-----------------No Block State -------------
                if(mBlockState == 0){
                    mBlockDatabase.child(mCurrentUser.getUid()).child(user_id).child("BlockType").setValue("blocked_to").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                 mBlockDatabase.child(user_id).child(mCurrentUser.getUid()).child("BlockType").setValue("blocked_from").addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mBlockState = 1; //user Blocked
                                            mProfileBlockBtn.setText("UnBlock");

                                            Toast.makeText(ProfileActivity.this,"User is Blocked", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(ProfileActivity.this,"The User has been blocked", Toast.LENGTH_LONG).show();
                                        }
                                     }
                                 });
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed in blocking the user", Toast.LENGTH_LONG).show();
                            }

                            mProfileBlockBtn.setEnabled(true);
                            mProgressDialog.dismiss();
                        }
                    });
                }

                // -------------- Blocked To State ----------------
                if(mBlockState == 1){
                    mBlockDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mBlockDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mBlockState = 0; //Not Blocked
                                        mProfileBlockBtn.setText("Block");
                                        Toast.makeText(ProfileActivity.this,"UnBlock is successful", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this,"Fail in Unblocking", Toast.LENGTH_LONG).show();
                            }

                            mProfileBlockBtn.setEnabled(true);
                            mProgressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser != null){
            mCurrentUserReference.child("online").setValue(1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCurrentUser!=null){
            mCurrentUserReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
