# com.kota 套件結構指南

本文件說明 `com.kota` 套件的第一層和第二層資料夾結構，提供給 GitHub Copilot 參考。

**applyto**: `app/src/main/java/com/kota/**/*.kt`, `app/src/main/java/com/kota/**/*.java`

---

## 📦 第一層模組總覽

```
com.kota/
├── asFramework/    # 自訂應用程式框架（UI、對話框、網路、執行緒管理）
├── Bahamut/        # 核心業務邏輯（BBS 功能實現）
├── dataPool/       # 資料緩衝和迭代器
├── telnet/         # Telnet 客戶端核心
├── telnetUI/       # Telnet UI 元件
└── textEncoder/    # Big5 與 UTF-8 編碼轉換
```

---

## 🔧 asFramework - 應用程式框架

**目的**: 提供 UI、對話框、網路、執行緒管理等基礎功能

### 第二層結構

#### `dialog/` - 對話框系統
對話框元件庫，包含警告框、列表對話框、處理中對話框等
- `ASAlertDialog.kt` - 警告對話框
- `ASListDialog.kt` - 列表對話框
- `ASProcessingDialog.kt` - 處理中對話框
- 各種對話框監聽器接口

#### `model/` - 基礎資料模型
幾何和基礎模型定義
- `ASPoint.kt` - 點座標
- `ASSize.kt` - 尺寸

#### `network/` - 網路管理
網路狀態監控和管理功能
- `ASNetworkStateChangeReceiver.kt` - 網路狀態變更接收器

#### `pageController/` - 頁面控制器
頁面導航和視圖控制器系統，處理頁面切換、動畫和生命週期管理
- `ASViewController.kt` - 視圖控制器基類
- `ASNavigationController.kt` - 導航控制器（iOS 風格的頁面堆疊管理）
- `ASListViewController.kt` - 列表視圖控制器
- `ASAnimation.kt` / `ASPageAnimation.kt` - 動畫系統
- `ASGestureView.kt` - 手勢處理視圖
- 各種生命週期監聽器

#### `thread/` - 執行緒管理
非同步任務執行和主執行緒調度
- `ASRunner.kt` - **核心執行緒包裝器**（所有 UI 更新必須使用）
- `ASCoroutine.kt` - 協程工具

#### `ui/` - UI 元件庫
自訂 UI 元件，包含列表視圖、捲動視圖、手勢處理等
- `ASListView.kt` - 自訂列表視圖
- `ASScrollView.kt` - 捲動視圖
- `ASToast.kt` / `ASSnackBar.kt` - 提示訊息

#### `utils/` - 工具類別
串流讀寫器和型別轉換器
- `ASStreamReader.kt` / `ASStreamWriter.kt` - 串流處理
- `ASTypeConvertor.kt` - 型別轉換

**架構特點**:
- iOS 風格的視圖控制器架構
- 完整的頁面生命週期管理
- 自訂手勢和動畫系統

---

## 🎮 Bahamut - 核心業務邏輯

**目的**: 實現巴哈姆特 BBS 的所有業務功能

### 第二層結構

#### `command/` - BBS 命令系統
實現所有 BBS 操作命令
- 文章操作: `BahamutCommandLoadArticle`, `BahamutCommandPostArticle`, `BahamutCommandPushArticle`
- 文章編輯: `BahamutCommandEditArticle`, `BahamutCommandDeleteArticle`, `BahamutCommandGoodArticle`
- 區塊載入: `BahamutCommandLoadBlock`, `BahamutCommandLoadFirstBlock`, `BahamutCommandLoadLastBlock`
- 搜尋功能: `BahamutCommandSearchArticle`, `BahamutCommandTheSameTitle*`
- 信件系統: `BahamutCommandSendMail`, `BahamutCommandFSendMail`
- `TelnetCommand.kt` - 命令基類

#### `dataModels/` - 資料模型與本地儲存
本地資料庫和快取管理
- `Bookmark.kt` / `BookmarkList.kt` / `BookmarkStore.kt` - 書籤系統
- `ArticleTemp.kt` / `ArticleTempStore.kt` - 文章暫存
- `ShortenUrl.kt` / `UrlDatabase.kt` - 縮網址資料庫
- `ReferenceAuthor.kt` - 引用作者資料

#### `dialogs/` - 對話框集合
各種業務對話框
- `DialogPostArticle.kt` - 發文對話框
- `DialogPushArticle.kt` - 推文對話框
- `DialogSearchArticle.kt` / `DialogSearchBoard.kt` - 搜尋對話框
- `DialogColorPicker.kt` - 選色器
- `DialogInsertExpression.kt` / `DialogInsertSymbol.kt` - 插入表情符號/符號
- `DialogShortenUrl.kt` / `DialogShortenImage.kt` - 縮網址/圖片上傳
- `DialogReference.kt` - 引用對話框
- `uploadImgMethod/` - 圖片上傳方法

