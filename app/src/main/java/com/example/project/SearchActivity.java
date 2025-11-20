package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapter.StockDashboardAdapter;
import com.example.project.model.Stock;
import com.example.project.repository.WatchlistRepository;
import com.example.project.viewmodel.StockViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
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
    private WatchlistRepository watchlistRepository;

    private Chip chipAAPL, chipTSLA, chipGOOGL, chipMSFT, chipAMZN, chipMETA, chipNVDA, chipNFLX;

    private static final List<String> ALL_POPULAR_STOCKS = Arrays.asList(
            // Tech Giants
            "AAPL", "MSFT", "GOOGL", "GOOG", "AMZN", "META", "TSLA", "NVDA", "NFLX", "AMD",
            // Finance
            "JPM", "BAC", "WFC", "GS", "MS", "V", "MA", "AXP",
            // Consumer
            "WMT", "TGT", "COST", "NKE", "SBUX", "MCD", "DIS", "CMCSA",
            // Healthcare
            "JNJ", "UNH", "PFE", "ABBV", "TMO", "DHR", "CVS", "LLY",
            // Industrial
            "BA", "CAT", "GE", "MMM", "HON", "UPS", "RTX",
            // Energy
            "XOM", "CVX", "COP", "SLB", "EOG",
            // Telecom
            "T", "VZ", "TMUS",
            // Retail
            "HD", "LOW", "TJX",
            // Semiconductor
            "INTC", "QCOM", "AVGO", "MU", "AMAT", "LRCX",
            // Software
            "ORCL", "ADBE", "CRM", "NOW", "INTU", "PANW",
            // Social Media & Entertainment
            "SNAP", "PINS", "SPOT", "RBLX",
            // E-commerce
            "EBAY", "ETSY", "SHOP",
            // Auto
            "F", "GM", "RIVN", "LCID",
            // Biotech
            "GILD", "BIIB", "VRTX", "REGN", "MRNA",
            // Aerospace
            "LMT", "NOC", "GD",
            // Hospitality
            "MAR", "HLT", "ABNB",
            // Payment
            "PYPL", "SQ", "COIN",
            // Cloud/SaaS
            "SNOW", "DDOG", "CRWD", "ZS", "NET",
            // Communication
            "TWTR", "ZM", "DOCU"
    );

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
        watchlistRepository = WatchlistRepository.getInstance(this);

        // Connect if not already connected
        if (!viewModel.isConnected()) {
            viewModel.connect();
        }
    }

    private void setupRecyclerView() {
        resultsAdapter = new StockDashboardAdapter();
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(resultsAdapter);

        // ✅ แก้: Handle stock click - เพิ่มหุ้นเข้า watchlist และเปิดหน้ารายละเอียด
        resultsAdapter.setOnStockClickListener(stock -> {
            boolean added = watchlistRepository.addSymbol(stock.getSymbol());
            if (added) {
                viewModel.addStock(stock.getSymbol());
                Toast.makeText(this, "เพิ่ม " + stock.getSymbol() + " เข้า Watchlist แล้ว", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, stock.getSymbol() + " อยู่ใน Watchlist แล้ว", Toast.LENGTH_SHORT).show();
            }

            // เปิดหน้ารายละเอียด
            openStockDetail(stock.getSymbol());
        });
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
        chipAAPL.setOnClickListener(v -> searchAndAddStock("AAPL"));
        chipTSLA.setOnClickListener(v -> searchAndAddStock("TSLA"));
        chipGOOGL.setOnClickListener(v -> searchAndAddStock("GOOGL"));
        chipMSFT.setOnClickListener(v -> searchAndAddStock("MSFT"));
        chipAMZN.setOnClickListener(v -> searchAndAddStock("AMZN"));
        chipMETA.setOnClickListener(v -> searchAndAddStock("META"));
        chipNVDA.setOnClickListener(v -> searchAndAddStock("NVDA"));
        chipNFLX.setOnClickListener(v -> searchAndAddStock("NFLX"));
    }

    /**
     * ✅ แก้: Search จากรายการหุ้นทั้งหมด พร้อม observe ข้อมูลจริงจาก ViewModel
     */
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

        // Search จาก ALL_POPULAR_STOCKS
        String upperQuery = query.toUpperCase();
        List<String> matchedSymbols = ALL_POPULAR_STOCKS.stream()
                .filter(symbol -> symbol.contains(upperQuery))
                .limit(20)
                .collect(Collectors.toList());

        if (matchedSymbols.isEmpty()) {
            // ถ้าไม่เจอในรายการ ให้แสดง symbol ที่พิมพ์เข้ามาเอง
            if (upperQuery.matches("[A-Z]+") && upperQuery.length() <= 5) {
                matchedSymbols = Arrays.asList(upperQuery);
            } else {
                recyclerResults.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                return;
            }
        }

        // ✅ แก้: Subscribe หุ้นเหล่านี้เพื่อดึงข้อมูลจริง
        for (String symbol : matchedSymbols) {
            viewModel.addStock(symbol);
        }

        // ✅ แก้: Observe ข้อมูลจริงจาก ViewModel
        List<String> finalMatchedSymbols = matchedSymbols;
        viewModel.getStockList().observe(this, stocks -> {
            if (stocks != null) {
                List<Stock> matchedStocks = stocks.stream()
                        .filter(stock -> finalMatchedSymbols.contains(stock.getSymbol()))
                        .collect(Collectors.toList());

                if (!matchedStocks.isEmpty()) {
                    recyclerResults.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                    resultsAdapter.setStocks(matchedStocks);
                }
            }
        });
    }

    /**
     * ✅ แก้: เพิ่มหุ้นเข้า watchlist และเปิดหน้ารายละเอียด
     */
    private void searchAndAddStock(String symbol) {
        searchInput.setText(symbol);

        boolean added = watchlistRepository.addSymbol(symbol);
        if (added) {
            viewModel.addStock(symbol);
            Toast.makeText(this, "เพิ่ม " + symbol + " เข้า Watchlist แล้ว", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, symbol + " อยู่ใน Watchlist แล้ว", Toast.LENGTH_SHORT).show();
        }

        // เปิดหน้ารายละเอียด
        openStockDetail(symbol);
    }

    /**
     * ✅ แก้: เปิดหน้ารายละเอียดด้วย symbol เท่านั้น ให้หน้า StockDetailActivity ดึงข้อมูลเอง
     */
    private void openStockDetail(String symbol) {
        Intent intent = new Intent(this, StockDetailActivity.class);
        intent.putExtra("symbol", symbol);
        intent.putExtra("price", 0.0);  // ให้หน้า StockDetail ดึงข้อมูลเอง
        intent.putExtra("change", 0.0);
        startActivity(intent);
    }
}