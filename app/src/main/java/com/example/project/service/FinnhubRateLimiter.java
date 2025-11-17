package com.example.project.service;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Rate limiter for Finnhub API calls
 * Enforces the 30 API calls per second limit as per Finnhub documentation
 */
public class FinnhubRateLimiter {
    private static final String TAG = "FinnhubRateLimiter";
    private static final int MAX_CALLS_PER_SECOND = 30;
    private static final long TIME_WINDOW_MS = 1000; // 1 second

    private final Queue<Long> requestTimestamps;
    private final Object lock = new Object();

    public FinnhubRateLimiter() {
        this.requestTimestamps = new LinkedList<>();
    }

    /**
     * Attempts to acquire permission to make an API call
     * Blocks if rate limit would be exceeded until it's safe to proceed
     *
     * @return true if permission granted, false if interrupted
     */
    public boolean acquire() {
        synchronized (lock) {
            try {
                long now = System.currentTimeMillis();

                // Remove timestamps older than 1 second
                while (!requestTimestamps.isEmpty() &&
                       now - requestTimestamps.peek() >= TIME_WINDOW_MS) {
                    requestTimestamps.poll();
                }

                // If we've hit the limit, wait until the oldest request expires
                if (requestTimestamps.size() >= MAX_CALLS_PER_SECOND) {
                    long oldestTimestamp = requestTimestamps.peek();
                    long waitTime = TIME_WINDOW_MS - (now - oldestTimestamp);

                    if (waitTime > 0) {
                        Log.d(TAG, String.format("Rate limit reached. Waiting %dms before next request", waitTime));
                        Thread.sleep(waitTime);

                        // Update 'now' after sleeping
                        now = System.currentTimeMillis();

                        // Clean up expired timestamps again
                        while (!requestTimestamps.isEmpty() &&
                               now - requestTimestamps.peek() >= TIME_WINDOW_MS) {
                            requestTimestamps.poll();
                        }
                    }
                }

                // Record this request
                requestTimestamps.offer(now);
                Log.d(TAG, String.format("Request approved. Current: %d/%d calls in last second",
                    requestTimestamps.size(), MAX_CALLS_PER_SECOND));

                return true;

            } catch (InterruptedException e) {
                Log.e(TAG, "Rate limiter interrupted", e);
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * Checks if a request can be made immediately without blocking
     *
     * @return true if under rate limit, false otherwise
     */
    public boolean canMakeRequest() {
        synchronized (lock) {
            long now = System.currentTimeMillis();

            // Remove timestamps older than 1 second
            while (!requestTimestamps.isEmpty() &&
                   now - requestTimestamps.peek() >= TIME_WINDOW_MS) {
                requestTimestamps.poll();
            }

            return requestTimestamps.size() < MAX_CALLS_PER_SECOND;
        }
    }

    /**
     * Gets the current number of requests in the time window
     */
    public int getCurrentRequestCount() {
        synchronized (lock) {
            long now = System.currentTimeMillis();

            // Remove timestamps older than 1 second
            while (!requestTimestamps.isEmpty() &&
                   now - requestTimestamps.peek() >= TIME_WINDOW_MS) {
                requestTimestamps.poll();
            }

            return requestTimestamps.size();
        }
    }

    /**
     * Resets the rate limiter (useful for testing or error recovery)
     */
    public void reset() {
        synchronized (lock) {
            requestTimestamps.clear();
            Log.d(TAG, "Rate limiter reset");
        }
    }
}
