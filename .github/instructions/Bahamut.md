# Bahamut - 核心業務邏輯層

**applyto**: `app/src/main/java/com/kota/Bahamut/**/*.kt`, `app/src/main/java/com/kota/Bahamut/**/*.java`

## 📋 模組概述

Bahamut 模組是整個 BBS 客戶端的核心業務邏輯層，實現所有與巴哈姆特 BBS (bbs.gamer.com.tw) 互動的功能。使用 Telnet 協定連接 BBS 伺服器，處理 ANSI 色碼，實現文章瀏覽、發文推文、看板管理、書籤功能等完整的 BBS 操作。

**技術棧**: Kotlin + Java 混合, Telnet 協定, ANSI 解析  
**設計模式**: 命令模式, 單例模式, 物件池模式, MVC  
**命名前綴**: Bahamut (業務類別), Dialog (對話框), Page (頁面)

---

## 📂 子模組結構

### 1️⃣ `command/` - BBS 命令系統（核心）
封裝所有 BBS 操作為命令物件，使用命令模式實現。

**功能分類**:
- 文章操作：載入、發文、推文、編輯、刪除、搜尋
- 區塊管理：載入第一/最後區塊、區塊切換
- 導航命令：同標題導航、文章列表移動
- 信件系統：寄信、轉寄

**核心設計**: 每個命令封裝 Telnet 指令序列，支援非同步執行和回呼

### 2️⃣ `dataModels/` - 資料模型與本地儲存
本地資料庫、快取、書籤管理。

**功能分類**:
- 書籤系統：`Bookmark`, `BookmarkList`, `BookmarkStore`
- 文章暫存：`ArticleTemp`, `ArticleTempStore`
- 縮網址：`ShortenUrl`, `UrlDatabase`
- 引用資料：`ReferenceAuthor`

### 3️⃣ `dialogs/` - 業務對話框
所有 BBS 功能相關的對話框。

**功能分類**:
- 文章操作：發文、推文、搜尋、引用
- 編輯工具：插入表情、插入符號、選擇簽名檔
- 色彩工具：選色器、著色對話框
- 圖片網址：縮網址、縮圖上傳
- 看板搜尋：搜尋看板對話框

### 4️⃣ `listPage/` - 列表頁面基礎架構（重要）
**所有列表頁面的基類**，實現 20 項/區塊的分頁載入機制。

**核心元件**:
- `TelnetListPage` - 列表頁面基類
- `TelnetListPageBlock` - 資料區塊（20 項）
- `TelnetListPageItem` - 列表項目
- `ListState` - 列表狀態儲存

**關鍵機制**: 區塊載入、物件池、狀態保存、自動刷新

### 5️⃣ `pages/` - 業務頁面集合（最大子模組）
所有功能頁面的實現。

**頁面分類**:
- **文章相關**: `articlePage/` - 文章閱讀和顯示
- **看板相關**: `boardPage/` - 看板瀏覽和管理
- **書籤管理**: `bookmarkPage/` - 我的最愛
- **精華區**: `essencePage/` - 精華區瀏覽
- **信箱**: `mailPage/` - 私人信箱
- **訊息**: `messages/` - 系統訊息
- **使用者**: `bbsUser/` - 使用者資訊
- **黑名單**: `blockListPage/` - 黑名單管理
- **主題**: `theme/` - 主題設定與深色模式（詳見 [Bahamut-theme.md](Bahamut-theme.md)）
- **登入**: `login/` - 登入頁面
- **其他**: 主頁、設定、分類、發文等

### 6️⃣ `service/` - 背景服務與設定
系統服務、使用者設定、雲端備份等。

**功能分類**:
- **設定管理**: `UserSettings`, `TempSettings`, `NotificationSettings`
- **背景服務**: `BahaBBSBackgroundService`
- **雲端功能**: `CloudBackup`, `CloudBackupListener`
- **加密功能**: `AESCrypt`
- **付費功能**: `MyBillingClient`
- **工具**: `CommonFunctions`, `AhoCorasick`

---

## 🎯 核心架構

### 根目錄關鍵檔案

#### `BahamutController.kt` - 主控制器
```kotlin
class BahamutController : ASNavigationController(), TelnetClientListener
```
- **職責**: 整個 BBS 客戶端的主控制器
- **功能**: 管理頁面堆疊、處理 Telnet 連接、響應使用者操作
- **生命週期**: 與 MainActivity 同生命週期
- **關鍵方法**: `onCreate()`, `onTelnetConnected()`, `onTelnetDisconnected()`

