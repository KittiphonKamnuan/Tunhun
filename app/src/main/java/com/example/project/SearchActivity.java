package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapter.StockDashboardAdapter;
import com.example.project.model.Stock;
import com.example.project.viewmodel.StockViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton btnBack;
    private RecyclerView recyclerResults;
    private View layoutSuggestions;
    private View emptyState;
    private StockDashboardAdapter resultsAdapter;
    private StockViewModel viewModel;

    private Chip chipAAPL, chipTSLA, chipGOOGL, chipMSFT, chipAMZN, chipMETA, chipNVDA, chipNFLX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupListeners();

        // Show keyboard automatically
        searchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void initViews() {
        searchInput = findViewById(R.id.edit_search);
        btnBack = findViewById(R.id.btn_back);
        recyclerResults = findViewById(R.id.recycler_results);
        layoutSuggestions = findViewById(R.id.layout_suggestions);
        emptyState = findViewById(R.id.empty_state);

        chipAAPL = findViewById(R.id.chip_aapl);
        chipTSLA = findViewById(R.id.chip_tsla);
        chipGOOGL = findViewById(R.id.chip_googl);
        chipMSFT = findViewById(R.id.chip_msft);
        chipAMZN = findViewById(R.id.chip_amzn);
        chipMETA = findViewById(R.id.chip_meta);
        chipNVDA = findViewById(R.id.chip_nvda);
        chipNFLX = findViewById(R.id.chip_nflx);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StockViewModel.class);

        // Connect if not already connected
        if (!viewModel.isConnected()) {
            viewModel.connect();
        }
    }

    private void setupRecyclerView() {
        resultsAdapter = new StockDashboardAdapter();
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(resultsAdapter);

        // Handle stock click
        resultsAdapter.setOnStockClickListener(this::openStockDetail);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Search text change
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip clicks
        chipAAPL.setOnClickListener(v -> searchStock("AAPL"));
        chipTSLA.setOnClickListener(v -> searchStock("TSLA"));
        chipGOOGL.setOnClickListener(v -> searchStock("GOOGL"));
        chipMSFT.setOnClickListener(v -> searchStock("MSFT"));
        chipAMZN.setOnClickListener(v -> searchStock("AMZN"));
        chipMETA.setOnClickListener(v -> searchStock("META"));
        chipNVDA.setOnClickListener(v -> searchStock("NVDA"));
        chipNFLX.setOnClickListener(v -> searchStock("NFLX"));
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Show suggestions
            layoutSuggestions.setVisibility(View.VISIBLE);
            recyclerResults.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
            return;
        }

        // Hide suggestions
        layoutSuggestions.setVisibility(View.GONE);

        // Search in current stock list
        viewModel.getStockList().observe(this, stocks -> {
            if (stocks != null) {
                List<Stock> filteredStocks = stocks.stream()
                        .filter(stock -> stock.getSymbol().toUpperCase().contains(query.toUpperCase()))
                        .collect(Collectors.toList());

                if (filteredStocks.isEmpty()) {
                    recyclerResults.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerResults.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                    resultsAdapter.setStocks(filteredStocks);
                }
            }
        });
    }

    private void searchStock(String symbol) {
        searchInput.setText(symbol);

        // Add stock to viewmodel if not already added
        viewModel.addStock(symbol);

        // Perform search
        performSearch(symbol);
    }

    private void openStockDetail(Stock stock) {
        Intent intent = new Intent(this, StockDetailActivity.class);
        intent.putExtra("symbol", stock.getSymbol());
        intent.putExtra("price", stock.getCurrentPrice());
        intent.putExtra("change", stock.getChangePercent());
        startActivity(intent);
    }
}
