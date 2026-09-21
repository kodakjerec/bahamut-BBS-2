package com.kota.Bahamut.dialogs

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ZoomableImageView
import com.kota.Bahamut.ui.dialogs.BahaDialogManager
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * Compose 架構之圖片全螢幕大圖預覽對話框
 * 支援雙指捏合縮放、三階雙擊放大與拖曳平移，點擊背景黑色區域可直接關閉對話框。
 */
@Composable
fun DialogImageViewContent(
    imageUrl: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    var loadedDrawable by remember(imageUrl) { mutableStateOf<Drawable?>(null) }
    var isLoading by remember(imageUrl) { mutableStateOf(true) }
    var isError by remember(imageUrl) { mutableStateOf(false) }

    LaunchedEffect(imageUrl) {
        if (imageUrl.isEmpty()) {
            onDismissRequest()
            return@LaunchedEffect
        }
        isLoading = true
        isError = false

        Glide.with(context)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("DialogImageView", "Image load failed for URL: $imageUrl", e)
                    isError = true
                    isLoading = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadedDrawable = resource
                    isLoading = false
                    return false
                }
            })
            .submit()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // 全螢幕深色半透明背景，點擊背景直接關閉對話框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = colors.textLink,
                    strokeWidth = 3.dp
                )
            } else if (loadedDrawable != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        ZoomableImageView(ctx).apply {
                            setOnClickListener {
                                onDismissRequest()
                            }
                        }
                    },
                    update = { zoomImageView ->
                        val drawable = loadedDrawable
                        if (drawable is GifDrawable) {
                            drawable.startFromFirstFrame()
                        }
                        zoomImageView.setImageDrawable(drawable)
                    }
                )
            } else if (isError) {
                BahaText(
                    text = "載入圖片失敗，點擊背景關閉",
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 外部輔助類別 (向下相容舊有呼叫方式 `DialogImageView().setImageUrl(url).show()`)
 */
class DialogImageView {
    private var imageUrl: String = ""

    fun setImageUrl(url: String): DialogImageView {
        this.imageUrl = url
        return this
    }

    fun show() {
        if (imageUrl.isNotEmpty()) {
            BahaDialogManager.showImage(imageUrl)
        }
    }

    companion object {
        fun show(imageUrl: String) {
            BahaDialogManager.showImage(imageUrl)
        }
    }
}
