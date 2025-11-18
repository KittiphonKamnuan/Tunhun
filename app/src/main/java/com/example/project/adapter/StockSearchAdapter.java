package com.example.project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying stock search results
 */
public class StockSearchAdapter extends RecyclerView.Adapter<StockSearchAdapter.ViewHolder> {

    private List<String> stocks = new ArrayList<>();
    private OnStockClickListener listener;

    public interface OnStockClickListener {
        void onStockClick(String symbol);
    }

    public void setStocks(List<String> stocks) {
        this.stocks = stocks;
        notifyDataSetChanged();
    }

    public void setOnStockClickListener(OnStockClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String symbol = stocks.get(position);
        holder.bind(symbol);
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView symbolText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            symbolText = itemView.findViewById(R.id.text_symbol);
        }

        void bind(String symbol) {
            symbolText.setText(symbol);

            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStockClick(symbol);
                }
            });
        }
    }
}
