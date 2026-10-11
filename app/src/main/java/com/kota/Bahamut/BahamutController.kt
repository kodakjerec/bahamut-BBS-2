package com.kota.Bahamut

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.kota.Bahamut.dataModels.ArticleTempStore
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.pages.StartPage
import com.kota.Bahamut.pages.login.WebAutoSignInManager
import com.kota.Bahamut.pages.messages.MessageSmall
import com.kota.Bahamut.pages.model.BoardEssencePageItem
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.pages.model.ClassPageBlock
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.pages.model.MailBoxPageBlock
import com.kota.Bahamut.pages.model.MailBoxPageItem
import com.kota.Bahamut.service.BahaBBSBackgroundService
import com.kota.Bahamut.service.CommonFunctions.changeScreenOrientation
import com.kota.Bahamut.service.MyBillingClient.checkPurchase
import com.kota.Bahamut.service.MyBillingClient.closeBillingClient
import com.kota.Bahamut.service.MyBillingClient.initBillingClient
import com.kota.Bahamut.service.SyncManager
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.Bahamut.service.TempSettings.setMessageSmall
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAnimationEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesKeepWifi
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASDialog.Companion.dismissAllDialogs
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASViewController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.Companion.construct
import com.kota.telnet.TelnetClientListener
import com.kota.telnetUI.TelnetPage
import com.kota.textEncoder.B2UEncoder
import com.kota.textEncoder.U2BEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Vector

/**
 * [BahamutController] - 應用程式主導航控制器與 Telnet 連線事件管理中心。
 *
 * 職責：
 * 1. 繼承 [ASNavigationController]，作為全域頁面堆疊切換之根控制器。
 * 2. 實作 [TelnetClientListener]，監聽 Telnet 連線建立、成功、失敗與斷線狀態。
 * 3. 管理全域系統初始化 (編碼器、書籤、暫存檔、金流 BillingClient、前景服務等)。
 */
