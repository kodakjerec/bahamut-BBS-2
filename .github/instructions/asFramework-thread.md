# asFramework/thread - 執行緒管理系統（關鍵模組）

**applyto**: `app/src/main/java/com/kota/asFramework/thread/**/*.kt`

## 📋 模組概述

thread 模組是整個應用程式的執行緒管理核心，提供統一的執行緒調度和非同步任務執行功能。**所有 UI 更新都必須使用此模組**，禁止直接使用 Handler 或 runOnUiThread。

**🔴 這是強制性架構規範，違反會導致不可預測的執行緒問題！**

**技術棧**: Kotlin, Android Handler/Looper, Kotlin Coroutines  
**設計模式**: 模板方法模式 (Template Method)  
**命名前綴**: AS (Application Structure)

---

## 📂 核心類別

### 1️⃣ `ASRunner.kt` - 執行緒包裝器（核心）

**所有 UI 更新的統一執行緒調度器**，提供主執行緒/背景執行緒切換功能。

#### 完整實作
```kotlin
package com.kota.asFramework.thread

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.concurrent.atomic.AtomicInteger

abstract class ASRunner {
    private var token = 0
    private var runnable: Runnable? = null
    
    /**
     * 必須實作的執行方法
     * 這個方法會在適當的執行緒上執行
     */
    abstract fun run()
    
    /**
     * 在主執行緒內執行
     * ✅ 用於 UI 更新
     * ✅ 執行緒安全：可從任何執行緒呼叫
     */
    fun runInMainThread(): ASRunner {
        if (Thread.currentThread() === mainThread) {
            // 已在主執行緒，直接執行
            run()
        } else {
            // 在其他執行緒，發送到主執行緒
            val message = Message()
            message.obj = this
            mainHandler?.sendMessage(message)
        }
        return this
    }
    
    /**
     * 延遲執行（在主執行緒）
     * @param delayMillis 延遲毫秒數
     * ✅ 用於定時任務、動畫、自動刷新
     */
    fun postDelayed(delayMillis: Int) {
        // 先取消之前的任務
        cancel()
        
        token = tokenGenerator.incrementAndGet()
        runnable = Runnable { this.run() }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mainHandler?.postDelayed(runnable!!, token, delayMillis.toLong())
        } else {
            mainHandler?.postDelayed(runnable!!, delayMillis.toLong())
        }
    }
    
    /**
     * 取消延遲執行的任務
     */
    fun cancel() {
        if (runnable != null) {
            mainHandler?.removeCallbacks(runnable!!, token)
        }
        runnable = null
    }
    
    /**
     * 釋放資源
     */
    fun release() {
        cancel()
    }
    
    companion object {
        var mainLooper: Looper = Looper.getMainLooper()
        var mainThread: Thread? = null
        var mainHandler: Handler? = null
        private val tokenGenerator = AtomicInteger()
        
        /**
         * 初始化（在 Application.onCreate 呼叫）
         */
        @JvmStatic
        fun construct() {
            mainThread = Thread.currentThread()
            mainHandler = object : Handler(mainLooper) {
                override fun handleMessage(message: Message) {
                    val runner = message.obj as ASRunner
                    runner.run()
                }
            }
        }
        
        /**
         * 檢查當前是否在主執行緒
         */
        val isMainThread: Boolean
            get() = Thread.currentThread() === mainThread
        
        /**
         * 在新執行緒內執行
         * ✅ 用於背景任務、網路請求、檔案 I/O
         */
        @JvmStatic
        fun runInNewThread(runnable: Runnable?) {
            val thread = Thread(runnable)
            thread.start()
        }
    }
}
```

---

### 2️⃣ `ASCoroutine.kt` - Kotlin 協程工具

提供 Kotlin 協程的便捷方法，與 ASRunner 配合使用。

