package com.example.project.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.project.R;
import com.example.project.repository.PortfolioRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Dialog for buying stocks
 */
public class BuyStockDialog extends DialogFragment {

    private String symbol;
    private double currentPrice;
    private double availableBalance;

    private TextView symbolText;
    private TextView priceText;
    private TextView balanceText;
    private TextInputEditText sharesInput;
    private TextView totalCostText;
    private MaterialButton btnCancel;
    private MaterialButton btnConfirmBuy;

    private PortfolioRepository portfolioRepository;
    private OnBuyConfirmedListener listener;

    public interface OnBuyConfirmedListener {
        void onBuyConfirmed(String symbol, double shares, double totalCost);
    }

    /**
     * Create new instance of BuyStockDialog
     */
    public static BuyStockDialog newInstance(String symbol, double currentPrice, double availableBalance) {
        BuyStockDialog dialog = new BuyStockDialog();
        Bundle args = new Bundle();
        args.putString("symbol", symbol);
        args.putDouble("price", currentPrice);
        args.putDouble("balance", availableBalance);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get arguments
        if (getArguments() != null) {
            symbol = getArguments().getString("symbol");
            currentPrice = getArguments().getDouble("price");
            availableBalance = getArguments().getDouble("balance");
        }

        // Initialize repository
        portfolioRepository = PortfolioRepository.getInstance(requireContext());

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_buy_stock, null);

        initViews(view);
        setupListeners();
        updateUI();

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        symbolText = view.findViewById(R.id.text_dialog_symbol);
        priceText = view.findViewById(R.id.text_dialog_price);
        balanceText = view.findViewById(R.id.text_dialog_balance);
        sharesInput = view.findViewById(R.id.input_shares);
        totalCostText = view.findViewById(R.id.text_total_cost);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnConfirmBuy = view.findViewById(R.id.btn_confirm_buy);
    }

    private void setupListeners() {
        // Cancel button
        btnCancel.setOnClickListener(v -> dismiss());

        // Shares input text watcher
        sharesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalCost();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm buy button
        btnConfirmBuy.setOnClickListener(v -> confirmPurchase());
    }

    private void updateUI() {
        symbolText.setText(symbol);
        priceText.setText(String.format("$%.2f", currentPrice));
        balanceText.setText(String.format("$%.2f", availableBalance));
    }

    private void updateTotalCost() {
        String sharesStr = sharesInput.getText().toString();
        if (sharesStr.isEmpty()) {
            totalCostText.setText("$0.00");
            return;
        }

        try {
            double shares = Double.parseDouble(sharesStr);
            double totalCost = shares * currentPrice;
            totalCostText.setText(String.format("$%.2f", totalCost));

            // Change color if exceeds balance
            if (totalCost > availableBalance) {
                totalCostText.setTextColor(getResources().getColor(R.color.negative_red, null));
            } else {
                totalCostText.setTextColor(getResources().getColor(R.color.dimePurple, null));
            }
        } catch (NumberFormatException e) {
            totalCostText.setText("$0.00");
        }
    }

    private void confirmPurchase() {
        String sharesStr = sharesInput.getText().toString();

        // Validate input
        if (sharesStr.isEmpty()) {
            Toast.makeText(getContext(), R.string.toast_enter_shares, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double shares = Double.parseDouble(sharesStr);

            if (shares <= 0) {
                Toast.makeText(getContext(), R.string.toast_invalid_shares_zero, Toast.LENGTH_SHORT).show();
                return;
            }

            double totalCost = shares * currentPrice;

            if (totalCost > availableBalance) {
                Toast.makeText(getContext(), R.string.toast_insufficient_balance, Toast.LENGTH_SHORT).show();
                return;
            }

            // Execute purchase
            boolean success = portfolioRepository.buyStock(symbol, shares, currentPrice);

            if (success) {
                Toast.makeText(getContext(),
                        getString(R.string.toast_buy_success, String.valueOf((int)shares), symbol),
                        Toast.LENGTH_SHORT).show();

                // Notify listener
                if (listener != null) {
                    listener.onBuyConfirmed(symbol, shares, totalCost);
                }

                dismiss();
            } else {
                Toast.makeText(getContext(), R.string.toast_purchase_failed, Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.toast_invalid_shares, Toast.LENGTH_SHORT).show();
        }
    }

    public void setOnBuyConfirmedListener(OnBuyConfirmedListener listener) {
        this.listener = listener;
    }
}
