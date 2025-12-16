# telnetUI - Telnet UI 元件層

**applyto**: `app/src/main/java/com/kota/telnetUI/**/*.kt`

## 📋 模組概述

telnetUI 模組提供 Telnet 終端機的使用者介面元件，負責將 `telnet` 模組解析的終端機資料（`TelnetFrame`）渲染到 Android 畫面上。支援 ANSI 256 色顯示、多種字體大小、自訂繪製邏輯，提供高效的終端機畫面渲染。

**技術棧**: Kotlin, Android Canvas, Custom View  
**設計模式**: 策略模式（不同字體大小）, 委派模式  
**命名前綴**: Telnet

---

## 📂 子模組結構

### 1️⃣ `textView/` - 文字視圖元件（唯一子模組）
支援多種字體大小的終端機文字顯示元件。

**核心元件**:
- `TelnetTextView.kt` - **文字視圖基類**（抽象類別）
- `TelnetTextViewSmall.kt` - 小字體視圖
- `TelnetTextViewNormal.kt` - 標準字體視圖
- `TelnetTextViewLarge.kt` - 大字體視圖
- `TelnetTextViewUltraLarge.kt` - 超大字體視圖

**設計**: 使用策略模式，根據使用者設定選擇不同的字體大小實作

---

## 🎯 核心元件架構

### `TelnetView.kt` - Telnet 視圖
終端機主視圖，顯示完整的 24x80 終端機畫面。

```kotlin
class TelnetView(context: Context) : View(context)
```

**職責**:
- 顯示終端機畫面（`TelnetFrame`）
- 處理觸控事件
- 管理 `TelnetViewDrawer`
- 處理畫面更新和重繪

**關鍵方法**:
```kotlin
fun setFrame(frame: TelnetFrame)  // 設定要顯示的畫面
fun refresh()                      // 刷新顯示
fun setFontSize(size: Int)        // 設定字體大小
```

### `TelnetViewDrawer.kt` - 視圖繪製器
**最核心的繪製邏輯處理器**，負責將 `TelnetFrame` 繪製到 Canvas 上。

```kotlin
class TelnetViewDrawer
```

**職責**:
- 解析 `TelnetRow` 的 ANSI 色碼
- 繪製背景色塊
- 繪製文字（前景色）
- 處理特殊屬性（粗體、底線、閃爍）
- 繪製游標

**繪製流程**:
```
1. 遍歷 TelnetFrame 的每一行（24 行）
   ↓
2. 遍歷每一行的每個字元（80 個）
   ↓
3. 解析字元的 ANSI 屬性（色碼、粗體等）
   ↓
4. 繪製背景矩形（如果有背景色）
   ↓
5. 繪製文字字元（套用前景色和屬性）
   ↓
6. 繪製游標（如果游標在此位置）
```

**關鍵方法**:
```kotlin
fun draw(canvas: Canvas, frame: TelnetFrame)  // 主繪製方法
private fun drawChar(canvas: Canvas, char: Char, x: Int, y: Int, 
                     foreColor: Int, backColor: Int, attrs: Int)
private fun drawCursor(canvas: Canvas, x: Int, y: Int)
```

### `TelnetPage.kt` - Telnet 頁面
Telnet 頁面基類，整合 `TelnetView` 和頁面邏輯。

```kotlin
abstract class TelnetPage : ASViewController()
```

**職責**:
- 管理 `TelnetView` 的生命週期
- 處理頁面出現/消失事件
- 協調視圖更新

### `TelnetHeaderItemView.kt` - 標題項目視圖
列表標題項目的視圖元件。

```kotlin
class TelnetHeaderItemView(context: Context) : View(context)
```

**用途**: 在文章列表、看板列表等顯示標題欄

### `DividerView.kt` - 分隔線視圖
分隔線元件。

```kotlin
class DividerView(context: Context) : View(context)
```

**用途**: 在 UI 中顯示分隔線

---

## 🎨 字體大小策略

### `TelnetTextView` 基類
抽象基類，定義通用介面：

```kotlin
abstract class TelnetTextView(context: Context) : View(context) {
    abstract val charWidth: Int      // 字元寬度（像素）
    abstract val charHeight: Int     // 字元高度（像素）
    abstract val fontSize: Float     // 字體大小（sp）
    
    abstract fun drawText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint)
}
```

### 字體大小實作

