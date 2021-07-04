package ViewHolder;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.androideatit.Cart;
import com.example.androideatit.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Common.Common;
import Database.Database;
import Interface.ItemClickListener;
import Model.Order;

class CartViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener, View.OnCreateContextMenuListener
{
    public TextView txt_cart_name , txt_price;
    public ElegantNumberButton btn_quantiy;
    public ImageView cart_image;

    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name)

    {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_cart_name = itemView.findViewById(R.id.cart_item_name);
        txt_price = itemView.findViewById(R.id.cart_item_Price);
        btn_quantiy = itemView.findViewById(R.id.btn_quantity);
        cart_image=itemView.findViewById(R.id.cart_image);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Select action");
        menu.add(0,0,getAdapterPosition(), Common.DELETE);


    }
}

public class CartAdapter extends  RecyclerView.Adapter<CartViewHolder>
{

    private List<Order> listData = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData1, Cart cart) {
        this.listData = listData1;
        this.cart = cart;
    }



    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView = inflater.inflate(R.layout.cart_layout , parent ,false);

        return  new CartViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, final int position) {

//        TextDrawable drawable = TextDrawable.builder()
//                                .buildRound(""+listData.get(position).getQuantity(), Color.RED);
//
//        holder.btn_quantiy.setImageDrawable(drawable);


        Picasso.get().load(listData.get(position).getImage()).into(holder.cart_image);


        holder.btn_quantiy.setNumber(listData.get(position).getQuantity());

        holder.btn_quantiy.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {

                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //update total price

                //Calculate total price
                int total=0;

                List<Order> orders = new Database(cart).getCarts();

                for(Order item : orders)
                {
                    total+=(Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                }
                Locale locale = new Locale("en","US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                cart.txtTotalPrice.setText(fmt.format(total));


            }
        });

        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(position).getPrice())) * (Integer.parseInt(listData.get(position).getQuantity()));

        holder.txt_price.setText(fmt.format(price));
        holder.txt_cart_name.setText(listData.get(position).getProductName());


    }

    @Override
    public int getItemCount()
    {
        return listData.size();
    }
}
