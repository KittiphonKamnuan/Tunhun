package com.example.project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final int REFRESH_INTERVAL_MS = 30000; // Fixed at 30 seconds

    private Switch soundSwitch;
    private Switch notificationSwitch;
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
        soundSwitch = findViewById(R.id.switch_sound);
        notificationSwitch = findViewById(R.id.switch_notifications);
        saveButton = findViewById(R.id.button_save);
        resetButton = findViewById(R.id.button_reset);
    }

    private void loadSettings() {
        soundSwitch.setChecked(prefs.getBoolean(KEY_SOUND_ENABLED, false));
        notificationSwitch.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS, true));
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveSettings());
        resetButton.setOnClickListener(v -> resetSettings());
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_SOUND_ENABLED, soundSwitch.isChecked());
        editor.putBoolean(KEY_NOTIFICATIONS, notificationSwitch.isChecked());
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

    // Fixed refresh interval at 30 seconds
    public static int getRefreshInterval() {
        return REFRESH_INTERVAL_MS;
    }
}
