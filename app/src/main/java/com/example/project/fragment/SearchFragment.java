package com.example.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.StockDetailActivity;
import com.example.project.adapter.StockSearchAdapter;
import com.example.project.repository.WatchlistRepository;
import com.example.project.viewmodel.StockViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fragment for searching stocks
 */
public class SearchFragment extends Fragment {

    private EditText searchInput;
    private RecyclerView searchResults;
    private TextView emptyView;
    private StockSearchAdapter adapter;
    private StockViewModel viewModel;
    private WatchlistRepository watchlistRepository;

    // Comprehensive list of popular stocks for search
    private static final List<String> ALL_POPULAR_STOCKS = Arrays.asList(
            // Tech Giants
            "AAPL", "MSFT", "GOOGL", "GOOG", "AMZN", "META", "TSLA", "NVDA",
            "NFLX", "ORCL", "CSCO", "INTC", "AMD", "ADBE", "CRM", "QCOM",
            "IBM", "TXN", "AVGO", "PYPL", "SHOP", "UBER", "LYFT", "SQ",

            // Finance
            "JPM", "BAC", "WFC", "C", "GS", "MS", "BLK", "SCHW", "AXP",
            "V", "MA", "COF", "USB", "PNC", "TFC",

            // Consumer
            "WMT", "HD", "NKE", "SBUX", "MCD", "DIS", "CMCSA", "TGT", "LOW",
            "COST", "CVS", "KO", "PEP", "PG", "CL", "KMB",

            // Healthcare
            "JNJ", "UNH", "PFE", "ABBV", "TMO", "MRK", "ABT", "LLY", "DHR",
            "AMGN", "BMY", "GILD", "CVS", "CI", "HUM",

            // Energy
            "XOM", "CVX", "COP", "SLB", "EOG", "MPC", "PSX", "VLO", "OXY",

            // Industrial
            "BA", "CAT", "GE", "HON", "UPS", "LMT", "MMM", "DE", "UNP",

            // Consumer Discretionary
            "AMZN", "TSLA", "HD", "NKE", "MCD", "SBUX", "TGT", "LOW", "TJX",

            // Communication Services
            "GOOGL", "META", "DIS", "NFLX", "CMCSA", "T", "VZ", "TMUS",

            // Materials
            "LIN", "APD", "SHW", "ECL", "FCX", "NEM", "DOW",

            // Utilities
            "NEE", "DUK", "SO", "D", "AEP", "EXC", "SRE",

            // Real Estate
            "AMT", "PLD", "CCI", "EQIX", "PSA", "SPG", "O", "WELL",

            // Popular ETFs
            "SPY", "QQQ", "IWM", "DIA", "VOO", "VTI", "VEA", "VWO",

            // Crypto & Fintech
            "COIN", "SQ", "HOOD", "SOFI", "AFRM", "UPST",

            // EV & Clean Energy
            "TSLA", "NIO", "RIVN", "LCID", "XPEV", "ENPH", "SEDG", "PLUG",

            // Semiconductors
            "NVDA", "AMD", "INTC", "TSM", "QCOM", "AVGO", "TXN", "MU", "ASML"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearch();

        // Show all stocks initially
        filterStocks("");
    }

    private void initViews(View view) {
        searchInput = view.findViewById(R.id.search_input);
        searchResults = view.findViewById(R.id.search_results);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StockViewModel.class);
        watchlistRepository = WatchlistRepository.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        adapter = new StockSearchAdapter();
        searchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults.setAdapter(adapter);

        adapter.setOnStockClickListener(symbol -> {
            boolean added = watchlistRepository.addSymbol(symbol);
            if (added) {
                viewModel.addStock(symbol);
                Toast.makeText(getContext(), "เพิ่ม " + symbol + " เข้า Watchlist แล้ว", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), symbol + " อยู่ใน Watchlist แล้ว", Toast.LENGTH_SHORT).show();
            }
            openStockDetail(symbol);
        });
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStocks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterStocks(String query) {
        if (query.isEmpty()) {
            adapter.setStocks(ALL_POPULAR_STOCKS);
            updateEmptyView(false);
        } else {
            String upperQuery = query.toUpperCase();
            List<String> filtered = ALL_POPULAR_STOCKS.stream()
                    .filter(symbol -> symbol.contains(upperQuery))
                    .collect(Collectors.toList());
            adapter.setStocks(filtered);
            updateEmptyView(filtered.isEmpty());
        }
    }

    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        searchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openStockDetail(String symbol) {
        Intent intent = new Intent(getContext(), StockDetailActivity.class);
        intent.putExtra("symbol", symbol);
        intent.putExtra("price", 0.0);
        intent.putExtra("change", 0.0);
        startActivity(intent);
    }
}
