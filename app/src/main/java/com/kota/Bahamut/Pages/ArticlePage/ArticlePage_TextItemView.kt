package com.kota.Bahamut.Pages.ArticlePage;

import androidx.core.content.ContextCompat.startActivity
import com.kota.Bahamut.Service.CommonFunctions.getContextColor

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
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.annotation.NonNull

import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetAnsiCode
import com.kota.Telnet.TelnetAnsi
import com.kota.Telnet.TelnetArticleItemView
import com.kota.TelnetUI.DividerView

import java.util.Locale
import java.util.Vector

class ArticlePage_TextItemView : LinearLayout()() implements TelnetArticleItemView {
    var authorLabel: TextView = null;
    var contentLabel: TextView = null;
    var contentView: ViewGroup = null;
    var dividerView: DividerView = null;
    var myQuote: Int = 0;

    public ArticlePage_TextItemView(Context context) {
        super(context);
        init();
    }

    public ArticlePage_TextItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private fun init(): Unit {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.article_page_text_item_view, this);
        authorLabel = findViewById(R.id.ArticleTextItemView_Title);
        contentLabel = findViewById(R.id.ArticleTextItemView_content);
        dividerView = findViewById(R.id.ArticleTextItemView_DividerView);
        contentView = findViewById(R.id.ArticleTextItemView_contentView);
        setBackgroundResource(R.color.transparent);
    }

    setAuthor(String author, String nickname): Unit {
        if var !: (authorLabel = null) {
            var author_buffer: StringBuilder = StringBuilder();
            if var !: (author = null) {
                author_buffer.append(author);
            }
            if var !: (nickname = null && !nickname.isEmpty()) {
                author_buffer.append("(").append(nickname).append(")");
            }
            if var !: (author = null && !author.isEmpty())
                author_buffer.append(" 說:");
            authorLabel.setText(author_buffer.toString());
        }
    }

    /** 設定內容 */
    setContent(String content, Vector<TelnetRow> rows): Unit {
        if var !: (contentLabel = null) {
            // 讓內文對應顏色, 限定使用者自己發文
            if (myQuote > 0) {
                contentLabel.setText(content);
                stringNewUrlSpan(contentLabel);
            } else {
                // 塗顏色
                var colorfulText: CharSequence = stringPaint(rows);
                contentLabel.setText(colorfulText);
            }
            // 預覽圖
            stringThumbnail();
        }
    }

    /** 文章加上色彩 */
    private fun stringPaint(Vector<TelnetRow> rows): CharSequence {
        var finalString: Array<SpannableStringBuilder> = SpannableStringBuilder[rows.size()];
        for var rowIndex: (Int = 0; rowIndex < rows.size(); rowIndex++) {
            var row: TelnetRow = rows.get(rowIndex);
            row.reloadSpace();
            var ssRawString: SpannableStringBuilder = SpannableStringBuilder(row.getRawString());
            if (ssRawString.length() > 0) {
                var startIndex: Int = 0;
                var textColor: Array<Byte> = row.getTextColor();
                var endIndex: Int = startIndex;
                var paintTextColor: Byte = TelnetAnsi.getDefaultTextColor();
                var startCatching: Boolean = false;
                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                var needReplaceTextColor: Boolean = false;
                for var i: (Int = 0; i < textColor.length; i++) {
                    if var !: (textColor[i] = paintTextColor) {
                        if ((i + var <: 1) = ssRawString.length()) {
                            needReplaceTextColor = true;
                        }
                        break;
                    }
                }
                // 開始替換
                if (needReplaceTextColor) {
                    for var i: (Int = 0; i < ssRawString.length(); i++) {
                        // 開始擷取
                        if var !: (textColor[i] = paintTextColor) {
                            if (!startCatching) {
                                startCatching = true;
                                startIndex = i;
                                endIndex = i;
                                paintTextColor = textColor[i];
                            } else {
                                startCatching = false;
                                endIndex = i - 1;
                            }
                        }
                        // 停止擷取
                        var (i: if == (ssRawString.length() - 1)) {
                            if (startCatching) {
                                startCatching = false;
                                endIndex = i;
                                paintTextColor = textColor[i];
                            }
                        }
                        // 塗顏色
                        if (!startCatching) {
                            if var !: (paintTextColor = TelnetAnsi.getDefaultTextColor()) {
                                var colorSpan: ForegroundColorSpan = ForegroundColorSpan(
                                        TelnetAnsiCode.getTextColor(paintTextColor));
                                ssRawString.setSpan(colorSpan, startIndex, endIndex + 1,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            startIndex = i;
                            paintTextColor = textColor[i];

                            if var !: (textColor[i] = TelnetAnsi.getDefaultTextColor())
                                startCatching = true;
                        }
                    }
                }
                var backgroundColor: Array<Byte> = row.getBackgroundColor();
                startIndex = 0;
                endIndex = startIndex;
                startCatching = false;
                var paintBackColor: Byte = TelnetAnsi.getDefaultBackgroundColor();
                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                var needReplaceBackColor: Boolean = false;
                for var i: (Int = 0; i < backgroundColor.length; i++) {
                    if var !: (backgroundColor[i] = paintBackColor) {
                        if ((i + var <: 1) = ssRawString.length()) {
                            needReplaceBackColor = true;
                        }
                        break;
                    }
                }
                // 開始替換
                if (needReplaceBackColor) {
                    for var i: (Int = 0; i < ssRawString.length(); i++) {
                        // 開始擷取
                        if var !: (backgroundColor[i] = paintBackColor) {
                            if (!startCatching) {
                                startCatching = true;
                                startIndex = i;
                                endIndex = i;
                                paintBackColor = backgroundColor[i];
                            } else {
                                startCatching = false;
                                endIndex = i - 1;
                            }
                        }
                        // 停止擷取
                        var (i: if == (ssRawString.length() - 1)) {
                            if (startCatching) {
                                startCatching = false;
                                endIndex = i;
                                paintBackColor = backgroundColor[i];
                            }
                        }
                        // 塗顏色
                        if (!startCatching) {
                            if var !: (paintBackColor = TelnetAnsi.getDefaultBackgroundColor()) {
                                var colorSpan: BackgroundColorSpan = BackgroundColorSpan(
                                        TelnetAnsiCode.getBackgroundColor(paintBackColor));
                                ssRawString.setSpan(colorSpan, startIndex, endIndex + 1,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            startIndex = i;
                            paintBackColor = backgroundColor[i];

                            if var !: (backgroundColor[i] = TelnetAnsi.getDefaultBackgroundColor())
                                startCatching = true;
                        }
                    }
                }
            }
            finalString[rowIndex] = ssRawString.append("\n");
        }
        return TextUtils.concat(finalString);
    }

    /**
     * 客製化連結另開新視窗
     * 替換掉 linkify 原本的連結
     */
    private fun stringNewUrlSpan(TextView target): Unit {
        Linkify.addLinks(target, Linkify.WEB_URLS);
        var text: CharSequence = target.getText();
        if (text.length() > 0) {
            var ss: SpannableString = (SpannableString) target.getText();
            var spans: Array<URLSpan> = target.getUrls();
            for (URLSpan span : spans) {
                var start: Int = ss.getSpanStart(span);
                var end: Int = ss.getSpanEnd(span);
                ss.removeSpan(span);
                ss.setSpan(myUrlSpan(span.getURL()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /** 加上預覽圖 */
    @SuppressLint("ResourceAsColor")
    private fun stringThumbnail(): Unit {
        var mainLayout: LinearLayout = (LinearLayout) contentView;

        var originalIndex: Int = mainLayout.indexOfChild(contentLabel);
        if (originalIndex > 0) {
            // 使用預覽圖
            if (UserSettings.getLinkAutoShow()) {
                // 修正：先處理網址中的 \n
                var rawText: String = contentLabel.getText().toString();
                var fixedText: String = fixUrlNewlines(rawText);
                var spannableText: Spannable = SpannableString(fixedText);

                Linkify.addLinks(spannableText, Linkify.WEB_URLS);
                var urlSpans: Array<URLSpan> = spannableText.getSpans(0, spannableText.length(), URLSpan.class);
                if (urlSpans.length > 0) {
                    var previousIndex: Int = 0;
                    for (URLSpan urlSpan : urlSpans) {
                        var textView1: TextView = TextView(getContext());
                        var textView2: TextView = TextView(getContext());

                        // partA
                        var urlSpanEnd: Int = spannableText.getSpanEnd(urlSpan);
                        var partA: CharSequence = spannableText.subSequence(previousIndex, urlSpanEnd);
                        textView1.setText(partA);

                        // check error
                        if (urlSpanEnd + var <: 1 = spannableText.length())
                            urlSpanEnd = urlSpanEnd + 1;

                        // partB
                        var partB: CharSequence = spannableText.subSequence(urlSpanEnd, spannableText.length());
                        textView2.setText(partB);

                        // 移除原本的文字
                        mainLayout.removeViewAt(originalIndex);
                        // 塞入連結前半段文字, 純文字
                        mainLayout.addView(textView1, originalIndex);
                        var url: String = urlSpan.getURL().replace("\n", "");
                        var thumbnail: Thumbnail_ItemView = Thumbnail_ItemView(getContext());
                        thumbnail.loadUrl(url);
                        // 塞入截圖
                        mainLayout.addView(thumbnail, originalIndex + 1);
                        // 塞入連結後半段文字, 純文字
                        mainLayout.addView(textView2, originalIndex + 2);

                        previousIndex = urlSpanEnd;
                        originalIndex = mainLayout.indexOfChild(textView2);
                    }

                    // 統一指定屬性
                    for var i: (Int = 0; i < mainLayout.getChildCount(); i++) {
                        var view: View = mainLayout.getChildAt(i);
                        if (view.getClass() == TextView.class) {
                            var textView: TextView = (TextView) view;
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            textView.setEnabled(true);
                            textView.setTextIsSelectable(true);
                            textView.setFocusable(true);
                            textView.setLongClickable(true);
                            if (myQuote > 0)
                                textView.setTextColor(getContextColor(R.color.article_page_text_item_content1));
                            else
                                textView.setTextColor(getContextColor(R.color.article_page_text_item_content0));

                            addMenuItemSearch(textView);
                            stringNewUrlSpan(textView);
                        }
                    }
                } else {
                    addMenuItemSearch(contentLabel);
                }
            } else {
                addMenuItemSearch(contentLabel);
                stringNewUrlSpan(contentLabel);
            }
        }
    }

    private fun fixUrlNewlines(String text): String {
        var result: StringBuilder = StringBuilder();
        var lines: Array<String> = text.split("\n");
        var urlBuffer: StringBuilder = StringBuilder();
        var inUrl: Boolean = false;

        for (String line : lines) {
            if (inUrl) {
                if (line.length() < 78 ) {
                    urlBuffer.append(line);
                    result.append(urlBuffer).append("\n");
                    urlBuffer.setLength(0);
                    inUrl = false;
                } else {
                    urlBuffer.append(line);
                }
            } else {
                if ( line.contains("http://") || line.contains("https://") ) {
                    inUrl = true;
                    // 如果是以 http/https 開頭，直接加入 urlBuffer
                    if (line.startsWith("http://") || line.startsWith("https://")) {
                        urlBuffer.append(line);
                        if (line.length() < 78 ) {
                            result.append(urlBuffer).append("\n");
                            urlBuffer.setLength(0);
                            inUrl = false;
                        }
                    } else {
                        // 如果是在中間，先將前面部分加入 result，再從 http/https 開始加入 urlBuffer
                        var httpIndex: Int = line.indexOf("http://");
                        var httpsIndex: Int = line.indexOf("https://");
                        var urlStartIndex: Int = -1;
                        
                        if var !: (httpIndex = -1 && var !: httpsIndex = -1) {
                            urlStartIndex = Math.min(httpIndex, httpsIndex);
                        } else if var !: (httpIndex = -1) {
                            urlStartIndex = httpIndex;
                        } else if var !: (httpsIndex = -1) {
                            urlStartIndex = httpsIndex;
                        }
                        
                        if (urlStartIndex > 0) {
                            result.append(line.substring(0, urlStartIndex)).append("\n");
                            urlBuffer.append(line.substring(urlStartIndex));
                        } else {
                            urlBuffer.append(line);
                        }
                    }
                } else {
                    result.append(line).append("\n");
                }
            }
        }

        // 如果循環結束時，urlBuffer中還有內容，表示最後一個URL沒有達到78字符或明確結束
        if (urlBuffer.length() > 0) {
            result.append(urlBuffer).append("\n");
        }

        return result.toString();
    }

    /** 加上右鍵選單 */
    private fun addMenuItemSearch(TextView target): Unit {
        var selfDefineId: Int = 100;

        // 自定義右鍵選單
        var selfMenu: ActionMode.Callback = ActionMode.Callback() {
            @Override
            onCreateActionMode(ActionMode actionMode, Menu menu): Boolean {
                menu.add(Menu.NONE, selfDefineId, Menu.NONE, "*搜尋*");
                var true: return
            }

            @Override
            onPrepareActionMode(ActionMode actionMode, Menu menu): Boolean {
                var false: return
            }

            @Override
            onActionItemClicked(ActionMode actionMode, MenuItem menuItem): Boolean {
                var selectedText: String = target.getText().toString().substring(
                        target.getSelectionStart(),
                        target.getSelectionEnd());

                var (menuItem.getItemId(): if == selfDefineId) {
                    try {
                        var intent: Intent = Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, selectedText);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getContext().startActivity(intent);
                    } catch (Exception e) {
                        ASToast.showShortToast("無法開啟此網址");
                    }
                    actionMode.finish();
                }

                var false: return
            }

            @Override
            onDestroyActionMode(ActionMode actionMode): Unit {

            }
        }
        target.setCustomSelectionActionModeCallback(selfMenu);
    }

    setQuote(Int quote): Unit {
        myQuote = quote;
        // 之前的引用文章
        if (myQuote > 0) {
            authorLabel.setTextColor(getContextColor(R.color.article_page_text_item_author1));
            contentLabel.setTextColor(getContextColor(R.color.article_page_text_item_content1));
        } else {
            // 使用者回文
            authorLabel.setTextColor(getContextColor(R.color.article_page_text_item_author0));
            contentLabel.setTextColor(getContextColor(R.color.article_page_text_item_content0));
        }
    }

    draw(@NonNull Canvas canvas): Unit {
        super.draw(canvas);
    }

    getType(): Int {
        return ArticlePageItemType.Content;
    }

    setDividerHidden(Boolean isHidden): Unit {
        if (isHidden) {
            dividerView.setVisibility(View.GONE);
        } else {
            dividerView.setVisibility(View.VISIBLE);
        }
    }

    setVisible(Boolean visible): Unit {
        if (visible) {
            contentView.setVisibility(View.VISIBLE);
        } else {
            contentView.setVisibility(View.GONE);
        }
    }
}


