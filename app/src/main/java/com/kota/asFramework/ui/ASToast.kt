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
import java.lang.ref.WeakReference // 加入此行

object ASToast {
    /**
     * 堆疊式 Toast 顯示器
     */
    private var previousToastRef: Toast? = null

    /** Toast 容器（使用弱引用避免記憶體洩漏） */
    private var toastContainerRef: WeakReference<LinearLayout>? = null

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
            val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager

            var toastContainer = toastContainerRef?.get()

            // 初始化容器
            if (toastContainer == null || toastContainer.windowToken == null) {
                toastContainer = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                toastContainerRef = WeakReference(toastContainer)

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
                    windowManager.addView(toastContainer, params)
                } catch (_: Exception) {
                    fallbackToSystemToast(message, duration)
                    return@ensureMainThread
                }
            }

            val toastView = createToastView(message)
            toastView.alpha = 0f

            // 新 Toast 加在底部
            toastContainer.addView(toastView)

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
                setColor(Color.DKGRAY)
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
                    val container = view.parent as? LinearLayout
                    container?.removeView(view)

                    if (container?.childCount == 0) {
                        try {
                            val wm = container.context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
                            wm.removeView(container)
                        } catch (_: Exception) {}

                        if (toastContainerRef?.get() == container) {
                            toastContainerRef = null
                        }
                    }
                }
            })
            start()
        }
    }

    private fun fallbackToSystemToast(message: String?, duration: Long) {
        previousToastRef?.cancel()
        val context = ASNavigationController.currentController ?: return
        val toastDuration = if (duration <= DURATION_SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
        val toast = Toast.makeText(context, message, toastDuration)
        previousToastRef = toast
        toast.show()
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
            val container = toastContainerRef?.get()
            if (container != null) {
                container.removeAllViews()
                try {
                    val wm = container.context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
                    wm.removeView(container)
                } catch (_: Exception) {}
                toastContainerRef = null
            }
        }
    }
}