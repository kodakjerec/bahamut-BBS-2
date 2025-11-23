package com.kota.Bahamut

import android.content.Intent
import android.util.Log
import com.kota.Bahamut.dataModels.ArticleTempStore
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.pages.StartPage
import com.kota.Bahamut.pages.messages.MessageSmall
import com.kota.Bahamut.pages.model.BoardEssencePageItem
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.pages.model.ClassPageBlock
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.pages.model.MailBoxPageBlock
import com.kota.Bahamut.pages.model.MailBoxPageItem
import com.kota.Bahamut.pages.theme.ThemeStore.upgrade
import com.kota.Bahamut.service.BahaBBSBackgroundService
import com.kota.Bahamut.service.CloudBackup
import com.kota.Bahamut.service.CommonFunctions.changeScreenOrientation
import com.kota.Bahamut.service.MyBillingClient.checkPurchase
import com.kota.Bahamut.service.MyBillingClient.closeBillingClient
import com.kota.Bahamut.service.MyBillingClient.initBillingClient
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.Bahamut.service.TempSettings.setMessageSmall
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAnimationEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesKeepWifi
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASViewController
import com.kota.asFramework.thread.ASRunner
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

class BahamutController : ASNavigationController(), TelnetClientListener {
    // com.kota.asFramework.pageController.ASNavigationController
    override fun onControllerWillLoad() {
        requestWindowFeature(1)
        try {
            B2UEncoder.constructInstance(resources.openRawResource(R.raw.b2u))
            U2BEncoder.constructInstance(resources.openRawResource(R.raw.u2b))
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        // 書籤
        val bookmarkFilePath = filesDir.path + "/bookmark.dat"
        BookmarkStore.upgrade(this, bookmarkFilePath)

        // 暫存檔
        val articleFilePath = filesDir.path + "/article_temp.dat"
        ArticleTempStore.upgrade(this, articleFilePath)

        // 系統架構
        construct(BahamutStateHandler.Companion.getInstance())
        TelnetClient.myInstance!!.setListener(this)


        // 設定 TelnetConnector 的設備控制器
        TelnetClient.myInstance!!.telnetConnector?.setDeviceController(deviceController)

        PageContainer.Companion.constructInstance()

        // UserSettings
        isAnimationEnable = propertiesAnimationEnable
        // 啟用wifi鎖定
        if (propertiesKeepWifi) {
            deviceController?.lockWifi()
        }

        // 共用函數
        TempSettings.myContext = this
        TempSettings.myActivity = currentController
        changeScreenOrientation()

        // 以下需等待 共用函數 設定完畢
        // VIP
        TempSettings.applicationContext = applicationContext
        initBillingClient()

        // 外觀
        upgrade(this)
    }

    // com.kota.asFramework.pageController.ASNavigationController
    override fun onControllerDidLoad() {
        val startPage: StartPage? = PageContainer.instance!!.startPage
        pushViewController(startPage, false)
    }

    override fun onResume() {
        checkPurchase()
        super.onResume()
    }

    override fun onDestroy() {
        // 關閉VIP
        closeBillingClient()

        // 備份雲端
        if (getCloudSave()) {
            val cloudBackup = CloudBackup()
            cloudBackup.backup()
        }

        // 強制關閉連線
        TelnetClient.myInstance!!.close()


        // 清理 TelnetConnector 的設備控制器引用
        if (TelnetClient.myInstance!!.telnetConnector != null) {
            TelnetClient.myInstance!!.telnetConnector?.setDeviceController(null)
        }

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }

    override val controllerName: String?
        // com.kota.asFramework.pageController.ASNavigationController
        get() = R.string.app_name.toString()

    // com.kota.asFramework.pageController.ASNavigationController
    override fun onBackLongPressed(): Boolean {
        var result = true
        if (TelnetClient.myInstance!!.telnetConnector?.isConnecting == true) {
            val dialog = ASAlertDialog()
            dialog
                .setMessage("是否確定要強制斷線?")
                .addButton("取消")
                .addButton("斷線")
                .setListener(object : ASAlertDialogListener {
                    // from class: com.kota.Bahamut.BahamutController.1
                    // com.kota.asFramework.dialog.ASAlertDialogListener
                    override fun onAlertDialogDismissWithButtonIndex(
                        paramASAlertDialog: ASAlertDialog,
                        paramInt: Int
                    ) {
                        if (paramInt == 1) {
                            TelnetClient.myInstance!!.close()
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:hh:ss", Locale.TRADITIONAL_CHINESE)
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
        val timeString = dateFormat.format(Date())
        println("BahaBBS connection start:$timeString")
    }

    // com.kota.telnet.TelnetClientListener
    override fun onTelnetClientConnectionStart(telnetClient: TelnetClient) {
        object : ASRunner() {
            // from class: com.kota.Bahamut.BahamutController.2
            // com.kota.asFramework.thread.ASRunner
            override fun run() {
                this@BahamutController.showConnectionStartMessage()
            }
        }.runInMainThread()
    }

    // com.kota.telnet.TelnetClientListener
    override fun onTelnetClientConnectionSuccess(telnetClient: TelnetClient) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        startForegroundService(intent)
    }

    // com.kota.telnet.TelnetClientListener
    override fun onTelnetClientConnectionFail(telnetClient: TelnetClient) {
        dismissProcessingDialog()
        showShortToast("連線失敗，請檢查網路連線或稍後再試")
    }

    // com.kota.telnet.TelnetClientListener
    override fun onTelnetClientConnectionClosed(telnetClient: TelnetClient) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        stopService(intent)
        object : ASRunner() {
            override fun run() {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:hh:ss", Locale.TRADITIONAL_CHINESE)
                dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
                val timeString = dateFormat.format(Date())
                println("BahaBBS connection close:$timeString")

                this@BahamutController.handleNormalConnectionClosed()

                showShortToast("連線已中斷")

                dismissProcessingDialog()

                if (getCloudSave()) {
                    val cloudBackup = CloudBackup()
                    cloudBackup.backup()
                }

                if (getMessageSmall() != null) {
                    val messageSmall: MessageSmall? = getMessageSmall()
                    currentController?.removeForeverView(messageSmall)
                    setMessageSmall(null)
                }
            }
        }.runInMainThread()
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
        val startPage: StartPage? = PageContainer.instance!!.startPage
        if (!newControllers.contains(startPage)) {
            newControllers.insertElementAt(startPage, 0)
        }
        setViewControllers(newControllers)
    }

    // com.kota.asFramework.pageController.ASNavigationController, android.app.Activity, android.content.ComponentCallbacks
    override fun onLowMemory() {
        super.onLowMemory()
        BoardPageBlock.release()
        BoardPageItem.release()
        BoardEssencePageItem.Companion.release()
        ClassPageBlock.release()
        ClassPageItem.release()
        MailBoxPageBlock.release()
        MailBoxPageItem.release()
        System.gc()
    }

    override var isAnimationEnable: Boolean = false
        // com.kota.asFramework.pageController.ASNavigationController
        get() = propertiesAnimationEnable

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
