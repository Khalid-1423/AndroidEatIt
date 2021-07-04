package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Common.Common;
import Database.Database;
import Model.Order;
import Model.Request;
import ViewHolder.CartAdapter;


public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public  TextView txtTotalPrice;
    Button btnPlace;

    List <Order> cart = new ArrayList<>();

    CartAdapter adapter;


//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cart);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = findViewById(R.id.total);
        btnPlace=findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if(cart.size() > 0)
              {
                  showAlertDialog();
              }
              else
              {
                  Toast.makeText(Cart.this, "Your cart is empty!!!", Toast.LENGTH_SHORT).show();
              }

            }

        });


       loadListFood();



    }

    private void showAlertDialog()
    {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        final EditText edtAddress = order_address_comment.findViewById(R.id.edtAddress);
        final EditText edtComment = order_address_comment.findViewById(R.id.edtComment);

        final RadioButton rdiHomeAddress = order_address_comment.findViewById(R.id.rdiHomeAddress);

        final RadioButton rdiCOD = order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal = order_address_comment.findViewById(R.id.rdiPaypal);



        //Even Radio

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    if(Common.currentUser.getHomeAddress() !=null || !TextUtils.isEmpty(Common.currentUser.getHomeAddress()))
                    {
                        String address = Common.currentUser.getHomeAddress();
                        edtAddress.setText(address);

                    }
                    else
                    {
                        Toast.makeText(Cart.this, "Please update your Home Address", Toast.LENGTH_SHORT).show();
                    }





                }


            }
        });

        //Check payment

        if(!rdiCOD.isChecked() && !rdiPaypal.isChecked())
        {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
        else if(rdiPaypal.isChecked())
        {

        }
        else if(rdiCOD.isChecked())
        {
            String address = Common.currentUser.getHomeAddress();

            Request request = new Request(
                    Common.currentUser.getPhone(),
                    Common.currentUser.getName(),
                    address,
                    txtTotalPrice.getText().toString(),
                    "0", //status
                    edtComment.getText().toString(),
                    "COD",
                    cart
            );

            //submit to firebase

            String order_number = String.valueOf(System.currentTimeMillis());
            requests.child(order_number).setValue(request);

            new Database(getBaseContext()).cleanCart();
            Toast.makeText(this, "Thank you , Order Place", Toast.LENGTH_SHORT).show();
            finish();


        }


        alertDialog.setView(order_address_comment);

        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Create new Request
                //create new request

                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        "0", //status
                        edtComment.getText().toString(),
                        "Paypal",
                        cart
                );

                //submut to Firebase
                //we will be using System.CurrentMilli to key


                requests.child(String.valueOf(System.currentTimeMillis()))
                        .setValue(request);

                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank you, Order Placed!", Toast.LENGTH_SHORT).show();
                finish();


            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertDialog.show();


    }

    private void loadListFood() {


        cart = new Database(this).getCarts();

        adapter = new CartAdapter(cart,this);

        adapter.notifyDataSetChanged();

        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total=0;

        for(Order order : cart)
        {
            total+=(Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        }
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));



    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getTitle().equals(Common.DELETE))

            deleteCart(item.getOrder());
            return true;

        
    }

    private void deleteCart(int position) {
        //we'll remove item at List<Order> by position
        cart.remove(position);

        //After that, we'll delete all old data from SQLite

        new  Database(this).cleanCart();

        //And finally, we'll update new data from List<Order> to SQLite

        for(Order item : cart)
        {
            new Database(this).addToCart(item);
        }

        //Refresh

        loadListFood();

    }
}
