package com.example.amit.hey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateButton;

    //  progress Bar code
    private ProgressDialog mRegProgressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private android.support.v7.widget.Toolbar mtoolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mtoolBar = findViewById(R.id.register_app_bar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle(R.string.reg_toolbar_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgressBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreateButton = findViewById(R.id.reg_create_account);

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    mRegProgressBar.setTitle("Registering User");
                    mRegProgressBar.setMessage("Please Wait while account is created");
                    mRegProgressBar.setCanceledOnTouchOutside(false);
                    mRegProgressBar.show();

                }

                registerUser(display_name, email, password);

            }
        });

    }

    private void registerUser(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String userId = currentUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi there! I am using Hey Chat");
                    userMap.put("image", "default");
                    userMap.put("thuumb_image", "default");
                    userMap.put("device_token", device_token);

                    databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mRegProgressBar.dismiss();
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                            else {
                                mRegProgressBar.dismiss();
                                Toast.makeText(RegisterActivity.this, "Email Created!! Add Details Later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });




                }
                else {
                    mRegProgressBar.hide();
                    Toast.makeText(RegisterActivity.this, "Something went wrong! Try again Later!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
