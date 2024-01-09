package com.kota.Telnet.Logic;

import com.kota.Telnet.Model.TelnetModel;
import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetArticleItemInfo;
import com.kota.Telnet.TelnetArticlePage;
import com.kota.Telnet.TelnetArticlePush;
import java.util.Iterator;
import java.util.Vector;

public class Article_Handler {
    TelnetArticle _article = new TelnetArticle();
    TelnetArticlePage _last_page = null;
    Vector<TelnetArticlePage> _pages = new Vector<>();

    public void loadPage(TelnetModel aModel) {
        int page_index;
        int start_line = 1;
        if (aModel != null && (page_index = parsePageIndex(aModel.getLastRow())) > 0) {
            TelnetArticlePage page = new TelnetArticlePage();
            if (page_index == 1) {
                start_line = 0;
            }
            for (int source_index = start_line; source_index < 23; source_index++) {
                page.addRow(aModel.getRow(source_index));
            }
            this._pages.add(page);
        }
    }

    public void loadLastPage(TelnetModel aModel) {
        int start = this._pages.size() > 0 ? 1 : 0;
        int end = 23;
        while (start < 23 && aModel.getRow(start).isEmpty()) {
            start++;
        }
        while (end > start && aModel.getRow(end).isEmpty()) {
            end--;
        }
        TelnetArticlePage page = this._last_page;
        if (page == null) {
            page = new TelnetArticlePage();
        }
        page.clear();
        for (int i = start; i < end; i++) {
            page.addRow(aModel.getRow(i));
        }
        this._last_page = page;
    }

    public void clear() {
        this._article.clear();
        this._pages.clear();
        this._last_page = null;
    }

    public void build() {
        this._article.clear();
        Vector<TelnetRow> rows = new Vector<>();
        buildRows(rows);
        trimRows(rows);
        this._article.setFrameData(rows);
        if (loadHeader(rows)) {
            for (int i = 0; i < 5; i++) {
                rows.remove(0);
            }
        }
        boolean main_block_did_read = false;
        boolean end_line_did_read = false;
        TelnetArticleItem processing_item = null;
        Iterator<TelnetRow> it = rows.iterator();
        while (it.hasNext()) {
            TelnetRow row = it.next();
            String row_string = row.toString();
            int quoteLevel = row.getQuoteLevel();
            if (row_string.matches("※( *)引述( *)《(.+)( *)(\\((.+)\\))?》之銘言：")) {
                String author = "";
                String nickname = "";
                char[] row_words = row_string.toCharArray();
                int author_start = 0;
                int author_end = 0;
                int i2 = 0;
                while (true) {
                    if (i2 >= row_words.length) {
                        break;
                    } else if (row_words[i2] == 12298) {
                        author_start = i2 + 1;
                        break;
                    } else {
                        i2++;
                    }
                }
                int i3 = author_start;
                while (true) {
                    if (i3 >= row_words.length) {
                        break;
                    } else if (row_words[i3] == 12299) {
                        author_end = i3;
                        break;
                    } else {
                        i3++;
                    }
                }
                if (author_end > author_start) {
                    author = row_string.substring(author_start, author_end).trim();
                }
                char[] author_words = author.toCharArray();
                int nickname_start = 0;
                int nickname_end = author_words.length - 1;
                while (nickname_start < author_words.length && author_words[nickname_start] != '(') {
                    nickname_start++;
                }
                while (nickname_end >= 0 && author_words[nickname_end] != ')') {
                    nickname_end--;
                }
                if (nickname_end > nickname_start + 1) {
                    nickname = author.substring(nickname_start + 1, nickname_end).trim();
                }
                if (nickname.length() > 0) {
                    author = author.substring(0, nickname_start);
                }
                String author2 = author.trim();
                TelnetArticleItemInfo item_info = new TelnetArticleItemInfo();
                item_info.author = author2;
                item_info.nickname = nickname;
                item_info.quoteLevel = quoteLevel + 1;
                this._article.addInfo(item_info);
            } else if (!row_string.matches("※ 修改:.*")) {
                if (row_string.equals("--")) {
                    main_block_did_read = true;
                    processing_item = null;
                } else if (row_string.matches("※ Origin: 巴哈姆特<((gamer\\.com\\.tw)|(www\\.gamer\\.com\\.tw)|(bbs\\.gamer\\.com\\.tw)|(BAHAMUT\\.ORG))> ◆ From: (.+)")) {
                    end_line_did_read = true;
                    processing_item = null;
                } else if (!end_line_did_read || !row_string.matches(".+：.+\\(.+\\)")) {
                    if (processing_item == null || processing_item.getQuoteLevel() != quoteLevel) {
                        processing_item = new TelnetArticleItem();
                        if (main_block_did_read) {
                            this._article.addExtendItem(processing_item);
                        } else {
                            this._article.addMainItem(processing_item);
                        }
                        processing_item.setQuoteLevel(quoteLevel);
                        if (quoteLevel != 0) {
                            int i4 = this._article.getInfoSize() - 1;
                            while (true) {
                                if (i4 < 0) {
                                    break;
                                }
                                TelnetArticleItemInfo item_info2 = this._article.getInfo(i4);
                                if (item_info2.quoteLevel == quoteLevel) {
                                    processing_item.setAuthor(item_info2.author);
                                    processing_item.setNickname(item_info2.nickname);
                                    break;
                                }
                                i4--;
                            }
                        } else {
                            processing_item.setAuthor(this._article.Author);
                            processing_item.setNickname(this._article.Nickname);
                        }
                    }
                    if (processing_item != null) {
                        processing_item.addRow(row);
                    }
                } else {
                    String author3 = "";
                    String content = "";
                    String datetime = "";
                    String date = "";
                    String time = "";
                    char[] row_words2 = row_string.toCharArray();
                    int author_end2 = 0;
                    int i5 = 0;
                    while (true) {
                        if (i5 >= row_words2.length) {
                            break;
                        } else if (row_words2[i5] == 65306) {
                            author_end2 = i5;
                            break;
                        } else {
                            i5++;
                        }
                    }
                    if (author_end2 > 0) {
                        author3 = row_string.substring(0, author_end2).trim();
                    }
                    int datetime_start = 0;
                    int datetime_end = 0;
                    int i6 = row_words2.length - 1;
                    while (true) {
                        if (i6 < 0) {
                            break;
                        } else if (row_words2[i6] == ')') {
                            datetime_end = i6;
                            break;
                        } else {
                            i6--;
                        }
                    }
                    int i7 = datetime_end - 1;
                    while (true) {
                        if (i7 < 0) {
                            break;
                        } else if (row_words2[i7] == '(') {
                            datetime_start = i7 + 1;
                            break;
                        } else {
                            i7--;
                        }
                    }
                    if (datetime_end > datetime_start) {
                        datetime = row_string.substring(datetime_start, datetime_end);
                    }
                    String[] datetime_parts = datetime.split(" +");
                    if (datetime_parts.length == 2) {
                        date = datetime_parts[0];
                        time = datetime_parts[1];
                    }
                    int content_start = author_end2 + 1;
                    int content_end = datetime_start - 1;
                    if (content_end > content_start) {
                        content = row_string.substring(content_start, content_end).trim();
                    }
                    TelnetArticlePush push = new TelnetArticlePush();
                    push.author = author3;
                    push.content = content;
                    push.date = date;
                    push.time = time;
                    this._article.addPush(push);
                }
            }
        }
        this._article.build();
    }

