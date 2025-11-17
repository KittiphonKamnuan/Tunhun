package com.example.project.service;

import android.util.Log;

import com.example.project.BuildConfig;
import com.example.project.model.TradeMessage;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for connecting to Finnhub API and receiving real-time stock trades
 */
public class FinnhubWebSocketClient extends WebSocketListener {
    private static final String TAG = "FinnhubWebSocket";
    private static final String FINNHUB_WS_URL = "wss://ws.finnhub.io?token=";

    private WebSocket webSocket;
    private OkHttpClient client;
    private Gson gson;
    private Set<String> subscribedSymbols;
    private TradeUpdateListener tradeUpdateListener;
    private boolean isConnected = false;

    // Rate limiting components
    private FinnhubRateLimiter rateLimiter;
    private SubscriptionQueue subscriptionQueue;
    private RateLimitListener rateLimitListener;

    public interface TradeUpdateListener {
        void onTradeUpdate(TradeMessage tradeMessage);
        void onConnectionStatusChanged(boolean connected);
    }

    public interface RateLimitListener {
        void onRateLimitApplied(int pendingRequests);
        void onSubscriptionQueued(String symbol, boolean isSubscribe);
    }

    public FinnhubWebSocketClient() {
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
        this.gson = new Gson();
        this.subscribedSymbols = new HashSet<>();

        // Initialize rate limiting components
        this.rateLimiter = new FinnhubRateLimiter();
        this.subscriptionQueue = new SubscriptionQueue(rateLimiter);
        setupSubscriptionQueueCallback();
    }

    public void setTradeUpdateListener(TradeUpdateListener listener) {
        this.tradeUpdateListener = listener;
    }

    public void setRateLimitListener(RateLimitListener listener) {
        this.rateLimitListener = listener;
    }

    private void setupSubscriptionQueueCallback() {
        subscriptionQueue.setCallback(new SubscriptionQueue.SubscriptionCallback() {
            @Override
            public void onSubscriptionProcessed(String symbol, boolean isSubscribe, boolean success) {
                if (success) {
                    if (isSubscribe) {
                        subscribedSymbols.add(symbol);
                    } else {
                        subscribedSymbols.remove(symbol);
                    }
                }
            }

            @Override
            public void onRateLimitApplied(int pendingCount) {
                if (rateLimitListener != null) {
                    rateLimitListener.onRateLimitApplied(pendingCount);
                }
            }
        });
    }

    public void connect() {
        if (isConnected) {
            Log.d(TAG, "Already connected");
            return;
        }

        Request request = new Request.Builder()
                .url(FINNHUB_WS_URL + BuildConfig.FINNHUB_API_KEY)
                .build();

        webSocket = client.newWebSocket(request, this);
        Log.d(TAG, "Connecting to Finnhub WebSocket...");
    }

    public void disconnect() {
        if (webSocket != null) {
            // Unsubscribe from all symbols before disconnecting
            for (String symbol : new HashSet<>(subscribedSymbols)) {
                unsubscribe(symbol);
            }
            webSocket.close(1000, "Client disconnecting");
            webSocket = null;
        }

        // Clear any pending subscription requests
        if (subscriptionQueue != null) {
            subscriptionQueue.clear();
        }

        isConnected = false;
        if (tradeUpdateListener != null) {
            tradeUpdateListener.onConnectionStatusChanged(false);
        }
        Log.d(TAG, "Disconnected from WebSocket");
    }

    public void subscribe(String symbol) {
        if (webSocket != null && isConnected) {
            // Queue the subscription request with rate limiting
            subscriptionQueue.enqueueSubscribe(symbol, (sym, isSubscribe) -> {
                if (webSocket != null && isConnected) {
                    String subscribeMessage = String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", sym);
                    boolean sent = webSocket.send(subscribeMessage);
                    if (sent) {
                        Log.d(TAG, "Subscribed to: " + sym);
                    } else {
                        Log.e(TAG, "Failed to send subscribe message for: " + sym);
                    }
                    return sent;
                }
                return false;
            });

            if (rateLimitListener != null) {
                rateLimitListener.onSubscriptionQueued(symbol, true);
            }
        } else {
            Log.w(TAG, "Cannot subscribe - not connected");
        }
    }

