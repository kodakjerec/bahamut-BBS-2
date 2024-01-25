package com.kota.Bahamut;

import com.kota.Bahamut.Pages.ArticlePage;
import com.kota.Bahamut.Pages.BoardLinkPage;
import com.kota.Bahamut.Pages.BoardPage;
import com.kota.Bahamut.Pages.BoardSearchPage;
import com.kota.Bahamut.Pages.ClassPage;
import com.kota.Bahamut.Pages.LoginPage;
import com.kota.Bahamut.Pages.MailBoxPage;
import com.kota.Bahamut.Pages.MainPage;
import com.kota.Bahamut.Pages.StartPage;
import java.util.Iterator;
import java.util.Stack;

/* loaded from: classes.dex */
public class PageContainer {
    private static PageContainer _instance = null;
    private StartPage _start_page = null;
    private LoginPage _login_page = null;
    private MainPage _main_page = null;
    private Stack<ClassPage> _class_page = new Stack<>();
    private BoardPage _board_page = null;
    private BoardLinkPage _board_title_linked_page = null;
    private BoardSearchPage _board_search_page = null;
    private MailBoxPage _mail_page = null;
    private ArticlePage _article_page = null;

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
        if (this._start_page != null) {
            this._start_page.clear();
            this._start_page = null;
        }
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
        if (this._class_page != null) {
            Iterator<ClassPage> it = this._class_page.iterator();
            while (it.hasNext()) {
                ClassPage page = it.next();
                page.clear();
            }
            this._class_page.clear();
        }
    }

    public BoardPage getBoardPage() {
        if (this._board_page == null) {
            this._board_page = new BoardPage();
        }
        return this._board_page;
    }

    public void cleanBoardPage() {
        if (this._board_page != null) {
            this._board_page.clear();
            this._board_page = null;
        }
    }

    public BoardLinkPage getBoard_linked_Title_page() {
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

    public BoardSearchPage getBoard_Search_page() {
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
}
