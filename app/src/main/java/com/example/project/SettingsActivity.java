package com.example.project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_AUTO_REFRESH = "auto_refresh";
    private static final String KEY_REFRESH_INTERVAL = "refresh_interval";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private Switch autoRefreshSwitch;
    private Switch soundSwitch;
    private Switch notificationSwitch;
    private Spinner refreshIntervalSpinner;
    private MaterialButton saveButton;
    private MaterialButton resetButton;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupToolbar();
        initViews();
        loadSettings();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }

    private void initViews() {
        autoRefreshSwitch = findViewById(R.id.switch_auto_refresh);
        soundSwitch = findViewById(R.id.switch_sound);
        notificationSwitch = findViewById(R.id.switch_notifications);
        refreshIntervalSpinner = findViewById(R.id.spinner_refresh_interval);
        saveButton = findViewById(R.id.button_save);
        resetButton = findViewById(R.id.button_reset);

        // Setup spinner with intervals
        String[] intervals = {"1 second", "2 seconds", "3 seconds", "5 seconds", "10 seconds"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            intervals
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refreshIntervalSpinner.setAdapter(adapter);
    }

    private void loadSettings() {
        autoRefreshSwitch.setChecked(prefs.getBoolean(KEY_AUTO_REFRESH, true));
        soundSwitch.setChecked(prefs.getBoolean(KEY_SOUND_ENABLED, false));
        notificationSwitch.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS, true));

        int intervalIndex = prefs.getInt(KEY_REFRESH_INTERVAL, 1); // Default: 2 seconds
        refreshIntervalSpinner.setSelection(intervalIndex);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveSettings());
        resetButton.setOnClickListener(v -> resetSettings());
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_AUTO_REFRESH, autoRefreshSwitch.isChecked());
        editor.putBoolean(KEY_SOUND_ENABLED, soundSwitch.isChecked());
        editor.putBoolean(KEY_NOTIFICATIONS, notificationSwitch.isChecked());
        editor.putInt(KEY_REFRESH_INTERVAL, refreshIntervalSpinner.getSelectedItemPosition());
        editor.apply();

        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void resetSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        loadSettings();
        Toast.makeText(this, "Settings reset to default", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper method to get refresh interval in milliseconds
    public static int getRefreshInterval(SharedPreferences prefs) {
        int index = prefs.getInt(KEY_REFRESH_INTERVAL, 1);
        int[] intervals = {1000, 2000, 3000, 5000, 10000};
        return intervals[Math.min(index, intervals.length - 1)];
    }

    public static boolean isAutoRefreshEnabled(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_AUTO_REFRESH, true);
    }
}
