package com.ti.jguillendev.artbycoins;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ti.jguillendev.utils.InAppPurchaseSimpleProduct;

import java.util.ArrayList;

public class AppPurchaseProductsRecyclerViewAdapter extends RecyclerView.Adapter<AppPurchaseProductsRecyclerViewAdapter.AppPurchaseProductsViewHolder> {


    private ArrayList<InAppPurchaseSimpleProduct> _appPurchaseProductList;
    private OnItemTapListener _onAppPurchaseProductTapListener;
    private OnItemLongTapListener _onAppPurchaseProductLongTapListener;



    public interface  OnItemTapListener {

        void onItemClicked(int position);
    }

    public interface  OnItemLongTapListener {

        void onItemLongClicked(int position);
    }

    public void setOnAppPurchaseProductTapListener(OnItemTapListener listener) {

        this._onAppPurchaseProductTapListener = listener;

    }

    public void setOnAppPurchaseProductLongTapListener(OnItemLongTapListener longListener) {

        this._onAppPurchaseProductLongTapListener = longListener;
    }




    public static class AppPurchaseProductsViewHolder extends  RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView sku;
        public TextView price;

        public AppPurchaseProductsViewHolder(View itemView, final OnItemTapListener listener, final OnItemLongTapListener longListener) {
            super(itemView);

            this.imageView =  itemView.findViewById(R.id.imageView);
            this.sku = itemView.findViewById(R.id.title);
            this.price = itemView.findViewById(R.id.subTitle);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    //hacer algo
                    if(longListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            longListener.onItemLongClicked(position);
                        }
                    }
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){

                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClicked(position);
                        }
                    }

                }
            });
        }
    }

    public AppPurchaseProductsRecyclerViewAdapter(ArrayList<InAppPurchaseSimpleProduct> appPurchaseProducts){

        this._appPurchaseProductList = appPurchaseProducts;

    }

    @NonNull
    @Override
    public AppPurchaseProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.in_app_purchase_product_item, parent,false);
        AppPurchaseProductsViewHolder productViewHolder = new AppPurchaseProductsViewHolder(v, _onAppPurchaseProductTapListener, _onAppPurchaseProductLongTapListener);
        return productViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AppPurchaseProductsViewHolder holder, int position) {

        InAppPurchaseSimpleProduct currentItem =
                _appPurchaseProductList.get(position);
        holder.imageView.setImageResource(currentItem.getpImageResource());
        holder.sku.setText(currentItem.getpSku());
        holder.price.setText(currentItem.getpPrice());
    }

    @Override
    public int getItemCount() {
        return _appPurchaseProductList.size();
    }
}
