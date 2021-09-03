package com.qstest.data;

import androidx.core.content.ContextCompat;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.qstest.QSApp;

//package-private because connections must be made from here

class ConnUtils {

    private static ConnUtils sInstance;

    // TODO check thread safety
    public synchronized static ConnUtils getInstance() {
        if(sInstance == null) {
            sInstance = new ConnUtils();
        }
        return sInstance;
    }

    // Volley request queue
    private RequestQueue requestQueue;

    private ConnUtils() {
        initVolley();
    }

    private void initVolley() {
        Cache cache = new DiskBasedCache(QSApp.getContext().getCacheDir(), 1024*1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
