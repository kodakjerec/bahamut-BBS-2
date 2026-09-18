package com.kota.Bahamut.pages.theme

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaTopAppBar
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.ThemeHybridShowcase
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetComposePage

/**
 * 外觀管理頁面 (純 Jetpack Compose 實作)
 */
class ThemeManagerPage : TelnetComposePage() {
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_THEME_MANAGER_PAGE

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    @Composable
    override fun ComposeContent() {
        ThemeManagerScreen(
            onBackClick = { onBackPressed() }
        )
    }

    override fun onPageWillAppear() {
        super.onPageWillAppear()
        navigationController.setNavigationTitle(getContextString(R.string.theme_manager_page))
    }

    override fun onBackPressed(): Boolean {
        PageContainer.instance!!.cleanThemeManagerPage()
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        return true
    }
}

/**
 * 外觀管理 Compose 畫面主體
 */
@Composable
fun ThemeManagerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val themes = remember { ThemeStore.getThemeStore() }
    var selectedIndex by remember { mutableIntStateOf(ThemeStore.getSelectIndex()) }
    var pendingThemeIndex by remember { mutableStateOf<Int?>(null) }

    // 更換外觀確認對話框
    if (pendingThemeIndex != null) {
        val targetIndex = pendingThemeIndex!!
        val targetThemeName = themes.getOrNull(targetIndex)?.name ?: ""

        AlertDialog(
            onDismissRequest = { pendingThemeIndex = null },
            title = {
                BahaText(
                    text = "更換外觀"
                )
            },
            text = {
                BahaText(
                    text = "更換為「$targetThemeName」外觀將會中斷目前的連線並重新啟動應用程式，是否確定更換?",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newIndex = targetIndex
                        pendingThemeIndex = null
                        selectedIndex = newIndex
                        ThemeStore.setSelectIndex(newIndex)

                        Toast.makeText(
                            context,
                            context.getString(R.string.theme_manager_page_msg01),
                            Toast.LENGTH_SHORT
                        ).show()

                        // 執行斷線流程
                        TelnetClient.myInstance?.close()
                        TempSettings.lastVisitArticleNumber = 0

                        // 立即重啟 Activity 以套用原生主題
                        (context as? Activity)?.recreate()
                    }
                ) {
                    BahaText(
                        text = "確定",
                        color = colors.titleBarTitle
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingThemeIndex = null }) {
                    BahaText(text = "取消", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBackground)
    ) {
        // 頂部導覽列
        BahaTopAppBar(
            title = stringResource(R.string.theme_manager_page),
            onBackClick = onBackClick
        )

        // 內容區域 (可捲動)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 頂端主題選擇按鈕列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEachIndexed { index, theme ->
                    BahaButton(
                        text = theme.name,
                        onClick = {
                            if (index != selectedIndex) {
                                pendingThemeIndex = index
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isSelected = (index == selectedIndex),
                        type = ButtonType.NORMAL
                    )
                }
            }

            // 說明引導文字
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.dialogBorder, RoundedCornerShape(6.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                BahaText(
                    text = stringResource(R.string.theme_manager_page_guide),
                    color = colors.textPrimary,
                    fontSize = BahaTextSize.CAPTION,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // 即時色票與元件展示卡片
            ThemeHybridShowcase()
        }

        // 畫面底部固定導覽列 (滿版無縫 50dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.toolbarDivider)
        )
        BahaButton(
            text = stringResource(R.string._back),
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            type = ButtonType.NORMAL
        )
    }
}
