package com.kota.Bahamut.pages

import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.dialogs.DialogSearchBoard
import com.kota.Bahamut.dialogs.DialogSearchBoardListener
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.model.ClassPageBlock
import com.kota.Bahamut.pages.model.ClassPageBlock.Companion.recycle
import com.kota.Bahamut.pages.model.ClassPageHandler
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.pages.model.ClassPageItem.Companion.recycle
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.logic.SearchBoardHandler
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnetUI.TelnetHeaderItemView
import java.util.Timer
import java.util.TimerTask

class ClassPage : TelnetListPage(), View.OnClickListener, DialogSearchBoardListener {
    var mainLayout: RelativeLayout? = null
    private var _title: String? = ""

    val pageType: Int
        get() = BahamutPage.BAHAMUT_CLASS

    val pageLayout: Int
        get() = R.layout.class_page

    override fun onPageDidLoad() {
        super.onPageDidLoad()

        mainLayout = findViewById(R.id.content_view) as RelativeLayout?

        val list_view = mainLayout!!.findViewById<ListView>(R.id.ClassPage_listView)
        list_view.setEmptyView(mainLayout!!.findViewById<View>(R.id.ClassPage_listEmptyView))
        listView = list_view
        mainLayout!!.findViewById<View>(R.id.ClassPage_SearchButton).setOnClickListener(this)
        mainLayout!!.findViewById<View>(R.id.ClassPage_FirstPageButton).setOnClickListener(this)
        mainLayout!!.findViewById<View>(R.id.ClassPage_LastestPageButton).setOnClickListener(this)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            // 進入洽特
            // 查詢看板 => Chat => 定位到Chat:Enter
            val timer = Timer()
            val task1: TimerTask = object : TimerTask() {
                override fun run() {
                    TelnetClient.client!!.sendStringToServerInBackground("sChat")
                }
            }
            timer.schedule(task1, 300)
        }
    }

    override fun onPageDidDisappear() {
        super.onPageDidDisappear()
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        var title = this._title
        if (title == null || title.length == 0) {
            title = getContextString(R.string.loading)
        }

        val header_view =
            mainLayout!!.findViewById<TelnetHeaderItemView?>(R.id.ClassPage_headerView)
        if (header_view != null) {
            if (!TempSettings.lastVisitBoard.isEmpty()) {
                val finalLastVisitBoard = TempSettings.lastVisitBoard
                val lastVisitBoard =
                    finalLastVisitBoard + getContextString(R.string.toolbar_item_rr)

                val _detail_2 = mainLayout!!.findViewById<TextView>(R.id.ClassPage_lastVisit)
                _detail_2.setVisibility(View.VISIBLE)
                _detail_2.bringToFront()
                _detail_2.setText(lastVisitBoard)
                _detail_2.setOnClickListener(View.OnClickListener { v: View? ->
                    TelnetClient.client!!.sendStringToServer("s" + finalLastVisitBoard)
                })
            }
            val _detail = "看板列表"
            header_view.setData(title, _detail, "")
        }
    }

    protected override fun onBackPressed(): Boolean {
        clear()
        PageContainer.getInstance().popClassPage()
        navigationController!!.popViewController()
        TelnetClient.client!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return true
    }

    protected override fun onSearchButtonClicked(): Boolean {
        showSearchBoardDialog()
        return true
    }

    private fun showSearchBoardDialog() {
        val dialog = DialogSearchBoard()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSearchButtonClickedWithKeyword(keyword: String?) {
        SearchBoardHandler.instance.clear()
        showProcessingDialog("搜尋中")
        create().pushString("s" + keyword + " ").sendToServerInBackground()
    }

    fun setClassTitle(aTitle: String?) {
        this._title = aTitle
    }

    override fun onClick(aView: View) {
        val get_id = aView.getId()
        if (get_id == R.id.ClassPage_FirstPageButton) {
            moveToFirstPosition()
        } else if (get_id == R.id.ClassPage_LastestPageButton) {
            moveToLastPosition()
        } else if (get_id == R.id.ClassPage_SearchButton) {
            onSearchButtonClicked()
        }
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (listName != null && listName == "Favorite") {
            val item_index = index + 1
            ASAlertDialog.createDialog().setMessage("確定要將此看板移出我的最愛?").addButton("取消")
                .addButton("確定")
                .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index1: Int ->
                    if (index1 == 1) {
                        TelnetClient.client!!
                            .sendStringToServerInBackground(item_index.toString() + "\nd")
                        this@ClassPage.loadLastBlock()
                    }
                }).scheduleDismissOnPageDisappear(this).show()
            return true
        } else if ((getItem(index) as ClassPageItem).isDirectory) {
            return false
        } else {
            val item_index2 = index + 1
            ASAlertDialog.createDialog().setMessage("確定要將此看板加入我的最愛?").addButton("取消")
                .addButton("確定")
                .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index12: Int ->
                    if (index12 == 1) {
                        TelnetClient.client!!
                            .sendStringToServerInBackground(item_index2.toString() + "\na")
                    }
                }).show()
            return true
        }
    }

    public override fun onReceivedGestureRight(): Boolean {
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
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    val board = SearchBoardHandler.instance.getBoard(index)
                    if (this@ClassPage.listName == "Favorite") {
                        this@ClassPage.showAddBoardToFavoriteDialog(board)
                        return
                    }
                    TelnetClient.client!!.sendStringToServerInBackground("s" + board)

                    SearchBoardHandler.instance.clear()
                }

                override fun onListDialogItemLongClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return false
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    fun showAddBoardToFavoriteDialog(boardName: String?) {
        ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?")
            .addButton("取消").addButton("加入")
            .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    create().pushKey(TelnetKeyboard.LEFT_ARROW).pushString("B\n")
                        .pushKey(TelnetKeyboard.HOME).pushString("/" + boardName + "\na ")
                        .pushKey(TelnetKeyboard.LEFT_ARROW).pushString("F\ns" + boardName + "\n")
                        .sendToServerInBackground()
                    return@setListener
                }
                TelnetClient.client!!.sendStringToServerInBackground("s" + boardName)
                SearchBoardHandler.instance.clear()
            }).scheduleDismissOnPageDisappear(this).show()
    }

    public override fun loadPage(): TelnetListPageBlock? {
        return ClassPageHandler.instance.load()
    }

    val isAutoLoadEnable: Boolean
        get() = false

    public override fun getListIdFromListName(aName: String?): String? {
        return aName + "[Class]"
    }

    public override fun loadItemAtIndex(index: Int) {
        val item = getItem(index) as ClassPageItem?
        if (item == null) return

        if (item.isDirectory) {
            PageContainer.getInstance().pushClassPage(item.Name, item.Title)
            navigationController!!.pushViewController(PageContainer.getInstance().getClassPage())
        } else {
            val page = PageContainer.getInstance().getBoardPage()
            page.prepareInitial()
            navigationController!!.pushViewController(page)
        }
        super.loadItemAtIndex(index)
    }

    /** 填入看板  */
    public override fun getView(index: Int, itemView: View?, parentView: ViewGroup?): View? {
        var itemView = itemView
        val item_index = index + 1
        val item_block = ItemUtils.getBlock(item_index)
        val item = getItem(index) as ClassPageItem?
        if (item == null && currentBlock != item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block)
        }
        if (itemView == null) {
            itemView = ClassPageItemView(context)
            itemView.setLayoutParams(AbsListView.LayoutParams(-1, -2))
        }
        (itemView as ClassPageItemView).setItem(item)
        return itemView
    }

    public override fun recycleBlock(aBlock: TelnetListPageBlock?) {
        recycle(aBlock as ClassPageBlock?)
    }

    public override fun recycleItem(aItem: TelnetListPageItem?) {
        recycle(aItem as ClassPageItem?)
    }
}
