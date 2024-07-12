package com.kota.Telnet.Logic;

import static com.kota.Telnet.TelnetArticle.NEW;
import static com.kota.Telnet.TelnetArticle.REPLY;

import com.kota.Telnet.Model.TelnetModel;
import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetArticleItemInfo;
import com.kota.Telnet.TelnetArticlePage;
import com.kota.Telnet.TelnetArticlePush;

import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Article_Handler {
    TelnetArticle telnetArticle = new TelnetArticle();
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
            _pages.add(page);
        }
    }

    public void loadLastPage(TelnetModel aModel) {
        int start = _pages.size() > 0 ? 1 : 0;
        int end = 23;
        while (start < 23 && aModel.getRow(start).isEmpty()) {
            start++;
        }
        while (end > start && aModel.getRow(end).isEmpty()) {
            end--;
        }
        TelnetArticlePage page = _last_page;
        if (page == null) {
            page = new TelnetArticlePage();
        }
        page.clear();
        for (int i = start; i < end; i++) {
            page.addRow(aModel.getRow(i));
        }
        _last_page = page;
    }

    public void clear() {
        telnetArticle.clear();
        _pages.clear();
        _last_page = null;
    }

    /** 分析每行內容 */
    public void build() {
        telnetArticle.clear();
        Vector<TelnetRow> rows = new Vector<>();
        buildRows(rows);
        trimRows(rows);
        telnetArticle.setFrameData(rows);
        if (loadHeader(rows)) {
            rows.subList(0, 4).clear();
        }
        boolean main_block_did_read = false; // 讀取到主區塊
        boolean end_line_did_read = false; // 是否讀取到最後一行
        TelnetArticleItem processing_item = null;
        String regexEndArticle = "※ Origin: 巴哈姆特<((gamer\\.com\\.tw)|(www\\.gamer\\.com\\.tw)|(bbs\\.gamer\\.com\\.tw)|(BAHAMUT\\.ORG))> ◆ From: ((?<fromIP>.+))";
        for (TelnetRow row : rows) {
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
                telnetArticle.addInfo(item_info);
            } else if (!row_string.matches("※ 修改:.*")) {
                if (row_string.equals("--")) {
                    main_block_did_read = true;
                    processing_item = null;
                } else if (row_string.matches(regexEndArticle)) {
                    end_line_did_read = true;
                    processing_item = null;
                    Pattern pattern = Pattern.compile(regexEndArticle);
                    Matcher matcher = pattern.matcher(row_string);
                    if (matcher.find()) {
                        MatchResult result = matcher.toMatchResult();
                        telnetArticle.fromIP = result.group(matcher.groupCount());
                    }
                } else if (!end_line_did_read || !row_string.matches(".+：.+\\(.+\\)")) {
                    if (processing_item == null || processing_item.getQuoteLevel() != quoteLevel) {
                        processing_item = new TelnetArticleItem();
                        if (main_block_did_read) {
                            telnetArticle.addExtendItem(processing_item);
                        } else {
                            telnetArticle.addMainItem(processing_item);
                        }
                        processing_item.setQuoteLevel(quoteLevel);
                        if (quoteLevel != 0) {
                            int i4 = telnetArticle.getInfoSize() - 1;
                            while (true) {
                                if (i4 < 0) {
                                    break;
                                }
                                TelnetArticleItemInfo item_info2 = telnetArticle.getInfo(i4);
                                if (item_info2.quoteLevel == quoteLevel) {
                                    processing_item.setAuthor(item_info2.author);
                                    processing_item.setNickname(item_info2.nickname);
                                    break;
                                }
                                i4--;
                            }
                        } else {
                            processing_item.setAuthor(telnetArticle.Author);
                            processing_item.setNickname(telnetArticle.Nickname);
                        }
                    }
                    // 其他狀況
                    processing_item.addRow(row);
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
                    telnetArticle.addPush(push);
                }
            }
        }
        telnetArticle.build();
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
        if (row_0.toContentString().contains("作者")) {
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
        }
        telnetArticle.Author = author;
        telnetArticle.Nickname = nickname;

        String boardName = "";
        if (row_0.toContentString().contains("看板"))
            boardName = row_0.getSpaceString(66, 78).trim();
        telnetArticle.BoardName = boardName;

        String title_string = "";
        if (row_1.toContentString().contains("標題")) {
            title_string = row_1.getSpaceString(7, 78).trim();
            if (title_string.startsWith("Re: ")) {
                telnetArticle.Title = title_string.substring(4);
                telnetArticle.Type = REPLY;
            } else {
                telnetArticle.Title = title_string;
                telnetArticle.Type = NEW;
            }
        }

        String dateTime = "";
        if (row_2.toContentString().contains("時間")) {
            dateTime = row_2.getSpaceString(7, 30).trim();
        }
        telnetArticle.DateTime = dateTime;
        // 只要任意一個屬性有值, 就應正常顯示
        if (!author.isEmpty() || !nickname.isEmpty() || !boardName.isEmpty() || !title_string.isEmpty()) {
            return true;
        } else {
            // 被修改過格式不正確
            return false;
        }
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
        synchronized (_pages) {
            if (_pages != null && _pages.size() > 0) {
                int page_count = _pages.size();
                for (int page_index = 0; page_index < page_count; page_index++) {
                    addPage(_pages.get(page_index), rows);
                }
            }
            if (_last_page != null) {
                addPage(_last_page, rows);
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
        return telnetArticle;
    }

    public void newArticle() {
        telnetArticle = new TelnetArticle();
    }
}
