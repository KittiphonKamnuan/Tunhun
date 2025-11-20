package com.example.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MainActivity;
import com.example.project.R;
import com.example.project.StockDetailActivity;
import com.example.project.adapter.StockAdapter;
import com.example.project.adapter.StockDashboardAdapter;
import com.example.project.model.MarketStatus;
import com.example.project.model.Stock;
import com.example.project.service.FinnhubApiService;
import com.example.project.repository.WatchlistRepository;
import com.example.project.viewmodel.StockViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {

    private StockViewModel viewModel;
    private FinnhubApiService apiService;

    // ✅ แก้: เพิ่ม adapter สำหรับ watchlist
    private StockAdapter watchlistAdapter;
    private StockDashboardAdapter trendingAdapter;
    private StockDashboardAdapter popularAdapter;

    private RecyclerView recyclerWatchlist;
    private RecyclerView recyclerTrending;
    private RecyclerView recyclerPopular;
    private View searchBar;

    // ✅ แก้: เพิ่ม views สำหรับ watchlist section
    private View watchlistSection;
    private TextView textViewAllWatchlist;

    // Market Status Views
    private TextView marketEmojiText;
    private TextView marketStatusText;
    private TextView marketSessionText;

    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private Handler marketStatusRefreshHandler;
    private Runnable marketStatusRefreshRunnable;

    private WatchlistRepository watchlistRepository;
    private List<String> watchlistSymbols = new ArrayList<>();
    private List<Stock> latestStockList = new ArrayList<>();

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
        observeWatchlist();
        loadStocks();
        loadMarketStatus();
        startAutoRefresh();
        startMarketStatusRefresh();
    }

    private void initViews(View view) {
        // ✅ แก้: เพิ่ม watchlist views
        watchlistSection = view.findViewById(R.id.watchlist_section);
        recyclerWatchlist = view.findViewById(R.id.recycler_watchlist);
        textViewAllWatchlist = view.findViewById(R.id.text_view_all_watchlist);

        recyclerTrending = view.findViewById(R.id.recycler_trending);
        recyclerPopular = view.findViewById(R.id.recycler_popular);
        searchBar = view.findViewById(R.id.search_bar);

        // Market Status Views
        marketEmojiText = view.findViewById(R.id.text_market_emoji);
        marketStatusText = view.findViewById(R.id.text_market_status);
        marketSessionText = view.findViewById(R.id.text_market_session);

        // Set search bar click listener
        searchBar.setOnClickListener(v -> openSearchActivity());

        // ✅ แก้: Set View All Watchlist click listener
        if (textViewAllWatchlist != null) {
            textViewAllWatchlist.setOnClickListener(v -> {
                // นำทางไปหน้า Watchlist (tab ที่ 2)
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToWatchlist();
                }
            });
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StockViewModel.class);
        apiService = new FinnhubApiService();
        watchlistRepository = WatchlistRepository.getInstance(requireContext());
    }

    private void setupRecyclerViews() {
        watchlistAdapter = new StockAdapter();
        recyclerWatchlist.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerWatchlist.setAdapter(watchlistAdapter);

        watchlistAdapter.setOnStockClickListener(this::openStockDetail);

        watchlistAdapter.setOnStockRemoveListener(stock -> {
            watchlistRepository.removeSymbol(stock.getSymbol());
            viewModel.removeStock(stock.getSymbol());
            Toast.makeText(getContext(), "ลบ " + stock.getSymbol() + " ออกจาก Watchlist แล้ว", Toast.LENGTH_SHORT).show();
        });

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
            if (stocks == null) {
                latestStockList = new ArrayList<>();
            } else {
                latestStockList = stocks;
            }

            updateWatchlistSection();

            // Filter and update adapters
            trendingAdapter.setStocks(filterStocks(latestStockList, TRENDING_STOCKS));
            popularAdapter.setStocks(filterStocks(latestStockList, POPULAR_STOCKS));
        });
    }

    private void observeWatchlist() {
        watchlistRepository.getWatchlistSymbols().observe(getViewLifecycleOwner(), symbols -> {
            watchlistSymbols = symbols != null ? symbols : new ArrayList<>();
            updateWatchlistSection();
        });
    }

    private void updateWatchlistSection() {
        if (watchlistSection == null) return;

        if (watchlistSymbols == null || watchlistSymbols.isEmpty()) {
            watchlistSection.setVisibility(View.GONE);
            watchlistAdapter.setStockList(new ArrayList<>());
            return;
        }

        List<Stock> watchlistStocks = latestStockList.stream()
                .filter(stock -> watchlistSymbols.contains(stock.getSymbol()))
                .collect(Collectors.toList());

        if (watchlistStocks.isEmpty()) {
            watchlistSection.setVisibility(View.GONE);
        } else {
            watchlistSection.setVisibility(View.VISIBLE);
            watchlistAdapter.setStockList(watchlistStocks);
        }
    }

    private List<Stock> filterStocks(List<Stock> allStocks, List<String> symbols) {
        return allStocks.stream()
                .filter(stock -> symbols.contains(stock.getSymbol()))
                .collect(Collectors.toList());
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

    private void loadMarketStatus() {
        apiService.fetchMarketStatus("US", new FinnhubApiService.MarketStatusCallback() {
            @Override
            public void onSuccess(MarketStatus status) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateMarketStatusUI(status));
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        marketEmojiText.setText("⚪");
                        marketStatusText.setText(R.string.market_status_label);
                        marketSessionText.setText("Loading...");
                    });
                }
            }
        });
    }

    private void updateMarketStatusUI(MarketStatus status) {
        marketEmojiText.setText(status.getStatusEmoji());

        boolean isOpen = false;
        if (status.getStatusText() != null) {
            isOpen = status.getStatusText().toLowerCase().contains("open");
        }

        if (isOpen) {
            marketStatusText.setText(R.string.market_status_open);
        } else {
            marketStatusText.setText(R.string.market_status_closed);
        }

        String sessionRaw = status.getSessionText();
        if (sessionRaw == null) {
            marketSessionText.setText("");
            return;
        }

        String sessionLower = sessionRaw.toLowerCase();
        if (sessionLower.contains("regular")) {
            marketSessionText.setText(R.string.session_regular);
        } else if (sessionLower.contains("pre")) {
            marketSessionText.setText(R.string.session_pre);
        } else if (sessionLower.contains("post") || sessionLower.contains("after")) {
            marketSessionText.setText(R.string.session_post);
        } else if (sessionLower.contains("close")) {
            marketSessionText.setText(R.string.session_closed);
        } else {
            marketSessionText.setText(sessionRaw);
        }
    }

    private void startMarketStatusRefresh() {
        marketStatusRefreshHandler = new Handler(Looper.getMainLooper());
        marketStatusRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadMarketStatus();
                marketStatusRefreshHandler.postDelayed(this, 60000);
            }
        };
        marketStatusRefreshHandler.post(marketStatusRefreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        if (marketStatusRefreshHandler != null && marketStatusRefreshRunnable != null) {
            marketStatusRefreshHandler.removeCallbacks(marketStatusRefreshRunnable);
        }
        if (apiService != null) {
            apiService.cancelAllRequests();
        }
    }
}