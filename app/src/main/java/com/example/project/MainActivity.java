package com.example.project;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.project.fragment.DashboardFragment;
import com.example.project.fragment.WatchlistFragment;
import com.example.project.fragment.SearchFragment;
import com.example.project.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Set default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        // Setup bottom navigation listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.navigation_portfolio) {
                fragment = new WatchlistFragment();
            } else if (itemId == R.id.navigation_search) {
                fragment = new SearchFragment();
            } else if (itemId == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Navigate to Watchlist tab (Portfolio/Watchlist)
     */
    public void navigateToWatchlist() {
        if (bottomNavigation != null) {
            // เปลี่ยนเป็น id ของแท็บ Watchlist ของคุณ
            bottomNavigation.setSelectedItemId(R.id.navigation_portfolio);
        }
    }
}
