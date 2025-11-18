package com.example.project.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.Stock;
import com.example.project.util.ChartHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StockDashboardAdapter extends RecyclerView.Adapter<StockDashboardAdapter.ViewHolder> {

    private List<Stock> stocks = new ArrayList<>();
    private OnStockClickListener clickListener;

    // Company names map
    private static final Map<String, String> COMPANY_NAMES = new HashMap<>();
    static {
        COMPANY_NAMES.put("AAPL", "Apple Inc.");
        COMPANY_NAMES.put("TSLA", "Tesla Inc.");
        COMPANY_NAMES.put("GOOGL", "Alphabet Inc.");
        COMPANY_NAMES.put("MSFT", "Microsoft Corp.");
        COMPANY_NAMES.put("AMZN", "Amazon.com Inc.");
        COMPANY_NAMES.put("META", "Meta Platforms Inc.");
        COMPANY_NAMES.put("NVDA", "NVIDIA Corporation");
        COMPANY_NAMES.put("NFLX", "Netflix Inc.");
    }

    public interface OnStockClickListener {
        void onStockClick(Stock stock);
    }

    public void setOnStockClickListener(OnStockClickListener listener) {
        this.clickListener = listener;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks != null ? stocks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stocks.get(position);
        holder.bind(stock);
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView symbolText;
        private final TextView companyText;
        private final TextView priceText;
        private final TextView changeText;
        private final LineChart miniChart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.text_symbol);
            companyText = itemView.findViewById(R.id.text_company_name);
            priceText = itemView.findViewById(R.id.text_price);
            changeText = itemView.findViewById(R.id.text_change);
            miniChart = itemView.findViewById(R.id.mini_chart);

            // Configure mini chart once
            ChartHelper.configureMiniChart(miniChart);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onStockClick(stocks.get(position));
                }
            });
        }

        public void bind(Stock stock) {
            String symbol = stock.getSymbol();

            // Set symbol
            symbolText.setText(symbol);

            // Set company name
            companyText.setText(COMPANY_NAMES.getOrDefault(symbol, "Company"));

            // Set price
            priceText.setText(stock.getFormattedPrice());

            // Set change with arrow
            String arrow = stock.isPositiveChange() ? "↑" : "↓";
            changeText.setText(arrow + " " + stock.getFormattedChangePercent());

            // Set color
            int color = stock.isPositiveChange() ?
                    Color.parseColor("#10B981") : Color.parseColor("#EF4444");
            changeText.setTextColor(color);

            // Update mini chart with sparkline data
            updateMiniChart(stock);
        }

        private void updateMiniChart(Stock stock) {
            // Generate sparkline data (simulated trend based on current price)
            List<Entry> entries = generateSparklineData(
                    stock.getCurrentPrice(),
                    stock.getChangePercent()
            );

            // Update chart
            ChartHelper.updateMiniChart(
                    miniChart,
                    entries,
                    stock.getChangePercent(),
                    itemView.getContext()
            );
        }

        /**
         * Generates simulated sparkline data for mini chart.
         * Creates a realistic price trend based on current price and change percent.
         */
        private List<Entry> generateSparklineData(double currentPrice, double changePercent) {
            List<Entry> entries = new ArrayList<>();
            Random random = new Random(System.currentTimeMillis());

            int dataPoints = 20; // Number of points in sparkline
            double startPrice = currentPrice / (1 + (changePercent / 100));

            for (int i = 0; i < dataPoints; i++) {
                // Generate smooth progression from start to current price
                double progress = (double) i / (dataPoints - 1);
                double price = startPrice + (currentPrice - startPrice) * progress;

                // Add small random variation for realistic look (±0.5%)
                double variation = 1 + (random.nextDouble() - 0.5) * 0.01;
                price *= variation;

                entries.add(new Entry(i, (float) price));
            }

            return entries;
        }
    }
}