| 類別 | 字體大小 | 適用場景 | 字元尺寸 |
|------|---------|---------|----------|
| `TelnetTextViewSmall` | 10sp | 小螢幕裝置 | 6x12 |
| `TelnetTextViewNormal` | 14sp | 標準螢幕 | 8x16 |
| `TelnetTextViewLarge` | 18sp | 大螢幕或視力輔助 | 10x20 |
| `TelnetTextViewUltraLarge` | 24sp | 超大螢幕或視力輔助 | 14x28 |

### 字體選擇邏輯
```kotlin
fun createTelnetTextView(context: Context, fontSize: Int): TelnetTextView {
    return when (fontSize) {
        1 -> TelnetTextViewSmall(context)
        2 -> TelnetTextViewNormal(context)
        3 -> TelnetTextViewLarge(context)
        4 -> TelnetTextViewUltraLarge(context)
        else -> TelnetTextViewNormal(context)
    }
}
```

---

## ⚡ ANSI 色碼渲染

### 支援的色碼範圍
- **標準 16 色** (0-15): ANSI 基本色
- **256 色模式** (0-255): 擴充色彩
  - 0-15: 標準色
  - 16-231: 216 色調色盤（6x6x6）
  - 232-255: 24 階灰階

### 色碼對應表
```kotlin
// 標準 16 色
private val ANSI_COLORS = intArrayOf(
    0xFF000000.toInt(),  // 0: 黑色
    0xFFAA0000.toInt(),  // 1: 紅色
    0xFF00AA00.toInt(),  // 2: 綠色
    0xFFAAAA00.toInt(),  // 3: 黃色
    0xFF0000AA.toInt(),  // 4: 藍色
    0xFFAA00AA.toInt(),  // 5: 洋紅
    0xFF00AAAA.toInt(),  // 6: 青色
    0xFFAAAAAA.toInt(),  // 7: 白色
    0xFF555555.toInt(),  // 8: 亮黑（灰）
    0xFFFF5555.toInt(),  // 9: 亮紅
    0xFF55FF55.toInt(),  // 10: 亮綠
    0xFFFFFF55.toInt(),  // 11: 亮黃
    0xFF5555FF.toInt(),  // 12: 亮藍
    0xFFFF55FF.toInt(),  // 13: 亮洋紅
    0xFF55FFFF.toInt(),  // 14: 亮青
    0xFFFFFFFF.toInt()   // 15: 亮白
)
```

### 屬性處理
```kotlin
// ANSI 屬性位元
private const val ATTR_BOLD = 0x01       // 粗體
private const val ATTR_UNDERLINE = 0x02  // 底線
private const val ATTR_BLINK = 0x04      // 閃爍
private const val ATTR_REVERSE = 0x08    // 反相

// 檢查屬性
fun isBold(attrs: Int): Boolean = (attrs and ATTR_BOLD) != 0
fun isUnderline(attrs: Int): Boolean = (attrs and ATTR_UNDERLINE) != 0
fun isBlink(attrs: Int): Boolean = (attrs and ATTR_BLINK) != 0
fun isReverse(attrs: Int): Boolean = (attrs and ATTR_REVERSE) != 0
```

---

## 🔧 使用模式

### 1. 建立和配置 TelnetView
```kotlin
// 在頁面中建立
val telnetView = TelnetView(context)
telnetView.setFontSize(UserSettings.fontSize)  // 從設定讀取

// 設定畫面資料
telnetView.setFrame(TelnetModel.frame)

// 刷新顯示
telnetView.refresh()
```

### 2. 處理畫面更新
```kotlin
// 當 Telnet 資料更新時
override fun onTelnetDataReceived() {
    object : ASRunner() {
        override fun run() {
            // 更新 TelnetView
            telnetView.setFrame(TelnetModel.frame)
            telnetView.invalidate()  // 觸發重繪
        }
    }.runInMainThread()
}
```

### 3. 自訂繪製
```kotlin
// 擴充 TelnetViewDrawer
class CustomTelnetViewDrawer : TelnetViewDrawer() {
    override fun draw(canvas: Canvas, frame: TelnetFrame) {
        // 自訂繪製邏輯
        super.draw(canvas, frame)  // 調用基礎繪製
        
        // 繪製額外元素
        drawCustomElements(canvas)
    }
}
```

### 4. 字體大小切換
```kotlin
// 在設定頁面切換字體
fun onFontSizeChanged(newSize: Int) {
    UserSettings.fontSize = newSize
    
    // 重建 TelnetTextView
    val newTextView = createTelnetTextView(context, newSize)
    
    // 替換視圖
    containerView.removeAllViews()
    containerView.addView(newTextView)
    
    // 更新顯示
    newTextView.setFrame(TelnetModel.frame)
}
```

---

