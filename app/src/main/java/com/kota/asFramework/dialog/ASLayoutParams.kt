package com.kota.asFramework.dialog

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.core.graphics.drawable.toDrawable

/**
 * [ASLayoutParams] - 對話框與列表介面元件之版面佈局、尺寸與顏色常數管理類別。
 *
 * 職責：
 * 1. 提供對話框 (AlertDialog, ListDialog) 的按鈕背景 (Drawable) 與文字顏色 (ColorStateList)。
 * 2. 統一管理觸控區塊 (TouchBlock) 寬高、對話框寬度、邊距 (Padding) 與字型大小。
 */
class ASLayoutParams private constructor() {

    // --- Android 系統 State 屬性 ---
    private val STATE_PRESSED_ENABLED = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
    private val STATE_ENABLED_FOCUSED = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused)
    private val STATE_ENABLED = intArrayOf(android.R.attr.state_enabled)
    private val STATE_EMPTY = IntArray(0)

    // --- 顏色常數 (清晰取代舊有硬編碼負整數) ---
    private val COLOR_PRESSED_BG = 0xFF333333.toInt()     // 深灰按壓背景 (#333333，舊硬編碼 -13421773)
    private val COLOR_BLACK = Color.BLACK                  // 純黑 (#000000，舊硬編碼 -16777216)
    private val COLOR_WHITE = Color.WHITE                  // 純白 (#FFFFFF，舊硬編碼 -1)
    private val COLOR_DISABLED_TEXT = 0xFF808080.toInt()   // 停用文字灰 (#808080，舊硬編碼 -8355712)

    /**
     * 對話框按鈕與單元格之背景 StateListDrawable
     */
    val alertItemBackgroundDrawable: Drawable
        get() {
            val stateListDrawable = StateListDrawable()
            stateListDrawable.addState(STATE_PRESSED_ENABLED, COLOR_PRESSED_BG.toDrawable())
            stateListDrawable.addState(STATE_ENABLED_FOCUSED, COLOR_BLACK.toDrawable())
            stateListDrawable.addState(STATE_ENABLED, COLOR_BLACK.toDrawable())
            stateListDrawable.addState(STATE_EMPTY, COLOR_BLACK.toDrawable())
            return stateListDrawable
        }

    /**
     * 對話框按鈕之文字 ColorStateList
     */
    val alertItemTextColor: ColorStateList
        get() = ColorStateList(
            arrayOf(
                STATE_PRESSED_ENABLED,
                STATE_ENABLED_FOCUSED,
                STATE_ENABLED,
                STATE_EMPTY
            ),
            intArrayOf(COLOR_BLACK, COLOR_BLACK, COLOR_WHITE, COLOR_DISABLED_TEXT)
        )

    /** 預設觸控區塊高度 (60dp) */
    val defaultTouchBlockHeight: Float
        get() = 60.0f

    /** 預設觸控區塊寬度 (60dp) */
    val defaultTouchBlockWidth: Float
        get() = 60.0f

    /** 大尺寸對話框寬度 (320dp) */
    val dialogWidthLarge: Float
        get() = 320.0f

    /** 一般尺寸對話框寬度 (270dp) */
    val dialogWidthNormal: Float
        get() = 270.0f

    /**
     * 清單單元格之背景 StateListDrawable
     */
    val listItemBackgroundDrawable: Drawable
        get() {
            val stateListDrawable = StateListDrawable()
            stateListDrawable.addState(STATE_PRESSED_ENABLED, COLOR_PRESSED_BG.toDrawable())
            stateListDrawable.addState(STATE_ENABLED_FOCUSED, COLOR_BLACK.toDrawable())
            stateListDrawable.addState(STATE_ENABLED, COLOR_BLACK.toDrawable())
            stateListDrawable.addState(STATE_EMPTY, COLOR_BLACK.toDrawable())
            return stateListDrawable
        }

    /**
     * 清單單元格之文字 ColorStateList
     */
    val listItemTextColor: ColorStateList
        get() = ColorStateList(
            arrayOf(
                STATE_PRESSED_ENABLED,
                STATE_ENABLED_FOCUSED,
                STATE_ENABLED
            ),
            intArrayOf(COLOR_BLACK, COLOR_BLACK, COLOR_WHITE)
        )

    /** 大內邊距 (20dp) */
    val paddingLarge: Float
        get() = 20.0f

    /** 一般內邊距 (10dp) */
    val paddingNormal: Float
        get() = 10.0f

    /** 小內邊距 (5dp) */
    val paddingSmall: Float
        get() = 5.0f

    /** 大字體大小 (24sp) */
    val textSizeLarge: Float
        get() = 24.0f

    /** 一般字體大小 (20sp) */
    val textSizeNormal: Float
        get() = 20.0f

    /** 小字體大小 (16sp) */
    val textSizeSmall: Float
        get() = 16.0f

    /** 超大字體大小 (28sp) */
    val textSizeUltraLarge: Float
        get() = 28.0f

    companion object {
        private var layoutParamsInstance: ASLayoutParams? = null

        /** 取得 [ASLayoutParams] 單例物件 */
        @JvmStatic
        val instance: ASLayoutParams
            get() {
                if (layoutParamsInstance == null) {
                    layoutParamsInstance = ASLayoutParams()
                }
                return layoutParamsInstance!!
            }
    }
}
