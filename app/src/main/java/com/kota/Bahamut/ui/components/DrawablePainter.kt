package com.kota.Bahamut.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

/**
 * Remembers a [Painter] for a drawable or mipmap resource ID.
 * Unlike [androidx.compose.ui.res.painterResource], this supports non-vector XML drawables
 * such as AdaptiveIcons ([android.graphics.drawable.AdaptiveIconDrawable]), layer-lists, shapes, etc.
 */
@Composable
fun rememberDrawablePainter(@DrawableRes resId: Int): Painter {
    val context = LocalContext.current
    return remember(resId, context) {
        try {
            val drawable = ContextCompat.getDrawable(context, resId)
            if (drawable != null) {
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 200
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 200
                val bitmap = drawable.toBitmap(width = width, height = height)
                BitmapPainter(bitmap.asImageBitmap())
            } else {
                ColorPainter(Color.Transparent)
            }
        } catch (_: Exception) {
            ColorPainter(Color.Transparent)
        }
    }
}
