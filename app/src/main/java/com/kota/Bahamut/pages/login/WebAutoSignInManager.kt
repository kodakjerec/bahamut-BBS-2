package com.kota.Bahamut.pages.login

import android.content.Context
import android.util.Log
import com.kota.Bahamut.R
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Web 自動簽到管理器
 *
 * 負責協調 BBS 登入成功後觸發的 Web 登入與換日後的跨日自動簽到。
 *
 * 規則：
 * 1. 每次登入成功就會執行 web 登入；回到 startPage 或斷線就停止。
 * 2. Web 自動簽到、Web 帳號、Web 密碼三者同時存在時，自動登入才能順利運作，不然則顯示 debugView 供手動登入。
 * 3. 只會保留最近的一次 web 登入（取消舊的 web 登入）。
 * 4. 跨日登入保留（換日每天只執行一次，避免重複累積）。
 * 5. 如果跨日登入與 web 登入重疊，則只保留跨日登入。
 */
object WebAutoSignInManager {
    private const val TAG = "WebAutoSignInManager"

    enum class AutoLoginType {
        NONE,
        WEB_LOGIN,       // 每次登入成功時執行的 web 登入
        CROSS_DAY_LOGIN  // 換日（跨日）自動簽到
    }

    private var currentLoginType = AutoLoginType.NONE
    private var currentDebugView: LoginWebDebugView? = null
    private var crossDayJob: Job? = null
    private var lastCrossDayDate: String? = null

    /**
     * 是否在 Web 登入時開啟 DebugView 觀看登入過程
     */
    var showDebugView: Boolean = false

    /**
     * 是否具備 Web 登入帳號與密碼
     */
    fun hasWebCredentials(): Boolean {
        return UserSettings.propertiesWebUsername.isNotEmpty() &&
                UserSettings.propertiesWebPassword.isNotEmpty()
    }

    /**
     * 是否滿足 Web 自動登入的完整條件（自動簽到已開啟且具備 Web 帳號密碼）
     */
    fun canAutoSignIn(): Boolean {
        return UserSettings.propertiesWebSignIn && hasWebCredentials()
    }

    /**
     * 取得當前日期字串 (yyyyMMdd)
     */
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * BBS 登入成功時呼叫
     */
    fun onLoginSuccess(context: Context? = null) {
        if (!UserSettings.propertiesWebSignIn) {
            stop()
            return
        }

        val targetContext = resolveContext(context) ?: return

        // 啟動跨日檢查排程
        startCrossDayWatcher()

        // 執行 web 登入
        performWebLogin(targetContext)
    }

    /**
     * 執行一般 web 登入
     * 規則：
     * - 如果跨日登入正在進行中（重疊），只保留跨日登入，忽略本次 web 登入
     * - 只保留最近的一次 web 登入：若已有進行中的 web 登入，取消並清理舊登入
     */
    fun performWebLogin(context: Context) {
        if (!UserSettings.propertiesWebSignIn) return

        ASCoroutine.ensureMainThread {
            // 如果跨日登入與 web 登入重疊，則只保留跨日登入
            if (currentLoginType == AutoLoginType.CROSS_DAY_LOGIN) {
                Log.i(TAG, "跨日登入進行中，重疊時只保留跨日登入，忽略本次 web 登入")
                return@ensureMainThread
            }

            // 只會保留最近的一次 web 登入
            if (currentLoginType == AutoLoginType.WEB_LOGIN) {
                Log.i(TAG, "已有 web 登入執行中，取消舊登入，只保留最近的一次 web 登入")
                cancelCurrentLogin()
            }

            currentLoginType = AutoLoginType.WEB_LOGIN
            startLoginFlow(context, AutoLoginType.WEB_LOGIN)
        }
    }

    /**
     * 執行跨日登入
     * 規則：
     * - 跨日登入保留
     * - 如果跨日登入與 web 登入重疊，則只保留跨日登入（取消 web 登入，保留跨日登入）
     */
    fun performCrossDayLogin(context: Context) {
        if (!UserSettings.propertiesWebSignIn) return

        ASCoroutine.ensureMainThread {
            // 如果跨日登入與 web 登入重疊，則只保留跨日登入
            if (currentLoginType == AutoLoginType.WEB_LOGIN) {
                Log.i(TAG, "web 登入執行中，與跨日登入重疊，取消 web 登入，只保留跨日登入")
                cancelCurrentLogin()
            } else if (currentLoginType == AutoLoginType.CROSS_DAY_LOGIN) {
                Log.i(TAG, "跨日登入已在執行中，保留原跨日登入")
                return@ensureMainThread
            }

            currentLoginType = AutoLoginType.CROSS_DAY_LOGIN
            startLoginFlow(context, AutoLoginType.CROSS_DAY_LOGIN)
        }
    }

