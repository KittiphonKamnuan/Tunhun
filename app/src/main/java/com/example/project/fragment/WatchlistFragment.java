package com.example.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.StockDetailActivity;
import com.example.project.adapter.PortfolioAdapter;
import com.example.project.model.PortfolioItem;
import com.example.project.repository.PortfolioRepository;
import com.example.project.repository.StockRepository;

import java.util.List;

/**
 * Fragment for displaying user's portfolio (stocks they own)
 */
public class WatchlistFragment extends Fragment {

    private PortfolioRepository portfolioRepository;
    private StockRepository stockRepository;
    private PortfolioAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;
    private TextView balanceText;
    private TextView totalValueText;
    private TextView profitLossText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRepositories();
        setupRecyclerView();
        observeData();
        updatePrices();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_watchlist);
        emptyStateView = view.findViewById(R.id.empty_state);
        balanceText = view.findViewById(R.id.text_balance);
        totalValueText = view.findViewById(R.id.text_total_value);
        profitLossText = view.findViewById(R.id.text_profit_loss);
    }

    private void setupRepositories() {
        portfolioRepository = PortfolioRepository.getInstance(requireContext());
        stockRepository = StockRepository.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        adapter = new PortfolioAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Handle item clicks - open stock detail
        adapter.setOnPortfolioItemClickListener(item -> {
            Intent intent = new Intent(getContext(), StockDetailActivity.class);
            intent.putExtra("symbol", item.getSymbol());
            intent.putExtra("price", item.getCurrentPrice());
            intent.putExtra("change", item.getProfitLossPercent());
            startActivity(intent);
        });
    }

    private void observeData() {
        // Observe portfolio changes
        portfolioRepository.getPortfolio().observe(getViewLifecycleOwner(), portfolioItems -> {
            adapter.setPortfolioItems(portfolioItems);

            // Show/hide empty state
            if (portfolioItems == null || portfolioItems.isEmpty()) {
                emptyStateView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            // Update summary
            updateSummary(portfolioItems);
        });

        // Observe balance changes
        portfolioRepository.getBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balanceText != null && balance != null) {
                balanceText.setText(String.format("$%.2f", balance));
            }
        });

        // Observe stock price updates to update portfolio prices
        stockRepository.getStockList().observe(getViewLifecycleOwner(), stocks -> {
            if (stocks != null) {
                for (com.example.project.model.Stock stock : stocks) {
                    portfolioRepository.updateStockPrice(stock.getSymbol(), stock.getCurrentPrice());
                }
            }
        });
    }

    /**
     * Update portfolio summary (total value and profit/loss)
     */
    private void updateSummary(List<PortfolioItem> items) {
        if (items == null || items.isEmpty()) {
            if (totalValueText != null) {
                totalValueText.setText("$0.00");
            }
            if (profitLossText != null) {
                profitLossText.setText("$0.00");
            }
            return;
        }

        double totalValue = portfolioRepository.getTotalPortfolioValue();
        double totalProfitLoss = portfolioRepository.getTotalProfitLoss();

        if (totalValueText != null) {
            totalValueText.setText(String.format("$%.2f", totalValue));
        }

        if (profitLossText != null) {
            String sign = totalProfitLoss >= 0 ? "+" : "";
            profitLossText.setText(String.format("%s$%.2f", sign, totalProfitLoss));

            // Set color based on profit/loss
            int color = totalProfitLoss >= 0 ?
                    getResources().getColor(R.color.positive_green, null) :
                    getResources().getColor(R.color.negative_red, null);
            profitLossText.setTextColor(color);
        }
    }

    /**
     * Update stock prices by ensuring WebSocket connection
     */
    private void updatePrices() {
        if (!stockRepository.isConnected()) {
            stockRepository.connect();
        }

        // Subscribe to all stocks in portfolio
        List<PortfolioItem> items = portfolioRepository.getPortfolio().getValue();
        if (items != null) {
            for (PortfolioItem item : items) {
                stockRepository.addStock(item.getSymbol());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePrices();
    }
}
