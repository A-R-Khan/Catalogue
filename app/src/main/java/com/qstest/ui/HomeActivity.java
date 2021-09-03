package com.qstest.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qstest.R;
import com.qstest.data.models.ProductModel;
import com.qstest.viewmodels.FeedViewModel;

import java.util.List;

import mva3.adapter.ListSection;
import mva3.adapter.MultiViewAdapter;
import mva3.adapter.util.InfiniteLoadingHelper;

public class HomeActivity extends AppCompatActivity {

    private FeedViewModel feedViewModel;
    private RecyclerView recyclerView;

    private ListSection<LiveData<ProductModel>> productListSection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_layout);

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        productListSection = new ListSection<>();
        // In case of a config change
        if(feedViewModel.getLoadedProducts().size() > 0 && savedInstanceState != null) {
            productListSection.addAll(feedViewModel.getLoadedProducts());
        }
        setupRecyclerView();

    }

    private void setupRecyclerView() {

        recyclerView = findViewById(R.id.recycler_view);

        /*
        I use this adapter because it is extremely scalable and easy to use compared
        to the native RecyclerView adapter
        */
        MultiViewAdapter adapter = new MultiViewAdapter();

        adapter.registerItemBinders(new FeedAdapter(this));
        adapter.addSection(productListSection);

        InfiniteLoadingHelper infiniteLoadingHelper = new InfiniteLoadingHelper(recyclerView, R.layout.loading_footer_layout) {

            @Override
            public void onLoadNextPage(int page) {
                loadData();
                markCurrentPageLoaded();
            }

            @Override
            public void markCurrentPageLoaded() {
                super.markCurrentPageLoaded();
            }

            //TODO detect and set
            @Override
            public void markAllPagesLoaded() {
                super.markAllPagesLoaded();
            }

        };

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        DividerItemDecoration dividerDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_drawable, getTheme()));
        recyclerView.addItemDecoration(dividerDecoration);

        adapter.setInfiniteLoadingHelper(infiniteLoadingHelper);
        infiniteLoadingHelper.onLoadNextPage(0);
    }

    private void loadData() {
        List<LiveData<ProductModel>> products = feedViewModel.getNextProducts();
        synchronized (this) {
            productListSection.addAll(products);
        }
    }
}
