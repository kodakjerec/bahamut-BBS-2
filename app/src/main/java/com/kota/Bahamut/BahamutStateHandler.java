package com.kota.Bahamut;

import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASSnackBar;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.Command.BahamutCommandLoadArticleEnd;
import com.kota.Bahamut.Command.BahamutCommandLoadArticleEndForSearch;
import com.kota.Bahamut.Command.BahamutCommandLoadMoreArticle;
import com.kota.Bahamut.Pages.Messages.MessageDatabase;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage;
import com.kota.Bahamut.Pages.BBSUser.UserConfigPage;
import com.kota.Bahamut.Pages.BoardPage.BoardLinkPage;
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage;
import com.kota.Bahamut.Pages.BoardPage.BoardPageAction;
import com.kota.Bahamut.Pages.BoardPage.BoardSearchPage;
import com.kota.Bahamut.Pages.ClassPage;
import com.kota.Bahamut.Pages.EssencePage.ArticleEssencePage;
import com.kota.Bahamut.Pages.EssencePage.BoardEssencePage;
import com.kota.Bahamut.Pages.LoginPage;
import com.kota.Bahamut.Pages.MailBoxPage;
import com.kota.Bahamut.Pages.MailPage;
import com.kota.Bahamut.Pages.MainPage;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.Pages.BBSUser.UserInfoPage;
import com.kota.Bahamut.Service.HeroStep;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.Logic.Article_Handler;
import com.kota.Telnet.Logic.SearchBoard_Handler;
import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetCursor;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.Telnet.TelnetStateHandler;
import com.kota.Telnet.TelnetUtils;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TextEncoder.B2UEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BahamutStateHandler extends TelnetStateHandler {
    static final int STEP_CONNECTING = 0;
    static final int STEP_WORKING = 1;
    static final int UNKNOWN = -1;
    static BahamutStateHandler instance = null;
    String articleNumber;
    int nowStep = 0;
    Vector<TelnetRow> rows = new Vector<>(); // debug用
    String row_string_00 = "";
    String row_string_01 = "";
    String row_string_02 = "";
    String row_string_23 = "";
    String firstHeader = "";
    String lastHeader = "";
    TelnetCursor telnetCursor = null;
    final Article_Handler articleHandler = new Article_Handler();
    boolean duringReadingArticle = false; // 正在讀取文章

    public void setArticleNumber(String aArticleNumber) {
        this.articleNumber = aArticleNumber;
    }

    public static BahamutStateHandler getInstance() {
        if (instance == null) {
            instance = new BahamutStateHandler();
        }
        return instance;
    }

    BahamutStateHandler() {
    }

    void loadState() {
        this.row_string_00 = getRowString(0).trim();
        this.row_string_01 = getRowString(1).trim();
        this.row_string_02 = getRowString(2).trim();
        this.row_string_23 = getRowString(23).trim();
        this.rows = getRows();
        this.firstHeader = TelnetUtils.getHeader(this.row_string_00);
        this.lastHeader = TelnetUtils.getHeader(this.row_string_23);
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

    private String lastReceivedMessage = "";
    /**
     * 接收到訊息
     */
    void detectMessage() {
        try (MessageDatabase db = new MessageDatabase(MyApplication.getInstance())) {
            int column = this.telnetCursor.column;
            TelnetRow row = TelnetClient.getModel().getRow(23);
            ByteArrayOutputStream name_buffer = new ByteArrayOutputStream(80);
            ByteArrayOutputStream msg_buffer = new ByteArrayOutputStream(80);
            int end_point = -1;
            int i = 0;
            while (true) {
                if (i >= row.data.length) {
                    break;
                }
                byte background_color = row.backgroundColor[i];
                byte data = row.data[i];
                if (background_color == 6) {
                    name_buffer.write(data);
                } else if (background_color == 5) {
                    msg_buffer.write(data);
                } else {
                    end_point = i;
                    break;
                }
                i++;
            }
            if (name_buffer.size() > 0 && msg_buffer.size() > 0) {
                String name = B2UEncoder.getInstance().encodeToString(name_buffer.toByteArray());
                String msg = B2UEncoder.getInstance().encodeToString(msg_buffer.toByteArray());
                if (end_point == column && name.startsWith("★")) {
                    String name2 = name.substring(1, name.length() - 1);
                    String msg2 = msg.substring(1);
                    // 因為BBS會更新畫面, 會重複出現相同訊息. 只要最後接收的訊息一樣就不顯示
                    if (!Objects.equals(lastReceivedMessage, name2+msg2)) {
                        // 更新未讀取訊息
                        int totalUnreadCount = TempSettings.getNotReadMessageCount();
                        totalUnreadCount++;
                        TempSettings.setNotReadMessageCount(totalUnreadCount);

                        // 紀錄訊息
                        db.receiveMessage(name2, msg2);

                        // 顯示訊息
                        ASSnackBar.show(name2, msg2);
                        lastReceivedMessage = name2+msg2;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 處理非切換主頁面的需求 */
    boolean pass_1() {
        boolean run_pass_2 = true;
        if (this.row_string_23.contains("您有一篇文章尚未完成")) {
            TelnetClient.getClient().sendStringToServer("S\n1\n");
            run_pass_2 = false;
        }
        if (run_pass_2 && this.row_string_23.contains("[請按任意鍵繼續]") && getCurrentPage() != 1) {
            String continue_message = cutOffContinueMessage(this.row_string_23);
            if (continue_message.length() > 0) {
                ASToast.showShortToast(continue_message);
            }
            if (this.row_string_23.contains("★ 引言太多")) {
                // 放棄此次編輯內容
                byte[] data = TelnetOutputBuilder.create()
                        .pushKey(TelnetKeyboard.SPACE)
                        .build();
                TelnetClient.getClient().sendDataToServer(data);
                TelnetPage top_page = (TelnetPage) ASNavigationController.getCurrentController().getTopController();
                if (top_page instanceof MailBoxPage) {
                    MailBoxPage page2 = PageContainer.getInstance().getMailBoxPage();
                    page2.recoverPost();
                } else if (top_page instanceof PostArticlePage) {
                    // 最上層是 發文 或 看板
                    // 清除最先遇到的 BoardSearch, BoardLink, BoardMain
                    Vector<ASViewController> controllers = ASNavigationController.getCurrentController().getAllController();
                    for (int i = controllers.size(); i > 0; i--) {
                        TelnetPage nowPage = (TelnetPage) controllers.get(i - 1);

                        if (nowPage.getClass().equals(BoardMainPage.class)) {
                            BoardMainPage page = PageContainer.getInstance().getBoardPage();
                            page.recoverPost();
                            return false;
                        } else if (nowPage.getClass().equals(BoardLinkPage.class)) {
                            BoardLinkPage page = PageContainer.getInstance().getBoardLinkedTitlePage();
                            page.recoverPost();
                            return false;
                        } else if (nowPage.getClass().equals(BoardSearchPage.class)) {
                            BoardSearchPage page = PageContainer.getInstance().getBoardSearchPage();
                            page.recoverPost();
                            return false;
                        }
                    }
                }
                return false;
            }
            TelnetClient.getClient().sendStringToServer("");
            return false;
        } else if (this.row_string_23.contains("要新增資料嗎？(Y/N) [N]")) {
            ASToast.showShortToast("此看板無文章");
            TelnetClient.getClient().sendStringToServer("N");
            return false;
        } else if (this.row_string_23.contains("● 請按任意鍵繼續 ●")) {
            if (this.row_string_00.contains("順利貼出佈告")) {
                // 順利貼出佈告, 請按任意鍵繼續
                TelnetPage top_page = (TelnetPage) ASNavigationController.getCurrentController().getTopController();
                if (top_page instanceof PostArticlePage || top_page instanceof BoardMainPage) {
                    // 最上層是 發文 或 看板
                    BoardMainPage page = PageContainer.getInstance().getBoardPage();
                    page.finishPost();
                } else if (top_page instanceof MailBoxPage) {
                    MailBoxPage page2 = PageContainer.getInstance().getMailBoxPage();
                    page2.finishPost();
                }
            } else if (this.row_string_02.contains("HP：") && this.row_string_02.contains("MP：")) {
                ArticlePage page = PageContainer.getInstance().getArticlePage();
                Vector<String> userData = new Vector<>();
                this.rows.forEach(row -> userData.add(row.toString()));
                page.ctrlQUser(userData);
            } else if (this.row_string_00.contains("過  路  勇  者  的  足  跡")) {
                // 逐行塞入勇者足跡
                insertHeroSteps();
            }

            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE);
            return false;
        } else if (this.row_string_23.contains("請按 [SPACE] 繼續觀賞") && this.row_string_00.contains("過  路  勇  者  的  足  跡")) {
            // 逐行塞入勇者足跡
            insertHeroSteps();
            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE);
            return false;
        } else if (this.row_string_23.startsWith("★") && !this.row_string_23.substring(1,2).isEmpty()) {
            detectMessage();
            return false;
        } else {
            return run_pass_2;
        }
    }

    /** 逐行塞入勇者足跡 */
    void insertHeroSteps() {
        boolean startCatching = false;
        HeroStep heroStep = null;
        int countRows = 0;
        for (int i = 0; i < rows.size(); i++) {
            TelnetRow fromRow = rows.get(i);
            if (startCatching) {
                // 開始擷取本文
                if (fromRow.isEmpty()) {
                    // 擷取完畢
                    startCatching = false;
                    countRows = 0;
                    TempSettings.setHeroStep(heroStep);
                } else {
                    countRows++;
                    String oldContent = heroStep.getContent();
                    // 第二行開始才加入換行
                    if (!oldContent.isEmpty())
                        oldContent+="\n";
                    // 塞入本行內容
                    heroStep.setContent(oldContent+fromRow.toContentString());
                    if (countRows>=3) {
                        // 最多留言三行, 強制結束
                        startCatching = false;
                        countRows = 0;
                        TempSettings.setHeroStep(heroStep);
                    }
                }
            } else {
                if (fromRow.getRawString().contains("(")) {
                    // 開始擷取
                    startCatching = true;
                    countRows = 0;
                    String rawString = fromRow.getRawString();
                    int nameLastIndex = rawString.indexOf(")");
                    String authorName = rawString.substring(0, nameLastIndex+1).trim();
                    String datetime = rawString.substring(nameLastIndex+2).trim();
                    heroStep = new HeroStep(authorName, datetime, "");
                }
            }
        }
    }

    void handleLoginPage() {
        setCurrentPage(BahamutPage.BAHAMUT_LOGIN);
        LoginPage page = PageContainer.getInstance().getLoginPage();
        if (page.onPagePreload()) {
            showPage(page);
        }
    }

    void handleMainPage() {
        this.nowStep = STEP_WORKING;

        if (getCurrentPage() < BahamutPage.BAHAMUT_MAIN) {
            PageContainer.getInstance().getLoginPage().onLoginSuccess();
        }

        setCurrentPage(BahamutPage.BAHAMUT_MAIN);
        MainPage page = PageContainer.getInstance().getMainPage();
        if (page.onPagePreload()) {
            showPage(page);
        }

        if (this.lastHeader.equals("本次")) {
            new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.1
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    MainPage page2 = PageContainer.getInstance().getMainPage();
                    if (page2.isTopPage()) {
                        page2.onProcessHotMessage();
                    }
                }
            }.runInMainThread();
        } else if (this.lastHeader.equals("G)")) {
            new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.2
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    MainPage page2 = PageContainer.getInstance().getMainPage();
                    if (page2.isTopPage()) {
                        page2.onCheckGoodbye();
                    }
                }
            }.runInMainThread();
        }
        if (this.row_string_23.contains("[訪客]")) {
            // 紀錄線上人數
            int startIndex = row_string_23.indexOf("[訪客] ")+4;
            int endIndex = row_string_23.indexOf(" 人");
            page.setOnlinePeople(this.row_string_23.substring(startIndex, endIndex).trim());
        }
    }

    void handleMailBoxPage() {
        setCurrentPage(BahamutPage.BAHAMUT_MAIL_BOX);
        if (this.telnetCursor.column == 1) {
            MailBoxPage page = PageContainer.getInstance().getMailBoxPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    void handleSearchBoard() {
        if (this.row_string_23.startsWith("★ 列表") && this.telnetCursor.equals(23, 29)) {
            SearchBoard_Handler.getInstance().read();
            TelnetClient.getClient().sendKeyboardInputToServer(67);
        } else if (this.telnetCursor.row == 1) {
            SearchBoard_Handler.getInstance().read();
            byte[] data = TelnetOutputBuilder.create().pushKey(TelnetKeyboard.CTRL_Y).pushString("\n\n").build();
            TelnetClient.getClient().sendDataToServer(data);
            new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.3
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    ClassPage page = PageContainer.getInstance().getClassPage();
                    page.onSearchBoardFinished();
                }
            }.runInMainThread();
        }
    }

    void handleClassPage() {
        ClassPage page;
        setCurrentPage(BahamutPage.BAHAMUT_CLASS);
        if (this.telnetCursor.column == 1 && (page = PageContainer.getInstance().getClassPage()) != null && page.onPagePreload()) {
            showPage(page);
        }
    }

    void handleBoardPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD);
        if (this.telnetCursor.column == 1) {
            BoardMainPage page = PageContainer.getInstance().getBoardPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    void handleBoardSearchPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_SEARCH);
        if (this.telnetCursor.column == 1) {
            BoardSearchPage page = PageContainer.getInstance().getBoardSearchPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    void handleBoardEssencePage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_ESSENCE);
        if (this.telnetCursor.column == 1) {
            BoardEssencePage page = PageContainer.getInstance().getBoardEssencePage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }

    }

    void handleBoardTitleLinkedPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_LINK);
        if (this.telnetCursor.column == 1) {
            BoardLinkPage page = PageContainer.getInstance().getBoardLinkedTitlePage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    /** 頁面: 個人設定  */
    void handleUserPage() {

        // 傳給個人設定, 頁面更新資料
        if (this.row_string_23.contains("修改資料(Y/N)?[N]")) {
            setCurrentPage(BahamutPage.BAHAMUT_USER_INFO_PAGE);
            // 個人資料
            UserInfoPage page = PageContainer.getInstance().getUserInfoPage();
            page.updateUserInfoPageContent(rows);
        } else if (this.row_string_23.contains("請按鍵切換設定，或按")) {
            setCurrentPage(BahamutPage.BAHAMUT_USER_CONFIG_PAGE);
            // 操作模式
            UserConfigPage page = PageContainer.getInstance().getUserConfigPage();
            page.updateUserConfigPageContent(rows);
        }
    }

    void handleArticle() {
        TelnetPage top_page = (TelnetPage) ASNavigationController.getCurrentController().getTopController();
        if (top_page instanceof BoardMainPage) {
            setCurrentPage(BahamutPage.BAHAMUT_ARTICLE);
        } else if (top_page instanceof MailBoxPage) {
            setCurrentPage(BahamutPage.BAHAMUT_MAIL);
        } else if (top_page instanceof BoardEssencePage) {
            setCurrentPage(BahamutPage.BAHAMUT_ARTICLE_ESSENCE);
        }
        myAsRunner.cancel();
        myAsRunner.postDelayed(5000);

        if (!this.duringReadingArticle) {
            onReadArticleStart();
        }
    }

    // 變更讀取條進度
    void handleArticlePercentage() {
        String resourceString = "((?<percent>\\d+)%)";
        Pattern pattern = Pattern.compile(resourceString);
        Matcher matcher = pattern.matcher(row_string_23);
        if (matcher.find()) {
            String percent = matcher.toMatchResult().group(1);
            TelnetPage top_page = (TelnetPage) ASNavigationController.getCurrentController().getTopController();
            if (top_page instanceof ArticleEssencePage page) {
                page.changeLoadingPercentage(percent);
            } else if (top_page instanceof MailPage page) {
                page.changeLoadingPercentage(percent);
            } else if (top_page instanceof ArticlePage page) {
                page.changeLoadingPercentage(percent);
            }
        }
    }

    @Override // com.kota.Telnet.TelnetStateHandler
    public void handleState() {
        loadState();
        this.telnetCursor = TelnetClient.getModel().getCursor();
        if (pass_1()) {
            if (getCurrentPage() == BahamutPage.BAHAMUT_CLASS && this.row_string_23.contains("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
                new BahamutCommandLoadMoreArticle().execute();
            } else if (getCurrentPage() > BahamutPage.BAHAMUT_MAIL_BOX && this.row_string_23.contains("文章選讀") && this.row_string_23.endsWith("搜尋作者")) {
                handleArticle();
                onReadArticleFinished();
                // 串接文章狀況下, 文章讀取完畢指令不同, 但是第23行內容一樣,會誤判,因此根據之前最後一頁判斷狀況
                int lastPage = getCurrentPage();
                if (lastPage==BahamutPage.BAHAMUT_ARTICLE)
                    new BahamutCommandLoadArticleEnd().execute();
                else
                    new BahamutCommandLoadArticleEndForSearch().execute();
            } else if (getCurrentPage() > BahamutPage.BAHAMUT_CLASS && this.row_string_23.contains("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
                handleArticle();
                onReadArticlePage();
                handleArticlePercentage();
                new BahamutCommandLoadMoreArticle().execute();
            } else if (getCurrentPage() > BahamutPage.BAHAMUT_CLASS && this.row_string_23.contains("魚雁往返") && this.row_string_23.endsWith("標記")) {
                handleArticle();
                onReadArticleFinished();
                new BahamutCommandLoadArticleEnd().execute();
            } else if (getCurrentPage() > BahamutPage.BAHAMUT_CLASS && this.row_string_23.contains("閱讀精華") && this.row_string_23.trim().endsWith("離開")) {
                handleArticle();
                onReadArticleFinished();
                new BahamutCommandLoadArticleEnd().execute();
            } else if (this.firstHeader.equals("對戰") && getCurrentPage() < 5) {
                handleLoginPage();
            } else if (this.row_string_00.contains("【主功能表】")) {
                handleMainPage();
            } else if (this.row_string_00.contains("【郵件選單】")) {
                handleMailBoxPage();
            } else if (this.row_string_00.contains("【看板列表】")) {
                if (this.row_string_01.contains("請輸入看板名稱")) {
                    handleSearchBoard();
                } else if (this.row_string_02.contains("總數")) {
                    TelnetClient.getClient().sendKeyboardInputToServer(99);
                } else {
                    handleClassPage();
                }
            } else if (this.row_string_00.contains("【主題串列】")) {
                if (PageContainer.getInstance().getBoardPage().getLastListAction() == BoardPageAction.SEARCH) {
                    handleBoardSearchPage();
                } else {
                    handleBoardTitleLinkedPage();
                }
            } else if (this.row_string_00.contains("【精華文章】")) {
                handleBoardEssencePage();
            } else if (this.row_string_00.contains("【板主：")) {
                handleBoardPage();
            } else if (this.row_string_00.contains("【個人設定】")) {
                handleUserPage();
            }else if (this.row_string_23.contains("您要刪除上述記錄嗎")) {
                TelnetClient.getClient().sendStringToServer("n");
            } else if (this.row_string_23.equals("● 請按任意鍵繼續 ●")) {
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE);
            } else if (this.lastHeader.equals("您想")) {
                new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.4
                    @Override // com.kota.ASFramework.Thread.ASRunner
                    public void run() {
                        LoginPage page = PageContainer.getInstance().getLoginPage();
                        if (page.isTopPage()) {
                            page.onSaveArticle();
                        }
                    }
                }.runInMainThread();
            } else if (this.row_string_23.contains("★ 請閱讀最新公告")) {
                TelnetClient.getClient().sendStringToServer("");
            } else if (this.nowStep == STEP_CONNECTING && this.firstHeader.equals("--")) {
                // TODO: 不知道甚麼狀況
                setCurrentPage(BahamutPage.BAHAMUT_INSTRUCTIONS);
                if (this.lastHeader.equals("●請") || this.lastHeader.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            } else if (this.nowStep == STEP_CONNECTING && this.firstHeader.equals("□□")) {
                setCurrentPage(BahamutPage.BAHAMUT_SYSTEM_ANNOUNCEMENT);
                if (this.lastHeader.equals("●請") || this.lastHeader.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            } else if (this.firstHeader.equals("【過")) {
                setCurrentPage(BahamutPage.BAHAMUT_PASSED_SIGNATURE);
                if (this.lastHeader.equals("●請") || this.lastHeader.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            }
        }
    }

    void onReadArticleStart() {
        this.duringReadingArticle = true;
        this.articleHandler.clear();
    }

    void onReadArticlePage() {
        this.articleHandler.loadPage(TelnetClient.getModel());
        cleanFrame();
    }

    void onReadArticleFinished() {
        this.articleHandler.loadLastPage(TelnetClient.getModel());
        this.articleHandler.build();
        TelnetArticle article = this.articleHandler.getArticle();
        this.articleHandler.newArticle();

        if (this.articleNumber != null) {
            article.Number = Integer.parseInt(this.articleNumber);
        }
        if (this.row_string_23.contains("魚雁往返")) {
            showMail(article);
        } else if (this.row_string_23.contains("閱讀精華")) {
            showEssence(article);
        } else {
            showArticle(article);
        }
        this.duringReadingArticle = false;
        myAsRunner.cancel();
    }
    // 強制進入讀取完畢
    ASRunner myAsRunner = new ASRunner(){
        @Override
        public void run() {
            onReadArticleFinished();
            // 串接文章狀況下, 文章讀取完畢指令不同, 但是第23行內容一樣,會誤判,因此根據之前最後一頁判斷狀況
            int lastPage = getCurrentPage();
            if (lastPage==BahamutPage.BAHAMUT_ARTICLE)
                new BahamutCommandLoadArticleEnd().execute();
            else
                new BahamutCommandLoadArticleEndForSearch().execute();
        }
    };

    // 顯示文章內文
    void showArticle(final TelnetArticle aArticle) {
        new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.5
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                try {
                    PageContainer.getInstance().getArticlePage().setArticle(aArticle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runInMainThread();
    }

    // 顯示郵件內文
    void showMail(final TelnetArticle aArticle) {
        new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.6
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                MailPage mailPage;
                try {
                    TelnetPage last_page = (TelnetPage) ASNavigationController.getCurrentController().getViewControllers().lastElement();
                    // 檢查最上層的頁面是不是 mail page
                    // 如果不是=>就把mail page推到最上層
                    if (last_page == null || last_page.getPageType() != BahamutPage.BAHAMUT_MAIL) {
                        mailPage = new MailPage();
                        ASNavigationController.getCurrentController().pushViewController(mailPage);
                    } else {
                        mailPage = (MailPage) last_page;
                    }
                    mailPage.setArticle(aArticle);
                } catch (Exception ignored) {
                }
            }
        }.runInMainThread();
    }

    // 顯示精華區內文
    void showEssence(final TelnetArticle aArticle) {
        new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.6
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                ArticleEssencePage articleEssencePage;
                try {
                    TelnetPage last_page = (TelnetPage) ASNavigationController.getCurrentController().getViewControllers().lastElement();
                    // 檢查最上層的頁面是不是 mail page
                    // 如果不是=>就把mail page推到最上層
                    if (last_page == null || last_page.getPageType() != BahamutPage.BAHAMUT_ARTICLE_ESSENCE) {
                        articleEssencePage = new ArticleEssencePage();
                        ASNavigationController.getCurrentController().pushViewController(articleEssencePage);
                    } else {
                        articleEssencePage = (ArticleEssencePage) last_page;
                    }
                    articleEssencePage.setArticle(aArticle);
                } catch (Exception ignored) {
                }
            }
        }.runInMainThread();
    }

    // 通用顯示頁面
    void showPage(TelnetPage aPage) {
        final TelnetPage top_page = (TelnetPage) ASNavigationController.getCurrentController().getTopController();
        if (aPage == top_page) {
            new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.7
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    top_page.requestPageRefresh();
                }
            }.runInMainThread();
        } else if (top_page!= null && !top_page.isPopupPage() && aPage != null) {
            if (ASNavigationController.getCurrentController().containsViewController(aPage)) {
                ASNavigationController.getCurrentController().popToViewController(aPage);
            } else {
                ASNavigationController.getCurrentController().pushViewController(aPage);
            }
        }
    }

    @Override // com.kota.Telnet.TelnetStateHandler
    public void clear() {
        this.nowStep = STEP_CONNECTING;
        setCurrentPage(BahamutPage.UNKNOWN);
    }

    String cutOffContinueMessage(String aMessage) {
        int start = 0;
        int end = aMessage.length() - 1;
        char[] words = aMessage.toCharArray();
        while (start < words.length) {
            if (words[start] == 9733 || words[start] == 32)
                start++;
            else
                break;
        }
        while (words[end] != '[' && end >= 0) {
            end--;
        }
        if (end <= start) {
            return "";
        }
        return aMessage.substring(start, end).trim();
    }
}
