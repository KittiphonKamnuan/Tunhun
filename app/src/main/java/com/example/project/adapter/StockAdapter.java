package com.example.project.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.Stock;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying stock items
 */
public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {
    private List<Stock> stockList;
    private OnStockRemoveListener removeListener;

    public interface OnStockRemoveListener {
        void onStockRemove(Stock stock);
    }

    public StockAdapter() {
        this.stockList = new ArrayList<>();
    }

    public void setOnStockRemoveListener(OnStockRemoveListener listener) {
        this.removeListener = listener;
    }

    public void setStockList(List<Stock> stocks) {
        this.stockList = stocks != null ? stocks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.bind(stock);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    class StockViewHolder extends RecyclerView.ViewHolder {
        private final TextView symbolTextView;
        private final TextView priceTextView;
        private final TextView changeTextView;
        private final ImageButton removeButton;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.text_symbol);
            priceTextView = itemView.findViewById(R.id.text_price);
            changeTextView = itemView.findViewById(R.id.text_change);
            removeButton = itemView.findViewById(R.id.button_remove);
        }

        public void bind(Stock stock) {
            symbolTextView.setText(stock.getSymbol());
            priceTextView.setText(stock.getFormattedPrice());

            // Format change with arrow
            String arrow = stock.isPositiveChange() ? "↑" : "↓";
            changeTextView.setText(arrow + " " + stock.getFormattedChangePercent());

            // Set color based on positive/negative change (minimalist - text only)
            if (stock.isPositiveChange()) {
                changeTextView.setTextColor(Color.parseColor("#10B981"));  // positiveGreen
            } else {
                changeTextView.setTextColor(Color.parseColor("#EF4444"));  // negativeRed
            }

            // Handle remove button click
            removeButton.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onStockRemove(stock);
                }
            });
        }
    }
}
