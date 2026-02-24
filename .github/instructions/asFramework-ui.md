# asFramework/ui - UI 元件庫

**applyto**: `app/src/main/java/com/kota/asFramework/ui/**/*.kt`

## 📋 模組概述

ui 模組提供自訂的 UI 元件集合,包括列表視圖、捲動視圖、提示訊息等。這些元件針對 BBS 客戶端的特殊需求進行優化。

**技術棧**: Kotlin, Android Custom Views  
**設計模式**: 委派模式 (Delegate)  
**命名前綴**: AS (Application Structure)

---

## 📂 主要元件

### 1️⃣ `ASListView.kt` - 自訂列表視圖

擴展 Android ListView,提供額外功能。

```kotlin
class ASListView(context: Context, attrs: AttributeSet?) : ListView(context, attrs) {
    
    /**
     * Overscroll 委派（滑動到頂部/底部時觸發）
     */
    var overscrollDelegate: ASListViewOverscrollDelegate? = null
    
    /**
     * 擴展選項委派
     */
    var extentOptionalDelegate: ASListViewExtentOptionalDelegate? = null
    
    override fun overScrollBy(
        deltaX: Int, deltaY: Int,
        scrollX: Int, scrollY: Int,
        scrollRangeX: Int, scrollRangeY: Int,
        maxOverScrollX: Int, maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        
        // 檢測是否滑動到頂部或底部
        if (isTouchEvent) {
            if (deltaY < 0 && scrollY == 0) {
                // 在頂部繼續向上滑
                overscrollDelegate?.onASListViewOverscrollTop(this, -deltaY)
            } else if (deltaY > 0 && scrollY == scrollRangeY) {
                // 在底部繼續向下滑
                overscrollDelegate?.onASListViewOverscrollBottom(this, deltaY)
            }
        }
        
        return super.overScrollBy(
            deltaX, deltaY, scrollX, scrollY,
            scrollRangeX, scrollRangeY,
            maxOverScrollX, maxOverScrollY,
            isTouchEvent
        )
    }
}

/**
 * Overscroll 委派介面
 */
interface ASListViewOverscrollDelegate {
    fun onASListViewOverscrollTop(listView: ASListView, deltaY: Int)
    fun onASListViewOverscrollBottom(listView: ASListView, deltaY: Int)
}
```

**使用範例**:
```kotlin
class BoardMainPage : TelnetListPage() {
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        
        listView.overscrollDelegate = object : ASListViewOverscrollDelegate {
            override fun onASListViewOverscrollTop(listView: ASListView, deltaY: Int) {
                // 下拉刷新
                if (deltaY > 200) {
                    loadFirstBlock()
                }
            }
            
            override fun onASListViewOverscrollBottom(listView: ASListView, deltaY: Int) {
                // 上拉載入更多
                if (deltaY > 200) {
                    loadNextBlock()
                }
            }
        }
    }
}
```

---

### 2️⃣ `ASScrollView.kt` - 捲動視圖

自訂 ScrollView,支援捲動事件監聽。

```kotlin
class ASScrollView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    
    var scrollListener: OnScrollListener? = null
    
    interface OnScrollListener {
        fun onScrollChanged(scrollY: Int, oldScrollY: Int)
    }
    
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollListener?.onScrollChanged(t, oldt)
    }
}
```

---

### 3️⃣ `ASToast.kt` - 提示訊息

統一的 Toast 顯示工具。

```kotlin
object ASToast {
    
    const val LENGTH_SHORT = Toast.LENGTH_SHORT
    const val LENGTH_LONG = Toast.LENGTH_LONG
    
    /**
     * 顯示 Toast（執行緒安全）
     */
    @JvmStatic
    fun show(context: Context, message: String, duration: Int = LENGTH_SHORT) {
        object : ASRunner() {
            override fun run() {
                Toast.makeText(context, message, duration).show()
            }
        }.runInMainThread()
    }
    
    @JvmStatic
    fun show(context: Context, messageResId: Int, duration: Int = LENGTH_SHORT) {
        show(context, context.getString(messageResId), duration)
    }
}
```

**使用範例**:
```kotlin
// 簡短提示
ASToast.show(context, "操作成功")

// 長時間顯示
ASToast.show(context, "正在處理，請稍候...", ASToast.LENGTH_LONG)

// 從資源載入
ASToast.show(context, R.string.error_network, ASToast.LENGTH_LONG)
```

---

### 4️⃣ `ASSnackBar.kt` - Snackbar 提示

類似 Material Design Snackbar 的提示元件。

```kotlin
object ASSnackBar {
    
    const val LENGTH_SHORT = 2000
    const val LENGTH_LONG = 4000
    const val LENGTH_INDEFINITE = -1
    
    /**
     * 顯示 Snackbar
     */
    @JvmStatic
    fun show(
        view: View,
        message: String,
        duration: Int = LENGTH_SHORT,
        actionText: String? = null,
        action: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(view, message, duration)
        
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) {
                action()
            }
        }
        
        snackbar.show()
    }
}
```

**使用範例**:
```kotlin
// 簡單訊息
ASSnackBar.show(rootView, "文章已儲存")

// 帶操作按鈕
ASSnackBar.show(
    view = rootView,
    message = "文章已刪除",
    duration = ASSnackBar.LENGTH_LONG,
    actionText = "復原"
) {
    restoreArticle()
}
```

---

### 5️⃣ `ASListViewItemView.kt` - 列表項目視圖基類

列表項目的基類,提供統一介面。

```kotlin
abstract class ASListViewItemView(context: Context) : LinearLayout(context) {
    
    /**
     * 更新項目視圖
     */
    abstract fun updateView(item: Any)
    
    /**
     * 回收資源
     */
    open fun recycle() {
        // 清理資源
    }
}
```

---

## 🎯 使用場景

### 下拉刷新和上拉載入

```kotlin
class MyListPage : TelnetListPage() {
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        
        listView.overscrollDelegate = object : ASListViewOverscrollDelegate {
            
            override fun onASListViewOverscrollTop(listView: ASListView, deltaY: Int) {
                if (deltaY > 200 && !isLoading) {
                    showRefreshHint()
                    loadFirstBlock()
                }
            }
            
            override fun onASListViewOverscrollBottom(listView: ASListView, deltaY: Int) {
                if (deltaY > 200 && !isLoading) {
                    showLoadMoreHint()
                    loadNextBlock()
                }
            }
        }
    }
    
    private fun showRefreshHint() {
        ASSnackBar.show(pageView!!, "重新整理中...")
    }
    
    private fun showLoadMoreHint() {
        ASSnackBar.show(pageView!!, "載入更多...")
    }
}
```

---

## 📚 相關模組

- [asFramework-pageController](asFramework-pageController.md) - 頁面控制器
- [Bahamut-listPage](Bahamut-listPage.md) - 列表頁面使用這些元件

---

## 📝 技術特點總結

1. **自訂視圖**: 擴展 Android 原生元件
2. **委派模式**: 使用委派解耦業務邏輯
3. **執行緒安全**: Toast/Snackbar 自動處理執行緒
4. **統一介面**: 提供一致的 API
5. **BBS 優化**: 針對 BBS 客戶端的特殊需求