class BahamutController : ASNavigationController(), TelnetClientListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyHighRefreshRate(this, immediate = true)
    }

    override fun onControllerWillLoad() {
        requestWindowFeature(1)
        try {
            if (B2UEncoder.instance == null) {
                B2UEncoder.constructInstance(resources.openRawResource(R.raw.b2u))
            }
            if (U2BEncoder.instance == null) {
                U2BEncoder.constructInstance(resources.openRawResource(R.raw.u2b))
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "Encoder init error")
        }

        // 初始化書籤資料
        val bookmarkFilePath = filesDir.path + "/bookmark.dat"
        BookmarkStore.upgrade(this, bookmarkFilePath)

        // 初始化暫存檔資料
        val articleFilePath = filesDir.path + "/article_temp.dat"
        ArticleTempStore.upgrade(this, articleFilePath)

        // 初始化 TelnetClient 及狀態處理器
        if (TelnetClient.myInstance == null) {
            construct(BahamutStateHandler.getInstance())
        }
        TelnetClient.myInstance!!.setListener(this)

        // 若已連線，觸發一次狀態更新以同步 UI
        if (TelnetClient.myInstance!!.telnetConnector?.isConnecting == true) {
            ASCoroutine.ensureMainThread {
                BahamutStateHandler.getInstance().handleState()
            }
        }

        // 設定 TelnetConnector 的設備控制器
        TelnetClient.myInstance!!.telnetConnector?.setDeviceController(deviceController)

        if (PageContainer.instance == null) {
            PageContainer.constructInstance()
        }

        // 設定 UserSettings 動畫與 Wi-Fi 鎖定
        isAnimationEnable = propertiesAnimationEnable
        if (propertiesKeepWifi) {
            deviceController?.lockWifi()
        }

        // 全域設定
        TempSettings.myContext = this
        TempSettings.myActivity = currentController
        changeScreenOrientation()

        TempSettings.applicationContext = applicationContext
        initBillingClient()
    }

    override fun onControllerDidLoad() {
        // 未連線時，顯示起始頁面 (StartPage)
        if (TelnetClient.myInstance?.telnetConnector?.isConnecting != true) {
            val startPage: StartPage = PageContainer.instance!!.startPage
            pushViewController(startPage, false)
        }
    }

    override fun onResume() {
        checkPurchase()
        super.onResume()
    }

    override fun onDestroy() {
        dismissAllDialogs()
        closeBillingClient()
        SyncManager.cleanup()

        // 僅在 Activity 真正結束時關閉 Telnet 連線
        if (isFinishing) {
            TelnetClient.myInstance!!.close()
        }

        if (TelnetClient.myInstance!!.telnetConnector != null) {
            TelnetClient.myInstance!!.telnetConnector?.setDeviceController(null)
        }

        super.onDestroy()
    }

    override val controllerName: String
        get() = R.string.app_name.toString()

    /**
     * 處理實體返回鍵長按事件：彈出「強制斷線」確認對話框
     */
    override fun onBackLongPressed(): Boolean {
        var result = true
        if (TelnetClient.myInstance!!.telnetConnector?.isConnecting == true) {
            val dialog = ASAlertDialog("FORCE_CLOSE_CONFIRM")
            dialog
                .setMessage("是否確定要強制斷線?")
                .addButton("取消")
                .addButton("斷線")
                .setListener(object : ASAlertDialogListener {
                    override fun onAlertDialogDismissWithButtonIndex(
                        paramASAlertDialog: ASAlertDialog,
                        paramInt: Int
                    ) {
                        if (paramInt == 1) {
                            TelnetClient.myInstance!!.close()
                            TempSettings.lastVisitArticleNumber = 0
                        }
                    }
                }).show()
        } else {
            result = false
        }
        dismissProcessingDialog()
        return result
    }

    private fun showConnectionStartMessage() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TRADITIONAL_CHINESE)
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
        val timeString = dateFormat.format(Date())
        println("BahaBBS connection start:$timeString")
    }

    override fun onTelnetClientConnectionStart(telnetClient: TelnetClient) {
        ASCoroutine.ensureMainThread {
            this@BahamutController.showConnectionStartMessage()
        }
    }

    override fun onTelnetClientConnectionSuccess(telnetClient: TelnetClient) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        startForegroundService(intent)
    }

    override fun onTelnetClientConnectionFail(telnetClient: TelnetClient) {
        dismissProcessingDialog()
        showShortToast("連線失敗，請檢查網路連線或稍後再試")
    }

    override fun onTelnetClientConnectionClosed(telnetClient: TelnetClient) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        stopService(intent)
        WebAutoSignInManager.stop()
        ASCoroutine.ensureMainThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TRADITIONAL_CHINESE)
            dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
            val timeString = dateFormat.format(Date())
            println("BahaBBS connection close:$timeString")

            this@BahamutController.handleNormalConnectionClosed()
            showShortToast("連線已中斷")
            dismissProcessingDialog()

            SyncManager.onConnectionClosed()

            if (getMessageSmall() != null) {
                val messageSmall: MessageSmall? = getMessageSmall()
                currentController?.removeForeverView(messageSmall)
                setMessageSmall(null)
            }
        }
    }

    private fun handleNormalConnectionClosed() {
        val pages: Vector<ASViewController> = currentController!!.viewControllers
        val newControllers = Vector<ASViewController>()
        for (controller in pages) {
            val telnetPage = controller as TelnetPage
            if (telnetPage.pageType == 0 || telnetPage.isKeepOnOffline) {
                newControllers.add(telnetPage)
            }
        }
        val startPage: StartPage = PageContainer.instance!!.startPage
        if (!newControllers.contains(startPage)) {
            newControllers.insertElementAt(startPage, 0)
        }
        setViewControllers(newControllers)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        BoardPageBlock.release()
        BoardPageItem.release()
        BoardEssencePageItem.release()
        ClassPageBlock.release()
        ClassPageItem.release()
        MailBoxPageBlock.release()
        MailBoxPageItem.release()
        System.gc()
    }

    override var isAnimationEnable: Boolean = false
        get() = propertiesAnimationEnable

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Glide.get(this).trimMemory(level)
    }

    companion object {
        private var targetActivityRef: java.lang.ref.WeakReference<Activity>? = null

        private val highRefreshRateCoroutine = object : ASCoroutine() {
            override suspend fun run() {
                val act = targetActivityRef?.get() ?: return
                performApplyHighRefreshRate(act)
            }
        }

        fun isHighRefreshRateSupported(context: Context?): Boolean {
            if (context == null) return false
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(WINDOW_SERVICE) as? android.view.WindowManager)?.defaultDisplay
            } ?: return false

            val supportedModes = display.supportedModes ?: return false
            return supportedModes.any { it.refreshRate > 60.5f }
        }

        @JvmStatic
        @JvmOverloads
        fun applyHighRefreshRate(activity: Activity, immediate: Boolean = false) {
            targetActivityRef = java.lang.ref.WeakReference(activity)
            if (immediate) {
                highRefreshRateCoroutine.cancel()
                ASCoroutine.ensureMainThread {
                    performApplyHighRefreshRate(activity)
                }
            } else {
                highRefreshRateCoroutine.postDelayed(300L)
            }
        }

        @android.annotation.SuppressLint("NewApi", "DEPRECATION")
        private fun performApplyHighRefreshRate(activity: Activity) {
            val window = activity.window ?: return

            val currentDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display
            } else {
                activity.windowManager.defaultDisplay
            } ?: window.decorView.display ?: return

            val currentMode = currentDisplay.mode ?: return
            val supportedModes = currentDisplay.supportedModes ?: return

            val targetMode: android.view.Display.Mode?

            if (UserSettings.propertiesHighRefreshRate) {
                // 【開啟高幀率】
                // 策略 1：優先在「當前解析度」下尋找最高刷新率的 Mode
                var highMode = supportedModes
                    .filter { it.physicalWidth == currentMode.physicalWidth && it.physicalHeight == currentMode.physicalHeight }
                    .maxByOrNull { it.refreshRate }

                // 策略 2：若當前解析度無更高刷新率，選取刷新率最高中解析度最大的 Mode（避免選到小解析度 720p 等）
                if (highMode == null || highMode.refreshRate <= currentMode.refreshRate) {
                    highMode = supportedModes.maxWithOrNull(
                        compareBy<android.view.Display.Mode> { it.refreshRate }
                            .thenBy { it.physicalWidth * it.physicalHeight }
                    )
                }
                targetMode = highMode
            } else {
                // 【關閉高幀率 - 明確指定 60Hz Mode 避免卡在 40Hz 等動態降頻模式】
                // 階層 1：優先在當前解析度下找接近 60Hz (58Hz~62Hz) 的 Mode
                var normMode = supportedModes
                    .filter {
                        it.physicalWidth == currentMode.physicalWidth &&
                                it.physicalHeight == currentMode.physicalHeight &&
                                it.refreshRate in 58.0f..62.0f
                    }
                    .minByOrNull { kotlin.math.abs(it.refreshRate - 60.0f) }

                // 階層 2：找任意解析度下最接近 60Hz 且解析度最大的 Mode
                if (normMode == null) {
                    normMode = supportedModes
                        .filter { it.refreshRate in 58.0f..62.0f }
                        .maxWithOrNull(
                            compareBy<android.view.Display.Mode> { -kotlin.math.abs(it.refreshRate - 60.0f) }
                                .thenBy { it.physicalWidth * it.physicalHeight }
                        )
                }

                // 階層 3：若無標準 60Hz，優先在當前解析度尋找 <= 62Hz 中最高的 Mode (例如 50Hz, 48Hz)
                if (normMode == null) {
                    normMode = supportedModes
                        .filter {
                            it.physicalWidth == currentMode.physicalWidth &&
                                    it.physicalHeight == currentMode.physicalHeight &&
                                    it.refreshRate <= 62.0f
                        }
                        .maxByOrNull { it.refreshRate }
                }

                // 階層 4：若仍無，全域尋找 <= 62Hz 的最大模式，若連 <= 62Hz 都沒有，則挑選全域最低刷新率模式
                if (normMode == null) {
                    normMode = supportedModes
                        .filter { it.refreshRate <= 62.0f }
                        .maxWithOrNull(
                            compareBy<android.view.Display.Mode> { it.refreshRate }
                                .thenBy { it.physicalWidth * it.physicalHeight }
                        )
                        ?: supportedModes.minWithOrNull(
                            compareBy<android.view.Display.Mode> { it.refreshRate }
                                .thenByDescending { it.physicalWidth * it.physicalHeight }
                        )
                }

                targetMode = normMode
            }

            if (targetMode != null) {
                if (targetMode.modeId != currentMode.modeId) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = targetMode.modeId
                    window.attributes = lp
                }
            } else if (!UserSettings.propertiesHighRefreshRate) {
                // 若找不到標準 60Hz Mode，備用重置為 0
                val lp = window.attributes
                if (lp.preferredDisplayModeId != 0) {
                    lp.preferredDisplayModeId = 0
                    window.attributes = lp
                }
            }
        }
    }
}
