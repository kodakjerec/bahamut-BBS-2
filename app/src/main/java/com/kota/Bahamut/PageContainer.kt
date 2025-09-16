package com.kota.Bahamut

import com.kota.Bahamut.pages.articlePage.ArticlePage
import com.kota.Bahamut.pages.bbsUser.UserConfigPage
import com.kota.Bahamut.pages.bbsUser.UserInfoPage
import com.kota.Bahamut.pages.BillingPage
import com.kota.Bahamut.pages.boardPage.BoardLinkPage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.boardPage.BoardSearchPage
import com.kota.Bahamut.pages.ClassPage
import com.kota.Bahamut.pages.essencePage.ArticleEssencePage
import com.kota.Bahamut.pages.essencePage.BoardEssencePage
import com.kota.Bahamut.pages.login.LoginPage
import com.kota.Bahamut.pages.MailBoxPage
import com.kota.Bahamut.pages.MainPage
import com.kota.Bahamut.pages.messages.MessageMain
import com.kota.Bahamut.pages.messages.MessageSub
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.StartPage
import com.kota.Bahamut.pages.theme.ThemeManagerPage
import java.util.Stack

/* loaded from: classes.dex */
class PageContainer private constructor() {
    private var _start_page: StartPage? = null
    private var _login_page: LoginPage? = null
    private var _main_page: MainPage? = null
    private val _class_page = Stack<ClassPage>()
    private var _board_page: BoardMainPage? = null
    private var _board_title_linked_page: BoardLinkPage? = null
    private var _board_search_page: BoardSearchPage? = null
    private var _mail_page: MailBoxPage? = null
    private var _article_page: ArticlePage? = null
    private var _billing_page: BillingPage? = null
    private var _post_article_page: PostArticlePage? = null
    private val _board_essence_page_list = Stack<BoardEssencePage>()
    private var articleEssencePage: ArticleEssencePage? = null
    private var themeManagerPage: ThemeManagerPage? = null
    private var userInfoPage: UserInfoPage? = null
    private var userConfigPage: UserConfigPage? = null
    private var messageMain: MessageMain? = null
    private var messageSub: MessageSub? = null

    val startPage: StartPage
        get() {
            if (this._start_page == null) {
                this._start_page = StartPage()
            }
            return this._start_page!!
        }

    fun cleanStartPage() {
        cleanLoginPage()
        cleanMainPage()
        cleanClassPage()
        cleanBoardPage()
        cleanBoardTitleLinkedPage()
        cleanBoardSearchPage()
        cleanBoardEssencePage()
        cleanArticleEssencePage()
        cleanMainBoxPage()
        cleanArticlePage()
        cleanBillingPage()
        cleanPostArticlePage()
        cleanThemeManagerPage()
        cleanUserInfoPage()
        cleanUserConfigPage()
        cleanMessageMain()
        cleanMessageSub()
    }

    val loginPage: LoginPage
        get() {
            if (this._login_page == null) {
                this._login_page = LoginPage()
            }
            return this._login_page!!
        }

    fun cleanLoginPage() {
        if (this._login_page != null) {
            this._login_page!!.clear()
            this._login_page = null
        }
    }

    val mainPage: MainPage
        get() {
            if (this._main_page == null) {
                this._main_page = MainPage()
            }
            return this._main_page!!
        }

    fun cleanMainPage() {
        if (this._main_page != null) {
            this._main_page!!.clear()
            this._main_page = null
        }
    }

    fun pushClassPage(aClassName: String?, aClassTitle: String?) {
        val class_page = ClassPage()
        class_page.listName = aClassName
        class_page.setClassTitle(aClassTitle)
        this._class_page.push(class_page)
    }

    fun popClassPage() {
        if (this._class_page.size > 0) {
            this._class_page.pop()
        }
    }

    val classPage: ClassPage?
        get() {
            if (this._class_page.size > 0) {
                return this._class_page.lastElement()
            }
            return null
        }

    fun cleanClassPage() {
        for (page in this._class_page) {
            page.clear()
        }
        this._class_page.clear()
    }

    val boardPage: BoardMainPage
        get() {
            if (this._board_page == null) {
                this._board_page = BoardMainPage()
            }
            return this._board_page!!
        }

    fun cleanBoardPage() {
        if (this._board_page != null) {
            this._board_page!!.clear()
            this._board_page = null
        }
    }

    val boardLinkedTitlePage: BoardLinkPage
        get() {
            if (this._board_title_linked_page == null) {
                this._board_title_linked_page = BoardLinkPage()
            }
            return this._board_title_linked_page!!
        }

    fun cleanBoardTitleLinkedPage() {
        if (this._board_title_linked_page != null) {
            this._board_title_linked_page!!.clear()
            this._board_title_linked_page = null
        }
    }

