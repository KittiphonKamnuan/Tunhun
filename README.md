# ทันหุ้น (Stock Tracker)

A minimalist, real-time stock tracking Android application built with Java and MVVM architecture.

## Features

- **Real-time Stock Updates**: Live price updates using WebSocket connection to Finnhub API
- **Minimalist Design**: Clean, simple interface focused on essential information
- **Watchlist Management**: Add and remove stocks to your personal watchlist
- **Persistent Storage**: Watchlist is saved locally using SharedPreferences
- **Activity Lifecycle Management**: Automatically connects/disconnects WebSocket based on app state
- **US Stock Market Support**: Track stocks from US exchanges (NASDAQ, NYSE, etc.)

## Architecture

The app follows the **MVVM (Model-View-ViewModel)** architecture pattern:

- **Model**: `Stock`, `TradeMessage` - Data models
- **View**: `MainActivity`, XML layouts - UI layer
- **ViewModel**: `StockViewModel` - Business logic and data management
- **Repository**: `StockRepository` - Data source management
- **Service**: `FinnhubWebSocketClient` - WebSocket connection handling

## Tech Stack

- **Language**: Java 11
- **Platform**: Android (Min SDK 26)
- **Libraries**:
  - OkHttp 4.12.0 - WebSocket connection
  - Gson 2.10.1 - JSON parsing
  - AndroidX Lifecycle 2.8.7 - ViewModel and LiveData
  - RecyclerView 1.3.2 - List display
  - CardView 1.0.0 - UI components

## Setup Instructions

### 1. Get a Finnhub API Key

1. Visit [Finnhub.io](https://finnhub.io/)
2. Sign up for a free account
3. Get your API key from the dashboard

### 2. Add Your API Key

Open `app/src/main/java/com/example/project/service/FinnhubWebSocketClient.java` and replace the placeholder with your actual API key:

```java
// Replace this line:
private static final String API_KEY = "YOUR_API_KEY_HERE";

// With your actual key:
private static final String API_KEY = "your_actual_api_key_here";
```

### 3. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Connect an Android device or start an emulator (Android 8.0 or higher)
4. Click "Run" or use `./gradlew assembleDebug`

## Usage

1. **Launch the app** - The app will automatically connect to Finnhub WebSocket
2. **Add stocks** - Enter a stock symbol (e.g., AAPL, MSFT, GOOGL) and click "Add"
3. **View live updates** - Stock prices will update in real-time
4. **Remove stocks** - Click the delete button on any stock to remove it
5. **Watchlist persists** - Your watchlist is saved automatically

## Project Structure

```
app/src/main/java/com/example/project/
├── MainActivity.java                 # Main UI activity
├── adapter/
│   └── StockAdapter.java            # RecyclerView adapter
├── model/
│   ├── Stock.java                   # Stock data model
│   └── TradeMessage.java            # WebSocket message model
├── repository/
│   └── StockRepository.java         # Data management
├── service/
│   └── FinnhubWebSocketClient.java  # WebSocket client
└── viewmodel/
    └── StockViewModel.java          # ViewModel
```

## Key Implementation Details

### WebSocket Connection

The app uses OkHttp's WebSocket to connect to Finnhub:
- Connects when the activity resumes
- Disconnects when the activity pauses
- Automatically subscribes to all watchlist stocks
- Handles connection status changes

### Data Persistence

Stock symbols are saved to SharedPreferences as JSON:
- Saved automatically when stocks are added/removed
- Loaded on app startup
- No network required to view saved watchlist

### Lifecycle Management

The app properly handles the Android lifecycle:
- `onResume()`: Connects to WebSocket
- `onPause()`: Disconnects to save battery and resources
- `onCleared()`: Cleans up resources in ViewModel

## Limitations

- US stock market only
- No historical charts
- No price alerts
- No user authentication
- Finnhub free tier has rate limits

## Future Enhancements

- Add stock search with autocomplete
- Display market status (open/closed)
- Show more detailed stock information
- Add pull-to-refresh
- Implement price change notifications
- Support for other markets (Thai stocks, crypto, etc.)

## License

This project was created for educational purposes as part of a university project.

## Credits

- Stock data provided by [Finnhub.io](https://finnhub.io/)
- Built with Android Studio
- Icons from Android Material Design

---

**Note**: This app requires an active internet connection and a valid Finnhub API key to function properly.
