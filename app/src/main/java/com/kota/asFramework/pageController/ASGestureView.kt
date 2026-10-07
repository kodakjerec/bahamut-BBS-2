package com.kota.asFramework.pageController

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * [ASGestureView] - 手勢偵測 FrameLayout 容器。
 *
 * 職責：
 * 1. 攔截並分析上、下、左、右滑動手勢 ([onFling])。
 * 2. 透過 [ASGestureViewDelegate] 代理介面將滑動事件發送至頁面控制器（如向右滑動返回上頁）。
 */
class ASGestureView(paramContext: Context?) : FrameLayout(paramContext!!),
    GestureDetector.OnGestureListener {

    /** 手勢事件委派介面，用來通知外部 (如 ASNavigationControllerView) 手勢觸發結果 */
    private var gestureViewDelegate: ASGestureViewDelegate? = null
    /** 用來標記當前觸控事件是否已經被攔截處理 (例如已觸發換頁手勢)，若鎖定則不再將事件傳遞給底層視圖 */
    private var eventLocked = false
    /** 標記手指按下時是否位於浮動工具列上。若是，則免疫全域手勢偵測，避免與工具列拖曳/滑動衝突 */
    private var isToolbarTracking = false
    /** Android 原生的手勢偵測器，用來解析複雜的觸控序列 (如 Fling) */
    private val gestureDetector: GestureDetector = GestureDetector(paramContext, this)

    init {
        filter = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            550.0f,
            paramContext!!.resources.displayMetrics
        ).toInt()
    }

    /**
     * 手指初次碰到螢幕時觸發。
     *
     * @return 必須回傳 true，代表接管後續觸控事件序列。
     */
    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    /**
     * 手指在螢幕上快速滑動後離開時觸發。
     * 內部會判斷滑動速度與方向(上下左右)，並透過 [gestureViewDelegate] 進行相應動作。
     * 若成功判別為全域滑動且有處理，會將事件鎖定 ([eventLocked] = true) 並傳送 CANCEL 給子視圖。
     *
     * @param paramMotionEvent1 按下時的 MotionEvent
     * @param paramMotionEvent2 滑動離開時的 MotionEvent
     * @param paramFloat1 X軸上的滑動速度 (pixels/second)
     * @param paramFloat2 Y軸上的滑動速度 (pixels/second)
     * @return 是否已消耗此滑動事件
     */
    override fun onFling(
        paramMotionEvent1: MotionEvent?,
        paramMotionEvent2: MotionEvent,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        var bool1 = false
        if (this.gestureViewDelegate != null) {
            val f2 = abs(paramFloat1)
            val f1 = abs(paramFloat2)
            if (f2 > filter && f2 > range * f1) {
                bool1 = if (paramFloat1 > 0.0f) {
                    this.gestureViewDelegate!!.onASGestureReceivedGestureRight()
                } else {
                    this.gestureViewDelegate!!.onASGestureReceivedGestureLeft()
                }
            } else {
                if (f1 > filter) {
                    if (f1 > range * f2) {
                        bool1 = if (paramFloat2 > 0.0f) {
                            this.gestureViewDelegate!!.onASGestureReceivedGestureDown()
                        } else {
                            this.gestureViewDelegate!!.onASGestureReceivedGestureUp()
                        }
                    }
                }
            }
        }
        this.eventLocked = bool1
        if (bool1 && this.gestureViewDelegate != null) {
            val cancelEvent = MotionEvent.obtain(paramMotionEvent2)
            cancelEvent.action = MotionEvent.ACTION_CANCEL
            this.gestureViewDelegate?.onASGestureDisPathTouchEvent(cancelEvent)
            cancelEvent.recycle()
        }
        return bool1
    }

    /** 長按時觸發 (此處未實作額外行為) */
    override fun onLongPress(p0: MotionEvent) {}

    /**
     * 手指在螢幕上拖曳滾動時觸發。
     * (此處回傳 false 交給子視圖自行處理 Scroll 行為)
     */
    override fun onScroll(
        paramMotionEvent2: MotionEvent?,
        p1: MotionEvent,
        paramFloat2: Float,
        p3: Float
    ): Boolean {
        return false
    }

    /** 手指輕觸螢幕尚未鬆開或拖曳時觸發 (此處未實作額外行為) */
    override fun onShowPress(p0: MotionEvent) {}

    /** 輕觸後立即鬆開時觸發 (代表單擊) */
    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    /**
     * 攔截並處理所有觸控事件：
     * 1. 檢查是否在工具列上 ([isToolbarTracking])。
     * 2. 若不在工具列上，則交給 [gestureDetector] 解析手勢 (如 Fling)。
     * 3. 若手勢未被鎖定 ([eventLocked] == false)，則將事件傳遞給 [gestureViewDelegate] 分發給底層視圖。
     * 4. 處理完 UP 或 CANCEL 時，重置狀態。
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(paramMotionEvent: MotionEvent): Boolean {
        if (paramMotionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
            val floatingToolbar = com.kota.Bahamut.pages.model.ToolBarFloating.activeInstance
            isToolbarTracking = floatingToolbar != null && floatingToolbar.isShown && floatingToolbar.isTouchInsideToolbar(paramMotionEvent.rawX, paramMotionEvent.rawY)
        }

        if (!isToolbarTracking) {
            this.gestureDetector.onTouchEvent(paramMotionEvent)
        }

        if (!this.eventLocked && this.gestureViewDelegate != null) {
            this.gestureViewDelegate?.onASGestureDisPathTouchEvent(paramMotionEvent)
        }

        if (paramMotionEvent.actionMasked == MotionEvent.ACTION_UP || paramMotionEvent.actionMasked == MotionEvent.ACTION_CANCEL) {
            this.eventLocked = false
            this.isToolbarTracking = false
        }
        return true
    }

    /**
     * 設定手勢事件代理者。
     * 通常由外層的 NavigationController 呼叫以接收全域手勢回調。
     */
    fun setDelegate(paramASGestureViewDelegate: ASGestureViewDelegate?) {
        this.gestureViewDelegate = paramASGestureViewDelegate
    }

    companion object {
        /** 觸發滑動判定的最小速度閾值 (滑動速度需大於此值) */
        var filter: Int = 550
        /** 滑動方向的敏感度比例 (用來判斷是水平還是垂直滑動，需大於另一軸的 [range] 倍) */
        var range: Float = 2.4f
    }
}
