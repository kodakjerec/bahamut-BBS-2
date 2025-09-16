package com.kota.Bahamut

import android.content.Intent
import android.os.Build
import android.util.Log
import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASAlertDialog.setListener
import com.kota.ASFramework.Dialog.ASAlertDialogListener
import com.kota.ASFramework.Dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.PageController.ASViewController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast.showShortToast
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
import com.kota.Bahamut.Pages.Theme.ThemeStore.upgrade
import com.kota.Bahamut.Service.BahaBBSBackgroundService
import com.kota.Bahamut.Service.CloudBackup
import com.kota.Bahamut.Service.CommonFunctions.changeScreenOrientation
import com.kota.Bahamut.Service.MyBillingClient.checkPurchase
import com.kota.Bahamut.Service.MyBillingClient.closeBillingClient
import com.kota.Bahamut.Service.MyBillingClient.initBillingClient
import com.kota.Bahamut.Service.NotificationSettings.getCloudSave
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.TempSettings.getMessageSmall
import com.kota.Bahamut.Service.TempSettings.setMessageSmall
import com.kota.Bahamut.Service.UserSettings.Companion.propertiesAnimationEnable
import com.kota.Bahamut.Service.UserSettings.Companion.propertiesKeepWifi
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetClient.Companion.construct
import com.kota.Telnet.TelnetClient.connector
import com.kota.Telnet.TelnetClientListener
import com.kota.Telnet.TelnetConnector.isConnecting
import com.kota.Telnet.TelnetConnector.setDeviceController
import com.kota.Telnet.TelnetCursor.equals
import com.kota.TelnetUI.TelnetPage
import com.kota.TextEncoder.B2UEncoder
import com.kota.TextEncoder.B2UEncoder.Companion.constructInstance
import com.kota.TextEncoder.U2BEncoder
import com.kota.TextEncoder.U2BEncoder.Companion.constructInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.Vector

