# asFramework/utils - 工具類別

**applyto**: `app/src/main/java/com/kota/asFramework/utils/**/*.kt`

## 📋 模組概述

utils 模組提供通用工具類別,包括串流處理、型別轉換等輔助功能。

**技術棧**: Kotlin, Java I/O  
**命名前綴**: AS (Application Structure)

---

## 📂 主要類別

### 1️⃣ `ASStreamReader.kt` - 串流讀取器

```kotlin
class ASStreamReader(private val inputStream: InputStream) {
    
    fun readLine(): String? {
        val builder = StringBuilder()
        var byte: Int
        
        while (inputStream.read().also { byte = it } != -1) {
            val char = byte.toChar()
            if (char == '\n') break
            if (char != '\r') builder.append(char)
        }
        
        return if (builder.isEmpty() && byte == -1) null else builder.toString()
    }
    
    fun readAll(): String {
        return inputStream.bufferedReader().use { it.readText() }
    }
    
    fun close() {
        inputStream.close()
    }
}
```

---

### 2️⃣ `ASStreamWriter.kt` - 串流寫入器

```kotlin
class ASStreamWriter(private val outputStream: OutputStream) {
    
    fun writeLine(text: String) {
        outputStream.write((text + "\n").toByteArray())
        outputStream.flush()
    }
    
    fun write(text: String) {
        outputStream.write(text.toByteArray())
        outputStream.flush()
    }
    
    fun close() {
        outputStream.close()
    }
}
```

---

### 3️⃣ `ASTypeConvertor.kt` - 型別轉換器

```kotlin
object ASTypeConvertor {
    
    /**
     * String 轉 Int (安全)
     */
    fun stringToInt(str: String?, defaultValue: Int = 0): Int {
        return try {
            str?.toIntOrNull() ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    /**
     * String 轉 Long (安全)
     */
    fun stringToLong(str: String?, defaultValue: Long = 0L): Long {
        return try {
            str?.toLongOrNull() ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    /**
     * Byte Array 轉 Hex String
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Hex String 轉 Byte Array
     */
    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) +
                          Character.digit(hex[i + 1], 16)).toByte()
        }
        
        return data
    }
}
```

**使用範例**:
```kotlin
// 安全的字串轉數字
val count = ASTypeConvertor.stringToInt(userInput, defaultValue = 10)
val size = ASTypeConvertor.stringToLong(fileSize, defaultValue = 0L)

// Hex 轉換
val hex = ASTypeConvertor.bytesToHex(byteArrayOf(0x1A, 0x2B, 0x3C))
// "1a2b3c"

val bytes = ASTypeConvertor.hexToBytes("1a2b3c")
// [26, 43, 60]
```

---

## 📚 相關模組

- [textEncoder](textEncoder.md) - 文字編碼轉換
- [dataPool](dataPool.md) - 資料池管理

---

## 📝 技術特點總結

1. **安全轉換**: 提供預設值避免異常
2. **串流處理**: 簡化 I/O 操作
3. **通用工具**: 常用功能的封裝
