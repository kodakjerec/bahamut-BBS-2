package com.kota.asFramework.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.ANIMATION_DURATION
import com.kota.asFramework.ui.ASToast.DEBOUNCE_INTERVAL

/**
 * 自訂堆疊式 Toast 顯示器 ([ASToast])。
 *
 * 功能特色：
 * 1. **堆疊式呈現**：多個 Toast 訊息可依序向上/向下堆疊呈現，避免舊訊息被瞬間覆蓋。
 * 2. **Debounce 防抖與重複過濾**：設定固定時間間隔 ([DEBOUNCE_INTERVAL]) 與畫面上文字比對，自動過濾快速連續觸發的相同訊息。
 * 3. **淡入淡出動畫**：包含滑順的 Alpha 動畫 ([ANIMATION_DURATION])。
 * 4. **系統降級備援**：當無法取得 WindowManager 或 DecorView 權限時，自動切換為原生 [Toast]。
 */
object ASToast {

    /** 原生系統 Toast 備援引用 */
    private var previousToastRef: Toast? = null

    /** 自訂 Toast 視圖懸浮容器 (LinearLayout) */
    private var toastContainer: LinearLayout? = null

    /** 主執行緒 Handler */
    private val mainHandler = Handler(Looper.getMainLooper())

    /** 短時間顯示長度 (毫秒) */
    private const val DURATION_SHORT = 2000L

    /** 長時間顯示長度 (毫秒) */
    private const val DURATION_LONG = 3500L

    /** 淡入/淡出動畫時間 (毫秒) */
    private const val ANIMATION_DURATION = 200L

    /** 同一訊息連續觸發的去重複防抖時間門檻 (毫秒) */
    private const val DEBOUNCE_INTERVAL = 500L

    /** Handler 延遲任務的追蹤標記物件 */
    private val TOAST_TOKEN = Any()

    /** 最近一次顯示的訊息內容 (用於防抖過濾) */
    private var lastMessage: String? = null

    /** 最近一次顯示訊息的時間戳記 (uptimeMillis) */
    private var lastShownTime: Long = 0L

    /**
     * 顯示短時間 Toast 訊息 (約 2 秒)
     *
     * @param aToastMessage 要顯示的文字訊息
     */
    @JvmStatic
    fun showShortToast(aToastMessage: String?) {
        showStackedToast(aToastMessage, DURATION_SHORT)
    }

    /**
     * 顯示長時間 Toast 訊息 (約 3.5 秒)
     *
     * @param aToastMessage 要顯示的文字訊息
     */
    @JvmStatic
    fun showLongToast(aToastMessage: String?) {
        showStackedToast(aToastMessage, DURATION_LONG)
    }

    /**
     * 顯示堆疊式 Toast 主邏輯 (包含 Debounce 去重、懸浮視圖建立與動態動畫)
     *
     * @param message 訊息內容
     * @param duration 顯示持續時間 (毫秒)
     */
    private fun showStackedToast(message: String?, duration: Long) {
        if (message.isNullOrEmpty()) return

        ASCoroutine.ensureMainThread {
            val now = SystemClock.uptimeMillis()

            // 1. 時間防抖 (Debounce)：若完全相同的訊息在時間門檻內重複發送，直接過濾
            if (message == lastMessage && (now - lastShownTime) < DEBOUNCE_INTERVAL) {
                return@ensureMainThread
            }

            // 2. 視圖文字過濾：若當前顯示容器中已有相同文字的 Toast，不重複疊加
            val existingContainer = toastContainer
            if (existingContainer != null && existingContainer.windowToken != null) {
                for (i in 0 until existingContainer.childCount) {
                    val child = existingContainer.getChildAt(i)
                    if (child is TextView && child.text == message) {
                        return@ensureMainThread
                    }
                }
            }

            // 更新最後顯示紀錄
            lastMessage = message
            lastShownTime = now

            val context = ASNavigationController.currentController ?: return@ensureMainThread
            val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager

            var container = toastContainer

            // 初始化懸浮容器
            if (container == null || container.windowToken == null) {
                container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                toastContainer = container

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
                    windowManager.addView(container, params)
                } catch (_: Exception) {
                    fallbackToSystemToast(message, duration)
                    return@ensureMainThread
                }
            }

            // 建立 Toast TextView 視圖
            val toastView = createToastView(message)
            toastView.alpha = 0f

            // 新 Toast 增加在容器底部
            container.addView(toastView)

            // 執行淡入動畫
            ObjectAnimator.ofFloat(toastView, "alpha", 0f, 1f).apply {
                this.duration = ANIMATION_DURATION
                start()
            }

            // 設定定時自動淡出清理任務
            mainHandler.postAtTime({ removeToastView(toastView) }, TOAST_TOKEN, SystemClock.uptimeMillis() + duration)
        }
    }

    /**
     * 建立 Toast 文字視圖與背景樣式
     *
     * @param message 訊息內文
     * @return 配置完成的 [TextView] View
     */
    private fun createToastView(message: String): View {
        val context = ASNavigationController.currentController ?: throw IllegalStateException("NavigationController is null")

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

    /**
     * 執行 Toast 視圖淡出動畫並從 WindowManager/容器 中移除
     *
     * @param view 要移除的 Toast View
     */
    private fun removeToastView(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            duration = ANIMATION_DURATION
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val container = view.parent as? LinearLayout
                    container?.removeView(view)

                    // 若容器內已無 Toast View，銷毀容器
                    if (container?.childCount == 0) {
                        try {
                            val wm = container.context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
                            wm.removeView(container)
                        } catch (_: Exception) {}

                        if (toastContainer == container) {
                            toastContainer = null
                        }
                    }
                }
            })
            start()
        }
    }

    /**
     * 當無法使用自訂 WindowManager 容器時的降級備援 (使用原生 [Toast])
     *
     * @param message 訊息內文
     * @param duration 持續時間
     */
    private fun fallbackToSystemToast(message: String?, duration: Long) {
        previousToastRef?.cancel()
        val context = ASNavigationController.currentController ?: return
        val toastDuration = if (duration <= DURATION_SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
        val toast = Toast.makeText(context, message, toastDuration)
        previousToastRef = toast
        toast.show()
    }

    /**
     * DP 轉成 PX 像素單位轉換工具
     */
    private fun dpToPx(dp: Int): Int {
        val context = ASNavigationController.currentController ?: return dp
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    /**
     * 清除所有正在顯示或排隊中的 Toast 視圖與定時任務
     */
    @JvmStatic
    fun clearAll() {
        ASCoroutine.ensureMainThread {
            // 取消所有排隊中的延遲任務
            mainHandler.removeCallbacksAndMessages(TOAST_TOKEN)

            // 重置防抖記錄
            lastMessage = null
            lastShownTime = 0L

            // 清理並銷毀容器
            val container = toastContainer
            if (container != null) {
                container.removeAllViews()
                try {
                    val wm = container.context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
                    wm.removeViewImmediate(container)
                } catch (_: Exception) {
                } finally {
                    toastContainer = null
                }
            }
        }
    }
}
