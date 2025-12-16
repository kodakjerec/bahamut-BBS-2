# asFramework/network - 網路狀態管理

**applyto**: `app/src/main/java/com/kota/asFramework/network/**/*.kt`

## 📋 模組概述

network 模組提供網路狀態監控功能，用於追蹤系統網路連接狀態變化。透過 Android BroadcastReceiver 機制，即時偵測網路連線/斷線、網路類型切換等事件，讓應用程式能根據網路狀態做出適當反應。

**技術棧**: Kotlin, Android BroadcastReceiver, ConnectivityManager  
**設計模式**: 觀察者模式 (Observer Pattern)  
**命名前綴**: AS (Application Structure)

---

## 📂 檔案結構

### `ASNetworkStateChangeReceiver.kt` - 網路狀態變更接收器

系統級網路狀態監聽器，接收並處理網路狀態變更廣播。

#### 類別定義
```kotlin
class ASNetworkStateChangeReceiver : BroadcastReceiver() {
    
    interface NetworkStateChangeListener {
        /**
         * 當網路狀態改變時觸發
         * @param isConnected 是否已連接
         * @param networkType 網路類型
         */
        fun onNetworkStateChanged(isConnected: Boolean, networkType: NetworkType)
    }
    
    enum class NetworkType {
        WIFI,           // WiFi 連接
        MOBILE,         // 行動網路
        ETHERNET,       // 乙太網路
        NONE            // 無連接
    }
    
    private val listeners = mutableSetOf<NetworkStateChangeListener>()
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as ConnectivityManager
            
            val networkInfo = connectivityManager.activeNetworkInfo
            val isConnected = networkInfo?.isConnected ?: false
            val networkType = determineNetworkType(networkInfo)
            
            notifyListeners(isConnected, networkType)
        }
    }
    
    private fun determineNetworkType(networkInfo: NetworkInfo?): NetworkType {
        return when (networkInfo?.type) {
            ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
            ConnectivityManager.TYPE_MOBILE -> NetworkType.MOBILE
            ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
            else -> NetworkType.NONE
        }
    }
    
    private fun notifyListeners(isConnected: Boolean, networkType: NetworkType) {
        listeners.forEach { listener ->
            listener.onNetworkStateChanged(isConnected, networkType)
        }
    }
    
    fun addListener(listener: NetworkStateChangeListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: NetworkStateChangeListener) {
        listeners.remove(listener)
    }
    
    companion object {
        private var instance: ASNetworkStateChangeReceiver? = null
        
        fun getInstance(): ASNetworkStateChangeReceiver {
            if (instance == null) {
                instance = ASNetworkStateChangeReceiver()
            }
            return instance!!
        }
        
        /**
         * 檢查當前網路連接狀態
         */
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected ?: false
        }
        
        /**
         * 獲取當前網路類型
         */
        fun getCurrentNetworkType(context: Context): NetworkType {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return getInstance().determineNetworkType(networkInfo)
        }
    }
}
```

---

## 🎯 使用指南

### 註冊網路狀態監聽器

#### 在 AndroidManifest.xml 中宣告權限
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

#### 動態註冊接收器
```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var networkReceiver: ASNetworkStateChangeReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 獲取接收器實例
        networkReceiver = ASNetworkStateChangeReceiver.getInstance()
        
        // 註冊接收器
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
        
        // 添加監聽器
        networkReceiver.addListener(object : 
            ASNetworkStateChangeReceiver.NetworkStateChangeListener {
            
            override fun onNetworkStateChanged(
                isConnected: Boolean, 
                networkType: ASNetworkStateChangeReceiver.NetworkType
            ) {
                handleNetworkChange(isConnected, networkType)
            }
        })
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 移除監聽器
        networkReceiver.removeListener(networkListener)
        // 取消註冊接收器
        unregisterReceiver(networkReceiver)
    }
    
    private fun handleNetworkChange(
        isConnected: Boolean, 
        networkType: ASNetworkStateChangeReceiver.NetworkType
    ) {
        when {
            !isConnected -> {
                showNoNetworkWarning()
            }
            networkType == ASNetworkStateChangeReceiver.NetworkType.MOBILE -> {
                showMobileDataWarning()
            }
            networkType == ASNetworkStateChangeReceiver.NetworkType.WIFI -> {
                resumeBackgroundTasks()
            }
        }
    }
}
```

### 檢查網路狀態

#### 基本檢查
```kotlin
class NetworkUtils {
    companion object {
        fun checkNetworkAndExecute(
            context: Context, 
            onSuccess: () -> Unit,
            onFailure: () -> Unit
        ) {
            if (ASNetworkStateChangeReceiver.isNetworkAvailable(context)) {
                onSuccess()
            } else {
                onFailure()
                showNoNetworkDialog(context)
            }
        }
    }
}

// 使用
NetworkUtils.checkNetworkAndExecute(
    context = this,
    onSuccess = { loadDataFromServer() },
    onFailure = { loadCachedData() }
)
```

