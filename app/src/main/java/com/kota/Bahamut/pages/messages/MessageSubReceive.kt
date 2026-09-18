package com.kota.Bahamut.pages.messages

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.articlePage.MyUrlSpan
import com.kota.Bahamut.pages.articlePage.ThumbnailItemView
import androidx.compose.ui.platform.ComposeView
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.Bahamut.service.UserSettings
import java.text.SimpleDateFormat
import java.util.Date

class MessageSubReceive(context: Context): RelativeLayout(context) {
    private var txtMessage: TextView
    private var txtReceivedDate: TextView
    private var contentParent: LinearLayout
    private var scale = context.resources.displayMetrics.density

    init {
        val margin = (5 * scale).toInt()
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, margin, 0, margin)
        }
        layoutParams = lp

        contentParent = LinearLayout(context).apply {
            id = R.id.Message_Sub_Receive_Content_Parent
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(ALIGN_PARENT_START)
            }
            orientation = LinearLayout.VERTICAL
        }

        txtMessage = TextView(context).apply {
            id = R.id.Message_Sub_Receive_Content
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setTextColor(ContextCompat.getColor(context, R.color.article_page_text_item_content0))
            setBackgroundColor(ContextCompat.getColor(context, R.color.list_page_item_arrow_background))
            setTextIsSelectable(true)
            setPadding((5 * scale).toInt(), (5 * scale).toInt(), (10 * scale).toInt(), (5 * scale).toInt())
            textSize = 16f
        }
        contentParent.addView(txtMessage)

        txtReceivedDate = TextView(context).apply {
            id = R.id.Message_Sub_Receive_Time
            val dateParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(RIGHT_OF, R.id.Message_Sub_Receive_Content_Parent)
                addRule(ALIGN_BOTTOM, R.id.Message_Sub_Receive_Content_Parent)
                leftMargin = (5 * scale).toInt()
            }
            layoutParams = dateParams
            textSize = 11f
            setTextColor(ContextCompat.getColor(context, R.color.halfWhite))
        }

        addView(contentParent)
        addView(txtReceivedDate)
    }

    @SuppressLint("SimpleDateFormat")
    fun setContent(fromObject: BahaMessage) {
        txtMessage.text = fromObject.message
        val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm")
        val date = Date(fromObject.receivedDate)
        txtReceivedDate.text = sdf.format(date).substring(11)
        val wordWidth: Int = (txtReceivedDate.paint.measureText(txtReceivedDate.text.toString()) * 1.2).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels
        txtMessage.maxWidth = screenWidth - wordWidth

        stringThumbnail()
    }

    private fun stringNewUrlSpan(target: TextView) {
        Linkify.addLinks(target, Linkify.WEB_URLS)
        val text = target.text
        if (text.isNotEmpty()) {
            val ss = target.text as SpannableString
            val spans = target.urls
            for (span in spans) {
                val start = ss.getSpanStart(span)
                val end = ss.getSpanEnd(span)
                ss.removeSpan(span)
                ss.setSpan(MyUrlSpan(span.url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun stringThumbnail() {
        if (txtMessage.parent == null)
            return
        val mainLayout = txtMessage.parent as LinearLayout
        var originalIndex = mainLayout.indexOfChild(txtMessage)

        val txtMessageBackgroundColor = txtMessage.background
        val txtMessageMaxWidth = txtMessage.maxWidth

        Linkify.addLinks(txtMessage, Linkify.WEB_URLS)
        if (UserSettings.linkAutoShow) {
            val originalString = txtMessage.text as SpannableString
            val urlSpans: Array<URLSpan> = txtMessage.urls
            if (urlSpans.isNotEmpty()) {
                var previousIndex = 0
                for (urlSpan in urlSpans) {
                    val textView1 = TextView(context)
                    val textView2 = TextView(context)

                    var urlSpanEnd = originalString.getSpanEnd(urlSpan)
                    val partA = originalString.subSequence(previousIndex, urlSpanEnd)
                    textView1.text = partA

                    if (urlSpanEnd + 1 <= originalString.length) urlSpanEnd += 1

                    val partB = originalString.subSequence(urlSpanEnd, originalString.length)
                    textView2.text = partB

                    mainLayout.removeViewAt(originalIndex)
                    mainLayout.addView(textView1, originalIndex)
                    val url = urlSpan.url
                    val thumbnail = ComposeView(context).apply {
                        setBahamutContent {
                            ThumbnailItemView(url = url)
                        }
                    }
                    mainLayout.addView(thumbnail, originalIndex + 1)
                    if (textView2.text.isNotEmpty()) {
                        mainLayout.addView(textView2, originalIndex + 2)
                    }
                    previousIndex = urlSpanEnd
                    originalIndex = mainLayout.indexOfChild(textView2)
                }

                for (i in 0 until mainLayout.childCount) {
                    val view = mainLayout.getChildAt(i)
                    if (view.javaClass == TextView::class.java) {
                        val textView = view as TextView
                        textView.isEnabled = true
                        textView.setTextIsSelectable(true)
                        textView.isFocusable = true
                        textView.isLongClickable = true
                        textView.background = txtMessageBackgroundColor
                        textView.maxWidth = txtMessageMaxWidth
                        stringNewUrlSpan(textView)
                    } else if (view is ComposeView) {
                        view.layoutParams.width = txtMessageMaxWidth
                    }
                }
            }
        } else {
            stringNewUrlSpan(txtMessage)
        }
    }

}