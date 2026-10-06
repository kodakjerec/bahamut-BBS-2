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
import android.view.ViewConfiguration
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
 * - 按住圓點可任意拖動，放開手指時依據是否超過中線，自動吸附至左下角或右下角
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
 * 3. 閒置逾時 (預設 3 秒，沿用 UserSetting)
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

    /** FAB 手勢觸控監聽器 */
    @SuppressLint("ClickableViewAccessibility")
    private val fabTouchListener = OnTouchListener { v, event ->
        handleFabTouch(v, event)
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

        // 初始為收合狀態 (actionsCard 為 GONE，避免佔位及遮擋點擊)
        cardActionsContainer?.visibility = View.GONE
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

        // FAB 手勢觸控 (支援任意拖曳移動與中線判斷貼合左/右下)
        cardFab?.setOnTouchListener(fabTouchListener)
    }

    /** FAB 手勢觸控處理：支援按住任意拖動，放開時依中線判斷貼合左下或右下 */
    private fun handleFabTouch(v: View, event: MotionEvent): Boolean {
        val density = resources.displayMetrics.density
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                animate().cancel()
                isDragging = false
                touchDownRawX = event.rawX
                touchDownRawY = event.rawY
                initialTranslationX = translationX
                initialTranslationY = translationY

                v.parent?.requestDisallowInterceptTouchEvent(true)
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - touchDownRawX
                val dy = event.rawY - touchDownRawY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                if (!isDragging && dist > touchSlop) {
                    isDragging = true
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    parent?.requestDisallowInterceptTouchEvent(true)
                    if (isExpanded) {
                        collapse()
                    }
                }

                if (isDragging) {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    parent?.requestDisallowInterceptTouchEvent(true)

                    // 按住按鈕時可以任意拖動 (即時跟隨手指 X, Y)
                    val parentView = parent as? View
                    val parentHeight = (if (parentView != null && parentView.height > 0) parentView.height else resources.displayMetrics.heightPixels).toFloat()
                    val fabSize = 60f * density
                    val maxDown = 80f * density
                    val maxUp = -(parentHeight - 80f * density - fabSize)

                    translationX = initialTranslationX + dx
                    translationY = (initialTranslationY + dy).coerceIn(maxUp, maxDown)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    isDragging = false

                    // 取得螢幕寬度與中線
                    val screenWidth = resources.displayMetrics.widthPixels.toFloat()
                    val screenCenterX = screenWidth / 2f

                    // 取得放開時 FAB 在螢幕上的中心點 X
                    val fabLoc = IntArray(2)
                    v.getLocationOnScreen(fabLoc)
                    val fabCenterX = fabLoc[0] + v.width / 2f

                    // 當拖動超過中線，判斷屬於左下角還是右下角
                    val targetIsLeft = fabCenterX < screenCenterX

                    val marginPx = 16f * density
                    val fabWidth = v.width.toFloat()

                    val parentView = parent as? View
                    val containerWidth = (if (parentView != null && parentView.width > 0) parentView.width else screenWidth.toInt()).toFloat()

                    // 計算動畫吸附至左下角或右下角的目標 translationX
                    val targetTranslationX = if (targetIsLeft == isAnchoredLeft) {
                        0f
                    } else {
                        if (isAnchoredLeft) {
                            // 當前靠左錨點，目標靠右下角：向右平移至 (containerWidth - marginPx - fabWidth)
                            (containerWidth - marginPx - fabWidth) - marginPx
                        } else {
                            // 當前靠右錨點，目標靠左下角：向左平移至 marginPx (負值)
                            marginPx - (containerWidth - marginPx - fabWidth)
                        }
                    }

                    // 放開手指時平滑吸附至左下角或右下角 (Y 軸平滑回彈至 0f)
                    animate()
                        .translationX(targetTranslationX)
                        .translationY(0f)
                        .setDuration(200L)
                        .withEndAction {
                            if (targetIsLeft != isAnchoredLeft) {
                                applyAnchorSide(targetIsLeft)
                            }
                            translationX = 0f
                            translationY = 0f
                        }
                        .start()
                } else {
                    // 點擊事件：切換展開/收合
                    v.performClick()
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    animate()
                        .translationX(0f)
                        .translationY(0f)
                        .setDuration(200L)
                        .start()
                }
                return true
            }

            else -> return false
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
            actionsParams.leftMargin = gapPx
            actionsParams.marginEnd = 0
            actionsParams.rightMargin = 0
            actionsCard.layoutParams = actionsParams
            root.addView(actionsCard)
        } else {
            // 靠右下： ActionsCard (左) + FAB (右)
            actionsParams.marginEnd = gapPx
            actionsParams.rightMargin = gapPx
            actionsParams.marginStart = 0
            actionsParams.leftMargin = 0
            actionsCard.layoutParams = actionsParams
            root.addView(actionsCard)

            fab.layoutParams = fabParams
            root.addView(fab)
        }

        fab.setOnTouchListener(fabTouchListener)
        enforcePositioning()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isDragging) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
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
            p.removeRule(RelativeLayout.ALIGN_PARENT_LEFT)
            p.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)

            if (isAnchoredLeft) {
                p.addRule(RelativeLayout.ALIGN_PARENT_START)
                p.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                p.leftMargin = marginPx
                p.rightMargin = 0
                p.marginStart = marginPx
                p.marginEnd = 0
            } else {
                p.addRule(RelativeLayout.ALIGN_PARENT_END)
                p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                p.rightMargin = marginPx
                p.leftMargin = 0
                p.marginEnd = marginPx
                p.marginStart = 0
            }
            p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            p.bottomMargin = marginBottomPx
            layoutParams = p
        } else if (p is LayoutParams) {
            p.gravity = gravityVal
            if (isAnchoredLeft) {
                p.leftMargin = marginPx
                p.rightMargin = 0
                p.marginStart = marginPx
                p.marginEnd = 0
            } else {
                p.rightMargin = marginPx
                p.leftMargin = 0
                p.marginEnd = marginPx
                p.marginStart = 0
            }
            p.bottomMargin = marginBottomPx
            layoutParams = p
        }

        // 同步更新內部 LinearLayout 的 layout_gravity
        val root = findViewById<LinearLayout>(R.id.ToolbarFloating)
        if (root != null) {
            val rootLp = root.layoutParams as? LayoutParams
            if (rootLp != null) {
                rootLp.gravity = gravityVal
                root.layoutParams = rootLp
            }
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
        container.alpha = 0f
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
                            container.visibility = View.GONE
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
            enforcePositioning()
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
        val fab = cardFab
        val actions = cardActionsContainer

        val fabHit = if (fab != null && fab.visibility == View.VISIBLE) isViewHit(fab, rawX, rawY) else false
        val actionsHit = if (isExpanded && actions != null && actions.visibility == View.VISIBLE) isViewHit(actions, rawX, rawY) else false

        return fabHit || actionsHit
    }

    private fun isViewHit(v: View, rawX: Float, rawY: Float): Boolean {
        val loc = IntArray(2)
        v.getLocationOnScreen(loc)
        val left = loc[0].toFloat()
        val top = loc[1].toFloat()
        val right = left + v.width.toFloat()
        val bottom = top + v.height.toFloat()
        val buffer = 6f * resources.displayMetrics.density
        return rawX >= (left - buffer) && rawX <= (right + buffer) &&
               rawY >= (top - buffer) && rawY <= (bottom + buffer)
    }

    /** 處理外部點擊事件：若展開狀態且點擊 outside，聯動收合 */
    fun handleOutsideTouch(event: MotionEvent) {
        if (!isExpanded || !isShown || isDragging) return

        if (event.action == MotionEvent.ACTION_DOWN) {
            if (!isTouchInsideToolbar(event.rawX, event.rawY)) {
                collapse()
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
