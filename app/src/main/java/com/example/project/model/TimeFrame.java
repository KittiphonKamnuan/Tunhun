package com.example.project.model;

import java.util.Calendar;

/**
 * Enum representing different time frames for stock price charts.
 * Each time frame defines resolution, duration, and display settings for API calls and UI.
 */
public enum TimeFrame {
    // Short term
    ONE_DAY("D", 1 * 24 * 60 * 60, "1D"),           // Daily resolution, 1 day
    FIVE_DAYS("D", 5 * 24 * 60 * 60, "5D"),         // Daily resolution, 5 days

    // Medium term
    ONE_MONTH("D", 30 * 24 * 60 * 60, "1M"),        // Daily resolution, 1 month
    SIX_MONTHS("D", 180 * 24 * 60 * 60, "6M"),      // Daily resolution, 6 months

    // Year to Date (calculated dynamically)
    YTD("D", 0, "YTD"),                              // Daily resolution, from Jan 1 to now

    // Long term
    ONE_YEAR("D", 365 * 24 * 60 * 60, "1Y"),        // Daily resolution, 1 year
    FIVE_YEARS("W", 5 * 365 * 24 * 60 * 60, "5Y"),  // Weekly resolution, 5 years
    MAX("M", 10 * 365 * 24 * 60 * 60, "MAX");       // Monthly resolution, 10 years (max)

    private final String resolution;      // Finnhub API resolution (1, 5, 15, 30, 60, D, W, M)
    private final long durationSeconds;   // Duration to fetch in seconds
    private final String label;           // Display label for UI

    /**
     * Constructor for TimeFrame enum.
     *
     * @param resolution      Finnhub API resolution ("1", "5", "30", "60", "D", "W", "M")
     * @param durationSeconds Duration to fetch in seconds
     * @param label           Display label for UI (e.g., "5M", "1H")
     */
    TimeFrame(String resolution, long durationSeconds, String label) {
        this.resolution = resolution;
        this.durationSeconds = durationSeconds;
        this.label = label;
    }

    /**
     * Gets the Finnhub API resolution parameter.
     * Values: 1, 5, 15, 30, 60 for minutes; D for day; W for week; M for month
     *
     * @return API resolution string
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * Gets the duration to fetch in seconds.
     *
     * @return Duration in seconds
     */
    public long getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * Gets the display label for this time frame.
     *
     * @return Display label (e.g., "5M", "1H")
     */
    public String getLabel() {
        return label;
    }

    /**
     * Calculates the 'from' timestamp for API calls.
     * For YTD, calculates from January 1st of current year.
     *
     * @param currentTime Current Unix timestamp in seconds
     * @return 'from' timestamp
     */
    public long getFromTimestamp(long currentTime) {
        if (this == YTD) {
            // Calculate from January 1st of current year
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(currentTime * 1000);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis() / 1000;
        }
        return currentTime - durationSeconds;
    }

    /**
     * Checks if this is an intraday time frame (minute-based resolution).
     *
     * @return true if intraday, false otherwise
     */
    public boolean isIntraday() {
        return !resolution.equals("D") && !resolution.equals("W") && !resolution.equals("M");
    }
}
