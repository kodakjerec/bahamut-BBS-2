package com.kota.Bahamut.dialogs

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASDialog
import java.util.Vector

class DialogQueryHero : ASDialog() {
    private var heroId by mutableStateOf("")
    private var heroNick by mutableStateOf("")
    private var heroHp by mutableStateOf("")
    private var heroMp by mutableStateOf("")
    private var heroAuth1 by mutableStateOf("")
    private var heroAuth2 by mutableStateOf("")
    private var heroLastDate by mutableStateOf("")
    private var heroLastTime by mutableStateOf("")
    private var heroFromIp by mutableStateOf("")
    private var showWebView by mutableStateOf(false)

    override val name: String?
        get() = "BahamutQueryHeroDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.dialog_query_hero))
        setComposeContent {
            Content()
        }
    }

    @SuppressLint("SetTextI18n")
    fun getData(fromStrings: Vector<String>) {
        try {
            val regex = """
                ^(?<id>\w+)\((?<nick>.*)\) HP： (?<hp>\d+) ，MP： (?<mp>\d+) ，(?<authType>\w+)：(?<authStatus>.*)
            """.trimIndent().toRegex()
            val regex2 = """
                ^上次\((?<lastDate>.*)日 (?<lastTime>.*)\)來自\((?<fromIp>.*)\)
            """.trimIndent().toRegex()

            val match = regex.find(fromStrings[2])!!
            heroId = match.groups[1]?.value ?: ""
            heroNick = match.groups[2]?.value ?: ""
            heroHp = match.groups[3]?.value ?: ""
            heroMp = match.groups[4]?.value ?: ""
            heroAuth1 = match.groups[5]?.value ?: "未知"
            heroAuth2 = match.groups[6]?.value ?: "無"

            val match2 = regex2.find(fromStrings[3])!!
            heroLastDate = (match2.groups[1]?.value ?: "") + "日"
            heroLastTime = match2.groups[2]?.value ?: ""
            heroFromIp = match2.groups[3]?.value ?: ""
        } catch (_: Exception) {
            dismiss()
            ASAlertDialog.createDialog()
                .setTitle("錯誤")
                .setMessage("取得勇者資料出錯")
                .addButton("確定")
                .setListener { aDialog: ASAlertDialog, _: Int -> aDialog.dismiss() }
                .show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun Content() {
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        BahaAlertDialogContent(
            modifier = Modifier.widthIn(min = 280.dp, max = 360.dp),
            title = CommonFunctions.getContextString(R.string.dialog_query_hero),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = { dismiss() }
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // ID & 暱稱
                Text(
                    text = if (heroNick.isNotEmpty()) "$heroId ($heroNick)" else heroId,
                    color = colors.bbsAuthor0,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // HP / MP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "HP: $heroHp",
                        color = Color(0xFFE53935),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MP: $heroMp",
                        color = Color(0xFF1E88E5),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 認證
                if (heroAuth1.isNotEmpty() || heroAuth2.isNotEmpty()) {
                    Text(
                        text = "$heroAuth1 : $heroAuth2",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // 上次上線時間 & IP
                Text(
                    text = "${CommonFunctions.getContextString(R.string.dialog_query_hero_last_time)}: $heroLastDate $heroLastTime",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "${CommonFunctions.getContextString(R.string.dialog_query_hero_from)}: $heroFromIp",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // 顯示巴哈首頁按鈕
                if (!showWebView && heroId.isNotEmpty()) {
                    BahaButton(
                        text = CommonFunctions.getContextString(R.string.dialog_query_hero_show_web_view),
                        type = ButtonType.NORMAL,
                        onClick = { showWebView = true },
                        modifier = Modifier.fillMaxWidth(),
                        minHeight = 36.dp
                    )
                }

                // WebView
                if (showWebView && heroId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, colors.divider)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView, url: String) {
                                            super.onPageFinished(view, url)
                                            try {
                                                view.evaluateJavascript("document.getElementsByClassName('download-app_box01')[0]?.remove();", null)
                                                view.evaluateJavascript("document.getElementsByClassName('bh-banner')[0]?.remove();", null)
                                                view.evaluateJavascript("document.getElementsByClassName('sidebar-navbar_rwd')[0]?.remove();", null)
                                            } catch (e: Exception) {
                                                android.util.Log.e("DialogQueryHero", "evaluateJavascript error", e)
                                            }
                                        }
                                    }
                                    loadUrl("https://m.gamer.com.tw/home/home.php?owner=$heroId")
                                }
                            },
                            onRelease = { view ->
                                try {
                                    (view.parent as? android.view.ViewGroup)?.removeView(view)
                                    view.stopLoading()
                                    view.loadUrl("about:blank")
                                    view.destroy()
                                } catch (e: Exception) {
                                    android.util.Log.e("DialogQueryHero", "WebView release error", e)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
