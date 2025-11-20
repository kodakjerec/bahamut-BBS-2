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
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandDeleteArticle
import com.kota.Bahamut.command.BahamutCommandSearchArticle
import com.kota.Bahamut.command.BahamutCommandSendMail
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.dialogs.DialogSelectArticle
import com.kota.Bahamut.dialogs.DialogSelectArticleListener
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.model.MailBoxPageBlock
import com.kota.Bahamut.pages.model.MailBoxPageHandler
import com.kota.Bahamut.pages.model.MailBoxPageItem
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetHeaderItemView
import java.util.Vector

class MailBoxPage : TelnetListPage(), ListAdapter, DialogSearchArticleListener,
    DialogSelectArticleListener, SendMailPageListener, View.OnClickListener,
    OnLongClickListener {
    var backButton: Button? = null
    var headerItemView: TelnetHeaderItemView? = null
    var listEmptyView: View? = null
    var myListView: ListView? = null
    var pageDownButton: Button? = null
    var pageUpButton: Button? = null

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIL_BOX

    override val pageLayout: Int
        get() = R.layout.mail_box_page

    override fun onPageDidLoad() {
        super.onPageDidLoad()
        myListView = findViewById(R.id.MailBoxPage_listView) as ListView?
        listEmptyView = findViewById(R.id.MailBoxPage_listEmptyView)
        myListView?.emptyView = listEmptyView
        listView = myListView
        backButton = findViewById(R.id.Mail_backButton) as Button?
        backButton?.setOnClickListener(this)
        backButton?.setOnLongClickListener(this)
        pageUpButton = findViewById(R.id.Mail_pageUpButton) as Button?
        pageUpButton?.setOnClickListener(this)
        pageUpButton?.setOnLongClickListener(this)
        pageDownButton = findViewById(R.id.Mail_pageDownButton) as Button?
        pageDownButton?.setOnClickListener(this)
        pageDownButton?.setOnLongClickListener(this)
        findViewById(R.id.Mail_SearchButton)?.setOnClickListener(this)
        headerItemView = findViewById(R.id.MailBox_headerView) as TelnetHeaderItemView?

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    override fun onPageDidDisappear() {
        backButton = null
        pageUpButton = null
        pageDownButton = null
        headerItemView = null
        listEmptyView = null
        super.onPageDidDisappear()
    }

    override fun loadPage(): TelnetListPageBlock? {
        return MailBoxPageHandler.instance.load()
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        headerItemView?.setData("我的信箱", "您有 $itemSize 封信在信箱內", "")
    }

    override fun onBackPressed(): Boolean {
        clear()
        navigationController.popViewController()
        create().pushKey(TelnetKeyboard.LEFT_ARROW).pushKey(TelnetKeyboard.LEFT_ARROW)
            .sendToServerInBackground(1)
        return true
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (isItemCanLoadAtIndex(index)) {
            onDeleteArticle(itemView, index + 1)
            return true
        }
        return false
    }

    override fun onSearchButtonClicked(): Boolean {
        showSelectArticleDialog()
        return true
    }

    fun showSelectArticleDialog() {
        val dialog = DialogSelectArticle()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>) {
        pushCommand(
            BahamutCommandSearchArticle(
                vector[0]!!,
                vector[1],
                if (vector[2] == "YES") "y" else "n",
                vector[3]
            )
        )
    }

    override fun onSelectDialogDismissWIthIndex(str: String) {
        var itemIndex = -1
        try {
            itemIndex = str.toInt() - 1
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        if (itemIndex >= 0) {
            setListViewSelection(itemIndex)
        }
    }

    override fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    ) {
        pushCommand(BahamutCommandSendMail(receiver, title, content))
    }

    // 點下文章先做檢查
    override fun isItemCanLoadAtIndex(index: Int): Boolean {
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
            .setListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    // data
                    val mailBoxPageItem = getItem(itemIndex - 1) as MailBoxPageItem?
                    mailBoxPageItem?.isDeleted = true

                    myListView?.removeViewInLayout(view)

                    // telnet
                    val command: TelnetCommand = BahamutCommandDeleteArticle(itemIndex)
                    this@MailBoxPage.pushCommand(command)
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    override fun onLongClick(aView: View): Boolean {
        val getId = aView.id
        return if (getId == R.id.Mail_pageDownButton) {
            true
        } else {
            getId == R.id.Mail_pageUpButton
        }
    }

    override fun onClick(aView: View) {
        val getId = aView.id
        when (getId) {
            R.id.Mail_backButton -> {
                onPostButtonClicked()
            }
            R.id.Mail_pageDownButton -> {
                setManualLoadPage()
                moveToLastPosition()
                showShortToast(getContextString(R.string.already_to_bottom))
            }
            R.id.Mail_pageUpButton -> {
                moveToFirstPosition()
                showShortToast(getContextString(R.string.already_to_top))
            }
            R.id.Mail_SearchButton -> {
                showSelectArticleDialog()
            }
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast(getContextString(R.string._back))
        return true
    }

    fun onPostButtonClicked() {
        val sendMainPage = SendMailPage()
        sendMainPage.setListener(this)
        navigationController.pushViewController(sendMainPage)
    }

    fun loadPreviousArticle() {
        val targetNumber = loadingItemNumber - 1
        if (targetNumber < 1) {
            showShortToast(getContextString(R.string.already_to_top))
        } else {
            loadItemAtNumber(targetNumber)
        }
    }

    fun loadNextArticle() {
        val targetIndex = loadingItemNumber + 1
        if (targetIndex > itemSize) {
            showShortToast(getContextString(R.string.already_to_bottom))
        } else {
            loadItemAtNumber(targetIndex)
        }
    }

    override val isAutoLoadEnable: Boolean
        get() = false

    override var listName: String? = null
        get() = "[MailBox]"

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var view = view
        val itemIndex = i + 1
        val itemBlock = ItemUtils.getBlock(itemIndex)
        val item = getItem(i) as MailBoxPageItem?
        val currentBlock = currentBlock
        if (item == null && currentBlock != itemBlock && !isLoadingBlock(itemIndex)) {
            loadBoardBlock(itemBlock)
        }

        if (view == null) {
            view = MailBoxPageItemView(context)
            view.layoutParams = AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val itemView = view as MailBoxPageItemView
        itemView.setItem(item)
        itemView.setIndex(itemIndex)

        return view
    }

    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock?) {
        MailBoxPageBlock.recycle(telnetListPageBlock as MailBoxPageBlock)
    }

    override fun recycleItem(telnetListPageItem: TelnetListPageItem?) {
        MailBoxPageItem.recycle(telnetListPageItem as MailBoxPageItem)
    }

    fun recoverPost() {
        object : ASRunner() {
            override fun run() {
            }
        }.runInMainThread()
    }

    fun finishPost() {
        object : ASRunner() {
            override fun run() {
            }
        }.runInMainThread()
    }

    override fun onSearchDialogCancelButtonClicked() {
    }
}
