package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.dialogs.DialogImageView
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.linkShowOnlyWifi
import com.kota.Bahamut.service.UserSettings.Companion.linkShowThumbnail
import com.kota.asFramework.thread.ASCoroutine
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Vector
import kotlin.math.min

class ThumbnailItemView(var myContext: Context) : LinearLayout(myContext) {
    private val isDebug = true // 開啟除錯模式，跳過 urlBase 和 cloudflare

    companion object {
        val manualLoadedUrls: MutableSet<String> =
            java.util.concurrent.ConcurrentHashMap.newKeySet()

        fun clearManualLoadedUrls() {
            manualLoadedUrls.clear()
        }

        private val sharedClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }

        private var cachedUserAgent: String? = null

        fun getRealUserAgent(context: Context): String {
            if (cachedUserAgent == null) {
                try {
                    // 取得裝置真實 WebView 的 User-Agent
                    cachedUserAgent = android.webkit.WebSettings.getDefaultUserAgent(context)
                } catch (e: Exception) {
                    cachedUserAgent = System.getProperty("http.agent")
                        ?: "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36"
                }
            }
            return cachedUserAgent!!
        }
    }

    var mainLayout: LinearLayout? = null
    var viewWidth: Int
    var viewHeight: Int

    // 預設圖層
    lateinit var layoutDefault: LinearLayout

    // 圖片圖層
    lateinit var layoutPic: LinearLayout
    lateinit var loadingView: ProgressBar
    lateinit var photoViewPic: PhotoView
    lateinit var imageViewButton: Button
    lateinit var retryButton: Button

    // 內容圖層
    lateinit var layoutNormal: LinearLayout
    lateinit var titleView: TextView
    lateinit var descriptionView: TextView
    lateinit var urlView: TextView
    var isPic: Boolean = false // 是否為圖片
    var loadThumbnailImg: Boolean = false // 自動顯示預覽圖
    var loadOnlyWifi: Boolean = false // 只在wifi下預覽
    var imgLoaded: Boolean = false // 已經讀取預覽圖

    var myUrl: String = ""
    var myTitle: String = ""
    var myDescription: String = ""
    var myImageUrl: String = ""

    /** 判斷URL內容  */
    fun loadUrl(url: String) {
        myUrl = url
        ASCoroutine.ensureMainThread {
            urlView.text = url
        }

        ASCoroutine.runInNewCoroutine {
            try {
                 val findUrl: Vector<String> = if (!isDebug) {
                     UrlDatabase(context).use { urlDatabase ->
                         urlDatabase.getUrl(url)
                     }
                 } else {
                     Vector()
                 }
                
                if (findUrl.isNotEmpty()) {
                    // 已經有URL資料
                    myTitle = findUrl[1]
                    myDescription = findUrl[2]
                    myImageUrl = findUrl[3]
                    isPic = findUrl[4] != "0"
                    picoUrlChangeStatus(isPic)
                } else {
                    val apiUrl = "https://worker-get-url-content.kodakjerec.work/"
                    val body: RequestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("url", myUrl)
                        .build()
                    val request: Request = Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .build()

                    // 尋找URL資料
                    try {
                        var contentType = ""
                        
                        if (!isDebug) {
                            try {
                                // 嘗試使用共用的 sharedClient 向 Cloudflare Worker 取得網址資料
                                val response: Response = sharedClient.newCall(request).execute()
                                val data = response.body
                                val jsonObject = JSONObject(data.string())
                                contentType = jsonObject.optString("contentType", "")

                                if (checkIsMedia(contentType)) {
                                    isPic = true
                                }
                                myTitle = jsonObject.optString("title", "")
                                myDescription = jsonObject.optString("desc", "")
                                myImageUrl = jsonObject.optString("imageUrl", "")
                            } catch (e: Exception) {
                                Log.e("loadUrl", "Cloudflare request failed: ${e.message}")
                            }
                        }

                        // 遠端詢問 cloudflare 失敗（沒資料或發生 exception），改由本地直接連線獲取內容
                        if (myTitle.isEmpty() || myDescription.isEmpty()) {
                            // 如果是 Youtube 網址，先用 oembed 解析
                            val isYoutubeParsed = parseYoutube(myUrl)

                            // 如果是 twitter or X，用 api.vxtwitter.com 解析
                            val isTwitterParsed = parseTwitter(myUrl)

                            // 如果都不是 (或 API 解析失敗)，才進行一般 Jsoup 解析
                            if (!isYoutubeParsed && !isTwitterParsed) {
                                var userAgent: String = getRealUserAgent(myContext)
                                if (myUrl.contains("youtu") || myUrl.contains("amazon")) {
                                    userAgent =
                                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"
                                }

                                // cookie
                                // Create a new Map to store cookies
                                val cookies: HashMap<String, String> = HashMap()
                                if (myUrl.contains("ptt"))
                                    cookies["over18"] = "1"  // Add the over18 cookie with value 1

                                // 1. 先用 HEAD 請求確認類型，這完全不下載 Body
                                try {
                                    val headResp = Jsoup.connect(myUrl)
                                        .method(Connection.Method.HEAD)
                                        .header("User-Agent", userAgent)
                                        .header("Accept", "*/*")
                                        .cookies(cookies)
                                        .timeout(5000)
                                        .ignoreContentType(true)
                                        .execute()

                                    contentType = headResp.contentType() ?: ""
                                } catch (_: Exception) {
                                }

                                if (checkIsMedia(contentType)) {
                                    isPic = true
                                } else {
                                    // 2. 如果是網頁（或 HEAD 失敗），才執行限制大小的 GET
                                    val getResp: Connection.Response = Jsoup
                                        .connect(myUrl)
                                        .method(Connection.Method.GET)
                                        .header("User-Agent", userAgent)
                                        .header("Accept", "*/*")
                                        .header("Range", "bytes=0-262144") // 請求前 256KB
                                        .cookies(cookies)
                                        .timeout(10000)
                                        .ignoreContentType(true)
                                        .maxBodySize(256 * 1024) // 限制只下載前 256KB
                                        .execute()

                                    contentType = getResp.contentType() ?: ""

                                    if (checkIsMedia(contentType)) {
                                        isPic = true
                                    }

                                    if (contentType.contains("text/html") || contentType.contains("xhtml")) {
                                        // 文字處理
                                        val document: Document = getResp.parse()

                                        parseHtmlMetadata(document)
                                    }
                                }
                            } // end of if (!isYoutubeParsed && !isTwitterParsed)

                            // 圖片處理
                            if (isPic) {
                                if (myTitle.isEmpty()) myTitle = myUrl
                                if (myImageUrl.isEmpty()) myImageUrl = myUrl // 圖片網址就是預覽圖網址
                            }
                        }

                        // 圖片處理
                        picoUrlChangeStatus(isPic)

                        UrlDatabase(context).use { urlDatabase ->
                            urlDatabase.addUrl(myUrl, myTitle, myDescription, myImageUrl, isPic)
                        }

                        // 上傳至cloudflare, 方便之後擷取
                        try {
                            val uploadBody: RequestBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("url", myUrl)
                                .addFormDataPart("title", myTitle)
                                .addFormDataPart("description", myDescription)
                                .addFormDataPart("imageUrl", myImageUrl)
                                .addFormDataPart("contentType", contentType)
                                .build()
                            val uploadRequest: Request = Request.Builder()
                                .url(apiUrl)
                                .post(uploadBody)
                                .build()
                            sharedClient.newCall(uploadRequest).execute().use { }
                        } catch (_: Exception) {
                        }

                    } catch (e: Exception) {
                        Log.e("loadUrl", e.message.toString())
                        ASCoroutine.ensureMainThread {
                            setFail()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("loadUrl", e.message.toString())
                ASCoroutine.ensureMainThread {
                    setFail()
                }
            }
        }
    }

    private data class JsonLdMetadata(
        val title: String = "",
        val description: String = "",
        val imageUrl: String = ""
    )

    private fun parseJsonLd(document: Document): JsonLdMetadata {
        val candidates = mutableListOf<JsonObject>()
        val gson = GsonBuilder().create()

        fun collectNodes(element: JsonElement) {
            when {
                element.isJsonArray -> element.asJsonArray.forEach(::collectNodes)
                element.isJsonObject -> {
                    val objectValue = element.asJsonObject
                    candidates.add(objectValue)
                    objectValue.get("@graph")?.let(::collectNodes)
                    objectValue.get("@included")?.let(::collectNodes)
                }
            }
        }

        document.select("script[type=application/ld+json]").forEach { script ->
            try {
                val json = script.data().ifBlank { script.html() }
                gson.fromJson(json, JsonElement::class.java)?.let(::collectNodes)
            } catch (e: Exception) {
                Log.d("loadUrl", "Unable to parse JSON-LD: ${e.message}")
            }
        }

        fun JsonObject.schemaPriority(): Int {
            val type = get("@type") ?: return 0
            val typeNames = if (type.isJsonArray) {
                type.asJsonArray.mapNotNull { it.takeJsonLdString() }
            } else {
                listOfNotNull(type.takeJsonLdString())
            }
            return if (typeNames.any {
                    it.substringAfterLast('/').substringAfterLast('#').lowercase() in setOf(
                        "article", "newsarticle", "blogposting", "webpage", "product",
                        "videoobject", "recipe", "event", "book", "softwareapplication",
                        "course", "jobposting"
                    )
                }) 1 else 0
        }

        val orderedCandidates = candidates.sortedByDescending { it.schemaPriority() }
        val title = orderedCandidates.firstNotNullOfOrNull { candidate ->
            candidate.get("headline").takeJsonLdString()
                ?: candidate.get("name").takeJsonLdString()
        }.orEmpty()
        val description = orderedCandidates.firstNotNullOfOrNull { candidate ->
            candidate.get("description").takeJsonLdString()
        }.orEmpty()
        val imageUrl = orderedCandidates.firstNotNullOfOrNull { candidate ->
            candidate.get("image").takeJsonLdString()
                ?: candidate.get("thumbnailUrl").takeJsonLdString()
        }?.let { resolveJsonLdUrl(it, document.baseUri()) }.orEmpty()

        return JsonLdMetadata(title, description, imageUrl)
    }

    private fun JsonElement?.takeJsonLdString(): String? {
        if (this == null || isJsonNull) return null
        return when {
            isJsonPrimitive -> asString.trim().takeIf { it.isNotEmpty() }
            isJsonArray -> asJsonArray.firstNotNullOfOrNull { it.takeJsonLdString() }
            isJsonObject -> {
                val objectValue = asJsonObject
                objectValue.get("url").takeJsonLdString()
                    ?: objectValue.get("contentUrl").takeJsonLdString()
                    ?: objectValue.get("@id").takeJsonLdString()
            }

            else -> null
        }
    }

    private fun resolveJsonLdUrl(url: String, baseUri: String): String {
        return try {
            java.net.URI(baseUri).resolve(url).toString()
        } catch (_: Exception) {
            url
        }
    }

    /** 針對 B 站數據進行 Gson 深度解析 */
    private fun parseBilibiliData(document: Document) {
        try {
            // 尋找包含狀態數據的 script 標籤
            val scriptTag =
                document.select("script").find { it.data().contains("window.__INITIAL_STATE__") }

            scriptTag?.let {
                val scriptData = it.data()
                // 1. 設定起始點：從 window.__INITIAL_STATE__={ 之後開始
                val prefix = "window.__INITIAL_STATE__="
                val startPos = scriptData.indexOf(prefix)

                // 2. 設定結束點：你發現的規律關鍵字
                val suffix = ";(function()"
                val endPos = scriptData.indexOf(suffix)

                if (startPos != -1 && endPos != -1) {
                    // 截取 prefix 之後到 suffix 之前的內容
                    val jsonString = scriptData.substring(startPos + prefix.length, endPos)

                    // 使用 GsonBuilder 建立一個「寬容模式」的 Gson
                    val gson = GsonBuilder().create()
                    val rootObj = gson.fromJson(jsonString, JsonObject::class.java)

                    // 利用 Gson 的層級訪問安全地取得 desc
                    // 路徑：video -> viewInfo -> desc
                    val videoDesc = rootObj.getAsJsonObject("video")
                        ?.getAsJsonObject("viewInfo")
                        ?.get("desc")?.asString

                    if (!videoDesc.isNullOrEmpty()) {
                        myDescription = videoDesc
                    }

                    // 導航到 video -> viewInfo -> pic
                    var imageUrl = rootObj.getAsJsonObject("video")
                        ?.getAsJsonObject("viewInfo")
                        ?.get("pic")?.asString

                    if (!imageUrl.isNullOrEmpty()) {
                        imageUrl = imageUrl.replace("http:", "https:")
                        myImageUrl = imageUrl
                    }
                }
            }
        } catch (e: Exception) {
            // 發生錯誤時保留原本 meta 抓到的數據
            e.printStackTrace()
        }
    }

    /**
     * 從 Jsoup 解析完的 HTML Document 中擷取網頁的標題、描述、以及預覽圖網址。
     * 會依序檢查 OpenGraph (og:title 等)、Twitter Card 以及 JSON-LD 等不同的 meta 標籤，
     * 並將結果寫入 myTitle, myDescription, myImageUrl 等屬性中。
     * 
     * @param document Jsoup 解析出來的 HTML 文件模型
     */
    private fun parseHtmlMetadata(document: Document) {
        val ogType = document.select("meta[property=og:type]").attr("content").lowercase()
        if (ogType.startsWith("video")) {
            isPic = true
        }

        // 文字標題處理 (優先順序：OG > Twitter > name=title > <title> > JSON-LD)
        myTitle = document.select("meta[property=og:title]").attr("content")
        if (myTitle.isEmpty()) myTitle = document.select("meta[name=twitter:title]").attr("content")
        if (myTitle.isEmpty()) myTitle = document.select("meta[name=title]").attr("content")
        if (myTitle.isEmpty()) myTitle = document.title()

        // 文字描述處理 (優先順序：OG > Twitter > name=description > JSON-LD)
        myDescription = document.select("meta[property=og:description]").attr("content")
        if (myDescription.isEmpty()) myDescription = document.select("meta[name=twitter:description]").attr("content")
        if (myDescription.isEmpty()) myDescription = document.select("meta[name=description]").attr("content")

        val jsonLd = parseJsonLd(document)
        if (myTitle.isEmpty()) myTitle = jsonLd.title
        if (myDescription.isEmpty()) myDescription = jsonLd.description

        // 預覽圖處理 (優先順序：OG > Twitter > JSON-LD > 網頁圖示/圖標)
        myImageUrl = document.select("meta[property=og:image]").attr("content")
        if (myImageUrl.isEmpty()) myImageUrl = document.select("meta[property=og:image:secure_url]").attr("content")
        if (myImageUrl.isEmpty()) myImageUrl = document.select("meta[name=twitter:image]").attr("content")
        if (myImageUrl.isEmpty()) myImageUrl = document.select("meta[name=twitter:image:src]").attr("content")
        if (myImageUrl.isEmpty()) myImageUrl = document.select("meta[property=og:images]").attr("content")
        if (myImageUrl.isEmpty()) myImageUrl = jsonLd.imageUrl
        
        // 其他圖標備用
        if (myImageUrl.isEmpty()) myImageUrl = document.select("link[rel=image_src]").attr("href")
        if (myImageUrl.isEmpty()) myImageUrl = document.select("link[rel=apple-touch-icon]").attr("href")
        if (myImageUrl.isEmpty()) myImageUrl = document.select("link[rel=apple-touch-icon-precomposed]").attr("href")
        if (myImageUrl.isEmpty()) myImageUrl = document.select("#landingImage").attr("src")

        // 最終備用：網站 favicon
        if (myImageUrl.isEmpty()) {
            myImageUrl = document.select("link[rel~=.*icon.*]").attr("href")
        }

        // 處理相對路徑與 // 開頭的圖片
        if (myImageUrl.isNotEmpty() && !myImageUrl.startsWith("http")) {
            if (myImageUrl.startsWith("//")) {
                myImageUrl = "https:$myImageUrl"
            } else if (myImageUrl.startsWith("/")) {
                try {
                    val uri = android.net.Uri.parse(myUrl)
                    myImageUrl = "${uri.scheme}://${uri.host}$myImageUrl"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. 針對 B 站數據進行 Gson 深度解析
        parseBilibiliData(document)
    }

    /**
     * 檢查給定的 Content-Type 或網址副檔名是否屬於多媒體檔案 (圖片、影片、音訊)。
     * 
     * @param contentType 伺服器回傳的 Content-Type 字串 (例如 "image/jpeg")
     * @return 如果是多媒體檔案則回傳 true，否則回傳 false
     */
    private fun checkIsMedia(contentType: String): Boolean {
        if (contentType.startsWith("image/") || 
            contentType.startsWith("video/") || 
            contentType.startsWith("audio/")) {
            return true
        }
        return false
    }

    fun markManualLoaded() {
        if (myUrl.isNotEmpty()) manualLoadedUrls.add(myUrl)
        if (myImageUrl.isNotEmpty()) manualLoadedUrls.add(myImageUrl)
    }

    /** 判斷是圖片或連結, 改變顯示狀態  */
    fun picoUrlChangeStatus(isPic: Boolean) {
        loadThumbnailImg = linkShowThumbnail // 讀取預覽圖設定
        loadOnlyWifi = linkShowOnlyWifi // 只在wifi下預覽設定
        val transportType = TempSettings.transportType

        val isManualLoaded = (myUrl.isNotEmpty() && manualLoadedUrls.contains(myUrl)) ||
                (myImageUrl.isNotEmpty() && manualLoadedUrls.contains(myImageUrl))
        val shouldLoadImage =
            (loadThumbnailImg && (!loadOnlyWifi || transportType == 1)) || isManualLoaded

        ASCoroutine.ensureMainThread {
            retryButton.visibility = GONE
            if (isPic) { // 純圖片
                layoutDefault.visibility = GONE

                // 圖片
                layoutPic.visibility = VISIBLE
                if (shouldLoadImage) {
                    prepareLoadImage()
                } else if (myImageUrl == "") {
                    imageViewButton.visibility = GONE
                }

                // 內容
                layoutNormal.visibility = GONE
            } else { // 內容網址
                layoutDefault.visibility = GONE

                // 圖片
                layoutPic.visibility = VISIBLE
                if (shouldLoadImage) {
                    prepareLoadImage()
                } else if (myImageUrl == "") {
                    imageViewButton.visibility = GONE
                }

                // 內容
                layoutNormal.visibility = VISIBLE
                setNormal()
            }
        }
    }

    /** 純圖片  */
    fun prepareLoadImage() {
        markManualLoaded()
        if (imgLoaded) return

        loadImage()
        urlView.text = myImageUrl
    }

    /** 內容網址  */
    private fun setNormal() {
        if (!myTitle.isEmpty()) {
            titleView.text = myTitle
            titleView.visibility = VISIBLE
        }
        if (!myDescription.isEmpty()) {
            descriptionView.text = myDescription
            descriptionView.visibility = VISIBLE
        }
        urlView.text = myUrl
    }

    /** 意外處理  */
    private fun setFail() {
        imgLoaded = false
        layoutDefault.visibility = GONE

        // 圖片
        layoutPic.visibility = VISIBLE
        loadingView.visibility = GONE
        photoViewPic.visibility = GONE
        imageViewButton.visibility = GONE
        retryButton.visibility = VISIBLE

        // 內容
        layoutNormal.visibility = GONE
    }

    /** 讀取圖片  */
    private fun loadImage() {
        imgLoaded = true
        // 立即顯示 loading，隱藏其他
        imageViewButton.visibility = GONE
        retryButton.visibility = GONE
        loadingView.visibility = VISIBLE
        photoViewPic.visibility = GONE
        photoViewPic.contentDescription = myDescription

        if (myImageUrl.isEmpty()) {
            setFail()
            return
        }

        ASCoroutine.ensureMainThread {
            try {

                // 使用 Glide 載入圖片，直接載入到 PhotoView
                Glide.with(this@ThumbnailItemView)
                    .load(myImageUrl)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e(
                                "GlideError",
                                "Image load failed for URL: $myImageUrl",
                                e
                            ) // 記錄錯誤訊息
                            ASCoroutine.ensureMainThread {
                                setFail()
                            }
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
                            try {
                                val bitmap: Bitmap
                                if (resource is GifDrawable) bitmap = resource.firstFrame
                                else bitmap = (resource as BitmapDrawable).bitmap

                                // 取得圖片原始寬高
                                val picHeight = bitmap.height
                                val picWidth = bitmap.width

                                // 目標最大尺寸設定：寬度滿版，高度為畫面一半
                                var targetHeight = viewHeight / 2
                                var targetWidth = viewWidth

                                // 計算縮放比例：無論是放大還是縮小，都會等比例縮放直到寬度碰到滿版，或高度碰到「畫面一半」的邊界
                                val scaleWidth = targetWidth.toFloat() / picWidth
                                val scaleHeight = targetHeight.toFloat() / picHeight
                                val scale = min(scaleWidth, scaleHeight)

                                val tempHeight = (picHeight * scale).toInt()
                                targetHeight = min(tempHeight, targetHeight)
                                photoViewPic.minimumHeight = targetHeight

                                val tempWidth = (picWidth * scale).toInt()
                                targetWidth = min(tempWidth, targetWidth)
                                photoViewPic.minimumWidth = targetWidth

                                // 隱藏 loading，顯示圖片
                                loadingView.visibility = GONE
                                photoViewPic.visibility = VISIBLE

                                if (resource is GifDrawable) {
                                    resource.startFromFirstFrame()
                                    photoViewPic.setImageDrawable(resource)
                                } else {
                                    val newBitmap = bitmap.scale(targetWidth, targetHeight)
                                    photoViewPic.setImageBitmap(newBitmap)
                                }
                            } catch (_: Exception) {
                                ASCoroutine.ensureMainThread {
                                    setFail()
                                }
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            } catch (_: Exception) {
                ASCoroutine.ensureMainThread {
                    setFail()
                }
            }
        }
    }

    /** 用預設瀏覽器開啟連結 */
    var openUrlListener: OnClickListener = OnClickListener {
        val intent = Intent(Intent.ACTION_VIEW, myUrl.toUri())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        myContext.startActivity(intent)
    }

    /** 用簡易圖片檢視視窗開啟圖片 */
    var openImageListener: OnLongClickListener = OnLongClickListener {
        DialogImageView()
            .setImageUrl(myImageUrl)
            .show()
        true
    }

    /** 點擊標題展開或收起 */
    var titleListener: OnClickListener = OnClickListener { view: View? ->
        val textView = view as TextView
        if (textView.maxLines == 2) textView.maxLines = 9
        else textView.maxLines = 2
    }

    /** 點擊描述展開或收起 */
    var descriptionListener: OnClickListener = OnClickListener { view: View? ->
        val textView = view as TextView
        if (textView.maxLines == 1) textView.maxLines = 9
        else textView.maxLines = 1
    }

    /** 針對 Youtube 網址進行 oembed 解析 */
    private fun parseYoutube(url: String): Boolean {
        if (!url.contains("youtu.be") && !url.contains("youtube.com")) return false
        
        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=$url"
            val request: Request = Request.Builder()
                .url(oembedUrl)
                .get()
                .build()
            val response: Response = sharedClient.newCall(request).execute()
            val data = response.body
            if (data != null) {
                val jsonObject = JSONObject(data.string())
                myTitle = jsonObject.optString("title", "")
                myDescription = jsonObject.optString("author_name", "")
                myImageUrl = jsonObject.optString("thumbnail_url", "")
                isPic = false // 顯示為內容網址 (包含圖片與文字)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /** 針對 Twitter / X 網址進行 api.vxtwitter.com 解析 */
    private fun parseTwitter(url: String): Boolean {
        try {
            val uri = android.net.Uri.parse(url)
            val hostname = uri.host ?: return false
            val isTwitterHost = hostname == "twitter.com" || hostname.endsWith(".twitter.com") ||
                    hostname == "x.com" || hostname.endsWith(".x.com")

            if (isTwitterHost) {
                // 將網域替換為 api.vxtwitter.com，保留路徑與參數
                val apiUrl = url.replaceFirst(hostname, "api.vxtwitter.com")
                val request: Request = Request.Builder()
                    .url(apiUrl)
                    .get()
                    .build()
                val response: Response = sharedClient.newCall(request).execute()
                val data = response.body
                if (data != null) {
                    val jsonObject = JSONObject(data.string())
                    val userName = jsonObject.optString("user_name", "")
                    val screenName = jsonObject.optString("user_screen_name", "")
                    myTitle = "$userName @$screenName"
                    myDescription = jsonObject.optString("text", "")

                    val mediaURLs = jsonObject.optJSONArray("mediaURLs")
                    if (mediaURLs != null && mediaURLs.length() > 0) {
                        myImageUrl = mediaURLs.optString(0, "")
                    }
                    isPic = false // 顯示為連結預覽
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    init {
        val metrics = DisplayMetrics()
        myContext.resources.displayMetrics.also { metrics.setTo(it) }
        viewWidth = metrics.widthPixels
        viewHeight = metrics.heightPixels
        init()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.thumbnail,
            this
        )
        mainLayout = findViewById(R.id.thumbnail_content_view)
        layoutDefault = mainLayout!!.findViewById(R.id.thumbnail_default)

        layoutPic = mainLayout!!.findViewById(R.id.thumbnail_pic)
        loadingView = mainLayout!!.findViewById(R.id.thumbnail_loading)
        photoViewPic = mainLayout!!.findViewById(R.id.thumbnail_image_pic)
        photoViewPic.setOnClickListener(openUrlListener)
        photoViewPic.setOnLongClickListener(openImageListener)
        photoViewPic.maximumScale = 20.0f
        photoViewPic.mediumScale = 3.0f

        imageViewButton = mainLayout!!.findViewById(R.id.thumbnail_image_button)
        imageViewButton.setOnClickListener { view: View? -> prepareLoadImage() }

        retryButton = mainLayout!!.findViewById(R.id.thumbnail_retry_button)
        retryButton.setOnClickListener {
            retryButton.visibility = GONE
            imgLoaded = false
            markManualLoaded()
            if (myImageUrl.isNotEmpty()) {
                prepareLoadImage()
            } else {
                layoutDefault.visibility = VISIBLE
                layoutPic.visibility = GONE
                loadUrl(myUrl)
            }
        }

        layoutNormal = mainLayout!!.findViewById(R.id.thumbnail_normal)

        // title 顏色 = 作者內文 顏色
        titleView = mainLayout!!.findViewById(R.id.thumbnail_title)
        titleView.setOnClickListener(titleListener)
        // description 色為 title 減半
        descriptionView = mainLayout!!.findViewById(R.id.thumbnail_description)
        descriptionView.setOnClickListener(descriptionListener)
        urlView = mainLayout!!.findViewById(R.id.thumbnail_url)
    }
}
