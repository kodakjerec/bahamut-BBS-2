package com.kota.Bahamut.pages.articlePage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
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
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.linkShowOnlyWifi
import com.kota.Bahamut.service.UserSettings.Companion.linkShowThumbnail
import com.kota.asFramework.thread.ASRunner
import java.util.Vector
import kotlin.math.min

class ThumbnailItemView(var myContext: Context) : LinearLayout(myContext) {
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
            UrlDatabase(context).use { urlDatabase ->
                val findUrl: Vector<String> = urlDatabase.getUrl(myUrl)
                urlView?.text = myUrl
                // 已經有URL資料
                myTitle = findUrl[1]
                myDescription = findUrl[2]
                myImageUrl = findUrl[3]
                isPic = findUrl[4] != "0"
                picoUrlChangeStatus(isPic)
            }
        } catch (_: Exception) {
            object : ASRunner() {
                // com.kota.asFramework.thread.ASRunner
                override fun run() {
                    setFail()
                }
            }.runInMainThread()
        }
    }

    /** 判斷是圖片或連結, 改變顯示狀態  */
    fun picoUrlChangeStatus(isPic: Boolean) {
        loadThumbnailImg = linkShowThumbnail
        loadOnlyWifi = linkShowOnlyWifi
        val transportType = TempSettings.transportType

        if (isPic) { // 純圖片
            object : ASRunner() {
                // com.kota.asFramework.thread.ASRunner
                override fun run() {
                    layoutDefault?.visibility = GONE

                    // 圖片
                    layoutPic?.visibility = VISIBLE
                    if (loadThumbnailImg && (!loadOnlyWifi || transportType == 1)) {
                        prepareLoadImage()
                    } else if (myImageUrl == "") {
                        imageViewButton?.visibility = GONE
                    }

                    // 內容
                    layoutNormal?.visibility = GONE
                }
            }.runInMainThread()
        } else { // 內容網址
            object : ASRunner() {
                // com.kota.asFramework.thread.ASRunner
                override fun run() {
                    layoutDefault?.visibility = GONE

                    // 圖片
                    layoutPic?.visibility = VISIBLE
                    if (loadThumbnailImg && (!loadOnlyWifi || transportType == 1)) {
                        prepareLoadImage()
                    } else if (myImageUrl == "") {
                        imageViewButton?.visibility = GONE
                    }

                    // 內容
                    layoutNormal?.visibility = VISIBLE
                    setNormal()
                }
            }.runInMainThread()
        }
    }

    /** 純圖片  */
    fun prepareLoadImage() {
        if (imgLoaded) return

        viewHeight = if (isPic) {
            viewHeight / 2
        } else {
            viewHeight / 4
        }
        photoViewPic?.minimumHeight = viewHeight
        loadImage()
        urlView?.text = myImageUrl
    }

    /** 內容網址  */
    private fun setNormal() {
        if (!myTitle.isEmpty()) {
            titleView?.text = myTitle
            titleView?.visibility = VISIBLE
        }
        if (!myDescription.isEmpty()) {
            descriptionView?.text = myDescription
            descriptionView?.visibility = VISIBLE
        }
        urlView?.text = myUrl
    }

    /** 意外處理  */
    private fun setFail() {
        layoutDefault?.visibility = GONE

        // 圖片
        layoutPic?.visibility = GONE

        // 內容
        layoutNormal?.visibility = GONE
    }

    /** 讀取圖片  */
    private fun loadImage() {
        imgLoaded = true
        object : ASRunner() {
            @SuppressLint("ResourceType")
            override fun run() {
                imageViewButton?.visibility = GONE
                photoViewPic?.visibility = VISIBLE
                photoViewPic?.contentDescription = myDescription
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
                        return
                    }

                    photoViewPic?.setImageDrawable(circularProgressDrawable)

                    Glide.with(this@ThumbnailItemView)
                        .load(myImageUrl)
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                object : ASRunner() {
                                    // com.kota.asFramework.thread.ASRunner
                                    override fun run() {
                                        setFail()
                                    }
                                }.runInMainThread()
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
                                    photoViewPic?.minimumHeight = targetHeight

                                    val tempWidth = (picWidth * scale).toInt()
                                    targetWidth = min(tempWidth, targetWidth)
                                    photoViewPic?.minimumWidth = targetWidth

                                    if (resource is GifDrawable) {
                                        resource.startFromFirstFrame()
                                        photoViewPic?.setImageDrawable(resource)
                                    } else {
                                        val newBitmap = bitmap.scale(targetWidth, targetHeight)
                                        photoViewPic?.setImageBitmap(newBitmap)
                                    }
                                } catch (_: Exception) {
                                    object : ASRunner() {
                                        // com.kota.asFramework.thread.ASRunner
                                        override fun run() {
                                            setFail()
                                        }
                                    }.runInMainThread()
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                } catch (_: Exception) {
                    object : ASRunner() {
                        // com.kota.asFramework.thread.ASRunner
                        override fun run() {
                            setFail()
                        }
                    }.runInMainThread()
                }
            }
        }.runInMainThread()
    }

    var openUrlListener: OnClickListener = OnClickListener {
        val intent = Intent(Intent.ACTION_VIEW, myUrl.toUri())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        myContext.startActivity(intent)
    }

    var titleListener: OnClickListener = OnClickListener { view: View? ->
        val textView = view as TextView
        if (textView.maxLines == 2) textView.maxLines = 9
        else textView.maxLines = 2
    }
    var descriptionListener: OnClickListener = OnClickListener { view: View? ->
        val textView = view as TextView
        if (textView.maxLines == 1) textView.maxLines = 9
        else textView.maxLines = 1
    }

    init {
        val metrics = DisplayMetrics()
        (myContext as Activity).windowManager.defaultDisplay.getMetrics(metrics)
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
        layoutDefault = mainLayout?.findViewById(R.id.thumbnail_default)

        layoutPic = mainLayout?.findViewById(R.id.thumbnail_pic)
        photoViewPic = mainLayout?.findViewById(R.id.thumbnail_image_pic)
        photoViewPic?.setOnClickListener(openUrlListener)
        photoViewPic?.maximumScale = 20.0f
        photoViewPic?.mediumScale = 3.0f

        imageViewButton = mainLayout?.findViewById(R.id.thumbnail_image_button)
        imageViewButton?.setOnClickListener { view: View? -> prepareLoadImage() }

        layoutNormal = mainLayout?.findViewById(R.id.thumbnail_normal)
        titleView = mainLayout?.findViewById(R.id.thumbnail_title)
        titleView?.setOnClickListener(titleListener)
        descriptionView = mainLayout?.findViewById(R.id.thumbnail_description)
        descriptionView?.setOnClickListener(descriptionListener)
        urlView = mainLayout?.findViewById(R.id.thumbnail_url)
    }
}
