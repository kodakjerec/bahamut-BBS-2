package com.kota.Bahamut;

import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.DataModels.AppDatabase;
import com.kota.Bahamut.Pages.BoardLinkPage;
import com.kota.Bahamut.Pages.BoardPage;
import com.kota.Bahamut.Pages.BoardSearchPage;
import com.kota.Bahamut.Pages.ClassPage;
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
import com.kota.TextEncoder.B2UEncoder;
import java.io.ByteArrayOutputStream;

/* loaded from: classes.dex */
public class BahamutStateHandler extends TelnetStateHandler {
    private static final int STEP_CONNECTING = 0;
    private static final int STEP_WORKING = 1;
    public static final int UNKNOWN = -1;
    private static BahamutStateHandler _instance = null;
    public String _article_number;
    private int _step = 0;
    private String row_string_00 = "";
    private String row_string_01 = "";
    private String row_string_02 = "";
    private String row_string_23 = "";
    private String first_header = "";
    private String last_header = "";
    private TelnetCursor _cursor = null;
    private Article_Handler _article_handler = new Article_Handler();
    private boolean _reading_article = false;

    public void setArticleNumber(String aArticleNumber) {
        this._article_number = aArticleNumber;
    }

    public static BahamutStateHandler getInstance() {
        if (_instance == null) {
            _instance = new BahamutStateHandler();
        }
        return _instance;
    }

    private BahamutStateHandler() {
    }

    private void loadState() {
        this.row_string_00 = getRowString(0).trim();
        this.row_string_01 = getRowString(1).trim();
        this.row_string_02 = getRowString(2).trim();
        this.row_string_23 = getRowString(23).trim();
        this.first_header = TelnetUtils.getHeader(this.row_string_00);
        this.last_header = TelnetUtils.getHeader(this.row_string_23);
    }

    private void printState() {
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

    private boolean detectMessage() {
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

    private boolean pass_1() {
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
                if (top_page instanceof PostArticlePage || top_page instanceof BoardPage) {
                    // 最上層是 發文 或 看板
                    BoardPage page = PageContainer.getInstance().getBoardPage();
                    page.recoverPost();
                } else if (top_page instanceof MailBoxPage) {
                    MailBoxPage page2 = PageContainer.getInstance().getMailBoxPage();
                    page2.recoverPost();
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
                if (top_page instanceof PostArticlePage || top_page instanceof BoardPage) {
                    // 最上層是 發文 或 看板
                    BoardPage page = PageContainer.getInstance().getBoardPage();
                    page.finishPost();
                } else if (top_page instanceof MailBoxPage) {
                    MailBoxPage page2 = PageContainer.getInstance().getMailBoxPage();
                    page2.finishPost();
                }
            }

            TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW);
            return false;
        } else {
            return run_pass_2;
        }
    }

    public void handleLoginPage() {
        setCurrentPage(1);
        LoginPage page = PageContainer.getInstance().getLoginPage();
        if (page.onPagePreload()) {
            showPage(page);
        }
    }

