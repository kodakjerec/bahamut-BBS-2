# 巴哈姆特 BBS Android 用戶端 - AI 程式碼撰寫指南

## 專案概述
使用 Telnet 協定連線至 bbs.gamer.com.tw 的 Android BBS 用戶端。從 v1.6 反編譯，Kotlin/Java 混合代碼庫。

**技術堆疊：** Kotlin 2.1.0, Android SDK 36, minSdk 26, Java 17

## 關鍵架構模式

### 1. **執行緒模型 - Coroutines / ASCoroutine**
UI 只能在主執行緒更新。已確定在主執行緒時可直接更新；從背景執行緒切回 UI 時使用 `ASCoroutine.ensureMainThread`。背景 I/O 或其他耗時工作使用 `ASCoroutine.runInNewCoroutine`，不要在主執行緒執行同步操作，也不要直接使用 `Handler` 或 `runOnUiThread`。

```kotlin
// 確保在主執行緒更新 UI
ASCoroutine.ensureMainThread {
    // 在此更新 UI
}

// 背景執行緒執行
ASCoroutine.runInNewCoroutine {
    // 背景作業
}

// 需要可取消的延遲工作時使用 coroutine Job
private var delayedJob: Job? = null

fun scheduleWork() {
    delayedJob?.cancel()
    delayedJob = CoroutineScope(Dispatchers.Main).launch {
        delay(3000)
        // 在主執行緒執行
    }
}

fun cancelScheduledWork() {
    delayedJob?.cancel()
    delayedJob = null
}
```

**檢查執行緒上下文：** `ASCoroutine.isMainThread`

### 2. **基於區塊的分頁 (20 個項目/區塊)**
所有基於列表的頁面都繼承自 `TelnetListPage`，使用區塊載入：

```kotlin
// 區塊計算
val blockIndex = itemIndex / 20  // getBlockIndex()
val indexInBlock = itemIndex % 20 // getIndexInBlock()

// 區塊管理
setBlock(blockIndex, telnetListPageBlock)
getBlock(blockIndex)
removeBlock(blockIndex)
```

**區塊生命週期：** 載入 → 快取 → 回收 (物件池模式)

### 3. **ListView Adapter 更新 - 極度重要**
**絕對不要連續呼叫多次 adapter 更新。只使用一次 safeNotifyDataSetChanged()：**

```kotlin
// ❌ 錯誤 - 多次更新會導致崩潰
adapter.notifyDataSetChanged()
safeNotifyDataSetChanged()
listView.invalidateViews()

// ✅ 正確 - 確保單次更新在主執行緒執行
ASCoroutine.ensureMainThread {
    safeNotifyDataSetChanged() // 呼叫 mDataSetObservable.notifyChanged()
}
```

**已知崩潰模式：** 在發表文章後的流程中重複通知 (`BoardMainPage.recoverPost()`, `finishPost()`)

### 4. **頁面導航 - ASNavigationController**
自訂的類 iOS 導航堆疊：

```kotlin
// 頁面生命週期
onPageDidLoad()       // 執行一次，View 建立時
onPageWillAppear()    // 顯示前，載入狀態
onPageDidAppear()     // 顯示中，開始自動重新整理
onPageWillDisappear() // 隱藏前，停止計時器
onPageDidDisappear()  // 隱藏後，儲存狀態

// 導航
navigationController.pushViewController(page)
navigationController.popViewController()
navigationController.popToViewController(page)
```

### 5. **單例頁面管理 - PageContainer**
頁面是快取的單例 (Singleton)，透過 `PageContainer.instance` 存取：

```kotlin
// 存取頁面
val boardPage = PageContainer.instance!!.boardPage  // 取得/建立
PageContainer.instance!!.cleanBoardPage()          // 銷毀

// 基於堆疊的頁面 (Class, BoardEssence)
PageContainer.instance!!.pushClassPage(name, title)
PageContainer.instance!!.popClassPage()
```

### 6. **Telnet 命令模式**
所有 Telnet 操作都使用命令堆疊：

