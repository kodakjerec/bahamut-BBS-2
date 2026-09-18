package com.kota.Bahamut.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getConnectIpAddress
import com.kota.Bahamut.service.NotificationSettings.getConnectMethod
import com.kota.Bahamut.service.NotificationSettings.getShowNotificationPermissionDialog
import com.kota.Bahamut.service.NotificationSettings.setConnectIpAddress
import com.kota.Bahamut.service.NotificationSettings.setConnectMethod
import com.kota.Bahamut.service.NotificationSettings.setShowNotificationPermissionDialog
import com.kota.Bahamut.service.SyncManager
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetComposePage

/**
 * 起始連線頁面 (純 Jetpack Compose 實作)
 */
class StartPage : TelnetComposePage() {

    override val pageType: Int
        get() = BahamutPage.START

    override fun onPageDidLoad() {
        navigationController.setNavigationTitle("勇者入口")
    }

    override fun onPageWillAppear() {
        val pageContainer = PageContainer.instance
        pageContainer?.cleanStartPage()
    }

    override fun onPageDidDisappear() {
        clear()
        super.onPageDidDisappear()
    }

    override fun clear() {
        ASProcessingDialog.dismissProcessingDialog()
        super.clear()
    }

    /** 按下離開 */
    fun onExitButtonClicked() {
        ASProcessingDialog.dismissProcessingDialog()
        SyncManager.uploadBeforeExit {
            navigationController.finish()
        }
    }

    /** 手機: 上一步 */
    override fun onBackPressed(): Boolean {
        onExitButtonClicked()
        return true
    }

    /** 按下連線按鈕 */
    fun onConnectButtonClicked() {
        // 顯示權限對話框
        if (getShowNotificationPermissionDialog()) {
            connect()
        } else {
            setShowNotificationPermissionDialog(true)
            checkAndRequestNotificationPermission()
        }
    }

