package com.example.amit.hey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private CircleImageView circleImageView;
    private TextView displayName;
    private TextView displayStatus;
    private Button statusButton;
    private Button imageButton;

    //Firebase Storage

    private StorageReference imageReference;

    //progress dialog for uploading image
    private ProgressDialog imageUploadProgress;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        circleImageView = findViewById(R.id.settings_image_view);

        displayName = findViewById(R.id.settings_display_name);
        displayStatus = findViewById(R.id.settings_detail);
        statusButton = findViewById(R.id.settings_change_status_button);
        imageButton = findViewById(R.id.settings_change_image_button);

        imageReference = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String userId = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        databaseReference.keepSynced(true);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thuumb_image").getValue().toString();

                displayName.setText(name);
                displayStatus.setText(status);

//                Log.i("Amit", image);

                Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(SettingsActivity.this).load(image).into(circleImageView);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = displayStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status", status_value);
                startActivity(statusIntent);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);



                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELCT IMAGE"), GALLERY_PICK);

            }
        });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .start(this);

          //  Toast.makeText(SettingsActivity.this, imageuri, Toast.LENGTH_SHORT).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUploadProgress = new ProgressDialog(SettingsActivity.this);
                imageUploadProgress.setTitle("Uploading Image");
                imageUploadProgress.setMessage("Please wait a few seconds while we upload");
                imageUploadProgress.setCanceledOnTouchOutside(false);
                imageUploadProgress.show();

                Uri resultUri = result.getUri();

                String curUserId = currentUser.getUid();

                final StorageReference imagePath = imageReference.child("display_images").child(curUserId + ".jpg");

//                imagePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful()){
//
//                            String usi1 = task.getResult().getStorage().getDownloadUrl().toString();
//                            String usi2 = imagePath.getDownloadUrl().toString();
//                            String uri3 = imagePath.getMetadata().toString();
//
//                            Log.i("Amit", usi1);
//                            Log.i("Amit", usi2);
//                            Log.i("Amit", uri3);
//
//                            String download_uri =  task.getResult().toString();
//
//                            Log.i("Amit", download_uri);
//
//
//                        }
//                        else {
//                            Toast.makeText(SettingsActivity.this, "Error in uploading image", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });


                imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imagePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String uri = task.getResult().toString();
//                                Log.i("Amit", uri);

                                databaseReference.child("image").setValue(uri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        imageUploadProgress.dismiss();
                                    }
                                }
                            });
                            }
                        });
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
