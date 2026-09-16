package com.kota.Bahamut.pages.articlePage

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kota.Bahamut.R
import com.kota.Bahamut.service.UserSettings
import com.kota.telnet.TelnetArticleItemView
import com.kota.telnet.TelnetArticlePush

class ArticlePagePushItemView(context: Context) : ConstraintLayout(context), TelnetArticleItemView {
    private var txtAuthor: TextView
    private var txtContent: TextView
    private var txtDatetime: TextView
    private var txtFloor: TextView

    init {
        val density = context.resources.displayMetrics.density
        fun dp(value: Float): Int = (value * density + 0.5f).toInt()

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setPadding(dp(8f), dp(4f), dp(8f), dp(4f))

        txtAuthor = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ArticlePushItemView_Author
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                startToStart = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
            }
            setTextColor(com.kota.Bahamut.service.CommonFunctions.getContextColor(R.color.halfWhite))
        }
        addView(txtAuthor)

        txtFloor = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ArticlePushItemView_Floor
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                startToEnd = R.id.ArticlePushItemView_Author
                topToTop = LayoutParams.PARENT_ID
            }
            setTextColor(com.kota.Bahamut.service.CommonFunctions.getContextColor(R.color.halfWhite))
        }
        addView(txtFloor)

        txtDatetime = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ArticlePushItemView_Datetime
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                endToEnd = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
            }
            setTextColor(com.kota.Bahamut.service.CommonFunctions.getContextColor(R.color.halfWhite))
        }
        addView(txtDatetime)

        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                startToStart = LayoutParams.PARENT_ID
                topToBottom = R.id.ArticlePushItemView_Author
            }
        }

        txtContent = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ArticlePushItemView_Content
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setTextColor(com.kota.Bahamut.service.CommonFunctions.getContextColor(R.color.board_item_follow_other_read))
            setTextIsSelectable(true)
        }
        contentContainer.addView(txtContent)
        addView(contentContainer)
    }

    @SuppressLint("SetTextI18n")
    fun setContent(item: TelnetArticlePush) {
        val theme = com.kota.Bahamut.pages.theme.ThemeStore.getSelectTheme()
        val rgbToInt = com.kota.Bahamut.service.CommonFunctions::rgbToInt
        
        txtAuthor.text = item.author
        txtAuthor.setTextColor(rgbToInt(theme.articlePushAuthorColor))
        
        txtContent.text = item.content
        txtContent.setTextColor(rgbToInt(theme.articlePushContentColor))
        
        txtDatetime.text = item.date+" "+item.time
        txtDatetime.setTextColor(rgbToInt(theme.articlePushAuthorColor))
        
        txtFloor.setTextColor(rgbToInt(theme.articlePushAuthorColor))

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
                ss.setSpan(MyUrlSpan(span.url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /** 加上預覽圖  */
    @SuppressLint("ResourceAsColor")
    private fun stringThumbnail() {
        if (txtContent.parent ==null)
            return
        val mainLayout = txtContent.parent as LinearLayout
        var originalIndex = mainLayout.indexOfChild(txtContent)

        Linkify.addLinks(txtContent, Linkify.WEB_URLS)
        // 使用預覽圖
        if (UserSettings.linkAutoShow) {
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
                    val thumbnail = ThumbnailItemView(context)
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
                        stringNewUrlSpan(textView)
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

    override val type: Int
        get() = ArticlePageItemType.PUSH
}