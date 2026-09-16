package com.kota.Bahamut.pages.messages

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.NotificationSettings
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine

class MessageSmall(context: Context): LinearLayout(context) {
    private var mainLayout: RelativeLayout
    private var badgeView: TextView
    private var iconView: Button
    private var scale = 0f // 畫面精度

    init {
        scale = getContext().resources.displayMetrics.density

        mainLayout = RelativeLayout(context).apply {
            id = R.id.Message_Small_Layout
            layoutParams = LayoutParams((48 * scale).toInt(), (48 * scale).toInt())
        }

        iconView = Button(context).apply {
            id = R.id.Message_Small_Icon
            val iconParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins((3 * scale).toInt(), (3 * scale).toInt(), (3 * scale).toInt(), (3 * scale).toInt())
            }
            layoutParams = iconParams
            setPadding((3 * scale).toInt(), 0, 0, 0)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_comment, 0, 0, 0)
            setBackgroundColor(Color.TRANSPARENT)
        }

        badgeView = TextView(context).apply {
            id = R.id.Message_Small_Badge
            val badgeParams = RelativeLayout.LayoutParams(
                (16 * scale).toInt(),
                (16 * scale).toInt()
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }
            layoutParams = badgeParams
            elevation = 10 * scale
            setBackgroundResource(R.drawable.shape_circle_red)
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            text = "1"
            visibility = GONE
        }

        mainLayout.addView(iconView)
        mainLayout.addView(badgeView)
        addView(mainLayout)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun afterInit() {
        val myLayout: LayoutParams = mainLayout.layoutParams as LayoutParams
        myLayout.leftMargin = context.resources.displayMetrics.widthPixels / 2
        iconView.setOnTouchListener(onTouchListener)
    }

    /** 更新Badge */
    fun updateBadge(aNumber: String) {
        ASCoroutine.ensureMainThread {
            if (aNumber != "0") {
                badgeView.text = aNumber
                badgeView.visibility = VISIBLE
            } else {
                badgeView.text = ""
                badgeView.visibility = GONE
            }
        }
    }

    fun show() {
        if (NotificationSettings.getShowMessageFloating())
            this.visibility = VISIBLE
    }

    fun hide() {
        this.visibility = GONE
    }

    // 移動toolbar
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _: View?, event: MotionEvent ->
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
                    val aPage = PageContainer.instance!!.getMessageMain()
                    ASNavigationController.currentController?.pushViewController(aPage)
                    BahamutStateHandler.bahamutStateHandler?.currentPage =
                        BahamutPage.BAHAMUT_MESSAGE_MAIN_PAGE
                } else {
                    updateLayout(pointX, pointY, false)
                }
            }

            MotionEvent.ACTION_MOVE ->
                updateLayout(pointX, pointY, true)
        }
        true
    }

    // 更新toolbar位置
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun updateLayout(deltaX: Float, deltaY: Float, dragging: Boolean) {
        var dx = deltaX
        var dy = deltaY
        val barWidth: Int = mainLayout.layoutParams.width
        val barHeight: Int = mainLayout.layoutParams.height
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        var screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
        val resourceId: Int = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navigationBarHeight = context.resources.getDimensionPixelSize(resourceId)
        screenHeight -= navigationBarHeight

        // X軸錯誤處理
        if (dx < 0) {
            dx = 0f
        } else if (dx + barWidth > screenWidth) {
            dx = screenWidth - barWidth
        }

        // Y軸錯誤處理
        if (dy < 0) {
            dy = 0f
        } else if (dy + barHeight > screenHeight) {
            dy = screenHeight - barHeight
        }

        val params = mainLayout.layoutParams as LayoutParams
        params.leftMargin = dx.toInt()
        params.topMargin = dy.toInt()
        mainLayout.layoutParams = params
    }
}