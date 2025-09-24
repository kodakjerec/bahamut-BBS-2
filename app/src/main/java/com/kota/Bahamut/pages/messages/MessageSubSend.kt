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
import com.kota.asFramework.thread.ASRunner
import com.kota.Bahamut.pages.articlePage.Thumbnail_ItemView
import com.kota.Bahamut.pages.articlePage.myUrlSpan
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.UserSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageSubSend(context: Context): RelativeLayout(context) {
    private var txtMessage: TextView
    private var txtSendDate: TextView
    private var txtSendStatus: TextView
    lateinit var myBahaMessage: BahaMessage
    init {
        inflate(context, R.layout.message_sub_send, this)
        txtMessage = findViewById(R.id.Message_Sub_Sender_Content)
        txtSendDate = findViewById(R.id.Message_Sub_Sender_Time)
        txtSendStatus = findViewById(R.id.Message_Sub_Sender_Status)
    }

    fun setContent(fromObject: BahaMessage) {
        myBahaMessage = fromObject
        txtMessage.text = myBahaMessage.message
        // 設定日期格式
        val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm", Locale.TRADITIONAL_CHINESE)
        // 將時間戳轉換為 Date 物件
        val date = Date(myBahaMessage.receivedDate)
        txtSendDate.text = sdf.format(date).substring(11)
        val wordWidth:Int = (txtSendDate.paint.measureText(txtSendDate.text.toString())*1.2).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels
        txtMessage.maxWidth = screenWidth - wordWidth
        setStatus(myBahaMessage.status)

        // 預覽圖
        stringThumbnail()
    }

    fun setStatus(status: MessageStatus?) {
        val txtStatus = when(status) {
            MessageStatus.Default -> "" // default
            MessageStatus.Success -> "已讀" // 成功
            MessageStatus.CloseBBCall -> "閉關" // 關閉呼叫器
            MessageStatus.Escape -> "離去" // 對方已經離去
            MessageStatus.Offline -> "離線" // 沒上線
            else -> "未知"
        }
        object : ASRunner() {
            override fun run() {
                txtSendStatus.text = txtStatus
            }
        }.runInMainThread()
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
        if (UserSettings.linkAutoShow) {
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
                    textView1.setLinkTextColor(getContextColor(R.color.text_color_link))

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