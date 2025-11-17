package com.example.project.mock;

import android.os.Handler;
import android.os.Looper;

import com.example.project.model.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Mock data provider for testing without API key
 * Simulates real-time stock price updates
 */
public class MockStockDataProvider {
    private static final Map<String, StockData> MOCK_STOCKS = new HashMap<>();
    private final Handler handler;
    private final Random random;
    private StockUpdateListener listener;
    private boolean isRunning = false;

    static {
        // Popular US stocks with realistic starting prices
        MOCK_STOCKS.put("AAPL", new StockData("AAPL", 178.50, 1.25));
        MOCK_STOCKS.put("MSFT", new StockData("MSFT", 378.90, 0.85));
        MOCK_STOCKS.put("GOOGL", new StockData("GOOGL", 141.20, -0.45));
        MOCK_STOCKS.put("AMZN", new StockData("AMZN", 178.35, 1.15));
        MOCK_STOCKS.put("TSLA", new StockData("TSLA", 242.80, 2.35));
        MOCK_STOCKS.put("META", new StockData("META", 484.20, 0.95));
        MOCK_STOCKS.put("NVDA", new StockData("NVDA", 136.50, 3.20));
        MOCK_STOCKS.put("NFLX", new StockData("NFLX", 682.40, -1.10));
        MOCK_STOCKS.put("DIS", new StockData("DIS", 112.30, 0.65));
        MOCK_STOCKS.put("BA", new StockData("BA", 178.90, -0.35));
    }

    public interface StockUpdateListener {
        void onStockUpdate(String symbol, double price, double changePercent);
    }

    public MockStockDataProvider() {
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }

    public void setStockUpdateListener(StockUpdateListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        scheduleNextUpdate();
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void scheduleNextUpdate() {
        if (!isRunning) return;

        // Random delay between 1-3 seconds
        int delay = 1000 + random.nextInt(2000);

        handler.postDelayed(() -> {
            updateRandomStock();
            scheduleNextUpdate();
        }, delay);
    }

    private void updateRandomStock() {
        if (MOCK_STOCKS.isEmpty() || listener == null) return;

        // Pick a random stock to update
        List<String> symbols = new ArrayList<>(MOCK_STOCKS.keySet());
        String symbol = symbols.get(random.nextInt(symbols.size()));
        StockData data = MOCK_STOCKS.get(symbol);

        if (data != null) {
            // Simulate small price change (-0.5% to +0.5%)
            double changePercent = (random.nextDouble() - 0.5);
            double priceChange = data.basePrice * (changePercent / 100.0);
            double newPrice = data.currentPrice + priceChange;

            // Keep price within reasonable bounds (±10% of base price)
            double minPrice = data.basePrice * 0.9;
            double maxPrice = data.basePrice * 1.1;
            newPrice = Math.max(minPrice, Math.min(maxPrice, newPrice));

            // Update current price and calculate total change
            data.currentPrice = newPrice;
            double totalChangePercent = ((newPrice - data.basePrice) / data.basePrice) * 100;

            listener.onStockUpdate(symbol, newPrice, totalChangePercent);
        }
    }

    public Stock getInitialStockData(String symbol) {
        StockData data = MOCK_STOCKS.get(symbol.toUpperCase());
        if (data != null) {
            Stock stock = new Stock(symbol.toUpperCase());
            stock.setOpeningPrice(data.basePrice);  // Set opening price from base price
            stock.setCurrentPrice(data.currentPrice);
            stock.calculateChangePercentFromOpening();  // Calculate from opening price
            return stock;
        }

        // Return a stock with random data if not in predefined list
        Stock stock = new Stock(symbol.toUpperCase());
        double randomPrice = 50 + random.nextDouble() * 150;
        stock.setOpeningPrice(randomPrice);  // Set opening price
        stock.setCurrentPrice(randomPrice);
        stock.calculateChangePercentFromOpening();  // Will be 0% initially
        return stock;
    }

    public boolean isValidSymbol(String symbol) {
        return MOCK_STOCKS.containsKey(symbol.toUpperCase());
    }

    public List<String> getSuggestedSymbols() {
        return new ArrayList<>(MOCK_STOCKS.keySet());
    }

    private static class StockData {
        String symbol;
        double basePrice;
        double currentPrice;
        double initialChangePercent;

        StockData(String symbol, double basePrice, double initialChangePercent) {
            this.symbol = symbol;
            this.basePrice = basePrice;
            this.currentPrice = basePrice;
            this.initialChangePercent = initialChangePercent;
        }
    }
}