    public void unsubscribe(String symbol) {
        if (webSocket != null && isConnected) {
            // Queue the unsubscription request with rate limiting
            subscriptionQueue.enqueueUnsubscribe(symbol, (sym, isSubscribe) -> {
                if (webSocket != null && isConnected) {
                    String unsubscribeMessage = String.format("{\"type\":\"unsubscribe\",\"symbol\":\"%s\"}", sym);
                    boolean sent = webSocket.send(unsubscribeMessage);
                    if (sent) {
                        Log.d(TAG, "Unsubscribed from: " + sym);
                    } else {
                        Log.e(TAG, "Failed to send unsubscribe message for: " + sym);
                    }
                    return sent;
                }
                return false;
            });

            if (rateLimitListener != null) {
                rateLimitListener.onSubscriptionQueued(symbol, false);
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public Set<String> getSubscribedSymbols() {
        return new HashSet<>(subscribedSymbols);
    }

    /**
     * Gets the current rate limiter stats for monitoring
     */
    public int getCurrentRequestCount() {
        return rateLimiter != null ? rateLimiter.getCurrentRequestCount() : 0;
    }

    /**
     * Gets the number of pending subscription requests
     */
    public int getPendingSubscriptionCount() {
        return subscriptionQueue != null ? subscriptionQueue.getPendingCount() : 0;
    }

    /**
     * Checks if rate limiting allows immediate requests
     */
    public boolean canMakeRequest() {
        return rateLimiter != null && rateLimiter.canMakeRequest();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        isConnected = true;
        Log.d(TAG, "WebSocket connected successfully");

        if (tradeUpdateListener != null) {
            tradeUpdateListener.onConnectionStatusChanged(true);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);

        try {
            // Parse the JSON message
            TradeMessage tradeMessage = gson.fromJson(text, TradeMessage.class);

            if (tradeMessage != null && "trade".equals(tradeMessage.getType())
                    && tradeMessage.getData() != null && !tradeMessage.getData().isEmpty()) {

                if (tradeUpdateListener != null) {
                    tradeUpdateListener.onTradeUpdate(tradeMessage);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message: " + text, e);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        Log.d(TAG, "WebSocket closing: " + reason);
        isConnected = false;
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.d(TAG, "WebSocket closed: " + reason);
        isConnected = false;
        if (tradeUpdateListener != null) {
            tradeUpdateListener.onConnectionStatusChanged(false);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);

        // Check for HTTP 429 (Too Many Requests) status code
        if (response != null && response.code() == 429) {
            Log.e(TAG, "Rate limit exceeded (HTTP 429). Too many API requests.");
            Log.e(TAG, "Current request count: " + getCurrentRequestCount() + "/30");
            Log.e(TAG, "Pending subscriptions: " + getPendingSubscriptionCount());

            // Reset the rate limiter to recover from rate limit errors
            if (rateLimiter != null) {
                rateLimiter.reset();
                Log.d(TAG, "Rate limiter reset due to 429 error");
            }

            // Clear pending requests to prevent further rate limit violations
            if (subscriptionQueue != null) {
                subscriptionQueue.clear();
                Log.d(TAG, "Subscription queue cleared due to 429 error");
            }
        } else if (response != null) {
            Log.e(TAG, "WebSocket error with HTTP code: " + response.code());
        } else {
            Log.e(TAG, "WebSocket error", t);
        }

        isConnected = false;
        if (tradeUpdateListener != null) {
            tradeUpdateListener.onConnectionStatusChanged(false);
        }
    }
}
