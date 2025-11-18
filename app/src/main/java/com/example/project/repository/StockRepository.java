package com.example.project.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.project.model.Stock;
import com.example.project.model.TradeMessage;
import com.example.project.service.FinnhubWebSocketClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing stock data and WebSocket connection
 */
public class StockRepository {
    private static final String TAG = "StockRepository";
    private static final String PREFS_NAME = "stock_watchlist_prefs";
    private static final String KEY_WATCHLIST = "watchlist";

    private static StockRepository instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final FinnhubWebSocketClient webSocketClient;
    private final Handler mainHandler;

    private final Map<String, Stock> stockMap;
    private final MutableLiveData<List<Stock>> stockListLiveData;
    private final MutableLiveData<Boolean> connectionStatusLiveData;

    private StockRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.webSocketClient = new FinnhubWebSocketClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.stockMap = new HashMap<>();
        this.stockListLiveData = new MutableLiveData<>(new ArrayList<>());
        this.connectionStatusLiveData = new MutableLiveData<>(false);

        setupWebSocketListener();
        loadWatchlistFromPreferences();
    }

    public static synchronized StockRepository getInstance(Context context) {
        if (instance == null) {
            instance = new StockRepository(context.getApplicationContext());
        }
        return instance;
    }

    private void setupWebSocketListener() {
        webSocketClient.setTradeUpdateListener(new FinnhubWebSocketClient.TradeUpdateListener() {
            @Override
            public void onTradeUpdate(TradeMessage tradeMessage) {
                // Process trade updates on the main thread
                mainHandler.post(() -> processTradeUpdate(tradeMessage));
            }

            @Override
            public void onConnectionStatusChanged(boolean connected) {
                mainHandler.post(() -> {
                    connectionStatusLiveData.setValue(connected);
                    Log.d(TAG, "Connection status changed: " + connected);

                    // Resubscribe to all stocks when reconnected
                    if (connected) {
                        for (String symbol : stockMap.keySet()) {
                            webSocketClient.subscribe(symbol);
                        }
                    }
                });
            }
        });
    }

    private void processTradeUpdate(TradeMessage tradeMessage) {
        if (tradeMessage.getData() == null || tradeMessage.getData().isEmpty()) {
            return;
        }

        boolean updated = false;
        for (TradeMessage.TradeData trade : tradeMessage.getData()) {
            String symbol = trade.getSymbol();
            Stock stock = stockMap.get(symbol);

            if (stock != null) {
                double currentPrice = stock.getCurrentPrice();
                double newPrice = trade.getPrice();

                // Set opening price from first trade received
                if (currentPrice == 0 && newPrice > 0) {
                    stock.setOpeningPrice(newPrice);
                    Log.d(TAG, symbol + " opening price set to: " + newPrice);
                }

                // Update current price
                stock.setCurrentPrice(newPrice);

                // Calculate percentage change from opening price
                stock.calculateChangePercentFromOpening();

                updated = true;
            }
        }

        if (updated) {
            notifyStockListChanged();
        }
    }

    public void connect() {
        webSocketClient.connect();
    }

    public void disconnect() {
        webSocketClient.disconnect();
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

        if (webSocketClient.isConnected()) {
            webSocketClient.subscribe(upperSymbol);
        }

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
            if (webSocketClient.isConnected()) {
                webSocketClient.unsubscribe(upperSymbol);
            }

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
        return webSocketClient.isConnected();
    }
}