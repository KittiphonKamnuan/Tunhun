package com.example.project.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project.MainActivity;
import com.example.project.R;
import com.example.project.SettingsActivity; // ถ้าคุณมีปุ่มไปหน้า Settings
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. ปุ่มเปลี่ยนภาษา (Language)
        MaterialCardView cardLanguage = view.findViewById(R.id.card_language);
        cardLanguage.setOnClickListener(v -> showChangeLanguageDialog());

        // 2. ปุ่มตั้งค่า (Settings)
        MaterialCardView cardSettings = view.findViewById(R.id.card_settings);
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        // 3. ปุ่ม Logout (ตัวอย่าง)
        MaterialCardView cardLogout = view.findViewById(R.id.card_logout);
        cardLogout.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
            // ใส่โค้ด Logout จริงๆ ตรงนี้
        });
    }

    private void showChangeLanguageDialog() {
        // รายชื่อภาษาที่จะแสดงให้เลือก
        final String[] listItems = {"English", "ไทย"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Language / เลือกภาษา");
        builder.setSingleChoiceItems(listItems, -1, (dialogInterface, i) -> {
            if (i == 0) {
                // เลือก English
                setLocale("en");
                getActivity().recreate(); // รีสตาร์ทหน้าเพื่อให้ภาษาเปลี่ยน
            } else if (i == 1) {
                // เลือก ไทย
                setLocale("th");
                getActivity().recreate(); // รีสตาร์ทหน้าเพื่อให้ภาษาเปลี่ยน
            }
            dialogInterface.dismiss();
        });

        AlertDialog mDialog = builder.create();
        mDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        if (getActivity() != null) {
            getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());

            // บันทึกภาษาลงเครื่อง
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();
            editor.putString("My_Lang", lang);
            editor.apply();

            // สั่ง Restart แอปไปหน้า Main เพื่อให้โหลดภาษาใหม่ทั้งแอป
            Intent i = new Intent(getActivity(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // ล้าง Activity เก่าออก
            startActivity(i);
        }
    }
}