#### `listPage/` - 列表頁面基礎架構
實現分頁載入和狀態管理（**20 項/區塊**）
- `TelnetListPage.kt` - **列表頁面基類**（所有列表頁面繼承）
- `TelnetListPageBlock.kt` / `TelnetListPageItem.kt` - 區塊和項目
- `ListState.kt` / `ListStateStore.kt` - 列表狀態儲存
- `PagePreloadCommand.kt` / `PageRefreshCommand.kt` - 預載和刷新命令

#### `pages/` - 業務頁面集合
所有功能頁面實現
- `articlePage/` - 文章閱讀頁面
- `boardPage/` - 看板頁面
- `bookmarkPage/` - 書籤管理頁面
- `essencePage/` - 精華區頁面
- `mailPage/` - 信箱頁面
- `messages/` - 訊息系統
- `blockListPage/` - 黑名單頁面
- `bbsUser/` - 使用者資訊頁面
- `login/` - 登入頁面
- `theme/` - 主題管理與深色模式規範（詳見 [Bahamut-theme.md](Bahamut-theme.md)）
- `model/` - 頁面資料模型
- `MainPage.kt` / `StartPage.kt` - 主頁面和啟動頁
- `SystemSettingsPage.kt` - 系統設定頁面
- `ClassPage.kt` - 分類頁面
- `PostArticlePage.kt` - 發文頁面

#### `service/` - 背景服務與設定管理
系統服務和使用者設定
- `BahaBBSBackgroundService.kt` - 背景服務
- `UserSettings.kt` / `TempSettings.kt` / `NotificationSettings.kt` - 設定管理
- `CloudBackup.kt` - 雲端備份
- `AESCrypt.kt` - 加密功能
- `CommonFunctions.kt` - 共用函式
- `MyBillingClient.kt` - 付費功能
- `AhoCorasick.kt` - 字串匹配演算法

### 根目錄核心檔案
- `BahamutController.kt` - **主控制器**（繼承 ASNavigationController）
- `BahamutStateHandler.kt` - **狀態處理器**（解析 Telnet 回應，驅動頁面轉換）
- `BahamutPage.kt` - 頁面類型定義接口
- `PageContainer.kt` - **頁面容器**（單例頁面快取管理）
- `MyApplication.kt` - 應用程式入口

**技術特點**:
- 使用 Telnet 協定連接 BBS
- 完整的 ANSI 色碼解析
- 物件池模式（Object Pooling）提升效能
- 本地資料快取和雲端備份

---

## 💾 dataPool - 資料緩衝管理

**目的**: 提供高效的資料緩衝和迭代器功能

### 核心元件（無第二層）
- `ByteIterator.kt` - 位元組迭代器，便捷的資料遍歷
- `MutableByteBuffer.kt` - 可變位元組緩衝區，動態管理讀寫操作

**使用場景**:
- Telnet 資料流處理
- 網路資料接收和解析
- 高效記憶體管理

---

## 📡 telnet - Telnet 客戶端核心

**目的**: 實現完整的 Telnet 客戶端功能，支援 Socket 和 WebSocket

### 第二層結構

#### `logic/` - 業務邏輯處理
BBS 特定邏輯處理
- `ArticleHandler.kt` - 文章處理邏輯
- `SearchBoardHandler.kt` - 看板搜尋處理
- `ClassMode.kt` / `ClassStep.kt` - 分類模式和步驟
- `ItemUtils.kt` - 項目工具函式

#### `model/` - Telnet 資料模型
終端機資料結構定義
- `TelnetModel.kt` - Telnet 主模型
- `TelnetFrame.kt` - **畫面幀**（終端機顯示內容）
- `TelnetRow.kt` - 終端機列資料
- `TelnetData.kt` - Telnet 資料
- `BitSpaceType.kt` - 位元空間類型

#### `reference/` - 參考定義與常數
Telnet 和 ANSI 規範定義
- `TelnetAnsiCode.kt` - **ANSI 轉義碼定義**
- `TelnetAsciiCode.kt` - ASCII 碼定義
- `TelnetKeyboard.kt` - 鍵盤輸入定義
- `TelnetDef.kt` - Telnet 通用定義

### 根目錄核心元件
- `TelnetClient.kt` - **Telnet 客戶端主類別**
- `TelnetConnector.kt` - 連接管理器
- `TelnetChannel.kt` - 通道介面
- `TelnetDefaultSocketChannel.kt` - 傳統 Socket 實現
- `TelnetWebSocketChannel.kt` - WebSocket 實現
- `TelnetReceiver.kt` / `TelnetReceiverThread.kt` - 資料接收器
- `TelnetAnsi.kt` - **ANSI 解析器**
- `TelnetArticle.kt` / `TelnetArticleItem.kt` - 文章相關類別
- `TelnetStateHandler.kt` - **狀態處理器基類**
- `TelnetOutputBuilder.kt` - 輸出建構器
- `TelnetUtils.kt` - 工具函式

**技術特點**:
- 雙連接模式（Socket / WebSocket）
- 完整的 ANSI 轉義序列解析
- 終端機畫面模擬（24x80）
- 非同步資料接收
- 事件驅動架構

---

## 🖥️ telnetUI - Telnet UI 元件

**目的**: 提供 Telnet 終端機的使用者介面元件

### 第二層結構

