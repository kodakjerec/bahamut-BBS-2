# telnet - Telnet 客戶端核心層

**applyto**: `app/src/main/java/com/kota/telnet/**/*.kt`

## 📋 模組概述

telnet 模組是整個 BBS 客戶端的通訊核心，實現完整的 Telnet 客戶端功能。支援傳統 Socket 和 WebSocket 兩種連接方式，處理 ANSI 轉義序列，模擬終端機畫面（24 列 x 80 行），提供 BBS 資料的接收、解析和狀態管理。

**技術棧**: Kotlin, Telnet 協定, WebSocket, ANSI 標準  
**設計模式**: 策略模式（Channel）, 觀察者模式（Listener）, 狀態機模式  
**命名前綴**: Telnet

---

## 📂 子模組結構

### 1️⃣ `logic/` - 業務邏輯處理
BBS 特定的資料處理邏輯。

**核心元件**:
- `ArticleHandler` - 文章資料解析和處理
- `SearchBoardHandler` - 看板搜尋邏輯處理
- `ClassMode` / `ClassStep` - 分類模式和步驟定義
- `ItemUtils` - 列表項目工具函式

**功能**: 將原始 Telnet 資料轉換為結構化的業務資料

### 2️⃣ `model/` - Telnet 資料模型
終端機資料結構定義。

**核心元件**:
- `TelnetModel` - **Telnet 主模型**（中央資料存儲）
- `TelnetFrame` - **畫面幀**（終端機顯示內容，24x80）
- `TelnetRow` - 終端機列資料（單行內容）
- `TelnetData` - Telnet 原始資料
- `BitSpaceType` - 位元空間類型

**關鍵**: `TelnetFrame` 代表完整的終端機畫面狀態

### 3️⃣ `reference/` - 參考定義與常數
Telnet 和 ANSI 協定規範。

**核心元件**:
- `TelnetAnsiCode` - **ANSI 轉義碼定義**（色碼、游標控制）
- `TelnetAsciiCode` - ASCII 碼定義
- `TelnetKeyboard` - **鍵盤輸入定義**（方向鍵、功能鍵）
- `TelnetDef` - Telnet 通用定義

**用途**: 提供所有 Telnet 相關的常數和代碼定義

---

## 🎯 核心元件架構

### `TelnetClient.kt` - Telnet 客戶端主類別
整個 Telnet 模組的入口和協調者。

```kotlin
class TelnetClient(private val listener: TelnetClientListener)
```

**職責**:
- 管理 Telnet 連接生命週期
- 協調 Connector、Receiver、StateHandler
- 提供對外 API（連接、斷開、發送資料）
- 分發事件通知

**關鍵方法**:
```kotlin
fun connect(host: String, port: Int, useWebSocket: Boolean)  // 連接
fun disconnect()                                              // 斷開
fun send(data: ByteArray)                                     // 發送資料
fun isConnected(): Boolean                                    // 連接狀態
```

### `TelnetConnector.kt` - 連接管理器
管理實際的網路連接。

```kotlin
class TelnetConnector(private val listener: TelnetConnectorListener)
```

**職責**:
- 建立和管理 `TelnetChannel`
- 處理連接/斷開/重連邏輯
- 管理連接超時

**支援的通道**:
- `TelnetDefaultSocketChannel` - 傳統 Socket（TCP）
- `TelnetWebSocketChannel` - WebSocket

### `TelnetChannel.kt` - 通道介面
連接策略介面（策略模式）。

```kotlin
interface TelnetChannel {
    fun open(host: String, port: Int)
    fun close()
    fun send(data: ByteArray)
    fun setListener(listener: TelnetChannelListener)
}
```

**實作類別**:
- `TelnetDefaultSocketChannel` - 使用 `java.net.Socket`
- `TelnetWebSocketChannel` - 使用 WebSocket 客戶端

### `TelnetReceiver.kt` - 資料接收器
處理從伺服器接收的資料流。

```kotlin
class TelnetReceiver(private val client: TelnetClient)
```

