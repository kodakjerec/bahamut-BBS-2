package com.kota.telnet.logic

import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleEditRecord
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetArticleItemInfo
import com.kota.telnet.TelnetArticlePage
import com.kota.telnet.TelnetArticlePush
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.model.TelnetRow
import java.util.Vector
import java.util.regex.Pattern

/**
 * 文章解析處理器
 * 
 * 負責將 Telnet 接收的原始文章資料（80欄位寬的文字畫面）轉譯成結構化的文章物件
 * 
 * Telnet 畫面結構（每頁24行，每行80欄位）：
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ Row 0: "作者 xxx (nickname)                                       看板 BoardName"  │
 * │ Row 1: "標題 Re: 文章標題..."                                                        │
 * │ Row 2: "時間 Mon Apr 29 12:00:00 2024"                                              │
 * │ Row 3: ──────────────────────────────────────────────────────────────────────      │
 * │ Row 4~: 文章內容...                                                                  │
 * │ ...                                                                                 │
 * │ "--" (分隔線，之後為推文區)                                                           │
 * │ 推文格式: "推 作者：推文內容 (日期 時間)"                                              │
 * │ Row 23: 狀態列 "瀏覽 P.1/5"                                                          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
class ArticleHandler {
    /** 解析完成的文章物件，包含 title, author, content 等 */
    var article: TelnetArticle = TelnetArticle()
    
    /** 最後一頁的內容（可能不完整，需特別處理） */
    var lastPage: TelnetArticlePage? = null
    
    /** 已載入的完整頁面集合（翻頁時累積） */
    var pages: Vector<TelnetArticlePage> = Vector<TelnetArticlePage>()

    /**
     * 載入一頁文章內容
     * 
     * 從 Telnet 畫面模型中擷取當前頁面的文章內容
     * 第一頁從 Row 0 開始（包含標頭），後續頁面從 Row 1 開始（跳過狀態列）
     * 
     * @param aModel Telnet 畫面模型，包含 24 行 x 80 欄的文字資料
     */
    fun loadPage(aModel: TelnetModel?) {
        var pageIndex: Int = -1
        var startLine = 1  // 預設從第 1 行開始（跳過標頭或上頁殘留）
        if (aModel != null && (parsePageIndex(aModel.lastRow).also { pageIndex = it }) > 0) {
            val page = TelnetArticlePage()
            if (pageIndex == 1) {
                startLine = 0  // 第一頁需要從 Row 0 開始，包含作者/標題/時間標頭
            }
            // 擷取 Row startLine ~ Row 22（Row 23 是狀態列，不納入內文）
            for (sourceIndex in startLine..22) {
                page.addRow(aModel.getRow(sourceIndex)!!)
            }
            pages.add(page)
        }
    }

    /**
     * 載入最後一頁內容
     * 
     * 最後一頁可能有空白行，需要裁切掉頭尾的空行
     * 若尚未載入任何頁面，則從 Row 0 開始；否則從 Row 1 開始
     * 
     * @param aModel Telnet 畫面模型
     */
    fun loadLastPage(aModel: TelnetModel?) {
        var start = if (pages.isNotEmpty()) 1 else 0  // 根據是否有前頁決定起始行
        var end = 23
        
        // 跳過開頭的空白行
        while (start < 23 && aModel?.getRow(start)?.isEmpty == true) {
            start++
        }
        // 跳過結尾的空白行
        while (end > start && aModel?.getRow(end)?.isEmpty == true) {
            end--
        }
        
        val page: TelnetArticlePage = lastPage ?: TelnetArticlePage()
        page.clear()
        for (i in start..<end) {
            page.addRow(aModel!!.getRow(i)!!)
        }
        lastPage = page
    }

    /** 清除所有已載入的資料，準備解析新文章 */
    fun clear() {
        article.clear()
        pages.clear()
        lastPage = null
    }

    /**
     * 主要解析方法：將所有載入的頁面資料轉譯成結構化文章
     * 
     * 解析流程：
     * 1. 合併所有頁面的行資料 (buildRows)
     * 2. 修剪跨行文字 (trimRows)
     * 3. 解析標頭資訊：作者、標題、時間 (loadHeader)
     * 4. 解析內文：引述區塊、主文、推文
     * 
     * 文章結構識別：
     * - "※ 引述《xxx》之銘言：" → 引述區塊開始
     * - "--" → 主文結束，推文區開始
     * - "※ Origin: 巴哈姆特..." → 文章結尾，包含來源 IP
     * - "推 作者：內容 (日期 時間)" → 推文格式
     */
    fun build() {
        article.clear()
        val rows = Vector<TelnetRow>()
        buildRows(rows)   // 合併所有頁面的行
        trimRows(rows)    // 處理跨行文字
        article.setFrameData(rows)
        
        // 解析標頭（作者/標題/時間），成功則移除前 4 行（標頭+分隔線）
        if (loadHeader(rows)) {
            rows.subList(0, 4).clear()
        }
        
        var mainBlockDidRead = false  // 旗標：是否已讀過 "--" 分隔線，之後為推文區
        var endLineDidRead = false    // 旗標：是否已讀到文章結尾（※ Origin 行）
        var processingItem: TelnetArticleItem? = null  // 當前正在處理的內容區塊
        
        // 正規表達式：匹配文章結尾行，並擷取來源 IP
        // 格式: "※ Origin: 巴哈姆特<bbs.gamer.com.tw> ◆ From: 123.456.789.012"
        val regexEndArticle =
            "※ Origin: 巴哈姆特<((gamer\\.com\\.tw)|(www\\.gamer\\.com\\.tw)|(bbs\\.gamer\\.com\\.tw)|(BAHAMUT\\.ORG))> ◆ From: ((?<fromIP>.+))"
        
        for (row in rows) {
            val rowString = row.toString()
            val quoteLevel = row.quoteLevel  // 引述層級（0=原文，1=一層引述，2=二層引述...）
            
            // ========== 檢測引述標記行 ==========
            // 格式: "※ 引述《作者 (暱稱)》之銘言："
            if (rowString.matches("※( *)引述( *)《(.+)( *)(\\((.+)\\))?》之銘言：".toRegex())) {
                var author = ""
                var nickname = ""
                val rowWords = rowString.toCharArray()
                
                // ---------- 擷取作者名稱 ----------
                // 尋找《》符號（Unicode 12298 和 12299）中間的文字
                var authorStart = 0
                var authorEnd = 0
                
                // 找《符號位置 (Unicode 12298 = 0x300A)
                var i2 = 0
                while (true) {
                    if (i2 >= rowWords.size) {
                        break
                    } else if (rowWords[i2].code == 12298) {  // '《' 的 Unicode 碼
                        authorStart = i2 + 1
                        break
                    } else {
                        i2++
                    }
                }
                
                // 找》符號位置 (Unicode 12299 = 0x300B)
                var i3 = authorStart
                while (true) {
                    if (i3 >= rowWords.size) {
                        break
                    } else if (rowWords[i3].code == 12299) {  // '》' 的 Unicode 碼
                        authorEnd = i3
                        break
                    } else {
                        i3++
                    }
                }
                
                // 擷取《》之間的字串作為作者（可能包含暱稱）
                if (authorEnd > authorStart) {
                    author = rowString.substring(authorStart, authorEnd).trim()
                }
                
                // ---------- 從作者字串中分離暱稱 ----------
                // 格式: "作者ID (暱稱)" → 需要拆分
                val authorWords = author.toCharArray()
                var nicknameStart = 0
                var nicknameEnd = authorWords.size - 1
                
                // 從左找第一個 '('
                while (nicknameStart < authorWords.size && authorWords[nicknameStart] != '(') {
                    nicknameStart++
                }
                // 從右找最後一個 ')'
                while (nicknameEnd >= 0 && authorWords[nicknameEnd] != ')') {
                    nicknameEnd--
                }
                
                // 擷取括號內的暱稱
                if (nicknameEnd > nicknameStart + 1) {
                    nickname = author.substring(nicknameStart + 1, nicknameEnd).trim()
                }
                // 從作者字串移除暱稱部分
                if (nickname.isNotEmpty()) {
                    author = author.substring(0, nicknameStart)
                }
                
                val author2 = author.trim()
                
                // 建立引述資訊物件，記錄這個引述區塊的作者
                val itemInfo = TelnetArticleItemInfo()
                itemInfo.author = author2
                itemInfo.nickname = nickname
                itemInfo.quoteLevel = quoteLevel + 1
                article.addInfo(itemInfo)
                
            // ========== 解析修改紀錄行 ==========
            // 格式: "※ 修改: 作者 (IP), 日期時間"
            // 例如: "※ 修改: abc123 (123.456.789.012), 04/29/2024 12:00:00"
            } else if (rowString.matches("※ 修改:.*".toRegex())) {
                parseEditRecord(rowString)
                
            } else {
                
                // ========== 檢測主文/推文分隔線 ==========
                if (rowString == "--") {
                    mainBlockDidRead = true   // 標記：之後的內容屬於推文區
                    processingItem = null     // 重置當前處理區塊
                    
                // ========== 檢測文章結尾行，擷取來源 IP ==========
                } else if (rowString.matches(regexEndArticle.toRegex())) {
                    endLineDidRead = true
                    processingItem = null
                    
                    // 使用正規表達式擷取 IP 位址
                    val pattern = Pattern.compile(regexEndArticle)
                    val matcher = pattern.matcher(rowString)
                    if (matcher.find()) {
                        val result = matcher.toMatchResult()
                        article.fromIP = result.group(matcher.groupCount())
                    }
                    
                // ========== 檢測推文行 ==========
                // 推文格式: "推 作者：推文內容 (日期 時間)"
                // 只有在讀到結尾行後，才會解析符合此格式的行為推文
                } else if (!endLineDidRead || !rowString.matches(".+：.+\\(.+\\)".toRegex())) {
                    
                    // ========== 處理一般內文行 ==========
                    // 根據引述層級建立新的內容區塊，或加入現有區塊
                    if (processingItem == null || processingItem.quoteLevel != quoteLevel) {
                        processingItem = TelnetArticleItem()
                        
                        if (mainBlockDidRead) {
                            article.addExtendItem(processingItem)  // 推文區後的延伸內容
                        } else {
                            article.addMainItem(processingItem)    // 主文內容
                        }
                        
                        processingItem.quoteLevel = quoteLevel
                        
                        // 為引述內容設定作者資訊
                        if (quoteLevel != 0) {
                            // 從已記錄的引述資訊中，找到對應層級的作者
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
                            // 非引述內容，使用原文作者
                            processingItem.author = article.author
                            processingItem.nickname = article.nickName
                        }
                    }
                    // 將此行加入當前處理的內容區塊
                    processingItem.addRow(row)
                    
                } else {
                    // ========== 解析推文行 ==========
                    parsePush(rowString)
                }
            }
        }
        article.build()  // 最終組裝文章物件
    }
    
    /**
     * 解析推文行
     * 
     * 推文格式: "推 作者：推文內容 (日期 時間)"
     *          ↑    ↑         ↑
     *          |    |         └─ datetime (括號內)
     *          |    └─ 全形冒號 (Unicode 65306)
     *          └─ author (冒號前，含推/噓/→前綴)
     * 
     * @param rowString 推文行的原始文字
     */
    private fun parsePush(rowString: String) {
        val push = TelnetArticlePush()
        
        try {
            val chars = rowString.toCharArray()
            
            // ---------- 擷取推文作者 ----------
            // 尋找全形冒號 '：' (Unicode 65306 = 0xFF1A) 的位置
            var authorEnd = 0
            for (i in chars.indices) {
                if (chars[i].code == 65306) {  // '：' 全形冒號
                    authorEnd = i
                    break
                }
            }
            // 冒號前的文字即為作者（含推/噓/→前綴）
            if (authorEnd > 0) {
                push.author = rowString.substring(0, authorEnd).trim()
            }
            
            // ---------- 擷取推文日期時間 ----------
            // 從行尾往前找括號，擷取 "(日期 時間)"
            var datetimeStart = 0
            var datetimeEnd = 0
            
            // 從尾端找 ')' 的位置
            for (i in chars.size - 1 downTo 0) {
                if (chars[i] == ')') {
                    datetimeEnd = i
                    break
                }
            }
            
            // 往前找對應的 '(' 的位置
            for (i in datetimeEnd - 1 downTo 0) {
                if (chars[i] == '(') {
                    datetimeStart = i + 1
                    break
                }
            }
            
            // 擷取括號內的日期時間字串，並拆分為 date 和 time
            // 例如: "04/29 12:00" → date="04/29", time="12:00"
            if (datetimeEnd > datetimeStart) {
                val datetime = rowString.substring(datetimeStart, datetimeEnd)
                val datetimeParts = datetime.split(" +".toRegex()).dropLastWhile { it.isEmpty() }
                if (datetimeParts.size == 2) {
                    push.date = datetimeParts[0]
                    push.time = datetimeParts[1]
                }
            }
            
            // ---------- 擷取推文內容 ----------
            // 內容位於「全形冒號之後」到「左括號之前」
            val contentStart = authorEnd + 1
            val contentEnd = datetimeStart - 1
            if (contentEnd > contentStart) {
                push.content = rowString.substring(contentStart, contentEnd).trim()
            }
        } catch (e: Exception) {
            // 解析失敗時不拋出異常
            e.printStackTrace()
        }
        
        article.addPush(push)
    }

    /**
     * 解析文章標頭（作者、標題、時間、看板）
     * 
     * 標頭結構（固定欄位位置）：
     * ┌──────────────────────────────────────────────────────────────────────────────┐
     * │ Row 0: "作者  作者ID (暱稱)                                       看板  BoardName"   │
     * │        欄位 0-6: "作者"標籤                                                         │
     * │        欄位 7-58: 作者ID + 暱稱                                                     │
     * │        欄位 59-65: "看板"標籤                                                       │
     * │        欄位 66-78: 看板名稱                                                         │
     * │                                                                                    │
     * │ Row 1: "標題  Re: 文章標題..."                                                      │
     * │        欄位 0-6: "標題"標籤                                                         │
     * │        欄位 7-78: 標題內容（可能有 "Re: " 前綴表示回覆文）                            │
     * │                                                                                    │
     * │ Row 2: "時間  Mon Apr 29 12:00:00 2024"                                            │
     * │        欄位 0-6: "時間"標籤                                                         │
     * │        欄位 7-30: 發文時間                                                          │
     * │                                                                                    │
     * │ Row 3: ─────────────────────── (分隔線)                                            │
     * └──────────────────────────────────────────────────────────────────────────────┘
     * 
     * @param rows 文章所有行資料
     * @return true 如果成功解析到任一標頭欄位；false 如果資料不足
     */
    private fun loadHeader(rows: Vector<TelnetRow>): Boolean {
        // 至少需要 4 行（作者、標題、時間、分隔線）
        if (rows.size <= 3) {
            return false
        }
        
        val row0 = rows[0]  // 作者行
        val row1 = rows[1]  // 標題行
        val row2 = rows[2]  // 時間行
        
        // ========== 解析作者與暱稱 ==========
        // 從 Row 0 的第 7~58 欄擷取作者資訊
        // 格式: "作者ID (暱稱)" 例如: "abc123 (小明)"
        val authorString = row0.getSpaceString(7, 58).trim()
        var author = ""
        var nickname = ""
        
        if (row0.toContentString().contains("作者")) {
            try {
                val authorWords = authorString.toCharArray()
                
                // ---------- 擷取作者 ID ----------
                // 尋找第一個 '(' 的位置，之前的文字為作者 ID
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
                    author = authorString.substring(0, authorEnd).trim()
                }
                
                // ---------- 擷取暱稱 ----------
                // 暱稱位於括號內，從 '(' 之後到 ')' 之前
                val nicknameStart = authorEnd + 1
                var nicknameEnd = nicknameStart
                
                // 從尾端往前找 ')' 的位置
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
                        authorString.substring(nicknameStart, nicknameEnd).trim()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        article.author = author
        article.nickName = nickname

        // ========== 解析看板名稱 ==========
        // 從 Row 0 的第 66~78 欄擷取看板名稱
        var boardName = ""
        if (row0.toContentString().contains("看板")) boardName =
            row0.getSpaceString(66, 78).trim()
        article.boardName = boardName

        // ========== 解析標題 ==========
        // 從 Row 1 的第 7~78 欄擷取標題
        // 若以 "Re: " 開頭，表示為回覆文章
        var titleString = ""
        if (row1.toContentString().contains("標題")) {
            titleString = row1.getSpaceString(7, 78).trim()
            if (titleString.startsWith("Re: ")) {
                // 回覆文：移除 "Re: " 前綴，設定文章類型為 REPLY
                article.title = titleString.substring(4)
                article.articleType = TelnetArticle.REPLY
            } else {
                // 新文章
                article.title = titleString
                article.articleType = TelnetArticle.NEW
            }
        }

        // ========== 解析發文時間 ==========
        // 從 Row 2 的第 7~30 欄擷取時間
        var dateTime = ""
        if (row2.toContentString().contains("時間")) {
            dateTime = row2.getSpaceString(7, 30).trim()
        }
        article.dateTime = dateTime
        
        // 只要任意一個屬性有值，就視為成功解析（可能因格式被修改而不完整）
        return !author.isEmpty() || !nickname.isEmpty() || !boardName.isEmpty() || !titleString.isEmpty()
    }

    /** 將單行加入行列表 */
    private fun addRow(row: TelnetRow?, rows: Vector<TelnetRow>) {
        rows.add(row)
    }

    /** 將整頁的所有行加入行列表 */
    private fun addPage(page: TelnetArticlePage?, rows: Vector<TelnetRow>) {
        if (page != null) {
            for (i in 0..<page.rowCount) {
                addRow(page.getRow(i), rows)
            }
        }
    }

    /**
     * 合併所有頁面的行資料
     * 將 pages 集合中的每一頁，加上 lastPage，依序合併成單一行列表
     */
    private fun buildRows(rows: Vector<TelnetRow>) {
        synchronized(pages) {
            if (pages != null && pages.isNotEmpty()) {
                val pageCount = pages.size
                for (pageIndex in 0..<pageCount) {
                    addPage(pages[pageIndex], rows)
                }
            }
            if (lastPage != null) {
                addPage(lastPage, rows)
            }
        }
    }

    /**
     * 修剪跨行文字
     * 
     * 處理 Telnet 80 欄位限制造成的文字換行問題
     * 當一行最後一個字元不是空白（表示被截斷），且下一行沒有引述標記時，
     * 將下一行合併到當前行
     */
    private fun trimRows(rows: Vector<TelnetRow>) {
        for (i in rows.size - 1 downTo 0) {
            val currentRow = rows[i]
            // 檢查第 79 欄（最後一欄）是否有字元（表示可能被截斷）
            if (currentRow!!.data[79].toInt() != 0 && i < rows.size - 1) {
                val testRow: TelnetRow = rows[i + 1]
                // 若下一行沒有引述空白，且空白數小於當前行，則合併
                if (testRow.quoteSpace == 0 && testRow.quoteSpace < currentRow.quoteSpace) {
                    currentRow.append(testRow)
                    rows.removeAt(i + 1)
                }
            }
        }
    }

    /**
     * 從狀態列解析當前頁碼
     * 
     * 狀態列格式: "瀏覽 P.123/456 文章選讀..."
     *                  ↑
     *                  欄位 8~13: 頁碼數字 (ASCII 48-57 = '0'-'9')
     * 
     * @param aRow 狀態列（Row 23）
     * @return 當前頁碼，解析失敗返回 0
     */
    fun parsePageIndex(aRow: TelnetRow): Int {
        var d: Byte = 0
        var index = 0
        var i = 8  // 從第 8 欄開始（"瀏覽 P." 後面）
        
        // 讀取連續的數字字元 (ASCII 48='0' ~ 57='9')，最多到第 14 欄
        while (i < 14 && (aRow.data[i].also { d = it }) >= 48 && d <= 57) {
            // 將 ASCII 碼轉換為數字並組合 (例如 "123" → 1*100 + 2*10 + 3)
            index = (index * 10) + ((d - 48) and 255)
            i++
        }
        return index
    }
    
    /**
     * 解析修改紀錄行
     * 
     * 修改紀錄格式: "※ 修改: 作者 (IP), 日期時間"
     * 範例: "※ 修改: abc123 (123.456.789.012), 04/29/2024 12:00:00"
     * 
     * @param rowString 修改紀錄行的原始文字
     */
    private fun parseEditRecord(rowString: String) {
        val record = TelnetArticleEditRecord()
        record.rawString = rowString
        
        try {
            // 移除前綴 "※ 修改: " (包含全形和半形冒號)
            var content = rowString
            if (content.startsWith("※ 修改:")) {
                content = content.substring(5).trim()  // "※ 修改:" = 5 字元
            } else if (content.startsWith("※ 修改：")) {
                content = content.substring(5).trim()  // "※ 修改：" = 5 字元
            }
            
            // ---------- 擷取作者 ----------
            // 從開頭到第一個 '(' 之前
            val chars = content.toCharArray()
            var authorEnd = 0
            for (i in chars.indices) {
                if (chars[i] == '(') {
                    authorEnd = i
                    break
                }
            }
            if (authorEnd > 0) {
                record.author = content.substring(0, authorEnd).trim()
            }
            
            // ---------- 擷取 IP ----------
            // 從 '(' 後到 ')' 之前
            var ipStart = authorEnd + 1
            var ipEnd = ipStart
            for (i in ipStart..<chars.size) {
                if (chars[i] == ')') {
                    ipEnd = i
                    break
                }
            }
            if (ipEnd > ipStart) {
                record.ip = content.substring(ipStart, ipEnd).trim()
            }
            
            // ---------- 擷取日期時間 ----------
            // ')' 後面通常有 ", " 分隔，之後是日期時間
            if (ipEnd + 1 < chars.size) {
                var dateTimeStart = ipEnd + 1
                // 跳過 "), " 或 ") "
                while (dateTimeStart < chars.size && 
                       (chars[dateTimeStart] == ',' || chars[dateTimeStart] == ' ')) {
                    dateTimeStart++
                }
                if (dateTimeStart < chars.size) {
                    record.dateTime = content.substring(dateTimeStart).trim()
                }
            }
        } catch (e: Exception) {
            // 解析失敗時保留原始字串，不拋出異常
            e.printStackTrace()
        }
        
        article.addEditRecord(record)
    }

    /** 建立新的空白文章物件，準備接收新文章資料 */
    fun newArticle() {
        this.article = TelnetArticle()
    }
}