    /** 連線 */
    fun connect() {
        val transportType = navigationController.deviceController?.isNetworkAvailable ?: -1
        TempSettings.transportType = transportType
        when {
            transportType > -1 -> {
                ASProcessingDialog.showProcessingDialog("連線中") {
                    TelnetClient.myInstance?.close()
                    false
                }
                val connectIpAddress = getConnectIpAddress()
                ASCoroutine.runInNewCoroutine {
                    TelnetClient.myInstance?.connect(connectIpAddress, 23)
                }
            }
            else -> ASToast.showShortToast("您未連接網路")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context?.startActivity(intent)
    }

    @Composable
    override fun ComposeContent() {
        val colors = AppTheme.colors
        val currentContext = LocalContext.current

        // 版本號碼
        val versionText = remember {
            try {
                val packageInfo: PackageInfo =
                    currentContext.packageManager.getPackageInfo(currentContext.packageName, 0)
                val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
                val versionName = packageInfo.versionName
                "$versionCode - $versionName"
            } catch (_: Exception) {
                ""
            }
        }

        // 連線方式狀態 (Telnet vs WebSocket)
        val methodTelnet = stringResource(R.string.start_connect_method1)
        val methodWebSocket = stringResource(R.string.start_connect_method2)
        var selectedMethod by remember {
            val saved = getConnectMethod()
            mutableStateOf(if (saved == methodTelnet) methodTelnet else methodWebSocket)
        }

        // 連線位址狀態 (bbs.gamer.com.tw vs 114.32.114.150)
        val ip1 = stringResource(R.string.start_connect_ip1)
        val ip2 = stringResource(R.string.start_connect_ip2)
        var selectedIp by remember {
            val saved = getConnectIpAddress()
            mutableStateOf(if (saved == ip1) ip1 else ip2)
        }

        // WebSocket 模式下禁用 IP 選擇
        val isWebSocket = (selectedMethod == methodWebSocket)
        val isIpEnabled = !isWebSocket

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 可捲動內容區塊
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. 公告標題列與版本號
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.notices),
                        fontSize = BahaTextSize.TITLE
                    )
                    BahaText(
                        text = versionText,
                        fontSize = BahaTextSize.CAPTION,
                        color = colors.textSecondary
                    )
                }

                // 2. 公告說明 1
                BahaText(
                    text = stringResource(R.string.start_msg_1).trim(),
                    fontSize = BahaTextSize.CAPTION,
                    color = colors.textSecondary,
                    lineHeight = 20.sp
                )

                // 3. 公告說明 2
                BahaText(
                    text = stringResource(R.string.start_msg_2),
                    fontSize = BahaTextSize.BODY,
                    lineHeight = 22.sp
                )

                // 4. 開啟帳號連結 (啟用 BBS 權限)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BahaText(
                        text = "第一次登入請先至",
                        fontSize = BahaTextSize.CAPTION,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    BahaText(
                        text = "https://user.gamer.com.tw/openBBS.php",
                        fontSize = BahaTextSize.CAPTION,
                        color = colors.textLink,
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                openUrl("https://user.gamer.com.tw/openBBS.php")
                            }
                            .padding(vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    BahaText(
                        text = "啟用BBS權限 ！",
                        fontSize = BahaTextSize.CAPTION,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                // 5. 連線位址區塊
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isIpEnabled) 1.0f else 0.45f)
                ) {
                    BahaText(
                        text = stringResource(R.string.start_connect_ip),
                        fontSize = BahaTextSize.CAPTION,
                        color = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                    ) {
                        listOf(ip1, ip2).forEach { ipText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (ipText == selectedIp),
                                        enabled = isIpEnabled,
                                        role = Role.RadioButton,
                                        onClick = {
                                            if (isIpEnabled) {
                                                selectedIp = ipText
                                                setConnectIpAddress(ipText)
                                            }
                                        }
                                    )
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .height(48.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (ipText == selectedIp),
                                    onClick = null,
                                    enabled = isIpEnabled,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.checkboxTint,
                                        unselectedColor = colors.checkboxUncheckedTint
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BahaText(
                                    text = ipText,
                                    fontSize = BahaTextSize.BASE,
                                    color = if (isIpEnabled) colors.textPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // 6. 連線方式區塊 (Telnet vs WebSocket)
                Column(modifier = Modifier.fillMaxWidth()) {
                    BahaText(
                        text = stringResource(R.string.start_connect_method),
                        fontSize = BahaTextSize.CAPTION,
                        color = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                    ) {
                        listOf(methodTelnet, methodWebSocket).forEach { methodText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (methodText == selectedMethod),
                                        role = Role.RadioButton,
                                        onClick = {
                                            selectedMethod = methodText
                                            setConnectMethod(methodText)
                                        }
                                    )
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .height(48.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (methodText == selectedMethod),
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.checkboxTint,
                                        unselectedColor = colors.checkboxUncheckedTint
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BahaText(
                                    text = methodText,
                                    fontSize = BahaTextSize.BASE
                                )
                            }
                        }
                    }
                }

                // 7. 避難所標題與圖示按鈕
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    BahaText(
                        text = stringResource(R.string.start_vault),
                        fontSize = BahaTextSize.CAPTION,
                        color = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val shelterItems = remember {
                        listOf(
                            Triple(R.drawable.icon_discord, "Discord", "https://discord.gg/YP8dthZ"),
                            Triple(R.drawable.icon_facebook, "Facebook", "https://www.facebook.com/groups/264144897071532"),
                            Triple(R.drawable.icon_reddit, "Reddit", "https://www.reddit.com/r/bahachat"),
                            Triple(R.drawable.icon_steam, "Steam", "https://steamcommunity.com/groups/BAHACHAT"),
                            Triple(R.drawable.icon_telegram, "Telegram", "https://t.me/joinchat/MF5hqkuZN3B0NFqSyiz30A")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        shelterItems.forEach { (iconRes, name, url) ->
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = name,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { openUrl(url) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 8. 底部操作工具列 (滿版無縫，高度 50dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.toolbarBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaButton(
                    text = stringResource(R.string.exit),
                    onClick = { onExitButtonClicked() },
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
                    text = stringResource(R.string.start_page_instructions),
                    onClick = { openUrl("https://kodaks-organization-1.gitbook.io/bahabbs-zhan-ba-ha-shi-yong-shou-ce/") },
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
                    text = stringResource(R.string.connect),
                    onClick = { onConnectButtonClicked() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }

    companion object {
        /**
         * 檢查並要求通知權限 (Android 13+)
         */
        fun checkAndRequestNotificationPermission() {
            val controller = ASNavigationController.currentController ?: return

            // 只在 Android 13+ 需要通知權限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (controller.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.notification_permission_title))
                        .setMessage(getContextString(R.string.notification_permission_message))
                        .addButton(getContextString(R.string.notification_permission_later))
                        .addButton(getContextString(R.string.notification_permission_goto_settings))
                        .setDefaultButtonIndex(0)
                        .setListener { _, index ->
                            if (index == 1) {
                                try {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, controller.packageName)
                                    }
                                    controller.startActivity(intent)
                                } catch (_: Exception) {
                                    try {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = ("package:" + controller.packageName).toUri()
                                        }
                                        controller.startActivity(intent)
                                    } catch (_: Exception) {
                                        ASToast.showShortToast("無法開啟設定頁面")
                                    }
                                }
                            }
                        }
                        .show()
                }
            }
        }
    }
}