#### `textView/` - 文字視圖元件
支援多種字體大小的終端機文字顯示
- `TelnetTextView.kt` - **文字視圖基類**
- `TelnetTextViewSmall.kt` - 小字體視圖
- `TelnetTextViewNormal.kt` - 標準字體視圖
- `TelnetTextViewLarge.kt` - 大字體視圖
- `TelnetTextViewUltraLarge.kt` - 超大字體視圖

### 根目錄核心元件
- `TelnetPage.kt` - Telnet 頁面
- `TelnetView.kt` - Telnet 視圖
- `TelnetViewDrawer.kt` - **視圖繪製器**（處理 ANSI 色碼渲染）
- `TelnetHeaderItemView.kt` - 標題項目視圖
- `DividerView.kt` - 分隔線視圖

**技術特點**:
- 支援 ANSI 色碼顯示（256 色）
- 多種字體大小選項
- 高效的畫面渲染（Canvas 繪製）
- 自訂繪製邏輯

---

## 🔤 textEncoder - 編碼轉換

**目的**: 提供 Big5 和 UTF-8 編碼之間的轉換功能

### 核心元件（無第二層）
- `B2UEncoder.kt` - **Big5 轉 UTF-8 編碼器**（BBS 資料解碼）
- `U2BEncoder.kt` - **UTF-8 轉 Big5 編碼器**（使用者輸入編碼）
- `TextConverterBuffer.kt` - 文字轉換緩衝區

**使用場景**:
- BBS 資料接收時將 Big5 轉為 UTF-8 顯示
- 使用者輸入時將 UTF-8 轉為 Big5 傳送
- 文章內容編碼處理

**注意事項**:
- Big5 為雙位元組編碼，需正確處理字元邊界
- 某些特殊字元可能需要額外處理

---

## 🔗 模組間關係

```
textEncoder (編碼轉換)
    ↓
dataPool (資料緩衝)
    ↓
telnet (Telnet 客戶端)
    ↓
telnetUI (終端機 UI)
    ↓
Bahamut (業務邏輯)
    ↓
asFramework (應用程式框架)
```

**依賴方向**: 上層模組依賴下層模組，下層模組不依賴上層

---

## 📋 重要設計模式

### 1. 物件池模式（Object Pooling）
所有列表項目和區塊使用物件池，避免重複建立物件
```kotlin
companion object {
    private val _pool = Stack<Item>()
    fun create(): Item { /* 從池中取出或新建 */ }
    fun recycle(item: Item) { /* 回收到池中 */ }
}
```

### 2. 單例頁面管理
主要頁面透過 `PageContainer` 單例管理，避免重複建立
```kotlin
PageContainer.instance!!.boardPage  // 取得或建立
PageContainer.instance!!.cleanBoardPage()  // 銷毀
```

### 3. 命令模式（Command Pattern）
所有 Telnet 操作封裝為命令物件，支援命令堆疊和重試
```kotlin
class BahamutCommandLoadBlock : TelnetCommand() {
    override fun execute(page: TelnetListPage) { /* 執行 */ }
    override fun executeFinished(page: TelnetListPage, block: TelnetListPageBlock?) { /* 完成 */ }
}
```

### 4. 區塊分頁載入（Block Loading）
所有列表使用 20 項/區塊的分頁載入，支援預載和快取
```kotlin
val blockIndex = itemIndex / 20
val indexInBlock = itemIndex % 20
setBlock(blockIndex, block)
```

---

## ⚡ 關鍵執行緒模型

### ASRunner 執行緒管理
**所有 UI 更新必須使用 ASRunner，禁止直接使用 Handler/runOnUiThread**

```kotlin
// 主執行緒執行
object : ASRunner() {
    override fun run() { /* UI 更新 */ }
}.runInMainThread()

// 背景執行
ASCoroutine.runInNewCoroutine { /* 背景工作 */ }

// 延遲執行
runner.postDelayed(3000)
runner.cancel()
```

---

## 📝 命名慣例

- **AS*** = asFramework 模組的類別前綴（AS = Application Structure）
- **Telnet*** = telnet/telnetUI 模組的類別前綴
- **Bahamut*** = Bahamut 業務邏輯類別前綴
- **Dialog*** = 對話框類別
- ***Page = 頁面類別
- ***Command = 命令類別
- ***Listener = 監聽器接口

---

## 🎯 開發指南快速參考

1. **修改 UI** → 查看 `asFramework/ui/` 或 `asFramework/pageController/`
2. **新增 BBS 功能** → 查看 `Bahamut/command/` 和 `Bahamut/pages/`
3. **修改文章顯示** → 查看 `telnetUI/` 和 `telnet/`
4. **處理編碼問題** → 查看 `textEncoder/`
5. **修改對話框** → 查看 `asFramework/dialog/` 或 `Bahamut/dialogs/`
6. **新增設定項** → 查看 `Bahamut/service/UserSettings.kt`
7. **修改列表載入** → 查看 `Bahamut/listPage/TelnetListPage.kt`

---

**最後更新**: 2025-12-11  
**Kotlin 版本**: 2.1.0  
**Android SDK**: 36 (minSdk 26)
