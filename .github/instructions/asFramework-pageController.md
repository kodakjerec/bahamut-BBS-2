# asFramework/pageController - 頁面控制器系統（核心架構）

**applyto**: `app/src/main/java/com/kota/asFramework/pageController/**/*.kt`

## 📋 模組概述

pageController 是整個應用程式最核心的模組，實現類似 iOS UIKit 的頁面管理架構。提供視圖控制器 (ViewController)、導航控制器 (NavigationController)、頁面堆疊管理、生命週期管理、頁面切換動畫等完整功能。

**這是整個專案的架構基石，所有業務頁面都基於此模組構建。**

**技術棧**: Kotlin, Android ViewGroup, Custom Views  
**設計模式**: MVC, 模板方法模式 (Template Method), 觀察者模式  
**命名前綴**: AS (Application Structure)  
**設計靈感**: iOS UIKit

---

## 📂 核心類別

### 1️⃣ `ASViewController.kt` - 視圖控制器基類

所有頁面的基類，管理單一頁面的生命週期和視圖。

#### 生命週期方法
```kotlin
abstract class ASViewController {
    // 頁面佈局資源 ID（必須實作）
    abstract val pageLayout: Int
    
    // ===== 生命週期方法（按觸發順序） =====
    
    /**
     * 頁面視圖載入完成（僅觸發一次）
     * 用於初始化 UI 元件、設定監聽器
     */
    open fun onPageDidLoad() {
        isLoaded = true
    }
    
    /**
     * 頁面即將出現（每次出現都觸發）
     * 用於載入最新資料、恢復狀態
     */
    open fun onPageWillAppear() {}
    
    /**
     * 頁面已經出現（完全可見）
     * 用於啟動自動刷新、播放動畫
     */
    open fun onPageDidAppear() {}
    
    /**
     * 頁面即將消失
     * 用於停止計時器、暫停動畫
     */
    open fun onPageWillDisappear() {}
    
    /**
     * 頁面已經消失
     * 用於儲存狀態、清理暫存資料
     */
    open fun onPageDidDisappear() {}
    
    /**
     * 頁面刷新請求
     * 用於重新載入資料
     */
    open fun onPageRefresh() {}
    
    /**
     * 頁面從導航堆疊移除
     * 用於釋放資源、清理快取
     */
    open fun onPageDidRemoveFromNavigationController() {}
    
    /**
     * 頁面記憶體釋放
     * 最終清理方法
     */
    open fun onPageDidUnload() {}
    
    // ===== 按鍵處理 =====
    
    /**
     * 返回鍵處理
     * @return true 表示消費事件，false 表示不處理
     */
    open fun onBackPressed(): Boolean {
        navigationController.popViewController()
        return true
    }
    
    open fun onSearchButtonClicked(): Boolean = false
    open fun onMenuButtonClicked(): Boolean = false
    
    // ===== 手勢處理 =====
    
    fun onReceivedGestureUp(): Boolean = false
    fun onReceivedGestureDown(): Boolean = false
    fun onReceivedGestureLeft(): Boolean = false
    open fun onReceivedGestureRight(): Boolean = false
}
```

#### 使用範例
```kotlin
class BoardMainPage : TelnetListPage() {
    
    override val pageLayout: Int = R.layout.board_page
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        
        // 初始化 UI
        listView = findViewById(R.id.list_view) as ASListView
        listView.setAdapter(adapter)
        
        // 設定監聽器
        listView.setOnItemClickListener { _, _, position, _ ->
            handleArticleClick(position)
        }
        
        Log.d(TAG, "BoardMainPage loaded")
    }
    
    override fun onPageWillAppear() {
        super.onPageWillAppear()
        
        // 恢復列表狀態
        loadListState()
        
        // 載入最新資料
        if (shouldRefresh()) {
            refresh()
        }
    }
    
    override fun onPageDidAppear() {
        super.onPageDidAppear()
        
        // 啟動自動刷新
        startAutoLoad()
    }
    
    override fun onPageWillDisappear() {
        super.onPageWillDisappear()
        
        // 停止自動刷新
        stopAutoLoad()
        
        // 取消網路請求
        cancelPendingCommands()
    }
    
    override fun onPageDidDisappear() {
        super.onPageDidDisappear()
        
        // 儲存列表狀態
        saveListState()
    }
    
    override fun onPageDidRemoveFromNavigationController() {
        super.onPageDidRemoveFromNavigationController()
        
        // 清理快取
        clearCachedBlocks()
        
        // 釋放資源
        adapter.clear()
    }
    
    override fun onBackPressed(): Boolean {
        // 自訂返回邏輯
        if (isSearchMode) {
            exitSearchMode()
            return true // 消費事件
        }
        return super.onBackPressed() // 預設行為：Pop 頁面
    }
}
```

