package com.kota.Bahamut.pages.model

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.google.android.material.card.MaterialCardView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.UserSettings.Companion.floatingLocation
import com.kota.Bahamut.service.UserSettings.Companion.setFloatingLocation
import com.kota.Bahamut.service.UserSettings.Companion.toolbarAlpha
import com.kota.Bahamut.service.UserSettings.Companion.toolbarIdle
import java.lang.ref.WeakReference
import kotlin.math.hypot

/**
 * 浮動工具列元件 (`ToolBarFloating`)
 *
 * 預設狀態 (收合)：
 * - 顯示 60dp x 60dp 圓形浮動按鈕 FAB (內部為精確居中的圓點)
 * - 錨點位置 (靠左下 or 靠右下) 記錄於 UserSetting (floatingLocation)
 * - 拖曳圓點可移動位置，放開後自動貼合左下或右下
 * - 透明度沿用 UserSetting (預設 40%)
 *
 * 展開狀態：
 * - 點擊圓形按鈕後水平展開，彈出視窗為矩形方框無圓角，符合 app UI 風格
 * - 若靠右下，向左展開；若靠左下，向右展開
 * - 展開動畫：Alpha 淡入 0 -> 1 (180ms)
 *
 * 收合條件：
 * 1. 再次點擊圓形按鈕
 * 2. 點擊工具列外區域
 * 3. 開始捲動內容 (MotionEvent outside while expanded)
 * 4. 閒置逾時 (預設 3 秒，沿用 UserSetting)
 */
