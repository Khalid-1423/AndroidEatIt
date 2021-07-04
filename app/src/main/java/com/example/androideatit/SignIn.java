package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import Common.Common;
import Model.User;
import io.paperdb.Paper;


public class SignIn extends AppCompatActivity {

    private EditText edtPhone, edtPassword;
    private Button btnSingIn;

    private CheckBox ckbRemember;
    private TextView txtForgotPwd;
    FirebaseDatabase database;
     DatabaseReference table_user;


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


        setContentView(R.layout.activity_sign_in);

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnSingIn=findViewById(R.id.btnSignIn);
        ckbRemember = findViewById(R.id.ckbRemember);
        txtForgotPwd = findViewById(R.id.txtForgotPwd);


        //set font
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/restaurant_font.otf");
        edtPhone.setTypeface(face);
        ckbRemember.setTypeface(face);
        txtForgotPwd.setTypeface(face);
        btnSingIn.setTypeface(face);



        //Init Firebase

        Paper.init(this);


        //Init Firebase

         database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showForgotPwdDialog();

            }
        });



        btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Common.isConnectedToInternet(getBaseContext()))
                {
                    //save user & password

                    if(ckbRemember.isChecked())
                    {
                        Paper.book().write(Common.USER_KEY,edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                    }



                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Please waiting........");
                    mDialog.show();

                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Check is user not exist in database

                            if(dataSnapshot.child(edtPhone.getText().toString()).exists())
                            {
                                //Get User Information
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                                user.setPhone(edtPhone.getText().toString()); //set Phone

                                if(user.getPassword().equals(edtPassword.getText().toString()))
                                {
                                    //Toast.makeText(SignIn.this, "Sign in successfully!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignIn.this,Home.class);
                                    Common.currentUser=user;
                                    startActivity(intent);
                                    finish();

                                    table_user.removeEventListener(this); // why?

                                }
                                else
                                {
                                    Toast.makeText(SignIn.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                                }

                            }
                            else
                            {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "User not exist in Database!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
                else
                {
                    Toast.makeText(SignIn.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                    return;

                }



                }



        });




    }

    private void showForgotPwdDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();

        View forgot_view = inflater.inflate(R.layout.forgot_password_layout,null);

        builder.setView(forgot_view);
        builder.setIcon(R.drawable.ic_security_black_24dp);

        final EditText edtPhone = forgot_view.findViewById(R.id.edtPhone);
        final EditText edtSecureCode = forgot_view.findViewById(R.id.edtSecureCode);


        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //check If user available
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.child(edtPhone.getText().toString())
                                .getValue(User.class);
                        
                        if(user.getSecureCode().equals(edtSecureCode.getText().toString()))
                        {
                            Toast.makeText(SignIn.this, "Your password : "+user.getPassword(), Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(SignIn.this, "Wrong secure code!", Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }
}
