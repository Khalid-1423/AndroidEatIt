package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import Common.Common;
import Model.User;
import info.hoang8f.widget.FButton;
import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    private FButton btnSignIn, btnSignUp;
    private TextView txtSlogan;

    String hashKey ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 //       add this code before setContentView
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/restaurant_font.otf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );
//

        setContentView(R.layout.activity_main);









        btnSignIn=findViewById(R.id.btnSignIn);
        btnSignUp=findViewById(R.id.btnSignUp);

        txtSlogan=findViewById(R.id.txtSlogan);

        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtSlogan.setTypeface(face);

        //Init Paper

        Paper.init(this);






        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {





                Intent intent = new Intent(MainActivity.this,SignIn.class);
                startActivity(intent);

            }
        });


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SignUp.class);
                startActivity(intent);


            }
        });


        //Check remember

        String user = Paper.book().read(Common.USER_KEY);
        String pwd  = Paper.book().read(Common.PWD_KEY);

       if(user!=null && pwd!=null)
       {
           if(!user.isEmpty() && !pwd.isEmpty())
           {
               login(user,pwd);
           }

       }


    }



    private void login(final String phone, final String pwd) {

        //Init Firebase

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        if(Common.isConnectedToInternet(getBaseContext()))
        {

            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please waiting........");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Check is user not exist in database

                    if(dataSnapshot.child(phone).exists())
                    {
                        //Get User Information
                        mDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone); //set Phone

                        if(user.getPassword().equals(pwd))
                        {
                            //Toast.makeText(SignIn.this, "Sign in successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this,Home.class);
                            Common.currentUser=user;
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else
                    {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User not exist in Database!", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else
        {
            Toast.makeText(MainActivity.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
            return;

        }
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }
}
