# telnet/reference - 參考定義與常數

**applyto**: `app/src/main/java/com/kota/telnet/reference/**/*.kt`

## 📋 模組概述

reference 模組提供 Telnet 相關的常數定義、按鍵代碼、ANSI 色碼等參考資料。

**技術棧**: Kotlin, Constants  
**設計模式**: Constant Object Pattern

---

## 📂 主要元件

### 1️⃣ `TelnetKeyboard.kt` - 鍵盤按鍵常數

```kotlin
object TelnetKeyboard {
    
    // 控制字元
    const val CTRL_A = "\u0001"
    const val CTRL_B = "\u0002"
    const val CTRL_C = "\u0003"
    const val CTRL_D = "\u0004"
    const val CTRL_E = "\u0005"
    const val CTRL_F = "\u0006"
    const val CTRL_G = "\u0007"
    const val CTRL_H = "\u0008"
    const val CTRL_I = "\u0009"  // Tab
    const val CTRL_J = "\u000A"  // Line Feed
    const val CTRL_K = "\u000B"
    const val CTRL_L = "\u000C"
    const val CTRL_M = "\u000D"  // Enter
    const val CTRL_N = "\u000E"
    const val CTRL_O = "\u000F"
    const val CTRL_P = "\u0010"
    const val CTRL_Q = "\u0011"
    const val CTRL_R = "\u0012"
    const val CTRL_S = "\u0013"
    const val CTRL_T = "\u0014"
    const val CTRL_U = "\u0015"
    const val CTRL_V = "\u0016"
    const val CTRL_W = "\u0017"
    const val CTRL_X = "\u0018"
    const val CTRL_Y = "\u0019"
    const val CTRL_Z = "\u001A"
    
    // 方向鍵
    const val KEY_UP = "\u001B[A"
    const val KEY_DOWN = "\u001B[B"
    const val KEY_RIGHT = "\u001B[C"
    const val KEY_LEFT = "\u001B[D"
    
    // 功能鍵
    const val KEY_HOME = "\u001B[H"
    const val KEY_END = "\u001B[F"
    const val KEY_PAGE_UP = "\u001B[5~"
    const val KEY_PAGE_DOWN = "\u001B[6~"
    const val KEY_INSERT = "\u001B[2~"
    const val KEY_DELETE = "\u001B[3~"
    
    // 特殊鍵
    const val KEY_ENTER = "\r"
    const val KEY_BACKSPACE = "\u0008"
    const val KEY_TAB = "\t"
    const val KEY_ESC = "\u001B"
    const val KEY_SPACE = " "
}

/**
 * Telnet 輸出建構器
 */
class TelnetOutputBuilder {
    
    private val buffer = StringBuilder()
    
    fun pushKey(key: String): TelnetOutputBuilder {
        buffer.append(key)
        return this
    }
    
    fun pushString(text: String): TelnetOutputBuilder {
        buffer.append(text)
        return this
    }
    
    fun pushEnter(): TelnetOutputBuilder {
        buffer.append(TelnetKeyboard.KEY_ENTER)
        return this
    }
    
    fun sendToServer() {
        TelnetClient.getInstance().send(buffer.toString())
        buffer.clear()
    }
    
    companion object {
        fun create() = TelnetOutputBuilder()
    }
}
```

**使用範例**:
```kotlin
// 發送按鍵序列
TelnetOutputBuilder.create()
    .pushKey(TelnetKeyboard.CTRL_Z)    // 跳轉命令
    .pushString("100")                  // 輸入編號
    .pushEnter()                        // 確認
    .sendToServer()

// 發送方向鍵
TelnetOutputBuilder.create()
    .pushKey(TelnetKeyboard.KEY_DOWN)
    .pushKey(TelnetKeyboard.KEY_DOWN)
    .sendToServer()
```

---

### 2️⃣ `TelnetColor.kt` - ANSI 色碼常數

