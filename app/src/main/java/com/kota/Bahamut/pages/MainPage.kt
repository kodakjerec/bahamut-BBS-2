package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.PowerManager
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.DialogHeroStep
import com.kota.Bahamut.pages.messages.MessageDatabase
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.HeroStep
import com.kota.Bahamut.service.NotificationSettings.getAlarmIgnoreBatteryOptimizations
import com.kota.Bahamut.service.NotificationSettings.getShowHeroStep
import com.kota.Bahamut.service.NotificationSettings.setAlarmIgnoreBatteryOptimizations
import com.kota.Bahamut.service.NotificationSettings.setShowHeroStep
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.getHeroStepList
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.telnet.TelnetClient
import com.kota.telnet.model.TelnetFrame
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView

class MainPage : TelnetPage() {
    var onlinePeopleCount: String? = "" // 線上人數
    var bbCallStatus: String? = "" // 呼叫器
    var mainLayout: RelativeLayout? = null
    var boardsListener: View.OnClickListener = View.OnClickListener { v: View? ->
        PageContainer.instance?.pushClassPage("Boards", "佈告討論區")
        this@MainPage.navigationController.pushViewController(
            PageContainer.instance?.classPage
        )
        TelnetClient.myInstance?.sendStringToServerInBackground("b")
    }
    var classListener: View.OnClickListener = View.OnClickListener { v: View? ->
        PageContainer.instance?.pushClassPage("Class", "分組討論區")
        this@MainPage.navigationController.pushViewController(
            PageContainer.instance?.classPage
        )
        TelnetClient.myInstance?.sendStringToServerInBackground("c")
    }
    var favoriteListener: View.OnClickListener = View.OnClickListener { v: View? ->
        PageContainer.instance?.pushClassPage("Favorite", "我的最愛")
        this@MainPage.navigationController.pushViewController(
            PageContainer.instance?.classPage
        )
        TelnetClient.myInstance?.sendStringToServerInBackground("f")
    }
    var telnetFrameBuffer: TelnetFrame? = null
    var goodbyeDialog: ASDialog? = null
    var logoutListener: View.OnClickListener = View.OnClickListener { v: View? ->
        TelnetClient.myInstance?.sendStringToServerInBackground("g")
    }
    var mailListener: View.OnClickListener = View.OnClickListener { v: View? ->
        this@MainPage.navigationController.pushViewController(
            PageContainer.instance?.mailBoxPage
        )
        TelnetClient.myInstance?.sendStringToServerInBackground("m\nr")
    }
    var saveHotMessageDialog: ASDialog? = null
    var systemSettingListener: View.OnClickListener = View.OnClickListener { v: View? ->
        this@MainPage.navigationController.pushViewController(SystemSettingsPage())
    }

    /** 顯示勇者足跡  */
    var showHeroStepListener: View.OnClickListener = View.OnClickListener { v: View? ->
        // 切換顯示
        var isShowHeroStep = getShowHeroStep()
        isShowHeroStep = !isShowHeroStep
        setShowHeroStep(isShowHeroStep)
        mainLayout?.findViewById<View>(R.id.Main_Block_HeroStepList)!!.visibility = if (isShowHeroStep) View.VISIBLE else View.GONE
    }

    /** 顯示聊天main  */
    var showMessageMainListener: View.OnClickListener = View.OnClickListener { v: View? ->
        val aPage = PageContainer.instance?.getMessageMain()
        this@MainPage.navigationController.pushViewController(aPage)

        // 隱藏小視窗
        if (getMessageSmall() != null) getMessageSmall()?.hide()
    }

    override val pageLayout: Int
        get() = R.layout.main_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIN

    override fun onPageDidLoad() {
        // 重置設定
        BahamutStateHandler.getInstance().duringReadingArticle = false

        mainLayout = findViewById(R.id.content_view) as RelativeLayout?
        mainLayout?.findViewById<View>(R.id.Main_BoardsButton)!!
            .setOnClickListener(this.boardsListener)
        mainLayout?.findViewById<View>(R.id.Main_ClassButton)!!
            .setOnClickListener(this.classListener)
        mainLayout?.findViewById<View>(R.id.Main_FavoriteButton)!!
            .setOnClickListener(this.favoriteListener)
        mainLayout?.findViewById<View>(R.id.Main_LogoutButton)!!
            .setOnClickListener(this.logoutListener)
        mainLayout?.findViewById<View>(R.id.Main_MailButton)!!.setOnClickListener(this.mailListener)
        mainLayout?.findViewById<View>(R.id.Main_SystemSettingsButton)!!
            .setOnClickListener(this.systemSettingListener)
        val mainOnlinePeople = mainLayout?.findViewById<TextView>(R.id.Main_OnlinePeople)!!
        mainOnlinePeople.text = onlinePeopleCount // 線上人數

        // 顯示勇者足跡
        val heroStepList: MutableList<HeroStep> = getHeroStepList()
        val heroStepListLayout = mainLayout?.findViewById<LinearLayout>(R.id.Main_HeroStepList)!!
        for (heroStep in heroStepList) {
            val itemView = HeroStepItemView(context)
            itemView.setItem(heroStep)
            heroStepListLayout.addView(itemView)
        }
        // 沒資料不顯示
        if (heroStepList.isEmpty()) {
            setShowHeroStep(false)
        }
        val isShowHeroStep = getShowHeroStep()
        mainLayout?.findViewById<View>(R.id.Main_Block_HeroStepList)!!.visibility = if (isShowHeroStep) View.VISIBLE else View.GONE
        mainLayout?.findViewById<View>(R.id.Main_HeroStepButton)!!
            .setOnClickListener(this.showHeroStepListener)

        // 顯示呼叫器
        val txtBBCall = mainLayout?.findViewById<TextView>(R.id.Main_BBCall)!!
        txtBBCall.text = bbCallStatus
        mainLayout?.findViewById<View>(R.id.Main_BBCall_Layout)!!
            .setOnClickListener(showMessageMainListener)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)

