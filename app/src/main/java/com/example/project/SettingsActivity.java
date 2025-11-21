package com.example.project;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    // Track initial settings
    private boolean initialSoundEnabled;
    private boolean initialNotificationsEnabled;

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
            getSupportActionBar().setTitle(R.string.menu_settings);
        }
    }

    private void initViews() {
        soundSwitch = findViewById(R.id.switch_sound);
        notificationSwitch = findViewById(R.id.switch_notifications);
        saveButton = findViewById(R.id.button_save);
        resetButton = findViewById(R.id.button_reset);
    }

    private void loadSettings() {
        initialSoundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, false);
        initialNotificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);

        soundSwitch.setChecked(initialSoundEnabled);
        notificationSwitch.setChecked(initialNotificationsEnabled);
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

        Toast.makeText(this, R.string.toast_settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void resetSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        loadSettings();
        Toast.makeText(this, R.string.toast_settings_reset, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    private void handleBackPressed() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        boolean currentSound = soundSwitch.isChecked();
        boolean currentNotifications = notificationSwitch.isChecked();

        return currentSound != initialSoundEnabled ||
               currentNotifications != initialNotificationsEnabled;
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_unsaved_title)
                .setMessage(R.string.dialog_unsaved_message)
                .setPositiveButton(R.string.dialog_save, (dialog, which) -> saveSettings())
                .setNegativeButton(R.string.dialog_discard, (dialog, which) -> finish())
                .setNeutralButton(R.string.dialog_cancel, null)
                .show();
    }

    // Fixed refresh interval at 30 seconds
    public static int getRefreshInterval() {
        return REFRESH_INTERVAL_MS;
    }
}
