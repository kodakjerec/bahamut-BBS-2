package com.kota.Bahamut

import com.kota.Bahamut.pages.BillingPage
import com.kota.Bahamut.pages.ClassPage
import com.kota.Bahamut.pages.mailPage.MailBoxPage
import com.kota.Bahamut.pages.MainPage
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.StartPage
import com.kota.Bahamut.pages.articlePage.ArticlePage
import com.kota.Bahamut.pages.bbsUser.UserConfigPage
import com.kota.Bahamut.pages.bbsUser.UserInfoPage
import com.kota.Bahamut.pages.boardPage.BoardLinkPage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.boardPage.BoardSearchPage
import com.kota.Bahamut.pages.essencePage.ArticleEssencePage
import com.kota.Bahamut.pages.essencePage.BoardEssencePage
import com.kota.Bahamut.pages.login.LoginPage
import com.kota.Bahamut.pages.messages.MessageMain
import com.kota.Bahamut.pages.messages.MessageSub
import com.kota.Bahamut.pages.theme.ThemeManagerPage
import java.util.Stack

class PageContainer private constructor() {
    private var startPageInstance: StartPage? = null
    private var loginPageInstance: LoginPage? = null
    private var mainPageInstance: MainPage? = null
    private val classPageStack = Stack<ClassPage>()
    private var boardMainPageInstance: BoardMainPage? = null
    private var boardTitleLinkPageInstance: BoardLinkPage? = null
    private var boardSearchPageInstance: BoardSearchPage? = null
    private var mailBoxPageInstance: MailBoxPage? = null
    private var articlePageInstance: ArticlePage? = null
    private var billingPageInstance: BillingPage? = null
    private var postArticlePageInstance: PostArticlePage? = null
    private val boardEssencePageStack = Stack<BoardEssencePage>()
    var myArticleEssencePage: ArticleEssencePage? = null
    private var themeManagerPage: ThemeManagerPage? = null
    private var userInfoPage: UserInfoPage? = null
    private var userConfigPage: UserConfigPage? = null
    var myMessageMain: MessageMain? = null
    var myMessageSub: MessageSub? = null

    val startPage: StartPage
        get() {
            if (this.startPageInstance == null) {
                this.startPageInstance = StartPage()
            }
            return this.startPageInstance!!
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
            if (this.loginPageInstance == null) {
                this.loginPageInstance = LoginPage()
            }
            return this.loginPageInstance!!
        }

    fun cleanLoginPage() {
        if (this.loginPageInstance != null) {
            this.loginPageInstance?.clear()
            this.loginPageInstance = null
        }
    }

    val mainPage: MainPage
        get() {
            if (this.mainPageInstance == null) {
                this.mainPageInstance = MainPage()
            }
            return this.mainPageInstance!!
        }

    fun cleanMainPage() {
        if (this.mainPageInstance != null) {
            this.mainPageInstance?.clear()
            this.mainPageInstance = null
        }
    }

    fun pushClassPage(aClassName: String?, aClassTitle: String?) {
        val classPage1 = ClassPage()
        classPage1.listName = aClassName
        classPage1.setClassTitle(aClassTitle)
        this.classPageStack.push(classPage1)
    }

    fun popClassPage() {
        if (this.classPageStack.isNotEmpty()) {
            this.classPageStack.pop()
        }
    }

    val classPage: ClassPage
        get() {
            if (this.classPageStack.isNotEmpty()) {
                return this.classPageStack.lastElement()
            }
            return this.classPageStack.lastElement()
        }

    fun cleanClassPage() {
        for (page in this.classPageStack) {
            page.clear()
        }
        this.classPageStack.clear()
    }

    val boardPage: BoardMainPage
        get() {
            if (this.boardMainPageInstance == null) {
                this.boardMainPageInstance = BoardMainPage()
            }
            return this.boardMainPageInstance!!
        }

    fun cleanBoardPage() {
        if (this.boardMainPageInstance != null) {
            this.boardMainPageInstance?.clear()
            this.boardMainPageInstance = null
        }
    }

    val boardLinkedTitlePage: BoardLinkPage
        get() {
            if (this.boardTitleLinkPageInstance == null) {
                this.boardTitleLinkPageInstance = BoardLinkPage()
            }
            return this.boardTitleLinkPageInstance!!
        }

