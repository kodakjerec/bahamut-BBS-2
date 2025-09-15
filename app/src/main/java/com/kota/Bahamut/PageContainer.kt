package com.kota.Bahamut

import com.kota.Bahamut.Pages.ArticlePage.ArticlePage
import com.kota.Bahamut.Pages.BBSUser.UserConfigPage
import com.kota.Bahamut.Pages.BillingPage
import com.kota.Bahamut.Pages.BoardPage.BoardLinkPage
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage
import com.kota.Bahamut.Pages.BoardPage.BoardSearchPage
import com.kota.Bahamut.Pages.ClassPage
import com.kota.Bahamut.Pages.EssencePage.ArticleEssencePage
import com.kota.Bahamut.Pages.EssencePage.BoardEssencePage
import com.kota.Bahamut.Pages.Login.LoginPage
import com.kota.Bahamut.Pages.MailBoxPage
import com.kota.Bahamut.Pages.MainPage
import com.kota.Bahamut.Pages.Messages.MessageMain
import com.kota.Bahamut.Pages.Messages.MessageSub
import com.kota.Bahamut.Pages.PostArticlePage
import com.kota.Bahamut.Pages.StartPage
import com.kota.Bahamut.Pages.Theme.ThemeManagerPage
import com.kota.Bahamut.Pages.BBSUser.UserInfoPage
import java.util.Stack

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

    fun getStartPage(): StartPage {
        if (_start_page == null) {
            _start_page = StartPage()
        }
        return _start_page!!
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

    fun getLoginPage(): LoginPage {
        if (_login_page == null) {
            _login_page = LoginPage()
        }
        return _login_page!!
    }

    fun cleanLoginPage() {
        _login_page?.let {
            it.clear()
            _login_page = null
        }
    }

    fun getMainPage(): MainPage {
        if (_main_page == null) {
            _main_page = MainPage()
        }
        return _main_page!!
    }

    fun cleanMainPage() {
        _main_page?.let {
            it.clear()
            _main_page = null
        }
    }

    fun pushClassPage(aClassName: String, aClassTitle: String) {
        val classPage = ClassPage().apply {
            setListName(aClassName)
            setClassTitle(aClassTitle)
        }
        _class_page.push(classPage)
    }

    fun popClassPage() {
        if (_class_page.size > 0) {
            _class_page.pop()
        }
    }

    fun getClassPage(): ClassPage? {
        return if (_class_page.size > 0) {
            _class_page.lastElement()
        } else null
    }

    fun cleanClassPage() {
        for (page in _class_page) {
            page.clear()
        }
        _class_page.clear()
    }

    fun getBoardPage(): BoardMainPage {
        if (_board_page == null) {
            _board_page = BoardMainPage()
        }
        return _board_page!!
    }

    fun cleanBoardPage() {
        _board_page?.let {
            it.clear()
            _board_page = null
        }
    }

    fun getBoardLinkedTitlePage(): BoardLinkPage {
        if (_board_title_linked_page == null) {
            _board_title_linked_page = BoardLinkPage()
        }
        return _board_title_linked_page!!
    }

    fun cleanBoardTitleLinkedPage() {
        _board_title_linked_page?.let {
            it.clear()
            _board_title_linked_page = null
        }
    }

    fun getBoardSearchPage(): BoardSearchPage {
        if (_board_search_page == null) {
            _board_search_page = BoardSearchPage()
        }
        return _board_search_page!!
    }

    fun cleanBoardSearchPage() {
        _board_search_page?.let {
            it.clear()
            _board_search_page = null
        }
    }

    fun getBoardEssencePage(): BoardEssencePage? {
        return if (_board_essence_page_list.size > 0) {
            _board_essence_page_list.lastElement()
        } else null
    }

    fun cleanBoardEssencePage() {
        for (page in _board_essence_page_list) {
            page.clear()
        }
        _board_essence_page_list.clear()
    }

    fun pushBoardEssencePage(aClassName: String, aClassTitle: String) {
        val boardEssencePage = BoardEssencePage().apply {
            clear()
            setListName(aClassName)
            setClassTitle(aClassTitle)
        }
        _board_essence_page_list.push(boardEssencePage)
    }

    fun popBoardEssencePage() {
        if (_board_essence_page_list.size > 0) {
            _board_essence_page_list.pop()
        }
    }

    fun getMailBoxPage(): MailBoxPage {
        if (_mail_page == null) {
            _mail_page = MailBoxPage()
        }
        return _mail_page!!
    }

    fun cleanMainBoxPage() {
        _mail_page?.let {
            it.clear()
            _mail_page = null
        }
    }

    fun getArticlePage(): ArticlePage {
        if (_article_page == null) {
            _article_page = ArticlePage()
        }
        return _article_page!!
    }

    fun cleanArticlePage() {
        _article_page?.let {
            it.clear()
            _article_page = null
        }
    }

    fun getBillingPage(): BillingPage {
        if (_billing_page == null) {
            _billing_page = BillingPage()
        }
        return _billing_page!!
    }

    fun cleanBillingPage() {
        _billing_page?.let {
            it.clear()
            _billing_page = null
        }
    }

    fun getPostArticlePage(): PostArticlePage {
        if (_post_article_page == null) {
            _post_article_page = PostArticlePage()
        }
        return _post_article_page!!
    }

    fun cleanPostArticlePage() {
        _post_article_page?.let {
            it.clear()
            _post_article_page = null
        }
    }

    fun getArticleEssencePage(): ArticleEssencePage {
        if (articleEssencePage == null) {
            articleEssencePage = ArticleEssencePage()
        }
        return articleEssencePage!!
    }

    fun cleanArticleEssencePage() {
        articleEssencePage?.let {
            it.clear()
            articleEssencePage = null
        }
    }

    fun getThemeManagerPage(): ThemeManagerPage {
        if (themeManagerPage == null) {
            themeManagerPage = ThemeManagerPage()
        }
        return themeManagerPage!!
    }

    fun cleanThemeManagerPage() {
        themeManagerPage?.let {
            it.clear()
            themeManagerPage = null
        }
    }

    fun getUserInfoPage(): UserInfoPage {
        if (userInfoPage == null) {
            userInfoPage = UserInfoPage()
        }
        return userInfoPage!!
    }

    fun cleanUserInfoPage() {
        userInfoPage?.let {
            it.clear()
            userInfoPage = null
        }
    }

    fun getUserConfigPage(): UserConfigPage {
        if (userConfigPage == null) {
            userConfigPage = UserConfigPage()
        }
        return userConfigPage!!
    }

    fun cleanUserConfigPage() {
        userConfigPage?.let {
            it.clear()
            userConfigPage = null
        }
    }

    fun getMessageMain(): MessageMain {
        if (messageMain == null) {
            messageMain = MessageMain()
        }
        return messageMain!!
    }

    fun cleanMessageMain() {
        messageMain?.let {
            it.clear()
            messageMain = null
        }
    }

    fun getMessageSub(): MessageSub {
        if (messageSub == null) {
            messageSub = MessageSub()
        }
        return messageSub!!
    }

    fun cleanMessageSub() {
        messageSub?.let {
            it.clear()
            messageSub = null
        }
    }

    companion object {
        private var _instance: PageContainer? = null

        fun getInstance(): PageContainer {
            return _instance!!
        }

        fun constructInstance() {
            _instance = PageContainer()
        }
    }
}
