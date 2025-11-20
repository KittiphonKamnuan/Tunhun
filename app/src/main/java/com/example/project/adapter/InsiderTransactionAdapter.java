package com.example.project.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.InsiderTransaction;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying insider transactions in a RecyclerView
 */
public class InsiderTransactionAdapter extends RecyclerView.Adapter<InsiderTransactionAdapter.ViewHolder> {

    private List<InsiderTransaction> transactions = new ArrayList<>();
    private Context context;

    public InsiderTransactionAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_insider_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InsiderTransaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    /**
     * Update the list of transactions
     */
    public void setTransactions(List<InsiderTransaction> newTransactions) {
        this.transactions = newTransactions != null ? newTransactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for insider transaction items
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textInsiderName;
        private TextView textTransactionType;
        private TextView textTransactionDate;
        private TextView textSharesChanged;
        private TextView textTransactionPrice;
        private TextView textTotalShares;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textInsiderName = itemView.findViewById(R.id.text_insider_name);
            textTransactionType = itemView.findViewById(R.id.text_transaction_type);
            textTransactionDate = itemView.findViewById(R.id.text_transaction_date);
            textSharesChanged = itemView.findViewById(R.id.text_shares_changed);
            textTransactionPrice = itemView.findViewById(R.id.text_transaction_price);
            textTotalShares = itemView.findViewById(R.id.text_total_shares);
        }

        public void bind(InsiderTransaction transaction) {
            // Set insider name
            textInsiderName.setText(transaction.getName());

            // Set transaction date
            textTransactionDate.setText(transaction.getTransactionDate());

            // Set transaction type with color
            String transactionType = transaction.getTransactionType();
            textTransactionType.setText(transactionType);

            // Style the transaction type badge
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(12f);

            if (transaction.isBuy()) {
                background.setColor(ContextCompat.getColor(context, R.color.positive_green));
                textTransactionType.setBackground(background);
            } else if (transaction.isSell()) {
                background.setColor(ContextCompat.getColor(context, R.color.negative_red));
                textTransactionType.setBackground(background);
            } else {
                background.setColor(ContextCompat.getColor(context, R.color.text_secondary));
                textTransactionType.setBackground(background);
            }

            // Set shares changed with sign and color
            long change = transaction.getChange();
            String sharesChangedText;
            if (change > 0) {
                sharesChangedText = "+" + NumberFormat.getNumberInstance(Locale.US).format(change);
                textSharesChanged.setTextColor(ContextCompat.getColor(context, R.color.positive_green));
            } else if (change < 0) {
                sharesChangedText = NumberFormat.getNumberInstance(Locale.US).format(change);
                textSharesChanged.setTextColor(ContextCompat.getColor(context, R.color.negative_red));
            } else {
                sharesChangedText = "0";
                textSharesChanged.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
            textSharesChanged.setText(sharesChangedText);

            // Set transaction price
            textTransactionPrice.setText(String.format(Locale.US, "$%.2f", transaction.getTransactionPrice()));

            // Set total shares after transaction
            textTotalShares.setText(NumberFormat.getNumberInstance(Locale.US).format(transaction.getShare()));
        }
    }
}
