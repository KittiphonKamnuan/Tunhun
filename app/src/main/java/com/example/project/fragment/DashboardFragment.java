package com.example.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.StockDetailActivity;
import com.example.project.adapter.StockDashboardAdapter;
import com.example.project.model.Stock;
import com.example.project.viewmodel.StockViewModel;

import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {

    private StockViewModel viewModel;
    private StockDashboardAdapter trendingAdapter;
    private StockDashboardAdapter popularAdapter;
    private RecyclerView recyclerTrending;
    private RecyclerView recyclerPopular;
    private View searchBar;

    private Handler refreshHandler;
    private Runnable refreshRunnable;

    // Predefined popular stocks
    private static final List<String> TRENDING_STOCKS = Arrays.asList("AAPL", "TSLA", "GOOGL", "MSFT");
    private static final List<String> POPULAR_STOCKS = Arrays.asList("AMZN", "META", "NVDA", "NFLX");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerViews();
        loadStocks();
        startAutoRefresh();
    }

    private void initViews(View view) {
        recyclerTrending = view.findViewById(R.id.recycler_trending);
        recyclerPopular = view.findViewById(R.id.recycler_popular);
        searchBar = view.findViewById(R.id.search_bar);

        // Set search bar click listener
        searchBar.setOnClickListener(v -> openSearchActivity());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StockViewModel.class);
    }

    private void setupRecyclerViews() {
        // Trending stocks
        trendingAdapter = new StockDashboardAdapter();
        recyclerTrending.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTrending.setAdapter(trendingAdapter);

        // Popular stocks
        popularAdapter = new StockDashboardAdapter();
        recyclerPopular.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPopular.setAdapter(popularAdapter);

        // Handle clicks
        trendingAdapter.setOnStockClickListener(this::openStockDetail);
        popularAdapter.setOnStockClickListener(this::openStockDetail);
    }

    private void loadStocks() {
        // Connect to WebSocket
        if (!viewModel.isConnected()) {
            viewModel.connect();
        }

        // Add default stocks for trending and popular sections
        for (String symbol : TRENDING_STOCKS) {
            viewModel.addStock(symbol);
        }
        for (String symbol : POPULAR_STOCKS) {
            viewModel.addStock(symbol);
        }

        // Observe stock updates
        viewModel.getStockList().observe(getViewLifecycleOwner(), stocks -> {
            if (stocks != null) {
                // Filter and update adapters
                trendingAdapter.setStocks(filterStocks(stocks, TRENDING_STOCKS));
                popularAdapter.setStocks(filterStocks(stocks, POPULAR_STOCKS));
            }
        });
    }

    private List<Stock> filterStocks(List<Stock> allStocks, List<String> symbols) {
        return allStocks.stream()
                .filter(stock -> symbols.contains(stock.getSymbol()))
                .collect(java.util.stream.Collectors.toList());
    }

    private void openStockDetail(Stock stock) {
        Intent intent = new Intent(getContext(), StockDetailActivity.class);
        intent.putExtra("symbol", stock.getSymbol());
        intent.putExtra("price", stock.getCurrentPrice());
        intent.putExtra("change", stock.getChangePercent());
        startActivity(intent);
    }

    private void openSearchActivity() {
        Intent intent = new Intent(getContext(), com.example.project.SearchActivity.class);
        startActivity(intent);
    }

    private void startAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Refresh stock data
                viewModel.getStockList().observe(getViewLifecycleOwner(), stocks -> {
                    if (stocks != null) {
                        trendingAdapter.setStocks(filterStocks(stocks, TRENDING_STOCKS));
                        popularAdapter.setStocks(filterStocks(stocks, POPULAR_STOCKS));
                    }
                });
                // Refresh every 60 seconds
                refreshHandler.postDelayed(this, 60000);
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop auto-refresh
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}
