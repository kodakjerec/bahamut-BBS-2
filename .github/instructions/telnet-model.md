# telnet/model - Telnet 資料模型

**applyto**: `app/src/main/java/com/kota/telnet/model/**/*.kt`, `app/src/main/java/com/kota/telnet/model/**/*.java`

## 📋 模組概述

model 模組提供 Telnet 連線和螢幕資料的核心模型,包含 Telnet 客戶端、螢幕緩衝區、ANSI 色碼處理等。

**技術棧**: Kotlin + Java, Socket, ANSI Escape Codes  
**設計模式**: 單例模式, 觀察者模式

---

## 📂 主要元件

### 1️⃣ `TelnetClient.kt` - Telnet 客戶端

```kotlin
class TelnetClient private constructor() {
    
    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    var listener: TelnetClientListener? = null
    
    interface TelnetClientListener {
        fun onConnected()
        fun onDisconnected()
        fun onDataReceived(data: ByteArray)
        fun onError(error: Exception)
    }
    
    /**
     * 連接到伺服器
     */
    fun connect(host: String, port: Int) {
        ASCoroutine.runInNewCoroutine {
            try {
                socket = Socket(host, port)
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                
                listener?.onConnected()
                
                // 開始接收資料
                startReceiving()
            } catch (e: Exception) {
                listener?.onError(e)
            }
        }
    }
    
    /**
     * 發送資料
     */
    fun send(data: ByteArray) {
        try {
            outputStream?.write(data)
            outputStream?.flush()
        } catch (e: Exception) {
            listener?.onError(e)
        }
    }
    
    /**
     * 發送字串
     */
    fun send(text: String) {
        send(text.toByteArray())
    }
    
    /**
     * 斷開連線
     */
    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
            
            listener?.onDisconnected()
        } catch (e: Exception) {
            listener?.onError(e)
        }
    }
    
    /**
     * 接收資料迴圈
     */
    private fun startReceiving() {
        ASCoroutine.runInNewCoroutine {
            val buffer = ByteArray(1024)
            
            try {
                while (true) {
                    val bytesRead = inputStream?.read(buffer) ?: -1
                    if (bytesRead == -1) break
                    
                    val data = buffer.copyOf(bytesRead)
                    listener?.onDataReceived(data)
                    
                    // 更新螢幕模型
                    TelnetModel.processData(data)
                }
            } catch (e: Exception) {
                listener?.onError(e)
            } finally {
                disconnect()
            }
        }
    }
    
    companion object {
        @Volatile
        private var instance: TelnetClient? = null
        
        fun getInstance(): TelnetClient {
            return instance ?: synchronized(this) {
                instance ?: TelnetClient().also { instance = it }
            }
        }
    }
}
```

---

### 2️⃣ `TelnetModel.kt` - 螢幕資料模型