    val boardSearchPage: BoardSearchPage
        get() {
            if (this._board_search_page == null) {
                this._board_search_page = BoardSearchPage()
            }
            return this._board_search_page!!
        }

    fun cleanBoardSearchPage() {
        if (this._board_search_page != null) {
            this._board_search_page!!.clear()
            this._board_search_page = null
        }
    }

    val boardEssencePage: BoardEssencePage?
        get() {
            if (this._board_essence_page_list.size > 0) {
                return this._board_essence_page_list.lastElement()
            }
            return null
        }

    fun cleanBoardEssencePage() {
        for (page in this._board_essence_page_list) {
            page.clear()
        }
        this._board_essence_page_list.clear()
    }

    fun pushBoardEssencePage(aClassName: String?, aClassTitle: String) {
        val boardEssencePage = BoardEssencePage()
        boardEssencePage.clear()
        boardEssencePage.listName = aClassName
        boardEssencePage.setClassTitle(aClassTitle)
        this._board_essence_page_list.push(boardEssencePage)
    }

    fun popBoardEssencePage() {
        if (this._board_essence_page_list.size > 0) {
            this._board_essence_page_list.pop()
        }
    }

    fun getArticleEssencePage(): ArticleEssencePage {
        if (this.articleEssencePage == null) {
            this.articleEssencePage = ArticleEssencePage()
        }
        return this.articleEssencePage!!
    }

    fun cleanArticleEssencePage() {
        if (this.articleEssencePage != null) {
            this.articleEssencePage!!.clear()
            this.articleEssencePage = null
        }
    }

    val mailBoxPage: MailBoxPage
        get() {
            if (this._mail_page == null) {
                this._mail_page = MailBoxPage()
            }
            return this._mail_page!!
        }

    fun cleanMainBoxPage() {
        if (this._mail_page != null) {
            this._mail_page!!.clear()
            this._mail_page = null
        }
    }

    val articlePage: ArticlePage
        get() {
            if (this._article_page == null) {
                this._article_page = ArticlePage()
            }
            return this._article_page!!
        }

    fun cleanArticlePage() {
        if (this._article_page != null) {
            this._article_page!!.clear()
            this._article_page = null
        }
    }

    val billingPage: BillingPage
        get() {
            if (this._billing_page == null) {
                this._billing_page = BillingPage()
            }
            return this._billing_page!!
        }

    fun cleanBillingPage() {
        if (this._billing_page != null) {
            this._billing_page!!.clear()
            this._billing_page = null
        }
    }

    val postArticlePage: PostArticlePage
        get() {
            if (this._post_article_page == null) {
                this._post_article_page = PostArticlePage()
            }
            return this._post_article_page!!
        }

    fun cleanPostArticlePage() {
        if (this._post_article_page != null) {
            this._post_article_page!!.clear()
            this._post_article_page = null
        }
    }

    fun getThemeManagerPage(): ThemeManagerPage {
        if (this.themeManagerPage == null) {
            this.themeManagerPage = ThemeManagerPage()
        }
        return this.themeManagerPage!!
    }

    fun cleanThemeManagerPage() {
        if (this.themeManagerPage != null) {
            this.themeManagerPage!!.clear()
            this.themeManagerPage = null
        }
    }

    fun getUserInfoPage(): UserInfoPage {
        if (this.userInfoPage == null) {
            this.userInfoPage = UserInfoPage()
        }
        return this.userInfoPage!!
    }

    fun cleanUserInfoPage() {
        if (this.userInfoPage != null) {
            this.userInfoPage!!.clear()
            this.userInfoPage = null
        }
    }

    fun getUserConfigPage(): UserConfigPage {
        if (this.userConfigPage == null) {
            this.userConfigPage = UserConfigPage()
        }
        return this.userConfigPage!!
    }

    fun cleanUserConfigPage() {
        if (this.userConfigPage != null) {
            this.userConfigPage!!.clear()
            this.userConfigPage = null
        }
    }

    fun getMessageMain(): MessageMain {
        if (this.messageMain == null) {
            this.messageMain = MessageMain()
        }
        return this.messageMain!!
    }

    fun cleanMessageMain() {
        if (this.messageMain != null) {
            this.messageMain!!.clear()
            this.messageMain = null
        }
    }

    fun getMessageSub(): MessageSub {
        if (this.messageSub == null) {
            this.messageSub = MessageSub()
        }
        return this.messageSub!!
    }

    fun cleanMessageSub() {
        if (this.messageSub != null) {
            this.messageSub!!.clear()
            this.messageSub = null
        }
    }

    companion object {
        var instance: PageContainer? = null
            private set

        fun constructInstance() {
            instance = PageContainer()
        }
    }
}
