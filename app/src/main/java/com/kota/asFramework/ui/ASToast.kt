package com.kota.asFramework.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine

/**
 * 堆疊式 Toast 顯示器
 * 
 * 特色：
 * - 新 Toast 出現在下方，舊的向上堆疊
 * - 每個 Toast 保持各自的消失時間
 * - 支援淡入淡出動畫
 */
object ASToast {
    private var previousToast: Toast? = null
    
    /** Toast 容器（垂直 LinearLayout，新項目加在底部） */
    private var toastContainer: LinearLayout? = null
    
    /** WindowManager 用於顯示浮動視窗 */
    private var windowManager: WindowManager? = null
    
    /** 主執行緒 Handler */
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private const val DURATION_SHORT = 2000L
    private const val DURATION_LONG = 3500L
    private const val ANIMATION_DURATION = 200L
    
    @JvmStatic
    fun showShortToast(aToastMessage: String?) {
        showStackedToast(aToastMessage, DURATION_SHORT)
    }

    @JvmStatic
    fun showLongToast(aToastMessage: String?) {
        showStackedToast(aToastMessage, DURATION_LONG)
    }
    
    private fun showStackedToast(message: String?, duration: Long) {
        if (message.isNullOrEmpty()) return
        
        ASCoroutine.ensureMainThread {
            val context = ASNavigationController.currentController ?: return@ensureMainThread
            
            if (windowManager == null) {
                windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
            }
            
            // 初始化容器
            if (toastContainer == null || toastContainer?.windowToken == null) {
                toastContainer = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    y = dpToPx(80)
                    token = context.window.decorView.windowToken
                }
                
                try {
                    windowManager?.addView(toastContainer, params)
                } catch (e: Exception) {
                    fallbackToSystemToast(message, duration)
                    return@ensureMainThread
                }
            }
            
            val toastView = createToastView(message)
            toastView.alpha = 0f
            
            // 新 Toast 加在底部
            toastContainer?.addView(toastView)
            
            // 淡入動畫
            ObjectAnimator.ofFloat(toastView, "alpha", 0f, 1f).apply {
                this.duration = ANIMATION_DURATION
                start()
            }
            
            // 設定消失時間
            mainHandler.postDelayed({ removeToastView(toastView) }, duration)
        }
    }
    
    private fun createToastView(message: String): View {
        val context = ASNavigationController.currentController ?: throw IllegalStateException()
        
        return TextView(context).apply {
            text = message
            setTextColor(Color.WHITE)
            textSize = 14f
            
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6323232"))
                cornerRadius = dpToPx(20).toFloat()
            }
            
            val paddingH = dpToPx(16)
            val paddingV = dpToPx(10)
            setPadding(paddingH, paddingV, paddingH, paddingV)
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(4), 0, dpToPx(4))
            }
            
            gravity = Gravity.CENTER
        }
    }
    
    private fun removeToastView(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            duration = ANIMATION_DURATION
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    toastContainer?.removeView(view)
                    if (toastContainer?.childCount == 0) {
                        try { windowManager?.removeView(toastContainer) } catch (e: Exception) {}
                        toastContainer = null
                    }
                }
            })
            start()
        }
    }
    
    private fun fallbackToSystemToast(message: String?, duration: Long) {
        previousToast?.cancel()
        val toastDuration = if (duration <= DURATION_SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
        previousToast = Toast.makeText(ASNavigationController.currentController, message, toastDuration)
        previousToast?.show()
    }
    
    private fun dpToPx(dp: Int): Int {
        val context = ASNavigationController.currentController ?: return dp
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics
        ).toInt()
    }
    
    @JvmStatic
    fun clearAll() {
        ASCoroutine.ensureMainThread {
            mainHandler.removeCallbacksAndMessages(null)
            toastContainer?.removeAllViews()
            try { windowManager?.removeView(toastContainer) } catch (e: Exception) {}
            toastContainer = null
        }
    }
}