        // 檢查不斷線掛網
        checkBatteryLife()

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            object : ASRunner() {
                override fun run() {
                    showProcessingDialog(getContextString(R.string.is_under_auto_logging_chat))
                    // 進入布告討論區
                    mainLayout?.findViewById<View>(R.id.Main_BoardsButton)!!.performClick()
                }
            }.postDelayed(300)
        }
    }

    override fun onPageRefresh() {
        setFrameToTelnetView()
    }

    private fun setFrameToTelnetView() {
        val telnetView = mainLayout?.findViewById<TelnetView?>(R.id.Main_TelnetView)
        if (telnetView != null) {
            if (BahamutStateHandler.bahamutStateHandler?.currentPage == BahamutPage.BAHAMUT_MAIN) {
                this.telnetFrameBuffer = TelnetClient.model.frame?.clone()
                for (i in 12..23) {
                    this.telnetFrameBuffer?.removeRow(12)
                }
                this.telnetFrameBuffer?.removeRow(0)
            }
            if (this.telnetFrameBuffer != null) {
                telnetView.frame = this.telnetFrameBuffer!!
            }
        }
    }

    override fun onPageWillDisappear() {
        clear()
    }

    override fun onPageDidDisappear() {
        this.goodbyeDialog = null
        this.saveHotMessageDialog = null
        super.onPageDidDisappear()
    }

    /* access modifiers changed from: protected */
    override fun onBackPressed(): Boolean {
        this.logoutListener.onClick(null)
        return true
    }

    /** 給其他頁面呼叫訊息  */
    fun onProcessHotMessage() {
        if (this.saveHotMessageDialog == null) {
            this.saveHotMessageDialog = ASAlertDialog.createDialog()
                .setTitle("熱訊")
                .setMessage("本次上站熱訊處理 ")
                .addButton("備忘錄")
                .addButton("保留")
                .addButton("清除")
                .setListener { aDialog: ASAlertDialog?, index: Int ->
                    this@MainPage.saveHotMessageDialog = null
                    when (index) {
                        0 -> TelnetClient.myInstance?.sendStringToServerInBackground("M")
                        1 -> TelnetClient.myInstance?.sendStringToServerInBackground("K")
                        2 -> {
                            TelnetClient.myInstance?.sendStringToServerInBackground("C")
                            MessageDatabase(context).use { db ->
                                db.clearDb()
                            }
                        }

                        else -> TelnetClient.myInstance?.sendStringToServerInBackground("K")
                    }
                }
            this.saveHotMessageDialog?.setOnDismissListener { dialog: DialogInterface? ->
                // 預設離開
                if (this.saveHotMessageDialog != null) {
                    TelnetClient.myInstance?.sendStringToServerInBackground("K")
                }
            }
            this.saveHotMessageDialog?.show()
        }
    }

    /** 給其他頁面呼叫離開  */
    fun onCheckGoodbye() {
        if (this.goodbyeDialog == null) {
            this.goodbyeDialog = ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.logout))
                .setMessage("是否確定要登出?")
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.main_hero_step))
                .addButton(getContextString(R.string.confirm))
                .setListener { aDialog: ASAlertDialog?, index: Int ->
                    this@MainPage.goodbyeDialog = null
                    when (index) {
                        2 ->  // 確定
                            TelnetClient.myInstance?.sendStringToServerInBackground("G")

                        1 -> { // 勇者足跡
                            TelnetClient.myInstance?.sendStringToServerInBackground("N")
                            val dialogHeroStep = DialogHeroStep()
                            dialogHeroStep.show()
                        }

                        0 ->  // 取消
                            TelnetClient.myInstance?.sendStringToServerInBackground("Q")

                        else -> {}
                    }
                }
                .scheduleDismissOnPageDisappear(this)
            this.goodbyeDialog?.setOnDismissListener { dialog: DialogInterface? ->
                // 預設離開
                if (this.goodbyeDialog != null) {
                    TelnetClient.myInstance?.sendStringToServerInBackground("G")
                }
            }
        }
        this.goodbyeDialog?.show()
    }

    override fun clear() {
        if (this.goodbyeDialog != null) {
            if (this.goodbyeDialog?.isShowing!!) {
                this.goodbyeDialog?.dismiss()
            }
            this.goodbyeDialog = null
        }
        if (this.saveHotMessageDialog != null) {
            if (this.saveHotMessageDialog?.isShowing!!) {
                this.saveHotMessageDialog?.dismiss()
            }
            this.saveHotMessageDialog = null
        }
    }

    /** 設定線上人數  */
    fun setOnlinePeople(peoples: String?) {
        onlinePeopleCount = peoples
    }

    /** 設定呼叫器  */
    fun setBBCall(status: String?) {
        bbCallStatus = status
    }

    /** 不受電池最佳化限制  */
    @SuppressLint("BatteryLife")
    private fun checkBatteryLife() {
        if (!getAlarmIgnoreBatteryOptimizations()) {
            val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName: String? = context?.packageName

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.string._warning))
                    .setMessage(getContextString(R.string.ignoreBattery_msg01))
                    .addButton(getContextString(R.string.sure))
                    .show()
            }
            setAlarmIgnoreBatteryOptimizations(true)
        }
    }
}
