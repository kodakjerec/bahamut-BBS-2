package com.kota.telnet.logic

import com.kota.telnet.model.TelnetModel
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetArticleItemInfo
import com.kota.telnet.TelnetArticlePage
import com.kota.telnet.TelnetArticlePush
import java.util.Vector
import java.util.regex.Pattern

class ArticleHandler {
    var article: TelnetArticle = TelnetArticle()
    var _last_page: TelnetArticlePage? = null
    var _pages: Vector<TelnetArticlePage?>? = Vector<TelnetArticlePage?>()

    fun loadPage(aModel: TelnetModel?) {
        val page_index: Int
        var start_line = 1
        if (aModel != null && (parsePageIndex(aModel.getLastRow()).also { page_index = it }) > 0) {
            val page = TelnetArticlePage()
            if (page_index == 1) {
                start_line = 0
            }
            for (source_index in start_line..22) {
                page.addRow(aModel.getRow(source_index))
            }
            _pages!!.add(page)
        }
    }

    fun loadLastPage(aModel: TelnetModel) {
        var start = if (_pages!!.size > 0) 1 else 0
        var end = 23
        while (start < 23 && aModel.getRow(start).isEmpty()) {
            start++
        }
        while (end > start && aModel.getRow(end).isEmpty()) {
            end--
        }
        var page = _last_page
        if (page == null) {
            page = TelnetArticlePage()
        }
        page.clear()
        for (i in start..<end) {
            page.addRow(aModel.getRow(i))
        }
        _last_page = page
    }

    fun clear() {
        article.clear()
        _pages!!.clear()
        _last_page = null
    }

    /** 分析每行內容  */
    fun build() {
        article.clear()
        val rows = Vector<TelnetRow>()
        buildRows(rows)
        trimRows(rows)
        article.setFrameData(rows)
        if (loadHeader(rows)) {
            rows.subList(0, 4).clear()
        }
        var main_block_did_read = false // 讀取到主區塊
        var end_line_did_read = false // 是否讀取到最後一行
        var processing_item: TelnetArticleItem? = null
        val regexEndArticle =
            "※ Origin: 巴哈姆特<((gamer\\.com\\.tw)|(www\\.gamer\\.com\\.tw)|(bbs\\.gamer\\.com\\.tw)|(BAHAMUT\\.ORG))> ◆ From: ((?<fromIP>.+))"
        for (row in rows) {
            val row_string = row.toString()
            val quoteLevel = row.getQuoteLevel()
            if (row_string.matches("※( *)引述( *)《(.+)( *)(\\((.+)\\))?》之銘言：".toRegex())) {
                var author = ""
                var nickname = ""
                val row_words = row_string.toCharArray()
                var author_start = 0
                var author_end = 0
                var i2 = 0
                while (true) {
                    if (i2 >= row_words.size) {
                        break
                    } else if (row_words[i2].code == 12298) {
                        author_start = i2 + 1
                        break
                    } else {
                        i2++
                    }
                }
                var i3 = author_start
                while (true) {
                    if (i3 >= row_words.size) {
                        break
                    } else if (row_words[i3].code == 12299) {
                        author_end = i3
                        break
                    } else {
                        i3++
                    }
                }
                if (author_end > author_start) {
                    author = row_string.substring(author_start, author_end).trim { it <= ' ' }
                }
                val author_words = author.toCharArray()
                var nickname_start = 0
                var nickname_end = author_words.size - 1
                while (nickname_start < author_words.size && author_words[nickname_start] != '(') {
                    nickname_start++
                }
                while (nickname_end >= 0 && author_words[nickname_end] != ')') {
                    nickname_end--
                }
                if (nickname_end > nickname_start + 1) {
                    nickname = author.substring(nickname_start + 1, nickname_end).trim { it <= ' ' }
                }
                if (nickname.length > 0) {
                    author = author.substring(0, nickname_start)
                }
                val author2 = author.trim { it <= ' ' }
                val item_info = TelnetArticleItemInfo()
                item_info.author = author2
                item_info.nickname = nickname
                item_info.quoteLevel = quoteLevel + 1
                article.addInfo(item_info)
            } else if (!row_string.matches("※ 修改:.*".toRegex())) {
                if (row_string == "--") {
                    main_block_did_read = true
                    processing_item = null
                } else if (row_string.matches(regexEndArticle.toRegex())) {
                    end_line_did_read = true
                    processing_item = null
                    val pattern = Pattern.compile(regexEndArticle)
                    val matcher = pattern.matcher(row_string)
                    if (matcher.find()) {
                        val result = matcher.toMatchResult()
                        article.fromIP = result.group(matcher.groupCount())
                    }
                } else if (!end_line_did_read || !row_string.matches(".+：.+\\(.+\\)".toRegex())) {
                    if (processing_item == null || processing_item.getQuoteLevel() != quoteLevel) {
                        processing_item = TelnetArticleItem()
                        if (main_block_did_read) {
                            article.addExtendItem(processing_item)
                        } else {
                            article.addMainItem(processing_item)
                        }
                        processing_item.setQuoteLevel(quoteLevel)
                        if (quoteLevel != 0) {
                            var i4 = article.getInfoSize() - 1
                            while (true) {
                                if (i4 < 0) {
                                    break
                                }
                                val item_info2 = article.getInfo(i4)
                                if (item_info2.quoteLevel == quoteLevel) {
                                    processing_item.setAuthor(item_info2.author)
                                    processing_item.setNickname(item_info2.nickname)
                                    break
                                }
                                i4--
                            }
                        } else {
                            processing_item.setAuthor(article.author)
                            processing_item.setNickname(article.Nickname)
                        }
                    }
                    // 其他狀況
                    processing_item.addRow(row)
                } else {
                    var author3 = ""
                    var content = ""
                    var datetime = ""
                    var date: String? = ""
                    var time: String? = ""
                    val row_words2 = row_string.toCharArray()
                    var author_end2 = 0
                    var i5 = 0
                    while (true) {
                        if (i5 >= row_words2.size) {
                            break
                        } else if (row_words2[i5].code == 65306) {
                            author_end2 = i5
                            break
                        } else {
                            i5++
                        }
                    }
                    if (author_end2 > 0) {
                        author3 = row_string.substring(0, author_end2).trim { it <= ' ' }
                    }
                    var datetime_start = 0
                    var datetime_end = 0
                    var i6 = row_words2.size - 1
                    while (true) {
                        if (i6 < 0) {
                            break
                        } else if (row_words2[i6] == ')') {
                            datetime_end = i6
                            break
                        } else {
                            i6--
                        }
                    }
                    var i7 = datetime_end - 1
                    while (true) {
                        if (i7 < 0) {
                            break
                        } else if (row_words2[i7] == '(') {
                            datetime_start = i7 + 1
                            break
                        } else {
                            i7--
                        }
                    }
                    if (datetime_end > datetime_start) {
                        datetime = row_string.substring(datetime_start, datetime_end)
                    }
                    val datetime_parts: Array<String?> =
                        datetime.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (datetime_parts.size == 2) {
                        date = datetime_parts[0]
                        time = datetime_parts[1]
                    }
                    val content_start = author_end2 + 1
                    val content_end = datetime_start - 1
                    if (content_end > content_start) {
                        content =
                            row_string.substring(content_start, content_end).trim { it <= ' ' }
                    }
                    val push = TelnetArticlePush()
                    push.author = author3
                    push.content = content
                    push.date = date
                    push.time = time
                    article.addPush(push)
                }
            }
        }
        article.build()
    }