class ToolBarFloating @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardActionsContainer: MaterialCardView? = null
    private var cardFab: MaterialCardView? = null

    private var btnSetting: Button? = null
    private var btn1: Button? = null
    private var btn2: Button? = null

    /** 是否靠左邊下方 */
    private var isAnchoredLeft: Boolean = false

    /** 是否為展開狀態 */
    var isExpanded: Boolean = false
        private set

    /** 閒置自動收合時間 (毫秒) */
    private var autoHideDelayMs: Long = 3000L

    /** 收合狀態時的不透明度 (0.0 ~ 1.0) */
    private var collapsedAlpha: Float = 0.4f

    /** 拖曳狀態變數 */
    private var isDragging = false
    private var touchDownRawX = 0f
    private var touchDownRawY = 0f
    private var initialTranslationX = 0f
    private var initialTranslationY = 0f

    /** 閒置倒數 Handler */
    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoHideRunnable = Runnable {
        if (isExpanded) {
            collapse()
        }
    }

    init {
        init(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context) {
        inflate(context, R.layout.toolbar_floating, this)

        cardActionsContainer = findViewById(R.id.ToolbarFloating_actions_card)
        cardFab = findViewById(R.id.ToolbarFloating_fab)

        btnSetting = findViewById(R.id.ToolbarFloating_setting)
        btn1 = findViewById(R.id.ToolbarFloating_1)
        btn2 = findViewById(R.id.ToolbarFloating_2)

        // 讀取 UserSettings 記錄的懸浮位置判斷靠左或靠右
        val loc = floatingLocation
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        isAnchoredLeft = if (loc.isNotEmpty() && loc[0]!! >= 0f) {
            loc[0]!! < screenWidth / 2f
        } else {
            false // 預設靠右邊下方
        }

        updateSettings()

        // 初始為收合狀態 (使用 INVISIBLE 保留寬度避免被 RelativeLayout 裁切)
        cardActionsContainer?.visibility = View.INVISIBLE
        cardActionsContainer?.alpha = 0f
        cardFab?.alpha = collapsedAlpha

        // 套用錨點邊界配置與 View 順序
        applyAnchorSide(isAnchoredLeft)

        // FAB 點擊事件 (切換展開/收合)
        cardFab?.setOnClickListener {
            if (isExpanded) {
                collapse()
            } else {
                expand()
            }
        }

        // FAB 手勢觸控 (支援拖曳移動貼合左/右下)
        cardFab?.setOnTouchListener(fabTouchListener)
        findViewById<View>(R.id.ToolbarFloating_fab_icon)?.setOnTouchListener(fabTouchListener)
    }

    /** FAB 手勢觸控監聽器 */
    @SuppressLint("ClickableViewAccessibility")
    private val fabTouchListener = OnTouchListener { v, event ->
        val density = resources.displayMetrics.density
        val dragSlop = 5f * density

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                touchDownRawX = event.rawX
                touchDownRawY = event.rawY
                initialTranslationX = translationX
                initialTranslationY = translationY

                v.parent?.requestDisallowInterceptTouchEvent(true)
                parent?.requestDisallowInterceptTouchEvent(true)
                true
            }

            MotionEvent.ACTION_MOVE -> {
                v.parent?.requestDisallowInterceptTouchEvent(true)
                parent?.requestDisallowInterceptTouchEvent(true)

                val dx = event.rawX - touchDownRawX
                val dy = event.rawY - touchDownRawY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                if (!isDragging && dist > dragSlop) {
                    isDragging = true
                    if (isExpanded) {
                        collapse()
                    }
                }

                if (isDragging) {
                    translationX = initialTranslationX + dx
                    translationY = initialTranslationY + dy
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    isDragging = false

                    val fabLoc = IntArray(2)
                    cardFab?.getLocationOnScreen(fabLoc)
                    val fabCenterX = fabLoc[0] + (cardFab?.width ?: 0) / 2f
                    val screenWidth = resources.displayMetrics.widthPixels.toFloat()
                    val newIsLeft = fabCenterX < screenWidth / 2f

                    animate()
                        .translationX(0f)
                        .translationY(0f)
                        .setDuration(150L)
                        .withEndAction {
                            applyAnchorSide(newIsLeft)
                        }
                        .start()
                } else {
                    // 短按點擊：直接切換展開/收合
                    if (isExpanded) {
                        collapse()
                    } else {
                        expand()
                    }
                }
                true
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    animate().translationX(0f).translationY(0f).setDuration(150L).start()
                }
                true
            }

            else -> false
        }
    }

    /** 切換靠左下或靠右下位置配置 */
    private fun applyAnchorSide(isLeft: Boolean) {
        isAnchoredLeft = isLeft

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val savedX = if (isLeft) 0f else screenWidth
        setFloatingLocation(savedX, 0f)

        val root = findViewById<LinearLayout>(R.id.ToolbarFloating) ?: return
        val actionsCard = cardActionsContainer ?: return
        val fab = cardFab ?: return

        root.removeAllViews()

        val density = resources.displayMetrics.density
        val gapPx = (8 * density).toInt()

        val fabSizePx = (60 * density).toInt()
        val fabParams = LinearLayout.LayoutParams(fabSizePx, fabSizePx)

        val actionsParams = LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            (52 * density).toInt()
        )

        if (isLeft) {
            // 靠左下： FAB (左) + ActionsCard (右)
            fab.layoutParams = fabParams
            root.addView(fab)
            actionsParams.marginStart = gapPx
            actionsParams.marginEnd = 0
            actionsCard.layoutParams = actionsParams
            root.addView(actionsCard)
        } else {
            // 靠右下： ActionsCard (左) + FAB (右)
            actionsParams.marginEnd = gapPx
            actionsParams.marginStart = 0
            actionsCard.layoutParams = actionsParams
            root.addView(actionsCard)

            fab.layoutParams = fabParams
            root.addView(fab)
        }

        enforcePositioning()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activeInstance = this
        enforcePositioning()
        findActivity()?.let { attachGlobalWindowCallback(it) }
    }

    override fun onDetachedFromWindow() {
        cancelAutoHideTimer()
        if (activeInstance == this) {
            activeInstance = null
        }
        super.onDetachedFromWindow()
    }

    /** 確保元件位於靠左/靠右邊界下方，間距 16dp, 80dp */
    private fun enforcePositioning() {
        val density = resources.displayMetrics.density
        val marginPx = (16 * density).toInt()
        val marginBottomPx = (80 * density).toInt()

        val p = layoutParams
        val gravityVal = if (isAnchoredLeft) (Gravity.START or Gravity.BOTTOM) else (Gravity.END or Gravity.BOTTOM)

        if (p is RelativeLayout.LayoutParams) {
            p.removeRule(RelativeLayout.ALIGN_PARENT_START)
            p.removeRule(RelativeLayout.ALIGN_PARENT_END)
            if (isAnchoredLeft) {
                p.addRule(RelativeLayout.ALIGN_PARENT_START)
                p.marginStart = marginPx
                p.marginEnd = 0
            } else {
                p.addRule(RelativeLayout.ALIGN_PARENT_END)
                p.marginEnd = marginPx
                p.marginStart = 0
            }
            p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            p.bottomMargin = marginBottomPx
            layoutParams = p
        } else if (p is LayoutParams) {
            p.gravity = gravityVal
            if (isAnchoredLeft) {
                p.marginStart = marginPx
                p.marginEnd = 0
            } else {
                p.marginEnd = marginPx
                p.marginStart = 0
            }
            p.bottomMargin = marginBottomPx
            layoutParams = p
        }

        requestLayout()
        invalidate()
    }

    /** 尋找當前 View 所屬的 Activity */
    private fun findActivity(): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /** 重新讀取並套用 UserSettings */
    fun updateSettings() {
        activeInstance = this

        val idleSec = if (toolbarIdle <= 0f) 3.0f else toolbarIdle
        autoHideDelayMs = (idleSec * 1000f).toLong().coerceAtLeast(1000L)

        val alphaVal = if (toolbarAlpha <= 0f) 40.0f else toolbarAlpha
        collapsedAlpha = (alphaVal / 100f).coerceIn(0.1f, 1.0f)

        if (!isExpanded) {
            cardFab?.alpha = collapsedAlpha
        }
    }

    /** 展開工具列 */
    fun expand() {
        if (isExpanded) {
            resetAutoHideTimer()
            return
        }
        isExpanded = true

        val container = cardActionsContainer ?: return
        container.animate().cancel()
        container.visibility = View.VISIBLE

        container.animate()
            .alpha(1.0f)
            .setDuration(180L)
            .setListener(null)
            .start()

        cardFab?.animate()
            ?.alpha(1.0f)
            ?.setDuration(180L)
            ?.start()

        resetAutoHideTimer()
    }

    /** 收合工具列 */
    fun collapse() {
        if (!isExpanded) return
        isExpanded = false
        cancelAutoHideTimer()

        val container = cardActionsContainer
        if (container != null) {
            container.animate().cancel()
            container.animate()
                .alpha(0.0f)
                .setDuration(180L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (!isExpanded) {
                            container.visibility = View.INVISIBLE
                        }
                    }
                })
                .start()
        }

        cardFab?.animate()
            ?.alpha(collapsedAlpha)
            ?.setDuration(180L)
            ?.start()
    }

    /** 控制整個浮動工具列的顯示狀態 */
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.VISIBLE) {
            activeInstance = this
            updateSettings()
        } else {
            collapse()
        }
    }

    /** 重置閒置隱藏計時器 */
    private fun resetAutoHideTimer() {
        cancelAutoHideTimer()
        mainHandler.postDelayed(autoHideRunnable, autoHideDelayMs)
    }

    /** 取消閒置隱藏計時器 */
    private fun cancelAutoHideTimer() {
        mainHandler.removeCallbacks(autoHideRunnable)
    }

    /** 判斷觸控座標 (rawX, rawY) 是否在 Floating Toolbar 範圍內 */
    private fun isTouchInsideToolbar(rawX: Float, rawY: Float): Boolean {
        val targetView = if (isExpanded) this else (cardFab ?: this)
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        val left = location[0].toFloat()
        val top = location[1].toFloat()
        val right = left + targetView.width.toFloat()
        val bottom = top + targetView.height.toFloat()
        return rawX in left..right && rawY in top..bottom
    }

    /** 處理外部點擊/滾動事件：若展開狀態且點擊 outside，聯動收合 */
    fun handleOutsideTouch(event: MotionEvent) {
        if (!isExpanded || !isShown || isDragging) return

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (!isTouchInsideToolbar(event.rawX, event.rawY)) {
                    collapse()
                }
            }
        }
    }

    // --- 相容外部調用 API ---

    fun setOnClickListenerSetting(listener: OnClickListener?) {
        btnSetting?.setOnClickListener { v ->
            resetAutoHideTimer()
            listener?.onClick(v)
        }
    }

    fun setTextSetting(text: String?) {
        btnSetting?.text = text
    }

    fun setOnClickListener1(listener: OnClickListener?) {
        btn1?.setOnClickListener { v ->
            resetAutoHideTimer()
            listener?.onClick(v)
        }
    }

    fun setOnLongClickListener1(listener: OnLongClickListener?) {
        btn1?.setOnLongClickListener { v ->
            resetAutoHideTimer()
            listener?.onLongClick(v) ?: false
        }
    }

    fun setText1(text: String?) {
        btn1?.text = text
    }

    fun setOnClickListener2(listener: OnClickListener?) {
        btn2?.setOnClickListener { v ->
            resetAutoHideTimer()
            listener?.onClick(v)
        }
    }

    fun setOnLongClickListener2(listener: OnLongClickListener?) {
        btn2?.setOnLongClickListener { v ->
            resetAutoHideTimer()
            listener?.onLongClick(v) ?: false
        }
    }

    fun setText2(text: String?) {
        btn2?.text = text
    }

    companion object {
        private var activeInstanceRef: WeakReference<ToolBarFloating>? = null

        var activeInstance: ToolBarFloating?
            get() = activeInstanceRef?.get()
            private set(value) {
                activeInstanceRef = if (value != null) WeakReference(value) else null
            }

        fun attachGlobalWindowCallback(activity: Activity) {
            val window = activity.window ?: return
            val currentCallback = window.callback ?: return
            if (currentCallback is GlobalWindowCallback) return

            window.callback = GlobalWindowCallback(currentCallback)
        }
    }

    private class GlobalWindowCallback(
        private val delegate: Window.Callback
    ) : Window.Callback by delegate {
        override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
            if (event != null) {
                val active = activeInstance
                if (active != null && active.isShown) {
                    active.handleOutsideTouch(event)
                }
            }
            return delegate.dispatchTouchEvent(event)
        }
    }
}
