package com.kumi.Bahamut;

import com.kumi.Bahamut.Pages.ArticlePage;
import com.kumi.Bahamut.Pages.BoardLinkPage;
import com.kumi.Bahamut.Pages.BoardPage;
import com.kumi.Bahamut.Pages.BoardSearchPage;
import com.kumi.Bahamut.Pages.ClassPage;
import com.kumi.Bahamut.Pages.LoginPage;
import com.kumi.Bahamut.Pages.MailBoxPage;
import com.kumi.Bahamut.Pages.MainPage;
import com.kumi.Bahamut.Pages.StartPage;
import java.util.Iterator;
import java.util.Stack;

public class PageContainer {
  private static PageContainer _instance = null;
  
  private ArticlePage _article_page = null;
  
  private BoardPage _board_page = null;
  
  private BoardSearchPage _board_search_page = null;
  
  private BoardLinkPage _board_title_linked_page = null;
  
  private Stack<ClassPage> _class_page = new Stack<ClassPage>();
  
  private LoginPage _login_page = null;
  
  private MailBoxPage _mail_page = null;
  
  private MainPage _main_page = null;
  
  private StartPage _start_page = null;
  
  public static void constructInstance() {
    _instance = new PageContainer();
  }
  
  public static PageContainer getInstance() {
    return _instance;
  }
  
  public void cleanArticlePage() {
    if (this._article_page != null) {
      this._article_page.clear();
      this._article_page = null;
    } 
  }
  
  public void cleanBoardPage() {
    if (this._board_page != null) {
      this._board_page.clear();
      this._board_page = null;
    } 
  }
  
  public void cleanBoardSearchPage() {
    if (this._board_search_page != null) {
      this._board_search_page.clear();
      this._board_search_page = null;
    } 
  }
  
  public void cleanBoardTitleLinkedPage() {
    if (this._board_title_linked_page != null) {
      this._board_title_linked_page.clear();
      this._board_title_linked_page = null;
    } 
  }
  
  public void cleanClassPage() {
    if (this._class_page != null) {
      Iterator<ClassPage> iterator = this._class_page.iterator();
      while (iterator.hasNext())
        ((ClassPage)iterator.next()).clear(); 
      this._class_page.clear();
    } 
  }
  
  public void cleanLoginPage() {
    if (this._login_page != null) {
      this._login_page.clear();
      this._login_page = null;
    } 
  }
  
  public void cleanMainBoxPage() {
    if (this._mail_page != null) {
      this._mail_page.clear();
      this._mail_page = null;
    } 
  }
  
  public void cleanMainPage() {
    if (this._main_page != null) {
      this._main_page.clear();
      this._main_page = null;
    } 
  }
  
  public void cleanStartPage() {
    if (this._start_page != null) {
      this._start_page.clear();
      this._start_page = null;
    } 
  }
  
  public ArticlePage getArticlePage() {
    if (this._article_page == null)
      this._article_page = new ArticlePage(); 
    return this._article_page;
  }
  
  public BoardPage getBoardPage() {
    if (this._board_page == null)
      this._board_page = new BoardPage(); 
    return this._board_page;
  }
  
  public BoardLinkPage getBoard_Linked_Title_Page() {
    if (this._board_title_linked_page == null)
      this._board_title_linked_page = new BoardLinkPage(); 
    return this._board_title_linked_page;
  }
  
  public BoardSearchPage getBoard_Search_Page() {
    if (this._board_search_page == null)
      this._board_search_page = new BoardSearchPage(); 
    return this._board_search_page;
  }
  
  public ClassPage getClassPage() {
    return (this._class_page.size() > 0) ? this._class_page.lastElement() : null;
  }
  
  public LoginPage getLoginPage() {
    if (this._login_page == null)
      this._login_page = new LoginPage(); 
    return this._login_page;
  }
  
  public MailBoxPage getMailBoxPage() {
    if (this._mail_page == null)
      this._mail_page = new MailBoxPage(); 
    return this._mail_page;
  }
  
  public MainPage getMainPage() {
    if (this._main_page == null)
      this._main_page = new MainPage(); 
    return this._main_page;
  }
  
  public StartPage getStartPage() {
    if (this._start_page == null)
      this._start_page = new StartPage(); 
    return this._start_page;
  }
  
  public void popClassPage() {
    if (this._class_page.size() > 0)
      this._class_page.pop(); 
  }
  
  public void pushClassPage(String paramString1, String paramString2) {
    ClassPage classPage = new ClassPage();
    classPage.setListName(paramString1);
    classPage.setClassTitle(paramString2);
    this._class_page.push(classPage);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\PageContainer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */