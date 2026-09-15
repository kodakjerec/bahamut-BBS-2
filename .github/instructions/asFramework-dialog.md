# asFramework/dialog - 對話框系統

**applyto**: `app/src/main/java/com/kota/asFramework/dialog/**/*.kt`

## 📋 模組概述

dialog 模組提供完整的對話框系統，包含警告對話框、列表選擇對話框、處理中對話框等常用元件。設計理念強調統一的介面、簡潔的 API 和靈活的委派機制。

**技術棧**: Kotlin, Android Dialog  
**設計模式**: 單例模式 (Singleton), 委派模式 (Delegate)  
**命名前綴**: AS (Application Structure)

---

## 📂 檔案結構

### 核心對話框

#### `ASDialog.kt` - 對話框基類
所有對話框的基類，提供基本的生命週期管理。

```kotlin
abstract class ASDialog : Dialog {
    override fun show()
    override fun dismiss()
    override fun onBackPressed()
}
```

#### `ASAlertDialog.kt` - 警告對話框
顯示訊息、確認操作的標準對話框。

**特性**:
- 支援流式鏈式呼叫 (Chaining API: `.setTitle()`, `.setMessage()`, `.addButton()`)
- 動態主題色彩適配（邊框 `bahamut_dialogBorderColor`、標題 `bahamut_dialogTitle*`、按鈕 `bahamut_dialogItemBackground`）
- 自訂多按鈕與點擊監聽器

**使用範例**:
```kotlin
// 簡單訊息對話框
ASAlertDialog.createDialog()
    .setTitle("提示")
    .setMessage("操作成功")
    .addButton("確定")
    .show()

// 多按鈕確認對話框
ASAlertDialog.createDialog()
    .setTitle("確認刪除")
    .setMessage("確定要刪除這篇文章嗎？")
    .addButton("取消") // index = 0
    .addButton("確定") // index = 1
    .setListener { dialog, index ->
        when (index) {
            1 -> deleteArticle()
        }
    }
    .show()
```

#### `ASAlertDialogListener.kt` - 警告對話框監聽器
```kotlin
interface ASAlertDialogListener {
    fun onAlertDialogDismissWithButtonIndex(paramASAlertDialog: ASAlertDialog, paramInt: Int)
}
```

#### `ASListDialog.kt` - 列表選擇對話框
顯示可滾動選擇項目的列表對話框（如文章暫存檔清單）。項目底色與文字透過 `bahamut_listDialogItemBackground` 與 `bahamut_listDialogItemTextColor` 與全域按鈕完全解耦，黑底白字不污染。

**使用範例**:
```kotlin
val dialog = ASListDialog.createDialog()
    .setTitle("請選擇看板")
    .addItem("C_Chat")
    .addItem("Gossiping")
    .addItem("NBA")
    .setListener(object : ASListDialogItemClickListener {
        override fun onListDialogItemClicked(dialog: ASListDialog?, index: Int, title: String?) {
            println("選取了: $title")
        }

        override fun onListDialogItemLongClicked(dialog: ASListDialog?, index: Int, title: String?): Boolean {
            return false
        }
    })
dialog.show()
```

#### `ASListDialogItemClickListener.kt` - 列表項目點擊監聽器
```kotlin
interface ASListDialogItemClickListener {
    fun onListDialogItemClicked(paramASListDialog: ASListDialog?, index: Int, title: String?)
    fun onListDialogItemLongClicked(paramASListDialog: ASListDialog?, index: Int, title: String?): Boolean
}
```

#### `ASListDialogExtendedItemClickListener.kt` - 擴展列表項目點擊監聽器
```kotlin
interface ASListDialogExtendedItemClickListener {
    fun onListDialogExtendedItemClicked(paramASListDialog: ASListDialog?, paramInt: Int)
    fun onListDialogExtendedItemLongClicked(paramASListDialog: ASListDialog?, paramInt: Int): Boolean
}
```

#### `ASProcessingDialog.kt` - 處理中對話框
顯示載入或處理狀態的模態對話框（單例模式）。

**特性**:
- 全域單例（一次只能有一個）
- 顯示處理訊息
- 自動執行緒安全
- 返回鍵處理

