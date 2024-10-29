package com.kota.Bahamut.Pages.ArticlePage

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.TelnetArticleItemView
import com.kota.Telnet.TelnetArticlePush

class ArticlePagePushItemView(context: Context) : ConstraintLayout(context), TelnetArticleItemView {
    private var txtAuthor: TextView
    private var txtContent: TextView
    private var txtDatetime: TextView
    private var txtFloor: TextView

    init {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.article_page_push_item_view,
            this
        )
        txtAuthor = findViewById(R.id.ArticlePushItemView_Author)
        txtContent = findViewById(R.id.ArticlePushItemView_Content)
        txtDatetime = findViewById(R.id.ArticlePushItemView_Datetime)
        txtFloor = findViewById(R.id.ArticlePushItemView_Floor)
    }

    @SuppressLint("SetTextI18n")
    fun setContent(item: TelnetArticlePush) {
        txtAuthor.text = item.author
        txtContent.text = item.content
        txtDatetime.text = item.date+" "+item.time

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
        val mainLayout = txtContent.parent as LinearLayout
        var originalIndex = mainLayout.indexOfChild(txtContent)

        // 先取得原本的寬度,顏色
        val txtContentBackgroundColor = txtContent.background
        val txtContentMaxWidth = txtContent.maxWidth

        Linkify.addLinks(txtContent, Linkify.WEB_URLS)
        // 使用預覽圖
        if (UserSettings.getLinkAutoShow()) {
            val originalString = txtContent.text as SpannableString
            val urlSpans: Array<URLSpan> = txtContent.urls
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
                        textView.background = txtContentBackgroundColor
                        textView.maxWidth = txtContentMaxWidth
                        stringNewUrlSpan(textView)
                    } else if (view.javaClass == Thumbnail_ItemView::class.java) {
                        val thumbnail = view as Thumbnail_ItemView
                        thumbnail.layoutParams.width = txtContentMaxWidth
                    }
                }
            }
        } else {
            stringNewUrlSpan(txtContent)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setFloor(floor: Int) {
        txtFloor.text = " [ $floor 樓]"
    }

    override fun getType(): Int {
        return ArticlePageItemType.Push
    }
}