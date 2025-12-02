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
import com.kota.Bahamut.service.NotificationSettings.getShowMessageFloating
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.Bahamut.service.TempSettings.getNotReadMessageCount
import com.kota.Bahamut.service.TempSettings.setMessageSmall
import com.kota.Bahamut.service.TempSettings.setNotReadMessageCount
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASViewController
import com.kota.asFramework.thread.ASRunner
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

class BahamutStateHandler internal constructor() : TelnetStateHandler() {
    var myArticleNumber: String = ""
    var nowStep: Int = 0
    var telnetRows: Vector<TelnetRow> = Vector<TelnetRow>() // debug用
    var rowString00: String = ""
    var rowString01: String = ""
    var rowString02: String = ""
    var rowStringFinal: String = ""
    var firstHeader: String = ""
    var lastHeader: String = ""
    var telnetCursor: TelnetCursor? = null
    val articleHandler: ArticleHandler = ArticleHandler()
    var duringReadingArticle: Boolean = false // 正在讀取文章

    /** 設定文章編號  */
    fun setArticleNumber(aArticleNumber: String) {
        this.myArticleNumber = aArticleNumber
    }

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
     * 接收到訊息
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

    /** 處理非切換主頁面的需求  */
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
                    val controllers: Vector<ASViewController> = ASNavigationController.currentController!!.allController
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

    /** 逐行塞入勇者足跡  */
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

