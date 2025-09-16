package com.kota.Bahamut.pages

import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.command.BahamutCommandDeleteArticle
import com.kota.Bahamut.command.BahamutCommandSearchArticle
import com.kota.Bahamut.command.BahamutCommandSendMail
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.dialogs.Dialog_SelectArticle
import com.kota.Bahamut.dialogs.Dialog_SelectArticle_Listener
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.model.MailBoxPageBlock
import com.kota.Bahamut.pages.model.MailBoxPageBlock.recycle
import com.kota.Bahamut.pages.model.MailBoxPageHandler
import com.kota.Bahamut.pages.model.MailBoxPageItem
import com.kota.Bahamut.pages.model.MailBoxPageItem.Companion.recycle
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnetUI.TelnetHeaderItemView
import java.util.Vector

class MailBoxPage : TelnetListPage(), ListAdapter, DialogSearchArticleListener,
    Dialog_SelectArticle_Listener, SendMailPage_Listener, View.OnClickListener,
    OnLongClickListener {
    var _back_button: Button? = null
    var _header_view: TelnetHeaderItemView? = null
    var _list_empty_view: View? = null
    var _list_view: ListView? = null
    var _page_down_button: Button? = null
    var _page_up_button: Button? = null

    val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIL_BOX

    val pageLayout: Int
        get() = R.layout.mail_box_page

    override fun onPageDidLoad() {
        super.onPageDidLoad()
        _list_view = findViewById(R.id.MailBoxPage_listView) as ListView?
        _list_empty_view = findViewById(R.id.MailBoxPage_listEmptyView)
        _list_view!!.setEmptyView(_list_empty_view)
        listView = _list_view
        _back_button = findViewById(R.id.Mail_backButton) as Button?
        _back_button!!.setOnClickListener(this)
        _back_button!!.setOnLongClickListener(this)
        _page_up_button = findViewById(R.id.Mail_pageUpButton) as Button?
        _page_up_button!!.setOnClickListener(this)
        _page_up_button!!.setOnLongClickListener(this)
        _page_down_button = findViewById(R.id.Mail_pageDownButton) as Button?
        _page_down_button!!.setOnClickListener(this)
        _page_down_button!!.setOnLongClickListener(this)
        findViewById(R.id.Mail_SearchButton)!!.setOnClickListener(this)
        _header_view = findViewById(R.id.MailBox_headerView) as TelnetHeaderItemView?

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    override fun onPageDidDisappear() {
        _back_button = null
        _page_up_button = null
        _page_down_button = null
        _header_view = null
        _list_empty_view = null
        super.onPageDidDisappear()
    }

    public override fun loadPage(): TelnetListPageBlock? {
        return MailBoxPageHandler.instance.load()
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        _header_view!!.setData("我的信箱", "您有 " + itemSize + " 封信在信箱內", "")
    }

    override fun clear() {
        super.clear()
    }

    protected override fun onBackPressed(): Boolean {
        clear()
        navigationController!!.popViewController()
        create().pushKey(TelnetKeyboard.LEFT_ARROW).pushKey(TelnetKeyboard.LEFT_ARROW)
            .sendToServerInBackground(1)
        return true
    }

    override fun onListViewItemLongClicked(view: View?, index: Int): Boolean {
        if (isItemCanLoadAtIndex(index)) {
            onDeleteArticle(view, index + 1)
            return true
        }
        return false
    }

    protected override fun onSearchButtonClicked(): Boolean {
        showSelectArticleDialog()
        return true
    }

    fun showSelectArticleDialog() {
        val dialog = Dialog_SelectArticle()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSearchDialogSearchButtonClickedWithValues(values: Vector<String?>) {
        pushCommand(
            BahamutCommandSearchArticle(
                values.get(0)!!,
                values.get(1),
                if (values.get(2) == "YES") "y" else "n",
                values.get(3)
            )
        )
    }

    override fun onSelectDialogDismissWIthIndex(aIndexString: String) {
        var item_index = -1
        try {
            item_index = aIndexString.toInt() - 1
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
        if (item_index >= 0) {
            setListViewSelection(item_index)
        }
    }

    override fun onSendMailDialogSendButtonClicked(
        aDialog: SendMailPage?,
        receiver: String,
        title: String,
        content: String
    ) {
        pushCommand(BahamutCommandSendMail(receiver, title, content))
    }

    // 點下文章先做檢查
    public override fun isItemCanLoadAtIndex(index: Int): Boolean {
        val mailBoxPageItem = getItem(index) as MailBoxPageItem?
        if (mailBoxPageItem == null || mailBoxPageItem.isDeleted) {
            showShortToast("此信件已被刪除")
            return false
        }
        return true
    }

    // 刪除文章
    fun onDeleteArticle(view: View?, itemIndex: Int) {
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string.delete))
            .setMessage(getContextString(R.string.del_this_mail))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.delete))
            .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    // data
                    val mailBoxPageItem = getItem(itemIndex - 1) as MailBoxPageItem?
                    mailBoxPageItem!!.isDeleted = true

                    _list_view!!.removeViewInLayout(view)

                    // telnet
                    val command: TelnetCommand = BahamutCommandDeleteArticle(itemIndex)
                    this@MailBoxPage.pushCommand(command)
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    override fun onLongClick(aView: View): Boolean {
        val get_id = aView.getId()
        if (get_id == R.id.Mail_pageDownButton) {
            return true
        } else {
            return get_id == R.id.Mail_pageUpButton
        }
    }

    override fun onClick(aView: View) {
        val get_id = aView.getId()
        if (get_id == R.id.Mail_backButton) {
            onPostButtonClicked()
        } else if (get_id == R.id.Mail_pageDownButton) {
            setManualLoadPage()
            moveToLastPosition()
            showShortToast(getContextString(R.string.already_to_bottom))
        } else if (get_id == R.id.Mail_pageUpButton) {
            moveToFirstPosition()
            showShortToast(getContextString(R.string.already_to_top))
        } else if (get_id == R.id.Mail_SearchButton) {
            showSelectArticleDialog()
        }
    }

    public override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast(getContextString(R.string._back))
        return true
    }

    fun onPostButtonClicked() {
        val send_main_page = SendMailPage()
        send_main_page.setListener(this)
        navigationController!!.pushViewController(send_main_page)
    }

    fun loadPreviousArticle() {
        val target_number = loadingItemNumber - 1
        if (target_number < 1) {
            showShortToast(getContextString(R.string.already_to_top))
        } else {
            loadItemAtNumber(target_number)
        }
    }

    fun loadNextArticle() {
        val target_index = loadingItemNumber + 1
        if (target_index > itemSize) {
            showShortToast(getContextString(R.string.already_to_bottom))
        } else {
            loadItemAtNumber(target_index)
        }
    }

    val isAutoLoadEnable: Boolean
        get() = false

    val listName: String?
        get() = "[MailBox]"

    override fun getView(index: Int, view: View?, parentView: ViewGroup?): View? {
        var view = view
        val item_index = index + 1
        val item_block = ItemUtils.getBlock(item_index)
        val item = getItem(index) as MailBoxPageItem?
        val currentBlock = currentBlock
        if (item == null && currentBlock != item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block)
        }

        if (view == null) {
            view = MailBoxPage_ItemView(context)
            view.setLayoutParams(
                AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val item_view = view as MailBoxPage_ItemView
        item_view.setItem(item)
        item_view.setIndex(item_index)

        return view
    }

    public override fun recycleBlock(aBlock: TelnetListPageBlock?) {
        recycle(aBlock as MailBoxPageBlock?)
    }

    public override fun recycleItem(aItem: TelnetListPageItem?) {
        recycle(aItem as MailBoxPageItem?)
    }

    fun recoverPost() {
        object : ASRunner() {
            public override fun run() {
            }
        }.runInMainThread()
    }

    fun finishPost() {
        object : ASRunner() {
            public override fun run() {
            }
        }.runInMainThread()
    }

    override fun onSearchDialogCancelButtonClicked() {
    }
}
