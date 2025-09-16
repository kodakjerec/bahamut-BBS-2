package com.kota.Bahamut.pages.articlePage

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.UserSettings.Companion.linkAutoShow
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetAnsiCode.getBackgroundColor
import com.kota.telnet.reference.TelnetAnsiCode.getTextColor
import com.kota.telnet.TelnetAnsi
import com.kota.telnet.TelnetArticleItemView
import com.kota.telnetUI.DividerView
import java.util.Vector
import kotlin.math.min

class ArticlePage_TextItemView : LinearLayout, TelnetArticleItemView {
    var authorLabel: TextView? = null
    var contentLabel: TextView? = null
    var contentView: ViewGroup? = null
    var dividerView: DividerView? = null
    var myQuote: Int = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.article_page_text_item_view, this)
        authorLabel = findViewById<TextView?>(R.id.ArticleTextItemView_Title)
        contentLabel = findViewById<TextView?>(R.id.ArticleTextItemView_content)
        dividerView = findViewById<DividerView?>(R.id.ArticleTextItemView_DividerView)
        contentView = findViewById<ViewGroup?>(R.id.ArticleTextItemView_contentView)
        setBackgroundResource(R.color.transparent)
    }

    fun setAuthor(author: String?, nickname: String?) {
        if (authorLabel != null) {
            val author_buffer = StringBuilder()
            if (author != null) {
                author_buffer.append(author)
            }
            if (nickname != null && !nickname.isEmpty()) {
                author_buffer.append("(").append(nickname).append(")")
            }
            if (author != null && !author.isEmpty()) author_buffer.append(" 說:")
            authorLabel!!.setText(author_buffer.toString())
        }
    }

    /** 設定內容  */
    fun setContent(content: String?, rows: Vector<TelnetRow>) {
        if (contentLabel != null) {
            // 讓內文對應顏色, 限定使用者自己發文
            if (myQuote > 0) {
                contentLabel!!.setText(content)
                stringNewUrlSpan(contentLabel!!)
            } else {
                // 塗顏色
                val colorfulText = stringPaint(rows)
                contentLabel!!.setText(colorfulText)
            }
            // 預覽圖
            stringThumbnail()
        }
    }

    /** 文章加上色彩  */
    private fun stringPaint(rows: Vector<TelnetRow>): CharSequence? {
        val finalString = arrayOfNulls<SpannableStringBuilder>(rows.size)
        for (rowIndex in rows.indices) {
            val row = rows.get(rowIndex)
            row.reloadSpace()
            val ssRawString = SpannableStringBuilder(row.rawString)
            if (ssRawString.length > 0) {
                var startIndex = 0
                val textColor: ByteArray = row.getTextColor()!!
                var endIndex = startIndex
                var paintTextColor: Byte = TelnetAnsi.getDefaultTextColor()
                var startCatching = false
                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                var needReplaceTextColor = false
                for (i in textColor.indices) {
                    if (textColor[i] != paintTextColor) {
                        if ((i + 1) <= ssRawString.length) {
                            needReplaceTextColor = true
                        }
                        break
                    }
                }
                // 開始替換
                if (needReplaceTextColor) {
                    for (i in 0..<ssRawString.length) {
                        // 開始擷取
                        if (textColor[i] != paintTextColor) {
                            if (!startCatching) {
                                startCatching = true
                                startIndex = i
                                endIndex = i
                                paintTextColor = textColor[i]
                            } else {
                                startCatching = false
                                endIndex = i - 1
                            }
                        }
                        // 停止擷取
                        if (i == (ssRawString.length - 1)) {
                            if (startCatching) {
                                startCatching = false
                                endIndex = i
                                paintTextColor = textColor[i]
                            }
                        }
                        // 塗顏色
                        if (!startCatching) {
                            if (paintTextColor != TelnetAnsi.getDefaultTextColor()) {
                                val colorSpan = ForegroundColorSpan(
                                    getTextColor(paintTextColor)
                                )
                                ssRawString.setSpan(
                                    colorSpan, startIndex, endIndex + 1,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }

                            startIndex = i
                            paintTextColor = textColor[i]

                            if (textColor[i] != TelnetAnsi.getDefaultTextColor()) startCatching =
                                true
                        }
                    }
                }
                val backgroundColor = row.getBackgroundColor()
                startIndex = 0
                endIndex = startIndex
                startCatching = false
                var paintBackColor: Byte = TelnetAnsi.getDefaultBackgroundColor()
                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                var needReplaceBackColor = false
                for (i in backgroundColor!!.indices) {
                    if (backgroundColor[i] != paintBackColor) {
                        if ((i + 1) <= ssRawString.length) {
                            needReplaceBackColor = true
                        }
                        break
                    }
                }
                // 開始替換
                if (needReplaceBackColor) {
                    for (i in 0..<ssRawString.length) {
                        // 開始擷取
                        if (backgroundColor[i] != paintBackColor) {
                            if (!startCatching) {
                                startCatching = true
                                startIndex = i
                                endIndex = i
                                paintBackColor = backgroundColor[i]
                            } else {
                                startCatching = false
                                endIndex = i - 1
                            }
                        }
                        // 停止擷取
                        if (i == (ssRawString.length - 1)) {
                            if (startCatching) {
                                startCatching = false
                                endIndex = i
                                paintBackColor = backgroundColor[i]
                            }
                        }
                        // 塗顏色
                        if (!startCatching) {
                            if (paintBackColor != TelnetAnsi.getDefaultBackgroundColor()) {
                                val colorSpan = BackgroundColorSpan(
                                    getBackgroundColor(paintBackColor)
                                )
                                ssRawString.setSpan(
                                    colorSpan, startIndex, endIndex + 1,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }

                            startIndex = i
                            paintBackColor = backgroundColor[i]

                            if (backgroundColor[i] != TelnetAnsi.getDefaultBackgroundColor()) startCatching =
                                true
                        }
                    }
                }
            }
            finalString[rowIndex] = ssRawString.append("\n")
        }
        return TextUtils.concat(*finalString)
    }

    /**
     * 客製化連結另開新視窗
     * 替換掉 linkify 原本的連結
     */
    private fun stringNewUrlSpan(target: TextView) {
        Linkify.addLinks(target, Linkify.WEB_URLS)
        val text = target.getText()
        if (text.length > 0) {
            val ss = target.getText() as SpannableString
            val spans = target.getUrls()
            for (span in spans) {
                val start = ss.getSpanStart(span)
                val end = ss.getSpanEnd(span)
                ss.removeSpan(span)
                ss.setSpan(myUrlSpan(span.getURL()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /** 加上預覽圖  */
    @SuppressLint("ResourceAsColor")
    private fun stringThumbnail() {
        val mainLayout = contentView as LinearLayout

        var originalIndex = mainLayout.indexOfChild(contentLabel)
        if (originalIndex > 0) {
            // 使用預覽圖
            if (linkAutoShow) {
                // 修正：先處理網址中的 \n
                val rawText = contentLabel!!.getText().toString()
                val fixedText = fixUrlNewlines(rawText)
                val spannableText: Spannable = SpannableString(fixedText)

                Linkify.addLinks(spannableText, Linkify.WEB_URLS)
                val urlSpans =
                    spannableText.getSpans<URLSpan?>(0, spannableText.length, URLSpan::class.java)
                if (urlSpans.size > 0) {
                    var previousIndex = 0
                    for (urlSpan in urlSpans) {
                        val textView1 = TextView(getContext())
                        val textView2 = TextView(getContext())

                        // partA
                        var urlSpanEnd = spannableText.getSpanEnd(urlSpan)
                        val partA = spannableText.subSequence(previousIndex, urlSpanEnd)
                        textView1.setText(partA)

                        // check error
                        if (urlSpanEnd + 1 <= spannableText.length) urlSpanEnd = urlSpanEnd + 1

                        // partB
                        val partB = spannableText.subSequence(urlSpanEnd, spannableText.length)
                        textView2.setText(partB)

                        // 移除原本的文字
                        mainLayout.removeViewAt(originalIndex)
                        // 塞入連結前半段文字, 純文字
                        mainLayout.addView(textView1, originalIndex)
                        val url = urlSpan.getURL().replace("\n", "")
                        val thumbnail = Thumbnail_ItemView(getContext())
                        thumbnail.loadUrl(url)
                        // 塞入截圖
                        mainLayout.addView(thumbnail, originalIndex + 1)
                        // 塞入連結後半段文字, 純文字
                        mainLayout.addView(textView2, originalIndex + 2)

                        previousIndex = urlSpanEnd
                        originalIndex = mainLayout.indexOfChild(textView2)
                    }

                    // 統一指定屬性
                    for (i in 0..<mainLayout.getChildCount()) {
                        val view = mainLayout.getChildAt(i)
                        if (view.javaClass == TextView::class.java) {
                            val textView = view as TextView
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                            textView.setEnabled(true)
                            textView.setTextIsSelectable(true)
                            textView.setFocusable(true)
                            textView.setLongClickable(true)
                            if (myQuote > 0) textView.setTextColor(getContextColor(R.color.article_page_text_item_content1))
                            else textView.setTextColor(getContextColor(R.color.article_page_text_item_content0))

                            addMenuItemSearch(textView)
                            stringNewUrlSpan(textView)
                        }
                    }
                } else {
                    addMenuItemSearch(contentLabel!!)
                }
            } else {
                addMenuItemSearch(contentLabel!!)
                stringNewUrlSpan(contentLabel!!)
            }
        }
    }

    private fun fixUrlNewlines(text: String): String {
        val result = StringBuilder()
        val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val urlBuffer = StringBuilder()
        var inUrl = false

        for (line in lines) {
            if (inUrl) {
                if (line.length < 78) {
                    urlBuffer.append(line)
                    result.append(urlBuffer).append("\n")
                    urlBuffer.setLength(0)
                    inUrl = false
                } else {
                    urlBuffer.append(line)
                }
            } else {
                if (line.contains("http://") || line.contains("https://")) {
                    inUrl = true
                    // 如果是以 http/https 開頭，直接加入 urlBuffer
                    if (line.startsWith("http://") || line.startsWith("https://")) {
                        urlBuffer.append(line)
                        if (line.length < 78) {
                            result.append(urlBuffer).append("\n")
                            urlBuffer.setLength(0)
                            inUrl = false
                        }
                    } else {
                        // 如果是在中間，先將前面部分加入 result，再從 http/https 開始加入 urlBuffer
                        val httpIndex = line.indexOf("http://")
                        val httpsIndex = line.indexOf("https://")
                        var urlStartIndex = -1

                        if (httpIndex != -1 && httpsIndex != -1) {
                            urlStartIndex = min(httpIndex, httpsIndex)
                        } else if (httpIndex != -1) {
                            urlStartIndex = httpIndex
                        } else if (httpsIndex != -1) {
                            urlStartIndex = httpsIndex
                        }

                        if (urlStartIndex > 0) {
                            result.append(line.substring(0, urlStartIndex)).append("\n")
                            urlBuffer.append(line.substring(urlStartIndex))
                        } else {
                            urlBuffer.append(line)
                        }
                    }
                } else {
                    result.append(line).append("\n")
                }
            }
        }

        // 如果循環結束時，urlBuffer中還有內容，表示最後一個URL沒有達到78字符或明確結束
        if (urlBuffer.length > 0) {
            result.append(urlBuffer).append("\n")
        }

        return result.toString()
    }

    /** 加上右鍵選單  */
    private fun addMenuItemSearch(target: TextView) {
        val selfDefineId = 100

        // 自定義右鍵選單
        val selfMenu: ActionMode.Callback = object : ActionMode.Callback {
            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu): Boolean {
                menu.add(Menu.NONE, selfDefineId, Menu.NONE, "*搜尋*")
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
                val selectedText = target.getText().toString().substring(
                    target.getSelectionStart(),
                    target.getSelectionEnd()
                )

                if (menuItem.getItemId() == selfDefineId) {
                    try {
                        val intent = Intent(Intent.ACTION_WEB_SEARCH)
                        intent.putExtra(SearchManager.QUERY, selectedText)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        getContext().startActivity(intent)
                    } catch (e: Exception) {
                        showShortToast("無法開啟此網址")
                    }
                    actionMode.finish()
                }

                return false
            }

            override fun onDestroyActionMode(actionMode: ActionMode?) {
            }
        }
        target.setCustomSelectionActionModeCallback(selfMenu)
    }

    fun setQuote(quote: Int) {
        myQuote = quote
        // 之前的引用文章
        if (myQuote > 0) {
            authorLabel!!.setTextColor(getContextColor(R.color.article_page_text_item_author1))
            contentLabel!!.setTextColor(getContextColor(R.color.article_page_text_item_content1))
        } else {
            // 使用者回文
            authorLabel!!.setTextColor(getContextColor(R.color.article_page_text_item_author0))
            contentLabel!!.setTextColor(getContextColor(R.color.article_page_text_item_content0))
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    val type: Int
        get() = ArticlePageItemType.Companion.Content

    fun setDividerHidden(isHidden: Boolean) {
        if (isHidden) {
            dividerView!!.setVisibility(GONE)
        } else {
            dividerView!!.setVisibility(VISIBLE)
        }
    }

    fun setVisible(visible: Boolean) {
        if (visible) {
            contentView!!.setVisibility(VISIBLE)
        } else {
            contentView!!.setVisibility(GONE)
        }
    }
}
