package com.kota.Bahamut.pages

import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.DialogSearchBoard
import com.kota.Bahamut.dialogs.DialogSearchBoardListener
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.model.ClassPageBlock
import com.kota.Bahamut.pages.model.ClassPageBlock.Companion.recycle
import com.kota.Bahamut.pages.model.ClassPageHandler
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.pages.model.ClassPageItem.Companion.recycle
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.logic.SearchBoardHandler
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetHeaderItemView
import java.util.Timer
import java.util.TimerTask

class ClassPage : TelnetListPage(), View.OnClickListener, DialogSearchBoardListener {
    lateinit var mainLayout: RelativeLayout
    private var title: String? = ""

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_CLASS

    override val pageLayout: Int
        get() = R.layout.class_page

    override fun onPageDidLoad() {
        super.onPageDidLoad()

        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        val listView1: ListView = mainLayout.findViewById(R.id.ClassPage_listView)
        listView1.emptyView = mainLayout.findViewById(R.id.ClassPage_listEmptyView)
        bindListView(listView1)
        mainLayout.findViewById<View>(R.id.ClassPage_SearchButton).setOnClickListener(this)
        mainLayout.findViewById<View>(R.id.ClassPage_FirstPageButton).setOnClickListener(this)
        mainLayout.findViewById<View>(R.id.ClassPage_LastestPageButton).setOnClickListener(this)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            // 進入洽特
            // 查詢看板 => Chat => 定位到Chat:Enter
            val timer = Timer()
            val task1: TimerTask = object : TimerTask() {
                override fun run() {
                    TelnetClient.myInstance!!.sendStringToServerInBackground("sChat")
                }
            }
            timer.schedule(task1, 300)
        }
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        var title = this.title
        if (title == null || title.isEmpty()) {
            title = getContextString(R.string.loading)
        }

        val headerView =
            mainLayout.findViewById<TelnetHeaderItemView>(R.id.ClassPage_headerView)
        if (headerView != null) {
            if (!TempSettings.lastVisitBoard.isEmpty()) {
                val finalLastVisitBoard = TempSettings.lastVisitBoard
                val lastVisitBoard =
                    finalLastVisitBoard + getContextString(R.string.toolbar_item_rr)

                val detail2 = mainLayout.findViewById<TextView>(R.id.ClassPage_lastVisit)
                if (detail2!==null) {
                    detail2.visibility = View.VISIBLE
                    detail2.bringToFront()
                    detail2.text = lastVisitBoard
                    detail2.setOnClickListener { v: View? ->
                        TelnetClient.myInstance!!.sendStringToServer("s$finalLastVisitBoard")
                    }
                }
            }
            val detail = "看板列表"
            headerView.setData(title, detail, "")
        }
    }

    override fun onBackPressed(): Boolean {
        clear()
        PageContainer.instance!!.popClassPage()
        navigationController.popViewController()
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return true
    }

    override fun onSearchButtonClicked(): Boolean {
        showSearchBoardDialog()
        return true
    }

    private fun showSearchBoardDialog() {
        val dialog = DialogSearchBoard()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSearchButtonClickedWithKeyword(str: String) {
        SearchBoardHandler.instance.clear()
        showProcessingDialog("搜尋中")
        create().pushString("s$str ").sendToServerInBackground()
    }

    fun setClassTitle(aTitle: String?) {
        this.title = aTitle
    }

    override fun onClick(aView: View) {
        val getId = aView.id
        when (getId) {
            R.id.ClassPage_FirstPageButton -> {
                moveToFirstPosition()
            }
            R.id.ClassPage_LastestPageButton -> {
                moveToLastPosition()
            }
            R.id.ClassPage_SearchButton -> {
                onSearchButtonClicked()
            }
        }
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (listName != null && listName == "Favorite") {
            val itemIndex = index + 1
            ASAlertDialog.createDialog().setMessage("確定要將此看板移出我的最愛?").addButton("取消")
                .addButton("確定")
                .setListener { aDialog: ASAlertDialog?, index1: Int ->
                    if (index1 == 1) {
                        TelnetClient.myInstance!!.sendStringToServerInBackground("$itemIndex\nd")
                        this@ClassPage.loadLastBlock()
                    }
                }.scheduleDismissOnPageDisappear(this).show()
            return true
        } else if ((getItem(index) as ClassPageItem).isDirectory) {
            return false
        } else {
            val itemIndex2 = index + 1
            ASAlertDialog.createDialog().setMessage("確定要將此看板加入我的最愛?").addButton("取消")
                .addButton("確定")
                .setListener { aDialog: ASAlertDialog?, index12: Int ->
                    if (index12 == 1) {
                        TelnetClient.myInstance!!.sendStringToServerInBackground("$itemIndex2\na")
                    }
                }.show()
            return true
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    fun onSearchBoardFinished() {
        println("onSearchBoardFinished")
        dismissProcessingDialog()
        ASListDialog.createDialog().addItems(SearchBoardHandler.instance.boards)
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    val board = SearchBoardHandler.instance.getBoard(index)
                    if (this@ClassPage.listName == "Favorite") {
                        this@ClassPage.showAddBoardToFavoriteDialog(board)
                        return
                    }
                    TelnetClient.myInstance!!.sendStringToServerInBackground("s$board")

                    SearchBoardHandler.instance.clear()
                }

                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return false
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    fun showAddBoardToFavoriteDialog(boardName: String?) {
        ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?")
            .addButton("取消").addButton("加入")
            .setListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    create().pushKey(TelnetKeyboard.LEFT_ARROW).pushString("B\n")
                        .pushKey(TelnetKeyboard.HOME).pushString("/$boardName\na ")
                        .pushKey(TelnetKeyboard.LEFT_ARROW).pushString("F\ns$boardName\n")
                        .sendToServerInBackground()
                    return@setListener
                }
                TelnetClient.myInstance!!.sendStringToServerInBackground("s$boardName")
                SearchBoardHandler.instance.clear()
            }.scheduleDismissOnPageDisappear(this).show()
    }

    override fun loadPage(): TelnetListPageBlock? {
        return ClassPageHandler.instance.load()
    }

    override val isAutoLoadEnable: Boolean
        get() = false

    override fun getListIdFromListName(aName: String?): String? {
        return "$aName[Class]"
    }

    override fun loadItemAtIndex(index: Int) {
        val item = getItem(index) as ClassPageItem

        if (item.isDirectory) {
            PageContainer.instance!!.pushClassPage(item.name, item.title)
            navigationController.pushViewController(PageContainer.instance!!.classPage)
        } else {
            val page = PageContainer.instance!!.boardPage
            page.prepareInitial()
            navigationController.pushViewController(page)
        }
        super.loadItemAtIndex(index)
    }

    /** 填入看板  */
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var itemView = view
        val itemIndex = i + 1
        val itemBlock = ItemUtils.getBlock(itemIndex)
        val item = getItem(i) as ClassPageItem?
        if (item == null && currentBlock != itemBlock && !isLoadingBlock(itemIndex)) {
            loadBoardBlock(itemBlock)
        }
        if (itemView == null) {
            itemView = ClassPageItemView(context)
            itemView.layoutParams = AbsListView.LayoutParams(-1, -2)
        }
        (itemView as ClassPageItemView).setItem(item)
        return itemView
    }

    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {
        recycle(telnetListPageBlock as ClassPageBlock)
    }

    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {
        recycle(telnetListPageItem as ClassPageItem)
    }
}
