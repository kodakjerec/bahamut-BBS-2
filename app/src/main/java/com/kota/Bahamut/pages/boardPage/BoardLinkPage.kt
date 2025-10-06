package com.kota.Bahamut.pages.boardPage

import android.view.View
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.telnet.TelnetClient
import com.kota.telnet.reference.TelnetKeyboard
import java.util.Vector

class BoardLinkPage : BoardMainPage() {
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD_LINK

    override val pageLayout: Int
        get() = R.layout.board_link_page

    override val listType: Int
        get() = BoardPageAction.Companion.LINK_TITLE

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        val headerView = findViewById(R.id.BoardPage_HeaderView) as BoardHeaderView?
        if (headerView != null) {
            var boardName = name
            if (boardName == null) {
                boardName = getContextString(R.string.loading)
            }
            headerView.setData(this.boardTitle, "主題串列", boardName)
        }
    }

    override fun onMenuButtonClicked(): Boolean {
        showSelectArticleDialog()
        return true
    }

    // 長按串接到的item
    override fun onListViewItemLongClicked(view: View?, i: Int): Boolean {
        val item = this@BoardLinkPage.getItem(i) as BoardPageItem?

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
                        bookmark.board = this@BoardLinkPage.name
                        bookmark.keyword = item.title
                        bookmark.title = bookmark.generateTitle()
                        val store = TempSettings.bookmarkStore
                        if (store != null) {
                            store.getBookmarkList(this@BoardLinkPage.name).addBookmark(bookmark)
                            store.store()
                        }
                    }
                }.scheduleDismissOnPageDisappear(this).show()
        }
        return true
    }

    override fun getListIdFromListName(str: String?): String? {
        return "$str[Board][TitleLinked]"
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
                            bookmark.board = this@BoardLinkPage.name
                            bookmark.keyword = item.title
                            bookmark.title = bookmark.generateTitle()
                            val store = TempSettings.bookmarkStore
                            if (store != null) {
                                store.getBookmarkList(this@BoardLinkPage.name)
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
        TelnetClient.myInstance?.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.instance?.cleanBoardTitleLinkedPage()
        return true
    }

    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>) {
        TODO("Not yet implemented")
    }

    override fun onPostDialogEditButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String?,
        str3: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun onPostDialogSendButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String?,
        str3: String?,
        str4: String?,
        str5: String?,
        boolean6: Boolean?
    ) {
        TODO("Not yet implemented")
    }
}
