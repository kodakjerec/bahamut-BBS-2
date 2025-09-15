package com.kota.ASFramework.PageController

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import kotlin.math.pow
import kotlin.math.sqrt

object ASWindowStateHandler {
    
    var activity: Activity? = null
    var contentViewHeight = -1
    var contentViewWidth = -1
    var screenHeightInch = 0.0
    var screenHeightPx = 0
    var screenInch = 0.0
    var screenWidthInch = 0.0
    var screenWidthPx = 0
    var statusBarHeight = 0
    var titleBarHeight = 0
    
    fun construct(activity: Activity) {
        this.activity = activity
        
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        screenWidthPx = displayMetrics.widthPixels
        screenHeightPx = displayMetrics.heightPixels
        screenWidthInch = displayMetrics.widthPixels / displayMetrics.xdpi
        screenHeightInch = displayMetrics.heightPixels / displayMetrics.ydpi
        screenInch = sqrt(screenWidthInch.pow(2.0) + screenHeightInch.pow(2.0))
        contentViewWidth = screenWidthPx
        
        // 適應 edge-to-edge 的處理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 在 edge-to-edge 模式下，初始設定為全螢幕
            // 實際的 insets 會由 WindowInsets 處理
            statusBarHeight = 0
            titleBarHeight = 0
            contentViewHeight = screenHeightPx
        } else {
            // 舊版本的處理方式
            val rect = Rect()
            val window = activity.window
            window.decorView.getWindowVisibleDisplayFrame(rect)
            statusBarHeight = rect.top
            
            val contentTop = window.findViewById<android.view.View>(android.R.id.content).top
            if (contentTop > 0) {
                titleBarHeight = contentTop - statusBarHeight
            }
            contentViewHeight = screenHeightPx - titleBarHeight - statusBarHeight
        }
    }
    
    /**
     * 更新 WindowInsets 資訊（用於 edge-to-edge 模式）
     */
    fun updateWindowInsets(top: Int, bottom: Int, left: Int, right: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            statusBarHeight = top
            // 更新內容視圖高度，扣除上下的系統欄
            contentViewHeight = screenHeightPx - top - bottom
        }
    }
}
