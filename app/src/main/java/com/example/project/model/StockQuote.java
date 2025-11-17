package com.example.project.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model class for stock quote data from Finnhub API.
 * Represents current price and trading information for a stock.
 */
public class StockQuote {

    @SerializedName("c")
    private double currentPrice;

    @SerializedName("d")
    private double change;

    @SerializedName("dp")
    private double percentChange;

    @SerializedName("h")
    private double highPrice;

    @SerializedName("l")
    private double lowPrice;

    @SerializedName("o")
    private double openPrice;

    @SerializedName("pc")
    private double previousClose;

    @SerializedName("t")
    private long timestamp;

    /**
     * Default constructor.
     */
    public StockQuote() {
    }

    /**
     * Gets the current price.
     *
     * @return Current price
     */
    public double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Gets the price change.
     *
     * @return Price change
     */
    public double getChange() {
        return change;
    }

    /**
     * Gets the percent change.
     *
     * @return Percent change
     */
    public double getPercentChange() {
        return percentChange;
    }

    /**
     * Gets the high price of the day.
     *
     * @return High price
     */
    public double getHighPrice() {
        return highPrice;
    }

    /**
     * Gets the low price of the day.
     *
     * @return Low price
     */
    public double getLowPrice() {
        return lowPrice;
    }

    /**
     * Gets the opening price.
     *
     * @return Opening price
     */
    public double getOpenPrice() {
        return openPrice;
    }

    /**
     * Gets the previous close price.
     *
     * @return Previous close price
     */
    public double getPreviousClose() {
        return previousClose;
    }

    /**
     * Gets the timestamp.
     *
     * @return Unix timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if the quote data is valid.
     *
     * @return true if current price is greater than 0, false otherwise
     */
    public boolean isValid() {
        return currentPrice > 0;
    }

    /**
     * Setters for testing purposes
     */
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public void setPercentChange(double percentChange) {
        this.percentChange = percentChange;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public void setPreviousClose(double previousClose) {
        this.previousClose = previousClose;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StockQuote{" +
                "currentPrice=" + currentPrice +
                ", change=" + change +
                ", percentChange=" + percentChange +
                ", openPrice=" + openPrice +
                '}';
    }
}
