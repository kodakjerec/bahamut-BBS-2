package com.kota.Bahamut

import android.util.Log
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASNavigationController.pushViewController
import com.kota.asFramework.pageController.ASViewController
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASSnackBar.show
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.command.BahamutCommandLoadArticleEnd
import com.kota.Bahamut.command.BahamutCommandLoadArticleEndForSearch
import com.kota.Bahamut.command.BahamutCommandLoadMoreArticle
import com.kota.Bahamut.pages.articlePage.ArticlePage
import com.kota.Bahamut.pages.articlePage.ArticlePage.setArticle
import com.kota.Bahamut.pages.bbsUser.UserConfigPage
import com.kota.Bahamut.pages.bbsUser.UserInfoPage
import com.kota.Bahamut.pages.boardPage.BoardLinkPage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.boardPage.BoardMainPage.cancelRunner
import com.kota.Bahamut.pages.boardPage.BoardMainPage.lastListAction
import com.kota.Bahamut.pages.boardPage.BoardMainPage.openPushArticleDialog
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.pages.boardPage.BoardSearchPage
import com.kota.Bahamut.pages.ClassPage
import com.kota.Bahamut.pages.essencePage.ArticleEssencePage
import com.kota.Bahamut.pages.essencePage.ArticleEssencePage.setArticle
import com.kota.Bahamut.pages.essencePage.BoardEssencePage
import com.kota.Bahamut.pages.login.LoginPage
import com.kota.Bahamut.pages.login.LoginPage.onLoginSuccess
import com.kota.Bahamut.pages.MailBoxPage
import com.kota.Bahamut.pages.MailPage
import com.kota.Bahamut.pages.MailPage.setArticle
import com.kota.Bahamut.pages.MainPage
import com.kota.Bahamut.pages.messages.BahaMessage
import com.kota.Bahamut.pages.messages.MessageDatabase
import com.kota.Bahamut.pages.messages.MessageMain
import com.kota.Bahamut.pages.messages.MessageSmall
import com.kota.Bahamut.pages.messages.MessageStatus
import com.kota.Bahamut.pages.messages.MessageSub
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.service.HeroStep
import com.kota.Bahamut.service.NotificationSettings.getShowMessageFloating
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.Bahamut.service.TempSettings.getNotReadMessageCount
import com.kota.Bahamut.service.TempSettings.setMessageSmall
import com.kota.Bahamut.service.TempSettings.setNotReadMessageCount
import com.kota.telnet.logic.ArticleHandler
import com.kota.telnet.logic.SearchBoardHandler
import com.kota.telnet.model.TelnetModel.cursor
import com.kota.telnet.model.TelnetModel.getRow
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.model
import com.kota.telnet.TelnetCursor
import com.kota.telnet.TelnetCursor.equals
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.TelnetStateHandler
import com.kota.telnet.TelnetUtils.getHeader
import com.kota.telnetUI.TelnetPage
import com.kota.textEncoder.B2UEncoder
import java.io.ByteArrayOutputStream
import java.util.Vector
import java.util.function.Consumer
import java.util.regex.Pattern

class BahamutStateHandler internal constructor() : TelnetStateHandler() {
    var articleNumber: String? = null
    var nowStep: Int = 0
    var rows: Vector<TelnetRow> = Vector<TelnetRow>() // debug用
    var row_string_00: String = ""
    var row_string_01: String = ""
    var row_string_02: String = ""
    var row_string_final: String = ""
    var firstHeader: String = ""
    var lastHeader: String = ""
    var telnetCursor: TelnetCursor? = null
    val articleHandler: ArticleHandler = ArticleHandler()
    var duringReadingArticle: Boolean = false // 正在讀取文章

    /** 設定文章編號  */
    fun setArticleNumber(aArticleNumber: String?) {
        this.articleNumber = aArticleNumber
    }

