package com.kota.Bahamut;

import com.kota.Bahamut.Pages.ArticlePage.ArticlePage;
import com.kota.Bahamut.Pages.BBSUser.UserConfigPage;
import com.kota.Bahamut.Pages.BillingPage;
import com.kota.Bahamut.Pages.BoardPage.BoardLinkPage;
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage;
import com.kota.Bahamut.Pages.BoardPage.BoardSearchPage;
import com.kota.Bahamut.Pages.ClassPage;
import com.kota.Bahamut.Pages.EssencePage.ArticleEssencePage;
import com.kota.Bahamut.Pages.EssencePage.BoardEssencePage;
import com.kota.Bahamut.Pages.Login.LoginPage;
import com.kota.Bahamut.Pages.MailBoxPage;
import com.kota.Bahamut.Pages.MainPage;
import com.kota.Bahamut.Pages.Messages.MessageMain;
import com.kota.Bahamut.Pages.Messages.MessageSub;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.Pages.StartPage;
import com.kota.Bahamut.Pages.Theme.ThemeManagerPage;
import com.kota.Bahamut.Pages.BBSUser.UserInfoPage;

import java.util.Stack;

/* loaded from: classes.dex */
public class PageContainer {
    private static PageContainer _instance = null;
    private StartPage _start_page = null;
    private LoginPage _login_page = null;
    private MainPage _main_page = null;
    private final Stack<ClassPage> _class_page = new Stack<>();
    private BoardMainPage _board_page = null;
    private BoardLinkPage _board_title_linked_page = null;
    private BoardSearchPage _board_search_page = null;
    private MailBoxPage _mail_page = null;
    private ArticlePage _article_page = null;
    private BillingPage _billing_page = null;
    private PostArticlePage _post_article_page = null;
    private final Stack<BoardEssencePage> _board_essence_page_list = new Stack<>();
    private ArticleEssencePage articleEssencePage = null;
    private ThemeManagerPage themeManagerPage = null;
    private UserInfoPage userInfoPage = null;
    private UserConfigPage userConfigPage = null;
    private MessageMain messageMain = null;
    private MessageSub messageSub = null;

    public static PageContainer getInstance() {
        return _instance;
    }

    public static void constructInstance() {
        _instance = new PageContainer();
    }

    private PageContainer() {
    }

    public StartPage getStartPage() {
        if (this._start_page == null) {
            this._start_page = new StartPage();
        }
        return this._start_page;
    }

    public void cleanStartPage() {
        cleanLoginPage();
        cleanMainPage();
        cleanClassPage();
        cleanBoardPage();
        cleanBoardTitleLinkedPage();
        cleanBoardSearchPage();
        cleanBoardEssencePage();
        cleanArticleEssencePage();
        cleanMainBoxPage();
        cleanArticlePage();
        cleanBillingPage();
        cleanPostArticlePage();
        cleanThemeManagerPage();
        cleanUserInfoPage();
        cleanUserConfigPage();
        cleanMessageMain();
        cleanMessageSub();
    }

    public LoginPage getLoginPage() {
        if (this._login_page == null) {
            this._login_page = new LoginPage();
        }
        return this._login_page;
    }

    public void cleanLoginPage() {
        if (this._login_page != null) {
            this._login_page.clear();
            this._login_page = null;
        }
    }

    public MainPage getMainPage() {
        if (this._main_page == null) {
            this._main_page = new MainPage();
        }
        return this._main_page;
    }

    public void cleanMainPage() {
        if (this._main_page != null) {
            this._main_page.clear();
            this._main_page = null;
        }
    }

    public void pushClassPage(String aClassName, String aClassTitle) {
        ClassPage class_page = new ClassPage();
        class_page.setListName(aClassName);
        class_page.setClassTitle(aClassTitle);
        this._class_page.push(class_page);
    }

    public void popClassPage() {
        if (this._class_page.size() > 0) {
            this._class_page.pop();
        }
    }

    public ClassPage getClassPage() {
        if (this._class_page.size() > 0) {
            return this._class_page.lastElement();
        }
        return null;
    }

    public void cleanClassPage() {
        for (ClassPage page : this._class_page) {
            page.clear();
        }
        this._class_page.clear();
    }

    public BoardMainPage getBoardPage() {
        if (this._board_page == null) {
            this._board_page = new BoardMainPage();
        }
        return this._board_page;
    }

    public void cleanBoardPage() {
        if (this._board_page != null) {
            this._board_page.clear();
            this._board_page = null;
        }
    }

    public BoardLinkPage getBoardLinkedTitlePage() {
        if (this._board_title_linked_page == null) {
            this._board_title_linked_page = new BoardLinkPage();
        }
        return this._board_title_linked_page;
    }

