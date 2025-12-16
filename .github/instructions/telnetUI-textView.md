# telnetUI/textView - 文字視圖元件

**applyto**: `app/src/main/java/com/kota/telnetUI/textView/**/*.kt`

## 📋 模組概述

textView 模組提供 Telnet 螢幕顯示的文字視圖元件,支援 ANSI 色碼渲染、Big5 編碼、游標顯示等功能。

**技術棧**: Kotlin, Android Custom View, Canvas Drawing  
**設計模式**: Custom View Pattern

---

## 📂 主要元件

### `TelnetTextView.kt` - Telnet 文字視圖

```kotlin
class TelnetTextView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 16f * resources.displayMetrics.density
        typeface = Typeface.MONOSPACE
    }
    
    private val cursorPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    
    // 螢幕尺寸
    private var charWidth = 0f
    private var charHeight = 0f
    private val screenWidth = 80
    private val screenHeight = 24
    
    // 螢幕資料
    private var screenBuffer: Array<CharArray> = Array(screenHeight) { CharArray(screenWidth) }
    private var colorBuffer: Array<IntArray> = Array(screenHeight) { IntArray(screenWidth) }
    
    init {
        // 計算字元尺寸
        val bounds = Rect()
        textPaint.getTextBounds("M", 0, 1, bounds)
        charWidth = bounds.width().toFloat()
        charHeight = bounds.height().toFloat()
        
        // 監聽螢幕更新
        TelnetModel.addScreenUpdateListener(object : TelnetModel.ScreenUpdateListener {
            override fun onScreenUpdated() {
                updateScreen()
            }
        })
    }
    
    /**
     * 更新螢幕內容
     */
    private fun updateScreen() {
        screenBuffer = TelnetModel.getScreen()
        
        object : ASRunner() {
            override fun run() {
                invalidate()  // 重繪
            }
        }.runInMainThread()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 繪製背景
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // 繪製文字
        for (y in 0 until screenHeight) {
            for (x in 0 until screenWidth) {
                val char = screenBuffer[y][x]
                if (char != ' ') {
                    val color = colorBuffer[y][x]
                    textPaint.color = color
                    
                    val posX = x * charWidth
                    val posY = (y + 1) * charHeight
                    
                    canvas.drawText(char.toString(), posX, posY, textPaint)
                }
            }
        }
        
        // 繪製游標
        drawCursor(canvas)
    }
    
    /**
     * 繪製游標
     */
    private fun drawCursor(canvas: Canvas) {
        val cursorX = TelnetModel.cursorX
        val cursorY = TelnetModel.cursorY
        
        if (cursorX in 0 until screenWidth && cursorY in 0 until screenHeight) {
            val posX = cursorX * charWidth
            val posY = cursorY * charHeight
            
            canvas.drawRect(
                posX, posY,
                posX + charWidth, posY + charHeight,
                cursorPaint
            )
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (screenWidth * charWidth).toInt()
        val height = (screenHeight * charHeight).toInt()
        
        setMeasuredDimension(width, height)
    }
    
    /**
     * 設定字型大小
     */
    fun setTextSize(size: Float) {
        textPaint.textSize = size * resources.displayMetrics.density
        
        // 重新計算字元尺寸
        val bounds = Rect()
        textPaint.getTextBounds("M", 0, 1, bounds)
        charWidth = bounds.width().toFloat()
        charHeight = bounds.height().toFloat()
        
        requestLayout()
        invalidate()
    }
    
    /**
     * 設定文字顏色
     */
    fun setTextColor(color: Int) {
        textPaint.color = color
        invalidate()
    }
    
    /**
     * 設定背景顏色
     */
    fun setBackgroundColor(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }
}
```

---

## 🎯 使用範例

### 在 Layout 中使用

```xml
<com.kota.telnetUI.textView.TelnetTextView
    android:id="@+id/telnet_text_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 在代碼中配置

```kotlin
class TelnetPage : ASViewController() {
    
    private lateinit var telnetTextView: TelnetTextView
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        
        telnetTextView = findViewById(R.id.telnet_text_view) as TelnetTextView
        
        // 設定字型大小
        telnetTextView.setTextSize(UserSettings.fontSize.toFloat())
        
        // 設定顏色
        telnetTextView.setTextColor(Color.WHITE)
        telnetTextView.setBackgroundColor(Color.BLACK)
    }
}
```

---

## 📚 相關模組

- [telnet-model](telnet-model.md) - 螢幕資料來源
- [asFramework-model](asFramework-model.md) - ASPoint/ASSize
- [Bahamut-pages](Bahamut-pages.md) - 使用此元件的頁面

---

## 📝 技術特點總結

1. **Canvas 繪製**: 高效能螢幕渲染
2. **ANSI 色碼**: 支援彩色文字
3. **Big5 編碼**: 支援中文顯示
4. **游標顯示**: 即時顯示游標位置
5. **字型設定**: 可調整字型大小
6. **自動更新**: 監聽螢幕變化自動重繪
