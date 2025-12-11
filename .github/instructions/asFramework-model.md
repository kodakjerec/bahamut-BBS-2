# asFramework/model - 基礎資料模型

**applyto**: `app/src/main/java/com/kota/asFramework/model/**/*.kt`

## 📋 模組概述

model 模組提供基礎的幾何和資料結構定義，用於整個應用程式的座標、尺寸計算。這些模型是其他模組的基礎建構元件，特別是 UI 佈局和動畫系統。

**技術棧**: Kotlin, Data Classes  
**設計模式**: Value Object  
**命名前綴**: AS (Application Structure)

---

## 📂 檔案結構

### `ASPoint.kt` - 點座標

表示二維空間中的點座標 (x, y)。

#### 類別定義
```kotlin
data class ASPoint(
    var x: Float = 0f,
    var y: Float = 0f
) {
    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())
    
    // 複製
    fun copy(): ASPoint = ASPoint(x, y)
    
    // 偏移
    fun offset(dx: Float, dy: Float) {
        x += dx
        y += dy
    }
    
    // 距離計算
    fun distanceTo(other: ASPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
    
    // 字串表示
    override fun toString(): String = "ASPoint(x=$x, y=$y)"
    
    companion object {
        val ZERO = ASPoint(0f, 0f)
    }
}
```

#### 使用範例

**基本使用**:
```kotlin
// 建立點
val point1 = ASPoint(100f, 200f)
val point2 = ASPoint(150, 250) // 整數自動轉換為浮點數

// 存取座標
println("X: ${point1.x}, Y: ${point1.y}") // X: 100.0, Y: 200.0

// 修改座標
point1.x = 120f
point1.y = 180f

// 偏移
point1.offset(10f, -20f) // x = 130, y = 160

// 複製
val copy = point1.copy()

// 零點
val origin = ASPoint.ZERO
```

**動畫應用**:
```kotlin
class MoveAnimation {
    private var currentPosition = ASPoint(0f, 0f)
    private val targetPosition = ASPoint(300f, 400f)
    
    fun animate(progress: Float) {
        // 線性插值
        currentPosition.x = lerp(currentPosition.x, targetPosition.x, progress)
        currentPosition.y = lerp(currentPosition.y, targetPosition.y, progress)
        
        updateViewPosition(currentPosition)
    }
    
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }
}
```

**觸控事件處理**:
```kotlin
override fun onTouchEvent(event: MotionEvent): Boolean {
    val touchPoint = ASPoint(event.x, event.y)
    
    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            lastTouchPoint = touchPoint.copy()
            return true
        }
        MotionEvent.ACTION_MOVE -> {
            val dx = touchPoint.x - lastTouchPoint.x
            val dy = touchPoint.y - lastTouchPoint.y
            handleDrag(dx, dy)
            lastTouchPoint = touchPoint
            return true
        }
    }
    return super.onTouchEvent(event)
}
```

**碰撞檢測**:
```kotlin
fun isPointInRect(point: ASPoint, rectOrigin: ASPoint, rectSize: ASSize): Boolean {
    return point.x >= rectOrigin.x &&
           point.x <= rectOrigin.x + rectSize.width &&
           point.y >= rectOrigin.y &&
           point.y <= rectOrigin.y + rectSize.height
}

// 使用
val clickPoint = ASPoint(150f, 200f)
val buttonOrigin = ASPoint(100f, 100f)
val buttonSize = ASSize(200f, 80f)

if (isPointInRect(clickPoint, buttonOrigin, buttonSize)) {
    handleButtonClick()
}
```

---

### `ASSize.kt` - 尺寸

表示寬度和高度。

#### 類別定義
```kotlin
data class ASSize(
    var width: Float = 0f,
    var height: Float = 0f
) {
    constructor(width: Int, height: Int) : this(width.toFloat(), height.toFloat())
    
    // 複製
    fun copy(): ASSize = ASSize(width, height)
    
    // 面積
    fun area(): Float = width * height
    
    // 是否為空
    fun isEmpty(): Boolean = width <= 0 || height <= 0
    
    // 縮放
    fun scale(factor: Float) {
        width *= factor
        height *= factor
    }
    
    // 適配尺寸（保持比例）
    fun aspectFit(containerSize: ASSize): ASSize {
        val widthRatio = containerSize.width / width
        val heightRatio = containerSize.height / height
        val scale = minOf(widthRatio, heightRatio)
        
        return ASSize(width * scale, height * scale)
    }
    
    // 填充尺寸（保持比例）
    fun aspectFill(containerSize: ASSize): ASSize {
        val widthRatio = containerSize.width / width
        val heightRatio = containerSize.height / height
        val scale = maxOf(widthRatio, heightRatio)
        
        return ASSize(width * scale, height * scale)
    }
    
    // 字串表示
    override fun toString(): String = "ASSize(width=$width, height=$height)"
    
    companion object {
        val ZERO = ASSize(0f, 0f)
    }
}
```

#### 使用範例