    private boolean loadHeader(Vector<TelnetRow> rows) {
        if (rows.size() <= 3) {
            return false;
        }
        TelnetRow row_0 = rows.get(0);
        TelnetRow row_1 = rows.get(1);
        TelnetRow row_2 = rows.get(2);
        String author_string = row_0.getSpaceString(7, 58).trim();
        String author = "";
        String nickname = "";
        try {
            char[] author_words = author_string.toCharArray();
            int author_end = 0;
            int i = 0;
            while (true) {
                if (i >= author_words.length) {
                    break;
                } else if (author_words[i] == '(') {
                    author_end = i;
                    break;
                } else {
                    i++;
                }
            }
            if (author_end > 0) {
                author = author_string.substring(0, author_end).trim();
            }
            int nickname_start = author_end + 1;
            int nickname_end = nickname_start;
            int i2 = author_words.length - 1;
            while (true) {
                if (i2 < 0) {
                    break;
                } else if (author_words[i2] == ')') {
                    nickname_end = i2;
                    break;
                } else {
                    i2--;
                }
            }
            if (nickname_end > nickname_start) {
                nickname = author_string.substring(nickname_start, nickname_end).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this._article.Author = author;
        this._article.Nickname = nickname;
        this._article.BoardName = row_0.getSpaceString(66, 78).trim();
        String title_string = row_1.getSpaceString(7, 78).trim();
        if (title_string.startsWith("Re: ")) {
            this._article.Title = title_string.substring(4);
            this._article.Type = 0;
        } else {
            this._article.Title = title_string;
            this._article.Type = 0;
        }
        this._article.DateTime = row_2.getSpaceString(7, 30).trim();
        return true;
    }

    private void addRow(TelnetRow row, Vector<TelnetRow> rows) {
        rows.add(row);
    }

    private void addPage(TelnetArticlePage page, Vector<TelnetRow> rows) {
        if (page != null) {
            for (int i = 0; i < page.getRowCount(); i++) {
                addRow(page.getRow(i), rows);
            }
        }
    }

    private void buildRows(Vector<TelnetRow> rows) {
        synchronized (this._pages) {
            if (this._pages != null && this._pages.size() > 0) {
                int page_count = this._pages.size();
                for (int page_index = 0; page_index < page_count; page_index++) {
                    addPage(this._pages.get(page_index), rows);
                }
            }
            if (this._last_page != null) {
                addPage(this._last_page, rows);
            }
        }
    }

    private void trimRows(Vector<TelnetRow> rows) {
        for (int i = 0; i < rows.size(); i++) {
            TelnetRow current_row = rows.get(i);
            if (current_row.data[79] != 0 && i < rows.size() - 1) {
                TelnetRow test_row = rows.get(i + 1);
                if (test_row.getQuoteSpace() == 0 && test_row.getDataSpace() < current_row.getQuoteSpace()) {
                    current_row.append(test_row);
                    rows.remove(i + 1);
                }
            }
        }
    }

    public int parsePageIndex(TelnetRow aRow) {
        byte d;
        int index = 0;
        int i = 8;
        while (i < 14 && (d = aRow.data[i]) >= 48 && d <= 57) {
            index = (index * 10) + ((d - 48) & 255);
            i++;
        }
        return index;
    }

    public TelnetArticle getArticle() {
        return this._article;
    }

    public void newArticle() {
        this._article = new TelnetArticle();
    }
}
