package com.example.project.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.PortfolioItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying portfolio holdings
 */
public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private List<PortfolioItem> portfolioItems = new ArrayList<>();
    private OnPortfolioItemClickListener clickListener;

    public interface OnPortfolioItemClickListener {
        void onPortfolioItemClick(PortfolioItem item);
    }

    public void setPortfolioItems(List<PortfolioItem> items) {
        this.portfolioItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnPortfolioItemClickListener(OnPortfolioItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PortfolioItem item = portfolioItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return portfolioItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView symbolText;
        private final TextView sharesText;
        private final TextView currentValueText;
        private final TextView avgCostText;
        private final TextView profitLossText;
        private final TextView profitLossPercentText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.text_symbol);
            sharesText = itemView.findViewById(R.id.text_shares);
            currentValueText = itemView.findViewById(R.id.text_current_value);
            avgCostText = itemView.findViewById(R.id.text_avg_cost);
            profitLossText = itemView.findViewById(R.id.text_profit_loss);
            profitLossPercentText = itemView.findViewById(R.id.text_profit_loss_percent);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onPortfolioItemClick(portfolioItems.get(position));
                }
            });
        }

        public void bind(PortfolioItem item) {
            // Set symbol
            symbolText.setText(item.getSymbol());

            // Set shares
            sharesText.setText(item.getFormattedShares() + " shares");

            // Set current value
            currentValueText.setText(item.getFormattedCurrentValue());

            // Set average cost
            avgCostText.setText(item.getFormattedAverageCost());

            // Set profit/loss
            profitLossText.setText(item.getFormattedProfitLoss());
            profitLossPercentText.setText("(" + item.getFormattedProfitLossPercent() + ")");

            // Set profit/loss color
            int profitLossColor = item.isProfitable() ?
                    Color.parseColor("#00C853") : Color.parseColor("#FF3B30");
            profitLossText.setTextColor(profitLossColor);
            profitLossPercentText.setTextColor(profitLossColor);
        }
    }
}
