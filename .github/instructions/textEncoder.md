# textEncoder - 編碼轉換層

**applyto**: `app/src/main/java/com/kota/textEncoder/**/*.kt`

## 📋 模組概述

textEncoder 模組提供 Big5 和 UTF-8 編碼之間的轉換功能，是連接 BBS 伺服器（Big5 編碼）和 Android 應用程式（UTF-8 編碼）的關鍵橋樑。BBS 伺服器使用繁體中文 Big5 編碼，而 Android 內部使用 UTF-8，所有文字資料都需要通過這個模組進行編碼轉換。

**技術棧**: Kotlin, Big5 編碼, UTF-8 編碼  
**設計模式**: 策略模式（雙向轉換）  
**核心功能**: 編碼轉換、字元邊界處理

---

## 📂 模組結構

textEncoder 模組**沒有子資料夾**，所有元件都在根目錄：

```
textEncoder/
├── B2UEncoder.kt            # Big5 → UTF-8 編碼器
├── U2BEncoder.kt            # UTF-8 → Big5 編碼器
├── TextConverterBuffer.kt   # 文字轉換緩衝區
└── copilot-instructions.md
```

---

## 🎯 核心元件

### `B2UEncoder.kt` - Big5 轉 UTF-8 編碼器
將 BBS 伺服器發送的 Big5 編碼文字轉換為 UTF-8 以在 Android 上顯示。

```kotlin
object B2UEncoder {
    fun convert(big5Data: ByteArray): String
    fun convert(big5Data: ByteArray, offset: Int, length: Int): String
}
```

**使用場景**:
- 接收 Telnet 資料時（BBS → App）
- 解析文章內容
- 顯示看板名稱、文章標題
- 處理使用者暱稱

**關鍵特性**:
- 處理 Big5 雙位元組字元
- 正確識別字元邊界
- 處理半形/全形字元
- 錯誤字元處理（替換為 �）

**內部邏輯**:
```kotlin
fun convert(big5Data: ByteArray): String {
    val result = StringBuilder()
    var i = 0
    
    while (i < big5Data.size) {
        val byte = big5Data[i].toInt() and 0xFF
        
        when {
            // ASCII 字元（單位元組）
            byte < 0x80 -> {
                result.append(byte.toChar())
                i++
            }
            
            // Big5 雙位元組字元
            byte in 0x81..0xFE && i + 1 < big5Data.size -> {
                val byte2 = big5Data[i + 1].toInt() and 0xFF
                
                if (isValidBig5SecondByte(byte2)) {
                    // 查表轉換為 Unicode
                    val unicode = big5ToUnicode(byte, byte2)
                    result.append(unicode.toChar())
                    i += 2
                } else {
                    // 無效字元
                    result.append('�')
                    i++
                }
            }
            
            // 無效位元組
            else -> {
                result.append('�')
                i++
            }
        }
    }
    
    return result.toString()
}
```

### `U2BEncoder.kt` - UTF-8 轉 Big5 編碼器
將使用者輸入的 UTF-8 文字轉換為 Big5 編碼以發送到 BBS 伺服器。

```kotlin
object U2BEncoder {
    fun convert(utf8Text: String): ByteArray
    fun convert(utf8Text: String, startIndex: Int, endIndex: Int): ByteArray
}
```

**使用場景**:
- 發送文章內容（App → BBS）
- 發送推文
- 搜尋關鍵字
- 輸入使用者名稱/密碼

**關鍵特性**:
- Unicode 轉 Big5 映射
- 處理無法轉換的字元（替換或移除）
- 優化輸出大小
- 保持字元完整性

**內部邏輯**:
```kotlin
fun convert(utf8Text: String): ByteArray {
    val output = ByteArrayOutputStream()
    
    for (char in utf8Text) {
        when {
            // ASCII 字元（單位元組）
            char.code < 0x80 -> {
                output.write(char.code)
            }
            
            // 中文字元（雙位元組）
            else -> {
                val big5Bytes = unicodeToBig5(char.code)
                
                if (big5Bytes != null) {
                    output.write(big5Bytes[0].toInt())
                    output.write(big5Bytes[1].toInt())
                } else {
                    // 無法轉換，使用 '?' 替代
                    output.write('?'.code)
                }
            }
        }
    }
    
    return output.toByteArray()
}
```

### `TextConverterBuffer.kt` - 文字轉換緩衝區
提供高效的編碼轉換功能，支援流式處理和緩衝管理。

