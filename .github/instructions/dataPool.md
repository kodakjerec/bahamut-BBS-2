# dataPool - 資料緩衝管理層

**applyto**: `app/src/main/java/com/kota/dataPool/**/*.kt`

## 📋 模組概述

dataPool 模組提供高效的資料緩衝和迭代器功能，專門用於處理位元組資料流。這是一個底層工具模組，為 `telnet` 模組的資料接收和解析提供基礎設施，確保記憶體使用效率和資料處理性能。

**技術棧**: Kotlin  
**設計模式**: 迭代器模式  
**核心功能**: 位元組緩衝管理、資料流遍歷

---

## 📂 模組結構

dataPool 模組**沒有子資料夾**，所有元件都在根目錄：

```
dataPool/
├── ByteIterator.kt         # 位元組迭代器
├── MutableByteBuffer.kt    # 可變位元組緩衝區
└── copilot-instructions.md
```

---

## 🎯 核心元件

### `MutableByteBuffer.kt` - 可變位元組緩衝區
動態管理位元組資料的讀寫操作。

```kotlin
class MutableByteBuffer(initialCapacity: Int = 1024)
```

**功能特性**:
- 動態擴充容量
- 高效的讀寫操作
- 支援批次操作
- 自動記憶體管理

**關鍵方法**:
```kotlin
// 寫入操作
fun writeByte(byte: Byte)                    // 寫入單一位元組
fun writeBytes(bytes: ByteArray)             // 寫入位元組陣列
fun writeBytes(bytes: ByteArray, offset: Int, length: Int)  // 寫入部分資料

// 讀取操作
fun readByte(): Byte                         // 讀取單一位元組
fun readBytes(length: Int): ByteArray        // 讀取指定長度
fun peek(): Byte                             // 查看但不移除
fun peekBytes(length: Int): ByteArray        // 查看多個位元組

// 狀態查詢
fun available(): Int                         // 可讀取的位元組數
fun capacity(): Int                          // 緩衝區容量
fun isEmpty(): Boolean                       // 是否為空
fun isFull(): Boolean                        // 是否已滿

// 管理操作
fun clear()                                  // 清空緩衝區
fun compact()                                // 壓縮緩衝區（移除已讀資料）
fun ensureCapacity(minCapacity: Int)        // 確保容量足夠
```

**內部結構**:
```kotlin
class MutableByteBuffer(initialCapacity: Int = 1024) {
    private var buffer: ByteArray              // 內部緩衝區
    private var readPosition: Int = 0          // 讀取位置
    private var writePosition: Int = 0         // 寫入位置
    private var capacity: Int = initialCapacity // 當前容量
    
    // 動態擴充邏輯
    private fun expandCapacity(minCapacity: Int) {
        val newCapacity = maxOf(capacity * 2, minCapacity)
        val newBuffer = ByteArray(newCapacity)
        System.arraycopy(buffer, 0, newBuffer, 0, writePosition)
        buffer = newBuffer
        capacity = newCapacity
    }
}
```

### `ByteIterator.kt` - 位元組迭代器
提供便捷的位元組資料遍歷功能。

```kotlin
class ByteIterator(private val data: ByteArray)
```

**功能特性**:
- 順序遍歷位元組資料
- 支援向前查看（lookahead）
- 位置追蹤和重置
- 邊界檢查

**關鍵方法**:
```kotlin
// 遍歷操作
fun hasNext(): Boolean                       // 是否有下一個位元組
fun next(): Byte                             // 取得下一個位元組
fun peek(): Byte                             // 查看下一個位元組（不移動）
fun peek(offset: Int): Byte                  // 查看前方第 n 個位元組

// 位置控制
fun position(): Int                          // 取得當前位置
fun setPosition(pos: Int)                    // 設定位置
fun reset()                                  // 重置到起始位置
fun skip(count: Int)                         // 跳過 n 個位元組

// 批次讀取
fun readBytes(length: Int): ByteArray        // 讀取多個位元組
fun readUntil(delimiter: Byte): ByteArray    // 讀取直到指定位元組
fun remaining(): Int                         // 剩餘位元組數
```