    public void cleanBoardTitleLinkedPage() {
        if (this._board_title_linked_page != null) {
            this._board_title_linked_page.clear();
            this._board_title_linked_page = null;
        }
    }

    public BoardSearchPage getBoardSearchPage() {
        if (this._board_search_page == null) {
            this._board_search_page = new BoardSearchPage();
        }
        return this._board_search_page;
    }

    public void cleanBoardSearchPage() {
        if (this._board_search_page != null) {
            this._board_search_page.clear();
            this._board_search_page = null;
        }
    }
    public BoardEssencePage getBoardEssencePage() {
        if (this._board_essence_page_list.size() > 0) {
            return this._board_essence_page_list.lastElement();
        }
        return null;
    }

    public void cleanBoardEssencePage() {
        for (BoardEssencePage page : this._board_essence_page_list) {
            page.clear();
        }
        this._board_essence_page_list.clear();
    }

    public void pushBoardEssencePage(String aClassName, String aClassTitle) {
        BoardEssencePage boardEssencePage = new BoardEssencePage();
        boardEssencePage.clear();
        boardEssencePage.setListName(aClassName);
        boardEssencePage.setClassTitle(aClassTitle);
        this._board_essence_page_list.push(boardEssencePage);
    }

    public void popBoardEssencePage() {
        if (this._board_essence_page_list.size() > 0) {
            this._board_essence_page_list.pop();
        }
    }
    public ArticleEssencePage getArticleEssencePage() {
        if (this.articleEssencePage == null) {
            this.articleEssencePage = new ArticleEssencePage();
        }
        return this.articleEssencePage;
    }

    public void cleanArticleEssencePage() {
        if (this.articleEssencePage != null) {
            this.articleEssencePage.clear();
            this.articleEssencePage = null;
        }
    }

    public MailBoxPage getMailBoxPage() {
        if (this._mail_page == null) {
            this._mail_page = new MailBoxPage();
        }
        return this._mail_page;
    }

    public void cleanMainBoxPage() {
        if (this._mail_page != null) {
            this._mail_page.clear();
            this._mail_page = null;
        }
    }

    public ArticlePage getArticlePage() {
        if (this._article_page == null) {
            this._article_page = new ArticlePage();
        }
        return this._article_page;
    }

    public void cleanArticlePage() {
        if (this._article_page != null) {
            this._article_page.clear();
            this._article_page = null;
        }
    }

    public BillingPage getBillingPage() {
        if (this._billing_page == null) {
            this._billing_page = new BillingPage();
        }
        return this._billing_page;
    }

    public void cleanBillingPage() {
        if (this._billing_page != null) {
            this._billing_page.clear();
            this._billing_page = null;
        }
    }

    public PostArticlePage getPostArticlePage() {
        if (this._post_article_page == null) {
            this._post_article_page = new PostArticlePage();
        }
        return this._post_article_page;
    }

    public void cleanPostArticlePage() {
        if (this._post_article_page != null) {
            this._post_article_page.clear();
            this._post_article_page = null;
        }
    }
    public ThemeManagerPage getThemeManagerPage() {
        if (this.themeManagerPage == null) {
            this.themeManagerPage = new ThemeManagerPage();
        }
        return this.themeManagerPage;
    }

    public void cleanThemeManagerPage() {
        if (this.themeManagerPage != null) {
            this.themeManagerPage.clear();
            this.themeManagerPage = null;
        }
    }
    public UserInfoPage getUserInfoPage() {
        if (this.userInfoPage == null) {
            this.userInfoPage = new UserInfoPage();
        }
        return this.userInfoPage;
    }

    public void cleanUserInfoPage() {
        if (this.userInfoPage != null) {
            this.userInfoPage.clear();
            this.userInfoPage = null;
        }
    }

    public UserConfigPage getUserConfigPage() {
        if (this.userConfigPage == null) {
            this.userConfigPage = new UserConfigPage();
        }
        return this.userConfigPage;
    }

    public void cleanUserConfigPage() {
        if (this.userConfigPage != null) {
            this.userConfigPage.clear();
            this.userConfigPage = null;
        }
    }

    public MessageMain getMessageMain() {
        if (this.messageMain == null) {
            this.messageMain = new MessageMain();
        }
        return this.messageMain;
    }
    public void cleanMessageMain() {
        if (this.messageMain != null) {
            this.messageMain.clear();
            this.messageMain = null;
        }
    }

    public MessageSub getMessageSub() {
        if (this.messageSub == null) {
            this.messageSub = new MessageSub();
        }
        return this.messageSub;
    }
    public void cleanMessageSub() {
        if (this.messageSub != null) {
            this.messageSub.clear();
            this.messageSub = null;
        }
    }
}