    fun loadState() {
        this.row_string_00 = getRowString(0).trim { it <= ' ' }
        this.row_string_01 = getRowString(1).trim { it <= ' ' }
        this.row_string_02 = getRowString(2).trim { it <= ' ' }
        // 隨然是取row 23, 但是偶爾遇到排版不正確情況, 取row 22, 以此類推
        // row 2 已經有使用, 當作界線
        var i = 23
        while (i > 2) {
            this.row_string_final = getRowString(i).trim { it <= ' ' }
            if (!this.row_string_final.isEmpty()) i = 0
            i--
        }
        rows.clear()
        rows.addAll((rows.clone() as kotlin.collections.MutableCollection<out TelnetRow?>?)!!)
        this.firstHeader = getHeader(this.row_string_00)
        this.lastHeader = getHeader(this.row_string_final)
    }

    /*
    void printState() {
        System.out.println("v********************************************************************************v");
        System.out.println("Current Page:" + getCurrentPage());
        String telnet_screen = "\n";
        for (int i = 0; i < 24; i++) {
            telnet_screen = telnet_screen + String.format("%1$02d.%2$s\n", Integer.valueOf(i + 1), TelnetClient.getModel().getRow(i).getRawString());
        }
        System.out.println("content:" + telnet_screen);
        System.out.println("cursor:" + TelnetClient.getModel().getCursor().toString());
        System.out.println("^********************************************************************************^");
    }
     */
    /**
     * 接收到訊息
     */
    fun detectMessage() {
        val column = this.telnetCursor!!.column
        val row: TelnetRow? = TelnetClient.model.getRow(23)
        val name_buffer = ByteArrayOutputStream(80)
        val msg_buffer = ByteArrayOutputStream(80)
        var end_point = -1
        var i = 0
        while (true) {
            if (i >= row!!.data.size) {
                break
            }
            val background_color = row.backgroundColor[i]
            val data = row.data[i]
            if (background_color.toInt() == 6) {
                name_buffer.write(data.toInt())
            } else if (background_color.toInt() == 5) {
                msg_buffer.write(data.toInt())
            } else {
                end_point = i
                break
            }
            i++
        }
        if (name_buffer.size() > 0 && msg_buffer.size() > 0) {
            val name: String = B2UEncoder.getInstance().encodeToString(name_buffer.toByteArray())
            val msg: String = B2UEncoder.getInstance().encodeToString(msg_buffer.toByteArray())
            if (end_point == column && name.startsWith("★")) {
                val name2 = name.substring(1, name.length - 1).trim { it <= ' ' }
                val msg2 = msg.substring(1).trim { it <= ' ' }
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
                            javaClass.getSimpleName(),
                            (if (e.message != null) e.message else "")!!
                        )
                    }

                    if (bahaMessage != null) {
                        val topPage = ASNavigationController.getCurrentController()
                            .getTopController() as TelnetPage?
                        if (topPage is MessageMain) {
                            // 顯示訊息
                            show(name2, msg2)
                            val aPage = topPage
                            aPage.loadMessageList(bahaMessage)
                        } else if (topPage is MessageSub) {
                            val aPage = topPage
                            aPage.insertMessage(bahaMessage)
                        } else {
                            // 如果是其他頁面:顯示訊息
                            show(name2, msg2)
                        }
                    }

                    TempSettings.lastReceivedMessage = name2 + msg2
                }
            }
        }
    }

    /** 處理非切換主頁面的需求  */
    fun pass_1(): Boolean {
        // 本文
        var run_pass_2 = true
        if (this.row_string_final.contains("您有一篇文章尚未完成")) {
            TelnetClient.getClient().sendStringToServer("S\n1\n")
            run_pass_2 = false
        }
        if (run_pass_2 && this.row_string_final.contains("[請按任意鍵繼續]") && currentPage != BahamutPage.Companion.BAHAMUT_LOGIN) {
            val continue_message = cutOffContinueMessage(this.row_string_final)
            if (!continue_message.isEmpty()) {
                if (continue_message.contains("推文") || continue_message.contains("請稍後片刻")) {
                    PageContainer.Companion.getInstance().getBoardPage().cancelRunner()
                }
                showShortToast(continue_message)
            }
            if (this.row_string_final.contains("★ 引言太多")) {
                // 放棄此次編輯內容
                val data = create()
                    .pushKey(TelnetKeyboard.SPACE)
                    .build()
                TelnetClient.getClient().sendDataToServer(data)
                val topPage =
                    ASNavigationController.getCurrentController().getTopController() as TelnetPage?
                if (topPage is MailBoxPage) {
                    val page2: MailBoxPage = PageContainer.Companion.getInstance().getMailBoxPage()
                    page2.recoverPost()
                } else if (topPage is PostArticlePage) {
                    // 最上層是 發文 或 看板
                    // 清除最先遇到的 BoardSearch, BoardLink, BoardMain
                    val controllers: Vector<ASViewController?> =
                        ASNavigationController.getCurrentController().getAllController()
                    for (i in controllers.size downTo 1) {
                        val nowPage = controllers.get(i - 1) as TelnetPage

                        if (nowPage.javaClass == BoardMainPage::class.java) {
                            val page: BoardMainPage =
                                PageContainer.Companion.getInstance().getBoardPage()
                            page.recoverPost()
                            return false
                        } else if (nowPage.javaClass == BoardLinkPage::class.java) {
                            val page: BoardLinkPage =
                                PageContainer.Companion.getInstance().getBoardLinkedTitlePage()
                            page.recoverPost()
                            return false
                        } else if (nowPage.javaClass == BoardSearchPage::class.java) {
                            val page: BoardSearchPage =
                                PageContainer.Companion.getInstance().getBoardSearchPage()
                            page.recoverPost()
                            return false
                        }
                    }
                }
                return false
            }
            TelnetClient.getClient().sendStringToServer("")
            return false
        } else if (this.row_string_final.contains("要新增資料嗎？(Y/N) [N]")) {
            showShortToast("此看板無文章")
            TelnetClient.getClient().sendStringToServer("N")
            return false
        } else if (this.row_string_final.contains("● 請按任意鍵繼續 ●")) {
            if (this.row_string_00.contains("順利貼出佈告")) {
                // 順利貼出佈告, 請按任意鍵繼續
                val topPage =
                    ASNavigationController.getCurrentController().getTopController() as TelnetPage?
                if (topPage is PostArticlePage || topPage is BoardMainPage) {
                    // 最上層是 發文 或 看板
                    val page: BoardMainPage = PageContainer.Companion.getInstance().getBoardPage()
                    page.finishPost()
                } else if (topPage is MailBoxPage) {
                    val page2: MailBoxPage = PageContainer.Companion.getInstance().getMailBoxPage()
                    page2.finishPost()
                }
            } else if (this.row_string_02.contains("HP：") && this.row_string_02.contains("MP：")) {
                val page: ArticlePage = PageContainer.Companion.getInstance().getArticlePage()
                val userData = Vector<String?>()
                this.rows.forEach(Consumer { row: TelnetRow? -> userData.add(row.toString()) })
                page.ctrlQUser(userData)
            } else if (this.row_string_00.contains("過  路  勇  者  的  足  跡")) {
                // 逐行塞入勇者足跡
                insertHeroSteps()
            }

            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            return false
        } else if (this.row_string_final.contains("請按 [SPACE] 繼續觀賞") && this.row_string_00.contains(
                "過  路  勇  者  的  足  跡"
            )
        ) {
            // 逐行塞入勇者足跡
            insertHeroSteps()
            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            return false
        } else if (this.row_string_final.startsWith("★") && !this.row_string_final.substring(1, 2)
                .isEmpty()
        ) {
            detectMessage()
            return false
        } else {
            return run_pass_2
        }
    }

    /** 逐行塞入勇者足跡  */
    fun insertHeroSteps() {
        var startCatching = false
        var heroStep: HeroStep? = null
        var countRows = 0
        for (i in rows.indices) {
            val fromRow: TelnetRow = rows.get(i)
            if (startCatching) {
                // 開始擷取本文
                if (fromRow.isEmpty) {
                    // 擷取完畢
                    startCatching = false
                    countRows = 0
                    TempSettings.setHeroStep(heroStep!!)
                } else {
                    countRows++
                    var oldContent = heroStep!!.content
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
                    val authorName = rawString.substring(0, nameLastIndex + 1).trim { it <= ' ' }
                    val datetime = rawString.substring(nameLastIndex + 2).trim { it <= ' ' }
                    heroStep = HeroStep(authorName, datetime, "")
                }
            }
        }
    }

    fun handleLoginPage() {
        currentPage = BahamutPage.Companion.BAHAMUT_LOGIN
        val page: LoginPage = PageContainer.Companion.getInstance().getLoginPage()
        if (page.onPagePreload()) {
            showPage(page)
        }
    }

    fun handleMainPage() {
        this.nowStep = STEP_WORKING

        if (currentPage < BahamutPage.Companion.BAHAMUT_MAIN) {
            PageContainer.Companion.getInstance().getLoginPage().onLoginSuccess()

            // 開啟訊息小視窗
            if (TempSettings.myContext != null) {
                if (getMessageSmall() == null) {
                    // 統計訊息數量
                    val messageSmall = MessageSmall(TempSettings.myContext!!)
                    messageSmall.afterInit()
                    setMessageSmall(messageSmall)
                    object : ASRunner() {
                        public override fun run() {
                            ASNavigationController.getCurrentController()
                                .addForeverView(messageSmall)

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

        currentPage = BahamutPage.Companion.BAHAMUT_MAIN
        val page: MainPage = PageContainer.Companion.getInstance().getMainPage()
        if (page.onPagePreload()) {
            showPage(page)
        }

        if (this.lastHeader == "本次") {
            object : ASRunner() {
                // from class: com.kota.Bahamut.BahamutStateHandler.1
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    val page2: MainPage = PageContainer.Companion.getInstance().getMainPage()
                    if (page2.isTopPage) {
                        page2.onProcessHotMessage()
                    }
                }
            }.runInMainThread()
        } else if (this.lastHeader == "G)") {
            object : ASRunner() {
                // from class: com.kota.Bahamut.BahamutStateHandler.2
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    val page2: MainPage = PageContainer.Companion.getInstance().getMainPage()
                    if (page2.isTopPage) {
                        page2.onCheckGoodbye()
                    }
                }
            }.runInMainThread()
        }
        if (this.row_string_final.contains("[訪客]")) {
            // 紀錄線上人數
            var startIndex = row_string_final.indexOf("[訪客]") + 4
            var endIndex = row_string_final.indexOf(" 人")
            page.setOnlinePeople(
                this.row_string_final.substring(startIndex, endIndex).trim { it <= ' ' })

            // 紀錄呼叫器
            startIndex = row_string_final.indexOf("[呼叫器]") + 5
            endIndex = row_string_final.length
            page.setBBCall(this.row_string_final.substring(startIndex, endIndex).trim { it <= ' ' })
        }
    }

    fun handleMailBoxPage() {
        currentPage = BahamutPage.Companion.BAHAMUT_MAIL_BOX
        if (this.telnetCursor!!.column == 1) {
            val page: MailBoxPage = PageContainer.Companion.getInstance().getMailBoxPage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleSearchBoard() {
        if (this.row_string_final.startsWith("★ 列表") && this.telnetCursor!!.equals(23, 29)) {
            SearchBoardHandler.instance.read()
            TelnetClient.getClient().sendKeyboardInputToServer(67)
        } else if (this.telnetCursor!!.row == 1) {
            SearchBoardHandler.instance.read()
            val data = create().pushKey(TelnetKeyboard.CTRL_Y).pushString("\n\n").build()
            TelnetClient.getClient().sendDataToServer(data)
            object : ASRunner() {
                // from class: com.kota.Bahamut.BahamutStateHandler.3
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    val page: ClassPage = PageContainer.Companion.getInstance().getClassPage()
                    page.onSearchBoardFinished()
                }
            }.runInMainThread()
        }
    }

    fun handleClassPage() {
        val page: ClassPage?
        currentPage = BahamutPage.Companion.BAHAMUT_CLASS
        if (this.telnetCursor!!.column == 1 && (PageContainer.Companion.getInstance().getClassPage()
                .also { page = it }) != null && page!!.onPagePreload()
        ) {
            showPage(page)
        }
    }

    fun handleBoardPage() {
        currentPage = BahamutPage.Companion.BAHAMUT_BOARD
        if (this.telnetCursor!!.column == 1) {
            val page: BoardMainPage = PageContainer.Companion.getInstance().getBoardPage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleBoardSearchPage() {
        currentPage = BahamutPage.Companion.BAHAMUT_BOARD_SEARCH
        if (this.telnetCursor!!.column == 1) {
            val page: BoardSearchPage = PageContainer.Companion.getInstance().getBoardSearchPage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleBoardEssencePage() {
        currentPage = BahamutPage.Companion.BAHAMUT_BOARD_ESSENCE
        if (this.telnetCursor!!.column == 1) {
            val page: BoardEssencePage = PageContainer.Companion.getInstance().getBoardEssencePage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    fun handleBoardTitleLinkedPage() {
        currentPage = BahamutPage.Companion.BAHAMUT_BOARD_LINK
        if (this.telnetCursor!!.column == 1) {
            val page: BoardLinkPage =
                PageContainer.Companion.getInstance().getBoardLinkedTitlePage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /** 頁面: 個人設定   */
    fun handleUserPage() {
        // 傳給個人設定, 頁面更新資料
        if (this.row_string_final.contains("修改資料(Y/N)?[N]")) {
            currentPage = BahamutPage.Companion.BAHAMUT_USER_INFO_PAGE
            // 個人資料
            val page: UserInfoPage = PageContainer.Companion.getInstance().getUserInfoPage()
            page.updateUserInfoPageContent(rows)
        } else if (this.row_string_final.contains("請按鍵切換設定，或按")) {
            currentPage = BahamutPage.Companion.BAHAMUT_USER_CONFIG_PAGE
            // 操作模式
            val page: UserConfigPage = PageContainer.Companion.getInstance().getUserConfigPage()
            page.updateUserConfigPageContent(rows)
        }
    }

    fun handleArticle() {
        val topPage =
            ASNavigationController.getCurrentController().getTopController() as TelnetPage?
        if (topPage is BoardMainPage) {
            currentPage = BahamutPage.Companion.BAHAMUT_ARTICLE
        } else if (topPage is MailBoxPage) {
            currentPage = BahamutPage.Companion.BAHAMUT_MAIL
        } else if (topPage is BoardEssencePage) {
            currentPage = BahamutPage.Companion.BAHAMUT_ARTICLE_ESSENCE
        }

        if (!this.duringReadingArticle) {
            onReadArticleStart()
        }
    }

    // 變更讀取條進度
    fun handleArticlePercentage() {
        val resourceString = "((?<percent>\\d+)%)"
        val pattern = Pattern.compile(resourceString)
        val matcher = pattern.matcher(row_string_final)
        if (matcher.find()) {
            val percent = matcher.toMatchResult().group(1)
            val topPage =
                ASNavigationController.getCurrentController().getTopController() as TelnetPage?
            if (topPage is ArticleEssencePage) {
                topPage.changeLoadingPercentage(percent)
            } else if (topPage is MailPage) {
                topPage.changeLoadingPercentage(percent)
            } else if (topPage is ArticlePage) {
                topPage.changeLoadingPercentage(percent)
            }
        }
    }

    // com.kota.Telnet.TelnetStateHandler
    public override fun handleState() {
        loadState()
        this.telnetCursor = TelnetClient.model.cursor
        val topPage =
            ASNavigationController.getCurrentController().getTopController() as TelnetPage?


        // 狀況：正在重整訊息 或者 訊息主視窗收到訊息
        if (topPage is MessageMain) {
            // HP:MP:會跟網友列表同時發生, 所以判斷要在之前
            if (this.row_string_02.contains("HP：") && this.row_string_02.contains("MP：")) {
                val page: ArticlePage = PageContainer.Companion.getInstance().getArticlePage()
                val userData = Vector<String?>()
                this.rows.forEach(Consumer { row: TelnetRow? -> userData.add(row.toString()) })
                page.ctrlQUser(userData)
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (this.row_string_00.startsWith("【網友列表】")) {
                // 載入名單
                object : ASRunner() {
                    // from class: com.kota.Bahamut.BahamutStateHandler.4
                    // com.kota.ASFramework.Thread.ASRunner
                    public override fun run() {
                        topPage.loadUserList(rows)
                    }
                }.runInMainThread()
            } else if (this.row_string_final.contains("瀏覽 P.")) {
                // 正在瀏覽訊息
                topPage.receiveSyncCommand(rows)
                BahamutCommandLoadMoreArticle().execute()
            } else if (this.row_string_final.contains("● 請按任意鍵繼續 ●")) {
                // 訊息最後一頁, 還有回到原本的那頁
                topPage.receiveSyncCommand(rows)
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)

                object : ASRunner() {
                    // from class: com.kota.Bahamut.BahamutStateHandler.4
                    // com.kota.ASFramework.Thread.ASRunner
                    public override fun run() {
                        topPage.loadMessageList()
                    }
                }.runInMainThread()
            } else if (this.row_string_final.startsWith("★") && !this.row_string_final.substring(
                    1,
                    2
                ).isEmpty()
            ) {
                detectMessage()
            }
        } else if (topPage is MessageSub) {
            if (this.row_string_final.contains("對方關掉呼叫器了")) {
                topPage.sendMessageFail(MessageStatus.CloseBBCall)
                showLongToast("對方關掉呼叫器了")
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (this.row_string_final.contains("對方已經離去")) {
                topPage.sendMessageFail(MessageStatus.Escape)
                showLongToast("對方已經離去")
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (getRowString(22).trim { it <= ' ' }.startsWith("★熱訊：")) {
                // 送出給對方的訊息
                // 一定要在"傳訊給"判斷之前, 因為這兩個判斷會同時出現
                topPage.sendMessagePart3()
            } else if (this.row_string_01.startsWith("傳訊給")) {
                // 送出對方id
                // 一定要在"熱訊回應"判斷之前, 因為這兩個判斷會同時出現
                topPage.sendMessagePart2()
            } else if (getRowString(22).trim { it <= ' ' }.contains("熱訊回應")) {
                // 我方發出ctrl+S, 但是被熱訊回應卡住, 送出Enter指令接傳訊給對方id
                TelnetClient.getClient().sendStringToServer("")
            } else if (this.row_string_final.startsWith("★") && !this.row_string_final.substring(
                    1,
                    2
                ).isEmpty()
            ) {
                detectMessage()
            }
        } else if (pass_1()) {
            if (currentPage == BahamutPage.Companion.BAHAMUT_CLASS && this.row_string_final.contains(
                    "瀏覽 P."
                ) && this.row_string_final.endsWith("結束")
            ) {
                BahamutCommandLoadMoreArticle().execute()
            } else if (currentPage > BahamutPage.Companion.BAHAMUT_MAIL_BOX && this.row_string_final.contains(
                    "文章選讀"
                ) && this.row_string_final.endsWith("搜尋作者")
            ) {
                handleArticle()
                onReadArticleFinished()
                // 串接文章狀況下, 文章讀取完畢指令不同, 但是第23行內容一樣,會誤判,因此根據之前最後一頁判斷狀況
                val lastPage = currentPage
                if (lastPage == BahamutPage.Companion.BAHAMUT_ARTICLE) BahamutCommandLoadArticleEnd().execute()
                else BahamutCommandLoadArticleEndForSearch().execute()
            } else if (currentPage > BahamutPage.Companion.BAHAMUT_CLASS && this.row_string_final.contains(
                    "瀏覽 P."
                ) && this.row_string_final.endsWith("結束")
            ) {
                handleArticle()
                onReadArticlePage()
                handleArticlePercentage()
                BahamutCommandLoadMoreArticle().execute()
            } else if (currentPage > BahamutPage.Companion.BAHAMUT_CLASS && this.row_string_final.contains(
                    "魚雁往返"
                ) && this.row_string_final.endsWith("標記")
            ) {
                handleArticle()
                onReadArticleFinished()
                BahamutCommandLoadArticleEnd().execute()
            } else if (currentPage > BahamutPage.Companion.BAHAMUT_CLASS && this.row_string_final.contains(
                    "閱讀精華"
                ) && this.row_string_final.trim { it <= ' ' }.endsWith("離開")
            ) {
                handleArticle()
                onReadArticleFinished()
                BahamutCommandLoadArticleEnd().execute()
            } else if (this.firstHeader == "對戰" && currentPage < 5) {
                handleLoginPage()
            } else if (this.row_string_00.contains("【主功能表】")) {
                handleMainPage()
            } else if (this.row_string_00.contains("【郵件選單】")) {
                handleMailBoxPage()
            } else if (this.row_string_00.contains("【看板列表】")) {
                if (this.row_string_01.contains("請輸入看板名稱")) {
                    handleSearchBoard()
                } else if (this.row_string_02.contains("總數")) {
                    TelnetClient.getClient().sendKeyboardInputToServer(99)
                } else {
                    handleClassPage()
                }
            } else if (this.row_string_00.contains("【主題串列】")) {
                if (PageContainer.Companion.getInstance()
                        .getBoardPage().lastListAction == BoardPageAction.SEARCH
                ) {
                    handleBoardSearchPage()
                } else {
                    handleBoardTitleLinkedPage()
                }
            } else if (this.row_string_00.contains("【精華文章】")) {
                handleBoardEssencePage()
            } else if (this.row_string_00.startsWith("【板主：") && this.row_string_00.contains("看板《")) {
                if (row_string_final.startsWith("推文(系統測試中)：")) {
                    // 呼叫推文訊息視窗
                    object : ASRunner() {
                        // from class: com.kota.Bahamut.BahamutStateHandler.4
                        // com.kota.ASFramework.Thread.ASRunner
                        public override fun run() {
                            PageContainer.Companion.getInstance().getBoardPage()
                                .openPushArticleDialog()
                        }
                    }.runInMainThread()
                    return
                }
                handleBoardPage()
            } else if (this.row_string_00.contains("【個人設定】")) {
                handleUserPage()
            } else if (this.row_string_final.contains("您要刪除上述記錄嗎")) {
                TelnetClient.getClient().sendStringToServer("n")
            } else if (this.row_string_final == "● 請按任意鍵繼續 ●") {
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (this.lastHeader == "您想") {
                object : ASRunner() {
                    // from class: com.kota.Bahamut.BahamutStateHandler.4
                    // com.kota.ASFramework.Thread.ASRunner
                    public override fun run() {
                        val page: LoginPage = PageContainer.Companion.getInstance().getLoginPage()
                        if (page.isTopPage) {
                            page.onSaveArticle()
                        }
                    }
                }.runInMainThread()
            } else if (this.row_string_final.contains("★ 請閱讀最新公告")) {
                TelnetClient.getClient().sendStringToServer("")
            } else if (this.nowStep == STEP_CONNECTING && this.firstHeader == "--") {
                // TODO: 不知道甚麼狀況
                currentPage = BahamutPage.Companion.BAHAMUT_INSTRUCTIONS
                if (this.lastHeader == "●請" || this.lastHeader == "請按") {
                    TelnetClient.getClient().sendStringToServer("")
                }
            } else if (this.nowStep == STEP_CONNECTING && this.firstHeader == "□□") {
                currentPage = BahamutPage.Companion.BAHAMUT_SYSTEM_ANNOUNCEMENT
                if (this.lastHeader == "●請" || this.lastHeader == "請按") {
                    TelnetClient.getClient().sendStringToServer("")
                }
            } else if (this.firstHeader == "【過") {
                currentPage = BahamutPage.Companion.BAHAMUT_PASSED_SIGNATURE
                if (this.lastHeader == "●請" || this.lastHeader == "請按") {
                    TelnetClient.getClient().sendStringToServer("")
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

        if (this.articleNumber != null) {
            article.Number = this.articleNumber!!.toInt()
        }
        if (this.row_string_final.contains("魚雁往返")) {
            showMail(article)
        } else if (this.row_string_final.contains("閱讀精華")) {
            showEssence(article)
        } else {
            showArticle(article)
        }
        this.duringReadingArticle = false
    }

    // 顯示文章內文
    fun showArticle(aArticle: TelnetArticle?) {
        object : ASRunner() {
            // from class: com.kota.Bahamut.BahamutStateHandler.5
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                try {
                    PageContainer.Companion.getInstance().getArticlePage().setArticle(aArticle)
                } catch (e: Exception) {
                    Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
                }
            }
        }.runInMainThread()
    }

    // 顯示郵件內文
    fun showMail(aArticle: TelnetArticle?) {
        object : ASRunner() {
            // from class: com.kota.Bahamut.BahamutStateHandler.6
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                val mailPage: MailPage?
                try {
                    val last_page =
                        ASNavigationController.getCurrentController().getViewControllers()
                            .lastElement() as TelnetPage?
                    // 檢查最上層的頁面是不是 mail page
                    // 如果不是=>就把mail page推到最上層
                    if (last_page == null || last_page.pageType != BahamutPage.Companion.BAHAMUT_MAIL) {
                        mailPage = MailPage()
                        ASNavigationController.getCurrentController().pushViewController(mailPage)
                    } else {
                        mailPage = last_page as MailPage
                    }
                    mailPage.setArticle(aArticle)
                } catch (ignored: Exception) {
                }
            }
        }.runInMainThread()
    }

    // 顯示精華區內文
    fun showEssence(aArticle: TelnetArticle?) {
        object : ASRunner() {
            // from class: com.kota.Bahamut.BahamutStateHandler.6
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                val articleEssencePage: ArticleEssencePage?
                try {
                    val last_page =
                        ASNavigationController.getCurrentController().getViewControllers()
                            .lastElement() as TelnetPage?
                    // 檢查最上層的頁面是不是 mail page
                    // 如果不是=>就把mail page推到最上層
                    if (last_page == null || last_page.pageType != BahamutPage.Companion.BAHAMUT_ARTICLE_ESSENCE) {
                        articleEssencePage = ArticleEssencePage()
                        ASNavigationController.getCurrentController()
                            .pushViewController(articleEssencePage)
                    } else {
                        articleEssencePage = last_page as ArticleEssencePage
                    }
                    articleEssencePage.setArticle(aArticle)
                } catch (ignored: Exception) {
                }
            }
        }.runInMainThread()
    }

    // 通用顯示頁面
    fun showPage(aPage: TelnetPage?) {
        val topPage =
            ASNavigationController.getCurrentController().getTopController() as TelnetPage?
        if (aPage === topPage) {
            object : ASRunner() {
                // from class: com.kota.Bahamut.BahamutStateHandler.7
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    topPage!!.requestPageRefresh()
                }
            }.runInMainThread()
        } else if (topPage != null && !topPage.isPopupPage && aPage != null) {
            if (ASNavigationController.getCurrentController().containsViewController(aPage)) {
                ASNavigationController.getCurrentController().popToViewController(aPage)
            } else {
                ASNavigationController.getCurrentController().pushViewController(aPage)
            }
        }
    }

    // com.kota.Telnet.TelnetStateHandler
    public override fun clear() {
        this.nowStep = STEP_CONNECTING
        currentPage = BahamutPage.Companion.UNKNOWN
    }

    fun cutOffContinueMessage(aMessage: String): String {
        var start = 0
        var end = aMessage.length - 1
        val words = aMessage.toCharArray()
        while (start < words.size) {
            if (words[start].code == 9733 || words[start].code == 32) start++
            else break
        }
        while (words[end] != '[' && end >= 0) {
            end--
        }
        if (end <= start) {
            return ""
        }
        return aMessage.substring(start, end).trim { it <= ' ' }
    }

    companion object {
        const val STEP_CONNECTING: Int = 0
        const val STEP_WORKING: Int = 1
        val UNKNOWN: Int = -1
        var instance: BahamutStateHandler? = null

        /** 回傳instance給其他頁面使用  */
        fun getInstance(): BahamutStateHandler {
            if (instance == null) {
                instance = BahamutStateHandler()
            }
            return instance!!
        }
    }
}
