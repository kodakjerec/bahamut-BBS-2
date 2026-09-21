package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.DialogHeroStep
import com.kota.Bahamut.pages.messages.MessageDatabase
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.HeroStep
import com.kota.Bahamut.service.NotificationSettings.getAlarmIgnoreBatteryOptimizations
import com.kota.Bahamut.service.NotificationSettings.setAlarmIgnoreBatteryOptimizations
import com.kota.Bahamut.service.NotificationSettings.setShowHeroStep
import com.kota.Bahamut.service.SyncManager
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.getHeroStepList
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.telnet.TelnetClient
import com.kota.telnet.model.TelnetFrame
import com.kota.telnetUI.TelnetComposePage
import com.kota.telnetUI.TelnetView

/**
 * 主選單頁面 (純 Jetpack Compose 實作)
 */
class MainPage : TelnetComposePage() {

    // 狀態變數 (供 Compose 即時重組)
    private var onlinePeopleText by mutableStateOf("")
    private var bbCallText by mutableStateOf("")
    private var isShowHeroStep by mutableStateOf(false)
    val heroStepItems = mutableStateListOf<HeroStep>()

    var onlinePeopleCount: String? = ""
    var bbCallStatus: String? = ""
    var telnetView: TelnetView? = null
    var telnetFrameBuffer: TelnetFrame? = null
    var goodbyeDialog: ASDialog? = null
    var saveHotMessageDialog: ASDialog? = null

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIN

