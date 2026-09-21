package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.dialogs.DialogImageView
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.linkShowOnlyWifi
import com.kota.Bahamut.service.UserSettings.Companion.linkShowThumbnail
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * 縮圖預覽資料模型
 */
data class ThumbnailData(
    val url: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val isPic: Boolean
)

/**
 * 共用 OkHttpClient
 */
private val sharedClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
}

/**
 * 針對 B 站數據進行 Gson 深度解析
 */
private fun parseBilibiliData(document: Document): Pair<String?, String?> {
    var bilibiliDesc: String? = null
    var bilibiliImg: String? = null
    try {
        val scriptTag = document.select("script").find { it.data().contains("window.__INITIAL_STATE__") }
        scriptTag?.let {
            val scriptData = it.data()
            val prefix = "window.__INITIAL_STATE__="
            val startPos = scriptData.indexOf(prefix)
            val suffix = ";(function()"
            val endPos = scriptData.indexOf(suffix)

            if (startPos != -1 && endPos != -1) {
                val jsonString = scriptData.substring(startPos + prefix.length, endPos)
                val gson = GsonBuilder().create()
                val rootObj = gson.fromJson(jsonString, JsonObject::class.java)

                val videoDesc = rootObj.getAsJsonObject("video")
                    ?.getAsJsonObject("viewInfo")
                    ?.get("desc")?.asString
                if (!videoDesc.isNullOrEmpty()) {
                    bilibiliDesc = videoDesc
                }

                var imageUrl = rootObj.getAsJsonObject("video")
                    ?.getAsJsonObject("viewInfo")
                    ?.get("pic")?.asString
                if (!imageUrl.isNullOrEmpty()) {
                    imageUrl = imageUrl.replace("http:", "https:")
                    bilibiliImg = imageUrl
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Pair(bilibiliDesc, bilibiliImg)
}

/**
 * 擷取 URL 資訊（優先查詢本地資料庫，無快取則請求遠端或解析網頁）
 */
suspend fun fetchThumbnailData(context: Context, targetUrl: String): ThumbnailData? = withContext(Dispatchers.IO) {
    try {
        UrlDatabase(context).use { urlDatabase ->
            val findUrl = urlDatabase.getUrl(targetUrl)
            if (findUrl.isNotEmpty()) {
                return@withContext ThumbnailData(
                    url = targetUrl,
                    title = findUrl[1],
                    description = findUrl[2],
                    imageUrl = findUrl[3],
                    isPic = findUrl[4] != "0"
                )
            }

            val apiUrl = "https://worker-get-url-content.kodakjerec.work/"
            val body: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("url", targetUrl)
                .build()
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()

            var isPic = false
            var title = ""
            var description = ""
            var imageUrl = ""
            var contentType = ""

            try {
                val response = sharedClient.newCall(request).execute()
                val data = response.body?.string()
                if (!data.isNullOrEmpty()) {
                    val jsonObject = JSONObject(data)
                    contentType = jsonObject.optString("contentType", "")
                    if (contentType.contains("image") || contentType.contains("video") || contentType.contains("audio")) {
                        isPic = true
                    }
                    title = jsonObject.optString("title", "")
                    description = jsonObject.optString("desc", "")
                    imageUrl = jsonObject.optString("imageUrl", "")
                }
            } catch (e: Exception) {
                Log.e("fetchThumbnailData", "Remote worker error: ${e.message}")
            }

            // 遠端詢問 cloudflare 解讀失敗，改由本地直接連線獲取內容
            if (title.isEmpty() || description.isEmpty()) {
                var userAgent = System.getProperty("http.agent") ?: ""
                if (targetUrl.contains("youtu") || targetUrl.contains("amazon")) {
                    userAgent = "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"
                }

                val cookies = HashMap<String, String>()
                if (targetUrl.contains("ptt")) {
                    cookies["over18"] = "1"
                }

                try {
                    val headResp = Jsoup.connect(targetUrl)
                        .method(Connection.Method.HEAD)
                        .header("User-Agent", userAgent)
                        .cookies(cookies)
                        .timeout(5000)
                        .ignoreContentType(true)
                        .execute()
                    contentType = headResp.contentType() ?: ""
                } catch (_: Exception) {}

                if (contentType.contains("image/") || contentType.contains("video/")) {
                    isPic = true
                } else {
                    try {
                        val getResp = Jsoup.connect(targetUrl)
                            .header("User-Agent", userAgent)
                            .header("Range", "bytes=0-102400")
                            .cookies(cookies)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .maxBodySize(100 * 1024)
                            .execute()

                        contentType = getResp.contentType() ?: ""
                        if (contentType.contains("image/") || contentType.contains("video/")) {
                            isPic = true
                        }
                        if (contentType.contains("text/html")) {
                            val document = getResp.parse()
                            title = document.title()
                            if (title.isEmpty()) {
                                title = document.select("meta[property=og:title]").attr("content")
                            }
                            description = document.select("meta[name=description]").attr("content")
                            if (description.isEmpty()) {
                                description = document.select("meta[property=og:description]").attr("content")
                            }
                            imageUrl = document.select("meta[property=og:image]").attr("content")
                            if (imageUrl.isEmpty()) {
                                imageUrl = document.select("meta[property=og:images]").attr("content")
                            }
                            if (imageUrl.isEmpty()) {
                                imageUrl = document.select("#landingImage").attr("src")
                            }

                            val (bDesc, bImg) = parseBilibiliData(document)
                            if (!bDesc.isNullOrEmpty()) description = bDesc
                            if (!bImg.isNullOrEmpty()) imageUrl = bImg
                        }
                    } catch (e: Exception) {
                        Log.e("fetchThumbnailData", "Jsoup parse error: ${e.message}")
                    }
                }

                if (isPic) {
                    if (title.isEmpty()) title = targetUrl
                    if (imageUrl.isEmpty()) imageUrl = targetUrl
                }
            }

            urlDatabase.addUrl(targetUrl, title, description, imageUrl, isPic)

            // 上傳至 cloudflare, 方便之後擷取
            try {
                val uploadBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("url", targetUrl)
                    .addFormDataPart("title", title)
                    .addFormDataPart("description", description)
                    .addFormDataPart("imageUrl", imageUrl)
                    .addFormDataPart("contentType", contentType)
                    .build()
                val uploadRequest = Request.Builder()
                    .url(apiUrl)
                    .post(uploadBody)
                    .build()
                sharedClient.newCall(uploadRequest).execute()
            } catch (_: Exception) {}

            ThumbnailData(
                url = targetUrl,
                title = title,
                description = description,
                imageUrl = imageUrl,
                isPic = isPic
            )
        }
    } catch (e: Exception) {
        Log.e("fetchThumbnailData", "Failed to fetch URL data: ${e.message}")
        null
    }
}

/**
 * Compose 縮圖項目元件 (ThumbnailItemView)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThumbnailItemView(
    url: String,
    loadAllTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    if (url.isEmpty()) return

    val context = LocalContext.current
    val colors = AppTheme.colors

    var thumbnailData by remember(url) { mutableStateOf<ThumbnailData?>(null) }
    var isLoadingUrl by remember(url) { mutableStateOf(true) }
    var isFailed by remember(url) { mutableStateOf(false) }

    var imageRequested by remember(url) { mutableStateOf(false) }
    var isImageLoading by remember(url) { mutableStateOf(false) }
    var loadedDrawable by remember(url) { mutableStateOf<Drawable?>(null) }

    var isTitleExpanded by remember(url) { mutableStateOf(false) }
    var isDescExpanded by remember(url) { mutableStateOf(false) }

    // 載入 URL 解析資料
    LaunchedEffect(url) {
        isLoadingUrl = true
        isFailed = false
        val data = fetchThumbnailData(context, url)
        isLoadingUrl = false
        if (data != null) {
            thumbnailData = data
        } else {
            isFailed = true
        }
    }

    // 響應自動載入設定或「載入全部圖片」觸發
    LaunchedEffect(thumbnailData, loadAllTrigger) {
        val data = thumbnailData
        if (data != null && data.imageUrl.isNotEmpty()) {
            val autoLoad = linkShowThumbnail && (!linkShowOnlyWifi || TempSettings.transportType == 1)
            if (autoLoad || loadAllTrigger > 0) {
                imageRequested = true
            }
        }
    }

    // 載入圖片 (Glide)
    LaunchedEffect(imageRequested, thumbnailData?.imageUrl) {
        val imageUrl = thumbnailData?.imageUrl
        if (imageRequested && !imageUrl.isNullOrEmpty() && loadedDrawable == null && !isImageLoading) {
            isImageLoading = true
            Glide.with(context)
                .load(imageUrl)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("ThumbnailItemView", "Image load failed for URL: $imageUrl", e)
                        isImageLoading = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(object : CustomTarget<Drawable?>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        loadedDrawable = resource
                        isImageLoading = false
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    // 若載入失敗則不顯示
    if (isFailed) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(bottom = 4.dp)
    ) {
        if (isLoadingUrl) {
            // URL 解析中預設狀態
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(colors.dialogTitleBackground),
                contentAlignment = Alignment.Center
            ) {
                BahaText(
                    text = stringResource(R.string.loading),
                    color = colors.buttonText,
                    fontSize = AppTheme.fontSize.caption
                )
            }
        } else {
            val data = thumbnailData
            if (data != null) {
                // 圖片區域
                if (data.imageUrl.isNotEmpty()) {
                    if (isImageLoading) {
                        // 圖片載入中圓圈動畫
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(colors.dialogTitleBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                                    color = colors.textLink,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    } else if (loadedDrawable != null) {
                        // 圖片已載入成功，使用 PhotoView 支援手勢縮放、GIF 動畫與全螢幕檢視
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            factory = { ctx ->
                                PhotoView(ctx).apply {
                                    maximumScale = 20.0f
                                    mediumScale = 3.0f
                                    setOnClickListener {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            }
                                            ctx.startActivity(intent)
                                        } catch (e: Exception) {
                                            Log.e("ThumbnailItemView", "Open url failed", e)
                                        }
                                    }
                                    setOnLongClickListener {
                                        DialogImageView()
                                            .setImageUrl(data.imageUrl)
                                            .show()
                                        true
                                    }
                                }
                            },
                            update = { photoView ->
                                photoView.contentDescription = data.description
                                val drawable = loadedDrawable
                                if (drawable is GifDrawable) {
                                    drawable.startFromFirstFrame()
                                    photoView.setImageDrawable(drawable)
                                } else if (drawable is BitmapDrawable) {
                                    val bitmap = drawable.bitmap
                                    val picHeight = bitmap.height
                                    val picWidth = bitmap.width
                                    val metrics = context.resources.displayMetrics
                                    val targetWidth = metrics.widthPixels
                                    val targetHeight = metrics.heightPixels

                                    val scaleWidth = targetWidth.toFloat() / picWidth
                                    val scaleHeight = targetHeight.toFloat() / picHeight
                                    val scale = minOf(scaleWidth, scaleHeight, 1f)

                                    val finalWidth = minOf((picWidth * scale).toInt(), targetWidth)
                                    val finalHeight = minOf((picHeight * scale).toInt(), targetHeight)

                                    photoView.minimumWidth = finalWidth
                                    photoView.minimumHeight = finalHeight

                                    val newBitmap = bitmap.scale(finalWidth, finalHeight)
                                    photoView.setImageBitmap(newBitmap)
                                } else if (drawable != null) {
                                    photoView.setImageDrawable(drawable)
                                }
                            }
                        )
                    } else if (!imageRequested) {
                        // 尚未載入圖片時顯示按鈕
                        Box(
                            modifier = modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(colors.dialogSelectArticleFocused)
                                .combinedClickable(
                                    onClick = { imageRequested = true }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            BahaText(
                                text = stringResource(R.string.thumbnail_show_pic),
                                color = colors.textPrimary,
                                fontSize = AppTheme.fontSize.title,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                // 網頁資訊（標題、描述、網址）
                if (!data.isPic) {
                    if (data.title.isNotEmpty()) {
                        BahaText(
                            text = data.title,
                            color = colors.bbsContent0,
                            fontSize = AppTheme.fontSize.title,
                            maxLines = if (isTitleExpanded) 9 else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTitleExpanded = !isTitleExpanded }
                                .padding(vertical = 2.dp)
                        )
                    }
                    if (data.description.isNotEmpty()) {
                        BahaText(
                            text = data.description,
                            color = colors.textSecondary,
                            fontSize = AppTheme.fontSize.body,
                            maxLines = if (isDescExpanded) 9 else 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDescExpanded = !isDescExpanded }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 相容別名
 */
@Composable
fun ThumbnailItem(
    url: String,
    loadAllTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    ThumbnailItemView(
        url = url,
        loadAllTrigger = loadAllTrigger,
        modifier = modifier
    )
}

