package com.example.project.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Queue manager for WebSocket subscription requests
 * Processes requests with rate limiting to avoid exceeding API limits
 */
public class SubscriptionQueue {
    private static final String TAG = "SubscriptionQueue";

    private final Queue<SubscriptionRequest> pendingRequests;
    private final FinnhubRateLimiter rateLimiter;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private boolean isProcessing = false;
    private final Object lock = new Object();

    public interface SubscriptionCallback {
        void onSubscriptionProcessed(String symbol, boolean isSubscribe, boolean success);
        void onRateLimitApplied(int pendingCount);
    }

    private SubscriptionCallback callback;

    public SubscriptionQueue(FinnhubRateLimiter rateLimiter) {
        this.pendingRequests = new LinkedList<>();
        this.rateLimiter = rateLimiter;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setCallback(SubscriptionCallback callback) {
        this.callback = callback;
    }

    /**
     * Adds a subscription request to the queue
     */
    public void enqueueSubscribe(String symbol, SubscriptionExecutor executor) {
        synchronized (lock) {
            pendingRequests.offer(new SubscriptionRequest(symbol, true, executor));
            Log.d(TAG, String.format("Enqueued subscribe request for %s. Queue size: %d",
                symbol, pendingRequests.size()));
        }
        startProcessing();
    }

    /**
     * Adds an unsubscription request to the queue
     */
    public void enqueueUnsubscribe(String symbol, SubscriptionExecutor executor) {
        synchronized (lock) {
            pendingRequests.offer(new SubscriptionRequest(symbol, false, executor));
            Log.d(TAG, String.format("Enqueued unsubscribe request for %s. Queue size: %d",
                symbol, pendingRequests.size()));
        }
        startProcessing();
    }

    /**
     * Starts processing the queue if not already running
     */
    private void startProcessing() {
        synchronized (lock) {
            if (isProcessing) {
                return;
            }
            isProcessing = true;
        }

        executorService.execute(this::processQueue);
    }

    /**
     * Processes requests from the queue with rate limiting
     */
    private void processQueue() {
        while (true) {
            SubscriptionRequest request;

            synchronized (lock) {
                request = pendingRequests.poll();
                if (request == null) {
                    isProcessing = false;
                    Log.d(TAG, "Queue empty, stopping processing");
                    return;
                }
            }

            try {
                // Acquire rate limit permission (blocks if necessary)
                if (!rateLimiter.acquire()) {
                    Log.w(TAG, "Rate limiter interrupted, skipping request");
                    notifyCallback(request.symbol, request.isSubscribe, false);
                    continue;
                }

                // Notify about rate limiting if there are pending requests
                int pendingCount = getPendingCount();
                if (pendingCount > 0 && callback != null) {
                    mainHandler.post(() ->
                        callback.onRateLimitApplied(pendingCount));
                }

                // Execute the actual subscription/unsubscription
                boolean success = request.executor.execute(request.symbol, request.isSubscribe);

                Log.d(TAG, String.format("%s %s %s",
                    request.isSubscribe ? "Subscribed to" : "Unsubscribed from",
                    request.symbol,
                    success ? "successfully" : "with failure"));

                notifyCallback(request.symbol, request.isSubscribe, success);

            } catch (Exception e) {
                Log.e(TAG, "Error processing subscription request", e);
                notifyCallback(request.symbol, request.isSubscribe, false);
            }
        }
    }

    /**
     * Notifies callback on main thread
     */
    private void notifyCallback(String symbol, boolean isSubscribe, boolean success) {
        if (callback != null) {
            mainHandler.post(() ->
                callback.onSubscriptionProcessed(symbol, isSubscribe, success));
        }
    }

    /**
     * Gets the number of pending requests
     */
    public int getPendingCount() {
        synchronized (lock) {
            return pendingRequests.size();
        }
    }

    /**
     * Clears all pending requests
     */
    public void clear() {
        synchronized (lock) {
            int cleared = pendingRequests.size();
            pendingRequests.clear();
            Log.d(TAG, String.format("Cleared %d pending requests", cleared));
        }
    }

    /**
     * Shuts down the queue processor
     */
    public void shutdown() {
        clear();
        executorService.shutdown();
        Log.d(TAG, "SubscriptionQueue shut down");
    }

    /**
     * Interface for executing subscription operations
     */
    public interface SubscriptionExecutor {
        boolean execute(String symbol, boolean isSubscribe);
    }

    /**
     * Internal class representing a subscription request
     */
    private static class SubscriptionRequest {
        final String symbol;
        final boolean isSubscribe;
        final SubscriptionExecutor executor;

        SubscriptionRequest(String symbol, boolean isSubscribe, SubscriptionExecutor executor) {
            this.symbol = symbol;
            this.isSubscribe = isSubscribe;
            this.executor = executor;
        }
    }
}
