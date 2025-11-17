package com.example.project;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.project.model.CandleData;
import com.example.project.model.StockQuote;
import com.example.project.model.TimeFrame;
import com.example.project.service.FinnhubApiService;
import com.example.project.util.ChartHelper;
import com.example.project.util.PriceDataGenerator;
import com.example.project.util.StockColorHelper;
import com.example.project.viewmodel.StockViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * Activity for displaying detailed stock information including price chart.
 * Shows current price, change percentage, and interactive chart with multiple time frames.
 */
public class StockDetailActivity extends AppCompatActivity {

    // Intent extras keys
    public static final String EXTRA_SYMBOL = "symbol";
    public static final String EXTRA_PRICE = "price";
    public static final String EXTRA_CHANGE = "change";

    // Auto-refresh interval in milliseconds
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
    private ImageButton btnBack;

    // Stock Information TextViews
    private TextView textOpenPrice;
    private TextView textHighPrice;
    private TextView textLowPrice;
    private TextView textPrevClose;

    // Data
    private String symbol;
    private double price;
    private double changePercent;
    private TimeFrame currentTimeFrame = TimeFrame.ONE_DAY; // ✅ แก้เป็น ONE_DAY

    // Services and handlers
    private StockViewModel viewModel;
    private FinnhubApiService apiService;
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    // Store quote data
    private StockQuote latestQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        extractIntentData();
        initializeViews();
        setupViewModel();
        setupApiService();

        // ✅ แก้: เพิ่ม listener สำหรับปุ่ม back
        setupBackButton();

        // Fetch quote data เสมอเพื่อให้ได้ข้อมูล Open, High, Low, Prev Close
        fetchQuoteData();

