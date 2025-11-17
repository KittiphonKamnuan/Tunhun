package com.example.project.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for stock candle/OHLC data from Finnhub API.
 * Represents historical price data for a specific time period.
 */
public class CandleData {

    @SerializedName("c")
    private List<Double> closePrices;

    @SerializedName("h")
    private List<Double> highPrices;

    @SerializedName("l")
    private List<Double> lowPrices;

    @SerializedName("o")
    private List<Double> openPrices;

    @SerializedName("t")
    private List<Long> timestamps;

    @SerializedName("v")
    private List<Long> volumes;

    @SerializedName("s")
    private String status;

    /**
     * Default constructor.
     */
    public CandleData() {
    }

    /**
     * Gets the list of closing prices.
     *
     * @return List of close prices
     */
    public List<Double> getClosePrices() {
        return closePrices;
    }

    /**
     * Gets the list of high prices.
     *
     * @return List of high prices
     */
    public List<Double> getHighPrices() {
        return highPrices;
    }

    /**
     * Gets the list of low prices.
     *
     * @return List of low prices
     */
    public List<Double> getLowPrices() {
        return lowPrices;
    }

    /**
     * Gets the list of opening prices.
     *
     * @return List of open prices
     */
    public List<Double> getOpenPrices() {
        return openPrices;
    }

    /**
     * Gets the list of timestamps (Unix time).
     *
     * @return List of timestamps
     */
    public List<Long> getTimestamps() {
        return timestamps;
    }

    /**
     * Gets the list of volumes.
     *
     * @return List of volumes
     */
    public List<Long> getVolumes() {
        return volumes;
    }

    /**
     * Gets the API response status.
     *
     * @return Status ("ok" if successful, "no_data" if no data available)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Checks if the candle data is valid and contains data.
     *
     * @return true if data is available, false otherwise
     */
    public boolean isValid() {
        return "ok".equalsIgnoreCase(status) &&
               closePrices != null &&
               !closePrices.isEmpty();
    }

    /**
     * Gets the number of data points.
     *
     * @return Number of candles
     */
    public int getDataPointCount() {
        return closePrices != null ? closePrices.size() : 0;
    }

    /**
     * Setters for testing purposes
     */
    public void setClosePrices(List<Double> closePrices) {
        this.closePrices = closePrices;
    }

    public void setHighPrices(List<Double> highPrices) {
        this.highPrices = highPrices;
    }

    public void setLowPrices(List<Double> lowPrices) {
        this.lowPrices = lowPrices;
    }

    public void setOpenPrices(List<Double> openPrices) {
        this.openPrices = openPrices;
    }

    public void setTimestamps(List<Long> timestamps) {
        this.timestamps = timestamps;
    }

    public void setVolumes(List<Long> volumes) {
        this.volumes = volumes;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
