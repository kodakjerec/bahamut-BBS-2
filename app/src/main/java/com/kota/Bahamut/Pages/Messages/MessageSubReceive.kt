package com.kota.Bahamut.Pages.Messages

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.Pages.ArticlePage.Thumbnail_ItemView
import com.kota.Bahamut.Pages.ArticlePage.myUrlSpan
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextColor
import com.kota.Bahamut.Service.UserSettings
import java.text.SimpleDateFormat
import java.util.Date

class MessageSubReceive(context: Context): RelativeLayout(context) {
    private var txtMessage: TextView
    private var txtReceivedDate: TextView
    init {
        inflate(context, R.layout.message_sub_receive, this)
        txtMessage = findViewById(R.id.Message_Sub_Receive_Content)
        txtReceivedDate = findViewById(R.id.Message_Sub_Receive_Time)
    }

    fun setContent(fromObject: BahaMessage) {
        txtMessage.text = fromObject.message
        // 設定日期格式
        val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm")
        // 將時間戳轉換為 Date 物件
        val date = Date(fromObject.receivedDate)
        txtReceivedDate.text = sdf.format(date).substring(11)
        val wordWidth:Int = (txtReceivedDate.paint.measureText(txtReceivedDate.text.toString())*1.2).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels
        txtMessage.maxWidth = screenWidth - wordWidth

        // 預覽圖
        stringThumbnail()
    }


    /** 客製化連結另開新視窗
     * 替換掉 linkify 原本的連結
     */
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
                ss.setSpan(myUrlSpan(span.url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /** 加上預覽圖  */
    @SuppressLint("ResourceAsColor")
    private fun stringThumbnail() {
        if (txtMessage.parent ==null)
            return
        val mainLayout = txtMessage.parent as LinearLayout
        var originalIndex = mainLayout.indexOfChild(txtMessage)

        // 先取得原本的寬度,顏色
        val txtMessageBackgroundColor = txtMessage.background
        val txtMessageMaxWidth = txtMessage.maxWidth

        Linkify.addLinks(txtMessage, Linkify.WEB_URLS)
        // 使用預覽圖
        if (UserSettings.getLinkAutoShow()) {
            val originalString = txtMessage.text as SpannableString
            val urlSpans: Array<URLSpan> = txtMessage.urls
            if (urlSpans.isNotEmpty()) {
                var previousIndex = 0
                for (urlSpan in urlSpans) {
                    val textView1 = TextView(context)
                    val textView2 = TextView(context)

                    // partA
                    var urlSpanEnd = originalString.getSpanEnd(urlSpan)
                    val partA = originalString.subSequence(previousIndex, urlSpanEnd)
                    textView1.text = partA

                    // check error
                    if (urlSpanEnd + 1 <= originalString.length) urlSpanEnd += 1

                    // partB
                    val partB = originalString.subSequence(urlSpanEnd, originalString.length)
                    textView2.text = partB

                    // 移除原本的文字
                    mainLayout.removeViewAt(originalIndex)
                    // 塞入連結前半段文字, 純文字
                    mainLayout.addView(textView1, originalIndex)
                    val url = urlSpan.url
                    val thumbnail = Thumbnail_ItemView(context)
                    thumbnail.loadUrl(url)
                    // 塞入截圖
                    mainLayout.addView(thumbnail, originalIndex + 1)
                    // 塞入連結後半段文字, 純文字
                    if (textView2.text.isNotEmpty()) {
                        mainLayout.addView(textView2, originalIndex + 2)
                    }
                    previousIndex = urlSpanEnd
                    originalIndex = mainLayout.indexOfChild(textView2)
                }

                // 統一指定屬性
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
                    } else if (view.javaClass == Thumbnail_ItemView::class.java) {
                        val thumbnail = view as Thumbnail_ItemView
                        thumbnail.layoutParams.width = txtMessageMaxWidth
                    }
                }
            }
        } else {
            stringNewUrlSpan(txtMessage)
        }
    }
}