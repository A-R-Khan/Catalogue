package com.qstest.utils;


import android.graphics.PorterDuff;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qstest.R;

public class CustomBindings {

    /*
    Loads with http url. Otherwise there are problems with caching
    */
    @BindingAdapter({"remoteImageHttp"})
    public static void loadImageUrl(ImageView view, String httpUrl) {

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(view.getContext());
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.setAlpha(100);
        circularProgressDrawable.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_OVER);
        circularProgressDrawable.start();

        GlideApp.with(view.getContext())
                .load(httpUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.error_drawable)
                .into(view);
    }

}