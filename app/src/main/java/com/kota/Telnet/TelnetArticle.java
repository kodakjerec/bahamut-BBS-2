package com.kota.Telnet;

import android.annotation.SuppressLint;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.Model.TelnetRow;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Vector<TelnetArticleItemInfo> _infos = new Vector<>();
    private final Vector<TelnetArticleItem> _items = new Vector<>();
    private final Vector<TelnetArticleItem> _main_items = new Vector<>();
    private final Vector<TelnetArticlePush> _pushs = new Vector<>();

    public void setFrameData(Vector<TelnetRow> rows) {
        this._frame = new TelnetFrame(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            this._frame.setRow(i, rows.get(i).clone());
        }
    }

    public TelnetFrame getFrame() {
        return this._frame;
    }

    public void addMainItem(TelnetArticleItem aItem) {
        this._main_items.add(aItem);
    }

    public void addExtendItem(TelnetArticleItem aItem) {
        this._extend_items.add(aItem);
    }

    public void addInfo(TelnetArticleItemInfo aInfo) {
        this._infos.add(aInfo);
    }

    public int getInfoSize() {
        return this._infos.size();
    }

    public TelnetArticleItemInfo getInfo(int index) {
        return this._infos.get(index);
    }

    public void addPush(TelnetArticlePush aPush) {
        this._pushs.add(aPush);
    }

    public void build() {
        Iterator<TelnetArticleItem> it = this._main_items.iterator();
        while (it.hasNext()) {
            it.next().build();
        }
        Iterator<TelnetArticleItem> it2 = this._extend_items.iterator();
        while (it2.hasNext()) {
            it2.next().build();
        }
        Vector<TelnetArticleItem> remove_items = new Vector<>();
        Iterator<TelnetArticleItem> it3 = this._main_items.iterator();
        while (it3.hasNext()) {
            TelnetArticleItem item = it3.next();
            if (item.isEmpty()) {
                remove_items.add(item);
            }
        }
        Iterator<TelnetArticleItem> it4 = remove_items.iterator();
        while (it4.hasNext()) {
            this._main_items.remove(it4.next());
        }
        remove_items.clear();
        Iterator<TelnetArticleItem> it5 = this._extend_items.iterator();
        while (it5.hasNext()) {
            TelnetArticleItem item2 = it5.next();
            if (item2.isEmpty()) {
                remove_items.add(item2);
            }
        }
        Iterator<TelnetArticleItem> it6 = remove_items.iterator();
        while (it6.hasNext()) {
            this._extend_items.remove(it6.next());
        }
        remove_items.clear();
        if (this._extend_items.size() > 0) {
            this._extend_items.lastElement().setType(1);
        }
        this._items.clear();
        this._items.addAll(this._main_items);
        this._items.addAll(this._extend_items);
    }

    public void clear() {
        this.Title = "";
        this.Author = "";
        this.BoardName = "";
        this.DateTime = "";
        this._main_items.clear();
        this._items.clear();
        this._pushs.clear();
        this._infos.clear();
        this._frame = null;
    }

    public String generateReplyTitle() {
        return "Re: " + this.Title;
    }

    public String generatrEditFormat() {
        StringBuffer content_buffer = new StringBuffer();
        String time_string = this._frame.getRow(2).toString().substring(4);
        content_buffer.append("作者: " + this.Author);
        if (this.Nickname != null && this.Nickname.length() > 0) {
            content_buffer.append("(" + this.Nickname + ")");
        }
        content_buffer.append(" 看板: " + this.BoardName + "\n");
        content_buffer.append("標題: %s\n");
        content_buffer.append("時間: " + time_string + "\n");
        content_buffer.append("\n%s");
        return content_buffer.toString();
    }

    public String generateEditTitle() {
        return this._frame.getRow(1).toString().substring(4);
    }

    public String generateEditContent() {
        StringBuffer content_buffer = new StringBuffer();
        for (int i = 5; i < this._frame.getRowSize(); i++) {
            content_buffer.append(this._frame.getRow(i).getRawString() + "\n");
        }
        return content_buffer.toString();
    }

    public String generateReplyContent() {
        int maximum_quote;
        StringBuilder content_builder = new StringBuilder();
        Set<Integer> level_buffer = new HashSet<>();
        level_buffer.add(0);
        Iterator<TelnetArticleItemInfo> it = this._infos.iterator();
        while (it.hasNext()) {
            level_buffer.add(Integer.valueOf(it.next().quoteLevel));
        }
        Integer[] quote_level_list = (Integer[]) level_buffer.toArray(new Integer[level_buffer.size()]);
        Arrays.sort(quote_level_list);
        if (quote_level_list.length < 2) {
            maximum_quote = quote_level_list[quote_level_list.length - 1].intValue();
        } else {
            maximum_quote = quote_level_list[1].intValue();
        }
        content_builder.append(String.format("※ 引述《%s (%s)》之銘言：", this.Author, this.Nickname));
        content_builder.append("\n");
        Iterator<TelnetArticleItemInfo> it2 = this._infos.iterator();
        while (it2.hasNext()) {
            TelnetArticleItemInfo info = it2.next();
            if (!isBlocked(info.author) && info.quoteLevel <= maximum_quote) {
                for (int i = 0; i < info.quoteLevel; i++) {
                    content_builder.append("> ");
                }
                content_builder.append(String.format("※ 引述《%s (%s)》之銘言：\n", info.author, info.nickname));
            }
        }
        Iterator<TelnetArticleItem> it3 = this._main_items.iterator();
        while (it3.hasNext()) {
            TelnetArticleItem item = it3.next();
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
        return this._items.size();
    }

    public TelnetArticleItem getItem(int index) {
        if (index < 0 || index >= this._items.size()) {
            return null;
        }
        return this._items.get(index);
    }

    public void setBlockList(String aList) {
        this._block_list = aList;
    }

    @SuppressLint({"DefaultLocale"})
    public boolean isBlocked(String name) {
        return this._block_list != null && name != null && this._block_list.contains("," + name.trim().toLowerCase() + ",");
    }

    public String getFullText() {
        if (this._frame == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int len = this._frame.getRowSize();
        for (int i = 0; i < len; i++) {
            TelnetRow row = this._frame.getRow(i);
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(row.getRawString());
        }
        return builder.toString();
    }

    public String[] getUrls() {
        String article_text = getFullText();
        System.out.println("article_text:" + article_text);
        Matcher matcher = Pattern.compile("(ftp://|http://|https://)?([a-zA-Z0-9_-~]+(:[a-zA-Z0-9_-~]+)?@)?([a-zA-Z0-9_-~]+(\\.[a-zA-Z0-9_-~]+){1,})((((/[a-zA-Z0-9_-~]+){0,})?((/[a-zA-Z0-9_-~*]+(\\.[a-zA-Z0-9_-~]+)?(\\?([a-zA-Z0-9_-~]+=([a-zA-Z0-9_-~%#*]+)?)(&[a-zA-Z0-9_-~]+=([a-zA-Z0-9_-~%#*]+)?){0,})?)|/))|(((/[a-zA-Z0-9_-~]+){0,})?((/[a-zA-Z0-9_-~*]+(\\.[a-zA-Z0-9_-~]+)?))))?").matcher(article_text);
        Vector<String> buffer = new Vector<>();
        while (matcher.find()) {
            String url_string = matcher.group();
            System.out.println("url_string:" + url_string);
            buffer.add(url_string);
        }
        return (String[]) buffer.toArray(new String[buffer.size()]);
    }
}
