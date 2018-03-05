package ke.co.kizzi.scanitmerchant.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ke.co.kizzi.scanitmerchant.R;

/**
 * Created by john on 30/07/2017.
 */

public class ProductsRecyclerAdapter extends  RecyclerView.Adapter<ProductsRecyclerHolder> {

    private String[] name;
    private String[] id_number;
    private String[] date;

    Context context;
    LayoutInflater inflater;
    public ProductsRecyclerAdapter(Context context, String[] id_number, String[] name,
                                   String[] date) {
        this.context=context;
        this.id_number = id_number;
        this.date = date;
        this.name = name;

        inflater= LayoutInflater.from(context);
    }
    @Override
    public ProductsRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflater.inflate(R.layout.products_list_item, parent, false);

        ProductsRecyclerHolder viewHolder=new ProductsRecyclerHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ProductsRecyclerHolder holder, int position) {

        holder.txtName.setText("Name: "+name[position]);
        holder.txtPrice.setText("Price: "+id_number[position]+"/=");
        holder.txtDescription.setText(date[position]);
    }

    @Override
    public int getItemCount() {
        return name.length;
    }



}
