package com.example.project.util;

import android.content.Context;

import com.example.project.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

/**
 * Helper class for configuring and updating LineChart for stock price display.
 * Provides reusable methods for chart styling and data management.
 */
public class ChartHelper {

    // Chart styling constants
    private static final float LINE_WIDTH = 2.5f;
    private static final int FILL_ALPHA = 30;
    private static final float X_AXIS_GRANULARITY = 1f;

    /**
     * Configures a LineChart with default stock chart styling.
     *
     * @param chart   The LineChart to configure
     * @param context Android context for accessing resources
     */
    public static void configureChart(LineChart chart, Context context) {
        // General chart settings
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        // Configure X-axis
        configureXAxis(chart, context);

        // Configure Y-axes
        configureYAxes(chart, context);
    }

    /**
     * Configures the X-axis of the chart.
     *
     * @param chart   The LineChart containing the X-axis
     * @param context Android context for accessing resources
     */
    private static void configureXAxis(LineChart chart, Context context) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
        xAxis.setGranularity(X_AXIS_GRANULARITY);
    }

    /**
     * Configures the Y-axes of the chart.
     *
     * @param chart   The LineChart containing the Y-axes
     * @param context Android context for accessing resources
     */
    private static void configureYAxes(LineChart chart, Context context) {
        // Left Y-axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(context.getResources().getColor(R.color.divider, null));
        leftAxis.setTextColor(context.getResources().getColor(R.color.text_secondary, null));

        // Right Y-axis (disabled)
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    /**
     * Creates a styled LineDataSet for stock price data.
     *
     * @param entries       List of data entries
     * @param label         Dataset label
     * @param color         Line and fill color
     * @param context       Android context
     * @return Configured LineDataSet
     */
    public static LineDataSet createStockDataSet(
            List<Entry> entries,
            String label,
            int color,
            Context context) {

        LineDataSet dataSet = new LineDataSet(entries, label);

        // Line styling
        dataSet.setColor(color);
        dataSet.setLineWidth(LINE_WIDTH);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Fill styling
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(FILL_ALPHA);

        return dataSet;
    }

    /**
     * Updates the chart with new data.
     * This method handles data set creation and chart refresh.
     *
     * @param chart         The LineChart to update
     * @param entries       New data entries
     * @param changePercent Stock change percentage (for color selection)
     * @param context       Android context
     */
    public static void updateChartData(
            LineChart chart,
            List<Entry> entries,
            double changePercent,
            Context context) {

        // Get appropriate color based on price change
        int color = StockColorHelper.getStockColor(context, changePercent);

        // Create dataset with styling
        LineDataSet dataSet = createStockDataSet(entries, "Price History", color, context);

        // Set data and refresh chart
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh the chart
    }

    /**
     * Clears all data from the chart.
     *
     * @param chart The LineChart to clear
     */
    public static void clearChart(LineChart chart) {
        chart.clear();
        chart.invalidate();
    }

    /**
     * Animates the chart with default animation settings.
     *
     * @param chart The LineChart to animate
     */
    public static void animateChart(LineChart chart) {
        chart.animateX(500); // 500ms animation
    }

    /**
     * Configures chart with custom settings.
     *
     * @param chart           The LineChart to configure
     * @param context         Android context
     * @param enableTouch     Enable touch interactions
     * @param enableDrag      Enable drag
     * @param enableScale     Enable scale/zoom
     */
    public static void configureChartCustom(
            LineChart chart,
            Context context,
            boolean enableTouch,
            boolean enableDrag,
            boolean enableScale) {

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(enableTouch);
        chart.setDragEnabled(enableDrag);
        chart.setScaleEnabled(enableScale);
        chart.setPinchZoom(enableScale);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        configureXAxis(chart, context);
        configureYAxes(chart, context);
    }

    /**
     * Configures a mini sparkline chart for dashboard cards.
     * Minimal styling with no axes, labels, or grid lines.
     *
     * @param chart   The LineChart to configure as sparkline
     */
    public static void configureMiniChart(LineChart chart) {
        // Disable all interactions
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        // Disable all visual elements except the line
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);

        // Disable axes
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        // Remove borders
        chart.setDrawBorders(false);

        // Set minimal view offsets
        chart.setViewPortOffsets(0f, 0f, 0f, 0f);
    }

    /**
     * Creates a minimal sparkline dataset.
     *
     * @param entries List of data entries
     * @param color   Line color
     * @return Configured LineDataSet for sparkline
     */
    public static LineDataSet createSparklineDataSet(List<Entry> entries, int color) {
        LineDataSet dataSet = new LineDataSet(entries, "");

        // Line styling
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // No fill for sparkline (cleaner look)
        dataSet.setDrawFilled(false);

        return dataSet;
    }

    /**
     * Updates mini sparkline chart with data.
     *
     * @param chart         The mini LineChart to update
     * @param entries       Data entries
     * @param changePercent Stock change percentage (for color)
     * @param context       Android context
     */
    public static void updateMiniChart(
            LineChart chart,
            List<Entry> entries,
            double changePercent,
            Context context) {

        // Get appropriate color based on price change
        int color = StockColorHelper.getStockColor(context, changePercent);

        // Create sparkline dataset
        LineDataSet dataSet = createSparklineDataSet(entries, color);

        // Set data and refresh
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }
}
