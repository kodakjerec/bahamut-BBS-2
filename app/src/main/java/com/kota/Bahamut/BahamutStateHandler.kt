package com.kota.Bahamut

import android.util.Log
import com.kota.Bahamut.command.BahamutCommandLoadArticleEnd
import com.kota.Bahamut.command.BahamutCommandLoadArticleEndForSearch
import com.kota.Bahamut.command.BahamutCommandLoadMoreArticle
import com.kota.Bahamut.pages.ClassPage
import com.kota.Bahamut.pages.MainPage
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.articlePage.ArticlePage
import com.kota.Bahamut.pages.bbsUser.UserConfigPage
import com.kota.Bahamut.pages.bbsUser.UserInfoPage
import com.kota.Bahamut.pages.boardPage.BoardLinkPage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.pages.boardPage.BoardSearchPage
import com.kota.Bahamut.pages.essencePage.ArticleEssencePage
import com.kota.Bahamut.pages.essencePage.BoardEssencePage
import com.kota.Bahamut.pages.login.LoginPage
import com.kota.Bahamut.pages.mailPage.MailBoxPage
import com.kota.Bahamut.pages.mailPage.MailPage
import com.kota.Bahamut.pages.messages.BahaMessage
import com.kota.Bahamut.pages.messages.MessageDatabase
import com.kota.Bahamut.pages.messages.MessageMain
import com.kota.Bahamut.pages.messages.MessageSmall
import com.kota.Bahamut.pages.messages.MessageStatus
import com.kota.Bahamut.pages.messages.MessageSub
import com.kota.Bahamut.service.HeroStep
import com.kota.Bahamut.service.EditFromLinkedState
import com.kota.Bahamut.service.EditFromLinkedStep
import com.kota.Bahamut.service.NotificationSettings.getShowMessageFloating
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.Bahamut.service.TempSettings.getNotReadMessageCount
import com.kota.Bahamut.service.TempSettings.setMessageSmall
import com.kota.Bahamut.service.TempSettings.setNotReadMessageCount
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASViewController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASSnackBar.show
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetCursor
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.TelnetStateHandler
import com.kota.telnet.TelnetUtils.getHeader
import com.kota.telnet.logic.ArticleHandler
import com.kota.telnet.logic.SearchBoardHandler
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage
import com.kota.textEncoder.B2UEncoder
import java.io.ByteArrayOutputStream
import java.util.Vector
import java.util.function.Consumer
import java.util.regex.Pattern

/**
 * Bahamut BBS 狀態處理器
 *
 * 負責解析 Telnet 伺服器回傳的畫面內容，判斷當前頁面狀態，
 * 並根據不同狀態觸發對應的頁面切換或動作執行。
 * 這是整個 BBS 客戶端的核心狀態機。
 *
 * @see TelnetStateHandler 父類別，提供基本的 Telnet 狀態處理功能
 */
class BahamutStateHandler internal constructor() : TelnetStateHandler() {
    /** 當前文章編號 */
    var myArticleNumber: String = ""
    /** 當前連線步驟狀態 (STEP_CONNECTING 或 STEP_WORKING) */
    var nowStep: Int = 0
    /** Telnet 畫面的所有行資料，供 debug 使用 */
    var telnetRows: Vector<TelnetRow> = Vector<TelnetRow>()
    /** 第 0 行字串內容 (通常是頁面標題) */
    var rowString00: String = ""
    /** 第 1 行字串內容 */
    var rowString01: String = ""
    /** 第 2 行字串內容 */
    var rowString02: String = ""
    /** 最後一行有內容的字串 (通常是操作提示) */
    var rowStringFinal: String = ""
    /** 第一行的標頭文字 */
    var firstHeader: String = ""
    /** 最後一行的標頭文字 */
    var lastHeader: String = ""
    /** 當前游標位置 */
    var telnetCursor: TelnetCursor? = null
    /** 文章內容處理器，負責解析文章內容 */
    val articleHandler: ArticleHandler = ArticleHandler()
    /** 是否正在讀取文章中 */
    var duringReadingArticle: Boolean = false

    /**
     * 設定文章編號
     *
     * @param aArticleNumber 文章編號字串
     */
    fun setArticleNumber(aArticleNumber: String) {
        this.myArticleNumber = aArticleNumber
    }

    /**
     * 載入當前 Telnet 畫面狀態
     *
     * 從 TelnetClient 取得當前畫面的各行資料，
     * 解析並儲存到對應的成員變數中供後續判斷使用。
     */
    fun loadState() {
        this.rowString00 = getRowString(0).trim()
        this.rowString01 = getRowString(1).trim()
        this.rowString02 = getRowString(2).trim()
        // 隨然是取row 23, 但是偶爾遇到排版不正確情況, 取row 22, 以此類推
        // row 2 已經有使用, 當作界線
        var i = 23
        while (i > 2) {
            this.rowStringFinal = getRowString(i).trim()
            if (!this.rowStringFinal.isEmpty()) i = 0
            i--
        }
        telnetRows.clear()
        // 從 TelnetClient 獲取當前的行資料
        TelnetClient.model.let { model ->
            for (i in 0 until model.rows.size) {
                model.getRow(i)?.let { row ->
                    telnetRows.add(row)
                }
            }
        }
        this.firstHeader = getHeader(this.rowString00)
        this.lastHeader = getHeader(this.rowStringFinal)
    }

