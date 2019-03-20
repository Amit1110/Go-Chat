package com.example.amit.hey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout emailText;
    private TextInputLayout passwordText;
    private Button loginButton;

    private ProgressDialog loginProgressBar;

    private DatabaseReference userDatabase;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toolbar = findViewById(R.id.login_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        loginProgressBar = new ProgressDialog(this);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.login_email);

        passwordText = findViewById(R.id.login_paaword);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordText.getEditText().getText().toString();
                String email = emailText.getEditText().getText().toString();
                if(!TextUtils.isEmpty(password) && !TextUtils.isEmpty(email)){

                    loginProgressBar.setTitle("Logging In");
                    loginProgressBar.setMessage("Please wait while we check your credentials");
                    loginProgressBar.setCanceledOnTouchOutside(false);
                    loginProgressBar.show();


                    loginUser(email, password);
                }
            }
        });

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    loginProgressBar.dismiss();


                    String currentUserId = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    userDatabase.child(currentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });



                }
                else {
                    loginProgressBar.hide();
                    Toast.makeText(LoginActivity.this, "Something went wrong! Try again Later!!", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }
}










































