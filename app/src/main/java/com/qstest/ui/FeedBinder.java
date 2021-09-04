package com.qstest.ui;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.qstest.R;
import com.qstest.data.models.ProductModel;
import com.qstest.databinding.ProductCardLayoutBinding;

import mva3.adapter.ItemBinder;
import mva3.adapter.ItemViewHolder;

public class FeedBinder extends ItemBinder<LiveData<ProductModel>, FeedBinder.ProductCardViewHolder> {

    private final LifecycleOwner lifecycleOwner;
    private boolean shouldAnimate = true;

    FeedBinder(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    // To make animation better
    private int lastPosition = -1;

    @Override
    public ProductCardViewHolder createViewHolder(ViewGroup parent) {
        ProductCardLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.product_card_layout, parent, false);
        return new ProductCardViewHolder(binding);
    }

    @Override
    public void bindViewHolder(ProductCardViewHolder holder, LiveData<ProductModel> item) {

        Animation animation = null;
        if(shouldAnimate) {
            animation = setAnimation(holder, holder.getLayoutPosition());
        }

        /*
        FIXME make cast more robust by checking context instance before cast
        (probably unnecessary)
        Note that this observer reference is lost when LifecycleOwner expires
         */
        item.removeObservers(lifecycleOwner);

        Animation finalAnimation = animation;
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
                if(finalAnimation != null && finalAnimation.hasEnded()) {
                    holder.binding.progressBar.setVisibility(View.GONE);
                } else if(finalAnimation == null) {
                    //TODO combine logical statements
                    holder.binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private Animation setAnimation(ProductCardViewHolder v, int position) {
        // Only animates if it came to the screen first time
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(v.itemView.getContext(), android.R.anim.slide_in_left);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (v.getItem().getValue() != null) {
                        ProductModel model = v.getItem().getValue();
                        if(!(model.getName() == null && model.getPrice() == null && model.getDesc() == null && model.getImage() == null)) {
                            v.binding.progressBar.setVisibility(View.GONE);
                        }
                    }

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            v.itemView.startAnimation(animation);
            lastPosition = position;
            return animation;
        }
        return null;
    }

    @Override
    public boolean canBindData(Object item) {
        return item instanceof LiveData;
    }

    public void setShouldAnimate(boolean shouldAnimate) {
        this.shouldAnimate = shouldAnimate;
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

        public void clearAnimation() {
            binding.getRoot().clearAnimation();
        }
    }
}