#### `BahamutStateHandler.kt` - 狀態處理器
```kotlin
class BahamutStateHandler : TelnetStateHandler()
```
- **職責**: 解析 Telnet 伺服器回應，驅動頁面轉換
- **功能**: 
  - 偵測當前 BBS 畫面類型（看板列表、文章列表、文章內容等）
  - 自動切換到對應的頁面
  - 處理狀態機轉換
- **關鍵機制**: 所有頁面轉換由狀態處理器驅動，而非使用者操作直接觸發

#### `PageContainer.kt` - 頁面容器（單例）
```kotlin
object PageContainer {
    var boardPage: BoardMainPage? = null
    var articlePage: ArticlePage? = null
    // ... 其他頁面
}
```
- **職責**: 管理主要頁面的單例快取
- **目的**: 避免重複建立頁面，保持狀態
- **使用方式**: 
  ```kotlin
  val page = PageContainer.instance!!.boardPage  // 取得或建立
  PageContainer.instance!!.cleanBoardPage()      // 銷毀
  ```

#### `BahamutPage.kt` - 頁面類型定義
```kotlin
interface BahamutPage {
    companion object {
        const val BAHAMUT_MAIN: Int = 1
        const val BAHAMUT_BOARD: Int = 2
        const val BAHAMUT_ARTICLE: Int = 3
        // ... 其他類型
    }
}
```

---

## 🔧 關鍵設計模式

### 1. 命令模式（Command Pattern）
所有 BBS 操作封裝為命令物件：

```kotlin
class BahamutCommandLoadBlock(private val blockIndex: Int) : TelnetCommand() {
    override fun execute(page: TelnetListPage) {
        // 1. 發送 Telnet 指令
        TelnetOutputBuilder.create()
            .pushKey(TelnetKeyboard.CTRL_Z)
            .pushString(blockIndex.toString())
            .sendToServer()
    }
    
    override fun executeFinished(page: TelnetListPage, block: TelnetListPageBlock?) {
        // 2. 處理回應
        page.setBlock(blockIndex, block)
    }
}

// 使用
pushCommand(BahamutCommandLoadBlock(0))
```

### 2. 物件池模式（Object Pooling）
所有列表項目和區塊使用物件池避免 GC：

```kotlin
class BoardPageItem {
    companion object {
        private val _pool = Stack<BoardPageItem>()
        
        fun create(): BoardPageItem {
            synchronized(_pool) {
                return if (_pool.isNotEmpty()) _pool.pop() 
                       else BoardPageItem()
            }
        }
        
        fun recycle(item: BoardPageItem) {
            item.reset()  // 清空資料
            synchronized(_pool) { _pool.push(item) }
        }
    }
}
```

**使用原則**:
- 建立項目使用 `create()`
- 移除項目後使用 `recycle()`
- 區塊移除時回收所有項目

### 3. 區塊分頁載入（Block Loading）
列表頁面採用 20 項/區塊的分頁機制：

```kotlin
// 區塊計算
val blockIndex = itemIndex / 20      // 區塊索引
val indexInBlock = itemIndex % 20    // 區塊內索引

// 區塊管理
setBlock(blockIndex, telnetListPageBlock)  // 設定區塊
getBlock(blockIndex)                       // 取得區塊
removeBlock(blockIndex)                    // 移除區塊（會自動回收項目）
```

**區塊生命週期**: 載入 → 快取 → 回收（物件池）

### 4. 單例頁面管理
主要頁面透過 `PageContainer` 快取：

```kotlin
// 堆疊式頁面（每次建立新實例）
val classPage = ClassPage(name, title)
navigationController.pushViewController(classPage)

// 單例頁面（使用快取）
val boardPage = PageContainer.instance!!.boardPage
navigationController.pushViewController(boardPage)
```

---

## ⚡ 關鍵執行流程

### 文章列表載入流程
```
1. 使用者進入看板
   ↓
2. BahamutStateHandler 偵測到看板頁面
   ↓
3. 自動推送 BoardMainPage
   ↓
4. BoardMainPage.onPageWillAppear()
   ↓
5. 推送 BahamutCommandLoadFirstBlock
   ↓
6. 接收 Telnet 回應並解析為 TelnetListPageBlock
   ↓
7. executeFinished() 回呼
   ↓
8. setBlock(0, block) 設定第一個區塊
   ↓
9. safeNotifyDataSetChanged() 更新 ListView
   ↓
10. 使用者捲動 → 觸發載入更多區塊
```

