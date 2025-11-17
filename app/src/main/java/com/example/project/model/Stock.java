package com.example.project.model;

public class Stock {
    private String symbol;
    private double currentPrice;
    private double openingPrice;  // Market opening price for % calculation
    private double changePercent;
    private long lastUpdateTime;

    public Stock(String symbol) {
        this.symbol = symbol;
        this.currentPrice = 0.0;
        this.openingPrice = 0.0;
        this.changePercent = 0.0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(double openingPrice) {
        this.openingPrice = openingPrice;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getFormattedPrice() {
        return String.format("$%.2f", currentPrice);
    }

    public String getFormattedChangePercent() {
        String prefix = changePercent >= 0 ? "+" : "";
        return String.format("%s%.2f%%", prefix, changePercent);
    }

    public boolean isPositiveChange() {
        return changePercent >= 0;
    }

    /**
     * Calculate percentage change from opening price
     * Formula: ((currentPrice - openingPrice) / openingPrice) * 100
     */
    public void calculateChangePercentFromOpening() {
        if (openingPrice > 0) {
            this.changePercent = ((currentPrice - openingPrice) / openingPrice) * 100;
        } else {
            this.changePercent = 0.0;
        }
    }
}
