package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
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
import com.kota.Bahamut.service.CommonFunctions.getContextColor
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
    var mainLayout: LinearLayout? = null
    var viewWidth: Int
    var viewHeight: Int

    // 預設圖層
    lateinit var layoutDefault: LinearLayout

    // 圖片圖層
    lateinit var layoutPic: LinearLayout
    lateinit var photoViewPic: PhotoView
    lateinit var imageViewButton: Button

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

        try {
            UrlDatabase(context).use { urlDatabase ->
                val findUrl: Vector<String> = urlDatabase.getUrl(myUrl)
                urlView.text = myUrl

                if (findUrl.isNotEmpty()) {
                    // 已經有URL資料
                    myTitle = findUrl[1]
                    myDescription = findUrl[2]
                    myImageUrl = findUrl[3]
                    isPic = findUrl[4] != "0"
                    picoUrlChangeStatus(isPic)
                } else {
                    val apiUrl = "https://worker-get-url-content.kodakjerec.workers.dev/"
                    val client: OkHttpClient = OkHttpClient()
                    val body: RequestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("url", myUrl)
                        .build()
                    val request: Request = Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .build()

                    // 尋找URL資料
                    ASCoroutine.runInNewCoroutine {
                        try {
                            // load heads
                            val response: Response = client.newCall(request).execute()
                            val data = response.body
                            val jsonObject: JSONObject = JSONObject(data.string())
                            var contentType: String = jsonObject.getString("contentType")

                            if (contentType.contains("image") || contentType.contains("video") || contentType.contains("audio")) {
                                isPic = true
                            }
                            myTitle = jsonObject.getString("title")
                            myDescription = jsonObject.getString("desc")
                            myImageUrl = jsonObject.getString("imageUrl")

                            // 遠端詢問 cloudflare 解讀失敗，改由本地直接連線獲取內容
                            if (myTitle == "" || myDescription == "") {
                                var userAgent: String = System.getProperty("http.agent")!!
                                if (myUrl.contains("youtu") || myUrl.contains("amazon"))
                                    userAgent = "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"


                                // cookie
                                // Create a new Map to store cookies
                                val cookies: HashMap<String, String> = HashMap()
                                if (myUrl.contains("ptt"))
                                    cookies.put("over18", "1")  // Add the over18 cookie with value 1

                                // 直接去ping對方
                                val resp: Connection.Response = Jsoup
                                    .connect(myUrl)
                                    .header("User-Agent", userAgent)
                                    .cookies(cookies)
                                    .timeout(10000)
                                    .ignoreContentType(true)
                                    .execute()
                                contentType = resp.contentType() ?: ""

                                if (contentType.contains("image/") || contentType.contains("video/")) {
                                    isPic = true
                                }

                                if (contentType.contains("text/html")) {
                                    // 文字處理
                                    val document: Document = resp.parse()

                                    myTitle = document.title()
                                    if (myTitle.isEmpty())
                                        myTitle = document.select("meta[property=og:title]")
                                            .attr("content")

                                    myDescription = document.select("meta[name=description]")
                                        .attr("content")
                                    if (myDescription.isEmpty())
                                        myDescription =
                                            document.select("meta[property=og:description]")
                                                .attr("content")

                                    myImageUrl = document.select("meta[property=og:image]")
                                        .attr("content")
                                    if (myImageUrl.isEmpty())
                                        myImageUrl = document.select("meta[property=og:image]")
                                            .attr("content")
                                    if (myImageUrl.isEmpty())
                                        myImageUrl = document.select("meta[property=og:images]")
                                            .attr("content")
                                    if (myImageUrl.isEmpty())
                                        myImageUrl =
                                            document.select("#landingImage").attr("src")


                                    // 2. 針對 B 站數據進行 Gson 深度解析
                                    parseBilibiliData(document)
                                }

                                // 圖片處理
                                if (isPic) {
                                    if (myTitle.isEmpty()) myTitle = myUrl
                                    if (myImageUrl.isEmpty()) myImageUrl = myUrl // 圖片網址就是預覽圖網址
                                }
                            }

                            // 圖片處理
                            picoUrlChangeStatus(isPic)

                            urlDatabase.addUrl(myUrl, myTitle, myDescription, myImageUrl, isPic)

                            // 上傳至cloudflare, 方便之後擷取
                            val body: RequestBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("url", myUrl)
                                .addFormDataPart("title", myTitle)
                                .addFormDataPart("description", myDescription)
                                .addFormDataPart("imageUrl",myImageUrl)
                                .addFormDataPart("contentType", contentType)
                                .build()
                            val request: Request = Request.Builder()
                                .url(apiUrl)
                                .post(body)
                                .build()
                            client.newCall(request).execute()

                        } catch (e: Exception) {
                            Log.e("loadUrl", e.message.toString())
                            ASCoroutine.ensureMainThread {
                                setFail()
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            ASCoroutine.ensureMainThread {
                setFail()
            }
        }
    }

    /** 針對 B 站數據進行 Gson 深度解析 */
    private fun parseBilibiliData(document: Document) {
        try {
            // 尋找包含狀態數據的 script 標籤
            val scriptTag = document.select("script").find { it.data().contains("window.__INITIAL_STATE__") }

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
                    val gson = GsonBuilder().setLenient().create()
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

    /** 判斷是圖片或連結, 改變顯示狀態  */
    fun picoUrlChangeStatus(isPic: Boolean) {
        loadThumbnailImg = linkShowThumbnail // 讀取預覽圖設定
        loadOnlyWifi = linkShowOnlyWifi // 只在wifi下預覽設定
        val transportType = TempSettings.transportType

        if (isPic) { // 純圖片
            ASCoroutine.ensureMainThread {
                layoutDefault.visibility = GONE

                // 圖片
                layoutPic.visibility = VISIBLE
                if (loadThumbnailImg && (!loadOnlyWifi || transportType == 1)) {
                    prepareLoadImage()
                } else if (myImageUrl == "") {
                    imageViewButton.visibility = GONE
                }

                // 內容
                layoutNormal.visibility = GONE
            }
        } else { // 內容網址
            ASCoroutine.ensureMainThread {
                layoutDefault.visibility = GONE

                // 圖片
                layoutPic.visibility = VISIBLE
                if (loadThumbnailImg && (!loadOnlyWifi || transportType == 1)) {
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
        layoutDefault.visibility = GONE

        // 圖片
        layoutPic.visibility = GONE

        // 內容
        layoutNormal.visibility = GONE
    }

    /** 讀取圖片  */
    private fun loadImage() {
        imgLoaded = true
        ASCoroutine.ensureMainThread {
            imageViewButton.visibility = GONE
            photoViewPic.visibility = VISIBLE
            photoViewPic.contentDescription = myDescription
            try {
                val circularProgressDrawable = CircularProgressDrawable(context)
                circularProgressDrawable.setStrokeWidth(10f)
                circularProgressDrawable.setCenterRadius(60f)
                // progress bar color
                val typedValue = TypedValue()

                context.theme
                    .resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true)
                circularProgressDrawable.setColorSchemeColors(getContextColor(typedValue.resourceId))
                // progress bar start
                circularProgressDrawable.start()

                if (myImageUrl.isEmpty()) {
                    // 如果圖片URL為空，則顯示進度條並標記為失敗
                    photoViewPic.setImageDrawable(circularProgressDrawable)
                    return@ensureMainThread
                }

                // 使用 Glide 載入圖片，直接載入到 PhotoView
                Glide.with(this@ThumbnailItemView)
                    .load(myImageUrl)
                    .placeholder(circularProgressDrawable) // 載入時顯示進度條
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("GlideError", "Image load failed for URL: $myImageUrl", e) // 記錄錯誤訊息
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
                                val picHeight = bitmap.height
                                val picWidth = bitmap.width
                                var targetHeight = viewHeight
                                var targetWidth = viewWidth

                                val scaleWidth = targetWidth.toFloat() / picWidth
                                val scaleHeight = targetHeight.toFloat() / picHeight
                                var scale = min(scaleWidth, scaleHeight)
                                if (scale > 1) scale = 1f

                                val tempHeight = (picHeight * scale).toInt()
                                targetHeight = min(tempHeight, targetHeight)
                                photoViewPic.minimumHeight = targetHeight

                                val tempWidth = (picWidth * scale).toInt()
                                targetWidth = min(tempWidth, targetWidth)
                                photoViewPic.minimumWidth = targetWidth

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
        photoViewPic = mainLayout!!.findViewById(R.id.thumbnail_image_pic)
        photoViewPic.setOnClickListener(openUrlListener)
        photoViewPic.setOnLongClickListener(openImageListener)
        photoViewPic.maximumScale = 20.0f
        photoViewPic.mediumScale = 3.0f

        imageViewButton = mainLayout!!.findViewById(R.id.thumbnail_image_button)
        imageViewButton.setOnClickListener { view: View? -> prepareLoadImage() }

        layoutNormal = mainLayout!!.findViewById(R.id.thumbnail_normal)
        titleView = mainLayout!!.findViewById(R.id.thumbnail_title)
        titleView.setOnClickListener(titleListener)
        descriptionView = mainLayout!!.findViewById(R.id.thumbnail_description)
        descriptionView.setOnClickListener(descriptionListener)
        urlView = mainLayout!!.findViewById(R.id.thumbnail_url)
    }
}
