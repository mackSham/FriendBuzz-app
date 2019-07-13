package com.example.macksham.friendbuzz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private EditText mPhoneText;
    private Button mSendCodeBtn;

    private EditText mVerText;
    private Button mVerifyBtn;

    private android.support.v7.widget.Toolbar mToolbar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    //Progress Dialog
    private ProgressDialog mRegProgess;

    private String mVerificationId;
    private String phoneNumber;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    //private int flag = 0; //Verification code is not send
    //private static final String KEY_FLAG = "flag_key";
    //private static final String KEY_PHONE = "phone_key";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mPhoneText = (EditText) findViewById(R.id.reg_numb);
        mSendCodeBtn = (Button) findViewById(R.id.send_btn);

        mVerText = findViewById(R.id.ver_numb);
        mVerifyBtn = findViewById(R.id.verify_btn);

        mRegProgess = new ProgressDialog(this);

        mToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create an Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        /*if(savedInstanceState != null){
            flag = savedInstanceState.getInt(KEY_FLAG);
            if(flag == 1){
                mPhoneText.setEnabled(false);
                mSendCodeBtn.setEnabled(false);
                phoneNumber = savedInstanceState.getString(KEY_PHONE);
                if(!TextUtils.isEmpty(phoneNumber)) {
                    mRegProgess.setTitle("Sending Verification Code");
                    mRegProgess.setMessage("Please wait while we sent verification code.");
                    mRegProgess.setCanceledOnTouchOutside(false);
                    mRegProgess.show();
                    flag = 1; //Indicates verification is in progress.
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            RegisterActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        }*/
        mSendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPhoneText.setEnabled(false);
                mSendCodeBtn.setEnabled(false);
                phoneNumber = mPhoneText.getText().toString();
                if(phoneNumber.length()==10){
                    String code = "+91";
                    phoneNumber = code + phoneNumber;
                }
                if(!TextUtils.isEmpty(phoneNumber)) {
                    mRegProgess.setTitle("Sending Verification Code");
                    mRegProgess.setMessage("Please wait while we sent verification code.");
                    mRegProgess.setCanceledOnTouchOutside(false);
                    mRegProgess.show();
                    //flag = 1; //Indicates verification is in progress.
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            RegisterActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verificationCode = mVerText.getText().toString();
                if(!TextUtils.isEmpty(verificationCode)) {

                    mVerifyBtn.setEnabled(false);
                    mRegProgess.setTitle("Verifying Verification Code");
                    mRegProgess.setMessage("Please wait while we verify the code.");
                    mRegProgess.setCanceledOnTouchOutside(false);
                    mRegProgess.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                mRegProgess.dismiss();
                //flag=0;
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                //flag=0;
                mPhoneText.setEnabled(true);
                mSendCodeBtn.setEnabled(true);
                mRegProgess.hide();
                Context context = getApplicationContext();
                CharSequence text = "Enter the correct phone number.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                mPhoneText.setEnabled(false);
                mSendCodeBtn.setEnabled(false);
                mVerText.setVisibility(View.VISIBLE);
                mVerifyBtn.setVisibility(View.VISIBLE);
                mVerText.setEnabled(true);
                mVerifyBtn.setEnabled(true);

                mRegProgess.dismiss();

                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }

            public void onCodeAutoRetrievalTimeOut(String verificationId){

                mPhoneText.setEnabled(true);
                mSendCodeBtn.setText("ReSend Code");
                mSendCodeBtn.setEnabled(true);

            }
        };
    }
    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        //phoneNumber = mPhoneText.getText().toString();



        savedInstanceState.putInt(KEY_FLAG,flag);
        savedInstanceState.putString(KEY_PHONE,phoneNumber);
        super.onSaveInstanceState(savedInstanceState);
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, phoneNumber, duration);
        toast.show();
    }*/
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //flag = 0;
                            mRegProgess.dismiss();
                            FirebaseUser current_user = task.getResult().getUser();
                            final String current_uid = current_user.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
                            //Toast.makeText(RegisterActivity.this,mDatabase.toString(),Toast.LENGTH_LONG).show();
                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {


                                    if (dataSnapshot.hasChild(current_uid)) {
                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        mDatabase.child(current_uid).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        });
                                    }else{
                                        Intent mainIntent = new Intent(RegisterActivity.this,NameAddActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        mainIntent.putExtra("userPhoneNumber",phoneNumber);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            //flag=0;
                            mRegProgess.hide();
                            Context context = getApplicationContext();
                            CharSequence text = "Error in Verification Code!";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                            mVerText.setVisibility(View.VISIBLE);
                            mVerifyBtn.setVisibility(View.VISIBLE);
                            mVerText.setEnabled(true);
                            mVerifyBtn.setEnabled(true);

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

}
