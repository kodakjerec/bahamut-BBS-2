package com.kota.asFramework.pageController

import android.R.id
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("StaticFieldLeak")
object ASWindowStateHandler {
    var activity: Activity?

    var contentViewHeight: Int

    var contentViewWidth: Int

    var screenHeightInch: Double

    var screenHeightPx: Int

    var screenInch: Double

    var screenWidthInch: Double

    var screenWidthPx: Int

    var statusBarHeight: Int = 0

    var titleBarHeight: Int = 0

    init {
        contentViewWidth = -1
        contentViewHeight = -1
        screenWidthPx = 0
        screenHeightPx = 0
        screenWidthInch = 0.0
        screenHeightInch = 0.0
        screenInch = 0.0
        activity = null
    }

    fun construct(paramActivity: Activity?) {
        activity = paramActivity

        val displayMetrics = DisplayMetrics()
        
        // 使用適合的 API 獲取顯示器指標
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ 使用 WindowMetrics
            val windowMetrics = activity?.windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            screenWidthPx = bounds.width()
            screenHeightPx = bounds.height()
            // 從 resources 獲取 density 相關資訊
            displayMetrics.density = activity?.resources.displayMetrics.density
            displayMetrics.xdpi = activity?.resources.displayMetrics.xdpi
            displayMetrics.ydpi = activity?.resources.displayMetrics.ydpi
        } else {
            // API 29 及以下使用傳統方法
            @Suppress("DEPRECATION")
            activity?.windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidthPx = displayMetrics.widthPixels
            screenHeightPx = displayMetrics.heightPixels
        }
        
        screenWidthInch = (screenWidthPx / displayMetrics.xdpi).toDouble()
        screenHeightInch = (screenHeightPx / displayMetrics.ydpi).toDouble()
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
            val window = activity?.window
            window.decorView.getWindowVisibleDisplayFrame(rect)
            statusBarHeight = rect.top
            val contentView = window.findViewById<View>(id.content)
            val i = contentView?.top ?: 0
            if (i > 0) titleBarHeight = i - statusBarHeight
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


