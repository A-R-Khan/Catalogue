package com.qstest.data;

public class Repository {

    private final ProductDataSource productDataSource;

    /*
     1. Since this is a small app, static instances are okay
        Dependency injection would be overkill
     2. productDataSource is tracking listeners, making multiple instances
        of Repository would create problems there even though Repository on its own is stateless
    */
    private static Repository sInstance;

    private Repository() {
        productDataSource = new ProductDataSourceFirebase();
    }

    //TODO check thread safety
    public static synchronized Repository getInstance() {
        if(sInstance == null) {
            sInstance = new Repository();
        }
        return sInstance;
    }

    public ProductDataSource getProductDataSource() {
        return productDataSource;
    }
}
