package ke.co.kizzi.scanitmerchant.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ke.co.kizzi.scanitmerchant.R;

/**
 * Created by john on 30/07/2017.
 */

public class ProductsRecyclerHolder extends RecyclerView.ViewHolder {

    TextView txtName,txtPrice,txtDescription;
    public ProductsRecyclerHolder(View itemView) {
        super(itemView);

        txtName= (TextView) itemView.findViewById(R.id.txtName);
        txtPrice= (TextView) itemView.findViewById(R.id.txtPrice);
        txtDescription= (TextView) itemView.findViewById(R.id.txtDescription);
    }
}