## 🎯 效能優化

### 1. 髒矩形優化
只重繪變更的區域，而非整個畫面：

```kotlin
// 記錄變更區域
private val dirtyRegion = Rect()

fun markDirty(row: Int, col: Int, width: Int, height: Int) {
    val x = col * charWidth
    val y = row * charHeight
    dirtyRegion.union(x, y, x + width * charWidth, y + height * charHeight)
}

override fun onDraw(canvas: Canvas) {
    // 只繪製髒矩形區域
    canvas.clipRect(dirtyRegion)
    super.onDraw(canvas)
    dirtyRegion.setEmpty()
}
```

### 2. 文字快取
快取常用文字的繪製結果：

```kotlin
private val textCache = LruCache<String, Bitmap>(100)

fun drawCachedText(canvas: Canvas, text: String, x: Float, y: Float) {
    val cached = textCache.get(text)
    if (cached != null) {
        canvas.drawBitmap(cached, x, y, null)
    } else {
        // 繪製並快取
        val bitmap = renderTextToBitmap(text)
        textCache.put(text, bitmap)
        canvas.drawBitmap(bitmap, x, y, null)
    }
}
```

### 3. 硬體加速
確保啟用硬體加速：

```kotlin
// 在 View 初始化時
init {
    setLayerType(View.LAYER_TYPE_HARDWARE, null)
}
```

---

## 🐛 常見問題和解決方案

### 問題 1: 中文字元顯示錯位
**原因**: Big5 雙位元組字元佔用兩個字元位置

**解決**:
```kotlin
// 檢查是否為 Big5 全形字元
fun isFullWidth(char: Char): Boolean {
    return char.code > 0x7F  // 非 ASCII
}

// 繪製時佔用兩倍寬度
if (isFullWidth(char)) {
    x += charWidth * 2
} else {
    x += charWidth
}
```

### 問題 2: 色碼解析錯誤
**原因**: ANSI 色碼序列不完整或格式錯誤

**解決**:
```kotlin
// 驗證色碼範圍
fun validateColor(color: Int): Int {
    return when {
        color < 0 -> 0
        color > 255 -> 255
        else -> color
    }
}
```

### 問題 3: 畫面閃爍
**原因**: 頻繁的整頁重繪

**解決**:
```kotlin
// 使用雙緩衝
private var offscreenBitmap: Bitmap? = null

override fun onDraw(canvas: Canvas) {
    if (offscreenBitmap == null) {
        offscreenBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
    
    val offscreenCanvas = Canvas(offscreenBitmap!!)
    drawToCanvas(offscreenCanvas)  // 繪製到離屏緩衝
    
    canvas.drawBitmap(offscreenBitmap!!, 0f, 0f, null)  // 一次性繪製
}
```

### 問題 4: 記憶體洩漏
**原因**: Bitmap 快取未釋放

**解決**:
```kotlin
override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    
    // 清理快取
    textCache.evictAll()
    offscreenBitmap?.recycle()
    offscreenBitmap = null
}
```

---

## 📝 開發規範

### 自訂 TelnetTextView
1. 繼承 `TelnetTextView`
2. 定義字元尺寸常數
3. 實作 `drawText()` 方法
4. 處理特殊字元（全形、符號）

### 自訂繪製邏輯
1. 擴充 `TelnetViewDrawer`
2. 覆寫 `draw()` 方法
3. 調用 `super.draw()` 保持基礎功能
4. 新增自訂繪製元素

### UI 執行緒安全
所有 View 操作必須在主執行緒：
```kotlin
// ✅ 正確
object : ASRunner() {
    override fun run() {
        telnetView.refresh()
    }
}.runInMainThread()

// ❌ 錯誤
telnetView.refresh()  // 可能在背景執行緒
```

---

## 🔗 與其他模組的關係

```
telnetUI (UI 渲染)
    ↓ 使用
    ├── telnet (資料來源 - TelnetFrame/TelnetModel)
    ├── asFramework (基礎 UI - ASViewController/ASView)
    └── 被使用於
        └── Bahamut (頁面整合 - ArticlePage/BoardMainPage)
```

---

## 📚 延伸閱讀

- [textView 詳細文件](.github/instructions/telnetUI-textView.md)
- [telnet 模組](.github/instructions/telnet.md)
- [ANSI 轉義序列規範](https://en.wikipedia.org/wiki/ANSI_escape_code)
- [Android Custom View 指南](https://developer.android.com/guide/topics/ui/custom-components)

---

**維護者**: Bahamut BBS 開發團隊  
**最後更新**: 2025-12-11