#### 根據網路類型調整行為
```kotlin
class DataSyncManager(private val context: Context) {
    
    fun startSync() {
        val networkType = ASNetworkStateChangeReceiver.getCurrentNetworkType(context)
        
        when (networkType) {
            ASNetworkStateChangeReceiver.NetworkType.WIFI -> {
                // WiFi 下載高品質圖片
                downloadHighQualityImages()
                syncLargeFiles()
            }
            ASNetworkStateChangeReceiver.NetworkType.MOBILE -> {
                // 行動網路只下載低品質
                downloadLowQualityImages()
                skipLargeFiles()
            }
            ASNetworkStateChangeReceiver.NetworkType.NONE -> {
                // 無網路，使用快取
                loadFromCache()
            }
            else -> {
                // 其他網路類型
                downloadMediumQualityImages()
            }
        }
    }
}
```

---

## 🔧 實際應用場景

### 1. Telnet 連接管理

```kotlin
class TelnetConnectionManager : ASNetworkStateChangeReceiver.NetworkStateChangeListener {
    
    private var isConnected = false
    private var reconnectJob: Job? = null
    
    override fun onNetworkStateChanged(
        isConnected: Boolean, 
        networkType: ASNetworkStateChangeReceiver.NetworkType
    ) {
        if (isConnected && !this.isConnected) {
            // 網路恢復，嘗試重連
            attemptReconnect()
        } else if (!isConnected && this.isConnected) {
            // 網路斷線，顯示提示
            showDisconnectionWarning()
        }
        
        this.isConnected = isConnected
    }
    
    private fun attemptReconnect() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(2000) // 等待網路穩定
            
            if (ASNetworkStateChangeReceiver.isNetworkAvailable(context)) {
                TelnetClient.reconnect()
            }
        }
    }
    
    private fun showDisconnectionWarning() {
        object : ASRunner() {
            override fun run() {
                ASSnackBar.show(
                    message = "網路連線已中斷",
                    duration = ASSnackBar.LENGTH_LONG
                )
            }
        }.runInMainThread()
    }
}
```

### 2. 圖片載入策略

```kotlin
class ImageLoader(private val context: Context) {
    
    fun loadImage(url: String, imageView: ImageView) {
        val networkType = ASNetworkStateChangeReceiver.getCurrentNetworkType(context)
        
        val quality = when (networkType) {
            ASNetworkStateChangeReceiver.NetworkType.WIFI -> ImageQuality.HIGH
            ASNetworkStateChangeReceiver.NetworkType.MOBILE -> {
                if (UserSettings.allowMobileDataDownload) {
                    ImageQuality.MEDIUM
                } else {
                    ImageQuality.LOW
                }
            }
            else -> ImageQuality.LOW
        }
        
        loadImageWithQuality(url, imageView, quality)
    }
    
    enum class ImageQuality {
        LOW, MEDIUM, HIGH
    }
}
```

### 3. 自動重試機制

```kotlin
class NetworkRetryHelper : ASNetworkStateChangeReceiver.NetworkStateChangeListener {
    
    private val pendingTasks = mutableListOf<() -> Unit>()
    
    fun executeWithRetry(task: () -> Unit) {
        if (ASNetworkStateChangeReceiver.isNetworkAvailable(context)) {
            try {
                task()
            } catch (e: IOException) {
                // 網路錯誤，加入待重試列表
                pendingTasks.add(task)
            }
        } else {
            // 無網路，加入待重試列表
            pendingTasks.add(task)
            showOfflineMessage()
        }
    }
    
    override fun onNetworkStateChanged(
        isConnected: Boolean, 
        networkType: ASNetworkStateChangeReceiver.NetworkType
    ) {
        if (isConnected && pendingTasks.isNotEmpty()) {
            // 網路恢復，執行待重試任務
            retryPendingTasks()
        }
    }
    
    private fun retryPendingTasks() {
        val tasks = pendingTasks.toList()
        pendingTasks.clear()
        
        ASCoroutine.runInNewCoroutine {
            tasks.forEach { task ->
                try {
                    task()
                } catch (e: Exception) {
                    // 重試失敗，再次加入列表
                    pendingTasks.add(task)
                }
            }
        }
    }
}
```

### 4. 流量監控

```kotlin
class DataUsageMonitor(private val context: Context) {
    
    private var wifiDataUsed = 0L
    private var mobileDataUsed = 0L
    
    fun trackDataUsage(bytesTransferred: Long) {
        val networkType = ASNetworkStateChangeReceiver.getCurrentNetworkType(context)
        
        when (networkType) {
            ASNetworkStateChangeReceiver.NetworkType.WIFI -> {
                wifiDataUsed += bytesTransferred
            }
            ASNetworkStateChangeReceiver.NetworkType.MOBILE -> {
                mobileDataUsed += bytesTransferred
                checkMobileDataLimit()
            }
            else -> { /* 忽略 */ }
        }
    }
    
    private fun checkMobileDataLimit() {
        val limit = UserSettings.mobileDataLimit
        if (limit > 0 && mobileDataUsed > limit) {
            showDataLimitWarning()
            pauseBackgroundSync()
        }
    }
    
    fun getDataUsageReport(): String {
        return """
            WiFi 用量: ${formatBytes(wifiDataUsed)}
            行動數據用量: ${formatBytes(mobileDataUsed)}
            總用量: ${formatBytes(wifiDataUsed + mobileDataUsed)}
        """.trimIndent()
    }
}
```

