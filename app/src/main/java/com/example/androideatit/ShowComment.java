package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import Common.Common;
import Model.Rating;
import ViewHolder.ShowCommentViewHolder;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;

    String foodId="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);

        //Firebase
        database=FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = findViewById(R.id.recyclerComment);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


                if(getIntent()!=null)
                {
                    foodId=getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }
                if(!foodId.isEmpty() && foodId!=null)
                {
                    if(Common.isConnectedToInternet(getBaseContext()))
                    {
                        loadComment(foodId);
                    }
                    else
                    {
                        Toast.makeText(ShowComment.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }









    }

    private void loadComment(String foodId) {

        //create request query



        FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                .setQuery(ratingTbl.orderByChild("foodId").equalTo(foodId),Rating.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull Rating rating) {

                showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRateValue()));
                showCommentViewHolder.txtComment.setText(rating.getComment());
                showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());


            }

            @NonNull
            @Override
            public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_comment_layout,parent,false);

                ShowCommentViewHolder  holder= new ShowCommentViewHolder(view);

                return  holder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();





    }


    @Override
    protected void onResume() {
        super.onResume();

        if(adapter!=null)
        {
            adapter.startListening();
        }
    }





}
