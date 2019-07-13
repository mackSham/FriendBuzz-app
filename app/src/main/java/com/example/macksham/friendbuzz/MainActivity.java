package com.example.macksham.friendbuzz;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity implements ContactsFragment.OnFragmentInteractionListener{

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private android.support.v7.widget.Toolbar mToolbar;
    private ViewPager mViewPager;
    private  SectionPagerAdopter mSectionsPageAdopter;
    private TabLayout mTabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("FriendBuzz");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        //Tabs
        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPageAdopter = new SectionPagerAdopter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPageAdopter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tab);
        mTabLayout.setupWithViewPager(mViewPager);

        //Toast.makeText(this,"Main Activity Create",Toast.LENGTH_LONG).show();
    }

    private void sendToRegistration() {
        Intent welcomeIntent = new Intent(MainActivity.this,WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToRegistration();
        }else{
            mUserRef.child("online").setValue(1);
        }
        //Toast.makeText(this,"Main Activity Start",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
        //Toast.makeText(this,"Main Activity Stop",Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_btn){
            mAuth.signOut();
            sendToRegistration();
        }
        if(item.getItemId() == R.id.main_account_settings_btn){
            Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.main_alluser_btn){
            Intent alluserIntent = new Intent(MainActivity.this,AllUserActivity.class);
            startActivity(alluserIntent);
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