    override fun onPageDidLoad() {
        BahamutStateHandler.getInstance().duringReadingArticle = false

        // 線上人數與呼叫器
        onlinePeopleText = onlinePeopleCount ?: ""
        bbCallText = bbCallStatus ?: ""

        // 載入勇者足跡
        loadHeroStepData()

        // 檢查電池最佳化
        checkBatteryLife()

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            object : ASCoroutine() {
                override suspend fun run() {
                    showProcessingDialog(getContextString(R.string.is_under_auto_logging_chat))
                    onBoardsClicked()
                }
            }.postDelayed(300L)
        }
    }

    override fun onPageDidAppear() {
        super.onPageDidAppear()
        loadHeroStepData()
    }

    override fun onPageRefresh() {
        setFrameToTelnetView()
        loadHeroStepData()
    }

    fun loadHeroStepData() {
        val heroStepList = getHeroStepList()
        if (heroStepList.isNotEmpty()) {
            if (heroStepItems.size != heroStepList.size || heroStepItems.isEmpty()) {
                heroStepItems.clear()
                heroStepItems.addAll(heroStepList)
            }
            // 只要有足跡資料就預設顯示（修復先前被誤設為 false 的狀態）
            isShowHeroStep = true
            setShowHeroStep(true)
        }
    }

    private fun setFrameToTelnetView() {
        val targetView = telnetView ?: return
        if (BahamutStateHandler.bahamutStateHandler?.currentPage == BahamutPage.BAHAMUT_MAIN) {
            val modelFrame = TelnetClient.model.frame
            if (modelFrame != null) {
                val buffer = modelFrame.clone()
                for (i in 12..23) {
                    buffer.removeRow(12)
                }
                buffer.removeRow(0)
                this.telnetFrameBuffer = buffer
                targetView.frame = buffer
            }
        } else if (this.telnetFrameBuffer != null) {
            targetView.frame = this.telnetFrameBuffer!!
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

    override fun onBackPressed(): Boolean {
        onLogoutClicked()
        return true
    }

    // -------------------------------------------------------------
    // 各項點擊事件
    // -------------------------------------------------------------

    fun onBoardsClicked() {
        PageContainer.instance?.pushClassPage("Boards", "佈告討論區")
        navigationController.pushViewController(PageContainer.instance!!.classPage)
        TelnetClient.myInstance?.sendStringToServerInBackground("b")
    }

    fun onClassClicked() {
        PageContainer.instance?.pushClassPage("Class", "分組討論區")
        navigationController.pushViewController(PageContainer.instance!!.classPage)
        TelnetClient.myInstance?.sendStringToServerInBackground("c")
    }

    fun onFavoriteClicked() {
        PageContainer.instance?.pushClassPage("Favorite", "我的最愛")
        navigationController.pushViewController(PageContainer.instance!!.classPage)
        TelnetClient.myInstance?.sendStringToServerInBackground("f")
    }

    fun onLogoutClicked() {
        TelnetClient.myInstance?.sendStringToServerInBackground("g")
    }

    fun onMailClicked() {
        navigationController.pushViewController(PageContainer.instance!!.mailBoxPage)
        TelnetClient.myInstance?.sendStringToServerInBackground("m\nr")
    }

    fun onSystemSettingsClicked() {
        navigationController.pushViewController(SystemSettingsPage())
    }

    fun onToggleHeroStep() {
        val heroStepList = getHeroStepList()
        if (heroStepList.isNotEmpty() && heroStepItems.isEmpty()) {
            heroStepItems.addAll(heroStepList)
        }
        val nextState = !isShowHeroStep
        isShowHeroStep = nextState
        setShowHeroStep(nextState)
    }

    fun onShowMessageMain() {
        val aPage = PageContainer.instance?.getMessageMain()
        if (aPage != null) {
            navigationController.pushViewController(aPage)
        }
        getMessageSmall()?.hide()
    }

    /** 給其他頁面呼叫訊息 */
    fun onProcessHotMessage() {
        if (this.saveHotMessageDialog == null) {
            this.saveHotMessageDialog = ASAlertDialog.createDialog()
                .setTitle("熱訊")
                .setMessage("本次上站熱訊處理 ")
                .addButton("備忘錄")
                .addButton("保留")
                .addButton("清除")
                .setListener { _, index: Int ->
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
            this.saveHotMessageDialog?.setOnDismissListener {
                if (this.saveHotMessageDialog != null) {
                    TelnetClient.myInstance?.sendStringToServerInBackground("K")
                }
            }
            this.saveHotMessageDialog?.show()
        }
    }

    /** 給其他頁面呼叫離開 */
    fun onCheckGoodbye() {
        if (this.goodbyeDialog == null) {
            this.goodbyeDialog = ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.logout))
                .setMessage("是否確定要登出?")
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.main_hero_step))
                .addButton(getContextString(R.string.confirm))
                .setListener { _, index: Int ->
                    this@MainPage.goodbyeDialog = null
                    when (index) {
                        2 -> {
                            SyncManager.uploadBeforeExit {
                                TelnetClient.myInstance?.sendStringToServerInBackground("G")
                                TempSettings.lastVisitArticleNumber = 0
                            }
                        }
                        1 -> {
                            TelnetClient.myInstance?.sendStringToServerInBackground("N")
                            DialogHeroStep().show()
                        }
                        0 -> {
                            TelnetClient.myInstance?.sendStringToServerInBackground("Q")
                        }
                    }
                }
                .scheduleDismissOnPageDisappear(this)
            this.goodbyeDialog?.setOnDismissListener {
                if (this.goodbyeDialog != null) {
                    SyncManager.uploadBeforeExit {
                        TelnetClient.myInstance?.sendStringToServerInBackground("G")
                    }
                }
            }
        }
        this.goodbyeDialog?.show()
    }

    override fun clear() {
        goodbyeDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        goodbyeDialog = null

        saveHotMessageDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        saveHotMessageDialog = null
    }

    fun setOnlinePeople(peoples: String?) {
        onlinePeopleCount = peoples
        onlinePeopleText = peoples ?: ""
    }

    fun setBBCall(status: String?) {
        bbCallStatus = status
        bbCallText = status ?: ""
    }

    @SuppressLint("BatteryLife")
    private fun checkBatteryLife() {
        if (!getAlarmIgnoreBatteryOptimizations()) {
            val powerManager = context?.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val packageName = context?.packageName

            if (powerManager?.isIgnoringBatteryOptimizations(packageName) == false) {
                ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.string._warning))
                    .setMessage(getContextString(R.string.ignoreBattery_msg01))
                    .addButton(getContextString(R.string.sure))
                    .show()
            }
            setAlarmIgnoreBatteryOptimizations(true)
        }
    }

    // -------------------------------------------------------------
    // Compose 畫面主體
    // -------------------------------------------------------------

    @Composable
    override fun ComposeContent() {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 1. 上半部 Telnet 終端 ASCII 歡迎畫面 (置頂，左右對齊視窗寬度)
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    TelnetView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        telnetView = this
                        setFrameToTelnetView()
                    }
                },
                update = { view ->
                    telnetView = view
                }
            )

            // 彈性留白 (讓下方功能區塊沉底對齊)
            Spacer(modifier = Modifier.weight(1f))

            // 2. 勇者足跡展開清單 (若有啟用且非空)
            if (isShowHeroStep && heroStepItems.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 148.dp)
                        .background(colors.pageBackground)
                ) {
                    items(heroStepItems) { heroStep ->
                        HeroStepRow(heroStep = heroStep)
                    }
                }
            }

            // 3. 狀態與功能主區塊
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.pageBackground)
            ) {
                // (1) 頂部三欄狀態資訊列 (線上人數 / 呼叫器 / 勇者足跡按鈕)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 線上人數
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BahaText(
                            text = stringResource(R.string.main_online_people),
                            fontSize = AppTheme.fontSize.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        BahaText(
                            text = onlinePeopleText.ifEmpty { "0" },
                            fontSize = AppTheme.fontSize.title,
                            color = colors.statusNotice
                        )
                    }

                    // 呼叫器
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .clickable { onShowMessageMain() }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            BahaText(
                                text = stringResource(R.string.main_bb_call),
                                fontSize = AppTheme.fontSize.caption,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Image(
                                painter = painterResource(id = R.drawable.icon_comment),
                                contentDescription = "BB Call",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        BahaText(
                            text = bbCallText.ifEmpty { "0" },
                            fontSize = AppTheme.fontSize.title,
                            color = colors.statusNotice
                        )
                    }

                    // 勇者足跡切換按鈕
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxSize()
                            .clickable { onToggleHeroStep() },
                        contentAlignment = Alignment.Center
                    ) {
                        BahaText(
                            text = stringResource(R.string.main_hero_step),
                            fontSize = AppTheme.fontSize.large,
                            color = colors.buttonText
                        )
                    }
                }

                // (2) 討論區選單列表
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                MainFolderItem(
                    title = stringResource(R.string.folder_boards),
                    onClick = { onBoardsClicked() }
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                MainFolderItem(
                    title = stringResource(R.string.folder_class),
                    onClick = { onClassClicked() }
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                MainFolderItem(
                    title = stringResource(R.string.folder_favorite),
                    onClick = { onFavoriteClicked() }
                )
            }

            // 4. 底部工具列 (登出 / 信箱 / 設定) (滿版無縫)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(colors.toolbarBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaButton(
                    text = stringResource(R.string.logout),
                    onClick = { onLogoutClicked() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.mailbox),
                    onClick = { onMailClicked() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.setting),
                    onClick = { onSystemSettingsClicked() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/**
 * 主頁目錄功能列項目
 */
@Composable
private fun MainFolderItem(
    title: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        BahaText(
            text = title,
            fontSize = AppTheme.fontSize.large,
            color = colors.buttonText,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 勇者足跡項目列 (經典 BBS 雙色標頭風格)
 */
@Composable
private fun HeroStepRow(
    heroStep: HeroStep
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.pageBackground)
    ) {
        // 標頭列：左側深藍底作者，右側灰銀底時間
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colors.titleBarBackground)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BahaText(
                    text = heroStep.authorNickname ?: stringResource(R.string.loading_),
                    color = colors.textPrimary,
                    fontSize = AppTheme.fontSize.body,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colors.chapterText)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                BahaText(
                    text = heroStep.datetime ?: "",
                    color = colors.titleBarBackground,
                    fontSize = AppTheme.fontSize.body,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
        // 留言內文 (黑底白字)
        BahaText(
            text = heroStep.content ?: stringResource(R.string.loading),
            color = colors.textPrimary,
            fontSize = AppTheme.fontSize.body,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )
        // 項目底部分隔線
        HorizontalDivider(color = colors.divider, thickness = 1.dp)
    }
}