    /**
     * 啟動跨日檢查器（每分鐘檢查一次是否換日）
     */
    private fun startCrossDayWatcher() {
        if (lastCrossDayDate == null) {
            lastCrossDayDate = getTodayDateString()
        }

        if (crossDayJob != null && crossDayJob!!.isActive) {
            return
        }

        crossDayJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(60_000L) // 每分鐘檢查一次
                val today = getTodayDateString()
                if (lastCrossDayDate != null && today != lastCrossDayDate) {
                    Log.i(TAG, "檢測到跨日 (上次: $lastCrossDayDate, 今日: $today)，執行跨日登入")
                    lastCrossDayDate = today

                    val context = resolveContext(null)
                    if (context != null) {
                        performCrossDayLogin(context)
                    } else {
                        Log.w(TAG, "跨日登入時無可用 Context，略過")
                    }
                }
            }
        }
    }

    /**
     * 啟動登入視圖與 WebView 流程
     */
    private fun startLoginFlow(context: Context, type: AutoLoginType) {
        try {
            val hasCredentials = hasWebCredentials()
            if (hasCredentials) {
                ASToast.showShortToast(context.getString(R.string.login_web_sign_in_msg01))
            } else {
                ASToast.showShortToast(context.getString(R.string.login_web_sign_in_need_manual))
            }

            val debugView = LoginWebDebugView(context)
            currentDebugView = debugView

            debugView.startAutoLogin(
                onLoginSuccess = {
                    TempSettings.setWebAutoLoginSuccessTime(System.currentTimeMillis())
                },
                onComplete = {
                    ASCoroutine.ensureMainThread {
                        if (currentDebugView === debugView) {
                            currentDebugView = null
                            currentLoginType = AutoLoginType.NONE
                        }
                    }
                }
            )

            // 若開啟除錯視圖，或未具備完整帳密（三者條件未滿足，需要手動登入），直接顯示對話框以觀看/操作登入過程
            if (showDebugView || !hasCredentials) {
                debugView.show()
            }
        } catch (e: Exception) {
            ASToast.showShortToast(context.getString(R.string.login_web_sign_in_msg04))
            Log.e(TAG, "啟動自動登入失敗: ${e.message}", e)
            if (currentLoginType == type) {
                currentLoginType = AutoLoginType.NONE
                currentDebugView = null
            }
        }
    }

    /**
     * 開啟當前進行中登入的 DebugView 視窗以觀看過程
     */
    fun openCurrentDebugView() {
        ASCoroutine.ensureMainThread {
            currentDebugView?.show()
        }
    }

    /**
     * 取消當前執行中的登入
     */
    private fun cancelCurrentLogin() {
        val debugView = currentDebugView
        currentDebugView = null
        currentLoginType = AutoLoginType.NONE
        if (debugView != null) {
            try {
                debugView.dismiss()
            } catch (e: Exception) {
                Log.e(TAG, "取消登入視圖失敗: ${e.message}", e)
            }
        }
    }

    /**
     * 回到 startPage 或 斷線就停止
     */
    @Synchronized
    fun stop() {
        val hasJob = crossDayJob != null
        val hasLogin = currentLoginType != AutoLoginType.NONE || currentDebugView != null

        // 如果原本就沒有任何排程或登入在執行，直接返回，避免重複執行與不必要的 MainThread 調度
        if (!hasJob && !hasLogin) {
            return
        }

        Log.i(TAG, "停止 Web 自動簽到與跨日監聽")

        // 停止跨日排程
        crossDayJob?.cancel()
        crossDayJob = null
        lastCrossDayDate = null

        // 取消並清理當前登入
        val debugViewToDismiss = currentDebugView
        currentDebugView = null
        currentLoginType = AutoLoginType.NONE

        if (debugViewToDismiss != null) {
            ASCoroutine.ensureMainThread {
                try {
                    debugViewToDismiss.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "取消登入視圖失敗: ${e.message}", e)
                }
            }
        }
    }

    /**
     * 解析可用的 Context，優先使用前景 Activity
     */
    private fun resolveContext(preferredContext: Context?): Context? {
        val activity = ASNavigationController.currentController ?: TempSettings.myActivity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            return activity
        }
        if (preferredContext != null) {
            return preferredContext
        }
        return TempSettings.myContext
    }

    fun getCurrentLoginType(): AutoLoginType = currentLoginType
    fun isRunning(): Boolean = (crossDayJob != null && crossDayJob?.isActive == true) ||
            currentLoginType != AutoLoginType.NONE ||
            currentDebugView != null
}