    fun handleLoginPage() {
        currentPage = BahamutPage.BAHAMUT_LOGIN
        val page: LoginPage = PageContainer.instance!!.loginPage
        if (page.onPagePreload()) {
            showPage(page)
        }
    }

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
                    object : ASRunner() {
                        override fun run() {
                            ASNavigationController.currentController?.addForeverView(messageSmall)

                            if (getShowMessageFloating()) {
                                messageSmall.show()
                            } else {
                                messageSmall.hide()
                            }
                        }
                    }.runInMainThread()

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
            object : ASRunner() {
                override fun run() {
                    val page2: MainPage = PageContainer.instance!!.mainPage
                    if (page2.isTopPage) {
                        page2.onProcessHotMessage()
                    }
                }
            }.runInMainThread()
        } else if (this.lastHeader == "G)") {
            object : ASRunner() {
                override fun run() {
                    val page2: MainPage = PageContainer.instance!!.mainPage
                    if (page2.isTopPage) {
                        page2.onCheckGoodbye()
                    }
                }
            }.runInMainThread()
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

    fun handleMailBoxPage() {
        currentPage = BahamutPage.BAHAMUT_MAIL_BOX
        if (this.telnetCursor!!.column == 1) {
            val page: MailBoxPage = PageContainer.instance!!.mailBoxPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleSearchBoard() {
        if (this.rowStringFinal.startsWith("★ 列表") && this.telnetCursor!!.equals(23, 29)) {
            SearchBoardHandler.instance.read()
            TelnetClient.myInstance!!.sendKeyboardInputToServer(67)
        } else if (this.telnetCursor!!.row == 1) {
            SearchBoardHandler.instance.read()
            val data = create().pushKey(TelnetKeyboard.CTRL_Y).pushString("\n\n").build()
            TelnetClient.myInstance!!.sendDataToServer(data)
            object : ASRunner() {
                override fun run() {
                    val page: ClassPage = PageContainer.instance!!.classPage
                    page.onSearchBoardFinished()
                }
            }.runInMainThread()
        }
    }

    fun handleClassPage() {
        currentPage = BahamutPage.BAHAMUT_CLASS
        if (this.telnetCursor!!.column == 1) {
            val page: ClassPage = PageContainer.instance!!.classPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleBoardPage() {
        currentPage = BahamutPage.BAHAMUT_BOARD
        if (this.telnetCursor!!.column == 1) {
            val page: BoardMainPage = PageContainer.instance!!.boardPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleBoardSearchPage() {
        currentPage = BahamutPage.BAHAMUT_BOARD_SEARCH
        if (this.telnetCursor!!.column == 1) {
            val page: BoardSearchPage = PageContainer.instance!!.boardSearchPage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleBoardEssencePage() {
        currentPage = BahamutPage.BAHAMUT_BOARD_ESSENCE
        if (this.telnetCursor!!.column == 1) {
            val page: BoardEssencePage = PageContainer.instance!!.boardEssencePage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleBoardTitleLinkedPage() {
        currentPage = BahamutPage.BAHAMUT_BOARD_LINK
        if (this.telnetCursor!!.column == 1) {
            val page: BoardLinkPage = PageContainer.instance!!.boardLinkedTitlePage
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /** 頁面: 個人設定   */
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

    // 變更讀取條進度
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

    // com.kota.telnet.TelnetStateHandler
    override fun handleState() {
        loadState()
        this.telnetCursor = TelnetClient.model.cursor
        val topPage =
            ASNavigationController.currentController?.topController as TelnetPage?


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
                object : ASRunner() {
                    override fun run() {
                        topPage.loadUserList(telnetRows)
                    }
                }.runInMainThread()
            } else if (this.rowStringFinal.contains("瀏覽 P.")) {
                // 正在瀏覽訊息
                topPage.receiveSyncCommand(telnetRows)
                BahamutCommandLoadMoreArticle().execute()
            } else if (this.rowStringFinal.contains("● 請按任意鍵繼續 ●")) {
                // 訊息最後一頁, 還有回到原本的那頁
                topPage.receiveSyncCommand(telnetRows)
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SPACE)

                object : ASRunner() {
                    override fun run() {
                        topPage.loadMessageList()
                    }
                }.runInMainThread()
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
                    TelnetClient.myInstance!!.sendKeyboardInputToServer(99)
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
                    object : ASRunner() {
                        override fun run() {
                            PageContainer.instance!!.boardPage.openPushArticleDialog()
                        }
                    }.runInMainThread()
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
                object : ASRunner() {
                    override fun run() {
                        val page: LoginPage = PageContainer.instance!!.loginPage
                        if (page.isTopPage) {
                            page.onSaveArticle()
                        }
                    }
                }.runInMainThread()
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

    fun onReadArticleStart() {
        this.duringReadingArticle = true
        this.articleHandler.clear()
    }

    fun onReadArticlePage() {
        this.articleHandler.loadPage(TelnetClient.model)
        cleanFrame()
    }

    fun onReadArticleFinished() {
        this.articleHandler.loadLastPage(TelnetClient.model)
        this.articleHandler.build()
        val article = this.articleHandler.article
        this.articleHandler.newArticle()

        try {
            article.myNumber = this.myArticleNumber.toInt()
        } catch (_: Exception) {
            article.myNumber = 0
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

    // 顯示文章內文
    fun showArticle(aArticle: TelnetArticle) {
        object : ASRunner() {
            override fun run() {
                try {
                    PageContainer.instance!!.articlePage.setArticle(aArticle)
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                }
            }
        }.runInMainThread()
    }

    // 顯示郵件內文
    fun showMail(aArticle: TelnetArticle) {
        object : ASRunner() {
            override fun run() {
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
        }.runInMainThread()
    }

    // 顯示精華區內文
    fun showEssence(aArticle: TelnetArticle?) {
        object : ASRunner() {
            override fun run() {
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
        }.runInMainThread()
    }

    // 通用顯示頁面
    fun showPage(aPage: TelnetPage?) {
        val currentController = ASNavigationController.currentController
        if (currentController != null) {
            val topPage = currentController.topController as TelnetPage?
            if (aPage === topPage) {
                object : ASRunner() {
                    override fun run() {
                        topPage?.requestPageRefresh()
                    }
                }.runInMainThread()
            } else if (topPage != null && !topPage.isPopupPage && aPage != null) {
                if (currentController.containsViewController(aPage)) {
                    currentController.popToViewController(aPage)
                } else {
                    currentController.pushViewController(aPage)
                }
            }
        }
    }

    // com.kota.telnet.TelnetStateHandler
    override fun clear() {
        this.nowStep = STEP_CONNECTING
        currentPage = BahamutPage.UNKNOWN
    }

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
        const val STEP_CONNECTING: Int = 0
        const val STEP_WORKING: Int = 1
        const val UNKNOWN: Int = -1
        var bahamutStateHandler: BahamutStateHandler? = null

        /** 回傳instance給其他頁面使用  */
        fun getInstance(): BahamutStateHandler {
            if (bahamutStateHandler == null) {
                bahamutStateHandler = BahamutStateHandler()
            }

            return bahamutStateHandler!!
        }
    }
}