#### 完整實作
```kotlin
package com.kota.asFramework.thread

import kotlinx.coroutines.*

object ASCoroutine {
    
    /**
     * 在新協程中執行（IO 執行緒池）
     * ✅ 用於網路請求、資料庫操作、檔案讀寫
     */
    fun runInNewCoroutine(block: suspend CoroutineScope.() -> Unit): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            block()
        }
    }
    
    /**
     * 在主執行緒執行
     * ✅ 用於 UI 更新
     */
    suspend fun runInMainThread(block: suspend CoroutineScope.() -> Unit) {
        withContext(Dispatchers.Main) {
            block()
        }
    }
    
    /**
     * 確保在主執行緒執行（如果已在主執行緒則直接執行）
     */
    fun ensureMainThread(block: () -> Unit) {
        if (ASRunner.isMainThread) {
            block()
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                block()
            }
        }
    }
    
    /**
     * 延遲執行
     */
    suspend fun delay(millis: Long) {
        kotlinx.coroutines.delay(millis)
    }
}
```

---

## 🎯 使用指南

### 基本使用模式

#### 1. 在主執行緒更新 UI
```kotlin
// ✅ 正確：使用 ASRunner
object : ASRunner() {
    override fun run() {
        // UI 更新代碼
        textView.text = "Hello World"
        button.isEnabled = true
        adapter.notifyDataSetChanged()
    }
}.runInMainThread()

// ❌ 錯誤：直接使用 Handler
Handler(Looper.getMainLooper()).post {
    textView.text = "Hello" // 不符合架構規範
}

// ❌ 錯誤：直接使用 runOnUiThread
activity.runOnUiThread {
    textView.text = "Hello" // 不符合架構規範
}
```

#### 2. 背景任務執行
```kotlin
// ✅ 使用 ASRunner.runInNewThread
ASRunner.runInNewThread {
    // 背景任務
    val data = loadDataFromNetwork()
    
    // 切換到主執行緒更新 UI
    object : ASRunner() {
        override fun run() {
            displayData(data)
        }
    }.runInMainThread()
}

// ✅ 使用 ASCoroutine（推薦）
ASCoroutine.runInNewCoroutine {
    val data = loadDataFromNetwork()
    
    // 切換到主執行緒
    withContext(Dispatchers.Main) {
        displayData(data)
    }
}
```

#### 3. 延遲執行
```kotlin
// ✅ 延遲 3 秒後執行
val runner = object : ASRunner() {
    override fun run() {
        showTimeoutMessage()
    }
}
runner.postDelayed(3000) // 3 秒後執行

// 取消延遲任務
runner.cancel()
```

---

## 📖 實際應用場景

### 1. ListView Adapter 更新（極度重要）

**🔴 這是已知的崩潰高發點，必須嚴格遵守規範！**

```kotlin
// ❌ 錯誤：多次連續呼叫會崩潰
fun updateList() {
    adapter.notifyDataSetChanged()
    safeNotifyDataSetChanged()
    listView.invalidateViews()  // 崩潰！
}

// ✅ 正確：使用 ASRunner 包裝，只呼叫一次
fun updateList() {
    object : ASRunner() {
        override fun run() {
            safeNotifyDataSetChanged() // 只呼叫一次
        }
    }.runInMainThread()
}

// ✅ 在 TelnetListPage 中的正確用法
override fun executeFinished(block: TelnetListPageBlock?) {
    setBlock(blockIndex, block) // 設定資料
    
    // 單一 UI 更新
    object : ASRunner() {
        override fun run() {
            safeNotifyDataSetChanged()
        }
    }.runInMainThread()
}
```

### 2. 自動刷新機制

```kotlin
class BoardMainPage : TelnetListPage() {
    
    private var autoLoadJob: Job? = null
    
    /**
     * 啟動自動載入（在 onPageDidAppear 呼叫）
     */
    fun startAutoLoad() {
        if (!isAutoLoadEnable) return
        stopAutoLoad() // 先停止舊的
        
        autoLoadJob = CoroutineScope(Dispatchers.IO).launch {
            delay(10000) // 初始延遲 10 秒
            
            while (isActive) {
                if (shouldAutoLoad()) {
                    loadLastBlock()
                }
                delay(1000) // 每秒檢查一次
            }
        }
    }
    
    /**
     * 停止自動載入（在 onPageWillDisappear 呼叫）
     */
    fun stopAutoLoad() {
        autoLoadJob?.cancel()
        autoLoadJob = null
    }
    
    /**
     * 載入最新區塊
     */
    private fun loadLastBlock() {
        val command = BahamutCommandLoadLastBlock()
        pushCommand(command)
    }
}
```

