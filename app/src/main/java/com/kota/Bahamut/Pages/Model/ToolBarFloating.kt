package com.kota.Bahamut.Pages.Model

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings.Companion.floatingLocation
import com.kota.Bahamut.Service.UserSettings.Companion.setFloatingLocation
import com.kota.Bahamut.Service.UserSettings.Companion.toolbarAlpha
import com.kota.Bahamut.Service.UserSettings.Companion.toolbarIdle
import java.util.Timer
import java.util.TimerTask

class ToolBarFloating(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var mainLayout: LinearLayout? = null
    private var btnSetting: Button? = null
    private var btn1: Button? = null
    private var btn2: Button? = null
    private var scale = 0f // 畫面精度
    private var timer: Timer? = null

    private var idleTime = 0f // 閒置多久
    private var alphaPercentage = 0f // 閒置不透明度

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context?) {
        idleTime = toolbarIdle
        alphaPercentage = toolbarAlpha / 100
        inflate(context, R.layout.toolbar_floating, this)
        scale = getContext().getResources().getDisplayMetrics().density

        mainLayout = findViewById<LinearLayout>(R.id.ToolbarFloating)
        // 取得上次紀錄
        val list = floatingLocation
        if (list.size > 0 && list.get(0)!! >= 0.0f) {
            val pointX: Float = list.get(0)!!
            val pointY: Float = list.get(1)!!
            updateLayout(pointX, pointY, false)
        } else {
            // 畫面預設值
            val screenWidth = getContext().getResources().getDisplayMetrics().widthPixels.toFloat()
            val screenHeight =
                getContext().getResources().getDisplayMetrics().heightPixels.toFloat()
            updateLayout(screenWidth, screenHeight / 2, false)
        }

        btnSetting = mainLayout!!.findViewById<Button>(R.id.ToolbarFloating_setting)
        btn1 = mainLayout!!.findViewById<Button>(R.id.ToolbarFloating_1)
        btn2 = mainLayout!!.findViewById<Button>(R.id.ToolbarFloating_2)
        btnSetting!!.setOnTouchListener(onTouchListener)

        // 啟用定時隱藏
        // 如果之前已經隱藏就不要再讓他顯現出來
        if (TempSettings.isFloatingInvisible) mainLayout!!.setAlpha(alphaPercentage)
        else startInvisible()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById<ViewGroup?>(R.id.ToolbarFloating))
    }

    // 移動toolbar
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { view: View?, event: MotionEvent? ->
        val duration = event!!.getEventTime() - event.getDownTime()
        var pointX = event.getRawX()
        var pointY = event.getRawY()
        // 微調手指中心點
        pointX -= scale * 30
        pointY -= scale * 60

        // 彈出視窗位置
        val location = IntArray(2)
        getRootView().getLocationOnScreen(location)
        pointX -= location[0].toFloat()
        pointY -= location[1].toFloat()

        when (event.getAction()) {
            MotionEvent.ACTION_DOWN ->                 // 手指按下
                cancelInvisible()

            MotionEvent.ACTION_UP -> {
                if (duration < 200) { // click
                    if (view is Button) {
                        view.performClick()
                    }
                } else { // 将LinearLayout的位置更新到最终的位置
                    updateLayout(pointX, pointY, false)
                }
                startInvisible()
            }

            MotionEvent.ACTION_MOVE ->                 // 更新LinearLayout的位置
                updateLayout(pointX, pointY, true)
        }
        true
    }

    init {
        init(context)
    }

    // 更新toolbar位置
    private fun updateLayout(deltaX: Float, deltaY: Float, dragging: Boolean) {
        // 获取LinearLayout的LayoutParams
        var deltaX = deltaX
        var deltaY = deltaY
        val barWidth = mainLayout!!.getLayoutParams().width
        val barHeight = mainLayout!!.getLayoutParams().height
        val screenWidth = getContext().getResources().getDisplayMetrics().widthPixels.toFloat()
        val screenHeight = getContext().getResources().getDisplayMetrics().heightPixels.toFloat()

        // X軸錯誤處理
        if (deltaX < 0) {
            deltaX = 0f
        } else if ((deltaX + barWidth) > screenWidth) {
            deltaX = screenWidth - barWidth
        } else {
            if (!dragging) {
                // 吸附X軸
                if (deltaX > screenWidth / 2) {
                    deltaX = screenWidth - barWidth
                } else {
                    deltaX = 0f
                }
            }
        }

        // Y軸錯誤處理
        if ((deltaY + barHeight) > screenHeight) {
            deltaY = screenHeight - barHeight
        } else if (deltaY < 0) {
            deltaY = 0f
        }

        val params = mainLayout!!.getLayoutParams() as LayoutParams
        // 更新LayoutParams中的leftMargin和topMargin
        params.leftMargin = deltaX.toInt()
        params.topMargin = deltaY.toInt()
        // 应用新的LayoutParams
        mainLayout!!.setLayoutParams(params)
        // 儲存位置
        setFloatingLocation(deltaX, deltaY)
    }

    // 指定按鈕動作和文字 btnSetting
    fun setOnClickListenerSetting(listener: OnClickListener?) {
        btnSetting!!.setOnClickListener(listener)
    }

    fun setTextSetting(text: String?) {
        btnSetting!!.setText(text)
    }

    // 指定按鈕動作和文字 btn1
    fun setOnClickListener1(listener: OnClickListener?) {
        btn1!!.setOnClickListener(listener)
    }

    fun setOnLongClickListener1(listener: OnLongClickListener?) {
        btn1!!.setOnLongClickListener(listener)
    }

    fun setText1(text: String?) {
        btn1!!.setText(text)
    }

    // 指定按鈕動作和文字 btn2
    fun setOnClickListener2(listener: OnClickListener?) {
        btn2!!.setOnClickListener(listener)
    }

    fun setOnLongClickListener2(listener: OnLongClickListener?) {
        btn2!!.setOnLongClickListener(listener)
    }

    fun setText2(text: String?) {
        btn2!!.setText(text)
    }

    // 指定layout顯示
    override fun setVisibility(visibility: Int) {
        mainLayout!!.setVisibility(visibility)
    }

    // 旋轉或變彈出視窗時, 將工具列回到右方預設位置
    override fun onConfigurationChanged(newConfig: Configuration?) {
        val screenWidth = getContext().getResources().getDisplayMetrics().widthPixels.toFloat()
        val screenHeight = getContext().getResources().getDisplayMetrics().heightPixels.toFloat()
        updateLayout(screenWidth, screenHeight / 2, false)

        super.onConfigurationChanged(newConfig)
    }

    private fun startInvisible() {
        if (timer != null) timer!!.cancel()

        timer = Timer()
        val task1: TimerTask = object : TimerTask() {
            override fun run() {
                mainLayout!!.setAlpha(alphaPercentage)
            }
        }
        timer!!.schedule(task1, idleTime.toInt() * 1000L)
        TempSettings.isFloatingInvisible = true
    }

    private fun cancelInvisible() {
        if (timer != null) timer!!.cancel()
        mainLayout!!.setAlpha(1f)
        TempSettings.isFloatingInvisible = false
    }
}
