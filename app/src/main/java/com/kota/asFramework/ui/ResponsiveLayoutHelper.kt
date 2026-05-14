package com.kota.asFramework.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.TypedValue

object ResponsiveLayoutHelper {

    private const val WINDOW_WIDTH_THRESHOLD_DP = 300

    /**
     * 判斷當前應用程式視窗是否為小視窗模式。
     *
     * @param activity 宿主 Activity。
     * @return true 如果是小視窗模式，false 否則。
     */
    fun isSmallWindow(activity: Activity): Boolean {
        val displayMetrics = activity.resources.displayMetrics
        val dpToPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            WINDOW_WIDTH_THRESHOLD_DP.toFloat(),
            displayMetrics
        ).toInt()

        // 獲取當前應用程式視窗的實際寬度
        val currentWindowWidthPx = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            activity.windowManager.currentWindowMetrics.bounds.width()
        } else {
            displayMetrics.widthPixels
        }

        val isSmall = currentWindowWidthPx < dpToPx

        Log.d("ResponsiveLayoutHelper", "當前視窗寬度: $currentWindowWidthPx px ($WINDOW_WIDTH_THRESHOLD_DP dp 等效: $dpToPx px)")
        Log.d("ResponsiveLayoutHelper", "是否為小視窗: $isSmall")

        return isSmall
    }

    // 提供給 BoardHeaderView 使用，用於獲取當前視窗寬度
    fun getCurrentWindowWidthPx(activity: Activity): Int {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            activity.windowManager.currentWindowMetrics.bounds.width()
        } else {
            activity.resources.displayMetrics.widthPixels
        }
    }

    // 提供給 BoardHeaderView 使用，用於獲取閾值像素
    fun getWindowWidthThresholdPx(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            WINDOW_WIDTH_THRESHOLD_DP.toFloat(),
            displayMetrics
        ).toInt()
    }
}