package com.example.amit.hey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private TextInputLayout satus_change;
    private Button saveButton;

    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        toolbar = findViewById(R.id.status_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Accunt Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent statusIntent = getIntent();
        String statusValue = statusIntent.getStringExtra("status");


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        satus_change = findViewById(R.id.status_change);
        saveButton = findViewById(R.id.status_change_button);
        satus_change.getEditText().setText(statusValue);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we save changes");
                progressDialog.show();

                String new_status = satus_change.getEditText().getText().toString();
                databaseReference.child("status").setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                        }
                        else {
                            Toast.makeText(StatusActivity.this, "Some error occured. Try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}