### 發文流程
```
1. 使用者點擊發文按鈕
   ↓
2. 顯示 DialogPostArticle
   ↓
3. 使用者輸入標題、內容
   ↓
4. 點擊確定 → DialogPostArticleListener.onPost()
   ↓
5. 推送 BahamutCommandPostArticle
   ↓
6. 發送文章內容到伺服器
   ↓
7. 等待伺服器回應
   ↓
8. executeFinished() 回呼
   ↓
9. 更新文章列表（重新載入最後區塊）
   ↓
10. safeNotifyDataSetChanged() 更新 UI
```

---

## 🐛 已知問題和注意事項

### ⚠️ 重要：ListView 更新崩潰問題
**問題**: `BoardMainPage` 在發文後可能出現 `IllegalStateException`

**原因**: 在 `recoverPost()` 和 `finishPost()` 方法中多次調用 adapter 更新：
```kotlin
// ❌ 錯誤：多次更新導致崩潰
adapter.notifyDataSetChanged()
safeNotifyDataSetChanged()
listView.invalidateViews()
```

**解決方案**: 只調用一次 `safeNotifyDataSetChanged()`，並包裝在 `ASRunner` 中：
```kotlin
// ✅ 正確
object : ASRunner() {
    override fun run() {
        safeNotifyDataSetChanged()  // 只調用一次
    }
}.runInMainThread()
```

### ⚠️ 自動刷新管理
所有使用自動刷新的頁面必須正確管理協程：

```kotlin
private var autoLoadJob: Job? = null

// 啟動自動刷新
fun startAutoLoad() {
    stopAutoLoad()  // 先停止舊的
    autoLoadJob = CoroutineScope(Dispatchers.IO).launch {
        while (isActive) {
            delay(1000)
            if (shouldAutoLoad()) loadLastBlock()
        }
    }
}

// 停止自動刷新（必須在 onPageWillDisappear 調用）
fun stopAutoLoad() {
    autoLoadJob?.cancel()
    autoLoadJob = null
}
```

### ⚠️ 狀態保存和恢復
列表頁面必須保存和恢復捲動位置：

```kotlin
// 保存（在 onPageWillDisappear）
override fun onPageWillDisappear() {
    super.onPageWillDisappear()
    saveListState()  // 儲存位置和偏移量
}

// 恢復（在 onPageDidAppear）
override fun onPageDidAppear() {
    super.onPageDidAppear()
    loadListState()  // 恢復捲動位置
}
```

---

## 📝 開發規範

### 建立新的 BBS 命令
1. 繼承 `TelnetCommand`
2. 實作 `execute()` - 發送 Telnet 指令
3. 實作 `executeFinished()` - 處理回應
4. 在 `BahamutCommandDef` 定義命令 ID

### 建立新的列表頁面
1. 繼承 `TelnetListPage`
2. 定義 Item 和 Block 類別（使用物件池）
3. 實作區塊載入邏輯
4. 實作 Adapter
5. 處理自動刷新和狀態保存

### 建立新的對話框
1. 繼承 `ASAlertDialog` 或 `ASListDialog`
2. 定義 Listener 介面
3. 建立 Builder 模式
4. 處理使用者輸入驗證

### 新增頁面到 PageContainer
```kotlin
// 在 PageContainer 中新增
private var _myPage: MyPage? = null
val myPage: MyPage
    get() {
        if (_myPage == null) {
            _myPage = MyPage()
        }
        return _myPage!!
    }

fun cleanMyPage() {
    _myPage = null
}
```

---

## 🔗 與其他模組的關係

```
Bahamut (業務邏輯)
    ↓ 依賴
    ├── asFramework (框架層 - 繼承 ASViewController/ASNavigationController)
    ├── telnet (Telnet 客戶端 - 使用 TelnetClient/TelnetCommand)
    ├── telnetUI (UI 元件 - 使用 TelnetView/TelnetPage)
    ├── textEncoder (編碼 - 使用 B2UEncoder/U2BEncoder)
    └── dataPool (資料池 - 使用 MutableByteBuffer)
```

---

## 📚 延伸閱讀

- [listPage 詳細文件](.github/instructions/Bahamut-listPage.md)
- [command 詳細文件](.github/instructions/Bahamut-command.md)
- [pages 詳細文件](.github/instructions/Bahamut-pages.md)
- [主要架構指南](.github/copilot-instructions.md)

---

**維護者**: Bahamut BBS 開發團隊  
**最後更新**: 2025-12-11
