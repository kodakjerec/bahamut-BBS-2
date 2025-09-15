package com.kota.Bahamut

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.Window
import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASAlertDialogListener
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.PageController.ASViewController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.DataModels.ArticleTempStore
import com.kota.Bahamut.DataModels.BookmarkStore
import com.kota.Bahamut.Pages.Model.BoardEssencePageItem
import com.kota.Bahamut.Pages.Model.BoardPageBlock
import com.kota.Bahamut.Pages.Model.BoardPageItem
import com.kota.Bahamut.Pages.Model.ClassPageBlock
import com.kota.Bahamut.Pages.Model.ClassPageItem
import com.kota.Bahamut.Pages.Model.MailBoxPageBlock
import com.kota.Bahamut.Pages.Model.MailBoxPageItem
import com.kota.Bahamut.Pages.StartPage
import com.kota.Bahamut.Pages.Theme.ThemeStore
import com.kota.Bahamut.Service.BahaBBSBackgroundService
import com.kota.Bahamut.Service.CloudBackup
import com.kota.Bahamut.Service.CommonFunctions.changeScreenOrientation
import com.kota.Bahamut.Service.MyBillingClient.checkPurchase
import com.kota.Bahamut.Service.MyBillingClient.closeBillingClient
import com.kota.Bahamut.Service.MyBillingClient.initBillingClient
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetClientListener
import com.kota.TelnetUI.TelnetPage
import com.kota.TextEncoder.B2UEncoder
import com.kota.TextEncoder.U2BEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.Vector

class BahamutController : ASNavigationController(), TelnetClientListener {
    
    override fun onControllerWillLoad() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            B2UEncoder.constructInstance(resources.openRawResource(R.raw.b2u))
            U2BEncoder.constructInstance(resources.openRawResource(R.raw.u2b))
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
        
        // 書籤
        val bookmarkFilePath = "${filesDir.path}/bookmark.dat"
        BookmarkStore.upgrade(this, bookmarkFilePath)

        // 暫存檔
        val articleFilePath = "${filesDir.path}/article_temp.dat"
        ArticleTempStore.upgrade(this, articleFilePath)

        // 系統架構
        TelnetClient.construct(BahamutStateHandler.getInstance())
        TelnetClient.getClient().setListener(this)
        
        // 設定 TelnetConnector 的設備控制器
        TelnetClient.getConnector().setDeviceController(deviceController)
        
        PageContainer.constructInstance()

        // UserSettings
        setAnimationEnable(UserSettings.getPropertiesAnimationEnable())
        // 啟用wifi鎖定
        if (UserSettings.getPropertiesKeepWifi()) {
            deviceController.lockWifi()
        }

        // 共用函數
        TempSettings.myContext = this
        TempSettings.myActivity = getCurrentController()
        changeScreenOrientation()

        // 以下需等待 共用函數 設定完畢
        // VIP
        TempSettings.applicationContext = applicationContext
        initBillingClient()

        // 外觀
        ThemeStore.upgrade(this)
    }

    override fun onControllerDidLoad() {
        val startPage = PageContainer.getInstance().startPage
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
        if (NotificationSettings.getCloudSave()) {
            val cloudBackup = CloudBackup()
            cloudBackup.backup()
        }

        // 強制關閉連線
        TelnetClient.getClient().close()
        
        // 清理 TelnetConnector 的設備控制器引用
        TelnetClient.getConnector()?.setDeviceController(null)

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun getControllerName(): String {
        return R.string.app_name.toString()
    }

    override fun onBackLongPressed(): Boolean {
        var result = true
        if (TelnetClient.getConnector().isConnecting) {
            val dialog = ASAlertDialog()
            dialog
                .setMessage("是否確定要強制斷線?")
                .addButton("取消")
                .addButton("斷線")
                .setListener(object : ASAlertDialogListener {
                    override fun onAlertDialogDismissWithButtonIndex(aDialog: ASAlertDialog, index: Int) {
                        if (index == 1) {
                            TelnetClient.getClient().close()
                        }
                    }
                }).show()
        } else {
            result = false
        }
        ASProcessingDialog.dismissProcessingDialog()
        return result
    }

    private fun showConnectionStartMessage() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:hh:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
        val timeString = dateFormat.format(Date())
        println("BahaBBS connection start:$timeString")
    }

    override fun onTelnetClientConnectionStart(aClient: TelnetClient) {
        object : ASRunner() {
            override fun run() {
                showConnectionStartMessage()
            }
        }.runInMainThread()
    }

    override fun onTelnetClientConnectionSuccess(aClient: TelnetClient) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onTelnetClientConnectionFail(aClient: TelnetClient) {
        ASProcessingDialog.dismissProcessingDialog()
        ASToast.showShortToast("連線失敗，請檢查網路連線或稍後再試")
    }

    override fun onTelnetClientConnectionClosed(aClient: TelnetClient) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        stopService(intent)
        object : ASRunner() {
            override fun run() {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:hh:ss")
                dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
                val timeString = dateFormat.format(Date())
                println("BahaBBS connection close:$timeString")

                handleNormalConnectionClosed()

                ASToast.showShortToast("連線已中斷")

                ASProcessingDialog.dismissProcessingDialog()

                if (NotificationSettings.getCloudSave()) {
                    val cloudBackup = CloudBackup()
                    cloudBackup.backup()
                }

                TempSettings.getMessageSmall()?.let { messageSmall ->
                    getCurrentController().removeForeverView(messageSmall)
                    TempSettings.setMessageSmall(null)
                }
            }
        }.runInMainThread()
    }

    private fun handleNormalConnectionClosed() {
        val pages = getCurrentController().viewControllers
        val newControllers = Vector<ASViewController>()
        for (controller in pages) {
            val telnetPage = controller as TelnetPage
            if (telnetPage.pageType == 0 || telnetPage.isKeepOnOffline) {
                newControllers.add(telnetPage)
            }
        }
        val startPage = PageContainer.getInstance().startPage
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

    override fun isAnimationEnable(): Boolean {
        return UserSettings.getPropertiesAnimationEnable()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
