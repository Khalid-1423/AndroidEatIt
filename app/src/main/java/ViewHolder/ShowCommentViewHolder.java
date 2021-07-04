package ViewHolder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androideatit.R;

import org.w3c.dom.Text;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtUserPhone, txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(@NonNull View itemView) {
        super(itemView);
        txtComment=itemView.findViewById(R.id.txtComment);
        txtComment=itemView.findViewById(R.id.txtUserPhone);
        ratingBar=itemView.findViewById(R.id.ratingBar);




    }
}
