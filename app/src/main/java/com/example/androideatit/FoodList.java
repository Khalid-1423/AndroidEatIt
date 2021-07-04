package com.example.androideatit;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Common.Common;
import Database.Database;
import Interface.ItemClickListener;
import Model.Food;
import Model.Food;
import Model.Order;
import ViewHolder.FoodViewHolder;
import ViewHolder.MenuViewHolder;


public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId="";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Search Functionality

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List <String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites

    Database localDB;


    //fb share
    private CallbackManager callbackManager;
    private LoginManager loginManager;


    SwipeRefreshLayout swipeRefreshLayout;

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


        setContentView(R.layout.activity_food_list);

        //firebase
        database=FirebaseDatabase.getInstance();
        foodList=database.getReference("Foods");

        //local DB

        localDB = new Database(this);


        swipeRefreshLayout=findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get Intent here

                if(getIntent()!=null)
                {
                    categoryId = getIntent().getStringExtra("CategoryId");
                }
                if(!categoryId.isEmpty() && categoryId!=null)
                {
                    if(Common.isConnectedToInternet(getBaseContext()))
                    {
                        loadListFood(categoryId);
                    }
                    else
                    {
                        Toast.makeText(FoodList.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                }


            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                //Get Intent here

                if(getIntent()!=null)
                {
                    categoryId = getIntent().getStringExtra("CategoryId");
                }
                if(!categoryId.isEmpty() && categoryId!=null)
                {
                    if(Common.isConnectedToInternet(getBaseContext()))
                    {
                        loadListFood(categoryId);
                    }
                    else
                    {
                        Toast.makeText(FoodList.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                }

                //Search
                materialSearchBar =findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter your food");
                loadSuggest(); // write function to load suggest from firebase

                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        //When user type their text, we will change suggest list

                        List<String> suggest = new ArrayList<String>();
                        for(String search : suggestList)
                        {
                            if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                            {
                                suggest.add(search);
                            }
                        }
                        materialSearchBar.setLastSuggestions(suggest);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //when searchbar is close
                        //restore orginal adapter

                        if(!enabled)
                        {
                            recyclerView.setAdapter(adapter);
                        }

                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {

                        //when search finish
                        //show result of search adapter
                        startSeach(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });




            }
        });




        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);






        

    }

    private void startSeach(CharSequence text) {

        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(foodList.orderByChild("name").equalTo(text.toString()), Food.class)
                        .build();

        //like : Select * From Foods where menuId = categoryId

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, final int i, @NonNull Food food) {

                foodViewHolder.food_name.setText(food.getName());

                Picasso.get().load(food.getImage()).into(foodViewHolder.food_image);


                final Food local =  food;
                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        // start new Activity

                        Intent intent = new Intent(FoodList.this,FoodDetail.class);
                        intent.putExtra("FoodId",searchAdapter.getRef(position).getKey()); //send Food Id to new Activity
                        startActivity(intent);


                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                FoodViewHolder holder = new FoodViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();





    }

    private void loadSuggest()
    {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                        {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName()); //add name of the food to suggestList
                        }

                        materialSearchBar.setLastSuggestions(suggestList);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadListFood(String categoryId) {

        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(foodList.orderByChild("menuId").equalTo(categoryId), Food.class)
                        .build();

        //like : Select * From Foods where menuId = categoryId

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder foodViewHolder, final int i, @NonNull final Food food) {

                foodViewHolder.food_name.setText(food.getName());

                foodViewHolder.food_price.setText(String.format("$ %s",food.getPrice().toString()));

                //set font
                Typeface face = Typeface.createFromAsset(getAssets(),"fonts/restaurant_font.otf");
                foodViewHolder.food_name.setTypeface(face);
                foodViewHolder.food_price.setTypeface(face);




                Picasso.get().load(food.getImage()).into(foodViewHolder.food_image);

                //quick cart

                foodViewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new Database(getBaseContext()).addToCart(new Order(
                                adapter.getRef(i).getKey(),
                                food.getName(),
                                "1",
                                food.getPrice(),
                                food.getDiscount(),
                                food.getImage()
                        ));
                    }
                });

                Toast.makeText(FoodList.this, "Added to Cart!", Toast.LENGTH_SHORT).show();

                //Add Favorites

                if(localDB.isFavorite(adapter.getRef(i).getKey()))
                {
                    foodViewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                }

                //click for share

                foodViewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // ---------------fb share start herer------------------------
                        FacebookSdk.sdkInitialize(getApplicationContext());

                        callbackManager = CallbackManager.Factory.create();

                        List<String> permissionNeeds = Arrays.asList("publish_actions");


                        //this loginManager helps you eliminate adding a LoginButton to your UI
                        LoginManager manager = LoginManager.getInstance();

                        manager.logInWithPublishPermissions(FoodList.this, permissionNeeds);

                        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
                        {
                            @Override
                            public void onSuccess(LoginResult loginResult)
                            {
                                sharePhotoToFacebook(food);                            }

                            @Override
                            public void onCancel()
                            {
                                System.out.println("onCancel");
                            }

                            @Override
                            public void onError(FacebookException exception)
                            {
                                System.out.println("onError");
                            }
                        });

                        // ---------------fb share end herer------------------------




                    }
                });



                //Click to change state of Favorities
                foodViewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!localDB.isFavorite(adapter.getRef(i).getKey()))
                        {
                            localDB.addToFavorites(adapter.getRef(i).getKey());
                            foodViewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+food.getName()+" was added to Favorites!", Toast.LENGTH_SHORT).show();
                            
                        }
                        else
                        {
                            localDB.removeFromFavorites(adapter.getRef(i).getKey());
                            foodViewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+food.getName()+" was removed to Favorites!", Toast.LENGTH_SHORT).show();

                        }



                    }
                });



                final Food local =  food;
                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                       // start new Activity

                        Intent intent = new Intent(FoodList.this,FoodDetail.class);
                        intent.putExtra("FoodId",adapter.getRef(position).getKey()); //send Food Id to new Activity
                        startActivity(intent);


                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                FoodViewHolder holder = new FoodViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

        swipeRefreshLayout.setRefreshing(false);



    }

    private void sharePhotoToFacebook(Food food){
        Bitmap image = BitmapFactory.decodeResource(getResources(),Integer.parseInt(food.getImage()));
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .setCaption("Give me my codez or I will ... you know, do that thing you don't like!")
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        ShareApi.share(content, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
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