    fun cleanBoardTitleLinkedPage() {
        if (this.boardTitleLinkPageInstance != null) {
            this.boardTitleLinkPageInstance?.clear()
            this.boardTitleLinkPageInstance = null
        }
    }

    val boardSearchPage: BoardSearchPage
        get() {
            if (this.boardSearchPageInstance == null) {
                this.boardSearchPageInstance = BoardSearchPage()
            }
            return this.boardSearchPageInstance!!
        }

    fun cleanBoardSearchPage() {
        if (this.boardSearchPageInstance != null) {
            this.boardSearchPageInstance?.clear()
            this.boardSearchPageInstance = null
        }
    }

    val boardEssencePage: BoardEssencePage
        get() {
            if (this.boardEssencePageStack.isNotEmpty()) {
                return this.boardEssencePageStack.lastElement()
            }
            return this.boardEssencePageStack.lastElement()
        }

    fun cleanBoardEssencePage() {
        for (page in this.boardEssencePageStack) {
            page.clear()
        }
        this.boardEssencePageStack.clear()
    }

    fun pushBoardEssencePage(aClassName: String?, aClassTitle: String) {
        val boardEssencePage = BoardEssencePage()
        boardEssencePage.clear()
        boardEssencePage.listName = aClassName
        boardEssencePage.setClassTitle(aClassTitle)
        this.boardEssencePageStack.push(boardEssencePage)
    }

    fun popBoardEssencePage() {
        if (this.boardEssencePageStack.isNotEmpty()) {
            this.boardEssencePageStack.pop()
        }
    }

    fun getArticleEssencePage(): ArticleEssencePage {
        if (this.myArticleEssencePage == null) {
            this.myArticleEssencePage = ArticleEssencePage()
        }
        return this.myArticleEssencePage!!
    }

    fun cleanArticleEssencePage() {
        if (this.myArticleEssencePage != null) {
            this.myArticleEssencePage?.clear()
            this.myArticleEssencePage = null
        }
    }

    val mailBoxPage: MailBoxPage
        get() {
            if (this.mailBoxPageInstance == null) {
                this.mailBoxPageInstance = MailBoxPage()
            }
            return this.mailBoxPageInstance!!
        }

    fun cleanMainBoxPage() {
        if (this.mailBoxPageInstance != null) {
            this.mailBoxPageInstance?.clear()
            this.mailBoxPageInstance = null
        }
    }

    val articlePage: ArticlePage
        get() {
            if (this.articlePageInstance == null) {
                this.articlePageInstance = ArticlePage()
            }
            return this.articlePageInstance!!
        }

    fun cleanArticlePage() {
        if (this.articlePageInstance != null) {
            this.articlePageInstance?.clear()
            this.articlePageInstance = null
        }
    }

    val billingPage: BillingPage
        get() {
            if (this.billingPageInstance == null) {
                this.billingPageInstance = BillingPage()
            }
            return this.billingPageInstance!!
        }

    fun cleanBillingPage() {
        if (this.billingPageInstance != null) {
            this.billingPageInstance?.clear()
            this.billingPageInstance = null
        }
    }

    val postArticlePage: PostArticlePage
        get() {
            if (this.postArticlePageInstance == null) {
                this.postArticlePageInstance = PostArticlePage()
            }
            return this.postArticlePageInstance!!
        }

    fun cleanPostArticlePage() {
        if (this.postArticlePageInstance != null) {
            this.postArticlePageInstance?.clear()
            this.postArticlePageInstance = null
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
            this.themeManagerPage?.clear()
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
            this.userInfoPage?.clear()
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
            this.userConfigPage?.clear()
            this.userConfigPage = null
        }
    }

    fun getMessageMain(): MessageMain {
        if (this.myMessageMain == null) {
            this.myMessageMain = MessageMain()
        }
        return this.myMessageMain!!
    }

    fun cleanMessageMain() {
        if (this.myMessageMain != null) {
            this.myMessageMain?.clear()
            this.myMessageMain = null
        }
    }

    fun getMessageSub(): MessageSub {
        if (this.myMessageSub == null) {
            this.myMessageSub = MessageSub()
        }
        return this.myMessageSub!!
    }

    fun cleanMessageSub() {
        if (this.myMessageSub != null) {
            this.myMessageSub?.clear()
            this.myMessageSub = null
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
