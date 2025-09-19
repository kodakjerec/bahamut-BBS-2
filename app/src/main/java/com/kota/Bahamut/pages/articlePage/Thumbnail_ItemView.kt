package com.kota.Bahamut.pages.articlePage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.thread.ASRunner.Companion.runInNewThread
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.linkShowOnlyWifi
import com.kota.Bahamut.service.UserSettings.Companion.linkShowThumbnail
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.Vector
import kotlin.math.min

class Thumbnail_ItemView(var myContext: Context) : LinearLayout(myContext) {
    var mainLayout: LinearLayout? = null
    var viewWidth: Int
    var viewHeight: Int

    // 預設圖層
    var layoutDefault: LinearLayout? = null

    // 圖片圖層
    var layoutPic: LinearLayout? = null
    var photoViewPic: PhotoView? = null
    var imageViewButton: Button? = null

    // 內容圖層
    var layoutNormal: LinearLayout? = null
    var titleView: TextView? = null
    var descriptionView: TextView? = null
    var urlView: TextView? = null
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
            UrlDatabase(getContext()).use { urlDatabase ->
                val findUrl: Vector<String>? = urlDatabase.getUrl(myUrl)
                urlView?.setText(myUrl)
                // 已經有URL資料
                if (findUrl != null) {
                    myTitle = findUrl.get(1)
                    myDescription = findUrl.get(2)
                    myImageUrl = findUrl.get(3)
                    isPic = findUrl.get(4) != "0"
                    picOrUrl_changeStatus(isPic)
                } else {
                    val apiUrl = "https://worker-get-url-content.kodakjerec.workers.dev/"
                    val client = OkHttpClient()
                    val body: RequestBody = MultipartBody.Builder()
                        .setType(MultipartBody.Companion.FORM)
                        .addFormDataPart("url", myUrl)
                        .build()
                    val request = Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .build()

                    // 尋找URL資料
                    runInNewThread(Runnable {
                        try {
                            // load heads
                            val response = client.newCall(request).execute()
                            checkNotNull(response.body)
                            val data = response.body.string()
                            val jsonObject = JSONObject(data)

                            var contentType = jsonObject.getString("contentType")

                            if (contentType.contains("image") || contentType.contains("video") || contentType.contains(
                                    "audio"
                                )
                            ) {
                                isPic = true
                            }
                            myTitle = jsonObject.getString("title")
                            myDescription = jsonObject.getString("desc")
                            myImageUrl = jsonObject.getString("imageUrl")

                            // 非圖片類比較會有擷取問題
                            if (!isPic && (myTitle == "" || myDescription == "")) {
                                var userAgent = System.getProperty("http.agent")
                                if (myUrl.contains("youtu") || myUrl.contains("amazon")) userAgent =
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"

                                // cookie
                                // Create a new Map to store cookies
                                val cookies: MutableMap<String?, String?> =
                                    HashMap<String?, String?>()
                                if (myUrl.contains("ptt")) cookies.put(
                                    "over18",
                                    "1"
                                ) // Add the over18 cookie with value 1


                                val resp = Jsoup
                                    .connect(myUrl)
                                    .header("User-Agent", userAgent!!)
                                    .cookies(cookies)
                                    .execute()
                                contentType = resp.contentType()

                                if (contentType.contains("image/") || contentType.contains("video/")) {
                                    isPic = true
                                }
                                // 域名判斷
                                if (url.contains("i.imgur")) {
                                    isPic = true
                                }

                                // 圖片處理
                                if (isPic) {
                                    myTitle = myUrl
                                    myDescription = ""
                                    myImageUrl = myUrl
                                } else {
                                    // 文字處理
                                    val document = resp.parse()

                                    myTitle = document.title()
                                    if (myTitle.isEmpty()) myTitle =
                                        document.select("meta[property=og:title]").attr("content")

                                    myDescription =
                                        document.select("meta[name=description]").attr("content")
                                    if (myDescription.isEmpty()) myDescription =
                                        document.select("meta[property=og:description]")
                                            .attr("content")

                                    myImageUrl =
                                        document.select("meta[property=og:image]").attr("content")
                                    if (myImageUrl.isEmpty()) myImageUrl =
                                        document.select("meta[property=og:image]").attr("content")
                                    if (myImageUrl.isEmpty()) myImageUrl =
                                        document.select("meta[property=og:images]").attr("content")
                                    if (myImageUrl.isEmpty()) myImageUrl =
                                        document.select("#landingImage").attr("src")
                                }
                            }

                            // 圖片處理
                            picOrUrl_changeStatus(isPic)

                            urlDatabase.addUrl(myUrl, myTitle, myDescription, myImageUrl, isPic)
                        } catch (ignored: Exception) {
                            object : ASRunner() {
                                // com.kota.ASFramework.Thread.ASRunner
                                public override fun run() {
                                    set_fail()
                                }
                            }.runInMainThread()
                        }
                    })
                }
            }
        } catch (ignored: Exception) {
            object : ASRunner() {
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    set_fail()
                }
            }.runInMainThread()
        }
    }

    /** 判斷是圖片或連結, 改變顯示狀態  */
    fun picOrUrl_changeStatus(_isPic: Boolean) {
        loadThumbnailImg = linkShowThumbnail
        loadOnlyWifi = linkShowOnlyWifi
        val _transportType = TempSettings.transportType

        if (_isPic) { // 純圖片
            object : ASRunner() {
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    layoutDefault?.setVisibility(GONE)

                    // 圖片
                    layoutPic?.setVisibility(VISIBLE)
                    if (loadThumbnailImg && (!loadOnlyWifi || _transportType == 1)) {
                        prepare_load_image()
                    } else if (myImageUrl == "") {
                        imageViewButton?.setVisibility(GONE)
                    }

                    // 內容
                    layoutNormal?.setVisibility(GONE)
                }
            }.runInMainThread()
        } else { // 內容網址
            object : ASRunner() {
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    layoutDefault?.setVisibility(GONE)

                    // 圖片
                    layoutPic?.setVisibility(VISIBLE)
                    if (loadThumbnailImg && (!loadOnlyWifi || _transportType == 1)) {
                        prepare_load_image()
                    } else if (myImageUrl == "") {
                        imageViewButton?.setVisibility(GONE)
                    }

                    // 內容
                    layoutNormal?.setVisibility(VISIBLE)
                    set_normal()
                }
            }.runInMainThread()
        }
    }

    /** 純圖片  */
    fun prepare_load_image() {
        if (imgLoaded) return

        if (isPic) {
            viewHeight = viewHeight / 2
        } else {
            viewHeight = viewHeight / 4
        }
        photoViewPic?.setMinimumHeight(viewHeight)
        loadImage()
        urlView?.setText(myImageUrl)
    }

    /** 內容網址  */
    private fun set_normal() {
        if (!myTitle.isEmpty()) {
            titleView?.setText(myTitle)
            titleView?.setVisibility(VISIBLE)
        }
        if (!myDescription.isEmpty()) {
            descriptionView?.setText(myDescription)
            descriptionView?.setVisibility(VISIBLE)
        }
        urlView?.setText(myUrl)
    }

    /** 意外處理  */
    private fun set_fail() {
        layoutDefault?.setVisibility(GONE)

        // 圖片
        layoutPic?.setVisibility(GONE)

        // 內容
        layoutNormal?.setVisibility(GONE)
    }

    /** 讀取圖片  */
    private fun loadImage() {
        imgLoaded = true
        object : ASRunner() {
            @SuppressLint("ResourceType")
            public override fun run() {
                imageViewButton?.setVisibility(GONE)
                photoViewPic?.setVisibility(VISIBLE)
                photoViewPic?.setContentDescription(myDescription)
                try {
                    val circularProgressDrawable = CircularProgressDrawable(getContext())
                    circularProgressDrawable.setStrokeWidth(10f)
                    circularProgressDrawable.setCenterRadius(60f)
                    // progress bar color
                    val typedValue = TypedValue()

                    getContext().getTheme()
                        .resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true)
                    circularProgressDrawable.setColorSchemeColors(getContextColor(typedValue.resourceId))
                    // progress bar start
                    circularProgressDrawable.start()

                    if (myImageUrl.isEmpty()) {
                        return
                    }

                    photoViewPic?.setImageDrawable(circularProgressDrawable)

                    Glide.with(this@Thumbnail_ItemView)
                        .load(myImageUrl)
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                object : ASRunner() {
                                    // com.kota.ASFramework.Thread.ASRunner
                                    public override fun run() {
                                        set_fail()
                                    }
                                }.runInMainThread()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable?>?,
                                dataSource: DataSource,
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
                                    if (resource is GifDrawable) bitmap = resource.getFirstFrame()
                                    else bitmap = (resource as BitmapDrawable).getBitmap()
                                    val picHeight = bitmap.getHeight()
                                    val picWidth = bitmap.getWidth()
                                    var targetHeight = viewHeight
                                    var targetWidth = viewWidth

                                    val scaleWidth = targetWidth.toFloat() / picWidth
                                    val scaleHeight = targetHeight.toFloat() / picHeight
                                    var scale = min(scaleWidth, scaleHeight)
                                    if (scale > 1) scale = 1f

                                    val tempHeight = (picHeight * scale).toInt()
                                    targetHeight = min(tempHeight, targetHeight)
                                    photoViewPic?.setMinimumHeight(targetHeight)

                                    val tempWidth = (picWidth * scale).toInt()
                                    targetWidth = min(tempWidth, targetWidth)
                                    photoViewPic?.setMinimumWidth(targetWidth)

                                    if (resource is GifDrawable) {
                                        resource.startFromFirstFrame()
                                        photoViewPic?.setImageDrawable(resource)
                                    } else {
                                        val newBitmap = Bitmap.createScaledBitmap(
                                            bitmap,
                                            targetWidth,
                                            targetHeight,
                                            true
                                        )
                                        photoViewPic?.setImageBitmap(newBitmap)
                                    }
                                } catch (ignored: Exception) {
                                    object : ASRunner() {
                                        // com.kota.ASFramework.Thread.ASRunner
                                        public override fun run() {
                                            set_fail()
                                        }
                                    }.runInMainThread()
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                } catch (ignored: Exception) {
                    object : ASRunner() {
                        // com.kota.ASFramework.Thread.ASRunner
                        public override fun run() {
                            set_fail()
                        }
                    }.runInMainThread()
                }
            }
        }.runInMainThread()
    }

    var openUrlListener: OnClickListener = object : OnClickListener {
        override fun onClick(view: View?) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(myUrl))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            myContext.startActivity(intent)
        }
    }

    var titleListener: OnClickListener = OnClickListener { view: View? ->
        val textView = view as TextView
        if (textView.getMaxLines() == 2) textView.setMaxLines(9)
        else textView.setMaxLines(2)
    }
    var descriptionListener: OnClickListener = OnClickListener { view: View? ->
        val textView = view as TextView
        if (textView.getMaxLines() == 1) textView.setMaxLines(9)
        else textView.setMaxLines(1)
    }

    init {
        val metrics = DisplayMetrics()
        (myContext as Activity).getWindowManager().getDefaultDisplay().getMetrics(metrics)
        viewWidth = metrics.widthPixels
        viewHeight = metrics.heightPixels
        init()
    }

    private fun init() {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.thumbnail,
            this
        )
        mainLayout = findViewById<LinearLayout>(R.id.thumbnail_content_view)
        layoutDefault = mainLayout?.findViewById<LinearLayout>(R.id.thumbnail_default)

        layoutPic = mainLayout?.findViewById<LinearLayout>(R.id.thumbnail_pic)
        photoViewPic = mainLayout?.findViewById<PhotoView>(R.id.thumbnail_image_pic)
        photoViewPic?.setOnClickListener(openUrlListener)
        photoViewPic?.setMaximumScale(20.0f)
        photoViewPic?.setMediumScale(3.0f)

        imageViewButton = mainLayout?.findViewById<Button>(R.id.thumbnail_image_button)
        imageViewButton?.setOnClickListener(OnClickListener { view: View? -> prepare_load_image() })

        layoutNormal = mainLayout?.findViewById<LinearLayout>(R.id.thumbnail_normal)
        titleView = mainLayout?.findViewById<TextView>(R.id.thumbnail_title)
        titleView?.setOnClickListener(titleListener)
        descriptionView = mainLayout?.findViewById<TextView>(R.id.thumbnail_description)
        descriptionView?.setOnClickListener(descriptionListener)
        urlView = mainLayout?.findViewById<TextView>(R.id.thumbnail_url)
    }
}
