# Stock Tracker - Data Source Configuration

## Current Mode: ✅ REAL DATA (Finnhub WebSocket API)

The application is currently configured to use **real-time stock data** from Finnhub API.

## Configuration Location

**File:** `app/src/main/java/com/example/project/repository/StockRepository.java`
**Line:** 34

```java
private static final boolean USE_MOCK_DATA = false;  // Real Data Mode
```

## Real Data Features

### ✅ Active Features (Real Data Mode)
- Real-time WebSocket connection to Finnhub API
- Live stock price updates from actual market data
- Rate limiting (30 calls/second)
- Subscription queue management
- HTTP 429 error handling
- Opening price tracking for % calculation

### Data Flow
1. User adds stock symbol (e.g., "AAPL")
2. App subscribes to Finnhub WebSocket for that symbol
3. First trade received → Sets opening price
4. All subsequent trades → Calculate % from opening price
5. UI updates in real-time with live data

### API Configuration
- **API Key:** Stored in `local.properties` (FINNHUB_API_KEY)
- **WebSocket URL:** `wss://ws.finnhub.io`
- **Rate Limit:** 30 requests/second (enforced by FinnhubRateLimiter)

## Mock Data Mode (Currently Disabled)

If you want to switch to Mock Data for testing without API:

### How to Enable Mock Data
Change line 34 in `StockRepository.java`:
```java
private static final boolean USE_MOCK_DATA = true;  // Mock Data Mode
```

### Mock Data Features
- Simulated stock price updates
- No internet/API key required
- 10 predefined stocks (AAPL, MSFT, GOOGL, AMZN, TSLA, META, NVDA, NFLX, DIS, BA)
- Random price fluctuations (±0.5% per update)
- Updates every 1-3 seconds

## Verification

### Check if Real Data is Working

**Logcat Tags to Monitor:**
```
D/StockRepository: [symbol] opening price set to: [price]
D/FinnhubRateLimiter: Request approved. Current: X/30 calls in last second
D/FinnhubWebSocket: Subscribed to: [symbol]
```

**Connection Status:**
- Look for "WebSocket connected successfully" in logs
- Dashboard should show "Connected" status indicator

### Troubleshooting Real Data

**Problem:** Not receiving updates
- **Check:** API key in `local.properties` is correct
- **Check:** Internet connection is active
- **Check:** Finnhub API key is valid (test at https://finnhub.io/dashboard)

**Problem:** 429 Rate Limit Error
- **Solution:** Rate limiter will auto-recover
- **Check:** Don't add too many stocks at once (respects 30/second limit)

**Problem:** Opening price not set
- **Solution:** Wait for first trade to arrive (may take a few seconds)
- **Check:** Symbol is valid and market is open (or pre/post-market trading)

## Build and Deploy

After changing USE_MOCK_DATA, rebuild the app:
```bash
./gradlew assembleDebug
```

## Current Implementation Status

| Feature | Status | Description |
|---------|--------|-------------|
| Real-time Data | ✅ Active | Finnhub WebSocket API |
| Rate Limiting | ✅ Active | 30 calls/second enforced |
| Opening Price | ✅ Active | % calculated from market open |
| HTTP 429 Handling | ✅ Active | Auto-recovery on rate limit |
| Subscription Queue | ✅ Active | Throttled request processing |
| Mock Data | ⏸️ Disabled | Available for testing |

---

**Last Updated:** 2025-11-14
**Mode:** Real Data (Finnhub API)
