# Telnet 連線背景不斷電處理改進

## 改進摘要

本次更新大幅改善了 Bahamut BBS Android 應用程式的 telnet 連線穩定性，主要針對背景運行時的不斷電處理。

## 主要改進項目

### 1. ASDeviceController.java 改進
- ✅ **修正 WakeLock 管理邏輯**
  - 移除了 `lockWifi()` 方法中的時間限制 (60 分鐘)
  - 改為無限制的 `acquire()`，確保 telnet 連線不會因超時而中斷
  - 添加了獨立的 `lockCpuWake()` 和 `unlockCpuWake()` 方法
  - 改善了異常處理機制

- ✅ **增強生命週期管理**
  - 添加了 `cleanup()` 方法避免 memory leak
  - 添加了 `forceKeepConnection()` 方法用於重要的網路操作
  - 添加了狀態檢查方法 `isWifiLocked()` 和 `isCpuWakeLocked()`

### 2. TelnetConnector.java 改進
- ✅ **整合設備控制器**
  - 添加了 `ASDeviceController` 引用
  - 連線時自動鎖定 WiFi 和 CPU
  - 斷線時自動釋放鎖定

- ✅ **改善 HolderThread 背景執行緒**
  - 增強了中斷處理機制
  - 添加了網路連線狀態檢查
  - 改善了 keep-alive 檢查邏輯 (150秒發送，300秒健康檢查)
  - 添加了詳細的日誌記錄

- ✅ **添加連線健康檢查**
  - `isConnectionHealthy()` 方法檢查連線狀態
  - `checkNetworkConnectivity()` 方法檢查網路可用性
  - 改善了 `sendHoldMessage()` 的錯誤處理

- ✅ **移除廢棄方法**
  - 移除了已廢棄的 `finalize()` 方法
  - 添加了 `cleanup()` 方法作為替代

### 3. BahamutController.java 改進
- ✅ **整合設備控制器**
  - 在 `onControllerWillLoad()` 中設定 TelnetConnector 的設備控制器
  - 在 `onDestroy()` 中清理設備控制器引用

### 4. ASNavigationController.java 改進
- ✅ **改善生命週期管理**
  - 在 `finish()` 中調用 `cleanup()` 而非個別釋放方法
  - 改善了 `onPause()` 和 `onResume()` 的處理
  - 在背景運行時保持連線，前景恢復時檢查網路狀態

### 5. AndroidManifest.xml 權限更新
- ✅ **添加必要權限**
  - `ACCESS_WIFI_STATE` - 存取 WiFi 狀態
  - `CHANGE_WIFI_STATE` - 變更 WiFi 狀態
  - 保留現有的 `WAKE_LOCK` 權限

## 技術細節

### Keep-alive 機制
- 每 30 秒檢查一次連線狀態
- 超過 150 秒無資料傳送時發送 keep-alive 訊息
- 超過 300 秒時進行連線健康檢查

### WakeLock 策略
- **WiFi Lock**: 使用 `WIFI_MODE_FULL_LOW_LATENCY` (Android Q+) 或 `WIFI_MODE_FULL_HIGH_PERF`
- **CPU Wake Lock**: 使用 `PARTIAL_WAKE_LOCK` 無時間限制
- 自動管理：連線時鎖定，斷線時釋放

### 錯誤處理
- 連線失敗時自動釋放所有鎖定
- 網路狀態變化時的智能處理
- 詳細的日誌記錄便於調試

## 使用方式

### 在主要 Activity 中
```java
// 創建設備控制器
ASDeviceController deviceController = new ASDeviceController(this);

// 設定給 TelnetConnector（在 BahamutController 中已自動處理）
TelnetClient.getConnector().setDeviceController(deviceController);

// 在 Activity 銷毀時清理
@Override
protected void onDestroy() {
    super.onDestroy();
    if (deviceController != null) {
        deviceController.cleanup();
    }
}
```

### 手動控制連線保持
```java
// 強制保持連線（用於重要操作）
deviceController.forceKeepConnection();

// 檢查鎖定狀態
if (deviceController.isWifiLocked()) {
    // WiFi 已鎖定
}

if (deviceController.isCpuWakeLocked()) {
    // CPU 已鎖定
}
```

## 預期效果

1. **大幅減少背景斷線**：WiFi 和 CPU 鎖定確保系統不會主動斷開連線
2. **智能 keep-alive**：定期發送保持連線訊息防止伺服器超時
3. **網路狀態監控**：監控網路變化並適當應對
4. **資源管理**：自動管理系統資源，避免不必要的電池消耗
5. **穩定性提升**：減少因系統休眠或網路管理導致的連線中斷

## 注意事項

1. 這些改進會增加電池消耗，因為需要保持 WiFi 和 CPU 活躍
2. 建議在設定中提供選項讓使用者選擇是否啟用強力保持連線功能
3. 在低電量時可以考慮自動調整 keep-alive 頻率
4. 需要測試在不同 Android 版本和設備上的兼容性

## 測試建議

1. 測試長時間背景運行的穩定性
2. 測試網路切換時的重連機制
3. 測試電池消耗情況
4. 測試在不同系統版本的兼容性
5. 測試內存洩漏情況