    private fun loadHeader(rows: Vector<TelnetRow>): Boolean {
        if (rows.size <= 3) {
            return false
        }
        val row_0 = rows.get(0)
        val row_1 = rows.get(1)
        val row_2 = rows.get(2)
        val author_string = row_0.getSpaceString(7, 58).trim { it <= ' ' }
        var author = ""
        var nickname = ""
        if (row_0.toContentString().contains("作者")) {
            try {
                val author_words = author_string.toCharArray()
                var author_end = 0
                var i = 0
                while (true) {
                    if (i >= author_words.size) {
                        break
                    } else if (author_words[i] == '(') {
                        author_end = i
                        break
                    } else {
                        i++
                    }
                }
                if (author_end > 0) {
                    author = author_string.substring(0, author_end).trim { it <= ' ' }
                }
                val nickname_start = author_end + 1
                var nickname_end = nickname_start
                var i2 = author_words.size - 1
                while (true) {
                    if (i2 < 0) {
                        break
                    } else if (author_words[i2] == ')') {
                        nickname_end = i2
                        break
                    } else {
                        i2--
                    }
                }
                if (nickname_end > nickname_start) {
                    nickname =
                        author_string.substring(nickname_start, nickname_end).trim { it <= ' ' }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        article.author = author
        article.Nickname = nickname

        var boardName = ""
        if (row_0.toContentString().contains("看板")) boardName =
            row_0.getSpaceString(66, 78).trim { it <= ' ' }
        article.BoardName = boardName

        var title_string = ""
        if (row_1.toContentString().contains("標題")) {
            title_string = row_1.getSpaceString(7, 78).trim { it <= ' ' }
            if (title_string.startsWith("Re: ")) {
                article.Title = title_string.substring(4)
                article.Type = TelnetArticle.REPLY
            } else {
                article.Title = title_string
                article.Type = TelnetArticle.NEW
            }
        }

        var dateTime = ""
        if (row_2.toContentString().contains("時間")) {
            dateTime = row_2.getSpaceString(7, 30).trim { it <= ' ' }
        }
        article.DateTime = dateTime
        // 只要任意一個屬性有值, 就應正常顯示
        if (!author.isEmpty() || !nickname.isEmpty() || !boardName.isEmpty() || !title_string.isEmpty()) {
            return true
        } else {
            // 被修改過格式不正確
            return false
        }
    }

    private fun addRow(row: TelnetRow?, rows: Vector<TelnetRow>) {
        rows.add(row)
    }

    private fun addPage(page: TelnetArticlePage?, rows: Vector<TelnetRow>) {
        if (page != null) {
            for (i in 0..<page.getRowCount()) {
                addRow(page.getRow(i), rows)
            }
        }
    }

    private fun buildRows(rows: Vector<TelnetRow>) {
        synchronized(_pages!!) {
            if (_pages != null && _pages!!.size > 0) {
                val page_count = _pages!!.size
                for (page_index in 0..<page_count) {
                    addPage(_pages!!.get(page_index), rows)
                }
            }
            if (_last_page != null) {
                addPage(_last_page, rows)
            }
        }
    }

    private fun trimRows(rows: Vector<TelnetRow>) {
        for (i in rows.indices) {
            val current_row = rows.get(i)
            if (current_row.data[79].toInt() != 0 && i < rows.size - 1) {
                val test_row = rows.get(i + 1)
                if (test_row.getQuoteSpace() == 0 && test_row.getDataSpace() < current_row.getQuoteSpace()) {
                    current_row.append(test_row)
                    rows.removeAt(i + 1)
                }
            }
        }
    }

    fun parsePageIndex(aRow: TelnetRow): Int {
        var d: Byte
        var index = 0
        var i = 8
        while (i < 14 && (aRow.data[i].also { d = it }) >= 48 && d <= 57) {
            index = (index * 10) + ((d - 48) and 255)
            i++
        }
        return index
    }

    fun newArticle() {
        this.article = TelnetArticle()
    }
}
