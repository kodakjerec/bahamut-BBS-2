package com.kota.Bahamut.pages.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import com.kota.Bahamut.R
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.floatingLocation
import com.kota.Bahamut.service.UserSettings.Companion.setFloatingLocation
import com.kota.Bahamut.service.UserSettings.Companion.toolbarAlpha
import com.kota.Bahamut.service.UserSettings.Companion.toolbarIdle
import com.kota.asFramework.thread.ASCoroutine

class ToolBarFloating(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var mainLayout: LinearLayout? = null
    private var btnSetting: Button? = null
    private var btn1: Button? = null
    private var btn2: Button? = null
    private var scale = 0f // 畫面精度

    private var idleTime = 0f // 閒置多久
    private var alphaPercentage = 0f // 閒置不透明度

    private var originalWindowCallback: Window.Callback? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context?) {
        idleTime = toolbarIdle
        alphaPercentage = toolbarAlpha / 100
        inflate(context, R.layout.toolbar_floating, this)
        scale = getContext().resources.displayMetrics.density

        mainLayout = findViewById(R.id.ToolbarFloating)
        // 取得上次紀錄
        val list = floatingLocation
        if (list.isNotEmpty() && list[0]!! >= 0.0f) {
            val pointX: Float = list[0]!!
            val pointY: Float = list[1]!!
            updateLayout(pointX, pointY, false)
        } else {
            // 畫面預設值：靠右吸附、高度居中
            val screenWidth = getContext().resources.displayMetrics.widthPixels.toFloat()
            val screenHeight = getContext().resources.displayMetrics.heightPixels.toFloat()
            updateLayout(screenWidth, screenHeight / 2f, false)
        }

        btnSetting = mainLayout?.findViewById(R.id.ToolbarFloating_setting)
        btn1 = mainLayout?.findViewById(R.id.ToolbarFloating_1)
        btn2 = mainLayout?.findViewById(R.id.ToolbarFloating_2)
        btnSetting?.setOnTouchListener(onTouchListener)

        // 啟用定時隱藏
        if (TempSettings.isFloatingInvisible) mainLayout?.alpha = alphaPercentage
        else startInvisible()
    }

    // 移動 toolbar 手勢監聽
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { view: View?, event: MotionEvent? ->
        if (event == null) return@OnTouchListener false

        val duration = event.eventTime - event.downTime
        var pointX = event.rawX
        var pointY = event.rawY

        // 微調手指中心點
        pointX -= scale * 30
        pointY -= scale * 60

        // 彈出視窗與父容器位置扣除
        val location = IntArray(2)
        rootView?.getLocationOnScreen(location)
        pointX -= location[0].toFloat()
        pointY -= location[1].toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> // 手指按下
                cancelInvisible()

            MotionEvent.ACTION_UP -> {
                if (duration < 200) { // 點擊
                    if (view is Button) {
                        view.performClick()
                    }
                } else { // 手指放開，將位置靠左或靠右吸附
                    updateLayout(pointX, pointY, false)
                }
                startInvisible()
            }

            MotionEvent.ACTION_MOVE -> // 拖曳中即時更新位置
                updateLayout(pointX, pointY, true)
        }
        true
    }

    init {
        init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachWindowTouchListener()
    }

    override fun onDetachedFromWindow() {
        detachWindowTouchListener()
        super.onDetachedFromWindow()
    }

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

    private fun attachWindowTouchListener() {
        val activity = findActivity() ?: return
        val window = activity.window ?: return
        if (originalWindowCallback != null) return

        val currentCallback = window.callback ?: return
        originalWindowCallback = currentCallback

        window.callback = object : Window.Callback by currentCallback {
            override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
                if (event != null) {
                    handleOutsideTouch(event)
                }
                return currentCallback.dispatchTouchEvent(event)
            }
        }
    }

    private fun detachWindowTouchListener() {
        val activity = findActivity() ?: return
        val window = activity.window ?: return
        if (originalWindowCallback != null) {
            window.callback = originalWindowCallback
            originalWindowCallback = null
        }
    }

    private fun isTouchInsideToolbar(rawX: Float, rawY: Float): Boolean {
        val targetView = mainLayout ?: this
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        val left = location[0].toFloat()
        val top = location[1].toFloat()
        val right = left + getBarWidth()
        val bottom = top + getBarHeight()
        return rawX >= left && rawX <= right && rawY >= top && rawY <= bottom
    }

    private fun handleOutsideTouch(event: MotionEvent) {
        if (visibility != View.VISIBLE || mainLayout?.visibility != View.VISIBLE) return

        if (event.action == MotionEvent.ACTION_DOWN) {
            val rawX = event.rawX
            val rawY = event.rawY

            // 當觸控點在 ToolBarFloating 範圍以外時，自動將工具列移動到對應的 Y 軸，X 軸保持不變
            if (!isTouchInsideToolbar(rawX, rawY)) {
                moveToY(rawY)
            }
        }
    }

    private fun moveToY(rawY: Float) {
        val location = IntArray(2)
        rootView?.getLocationOnScreen(location)
        val pointY = rawY - scale * 60 - location[1].toFloat()

        // 取得當前 X 軸左邊距 leftMargin
        val params = mainLayout?.layoutParams as? LayoutParams
        val currentLeftMargin = params?.leftMargin?.toFloat() ?: 0f

        // 更新 Layout，帶入當前的 X 軸位置保持靠左或靠右不變
        updateLayout(currentLeftMargin, pointY, dragging = false)
    }

    private fun getBarWidth(): Float {
        val width = mainLayout?.width ?: 0
        if (width > 0) return width.toFloat()
        val measuredWidth = mainLayout?.measuredWidth ?: 0
        if (measuredWidth > 0) return measuredWidth.toFloat()
        mainLayout?.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return (mainLayout?.measuredWidth ?: 0).toFloat()
    }

    private fun getBarHeight(): Float {
        val height = mainLayout?.height ?: 0
        if (height > 0) return height.toFloat()
        val measuredHeight = mainLayout?.measuredHeight ?: 0
        if (measuredHeight > 0) return measuredHeight.toFloat()
        mainLayout?.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return (mainLayout?.measuredHeight ?: 0).toFloat()
    }

    // 更新 toolbar 位置
    private fun updateLayout(targetX: Float, targetY: Float, dragging: Boolean) {
        val barWidth = getBarWidth()
        val barHeight = getBarHeight()

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        var finalX = targetX
        var finalY = targetY

        val maxRightX = (screenWidth - barWidth).coerceAtLeast(0f)
        val maxBottomY = (screenHeight - barHeight).coerceAtLeast(0f)

        if (dragging) {
            // 拖曳中：即時跟隨手指，並限制在螢幕可視寬度內
            finalX = finalX.coerceIn(0f, maxRightX)
        } else {
            // 拖曳結束放開或初始化：判斷使用者觸控/中心點位置，遵循靠左 (X=0) 或靠右 (X=maxRightX) 原則
            val touchCenterX = targetX + barWidth / 2f
            finalX = if (touchCenterX <= screenWidth / 2f) {
                0f
            } else {
                maxRightX
            }
        }

        // Y 軸維持使用者的 Touch Y，並限制在螢幕可視高度內
        finalY = finalY.coerceIn(0f, maxBottomY)

        val params = mainLayout?.layoutParams as? LayoutParams ?: LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = finalX.toInt()
        params.topMargin = finalY.toInt()
        mainLayout?.layoutParams = params

        // 儲存最新浮動位置
        setFloatingLocation(finalX, finalY)
    }

    // 指定按鈕動作和文字 btnSetting
    fun setOnClickListenerSetting(listener: OnClickListener?) {
        btnSetting?.setOnClickListener(listener)
    }

    fun setTextSetting(text: String?) {
        btnSetting?.text = text
    }

    // 指定按鈕動作和文字 btn1
    fun setOnClickListener1(listener: OnClickListener?) {
        btn1?.setOnClickListener(listener)
    }

    fun setOnLongClickListener1(listener: OnLongClickListener?) {
        btn1?.setOnLongClickListener(listener)
    }

    fun setText1(text: String?) {
        btn1?.text = text
    }

    // 指定按鈕動作和文字 btn2
    fun setOnClickListener2(listener: OnClickListener?) {
        btn2?.setOnClickListener(listener)
    }

    fun setOnLongClickListener2(listener: OnLongClickListener?) {
        btn2?.setOnLongClickListener(listener)
    }

    fun setText2(text: String?) {
        btn2?.text = text
    }

    // 指定 layout 顯示
    override fun setVisibility(visibility: Int) {
        mainLayout?.visibility = visibility
    }

    // 旋轉或變彈出視窗時, 將工具列回到右方預設位置
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
        updateLayout(screenWidth, screenHeight / 2f, false)
    }

    val startInvisible: ASCoroutine? = object : ASCoroutine() {
        override suspend fun run() {
            mainLayout?.alpha = alphaPercentage
        }
    }

    private fun startInvisible() {
        startInvisible?.cancel()
        startInvisible?.postDelayed(idleTime.toLong() * 1000L)
        TempSettings.isFloatingInvisible = true
    }

    private fun cancelInvisible() {
        startInvisible?.cancel()
        mainLayout?.alpha = 1f
        TempSettings.isFloatingInvisible = false
    }
}
