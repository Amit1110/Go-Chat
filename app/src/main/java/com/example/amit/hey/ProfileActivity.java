package com.example.amit.hey;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileDisplayName, profileStatus, profileFriendsCount;

    private Button profileSendButton, profileDeclineButton;
    private ImageView profileImageView;
    private DatabaseReference userReference;
    private ProgressDialog progressDialog;

    private String current_state;


    private DatabaseReference friendRequestReference;
    private FirebaseUser currentUser;

    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference rootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        final String userId = getIntent().getStringExtra("userId");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        rootReference = FirebaseDatabase.getInstance().getReference();


        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileDisplayName = findViewById(R.id.profile_display_name);
        profileStatus = findViewById(R.id.profile_display_status);
        profileFriendsCount = findViewById(R.id.profile_total_friends);
        profileImageView = findViewById(R.id.profile_image_view);
        profileSendButton = findViewById(R.id.profile_request_button);
        profileDeclineButton = findViewById(R.id.profile_decline_button);

        current_state = "not_friends";
        profileDeclineButton.setVisibility(View.INVISIBLE);
        profileDeclineButton.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load user data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileDisplayName.setText(display_name);
                profileStatus.setText(status);
                Picasso.with(getApplicationContext()).load(image).into(profileImageView);

                //-----------------------Friends LIst/ Request Feature ----------------- //

                friendRequestReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId)){
                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                current_state = "req_received";
                                profileSendButton.setText("ACCEPT FRIEND REQUEST");

                                profileDeclineButton.setVisibility(View.VISIBLE);
                                profileDeclineButton.setEnabled(true);

                            }else  if(req_type.equals("sent")){

                                current_state = "req_sent";
                                profileSendButton.setText("CANCEL FRIEND REQUEST");

                                profileDeclineButton.setVisibility(View.INVISIBLE);
                                profileDeclineButton.setEnabled(false);

                            }
                            progressDialog.dismiss();

                        }
                        else {
                            friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)){
                                        Log.i("Amit", "I am here!");
                                        current_state = "friends";
                                        profileSendButton.setText("UNFRIEND This Person");

                                        profileDeclineButton.setVisibility(View.INVISIBLE);
                                        profileDeclineButton.setEnabled(false);
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progressDialog.dismiss();

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                profileSendButton.setEnabled(false);


//                -------NOT FRIENDS  SEND Request ---------------------
                if(current_state.equals("not_friends")){

                    DatabaseReference notificationReference = rootReference.child("notifications").child(userId).push();
                    String newNotificationId = notificationReference.getKey();


                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");


                    Map<String,Object> requestMap = new HashMap<>();
                    //Map requestMap = new HashMap();
                    requestMap.put("Friend_Requests/" + currentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("Friend_Requests/" + userId + "/" + currentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + userId + "/" + newNotificationId, notificationData);

                    rootReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    if(databaseError != null){
                                        Log.i("Amit","as");
                                        Log.i("Amit", databaseError.getDetails());
                                        Toast.makeText(ProfileActivity.this, "There was error sending the request", Toast.LENGTH_SHORT);
                                    }

                                    profileSendButton.setEnabled(true);
                                    current_state = "req_Sent";
                                    profileSendButton.setText("Cancel Friend Request");

                                }

                            });
                }

                //-------------------REQUEST SENT  Cancle Request----------------------

                else if(current_state.equals("req_sent")){

                    friendRequestReference.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendRequestReference.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    profileSendButton.setEnabled(true);
                                    current_state = "not_friends";
                                    profileSendButton.setText("SEND FRIEND REQUEST");

                                    profileDeclineButton.setVisibility(View.INVISIBLE);
                                    profileDeclineButton.setEnabled(false);
                                }
                            });
                        }
                    });

                }

                // ---------------------REQUEST RECEIVED STATE--------------------------

                else if(current_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    friendDatabase.child(currentUser.getUid()).child(userId).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabase.child(userId).child(currentUser.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    friendRequestReference.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            friendRequestReference.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    profileSendButton.setEnabled(true);
                                                    current_state = "friends";
                                                    profileSendButton.setText("UNFRIEND This Person");

                                                    profileDeclineButton.setVisibility(View.INVISIBLE);
                                                    profileDeclineButton.setEnabled(false);
                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        }
                    });

                }

                // -------------------------------FRIENDS -- To Unfriend person-------------------------------
                else if(current_state.equals("friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + currentUser.getUid() + "/" + userId, null);
                    unfriendMap.put("Friends/" +  userId + "/" + currentUser.getUid(), null);

                    rootReference.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError==null){
                                current_state = "not_friends";

                                profileSendButton.setText("Send Friend Request");

                                profileDeclineButton.setVisibility(View.INVISIBLE);
                                profileDeclineButton.setEnabled(false);

                            }
                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error , Toast.LENGTH_SHORT);
                            }

                            profileSendButton.setEnabled(true);
                        }
                    });

                }
            }
        });


    }
}











//change all nested update statements to updateChildren to decrease time lag
















