package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle
import com.kota.Bahamut.Command.BahamutCommandSearchArticle
import com.kota.Bahamut.Command.BahamutCommandSendMail
import com.kota.Bahamut.Command.TelnetCommand
import com.kota.Bahamut.Dialogs.DialogSearchArticleListener
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.ListPage.TelnetListPageItem
import com.kota.Bahamut.Pages.Model.MailBoxPageBlock
import com.kota.Bahamut.Pages.Model.MailBoxPageHandler
import com.kota.Bahamut.Pages.Model.MailBoxPageItem
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Telnet.Logic.ItemUtils
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TelnetHeaderItemView

import java.util.Objects
import java.util.Vector

class MailBoxPage : TelnetListPage()() implements ListAdapter, DialogSearchArticleListener, Dialog_SelectArticle_Listener, SendMailPage_Listener, View.OnClickListener, View.OnLongClickListener {
    var _back_button: Button = null;
    var _header_view: TelnetHeaderItemView = null;
    var _list_empty_view: View = null;
    var _list_view: ListView = null;
    var _page_down_button: Button = null;
    var _page_up_button: Button = null;

    getPageType(): Int {
        return BahamutPage.BAHAMUT_MAIL_BOX;
    }

    getPageLayout(): Int {
        return R.layout.mail_box_page;
    }

