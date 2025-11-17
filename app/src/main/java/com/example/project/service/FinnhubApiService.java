package com.example.project.service;

import android.util.Log;

import com.example.project.BuildConfig;
import com.example.project.model.CandleData;
import com.example.project.model.StockQuote;
import com.example.project.model.TimeFrame;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service class for fetching historical stock data from Finnhub API.
 * Handles HTTP requests to retrieve candle/OHLC data for different time frames.
 */
public class FinnhubApiService {

    private static final String TAG = "FinnhubApiService";
    private static final String CANDLE_ENDPOINT = "https://finnhub.io/api/v1/stock/candle";
    private static final String QUOTE_ENDPOINT = "https://finnhub.io/api/v1/quote";
    private static final int TIMEOUT_SECONDS = 30;

    private final OkHttpClient httpClient;
    private final Gson gson;

    /**
     * Callback interface for candle data fetching.
     */
    public interface CandleDataCallback {
        /**
         * Called when candle data is successfully fetched.
         *
         * @param candleData The fetched candle data
         */
        void onSuccess(CandleData candleData);

        /**
         * Called when an error occurs during fetching.
         *
         * @param error Error message
         */
        void onError(String error);
    }

    /**
     * Callback interface for quote data fetching.
     */
    public interface QuoteCallback {
        /**
         * Called when quote data is successfully fetched.
         *
         * @param quote The fetched stock quote
         */
        void onSuccess(StockQuote quote);

        /**
         * Called when an error occurs during fetching.
         *
         * @param error Error message
         */
        void onError(String error);
    }

    /**
     * Constructor initializes HTTP client and JSON parser.
     */
    public FinnhubApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Fetches candle data for a stock symbol with specified time frame.
     *
     * @param symbol    Stock symbol (e.g., "AAPL")
     * @param timeFrame Time frame for the data
     * @param callback  Callback for handling response
     */
    public void fetchCandleData(String symbol, TimeFrame timeFrame, CandleDataCallback callback) {
        long currentTime = System.currentTimeMillis() / 1000; // Convert to Unix timestamp
        long fromTime = timeFrame.getFromTimestamp(currentTime);

        fetchCandleData(symbol, timeFrame.getResolution(), fromTime, currentTime, callback);
    }

    /**
     * Fetches candle data with custom time range.
     *
     * @param symbol     Stock symbol (e.g., "AAPL")
     * @param resolution Resolution (1, 5, 15, 30, 60, D, W, M)
     * @param from       Start time (Unix timestamp)
     * @param to         End time (Unix timestamp)
     * @param callback   Callback for handling response
     */
    public void fetchCandleData(
            String symbol,
            String resolution,
            long from,
            long to,
            CandleDataCallback callback) {

        String url = buildCandleUrl(symbol, resolution, from, to);
        Log.d(TAG, "Fetching candle data from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch candle data", e);
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response: " + response.code());
                    callback.onError("HTTP error: " + response.code());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Received candle data: " + responseBody.substring(0, Math.min(200, responseBody.length())));

                    CandleData candleData = gson.fromJson(responseBody, CandleData.class);

                    if (candleData != null && candleData.isValid()) {
                        callback.onSuccess(candleData);
                    } else {
                        String errorMsg = candleData != null
                                ? "No data available (status: " + candleData.getStatus() + ")"
                                : "Invalid response";
                        Log.w(TAG, errorMsg);
                        callback.onError(errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing candle data", e);
                    callback.onError("Parsing error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Fetches current quote data for a stock symbol.
     *
     * @param symbol   Stock symbol (e.g., "AAPL")
     * @param callback Callback for handling response
     */
    public void fetchQuote(String symbol, QuoteCallback callback) {
        String url = buildQuoteUrl(symbol);
        Log.d(TAG, "Fetching quote from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch quote", e);
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response: " + response.code());
                    callback.onError("HTTP error: " + response.code());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Received quote: " + responseBody);

                    StockQuote quote = gson.fromJson(responseBody, StockQuote.class);

                    if (quote != null && quote.isValid()) {
                        callback.onSuccess(quote);
                    } else {
                        String errorMsg = "Invalid quote data";
                        Log.w(TAG, errorMsg);
                        callback.onError(errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing quote", e);
                    callback.onError("Parsing error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Builds the complete URL for candle data API request.
     *
     * @param symbol     Stock symbol
     * @param resolution Resolution parameter
     * @param from       Start timestamp
     * @param to         End timestamp
     * @return Complete API URL
     */
    private String buildCandleUrl(String symbol, String resolution, long from, long to) {
        return CANDLE_ENDPOINT +
                "?symbol=" + symbol +
                "&resolution=" + resolution +
                "&from=" + from +
                "&to=" + to +
                "&token=" + BuildConfig.FINNHUB_API_KEY;
    }

    /**
     * Builds the complete URL for quote API request.
     *
     * @param symbol Stock symbol
     * @return Complete API URL
     */
    private String buildQuoteUrl(String symbol) {
        return QUOTE_ENDPOINT +
                "?symbol=" + symbol +
                "&token=" + BuildConfig.FINNHUB_API_KEY;
    }

    /**
     * Cancels all pending requests.
     */
    public void cancelAllRequests() {
        httpClient.dispatcher().cancelAll();
    }
}
