package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Common.Common;
import Model.User;


public class SignUp extends AppCompatActivity {

    private EditText edtPhone, edtName, edtPassword, edtSecureCode;
    private Button btnSignUp;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //add this code before setContentView
//
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/of.otf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );

        setContentView(R.layout.activity_sign_up);

        edtName = findViewById(R.id.edtName);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone=findViewById(R.id.edtPhone);
        edtSecureCode=findViewById(R.id.edtSecureCode);

        btnSignUp=findViewById(R.id.btnSignUp);

        //set font
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/restaurant_font.otf");
        edtName.setTypeface(face);
        edtPassword.setTypeface(face);
        edtPhone.setTypeface(face);
        edtSecureCode.setTypeface(face);
        btnSignUp.setTypeface(face);

        //init Firebase

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Common.isConnectedToInternet(getBaseContext())) {
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("Please waiting........");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //check if already user phone
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "User already exists!", Toast.LENGTH_SHORT).show();
                            } else {
                                mDialog.dismiss();
                                User user = new User(edtName.getText().toString(),
                                        edtPassword.getText().toString(),
                                        edtSecureCode.getText().toString());

                                table_user.child(edtPhone.getText().toString()).setValue(user);
                                Toast.makeText(SignUp.this, "Sign up Successfully!", Toast.LENGTH_SHORT).show();
                                finish();

                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else
                {
                    Toast.makeText(SignUp.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                    return;
                }



            }
        });


    }
}
