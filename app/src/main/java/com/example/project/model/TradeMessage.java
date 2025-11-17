package com.example.project.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a WebSocket trade message from Finnhub API
 * Format: {"data":[{"p":price,"s":"symbol","t":timestamp,"v":volume}],"type":"trade"}
 */
public class TradeMessage {
    @SerializedName("type")
    private String type;

    @SerializedName("data")
    private List<TradeData> data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TradeData> getData() {
        return data;
    }

    public void setData(List<TradeData> data) {
        this.data = data;
    }

    public static class TradeData {
        @SerializedName("p")
        private double price;

        @SerializedName("s")
        private String symbol;

        @SerializedName("t")
        private long timestamp;

        @SerializedName("v")
        private double volume;

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }
    }
}
