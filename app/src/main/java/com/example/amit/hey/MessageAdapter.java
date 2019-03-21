package com.example.amit.hey;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;


    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_single_layout, viewGroup, false);

        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView messageName;
        public TextView messageTime;



        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_image_layout);
            messageName = itemView.findViewById(R.id.name_text_layout);
            messageTime = itemView.findViewById(R.id.time_text_layout);


        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {


        String currentUserId = mAuth.getCurrentUser().getUid();
        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();

        userDatabase.child(from_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue().toString();
                messageViewHolder.messageName.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if(from_user.equals(currentUserId)){
            messageViewHolder.messageText.setBackgroundColor(Color.WHITE);
            messageViewHolder.messageText.setTextColor(Color.BLACK);

        }
        else {
            messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            messageViewHolder.messageText.setTextColor(Color.WHITE);
        }


        messageViewHolder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}



