    /**
     * 偵測並處理即時訊息 (水球)
     *
     * 解析第 23 行的背景顏色來判斷是否有新訊息，
     * 若有則儲存到資料庫並顯示通知。
     */
    fun detectMessage() {
        val column = this.telnetCursor!!.column
        val row: TelnetRow? = TelnetClient.model.getRow(23)
        val nameBuffer = ByteArrayOutputStream(80)
        val msgBuffer = ByteArrayOutputStream(80)
        var endPoint = -1
        var i = 0
        while (true) {
            if (i >= row?.data!!.size) {
                break
            }
            val backgroundColor = row.myBackgroundColor[i]
            val data = row.data[i]
            if (backgroundColor.toInt() == 6) {
                nameBuffer.write(data.toInt())
            } else if (backgroundColor.toInt() == 5) {
                msgBuffer.write(data.toInt())
            } else {
                endPoint = i
                break
            }
            i++
        }
        if (nameBuffer.size() > 0 && msgBuffer.size() > 0) {
            val name: String = B2UEncoder.instance!!.encodeToString(nameBuffer.toByteArray())
            val msg: String = B2UEncoder.instance!!.encodeToString(msgBuffer.toByteArray())
            if (endPoint == column && name.startsWith("★")) {
                val name2 = name.substring(1, name.length - 1).trim()
                val msg2 = msg.substring(1).trim()
                // 因為BBS會更新畫面, 會重複出現相同訊息. 只要最後接收的訊息一樣就不顯示
                if (TempSettings.lastReceivedMessage != name2 + msg2) {
                    // 更新未讀取訊息
                    var totalUnreadCount = getNotReadMessageCount()
                    totalUnreadCount++
                    setNotReadMessageCount(totalUnreadCount)

                    var bahaMessage: BahaMessage? = null
                    try {
                        MessageDatabase(TempSettings.myContext).use { db ->
                            // 紀錄訊息
                            bahaMessage = db.receiveMessage(name2, msg2, 0)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            javaClass.simpleName,
                            (if (e.message != null) e.message else "")!!
                        )
                    }

                    if (bahaMessage != null) {
                        val topPage = ASNavigationController.currentController?.topController as TelnetPage?
                        when (topPage) {
                            is MessageMain -> {
                                // 顯示訊息
                                show(name2, msg2)
                                val aPage = topPage
                                aPage.loadMessageList(bahaMessage)
                            }

                            is MessageSub -> {
                                val aPage = topPage
                                aPage.insertMessage(bahaMessage)
                            }

                            else -> {
                                // 如果是其他頁面:顯示訊息
                                show(name2, msg2)
                            }
                        }
                    }

                    TempSettings.lastReceivedMessage = name2 + msg2
                }
            }
        }
    }