**職責**:
- 接收原始位元組流
- 處理 Telnet 協定指令（IAC, DO, DONT, WILL, WONT）
- 分發資料給 ANSI 解析器

**執行緒**: 在 `TelnetReceiverThread` 中運行

### `TelnetAnsi.kt` - ANSI 解析器
解析 ANSI 轉義序列並更新終端機畫面。

```kotlin
class TelnetAnsi
```

**職責**:
- 解析 ANSI 轉義碼（CSI 序列）
- 處理游標移動指令
- 處理色碼和屬性
- 更新 `TelnetFrame`

**支援的 ANSI 指令**:
- `ESC[nA/B/C/D` - 游標移動（上下左右）
- `ESC[n;mH` - 游標定位
- `ESC[nJ` - 清除螢幕
- `ESC[nK` - 清除行
- `ESC[n;...m` - 設定顯示屬性（色碼）

### `TelnetStateHandler.kt` - 狀態處理器基類
狀態機處理器，由子類別（`BahamutStateHandler`）實作具體邏輯。

```kotlin
abstract class TelnetStateHandler {
    abstract fun handleState()
    protected fun loadState()  // 從 TelnetModel.frame 載入
}
```

**職責**:
- 偵測當前 BBS 畫面類型
- 驅動頁面轉換
- 處理狀態機邏輯

---

## 🔧 資料流程

### 接收資料流程
```
1. BBS 伺服器發送資料
   ↓
2. TelnetChannel 接收原始位元組
   ↓
3. TelnetReceiver 處理 Telnet 協定
   ↓
4. TelnetAnsi 解析 ANSI 轉義序列
   ↓
5. 更新 TelnetModel.frame (24x80 終端機畫面)
   ↓
6. TelnetStateHandler.handleState() 偵測畫面類型
   ↓
7. 通知 TelnetClientListener
   ↓
8. BahamutController 處理（切換頁面或更新 UI）
```

### 發送資料流程
```
1. 使用者操作或命令執行
   ↓
2. TelnetOutputBuilder.create()
   ↓
3. 建構輸出資料（按鍵、字串）
   ↓
4. U2BEncoder 轉換為 Big5 編碼
   ↓
5. TelnetClient.send()
   ↓
6. TelnetChannel.send()
   ↓
7. 發送到 BBS 伺服器
```

---

## ⚡ 關鍵使用模式

### 1. 建立連接
```kotlin
val client = TelnetClient(object : TelnetClientListener {
    override fun onConnected() {
        // 連接成功
    }
    
    override fun onDisconnected() {
        // 連接斷開
    }
    
    override fun onDataReceived(data: ByteArray) {
        // 接收資料
    }
})

// 連接（傳統 Socket）
client.connect("bbs.gamer.com.tw", 23, useWebSocket = false)

// 連接（WebSocket）
client.connect("wss://bbsws.gamer.com.tw", 443, useWebSocket = true)
```

### 2. 發送指令
```kotlin
// 使用 TelnetOutputBuilder
TelnetOutputBuilder.create()
    .pushKey(TelnetKeyboard.ARROW_DOWN)     // 方向鍵下
    .pushKey(TelnetKeyboard.ENTER)          // Enter
    .pushString("Hello World")              // 文字
    .pushKey(TelnetKeyboard.CTRL_P)         // Ctrl+P
    .sendToServer()
```

### 3. 讀取終端機畫面
```kotlin
// 取得當前畫面
val frame = TelnetModel.frame

// 讀取特定行（0-23）
val row = frame.getRow(0)
val text = row.toString()

// 讀取游標位置
val cursorX = TelnetModel.cursorX
val cursorY = TelnetModel.cursorY

// 檢查特定位置的內容
val char = frame.getChar(row = 10, col = 20)
```

