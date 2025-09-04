package com.kota.Bahamut.Pages.ArticlePage;

import static androidx.core.content.ContextCompat.startActivity;
import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.text.Spannable;
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
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.Reference.TelnetAnsiCode;
import com.kota.Telnet.TelnetAnsi;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.TelnetUI.DividerView;

import java.util.Locale;
import java.util.Vector;

public class ArticlePage_TextItemView extends LinearLayout implements TelnetArticleItemView {
    TextView authorLabel = null;
    TextView contentLabel = null;
    ViewGroup contentView = null;
    DividerView dividerView = null;
    int myQuote = 0;

    public ArticlePage_TextItemView(Context context) {
        super(context);
        init();
    }

    public ArticlePage_TextItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.article_page_text_item_view, this);
        authorLabel = findViewById(R.id.ArticleTextItemView_Title);
        contentLabel = findViewById(R.id.ArticleTextItemView_content);
        dividerView = findViewById(R.id.ArticleTextItemView_DividerView);
        contentView = findViewById(R.id.ArticleTextItemView_contentView);
        setBackgroundResource(R.color.transparent);
    }

    public void setAuthor(String author, String nickname) {
        if (authorLabel != null) {
            StringBuilder author_buffer = new StringBuilder();
            if (author != null) {
                author_buffer.append(author);
            }
            if (nickname != null && !nickname.isEmpty()) {
                author_buffer.append("(").append(nickname).append(")");
            }
            if (author != null && !author.isEmpty())
                author_buffer.append(" 說:");
            authorLabel.setText(author_buffer.toString());
        }
    }

    /** 設定內容 */
    public void setContent(String content, Vector<TelnetRow> rows) {
        if (contentLabel != null) {
            // 讓內文對應顏色, 限定使用者自己發文
            if (myQuote > 0) {
                contentLabel.setText(content);
                stringNewUrlSpan(contentLabel);
            } else {
                // 塗顏色
                CharSequence colorfulText = stringPaint(rows);
                contentLabel.setText(colorfulText);
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
                        if ((i + 1) <= ssRawString.length()) {
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
                                ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                                        TelnetAnsiCode.getTextColor(paintTextColor));
                                ssRawString.setSpan(colorSpan, startIndex, endIndex + 1,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            startIndex = i;
                            paintTextColor = textColor[i];

                            if (textColor[i] != TelnetAnsi.getDefaultTextColor())
                                startCatching = true;
                        }
                    }
                }
                byte[] backgroundColor = row.getBackgroundColor();
                startIndex = 0;
                endIndex = startIndex;
                startCatching = false;
                byte paintBackColor = TelnetAnsi.getDefaultBackgroundColor();
                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                boolean needReplaceBackColor = false;
                for (int i = 0; i < backgroundColor.length; i++) {
                    if (backgroundColor[i] != paintBackColor) {
                        if ((i + 1) <= ssRawString.length()) {
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
                                BackgroundColorSpan colorSpan = new BackgroundColorSpan(
                                        TelnetAnsiCode.getBackgroundColor(paintBackColor));
                                ssRawString.setSpan(colorSpan, startIndex, endIndex + 1,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    /**
     * 客製化連結另開新視窗
     * 替換掉 linkify 原本的連結
     */
    private void stringNewUrlSpan(TextView target) {
        Linkify.addLinks(target, Linkify.WEB_URLS);
        CharSequence text = target.getText();
        if (text.length() > 0) {
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
        LinearLayout mainLayout = (LinearLayout) contentView;

        int originalIndex = mainLayout.indexOfChild(contentLabel);
        if (originalIndex > 0) {
            // 使用預覽圖
            if (UserSettings.getLinkAutoShow()) {
                // 修正：先處理網址中的 \n
                String rawText = contentLabel.getText().toString();
                String fixedText = fixUrlNewlines(rawText);
                Spannable spannableText = new SpannableString(fixedText);

                Linkify.addLinks(spannableText, Linkify.WEB_URLS);
                URLSpan[] urlSpans = spannableText.getSpans(0, spannableText.length(), URLSpan.class);
                if (urlSpans.length > 0) {
                    int previousIndex = 0;
                    for (URLSpan urlSpan : urlSpans) {
                        TextView textView1 = new TextView(getContext());
                        TextView textView2 = new TextView(getContext());

                        // partA
                        int urlSpanEnd = spannableText.getSpanEnd(urlSpan);
                        CharSequence partA = spannableText.subSequence(previousIndex, urlSpanEnd);
                        textView1.setText(partA);

                        // check error
                        if (urlSpanEnd + 1 <= spannableText.length())
                            urlSpanEnd = urlSpanEnd + 1;

                        // partB
                        CharSequence partB = spannableText.subSequence(urlSpanEnd, spannableText.length());
                        textView2.setText(partB);

                        // 移除原本的文字
                        mainLayout.removeViewAt(originalIndex);
                        // 塞入連結前半段文字, 純文字
                        mainLayout.addView(textView1, originalIndex);
                        String url = urlSpan.getURL().replace("\n", "");
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

    private String fixUrlNewlines(String text) {
        // 支援 http/https，允許網址中間出現多個 \n
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "https?://[\\w\\-\\.\\/%\\?=&#\\n]+?\\.html");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group().replace("\n", "");
            matcher.appendReplacement(sb, url);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** 加上右鍵選單 */
    private void addMenuItemSearch(TextView target) {
        int selfDefineId = 100;

        // 自定義右鍵選單
        ActionMode.Callback selfMenu = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                menu.add(Menu.NONE, selfDefineId, Menu.NONE, "*搜尋*");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                String selectedText = target.getText().toString().substring(
                        target.getSelectionStart(),
                        target.getSelectionEnd());

                if (menuItem.getItemId() == selfDefineId) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, selectedText);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getContext().startActivity(intent);
                    } catch (Exception e) {
                        ASToast.showShortToast("無法開啟此網址");
                    }
                    actionMode.finish();
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        };
        target.setCustomSelectionActionModeCallback(selfMenu);
    }

    public void setQuote(int quote) {
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

    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
    }

    public int getType() {
        return ArticlePageItemType.Content;
    }

    public void setDividerHidden(boolean isHidden) {
        if (isHidden) {
            dividerView.setVisibility(View.GONE);
        } else {
            dividerView.setVisibility(View.VISIBLE);
        }
    }

    public void setVisible(boolean visible) {
        if (visible) {
            contentView.setVisibility(View.VISIBLE);
        } else {
            contentView.setVisibility(View.GONE);
        }
    }
}