---

### 2️⃣ `ASNavigationController.kt` - 導航控制器

管理視圖控制器堆疊，處理頁面切換和動畫。

#### 核心方法
```kotlin
open class ASNavigationController : Activity() {
    
    private val controllers = Vector<ASViewController>()
    var isAnimationEnable: Boolean = true
    var isInBackground: Boolean = false
    
    /**
     * Push 新頁面到堆疊
     * @param controller 要顯示的頁面
     * @param animated 是否顯示動畫
     */
    fun pushViewController(
        controller: ASViewController, 
        animated: Boolean = true
    ) {
        if (isAnimating) {
            // 正在動畫中，加入命令佇列
            enqueuePageCommand(PushCommand(controller, animated))
            return
        }
        
        val oldController = topController
        
        // 建立頁面視圖
        if (!controller.isLoaded) {
            buildPageView(controller)
            controller.onPageDidLoad()
        }
        
        // 加入堆疊
        controllers.add(controller)
        controller.navigationController = this
        
        // 生命週期通知
        oldController?.notifyPageWillDisappear()
        controller.notifyPageWillAppear()
        
        if (animated && isAnimationEnable) {
            // 播放動畫
            animatePush(controller) {
                oldController?.notifyPageDidDisappear()
                controller.notifyPageDidAppear()
            }
        } else {
            oldController?.notifyPageDidDisappear()
            controller.notifyPageDidAppear()
        }
    }
    
    /**
     * Pop 返回上一頁
     * @param animated 是否顯示動畫
     */
    fun popViewController(animated: Boolean = true) {
        if (controllers.size <= 1) return
        if (isAnimating) {
            enqueuePageCommand(PopCommand(animated))
            return
        }
        
        val currentController = topController
        controllers.removeAt(controllers.size - 1)
        val newTopController = topController
        
        currentController?.notifyPageWillDisappear()
        newTopController?.notifyPageWillAppear()
        
        if (animated && isAnimationEnable) {
            animatePop(currentController) {
                currentController?.notifyPageDidDisappear()
                currentController?.notifyPageDidRemoveFromNavigationController()
                cleanPageView(currentController)
                
                newTopController?.notifyPageDidAppear()
            }
        } else {
            currentController?.notifyPageDidDisappear()
            currentController?.notifyPageDidRemoveFromNavigationController()
            cleanPageView(currentController)
            
            newTopController?.notifyPageDidAppear()
        }
    }
    
    /**
     * Pop 到指定頁面
     * @param targetController 目標頁面
     * @param animated 是否顯示動畫
     */
    fun popToViewController(
        targetController: ASViewController, 
        animated: Boolean = true
    ) {
        if (!controllers.contains(targetController)) return
        
        while (topController != targetController && controllers.size > 1) {
            popViewController(animated)
        }
    }
    
    /**
     * Pop 到根頁面
     */
    fun popToRootViewController(animated: Boolean = true) {
        if (controllers.isEmpty()) return
        val rootController = controllers.first()
        popToViewController(rootController, animated)
    }
    
    /**
     * 獲取堆疊中的頁面
     */
    val topController: ASViewController?
        get() = controllers.lastOrNull()
    
    val controllerCount: Int
        get() = controllers.size
    
    fun getControllerAt(index: Int): ASViewController? {
        return controllers.getOrNull(index)
    }
}
```