**基本使用**:
```kotlin
// 建立尺寸
val size1 = ASSize(300f, 200f)
val size2 = ASSize(400, 300) // 整數自動轉換

// 存取尺寸
println("寬: ${size1.width}, 高: ${size1.height}")

// 計算面積
val area = size1.area() // 60000.0

// 檢查是否為空
if (!size1.isEmpty()) {
    renderView(size1)
}

// 縮放
val scaledSize = size1.copy()
scaledSize.scale(1.5f) // width = 450, height = 300
```

**圖片縮放**:
```kotlin
class ImageView {
    private val imageSize = ASSize(800f, 600f)  // 原始圖片尺寸
    private val viewSize = ASSize(400f, 300f)   // 視圖尺寸
    
    fun calculateFitSize(): ASSize {
        // 適配尺寸（不會超出邊界）
        return imageSize.aspectFit(viewSize)
    }
    
    fun calculateFillSize(): ASSize {
        // 填充尺寸（可能超出邊界）
        return imageSize.aspectFill(viewSize)
    }
}

// 使用
val imageView = ImageView()
val fitSize = imageView.calculateFitSize()     // ASSize(400, 300) - 適配
val fillSize = imageView.calculateFillSize()   // ASSize(400, 300) - 填充
```

**響應式佈局**:
```kotlin
class ResponsiveLayout {
    fun calculateItemSize(
        containerSize: ASSize,
        itemCount: Int,
        columns: Int,
        spacing: Float
    ): ASSize {
        val totalSpacing = spacing * (columns - 1)
        val itemWidth = (containerSize.width - totalSpacing) / columns
        
        val rows = ceil(itemCount.toFloat() / columns).toInt()
        val itemHeight = if (rows > 0) {
            (containerSize.height - spacing * (rows - 1)) / rows
        } else {
            0f
        }
        
        return ASSize(itemWidth, itemHeight)
    }
}

// 使用
val layout = ResponsiveLayout()
val containerSize = ASSize(800f, 600f)
val itemSize = layout.calculateItemSize(
    containerSize = containerSize,
    itemCount = 12,
    columns = 3,
    spacing = 10f
)
```

**視窗管理**:
```kotlin
class WindowManager {
    private val screenSize = ASSize(1080f, 1920f)
    
    fun calculateDialogSize(contentSize: ASSize): ASSize {
        val maxWidth = screenSize.width * 0.8f
        val maxHeight = screenSize.height * 0.7f
        val maxSize = ASSize(maxWidth, maxHeight)
        
        // 適配到最大尺寸
        return contentSize.aspectFit(maxSize)
    }
    
    fun calculateCenterPosition(dialogSize: ASSize): ASPoint {
        return ASPoint(
            x = (screenSize.width - dialogSize.width) / 2,
            y = (screenSize.height - dialogSize.height) / 2
        )
    }
}
```

---

## 🎯 組合使用

### 矩形區域表示

```kotlin
data class ASRect(
    val origin: ASPoint,
    val size: ASSize
) {
    val left: Float get() = origin.x
    val top: Float get() = origin.y
    val right: Float get() = origin.x + size.width
    val bottom: Float get() = origin.y + size.height
    
    val center: ASPoint
        get() = ASPoint(
            x = origin.x + size.width / 2,
            y = origin.y + size.height / 2
        )
    
    fun contains(point: ASPoint): Boolean {
        return point.x >= left && point.x <= right &&
               point.y >= top && point.y <= bottom
    }
    
    fun intersects(other: ASRect): Boolean {
        return !(left > other.right || right < other.left ||
                 top > other.bottom || bottom < other.top)
    }
}

// 使用
val rect1 = ASRect(
    origin = ASPoint(100f, 100f),
    size = ASSize(200f, 150f)
)

val rect2 = ASRect(
    origin = ASPoint(250f, 200f),
    size = ASSize(100f, 100f)
)

if (rect1.intersects(rect2)) {
    handleCollision()
}
```

### 邊距和內邊距

```kotlin
data class ASEdgeInsets(
    val top: Float = 0f,
    val left: Float = 0f,
    val bottom: Float = 0f,
    val right: Float = 0f
) {
    constructor(all: Float) : this(all, all, all, all)
    constructor(vertical: Float, horizontal: Float) : this(vertical, horizontal, vertical, horizontal)
    
    fun apply(size: ASSize): ASSize {
        return ASSize(
            width = size.width - left - right,
            height = size.height - top - bottom
        )
    }
    
    companion object {
        val ZERO = ASEdgeInsets(0f)
    }
}

// 使用
val containerSize = ASSize(400f, 300f)
val padding = ASEdgeInsets(top = 20f, left = 10f, bottom = 20f, right = 10f)
val contentSize = padding.apply(containerSize) // ASSize(380, 260)
```

### 動畫路徑

