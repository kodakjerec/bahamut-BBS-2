package com.kota.Bahamut.pages.boardPage

import android.view.View
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.EditFromLinkedState
import com.kota.Bahamut.service.EditFromLinkedStep
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.reference.TelnetKeyboard

class BoardSearchPage : BoardMainPage() {
    private var author: String = ""
    private var gy: String = ""
    private var keyword: String = ""
    private var mark: String = ""

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD_SEARCH

    override val pageLayout: Int
        get() = R.layout.board_search_page

    @Synchronized
    override fun onPageRefresh() {
        boardManager = "文章搜尋"
        super.onPageRefresh()
    }

    override fun onMenuButtonClicked(): Boolean {
        showSelectArticleDialog()
        return true
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        val item = this@BoardSearchPage.getItem(index) as BoardPageItem? ?: return true

        val isOwnArticle = item.author == UserSettings.propertiesUsername
        val dialog = ASListDialog.createDialog()
            .setTitle(item.title)
            .addItem(getContextString(R.string.insert) + getContextString(R.string.bookmark))

        if (isOwnArticle) {
            dialog.addItem(getContextString(R.string.edit_article))
        }

        dialog.setListener(object : ASListDialogItemClickListener {
            override fun onListDialogItemClicked(
                paramASListDialog: ASListDialog?,
                clickIndex: Int,
                title: String?
            ) {
                when (clickIndex) {
                    0 -> addBookmark(item)
                    1 -> if (isOwnArticle) startEditFromLinked(item, index)
                }
            }

            override fun onListDialogItemLongClicked(
                paramASListDialog: ASListDialog?,
                clickIndex: Int,
                title: String?
            ): Boolean = true
        }).scheduleDismissOnPageDisappear(this).show()

        return true
    }

    /** 加入書籤 */
    private fun addBookmark(item: BoardPageItem) {
        val bookmark = Bookmark()
        bookmark.board = this@BoardSearchPage.listName
        bookmark.keyword = item.title
        bookmark.title = bookmark.generateTitle()
        val store = TempSettings.bookmarkStore
        if (store != null) {
            store.getBookmarkList(this@BoardSearchPage.listName).addBookmark(bookmark)
            store.store()
        }
    }

    /** 從搜尋頁觸發編輯文章 */
    private fun startEditFromLinked(item: BoardPageItem, index: Int) {
        // 建立目標文章特徵
        val targetArticle = TelnetArticle()
        targetArticle.title = item.title
        targetArticle.author = item.author
        targetArticle.dateTime = item.date
        targetArticle.articleNumber = item.itemNumber
        targetArticle.boardName = this@BoardSearchPage.listName

        // 建立狀態
        val state = EditFromLinkedState(targetArticle)

        // 設定到指定位置
        setListViewSelection(index)

        // 判斷是否為區塊邊界（20的倍數）
        if (state.isBlockBoundary) {
            // 例外流程1: 先往上移
            state.step = EditFromLinkedStep.MOVE_UP_FOR_BOUNDARY
            create().pushKey(TelnetKeyboard.UP_ARROW).sendToServer()
        } else {
            // 正常流程: 直接送 "t"
            state.step = EditFromLinkedStep.SENT_T
            create().pushKey(TelnetKeyboard.SMALL_T).sendToServer()
        }

        TempSettings.editFromLinkedState = state
    }

    override val listType: Int
        get() = BoardPageAction.Companion.SEARCH

    override fun getListIdFromListName(aName: String?): String? {
        return "$aName[Board][Search]"
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

    fun setKeyword(keyword: String) {
        this.keyword = keyword
    }

    fun setAuthor(author: String) {
        this.author = author
    }

    fun setMark(mark: String) {
        this.mark = mark
    }

    fun setGy(gy: String) {
        this.gy = gy
    }

    override fun onBackPressed(): Boolean {
        clear()
        navigationController.popViewController()
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.instance!!.cleanBoardSearchPage()
        return true
    }
}
