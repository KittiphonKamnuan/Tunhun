package com.example.project.model;

/**
 * Model class representing current market status
 */
public class MarketStatus {
    private String exchange;
    private String holiday;
    private boolean isOpen;
    private String session;
    private String timezone;
    private long t; // timestamp

    public MarketStatus() {
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getHoliday() {
        return holiday;
    }

    public void setHoliday(String holiday) {
        this.holiday = holiday;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public long getTimestamp() {
        return t;
    }

    public void setTimestamp(long t) {
        this.t = t;
    }

    /**
     * Get formatted status text for display
     */
    public String getStatusText() {
        if (isOpen) {
            return "Market Open";
        } else {
            return "Market Closed";
        }
    }

    /**
     * Get formatted session text for display
     */
    public String getSessionText() {
        if (session == null || session.isEmpty()) {
            return "Closed";
        }

        switch (session.toLowerCase()) {
            case "pre-market":
                return "Pre-Market";
            case "regular":
                return "Market Hours";
            case "post-market":
                return "After Hours";
            default:
                return session;
        }
    }

    /**
     * Get status emoji
     */
    public String getStatusEmoji() {
        return isOpen ? "🟢" : "🔴";
    }

    /**
     * Check if market is in regular trading hours
     */
    public boolean isRegularSession() {
        return "regular".equalsIgnoreCase(session);
    }

    /**
     * Check if market is in pre-market session
     */
    public boolean isPreMarket() {
        return "pre-market".equalsIgnoreCase(session);
    }

    /**
     * Check if market is in post-market session
     */
    public boolean isPostMarket() {
        return "post-market".equalsIgnoreCase(session);
    }
}
