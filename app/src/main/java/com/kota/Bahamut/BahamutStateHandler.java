package com.kota.Bahamut;

import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.Command.BahamutCommandLoadArticleEnd;
import com.kota.Bahamut.Command.BahamutCommandLoadArticleEndForSearch;
import com.kota.Bahamut.Command.BahamutCommandLoadMoreArticle;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage;
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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public class BahamutStateHandler extends TelnetStateHandler {
    static final int STEP_CONNECTING = 0;
    static final int STEP_WORKING = 1;
    static final int UNKNOWN = -1;
    static BahamutStateHandler _instance = null;
    String _article_number;
    int _step = 0;
    Vector<TelnetRow> rows = new Vector<>(); // debug用
    String row_string_00 = "";
    String row_string_01 = "";
    String row_string_02 = "";
    String row_string_23 = "";
    String first_header = "";
    String last_header = "";
    TelnetCursor _cursor = null;
    final Article_Handler _article_handler = new Article_Handler();
    boolean _reading_article = false;

    public void setArticleNumber(String aArticleNumber) {
        this._article_number = aArticleNumber;
    }

    public static BahamutStateHandler getInstance() {
        if (_instance == null) {
            _instance = new BahamutStateHandler();
        }
        return _instance;
    }

    BahamutStateHandler() {
    }

    void loadState() {
        this.row_string_00 = getRowString(0).trim();
        this.row_string_01 = getRowString(1).trim();
        this.row_string_02 = getRowString(2).trim();
        this.row_string_23 = getRowString(23).trim();
        this.rows = getRows();
        this.first_header = TelnetUtils.getHeader(this.row_string_00);
        this.last_header = TelnetUtils.getHeader(this.row_string_23);
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

    boolean detectMessage() {
        try {
            int column = this._cursor.column;
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
                    AppDatabase db = new AppDatabase(MyApplication.getInstance());
                    db.saveMessage(name2, msg2);
                    db.loadMessages();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
     */

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
                this.rows.forEach(row-> userData.add(row.toString()));
                page.ctrlQUser(userData);
            }

            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE);
            return false;
        } else {
            return run_pass_2;
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
        this._step = 1;
        if (getCurrentPage() < BahamutPage.BAHAMUT_MAIN) {
            PageContainer.getInstance().getLoginPage().onLoginSuccess();
        }

        setCurrentPage(BahamutPage.BAHAMUT_MAIN);
        MainPage page = PageContainer.getInstance().getMainPage();
        if (page.onPagePreload()) {
            showPage(page);
        }
        if (this.last_header.equals("本次")) {
            new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.1
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    MainPage page2 = PageContainer.getInstance().getMainPage();
                    if (page2.isTopPage()) {
                        page2.onProcessHotMessage();
                    }
                }
            }.runInMainThread();
        } else if (this.last_header.equals("G)")) {
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
    }

    void handleMailBoxPage() {
        setCurrentPage(BahamutPage.BAHAMUT_MAIL_BOX);
        if (this._cursor.column == 1) {
            MailBoxPage page = PageContainer.getInstance().getMailBoxPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    void handleSearchBoard() {
        if (this.row_string_23.startsWith("★ 列表") && this._cursor.equals(23, 29)) {
            SearchBoard_Handler.getInstance().read();
            TelnetClient.getClient().sendKeyboardInputToServer(67);
        } else if (this._cursor.row == 1) {
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
        if (this._cursor.column == 1 && (page = PageContainer.getInstance().getClassPage()) != null && page.onPagePreload()) {
            showPage(page);
        }
    }

    void handleBoardPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD);
        if (this._cursor.column == 1) {
            BoardMainPage page = PageContainer.getInstance().getBoardPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    void handleBoardSearchPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_SEARCH);
        if (this._cursor.column == 1) {
            BoardSearchPage page = PageContainer.getInstance().getBoardSearchPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    void handleBoardEssencePage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_ESSENCE);
        if (this._cursor.column == 1) {
            BoardEssencePage page = PageContainer.getInstance().getBoardEssencePage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }

    }

    void handleBoardTitleLinkedPage() {
        setCurrentPage(BahamutPage.BAHAMUT_BOARD_LINK);
        if (this._cursor.column == 1) {
            BoardLinkPage page = PageContainer.getInstance().getBoardLinkedTitlePage();
            if (page.onPagePreload()) {
                showPage(page);
            }
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
        if (!this._reading_article) {
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
        this._cursor = TelnetClient.getModel().getCursor();
        if (pass_1()) {
            if (getCurrentPage() == BahamutPage.BAHAMUT_CLASS && this.row_string_23.contains("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
                new BahamutCommandLoadMoreArticle().execute();
            } else if (getCurrentPage() > BahamutPage.BAHAMUT_MAIL_BOX && this.row_string_23.contains("文章選讀") && this.row_string_23.endsWith("搜尋作者")) {
                handleArticle();
                onReadArticleFinished();
                // 2024.4.3 部分文章瀏覽到最底部按left arrow還是停留在最底部, 會有問題, 先改用其他方式看看
                if (getCurrentPage()==BahamutPage.BAHAMUT_ARTICLE)
                    new BahamutCommandLoadArticleEndForSearch().execute();
                else
                    new BahamutCommandLoadArticleEnd().execute();
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
            } else if (this.first_header.equals("對戰") && getCurrentPage() < 5) {
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
            } else if (this.row_string_23.contains("您要刪除上述記錄嗎")) {
                TelnetClient.getClient().sendStringToServer("n");
            } else if (this.row_string_23.equals("● 請按任意鍵繼續 ●")) {
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.SPACE);
            } else if (this.last_header.equals("您想")) {
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
            } else if (this._step == 0 && this.first_header.equals("--")) {
                setCurrentPage(BahamutPage.BAHAMUT_INSTRUCTIONS);
                if (this.last_header.equals("●請") || this.last_header.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            } else if (this._step == 0 && this.first_header.equals("□□")) {
                setCurrentPage(BahamutPage.BAHAMUT_SYSTEM_ANNOUNCEMENT);
                if (this.last_header.equals("●請") || this.last_header.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            } else if (this.first_header.equals("【過")) {
                setCurrentPage(BahamutPage.BAHAMUT_PASSED_SIGNATURE);
                if (this.last_header.equals("●請") || this.last_header.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            }
        }
    }

    void onReadArticleStart() {
        this._reading_article = true;
        this._article_handler.clear();
    }

    void onReadArticlePage() {
        this._article_handler.loadPage(TelnetClient.getModel());
        cleanFrame();
    }

    void onReadArticleFinished() {
        this._article_handler.loadLastPage(TelnetClient.getModel());
        this._article_handler.build();
        TelnetArticle article = this._article_handler.getArticle();
        this._article_handler.newArticle();
//        PageContainer.getInstance().getBoardPage();
        if (this._article_number != null) {
            article.Number = Integer.parseInt(this._article_number);
        }
        if (this.row_string_23.contains("魚雁往返")) {
            showMail(article);
        } else if (this.row_string_23.contains("閱讀精華")) {
            showEssence(article);
        } else {
            showArticle(article);
        }
        this._reading_article = false;
    }

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
        } else if (!top_page.isPopupPage() && aPage != null) {
            if (ASNavigationController.getCurrentController().containsViewController(aPage)) {
                ASNavigationController.getCurrentController().popToViewController(aPage);
            } else {
                ASNavigationController.getCurrentController().pushViewController(aPage);
            }
        }
    }

    @Override // com.kota.Telnet.TelnetStateHandler
    public void clear() {
        this._step = 0;
        setCurrentPage(-1);
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
