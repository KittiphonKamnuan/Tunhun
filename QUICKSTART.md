# 🚀 Quick Start Guide - ทันหุ้น

## ✅ Ready to Run!

The app is now configured with **MOCK DATA** - you can run it immediately without any setup!

## 📱 How to Run

### Option 1: Android Studio
1. Open Android Studio
2. Click "Run" button (▶️) or press Shift+F10
3. Select your device/emulator
4. Wait for the app to install and launch

### Option 2: Command Line
```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## 🎯 Using the App

### 1. Add Stocks
- Type a stock symbol in the input field (e.g., AAPL, MSFT, TSLA)
- Click "ADD" button or press Enter
- The stock will appear in your watchlist with real-time price updates

### 2. Mock Stock Symbols Available
The following stocks have mock data pre-configured:
- **AAPL** - Apple Inc.
- **MSFT** - Microsoft
- **GOOGL** - Google
- **AMZN** - Amazon
- **TSLA** - Tesla
- **META** - Meta (Facebook)
- **NVDA** - NVIDIA
- **NFLX** - Netflix
- **DIS** - Disney
- **BA** - Boeing

### 3. Watch Live Updates
- Prices update automatically every 1-3 seconds
- Green badge = price going up ↑
- Red badge = price going down ↓
- Connection status shown at top right

### 4. Remove Stocks
- Click the 🗑️ (trash) button on any stock to remove it
- Your watchlist is saved automatically

## 🎨 UI Features

### Modern Design
- ✅ Clean, minimalist interface
- ✅ Beautiful color scheme (Blue theme)
- ✅ Smooth card-based layout
- ✅ Status chips with color indicators
- ✅ Empty state with emoji
- ✅ Real-time connection status

### User Experience
- ✅ Instant feedback on actions
- ✅ Toast notifications
- ✅ Keyboard "Done" button support
- ✅ Auto-save watchlist
- ✅ Persistent data across sessions
- ✅ Proper lifecycle management

## 🔧 Switch to Real Data

Want to use real Finnhub API instead of mock data?

1. Get a free API key from [Finnhub.io](https://finnhub.io/)

2. Open `app/src/main/java/com/example/project/repository/StockRepository.java`

3. Change line 34:
   ```java
   // Change from:
   private static final boolean USE_MOCK_DATA = true;

   // To:
   private static final boolean USE_MOCK_DATA = false;
   ```

4. Open `app/src/main/java/com/example/project/service/FinnhubWebSocketClient.java`

5. Update line 14 with your API key:
   ```java
   private static final String API_KEY = "your_api_key_here";
   ```

6. Rebuild and run the app

## 📊 Mock Data Behavior

The mock data simulates real market behavior:
- Random price fluctuations (±0.5% per update)
- Updates every 1-3 seconds
- Prices stay within ±10% of base price
- Realistic starting prices for popular stocks
- Change percentages calculated from base price

## 🎓 Project Structure

```
app/src/main/java/com/example/project/
├── MainActivity.java              # Main screen
├── adapter/
│   └── StockAdapter.java         # RecyclerView adapter
├── model/
│   ├── Stock.java                # Stock data model
│   └── TradeMessage.java         # API message model
├── mock/
│   └── MockStockDataProvider.java # Mock data generator
├── repository/
│   └── StockRepository.java      # Data management
├── service/
│   └── FinnhubWebSocketClient.java # Real API client
└── viewmodel/
    └── StockViewModel.java       # ViewModel (MVVM)
```

## 🐛 Troubleshooting

### App won't build
- Make sure you have JDK 11 or higher
- Run `./gradlew clean` then try again

### Prices not updating
- Check if "Connected" is shown (green badge)
- Try removing and re-adding the stock

### Empty list after restart
- This is expected - mock data doesn't fetch from a real API
- Add stocks again to see them

## 📝 Features

- ✅ Mock data (no API key needed)
- ✅ Real-time price updates simulation
- ✅ Add/Remove stocks
- ✅ Persistent watchlist (SharedPreferences)
- ✅ Beautiful modern UI
- ✅ Status indicators
- ✅ Percentage change tracking
- ✅ Smooth animations
- ✅ Responsive design
- ✅ Proper lifecycle handling

## 🎉 You're Ready!

Just run the app and start adding stocks to your watchlist. The mock data will start updating automatically!

---

**Created for educational purposes**
**Course**: [Your Course Name]
**Student**: [Your Name]
