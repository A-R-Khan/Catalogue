package com.qstest.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qstest.data.models.ProductModel;
import com.qstest.utils.AggregatorLiveData;
import com.qstest.utils.LiveEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProductDataSourceFirebase implements ProductDataSource {

    private final Map<String, ValueEventListener> listenerTracker;

    // Since less number of product ids, it makes more sense to keep in memory than disk
    private final Map<String, Integer> idToPositionMap;
    private final ArrayList<String> idsSortedArray;

    ProductDataSourceFirebase() {
        listenerTracker = new HashMap<>();
        idToPositionMap = new HashMap<>();
        idsSortedArray = new ArrayList<>();
    }

    public LiveData<LiveEvent<Boolean>> downloadIds() {

        MutableLiveData<LiveEvent<Boolean>> resNotifier = new MutableLiveData<>();

        String url = "http://35.154.26.203/product-ids";
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
        response -> {
            // adding into the two-way lookup system
            for(int i = 0; i<response.length(); ++i) {
                try {
                    idsSortedArray.add((String) response.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                idToPositionMap.put(idsSortedArray.get(i), i);
            }
            resNotifier.setValue(new LiveEvent<>(true));
        },error -> {
            Log.e("PDS", "Error retrieving ids");
            error.printStackTrace();
            resNotifier.setValue(new LiveEvent<>(false));
        });
        ConnUtils.getInstance().getRequestQueue().add(jsonObjectRequest);

        return resNotifier;
    }

    @Override
    public List<String> getProductIdsAfter(String id, int pageSize) {

        if(id == null) {
            id = idsSortedArray.get(0);
        }
        int position = idToPositionMap.get(id);
        if(position == idsSortedArray.size()) {
            return new ArrayList<>();
        }
        return idsSortedArray.subList(position+1,
                // gymnastics
                Math.min(position + pageSize, idsSortedArray.size() - 1));
    }

    /*
    Interface to fake pagination although the actual calls are being done id by id
    TODO ensure non blocking if IDs stored on disk
    */
    @Override
    public List<LiveData<ProductModel>> getProducts(String lastId, int pageSize) {
        List<String> ids = getProductIdsAfter(lastId, pageSize);
        List<LiveData<ProductModel>> products = new ArrayList<>(pageSize);
        for (String id : ids) {
            products.add(getProduct(id));
        }
        return products;
    }

    /*
    Keep a reference to the returned LiveData at all times (or as long as required) in UI
    This gets automatically updated from cloud thanks to the AggregatorLiveData

    Pagination is very hard to implement efficiently on this particular DB schema
     */
    @Override
    public LiveData<ProductModel> getProduct(String id) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        ProductAggregatorLiveData productLiveData = new ProductAggregatorLiveData(id);

        // Just to be safe and prevent dangling connections
        if (id != null) removeProductValueEventListener(id);

        //Attach listeners to all fields
        for(ProductAttributeType type: ProductAttributeType.values()) {
            attachProductValueEventListener(id, type, productLiveData);
        }

        //Send a null model for pre-rendering and for further loading
        ProductModel tempModel = new ProductModel(id, null, null, null, null);
        productLiveData.setValue(tempModel);
        return productLiveData;
    }

    public void attachProductValueEventListener(String id, ProductAttributeType type, ProductAggregatorLiveData destination) {

        ProductValueEventListener productValueEventListener = new ProductValueEventListener(type, destination);
        FirebaseDatabase.getInstance().getReference()
                .child(type.getNode())
                .child(id)
                .addValueEventListener(productValueEventListener);

        //keeping track of listeners to remove them later
        listenerTracker.put(id + type.toString(), productValueEventListener);

    }

    /*
    ----!!!!!! THIS MUST BE CALLED EVERYTIME YOU REMOVE A PRODUCT FROM CACHE !!!!!!----
    i.e, if you might need to retrieve a product that was removed from memory, you MUST call this after removing or
    upon retrieving a second time
     */
    public void removeProductValueEventListener(String id) {

        try {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

            for (ProductAttributeType type : ProductAttributeType.values()) {
                if (listenerTracker.get(id + type.toString()) != null) {
                    dbRef.child(type.node).removeEventListener(listenerTracker.get(id + type.toString()));
                    listenerTracker.remove(id + type.toString());
                }
            }
        } catch (Exception e) {
        }
    }

    enum ProductAttributeType {

        NAME("name", "product-name"),
        DESC("desc", "product-desc"),
        IMAGE("image", "product-image"),
        PRICE("price", "product-price");

        private final String val;
        private final String node;

        ProductAttributeType(String val, String node) {
            this.val = val;
            this.node = node;
        }

        @NonNull
        @Override
        public String toString() {
            return val;
        }

        public String getNode() {
            return node;
        }
    }

    /*
    It just takes stuff from multiple sources and emits a value only when data is completely
    received
     */
    public static class ProductAggregatorLiveData extends AggregatorLiveData<ProductAttributeType, String, ProductModel> {

        String id;

        ProductAggregatorLiveData(String id) {
            this.id = id;
        }

        //formality
        @Override
        protected String mergeWithExistingData(ProductAttributeType typeofData, String oldData, String newData) {
            return newData;
        }

        /*
         only return if all are non-null (null in database retrieved values are replaced with hyphen
         just to be safe in UI later on and not face any surprise errors there)
        */
        @Override
        protected boolean checkDataForAggregability() {
            return dataOnHold.get(ProductAttributeType.NAME) != null
                    && dataOnHold.get(ProductAttributeType.DESC) != null
                    && dataOnHold.get(ProductAttributeType.IMAGE) != null
                    && dataOnHold.get(ProductAttributeType.PRICE) != null;
        }

        //this is the fun part
        @Override
        protected void aggregateData() {
            ProductModel productModel = new ProductModel(
                    id,
                    dataOnHold.get(ProductAttributeType.NAME),
                    dataOnHold.get(ProductAttributeType.DESC),
                    dataOnHold.get(ProductAttributeType.IMAGE),
                    dataOnHold.get(ProductAttributeType.PRICE)
            );
            setValue(productModel);
        }
    }

    /*
    Reduce clutter upstairs
     */
    private static class ProductValueEventListener implements ValueEventListener {

        private final AggregatorLiveData<ProductAttributeType, String, ProductModel> destination;
        private final ProductAttributeType type;

        ProductValueEventListener(ProductAttributeType type, AggregatorLiveData<ProductAttributeType, String, ProductModel> destination) {
            this.type = type;
            this.destination = destination;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            try {
                destination.holdData(this.type, snapshot.getValue() == null ? "-" : snapshot.getValue().toString());
            } catch (ClassCastException e) {
                Log.e("PDS", "Could not cast " + type + " to String from received value");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

}