```kotlin
class TextConverterBuffer(initialCapacity: Int = 1024) {
    fun appendBig5(data: ByteArray)          // 追加 Big5 資料
    fun appendUtf8(text: String)              // 追加 UTF-8 文字
    fun toUtf8String(): String                // 轉換為 UTF-8 字串
    fun toBig5ByteArray(): ByteArray          // 轉換為 Big5 位元組陣列
    fun clear()                               // 清空緩衝區
}
```

**使用場景**:
- 處理大量文字資料
- 流式轉換（邊接收邊轉換）
- 批次轉換優化

---

## ⚡ Big5 編碼詳解

### Big5 編碼範圍
Big5 是雙位元組編碼系統：

**第一位元組（高位元組）**:
- `0x81-0xFE` (129-254)

**第二位元組（低位元組）**:
- `0x40-0x7E` (64-126) - 前半區
- `0x80-0xFE` (128-254) - 後半區

**注意**: `0x7F` 不是有效的第二位元組

### 字元類型判斷
```kotlin
fun isAscii(byte: Byte): Boolean {
    return (byte.toInt() and 0xFF) < 0x80
}

fun isBig5LeadByte(byte: Byte): Boolean {
    val b = byte.toInt() and 0xFF
    return b in 0x81..0xFE
}

fun isValidBig5SecondByte(byte: Int): Boolean {
    return (byte in 0x40..0x7E) || (byte in 0x80..0xFE)
}

fun isFullWidth(char: Char): Boolean {
    // 全形字元（中文、全形符號）
    return char.code > 0x7F
}
```

### 常見 Big5 字元範圍
| 範圍 | 內容 |
|------|------|
| `0x20-0x7E` | ASCII 字元 |
| `0xA140-0xA3BF` | 標點符號、特殊符號 |
| `0xA440-0xC67E` | 常用中文字（一級字） |
| `0xC940-0xF9D5` | 次常用中文字（二級字） |

---

## 🔧 使用模式

### 1. 基本轉換
```kotlin
// Big5 → UTF-8（接收 BBS 資料）
val big5Data: ByteArray = receivedFromBBS()
val utf8Text = B2UEncoder.convert(big5Data)
textView.text = utf8Text

// UTF-8 → Big5（發送到 BBS）
val utf8Text = editText.text.toString()
val big5Data = U2BEncoder.convert(utf8Text)
sendToBBS(big5Data)
```

### 2. 部分轉換
```kotlin
// 轉換部分資料
val big5Data = byteArrayOf(...)
val utf8Text = B2UEncoder.convert(big5Data, offset = 10, length = 50)

// 轉換部分文字
val utf8Text = "Hello 世界 World"
val big5Data = U2BEncoder.convert(utf8Text, startIndex = 6, endIndex = 8)  // 只轉換 "世界"
```

### 3. 使用緩衝區處理大量資料
```kotlin
class ArticleProcessor {
    private val buffer = TextConverterBuffer(4096)
    
    fun processChunk(chunk: ByteArray) {
        // 累積 Big5 資料
        buffer.appendBig5(chunk)
        
        // 檢查是否有完整行
        if (hasCompleteLine()) {
            val line = buffer.toUtf8String()
            displayLine(line)
            buffer.clear()
        }
    }
}
```

### 4. Telnet 資料接收處理
```kotlin
class TelnetDataHandler {
    fun onDataReceived(data: ByteArray) {
        // 直接轉換為 UTF-8
        val text = B2UEncoder.convert(data)
        
        // 更新終端機畫面
        updateTerminal(text)
    }
}
```

### 5. 使用者輸入處理
```kotlin
class InputHandler {
    fun sendMessage(message: String) {
        // 檢查是否可轉換為 Big5
        val big5Data = try {
            U2BEncoder.convert(message)
        } catch (e: Exception) {
            // 包含無法轉換的字元
            showError("包含不支援的字元")
            return
        }
        
        // 發送到 BBS
        TelnetOutputBuilder.create()
            .pushBytes(big5Data)
            .sendToServer()
    }
}
```

---

## 🐛 常見問題和解決方案

### 問題 1: 亂碼（半個中文字）
**原因**: Big5 雙位元組字元被截斷

**解決**:
```kotlin
class SafeBig5Processor {
    private val pendingByte: Byte? = null
    
    fun process(chunk: ByteArray): String {
        val buffer = ByteArrayOutputStream()
        
        // 處理上次剩餘的位元組
        if (pendingByte != null) {
            buffer.write(pendingByte)
        }
        
        buffer.write(chunk)
        val data = buffer.toByteArray()
        
        // 檢查最後一個位元組是否為 Big5 首位元組
        if (data.isNotEmpty() && isBig5LeadByte(data.last())) {
            // 保留到下次處理
            pendingByte = data.last()
            return B2UEncoder.convert(data, 0, data.size - 1)
        } else {
            pendingByte = null
            return B2UEncoder.convert(data)
        }
    }
}
```

