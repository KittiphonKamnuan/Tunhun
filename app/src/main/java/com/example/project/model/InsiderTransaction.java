package com.example.project.model;

/**
 * Model class representing an insider transaction
 */
public class InsiderTransaction {
    private String name;
    private long share;
    private long change;
    private String filingDate;
    private String transactionDate;
    private String transactionCode;
    private double transactionPrice;

    public InsiderTransaction() {
    }

    public InsiderTransaction(String name, long share, long change, String filingDate,
                              String transactionDate, String transactionCode, double transactionPrice) {
        this.name = name;
        this.share = share;
        this.change = change;
        this.filingDate = filingDate;
        this.transactionDate = transactionDate;
        this.transactionCode = transactionCode;
        this.transactionPrice = transactionPrice;
    }

    // Getters
    public String getName() {
        return name;
    }

    public long getShare() {
        return share;
    }

    public long getChange() {
        return change;
    }

    public String getFilingDate() {
        return filingDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public double getTransactionPrice() {
        return transactionPrice;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setShare(long share) {
        this.share = share;
    }

    public void setChange(long change) {
        this.change = change;
    }

    public void setFilingDate(String filingDate) {
        this.filingDate = filingDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public void setTransactionPrice(double transactionPrice) {
        this.transactionPrice = transactionPrice;
    }

    /**
     * Check if this is a BUY transaction (positive change)
     */
    public boolean isBuy() {
        return change > 0;
    }

    /**
     * Check if this is a SELL transaction (negative change)
     */
    public boolean isSell() {
        return change < 0;
    }

    /**
     * Get transaction type as readable string
     */
    public String getTransactionType() {
        if (isBuy()) {
            return "BUY";
        } else if (isSell()) {
            return "SELL";
        } else {
            return "NO CHANGE";
        }
    }

    /**
     * Get absolute value of change
     */
    public long getAbsoluteChange() {
        return Math.abs(change);
    }
}
