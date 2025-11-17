package com.example.project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.adapter.StockAdapter;
import com.example.project.viewmodel.StockViewModel;

public class WatchlistFragment extends Fragment {

    private StockViewModel viewModel;
    private StockAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        observeData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_watchlist);
        emptyStateView = view.findViewById(R.id.empty_state);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StockViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new StockAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Handle removal
        adapter.setOnStockRemoveListener(stock -> {
            viewModel.removeStock(stock.getSymbol());
            Toast.makeText(getContext(), "Removed " + stock.getSymbol(), Toast.LENGTH_SHORT).show();
        });
    }

    private void observeData() {
        viewModel.getStockList().observe(getViewLifecycleOwner(), stocks -> {
            adapter.setStockList(stocks);

            // Show/hide empty state
            if (stocks == null || stocks.isEmpty()) {
                emptyStateView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Connect when fragment is visible
        if (!viewModel.isConnected()) {
            viewModel.connect();
        }
    }
}
