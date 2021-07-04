package ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androideatit.R;

import Interface.ItemClickListener;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

    public TextView food_name , food_price;
    public ImageView food_image , fav_image , share_image , quick_cart;

    public ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);


        food_name = itemView.findViewById(R.id.food_name);
        food_image=itemView.findViewById(R.id.food_image);
        fav_image=itemView.findViewById(R.id.fav);
        share_image=itemView.findViewById(R.id.btnShare);
        food_price=itemView.findViewById(R.id.food_price);
        quick_cart=itemView.findViewById(R.id.btn_quick_cart);



        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);


    }
}
