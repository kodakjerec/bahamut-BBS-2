package com.kota.Telnet.Logic

import com.kota.Telnet.Model.TelnetModel
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.TelnetArticle
import com.kota.Telnet.TelnetArticle.Companion.NEW
import com.kota.Telnet.TelnetArticle.Companion.REPLY
import com.kota.Telnet.TelnetArticleItem
import com.kota.Telnet.TelnetArticleItemInfo
import com.kota.Telnet.TelnetArticlePage
import com.kota.Telnet.TelnetArticlePush
import java.util.*
import java.util.regex.Pattern

private val TelnetArticle.getInfoSize: Any

class Article_Handler {
    var telnetArticle = TelnetArticle()
    private var _last_page: TelnetArticlePage? = null
    private val _pages = Vector<TelnetArticlePage>()

    fun loadPage(aModel: TelnetModel?) {
        var startLine = 1
        aModel?.let { model ->
            val pageIndex = parsePageIndex(model.lastRow)
            if (pageIndex > 0) {
                val page = TelnetArticlePage()
                if (pageIndex == 1) {
                    startLine = 0
                }
                for (sourceIndex in startLine until 23) {
                    page.addRow(model.getRow(sourceIndex))
                }
                _pages.add(page)
            }
        }
    }

    fun loadLastPage(aModel: TelnetModel) {
        var start = if (_pages.size > 0) 1 else 0
        var end = 23
        
        while (start < 23 && aModel.getRow(start)?.isEmpty() == true) {
            start++
        }
        while (end > start && aModel.getRow(end)?.isEmpty() == true) {
            end--
        }
        
        var page = _last_page
        if (page == null) {
            page = TelnetArticlePage()
        }
        page.clear()
        for (i in start until end) {
            page.addRow(aModel.getRow(i))
        }
        _last_page = page
    }

    fun clear() {
        telnetArticle.clear()
        _pages.clear()
        _last_page = null
    }

    /** 分析每行內容 */
    fun build() {
        telnetArticle.clear()
        val rows = Vector<TelnetRow>()
        buildRows(rows)
        trimRows(rows)
        telnetArticle.setFrameData(rows)
        
        if (loadHeader(rows)) {
            rows.subList(0, 4).clear()
        }
        
        var mainBlockDidRead = false // 讀取到主區塊
        var endLineDidRead = false // 是否讀取到最後一行
        var processingItem: TelnetArticleItem? = null
        val regexEndArticle = "※ Origin: 巴哈姆特<((gamer\\.com\\.tw)|(www\\.gamer\\.com\\.tw)|(bbs\\.gamer\\.com\\.tw)|(BAHAMUT\\.ORG))> ◆ From: ((?<fromIP>.+))"
        
        for (row in rows) {
            val rowString = row.toString()
            val quoteLevel = row.getQuoteLevel()
            
            if (rowString.matches("※( *)引述( *)《(.+)( *)(\\((.+)\\))?》之銘言：".toRegex())) {
                var author = ""
                var nickname = ""
                val rowWords = rowString.toCharArray()
                var authorStart = 0
                var authorEnd = 0
                
                var i = 0
                while (i < rowWords.size) {
                    if (rowWords[i] == '《') {
                        authorStart = i + 1
                        break
                    }
                    i++
                }
                
                i = authorStart
                while (i < rowWords.size) {
                    if (rowWords[i] == '》') {
                        authorEnd = i
                        break
                    }
                    i++
                }
                
                if (authorEnd > authorStart) {
                    author = rowString.substring(authorStart, authorEnd).trim()
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
                    nickname = author.substring(nicknameStart + 1, nicknameEnd).trim()
                }
                if (nickname.isNotEmpty()) {
                    author = author.substring(0, nicknameStart)
                }
                
                val finalAuthor = author.trim()
                val itemInfo = TelnetArticleItemInfo().apply {
                    this.author = finalAuthor
                    this.nickname = nickname
                    this.quoteLevel = quoteLevel + 1
                }
                telnetArticle.addInfo(itemInfo)
                
            } else if (!rowString.matches("※ 修改:.*".toRegex())) {
                when {
                    rowString == "--" -> {
                        mainBlockDidRead = true
                        processingItem = null
                    }
                    rowString.matches(regexEndArticle.toRegex()) -> {
                        endLineDidRead = true
                        processingItem = null
                        val pattern = Pattern.compile(regexEndArticle)
                        val matcher = pattern.matcher(rowString)
                        if (matcher.find()) {
                            val result = matcher.toMatchResult()
                            telnetArticle.fromIP = result.group(matcher.groupCount())
                        }
                    }
                    !endLineDidRead || !rowString.matches(".+：.+\\(.+\\)".toRegex()) -> {
                        if (processingItem == null || processingItem.getQuoteLevel() != quoteLevel) {
                            processingItem = TelnetArticleItem()
                            if (mainBlockDidRead) {
                                telnetArticle.addExtendItem(processingItem)
                            } else {
                                telnetArticle.addMainItem(processingItem)
                            }
                            processingItem.setQuoteLevel(quoteLevel)
                            
                            if (quoteLevel != 0) {
                                var i4 = telnetArticle.getInfoSize - 1
                                while (i4 >= 0) {
                                    val itemInfo = telnetArticle.getInfo(i4)
                                    if (itemInfo.quoteLevel == quoteLevel) {
                                        processingItem.setAuthor(itemInfo.author)
                                        processingItem.setNickname(itemInfo.nickname)
                                        break
                                    }
                                    i4--
                                }
                            } else {
                                processingItem.setAuthor(telnetArticle.Author)
                                processingItem.setNickname(telnetArticle.Nickname)
                            }
                        }
                        // 其他狀況
                        processingItem.addRow(row)
                    }
                    else -> {
                        var author = ""
                        var content = ""
                        var datetime = ""
                        var date = ""
                        var time = ""
                        val rowWords = rowString.toCharArray()
                        var authorEnd = 0
                        
                        var i = 0
                        while (i < rowWords.size) {
                            if (rowWords[i] == '：') {
                                authorEnd = i
                                break
                            }
                            i++
                        }
                        
                        if (authorEnd > 0) {
                            author = rowString.substring(0, authorEnd).trim()
                        }
                        
                        var datetimeStart = 0
                        var datetimeEnd = 0
                        
                        i = rowWords.size - 1
                        while (i >= 0) {
                            if (rowWords[i] == ')') {
                                datetimeEnd = i
                                break
                            }
                            i--
                        }
                        
                        i = datetimeEnd - 1
                        while (i >= 0) {
                            if (rowWords[i] == '(') {
                                datetimeStart = i + 1
                                break
                            }
                            i--
                        }
                        
                        if (datetimeEnd > datetimeStart) {
                            datetime = rowString.substring(datetimeStart, datetimeEnd)
                        }
                        
                        val datetimeParts = datetime.split(" +".toRegex())
                        if (datetimeParts.size == 2) {
                            date = datetimeParts[0]
                            time = datetimeParts[1]
                        }
                        
                        val contentStart = authorEnd + 1
                        val contentEnd = datetimeStart - 1
                        if (contentEnd > contentStart) {
                            content = rowString.substring(contentStart, contentEnd).trim()
                        }
                        
                        val push = TelnetArticlePush().apply {
                            this.author = author
                            this.content = content
                            this.date = date
                            this.time = time
                        }
                        telnetArticle.addPush(push)
                    }
                }
            }
        }
        telnetArticle.build()
    }

