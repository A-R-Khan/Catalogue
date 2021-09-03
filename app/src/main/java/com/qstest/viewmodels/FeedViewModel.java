package com.qstest.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qstest.data.Repository;
import com.qstest.data.models.ProductModel;
import com.qstest.utils.LiveEvent;

import java.util.ArrayList;
import java.util.List;

public class FeedViewModel extends ViewModel {

    private enum LoadStatus {
        LOADED_PAGE,
        LOADED_ALL,
        ERROR;
    }

    private static final int DEFAULT_PAGE_SIZE = 10;

    //CACHE AND NOTIFY
    private final List<LiveData<ProductModel>> products = new ArrayList<>();

    public FeedViewModel() {
    }

    public List<LiveData<ProductModel>> getNextProducts() {
        return getNextProducts(DEFAULT_PAGE_SIZE);
    }

    /*
    TODO make it non blocking by using live data if disk access is done
    Looks convoluted but there seems to be no other way
    */
    public List<LiveData<ProductModel>> getNextProducts(int pageSize) {
         List<LiveData<ProductModel>> loadedProducts = Repository.getInstance().getProductDataSource().getProducts(
                products.size() == 0 ? null : products.get(products.size() - 1).getValue().getId(),
                DEFAULT_PAGE_SIZE
         );
         products.addAll(loadedProducts);
         Log.e("FVM", "returned "+loadedProducts.size()+" products");
         return loadedProducts;
    }

    //To be called on configuration changes, etc so that data does not need to be reloaded
    public List<LiveData<ProductModel>>  getLoadedProducts() {
        return products;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