class BahamutController : ASNavigationController(), TelnetClientListener {
    // com.kota.ASFramework.PageController.ASNavigationController
    override fun onControllerWillLoad() {
        requestWindowFeature(1)
        try {
            B2UEncoder.constructInstance(getResources().openRawResource(R.raw.b2u))
            U2BEncoder.constructInstance(getResources().openRawResource(R.raw.u2b))
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
        // 書籤
        val bookmark_file_path = getFilesDir().getPath() + "/bookmark.dat"
        BookmarkStore.upgrade(this, bookmark_file_path)

        // 暫存檔
        val article_file_path = getFilesDir().getPath() + "/article_temp.dat"
        ArticleTempStore.upgrade(this, article_file_path)

        // 系統架構
        construct(BahamutStateHandler.Companion.getInstance())
        TelnetClient.getClient().setListener(this)


        // 設定 TelnetConnector 的設備控制器
        TelnetClient.connector.setDeviceController(deviceController)

        PageContainer.Companion.constructInstance()

        // UserSettings
        isAnimationEnable = propertiesAnimationEnable
        // 啟用wifi鎖定
        if (propertiesKeepWifi) {
            deviceController!!.lockWifi()
        }

        // 共用函數
        TempSettings.myContext = this
        TempSettings.myActivity = ASNavigationController.getCurrentController()
        changeScreenOrientation()

        // 以下需等待 共用函數 設定完畢
        // VIP
        TempSettings.applicationContext = getApplicationContext()
        initBillingClient()

        // 外觀
        upgrade(this)
    }

    // com.kota.ASFramework.PageController.ASNavigationController
    override fun onControllerDidLoad() {
        val start_page: StartPage? = PageContainer.Companion.getInstance().getStartPage()
        pushViewController(start_page, false)
    }

    protected override fun onResume() {
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
        TelnetClient.getClient().close()


        // 清理 TelnetConnector 的設備控制器引用
        if (TelnetClient.connector != null) {
            TelnetClient.connector.setDeviceController(null)
        }

        super.onDestroy()
    }

    // com.kota.ASFramework.PageController.ASNavigationController, android.app.Activity
    protected override fun onPause() {
        super.onPause()
    }

    val controllerName: String?
        // com.kota.ASFramework.PageController.ASNavigationController
        get() = R.string.app_name.toString()

    // com.kota.ASFramework.PageController.ASNavigationController
    public override fun onBackLongPressed(): Boolean {
        var result = true
        if (TelnetClient.connector.isConnecting) {
            val dialog = ASAlertDialog()
            dialog
                .setMessage("是否確定要強制斷線?")
                .addButton("取消")
                .addButton("斷線")
                .setListener(object : ASAlertDialogListener {
                    // from class: com.kota.Bahamut.BahamutController.1
                    // com.kota.ASFramework.Dialog.ASAlertDialogListener
                    override fun onAlertDialogDismissWithButtonIndex(
                        aDialog: ASAlertDialog?,
                        index: Int
                    ) {
                        if (index == 1) {
                            TelnetClient.getClient().close()
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
        val date_format = SimpleDateFormat("yyyy-MM-dd kk:hh:ss")
        date_format.setTimeZone(TimeZone.getTimeZone("GMT+8"))
        val time_string = date_format.format(Date())
        println("BahaBBS connection start:" + time_string)
    }

    // com.kota.Telnet.TelnetClientListener
    override fun onTelnetClientConnectionStart(aClient: TelnetClient?) {
        object : ASRunner() {
            // from class: com.kota.Bahamut.BahamutController.2
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                this@BahamutController.showConnectionStartMessage()
            }
        }.runInMainThread()
    }

    // com.kota.Telnet.TelnetClientListener
    override fun onTelnetClientConnectionSuccess(aClient: TelnetClient?) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // com.kota.Telnet.TelnetClientListener
    override fun onTelnetClientConnectionFail(aClient: TelnetClient?) {
        dismissProcessingDialog()
        showShortToast("連線失敗，請檢查網路連線或稍後再試")
    }

    // com.kota.Telnet.TelnetClientListener
    override fun onTelnetClientConnectionClosed(aClient: TelnetClient?) {
        val intent = Intent(this, BahaBBSBackgroundService::class.java)
        stopService(intent)
        object : ASRunner() {
            // from class: com.kota.Bahamut.BahamutController.3
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                val date_format = SimpleDateFormat("yyyy-MM-dd kk:hh:ss")
                date_format.setTimeZone(TimeZone.getTimeZone("GMT+8"))
                val time_string = date_format.format(Date())
                println("BahaBBS connection close:" + time_string)

                this@BahamutController.handleNormalConnectionClosed()

                showShortToast("連線已中斷")

                dismissProcessingDialog()

                if (getCloudSave()) {
                    val cloudBackup = CloudBackup()
                    cloudBackup.backup()
                }

                if (getMessageSmall() != null) {
                    getCurrentController().removeForeverView(getMessageSmall())
                    setMessageSmall(null)
                }
            }
        }.runInMainThread()
    }

    private fun handleNormalConnectionClosed() {
        val pages: Vector<ASViewController?> =
            ASNavigationController.getCurrentController().getViewControllers()
        val new_controllers = Vector<ASViewController?>()
        for (controller in pages) {
            val telnet_page = controller as TelnetPage
            if (telnet_page.pageType == 0 || telnet_page.isKeepOnOffline) {
                new_controllers.add(telnet_page)
            }
        }
        val start_page: StartPage? = PageContainer.Companion.getInstance().getStartPage()
        if (!new_controllers.contains(start_page)) {
            new_controllers.insertElementAt(start_page, 0)
        }
        setViewControllers(new_controllers)
    }

    // com.kota.ASFramework.PageController.ASNavigationController, android.app.Activity, android.content.ComponentCallbacks
    public override fun onLowMemory() {
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

    val isAnimationEnable: Boolean
        // com.kota.ASFramework.PageController.ASNavigationController
        get() = propertiesAnimationEnable

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
