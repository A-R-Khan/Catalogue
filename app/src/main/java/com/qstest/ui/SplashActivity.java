package com.qstest.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.qstest.R;
import com.qstest.data.Repository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout);
        downloadData();
    }

    public void downloadData() {
        Repository.getInstance().getProductDataSource()
                .downloadIds()
                .observe(this, booleanLiveEvent -> {
                    if(booleanLiveEvent == null) {
                        return;
                    }
                    Boolean res = booleanLiveEvent.getDataOnceAndReset();
                    if(Boolean.TRUE.equals(res)) {
                        Intent i = new Intent(this, HomeActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle(R.string.splash_error_title)
                                .setBackground(getDrawable(R.drawable.black_shape))
                                .setMessage(R.string.splash_error_message)
                                .setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .create()
                                .show();
                    }
                });

    }
}
