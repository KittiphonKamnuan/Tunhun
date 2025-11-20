package com.example.project.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.project.model.Stock;
import com.example.project.model.StockQuote;
import com.example.project.service.FinnhubApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing stock data with 30-second polling
 */
public class StockRepository {
    private static final String TAG = "StockRepository";
    private static final String PREFS_NAME = "stock_watchlist_prefs";
    private static final String KEY_WATCHLIST = "watchlist";
    private static final long POLLING_INTERVAL = 30000; // 30 seconds

    private static StockRepository instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final FinnhubApiService apiService;
    private final Handler mainHandler;

    private final Map<String, Stock> stockMap;
    private final MutableLiveData<List<Stock>> stockListLiveData;
    private final MutableLiveData<Boolean> connectionStatusLiveData;

    private Runnable pollingRunnable;
    private boolean isPolling = false;

    private StockRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.apiService = new FinnhubApiService();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.stockMap = new HashMap<>();
        this.stockListLiveData = new MutableLiveData<>(new ArrayList<>());
        this.connectionStatusLiveData = new MutableLiveData<>(false);

        loadWatchlistFromPreferences();
        setupPolling();
    }

    public static synchronized StockRepository getInstance(Context context) {
        if (instance == null) {
            instance = new StockRepository(context.getApplicationContext());
        }
        return instance;
    }

    private void setupPolling() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPolling) {
                    fetchAllStockPrices();
                    mainHandler.postDelayed(this, POLLING_INTERVAL);
                }
            }
        };
    }

    private void fetchAllStockPrices() {
        if (stockMap.isEmpty()) {
            return;
        }

        Log.d(TAG, "Fetching prices for " + stockMap.size() + " stocks");

        for (String symbol : new ArrayList<>(stockMap.keySet())) {
            apiService.fetchQuote(symbol, new FinnhubApiService.QuoteCallback() {
                @Override
                public void onSuccess(StockQuote quote) {
                    mainHandler.post(() -> updateStockPrice(symbol, quote));
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error fetching quote for " + symbol + ": " + error);
                }
            });
        }
    }

    private void updateStockPrice(String symbol, StockQuote quote) {
        Stock stock = stockMap.get(symbol);
        if (stock != null) {
            // Set opening price (previous close)
            if (stock.getCurrentPrice() == 0) {
                stock.setOpeningPrice(quote.getPreviousClose());
            }

            // Update current price
            stock.setCurrentPrice(quote.getCurrentPrice());

            // Calculate change percent
            double change = quote.getCurrentPrice() - quote.getPreviousClose();
            double changePercent = (change / quote.getPreviousClose()) * 100;
            stock.setChangePercent(changePercent);

            Log.d(TAG, symbol + " updated: $" + quote.getCurrentPrice() + " (" + String.format("%.2f", changePercent) + "%)");
        }
        notifyStockListChanged();
    }

    public void connect() {
        if (!isPolling) {
            isPolling = true;
            connectionStatusLiveData.setValue(true);
            Log.d(TAG, "Starting 30-second polling");
            mainHandler.post(pollingRunnable);
        }
    }

    public void disconnect() {
        if (isPolling) {
            isPolling = false;
            connectionStatusLiveData.setValue(false);
            mainHandler.removeCallbacks(pollingRunnable);
            Log.d(TAG, "Stopped polling");
        }
    }

    public void addStock(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            Log.w(TAG, "Cannot add empty symbol");
            return;
        }

        String upperSymbol = symbol.trim().toUpperCase();

        if (stockMap.containsKey(upperSymbol)) {
            Log.d(TAG, "Stock already in watchlist: " + upperSymbol);
            return;
        }

        Stock stock = new Stock(upperSymbol);
        stockMap.put(upperSymbol, stock);

        // Fetch initial price immediately
        apiService.fetchQuote(upperSymbol, new FinnhubApiService.QuoteCallback() {
            @Override
            public void onSuccess(StockQuote quote) {
                mainHandler.post(() -> updateStockPrice(upperSymbol, quote));
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching initial quote for " + upperSymbol + ": " + error);
            }
        });

        saveWatchlistToPreferences();
        notifyStockListChanged();
        Log.d(TAG, "Added stock: " + upperSymbol);
    }

    public void removeStock(String symbol) {
        if (symbol == null) {
            return;
        }

        String upperSymbol = symbol.toUpperCase();
        Stock removed = stockMap.remove(upperSymbol);

        if (removed != null) {
            saveWatchlistToPreferences();
            notifyStockListChanged();
            Log.d(TAG, "Removed stock: " + upperSymbol);
        }
    }

    private void notifyStockListChanged() {
        List<Stock> stockList = new ArrayList<>(stockMap.values());
        stockListLiveData.setValue(stockList);
    }

    /**
     * ✅ แก้: บันทึก watchlist ให้มั่นใจว่า commit สำเร็จ
     */
    private void saveWatchlistToPreferences() {
        List<String> symbols = new ArrayList<>(stockMap.keySet());
        String json = gson.toJson(symbols);

        // ใช้ commit() แทน apply() เพื่อให้บันทึกทันที
        boolean success = sharedPreferences.edit()
                .putString(KEY_WATCHLIST, json)
                .commit();

        if (success) {
            Log.d(TAG, "Watchlist saved successfully: " + symbols);
        } else {
            Log.e(TAG, "Failed to save watchlist");
        }
    }

    private void loadWatchlistFromPreferences() {
        String json = sharedPreferences.getString(KEY_WATCHLIST, null);

        if (json != null) {
            try {
                Type type = new TypeToken<List<String>>() {}.getType();
                List<String> symbols = gson.fromJson(json, type);

                if (symbols != null) {
                    for (String symbol : symbols) {
                        Stock stock = new Stock(symbol);
                        stockMap.put(symbol, stock);
                    }
                    notifyStockListChanged();
                    Log.d(TAG, "Loaded " + symbols.size() + " stocks from preferences");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading watchlist from preferences", e);
            }
        }
    }

    public LiveData<List<Stock>> getStockList() {
        return stockListLiveData;
    }

    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatusLiveData;
    }

    public boolean isConnected() {
        return isPolling;
    }
}