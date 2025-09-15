package com.kota.Bahamut

import android.util.Log
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.PageController.ASViewController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASSnackBar
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.Command.BahamutCommandLoadArticleEnd
import com.kota.Bahamut.Command.BahamutCommandLoadArticleEndForSearch
import com.kota.Bahamut.Command.BahamutCommandLoadMoreArticle
import com.kota.Bahamut.Pages.Messages.BahaMessage
import com.kota.Bahamut.Pages.Messages.MessageDatabase
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage
import com.kota.Bahamut.Pages.BBSUser.UserConfigPage
import com.kota.Bahamut.Pages.BoardPage.BoardLinkPage
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage
import com.kota.Bahamut.Pages.BoardPage.BoardPageAction
import com.kota.Bahamut.Pages.BoardPage.BoardSearchPage
import com.kota.Bahamut.Pages.ClassPage
import com.kota.Bahamut.Pages.EssencePage.ArticleEssencePage
import com.kota.Bahamut.Pages.EssencePage.BoardEssencePage
import com.kota.Bahamut.Pages.Login.LoginPage
import com.kota.Bahamut.Pages.MailBoxPage
import com.kota.Bahamut.Pages.MailPage
import com.kota.Bahamut.Pages.MainPage
import com.kota.Bahamut.Pages.Messages.MessageMain
import com.kota.Bahamut.Pages.Messages.MessageSmall
import com.kota.Bahamut.Pages.Messages.MessageStatus
import com.kota.Bahamut.Pages.Messages.MessageSub
import com.kota.Bahamut.Pages.PostArticlePage
import com.kota.Bahamut.Pages.BBSUser.UserInfoPage
import com.kota.Bahamut.Service.HeroStep
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.Logic.Article_Handler
import com.kota.Telnet.Logic.SearchBoard_Handler
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetArticle
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetCursor
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.Telnet.TelnetStateHandler
import com.kota.Telnet.TelnetUtils
import com.kota.TelnetUI.TelnetPage
import com.kota.TextEncoder.B2UEncoder
import java.io.ByteArrayOutputStream
import java.util.Vector
import java.util.regex.Pattern

class BahamutStateHandler private constructor() : TelnetStateHandler() {
    
    var articleNumber: String? = null
    private var nowStep = 0
    private val rows = Vector<TelnetRow>() // debug用
    private var row_string_00 = ""
    private var row_string_01 = ""
    private var row_string_02 = ""
    private var row_string_final = ""
    private var firstHeader = ""
    private var lastHeader = ""
    private var telnetCursor: TelnetCursor? = null
    private val articleHandler = Article_Handler()
    private var duringReadingArticle = false // 正在讀取文章

    /** 設定文章編號 */
    fun setArticleNumber(aArticleNumber: String) {
        articleNumber = aArticleNumber
    }

    private fun loadState() {
        row_string_00 = getRowString(0).trim()
        row_string_01 = getRowString(1).trim()
        row_string_02 = getRowString(2).trim()
        
        // 隨然是取row 23, 但是偶爾遇到排版不正確情況, 取row 22, 以此類推
        // row 2 已經有使用, 當作界線
        for (i in 23 downTo 3) {
            row_string_final = getRowString(i).trim()
            if (row_string_final.isNotEmpty()) {
                break
            }
        }
        
        rows.clear()
        @Suppress("UNCHECKED_CAST")
        rows.addAll(getRows().clone() as Collection<TelnetRow>)
        firstHeader = TelnetUtils.getHeader(row_string_00)
        lastHeader = TelnetUtils.getHeader(row_string_final)
    }