    /**
     * 處理非切換主頁面的需求
     *
     * 處理各種系統提示訊息，如：
     * - 未完成的文章提示
     * - 按任意鍵繼續提示
     * - 引言過多提示
     * - 即時訊息接收
     *
     * @return true 表示需要繼續處理頁面切換，false 表示已處理完畢
     */
    fun handleNonPageSwitching(): Boolean {
        // 本文
        var runPass2 = true
        if (this.rowStringFinal.contains("您有一篇文章尚未完成")) {
            TelnetClient.myInstance!!.sendStringToServer("S\n1\n")
            runPass2 = false
        }
        if (runPass2 && this.rowStringFinal.contains("[請按任意鍵繼續]") && currentPage != BahamutPage.BAHAMUT_LOGIN) {
            val continueMessage = cutOffContinueMessage(this.rowStringFinal)
            if (!continueMessage.isEmpty()) {
                if (continueMessage.contains("推文") || continueMessage.contains("請稍後片刻")) {
                    PageContainer.instance!!.boardPage.cancelRunner()
                }
                showShortToast(continueMessage)
            }
            if (this.rowStringFinal.contains("★ 引言太多")) {
                // 放棄此次編輯內容
                val data = create()
                    .pushKey(TelnetKeyboard.SPACE)
                    .build()
                TelnetClient.myInstance!!.sendDataToServer(data)
                val topPage =
                    ASNavigationController.currentController?.topController as TelnetPage?
                if (topPage is MailBoxPage) {
                    val page2: MailBoxPage = PageContainer.instance!!.mailBoxPage
                    page2.recoverPost()
                } else if (topPage is PostArticlePage) {
                    // 最上層是 發文 或 看板
                    // 清除最先遇到的 BoardSearch, BoardLink, BoardMain
                    val controllers: Vector<ASViewController> = ASNavigationController.currentController!!.viewControllers
                    for (i in controllers.size downTo 1) {
                        val nowPage = controllers[i - 1] as TelnetPage

                        when (nowPage.javaClass) {
                            BoardMainPage::class.java -> {
                                val page: BoardMainPage =
                                    PageContainer.instance!!.boardPage
                                page.recoverPost()
                                return false
                            }
                            BoardLinkPage::class.java -> {
                                val page: BoardLinkPage =
                                    PageContainer.instance!!.boardLinkedTitlePage
                                page.recoverPost()
                                return false
                            }
                            BoardSearchPage::class.java -> {
                                val page: BoardSearchPage =
                                    PageContainer.instance!!.boardSearchPage
                                page.recoverPost()
                                return false
                            }
                        }
                    }
                }
                return false
            }
            TelnetClient.myInstance!!.sendStringToServer("")
            return false
        } else if (this.rowStringFinal.contains("要新增資料嗎？(Y/N) [N]")) {
            showShortToast("此看板無文章")
            TelnetClient.myInstance!!.sendStringToServer("N")
            return false
        } else if (this.rowStringFinal.contains("● 請按任意鍵繼續 ●")) {
            if (this.rowString00.contains("順利貼出佈告")) {
                // 順利貼出佈告, 請按任意鍵繼續
                val topPage =
                    ASNavigationController.currentController?.topController as TelnetPage?
                if (topPage is PostArticlePage || topPage is BoardMainPage) {
                    // 最上層是 發文 或 看板
                    val page: BoardMainPage = PageContainer.instance!!.boardPage
                    page.finishPost()
                } else if (topPage is MailBoxPage) {
                    val page2: MailBoxPage = PageContainer.instance!!.mailBoxPage
                    page2.finishPost()
                }
            } else if (this.rowString02.contains("HP：") && this.rowString02.contains("MP：")) {
                val page: ArticlePage = PageContainer.instance!!.articlePage
                val userData = Vector<String>()
                this.telnetRows.forEach(Consumer { row: TelnetRow? -> userData.add(row.toString()) })
                page.ctrlQUser(userData)
            } else if (this.rowString00.contains("過  路  勇  者  的  足  跡")) {
                // 逐行塞入勇者足跡
                insertHeroSteps()
            }

            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            return false
        } else if (this.rowStringFinal.contains("請按 [SPACE] 繼續觀賞") && this.rowString00.contains(
                "過  路  勇  者  的  足  跡"
            )
        ) {
            // 逐行塞入勇者足跡
            insertHeroSteps()
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            return false
        } else if (this.rowStringFinal.startsWith("★") && !this.rowStringFinal.substring(1, 2)
                .isEmpty()
        ) {
            detectMessage()
            return false
        } else {
            return runPass2
        }
    }

    /**
     * 解析並儲存勇者足跡資料
     *
     * 從 Telnet 畫面中逐行解析訪客留言，
     * 每則留言包含作者名稱、時間和內容 (最多三行)。
     */
    fun insertHeroSteps() {
        var startCatching = false
        var heroStep: HeroStep? = null
        var countRows = 0
        for (i in telnetRows.indices) {
            val fromRow: TelnetRow = telnetRows[i]
            if (startCatching) {
                // 開始擷取本文
                if (fromRow.isEmpty) {
                    // 擷取完畢
                    startCatching = false
                    countRows = 0
                    TempSettings.setHeroStep(heroStep!!)
                } else {
                    countRows++
                    var oldContent = heroStep?.content!!
                    // 第二行開始才加入換行
                    if (!oldContent.isEmpty()) oldContent += "\n"
                    // 塞入本行內容
                    heroStep.content = oldContent + fromRow.toContentString()
                    if (countRows >= 3) {
                        // 最多留言三行, 強制結束
                        startCatching = false
                        countRows = 0
                        TempSettings.setHeroStep(heroStep)
                    }
                }
            } else {
                if (fromRow.rawString.contains("(")) {
                    // 開始擷取
                    startCatching = true
                    countRows = 0
                    val rawString = fromRow.rawString
                    val nameLastIndex = rawString.indexOf(")")
                    val authorName = rawString.substring(0, nameLastIndex + 1).trim()
                    val datetime = rawString.substring(nameLastIndex + 2).trim()
                    heroStep = HeroStep(authorName, datetime, "")
                }
            }
        }
    }

    /**
     * 處理登入頁面
     *
     * 設定當前頁面為登入頁面，並顯示登入畫面。
     */
    fun handleLoginPage() {
        currentPage = BahamutPage.BAHAMUT_LOGIN
        val page: LoginPage = PageContainer.instance!!.loginPage
        if (page.onPagePreload()) {
            showPage(page)
        }
    }

