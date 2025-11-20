package com.example.project.util;

import com.example.project.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * จัดการ API keys หลายตัวแบบหมุนเวียน (Round-robin)
 * เพื่อกระจาย API calls และหลีกเลี่ยง rate limit
 */
public class ApiKeyManager {
    private static ApiKeyManager instance;
    private final List<String> apiKeys;
    private final AtomicInteger currentIndex;

    private ApiKeyManager() {
        this.apiKeys = new ArrayList<>();
        this.currentIndex = new AtomicInteger(0);
        initApiKeys();
    }

    public static synchronized ApiKeyManager getInstance() {
        if (instance == null) {
            instance = new ApiKeyManager();
        }
        return instance;
    }

    /**
     * โหลด API keys จาก BuildConfig
     */
    private void initApiKeys() {
        // เพิ่ม API keys ทั้งหมดที่มี
        addApiKeyIfValid(BuildConfig.FINNHUB_API_KEY);
        addApiKeyIfValid(BuildConfig.FINNHUB_API_KEY_2);
        addApiKeyIfValid(BuildConfig.FINNHUB_API_KEY_3);
        addApiKeyIfValid(BuildConfig.FINNHUB_API_KEY_4);
    }

    private void addApiKeyIfValid(String apiKey) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            apiKeys.add(apiKey);
        }
    }

    /**
     * ดึง API key ตัวถัดไปแบบหมุนเวียน
     * Request 1: key[0], Request 2: key[1], Request 3: key[2], Request 4: key[3], Request 5: key[0]...
     */
    public String getNextApiKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("ไม่มี API keys! ตรวจสอบ local.properties");
        }

        int index = currentIndex.getAndIncrement() % apiKeys.size();
        return apiKeys.get(index);
    }

    /**
     * จำนวน API keys ที่มี
     */
    public int getKeyCount() {
        return apiKeys.size();
    }
}
