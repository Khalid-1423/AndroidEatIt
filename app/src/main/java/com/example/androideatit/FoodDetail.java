package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import Common.Common;
import Database.Database;
import Model.Food;
import Model.Order;
import Model.Rating;
import info.hoang8f.widget.FButton;


public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name , food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    ElegantNumberButton numberButton;

    RatingBar ratingBar;

    String foodId="";

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;

    FButton btnShowComment;

    Food currentFood;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //add this code before setContentView

//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/of.otf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );

        setContentView(R.layout.activity_food_detail);



        //Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTbl=database.getReference("Rating");

        //init View
        numberButton= findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnRating = findViewById(R.id.btn_rating);
        ratingBar=findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()

                ));

                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();



            }
        });

        btnCart.setCount(new Database(this).getCountCart());

        food_description = findViewById(R.id.food_description);
        food_name=findViewById(R.id.food_name);
        food_price=findViewById(R.id.food_price);

        //set font
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/restaurant_font.otf");
        food_description.setTypeface(face);
        food_name.setTypeface(face);
        food_price.setTypeface(face);

        food_image=findViewById(R.id.img_food);

        collapsingToolbarLayout=findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Food Id from Intent
        
        if(getIntent()!=null)
        {
            foodId = getIntent().getStringExtra("FoodId");
        }

        if(!foodId.isEmpty())
        {
            if(Common.isConnectedToInternet(getBaseContext()))
            {
                getDetailFood(foodId);
                getRatingFood(foodId);
            }
            else
            {
                Toast.makeText(this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        btnShowComment = findViewById(R.id.btnShowComment);
        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                startActivity(intent);





            }

        });


    }

    private void getRatingFood(String foodId)
    {
        com.google.firebase.database.Query foodRating =ratingTbl.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0, sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+= Integer.parseInt(item.getRateValue());
                    count++;
                }

              if(count!=0)
              {
                  float average = sum/count;
                  ratingBar.setRating(average);
              }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void showRatingDialog() {

       new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
               .setTitleTextColor(R.color.colorPrimary)
               .setDescriptionTextColor(R.color.colorPrimary)
               .setHint("Please write your comment here...")
               .setHintTextColor(R.color.colorAccent)
               .setCommentTextColor(android.R.color.white)
               .setCommentBackgroundColor(R.color.colorPrimaryDark)
               .setWindowAnimation(R.style.RatingDialogFadeAnim)
               .create(FoodDetail.this)
               .show();




    }
    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {

        //Get Rating and Upload to Firebase

        final Rating rating = new Rating(Common.currentUser.getPhone()
        ,foodId,String.valueOf(value),comments);

        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(FoodDetail.this, "Thank you for submit ratint!", Toast.LENGTH_SHORT).show();
                        
                    }
                });

        /*
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //remove old value (you can delete or let it be -- useless function
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //Update new Value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);

                }
                else
                {
                    //Update new Value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);

                }
                Toast.makeText(FoodDetail.this, "Thank you for submitting Rating!", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); */



    }

    private void getDetailFood(final String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentFood = dataSnapshot.getValue(Food.class);

                //Get Image
                Picasso.get().load(currentFood.getImage()).into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());

                food_description.setText(currentFood.getDescription());





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}
