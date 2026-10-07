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
import androidx.core.view.isVisible
import com.kota.asFramework.ui.ASToast

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

    /** 是否靠左 (僅決定選單展開方向) */
    private var isAnchoredLeft: Boolean = false

    var isExpanded: Boolean = false
        private set

    private var autoHideDelayMs: Long = 3000L
    private var collapsedAlpha: Float = 0.4f

    private var isDragging = false
    private var isSwiping = false
    private var isPositioningMode = false
    private var swipeDirection = 0
    private var swipeActionExecuted = false

    private var touchDownRawX = 0f
    private var touchDownRawY = 0f
    private var initialTranslationX = 0f
    private var initialTranslationY = 0f

    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoHideRunnable = Runnable {
        if (isExpanded) collapse()
    }

    private val longPressCenterRunnable = Runnable {
        if (!isDragging && !isSwiping) {
            isPositioningMode = true
            cardFab?.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            com.kota.asFramework.ui.ASToast.showLongToast("鬆開手指定位")
        }
    }

    private val swipeHoldRunnable = Runnable {
        if (isSwiping && !swipeActionExecuted) {
            swipeActionExecuted = true
            cardFab?.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            if (swipeDirection == -1) actionSwipeUpHold()
            else if (swipeDirection == 1) actionSwipeDownHold()
        }
    }

    // --- 行為定義區 (未來可擴充) ---
    private fun actionSwipeUp() { btn1?.performClick() }
    private fun actionSwipeDown() { btn2?.performClick() }
    private fun actionSwipeUpHold() { btn1?.performLongClick() }
    private fun actionSwipeDownHold() { btn2?.performLongClick() }

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

        updateSettings()

        cardActionsContainer?.visibility = View.GONE
        cardActionsContainer?.alpha = 0f
        cardFab?.alpha = collapsedAlpha

        // 初始套用位置
        post { applyPercentagePosition(animateFromCurrent = false) }

        cardFab?.setOnClickListener {
            if (isExpanded) collapse() else expand()
        }
        cardFab?.setOnTouchListener(fabTouchListener)
    }

    private fun handleFabTouch(v: View, event: MotionEvent): Boolean {
        val density = resources.displayMetrics.density
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                animate().cancel()
                isDragging = false
                isSwiping = false
                isPositioningMode = false
                swipeActionExecuted = false
                touchDownRawX = event.rawX
                touchDownRawY = event.rawY
                initialTranslationX = translationX
                initialTranslationY = translationY

                v.parent?.requestDisallowInterceptTouchEvent(true)
                parent?.requestDisallowInterceptTouchEvent(true)

                mainHandler.removeCallbacks(longPressCenterRunnable)
                mainHandler.removeCallbacks(swipeHoldRunnable)
                mainHandler.postDelayed(longPressCenterRunnable, 1000L)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - touchDownRawX
                val dy = event.rawY - touchDownRawY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                if (!isDragging && !isSwiping && dist > touchSlop) {
                    mainHandler.removeCallbacks(longPressCenterRunnable)
                    if (isPositioningMode) {
                        isDragging = true
                    } else {
                        isSwiping = true
                        swipeDirection = if (dy < 0) -1 else 1
                        mainHandler.postDelayed(swipeHoldRunnable, 1000L)
                    }
                }

                if (isDragging || isSwiping) {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    parent?.requestDisallowInterceptTouchEvent(true)
                    if (isExpanded) collapse()
                    translationX = initialTranslationX + dx
                    translationY = initialTranslationY + dy
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mainHandler.removeCallbacks(longPressCenterRunnable)
                mainHandler.removeCallbacks(swipeHoldRunnable)

                val dx = event.rawX - touchDownRawX
                val dy = event.rawY - touchDownRawY

                if (isPositioningMode && event.actionMasked == MotionEvent.ACTION_UP) {
                    savePositionAsPercentage()
                    applyPercentagePosition(animateFromCurrent = true)
                } else if (isSwiping && event.actionMasked == MotionEvent.ACTION_UP) {
                    if (!swipeActionExecuted) {
                        if (kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                            if (swipeDirection == -1) actionSwipeUp()
                            else actionSwipeDown()
                        }
                    }
                    applyPercentagePosition(animateFromCurrent = true)
                } else if (isDragging || isSwiping) {
                    applyPercentagePosition(animateFromCurrent = true)
                } else {
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                }

                isDragging = false
                isSwiping = false
                isPositioningMode = false
                return true
            }
            else -> return false
        }
    }

    private fun savePositionAsPercentage() {
        val parentView = parent as? View ?: return
        val pw = parentView.width.toFloat()
        val ph = parentView.height.toFloat()
        if (pw == 0f || ph == 0f) return

        val fabLoc = IntArray(2)
        cardFab?.getLocationOnScreen(fabLoc)
        val fabCenterX = fabLoc[0] + (cardFab?.width ?: 0) / 2f
        val fabCenterY = fabLoc[1] + (cardFab?.height ?: 0) / 2f

        val parentLoc = IntArray(2)
        parentView.getLocationOnScreen(parentLoc)
        val relX = fabCenterX - parentLoc[0]
        val relY = fabCenterY - parentLoc[1]

        var pctX = relX / pw
        var pctY = relY / ph

        pctX = pctX.coerceIn(0.1f, 0.9f)
        pctY = pctY.coerceIn(0.1f, 0.9f)

        setFloatingLocation(pctX, pctY)
    }

    private fun applyPercentagePosition(animateFromCurrent: Boolean = false) {
        val parentView = parent as? View ?: return
        val pw = parentView.width.toFloat()
        val ph = parentView.height.toFloat()
        if (pw == 0f || ph == 0f) return

        val density = resources.displayMetrics.density
        val fabHalf = 30f * density

        val loc = floatingLocation
        var pctX = if (loc.isNotEmpty() && loc[0]!! >= 0f) loc[0]!! else 0.9f
        var pctY = if (loc.isNotEmpty() && loc[1]!! >= 0f) loc[1]!! else 0.9f

        // Migrate legacy pixel positions to 90%
        if (pctX > 1.0f) pctX = 0.9f
        if (pctY > 1.0f) pctY = 0.9f

        pctX = pctX.coerceIn(0.1f, 0.9f)
        pctY = pctY.coerceIn(0.1f, 0.9f)

        val newIsLeft = pctX < 0.5f
        if (newIsLeft != isAnchoredLeft) {
            applyAnchorSide(newIsLeft)
        }

        val p = layoutParams
        if (p is RelativeLayout.LayoutParams) {
            p.removeRule(RelativeLayout.ALIGN_PARENT_START)
            p.removeRule(RelativeLayout.ALIGN_PARENT_END)
            p.removeRule(RelativeLayout.ALIGN_PARENT_LEFT)
            p.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            p.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
            p.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

            p.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            p.topMargin = (ph * pctY - fabHalf).toInt()

            if (newIsLeft) {
                p.addRule(RelativeLayout.ALIGN_PARENT_START)
                p.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                p.leftMargin = (pw * pctX - fabHalf).toInt()
                p.rightMargin = 0
                p.marginStart = p.leftMargin
                p.marginEnd = 0
            } else {
                p.addRule(RelativeLayout.ALIGN_PARENT_END)
                p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                p.rightMargin = (pw - pw * pctX - fabHalf).toInt()
                p.leftMargin = 0
                p.marginEnd = p.rightMargin
                p.marginStart = 0
            }

            if (animateFromCurrent) {
                val locBefore = IntArray(2)
                getLocationOnScreen(locBefore)

                layoutParams = p

                post {
                    val locAfter = IntArray(2)
                    getLocationOnScreen(locAfter)
                    val dx = locBefore[0] - locAfter[0]
                    val dy = locBefore[1] - locAfter[1]

                    translationX += dx
                    translationY += dy

                    animate()
                        .translationX(0f)
                        .translationY(0f)
                        .setDuration(200L)
                        .start()
                }
            } else {
                layoutParams = p
                translationX = 0f
                translationY = 0f
            }
        }
    }

    private fun applyAnchorSide(isLeft: Boolean) {
        isAnchoredLeft = isLeft
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
            (60 * density).toInt()
        )

        if (isLeft) {
            fab.layoutParams = fabParams
            root.addView(fab)
            actionsParams.marginStart = gapPx
            actionsParams.leftMargin = gapPx
            actionsParams.marginEnd = 0
            actionsParams.rightMargin = 0
            actionsCard.layoutParams = actionsParams
            root.addView(actionsCard)
        } else {
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
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isDragging || isSwiping) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activeInstance = this
        post { applyPercentagePosition(animateFromCurrent = false) }
        findActivity()?.let { attachGlobalWindowCallback(it) }
    }

    override fun onDetachedFromWindow() {
        cancelAutoHideTimer()
        if (activeInstance == this) {
            activeInstance = null
        }
        super.onDetachedFromWindow()
    }

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
        container.visibility = VISIBLE

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
                            container.visibility = GONE
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
        if (visibility == VISIBLE) {
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

        val fabHit = if (fab != null && fab.isVisible) isViewHit(fab, rawX, rawY) else false
        val actionsHit = if (isExpanded && actions != null && actions.isVisible) isViewHit(actions, rawX, rawY) else false

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