#### 使用範例
```kotlin
// Push 新頁面
val articlePage = ArticlePage()
navigationController.pushViewController(articlePage, animated = true)

// Pop 返回
navigationController.popViewController(animated = true)

// Pop 到指定頁面
val boardPage = PageContainer.instance!!.boardPage
navigationController.popToViewController(boardPage, animated = true)

// 檢查堆疊
if (navigationController.controllerCount > 5) {
    // 堆疊太深，清理舊頁面
    navigationController.popToRootViewController()
}
```

---

### 3️⃣ `ASListViewController.kt` - 列表視圖控制器

專門用於列表頁面的控制器基類。

```kotlin
abstract class ASListViewController : ASViewController() {
    
    protected var listView: ASListView? = null
    protected var adapter: BaseAdapter? = null
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        setupListView()
    }
    
    protected open fun setupListView() {
        listView = findViewById(R.id.list_view) as? ASListView
        listView?.setAdapter(adapter)
    }
    
    protected open fun scrollToTop() {
        listView?.setSelection(0)
    }
    
    protected open fun scrollToBottom() {
        adapter?.count?.let { count ->
            if (count > 0) {
                listView?.setSelection(count - 1)
            }
        }
    }
}
```

---

### 4️⃣ 動畫系統

#### `ASAnimation.kt` - 動畫基類
```kotlin
abstract class ASAnimation {
    var duration: Long = 300
    var interpolator: Interpolator? = null
    
    abstract fun start()
    abstract fun cancel()
    
    interface AnimationListener {
        fun onAnimationStart(animation: ASAnimation)
        fun onAnimationEnd(animation: ASAnimation)
        fun onAnimationCancel(animation: ASAnimation)
    }
}
```

#### `ASPageAnimation.kt` - 頁面動畫
```kotlin
class ASPageAnimation : ASAnimation() {
    
    companion object {
        // Push 動畫：從右往左滑入
        fun createPushAnimation(view: View, onEnd: () -> Unit): ASPageAnimation {
            return ASPageAnimation().apply {
                // 設定初始位置（螢幕右側）
                view.translationX = view.width.toFloat()
                
                // 動畫到最終位置
                view.animate()
                    .translationX(0f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction(onEnd)
                    .start()
            }
        }
        
        // Pop 動畫：從左往右滑出
        fun createPopAnimation(view: View, onEnd: () -> Unit): ASPageAnimation {
            return ASPageAnimation().apply {
                view.animate()
                    .translationX(view.width.toFloat())
                    .setDuration(300)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction(onEnd)
                    .start()
            }
        }
    }
}
```

---

### 5️⃣ 視圖元件

#### `ASPageView.kt` - 頁面視圖容器
```kotlin
class ASPageView(context: Context) : FrameLayout(context) {
    
    var ownerController: ASViewController? = null
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        ownerController?.onSizeChanged(w, h, oldw, oldh)
    }
}
```

#### `ASGestureView.kt` - 手勢視圖
```kotlin
class ASGestureView(context: Context) : View(context) {
    
    interface ASGestureViewDelegate {
        fun onGestureDetected(gesture: Gesture)
    }
    
    enum class Gesture {
        SWIPE_LEFT,
        SWIPE_RIGHT,
        SWIPE_UP,
        SWIPE_DOWN
    }
    
    var delegate: ASGestureViewDelegate? = null
    
    private var startX = 0f
    private var startY = 0f
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - startX
                val dy = event.y - startY
                
                if (abs(dx) > abs(dy) && abs(dx) > 100) {
                    // 水平滑動
                    if (dx > 0) {
                        delegate?.onGestureDetected(Gesture.SWIPE_RIGHT)
                    } else {
                        delegate?.onGestureDetected(Gesture.SWIPE_LEFT)
                    }
                    return true
                }
                
                if (abs(dy) > abs(dx) && abs(dy) > 100) {
                    // 垂直滑動
                    if (dy > 0) {
                        delegate?.onGestureDetected(Gesture.SWIPE_DOWN)
                    } else {
                        delegate?.onGestureDetected(Gesture.SWIPE_UP)
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
```

---