---

## ⚠️ 注意事項和陷阱

### 1. 權限檢查

```kotlin
// ❌ 錯誤：未檢查權限
fun checkNetwork() {
    ASNetworkStateChangeReceiver.isNetworkAvailable(context)
}

// ✅ 正確：確保已授予權限
fun checkNetwork() {
    if (ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        ASNetworkStateChangeReceiver.isNetworkAvailable(context)
    } else {
        requestNetworkPermission()
    }
}
```

### 2. 接收器生命週期

```kotlin
// ❌ 錯誤：忘記取消註冊
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val receiver = ASNetworkStateChangeReceiver.getInstance()
        registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        // 忘記在 onDestroy 取消註冊 -> 記憶體洩漏
    }
}

// ✅ 正確：配對註冊/取消註冊
class MyActivity : AppCompatActivity() {
    private lateinit var receiver: ASNetworkStateChangeReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = ASNetworkStateChangeReceiver.getInstance()
        registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
```

### 3. Android 版本相容性

```kotlin
// Android N (API 24) 及以上需要不同處理
fun getCurrentNetwork(context: Context): Network? {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
        as ConnectivityManager
    
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectivityManager.activeNetwork
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        // 舊版 API 處理
        null
    }
}
```

### 4. 多次觸發

```kotlin
// ⚠️ 網路狀態變更可能短時間內觸發多次
class NetworkDebouncer {
    private var lastNetworkState: Pair<Boolean, NetworkType>? = null
    private var debounceJob: Job? = null
    
    fun handleNetworkChange(isConnected: Boolean, networkType: NetworkType) {
        debounceJob?.cancel()
        debounceJob = CoroutineScope(Dispatchers.Main).launch {
            delay(500) // 延遲 500ms
            
            // 檢查狀態是否真的改變
            if (lastNetworkState?.first != isConnected || 
                lastNetworkState?.second != networkType) {
                
                lastNetworkState = Pair(isConnected, networkType)
                performActualNetworkAction(isConnected, networkType)
            }
        }
    }
}
```

---

## 🔧 最佳實踐

### 1. 全域網路管理器

```kotlin
object NetworkManager {
    private lateinit var context: Context
    private lateinit var receiver: ASNetworkStateChangeReceiver
    private val listeners = mutableSetOf<NetworkStateListener>()
    
    fun init(application: Application) {
        context = application.applicationContext
        receiver = ASNetworkStateChangeReceiver.getInstance()
        
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(receiver, filter)
        
        receiver.addListener(object : 
            ASNetworkStateChangeReceiver.NetworkStateChangeListener {
            
            override fun onNetworkStateChanged(
                isConnected: Boolean, 
                networkType: ASNetworkStateChangeReceiver.NetworkType
            ) {
                notifyAllListeners(isConnected, networkType)
            }
        })
    }
    
    fun addListener(listener: NetworkStateListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: NetworkStateListener) {
        listeners.remove(listener)
    }
    
    private fun notifyAllListeners(isConnected: Boolean, networkType: NetworkType) {
        listeners.forEach { it.onNetworkChanged(isConnected, networkType) }
    }
    
    interface NetworkStateListener {
        fun onNetworkChanged(isConnected: Boolean, networkType: NetworkType)
    }
}
```

### 2. 網路敏感操作包裝

```kotlin
suspend fun <T> executeWithNetwork(
    context: Context,
    operation: suspend () -> T
): Result<T> {
    return if (ASNetworkStateChangeReceiver.isNetworkAvailable(context)) {
        try {
            Result.success(operation())
        } catch (e: Exception) {
            Result.failure(e)
        }
    } else {
        Result.failure(NoNetworkException("網路未連接"))
    }
}

// 使用
val result = executeWithNetwork(context) {
    apiService.loadData()
}

result.onSuccess { data ->
    handleData(data)
}.onFailure { error ->
    showError(error)
}
```

---

## 📚 相關模組

- [asFramework-thread](asFramework-thread.md) - 執行緒管理（網路回呼切換）
- [Bahamut-service](Bahamut-service.md) - 背景服務（網路狀態監控）
- [telnet-logic](telnet-logic.md) - Telnet 連接管理

---

## 📝 技術特點總結

1. **系統級監聽**: 使用 Android BroadcastReceiver 監聽系統網路事件
2. **觀察者模式**: 支援多個監聽器訂閱網路狀態變更
3. **單例設計**: 全應用共用一個接收器實例
4. **類型識別**: 區分 WiFi、行動網路、乙太網路等類型
5. **生命週期管理**: 需要正確註冊和取消註冊避免洩漏
6. **執行緒安全**: 回呼在主執行緒執行，適合 UI 更新
