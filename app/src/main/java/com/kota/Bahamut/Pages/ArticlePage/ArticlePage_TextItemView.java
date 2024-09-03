package com.kota.Bahamut.Pages.ArticlePage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.Reference.TelnetAnsiCode;
import com.kota.Telnet.TelnetAnsi;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.TelnetUI.DividerView;

import java.util.Vector;

public class ArticlePage_TextItemView extends LinearLayout implements TelnetArticleItemView {
    TextView _author_label = null;
    TextView _content_label = null;
    ViewGroup _content_view = null;
    DividerView _divider_view = null;
    int _quote = 0;

    public ArticlePage_TextItemView(Context context) {
        super(context);
        init();
    }

    public ArticlePage_TextItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.article_page_text_item_view, this);
        _author_label = findViewById(R.id.ArticleTextItemView_Title);
        _content_label = findViewById(R.id.ArticleTextItemView_content);
        _divider_view = findViewById(R.id.ArticleTextItemView_DividerView);
        _content_view = findViewById(R.id.ArticleTextItemView_contentView);
        setBackgroundResource(R.color.transparent);
    }

    public void setAuthor(String author, String nickname) {
        if (_author_label != null) {
            StringBuilder author_buffer = new StringBuilder();
            if (author != null) {
                author_buffer.append(author);
            }
            if (nickname != null && !nickname.isEmpty()) {
                author_buffer.append("(").append(nickname).append(")");
            }
            if (author != null && !author.isEmpty())
                author_buffer.append(" 說:");
            _author_label.setText(author_buffer.toString());
        }
    }

    /** 設定內容 */
    public void setContent(String content, Vector<TelnetRow> rows) {
        if (_content_label != null) {
            // 讓內文對應顏色, 限定使用者自己發文
            if (_quote>0) {
                _content_label.setText(content);
                stringNewUrlSpan(_content_label);
            } else {
                // 塗顏色
                CharSequence colorfulText = stringPaint(rows);
                _content_label.setText(colorfulText);
            }
            // 預覽圖
            stringThumbnail();
        }
    }

    /** 文章加上色彩 */
    private CharSequence stringPaint(Vector<TelnetRow> rows) {
        SpannableStringBuilder[] finalString = new SpannableStringBuilder[rows.size()];
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            TelnetRow row = rows.get(rowIndex);
            row.reloadSpace();
            SpannableStringBuilder ssRawString = new SpannableStringBuilder(row.getRawString());
            if (ssRawString.length() > 0) {
                int startIndex = 0;
                byte[] textColor = row.getTextColor();
                int endIndex = startIndex;
                byte paintTextColor = TelnetAnsi.getDefaultTextColor();
                boolean startCatching = false;
                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                boolean needReplaceTextColor = false;
                for (int i = 0; i < textColor.length; i++) {
                    if (textColor[i] != paintTextColor) {
                        if ((i+1)<=ssRawString.length()) {
                            needReplaceTextColor = true;
                        }
                        break;
                    }
                }
                // 開始替換
                if (needReplaceTextColor) {
                    for (int i = 0; i < ssRawString.length(); i++) {
                        // 開始擷取
                        if (textColor[i] != paintTextColor) {
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
                        if (i == (ssRawString.length() - 1)) {
                            if (startCatching) {
                                startCatching = false;
                                endIndex = i;
                                paintTextColor = textColor[i];
                            }
                        }
                        // 塗顏色
                        if (!startCatching) {
                            if (paintTextColor != TelnetAnsi.getDefaultTextColor()) {
                                ForegroundColorSpan colorSpan = new ForegroundColorSpan(TelnetAnsiCode.getTextColor(paintTextColor));
                                ssRawString.setSpan(colorSpan, startIndex, endIndex+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            startIndex = i;
                            paintTextColor = textColor[i];

                            if (textColor[i] != TelnetAnsi.getDefaultTextColor())
                                startCatching = true;
                        }
                    }
                }
                byte[] backgroundColor = row.getBackgroundColor();
                startIndex =0;
                endIndex = startIndex;
                startCatching = false;
                byte paintBackColor = TelnetAnsi.getDefaultBackgroundColor();
                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                boolean needReplaceBackColor = false;
                for (int i = 0; i < backgroundColor.length; i++) {
                    if (backgroundColor[i] != paintBackColor) {
                        if ((i+1)<=ssRawString.length()) {
                            needReplaceBackColor = true;
                        }
                        break;
                    }
                }
                // 開始替換
                if (needReplaceBackColor) {
                    for (int i = 0; i < ssRawString.length(); i++) {
                        // 開始擷取
                        if (backgroundColor[i] != paintBackColor) {
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
                        if (i == (ssRawString.length() - 1)) {
                            if (startCatching) {
                                startCatching = false;
                                endIndex = i;
                                paintBackColor = backgroundColor[i];
                            }
                        }
                        // 塗顏色
                        if (!startCatching) {
                            if (paintBackColor != TelnetAnsi.getDefaultBackgroundColor()) {
                                BackgroundColorSpan colorSpan = new BackgroundColorSpan(TelnetAnsiCode.getBackgroundColor(paintBackColor));
                                ssRawString.setSpan(colorSpan, startIndex, endIndex+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            startIndex = i;
                            paintBackColor = backgroundColor[i];

                            if (backgroundColor[i] != TelnetAnsi.getDefaultBackgroundColor())
                                startCatching = true;
                        }
                    }
                }
            }
            finalString[rowIndex] = ssRawString.append("\n");
        }
        return TextUtils.concat(finalString);
    }

    /** 客製化連結另開新視窗
     *  替換掉 linkify 原本的連結
     * */
    private void stringNewUrlSpan(TextView target) {
        Linkify.addLinks(target,  Linkify.WEB_URLS);
        CharSequence text = target.getText();
        if (text.length()>0) {
            SpannableString ss = (SpannableString) target.getText();
            URLSpan[] spans = target.getUrls();
            for (URLSpan span : spans) {
                int start = ss.getSpanStart(span);
                int end = ss.getSpanEnd(span);
                ss.removeSpan(span);
                ss.setSpan(new myUrlSpan(span.getURL()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /** 加上預覽圖 */
    @SuppressLint("ResourceAsColor")
    private void stringThumbnail() {
        LinearLayout mainLayout = (LinearLayout) _content_view;

        int originalIndex = mainLayout.indexOfChild(_content_label);
        Linkify.addLinks(_content_label,  Linkify.WEB_URLS);

        if (originalIndex>0) {
            // 使用預覽圖
            if (UserSettings.getLinkAutoShow()) {
                SpannableString originalString = (SpannableString) _content_label.getText();
                URLSpan[] urlSpans = _content_label.getUrls();
                if (urlSpans.length>0) {
                    int previousIndex = 0;
                    for (URLSpan urlSpan : urlSpans) {
                        TextView textView1 = new TextView(getContext());
                        TextView textView2 = new TextView(getContext());

                        // partA
                        int urlSpanEnd = originalString.getSpanEnd(urlSpan);
                        CharSequence partA = originalString.subSequence(previousIndex, urlSpanEnd);
                        textView1.setText(partA);

                        // check error
                        if (urlSpanEnd + 1 <= originalString.length())
                            urlSpanEnd = urlSpanEnd + 1;

                        // partB
                        CharSequence partB = originalString.subSequence(urlSpanEnd, originalString.length());
                        textView2.setText(partB);

                        // 移除原本的文字
                        mainLayout.removeViewAt(originalIndex);
                        // 塞入連結前半段文字, 純文字
                        mainLayout.addView(textView1, originalIndex);
                        String url = urlSpan.getURL();
                        Thumbnail_ItemView thumbnail = new Thumbnail_ItemView(getContext());
                        thumbnail.loadUrl(url);
                        // 塞入截圖
                        mainLayout.addView(thumbnail, originalIndex + 1);
                        // 塞入連結後半段文字, 純文字
                        mainLayout.addView(textView2, originalIndex + 2);

                        previousIndex = urlSpanEnd;
                        originalIndex = mainLayout.indexOfChild(textView2);
                    }

                    // 統一指定屬性
                    for (int i = 0; i < mainLayout.getChildCount(); i++) {
                        View view = mainLayout.getChildAt(i);
                        if (view.getClass().equals(TextView.class)) {
                            TextView textView = (TextView) view;
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            textView.setEnabled(true);
                            textView.setTextIsSelectable(true);
                            textView.setFocusable(true);
                            textView.setLongClickable(true);
                            if (_quote > 0)
                                textView.setTextColor(getContextColor(R.color.article_page_text_item_content1));
                            else
                                textView.setTextColor(getContextColor(R.color.article_page_text_item_content0));
                            stringNewUrlSpan(textView);
                        }
                    }
                }
            } else {
                stringNewUrlSpan(_content_label);
            }
        }
    }

    public void setQuote(int quote) {
        _quote = quote;
        // 之前的引用文章
        if (_quote > 0) {
            _author_label.setTextColor(getContextColor(R.color.article_page_text_item_author1));
            _content_label.setTextColor(getContextColor(R.color.article_page_text_item_content1));
        } else {
            // 使用者回文
            _author_label.setTextColor(getContextColor(R.color.article_page_text_item_author0));
            _content_label.setTextColor(getContextColor(R.color.article_page_text_item_content0));
        }
    }

    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
    }

    public int getType() {
        return ArticlePageItemType.Content;
    }

    public void setDividerHidden(boolean isHidden) {
        if (isHidden) {
            _divider_view.setVisibility(View.GONE);
        } else {
            _divider_view.setVisibility(View.VISIBLE);
        }
    }

    public void setVisible(boolean visible) {
        if (visible) {
            _content_view.setVisibility(View.VISIBLE);
        } else {
            _content_view.setVisibility(View.GONE);
        }
    }
}
