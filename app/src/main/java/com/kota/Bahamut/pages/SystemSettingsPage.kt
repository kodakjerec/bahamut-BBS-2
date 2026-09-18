package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kota.Bahamut.ui.components.BahaDropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.blockListPage.ArticleExpressionListPage
import com.kota.Bahamut.pages.blockListPage.ArticleHeaderListPage
import com.kota.Bahamut.pages.blockListPage.BlockListPage
import com.kota.Bahamut.service.CloudBackup
import com.kota.Bahamut.service.CommonFunctions.changeScreenOrientation
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.Bahamut.service.NotificationSettings.getCloudSaveLastTime
import com.kota.Bahamut.service.NotificationSettings.setCloudSave
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.linkAutoShow
import com.kota.Bahamut.service.UserSettings.Companion.linkShowOnlyWifi
import com.kota.Bahamut.service.UserSettings.Companion.linkShowThumbnail
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAnimationEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesArticleMoveEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAutoToChat
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListForTitle
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBoardMoveEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesDrawerLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesFollowSystemDarkMode
import com.kota.Bahamut.service.UserSettings.Companion.propertiesGestureOnBoardEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesScreenOrientation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarOrder
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.setPropertiesArticleMoveDisable
import com.kota.Bahamut.service.UserSettings.Companion.setPropertiesBoardMoveDisable
import com.kota.Bahamut.service.UserSettings.Companion.setPropertiesLinkAutoShow
import com.kota.Bahamut.service.UserSettings.Companion.toolbarAlpha
import com.kota.Bahamut.service.UserSettings.Companion.toolbarIdle
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.RightArrow
import com.kota.Bahamut.ui.components.SettingsCheckboxItem
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemSettingsPage : TelnetPage() {

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_SYSTEM_SETTINGS

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    // State bindings
    var followSystemDarkModeState by mutableStateOf(propertiesFollowSystemDarkMode)
    var blockListEnableState by mutableStateOf(propertiesBlockListEnable)
    var blockListForTitleState by mutableStateOf(propertiesBlockListForTitle)
    var enableBoardMoveState by mutableStateOf(propertiesBoardMoveEnable > 0)
    var enableArticleMoveState by mutableStateOf(propertiesArticleMoveEnable)
    var enableGestureOnBoardState by mutableStateOf(propertiesGestureOnBoardEnable)
    var toolbarLocationState by mutableIntStateOf(propertiesToolbarLocation)
    var toolbarOrderState by mutableIntStateOf(propertiesToolbarOrder)
    var toolbarIdleState by mutableFloatStateOf(toolbarIdle)
    var toolbarAlphaState by mutableFloatStateOf(toolbarAlpha)
    var drawerLocationState by mutableIntStateOf(propertiesDrawerLocation)
    var linkAutoShowState by mutableStateOf(linkAutoShow)
    var linkShowThumbnailState by mutableStateOf(linkShowThumbnail)
    var linkShowOnlyWifiState by mutableStateOf(linkShowOnlyWifi)
    var autoToChatState by mutableStateOf(propertiesAutoToChat)
    var animationEnableState by mutableStateOf(propertiesAnimationEnable)
    var screenOrientationState by mutableIntStateOf(propertiesScreenOrientation)
    var cloudSaveEnableState by mutableStateOf(getCloudSave())
    var cloudSaveLastTimeString by mutableStateOf("")

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                SystemSettingsPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageDidLoad() {
        super.onPageDidLoad()
        var lastTime = TempSettings.cloudSaveLastTime
        if (lastTime <= 0L) {
            lastTime = getCloudSaveLastTime()
            TempSettings.cloudSaveLastTime = lastTime
        }
        if (lastTime > 0L) {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            cloudSaveLastTimeString = sdf.format(Date(lastTime))
        }
    }

    override fun onBackPressed(): Boolean {
        notifyDataUpdated()
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    private fun onFollowSystemDarkModeChanged(isChecked: Boolean) {
        if (propertiesFollowSystemDarkMode != isChecked) {
            val dialog = ASAlertDialog("DARK_MODE_CHANGE_CONFIRM")
            dialog.setTitle("更換深色模式")
                .setMessage("更換深色模式設定將會中斷目前的連線並重新啟動應用程式，是否確定更換?")
                .addButton("取消")
                .addButton("確定")
                .setDefaultButtonIndex(0)
                .setListener(object : ASAlertDialogListener {
                    override fun onAlertDialogDismissWithButtonIndex(
                        paramASAlertDialog: ASAlertDialog,
                        paramInt: Int
                    ) {
                        if (paramInt == 1) {
                            propertiesFollowSystemDarkMode = isChecked
                            followSystemDarkModeState = isChecked
                            notifyDataUpdated()
                            val nightMode = if (isChecked) {
                                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            } else {
                                AppCompatDelegate.MODE_NIGHT_NO
                            }
                            AppCompatDelegate.setDefaultNightMode(nightMode)

                            android.widget.Toast.makeText(
                                context,
                                getContextString(R.string.theme_manager_page_msg01),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            TelnetClient.myInstance?.close()
                            TempSettings.lastVisitArticleNumber = 0
                            context?.recreate()
                        } else {
                            followSystemDarkModeState = propertiesFollowSystemDarkMode
                        }
                    }
                }).show()
        }
    }

    private fun onCloudSaveChanged(isChecked: Boolean) {
        setCloudSave(isChecked)
        cloudSaveEnableState = isChecked
        if (getCloudSave()) {
            val cloudBackup = CloudBackup()
            cloudBackup.setListener {
                showProcessingDialog("設定套用中\n請重新進入設定")
                onBackPressed()
                object : ASCoroutine() {
                    override suspend fun run() {
                        dismissProcessingDialog()
                    }
                }.postDelayed(1500L)
            }
            cloudBackup.askCloudSave()
        }
    }

    @SuppressLint("BatteryLife")
    private fun onIgnoreBatteryOptimizations() {
        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val packageName: String? = context?.packageName
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (powerManager?.isIgnoringBatteryOptimizations(packageName) == true) {
            showLongToast(getContextString(R.string.ignoreBattery_msg02))
        } else {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = ("package:$packageName").toUri()
            startActivity(intent)
        }
    }

    @Composable
    fun SystemSettingsPageContent() {
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        val screenOrientationItems = stringArrayResource(R.array.system_setting_page_screen_orientation_items)
        val toolbarLocationItems = stringArrayResource(R.array.system_setting_page_toolbar_location_items)
        val toolbarOrderItems = stringArrayResource(R.array.system_setting_page_toolbar_order_items)
        val drawerLocationItems = stringArrayResource(R.array.system_setting_page_drawer_location_items)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // BBS 章節
                SettingsChapter(stringResource(R.string.system_setting_page_chapter_bbs))
                SettingsNavigationItem(stringResource(R.string.user_info)) {
                    TelnetClient.myInstance?.sendStringToServerInBackground("u\ni")
                    navigationController.pushViewController(PageContainer.instance!!.getUserInfoPage())
                }
                SettingsNavigationItem(stringResource(R.string.user_config)) {
                    TelnetClient.myInstance?.sendStringToServerInBackground("u\nc")
                    navigationController.pushViewController(PageContainer.instance!!.getUserConfigPage())
                }

                // 主題
                SettingsChapter(stringResource(R.string.theme))
                SettingsNavigationItem(stringResource(R.string.theme_manager_page)) {
                    navigationController.pushViewController(PageContainer.instance!!.getThemeManagerPage())
                }
                SettingsCheckboxItem(
                    title = stringResource(R.string.follow_system_dark_mode),
                    isChecked = followSystemDarkModeState,
                    onCheckedChange = { onFollowSystemDarkModeChanged(it) }
                )

                // 黑名單
                SettingsChapter(stringResource(R.string.system_setting_page_chapter_blocklist))
                SettingsCheckboxItem(
                    title = stringResource(R.string.switch_on_block_list),
                    isChecked = blockListEnableState,
                    onCheckedChange = {
                        blockListEnableState = it
                        propertiesBlockListEnable = it
                    }
                )
                SettingsCheckboxItem(
                    title = stringResource(R.string.switch_on_block_list_for_title),
                    isChecked = blockListForTitleState,
                    onCheckedChange = {
                        blockListForTitleState = it
                        propertiesBlockListForTitle = it
                    }
                )
                SettingsNavigationItem(stringResource(R.string.block_list_setting)) {
                    navigationController.pushViewController(BlockListPage())
                }

                // 看板與文章
                SettingsChapter(stringResource(R.string.system_setting_page_chapter_board_article))
                SettingsCheckboxItem(
                    title = stringResource(R.string.system_setting_page_enable_board_move),
                    isChecked = enableBoardMoveState,
                    onCheckedChange = {
                        enableBoardMoveState = it
                        setPropertiesBoardMoveDisable(if (it) 1 else 0)
                    }
                )
                SettingsCheckboxItem(
                    title = stringResource(R.string.system_setting_page_enable_article_move),
                    isChecked = enableArticleMoveState,
                    onCheckedChange = {
                        enableArticleMoveState = it
                        setPropertiesArticleMoveDisable(it)
                    }
                )

                // VIP 功能
                if (propertiesVIP) {
                    SettingsCheckboxItem(
                        title = stringResource(R.string.system_setting_page_enable_gesture_on_board),
                        isChecked = enableGestureOnBoardState,
                        onCheckedChange = {
                            enableGestureOnBoardState = it
                            propertiesGestureOnBoardEnable = it
                        }
                    )

                    SettingsSpinnerItem(
                        title = stringResource(R.string.system_setting_page_toolbar_location),
                        items = toolbarLocationItems,
                        selectedIndex = toolbarLocationState,
                        onItemSelected = { idx ->
                            toolbarLocationState = idx
                            propertiesToolbarLocation = idx
                        }
                    )

                    if (toolbarLocationState <= 2) {
                        SettingsSpinnerItem(
                            title = stringResource(R.string.system_setting_page_toolbar_order),
                            items = toolbarOrderItems,
                            selectedIndex = toolbarOrderState,
                            onItemSelected = { idx ->
                                toolbarOrderState = idx
                                propertiesToolbarOrder = idx
                            }
                        )
                    } else {
                        // 浮動工具列設定
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BahaText(
                                    text = stringResource(R.string.system_setting_page_toolbar_idle),
                                    modifier = Modifier.weight(1f)
                                )
                                BahaText(
                                    text = "${toolbarIdleState}s",
                                    color = colors.textSecondary,
                                    fontSize = BahaTextSize.BODY
                                )
                            }
                            Slider(
                                value = toolbarIdleState,
                                onValueChange = {
                                    toolbarIdleState = it
                                    toolbarIdle = it
                                },
                                valueRange = 0.0f..4.0f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.toolbarBackground,
                                    activeTrackColor = colors.toolbarBackground
                                )
                            )
                        }
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BahaText(
                                    text = stringResource(R.string.system_setting_page_toolbar_alpha),
                                    modifier = Modifier.weight(1f)
                                )
                                BahaText(
                                    text = "${toolbarAlphaState.toInt()}%",
                                    color = colors.textSecondary,
                                    fontSize = BahaTextSize.BODY
                                )
                            }
                            Slider(
                                value = toolbarAlphaState,
                                onValueChange = {
                                    toolbarAlphaState = it
                                    toolbarAlpha = it
                                },
                                valueRange = 20f..100f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.toolbarBackground,
                                    activeTrackColor = colors.toolbarBackground
                                )
                            )
                        }
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                    }

                    SettingsSpinnerItem(
                        title = stringResource(R.string.system_setting_page_drawer_location),
                        items = drawerLocationItems,
                        selectedIndex = drawerLocationState,
                        onItemSelected = { idx ->
                            drawerLocationState = idx
                            propertiesDrawerLocation = idx
                            changeScreenOrientation()
                        }
                    )

                    SettingsNavigationItem(stringResource(R.string.article_header_setting)) {
                        navigationController.pushViewController(ArticleHeaderListPage())
                    }
                    SettingsNavigationItem(stringResource(R.string.article_expression_setting)) {
                        navigationController.pushViewController(ArticleExpressionListPage())
                    }
                }

                // 連結預覽
                SettingsChapter(stringResource(R.string.system_setting_page_chapter_link))
                SettingsCheckboxItem(
                    title = stringResource(R.string.system_setting_page_chapter_link_auto_show),
                    isChecked = linkAutoShowState,
                    onCheckedChange = {
                        linkAutoShowState = it
                        setPropertiesLinkAutoShow(it)
                        if (!it) {
                            linkShowThumbnailState = false
                            linkShowThumbnail = false
                            linkShowOnlyWifiState = false
                            linkShowOnlyWifi = false
                        }
                    }
                )
                if (linkAutoShowState) {
                    SettingsCheckboxItem(
                        title = stringResource(R.string.system_setting_page_chapter_link_show_thumbnail),
                        isChecked = linkShowThumbnailState,
                        onCheckedChange = {
                            linkShowThumbnailState = it
                            linkShowThumbnail = it
                            if (!it) {
                                linkShowOnlyWifiState = false
                                linkShowOnlyWifi = false
                            }
                        }
                    )
                    if (linkShowThumbnailState) {
                        SettingsCheckboxItem(
                            title = stringResource(R.string.system_setting_page_chapter_link_show_only_wifi),
                            isChecked = linkShowOnlyWifiState,
                            onCheckedChange = {
                                linkShowOnlyWifiState = it
                                linkShowOnlyWifi = it
                            }
                        )
                    }
                }

                // 偏好設定
                SettingsChapter(stringResource(R.string.system_setting_page_chapter_preference))
                if (propertiesVIP) {
                    SettingsCheckboxItem(
                        title = stringResource(R.string.system_setting_page_enable_auto_to_chat),
                        isChecked = autoToChatState,
                        onCheckedChange = {
                            autoToChatState = it
                            propertiesAutoToChat = it
                        }
                    )
                }
                SettingsCheckboxItem(
                    title = stringResource(R.string.system_setting_page_enable_page_animation),
                    isChecked = animationEnableState,
                    onCheckedChange = {
                        animationEnableState = it
                        propertiesAnimationEnable = it
                        ASNavigationController.currentController?.isAnimationEnable = it
                    }
                )
                SettingsSpinnerItem(
                    title = stringResource(R.string.system_setting_page_screen_orientation),
                    items = screenOrientationItems,
                    selectedIndex = screenOrientationState,
                    onItemSelected = { idx ->
                        screenOrientationState = idx
                        propertiesScreenOrientation = idx
                        changeScreenOrientation()
                    }
                )
                SettingsNavigationItem(stringResource(R.string.system_setting_page_ignore_battery_optimizations)) {
                    onIgnoreBatteryOptimizations()
                }

                // 雲端備份 (VIP)
                if (propertiesVIP) {
                    SettingsChapter(stringResource(R.string.cloud_save))
                    SettingsCheckboxItem(
                        title = stringResource(R.string.cloud_save_setting_switch),
                        isChecked = cloudSaveEnableState,
                        onCheckedChange = { onCloudSaveChanged(it) }
                    )
                    if (cloudSaveLastTimeString.isNotEmpty()) {
                        BahaText(
                            text = cloudSaveLastTimeString,
                            color = colors.textSecondary,
                            fontSize = BahaTextSize.CAPTION,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }

                // 贊助章節
                SettingsChapter(stringResource(R.string.system_setting_page_chapter_donation))
                SettingsNavigationItem(stringResource(R.string.system_setting_page_goBillingPage)) {
                    navigationController.pushViewController(PageContainer.instance!!.billingPage)
                }
            }

            // 底部返回工具列 (滿版無縫)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string._back),
                onClick = { onBackPressed() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }

    @Composable
    private fun SettingsChapter(title: String) {
        val colors = AppTheme.colors
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.chapterBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            BahaText(
                text = title,
                color = colors.chapterText
            )
        }
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }

    @Composable
    private fun SettingsNavigationItem(title: String, onClick: () -> Unit) {
        val colors = AppTheme.colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BahaText(
                text = title,
                modifier = Modifier.weight(1f)
                .padding(horizontal = 12.dp)
            )
            RightArrow { }
        }
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }

    @Composable
    private fun SettingsSpinnerItem(
        title: String,
        items: Array<String>,
        selectedIndex: Int,
        onItemSelected: (Int) -> Unit
    ) {
        val colors = AppTheme.colors
        var expanded by remember { mutableStateOf(false) }
        val currentText = items.getOrNull(selectedIndex) ?: ""

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BahaText(
                text = title,
                modifier = Modifier.weight(1f)
            )
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    BahaText(
                        text = currentText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BahaText(
                        text = "▾",
                        color = colors.textSecondary
                    )
                }
                BahaDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    items = items,
                    selectedIndex = selectedIndex,
                    onItemSelected = onItemSelected
                )
            }
        }

        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }
}
