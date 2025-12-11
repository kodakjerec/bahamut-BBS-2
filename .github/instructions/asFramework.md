# asFramework - 應用程式框架層

**applyto**: `app/src/main/java/com/kota/asFramework/**/*.kt`

## 📋 模組概述

asFramework 是整個應用程式的基礎框架層，提供 UI、對話框、網路、執行緒管理等核心功能。設計理念參考 iOS UIKit 架構，為上層業務邏輯提供統一的基礎設施。

**技術棧**: Kotlin, Android SDK  
**設計模式**: MVC, 委派模式, 觀察者模式  
**命名前綴**: AS (Application Structure)

---

## 📂 子模組結構

### 1️⃣ `dialog/` - 對話框系統
統一的對話框管理和顯示，包含警告框、列表對話框、處理中對話框等。

**關鍵類別**:
- `ASAlertDialog` - 警告對話框
- `ASListDialog` - 列表選擇對話框
- `ASProcessingDialog` - 載入/處理中對話框

### 2️⃣ `model/` - 基礎資料模型
幾何和基礎資料結構定義。

**關鍵類別**:
- `ASPoint` - 點座標 (x, y)
- `ASSize` - 尺寸 (width, height)

### 3️⃣ `network/` - 網路狀態管理
監控網路連線狀態變化。

**關鍵類別**:
- `ASNetworkStateChangeReceiver` - 網路狀態變更廣播接收器

### 4️⃣ `pageController/` - 頁面控制器（核心）
**最重要的子模組**，實現 iOS 風格的視圖控制器架構。

**關鍵類別**:
- `ASViewController` - 視圖控制器基類
- `ASNavigationController` - 導航控制器（頁面堆疊管理）
- `ASListViewController` - 列表視圖控制器
- `ASAnimation` / `ASPageAnimation` - 動畫系統

### 5️⃣ `thread/` - 執行緒管理（關鍵）
**所有 UI 更新的統一執行緒調度器**。

**關鍵類別**:
- `ASRunner` - **核心執行緒包裝器**（必須使用，禁止 Handler/runOnUiThread）
- `ASCoroutine` - Kotlin 協程工具

### 6️⃣ `ui/` - UI 元件庫
自訂 UI 元件集合。

**關鍵類別**:
- `ASListView` - 自訂列表視圖
- `ASScrollView` - 捲動視圖
- `ASToast` / `ASSnackBar` - 提示訊息

### 7️⃣ `utils/` - 工具類別
通用工具和型別轉換。

**關鍵類別**:
- `ASStreamReader` / `ASStreamWriter` - 串流處理
- `ASTypeConvertor` - 型別轉換器

---

## 🎯 核心設計理念

### iOS 風格的視圖控制器架構
```kotlin
// 頁面生命週期（類似 iOS UIViewController）
override fun onPageDidLoad()        // 視圖載入（一次性）
override fun onPageWillAppear()     // 即將出現
override fun onPageDidAppear()      // 已經出現
override fun onPageWillDisappear()  // 即將消失
override fun onPageDidDisappear()   // 已經消失
```

### 頁面導航管理
```kotlin
// Push 新頁面（iOS 風格）
navigationController.pushViewController(page, animated = true)

// Pop 返回上一頁
navigationController.popViewController(animated = true)

// Pop 到指定頁面
navigationController.popToViewController(targetPage, animated = true)
```

### 執行緒管理原則
```kotlin
// ✅ 正確：使用 ASRunner 執行 UI 更新
object : ASRunner() {
    override fun run() {
        // UI 更新代碼
        textView.text = "Updated"
    }
}.runInMainThread()

// ✅ 正確：背景任務執行
ASCoroutine.runInNewCoroutine {
    // 背景工作（網路請求、資料處理等）
    val data = fetchDataFromServer()
}

// ❌ 錯誤：禁止直接使用
runOnUiThread { /* ... */ }           // 禁用
Handler(Looper.getMainLooper()) { }   // 禁用
```

### 對話框使用模式
```kotlin
// 警告對話框
ASAlertDialog.Builder(context)
    .setTitle("標題")
    .setMessage("訊息內容")
    .setPositiveButton("確定", listener)
    .setNegativeButton("取消", null)
    .show()

// 列表對話框
ASListDialog.Builder(context)
    .setItems(arrayOf("選項1", "選項2"))
    .setListener { index -> /* 處理選擇 */ }
    .show()

// 處理中對話框
ASProcessingDialog.showProcessingDialog("載入中...")
// ... 非同步操作
ASProcessingDialog.dismissProcessingDialog()
```

---

## 🔗 與其他模組的關係

```
asFramework (基礎框架)
    ↑
    ├── Bahamut (業務邏輯層，繼承 ASViewController/ASNavigationController)
    ├── telnetUI (UI 層，使用 ASRunner/ASView)
    └── telnet (服務層，使用 ASCoroutine/ASRunner)
```