```kotlin
object TelnetColor {
    
    // 前景色 (30-37)
    const val FG_BLACK = 30
    const val FG_RED = 31
    const val FG_GREEN = 32
    const val FG_YELLOW = 33
    const val FG_BLUE = 34
    const val FG_MAGENTA = 35
    const val FG_CYAN = 36
    const val FG_WHITE = 37
    
    // 背景色 (40-47)
    const val BG_BLACK = 40
    const val BG_RED = 41
    const val BG_GREEN = 42
    const val BG_YELLOW = 43
    const val BG_BLUE = 44
    const val BG_MAGENTA = 45
    const val BG_CYAN = 46
    const val BG_WHITE = 47
    
    // 樣式
    const val RESET = 0
    const val BOLD = 1
    const val DIM = 2
    const val ITALIC = 3
    const val UNDERLINE = 4
    const val BLINK = 5
    const val REVERSE = 7
    const val HIDDEN = 8
    
    /**
     * 轉換為 Android Color
     */
    fun ansiToAndroidColor(ansiCode: Int): Int {
        return when (ansiCode) {
            FG_BLACK -> Color.BLACK
            FG_RED -> Color.RED
            FG_GREEN -> Color.GREEN
            FG_YELLOW -> Color.YELLOW
            FG_BLUE -> Color.BLUE
            FG_MAGENTA -> Color.MAGENTA
            FG_CYAN -> Color.CYAN
            FG_WHITE -> Color.WHITE
            else -> Color.WHITE
        }
    }
}
```

---

### 3️⃣ `TelnetCommand.kt` - 命令常數

```kotlin
object TelnetCommandCode {
    
    // BBS 常用命令
    const val CMD_READ = "r"           // 閱讀
    const val CMD_POST = "^P"          // 發文
    const val CMD_PUSH = "推"           // 推文
    const val CMD_SEARCH = "/"         // 搜尋
    const val CMD_QUIT = "q"           // 離開
    const val CMD_HELP = "h"           // 說明
    const val CMD_MAIL = "m"           // 寄信
    const val CMD_GOOD = "g"           // 標記好文
    const val CMD_DELETE = "d"         // 刪除
    const val CMD_EDIT = "E"           // 編輯
    
    // 看板操作
    const val CMD_BOARD_LIST = "s"     // 看板列表
    const val CMD_CLASS_LIST = "c"     // 分類看板
    const val CMD_FAVORITE = "f"       // 我的最愛
    
    // 導航命令
    const val CMD_SAME_TITLE_PREV = "="  // 同標題上一篇
    const val CMD_SAME_TITLE_NEXT = "]"  // 同標題下一篇
    const val CMD_THREAD_PREV = "["      // 主題上一篇
    const val CMD_THREAD_NEXT = "]"      // 主題下一篇
}
```

---

### 4️⃣ `TelnetState.kt` - 狀態常數

```kotlin
enum class TelnetState {
    DISCONNECTED,      // 未連接
    CONNECTING,        // 連接中
    CONNECTED,         // 已連接
    LOGIN,             // 登入畫面
    MAIN_MENU,         // 主選單
    BOARD_LIST,        // 看板列表
    BOARD_MAIN,        // 看板主頁
    ARTICLE,           // 文章內容
    MAIL_LIST,         // 信件列表
    MAIL_CONTENT,      // 信件內容
    POSTING,           // 發文中
    EDITING,           // 編輯中
    ERROR              // 錯誤狀態
}
```

---

## 🎯 使用範例

### 發送 BBS 命令

```kotlin
// 進入看板
TelnetOutputBuilder.create()
    .pushString("s")                    // 搜尋看板命令
    .pushEnter()
    .pushString("C_Chat")               // 看板名稱
    .pushEnter()
    .sendToServer()

// 閱讀文章
TelnetOutputBuilder.create()
    .pushString(TelnetCommandCode.CMD_READ)
    .pushEnter()
    .sendToServer()

// 推文
TelnetOutputBuilder.create()
    .pushString(TelnetCommandCode.CMD_PUSH)
    .pushEnter()
    .pushString("1")                    // 1=推, 2=噓, 3=→
    .pushEnter()
    .pushString("推推！")
    .pushEnter()
    .sendToServer()
```

---

## 📚 相關模組

- [telnet-model](telnet-model.md) - Telnet 資料模型
- [telnet-logic](telnet-logic.md) - 業務邏輯
- [Bahamut-command](Bahamut-command.md) - 使用這些常數

---

## 📝 技術特點總結

1. **常數集中管理**: 統一維護按鍵和命令
2. **類型安全**: 使用 object 和 enum
3. **建構器模式**: TelnetOutputBuilder 簡化命令發送
4. **ANSI 支援**: 完整的色碼定義
5. **BBS 命令**: 封裝常用 BBS 操作