```kotlin
class BahamutCommandLoadBlock : TelnetCommand() {
    override fun execute(page: TelnetListPage) {
        // 傳送 Telnet 命令
        TelnetOutputBuilder.create()
            .pushKey(TelnetKeyboard.CTRL_Z)
            .sendToServer()
    }
    
    override fun executeFinished(page: TelnetListPage, block: TelnetListPageBlock?) {
        // 處理回應
        page.setBlock(blockIndex, block)
    }
}

// 使用方式
pushCommand(BahamutCommandLoadBlock(blockIndex))
```

### 7. **效能優化：物件池 (Object Pooling)**
**所有的頁面項目與區塊都使用物件池：**

```kotlin
companion object {
    private val _pool = Stack<BoardPageItem>()
    
    fun create(): BoardPageItem {
        synchronized(_pool) {
            return if (_pool.isNotEmpty()) _pool.pop() 
                   else BoardPageItem()
        }
    }
    
    fun recycle(item: BoardPageItem) {
        synchronized(_pool) { _pool.push(item) }
    }
}
```

**移除區塊時必須回收項目：**
```kotlin
recycleItem(item)
recycleBlock(block)
```

### 8. **狀態管理 - BahamutStateHandler**
解析伺服器回應的中央 Telnet 狀態機：

```kotlin
override fun handleState() {
    loadState() // 解析 TelnetModel.frame
    
    // 透過游標位置與內容偵測頁面
    if (rowString00.contains("文章選讀")) {
        handleBoardMainPage()
    }
}
```

**重要：** 狀態處理器驅動「所有」的頁面轉換，而非使用者動作。

## 常見模式

### 自動重新整理的協程 (Coroutines)
```kotlin
private var autoLoadJob: Job? = null

fun startAutoLoad() {
    if (!isAutoLoadEnable) return
    stopAutoLoad()
    
    autoLoadJob = CoroutineScope(Dispatchers.IO).launch {
        delay(10000) // 初始延遲
        while (isActive) {
            if (shouldAutoLoad()) loadLastBlock()
            delay(1000) // 檢查間隔
        }
    }
}

fun stopAutoLoad() {
    autoLoadJob?.cancel()
    autoLoadJob = null
}
```

### 列表狀態保存
```kotlin
// 消失前儲存
saveListState() // 儲存位置 + 頂部偏移量

// 出現時還原
loadListState() // 還原捲動位置
```

### 帶對話框的錯誤處理
```kotlin
ASProcessingDialog.showProcessingDialog("載入中")
// ... 非同步操作
ASProcessingDialog.dismissProcessingDialog()
```

## 關鍵檔案參考
- `TelnetListPage.kt` - 具有區塊載入功能的基礎列表頁面
- `BoardMainPage.kt` - 看板主畫面 (有已知 Bug)
- `ASCoroutine.kt` - coroutine 與主執行緒派送工具
- `ASNavigationController.kt` - 頁面堆疊管理器
- `BahamutStateHandler.kt` - Telnet 回應解析器
- `PageContainer.kt` - 單例頁面快取
- `ThemeStore.kt` - 主題與深色模式狀態管理器
- `TelnetClient.kt` - Telnet 連線管理器

## 常見陷阱
1. **多次 adapter 更新** → `ListView.IllegalStateException`
2. **在背景執行緒直接更新 UI** → 使用 `ASCoroutine.ensureMainThread` 切回主執行緒
3. **忘記回收物件** → 記憶體流失 (Memory leaks)
4. **直接實例化頁面** → 請使用 PageContainer
5. **在主執行緒執行同步操作** → 請使用 `ASCoroutine.runInNewCoroutine`
6. **硬編碼顏色數值** → 請使用 `?attr/bahamut_*` 或 `CommonFunctions.getThemeColor(R.attr.bahamut_*)` 以支援深色模式與電子墨水螢幕相容性

## 測試流程變更
修改列表頁面後：
1. 導覽至看板 (例如：C_Chat)
2. 發表文章 → 檢查是否崩潰
3. 快速捲動 → 驗證載入是否順暢
4. 切換背景/前景 → 檢查狀態是否保存
5. 監控記憶體 (物件池應能防止流失)