**使用範例**:
```kotlin
val data = byteArrayOf(0x1B, 0x5B, 0x33, 0x31, 0x6D, 0x48, 0x69)
val iterator = ByteIterator(data)

while (iterator.hasNext()) {
    val byte = iterator.next()
    
    // ANSI 轉義序列偵測
    if (byte == 0x1B.toByte() && iterator.peek() == 0x5B.toByte()) {
        // 處理 ESC[
        iterator.next()  // 跳過 [
        val sequence = iterator.readUntil('m'.code.toByte())
        processAnsiSequence(sequence)
    } else {
        // 一般字元
        processChar(byte)
    }
}
```

---

## ⚡ 使用場景

### 1. Telnet 資料接收
處理從 BBS 伺服器接收的資料流：

```kotlin
class TelnetReceiver {
    private val buffer = MutableByteBuffer(4096)
    
    fun onDataReceived(data: ByteArray) {
        // 將接收的資料寫入緩衝區
        buffer.writeBytes(data)
        
        // 處理完整的資料
        while (buffer.available() >= getMinimumDataSize()) {
            val packet = buffer.readBytes(getPacketSize())
            processPacket(packet)
        }
    }
    
    fun processPacket(packet: ByteArray) {
        val iterator = ByteIterator(packet)
        // 逐位元組解析
        while (iterator.hasNext()) {
            parseByte(iterator)
        }
    }
}
```

### 2. ANSI 序列解析
解析 ANSI 轉義序列：

```kotlin
fun parseAnsiSequence(iterator: ByteIterator): AnsiCode {
    // 檢查 ESC
    if (iterator.next() != 0x1B.toByte()) {
        throw IllegalArgumentException("Not an ANSI sequence")
    }
    
    // 檢查 [
    if (iterator.next() != '['.code.toByte()) {
        throw IllegalArgumentException("Invalid ANSI sequence")
    }
    
    // 讀取參數直到字母結尾
    val params = mutableListOf<Int>()
    val paramBytes = iterator.readUntil { byte ->
        byte in 'A'.code.toByte()..'z'.code.toByte()
    }
    
    // 解析參數
    // ...
    
    return AnsiCode(params)
}
```

### 3. 資料緩衝管理
管理大量資料的讀寫：

```kotlin
class DataManager {
    private val receiveBuffer = MutableByteBuffer(8192)
    private val sendBuffer = MutableByteBuffer(4096)
    
    fun queueData(data: ByteArray) {
        if (sendBuffer.available() + data.size > sendBuffer.capacity()) {
            sendBuffer.compact()  // 壓縮已發送的資料
        }
        sendBuffer.writeBytes(data)
    }
    
    fun flushData(): ByteArray {
        val data = sendBuffer.readBytes(sendBuffer.available())
        sendBuffer.clear()
        return data
    }
}
```

### 4. 分包處理
處理不完整的資料包：

```kotlin
class PacketProcessor {
    private val buffer = MutableByteBuffer()
    
    fun process(chunk: ByteArray) {
        buffer.writeBytes(chunk)
        
        // 持續處理完整的資料包
        while (hasCompletePacket()) {
            val packet = extractPacket()
            handlePacket(packet)
        }
        
        // 保留不完整的資料等待下次
    }
    
    private fun hasCompletePacket(): Boolean {
        if (buffer.available() < HEADER_SIZE) return false
        
        // 讀取封包長度（不移除資料）
        val lengthBytes = buffer.peekBytes(HEADER_SIZE)
        val packetLength = ByteBuffer.wrap(lengthBytes).int
        
        return buffer.available() >= packetLength
    }
}
```

---

## 🔧 效能優化

### 1. 容量預分配
根據使用場景預分配足夠的容量：

```kotlin
// 接收大量資料
val largeBuffer = MutableByteBuffer(initialCapacity = 65536)

// 小量資料
val smallBuffer = MutableByteBuffer(initialCapacity = 512)
```

### 2. 定期壓縮
避免緩衝區無限增長：