### 3. 網路請求 + UI 更新

```kotlin
class ArticlePage : TelnetPage() {
    
    fun loadArticle(articleId: String) {
        // 顯示載入對話框
        ASProcessingDialog.showProcessingDialog("載入中...")
        
        // 背景執行緒執行網路請求
        ASCoroutine.runInNewCoroutine {
            try {
                val article = TelnetClient.loadArticle(articleId)
                
                // 切換到主執行緒更新 UI
                object : ASRunner() {
                    override fun run() {
                        ASProcessingDialog.dismissProcessingDialog()
                        displayArticle(article)
                    }
                }.runInMainThread()
                
            } catch (e: Exception) {
                object : ASRunner() {
                    override fun run() {
                        ASProcessingDialog.dismissProcessingDialog()
                        showError(e.message)
                    }
                }.runInMainThread()
            }
        }
    }
}
```

### 4. 定時器和倒數計時

```kotlin
class CountdownView : View {
    
    private var countdownRunner: ASRunner? = null
    private var remainingSeconds = 60
    
    fun startCountdown() {
        stopCountdown() // 停止舊的
        
        countdownRunner = object : ASRunner() {
            override fun run() {
                if (remainingSeconds > 0) {
                    updateDisplay(remainingSeconds)
                    remainingSeconds--
                    
                    // 1 秒後再次執行
                    postDelayed(1000)
                } else {
                    onCountdownFinished()
                }
            }
        }
        
        countdownRunner?.runInMainThread()
    }
    
    fun stopCountdown() {
        countdownRunner?.cancel()
        countdownRunner = null
    }
}
```

### 5. 批次操作

```kotlin
class BatchProcessor {
    
    fun processItems(items: List<Item>) {
        ASProcessingDialog.showProcessingDialog("處理中...")
        
        ASCoroutine.runInNewCoroutine {
            var processed = 0
            
            items.forEach { item ->
                processItem(item)
                processed++
                
                // 更新進度（在主執行緒）
                object : ASRunner() {
                    override fun run() {
                        ASProcessingDialog.setMessage(
                            "處理中 $processed/${items.size}"
                        )
                    }
                }.runInMainThread()
                
                delay(100) // 避免 UI 阻塞
            }
            
            // 完成（在主執行緒）
            object : ASRunner() {
                override fun run() {
                    ASProcessingDialog.dismissProcessingDialog()
                    showSuccess("處理完成")
                }
            }.runInMainThread()
        }
    }
}
```

---

## ⚠️ 重要注意事項和常見錯誤

### 1. 禁止的操作

```kotlin
// ❌ 禁止：直接使用 Handler
Handler(Looper.getMainLooper()).post { /* ... */ }

// ❌ 禁止：直接使用 runOnUiThread
activity.runOnUiThread { /* ... */ }

// ❌ 禁止：直接使用 view.post
view.post { /* ... */ }

// ✅ 正確：統一使用 ASRunner
object : ASRunner() {
    override fun run() { /* ... */ }
}.runInMainThread()
```

### 2. 避免記憶體洩漏

```kotlin
// ❌ 錯誤：Activity 被銷毀後仍執行
class MyActivity : Activity() {
    fun delayedAction() {
        val runner = object : ASRunner() {
            override fun run() {
                // Activity 可能已被銷毀
                textView.text = "Hello" // 崩潰！
            }
        }
        runner.postDelayed(5000)
    }
}

// ✅ 正確：在 onDestroy 取消
class MyActivity : Activity() {
    private val delayedRunner = object : ASRunner() {
        override fun run() {
            textView.text = "Hello"
        }
    }
    
    fun delayedAction() {
        delayedRunner.postDelayed(5000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        delayedRunner.cancel() // 取消延遲任務
    }
}
```

### 3. ASRunner 重用問題

