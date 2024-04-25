package com.kota.Telnet;

import static com.kota.Telnet.Reference.TelnetAnsiCode.getBackAsciiCode;
import static com.kota.Telnet.Reference.TelnetAnsiCode.getTextAsciiCode;

import android.annotation.SuppressLint;
import android.text.SpannableString;

import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.Reference.TelnetAnsiCode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class TelnetArticle {
    public static final int MINIMUM_REMOVE_QUOTE = 1;
    public static final int NORMAL = 0;
    public static final int REPLY = 0;
    public String Author = "";
    public String BoardName = "";
    public String DateTime = "";
    public String Nickname = "";
    public int Number = 0;
    public String Title = "";
    public int Type = 0;
    private String _block_list = null;
    private final Vector<TelnetArticleItem> _extend_items = new Vector<>();
    private TelnetFrame _frame = null;
    private final Vector<TelnetArticleItemInfo> _info = new Vector<>();
    private final Vector<TelnetArticleItem> _items = new Vector<>();
    private final Vector<TelnetArticleItem> _main_items = new Vector<>();
    private final Vector<TelnetArticlePush> _push = new Vector<>();

    public void setFrameData(Vector<TelnetRow> rows) {
        _frame = new TelnetFrame(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            _frame.setRow(i, rows.get(i).clone());
        }
    }

    public TelnetFrame getFrame() {
        return _frame;
    }

    public void addMainItem(TelnetArticleItem aItem) {
        _main_items.add(aItem);
    }

    public void addExtendItem(TelnetArticleItem aItem) {
        _extend_items.add(aItem);
    }

    public void addInfo(TelnetArticleItemInfo aInfo) {
        _info.add(aInfo);
    }

    public int getInfoSize() {
        return _info.size();
    }

    public TelnetArticleItemInfo getInfo(int index) {
        return _info.get(index);
    }

    public void addPush(TelnetArticlePush aPush) {
        _push.add(aPush);
    }

    public void build() {
        for (TelnetArticleItem main_item : _main_items) {
            main_item.build();
        }
        for (TelnetArticleItem extend_item : _extend_items) {
            extend_item.build();
        }
        Vector<TelnetArticleItem> remove_items = new Vector<>();
        for (TelnetArticleItem item : _main_items) {
            if (item.isEmpty()) {
                remove_items.add(item);
            }
        }
        for (TelnetArticleItem removeItem : remove_items) {
            _main_items.remove(removeItem);
        }
        remove_items.clear();
        for (TelnetArticleItem item2 : _extend_items) {
            if (item2.isEmpty()) {
                remove_items.add(item2);
            }
        }
        for (TelnetArticleItem remove_item : remove_items) {
            _extend_items.remove(remove_item);
        }
        remove_items.clear();
        if (_extend_items.size() > 0) {
            _extend_items.lastElement().setType(1);
        }
        _items.clear();
        _items.addAll(_main_items);
        _items.addAll(_extend_items);
    }

    public void clear() {
        Title = "";
        Author = "";
        BoardName = "";
        DateTime = "";
        _main_items.clear();
        _items.clear();
        _push.clear();
        _info.clear();
        _frame = null;
    }

    public String generateReplyTitle() {
        return "Re: " + Title;
    }

    // 設定文章標題
    public String generateEditFormat() {
        StringBuilder content_buffer = new StringBuilder();
        String time_string = _frame.getRow(2).toString().substring(4);
        content_buffer.append("作者: ").append(Author);
        if (Nickname != null && Nickname.length() > 0) {
            content_buffer.append("(").append(Nickname).append(")");
        }
        content_buffer.append(" 看板: ").append(BoardName).append("\n");
        content_buffer.append("標題: %s\n");
        content_buffer.append("時間: ").append(time_string).append("\n");
        content_buffer.append("\n%s");
        return content_buffer.toString();
    }

    public String generateEditTitle() {
        return _frame.getRow(1).toString().substring(4);
    }

    // 產生 修改用的文章內容
    // 有附上ASCII色碼
    public String generateEditContent() {
        StringBuilder content_buffer = new StringBuilder();
        byte paintTextColor = TelnetAnsi.getDefaultTextColor();
        byte paintBackColor = TelnetAnsi.getDefaultBackgroundColor();

        for (int rowIndex = 5; rowIndex < _frame.getRowSize(); rowIndex++) {
            TelnetRow _row = _frame.getRow(rowIndex);
            String contentString = _row.getRawString();
            if (contentString != null) {
                // 不用換顏色的內容
                if (contentString.matches("※ .*") || contentString.matches("> .*")|| contentString.matches("--.*")) {
                    content_buffer.append(contentString).append("\n");
                } else {
                    // 換顏色
                    SpannableString ss = new SpannableString(contentString);
                    StringBuilder finalString = new StringBuilder();
                    byte[] textColor = _row.getTextColor();
                    byte[] backColor = _row.getBackgroundColor();
                    for (int i = 0; i < ss.length(); i++) {
                        finalString.append(ss.charAt(i));

                        // 有附加顏色
                        if (textColor[i] != paintTextColor || backColor[i] != paintBackColor) {
                            String appendString = "*[";

                            if (textColor[i] != paintTextColor) { // 前景不同
                                appendString += getTextAsciiCode(textColor[i]);

                                if (backColor[i] != paintBackColor) { // 背景不同
                                    appendString += ";" + getBackAsciiCode(backColor[i]);
                                }
                            } else if (backColor[i] != paintBackColor) { // 背景不同
                                appendString += getBackAsciiCode(backColor[i]);
                            }
                            appendString += "m";
                            finalString.insert(finalString.length() - 1, appendString);

                            // 下一輪
                            paintTextColor = textColor[i];
                            paintBackColor = backColor[i];
                        }
                    }

                    content_buffer.append(finalString).append("\n");
                }
            }
        }
//        // 全部跑完後還有不同顏色
//        if (paintTextColor != TelnetAnsi.getDefaultTextColor() || paintBackColor != TelnetAnsi.getDefaultBackgroundColor()) {
//            content_buffer.append("*[m");
//        }


        return content_buffer.toString();
    }

    // 產生 回應用的文章內容
    public String generateReplyContent() {
        int maximum_quote;
        StringBuilder content_builder = new StringBuilder();
        Set<Integer> level_buffer = new HashSet<>();
        level_buffer.add(0);
        for (TelnetArticleItemInfo telnetArticleItemInfo : _info) {
            level_buffer.add(telnetArticleItemInfo.quoteLevel);
        }
        Integer[] quote_level_list = level_buffer.toArray(new Integer[level_buffer.size()]);
        Arrays.sort(quote_level_list);
        if (quote_level_list.length < 2) {
            maximum_quote = quote_level_list[quote_level_list.length - 1];
        } else {
            maximum_quote = quote_level_list[1];
        }
        content_builder.append(String.format("※ 引述《%s (%s)》之銘言：", Author, Nickname));
        content_builder.append("\n");
        for (TelnetArticleItemInfo info : _info) {
            if (!isBlocked(info.author) && info.quoteLevel <= maximum_quote) {
                for (int i = 0; i < info.quoteLevel; i++) {
                    content_builder.append("> ");
                }
                content_builder.append(String.format("※ 引述《%s (%s)》之銘言：\n", info.author, info.nickname));
            }
        }
        for (TelnetArticleItem item : _main_items) {
            if (!isBlocked(item.getAuthor()) && item.getQuoteLevel() <= maximum_quote) {
                String[] row_strings = item.getContent().split("\n");
                for (String append : row_strings) {
                    for (int j = 0; j <= item.getQuoteLevel(); j++) {
                        content_builder.append("> ");
                    }
                    content_builder.append(append);
                    content_builder.append("\n");
                }
            }
        }
        return content_builder.toString();
    }

    public int getItemSize() {
        return _items.size();
    }

    public TelnetArticleItem getItem(int index) {
        if (index < 0 || index >= _items.size()) {
            return null;
        }
        return _items.get(index);
    }

    public void setBlockList(String aList) {
        _block_list = aList;
    }

    @SuppressLint({"DefaultLocale"})
    public boolean isBlocked(String name) {
        if (_block_list == null || name == null || !_block_list.contains("," + name.trim().toLowerCase() + ",")) {
            return false;
        }
        return true;
    }

    public String getFullText() {
        if (_frame == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int len = _frame.getRowSize();
        for (int i = 0; i < len; i++) {
            TelnetRow row = _frame.getRow(i);
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(row.getRawString());
        }
        return builder.toString();
    }

    public String getAuthor() {
        return Author;
    }
}