```kotlin
class AnimationPath {
    private val points = mutableListOf<ASPoint>()
    
    fun addPoint(x: Float, y: Float) {
        points.add(ASPoint(x, y))
    }
    
    fun getPointAt(progress: Float): ASPoint {
        if (points.isEmpty()) return ASPoint.ZERO
        if (points.size == 1) return points[0].copy()
        
        val totalLength = getTotalLength()
        val targetLength = totalLength * progress
        
        var currentLength = 0f
        for (i in 0 until points.size - 1) {
            val segmentLength = points[i].distanceTo(points[i + 1])
            if (currentLength + segmentLength >= targetLength) {
                val t = (targetLength - currentLength) / segmentLength
                return interpolate(points[i], points[i + 1], t)
            }
            currentLength += segmentLength
        }
        
        return points.last().copy()
    }
    
    private fun getTotalLength(): Float {
        var length = 0f
        for (i in 0 until points.size - 1) {
            length += points[i].distanceTo(points[i + 1])
        }
        return length
    }
    
    private fun interpolate(start: ASPoint, end: ASPoint, t: Float): ASPoint {
        return ASPoint(
            x = start.x + (end.x - start.x) * t,
            y = start.y + (end.y - start.y) * t
        )
    }
}
```

---

## ⚠️ 注意事項

### 1. 可變性

**ASPoint 和 ASSize 是可變的**（var 屬性），需要注意引用共享問題：

```kotlin
// ❌ 危險：共享引用
val originalPoint = ASPoint(100f, 100f)
val sharedPoint = originalPoint  // 指向同一個物件
sharedPoint.x = 200f
println(originalPoint.x) // 200 - 被修改了！

// ✅ 安全：使用 copy()
val copiedPoint = originalPoint.copy()
copiedPoint.x = 200f
println(originalPoint.x) // 100 - 未被修改
```

### 2. 浮點數精度

```kotlin
// ⚠️ 浮點數比較要小心
val size1 = ASSize(100.0f, 100.0f)
val size2 = ASSize(100.0f, 100.0f)

// ❌ 不建議直接比較
if (size1.width == size2.width) { /* ... */ }

// ✅ 建議使用容差比較
fun Float.isCloseTo(other: Float, epsilon: Float = 0.0001f): Boolean {
    return abs(this - other) < epsilon
}

if (size1.width.isCloseTo(size2.width)) { /* ... */ }
```

### 3. 零除錯誤

```kotlin
// ❌ 危險：可能除以零
fun calculateAspectRatio(size: ASSize): Float {
    return size.width / size.height  // 如果 height = 0 會拋出異常
}

// ✅ 安全：檢查空尺寸
fun calculateAspectRatio(size: ASSize): Float? {
    if (size.isEmpty() || size.height == 0f) return null
    return size.width / size.height
}
```

---

## 🔧 最佳實踐

### 1. 使用 Data Class 特性

```kotlin
// 利用 data class 的 copy() 方法
val original = ASSize(100f, 200f)
val scaled = original.copy(width = original.width * 2) // ASSize(200, 200)
```

### 2. 建立工廠方法

```kotlin
object ASPointFactory {
    fun fromMotionEvent(event: MotionEvent) = ASPoint(event.x, event.y)
    
    fun fromView(view: View) = ASPoint(view.x, view.y)
    
    fun center(rect: ASRect) = ASPoint(
        x = rect.origin.x + rect.size.width / 2,
        y = rect.origin.y + rect.size.height / 2
    )
}

object ASSizeFactory {
    fun fromView(view: View) = ASSize(view.width.toFloat(), view.height.toFloat())
    
    fun fromBitmap(bitmap: Bitmap) = ASSize(bitmap.width.toFloat(), bitmap.height.toFloat())
    
    fun square(side: Float) = ASSize(side, side)
}
```

### 3. 擴展函數

```kotlin
// ASPoint 擴展
fun ASPoint.toAndroidPoint(): Point = Point(x.toInt(), y.toInt())
fun ASPoint.toAndroidPointF(): PointF = PointF(x, y)

// ASSize 擴展
fun ASSize.toLayoutParams(): ViewGroup.LayoutParams {
    return ViewGroup.LayoutParams(width.toInt(), height.toInt())
}

// 使用
val point = ASPoint(100f, 200f)
canvas.drawCircle(point.toAndroidPointF(), radius, paint)
```

---

## 📚 相關模組

- [asFramework-pageController](asFramework-pageController.md) - 使用 ASPoint/ASSize 進行佈局
- [asFramework-ui](asFramework-ui.md) - UI 元件使用這些模型
- [telnetUI-textView](telnetUI-textView.md) - 文字視圖座標計算

---

## 📝 技術特點總結

1. **簡潔設計**: 使用 Kotlin data class，自動產生 equals/hashCode/toString
2. **類型安全**: 強型別座標和尺寸，避免參數順序錯誤
3. **可變性**: 使用 var 屬性支援就地修改，提高效能
4. **工具方法**: 提供常用的幾何計算方法
5. **零拷貝選項**: 可直接修改，也可使用 copy() 建立副本
6. **與 Android 整合**: 易於轉換為 Android 原生類型
