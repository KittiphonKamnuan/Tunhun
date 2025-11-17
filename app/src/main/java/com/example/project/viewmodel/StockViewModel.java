package com.example.project.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.project.model.Stock;
import com.example.project.repository.StockRepository;

import java.util.List;

/**
 * ViewModel for managing stock data in the UI
 */
public class StockViewModel extends AndroidViewModel {
    private final StockRepository repository;
    private final LiveData<List<Stock>> stockList;
    private final LiveData<Boolean> connectionStatus;

    public StockViewModel(@NonNull Application application) {
        super(application);
        repository = StockRepository.getInstance(application);
        stockList = repository.getStockList();
        connectionStatus = repository.getConnectionStatus();
    }

    public LiveData<List<Stock>> getStockList() {
        return stockList;
    }

    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatus;
    }

    public void connect() {
        repository.connect();
    }

    public void disconnect() {
        repository.disconnect();
    }

    public void addStock(String symbol) {
        repository.addStock(symbol);
    }

    public void removeStock(String symbol) {
        repository.removeStock(symbol);
    }

    public boolean isConnected() {
        return repository.isConnected();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Disconnect when ViewModel is cleared
        repository.disconnect();
    }
}
