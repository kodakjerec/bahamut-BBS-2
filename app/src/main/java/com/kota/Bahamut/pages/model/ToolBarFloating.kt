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
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.isVisible
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
 * - 記錄百分比位置於 UserSetting (floatingLocation)，畫面直立橫放皆保持相對比例
 * - 透明度沿用 UserSetting (預設 40%)
 *
 * 手勢操作功能：
 * - 上滑：上一頁/上一篇 (觸發 btn1 點擊)
 * - 下滑：下一頁/下一篇 (觸發 btn2 點擊)
 * - 上滑按住超過1秒：最前頁 (觸發 btn1 長按)
 * - 下滑按住超過1秒：最後頁 (觸發 btn2 長按)
 * - 長按圓點0.5秒：進入「自訂位置模式」(震動一次並顯示提示)，此時可拖動，鬆開手指儲存百分比座標，強制內縮至少10%邊緣。
 *
 * 展開狀態：
 * - 點擊圓形按鈕後水平展開，顯示詳細選單按鈕
 * - 若位於畫面右半部，向左展開；若位於畫面左半部，向右展開
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

    /** 展開時包含各個操作按鈕的橫向卡片容器 */
    private var cardActionsContainer: MaterialCardView? = null
    /** 圓形的浮動操作按鈕 (FAB)，做為工具列的收合/展開觸發點與拖曳錨點 */
    private var cardFab: MaterialCardView? = null

    /** 系統設定按鈕 (齒輪圖示) */
    private var btnSetting: Button? = null
    /** 第一顆操作按鈕 (預設為上一頁/上一篇) */
    private var btn1: Button? = null
    /** 第二顆操作按鈕 (預設為下一頁/下一篇) */
    private var btn2: Button? = null

    /** 是否靠左 (僅決定選單展開方向：靠左則向右展開，反之亦然) */
    private var isAnchoredLeft: Boolean = false

    /** 記錄目前工具列是否處於展開狀態 */
    var isExpanded: Boolean = false
        private set

    /** 工具列閒置自動收合的延遲時間 (單位：毫秒)，預設 3000 毫秒 */
    private var autoHideDelayMs: Long = 3000L
    /** 工具列收合時的透明度，由 UserSetting 取得，範圍 0.1f ~ 1.0f */
    private var collapsedAlpha: Float = 0.4f

    /** 判斷目前是否處於「拖曳」狀態 (用來平移工具列座標) */
    private var isDragging = false
    /** 判斷目前是否處於「滑動」狀態 (尚未達到長按條件時的上下位移) */
    private var isSwiping = false
    /** 判斷目前是否進入「自訂位置模式」(長按 FAB 0.5秒後觸發)，此模式下可拖曳儲存座標 */
    private var isPositioningMode = false
    /** 記錄滑動方向：-1 表示上滑，1 表示下滑 */
    private var swipeDirection = 0
    /** 標記在一次滑動過程中，是否已經觸發過「長按滑動」的對應事件 (如：最前頁/最後頁)，避免重複觸發 */
    private var swipeActionExecuted = false

    /** 記錄觸控事件 (ACTION_DOWN) 發生時的螢幕絕對 X 座標 */
    private var touchDownRawX = 0f
    /** 記錄觸控事件 (ACTION_DOWN) 發生時的螢幕絕對 Y 座標 */
    private var touchDownRawY = 0f
    /** 記錄觸控事件 (ACTION_DOWN) 發生時，元件目前的 X 軸平移量 (Translation X) */
    private var initialTranslationX = 0f
    /** 記錄觸控事件 (ACTION_DOWN) 發生時，元件目前的 Y 軸平移量 (Translation Y) */
    private var initialTranslationY = 0f

    /** 主要執行緒的 Handler，用於處理延遲任務 (如自動隱藏、長按判定) */
    private val mainHandler = Handler(Looper.getMainLooper())
    /** 閒置自動收合工具列的排程任務 */
    private val autoHideRunnable = Runnable {
        if (isExpanded) collapse()
    }

    /** 判斷「長按圓點中心」的排程任務。若觸控未移動超過 0.5 秒，即進入 isPositioningMode 並產生震動與 Toast 提示 */
    private val longPressCenterRunnable = Runnable {
        if (!isDragging && !isSwiping) {
            isPositioningMode = true
            cardFab?.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            com.kota.asFramework.ui.ASToast.showLongToast("鬆開手指定位")
        }
    }

    /** 判斷「長按滑動」的排程任務。若滑動後保持按住超過 1 秒，即觸發對應方向的長按行為 (如：最前頁/最後頁) */
    private val swipeHoldRunnable = Runnable {
        if (isSwiping && !swipeActionExecuted) {
            swipeActionExecuted = true
            cardFab?.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            if (swipeDirection == -1) actionSwipeUpHold()
            else if (swipeDirection == 1) actionSwipeDownHold()
        }
    }

    // --- 行為定義區 (未來可擴充，綁定手勢與按鈕行為) ---
    /** 上滑：上一頁/上一篇 */
    private fun actionSwipeUp() { btn1?.performClick() }
    /** 下滑：下一頁/下一篇 */
    private fun actionSwipeDown() { btn2?.performClick() }
    /** 上滑按住超過1秒：最前頁 */
    private fun actionSwipeUpHold() { btn1?.performLongClick() }
    /** 下滑按住超過1秒：最後頁 */
    private fun actionSwipeDownHold() { btn2?.performLongClick() }

    @SuppressLint("ClickableViewAccessibility")
    private val fabTouchListener = OnTouchListener { v, event ->
        handleFabTouch(v, event)
    }

    init {
        init(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    /** 初始化元件，綁定視圖與事件，並套用預設設定與位置 */
    private fun init(context: Context) {
        inflate(context, R.layout.toolbar_floating, this)
        cardActionsContainer = findViewById(R.id.ToolbarFloating_actions_card)
        cardFab = findViewById(R.id.ToolbarFloating_fab)
        btnSetting = findViewById(R.id.ToolbarFloating_setting)
        btn1 = findViewById(R.id.ToolbarFloating_1)
        btn2 = findViewById(R.id.ToolbarFloating_2)

        updateSettings()

        cardActionsContainer?.visibility = GONE
        cardActionsContainer?.alpha = 0f
        cardFab?.alpha = collapsedAlpha

        // 初始套用位置
        post { applyPercentagePosition(animateFromCurrent = false) }

        cardFab?.setOnClickListener {
            if (isExpanded) collapse() else expand()
        }
        cardFab?.setOnTouchListener(fabTouchListener)
    }

    /** 處理浮動按鈕的觸控事件，包含點擊、拖曳定位、滑動換頁及長按等手勢判斷 */
    private fun handleFabTouch(v: View, event: MotionEvent): Boolean {
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 手指剛按下時：重置所有狀態 (拖曳、滑動、定位模式)
                // 並記錄初始座標與目前的 translation，以便後續計算位移量
                animate().cancel()
                isDragging = false
                isSwiping = false
                isPositioningMode = false
                swipeActionExecuted = false
                touchDownRawX = event.rawX
                touchDownRawY = event.rawY
                initialTranslationX = translationX
                initialTranslationY = translationY

                // 請求父視圖不要攔截此觸控事件，確保我們能完整捕捉到 ACTION_MOVE 與 ACTION_UP
                v.parent?.requestDisallowInterceptTouchEvent(true)
                parent?.requestDisallowInterceptTouchEvent(true)

                // 取消先前的計時器，並重新設定 0.5 秒的長按計時器 (用於觸發自訂位置模式)
                mainHandler.removeCallbacks(longPressCenterRunnable)
                mainHandler.removeCallbacks(swipeHoldRunnable)
                mainHandler.postDelayed(longPressCenterRunnable, 500L)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 手指移動時：計算與初始按下的直線距離 (dist)
                val dx = event.rawX - touchDownRawX
                val dy = event.rawY - touchDownRawY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                // 若尚未判定為拖曳或滑動，且移動距離超過系統定義的防誤觸範圍 (touchSlop)：
                if (!isDragging && !isSwiping && dist > touchSlop) {
                    mainHandler.removeCallbacks(longPressCenterRunnable)
                    // 一旦移動超過 touchSlop，即視為中斷了原本的長按定位條件
                    // 但若已經進入了定位模式 (isPositioningMode = true)，則切換為拖曳狀態 (isDragging)
                    if (isPositioningMode) {
                        isDragging = true
                    } else {
                        // 若未進入定位模式，則視為「滑動」操作 (isSwiping)
                        // 並根據 Y 軸的變化量決定是上滑 (-1) 還是下滑 (1)，並啟動滑動長按計時器
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
                // 手指放開或事件取消：清除所有正在倒數的計時器
                mainHandler.removeCallbacks(longPressCenterRunnable)
                mainHandler.removeCallbacks(swipeHoldRunnable)

                val dx = event.rawX - touchDownRawX
                val dy = event.rawY - touchDownRawY

                // 若處於「自訂位置模式」，放開手指即代表定位完成：儲存百分比並套用新位置
                if (isPositioningMode && event.actionMasked == MotionEvent.ACTION_UP) {
                    savePositionAsPercentage()
                    applyPercentagePosition(animateFromCurrent = true)
                } else if (isSwiping && event.actionMasked == MotionEvent.ACTION_UP) {
                    // 若為滑動操作，且未觸發長按 (swipeActionExecuted = false)，
                    // 則判斷 Y 軸位移量是否大於 X 軸，如果是純粹的上下滑動，則執行相應的換頁行為
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
                    // 如果僅是普通的單擊操作，則觸發點擊事件 (展開/收合選單)
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

    /** 將目前浮動按鈕的位置轉換為相對於父視圖的百分比 (X, Y)，並確保不超出10%~90%範圍，最後儲存至 UserSetting */
    private fun savePositionAsPercentage() {
        val parentView = parent as? View ?: return
        val pw = parentView.width.toFloat()
        val ph = parentView.height.toFloat()
        if (pw == 0f || ph == 0f) return

        // 1. 取得浮動按鈕 (FAB) 位於螢幕的絕對座標，並計算其中心點
        val fabLoc = IntArray(2)
        cardFab?.getLocationOnScreen(fabLoc)
        val fabCenterX = fabLoc[0] + (cardFab?.width ?: 0) / 2f
        val fabCenterY = fabLoc[1] + (cardFab?.height ?: 0) / 2f

        // 2. 取得父視圖位於螢幕的絕對座標，進而求出 FAB 中心相對於父視圖的相對座標 (relX, relY)
        val parentLoc = IntArray(2)
        parentView.getLocationOnScreen(parentLoc)
        val relX = fabCenterX - parentLoc[0]
        val relY = fabCenterY - parentLoc[1]

        // 3. 將相對座標轉換為百分比 (0.0 ~ 1.0)
        var pctX = relX / pw
        var pctY = relY / ph

        // 4. 強制限制在 10% ~ 90% 之間，避免按鈕過於貼近螢幕邊緣導致無法觸控或被裁切
        pctX = pctX.coerceIn(0.1f, 0.9f)
        pctY = pctY.coerceIn(0.1f, 0.9f)

        setFloatingLocation(pctX, pctY)
    }

    /** 根據 UserSetting 中的百分比座標重新計算並套用浮動按鈕的位置，並可選擇是否加上平滑移動動畫 */
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

        // 相容舊版以像素 (Pixel) 儲存的座標值：若讀取到的值大於 1，強制修正為 0.9 (90%)
        // Migrate legacy pixel positions to 90%
        if (pctX > 1.0f) pctX = 0.9f
        if (pctY > 1.0f) pctY = 0.9f

        // 4. 強制限制在 10% ~ 90% 之間，避免按鈕過於貼近螢幕邊緣導致無法觸控或被裁切
        pctX = pctX.coerceIn(0.1f, 0.9f)
        pctY = pctY.coerceIn(0.1f, 0.9f)

        // 判斷按鈕位於畫面左半邊或右半邊，藉此決定選單展開時的方向
        val newIsLeft = pctX < 0.5f
        if (newIsLeft != isAnchoredLeft) {
            applyAnchorSide(newIsLeft)
        }

        val p = layoutParams
        if (p is RelativeLayout.LayoutParams) {
            // 清除所有的對齊規則，以便我們根據百分比重新定義 LayoutParams
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

            // 若設定 animateFromCurrent 為 true (如拖曳放開時)，計算佈局更新前後的位移差 (dx, dy)，
            // 並透過 translation 屬性將其平滑移動到新座標。
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

    /** 調整展開選單的方向：若按鈕位於畫面左側則選單向右展開，若位於右側則向左展開 */
    @SuppressLint("ClickableViewAccessibility")
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

    /** 覆寫事件分發，確保在拖曳或滑動時能正確攔截事件，防止底層視圖 (如看板列表或文章內容) 被誤觸或跟著捲動 */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isDragging || isSwiping) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
    }

    /** 當元件附加到視窗時觸發，將自身註冊為 activeInstance 並開始監聽全域觸控事件 */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activeInstance = this
        post { applyPercentagePosition(animateFromCurrent = false) }
        findActivity()?.let { attachGlobalWindowCallback(it) }
    }

    /** 當元件從視窗移除時觸發，清除閒置隱藏計時器與實體參照，避免記憶體洩漏 (Memory Leak) */
    override fun onDetachedFromWindow() {
        cancelAutoHideTimer()
        if (activeInstance == this) {
            activeInstance = null
        }
        super.onDetachedFromWindow()
    }

    /** 從 Context 中向上尋找所屬的 Activity，用於掛載全域視窗觸控監聽器 (GlobalWindowCallback) */
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
            post { applyPercentagePosition(animateFromCurrent = false) }
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
    fun isTouchInsideToolbar(rawX: Float, rawY: Float): Boolean {
        val fab = cardFab
        val actions = cardActionsContainer

        val fabHit = if (fab != null && fab.isVisible) isViewHit(fab, rawX, rawY) else false
        val actionsHit = if (isExpanded && actions != null && actions.isVisible) isViewHit(actions, rawX, rawY) else false

        return fabHit || actionsHit
    }

    /** 計算特定視圖的螢幕邊界 (加上緩衝區)，判斷觸控點是否落在該視圖範圍內 */
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

    /** 設定「系統設定」按鈕的點擊事件監聽器 */
    fun setOnClickListenerSetting(listener: OnClickListener?) {
        btnSetting?.setOnClickListener { v ->
            resetAutoHideTimer()
            listener?.onClick(v)
        }
    }

    /** 設定「系統設定」按鈕的顯示文字 */
    fun setTextSetting(text: String?) {
        btnSetting?.text = text
    }

    /** 設定「按鈕1」(第一顆選單按鈕) 的點擊事件監聽器 (通常對應上一頁/上一篇) */
    fun setOnClickListener1(listener: OnClickListener?) {
        btn1?.setOnClickListener { v ->
            resetAutoHideTimer()
            listener?.onClick(v)
        }
    }

    /** 設定「按鈕1」的長按事件監聽器 (通常對應最前頁) */
    fun setOnLongClickListener1(listener: OnLongClickListener?) {
        btn1?.setOnLongClickListener { v ->
            resetAutoHideTimer()
            listener?.onLongClick(v) ?: false
        }
    }

    /** 設定「按鈕1」的顯示文字 */
    fun setText1(text: String?) {
        btn1?.text = text
    }

    /** 設定「按鈕2」(第二顆選單按鈕) 的點擊事件監聽器 (通常對應下一頁/下一篇) */
    fun setOnClickListener2(listener: OnClickListener?) {
        btn2?.setOnClickListener { v ->
            resetAutoHideTimer()
            listener?.onClick(v)
        }
    }

    /** 設定「按鈕2」的長按事件監聽器 (通常對應最後頁) */
    fun setOnLongClickListener2(listener: OnLongClickListener?) {
        btn2?.setOnLongClickListener { v ->
            resetAutoHideTimer()
            listener?.onLongClick(v) ?: false
        }
    }

    /** 設定「按鈕2」的顯示文字 */
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

    /** 將觸控事件監聽器掛載到 Activity 的 Window 上，以便在使用者點擊工具列以外的地方時能自動收合選單 */
        fun attachGlobalWindowCallback(activity: Activity) {
            val window = activity.window ?: return
            val currentCallback = window.callback ?: return
            if (currentCallback is GlobalWindowCallback) return

            window.callback = GlobalWindowCallback(currentCallback)
        }
    }

    /**
     * 全域視窗回呼攔截器 (Decorator Pattern)。
     * 負責在 Activity 層級攔截所有的觸控事件，
     * 藉此判斷使用者是否點擊了浮動工具列以外的區域，以便自動收合選單。
     */
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