    /**
     * 接收到訊息
     */
    private fun detectMessage() {
        val column = telnetCursor?.column ?: return
        val row = TelnetClient.getModel().getRow(23)
        val nameBuffer = ByteArrayOutputStream(80)
        val msgBuffer = ByteArrayOutputStream(80)
        var endPoint = -1
        
        for (i in row.data.indices) {
            val backgroundColor = row.backgroundColor[i]
            val data = row.data[i]
            when (backgroundColor.toInt()) {
                6 -> nameBuffer.write(data.toInt())
                5 -> msgBuffer.write(data.toInt())
                else -> {
                    endPoint = i
                    break
                }
            }
        }
        
        if (nameBuffer.size() > 0 && msgBuffer.size() > 0) {
            val name = B2UEncoder.getInstance().encodeToString(nameBuffer.toByteArray())
            val msg = B2UEncoder.getInstance().encodeToString(msgBuffer.toByteArray())
            
            if (endPoint == column && name.startsWith("★")) {
                val name2 = name.substring(1, name.length - 1).trim()
                val msg2 = msg.substring(1).trim()
                
                // 因為BBS會更新畫面, 會重複出現相同訊息. 只要最後接收的訊息一樣就不顯示
                if (TempSettings.lastReceivedMessage != name2 + msg2) {
                    // 更新未讀取訊息
                    var totalUnreadCount = TempSettings.getNotReadMessageCount()
                    totalUnreadCount++
                    TempSettings.setNotReadMessageCount(totalUnreadCount)

                    var bahaMessage: BahaMessage? = null
                    try {
                        MessageDatabase(TempSettings.myContext).use { db ->
                            // 紀錄訊息
                            bahaMessage = db.receiveMessage(name2, msg2, 0)
                        }
                    } catch (e: Exception) {
                        Log.e(javaClass.simpleName, e.message ?: "")
                    }

                    bahaMessage?.let { message ->
                        val topPage = ASNavigationController.getCurrentController().topController as TelnetPage
                        when (topPage) {
                            is MessageMain -> {
                                // 顯示訊息
                                ASSnackBar.show(name2, msg2)
                                topPage.loadMessageList(message)
                            }
                            is MessageSub -> {
                                topPage.insertMessage(message)
                            }
                            else -> {
                                // 如果是其他頁面:顯示訊息
                                ASSnackBar.show(name2, msg2)
                            }
                        }
                    }

                    TempSettings.lastReceivedMessage = name2 + msg2
                }
            }
        }
    }

