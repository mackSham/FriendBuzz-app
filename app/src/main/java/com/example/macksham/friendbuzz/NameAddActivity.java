package com.example.macksham.friendbuzz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.HashMap;

public class NameAddActivity extends AppCompatActivity {


    private FirebaseUser mCurrentUser;
    private DatabaseReference mDataBase;

    TextInputEditText addName;
    ImageButton submit_button;

    //Encryption key
    private PrivateKey privateKey;
    private PublicKey publicKey;
    byte[] EncodedPublicKey;
    //Progress Dialog
    private ProgressDialog mAddNameProgess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_add);

        final String phoneNumber = getIntent().getStringExtra("userPhoneNumber");

        addName = (TextInputEditText) findViewById(R.id.add_name);
        submit_button = (ImageButton) findViewById(R.id.submit_button);

        mAddNameProgess = new ProgressDialog(this);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = addName.getText().toString();
                Log.d("Check onclick",name);
                mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                String current_uid = mCurrentUser.getUid();
                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                mDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                if(name != ""){
                    mDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                    mAddNameProgess.setTitle("Adding Name");
                    mAddNameProgess.setMessage("Please wait while we add your name");
                    mAddNameProgess.setCanceledOnTouchOutside(false);
                    mAddNameProgess.show();
                    try {
                        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                        final KeyPairGenerator keyPairGenerator =  KeyPairGenerator.getInstance("DH","BC");
                        keyPairGenerator.initialize(1024);

                        final KeyPair keyPair = keyPairGenerator.generateKeyPair();

                        privateKey = keyPair.getPrivate();
                        publicKey  = keyPair.getPublic();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("1. Encoded Public key",publicKey.toString());
                    EncodedPublicKey = publicKey.getEncoded();
                    //System.out.println("private Key"+privateKey);
                    //System.out.println("public Key"+publicKey);
                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("phone_number",phoneNumber);
                    userMap.put("name",name);
                    userMap.put("status","Hii I am using FriendBuzz");
                    userMap.put("image","default");
                    userMap.put("thumbnail_image","thumb_default");
                    userMap.put("device_token",deviceToken);
                    HashMap<String,byte[]> userEncryptionMap = new HashMap<>();
                    userEncryptionMap.put("public_key",EncodedPublicKey);
                    Log.d("2. Encoded Public key",EncodedPublicKey.toString());
                    mDataBase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //mDataBase.child("publicKey").setValue(EncodedPublicKey);
                            mAddNameProgess.dismiss();
                            Intent mainIntent = new Intent(NameAddActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
                }

            }
        });
    }
}
