package com.qstest.data;

import androidx.lifecycle.LiveData;

import com.qstest.data.models.ProductModel;
import com.qstest.utils.LiveEvent;

import java.util.List;

public interface ProductDataSource {
    public LiveData<LiveEvent<Boolean>> downloadIds();
    public List<String> getProductIdsAfter(String id, int pageSize);
    public LiveData<ProductModel> getProduct(String id);
    public List<LiveData<ProductModel>> getProducts(String lastId, int pageSize);
}
