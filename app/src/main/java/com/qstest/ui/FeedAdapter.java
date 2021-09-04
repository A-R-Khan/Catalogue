package com.qstest.ui;

import androidx.annotation.NonNull;

import mva3.adapter.ItemViewHolder;
import mva3.adapter.MultiViewAdapter;

public class FeedAdapter extends MultiViewAdapter {
    @Override
    public void onViewDetachedFromWindow(@NonNull ItemViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if(holder instanceof FeedBinder.ProductCardViewHolder) {
            ((FeedBinder.ProductCardViewHolder) holder).clearAnimation();
        }
    }
}
