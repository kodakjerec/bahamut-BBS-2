package com.kota.Bahamut.pages.boardPage

import android.view.View
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.telnet.TelnetClient
import com.kota.telnet.reference.TelnetKeyboard

class BoardSearchPage : BoardMainPage() {
    private var author: String? = null
    private var gy: String? = null
    private var keyword: String? = null
    private var mark: String? = null

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD_SEARCH

    override val pageLayout: Int
        get() = R.layout.board_search_page

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        val headerView = findViewById(R.id.BoardPage_HeaderView) as BoardHeaderView
        headerView.setData(this.boardTitle, "文章搜尋", listName)
    }

    override fun onMenuButtonClicked(): Boolean {
        showSelectArticleDialog()
        return true
    }

    override fun onListViewItemLongClicked(view: View?, i: Int): Boolean {
        val item = this@BoardSearchPage.getItem(i) as BoardPageItem?

        if (item != null) {
            ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.insert) + getContextString(R.string.bookmark))
                .setMessage(getContextString(R.string.insert_this_bookmark) + "\n\"" + item.title + "\"")
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.insert))
                .setListener { aDialog: ASAlertDialog?, index: Int ->
                    if (index == 1) {
                        val bookmark = Bookmark()
                        println("add bookmark:" + bookmark.title)
                        bookmark.board = this@BoardSearchPage.listName
                        bookmark.keyword = item.title
                        bookmark.title = bookmark.generateTitle()
                        val store = TempSettings.bookmarkStore
                        if (store != null) {
                            store.getBookmarkList(this@BoardSearchPage.listName)
                                .addBookmark(bookmark)
                            store.store()
                        }
                    }
                }.scheduleDismissOnPageDisappear(this).show()
        }
        return true
    }

    override val listType: Int
        get() = BoardPageAction.Companion.SEARCH

    override fun getListIdFromListName(str: String?): String? {
        return "$str[Board][Search]"
    }

    override fun onPostButtonClicked() {
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string.insert) + getContextString(R.string.bookmark))
            .setMessage(getContextString(R.string.insert_this_bookmark_search))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.insert))
            .setListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    val bookmark = Bookmark()
                    println("add bookmark:" + bookmark.title)
                    bookmark.board = this@BoardSearchPage.listName
                    bookmark.keyword = this@BoardSearchPage.keyword
                    bookmark.author = this@BoardSearchPage.author
                    bookmark.mark = this@BoardSearchPage.mark
                    bookmark.gy = this@BoardSearchPage.gy
                    bookmark.title = bookmark.generateTitle()
                    val store = TempSettings.bookmarkStore
                    if (store != null) {
                        store.getBookmarkList(this@BoardSearchPage.listName).addBookmark(bookmark)
                        store.store()
                    }
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    fun setKeyword(keyword: String?) {
        this.keyword = keyword
    }

    fun setAuthor(author: String?) {
        this.author = author
    }

    fun setMark(mark: String?) {
        this.mark = mark
    }

    fun setGy(gy: String?) {
        this.gy = gy
    }

    override fun onBackPressed(): Boolean {
        clear()
        navigationController.popViewController()
        TelnetClient.myInstance?.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.instance!!.cleanBoardSearchPage()
        return true
    }

    override val isAutoLoadEnable: Boolean
        get() = false
}