### 4. 畫面狀態偵測
```kotlin
// 在 BahamutStateHandler 中
override fun handleState() {
    loadState()  // 載入 TelnetModel.frame
    
    // 偵測畫面類型（根據特定位置的文字）
    val row0 = getRowString(0)  // 第 0 行
    val row23 = getRowString(23) // 第 23 行
    
    when {
        row0.contains("文章選讀") -> {
            // 看板文章列表
            handleBoardMainPage()
        }
        row0.contains("【 精華公佈欄 】") -> {
            // 精華區
            handleEssencePage()
        }
        // ... 其他狀態
    }
}
```

---

## 🔤 ANSI 色碼處理

### 色碼格式
```
ESC[屬性;前景色;背景色m文字
```

### 常用屬性
- `0` - 重置所有屬性
- `1` - 粗體/加亮
- `4` - 底線
- `5` - 閃爍
- `7` - 反相顯示

### 前景色（30-37）
- `30` - 黑色
- `31` - 紅色
- `32` - 綠色
- `33` - 黃色
- `34` - 藍色
- `35` - 洋紅
- `36` - 青色
- `37` - 白色

### 背景色（40-47）
- `40-47` - 對應前景色

### 範例
```
ESC[1;31;40m紅色粗體文字ESC[0m
ESC[32m綠色文字ESC[0m
ESC[1;33;44m黃色粗體藍底ESC[0m
```

---

## 🐛 常見問題和解決方案

### 問題 1: 連接逾時
```kotlin
// 設定連接逾時
val timeout = 10000  // 10 秒
connector.setTimeout(timeout)
```

### 問題 2: 畫面解析錯誤
```kotlin
// 檢查 ANSI 解析狀態
if (!ansi.isReady()) {
    // 等待更多資料
    return
}

// 確保畫面完整
if (frame.isEmpty()) {
    // 畫面尚未初始化
    return
}
```

### 問題 3: Big5 編碼問題
```kotlin
// 使用 textEncoder 模組處理
val utf8Text = B2UEncoder.convert(big5Bytes)  // Big5 → UTF-8
val big5Bytes = U2BEncoder.convert(utf8Text)  // UTF-8 → Big5
```

### 問題 4: 記憶體洩漏
```kotlin
// 斷開連接時清理資源
override fun onDestroy() {
    telnetClient.disconnect()
    telnetClient.setListener(null)
    super.onDestroy()
}
```

---

## 📝 開發規範

### Telnet 命令封裝
建議將所有 Telnet 操作封裝為命令物件（見 `Bahamut/command/`），而非直接使用 `TelnetOutputBuilder`。

### 狀態處理
所有狀態處理邏輯應在 `BahamutStateHandler` 中實作，不要在其他地方直接讀取 `TelnetModel.frame`。

### 執行緒安全
- `TelnetReceiver` 在獨立執行緒運行
- 更新 UI 必須切換到主執行緒（使用 `ASRunner`）
- 讀取 `TelnetModel` 時注意同步

### 錯誤處理
```kotlin
try {
    telnetClient.send(data)
} catch (e: TelnetConnectionClosedException) {
    // 連接已關閉
    Log.e(TAG, "Connection closed", e)
}
```

---

## 🔗 與其他模組的關係

```
telnet (Telnet 客戶端)
    ↓ 使用
    ├── textEncoder (編碼轉換 - B2UEncoder/U2BEncoder)
    ├── dataPool (資料緩衝 - MutableByteBuffer)
    └── 被使用於
        ├── Bahamut (業務邏輯 - TelnetCommand/BahamutStateHandler)
        └── telnetUI (UI 顯示 - TelnetView/TelnetViewDrawer)
```

---

## 📚 延伸閱讀

- [logic 詳細文件](.github/instructions/telnet-logic.md)
- [model 詳細文件](.github/instructions/telnet-model.md)
- [reference 詳細文件](.github/instructions/telnet-reference.md)
- [textEncoder 模組](.github/instructions/textEncoder.md)
- [Telnet 協定規範](https://tools.ietf.org/html/rfc854)
- [ANSI 轉義序列](https://en.wikipedia.org/wiki/ANSI_escape_code)

---

**維護者**: Bahamut BBS 開發團隊  
**最後更新**: 2025-12-11
