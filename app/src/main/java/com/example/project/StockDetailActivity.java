package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapter.InsiderTransactionAdapter;
import com.example.project.dialog.BuyStockDialog;
import com.example.project.dialog.SellStockDialog;
import com.example.project.model.CandleData;
import com.example.project.model.InsiderTransactionResponse;
import com.example.project.model.PortfolioItem;
import com.example.project.model.StockQuote;
import com.example.project.model.TimeFrame;
import com.example.project.repository.PortfolioRepository;
import com.example.project.repository.WatchlistRepository;
import com.example.project.service.FinnhubApiService;
import com.example.project.util.ChartHelper;
import com.example.project.util.PriceDataGenerator;
import com.example.project.util.StockColorHelper;
import com.example.project.viewmodel.StockViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Activity for displaying detailed stock information including price chart.
 */
public class StockDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "symbol";
    public static final String EXTRA_PRICE = "price";
    public static final String EXTRA_CHANGE = "change";

    private static final long REFRESH_INTERVAL_MS = 3000;
    private static final String TAG = "StockDetailActivity";

    // UI Components
    private TextView symbolText;
    private TextView priceText;
    private TextView changeText;
    private LineChart priceChart;
    private ChipGroup chipGroupTimeframe;
    private ProgressBar chartLoading;
    private TextView chartErrorText;
    private FloatingActionButton btnBack;
    private ImageView btnShare;
    private ImageView btnWatchlist;
    private MaterialButton btnBuy;
    private MaterialButton btnSell;

    // Stock Information TextViews
    private TextView textOpenPrice;
    private TextView textHighPrice;
    private TextView textLowPrice;
    private TextView textPrevClose;

    // Insider Transactions
    private RecyclerView recyclerInsiderTransactions;
    private InsiderTransactionAdapter insiderTransactionAdapter;
    private ProgressBar insiderLoading;
    private TextView insiderEmptyText;

    // Data
    private String symbol;
    private double price;
    private double changePercent;
    private TimeFrame currentTimeFrame = TimeFrame.ONE_DAY;

    // Services
    private StockViewModel viewModel;
    private FinnhubApiService apiService;
    private PortfolioRepository portfolioRepository;
    private WatchlistRepository watchlistRepository;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private StockQuote latestQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        extractIntentData();
        initializeViews();
        setupViewModel();
        setupApiService();
        setupPortfolioRepository();
        setupWatchlistRepository();

        setupBackButton();
        setupButtons();
        setupInsiderTransactions();

        fetchQuoteData();

        setupChart();
        setupListeners();

        // โหลดกราฟครั้งแรก
        loadChartData();

        // Load insider transactions
        loadInsiderTransactions();

        startAutoRefresh();
    }

    private void extractIntentData() {
        symbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        if (symbol == null) symbol = getIntent().getStringExtra("symbol");

        price = getIntent().getDoubleExtra(EXTRA_PRICE, 0.0);
        if (price == 0.0) price = getIntent().getDoubleExtra("price", 0.0);

        changePercent = getIntent().getDoubleExtra(EXTRA_CHANGE, 0.0);
        if (changePercent == 0.0) changePercent = getIntent().getDoubleExtra("change", 0.0);
    }

    private void initializeViews() {
        symbolText = findViewById(R.id.text_symbol_detail);
        priceText = findViewById(R.id.text_price_detail);
        changeText = findViewById(R.id.text_change_detail);
        priceChart = findViewById(R.id.price_chart);
        chipGroupTimeframe = findViewById(R.id.chip_group_timeframe);
        chartLoading = findViewById(R.id.chart_loading);
        chartErrorText = findViewById(R.id.chart_error_text);
        btnBack = findViewById(R.id.btn_back);
        btnShare = findViewById(R.id.btn_share);
        btnWatchlist = findViewById(R.id.btn_watchlist);
        btnBuy = findViewById(R.id.btn_buy);
        btnSell = findViewById(R.id.btn_sell);

        textOpenPrice = findViewById(R.id.text_open_price);
        textHighPrice = findViewById(R.id.text_high_price);
        textLowPrice = findViewById(R.id.text_low_price);
        textPrevClose = findViewById(R.id.text_prev_close);

        // Insider Transactions Views
        recyclerInsiderTransactions = findViewById(R.id.recycler_insider_transactions);
        insiderLoading = findViewById(R.id.insider_loading);
        insiderEmptyText = findViewById(R.id.insider_empty_text);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StockViewModel.class);
    }

    private void setupApiService() {
        apiService = new FinnhubApiService();
    }

    private void setupPortfolioRepository() {
        portfolioRepository = PortfolioRepository.getInstance(this);
    }

    private void setupWatchlistRepository() {
        watchlistRepository = WatchlistRepository.getInstance(this);
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupButtons() {
        if (btnBuy != null) {
            btnBuy.setOnClickListener(v -> showBuyDialog());
        }
        if (btnSell != null) {
            btnSell.setOnClickListener(v -> showSellDialog());
        }
        if (btnWatchlist != null) {
            // Set initial button state
            updateWatchlistIcon();

            btnWatchlist.setOnClickListener(v -> {
                if (symbol == null || symbol.trim().isEmpty()) {
                    Toast.makeText(this, R.string.error_symbol_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Toggle add/remove from watchlist
                boolean isInWatchlist = watchlistRepository.isInWatchlist(symbol);

                if (isInWatchlist) {
                    // Remove from watchlist
                    watchlistRepository.removeSymbol(symbol);
                    viewModel.removeStock(symbol);
                    Toast.makeText(this, getString(R.string.toast_remove_watchlist, symbol), Toast.LENGTH_SHORT).show();
                } else {
                    // Add to watchlist
                    watchlistRepository.addSymbol(symbol);
                    viewModel.addStock(symbol);
                    Toast.makeText(this, getString(R.string.toast_add_watchlist, symbol), Toast.LENGTH_SHORT).show();
                }

                // Update icon appearance
                updateWatchlistIcon();
            });
        }
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareStockInfo());
        }
    }

    private void shareStockInfo() {
        String shareText = getString(R.string.app_name) + "\n" +
                symbol + "\n" +
                getString(R.string.dialog_current_price) + " " + String.format("%.2f", price) + "\n" +
                getString(R.string.profit_loss_label) + ": " + StockColorHelper.formatChangePercent(changePercent);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.btn_share));
        startActivity(shareIntent);
    }

    /**
     * Update watchlist star icon based on watchlist status
     */
    private void updateWatchlistIcon() {
        if (btnWatchlist == null || symbol == null) return;

        boolean isInWatchlist = watchlistRepository.isInWatchlist(symbol);

        if (isInWatchlist) {
            // Show filled star (yellow)
            btnWatchlist.setImageResource(android.R.drawable.btn_star_big_on);
            btnWatchlist.setAlpha(1.0f); // Fully opaque
        } else {
            // Show transparent star outline
            btnWatchlist.setImageResource(android.R.drawable.btn_star_big_off);
            btnWatchlist.setAlpha(0.3f); // Semi-transparent
        }
    }

    private void showBuyDialog() {
        if (price <= 0) {
            Toast.makeText(this, R.string.toast_wait_price, Toast.LENGTH_SHORT).show();
            return;
        }
        double availableBalance = portfolioRepository.getCurrentBalance();
        BuyStockDialog dialog = BuyStockDialog.newInstance(symbol, price, availableBalance);
        dialog.setOnBuyConfirmedListener((symbol, shares, totalCost) -> {
            Toast.makeText(this, R.string.toast_order_success, Toast.LENGTH_LONG).show();
        });
        dialog.show(getSupportFragmentManager(), "BuyStockDialog");
    }

    private void showSellDialog() {
        if (price <= 0) {
            Toast.makeText(this, R.string.toast_wait_price, Toast.LENGTH_SHORT).show();
            return;
        }

        PortfolioItem item = portfolioRepository.getPortfolioItem(symbol);
        double ownedShares = item != null ? item.getShares() : 0;

        if (ownedShares <= 0) {
            Toast.makeText(this, R.string.toast_insufficient_shares, Toast.LENGTH_SHORT).show();
            return;
        }

        SellStockDialog dialog = SellStockDialog.newInstance(symbol, price, ownedShares);
        dialog.setOnSellConfirmedListener((symbol, shares, totalValue) -> {
            Toast.makeText(this, getString(R.string.toast_sell_success, String.valueOf((int)shares), symbol), Toast.LENGTH_LONG).show();
        });
        dialog.show(getSupportFragmentManager(), "SellStockDialog");
    }

    private void fetchQuoteData() {
        apiService.fetchQuote(symbol, new FinnhubApiService.QuoteCallback() {
            @Override
            public void onSuccess(StockQuote quote) {
                runOnUiThread(() -> {
                    latestQuote = quote;
                    price = quote.getCurrentPrice();
                    changePercent = quote.getPercentChange();
                    displayStockInfo();
                    displayStockInformation();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (price == 0) price = 100.0;
                    displayStockInfo();
                });
            }
        });
    }

    private void displayStockInfo() {
        symbolText.setText(symbol);
        priceText.setText(String.format("%.2f", price));
        changeText.setText(StockColorHelper.formatChangePercent(changePercent));
        changeText.setTextColor(StockColorHelper.getStockColor(this, changePercent));
    }

    private void displayStockInformation() {
        if (latestQuote != null) {
            textOpenPrice.setText(String.format("%.2f", latestQuote.getOpenPrice()));
            textHighPrice.setText(String.format("%.2f", latestQuote.getHighPrice()));
            textLowPrice.setText(String.format("%.2f", latestQuote.getLowPrice()));
            textPrevClose.setText(String.format("%.2f", latestQuote.getPreviousClose()));
        } else {
            textOpenPrice.setText("--");
            textHighPrice.setText("--");
            textLowPrice.setText("--");
            textPrevClose.setText("--");
        }
    }

    private void setupChart() {
        ChartHelper.configureChart(priceChart, this);
    }

    private void loadChartData() {
        showLoading();

        apiService.fetchCandleData(symbol, currentTimeFrame, new FinnhubApiService.CandleDataCallback() {
            @Override
            public void onSuccess(CandleData candleData) {
                runOnUiThread(() -> {
                    hideLoading();
                    updateChartWithApiData(candleData);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    // ⭐ จุดที่แก้ไข: ซ่อน Error เสมอ แล้วใช้ Mock Data
                    hideError();
                    updateChartWithMockData();
                });
            }
        });
    }

    private void updateChartWithApiData(CandleData candleData) {
        List<Entry> entries = PriceDataGenerator.convertCandleDataToEntries(candleData);

        // ⭐ จุดที่แก้ไข: ถ้าข้อมูลว่าง ให้ซ่อน Error แล้วใช้ Mock Data แทน
        if (entries == null || entries.isEmpty()) {
            hideError();
            updateChartWithMockData();
            return;
        }

        hideError();
        ChartHelper.updateChartData(priceChart, entries, changePercent, this);
    }

    private void updateChartWithMockData() {
        List<Entry> entries = PriceDataGenerator.generateMockPriceData(price, changePercent);
        ChartHelper.updateChartData(priceChart, entries, changePercent, this);
    }

    private void showLoading() {
        chartLoading.setVisibility(View.VISIBLE);
        chartErrorText.setVisibility(View.GONE);
        priceChart.setAlpha(0.3f);
    }

    private void hideLoading() {
        chartLoading.setVisibility(View.GONE);
        priceChart.setAlpha(1.0f);
    }

    private void showError(String error) {
        chartErrorText.setText(error);
        chartErrorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        chartErrorText.setVisibility(View.GONE);
    }

    private void setupListeners() {
        chipGroupTimeframe.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                currentTimeFrame = getTimeFrameFromChipId(checkedId);
                loadChartData();
            }
        });
    }

    private TimeFrame getTimeFrameFromChipId(int chipId) {
        if (chipId == R.id.chip_1d) return TimeFrame.ONE_DAY;
        else if (chipId == R.id.chip_5d) return TimeFrame.FIVE_DAYS;
        else if (chipId == R.id.chip_1m) return TimeFrame.ONE_MONTH;
        else if (chipId == R.id.chip_6m) return TimeFrame.SIX_MONTHS;
        else if (chipId == R.id.chip_ytd) return TimeFrame.YTD;
        else if (chipId == R.id.chip_1y) return TimeFrame.ONE_YEAR;
        else if (chipId == R.id.chip_5y) return TimeFrame.FIVE_YEARS;
        else if (chipId == R.id.chip_max) return TimeFrame.MAX;
        return TimeFrame.ONE_DAY;
    }

    private void setupInsiderTransactions() {
        insiderTransactionAdapter = new InsiderTransactionAdapter();
        recyclerInsiderTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerInsiderTransactions.setAdapter(insiderTransactionAdapter);
    }

    private void loadInsiderTransactions() {
        if (symbol == null || symbol.trim().isEmpty()) {
            return;
        }

        insiderLoading.setVisibility(View.VISIBLE);
        insiderEmptyText.setVisibility(View.GONE);
        recyclerInsiderTransactions.setVisibility(View.GONE);

        apiService.fetchInsiderTransactions(symbol, 20, new FinnhubApiService.InsiderTransactionsCallback() {
            @Override
            public void onSuccess(InsiderTransactionResponse response) {
                runOnUiThread(() -> {
                    insiderLoading.setVisibility(View.GONE);

                    if (response.getData() != null && !response.getData().isEmpty()) {
                        insiderTransactionAdapter.setTransactions(response.getData());
                        recyclerInsiderTransactions.setVisibility(View.VISIBLE);
                        insiderEmptyText.setVisibility(View.GONE);
                    } else {
                        recyclerInsiderTransactions.setVisibility(View.GONE);
                        insiderEmptyText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    insiderLoading.setVisibility(View.GONE);
                    recyclerInsiderTransactions.setVisibility(View.GONE);
                    insiderEmptyText.setVisibility(View.VISIBLE);
                    Log.w(TAG, "Failed to load insider transactions: " + error);
                });
            }
        });
    }

    private void startAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchQuoteData();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
        if (apiService != null) {
            apiService.cancelAllRequests();
        }
    }
}