    /**
     * 處理主功能表頁面
     *
     * 登入成功後的主頁面處理，包括：
     * - 初始化訊息小視窗
     * - 處理熱門訊息
     * - 顯示線上人數和呼叫器狀態
     */
    fun handleMainPage() {
        this.nowStep = STEP_WORKING

        if (currentPage < BahamutPage.BAHAMUT_MAIN) {
            PageContainer.instance!!.loginPage.onLoginSuccess()

            // 開啟訊息小視窗
            if (TempSettings.myContext != null) {
                if (getMessageSmall() == null) {
                    // 統計訊息數量
                    val messageSmall = MessageSmall(TempSettings.myContext!!)
                    messageSmall.afterInit()
                    setMessageSmall(messageSmall)
                    ASCoroutine.ensureMainThread {
                        ASNavigationController.currentController?.addForeverView(messageSmall)

                        if (getShowMessageFloating()) {
                            messageSmall.show()
                        } else {
                            messageSmall.hide()
                        }
                    }

                    MessageDatabase(TempSettings.myContext).use { db ->
                        db.getAllAndNewestMessage()
                    }
                }
            }
        }

        currentPage = BahamutPage.BAHAMUT_MAIN
        val page: MainPage = PageContainer.instance!!.mainPage
        if (page.onPagePreload()) {
            showPage(page)
        }

        if (this.lastHeader == "本次") {
            ASCoroutine.ensureMainThread {
                val page2: MainPage = PageContainer.instance!!.mainPage
                if (page2.isTopPage) {
                    page2.onProcessHotMessage()
                }
            }
        } else if (this.lastHeader == "G)") {
            ASCoroutine.ensureMainThread {
                val page2: MainPage = PageContainer.instance!!.mainPage
                if (page2.isTopPage) {
                    page2.onCheckGoodbye()
                }
            }
        }
        if (this.rowStringFinal.contains("[訪客]")) {
            // 紀錄線上人數
            var startIndex = rowStringFinal.indexOf("[訪客]") + 4
            var endIndex = rowStringFinal.indexOf(" 人")
            page.setOnlinePeople(
                this.rowStringFinal.substring(startIndex, endIndex).trim())

            // 紀錄呼叫器
            startIndex = rowStringFinal.indexOf("[呼叫器]") + 5
            endIndex = rowStringFinal.length
            page.setBBCall(this.rowStringFinal.substring(startIndex, endIndex).trim())
        }
    }