        setupChart();
        setupListeners();
        startAutoRefresh();
    }

    /**
     * Extracts stock data from intent extras.
     */
    private void extractIntentData() {
        symbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        if (symbol == null) {
            symbol = getIntent().getStringExtra("symbol");
        }
        price = getIntent().getDoubleExtra(EXTRA_PRICE, 0.0);
        if (price == 0.0) {
            price = getIntent().getDoubleExtra("price", 0.0);
        }
        changePercent = getIntent().getDoubleExtra(EXTRA_CHANGE, 0.0);
        if (changePercent == 0.0) {
            changePercent = getIntent().getDoubleExtra("change", 0.0);
        }
    }

    /**
     * Initializes all view references.
     */
    private void initializeViews() {
        symbolText = findViewById(R.id.text_symbol_detail);
        priceText = findViewById(R.id.text_price_detail);
        changeText = findViewById(R.id.text_change_detail);
        priceChart = findViewById(R.id.price_chart);
        chipGroupTimeframe = findViewById(R.id.chip_group_timeframe);
        chartLoading = findViewById(R.id.chart_loading);
        chartErrorText = findViewById(R.id.chart_error_text);
        btnBack = findViewById(R.id.btn_back);

        // ✅ แก้: เพิ่ม TextView สำหรับ Stock Information
        textOpenPrice = findViewById(R.id.text_open_price);
        textHighPrice = findViewById(R.id.text_high_price);
        textLowPrice = findViewById(R.id.text_low_price);
        textPrevClose = findViewById(R.id.text_prev_close);
    }

    /**
     * Sets up the ViewModel for stock data observation.
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StockViewModel.class);
    }

    /**
     * Sets up the API service for fetching historical data.
     */
    private void setupApiService() {
        apiService = new FinnhubApiService();
    }

    /**
     * ✅ แก้: เพิ่ม method สำหรับปุ่ม back
     */
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Fetches current quote data from API.
     */
    private void fetchQuoteData() {
        Log.d(TAG, "Fetching quote for " + symbol);

        apiService.fetchQuote(symbol, new FinnhubApiService.QuoteCallback() {
            @Override
            public void onSuccess(StockQuote quote) {
                runOnUiThread(() -> {
                    latestQuote = quote;
                    price = quote.getCurrentPrice();
                    changePercent = quote.getPercentChange();
                    Log.d(TAG, "Quote fetched: price=" + price + ", change=" + changePercent + "%");
                    displayStockInfo();
                    displayStockInformation(); // ✅ แก้: แสดงข้อมูล Open, High, Low, Prev Close
                    loadChartData();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to fetch quote: " + error);
                    Toast.makeText(StockDetailActivity.this,
                            "Failed to load stock data: " + error,
                            Toast.LENGTH_LONG).show();

                    // Set default values
                    price = 100.0;
                    changePercent = 0.0;
                    displayStockInfo();
                    loadChartData();
                });
            }
        });
    }

    /**
     * Displays current stock information in the UI.
     */
    private void displayStockInfo() {
        // Set symbol
        symbolText.setText(symbol);

        // Set price (without $ sign for cleaner look)
        priceText.setText(String.format("%.2f", price));

        // Set change with formatted text and color
        changeText.setText(StockColorHelper.formatChangePercent(changePercent));
        changeText.setTextColor(StockColorHelper.getStockColor(this, changePercent));
    }

    /**
     * ✅ แก้: เพิ่ม method สำหรับแสดง Stock Information
     */
    private void displayStockInformation() {
        if (latestQuote != null) {
            textOpenPrice.setText(String.format("$%.2f", latestQuote.getOpenPrice()));
            textHighPrice.setText(String.format("$%.2f", latestQuote.getHighPrice()));
            textLowPrice.setText(String.format("$%.2f", latestQuote.getLowPrice()));
            textPrevClose.setText(String.format("$%.2f", latestQuote.getPreviousClose()));
        } else {
            textOpenPrice.setText("--");
            textHighPrice.setText("--");
            textLowPrice.setText("--");
            textPrevClose.setText("--");
        }
    }

    /**
     * Initializes and configures the price chart.
     */
    private void setupChart() {
        ChartHelper.configureChart(priceChart, this);
    }

    /**
     * Loads chart data from Finnhub API.
     * Falls back to mock data if API is not available (e.g., free tier).
     */
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

                    // Don't show error if it's access restriction (free tier)
                    if (error.contains("access") || error.contains("resource")) {
                        Log.i(TAG, "Candle API not available (free tier), using mock data");
                    } else {
                        Log.e(TAG, "Failed to load chart data: " + error);
                    }

                    // Always fallback to mock data
                    hideError();
                    updateChartWithMockData();
                });
            }
        });
    }

    /**
     * Updates chart with API data.
     */
    private void updateChartWithApiData(CandleData candleData) {
        List<Entry> entries = PriceDataGenerator.convertCandleDataToEntries(candleData);

        if (entries.isEmpty()) {
            showError("No data available");
            updateChartWithMockData();
            return;
        }

        ChartHelper.updateChartData(priceChart, entries, changePercent, this);
        Log.d(TAG, "Chart updated with " + entries.size() + " data points from API");
    }

    /**
     * Updates chart with mock data as fallback.
     */
    private void updateChartWithMockData() {
        List<Entry> entries = PriceDataGenerator.generateMockPriceData(price, changePercent);
        ChartHelper.updateChartData(priceChart, entries, changePercent, this);
        Log.d(TAG, "Chart updated with mock data");
    }

    /**
     * Shows loading indicator and hides error message.
     */
    private void showLoading() {
        chartLoading.setVisibility(View.VISIBLE);
        chartErrorText.setVisibility(View.GONE);
        priceChart.setAlpha(0.3f);
    }

    /**
     * Hides loading indicator.
     */
    private void hideLoading() {
        chartLoading.setVisibility(View.GONE);
        priceChart.setAlpha(1.0f);
    }

    /**
     * Shows error message.
     */
    private void showError(String error) {
        chartErrorText.setText(error);
        chartErrorText.setVisibility(View.VISIBLE);
    }

    /**
     * Hides error message.
     */
    private void hideError() {
        chartErrorText.setVisibility(View.GONE);
    }

    /**
     * Sets up all click listeners for UI components.
     */
    private void setupListeners() {
        setupTimeframeChipListener();
    }

    /**
     * Configures the time frame chip group selection listener.
     */
    private void setupTimeframeChipListener() {
        chipGroupTimeframe.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                currentTimeFrame = getTimeFrameFromChipId(checkedId);
                loadChartData(); // Reload chart with new time frame
            }
        });
    }

    /**
     * Maps chip ID to corresponding TimeFrame enum value.
     *
     * @param chipId Resource ID of the selected chip
     * @return Corresponding TimeFrame enum value
     */
    private TimeFrame getTimeFrameFromChipId(int chipId) {
        if (chipId == R.id.chip_1d) {
            return TimeFrame.ONE_DAY;
        } else if (chipId == R.id.chip_5d) {
            return TimeFrame.FIVE_DAYS;
        } else if (chipId == R.id.chip_1m) {
            return TimeFrame.ONE_MONTH;
        } else if (chipId == R.id.chip_6m) {
            return TimeFrame.SIX_MONTHS;
        } else if (chipId == R.id.chip_ytd) {
            return TimeFrame.YTD;
        } else if (chipId == R.id.chip_1y) {
            return TimeFrame.ONE_YEAR;
        } else if (chipId == R.id.chip_5y) {
            return TimeFrame.FIVE_YEARS;
        } else if (chipId == R.id.chip_max) {
            return TimeFrame.MAX;
        }
        return TimeFrame.ONE_DAY; // ✅ แก้: Default เป็น ONE_DAY
    }

    /**
     * Starts automatic data refresh at regular intervals.
     */
    private void startAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                observeStockUpdates();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    /**
     * Observes stock updates from ViewModel and updates UI when data changes.
     */
    private void observeStockUpdates() {
        viewModel.getStockList().observe(this, stocks -> {
            if (stocks != null) {
                stocks.stream()
                        .filter(stock -> stock.getSymbol().equals(symbol))
                        .findFirst()
                        .ifPresent(stock -> {
                            // Update price info
                            price = stock.getCurrentPrice();
                            changePercent = stock.getChangePercent();
                            displayStockInfo();
                            // Note: We don't reload chart data on every price update
                            // Chart data is loaded only on timeframe change or manual refresh
                        });
            }
        });
    }

    /**
     * Stops automatic data refresh when activity is destroyed.
     */
    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();

        // Cancel any pending API requests
        if (apiService != null) {
            apiService.cancelAllRequests();
        }
    }
}