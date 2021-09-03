package com.qstest.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.qstest.R;
import com.qstest.data.models.ProductModel;
import com.qstest.databinding.ProductCardLayoutBinding;

import mva3.adapter.ItemBinder;
import mva3.adapter.ItemViewHolder;

public class FeedAdapter extends ItemBinder<LiveData<ProductModel>, FeedAdapter.ProductCardViewHolder> {

    private LifecycleOwner lifecycleOwner;
    FeedAdapter(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public ProductCardViewHolder createViewHolder(ViewGroup parent) {
        ProductCardLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.product_card_layout, parent, false);
        return new ProductCardViewHolder(binding);
    }

    @Override
    public void bindViewHolder(ProductCardViewHolder holder, LiveData<ProductModel> item) {
        /*
        FIXME make cast more robust by checking context instance before cast
        (probably unnecessary)
        Note that this LiveData reference is lost when LifecycleOwner expires
         */
        item.removeObservers(lifecycleOwner);
        item.observe(lifecycleOwner, productModel -> {
            holder.binding.setProductModel(new ProductModel(productModel));
            if(item.getValue() != null && item.getValue().getPrice() != null && !item.getValue().getPrice().equals("-")) {
                holder.binding.productPrice.setText(
                        holder.binding.getRoot().getContext().getResources().getString(R.string.rupees) + " " + item.getValue().getPrice()
                );
            }
            else {
                holder.binding.productPrice.setText("-");
            }
            if(productModel.getName() != null && productModel.getDesc() != null && productModel.getPrice() != null) {
                holder.binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean canBindData(Object item) {
        return item instanceof LiveData;
    }


    public static class ProductCardViewHolder extends ItemViewHolder<LiveData<ProductModel>> {

        ProductCardLayoutBinding binding;

        public ProductCardViewHolder(ProductCardLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ProductCardLayoutBinding getBinding() {
            return binding;
        }


    }
}
