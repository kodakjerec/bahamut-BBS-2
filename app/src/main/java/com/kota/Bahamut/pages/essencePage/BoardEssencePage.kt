package com.kota.Bahamut.pages.essencePage

import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.boardPage.BoardHeaderView
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.pages.model.BoardEssencePageItem
import com.kota.Bahamut.pages.model.BoardEssencePageItemView
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient

class BoardEssencePage : TelnetListPage() {
    private lateinit var mainLayout:RelativeLayout
    private var _title: String = ""

    override fun getPageType(): Int {
        return BahamutPage.BAHAMUT_BOARD_ESSENCE
    }

    override fun getPageLayout(): Int {
        return R.layout.board_essence_page
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        val headerView = findViewById(R.id.BoardPage_HeaderView) as BoardHeaderView
        headerView.setData("精華文章", _title, listName)
    }

    override fun getListType(): Int {
        return BoardPageAction.ESSENCE
    }

    override fun getListIdFromListName(aName: String?): String {
        return "[Board][Essence]"
    }

    // 搜尋, 現在為不做事
    override fun onSearchButtonClicked(): Boolean {
        return true
    }
    override fun onBackPressed(): Boolean {
        clear()
        PageContainer.getInstance().popBoardEssencePage()
        navigationController.popViewController()
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return true
    }

    override fun isAutoLoadEnable(): Boolean {
        return false
    }

    override fun loadPage(): TelnetListPageBlock {
        return BoardEssencePageHandler.instance!!.load()
    }

    override fun recycleBlock(aBlock: TelnetListPageBlock?) {
        BoardPageBlock.recycle(aBlock as BoardPageBlock?)
    }

    // com.kota.Bahamut.ListPage.TelnetListPage, android.widget.Adapter
    override fun getView(index: Int, view: View?, viewGroup: ViewGroup?): View {
        var view1 = view
        val itemIndex = index + 1
        val block = ItemUtils.getBlock(itemIndex)
        val boardEssencePageItem = getItem(index) as BoardEssencePageItem?
        if (boardEssencePageItem == null && currentBlock != block && !isLoadingBlock(itemIndex)) {
            loadBoardBlock(block)
        }
        if (view1 == null) {
            view1 = BoardEssencePageItemView(context)
            view1.setLayoutParams(
                AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        val boardEssencePageItemView = view1 as BoardEssencePageItemView
        boardEssencePageItemView.setItem(boardEssencePageItem)
        boardEssencePageItemView.setNumber(itemIndex)
        return boardEssencePageItemView
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun recycleItem(telnetListPageItem: TelnetListPageItem?) {
        BoardEssencePageItem.recycle(telnetListPageItem as BoardEssencePageItem)
    }

    override fun onPageDidLoad() {
        super.onPageDidLoad()

        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        val aSListView = mainLayout.findViewById<ASListView>(R.id.BoardPageListView)
        aSListView.emptyView = mainLayout.findViewById(R.id.BoardPageListEmptyView)
        listView = aSListView
        aSListView.onItemClickListener

        // 上一篇
        mainLayout.findViewById<View>(R.id.BoardPageFirstPageButton).setOnClickListener{
            moveToFirstPosition()
        }
        // 下一篇
        mainLayout.findViewById<View>(R.id.BoardPageLatestPageButton).setOnClickListener{
            moveToLastPosition()
        }

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout)
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun isItemCanLoadAtIndex(i: Int): Boolean {
        val boardEssencePageItem = getItem(i) as BoardEssencePageItem
        return !boardEssencePageItem.isDeleted && boardEssencePageItem.isBBSClickable
    }

    // 點下文章
    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun loadItemAtIndex(index: Int) {
        val item = getItem(index) as BoardEssencePageItem? ?: return
        if (!item.isBBSClickable) {
            ASToast.showShortToast("找沒有了耶...:(")
            return
        }

        if (item.isDirectory) {
            // 目錄

            // 如果現在最上層是article essence page, 表示是在內文按上一篇/下一篇
            val lastPage = ASNavigationController.getCurrentController().viewControllers.lastElement()
            if (lastPage.pageType == BahamutPage.BAHAMUT_ARTICLE_ESSENCE) {
                ASToast.showShortToast("找沒有了耶...:(")
            } else {
                // 進入目錄
                PageContainer.getInstance().pushBoardEssencePage(listName, _title)
                navigationController.pushViewController(PageContainer.getInstance().boardEssencePage)
                super.loadItemAtIndex(index)
            }
        } else {
            // 文章
            val articleEssencePage = PageContainer.getInstance().articleEssencePage
            articleEssencePage.setBoardEssencePage(this)
            articleEssencePage.clear()
            navigationController.pushViewController(articleEssencePage)
            super.loadItemAtIndex(index)
        }
    }

    //
    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun isItemBlocked(telnetListPageItem: TelnetListPageItem?): Boolean {
        return if (telnetListPageItem != null) {
            val boardEssencePageItem = telnetListPageItem as BoardEssencePageItem
            return !boardEssencePageItem.isBBSClickable
        } else
            false
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    fun setClassTitle(aTitle: String) {
        _title = aTitle
    }

    fun loadPreviousArticle() {
        val targetNumber = loadingItemNumber - 1
        if (targetNumber < 1) {
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.already_to_top))
        } else {
            loadItemAtNumber(targetNumber)
        }
    }

    fun loadNextArticle() {
        val targetIndex = loadingItemNumber + 1
        if (targetIndex > itemSize) {
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.already_to_bottom))
        } else {
            loadItemAtNumber(targetIndex)
        }
    }
}
