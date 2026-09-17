package com.kota.Bahamut.pages.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions
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

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context?) {
        idleTime = toolbarIdle
        alphaPercentage = toolbarAlpha / 100
        scale = getContext().resources.displayMetrics.density

        mainLayout = LinearLayout(context).apply {
            id = R.id.ToolbarFloating
            layoutParams = LayoutParams((80 * scale).toInt(), (180 * scale).toInt())
            orientation = VERTICAL
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
            setPadding(1, 1, 1, 1)
        }

        val inner = LinearLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            orientation = VERTICAL
        }

        btnSetting = Button(context).apply {
            id = R.id.ToolbarFloating_setting
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            text = CommonFunctions.getContextString(R.string.post)
            tag = "ToolbarItem"
        }

        btn1 = Button(context).apply {
            id = R.id.ToolbarFloating_1
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            text = CommonFunctions.getContextString(R.string.prev_page)
            tag = "ToolbarItem"
        }

        btn2 = Button(context).apply {
            id = R.id.ToolbarFloating_2
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            text = CommonFunctions.getContextString(R.string.last_page)
            tag = "ToolbarItem"
        }

        fun createDivider() = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
        }

        inner.addView(btnSetting)
        inner.addView(createDivider())
        inner.addView(btn1)
        inner.addView(createDivider())
        inner.addView(btn2)
        mainLayout?.addView(inner)
        addView(mainLayout)

        // 取得上次紀錄
        val list = floatingLocation
        if (list.isNotEmpty() && list[0] != null && list[0]!! >= 0.0f) {
            val pointX: Float = list[0]!!
            val pointY: Float = list[1]!!
            updateLayout(pointX, pointY, false)
        } else {
            val screenWidth = getContext().resources.displayMetrics.widthPixels.toFloat()
            val screenHeight = getContext().resources.displayMetrics.heightPixels.toFloat()
            updateLayout(screenWidth, screenHeight / 2, false)
        }

        btnSetting?.setOnTouchListener(onTouchListener)

        // 啟用定時隱藏
        if (TempSettings.isFloatingInvisible) mainLayout?.alpha = alphaPercentage
        else startInvisible()
    }

    // 移動toolbar
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { view: View?, event: MotionEvent? ->
        if (event == null) return@OnTouchListener false
        try {
            val duration = event.eventTime - event.downTime
            var pointX = event.rawX
            var pointY = event.rawY
            // 微調手指中心點
            pointX -= scale * 30
            pointY -= scale * 60

            // 彈出視窗位置
            val location = IntArray(2)
            rootView.getLocationOnScreen(location)
            pointX -= location[0].toFloat()
            pointY -= location[1].toFloat()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> cancelInvisible()
                MotionEvent.ACTION_UP -> {
                    if (duration < 200) {
                        view?.performClick()
                    } else {
                        updateLayout(pointX, pointY, false)
                    }
                    startInvisible()
                }
                MotionEvent.ACTION_MOVE -> updateLayout(pointX, pointY, true)
            }
        } catch (_: Exception) {
        }
        true
    }

    init {
        init(context)
    }

    // 更新toolbar位置
    private fun updateLayout(deltaX: Float, deltaY: Float, dragging: Boolean) {
        var dx = deltaX
        var dy = deltaY
        val barWidth = mainLayout?.layoutParams?.width ?: (80 * scale).toInt()
        val barHeight = mainLayout?.layoutParams?.height ?: (180 * scale).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()

        // X軸錯誤處理
        if (dx < 0) {
            dx = 0f
        } else if ((dx + barWidth) > screenWidth) {
            dx = screenWidth - barWidth
        } else {
            if (!dragging) {
                // 吸附X軸
                dx = if (dx > screenWidth / 2) {
                    screenWidth - barWidth
                } else {
                    0f
                }
            }
        }

        // Y軸錯誤處理
        if ((dy + barHeight) > screenHeight) {
            dy = screenHeight - barHeight
        } else if (dy < 0) {
            dy = 0f
        }

        val params = mainLayout?.layoutParams as? LayoutParams ?: LayoutParams(barWidth, barHeight)
        params.leftMargin = dx.toInt()
        params.topMargin = dy.toInt()
        mainLayout?.layoutParams = params
        setFloatingLocation(dx, dy)
    }

    fun setOnClickListenerSetting(listener: OnClickListener?) {
        btnSetting?.setOnClickListener(listener)
    }

    fun setTextSetting(text: String?) {
        btnSetting?.text = text
    }

    fun setOnClickListener1(listener: OnClickListener?) {
        btn1?.setOnClickListener(listener)
    }

    fun setOnLongClickListener1(listener: OnLongClickListener?) {
        btn1?.setOnLongClickListener(listener)
    }

    fun setText1(text: String?) {
        btn1?.text = text
    }

    fun setOnClickListener2(listener: OnClickListener?) {
        btn2?.setOnClickListener(listener)
    }

    fun setOnLongClickListener2(listener: OnLongClickListener?) {
        btn2?.setOnLongClickListener(listener)
    }

    fun setText2(text: String?) {
        btn2?.text = text
    }

    override fun setVisibility(visibility: Int) {
        mainLayout?.visibility = visibility
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
        updateLayout(screenWidth, screenHeight / 2, false)
        super.onConfigurationChanged(newConfig)
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
