package com.example.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Fragment for user profile and settings
 */
public class ProfileFragment extends Fragment {

    private TextView profileName;
    private TextView profileEmail;
    private MaterialCardView cardSettings;
    private MaterialCardView cardAbout;
    private MaterialCardView cardLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupProfile();
        setupClickListeners();
    }

    private void initViews(View view) {
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        cardSettings = view.findViewById(R.id.card_settings);
        cardAbout = view.findViewById(R.id.card_about);
        cardLogout = view.findViewById(R.id.card_logout);
    }

    private void setupProfile() {
        // Set default profile info
        profileName.setText("นักลงทุน");
        profileEmail.setText("investor@tunhun.com");
    }

    private void setupClickListeners() {
        cardSettings.setOnClickListener(v -> {
            // Navigate to settings
            android.widget.Toast.makeText(getContext(), "ฟีเจอร์การตั้งค่าจะมาเร็วๆ นี้", android.widget.Toast.LENGTH_SHORT).show();
        });

        cardAbout.setOnClickListener(v -> {
            // Show about info
            android.widget.Toast.makeText(getContext(), "ทันหุ้น v1.0 - Stock Tracking App", android.widget.Toast.LENGTH_SHORT).show();
        });

        cardLogout.setOnClickListener(v -> {
            // Logout action
            android.widget.Toast.makeText(getContext(), "ฟีเจอร์ออกจากระบบจะมาเร็วๆ นี้", android.widget.Toast.LENGTH_SHORT).show();
        });
    }
}