```kotlin
object TelnetModel {
    
    private const val SCREEN_WIDTH = 80
    private const val SCREEN_HEIGHT = 24
    
    // 螢幕緩衝區
    private val screen = Array(SCREEN_HEIGHT) { CharArray(SCREEN_WIDTH) { ' ' } }
    private val colors = Array(SCREEN_HEIGHT) { IntArray(SCREEN_WIDTH) { 0 } }
    
    // 游標位置
    var cursorX = 0
        private set
    var cursorY = 0
        private set
    
    /**
     * 處理接收的資料
     */
    fun processData(data: ByteArray) {
        val text = String(data, Charset.forName("Big5"))
        
        var i = 0
        while (i < text.length) {
            val char = text[i]
            
            when {
                char == '\u001B' -> {
                    // ANSI escape sequence
                    i = processAnsiSequence(text, i)
                }
                char == '\r' || char == '\n' -> {
                    moveCursorToNextLine()
                }
                else -> {
                    writeChar(char)
                }
            }
            i++
        }
        
        notifyScreenUpdated()
    }
    
    /**
     * 處理 ANSI escape sequence
     */
    private fun processAnsiSequence(text: String, startIndex: Int): Int {
        var i = startIndex + 1
        
        // 檢查 CSI (Control Sequence Introducer)
        if (i < text.length && text[i] == '[') {
            i++
            
            // 讀取參數
            val params = StringBuilder()
            while (i < text.length && text[i].isDigit() || text[i] == ';') {
                params.append(text[i])
                i++
            }
            
            // 讀取命令
            if (i < text.length) {
                val command = text[i]
                executeAnsiCommand(command, params.toString())
            }
        }
        
        return i
    }
    
    /**
     * 執行 ANSI 命令
     */
    private fun executeAnsiCommand(command: Char, params: String) {
        when (command) {
            'H', 'f' -> {
                // 移動游標
                val parts = params.split(';')
                cursorY = parts.getOrNull(0)?.toIntOrNull()?.minus(1) ?: 0
                cursorX = parts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: 0
            }
            'A' -> {
                // 向上移動
                val n = params.toIntOrNull() ?: 1
                cursorY = max(0, cursorY - n)
            }
            'B' -> {
                // 向下移動
                val n = params.toIntOrNull() ?: 1
                cursorY = min(SCREEN_HEIGHT - 1, cursorY + n)
            }
            'C' -> {
                // 向右移動
                val n = params.toIntOrNull() ?: 1
                cursorX = min(SCREEN_WIDTH - 1, cursorX + n)
            }
            'D' -> {
                // 向左移動
                val n = params.toIntOrNull() ?: 1
                cursorX = max(0, cursorX - n)
            }
            'J' -> {
                // 清除螢幕
                clearScreen()
            }
            'm' -> {
                // 設定顏色/樣式
                // 處理顏色代碼
            }
        }
    }
    
    /**
     * 寫入字元
     */
    private fun writeChar(char: Char) {
        if (cursorY < SCREEN_HEIGHT && cursorX < SCREEN_WIDTH) {
            screen[cursorY][cursorX] = char
            cursorX++
            
            if (cursorX >= SCREEN_WIDTH) {
                moveCursorToNextLine()
            }
        }
    }
    
    /**
     * 移到下一行
     */
    private fun moveCursorToNextLine() {
        cursorX = 0
        cursorY++
        
        if (cursorY >= SCREEN_HEIGHT) {
            // 捲動螢幕
            scrollScreen()
            cursorY = SCREEN_HEIGHT - 1
        }
    }
    
    /**
     * 捲動螢幕
     */
    private fun scrollScreen() {
        for (y in 1 until SCREEN_HEIGHT) {
            screen[y - 1] = screen[y].copyOf()
            colors[y - 1] = colors[y].copyOf()
        }
        screen[SCREEN_HEIGHT - 1] = CharArray(SCREEN_WIDTH) { ' ' }
        colors[SCREEN_HEIGHT - 1] = IntArray(SCREEN_WIDTH) { 0 }
    }
    
    /**
     * 清除螢幕
     */
    fun clearScreen() {
        for (y in 0 until SCREEN_HEIGHT) {
            screen[y] = CharArray(SCREEN_WIDTH) { ' ' }
            colors[y] = IntArray(SCREEN_WIDTH) { 0 }
        }
        cursorX = 0
        cursorY = 0
    }
    
    /**
     * 獲取螢幕內容
     */
    fun getScreen(): Array<CharArray> {
        return screen.map { it.copyOf() }.toTypedArray()
    }
    
    /**
     * 獲取指定行
     */
    fun getLine(y: Int): String {
        return if (y in 0 until SCREEN_HEIGHT) {
            String(screen[y])
        } else {
            ""
        }
    }
    
    /**
     * 獲取所有行
     */
    fun getScreenLines(): List<String> {
        return (0 until SCREEN_HEIGHT).map { getLine(it) }
    }
    
    private val listeners = mutableListOf<ScreenUpdateListener>()
    
    interface ScreenUpdateListener {
        fun onScreenUpdated()
    }
    
    fun addScreenUpdateListener(listener: ScreenUpdateListener) {
        listeners.add(listener)
    }
    
    private fun notifyScreenUpdated() {
        listeners.forEach { it.onScreenUpdated() }
    }
}
```

---

## 🎯 使用範例

### 連接 Telnet

```kotlin
val client = TelnetClient.getInstance()

client.listener = object : TelnetClient.TelnetClientListener {
    override fun onConnected() {
        Log.d(TAG, "Connected to BBS")
    }
    
    override fun onDisconnected() {
        Log.d(TAG, "Disconnected from BBS")
    }
    
    override fun onDataReceived(data: ByteArray) {
        // TelnetModel 會自動處理
    }
    
    override fun onError(error: Exception) {
        Log.e(TAG, "Error: ${error.message}")
    }
}

client.connect("bbs.gamer.com.tw", 23)
```

### 讀取螢幕內容

```kotlin
// 讀取指定行
val line10 = TelnetModel.getLine(10)

// 讀取所有行
val allLines = TelnetModel.getScreenLines()

// 檢查特定內容
if (TelnetModel.getLine(0).contains("主功能表")) {
    // 在主選單
}
```

---

## 📚 相關模組

- [telnet-logic](telnet-logic.md) - 業務邏輯處理
- [telnet-reference](telnet-reference.md) - 常數定義
- [telnetUI-textView](telnetUI-textView.md) - 螢幕顯示

---

## 📝 技術特點總結

1. **Socket 通訊**: TCP 連接 BBS 伺服器
2. **ANSI 解析**: 處理 ANSI escape sequences
3. **螢幕緩衝**: 80x24 螢幕模擬
4. **Big5 編碼**: 支援中文字元
5. **觀察者模式**: 螢幕更新通知