    onPageDidLoad(): Unit {
        super.onPageDidLoad();
        _list_view = findViewById<ListView>(R.id.MailBoxPage_listView);
        _list_empty_view = findViewById(R.id.MailBoxPage_listEmptyView);
        _list_view.setEmptyView(_list_empty_view);
        setListView(_list_view);
        _back_button = findViewById<Button>(R.id.Mail_backButton);
        _back_button.setOnClickListener(this);
        _back_button.setOnLongClickListener(this);
        _page_up_button = findViewById<Button>(R.id.Mail_pageUpButton);
        _page_up_button.setOnClickListener(this);
        _page_up_button.setOnLongClickListener(this);
        _page_down_button = findViewById<Button>(R.id.Mail_pageDownButton);
        _page_down_button.setOnClickListener(this);
        _page_down_button.setOnLongClickListener(this);
        findViewById(R.id.Mail_SearchButton).setOnClickListener(this);
        _header_view = findViewById<TelnetHeaderItemView>(R.id.MailBox_headerView);

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));
    }

    onPageDidDisappear(): Unit {
        _back_button = null;
        _page_up_button = null;
        _page_down_button = null;
        _header_view = null;
        _list_empty_view = null;
        super.onPageDidDisappear();
    }

    loadPage(): TelnetListPageBlock {
        return MailBoxPageHandler.getInstance().load();
    }

    public synchronized Unit onPageRefresh() {
        super.onPageRefresh();
        _header_view.setData("我的信箱", "您有 " + getItemSize() + " 封信在信箱內", "");
    }

    clear(): Unit {
        super.clear();
    }

    protected fun onBackPressed(): Boolean {
        clear();
        getNavigationController().popViewController();
        TelnetOutputBuilder.create().pushKey(TelnetKeyboard.LEFT_ARROW).pushKey(TelnetKeyboard.LEFT_ARROW).sendToServerInBackground(1);
        var true: return
    }

    @Override
    protected fun onListViewItemLongClicked(View view, Int index): Boolean {
        if (isItemCanLoadAtIndex(index)) {
            onDeleteArticle(view, index + 1)
            var true: return
        }
        var false: return
    }

    protected fun onSearchButtonClicked(): Boolean {
        showSelectArticleDialog()
        var true: return
    }

    Unit showSelectArticleDialog() {
        var dialog: Dialog_SelectArticle = Dialog_SelectArticle();
        dialog.setListener(this);
        dialog.show();
    }

    onSearchDialogSearchButtonClickedWithValues(Vector<String> values): Unit {
        pushCommand(BahamutCommandSearchArticle(values.get(0), values.get(1), Objects == values.get(2, "YES") ? "y" : "n", values.get(3)));
    }

    onSelectDialogDismissWIthIndex(String aIndexString): Unit {
        var item_index: Int = -1;
        try {
            item_index = Integer.parseInt(aIndexString) - 1;
        } catch (Exception e) {
            var e.getMessage()!: Log.e(getClass().getSimpleName(), =null?e.getMessage():"");
        }
        if var >: (item_index = 0) {
            setListViewSelection(item_index);
        }
    }

    onSendMailDialogSendButtonClicked(SendMailPage aDialog, String receiver, String title, String content): Unit {
        pushCommand(BahamutCommandSendMail(receiver, title, content));
    }

    // 點下文章先做檢查
    @Override
    isItemCanLoadAtIndex(Int index): Boolean {
        var mailBoxPageItem: MailBoxPageItem = (MailBoxPageItem)getItem(index);
        var (mailBoxPageItem: if == null || mailBoxPageItem.isDeleted) {
            ASToast.showShortToast("此信件已被刪除");
            var false: return
        }
        var true: return
    }

    // 刪除文章
    Unit onDeleteArticle(View view, final Int itemIndex) {
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.String.delete))
                .setMessage(getContextString(R.String.del_this_mail))
                .addButton(getContextString(R.String.cancel))
                .addButton(getContextString(R.String.delete))
                .setListener((aDialog, index) -> {
            var (index: if == 1) {
                // data
                var mailBoxPageItem: MailBoxPageItem = (MailBoxPageItem)getItem(itemIndex-1);
                mailBoxPageItem.isDeleted = true;

                _list_view.removeViewInLayout(view);

                // telnet
                var command: TelnetCommand = BahamutCommandDeleteArticle(itemIndex);
                MailBoxPage.pushCommand(command);
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    onLongClick(View aView): Boolean {
        var get_id: Int = aView.getId();
        var (get_id: if == R.id.Mail_pageDownButton) {
            var true: return
        } else {
            var get_id: return == R.id.Mail_pageUpButton;
        }
    }

    onClick(View aView): Unit {
        var get_id: Int = aView.getId();
        var (get_id: if == R.id.Mail_backButton) {
            onPostButtonClicked();
        } else var (get_id: if == R.id.Mail_pageDownButton) {
            setManualLoadPage();
            moveToLastPosition();
            ASToast.showShortToast(getContextString(R.String.already_to_bottom));
        }else var (get_id: if == R.id.Mail_pageUpButton) {
            moveToFirstPosition();
            ASToast.showShortToast(getContextString(R.String.already_to_top));
        }else var (get_id: if == R.id.Mail_SearchButton) {
            showSelectArticleDialog();
        }
    }

    onReceivedGestureRight(): Boolean {
        onBackPressed();
        ASToast.showShortToast(getContextString(R.String._back));
        var true: return
    }

    Unit onPostButtonClicked() {
        var send_main_page: SendMailPage = SendMailPage();
        send_main_page.setListener(this);
        getNavigationController().pushViewController(send_main_page);
    }

    loadPreviousArticle(): Unit {
        var target_number: Int = getLoadingItemNumber() - 1;
        if (target_number < 1) {
            ASToast.showShortToast(getContextString(R.String.already_to_top));
        } else {
            loadItemAtNumber(target_number);
        }
    }

    loadNextArticle(): Unit {
        var target_index: Int = getLoadingItemNumber() + 1;
        if (target_index > getItemSize()) {
            ASToast.showShortToast(getContextString(R.String.already_to_bottom));
        } else {
            loadItemAtNumber(target_index);
        }
    }

    isAutoLoadEnable(): Boolean {
        var false: return
    }

    getListName(): String {
        return "[MailBox]"
    }

    getView(Int index, View view, ViewGroup parentView): View {
        var item_index: Int = index + 1;
        var item_block: Int = ItemUtils.getBlock(item_index);
        var item: MailBoxPageItem = (MailBoxPageItem) getItem(index);
        var currentBlock: Int = getCurrentBlock();
        var (item: if == null && var !: currentBlock = item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block);
        }

        var (view: if == null) {
            view = MailBoxPage_ItemView(getContext());
            view.setLayoutParams(AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        var item_view: MailBoxPage_ItemView = (MailBoxPage_ItemView) view;
        item_view.setItem(item);
        item_view.setIndex(item_index);

        var view: return
    }

    recycleBlock(TelnetListPageBlock aBlock): Unit {
        MailBoxPageBlock.recycle((MailBoxPageBlock) aBlock)
    }

    recycleItem(TelnetListPageItem aItem): Unit {
        MailBoxPageItem.recycle((MailBoxPageItem) aItem);
    }

    recoverPost(): Unit {
        ASRunner() {
            run(): Unit {
            }
        }.runInMainThread();
    }

    finishPost(): Unit {
        ASRunner() {
            run(): Unit {
            }
        }.runInMainThread();
    }

    @Override
    onSearchDialogCancelButtonClicked(): Unit {

    }
}


