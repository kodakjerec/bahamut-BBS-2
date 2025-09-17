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
    var articlePage: TelnetArticlePage? = null
    var pages: Vector<TelnetArticlePage?>? = Vector<TelnetArticlePage?>()

    fun loadPage(aModel: TelnetModel?) {
        var pageIndex: Int = -1
        var startLine = 1
        if (aModel != null && (parsePageIndex(aModel.lastRow).also { pageIndex = it }) > 0) {
            val page = TelnetArticlePage()
            if (pageIndex == 1) {
                startLine = 0
            }
            for (sourceIndex in startLine..22) {
                page.addRow(aModel.getRow(sourceIndex))
            }
            pages!!.add(page)
        }
    }

    fun loadLastPage(aModel: TelnetModel) {
        var start = if (pages!!.isNotEmpty()) 1 else 0
        var end = 23
        while (start < 23 && aModel.getRow(start)?.isEmpty == true) {
            start++
        }
        while (end > start && aModel.getRow(end)?.isEmpty == true) {
            end--
        }
        var page = articlePage
        if (page == null) {
            page = TelnetArticlePage()
        }
        page.clear()
        for (i in start..<end) {
            page.addRow(aModel.getRow(i))
        }
        articlePage = page
    }

    fun clear() {
        article.clear()
        pages!!.clear()
        articlePage = null
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
        var mainBlockDidRead = false // 讀取到主區塊
        var endLineDidRead = false // 是否讀取到最後一行
        var processingItem: TelnetArticleItem? = null
        val regexEndArticle =
            "※ Origin: 巴哈姆特<((gamer\\.com\\.tw)|(www\\.gamer\\.com\\.tw)|(bbs\\.gamer\\.com\\.tw)|(BAHAMUT\\.ORG))> ◆ From: ((?<fromIP>.+))"
        for (row in rows) {
            val rowString = row.toString()
            val quoteLevel = row.quoteLevel
            if (rowString.matches("※( *)引述( *)《(.+)( *)(\\((.+)\\))?》之銘言：".toRegex())) {
                var author = ""
                var nickname = ""
                val rowWords = rowString.toCharArray()
                var authorStart = 0
                var authorEnd = 0
                var i2 = 0
                while (true) {
                    if (i2 >= rowWords.size) {
                        break
                    } else if (rowWords[i2].code == 12298) {
                        authorStart = i2 + 1
                        break
                    } else {
                        i2++
                    }
                }
                var i3 = authorStart
                while (true) {
                    if (i3 >= rowWords.size) {
                        break
                    } else if (rowWords[i3].code == 12299) {
                        authorEnd = i3
                        break
                    } else {
                        i3++
                    }
                }
                if (authorEnd > authorStart) {
                    author = rowString.substring(authorStart, authorEnd).trim { it <= ' ' }
                }
                val authorWords = author.toCharArray()
                var nicknameStart = 0
                var nicknameEnd = authorWords.size - 1
                while (nicknameStart < authorWords.size && authorWords[nicknameStart] != '(') {
                    nicknameStart++
                }
                while (nicknameEnd >= 0 && authorWords[nicknameEnd] != ')') {
                    nicknameEnd--
                }
                if (nicknameEnd > nicknameStart + 1) {
                    nickname = author.substring(nicknameStart + 1, nicknameEnd).trim { it <= ' ' }
                }
                if (nickname.isNotEmpty()) {
                    author = author.substring(0, nicknameStart)
                }
                val author2 = author.trim { it <= ' ' }
                val itemInfo = TelnetArticleItemInfo()
                itemInfo.author = author2
                itemInfo.nickname = nickname
                itemInfo.quoteLevel = quoteLevel + 1
                article.addInfo(itemInfo)
            } else if (!rowString.matches("※ 修改:.*".toRegex())) {
                if (rowString == "--") {
                    mainBlockDidRead = true
                    processingItem = null
                } else if (rowString.matches(regexEndArticle.toRegex())) {
                    endLineDidRead = true
                    processingItem = null
                    val pattern = Pattern.compile(regexEndArticle)
                    val matcher = pattern.matcher(rowString)
                    if (matcher.find()) {
                        val result = matcher.toMatchResult()
                        article.fromIP = result.group(matcher.groupCount())
                    }
                } else if (!endLineDidRead || !rowString.matches(".+：.+\\(.+\\)".toRegex())) {
                    if (processingItem == null || processingItem.quoteLevel != quoteLevel) {
                        processingItem = TelnetArticleItem()
                        if (mainBlockDidRead) {
                            article.addExtendItem(processingItem)
                        } else {
                            article.addMainItem(processingItem)
                        }
                        processingItem.quoteLevel = quoteLevel
                        if (quoteLevel != 0) {
                            var i4 = article.infoSize - 1
                            while (true) {
                                if (i4 < 0) {
                                    break
                                }
                                val itemInfo2 = article.getInfo(i4)
                                if (itemInfo2?.quoteLevel == quoteLevel) {
                                    processingItem.author = itemInfo2.author
                                    processingItem.nickname = itemInfo2.nickname
                                    break
                                }
                                i4--
                            }
                        } else {
                            processingItem.author = article.author
                            processingItem.nickname = article.nickName
                        }
                    }
                    // 其他狀況
                    processingItem.addRow(row)
                } else {
                    var author3 = ""
                    var content = ""
                    var datetime = ""
                    var date: String? = ""
                    var time: String? = ""
                    val rowWords2 = rowString.toCharArray()
                    var authorEnd2 = 0
                    var i5 = 0
                    while (true) {
                        if (i5 >= rowWords2.size) {
                            break
                        } else if (rowWords2[i5].code == 65306) {
                            authorEnd2 = i5
                            break
                        } else {
                            i5++
                        }
                    }
                    if (authorEnd2 > 0) {
                        author3 = rowString.substring(0, authorEnd2).trim { it <= ' ' }
                    }
                    var datetimeStart = 0
                    var datetimeEnd = 0
                    var i6 = rowWords2.size - 1
                    while (true) {
                        if (i6 < 0) {
                            break
                        } else if (rowWords2[i6] == ')') {
                            datetimeEnd = i6
                            break
                        } else {
                            i6--
                        }
                    }
                    var i7 = datetimeEnd - 1
                    while (true) {
                        if (i7 < 0) {
                            break
                        } else if (rowWords2[i7] == '(') {
                            datetimeStart = i7 + 1
                            break
                        } else {
                            i7--
                        }
                    }
                    if (datetimeEnd > datetimeStart) {
                        datetime = rowString.substring(datetimeStart, datetimeEnd)
                    }
                    val datetimeParts: Array<String?> =
                        datetime.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (datetimeParts.size == 2) {
                        date = datetimeParts[0]
                        time = datetimeParts[1]
                    }
                    val contentStart = authorEnd2 + 1
                    val contentEnd = datetimeStart - 1
                    if (contentEnd > contentStart) {
                        content =
                            rowString.substring(contentStart, contentEnd).trim { it <= ' ' }
                    }
                    val push = TelnetArticlePush()
                    push.author = author3
                    push.content = content
                    if (date != null) {
                        push.date = date
                    }
                    if (time != null) {
                        push.time = time
                    }
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
        val row0 = rows[0]
        val row1 = rows[1]
        val row2 = rows[2]
        val authorString = row0.getSpaceString(7, 58).trim { it <= ' ' }
        var author = ""
        var nickname = ""
        if (row0.toContentString().contains("作者")) {
            try {
                val authorWords = authorString.toCharArray()
                var authorEnd = 0
                var i = 0
                while (true) {
                    if (i >= authorWords.size) {
                        break
                    } else if (authorWords[i] == '(') {
                        authorEnd = i
                        break
                    } else {
                        i++
                    }
                }
                if (authorEnd > 0) {
                    author = authorString.substring(0, authorEnd).trim { it <= ' ' }
                }
                val nicknameStart = authorEnd + 1
                var nicknameEnd = nicknameStart
                var i2 = authorWords.size - 1
                while (true) {
                    if (i2 < 0) {
                        break
                    } else if (authorWords[i2] == ')') {
                        nicknameEnd = i2
                        break
                    } else {
                        i2--
                    }
                }
                if (nicknameEnd > nicknameStart) {
                    nickname =
                        authorString.substring(nicknameStart, nicknameEnd).trim { it <= ' ' }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        article.author = author
        article.nickName = nickname

        var boardName = ""
        if (row0.toContentString().contains("看板")) boardName =
            row0.getSpaceString(66, 78).trim { it <= ' ' }
        article.boardName = boardName

        var titleString = ""
        if (row1.toContentString().contains("標題")) {
            titleString = row1.getSpaceString(7, 78).trim { it <= ' ' }
            if (titleString.startsWith("Re: ")) {
                article.title = titleString.substring(4)
                article.articleType = TelnetArticle.REPLY
            } else {
                article.title = titleString
                article.articleType = TelnetArticle.NEW
            }
        }

        var dateTime = ""
        if (row2.toContentString().contains("時間")) {
            dateTime = row2.getSpaceString(7, 30).trim { it <= ' ' }
        }
        article.dateTime = dateTime
        // 只要任意一個屬性有值, 就應正常顯示
        return !author.isEmpty() || !nickname.isEmpty() || !boardName.isEmpty() || !titleString.isEmpty()
        // 被修改過格式不正確
    }

    private fun addRow(row: TelnetRow?, rows: Vector<TelnetRow>) {
        rows.add(row)
    }

    private fun addPage(page: TelnetArticlePage?, rows: Vector<TelnetRow>) {
        if (page != null) {
            for (i in 0..<page.rowCount) {
                addRow(page.getRow(i), rows)
            }
        }
    }

    private fun buildRows(rows: Vector<TelnetRow>) {
        synchronized(pages!!) {
            if (pages != null && pages!!.isNotEmpty()) {
                val pageCount = pages!!.size
                for (pageIndex in 0..<pageCount) {
                    addPage(pages!![pageIndex], rows)
                }
            }
            if (articlePage != null) {
                addPage(articlePage, rows)
            }
        }
    }

    private fun trimRows(rows: Vector<TelnetRow>) {
        for (i in rows.indices) {
            val currentRow = rows[i]
            if (currentRow.data[79].toInt() != 0 && i < rows.size - 1) {
                val testRow = rows[i + 1]
                if (testRow.quoteSpace == 0 && testRow.quoteSpace < currentRow.quoteSpace) {
                    currentRow.append(testRow)
                    rows.removeAt(i + 1)
                }
            }
        }
    }

    fun parsePageIndex(aRow: TelnetRow): Int {
        var d: Byte = 0
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
