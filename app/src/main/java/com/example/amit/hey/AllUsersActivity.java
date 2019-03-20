package com.example.amit.hey;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private DatabaseReference usersDatabase;

    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        toolbar = findViewById(R.id.users_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = findViewById(R.id.users_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(usersDatabase, Users.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {


                holder.user_display_name.setText(model.getName());
                holder.user_status.setText(model.getStatus());
                Picasso.with(getApplicationContext()).load(model.getImage()).into(holder.userImage);

                final String userId = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);

                        profileIntent.putExtra("userId",userId);
                        startActivity(profileIntent);

                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_layout,viewGroup, false);

                return new UserViewHolder(view);
            }


        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public TextView user_display_name;
        public TextView user_status;
        public CircleImageView userImage;

        public UserViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            user_display_name = mView.findViewById(R.id.user_single_name);
            userImage = mView.findViewById(R.id.user_single_image);
            user_status = mView.findViewById(R.id.user_single_status);
        }
    }
}
