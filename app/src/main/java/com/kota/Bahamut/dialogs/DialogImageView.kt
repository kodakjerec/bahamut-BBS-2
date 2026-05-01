package com.kota.Bahamut.dialogs

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.scale
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
import com.kota.asFramework.dialog.ASDialog
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.asFramework.thread.ASCoroutine
import kotlin.math.min

class DialogImageView : ASDialog() {
    private lateinit var photoView: PhotoView
    private var imageUrl: String = ""

    fun setImageUrl(url: String): DialogImageView {
        this.imageUrl = url
        return this
    }

    override fun show() {
        super.show()
        if (imageUrl.isNotEmpty()) {
            loadImage()
        } else {
            dismiss()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadImage() {
        ASCoroutine.ensureMainThread {
            try {
                val circularProgressDrawable = CircularProgressDrawable(context)
                circularProgressDrawable.setStrokeWidth(10f)
                circularProgressDrawable.setCenterRadius(60f)

                // 進度條顏色
                val typedValue = TypedValue()
                context.theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true)
                circularProgressDrawable.setColorSchemeColors(getContextColor(typedValue.resourceId))
                circularProgressDrawable.start()

                // 使用 Glide 載入圖片
                Glide.with(this@DialogImageView.context)
                    .load(imageUrl)
                    .placeholder(circularProgressDrawable)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("DialogImageView", "Image load failed for URL: $imageUrl", e)
                            ASCoroutine.ensureMainThread {
                                dismiss()
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
                                if (resource is GifDrawable) {
                                    bitmap = resource.firstFrame
                                } else {
                                    bitmap = (resource as BitmapDrawable).bitmap
                                }

                                val picHeight = bitmap.height
                                val picWidth = bitmap.width

                                // 固定視窗尺寸 (80% 螢幕寬高)
                                val fixedHeight = (context.resources.displayMetrics.heightPixels * 0.8).toInt()
                                val fixedWidth = (context.resources.displayMetrics.widthPixels * 0.8).toInt()
                                setDialogWidthHeight(photoView)

                                // 計算縮放比例，讓圖片最大化填滿視窗（碰到寬或高為止），維持比例
                                val scale = min(fixedWidth.toFloat() / picWidth, fixedHeight.toFloat() / picHeight)
                                val targetWidth = (picWidth * scale).toInt()
                                val targetHeight = (picHeight * scale).toInt()


                                // 顯示圖片，縮放到 targetWidth/targetHeight
                                if (resource is GifDrawable) {
                                    resource.startFromFirstFrame()
                                    // GIF 直接設置 Drawable，PhotoView 會自動處理縮放
                                    photoView.setImageDrawable(resource)
                                } else {
                                    val newBitmap = bitmap.scale(targetWidth, targetHeight)
                                    photoView.setImageBitmap(newBitmap)
                                }
                            } catch (e: Exception) {
                                Log.e("DialogImageView", "Error processing image", e)
                                ASCoroutine.ensureMainThread {
                                    dismiss()
                                }
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            } catch (e: Exception) {
                Log.e("DialogImageView", "Error loading image", e)
                dismiss()
            }
        }
    }

    override val name: String?
        get() = "DialogImageView"

    init {
        requestWindowFeature(1)
        window?.setBackgroundDrawable(null)

        // 創建主容器 - 透明背景，點擊外圍可關閉
        val mainContainer = FrameLayout(context)
        mainContainer.setBackgroundColor(android.graphics.Color.argb(128, 0, 0, 0))
        mainContainer.setOnClickListener { dismiss() }

        // 創建 PhotoView 容器
        val photoViewContainer = FrameLayout(context)
        photoView = PhotoView(context)
        photoView.maximumScale = 20.0f
        photoView.mediumScale = 3.0f

        val photoViewParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        photoViewParams.gravity = Gravity.CENTER

        photoViewContainer.addView(photoView, photoViewParams)

        // 防止點擊 PhotoView 時關閉
        photoViewContainer.setOnClickListener { }

        val containerParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        mainContainer.addView(photoViewContainer, containerParams)

        setContentView(mainContainer)
        setCancelable(true)
    }
}