**使用範例**:
```kotlin
// 顯示處理中對話框
ASProcessingDialog.showProcessingDialog("載入中...")

// 非同步操作
ASCoroutine.runInNewCoroutine {
    try {
        // 執行耗時操作
        val result = loadDataFromServer()
        
        // 在主執行緒更新 UI
        object : ASRunner() {
            override fun run() {
                ASProcessingDialog.dismissProcessingDialog()
                showResult(result)
            }
        }.runInMainThread()
    } catch (e: Exception) {
        object : ASRunner() {
            override fun run() {
                ASProcessingDialog.dismissProcessingDialog()
                showError(e)
            }
        }.runInMainThread()
    }
}

// 帶返回鍵處理的對話框
ASProcessingDialog.showProcessingDialog(
    aMessage = "上傳中...",
    onBackDelegate = object : ASProcessingDialogOnBackDelegate {
        override fun onASProcessingDialogOnBackDetected(dialog: ASProcessingDialog): Boolean {
            // 返回 true 表示消費了返回鍵事件
            cancelUpload()
            dialog.dismiss()
            return true
        }
    }
)

// 更新訊息
ASProcessingDialog.setMessage("處理中，請稍候...")

// 關閉對話框
ASProcessingDialog.dismissProcessingDialog()
```

#### `ASProcessingDialogOnBackDelegate.kt` - 返回鍵處理委派
```kotlin
interface ASProcessingDialogOnBackDelegate {
    /**
     * 當返回鍵被按下
     * @param dialog 對話框實例
     * @return true 表示消費事件，false 表示不處理
     */
    fun onASProcessingDialogOnBackDetected(dialog: ASProcessingDialog): Boolean
}
```

### 輔助類別

#### `ASDialogOnBackPressedDelegate.kt` - 返回鍵委派
通用的返回鍵處理介面。

```kotlin
interface ASDialogOnBackPressedDelegate {
    fun onASDialogBackPressed(dialog: ASDialog): Boolean
}
```

#### `ASLayoutParams.kt` - 版面配置參數
對話框的版面配置參數定義。

```kotlin
class ASLayoutParams {
    var width: Int
    var height: Int
    var gravity: Int
    var margins: Margins
    
    companion object {
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
    }
}
```

---

## 🎯 使用指南

### 選擇正確的對話框

| 對話框類型 | 使用場景 | 關鍵特性 |
|-----------|---------|---------|
| **ASAlertDialog** | 顯示訊息、確認操作 | 標題+訊息+按鈕 |
| **ASListDialog** | 從列表中選擇項目 | 可點擊列表 |
| **ASProcessingDialog** | 顯示載入/處理狀態 | 模態、單例、不可取消 |

### 執行緒安全注意事項

**ASProcessingDialog 自動處理執行緒安全**:
```kotlin
// ✅ 正確：可以在任何執行緒呼叫
ASCoroutine.runInNewCoroutine {
    ASProcessingDialog.showProcessingDialog("載入中...")
    // 內部會自動切換到主執行緒顯示
    
    performBackgroundTask()
    
    ASProcessingDialog.dismissProcessingDialog()
    // 內部會自動切換到主執行緒關閉
}

// ⚠️ 其他對話框需要在主執行緒
object : ASRunner() {
    override fun run() {
        ASAlertDialog.createDialog(
            context = activity,
            title = "提示",
            message = "操作完成"
        ).show()
    }
}.runInMainThread()
```

### 對話框生命週期管理

**單例模式的對話框**:
- `ASProcessingDialog` 使用單例模式
- 全域只有一個實例
- 自動管理生命週期
- 必須配對 show/dismiss

```kotlin
// ✅ 正確：配對呼叫
ASProcessingDialog.showProcessingDialog("載入中...")
// ... 執行操作
ASProcessingDialog.dismissProcessingDialog()

// ❌ 錯誤：多次 show 而不 dismiss
ASProcessingDialog.showProcessingDialog("載入1")
ASProcessingDialog.showProcessingDialog("載入2") // 覆蓋前一個
```

**一般對話框**:
```kotlin
// 建立新實例
val dialog = ASAlertDialog.createDialog(...)
dialog.show()

// 使用完畢後關閉
dialog.dismiss()

// 對話框會自動釋放資源
```

---

## ⚠️ 常見問題和陷阱

### 1. ASProcessingDialog 未關閉

**問題**:
```kotlin
// ❌ 錯誤：異常時忘記關閉
ASProcessingDialog.showProcessingDialog("載入中...")
val data = loadData() // 如果拋出異常，對話框永不關閉
ASProcessingDialog.dismissProcessingDialog()
```

**解決方案**:
```kotlin
// ✅ 正確：使用 try-finally 確保關閉
ASProcessingDialog.showProcessingDialog("載入中...")
try {
    val data = loadData()
    handleData(data)
} finally {
    ASProcessingDialog.dismissProcessingDialog()
}
```

### 2. 在錯誤的執行緒顯示對話框

