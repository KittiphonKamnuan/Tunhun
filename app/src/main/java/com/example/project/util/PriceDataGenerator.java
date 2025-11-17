package com.example.project.util;

import com.example.project.model.CandleData;
import com.example.project.model.TimeFrame;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting candle data and generating mock price data for stock charts.
 * Provides methods to convert API responses to chart-ready data format.
 */
public class PriceDataGenerator {

    private static final double VARIATION_MULTIPLIER = 0.5;
    private static final int DEFAULT_MOCK_DATA_POINTS = 50;

    /**
     * Converts CandleData from Finnhub API to chart Entry list.
     * Uses closing prices for the chart data points.
     *
     * @param candleData The candle data from API
     * @return List of chart entries
     */
    public static List<Entry> convertCandleDataToEntries(CandleData candleData) {
        List<Entry> entries = new ArrayList<>();

        if (candleData == null || !candleData.isValid()) {
            return entries;
        }

        List<Double> closePrices = candleData.getClosePrices();
        if (closePrices == null || closePrices.isEmpty()) {
            return entries;
        }

        for (int i = 0; i < closePrices.size(); i++) {
            Double price = closePrices.get(i);
            if (price != null && price > 0) {
                entries.add(new Entry(i, price.floatValue()));
            }
        }

        return entries;
    }

    /**
     * Generates mock historical price data for a given time frame.
     * The data is generated based on the current price and change percentage,
     * creating a realistic-looking price trend.
     * This is used as fallback when API data is not available.
     *
     * @param currentPrice   Current stock price
     * @param changePercent  Price change percentage
     * @return List of chart entries representing price history
     */
    public static List<Entry> generateMockPriceData(
            double currentPrice,
            double changePercent) {

        return generateMockPriceData(DEFAULT_MOCK_DATA_POINTS, currentPrice, changePercent);
    }

    /**
     * Generates mock historical price data with specified number of points.
     *
     * @param dataPoints     Number of data points to generate
     * @param currentPrice   Current stock price
     * @param changePercent  Price change percentage
     * @return List of chart entries representing price history
     */
    public static List<Entry> generateMockPriceData(
            int dataPoints,
            double currentPrice,
            double changePercent) {

        List<Entry> entries = new ArrayList<>();
        double changeRange = Math.abs(changePercent) * VARIATION_MULTIPLIER;

        for (int i = 0; i < dataPoints; i++) {
            double mockPrice = calculateMockPrice(
                    currentPrice,
                    changePercent,
                    changeRange,
                    dataPoints,
                    i
            );
            entries.add(new Entry(i, (float) mockPrice));
        }

        return entries;
    }

    /**
     * Calculates a single mock price point with variation.
     * This creates a smooth trend from the starting price to the current price.
     *
     * @param currentPrice   Current stock price
     * @param changePercent  Price change percentage
     * @param changeRange    Range of variation for randomness
     * @param totalPoints    Total number of data points
     * @param currentIndex   Current data point index
     * @return Calculated mock price
     */
    private static double calculateMockPrice(
            double currentPrice,
            double changePercent,
            double changeRange,
            int totalPoints,
            int currentIndex) {

        // Add random variation to make the chart more realistic
        double variation = (Math.random() - 0.5) * changeRange;

        // Calculate price based on position in the timeline
        double progressFactor = (totalPoints - currentIndex) / (double) totalPoints;
        double priceAdjustment = (changePercent / 100.0) * progressFactor;

        return currentPrice * (1 - priceAdjustment + variation / 100.0);
    }

    /**
     * Generates price data with custom parameters.
     * Useful for more fine-grained control over data generation.
     *
     * @param dataPoints     Number of data points to generate
     * @param startPrice     Starting price
     * @param endPrice       Ending price
     * @param volatility     Volatility factor (0.0 to 1.0)
     * @return List of chart entries
     */
    public static List<Entry> generateCustomPriceData(
            int dataPoints,
            double startPrice,
            double endPrice,
            double volatility) {

        List<Entry> entries = new ArrayList<>();
        double priceRange = endPrice - startPrice;
        double volatilityRange = Math.abs(priceRange) * volatility;

        for (int i = 0; i < dataPoints; i++) {
            double progress = i / (double) (dataPoints - 1);
            double variation = (Math.random() - 0.5) * volatilityRange;
            double price = startPrice + (priceRange * progress) + variation;

            entries.add(new Entry(i, (float) price));
        }

        return entries;
    }
}