**依賴方向**: 所有模組依賴 asFramework，asFramework 不依賴任何業務模組

---

## ⚠️ 關鍵限制和注意事項

### 1. **執行緒安全**
- **強制使用 ASRunner** 進行 UI 更新
- 檢查執行緒：`ASRunner.isMainThread`
- 背景任務使用 `ASCoroutine.runInNewCoroutine`

### 2. **頁面生命週期管理**
- 必須正確實作 `onPageDidLoad` / `onPageWillAppear` 等方法
- 在 `onPageWillDisappear` 停止計時器/動畫
- 在 `onPageDidDisappear` 儲存狀態

### 3. **記憶體管理**
- 避免在 ViewController 持有 Activity 的強引用
- 使用 WeakReference 避免記憶體洩漏
- 頁面銷毀時清理監聽器

### 4. **動畫系統**
- 使用 `ASAnimation` 而非直接操作 View Animation
- 動畫完成後必須調用回呼
- 注意動畫取消時的資源釋放

---

## 📝 開發規範

### 類別命名
- 所有類別以 `AS` 開頭（Application Structure）
- 介面/監聽器以 `Listener` 或 `Delegate` 結尾
- 抽象類別以 `AS` + 功能名稱

### 方法命名
- 生命週期方法：`onPage` + 動作（DidLoad, WillAppear 等）
- 委派方法：動詞開頭（onClick, onItemSelected）
- 工具方法：靜態方法使用駝峰式命名

### 檔案組織
```kotlin
// 典型的 ASViewController 結構
class MyViewController : ASViewController() {
    // 1. 屬性宣告
    private lateinit var listView: ASListView
    
    // 2. 生命週期方法
    override fun onPageDidLoad() { }
    override fun onPageWillAppear() { }
    
    // 3. UI 事件處理
    private fun onItemClick(index: Int) { }
    
    // 4. 私有方法
    private fun loadData() { }
    
    // 5. Companion object（靜態成員）
    companion object {
        private const val TAG = "MyViewController"
    }
}
```

---

## 🚀 常見使用場景

### 場景 1: 建立新頁面
1. 繼承 `ASViewController` 或 `ASListViewController`
2. 實作 `onPageDidLoad()` 初始化 UI
3. 實作 `onPageWillAppear()` 載入資料
4. 使用 `navigationController.pushViewController()` 顯示

### 場景 2: 顯示對話框
1. 選擇合適的對話框類型（Alert/List/Processing）
2. 使用 Builder 模式建立
3. 設定監聽器處理使用者操作
4. 調用 `show()` 顯示

### 場景 3: 執行背景任務
1. 使用 `ASCoroutine.runInNewCoroutine { }` 執行
2. 在任務完成後使用 `ASRunner().runInMainThread()` 更新 UI
3. 顯示 `ASProcessingDialog` 提供使用者回饋

### 場景 4: 處理網路狀態變更
1. 註冊 `ASNetworkStateChangeReceiver`
2. 實作狀態變更回呼
3. 在頁面銷毀時反註冊

---

## 🐛 常見錯誤和解決方案

### 錯誤 1: 直接更新 UI 導致崩潰
```kotlin
// ❌ 錯誤
Thread {
    textView.text = "Update"  // CalledFromWrongThreadException
}.start()

// ✅ 正確
ASCoroutine.runInNewCoroutine {
    val data = fetchData()
    object : ASRunner() {
        override fun run() {
            textView.text = data
        }
    }.runInMainThread()
}
```

### 錯誤 2: 頁面生命週期未正確處理
```kotlin
// ❌ 錯誤：在 onPageDidLoad 啟動計時器
override fun onPageDidLoad() {
    startTimer()  // 頁面隱藏時仍在運行
}

// ✅ 正確：在 onPageWillAppear 啟動
override fun onPageWillAppear() {
    startTimer()
}
override fun onPageWillDisappear() {
    stopTimer()  // 頁面隱藏時停止
}
```

### 錯誤 3: 記憶體洩漏
```kotlin
// ❌ 錯誤：持有 Activity 強引用
class MyViewController(private val activity: Activity)

// ✅ 正確：使用 WeakReference
class MyViewController(activity: Activity) {
    private val activityRef = WeakReference(activity)
}
```

---

## 📚 延伸閱讀

- [pageController 詳細文件](.github/instructions/asFramework-pageController.md)
- [thread 詳細文件](.github/instructions/asFramework-thread.md)
- [dialog 詳細文件](.github/instructions/asFramework-dialog.md)
- [主要架構指南](.github/copilot-instructions.md)

---

**維護者**: Bahamut BBS 開發團隊  
**最後更新**: 2025-12-11
