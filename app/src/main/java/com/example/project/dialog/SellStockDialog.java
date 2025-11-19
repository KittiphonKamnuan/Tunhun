package com.example.project.dialog;

import android.app.Dialog;
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
 * Dialog for selling stocks
 */
public class SellStockDialog extends DialogFragment {

    private String symbol;
    private double currentPrice;
    private double ownedShares;

    private TextView symbolText;
    private TextView priceText;
    private TextView ownedSharesText;
    private TextInputEditText sharesInput;
    private TextView totalValueText;
    private MaterialButton btnCancel;
    private MaterialButton btnConfirmSell;

    private PortfolioRepository portfolioRepository;
    private OnSellConfirmedListener listener;

    public interface OnSellConfirmedListener {
        void onSellConfirmed(String symbol, double shares, double totalValue);
    }

    public static SellStockDialog newInstance(String symbol, double currentPrice, double ownedShares) {
        SellStockDialog dialog = new SellStockDialog();
        Bundle args = new Bundle();
        args.putString("symbol", symbol);
        args.putDouble("price", currentPrice);
        args.putDouble("ownedShares", ownedShares);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            symbol = getArguments().getString("symbol");
            currentPrice = getArguments().getDouble("price");
            ownedShares = getArguments().getDouble("ownedShares");
        }

        portfolioRepository = PortfolioRepository.getInstance(requireContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_sell_stock, null);

        initViews(view);
        setupListeners();
        updateUI();

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        symbolText = view.findViewById(R.id.text_dialog_symbol_sell);
        priceText = view.findViewById(R.id.text_dialog_price_sell);
        ownedSharesText = view.findViewById(R.id.text_dialog_owned);
        sharesInput = view.findViewById(R.id.input_shares_sell);
        totalValueText = view.findViewById(R.id.text_total_value_sell);
        btnCancel = view.findViewById(R.id.btn_cancel_sell);
        btnConfirmSell = view.findViewById(R.id.btn_confirm_sell);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        sharesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalValue();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnConfirmSell.setOnClickListener(v -> confirmSale());
    }

    private void updateUI() {
        symbolText.setText(symbol);
        priceText.setText(String.format("$%.2f", currentPrice));
        if (ownedShares % 1 == 0) {
            ownedSharesText.setText(String.format("%.0f", ownedShares));
        } else {
            ownedSharesText.setText(String.format("%.2f", ownedShares));
        }
    }

    private void updateTotalValue() {
        String sharesStr = sharesInput.getText().toString();
        if (sharesStr.isEmpty()) {
            totalValueText.setText("$0.00");
            totalValueText.setTextColor(getResources().getColor(R.color.text_primary, null));
            return;
        }

        try {
            double shares = Double.parseDouble(sharesStr);
            double totalValue = shares * currentPrice;
            totalValueText.setText(String.format("$%.2f", totalValue));

            if (shares > ownedShares) {
                totalValueText.setTextColor(getResources().getColor(R.color.negative_red, null));
            } else {
                totalValueText.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
        } catch (NumberFormatException e) {
            totalValueText.setText("$0.00");
        }
    }

    private void confirmSale() {
        String sharesStr = sharesInput.getText().toString();

        if (sharesStr.isEmpty()) {
            Toast.makeText(getContext(), "กรุณาใส่จำนวนหุ้น", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double shares = Double.parseDouble(sharesStr);

            if (shares <= 0) {
                Toast.makeText(getContext(), "จำนวนหุ้นต้องมากกว่า 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (shares > ownedShares) {
                Toast.makeText(getContext(), "คุณมีหุ้นไม่พอสำหรับขาย", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalValue = shares * currentPrice;
            boolean success = portfolioRepository.sellStock(symbol, shares, currentPrice);

            if (success) {
                Toast.makeText(getContext(),
                        String.format("ขาย %.0f หุ้น %s สำเร็จ", shares, symbol),
                        Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onSellConfirmed(symbol, shares, totalValue);
                }

                dismiss();
            } else {
                Toast.makeText(getContext(), "การขายล้มเหลว กรุณาลองใหม่", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "จำนวนหุ้นไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
        }
    }

    public void setOnSellConfirmedListener(OnSellConfirmedListener listener) {
        this.listener = listener;
    }
}

