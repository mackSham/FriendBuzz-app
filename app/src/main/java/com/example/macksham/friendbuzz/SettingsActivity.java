package com.example.macksham.friendbuzz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
   //Firebase
    private DatabaseReference mUserDataBase;
    private FirebaseUser mCurrentUser;
    //Storage
    private StorageReference mStorageRef;


    //Android Layout Reference
    private android.support.v7.widget.Toolbar mToolbar;
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private EditText mChangeStatus;
    private Button mChangeStatusButton;
    private Button mChangeImageButton;
    //General
    private int btntype=0;

    //Progress
    private ProgressDialog mProgress;



    private static final int GALLERY_PICK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Toolbar
        mToolbar = findViewById(R.id.settings_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Accounts Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        //Reference of Layout
        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mChangeStatus = findViewById(R.id.change_status);
        mChangeStatusButton = findViewById(R.id.settiings_status_btn);
        mChangeImageButton = findViewById(R.id.settings_image_btn);

        //ProgessBar
        mProgress = new ProgressDialog(this);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        //Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mUserDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDataBase.keepSynced(true);
        mUserDataBase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String image = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumbnail_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                if(!image.equals("default")) {
                    //Picasso.get().load(image).placeholder(R.mipmap.default_avatar).into(mDisplayImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.default_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.mipmap.default_avatar).into(mDisplayImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btntype == 0){
                    mStatus.setVisibility(View.INVISIBLE);
                    mChangeStatus.setVisibility(View.VISIBLE);
                    String status = mStatus.getText().toString();
                    mChangeStatus.setText(status);
                    btntype = 1;
                }else{
                    mProgress.setTitle("Saving Status");
                    mProgress.setMessage("Please wait while we save the status");
                    mProgress.show();

                    String status = mChangeStatus.getText().toString();
                    mUserDataBase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mProgress.dismiss();
                            }else{
                                mProgress.dismiss();
                                Toast.makeText(getApplicationContext(),"There was some error in saving Changes",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    mChangeStatus.setVisibility(View.INVISIBLE);
                    mStatus.setVisibility(View.VISIBLE);
                    btntype = 0;
                }
            }
        });

        mChangeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
                /*Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);*/
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser != null){
            mUserDataBase.child("online").setValue(1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCurrentUser!=null){
            mUserDataBase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgress.setTitle("Changing Profile Pic");
                mProgress.setMessage("Please wait while we upload you profile pic");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                //Firebase
                String current_uid = mCurrentUser.getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                StorageReference filepath = mStorageRef.child("profile_images").child(current_uid + ".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumb").child(current_uid + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            final String download_url = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful()){
                                        Map update_hashMap = new HashMap<>();
                                        update_hashMap.put("image",download_url);
                                        update_hashMap.put("thumbnail_image",thumb_downloadUrl);
                                        mUserDataBase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mProgress.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Uploaded Successfully", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }else{
                                        mProgress.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Error in Uploading Thumbnail File", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SettingsActivity.this, "Error in Uploading File", Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    }
                });

                //Toast.makeText(SettingsActivity.this, resultUri.toString(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }




}