### 問題 2: 無法轉換的字元
**原因**: UTF-8 字元不在 Big5 字元集中（如日文、韓文、Emoji）

**解決**:
```kotlin
fun convertWithFallback(text: String): ByteArray {
    val output = ByteArrayOutputStream()
    
    for (char in text) {
        val big5Bytes = unicodeToBig5(char.code)
        
        if (big5Bytes != null) {
            output.write(big5Bytes)
        } else {
            // 無法轉換，使用替代策略
            when {
                char.isDigit() || char.isLetter() -> {
                    // 字母數字保留
                    output.write(char.code)
                }
                else -> {
                    // 其他字元替換為 '?'
                    output.write('?'.code)
                }
            }
        }
    }
    
    return output.toByteArray()
}
```

### 問題 3: 效能問題
**原因**: 頻繁的小量轉換

**解決**:
```kotlin
// ❌ 效能差：逐字元轉換
for (char in text) {
    val big5 = U2BEncoder.convert(char.toString())
    send(big5)
}

// ✅ 效能好：批次轉換
val big5Data = U2BEncoder.convert(text)
send(big5Data)
```

### 問題 4: 記憶體洩漏
**原因**: 轉換緩衝區未釋放

**解決**:
```kotlin
class ArticleLoader {
    private val buffer = TextConverterBuffer()
    
    fun load(data: ByteArray): String {
        buffer.clear()  // 重用前清空
        buffer.appendBig5(data)
        return buffer.toUtf8String()
    }
    
    fun cleanup() {
        buffer.clear()  // 釋放資源
    }
}
```

---

## 📝 開發規範

### 編碼轉換原則
1. **接收資料**: 總是使用 `B2UEncoder` 轉換為 UTF-8
2. **發送資料**: 總是使用 `U2BEncoder` 轉換為 Big5
3. **字元邊界**: 注意處理 Big5 雙位元組字元的完整性
4. **錯誤處理**: 優雅處理無法轉換的字元

### 效能優化
1. **批次轉換**: 避免逐字元轉換
2. **重用緩衝區**: 使用 `TextConverterBuffer` 處理大量資料
3. **預分配**: 根據資料大小預分配緩衝區
4. **快取映射表**: 快取常用的 Unicode ↔ Big5 映射

### 測試要點
```kotlin
// 測試 ASCII
assertEquals("Hello", B2UEncoder.convert("Hello".toByteArray(Charset.forName("Big5"))))

// 測試中文
val big5Text = "你好世界".toByteArray(Charset.forName("Big5"))
assertEquals("你好世界", B2UEncoder.convert(big5Text))

// 測試混合
val mixed = "Hello世界123".toByteArray(Charset.forName("Big5"))
assertEquals("Hello世界123", B2UEncoder.convert(mixed))

// 測試邊界
val incomplete = byteArrayOf(0xA4.toByte())  // Big5 首位元組但無第二位元組
// 應該優雅處理，不崩潰
```

---

## 🔗 與其他模組的關係

```
textEncoder (編碼轉換)
    ↓ 被使用於
    ├── telnet (Telnet 客戶端)
    │   ├── TelnetReceiver (接收資料：Big5 → UTF-8)
    │   └── TelnetOutputBuilder (發送資料：UTF-8 → Big5)
    ├── Bahamut (業務邏輯)
    │   ├── 文章顯示（Big5 → UTF-8）
    │   └── 文章發送（UTF-8 → Big5）
    └── dataPool (資料緩衝)
        └── TextConverterBuffer 使用 MutableByteBuffer
```

---

## 📚 延伸閱讀

- [Big5 編碼標準](https://zh.wikipedia.org/wiki/Big5)
- [UTF-8 編碼規範](https://zh.wikipedia.org/wiki/UTF-8)
- [字元編碼對照表](http://www.unicode.org/Public/MAPPINGS/OBSOLETE/EASTASIA/OTHER/BIG5.TXT)
- [telnet 模組](.github/instructions/telnet.md)
- [dataPool 模組](.github/instructions/dataPool.md)

---

**維護者**: Bahamut BBS 開發團隊  
**最後更新**: 2025-12-11