```kotlin
fun maintainBuffer(buffer: MutableByteBuffer) {
    // 如果已讀資料佔用超過 50%，進行壓縮
    if (buffer.readPosition() > buffer.capacity() / 2) {
        buffer.compact()
    }
}
```

### 3. 批次操作
使用批次操作而非逐位元組操作：

```kotlin
// ❌ 效能差
for (byte in data) {
    buffer.writeByte(byte)
}

// ✅ 效能好
buffer.writeBytes(data)
```

### 4. 重用物件
重用 ByteIterator 和 MutableByteBuffer：

```kotlin
class DataProcessor {
    private val buffer = MutableByteBuffer(4096)  // 重用
    
    fun process(data: ByteArray) {
        buffer.clear()  // 清空重用
        buffer.writeBytes(data)
        // 處理...
    }
}
```

---

## 🐛 常見問題和解決方案

### 問題 1: 緩衝區溢位
**原因**: 寫入資料超過容量且未自動擴充

**解決**:
```kotlin
// 自動擴充（預設行為）
buffer.writeBytes(largeData)  // 自動擴充

// 手動檢查
if (buffer.available() + newData.size > buffer.capacity()) {
    buffer.ensureCapacity(buffer.capacity() + newData.size)
}
buffer.writeBytes(newData)
```

### 問題 2: 記憶體洩漏
**原因**: 緩衝區未清理

**解決**:
```kotlin
class MyComponent {
    private val buffer = MutableByteBuffer()
    
    fun cleanup() {
        buffer.clear()  // 釋放內部資料
    }
    
    override fun onDestroy() {
        cleanup()
    }
}
```

### 問題 3: 迭代器越界
**原因**: 未檢查 hasNext()

**解決**:
```kotlin
// ❌ 錯誤
while (true) {
    val byte = iterator.next()  // 可能越界
}

// ✅ 正確
while (iterator.hasNext()) {
    val byte = iterator.next()
}
```

### 問題 4: 資料不完整
**原因**: 資料包跨越多次接收

**解決**:
```kotlin
private val pendingData = MutableByteBuffer()

fun onDataReceived(chunk: ByteArray) {
    pendingData.writeBytes(chunk)
    
    // 等待完整資料
    while (pendingData.available() >= expectedSize) {
        val packet = pendingData.readBytes(expectedSize)
        process(packet)
    }
    
    // 剩餘資料保留到下次
}
```

---

## 📝 開發規範

### 緩衝區管理原則
1. 根據使用場景選擇適當的初始容量
2. 定期壓縮緩衝區避免記憶體浪費
3. 處理完資料後及時清理
4. 避免在多執行緒間共用緩衝區（非執行緒安全）

### 迭代器使用原則
1. 總是使用 `hasNext()` 檢查邊界
2. 需要向前查看時使用 `peek()`
3. 批次讀取優於逐位元組讀取
4. 完成後重置位置以便重用

### 執行緒安全
dataPool 元件**不是執行緒安全**的，多執行緒使用需要外部同步：

```kotlin
class ThreadSafeBuffer {
    private val buffer = MutableByteBuffer()
    private val lock = ReentrantLock()
    
    fun writeBytes(data: ByteArray) {
        lock.withLock {
            buffer.writeBytes(data)
        }
    }
    
    fun readBytes(length: Int): ByteArray {
        return lock.withLock {
            buffer.readBytes(length)
        }
    }
}
```

---

## 🔗 與其他模組的關係

```
dataPool (資料緩衝)
    ↓ 被使用於
    ├── telnet (Telnet 客戶端 - TelnetReceiver 資料接收)
    └── textEncoder (編碼轉換 - TextConverterBuffer)
```

**依賴方向**: dataPool 不依賴任何其他模組，是純工具層

---

## 📚 延伸閱讀

- [telnet 模組](.github/instructions/telnet.md)
- [textEncoder 模組](.github/instructions/textEncoder.md)
- [Java ByteBuffer 文件](https://docs.oracle.com/javase/8/docs/api/java/nio/ByteBuffer.html)

---

**維護者**: Bahamut BBS 開發團隊  
**最後更新**: 2025-12-11
