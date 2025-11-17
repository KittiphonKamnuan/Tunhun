package com.example.project.util;

import android.content.Context;

import com.example.project.R;

/**
 * Utility class for handling stock-related color logic.
 * Provides methods to get appropriate colors based on stock price changes.
 */
public class StockColorHelper {

    /**
     * Gets the appropriate color for a stock based on its change percentage.
     *
     * @param context        Android context for accessing resources
     * @param changePercent  Stock price change percentage
     * @return Color resource ID (green for positive, red for negative)
     */
    public static int getStockColor(Context context, double changePercent) {
        return changePercent >= 0
                ? context.getResources().getColor(R.color.positive_green, null)
                : context.getResources().getColor(R.color.negative_red, null);
    }

    /**
     * Gets the arrow symbol based on price change direction.
     *
     * @param changePercent Stock price change percentage
     * @return "↑" for positive change, "↓" for negative change
     */
    public static String getArrowSymbol(double changePercent) {
        return changePercent >= 0 ? "↑" : "↓";
    }

    /**
     * Formats the change percentage with arrow symbol.
     *
     * @param changePercent Stock price change percentage
     * @return Formatted string like "↑ +2.34%" or "↓ -1.23%"
     */
    public static String formatChangePercent(double changePercent) {
        String arrow = getArrowSymbol(changePercent);
        String sign = changePercent >= 0 ? "+" : "";
        return String.format("%s %s%.2f%%", arrow, sign, changePercent);
    }

    /**
     * Checks if the stock price change is positive.
     *
     * @param changePercent Stock price change percentage
     * @return true if change is positive or zero, false otherwise
     */
    public static boolean isPositiveChange(double changePercent) {
        return changePercent >= 0;
    }
}
