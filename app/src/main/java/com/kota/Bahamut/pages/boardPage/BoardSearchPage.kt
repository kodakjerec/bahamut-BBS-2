package com.kota.Bahamut.pages.boardPage

import android.view.View
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient

class BoardSearchPage : BoardMainPage() {
    private var _author: String? = null
    private var _gy: String? = null
    private var _keyword: String? = null
    private var _mark: String? = null

    val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD_SEARCH

    val pageLayout: Int
        get() = R.layout.board_search_page

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        val header_view = findViewById(R.id.BoardPage_HeaderView) as BoardHeaderView?
        if (header_view != null) {
            var board_name = listName
            if (board_name == null) {
                board_name = getContextString(R.string.loading)
            }
            header_view.setData(this._board_title, "文章搜尋", board_name)
        }
    }

    override fun onMenuButtonClicked(): Boolean {
        showSelectArticleDialog()
        return true
    }

    override fun onListViewItemLongClicked(itemView: View?, selectedIndex: Int): Boolean {
        val item = this@BoardSearchPage.getItem(selectedIndex) as BoardPageItem?

        if (item != null) {
            ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.insert) + getContextString(R.string.bookmark))
                .setMessage(getContextString(R.string.insert_this_bookmark) + "\n\"" + item.Title + "\"")
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.insert))
                .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                    if (index == 1) {
                        val bookmark = Bookmark()
                        println("add bookmark:" + bookmark.title)
                        bookmark.board = this@BoardSearchPage.listName
                        bookmark.keyword = item.Title
                        bookmark.title = bookmark.generateTitle()
                        val store = TempSettings.bookmarkStore
                        if (store != null) {
                            store.getBookmarkList(this@BoardSearchPage.listName)
                                .addBookmark(bookmark)
                            store.store()
                        }
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        }
        return true
    }

    val listType: Int
        get() = BoardPageAction.Companion.SEARCH

    override fun getListIdFromListName(aName: String?): String? {
        return aName + "[Board][Search]"
    }

    override fun onPostButtonClicked() {
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string.insert) + getContextString(R.string.bookmark))
            .setMessage(getContextString(R.string.insert_this_bookmark_search))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.insert))
            .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    val bookmark = Bookmark()
                    println("add bookmark:" + bookmark.title)
                    bookmark.board = this@BoardSearchPage.listName
                    bookmark.keyword = this@BoardSearchPage._keyword
                    bookmark.author = this@BoardSearchPage._author
                    bookmark.mark = this@BoardSearchPage._mark
                    bookmark.gy = this@BoardSearchPage._gy
                    bookmark.title = bookmark.generateTitle()
                    val store = TempSettings.bookmarkStore
                    if (store != null) {
                        store.getBookmarkList(this@BoardSearchPage.listName).addBookmark(bookmark)
                        store.store()
                    }
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    fun setKeyword(keyword: String?) {
        this._keyword = keyword
    }

    fun setAuthor(author: String?) {
        this._author = author
    }

    fun setMark(mark: String?) {
        this._mark = mark
    }

    fun setGy(gy: String?) {
        this._gy = gy
    }

    override fun onBackPressed(): Boolean {
        clear()
        navigationController!!.popViewController()
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.getInstance().cleanBoardSearchPage()
        return true
    }

    val isAutoLoadEnable: Boolean
        get() = false
}
