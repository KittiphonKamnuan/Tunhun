package com.example.project.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.project.model.PortfolioItem;
import com.example.project.model.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing user's demo trading portfolio
 */
public class PortfolioRepository {
    private static final String TAG = "PortfolioRepository";
    private static final String PREFS_NAME = "portfolio_prefs";
    private static final String KEY_PORTFOLIO = "portfolio";
    private static final String KEY_BALANCE = "demo_balance";
    private static final String KEY_TRANSACTIONS = "transactions";
    private static final double INITIAL_BALANCE = 100000.0; // $100,000 demo money

    private static PortfolioRepository instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private final Map<String, PortfolioItem> portfolioMap;
    private final MutableLiveData<List<PortfolioItem>> portfolioLiveData;
    private final MutableLiveData<Double> balanceLiveData;
    private final List<Transaction> transactions;

    private PortfolioRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.portfolioMap = new HashMap<>();
        this.portfolioLiveData = new MutableLiveData<>(new ArrayList<>());
        this.balanceLiveData = new MutableLiveData<>(INITIAL_BALANCE);
        this.transactions = new ArrayList<>();

        loadFromPreferences();
    }

    public static synchronized PortfolioRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PortfolioRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Get portfolio items as LiveData
     */
    public LiveData<List<PortfolioItem>> getPortfolio() {
        return portfolioLiveData;
    }

    /**
     * Get demo balance as LiveData
     */
    public LiveData<Double> getBalance() {
        return balanceLiveData;
    }

    /**
     * Get current balance value
     */
    public double getCurrentBalance() {
        Double balance = balanceLiveData.getValue();
        return balance != null ? balance : INITIAL_BALANCE;
    }

    /**
     * Buy stock
     */
    public boolean buyStock(String symbol, double shares, double pricePerShare) {
        double totalCost = shares * pricePerShare;
        double currentBalance = getCurrentBalance();

        // Check if user has enough balance
        if (totalCost > currentBalance) {
            Log.w(TAG, "Insufficient balance for purchase");
            return false;
        }

        // Deduct from balance
        double newBalance = currentBalance - totalCost;
        balanceLiveData.setValue(newBalance);

        // Add or update portfolio item
        PortfolioItem existingItem = portfolioMap.get(symbol);
        if (existingItem != null) {
            // Add to existing position
            existingItem.addShares(shares, pricePerShare);
        } else {
            // Create new position
            PortfolioItem newItem = new PortfolioItem(symbol, shares, pricePerShare);
            portfolioMap.put(symbol, newItem);
        }

        // Record transaction
        Transaction transaction = new Transaction(symbol, Transaction.Type.BUY, shares, pricePerShare);
        transactions.add(transaction);

        // Save and notify
        saveToPreferences();
        notifyPortfolioChanged();

        Log.d(TAG, "Bought " + shares + " shares of " + symbol + " at $" + pricePerShare);
        return true;
    }

    /**
     * Sell stock
     */
    public boolean sellStock(String symbol, double shares, double pricePerShare) {
        PortfolioItem item = portfolioMap.get(symbol);

        // Check if user owns this stock
        if (item == null || item.getShares() < shares) {
            Log.w(TAG, "Insufficient shares to sell");
            return false;
        }

        // Add proceeds to balance
        double proceeds = shares * pricePerShare;
        double newBalance = getCurrentBalance() + proceeds;
        balanceLiveData.setValue(newBalance);

        // Update or remove portfolio item
        if (item.getShares() == shares) {
            // Selling all shares - remove from portfolio
            portfolioMap.remove(symbol);
        } else {
            // Selling partial shares
            item.setShares(item.getShares() - shares);
        }

        // Record transaction
        Transaction transaction = new Transaction(symbol, Transaction.Type.SELL, shares, pricePerShare);
        transactions.add(transaction);

        // Save and notify
        saveToPreferences();
        notifyPortfolioChanged();

        Log.d(TAG, "Sold " + shares + " shares of " + symbol + " at $" + pricePerShare);
        return true;
    }

    /**
     * Update current price for a stock in portfolio
     */
    public void updateStockPrice(String symbol, double currentPrice) {
        PortfolioItem item = portfolioMap.get(symbol);
        if (item != null) {
            item.setCurrentPrice(currentPrice);
            notifyPortfolioChanged();
        }
    }

    /**
     * Get portfolio item for specific symbol
     */
    public PortfolioItem getPortfolioItem(String symbol) {
        return portfolioMap.get(symbol);
    }

    /**
     * Check if user owns a specific stock
     */
    public boolean ownsStock(String symbol) {
        return portfolioMap.containsKey(symbol);
    }

    /**
     * Get total portfolio value
     */
    public double getTotalPortfolioValue() {
        double total = 0;
        for (PortfolioItem item : portfolioMap.values()) {
            total += item.getCurrentValue();
        }
        return total;
    }

    /**
     * Get total profit/loss
     */
    public double getTotalProfitLoss() {
        double total = 0;
        for (PortfolioItem item : portfolioMap.values()) {
            total += item.getProfitLoss();
        }
        return total;
    }

    /**
     * Reset portfolio to initial state
     */
    public void resetPortfolio() {
        portfolioMap.clear();
        transactions.clear();
        balanceLiveData.setValue(INITIAL_BALANCE);
        saveToPreferences();
        notifyPortfolioChanged();
        Log.d(TAG, "Portfolio reset to initial state");
    }

    /**
     * Notify observers of portfolio changes
     */
    private void notifyPortfolioChanged() {
        List<PortfolioItem> items = new ArrayList<>(portfolioMap.values());
        portfolioLiveData.setValue(items);
    }

    /**
     * Save portfolio to SharedPreferences
     */
    private void saveToPreferences() {
        List<PortfolioItem> items = new ArrayList<>(portfolioMap.values());
        String portfolioJson = gson.toJson(items);
        String transactionsJson = gson.toJson(transactions);
        double balance = getCurrentBalance();

        boolean success = sharedPreferences.edit()
                .putString(KEY_PORTFOLIO, portfolioJson)
                .putString(KEY_TRANSACTIONS, transactionsJson)
                .putFloat(KEY_BALANCE, (float) balance)
                .commit();

        if (success) {
            Log.d(TAG, "Portfolio saved successfully");
        } else {
            Log.e(TAG, "Failed to save portfolio");
        }
    }

    /**
     * Load portfolio from SharedPreferences
     */
    private void loadFromPreferences() {
        // Load portfolio items
        String portfolioJson = sharedPreferences.getString(KEY_PORTFOLIO, null);
        if (portfolioJson != null) {
            try {
                Type type = new TypeToken<List<PortfolioItem>>() {}.getType();
                List<PortfolioItem> items = gson.fromJson(portfolioJson, type);

                if (items != null) {
                    for (PortfolioItem item : items) {
                        portfolioMap.put(item.getSymbol(), item);
                    }
                    Log.d(TAG, "Loaded " + items.size() + " portfolio items");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading portfolio from preferences", e);
            }
        }

        // Load transactions
        String transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, null);
        if (transactionsJson != null) {
            try {
                Type type = new TypeToken<List<Transaction>>() {}.getType();
                List<Transaction> loadedTransactions = gson.fromJson(transactionsJson, type);

                if (loadedTransactions != null) {
                    transactions.addAll(loadedTransactions);
                    Log.d(TAG, "Loaded " + loadedTransactions.size() + " transactions");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading transactions from preferences", e);
            }
        }

        // Load balance
        float balance = sharedPreferences.getFloat(KEY_BALANCE, (float) INITIAL_BALANCE);
        balanceLiveData.setValue((double) balance);

        // Notify observers
        notifyPortfolioChanged();
    }
}