    /**
     * 處理郵件信箱頁面
     */
    fun handleMailBoxPage() {
        currentPage = BahamutPage.BAHAMUT_MAIL_BOX
        if (this.telnetCursor!!.column == 1) {
            val page: MailBoxPage = PageContainer.instance!!.mailBoxPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /**
     * 處理搜尋看板功能
     *
     * 當使用者搜尋看板時，讀取搜尋結果並通知 ClassPage 更新。
     */
    fun handleSearchBoard() {
        if (this.rowStringFinal.startsWith("★ 列表") && this.telnetCursor!!.equals(23, 29)) {
            SearchBoardHandler.instance.read()
            TelnetClient.myInstance!!.sendKeyboardInputToServer(67)
        } else if (this.telnetCursor!!.row == 1) {
            SearchBoardHandler.instance.read()
            val data = create().pushKey(TelnetKeyboard.CTRL_Y).pushString("\n\n").build()
            TelnetClient.myInstance!!.sendDataToServer(data)
            ASCoroutine.ensureMainThread {
                val page: ClassPage = PageContainer.instance!!.classPage
                page.onSearchBoardFinished()
            }
        }
    }

    /**
     * 處理看板列表頁面 (分類看板)
     */
    fun handleClassPage() {
        currentPage = BahamutPage.BAHAMUT_CLASS
        if (this.telnetCursor!!.column == 1) {
            val page: ClassPage = PageContainer.instance!!.classPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /**
     * 處理看板主頁面 (文章列表)
     */
    fun handleBoardPage() {
        currentPage = BahamutPage.BAHAMUT_BOARD
        if (this.telnetCursor!!.column == 1) {
            val page: BoardMainPage = PageContainer.instance!!.boardPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /**
     * 處理看板搜尋結果頁面
     */
    fun handleBoardSearchPage() {
        currentPage = BahamutPage.BAHAMUT_BOARD_SEARCH
        if (this.telnetCursor!!.column == 1) {
            val page: BoardSearchPage = PageContainer.instance!!.boardSearchPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /**
     * 處理精華區頁面
     */
    fun handleBoardEssencePage() {
        currentPage = BahamutPage.BAHAMUT_BOARD_ESSENCE
        if (this.telnetCursor!!.column == 1) {
            val page: BoardEssencePage = PageContainer.instance!!.boardEssencePage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /**
     * 處理主題串列頁面 (相同標題的文章串接)
     */
    fun handleBoardTitleLinkedPage() {
        currentPage = BahamutPage.BAHAMUT_BOARD_LINK
        if (this.telnetCursor!!.column == 1) {
            val page: BoardLinkPage = PageContainer.instance!!.boardLinkedTitlePage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /**
     * 處理個人設定頁面
     *
     * 包含個人資料頁面和操作模式設定頁面。
     */
    fun handleUserPage() {
        // 傳給個人設定, 頁面更新資料
        if (this.rowStringFinal.contains("修改資料(Y/N)?[N]")) {
            currentPage = BahamutPage.BAHAMUT_USER_INFO_PAGE
            // 個人資料
            val page: UserInfoPage = PageContainer.instance!!.getUserInfoPage()
            page.updateUserInfoPageContent(telnetRows)
        } else if (this.rowStringFinal.contains("請按鍵切換設定，或按")) {
            currentPage = BahamutPage.BAHAMUT_USER_CONFIG_PAGE
            // 操作模式
            val page: UserConfigPage = PageContainer.instance!!.getUserConfigPage()
            page.updateUserConfigPageContent(telnetRows)
        }
    }

    /**
     * 處理文章閱讀狀態
     *
     * 根據當前最上層頁面類型，設定對應的文章頁面狀態，
     * 並開始讀取文章內容。
     */
    fun handleArticle() {
        val topPage =
            ASNavigationController.currentController?.topController as TelnetPage?
        when (topPage) {
            is BoardMainPage -> {
                currentPage = BahamutPage.BAHAMUT_ARTICLE
            }

            is MailBoxPage -> {
                currentPage = BahamutPage.BAHAMUT_MAIL
            }

            is BoardEssencePage -> {
                currentPage = BahamutPage.BAHAMUT_ARTICLE_ESSENCE
            }
        }

        if (!this.duringReadingArticle) {
            onReadArticleStart()
        }
    }

    /**
     * 處理文章讀取進度百分比
     *
     * 從最後一行解析進度百分比，並更新對應頁面的載入進度顯示。
     */
    fun handleArticlePercentage() {
        val resourceString = "((?<percent>\\d+)%)"
        val pattern = Pattern.compile(resourceString)
        val matcher = pattern.matcher(rowStringFinal)
        if (matcher.find()) {
            val percent = matcher.toMatchResult().group(1)
            val topPage =
                ASNavigationController.currentController?.topController as TelnetPage?
            when (topPage) {
                is ArticleEssencePage -> {
                    topPage.changeLoadingPercentage(percent)
                }

                is MailPage -> {
                    topPage.changeLoadingPercentage(percent)
                }

                is ArticlePage -> {
                    topPage.changeLoadingPercentage(percent)
                }
            }
        }
    }

    /**
     * 處理從串接頁編輯文章的狀態
     *
     * 當使用者從 LinkPage/SearchPage 觸發編輯時，
     * 此函式根據當前狀態執行對應的操作。
     *
     * @return true 如果正在處理編輯流程，false 表示沒有進行中的編輯任務
     */
    fun handleEditFromLinkedState(): Boolean {
        val state = TempSettings.editFromLinkedState ?: return false

        when (state.step) {
            EditFromLinkedStep.MOVE_UP_FOR_BOUNDARY -> {
                // 偵測到 LinkPage/SearchPage，送出 "t"
                if (currentPage == BahamutPage.BAHAMUT_BOARD_LINK ||
                    currentPage == BahamutPage.BAHAMUT_BOARD_SEARCH) {
                    state.step = EditFromLinkedStep.SENT_T
                    create().pushKey(TelnetKeyboard.SMALL_T).sendToServer()
                    return true
                }
            }

            EditFromLinkedStep.SENT_T -> {
                // 解析 row4 取得 boardNumber
                val boardNum = parseBoardNumberFromRow4()
                state.boardNumber = boardNum
                state.isLastArticle = (boardNum == 1)

                // 離開串接頁
                state.step = EditFromLinkedStep.LEAVING_LINKED_PAGE
                create().pushKey(TelnetKeyboard.LEFT_ARROW).sendToServer()
                return true
            }

            EditFromLinkedStep.LEAVING_LINKED_PAGE -> {
                // 偵測到 BoardMainPage
                if (currentPage == BahamutPage.BAHAMUT_BOARD) {
                    state.step = EditFromLinkedStep.ON_BOARD_PAGE

                    ASCoroutine.ensureMainThread {
                        val boardPage = PageContainer.instance!!.boardPage

                        if (state.isLastArticle) {
                            // 例外2: 移到最後
                            state.step = EditFromLinkedStep.GOTO_LAST
                            boardPage.moveToLastPosition()
                        } else {
                            // 正常/例外1: 選擇文章並進入
                            state.step = EditFromLinkedStep.READING_ARTICLE
                            boardPage.setListViewSelection(state.boardNumber - 1)
                            boardPage.loadItemAtIndex(state.boardNumber - 1)
                        }
                    }
                    return true
                }
            }

            EditFromLinkedStep.GOTO_LAST -> {
                // 偵測到 BoardMainPage 已到最後
                if (currentPage == BahamutPage.BAHAMUT_BOARD) {
                    ASCoroutine.ensureMainThread {
                        val boardPage = PageContainer.instance!!.boardPage
                        state.step = EditFromLinkedStep.READING_ARTICLE
                        // 從最後一筆進入
                        boardPage.loadItemAtIndex(boardPage.listView.count - 1)
                    }
                    return true
                }
            }

            else -> {
                // 其他步驟由 ArticlePage 處理
            }
        }
        return false
    }

    /**
     * 從 row4 解析版面文章編號
     *
     * 格式: "  1234  作者  日期  標題"
     *
     * @return 解析出的編號，解析失敗回傳 0
     */
    private fun parseBoardNumberFromRow4(): Int {
        val row4 = getRowString(4).trim()
        val match = Regex("^\\s*(\\d+)").find(row4)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    /**
     * 主要狀態處理函式
     *
     * 繼承自 TelnetStateHandler，當 Telnet 畫面更新時被呼叫。
     * 根據畫面內容判斷當前狀態，並執行對應的處理邏輯。
     *
     * 處理流程：
     * 1. 載入當前畫面狀態
     * 2. 檢查是否在訊息頁面
     * 3. 處理非頁面切換的系統提示
     * 4. 根據畫面內容切換到對應頁面
     */
    override fun handleState() {
        loadState()
        this.telnetCursor = TelnetClient.model.cursor
        val topPage =
            ASNavigationController.currentController?.topController as TelnetPage?

        // 處理從串接頁編輯文章的狀態
        if (handleEditFromLinkedState()) {
            return
        }

        // 狀況：正在重整訊息 或者 訊息主視窗收到訊息
        if (topPage is MessageMain) {
            // HP:MP:會跟網友列表同時發生, 所以判斷要在之前
            if (this.rowString02.contains("HP：") && this.rowString02.contains("MP：")) {
                val page: ArticlePage = PageContainer.instance!!.articlePage
                val userData = Vector<String>()
                this.telnetRows.forEach(Consumer { row: TelnetRow? -> userData.add(row.toString()) })
                page.ctrlQUser(userData)
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (this.rowString00.startsWith("【網友列表】")) {
                // 載入名單
                ASCoroutine.ensureMainThread {
                        topPage.loadUserList(telnetRows)
                    }
            } else if (this.rowStringFinal.contains("瀏覽 P.")) {
                // 正在瀏覽訊息
                topPage.receiveSyncCommand(telnetRows)
                BahamutCommandLoadMoreArticle().execute()
            } else if (this.rowStringFinal.contains("● 請按任意鍵繼續 ●")) {
                // 訊息最後一頁, 還有回到原本的那頁
                topPage.receiveSyncCommand(telnetRows)
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)

                ASCoroutine.ensureMainThread {
                    topPage.loadMessageList()
                }
            } else if (this.rowStringFinal.startsWith("★") && !this.rowStringFinal.substring(
                    1,
                    2
                ).isEmpty()
            ) {
                detectMessage()
            }
        } else if (topPage is MessageSub) {
            if (this.rowStringFinal.contains("對方關掉呼叫器了")) {
                topPage.sendMessageFail(MessageStatus.CloseBBCall)
                showLongToast("對方關掉呼叫器了")
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (this.rowStringFinal.contains("對方已經離去")) {
                topPage.sendMessageFail(MessageStatus.Escape)
                showLongToast("對方已經離去")
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (getRowString(22).trim().startsWith("★熱訊：")) {
                // 送出給對方的訊息
                // 一定要在"傳訊給"判斷之前, 因為這兩個判斷會同時出現
                topPage.sendMessagePart3()
            } else if (this.rowString01.startsWith("傳訊給")) {
                // 送出對方id
                // 一定要在"熱訊回應"判斷之前, 因為這兩個判斷會同時出現
                topPage.sendMessagePart2()
            } else if (getRowString(22).trim().contains("熱訊回應")) {
                // 我方發出ctrl+S, 但是被熱訊回應卡住, 送出Enter指令接傳訊給對方id
                TelnetClient.myInstance!!.sendStringToServer("")
            } else if (this.rowStringFinal.startsWith("★") && !this.rowStringFinal.substring(
                    1,
                    2
                ).isEmpty()
            ) {
                detectMessage()
            }
        } else if (handleNonPageSwitching()) {
            if (currentPage == BahamutPage.BAHAMUT_CLASS && this.rowStringFinal.contains(
                    "瀏覽 P."
                ) && this.rowStringFinal.endsWith("結束")
            ) {
                BahamutCommandLoadMoreArticle().execute()
            } else if (currentPage > BahamutPage.BAHAMUT_MAIL_BOX && this.rowStringFinal.contains(
                    "文章選讀"
                ) && this.rowStringFinal.endsWith("搜尋作者")
            ) {
                handleArticle()
                onReadArticleFinished()
                // 串接文章狀況下, 文章讀取完畢指令不同, 但是第23行內容一樣,會誤判,因此根據之前最後一頁判斷狀況
                val lastPage = currentPage
                if (lastPage == BahamutPage.BAHAMUT_ARTICLE) BahamutCommandLoadArticleEnd().execute()
                else BahamutCommandLoadArticleEndForSearch().execute()
            } else if (currentPage > BahamutPage.BAHAMUT_CLASS && this.rowStringFinal.contains(
                    "瀏覽 P."
                ) && this.rowStringFinal.endsWith("結束")
            ) {
                handleArticle()
                onReadArticlePage()
                handleArticlePercentage()
                BahamutCommandLoadMoreArticle().execute()
            } else if (currentPage > BahamutPage.BAHAMUT_CLASS && this.rowStringFinal.contains(
                    "魚雁往返"
                ) && this.rowStringFinal.endsWith("標記")
            ) {
                handleArticle()
                onReadArticleFinished()
                BahamutCommandLoadArticleEnd().execute()
            } else if (currentPage > BahamutPage.BAHAMUT_CLASS && this.rowStringFinal.contains(
                    "閱讀精華"
                ) && this.rowStringFinal.trim().endsWith("離開")
            ) {
                handleArticle()
                onReadArticleFinished()
                BahamutCommandLoadArticleEnd().execute()
            } else if (this.firstHeader == "對戰" && currentPage < 5) {
                handleLoginPage()
            } else if (this.rowString00.contains("【主功能表】")) {
                handleMainPage()
            } else if (this.rowString00.contains("【郵件選單】")) {
                handleMailBoxPage()
            } else if (this.rowString00.contains("【看板列表】")) {
                if (this.rowString01.contains("請輸入看板名稱")) {
                    handleSearchBoard()
                } else if (this.rowString02.contains("總數")) {
                    TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_C)
                } else {
                    handleClassPage()
                }
            } else if (this.rowString00.contains("【主題串列】")) {
                if (PageContainer.instance!!.boardPage.lastListAction == BoardPageAction.SEARCH
                ) {
                    handleBoardSearchPage()
                } else {
                    handleBoardTitleLinkedPage()
                }
            } else if (this.rowString00.contains("【精華文章】")) {
                handleBoardEssencePage()
            } else if (this.rowString00.startsWith("【板主：") && this.rowString00.contains("看板《")) {
                if (rowStringFinal.startsWith("推文(系統測試中)：")) {
                    // 呼叫推文訊息視窗
                    ASCoroutine.ensureMainThread {
                        PageContainer.instance!!.boardPage.openPushArticleDialog()
                    }
                    return
                }
                handleBoardPage()
            } else if (this.rowString00.contains("【個人設定】")) {
                handleUserPage()
            } else if (this.rowStringFinal.contains("您要刪除上述記錄嗎")) {
                TelnetClient.myInstance!!.sendStringToServer("n")
            } else if (this.rowStringFinal == "● 請按任意鍵繼續 ●") {
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (this.lastHeader == "您想") {
                ASCoroutine.ensureMainThread {
                    val page: LoginPage = PageContainer.instance!!.loginPage
                    if (page.isTopPage) {
                        page.onSaveArticle()
                    }
                }
            } else if (this.rowStringFinal.contains("★ 請閱讀最新公告")) {
                TelnetClient.myInstance!!.sendStringToServer("")
            } else if (this.nowStep == STEP_CONNECTING && this.firstHeader == "--") {
                // TODO: 不知道甚麼狀況
                currentPage = BahamutPage.BAHAMUT_INSTRUCTIONS
                if (this.lastHeader == "●請" || this.lastHeader == "請按") {
                    TelnetClient.myInstance!!.sendStringToServer("")
                }
            } else if (this.nowStep == STEP_CONNECTING && this.firstHeader == "□□") {
                currentPage = BahamutPage.BAHAMUT_SYSTEM_ANNOUNCEMENT
                if (this.lastHeader == "●請" || this.lastHeader == "請按") {
                    TelnetClient.myInstance!!.sendStringToServer("")
                }
            } else if (this.firstHeader == "【過") {
                currentPage = BahamutPage.BAHAMUT_PASSED_SIGNATURE
                if (this.lastHeader == "●請" || this.lastHeader == "請按") {
                    TelnetClient.myInstance!!.sendStringToServer("")
                }
            }
        }
    }

    /**
     * 開始讀取文章
     *
     * 設定讀取狀態旗標並清除文章處理器的內容。
     */
    fun onReadArticleStart() {
        this.duringReadingArticle = true
        this.articleHandler.clear()
    }

    /**
     * 讀取文章的一頁
     *
     * 將當前畫面內容載入文章處理器，並清除畫面緩衝。
     */
    fun onReadArticlePage() {
        this.articleHandler.loadPage(TelnetClient.model)
        cleanFrame()
    }

    /**
     * 完成文章讀取
     *
     * 載入最後一頁內容，建立文章物件，
     * 並根據文章類型 (一般文章/郵件/精華) 顯示對應頁面。
     */
    fun onReadArticleFinished() {
        this.articleHandler.loadLastPage(TelnetClient.model)
        this.articleHandler.build()
        val article = this.articleHandler.article
        this.articleHandler.newArticle()

        try {
            article.articleNumber = this.myArticleNumber.toInt()
            // 如果現在是 boardPage, 則更新文章編號
            if (currentPage == BahamutPage.BAHAMUT_BOARD)
                article.boardNumber = article.articleNumber
        } catch (_: Exception) {
            article.articleNumber = 0
        }
        if (this.rowStringFinal.contains("魚雁往返")) {
            showMail(article)
        } else if (this.rowStringFinal.contains("閱讀精華")) {
            showEssence(article)
        } else {
            showArticle(article)
        }
        this.duringReadingArticle = false
    }

    /**
     * 顯示一般文章內容
     *
     * @param aArticle 要顯示的文章物件
     */
    fun showArticle(aArticle: TelnetArticle) {
        ASCoroutine.ensureMainThread {
            try {
                PageContainer.instance!!.articlePage.setArticle(aArticle)
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
            }
        }
    }

    /**
     * 顯示郵件內容
     *
     * 如果最上層不是 MailPage，則建立新的 MailPage 並推入導航堆疊。
     *
     * @param aArticle 要顯示的郵件物件
     */
    fun showMail(aArticle: TelnetArticle) {
        ASCoroutine.ensureMainThread {
            val mailPage: MailPage?
            try {
                val lastPage =
                    ASNavigationController.currentController!!.viewControllers.lastElement() as TelnetPage?
                // 檢查最上層的頁面是不是 mail page
                // 如果不是=>就把mail page推到最上層
                if (lastPage == null || lastPage.pageType != BahamutPage.BAHAMUT_MAIL) {
                    mailPage = MailPage()
                    ASNavigationController.currentController!!.pushViewController(mailPage)
                } else {
                    mailPage = lastPage as MailPage
                }
                mailPage.setArticle(aArticle)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * 顯示精華區文章內容
     *
     * 如果最上層不是 ArticleEssencePage，則建立新的頁面並推入導航堆疊。
     *
     * @param aArticle 要顯示的精華文章物件
     */
    fun showEssence(aArticle: TelnetArticle?) {
        ASCoroutine.ensureMainThread {
            val articleEssencePage: ArticleEssencePage?
            try {
                val lastPage =
                    ASNavigationController.currentController!!.viewControllers
                        .lastElement() as TelnetPage?
                // 檢查最上層的頁面是不是 mail page
                // 如果不是=>就把mail page推到最上層
                if (lastPage == null || lastPage.pageType != BahamutPage.BAHAMUT_ARTICLE_ESSENCE) {
                    articleEssencePage = ArticleEssencePage()
                    ASNavigationController.currentController!!.pushViewController(articleEssencePage)
                } else {
                    articleEssencePage = lastPage as ArticleEssencePage
                }
                articleEssencePage.setArticle(aArticle)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * 通用顯示頁面方法
     *
     * 根據目標頁面與當前堆疊的關係，執行對應的導航操作：
     * - 如果是同一頁面，則重新整理
     * - 如果已在堆疊中，則 pop 到該頁面
     * - 否則推入新頁面
     *
     * @param aPage 要顯示的頁面
     */
    fun showPage(aPage: TelnetPage?) {
        val currentController = ASNavigationController.currentController
        if (currentController != null) {
            val topPage = currentController.topController as TelnetPage?
            if (aPage === topPage) {
                ASCoroutine.ensureMainThread {
                    topPage?.requestPageRefresh()
                }
            } else if (topPage != null && !topPage.isPopupPage && aPage != null) {
                if (currentController.containsViewController(aPage)) {
                    currentController.popToViewController(aPage)
                } else {
                    currentController.pushViewController(aPage)
                }
            }
        }
    }

    /**
     * 清除狀態處理器
     *
     * 繼承自 TelnetStateHandler，重置連線狀態和當前頁面。
     */
    override fun clear() {
        this.nowStep = STEP_CONNECTING
        currentPage = BahamutPage.UNKNOWN
    }

    /**
     * 截取「請按任意鍵繼續」提示中的訊息內容
     *
     * 移除前導的星號和後続的 [請按任意鍵繼續] 提示，
     * 只保留實際的訊息內容。
     *
     * @param aMessage 原始訊息字串
     * @return 截取後的訊息內容，若無則回傳空字串
     */
    fun cutOffContinueMessage(aMessage: String): String {
        var start = 0
        var end = aMessage.length - 1
        val words = aMessage.toCharArray()
        while (start < words.size) {
            if (words[start].code == 9733 || words[start].code == 32) start++
            else break
        }
        while (words[end] != '[') {
            end--
        }
        if (end <= start) {
            return ""
        }
        return aMessage.substring(start, end).trim()
    }

    companion object {
        /** 連線中狀態 */
        const val STEP_CONNECTING: Int = 0
        /** 工作中狀態 (已登入) */
        const val STEP_WORKING: Int = 1
        /** 未知狀態 */
        const val UNKNOWN: Int = -1
        /** 單例實例 */
        var bahamutStateHandler: BahamutStateHandler? = null

        /**
         * 取得 BahamutStateHandler 單例實例
         *
         * 採用懶漢初始化模式，首次呼叫時建立實例。
         *
         * @return BahamutStateHandler 實例
         */
        fun getInstance(): BahamutStateHandler {
            if (bahamutStateHandler == null) {
                bahamutStateHandler = BahamutStateHandler()
            }

            return bahamutStateHandler!!
        }
    }
}
