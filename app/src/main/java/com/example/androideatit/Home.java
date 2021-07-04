package com.example.androideatit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.Common;
import Database.Database;
import Interface.ItemClickListener;
import Model.Banner;
import Model.Category;
import Model.Order;
import Service.ListenOrder;
import ViewHolder.MenuViewHolder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;


public class Home extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter ;

    SwipeRefreshLayout swipeRefreshLayout;

    CounterFab fab;





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
//

        setContentView(R.layout.activity_home);





        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //View

        swipeRefreshLayout=findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(Common.isConnectedToInternet(Home.this))
                {
                    loadMenu();
                }
                else
                {
                    Toast.makeText(Home.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        });

        //Default, load for first time

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet(Home.this))
                {
                    loadMenu();
                }
                else
                {
                    Toast.makeText(Home.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });


        //init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(category, Category.class)
                        .build();

        adapter = new
                FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MenuViewHolder menuViewHolder, int i, @NonNull Category category) {

                        menuViewHolder.txtMenuName.setText(category.getName());

                        //set font
                        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/restaurant_font.otf");
                        menuViewHolder.txtMenuName.setTypeface(face);


                        Picasso.get().load(category.getImage()).into(menuViewHolder.imageView);



                        menuViewHolder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View view, int position, boolean isLongClick) {

                                //Get CategoryId and send to the new Activity

                                Intent foodList = new Intent(Home.this,FoodList.class);

                                //Because, categoryId is key, so we just get key of this item

                                foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                                startActivity(foodList);



                            }
                        });


                    }

                    @NonNull
                    @Override
                    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false);
                        MenuViewHolder holder = new MenuViewHolder(view);
                        return holder;



                    }
                };





        Paper.init(this);

        fab = findViewById(R.id.fab);



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                startActivity(new Intent(getApplicationContext(),Cart.class));




            }
        });

        fab.setCount(new Database(this).getCountCart());




        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(Home.this);

        //Set Name for user

        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Load menu

        recycler_menu = findViewById(R.id.recycler_menu);

        //layoutManager = new LinearLayoutManager(this);
        //recycler_menu.setLayoutManager(layoutManager);

        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);

        recycler_menu.setLayoutAnimation(controller);


        //Register Service

        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);


        //Banner Slider

        ImageSlider imageSlider=findViewById(R.id.slider);

        List<SlideModel> slideModels=new ArrayList<>();


        slideModels.add(new SlideModel("https://i.pinimg.com/originals/24/9e/19/249e19e8eba672c8a726cdc2a37a91e4.jpg","CHAI KUIH"));
        slideModels.add(new SlideModel("https://d1alt1wkdk73qo.cloudfront.net/images/guide/ce28fb4275541dedef5f744e3c94d493/640x478_ac.jpg","YAM CAKE"));
        slideModels.add(new SlideModel("https://sparkpeo.hs.llnwd.net/e1/resize/630m620/e2/guid/Oven-Roasted-Chicken-Leg-Quarters/1aa45583-5e72-427b-b4b9-a4bc992343b3.jpg","ROASTED QUARTER CHICKEN"));
        imageSlider.setImageList(slideModels,true);


        imageSlider.setImageList(slideModels,true);


    }


    @Override
    protected void onStop() {
        super.onStop();

        adapter.stopListening();



    }

    private void loadMenu() {




        recycler_menu.setAdapter(adapter);
        adapter.startListening();
        swipeRefreshLayout.setRefreshing(false);

        //Animation

        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();




    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id==R.id.refresh)
        {
            loadMenu();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();



       if (id==R.id.nav_cart)
        {


            startActivity(new Intent(Home.this,Cart.class));

        }
        else if(id==R.id.nav_orders)
        {
            startActivity(new Intent(Home.this,OrderStatus.class));
        }
        else if(id==R.id.nav_log_out)
        {
            //Delete Remember user & password
            Paper.book().destroy();

            //logout
            Intent signIn=new Intent(Home.this,SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
            finish();



        }

        else if(id==R.id.nav_change_pwd)
       {
           showChangePasswordDialog();
       }

        else if(id==R.id.nav_home_address)
       {
           showHomeAddressDialog();

       }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);



        return true;
    }

    private void showHomeAddressDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("CHANGE HOME ADDRESS");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.home_address_layout,null);

        final  EditText edtHomeAddress = layout_home.findViewById(R.id.edtHomeAddress);

        alertDialog.setView(layout_home);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Set new Home Address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    Toast.makeText(Home.this, "Update Address Successfully!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });


            }

        });

        alertDialog.show(); // dont forget to show alertDialog!



    }

    private void showChangePasswordDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

        final EditText edtPassword = layout_pwd.findViewById(R.id.edtPassword);
        final EditText edtNewPassword= layout_pwd.findViewById(R.id.edtNewPassword);
        final EditText edtRepeatPaassword=layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Change password here

                //To use SpotsDialog, please use AlertDialog from  android.app

                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                //change old password

                if(edtPassword.getText().toString().equals(Common.currentUser.getPassword()))
                {
                    //check new password and repeat password

                    if(edtNewPassword.getText().toString().equals(edtRepeatPaassword.getText().toString()))
                    {
                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password",edtNewPassword.getText().toString());

                        //Make update
                        DatabaseReference user  = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, "Password Updated!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                    else
                    {
                        waitingDialog.dismiss();

                        Toast.makeText(Home.this, "New Password doesn't match!", Toast.LENGTH_SHORT).show();

                    }

                }
                else
                {
                    waitingDialog.dismiss();

                    Toast.makeText(Home.this, "Wrong old password!", Toast.LENGTH_SHORT).show();
                }




            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alertDialog.show();




    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart());

        //fix click back button from food and don't see category
        if(adapter!=null)
        {
            adapter.startListening();
        }



    }
}
