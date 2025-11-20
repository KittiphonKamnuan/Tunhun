package com.example.project.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repository for managing user's custom watchlist selections.
 * This is separated from StockRepository (which manages live WebSocket subscriptions)
 * so that we can clearly distinguish between curated sections (trending/popular)
 * and the symbols the user explicitly pins.
 */
public class WatchlistRepository {

    private static final String TAG = "WatchlistRepository";

    private static final String PREFS_NAME = "user_watchlist_prefs";
    private static final String KEY_SYMBOLS = "user_watchlist_symbols";

    // Legacy store (shared with StockRepository) used before refactor.
    private static final String LEGACY_PREFS_NAME = "stock_watchlist_prefs";
    private static final String LEGACY_KEY_SYMBOLS = "watchlist";

    private static WatchlistRepository instance;

    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final MutableLiveData<List<String>> watchlistLiveData;
    private final Context appContext;

    private WatchlistRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.watchlistLiveData = new MutableLiveData<>(new ArrayList<>());

        loadSymbols();
    }

    public static synchronized WatchlistRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WatchlistRepository(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<List<String>> getWatchlistSymbols() {
        return watchlistLiveData;
    }

    public boolean addSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }

        String upper = symbol.trim().toUpperCase();
        List<String> current = new ArrayList<>(getCurrentSymbols());

        if (current.contains(upper)) {
            return false;
        }

        current.add(upper);
        saveSymbols(current);
        watchlistLiveData.setValue(current);
        Log.d(TAG, "Added " + upper + " to user watchlist");
        return true;
    }

    public boolean removeSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }

        String upper = symbol.trim().toUpperCase();
        List<String> current = new ArrayList<>(getCurrentSymbols());

        boolean removed = current.remove(upper);
        if (removed) {
            saveSymbols(current);
            watchlistLiveData.setValue(current);
            Log.d(TAG, "Removed " + upper + " from user watchlist");
        }
        return removed;
    }

    public boolean isInWatchlist(String symbol) {
        if (symbol == null) return false;
        List<String> current = getCurrentSymbols();
        return current.contains(symbol.trim().toUpperCase());
    }

    private List<String> getCurrentSymbols() {
        List<String> symbols = watchlistLiveData.getValue();
        return symbols != null ? symbols : new ArrayList<>();
    }

    private void saveSymbols(List<String> symbols) {
        String json = gson.toJson(symbols);
        sharedPreferences.edit().putString(KEY_SYMBOLS, json).apply();
    }

    private void loadSymbols() {
        String json = sharedPreferences.getString(KEY_SYMBOLS, null);

        if (json != null) {
            try {
                Type type = new TypeToken<List<String>>() {}.getType();
                List<String> symbols = gson.fromJson(json, type);
                if (symbols != null) {
                    watchlistLiveData.setValue(new ArrayList<>(new HashSet<>(symbols)));
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse watchlist JSON", e);
            }
        }

        // No existing data in the new store, migrate legacy data if available.
        migrateLegacyWatchlist();
    }

    private void migrateLegacyWatchlist() {
        SharedPreferences legacyPrefs = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE);
        String legacyJson = legacyPrefs.getString(LEGACY_KEY_SYMBOLS, null);
        if (legacyJson == null) {
            watchlistLiveData.setValue(new ArrayList<>());
            return;
        }

        try {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> legacySymbols = gson.fromJson(legacyJson, type);
            if (legacySymbols == null) {
                watchlistLiveData.setValue(new ArrayList<>());
                return;
            }

            // Deduplicate (legacy list also stored trending/popular symbols)
            Set<String> uniqueSymbols = new HashSet<>();
            for (String symbol : legacySymbols) {
                if (symbol != null) {
                    uniqueSymbols.add(symbol.toUpperCase());
                }
            }

            List<String> cleanedSymbols = new ArrayList<>(uniqueSymbols);
            saveSymbols(cleanedSymbols);
            watchlistLiveData.setValue(cleanedSymbols);
            Log.d(TAG, "Migrated " + cleanedSymbols.size() + " symbols from legacy watchlist");
        } catch (Exception e) {
            Log.e(TAG, "Failed to migrate legacy watchlist", e);
            watchlistLiveData.setValue(new ArrayList<>());
        }
    }
}