## 🎯 完整使用流程

### 建立新頁面

```kotlin
// 1. 建立 Layout (board_page.xml)
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <com.kota.asFramework.ui.ASListView
        android:id="@+id/list_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
</FrameLayout>

// 2. 建立 Page 類別
class BoardPage : TelnetListPage() {
    
    override val pageLayout = R.layout.board_page
    
    private lateinit var listView: ASListView
    private val adapter = BoardAdapter()
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        
        listView = findViewById(R.id.list_view) as ASListView
        listView.setAdapter(adapter)
    }
    
    override fun onPageWillAppear() {
        super.onPageWillAppear()
        loadData()
    }
    
    override fun onPageDidAppear() {
        super.onPageDidAppear()
        startAutoRefresh()
    }
    
    override fun onPageWillDisappear() {
        super.onPageWillDisappear()
        stopAutoRefresh()
    }
}

// 3. 使用 PageContainer 管理 (單例模式)
class PageContainer {
    private var _boardPage: BoardPage? = null
    
    val boardPage: BoardPage
        get() {
            if (_boardPage == null) {
                _boardPage = BoardPage()
            }
            return _boardPage!!
        }
    
    fun cleanBoardPage() {
        _boardPage?.clear()
        _boardPage = null
    }
    
    companion object {
        var instance: PageContainer? = null
    }
}

// 4. 導航到頁面
val boardPage = PageContainer.instance!!.boardPage
navigationController.pushViewController(boardPage)
```

---

## ⚠️ 重要注意事項

### 1. 生命週期理解

```kotlin
// 生命週期觸發順序
pushViewController(newPage):
    oldPage.onPageWillDisappear()
    newPage.onPageWillAppear()
    [動畫播放]
    oldPage.onPageDidDisappear()
    newPage.onPageDidAppear()

popViewController():
    currentPage.onPageWillDisappear()
    previousPage.onPageWillAppear()
    [動畫播放]
    currentPage.onPageDidDisappear()
    currentPage.onPageDidRemoveFromNavigationController()
    previousPage.onPageDidAppear()
```

### 2. UI 更新必須用 ASRunner

```kotlin
// ❌ 錯誤
override fun onPageDidAppear() {
    // 在背景執行緒更新 UI
    Thread {
        textView.text = "Hello" // 崩潰！
    }.start()
}

// ✅ 正確
override fun onPageDidAppear() {
    ASCoroutine.runInNewCoroutine {
        val data = loadData()
        
        object : ASRunner() {
            override fun run() {
                textView.text = data
            }
        }.runInMainThread()
    }
}
```

### 3. 避免記憶體洩漏

```kotlin
// ❌ 錯誤：頁面持有 Activity 參考
class MyPage : ASViewController() {
    private var activity: Activity? = null
    
    override fun onPageDidLoad() {
        activity = navigationController // 洩漏！
    }
}

// ✅ 正確：在 onPageDidRemoveFromNavigationController 清理
class MyPage : ASViewController() {
    private var timer: Timer? = null
    
    override fun onPageDidAppear() {
        timer = Timer()
        timer?.schedule(task, 1000, 1000)
    }
    
    override fun onPageDidRemoveFromNavigationController() {
        timer?.cancel()
        timer = null
    }
}
```

---

## 📚 相關模組

- [asFramework-thread](asFramework-thread.md) - 執行緒管理
- [Bahamut-listPage](Bahamut-listPage.md) - 列表頁面基礎
- [Bahamut-pages](Bahamut-pages.md) - 業務頁面實作

---

## 📝 技術特點總結

1. **iOS 風格架構**: 參考 UIKit 設計，熟悉 iOS 開發者易上手
2. **完整生命週期**: 7 個生命週期方法涵蓋所有場景
3. **堆疊管理**: Vector 管理頁面堆疊，支援複雜導航
4. **動畫支援**: 內建 Push/Pop 動畫，可自訂
5. **觀察者模式**: 監聽器機制解耦頁面間依賴
6. **手勢支援**: 內建滑動手勢識別
7. **狀態管理**: 自動追蹤 appeared/disappeared 狀態
