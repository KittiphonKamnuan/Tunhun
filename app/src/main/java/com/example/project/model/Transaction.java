package com.example.project.model;

/**
 * Model class representing a stock transaction (buy/sell)
 */
public class Transaction {
    public enum Type {
        BUY, SELL
    }

    private String id;
    private String symbol;
    private Type type;
    private double shares;
    private double price;
    private long timestamp;

    public Transaction() {
    }

    public Transaction(String symbol, Type type, double shares, double price) {
        this.id = generateId();
        this.symbol = symbol;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getShares() {
        return shares;
    }

    public void setShares(double shares) {
        this.shares = shares;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get total transaction amount
     */
    public double getTotalAmount() {
        return shares * price;
    }

    /**
     * Get formatted transaction amount
     */
    public String getFormattedAmount() {
        return String.format("$%.2f", getTotalAmount());
    }

    /**
     * Get formatted transaction date
     */
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }

    /**
     * Generate unique transaction ID
     */
    private String generateId() {
        return "TXN_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
}
