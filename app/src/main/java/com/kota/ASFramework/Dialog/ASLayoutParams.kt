package com.kota.ASFramework.Dialog

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable

class ASLayoutParams private constructor() {
    
    companion object {
        @Volatile
        private var instance: ASLayoutParams? = null
        
        fun getInstance(): ASLayoutParams {
            return instance ?: synchronized(this) {
                instance ?: ASLayoutParams().also { instance = it }
            }
        }
    }
    
    init {
        initial()
    }
    
    private fun initial() {
        // 初始化邏輯
    }
    
    fun getAlertItemBackgroundDrawable(): Drawable {
        val stateListDrawable = StateListDrawable()
        
        var colorDrawable = ColorDrawable(-14066)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled), colorDrawable)
        
        colorDrawable = ColorDrawable(-8388608)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused), colorDrawable)
        
        colorDrawable = ColorDrawable(-12582912)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled), colorDrawable)
        
        colorDrawable = ColorDrawable(-14680064)
        stateListDrawable.addState(intArrayOf(), colorDrawable)
        
        return stateListDrawable
    }
    
    fun getAlertItemTextColor(): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf()
        )
        val colors = intArrayOf(-16777216, -16777216, -1, -8355712)
        return ColorStateList(states, colors)
    }
    
    fun getDefaultTouchBlockHeight(): Float = 60.0f
    
    fun getDefaultTouchBlockWidth(): Float = 60.0f
    
    fun getDialogWidthLarge(): Float = 320.0f
    
    fun getDialogWidthNormal(): Float = 270.0f
    
    fun getListItemBackgroundDrawable(): Drawable {
        val stateListDrawable = StateListDrawable()
        
        var colorDrawable = ColorDrawable(-14066)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled), colorDrawable)
        
        colorDrawable = ColorDrawable(-16777216)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused), colorDrawable)
        
        colorDrawable = ColorDrawable(-16777216)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled), colorDrawable)
        
        colorDrawable = ColorDrawable(-16777216)
        stateListDrawable.addState(intArrayOf(), colorDrawable)
        
        return stateListDrawable
    }
    
    fun getListItemTextColor(): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_enabled)
        )
        val colors = intArrayOf(-16777216, -16777216, -1, -8355712)
        return ColorStateList(states, colors)
    }
    
    fun getPaddingLarge(): Float = 20.0f
    
    fun getPaddingNormal(): Float = 10.0f
    
    fun getPaddingSmall(): Float = 5.0f
    
    fun getTextSizeLarge(): Float = 24.0f
    
    fun getTextSizeNormal(): Float = 20.0f
    
    fun getTextSizeSmall(): Float = 16.0f
    
    fun getTextSizeUltraLarge(): Float = 28.0f
}
