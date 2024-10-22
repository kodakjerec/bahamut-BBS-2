package com.kota.Bahamut.Pages.Messages

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.NotificationSettings

class MessageSmall(context: Context): LinearLayout(context) {
    private var mainLayout: RelativeLayout
    private var badgeView: TextView
    private var iconView: TextView
    private var scale = 0f // 畫面精度

    init {
        inflate(context, R.layout.message_small, this)
        scale = getContext().resources.displayMetrics.density

        mainLayout = findViewById(R.id.Message_Small_Layout)
        badgeView = mainLayout.findViewById(R.id.Message_Small_Badge)
        iconView = mainLayout.findViewById(R.id.Message_Small_Icon)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun afterInit() {
        val myLayout:LayoutParams = mainLayout.layoutParams as LayoutParams
        myLayout.leftMargin = context.resources.displayMetrics.widthPixels/2
        iconView.setOnTouchListener(onTouchListener)
    }

    /** 更新Badge */
    fun updateBadge(aNumber: String) {
        object : ASRunner() {
            override fun run() {
                if (aNumber != "0") {
                    badgeView.text = aNumber
                    badgeView.visibility = VISIBLE
                } else {
                    badgeView.text = ""
                    badgeView.visibility = GONE
                }
            }
        }.runInMainThread()
    }

    fun show() {
        // 設定內有允許顯示, 才會顯示
        if (NotificationSettings.getShowMessageFloating())
            this.visibility = VISIBLE
    }
    fun hide() {
        this.visibility = GONE
    }

    // 移動toolbar
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { view: View?, event: MotionEvent ->
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
            MotionEvent.ACTION_UP -> {
                if (duration < 200) { // click
                    val aPage = PageContainer.getInstance().messageMain
                    ASNavigationController.getCurrentController().pushViewController(aPage)
                    BahamutStateHandler.getInstance().currentPage =
                        BahamutPage.BAHAMUT_MESSAGE_MAIN_PAGE
                } else { // 将LinearLayout的位置更新到最终的位置
                    updateLayout(pointX, pointY, false)
                }
            }

            MotionEvent.ACTION_MOVE ->                 // 更新LinearLayout的位置
                updateLayout(pointX, pointY, true)
        }
        true
    }

    // 更新toolbar位置
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun updateLayout(deltaX: Float, deltaY: Float, dragging: Boolean) {
        // 获取 Layout 的LayoutParams
        var deltaX = deltaX
        var deltaY = deltaY
        val barWidth: Int = mainLayout.layoutParams.width
        val barHeight: Int = mainLayout.layoutParams.height
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        var screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
        val resourceId: Int = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navigationBarHeight = context.resources.getDimensionPixelSize(resourceId)
        screenHeight -= navigationBarHeight
        
        // X軸錯誤處理
        if (deltaX < 0) {
            deltaX = 0f
        } else if (deltaX + barWidth > screenWidth) {
            deltaX = screenWidth - barWidth
        }

        // Y軸錯誤處理
        if (deltaY < 0) {
            deltaY = 0f
        } else if (deltaY + barHeight > screenHeight) {
            deltaY = screenHeight - barHeight
        }

        val params = mainLayout.layoutParams as LayoutParams
        // 更新LayoutParams中的leftMargin和topMargin
        params.leftMargin = deltaX.toInt()
        params.topMargin = deltaY.toInt()
        // 应用新的LayoutParams
        mainLayout.layoutParams = params
    }
}