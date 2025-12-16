# Bahamut/service - 背景服務與設定

**applyto**: `app/src/main/java/com/kota/Bahamut/service/**/*.kt`, `app/src/main/java/com/kota/Bahamut/service/**/*.java`

## 📋 模組概述

service 模組提供背景服務、設定管理、加密、雲端備份、付費系統等核心功能。

**技術棧**: Kotlin + Java, Android Service, Google Play Billing  
**設計模式**: 單例模式, 觀察者模式

---

## 📂 主要元件

### 1️⃣ 設定管理

#### `UserSettings.kt` - 使用者設定
```kotlin
object UserSettings {
    
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    }
    
    // 自動登入
    var autoLogin: Boolean
        get() = prefs.getBoolean("auto_login", false)
        set(value) = prefs.edit().putBoolean("auto_login", value).apply()
    
    // 自動刷新
    var autoRefresh: Boolean
        get() = prefs.getBoolean("auto_refresh", true)
        set(value) = prefs.edit().putBoolean("auto_refresh", value).apply()
    
    // 字型大小
    var fontSize: Int
        get() = prefs.getInt("font_size", 16)
        set(value) = prefs.edit().putInt("font_size", value).apply()
}
```

#### `TempSettings.kt` - 暫存設定
臨時設定，不持久化。

#### `NotificationSettings.kt` - 通知設定
推播通知相關設定。

---

### 2️⃣ 背景服務

#### `BahaBBSBackgroundService.kt` - BBS 背景服務
```kotlin
class BahaBBSBackgroundService : Service() {
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 檢查新訊息、新推文等
        checkNewMessages()
        return START_STICKY
    }
    
    private fun checkNewMessages() {
        // 背景檢查邏輯
    }
}
```

---

### 3️⃣ 加密功能

#### `AESCrypt.kt` - AES 加密工具
```kotlin
object AESCrypt {
    
    fun encrypt(plainText: String, password: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = generateKey(password)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val encrypted = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }
    
    fun decrypt(encryptedText: String, password: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = generateKey(password)
        cipher.init(Cipher.DECRYPT_MODE, key)
        
        val decoded = Base64.decode(encryptedText, Base64.DEFAULT)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted)
    }
    
    private fun generateKey(password: String): SecretKey {
        // 使用 PBKDF2 生成金鑰
    }
}
```

---

### 4️⃣ 雲端備份

#### `CloudBackup.kt` - 雲端備份
```kotlin
class CloudBackup {
    
    interface CloudBackupListener {
        fun onBackupStart()
        fun onBackupProgress(progress: Int)
        fun onBackupSuccess()
        fun onBackupFailed(error: Exception)
    }
    
    var listener: CloudBackupListener? = null
    
    fun backup(data: String) {
        listener?.onBackupStart()
        
        ASCoroutine.runInNewCoroutine {
            try {
                // 上傳到雲端
                val result = uploadToCloud(data)
                
                object : ASRunner() {
                    override fun run() {
                        listener?.onBackupSuccess()
                    }
                }.runInMainThread()
            } catch (e: Exception) {
                object : ASRunner() {
                    override fun run() {
                        listener?.onBackupFailed(e)
                    }
                }.runInMainThread()
            }
        }
    }
}
```

---

### 5️⃣ 付費系統

#### `MyBillingClient.kt` - Google Play Billing
```kotlin
class MyBillingClient(private val context: Context) {
    
    private lateinit var billingClient: BillingClient
    
    fun init() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                handlePurchase(billingResult, purchases)
            }
            .enablePendingPurchases()
            .build()
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                // 連接成功
            }
            
            override fun onBillingServiceDisconnected() {
                // 斷線重連
            }
        })
    }
    
    fun purchase(sku: String) {
        // 發起購買流程
    }
}
```

---

### 6️⃣ 工具函式

#### `CommonFunctions.kt` - 通用工具
```kotlin
object CommonFunctions {
    
    fun getContextString(context: Context, resId: Int): String {
        return context.getString(resId)
    }
    
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
    }
}
```

#### `AhoCorasick.kt` - 字串匹配演算法
關鍵字過濾使用。

---

## 📚 相關模組

- [Bahamut-dataModels](Bahamut-dataModels.md) - 資料持久化
- [asFramework-network](asFramework-network.md) - 網路狀態

---

## 📝 技術特點總結

1. **設定持久化**: SharedPreferences
2. **背景服務**: 推播通知
3. **資料加密**: AES 加密敏感資料
4. **雲端備份**: 資料同步
5. **付費整合**: Google Play Billing
