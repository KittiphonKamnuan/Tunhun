package com.example.project.model;

import java.util.List;

/**
 * Response wrapper for insider transactions API
 */
public class InsiderTransactionResponse {
    private List<InsiderTransaction> data;
    private String symbol;

    public InsiderTransactionResponse() {
    }

    public InsiderTransactionResponse(List<InsiderTransaction> data, String symbol) {
        this.data = data;
        this.symbol = symbol;
    }

    public List<InsiderTransaction> getData() {
        return data;
    }

    public void setData(List<InsiderTransaction> data) {
        this.data = data;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
