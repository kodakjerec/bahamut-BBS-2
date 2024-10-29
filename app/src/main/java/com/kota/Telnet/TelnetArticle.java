package com.kota.Telnet;

import static com.kota.Telnet.Reference.TelnetAnsiCode.getBackAsciiCode;
import static com.kota.Telnet.Reference.TelnetAnsiCode.getTextAsciiCode;

import android.text.SpannableString;

import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.Model.TelnetRow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class TelnetArticle {
    public static final int MINIMUM_REMOVE_QUOTE = 1;
    public static final int NEW = 0;
    public static final int REPLY = 1;
    public String Title = "";
    public String Author = "";
    public String BoardName = "";
    public String DateTime = "";
    public String Nickname = "";
    public String fromIP = "";
    public int Number = 0;
    public int Type = 0; // NEW or REPLY
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

    /** 設定文章標題 */
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

    /** 產生 修改用的文章內容
        有附上ASCII色碼 */
    public String generateEditContent() {
        StringBuilder content_buffer = new StringBuilder();
        byte paintTextColor = TelnetAnsi.getDefaultTextColor();
        byte paintBackColor = TelnetAnsi.getDefaultBackgroundColor();

        int startContentRowIndex = 0;
        // 先找出指定行"時間", 下一行是分隔線, 再下一行是內容
        // 內容如果是空白"", 是發文時系統預設給的, 再往下找一行
        for(int i=0; i< _frame.getRowSize();i++) {
            TelnetRow _row = _frame.getRow(i);
            if (_row.getRawString().contains("時間")) {
                startContentRowIndex = i + 2;
                if (_frame.getRow(startContentRowIndex).getRawString().isEmpty())
                    startContentRowIndex += 1;
                break;
            }
        }

        for (int rowIndex = startContentRowIndex; rowIndex < _frame.getRowSize(); rowIndex++) {
            TelnetRow _row = _frame.getRow(rowIndex);
            String rawString = _row.getRawString();
            if (rawString != null) {
                // 不用換顏色的內容
                if (rawString.matches("※ .*") || rawString.matches("> .*")|| rawString.matches("--.*")) {
                    content_buffer.append(rawString).append("\n");
                } else {
                    // 換顏色
                    SpannableString ss = new SpannableString(rawString);
                    StringBuilder finalString = new StringBuilder();
                    byte[] textColor = _row.getTextColor();
                    byte[] backColor = _row.getBackgroundColor();

                    // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                    boolean needReplaceForeColor = false;
                    paintTextColor = TelnetAnsi.getDefaultTextColor();
                    paintBackColor = TelnetAnsi.getDefaultBackgroundColor();
                    for (int i = 0; i < textColor.length; i++) {
                        if (textColor[i] != paintTextColor || backColor[i] != paintBackColor) {
                            if ((i+1)<=rawString.length()) {
                                needReplaceForeColor = true;
                            }
                            break;
                        }
                    }

                    if (needReplaceForeColor) {
                        boolean isBlink = false;
                        for (int i = 0; i < ss.length(); i++) {
                            finalString.append(ss.charAt(i));

                            // 有附加顏色
                            if (textColor[i] != paintTextColor || backColor[i] != paintBackColor) {
                                String appendString = "*[";

                                if (!isBlink && textColor[i]>=8) {
                                    isBlink = true;
                                    appendString +="1;";
                                } else if (isBlink && textColor[i]<8) {
                                    isBlink = false;
                                    appendString +=";";
                                }

                                // 前景代號去除亮色
                                int noBlinkTextColor = textColor[i];
                                if (noBlinkTextColor >=8)
                                    noBlinkTextColor = noBlinkTextColor-8;
                                // 舊:前景代號去除亮色
                                int preBlinkTextColor = paintTextColor;
                                if (preBlinkTextColor >=8)
                                    preBlinkTextColor = preBlinkTextColor-8;

                                if (noBlinkTextColor != preBlinkTextColor) { // 前景不同
                                    appendString += getTextAsciiCode(noBlinkTextColor);

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
                        // 有替換顏色, 該行最後面加上還原碼
                        finalString.append("*[m");
                    } else {
                        finalString.append(rawString);
                    }
                    content_buffer.append(finalString).append("\n");
                }
            }
        }


        return content_buffer.toString();
    }

    /** 產生 回應用的文章內容
     * */
    public String generateReplyContent() {
        int maximum_quote; // 回應作者的階層, 0-父 1-祖父
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

        // 第一個回覆作者(一定不會被黑名單block)
        if (maximum_quote>-1)
            content_builder.append(String.format("※ 引述《%s (%s)》之銘言：\n", Author, Nickname));

        boolean blockListEnable = UserSettings.getPropertiesBlockListEnable();
        // 逐行上推作者
        for (TelnetArticleItemInfo info : _info) {
            if (!(blockListEnable && UserSettings.isBlockListContains(info.author)) && info.quoteLevel <= maximum_quote) {
                for (int i = 0; i < info.quoteLevel; i++) {
                    content_builder.append("> ");
                }
                content_builder.append(String.format("※ 引述《%s (%s)》之銘言：\n", info.author, info.nickname));
            }
        }
        // 作者內容
        for (TelnetArticleItem item : _main_items) {
            if (!(blockListEnable && UserSettings.isBlockListContains(item.getAuthor())) && item.getQuoteLevel() <= maximum_quote) {
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

    public int getPushSize() {
        return _push.size();
    }
    public TelnetArticlePush getPush(int index) { return _push.get(index); }
}