    /** 處理非切換主頁面的需求 */
    private fun pass_1(): Boolean {
        // 本文
        var runPass2 = true
        
        if (row_string_final.contains("您有一篇文章尚未完成")) {
            TelnetClient.getClient().sendStringToServer("S\n1\n")
            runPass2 = false
        }
        
        if (runPass2 && row_string_final.contains("[請按任意鍵繼續]") && getCurrentPage() != BahamutPage.BAHAMUT_LOGIN) {
            val continueMessage = cutOffContinueMessage(row_string_final)
            if (continueMessage.isNotEmpty()) {
                if (continueMessage.contains("推文") || continueMessage.contains("請稍後片刻")) {
                    PageContainer.getInstance().getBoardPage().cancelRunner()
                }
                ASToast.showShortToast(continueMessage)
            }
            
            if (row_string_final.contains("★ 引言太多")) {
                // 放棄此次編輯內容
                val data = TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.SPACE)
                    .build()
                TelnetClient.getClient().sendDataToServer(data)
                
                val topPage = ASNavigationController.getCurrentController().topController as TelnetPage
                when (topPage) {
                    is MailBoxPage -> {
                        val page2 = PageContainer.getInstance().getMailBoxPage()
                        page2.recoverPost()
                    }
                    is PostArticlePage -> {
                        // 最上層是 發文 或 看板
                        // 清除最先遇到的 BoardSearch, BoardLink, BoardMain
                        val controllers = ASNavigationController.getCurrentController().getAllController()
                        for (i in controllers.size downTo 1) {
                            val nowPage = controllers[i - 1] as TelnetPage

                            when (nowPage.javaClass) {
                                BoardMainPage::class.java -> {
                                    val page = PageContainer.getInstance().getBoardPage()
                                    page.recoverPost()
                                    return false
                                }
                                BoardLinkPage::class.java -> {
                                    val page = PageContainer.getInstance().getBoardLinkedTitlePage()
                                    page.recoverPost()
                                    return false
                                }
                                BoardSearchPage::class.java -> {
                                    val page = PageContainer.getInstance().getBoardSearchPage()
                                    page.recoverPost()
                                    return false
                                }
                            }
                        }
                    }
                }
                return false
            }
            TelnetClient.getClient().sendStringToServer("")
            return false
        } else if (row_string_final.contains("要新增資料嗎？(Y/N) [N]")) {
            ASToast.showShortToast("此看板無文章")
            TelnetClient.getClient().sendStringToServer("N")
            return false
        } else if (row_string_final.contains("● 請按任意鍵繼續 ●")) {
            if (row_string_00.contains("順利貼出佈告")) {
                // 順利貼出佈告, 請按任意鍵繼續
                val topPage = ASNavigationController.getCurrentController().topController as TelnetPage
                when (topPage) {
                    is PostArticlePage, is BoardMainPage -> {
                        // 最上層是 發文 或 看板
                        val page = PageContainer.getInstance().getBoardPage()
                        page.finishPost()
                    }
                    is MailBoxPage -> {
                        val page2 = PageContainer.getInstance().getMailBoxPage()
                        page2.finishPost()
                    }
                }
            } else if (row_string_02.contains("HP：") && row_string_02.contains("MP：")) {
                val page = PageContainer.getInstance().getArticlePage()
                val userData = Vector<String>()
                rows.forEach { row -> userData.add(row.toString()) }
                page.ctrlQUser(userData)
            } else if (row_string_00.contains("過  路  勇  者  的  足  跡")) {
                // 逐行塞入勇者足跡
                insertHeroSteps()
            }

            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            return false
        } else if (row_string_final.contains("請按 [SPACE] 繼續觀賞") && row_string_00.contains("過  路  勇  者  的  足  跡")) {
            // 逐行塞入勇者足跡
            insertHeroSteps()
            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            return false
        } else if (row_string_final.startsWith("★") && row_string_final.substring(1, 2).isNotEmpty()) {
            detectMessage()
            return false
        } else {
            return runPass2
        }
    }

    /** 逐行塞入勇者足跡 */
    private fun insertHeroSteps() {
        var startCatching = false
        var heroStep: HeroStep? = null
        var countRows = 0
        
        for (i in rows.indices) {
            val fromRow = rows[i]
            if (startCatching) {
                // 開始擷取本文
                if (fromRow.isEmpty()) {
                    // 擷取完畢
                    startCatching = false
                    countRows = 0
                    TempSettings.setHeroStep(heroStep)
                } else {
                    countRows++
                    var oldContent = heroStep?.getContent() ?: ""
                    // 第二行開始才加入換行
                    if (oldContent.isNotEmpty()) {
                        oldContent += "\n"
                    }
                    // 塞入本行內容
                    heroStep?.setContent(oldContent + fromRow.toContentString())
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

    private fun handleLoginPage() {
        setCurrentPage(BahamutPage.BAHAMUT_LOGIN)
        val page = PageContainer.getInstance().getLoginPage()
        if (page.onPagePreload()) {
            showPage(page)
        }
    }

    private fun handleMainPage() {
        nowStep = STEP_WORKING

        if (getCurrentPage() < BahamutPage.BAHAMUT_MAIN) {
            PageContainer.getInstance().getLoginPage().onLoginSuccess()

            // 開啟訊息小視窗
            TempSettings.myContext?.let { context ->
                if (TempSettings.getMessageSmall() == null) {
                    // 統計訊息數量
                    val messageSmall = MessageSmall(context)
                    messageSmall.afterInit()
                    TempSettings.setMessageSmall(messageSmall)
                    
                    object : ASRunner() {
                        override fun run() {
                            ASNavigationController.getCurrentController().addForeverView(messageSmall)

                            if (NotificationSettings.getShowMessageFloating()) {
                                messageSmall.show()
                            } else {
                                messageSmall.hide()
                            }
                        }
                    }.runInMainThread()

                    try {
                        MessageDatabase(context).use { db ->
                            db.getAllAndNewestMessage()
                        }
                    } catch (e: Exception) {
                        Log.e(javaClass.simpleName, e.message ?: "")
                    }
                }
            }
        }

        setCurrentPage(BahamutPage.BAHAMUT_MAIN)
        val page = PageContainer.getInstance().getMainPage()
        if (page.onPagePreload()) {
            showPage(page)
        }

        when (lastHeader) {
            "本次" -> {
                object : ASRunner() {
                    override fun run() {
                        val page2 = PageContainer.getInstance().getMainPage()
                        if (page2.isTopPage()) {
                            page2.onProcessHotMessage()
                        }
                    }
                }.runInMainThread()
            }
            "G)" -> {
                object : ASRunner() {
                    override fun run() {
                        val page2 = PageContainer.getInstance().getMainPage()
                        if (page2.isTopPage()) {
                            page2.onCheckGoodbye()
                        }
                    }
                }.runInMainThread()
            }
        }
        
        if (row_string_final.contains("[訪客]")) {
            // 紀錄線上人數
            val startIndex = row_string_final.indexOf("[訪客]") + 4
            val endIndex = row_string_final.indexOf(" 人")
            page.setOnlinePeople(row_string_final.substring(startIndex, endIndex).trim())

            // 紀錄呼叫器
            val startIndex2 = row_string_final.indexOf("[呼叫器]") + 5
            val endIndex2 = row_string_final.length
            page.setBBCall(row_string_final.substring(startIndex2, endIndex2).trim())
        }
    }

    private fun handleMailBoxPage() {
        setCurrentPage(BahamutPage.BAHAMUT_MAIL_BOX)
        if (telnetCursor?.column == 1) {
            val page = PageContainer.getInstance().getMailBoxPage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    private fun handleSearchBoard() {
        if (row_string_final.startsWith("★ 列表") && telnetCursor?.equals(23, 29) == true) {
            SearchBoard_Handler.getInstance().read()
            TelnetClient.getClient().sendKeyboardInputToServer(67)
        } else if (telnetCursor?.row == 1) {
            SearchBoard_Handler.getInstance().read()
            val data = TelnetOutputBuilder.create().pushKey(TelnetKeyboard.CTRL_Y).pushString("\n\n").build()
            TelnetClient.getClient().sendDataToServer(data)
            object : ASRunner() {
                override fun run() {
                    val page = PageContainer.getInstance().getClassPage()
                    page?.onSearchBoardFinished()
                }
            }.runInMainThread()
        }
    }

    private fun handleClassPage() {
        setCurrentPage(BahamutPage.BAHAMUT_CLASS)
        if (telnetCursor?.column == 1) {
            val page = PageContainer.getInstance().getClassPage()
            if (page?.onPagePreload() == true) {
                showPage(page)
            }
        }
    }

    private fun handleBoardPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD)
        if (telnetCursor?.column == 1) {
            val page = PageContainer.getInstance().getBoardPage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    private fun handleBoardSearchPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_SEARCH)
        if (telnetCursor?.column == 1) {
            val page = PageContainer.getInstance().getBoardSearchPage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    private fun handleBoardEssencePage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_ESSENCE)
        if (telnetCursor?.column == 1) {
            val page = PageContainer.getInstance().getBoardEssencePage()
            if (page?.onPagePreload() == true) {
                showPage(page)
            }
        }
    }

    private fun handleBoardTitleLinkedPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_LINK)
        if (telnetCursor?.column == 1) {
            val page = PageContainer.getInstance().getBoardLinkedTitlePage()
            if (page.onPagePreload()) {
                showPage(page)
            }
        }
    }

    /** 頁面: 個人設定  */
    private fun handleUserPage() {
        // 傳給個人設定, 頁面更新資料
        when {
            row_string_final.contains("修改資料(Y/N)?[N]") -> {
                setCurrentPage(BahamutPage.BAHAMUT_USER_INFO_PAGE)
                // 個人資料
                val page = PageContainer.getInstance().getUserInfoPage()
                page.updateUserInfoPageContent(rows)
            }
            row_string_final.contains("請按鍵切換設定，或按") -> {
                setCurrentPage(BahamutPage.BAHAMUT_USER_CONFIG_PAGE)
                // 操作模式
                val page = PageContainer.getInstance().getUserConfigPage()
                page.updateUserConfigPageContent(rows)
            }
        }
    }

    private fun handleArticle() {
        val topPage = ASNavigationController.getCurrentController().topController as TelnetPage
        when (topPage) {
            is BoardMainPage -> setCurrentPage(BahamutPage.BAHAMUT_ARTICLE)
            is MailBoxPage -> setCurrentPage(BahamutPage.BAHAMUT_MAIL)
            is BoardEssencePage -> setCurrentPage(BahamutPage.BAHAMUT_ARTICLE_ESSENCE)
        }

        if (!duringReadingArticle) {
            onReadArticleStart()
        }
    }

    // 變更讀取條進度
    private fun handleArticlePercentage() {
        val resourceString = "((?<percent>\\d+)%)"
        val pattern = Pattern.compile(resourceString)
        val matcher = pattern.matcher(row_string_final)
        if (matcher.find()) {
            val percent = matcher.toMatchResult().group(1)
            val topPage = ASNavigationController.getCurrentController().topController as TelnetPage
            when (topPage) {
                is ArticleEssencePage -> topPage.changeLoadingPercentage(percent)
                is MailPage -> topPage.changeLoadingPercentage(percent)
                is ArticlePage -> topPage.changeLoadingPercentage(percent)
            }
        }
    }

    override fun handleState() {
        loadState()
        telnetCursor = TelnetClient.getModel().cursor
        val topPage = ASNavigationController.getCurrentController().topController as TelnetPage

        // 狀況：正在重整訊息 或者 訊息主視窗收到訊息
        if (topPage is MessageMain) {
            // HP:MP:會跟網友列表同時發生, 所以判斷要在之前
            if (row_string_02.contains("HP：") && row_string_02.contains("MP：")) {
                val page = PageContainer.getInstance().getArticlePage()
                val userData = Vector<String>()
                rows.forEach { row -> userData.add(row.toString()) }
                page.ctrlQUser(userData)
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
            } else if (row_string_00.startsWith("【網友列表】")) {
                // 載入名單
                object : ASRunner() {
                    override fun run() {
                        topPage.loadUserList(rows)
                    }
                }.runInMainThread()
            } else if (row_string_final.contains("瀏覽 P.")) {
                // 正在瀏覽訊息
                topPage.receiveSyncCommand(rows)
                BahamutCommandLoadMoreArticle().execute()
            } else if (row_string_final.contains("● 請按任意鍵繼續 ●")) {
                // 訊息最後一頁, 還有回到原本的那頁
                topPage.receiveSyncCommand(rows)
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)

                object : ASRunner() {
                    override fun run() {
                        topPage.loadMessageList()
                    }
                }.runInMainThread()
            } else if (row_string_final.startsWith("★") && row_string_final.substring(1, 2).isNotEmpty()) {
                detectMessage()
            }
        }
        // 狀況: 正在發送訊息
        else if (topPage is MessageSub) {
            when {
                row_string_final.contains("對方關掉呼叫器了") -> {
                    topPage.sendMessageFail(MessageStatus.CloseBBCall)
                    ASToast.showLongToast("對方關掉呼叫器了")
                    TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
                }
                row_string_final.contains("對方已經離去") -> {
                    topPage.sendMessageFail(MessageStatus.Escape)
                    ASToast.showLongToast("對方已經離去")
                    TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
                }
                getRowString(22).trim().startsWith("★熱訊：") -> {
                    // 送出給對方的訊息
                    // 一定要在"傳訊給"判斷之前, 因為這兩個判斷會同時出現
                    topPage.sendMessagePart3()
                }
                row_string_01.startsWith("傳訊給") -> {
                    // 送出對方id
                    // 一定要在"熱訊回應"判斷之前, 因為這兩個判斷會同時出現
                    topPage.sendMessagePart2()
                }
                getRowString(22).trim().contains("熱訊回應") -> {
                    // 我方發出ctrl+S, 但是被熱訊回應卡住, 送出Enter指令接傳訊給對方id
                    TelnetClient.getClient().sendStringToServer("")
                }
                row_string_final.startsWith("★") && row_string_final.substring(1, 2).isNotEmpty() -> {
                    detectMessage()
                }
            }
        }
        // 其他
        else if (pass_1()) {
            when {
                getCurrentPage() == BahamutPage.BAHAMUT_CLASS && row_string_final.contains("瀏覽 P.") && row_string_final.endsWith("結束") -> {
                    BahamutCommandLoadMoreArticle().execute()
                }
                getCurrentPage() > BahamutPage.BAHAMUT_MAIL_BOX && row_string_final.contains("文章選讀") && row_string_final.endsWith("搜尋作者") -> {
                    handleArticle()
                    onReadArticleFinished()
                    // 串接文章狀況下, 文章讀取完畢指令不同, 但是第23行內容一樣,會誤判,因此根據之前最後一頁判斷狀況
                    val lastPage = getCurrentPage()
                    if (lastPage == BahamutPage.BAHAMUT_ARTICLE) {
                        BahamutCommandLoadArticleEnd().execute()
                    } else {
                        BahamutCommandLoadArticleEndForSearch().execute()
                    }
                }
                getCurrentPage() > BahamutPage.BAHAMUT_CLASS && row_string_final.contains("瀏覽 P.") && row_string_final.endsWith("結束") -> {
                    handleArticle()
                    onReadArticlePage()
                    handleArticlePercentage()
                    BahamutCommandLoadMoreArticle().execute()
                }
                getCurrentPage() > BahamutPage.BAHAMUT_CLASS && row_string_final.contains("魚雁往返") && row_string_final.endsWith("標記") -> {
                    handleArticle()
                    onReadArticleFinished()
                    BahamutCommandLoadArticleEnd().execute()
                }
                getCurrentPage() > BahamutPage.BAHAMUT_CLASS && row_string_final.contains("閱讀精華") && row_string_final.trim().endsWith("離開") -> {
                    handleArticle()
                    onReadArticleFinished()
                    BahamutCommandLoadArticleEnd().execute()
                }
                firstHeader == "對戰" && getCurrentPage() < 5 -> {
                    handleLoginPage()
                }
                row_string_00.contains("【主功能表】") -> {
                    handleMainPage()
                }
                row_string_00.contains("【郵件選單】") -> {
                    handleMailBoxPage()
                }
                row_string_00.contains("【看板列表】") -> {
                    when {
                        row_string_01.contains("請輸入看板名稱") -> handleSearchBoard()
                        row_string_02.contains("總數") -> TelnetClient.getClient().sendKeyboardInputToServer(99)
                        else -> handleClassPage()
                    }
                }
                row_string_00.contains("【主題串列】") -> {
                    if (PageContainer.getInstance().getBoardPage().getLastListAction() == BoardPageAction.SEARCH) {
                        handleBoardSearchPage()
                    } else {
                        handleBoardTitleLinkedPage()
                    }
                }
                row_string_00.contains("【精華文章】") -> {
                    handleBoardEssencePage()
                }
                row_string_00.startsWith("【板主：") && row_string_00.contains("看板《") -> {
                    if (row_string_final.startsWith("推文(系統測試中)：")) {
                        // 呼叫推文訊息視窗
                        object : ASRunner() {
                            override fun run() {
                                PageContainer.getInstance().getBoardPage().openPushArticleDialog()
                            }
                        }.runInMainThread()
                        return
                    }
                    handleBoardPage()
                }
                row_string_00.contains("【個人設定】") -> {
                    handleUserPage()
                }
                row_string_final.contains("您要刪除上述記錄嗎") -> {
                    TelnetClient.getClient().sendStringToServer("n")
                }
                row_string_final == "● 請按任意鍵繼續 ●" -> {
                    TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE)
                }
                lastHeader == "您想" -> {
                    object : ASRunner() {
                        override fun run() {
                            val page = PageContainer.getInstance().getLoginPage()
                            if (page.isTopPage()) {
                                page.onSaveArticle()
                            }
                        }
                    }.runInMainThread()
                }
                row_string_final.contains("★ 請閱讀最新公告") -> {
                    TelnetClient.getClient().sendStringToServer("")
                }
                nowStep == STEP_CONNECTING && firstHeader == "--" -> {
                    // TODO: 不知道甚麼狀況
                    setCurrentPage(BahamutPage.BAHAMUT_INSTRUCTIONS)
                    if (lastHeader == "●請" || lastHeader == "請按") {
                        TelnetClient.getClient().sendStringToServer("")
                    }
                }
                nowStep == STEP_CONNECTING && firstHeader == "□□" -> {
                    setCurrentPage(BahamutPage.BAHAMUT_SYSTEM_ANNOUNCEMENT)
                    if (lastHeader == "●請" || lastHeader == "請按") {
                        TelnetClient.getClient().sendStringToServer("")
                    }
                }
                firstHeader == "【過" -> {
                    setCurrentPage(BahamutPage.BAHAMUT_PASSED_SIGNATURE)
                    if (lastHeader == "●請" || lastHeader == "請按") {
                        TelnetClient.getClient().sendStringToServer("")
                    }
                }
            }
        }
    }

    private fun onReadArticleStart() {
        duringReadingArticle = true
        articleHandler.clear()
    }

    private fun onReadArticlePage() {
        articleHandler.loadPage(TelnetClient.getModel())
        cleanFrame()
    }

    private fun onReadArticleFinished() {
        articleHandler.loadLastPage(TelnetClient.getModel())
        articleHandler.build()
        val article = articleHandler.article
        articleHandler.newArticle()

        articleNumber?.let { number ->
            article.Number = number.toInt()
        }
        
        when {
            row_string_final.contains("魚雁往返") -> showMail(article)
            row_string_final.contains("閱讀精華") -> showEssence(article)
            else -> showArticle(article)
        }
        duringReadingArticle = false
    }

    // 顯示文章內文
    private fun showArticle(aArticle: TelnetArticle) {
        object : ASRunner() {
            override fun run() {
                try {
                    PageContainer.getInstance().getArticlePage().setArticle(aArticle)
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, e.message ?: "")
                }
            }
        }.runInMainThread()
    }

    // 顯示郵件內文
    private fun showMail(aArticle: TelnetArticle) {
        object : ASRunner() {
            override fun run() {
                try {
                    val lastPage = ASNavigationController.getCurrentController().viewControllers.lastElement() as? TelnetPage
                    // 檢查最上層的頁面是不是 mail page
                    // 如果不是=>就把mail page推到最上層
                    val mailPage = if (lastPage?.pageType != BahamutPage.BAHAMUT_MAIL) {
                        val newMailPage = MailPage()
                        ASNavigationController.getCurrentController().pushViewController(newMailPage)
                        newMailPage
                    } else {
                        lastPage as MailPage
                    }
                    mailPage.setArticle(aArticle)
                } catch (ignored: Exception) {
                }
            }
        }.runInMainThread()
    }

    // 顯示精華區內文
    private fun showEssence(aArticle: TelnetArticle) {
        object : ASRunner() {
            override fun run() {
                try {
                    val lastPage = ASNavigationController.getCurrentController().viewControllers.lastElement() as? TelnetPage
                    // 檢查最上層的頁面是不是 article essence page
                    // 如果不是=>就把article essence page推到最上層
                    val articleEssencePage = if (lastPage?.pageType != BahamutPage.BAHAMUT_ARTICLE_ESSENCE) {
                        val newArticleEssencePage = ArticleEssencePage()
                        ASNavigationController.getCurrentController().pushViewController(newArticleEssencePage)
                        newArticleEssencePage
                    } else {
                        lastPage as ArticleEssencePage
                    }
                    articleEssencePage.setArticle(aArticle)
                } catch (ignored: Exception) {
                }
            }
        }.runInMainThread()
    }

    // 通用顯示頁面
    private fun showPage(aPage: TelnetPage) {
        val topPage = ASNavigationController.getCurrentController().topController as? TelnetPage
        if (aPage == topPage) {
            object : ASRunner() {
                override fun run() {
                    topPage.requestPageRefresh()
                }
            }.runInMainThread()
        } else if (topPage?.isPopupPage() != true) {
            if (ASNavigationController.getCurrentController().containsViewController(aPage)) {
                ASNavigationController.getCurrentController().popToViewController(aPage)
            } else {
                ASNavigationController.getCurrentController().pushViewController(aPage)
            }
        }
    }

    override fun clear() {
        nowStep = STEP_CONNECTING
        setCurrentPage(BahamutPage.UNKNOWN)
    }

    private fun cutOffContinueMessage(aMessage: String): String {
        var start = 0
        var end = aMessage.length - 1
        val words = aMessage.toCharArray()
        
        while (start < words.size) {
            if (words[start] == 9733.toChar() || words[start] == ' ') {
                start++
            } else {
                break
            }
        }
        
        while (words[end] != '[' && end >= 0) {
            end--
        }
        
        return if (end <= start) {
            ""
        } else {
            aMessage.substring(start, end).trim()
        }
    }

    companion object {
        const val STEP_CONNECTING = 0
        const val STEP_WORKING = 1
        const val UNKNOWN = -1
        
        @Volatile
        private var instance: BahamutStateHandler? = null

        /** 回傳instance給其他頁面使用 */
        fun getInstance(): BahamutStateHandler {
            return instance ?: synchronized(this) {
                instance ?: BahamutStateHandler().also { instance = it }
            }
        }
    }
}