    public void handleMainPage() {
        this._step = 1;
        if (getCurrentPage() < 5) {
            PageContainer.getInstance().getLoginPage().onLoginSuccess();
        }
        setCurrentPage(5);
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

    public void handleMailBoxPage() {
        setCurrentPage(9);
        if (this._cursor.column == 1) {
            MailBoxPage page = PageContainer.getInstance().getMailBoxPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    public void handleSearchBoard() {
        if (this.row_string_23.startsWith("★ 列表") && this._cursor.equals(23, 29)) {
            SearchBoard_Handler.getInstance().read();
            TelnetClient.getClient().sendKeyboardInputToServer(67);
        } else if (this._cursor.row == 1) {
            SearchBoard_Handler.getInstance().read();
            byte[] data = TelnetOutputBuilder.create().pushData((byte) 25).pushString("\n\n").build();
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

    public void handleClassPage() {
        ClassPage page;
        setCurrentPage(6);
        if (this._cursor.column == 1 && (page = PageContainer.getInstance().getClassPage()) != null && page.onPagePreload()) {
            showPage(page);
        }
    }

    public void handleBoardPage() {
        setCurrentPage(10);
        if (this._cursor.column == 1) {
            BoardPage page = PageContainer.getInstance().getBoardPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    public void handleBoardSearchPage() {
        setCurrentPage(13);
        if (this._cursor.column == 1) {
            BoardSearchPage page = PageContainer.getInstance().getBoardSearchPage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    public void handleBoardTitleLinkedPage() {
        setCurrentPage(12);
        if (this._cursor.column == 1) {
            BoardLinkPage page = PageContainer.getInstance().getBoardLinkedTitlePage();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    public void handleArticle() {
        setCurrentPage(14);
        if (!this._reading_article) {
            onReadArticleStart();
        }
    }

    @Override // com.kota.Telnet.TelnetStateHandler
    public void handleState() {
        loadState();
        this._cursor = TelnetClient.getModel().getCursor();
        if (pass_1()) {
            if (getCurrentPage() == 6 && this.row_string_23.contains("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW);
            } else if (getCurrentPage() > 9 && this.row_string_23.contains("文章選讀") && this.row_string_23.endsWith("搜尋作者")) {
                handleArticle();
                onReadArticleFinished();
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW);
            } else if (getCurrentPage() > 6 && this.row_string_23.contains("魚雁往返") && this.row_string_23.endsWith("標記")) {
                handleArticle();
                onReadArticleFinished();
                TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW);
            } else if (getCurrentPage() > 6 && this.row_string_23.contains("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
                handleArticle();
                onReadArticlePage();
                TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.PAGE_DOWN, 1);
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
                if (PageContainer.getInstance().getBoardPage().getLastListAction() == 1) {
                    handleBoardSearchPage();
                } else {
                    handleBoardTitleLinkedPage();
                }
            } else if (this.row_string_00.contains("【板主：")) {
                handleBoardPage();
            } else if (this.row_string_23.contains("您要刪除上述記錄嗎")) {
                TelnetClient.getClient().sendStringToServer("n");
            } else if (this.row_string_23.equals("● 請按任意鍵繼續 ●")) {
                TelnetClient.getClient().sendKeyboardInputToServer(256);
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
                setCurrentPage(3);
                if (this.last_header.equals("●請") || this.last_header.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            } else if (this._step == 0 && this.first_header.equals("□□")) {
                setCurrentPage(2);
                if (this.last_header.equals("●請") || this.last_header.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            } else if (this.first_header.equals("【過")) {
                setCurrentPage(4);
                if (this.last_header.equals("●請") || this.last_header.equals("請按")) {
                    TelnetClient.getClient().sendStringToServer("");
                }
            }
        }
    }

    private void onReadArticleStart() {
        this._reading_article = true;
        this._article_handler.clear();
    }

    private void onReadArticlePage() {
        this._article_handler.loadPage(TelnetClient.getModel());
        cleanFrame();
    }

    private void onReadArticleFinished() {
        this._article_handler.loadLastPage(TelnetClient.getModel());
        this._article_handler.build();
        TelnetArticle article = this._article_handler.getArticle();
        this._article_handler.newArticle();
        PageContainer.getInstance().getBoardPage();
        if (this._article_number != null) {
            article.Number = Integer.parseInt(this._article_number);
        }
        if (this.row_string_23.contains("魚雁往返")) {
            showMail(article);
        } else {
            showArticle(article);
        }
        this._reading_article = false;
    }

    private void showArticle(final TelnetArticle aArticle) {
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

    private void showMail(final TelnetArticle aArticle) {
        new ASRunner() { // from class: com.kota.Bahamut.BahamutStateHandler.6
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                MailPage mail_page;
                MailPage mail_page2;
                try {
                    TelnetPage last_page = (TelnetPage) ASNavigationController.getCurrentController().getViewControllers().lastElement();
                    if (last_page == null || last_page.getPageType() != 15) {
                        mail_page = null;
                    } else {
                        MailPage mail_page3 = (MailPage) last_page;
                        mail_page = mail_page3;
                    }
                    if (mail_page == null) {
                        try {
                            mail_page2 = new MailPage();
                            ASNavigationController.getCurrentController().pushViewController(mail_page2);
                        } catch (Exception e) {
                            e = e;
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        mail_page2 = mail_page;
                    }
                    mail_page2.setArticle(aArticle);
                } catch (Exception e2) {
                    Exception e = e2;
                }
            }
        }.runInMainThread();
    }

    public void showPage(TelnetPage aPage) {
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

    private String cutOffContinueMessage(String aMessage) {
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
        String message = aMessage.substring(start, end).trim();
        return message;
    }
}
