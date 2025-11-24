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

class BoardLinkPage : BoardMainPage() {
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD_LINK

    override val pageLayout: Int
        get() = R.layout.board_link_page

    override val listType: Int
        get() = BoardPageAction.Companion.LINK_TITLE

    @Synchronized
    override fun onPageRefresh() {
        boardManager = "主題串列"
        super.onPageRefresh()
    }

    override fun onMenuButtonClicked(): Boolean {
        showSelectArticleDialog()
        return true
    }

    // 長按串接到的item
    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        val item = this@BoardLinkPage.getItem(index) as BoardPageItem?

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
                        bookmark.board = this@BoardLinkPage.listName
                        bookmark.keyword = item.title
                        bookmark.title = bookmark.generateTitle()
                        val store = TempSettings.bookmarkStore
                        if (store != null) {
                            store.getBookmarkList(this@BoardLinkPage.listName).addBookmark(bookmark)
                            store.store()
                        }
                    }
                }.scheduleDismissOnPageDisappear(this).show()
        }
        return true
    }

    override fun getListIdFromListName(aName: String?): String? {
        return "$aName[Board][TitleLinked]"
    }

    override fun onPostButtonClicked() {
        if (this@BoardLinkPage.getCount() > 0) {
            val item = this@BoardLinkPage.getItem(0) as BoardPageItem?

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
                            bookmark.board = this@BoardLinkPage.listName
                            bookmark.keyword = item.title
                            bookmark.title = bookmark.generateTitle()
                            val store = TempSettings.bookmarkStore
                            if (store != null) {
                                store.getBookmarkList(this@BoardLinkPage.listName)
                                    .addBookmark(bookmark)
                                store.store()
                            }
                        }
                    }.scheduleDismissOnPageDisappear(this).show()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        clear()
        navigationController.popViewController()
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.instance!!.cleanBoardTitleLinkedPage()
        return true
    }
}