```kotlin
// ⚠️ 注意：同一個 ASRunner 實例的 postDelayed 會取消前一個任務
val runner = object : ASRunner() {
    override fun run() {
        println("Execute")
    }
}

runner.postDelayed(1000) // 第一個任務
runner.postDelayed(2000) // 取消第一個，只執行這個

// ✅ 如需多個獨立任務，建立多個實例
val runner1 = object : ASRunner() { override fun run() { task1() } }
val runner2 = object : ASRunner() { override fun run() { task2() } }

runner1.postDelayed(1000)
runner2.postDelayed(2000)
```

### 4. 執行緒檢查

```kotlin
// ✅ 檢查當前執行緒
fun updateUI() {
    if (ASRunner.isMainThread) {
        // 已在主執行緒，直接更新
        textView.text = "Hello"
    } else {
        // 在背景執行緒，切換到主執行緒
        object : ASRunner() {
            override fun run() {
                textView.text = "Hello"
            }
        }.runInMainThread()
    }
}
```

---

## 🔧 最佳實踐

### 1. 統一的非同步模式

```kotlin
/**
 * 標準的非同步操作模式
 */
fun performAsyncTask(
    onStart: () -> Unit,
    onSuccess: (Result) -> Unit,
    onError: (Exception) -> Unit
) {
    // 主執行緒：顯示載入狀態
    object : ASRunner() {
        override fun run() {
            onStart()
        }
    }.runInMainThread()
    
    // 背景執行緒：執行任務
    ASCoroutine.runInNewCoroutine {
        try {
            val result = executeTask()
            
            // 主執行緒：顯示結果
            object : ASRunner() {
                override fun run() {
                    onSuccess(result)
                }
            }.runInMainThread()
            
        } catch (e: Exception) {
            // 主執行緒：顯示錯誤
            object : ASRunner() {
                override fun run() {
                    onError(e)
                }
            }.runInMainThread()
        }
    }
}

// 使用
performAsyncTask(
    onStart = { ASProcessingDialog.showProcessingDialog("載入中...") },
    onSuccess = { result ->
        ASProcessingDialog.dismissProcessingDialog()
        showResult(result)
    },
    onError = { error ->
        ASProcessingDialog.dismissProcessingDialog()
        showError(error)
    }
)
```

### 2. 可取消的協程任務

```kotlin
class MyPage : ASViewController() {
    
    private var loadJob: Job? = null
    
    override fun onPageWillAppear() {
        super.onPageWillAppear()
        loadData()
    }
    
    override fun onPageWillDisappear() {
        super.onPageWillDisappear()
        cancelLoad()
    }
    
    private fun loadData() {
        loadJob = ASCoroutine.runInNewCoroutine {
            val data = fetchDataFromServer()
            
            object : ASRunner() {
                override fun run() {
                    displayData(data)
                }
            }.runInMainThread()
        }
    }
    
    private fun cancelLoad() {
        loadJob?.cancel()
        loadJob = null
    }
}
```

### 3. 執行緒安全的單例

```kotlin
object ThreadSafeManager {
    
    @Volatile
    private var instance: Manager? = null
    
    fun getInstance(): Manager {
        return instance ?: synchronized(this) {
            instance ?: Manager().also { instance = it }
        }
    }
    
    fun performAction() {
        // 確保在主執行緒
        object : ASRunner() {
            override fun run() {
                updateUI()
            }
        }.runInMainThread()
    }
}
```

---

## 📚 相關模組

- [asFramework-pageController](asFramework-pageController.md) - 頁面生命週期中的執行緒管理
- [asFramework-dialog](asFramework-dialog.md) - 對話框的執行緒安全
- [Bahamut-listPage](Bahamut-listPage.md) - 列表更新的執行緒處理

---

## 📝 技術特點總結

1. **統一介面**: 所有執行緒操作通過 ASRunner/ASCoroutine
2. **執行緒安全**: 自動處理主執行緒/背景執行緒切換
3. **記憶體安全**: 支援取消延遲任務避免洩漏
4. **簡潔 API**: 抽象方法模式，代碼簡潔易讀
5. **Kotlin 協程整合**: 支援現代 Kotlin 協程
6. **禁止原生 API**: 強制使用框架 API 保持一致性
7. **崩潰預防**: 避免執行緒競爭和 UI 執行緒違規