    private fun loadHeader(rows: Vector<TelnetRow>): Boolean {
        if (rows.size <= 3) {
            return false
        }
        
        val row0 = rows[0]
        val row1 = rows[1]
        val row2 = rows[2]
        val authorString = row0.getSpaceString(7, 58).trim()
        var author = ""
        var nickname = ""
        
        if (row0.toContentString().contains("作者")) {
            try {
                val authorWords = authorString.toCharArray()
                var authorEnd = 0
                
                var i = 0
                while (i < authorWords.size) {
                    if (authorWords[i] == '(') {
                        authorEnd = i
                        break
                    }
                    i++
                }
                
                if (authorEnd > 0) {
                    author = authorString.substring(0, authorEnd).trim()
                }
                
                val nicknameStart = authorEnd + 1
                var nicknameEnd = nicknameStart
                
                i = authorWords.size - 1
                while (i >= 0) {
                    if (authorWords[i] == ')') {
                        nicknameEnd = i
                        break
                    }
                    i--
                }
                
                if (nicknameEnd > nicknameStart) {
                    nickname = authorString.substring(nicknameStart, nicknameEnd).trim()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        telnetArticle.Author = author
        telnetArticle.Nickname = nickname

        var boardName = ""
        if (row0.toContentString().contains("看板")) {
            boardName = row0.getSpaceString(66, 78).trim()
        }
        telnetArticle.BoardName = boardName

        var titleString = ""
        if (row1.toContentString().contains("標題")) {
            titleString = row1.getSpaceString(7, 78).trim()
            if (titleString.startsWith("Re: ")) {
                telnetArticle.Title = titleString.substring(4)
                telnetArticle.Type = REPLY
            } else {
                telnetArticle.Title = titleString
                telnetArticle.Type = NEW
            }
        }

        var dateTime = ""
        if (row2.toContentString().contains("時間")) {
            dateTime = row2.getSpaceString(7, 30).trim()
        }
        telnetArticle.DateTime = dateTime
        
        // 只要任意一個屬性有值, 就應正常顯示
        return author.isNotEmpty() || nickname.isNotEmpty() || boardName.isNotEmpty() || titleString.isNotEmpty()
    }

    private fun addRow(row: TelnetRow, rows: Vector<TelnetRow>) {
        rows.add(row)
    }

    private fun addPage(page: TelnetArticlePage?, rows: Vector<TelnetRow>) {
        page?.let {
            for (i in 0 until it.getRowCount()) {
                addRow(it.getRow(i), rows)
            }
        }
    }

    private fun buildRows(rows: Vector<TelnetRow>) {
        synchronized(_pages) {
            if (_pages.isNotEmpty()) {
                val pageCount = _pages.size
                for (pageIndex in 0 until pageCount) {
                    addPage(_pages[pageIndex], rows)
                }
            }
            _last_page?.let { addPage(it, rows) }
        }
    }

    private fun trimRows(rows: Vector<TelnetRow>) {
        var i = 0
        while (i < rows.size) {
            val currentRow = rows[i]
            if (currentRow.data[79] != 0.toByte() && i < rows.size - 1) {
                val testRow = rows[i + 1]
                if (testRow.getQuoteSpace() == 0 && testRow.getDataSpace() < currentRow.getQuoteSpace()) {
                    currentRow.append(testRow)
                    rows.removeAt(i + 1)
                    continue
                }
            }
            i++
        }
    }

    fun parsePageIndex(aRow: TelnetRow): Int {
        var index = 0
        var i = 8
        while (i < 14) {
            val d = aRow.data[i]
            if (d >= 48 && d <= 57) {
                index = (index * 10) + ((d - 48) and 255)
                i++
            } else {
                break
            }
        }
        return index
    }

    fun getArticle(): TelnetArticle = telnetArticle

    fun newArticle() {
        telnetArticle = TelnetArticle()
    }
}
