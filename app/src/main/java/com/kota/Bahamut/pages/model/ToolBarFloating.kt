package com.kota.Bahamut.pages.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import com.kota.Bahamut.R
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.floatingLocation
import com.kota.Bahamut.service.UserSettings.Companion.setFloatingLocation
import com.kota.Bahamut.service.UserSettings.Companion.toolbarAlpha
import com.kota.Bahamut.service.UserSettings.Companion.toolbarIdle
import com.kota.asFramework.thread.ASCoroutine

/**
 * 浮動工具列元件 (`ToolBarFloating`)
 *
 * 提供懸浮於頁面側邊的快速導覽與操作按鈕（例如：發表、上一頁、下一頁）。
 * 支援全域觸控跟隨、靠左/靠右自動吸附、邊界保護（避免被系統底欄遮擋）以及底部防抖動鎖定功能。
 */
class ToolBarFloating(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    /** 主要 Layout 容器 */
    private var mainLayout: LinearLayout? = null
    /** 最上方按鈕（例如：發表） */
    private var btnSetting: Button? = null
    /** 中間按鈕（例如：上一頁 / 上一篇） */
    private var btn1: Button? = null
    /** 最下方按鈕（例如：下一頁 / 下一篇） */
    private var btn2: Button? = null
    /** 螢幕密度 (density) */
    private var scale = 0f

    /** 閒置自動半透明/隱藏時間 (秒) */
    private var idleTime = 0f
    /** 閒置時的不透明度比例 (0.0 ~ 1.0) */
    private var alphaPercentage = 0f

    /** 底部遲滯 (Hysteresis) 鎖定狀態：防止在頁面底部滾動時工具列微幅震動 */
    private var isLockedAtBottom = false

    /** 初始化元件，載入佈局、設定監聽器與復原上次紀錄的懸浮位置 */
    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context?) {
        idleTime = if (toolbarIdle <= 0f) 1.0f else toolbarIdle
        alphaPercentage = toolbarAlpha / 100f
        inflate(context, R.layout.toolbar_floating, this)
        scale = getContext().resources.displayMetrics.density

        mainLayout = findViewById(R.id.ToolbarFloating)

        // 嘗試讀取上次儲存的浮動位置
        val list = floatingLocation
        if (list.isNotEmpty() && list[0]!! >= 0.0f) {
            val pointX: Float = list[0]!!
            val pointY: Float = list[1]!!
            updateLayout(pointX, pointY, false)
        } else {
            // 預設位置：靠右邊界吸附，高度位於畫面中央
            val screenWidth = getContext().resources.displayMetrics.widthPixels.toFloat()
            val screenHeight = getContext().resources.displayMetrics.heightPixels.toFloat()
            updateLayout(screenWidth, screenHeight / 2f, false)
        }

        btnSetting = mainLayout?.findViewById(R.id.ToolbarFloating_setting)
        btn1 = mainLayout?.findViewById(R.id.ToolbarFloating_1)
        btn2 = mainLayout?.findViewById(R.id.ToolbarFloating_2)
        btnSetting?.setOnTouchListener(onTouchListener)

        // 若啟用閒置隱藏，自動設定透明度與計時器
        if (TempSettings.isFloatingInvisible) {
            mainLayout?.alpha = alphaPercentage
        } else {
            startInvisible()
        }
    }

    /**
     * 直接拖曳浮動工具列的手勢監聽器
     */
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { view: View?, event: MotionEvent? ->
        if (event == null) return@OnTouchListener false

        val duration = event.eventTime - event.downTime
        var pointX = event.rawX
        var pointY = event.rawY

        // 手指中心點微調：
        // pointX 扣除工具列寬度的一半 (80dp / 2 = 40dp)
        // pointY 扣除「上一頁」與「下一頁」中間的分隔線位置 (120dp)
        pointX -= scale * 40f
        pointY -= scale * 120f

        // 扣除容器在螢幕上的視窗偏移量 (Status bar 等 offset)
        val location = IntArray(2)
        getLocationOnScreen(location)
        pointX -= location[0].toFloat()
        pointY -= location[1].toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 手指按下時取消透明度並重置閒置計時
                cancelInvisible()
            }

            MotionEvent.ACTION_UP -> {
                if (duration < 200) {
                    // 短按 (點擊事件)：執行按鈕點擊
                    if (view is Button) {
                        view.performClick()
                    }
                } else {
                    // 長按或拖曳結束放開：將工具列靠左或靠右邊界吸附
                    updateLayout(pointX, pointY, false)
                }
                startInvisible()
            }

            MotionEvent.ACTION_MOVE -> {
                // 拖曳中：即時更新工具列位置
                updateLayout(pointX, pointY, true)
            }
        }
        true
    }

    init {
        init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activeInstance = this
        findActivity()?.let { attachGlobalWindowCallback(it) }
    }

    override fun onDetachedFromWindow() {
        if (activeInstance == this) {
            activeInstance = null
        }
        super.onDetachedFromWindow()
    }

    /** 尋找當前 View 所屬的 Activity */
    private fun findActivity(): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    /** 判斷全域觸控座標 (rawX, rawY) 是否落於浮動工具列本體的邊界內 */
    private fun isTouchInsideToolbar(rawX: Float, rawY: Float): Boolean {
        val targetView = mainLayout ?: this
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        val left = location[0].toFloat()
        val top = location[1].toFloat()
        val right = left + getBarWidth()
        val bottom = top + getBarHeight()
        return rawX in left..right && rawY in top..bottom
    }

    /**
     * 處理工具列外部的全域滑動事件 (當使用者在頁面其他區域滑動時，工具列跟隨 Y 軸滑動)
     */
    fun handleOutsideTouch(event: MotionEvent) {
        if (!isShown || mainLayout?.visibility != View.VISIBLE) return

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val rawX = event.rawX
                val rawY = event.rawY

                // 當觸控/滑動點在 ToolBarFloating 範圍外時，更新工具列 Y 軸位置
                if (!isTouchInsideToolbar(rawX, rawY)) {
                    moveToXAndY(rawX, rawY)
                }
            }
        }
    }

    /** 根據全域觸控座標移動工具列 Y 軸，並依 X 軸點擊位置自動靠左或靠右吸附 */
    private fun moveToXAndY(rawX: Float, rawY: Float) {
        val location = IntArray(2)
        getLocationOnScreen(location)
        // Y 軸對齊於「上一頁」與「下一頁」之間的分隔線 (120dp)
        val pointY = rawY - scale * 120f - location[1].toFloat()
        val pointX = rawX - location[0].toFloat()

        updateLayout(pointX, pointY, dragging = false)
    }

    /** 取得工具列當前寬度（若 Layout 尚未測量則提供備用估算值 80dp） */
    private fun getBarWidth(): Float {
        val width = mainLayout?.width ?: 0
        if (width > 0) return width.toFloat()
        val measuredWidth = mainLayout?.measuredWidth ?: 0
        if (measuredWidth > 0) return measuredWidth.toFloat()
        mainLayout?.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val measured = mainLayout?.measuredWidth ?: 0
        if (measured > 0) return measured.toFloat()
        return scale * 80f
    }

    /** 取得工具列當前高度（若 Layout 尚未測量則提供備用估算值 180dp） */
    private fun getBarHeight(): Float {
        val height = mainLayout?.height ?: 0
        if (height > 0) return height.toFloat()
        val measuredHeight = mainLayout?.measuredHeight ?: 0
        if (measuredHeight > 0) return measuredHeight.toFloat()
        mainLayout?.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val measured = mainLayout?.measuredHeight ?: 0
        if (measured > 0) return measured.toFloat()
        return scale * 180f
    }

    /** 取得頁面容器寬度 (使用 parent 或 rootView 的寬度，非 ToolBarFloating 本身) */
    private fun getContainerWidth(): Float {
        val parentW = (parent as? View)?.width?.toFloat() ?: 0f
        if (parentW > 0f) return parentW
        val rootW = rootView?.width?.toFloat() ?: 0f
        if (rootW > 0f) return rootW
        return context.resources.displayMetrics.widthPixels.toFloat()
    }

    /** 取得頁面容器高度 (使用 parent 或 rootView 的高度，非 ToolBarFloating 本身) */
    private fun getContainerHeight(): Float {
        val parentH = (parent as? View)?.height?.toFloat() ?: 0f
        if (parentH > 0f) return parentH
        val rootH = rootView?.height?.toFloat() ?: 0f
        if (rootH > 0f) return rootH
        return context.resources.displayMetrics.heightPixels.toFloat()
    }

    /** 取得系統底欄 (Gesture / Navigation Bar) 的 Safe Inset 高度 */
    private fun getBottomInset(): Float {
        var inset = 0f
        val root = rootView ?: return inset
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insets = root.rootWindowInsets?.getInsets(WindowInsets.Type.systemBars())
            if (insets != null) {
                inset = insets.bottom.toFloat()
            }
        } else {
            @Suppress("DEPRECATION")
            val windowInsets = root.rootWindowInsets
            if (windowInsets != null) {
                @Suppress("DEPRECATION")
                inset = windowInsets.systemWindowInsetBottom.toFloat()
            }
        }
        return inset
    }

    /**
     * 更新工具列 Layout 位置
     *
     * @param targetX 目標 X 座標
     * @param targetY 目標 Y 座標
     * @param dragging 是否為使用者正在直接拖曳中
     */
    private fun updateLayout(targetX: Float, targetY: Float, dragging: Boolean) {
        val barWidth = getBarWidth()
        val barHeight = getBarHeight()

        val containerWidth = getContainerWidth()
        val containerHeight = getContainerHeight()
        val bottomInset = getBottomInset()

        var finalX = targetX
        val rawTargetY = targetY

        val maxRightX = (containerWidth - barWidth).coerceAtLeast(0f)
        val usableHeight = (containerHeight - bottomInset).coerceAtLeast(barHeight)
        val maxBottomY = (usableHeight - barHeight).coerceAtLeast(0f)

        // X 軸位置計算：
        if (dragging) {
            // 拖曳中：即時跟隨手指，限制於螢幕可視寬度內
            finalX = finalX.coerceIn(0f, maxRightX)
        } else {
            // 靜止/吸附：判斷目標位置位於左半邊或右半邊，自動貼合左邊界或右邊界
            finalX = if (targetX <= containerWidth / 2f) {
                0f
            } else {
                maxRightX
            }
        }

        // Y 軸位置與底部防抖動 (Hysteresis) 處理：
        // 當頁面滑到最底部時，鎖定在 maxBottomY；避免在邊界滾動時工具列跳動
        val unlockThreshold = scale * 36f // 36dp 反向向上滑動解鎖閾值

        var finalY: Float
        if (isLockedAtBottom) {
            // 若已處於底部鎖定狀態，必須向上滑動超過 unlockThreshold 才會解鎖
            if (rawTargetY < maxBottomY - unlockThreshold) {
                isLockedAtBottom = false
                finalY = rawTargetY
            } else {
                finalY = maxBottomY
            }
        } else {
            // 未鎖定時：若目標位置達到或超過 maxBottomY，進入底部鎖定
            if (rawTargetY >= maxBottomY) {
                isLockedAtBottom = true
                finalY = maxBottomY
            } else {
                finalY = rawTargetY
            }
        }

        // 確保 Y 軸嚴格限制在 [0, maxBottomY] 之間，確保 100% 不會超出可視畫面
        finalY = finalY.coerceIn(0f, maxBottomY)

        val params = mainLayout?.layoutParams as? LayoutParams ?: LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = finalX.toInt()
        params.topMargin = finalY.toInt()
        mainLayout?.layoutParams = params

        // 儲存最新的懸浮位置紀錄
        setFloatingLocation(finalX, finalY)
    }

    /** 設定第一個按鈕 (Setting) 的點擊事件 */
    fun setOnClickListenerSetting(listener: OnClickListener?) {
        btnSetting?.setOnClickListener(listener)
    }

    /** 設定第一個按鈕 (Setting) 的顯示文字 */
    fun setTextSetting(text: String?) {
        btnSetting?.text = text
    }

    /** 設定第二個按鈕 (btn1) 的點擊事件 */
    fun setOnClickListener1(listener: OnClickListener?) {
        btn1?.setOnClickListener(listener)
    }

    /** 設定第二個按鈕 (btn1) 的長按事件 */
    fun setOnLongClickListener1(listener: OnLongClickListener?) {
        btn1?.setOnLongClickListener(listener)
    }

    /** 設定第二個按鈕 (btn1) 的顯示文字 */
    fun setText1(text: String?) {
        btn1?.text = text
    }

    /** 設定第三個按鈕 (btn2) 的點擊事件 */
    fun setOnClickListener2(listener: OnClickListener?) {
        btn2?.setOnClickListener(listener)
    }

    /** 設定第三個按鈕 (btn2) 的長按事件 */
    fun setOnLongClickListener2(listener: OnLongClickListener?) {
        btn2?.setOnLongClickListener(listener)
    }

    /** 設定第三個按鈕 (btn2) 的顯示文字 */
    fun setText2(text: String?) {
        btn2?.text = text
    }

    /** 設定控制元件顯示狀態 */
    override fun setVisibility(visibility: Int) {
        mainLayout?.visibility = visibility
        if (visibility == View.VISIBLE) {
            activeInstance = this
        }
    }

    /** 當裝置旋轉或版面配置改變時，重置工具列至畫面右側預設位置 */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
        updateLayout(screenWidth, screenHeight / 2f, false)
    }

    /** 重新讀取並套用最新的閒置隱藏時間與不透明度設定 */
    fun updateSettings() {
        activeInstance = this
        idleTime = if (toolbarIdle <= 0f) 1.0f else toolbarIdle
        alphaPercentage = toolbarAlpha / 100f
        if (TempSettings.isFloatingInvisible) {
            mainLayout?.alpha = alphaPercentage
        }
        startInvisible()
    }

    companion object {
        /** 當前活躍的 ToolBarFloating 實例 */
        var activeInstance: ToolBarFloating? = null
            private set

        /** 附加全域 Window Callback 以監聽頁面手勢 */
        fun attachGlobalWindowCallback(activity: Activity) {
            val window = activity.window ?: return
            val currentCallback = window.callback ?: return
            if (currentCallback is GlobalWindowCallback) return

            window.callback = GlobalWindowCallback(currentCallback)
        }
    }

    /** 全域 Window 觸控監聽 Proxy */
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

    /** 閒置漸變透明 Coroutine 工作 */
    val startInvisible: ASCoroutine? = object : ASCoroutine() {
        override suspend fun run() {
            mainLayout?.alpha = alphaPercentage
        }
    }

    /** 開始閒置隱藏倒數 */
    private fun startInvisible() {
        startInvisible?.cancel()
        val delayMillis = (idleTime * 1000f).toLong().coerceAtLeast(1000L)
        startInvisible?.postDelayed(delayMillis)
        TempSettings.isFloatingInvisible = true
    }

    /** 取消閒置隱藏並恢復完全不透明 */
    private fun cancelInvisible() {
        startInvisible?.cancel()
        mainLayout?.alpha = 1f
        TempSettings.isFloatingInvisible = false
    }
}