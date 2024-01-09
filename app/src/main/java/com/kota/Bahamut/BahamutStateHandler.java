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
import java.util.List;
import java.util.Map;

public class BahamutStateHandler extends TelnetStateHandler {
    private static final int STEP_CONNECTING = 0;
    private static final int STEP_WORKING = 1;
    public static final int UNKNOWN = -1;
    private static BahamutStateHandler _instance = null;
    private final Article_Handler _article_handler = new Article_Handler();
    public String _article_number;
    private TelnetCursor _cursor = null;
    private boolean _reading_article = false;
    private int _step = 0;
    private String first_header = "";
    private String last_header = "";
    private String row_string_00 = "";
    private String row_string_01 = "";
    private String row_string_02 = "";
    private String row_string_23 = "";

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
                if (i < row.data.length) {
                    byte background_color = row.backgroundColor[i];
                    byte data = row.data[i];
                    if (background_color != 6) {
                        if (background_color != 5) {
                            end_point = i;
                            break;
                        }
                        msg_buffer.write(data);
                    } else {
                        name_buffer.write(data);
                    }
                    i++;
                }
            }
            if (name_buffer.size() > 0 && msg_buffer.size() > 0) {
                String name = B2UEncoder.getInstance().encodeToString(name_buffer.toByteArray());
                String msg = B2UEncoder.getInstance().encodeToString(msg_buffer.toByteArray());
                if (end_point == column && name.startsWith("★")) {
                    String name2 = name.substring(1, name.length() - 1);
                    String msg2 = msg.substring(1);
                    AppDatabase db = new AppDatabase(MyApplication.getInstance());
                    db.saveMessage(name2, msg2);
                    List<Map<String, Object>> loadMessages = db.loadMessages();
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
        if (this.row_string_23.startsWith("您有一篇文章尚未完成")) {
            TelnetClient.getClient().sendStringToServer("S\n1\n");
            run_pass_2 = false;
        }
        if (run_pass_2 && this.row_string_23.endsWith("[請按任意鍵繼續]") && getCurrentPage() != 1) {
            String continue_message = cutOffContinueMessage(this.row_string_23);
            if (continue_message.length() > 0) {
                ASToast.showShortToast(continue_message);
            }
            if (this.row_string_23.startsWith("★ 引言太多")) {
                TelnetClient.getClient().sendDataToServer(TelnetOutputBuilder.create().pushKey(32).pushKey(24).pushString("a\n\n").build());
                TelnetPage top_page = (TelnetPage) ASNavigationController.getCurrentController().getTopController();
                if (top_page instanceof BoardPage) {
                    PageContainer.getInstance().getBoardPage().recoverPost();
                } else if (top_page instanceof MailBoxPage) {
                    PageContainer.getInstance().getMailBoxPage().recoverPost();
                }
                return false;
            }
            TelnetClient.getClient().sendStringToServer("");
            return false;
        } else if (this.row_string_23.equals("要新增資料嗎？(Y/N) [N]")) {
            ASToast.showShortToast("此看板無文章");
            TelnetClient.getClient().sendStringToServer("N");
            return false;
        } else if (!this.row_string_23.equals("● 請按任意鍵繼續 ●")) {
            return run_pass_2;
        } else {
            TelnetClient.getClient().sendKeyboardInputToServer(256);
            return false;
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
            new ASRunner() {
                public void run() {
                    MainPage page = PageContainer.getInstance().getMainPage();
                    if (page.isTopPage()) {
                        page.onProcessHotMessage();
                    }
                }
            }.runInMainThread();
        } else if (this.last_header.equals("G)")) {
            new ASRunner() {
                public void run() {
                    MainPage page = PageContainer.getInstance().getMainPage();
                    if (page.isTopPage()) {
                        page.onCheckGoodbye();
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
            TelnetClient.getClient().sendDataToServer(TelnetOutputBuilder.create().pushData((byte) 25).pushString("\n\n").build());
            new ASRunner() {
                public void run() {
                    PageContainer.getInstance().getClassPage().onSearchBoardFinished();
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
            BoardSearchPage page = PageContainer.getInstance().getBoard_Search_Page();
            if (page.onPagePreload()) {
                showPage(page);
            }
        }
    }

    public void handleBoardTitleLinkedPage() {
        setCurrentPage(12);
        if (this._cursor.column == 1) {
            BoardLinkPage page = PageContainer.getInstance().getBoard_Linked_Title_Page();
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

    public void handleState() {
        loadState();
        this._cursor = TelnetClient.getModel().getCursor();
        if (!pass_1()) {
            return;
        }
        if (getCurrentPage() == 6 && this.row_string_23.startsWith("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
            TelnetClient.getClient().sendKeyboardInputToServer(256);
        } else if (getCurrentPage() > 9 && this.row_string_23.startsWith("文章選讀") && this.row_string_23.endsWith("搜尋作者")) {
            handleArticle();
            onReadArticleFinished();
            TelnetClient.getClient().sendKeyboardInputToServer(256);
        } else if (getCurrentPage() > 6 && this.row_string_23.startsWith("魚雁往返") && this.row_string_23.endsWith("標記")) {
            handleArticle();
            onReadArticleFinished();
            TelnetClient.getClient().sendKeyboardInputToServer(256);
        } else if (getCurrentPage() > 6 && this.row_string_23.startsWith("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
            handleArticle();
            onReadArticlePage();
            TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.PAGE_DOWN, 1);
        } else if (this.first_header.equals("對戰") && getCurrentPage() < 5) {
            handleLoginPage();
        } else if (this.row_string_00.startsWith("【主功能表】")) {
            handleMainPage();
        } else if (this.row_string_00.startsWith("【郵件選單】")) {
            handleMailBoxPage();
        } else if (this.row_string_00.startsWith("【看板列表】")) {
            if (this.row_string_01.startsWith("請輸入看板名稱")) {
                handleSearchBoard();
            } else if (this.row_string_02.startsWith("總數")) {
                TelnetClient.getClient().sendKeyboardInputToServer(99);
            } else {
                handleClassPage();
            }
        } else if (this.row_string_00.startsWith("【主題串列】")) {
            if (PageContainer.getInstance().getBoardPage().getLastListAction() == 1) {
                handleBoardSearchPage();
            } else {
                handleBoardTitleLinkedPage();
            }
        } else if (this.row_string_00.startsWith("【板主：")) {
            handleBoardPage();
        } else if (this.row_string_23.startsWith("您要刪除上述記錄嗎")) {
            TelnetClient.getClient().sendStringToServer("n");
        } else if (this.row_string_23.equals("● 請按任意鍵繼續 ●")) {
            TelnetClient.getClient().sendKeyboardInputToServer(256);
        } else if (this.last_header.equals("您想")) {
            new ASRunner() {
                public void run() {
                    LoginPage page = PageContainer.getInstance().getLoginPage();
                    if (page.isTopPage()) {
                        page.onSaveArticle();
                    }
                }
            }.runInMainThread();
        } else if (this.row_string_23.startsWith("★ 請閱讀最新公告")) {
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
        BoardPage boardPage = PageContainer.getInstance().getBoardPage();
        if (this._article_number != null) {
            article.Number = Integer.parseInt(this._article_number);
        }
        if (this.row_string_23.startsWith("魚雁往返")) {
            showMail(article);
        } else {
            showArticle(article);
        }
        this._reading_article = false;
    }

    private void showArticle(final TelnetArticle aArticle) {
        new ASRunner() {
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
        new ASRunner() {
            public void run() {
                MailPage mail_page;
                MailPage mail_page2 = null;
                try {
                    TelnetPage last_page = (TelnetPage) ASNavigationController.getCurrentController().getViewControllers().lastElement();
                    if (last_page == null || last_page.getPageType() != 15) {
                        mail_page = null;
                    } else {
                        mail_page = (MailPage) last_page;
                    }
                    if (mail_page == null) {
                        try {
                            mail_page2 = new MailPage();
                            ASNavigationController.getCurrentController().pushViewController(mail_page2);
                        } catch (Exception e) {
                            e = e;
                            MailPage mailPage = mail_page;
                            e.printStackTrace();
                        }
                    } else {
                        mail_page2 = mail_page;
                    }
                    mail_page2.setArticle(aArticle);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }.runInMainThread();
    }

    public void showPage(TelnetPage aPage) {
        final TelnetPage top_page = (TelnetPage) ASNavigationController.getCurrentController().getTopController();
        if (aPage == top_page) {
            new ASRunner() {
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

    public void clear() {
        this._step = 0;
        setCurrentPage(-1);
    }

    private String cutOffContinueMessage(String aMessage) {
        int start = 0;
        int end = aMessage.length() - 1;
        char[] words = aMessage.toCharArray();
        while (true) {
            if ((words[start] == 9733 || words[start] == ' ') && start < words.length) {
                start++;
            } else {
                break;
            }
        }
        while (words[end] != '[' && end >= 0) {
            end--;
        }
        if (end > start) {
            return aMessage.substring(start, end).trim();
        }
        return "";
    }
}
