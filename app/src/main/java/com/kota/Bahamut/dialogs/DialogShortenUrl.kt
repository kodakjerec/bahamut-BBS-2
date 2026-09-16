package com.kota.Bahamut.dialogs

import android.content.ClipboardManager
import android.content.Context
import android.text.util.Linkify
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.ShortenUrl
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaCheckboxLeft
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class DialogShortenUrl : ASDialog() {
    private var shortenUrlListener: DialogShortenUrlListener? = null
    private val urlDatabase = UrlDatabase(context)

    override val name: String?
        get() = "BahamutShortenUrlDialog"

    init {
        setTitle(context.getString(R.string.dialog_shorten_url_title))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors
        var isTransferMode by remember { mutableStateOf(true) }
        var inputUrl by remember { mutableStateOf("") }
        var outputShortUrl by remember { mutableStateOf("") }
        var nonIdEnabled by remember { mutableStateOf(UserSettings.shortUrlNonId) }

        val historyList = remember { mutableStateListOf<ShortenUrl>() }
        LaunchedEffect(Unit) {
            historyList.clear()
            historyList.addAll(urlDatabase.shortenUrls)

            // 自動讀取剪貼簿
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboardManager?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0)?.text?.toString() ?: ""
                if (clipText.isNotEmpty()) {
                    inputUrl = filterUrl(clipText, nonIdEnabled)
                }
            }
        }

        val textFieldColors = TextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedContainerColor = colors.pageBackground,
            unfocusedContainerColor = colors.pageBackground,
            focusedIndicatorColor = colors.toolbarBackgroundFocused,
            unfocusedIndicatorColor = colors.divider
        )

        BahaAlertDialogContent(
            modifier = Modifier.widthIn(min = 280.dp, max = 360.dp),
            title = CommonFunctions.getContextString(R.string.dialog_shorten_url_title),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = { dismiss() }
                ),
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.send),
                    type = ButtonType.NORMAL,
                    onClick = {
                        val result = if (outputShortUrl.isNotEmpty()) outputShortUrl else inputUrl
                        shortenUrlListener?.onShortenUrlDone(result)
                        dismiss()
                    }
                )
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 切換模式按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTransferMode) "縮網址" else CommonFunctions.getContextString(R.string.record),
                        color = colors.textSecondary,
                        fontSize = 14.sp
                    )
                    BahaButton(
                        text = if (isTransferMode) CommonFunctions.getContextString(R.string.record) else CommonFunctions.getContextString(R.string.dialog_shorten_url_transfer),
                        type = ButtonType.NORMAL,
                        onClick = {
                            isTransferMode = !isTransferMode
                            if (!isTransferMode) {
                                historyList.clear()
                                historyList.addAll(urlDatabase.shortenUrls)
                            }
                        },
                        minHeight = 32.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isTransferMode) {
                    // 輸入網址
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text(CommonFunctions.getContextString(R.string.keyword_hint), color = colors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 去識別化 Checkbox
                    BahaCheckboxLeft(
                        text = CommonFunctions.getContextString(R.string.dialog_shorten_url_non_id),
                        checked = nonIdEnabled,
                        onCheckedChange = { checked ->
                            nonIdEnabled = checked
                            UserSettings.setPropertiesShortUrlNonId(checked)
                            inputUrl = filterUrl(inputUrl, checked)
                        },
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 清除 與 轉檔 按鈕
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BahaButton(
                            text = CommonFunctions.getContextString(R.string.reset),
                            type = ButtonType.SECONDARY,
                            onClick = {
                                inputUrl = ""
                                outputShortUrl = ""
                            },
                            modifier = Modifier.weight(1f),
                            minHeight = 36.dp
                        )
                        BahaButton(
                            text = CommonFunctions.getContextString(R.string.dialog_shorten_url_transfer),
                            type = ButtonType.NORMAL,
                            onClick = {
                                transferUrl(inputUrl) { shortUrl ->
                                    outputShortUrl = shortUrl
                                }
                            },
                            modifier = Modifier.weight(1f),
                            minHeight = 36.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 預覽縮網址
                    if (outputShortUrl.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.dialogBlockBackground)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = outputShortUrl,
                                color = colors.bbsAuthor0,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // 歷史紀錄列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(historyList) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        outputShortUrl = item.shortenUrl ?: ""
                                        isTransferMode = true
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = item.title ?: item.shortenUrl ?: "",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (!item.description.isNullOrEmpty()) {
                                    Text(
                                        text = item.description ?: "",
                                        color = colors.textSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 2
                                    )
                                }
                                Text(
                                    text = item.shortenUrl ?: "",
                                    color = colors.bbsAuthor0,
                                    fontSize = 13.sp
                                )
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                        }
                    }
                }
            }
        }
    }

    private fun transferUrl(urlText: String, onResult: (String) -> Unit) {
        var shortenTimes = UserSettings.propertiesNoVipShortenTimes
        if (!UserSettings.propertiesVIP && shortenTimes > 30) {
            ASToast.showLongToast(CommonFunctions.getContextString(R.string.vip_only_message))
            return
        }
        if (urlText.isEmpty()) {
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.keyword_hint))
            return
        }

        val dummyTextView = TextView(context)
        dummyTextView.text = urlText
        Linkify.addLinks(dummyTextView, Linkify.WEB_URLS)
        val urls = dummyTextView.urls
        if (urls.isEmpty()) {
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.no_url))
            return
        }
        val targetUrl = urls[0].url

        // 檢查歷史紀錄
        val historyItem = urlDatabase.getShortenUrl(targetUrl)
        if (historyItem.isNotEmpty() && historyItem[0]?.shortenUrl != null) {
            val shortUrl = historyItem[0]!!.shortenUrl!!
            onResult(shortUrl)
            UserSettings.propertiesNoVipShortenTimes = ++shortenTimes
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.dialog_shorten_url_same_url))
            return
        }

        ASProcessingDialog.showProcessingDialog(CommonFunctions.getContextString(R.string.dialog_shorten_url_under_transfer))
        val apiUrl = "https://worker-short-url.kodakjerec.work/"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("type", "shortenurl")
            .addFormDataPart("url", targetUrl)
            .build()
        val request: Request = Request.Builder().url(apiUrl).post(body).build()

        ASCoroutine.runInNewCoroutine {
            try {
                client.newCall(request).execute().use { response ->
                    val data = response.body.string()
                    val jsonObject = JSONObject(data)
                    val status = jsonObject.optString("res")
                    if (status.isNotEmpty()) {
                        val shortUrl = jsonObject.getString("short_url")
                        val title = jsonObject.getString("title")
                        val description = jsonObject.getString("description")

                        ASCoroutine.ensureMainThread {
                            onResult(shortUrl)
                            UserSettings.propertiesNoVipShortenTimes = ++shortenTimes
                        }
                        urlDatabase.addShortenUrl(targetUrl, title, description, shortUrl)
                    } else {
                        val msg = jsonObject.getString("msg")
                        ASToast.showLongToast(msg)
                    }
                }
            } catch (e: Exception) {
                ASToast.showLongToast(e.message.toString())
                Log.e(javaClass.simpleName, e.message.toString())
            } finally {
                ASProcessingDialog.dismissProcessingDialog()
            }
        }
    }

    private fun filterUrl(rawUrl: String, removeId: Boolean): String {
        if (!removeId) return rawUrl

        val dummyTextView = TextView(context)
        dummyTextView.text = rawUrl
        Linkify.addLinks(dummyTextView, Linkify.WEB_URLS)
        val urls = dummyTextView.urls
        if (urls.isEmpty()) return rawUrl

        val firstUrl = urls[0].url
        val splits = firstUrl.split("?")
        if (splits.size >= 2) {
            var returnString = splits[0]
            if (splits[0].contains("www.youtube.com") || splits[0].contains("www.facebook.com")) {
                val reserveKeys = arrayOf("v", "fbid")
                val params = splits[1].split("&")
                val kept = mutableListOf<String>()
                for (param in params) {
                    val paramPair = param.split("=")
                    if (reserveKeys.contains(paramPair[0])) {
                        kept.add(param)
                    }
                }
                if (kept.isNotEmpty()) {
                    returnString += "?" + kept.joinToString("&")
                }
            }
            return returnString
        }
        return firstUrl
    }

    fun setListener(listener: DialogShortenUrlListener) {
        shortenUrlListener = listener
    }
}
