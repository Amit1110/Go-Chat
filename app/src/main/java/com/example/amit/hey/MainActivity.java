package com.example.amit.hey;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mtoolBar;

    private ViewPager viewPager;
    private SectionsAdapter sectionsAdapter;
    private TabLayout tabLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mtoolBar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle(R.string.main_Activity_Action_Bar);

        viewPager = findViewById(R.id.mainTabPager);

        sectionsAdapter = new SectionsAdapter(getSupportFragmentManager());

        if(mAuth.getCurrentUser() != null){
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


        viewPager.setAdapter(sectionsAdapter);

        tabLayout  = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if (currentUser == null){
            sendToStart();
        }
        else{
            userReference.child("online").setValue("true");
        }

//        updateUI(currentUser);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            userReference.child("online").setValue(ServerValue.TIMESTAMP);
            //userReference.child("lastSeen").setValue(ServerValue.TIMESTAMP);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logut_button){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        else if(item.getItemId() == R.id.main_settings_button){
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);

            startActivity(settingsIntent);
        }
        else if(item.getItemId() == R.id.main_users){
            Intent settingsIntent = new Intent(MainActivity.this, AllUsersActivity.class);

            startActivity(settingsIntent);
        }

        return true;
    }

    private void sendToStart(){
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();

    }
}































