package com.kota.Bahamut.pages.model

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Spanned
import android.util.AttributeSet


class PostEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr) {
    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste) {
            onInterceptClipDataToPlainText()
        }
        return super.onTextContextMenuItem(id)
    }

    /** 移除剪貼簿內的樣式 */
    private fun onInterceptClipDataToPlainText() {
        val clipboard = context
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null) {
            for (i in 0 until clip.itemCount) {
                val paste: CharSequence?
                // Get an item as text and remove all spans by toString().
                val text = clip.getItemAt(i).coerceToText(context)
                paste = (text as? Spanned)?.toString() ?: text
                if (paste != null) {
                    val newClip = ClipData.newPlainText("", paste)
                    clipboard.setPrimaryClip(newClip)
                }
            }
        }
    }
}