**問題**:
```kotlin
// ❌ 錯誤：在背景執行緒顯示 AlertDialog
Thread {
    val dialog = ASAlertDialog.createDialog(...)
    dialog.show() // 崩潰！
}.start()
```

**解決方案**:
```kotlin
// ✅ 正確：使用 ASRunner 切換到主執行緒
Thread {
    val result = performTask()
    
    object : ASRunner() {
        override fun run() {
            ASAlertDialog.createDialog(
                context = activity,
                message = "結果：$result"
            ).show()
        }
    }.runInMainThread()
}.start()
```

### 3. 對話框記憶體洩漏

**問題**:
```kotlin
// ❌ 錯誤：Activity 被銷毀後仍持有參考
class MyActivity : Activity() {
    private var dialog: ASAlertDialog? = null
    
    fun showDialog() {
        dialog = ASAlertDialog.createDialog(this, ...)
        dialog?.show()
    }
    
    // 忘記在 onDestroy 時清理
}
```

**解決方案**:
```kotlin
// ✅ 正確：及時清理
class MyActivity : Activity() {
    private var dialog: ASAlertDialog? = null
    
    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        dialog = null
    }
}
```

### 4. 對話框按鈕索引混淆

**記住按鈕索引規則**:
```kotlin
ASAlertDialog.createDialog(
    okButtonText = "確定",      // index = 0
    cancelButtonText = "取消"   // index = 1
)

// 處理點擊
override fun onASAlertDialogButtonClicked(dialog: ASAlertDialog, index: Int) {
    when (index) {
        0 -> // 確定按鈕
        1 -> // 取消按鈕
    }
}
```

---

## 🔧 最佳實踐

### 1. 統一的錯誤處理對話框

```kotlin
object DialogHelper {
    fun showError(context: Context, error: Throwable) {
        val message = when (error) {
            is NetworkException -> "網路連線失敗"
            is TimeoutException -> "連線逾時"
            else -> "發生錯誤：${error.message}"
        }
        
        ASAlertDialog.createDialog(
            context = context,
            title = "錯誤",
            message = message,
            okButtonText = "確定"
        ).show()
    }
    
    fun showSuccess(context: Context, message: String) {
        ASAlertDialog.createDialog(
            context = context,
            title = "成功",
            message = message,
            okButtonText = "確定"
        ).show()
    }
}
```

### 2. 可重用的確認對話框

```kotlin
fun showConfirmDialog(
    context: Context,
    title: String,
    message: String,
    onConfirm: () -> Unit
) {
    ASAlertDialog.createDialog(
        context = context,
        title = title,
        message = message,
        okButtonText = "確定",
        cancelButtonText = "取消",
        listener = object : ASAlertDialogListener {
            override fun onASAlertDialogButtonClicked(dialog: ASAlertDialog, index: Int) {
                if (index == 0) {
                    onConfirm()
                }
                dialog.dismiss()
            }
        }
    ).show()
}

// 使用
showConfirmDialog(
    context = this,
    title = "刪除確認",
    message = "確定要刪除嗎？"
) {
    performDelete()
}
```

### 3. 帶進度的處理對話框

```kotlin
suspend fun performLongOperation() {
    ASProcessingDialog.showProcessingDialog("初始化...")
    
    try {
        // 步驟 1
        ASProcessingDialog.setMessage("載入資料...")
        val data = loadData()
        
        // 步驟 2
        ASProcessingDialog.setMessage("處理資料...")
        val processed = processData(data)
        
        // 步驟 3
        ASProcessingDialog.setMessage("儲存結果...")
        saveResult(processed)
        
        ASProcessingDialog.dismissProcessingDialog()
        
        showSuccess("操作完成")
    } catch (e: Exception) {
        ASProcessingDialog.dismissProcessingDialog()
        showError(e)
    }
}
```

---

## 📚 相關模組

- [asFramework-pageController](asFramework-pageController.md) - 頁面控制器（對話框的顯示環境）
- [asFramework-thread](asFramework-thread.md) - 執行緒管理（執行緒安全）
- [Bahamut-dialogs](Bahamut-dialogs.md) - 業務對話框（使用這些基礎對話框）

---

## 📝 技術特點總結

1. **統一介面**: 所有對話框繼承自 ASDialog，提供一致的 API
2. **執行緒安全**: ASProcessingDialog 自動處理執行緒切換
3. **委派模式**: 使用監聽器和委派介面解耦業務邏輯
4. **單例管理**: ASProcessingDialog 使用單例避免多個載入對話框
5. **生命週期管理**: 自動管理對話框的顯示和關閉
6. **類型安全**: 使用泛型支援強型別的列表對話框
