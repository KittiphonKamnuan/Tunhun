package com.example.project.model;

/**
 * Model class representing a stock holding in user's portfolio
 */
public class PortfolioItem {
    private String symbol;
    private double shares;           // Number of shares owned
    private double averageCost;      // Average cost per share
    private double totalInvested;    // Total amount invested
    private double currentPrice;     // Current market price (updated real-time)

    public PortfolioItem() {
    }

    public PortfolioItem(String symbol, double shares, double averageCost) {
        this.symbol = symbol;
        this.shares = shares;
        this.averageCost = averageCost;
        this.totalInvested = shares * averageCost;
        this.currentPrice = averageCost; // Initial price
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getShares() {
        return shares;
    }

    public void setShares(double shares) {
        this.shares = shares;
        recalculateTotalInvested();
    }

    public double getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(double averageCost) {
        this.averageCost = averageCost;
        recalculateTotalInvested();
    }

    public double getTotalInvested() {
        return totalInvested;
    }

    public void setTotalInvested(double totalInvested) {
        this.totalInvested = totalInvested;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    // Calculated fields

    /**
     * Get current market value of holdings
     */
    public double getCurrentValue() {
        return shares * currentPrice;
    }

    /**
     * Get profit/loss amount
     */
    public double getProfitLoss() {
        return getCurrentValue() - totalInvested;
    }

    /**
     * Get profit/loss percentage
     */
    public double getProfitLossPercent() {
        if (totalInvested == 0) return 0;
        return (getProfitLoss() / totalInvested) * 100;
    }

    /**
     * Check if position is profitable
     */
    public boolean isProfitable() {
        return getProfitLoss() > 0;
    }

    /**
     * Get formatted shares string
     */
    public String getFormattedShares() {
        if (shares % 1 == 0) {
            return String.format("%.0f", shares);
        }
        return String.format("%.2f", shares);
    }

    /**
     * Get formatted average cost
     */
    public String getFormattedAverageCost() {
        return String.format("$%.2f", averageCost);
    }

    /**
     * Get formatted current value
     */
    public String getFormattedCurrentValue() {
        return String.format("$%.2f", getCurrentValue());
    }

    /**
     * Get formatted profit/loss
     */
    public String getFormattedProfitLoss() {
        double pl = getProfitLoss();
        String sign = pl >= 0 ? "+" : "";
        return String.format("%s$%.2f", sign, pl);
    }

    /**
     * Get formatted profit/loss percentage
     */
    public String getFormattedProfitLossPercent() {
        double plPercent = getProfitLossPercent();
        String sign = plPercent >= 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, plPercent);
    }

    /**
     * Add more shares to this position
     */
    public void addShares(double newShares, double pricePerShare) {
        double newTotalCost = totalInvested + (newShares * pricePerShare);
        double newTotalShares = shares + newShares;

        this.shares = newTotalShares;
        this.averageCost = newTotalCost / newTotalShares;
        this.totalInvested = newTotalCost;
    }

    /**
     * Recalculate total invested when shares or average cost changes
     */
    private void recalculateTotalInvested() {
        this.totalInvested = shares * averageCost;
    }
}
