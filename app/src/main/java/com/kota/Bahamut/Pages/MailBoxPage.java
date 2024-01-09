package com.kota.Bahamut.Pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kota.Bahamut.Command.BahamutCommandGoodArticle;
import com.kota.Bahamut.Command.BahamutCommandSearchArticle;
import com.kota.Bahamut.Command.BahamutCommandSendMail;
import com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Bahamut.ListPage.TelnetListPageItem;
import com.kota.Bahamut.Pages.Model.MailBoxPageBlock;
import com.kota.Bahamut.Pages.Model.MailBoxPageHandler;
import com.kota.Bahamut.Pages.Model.MailBoxPageItem;
import com.kota.Bahamut.R;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.TelnetUI.TelnetHeaderItemView;
import java.util.Vector;

public class MailBoxPage extends TelnetListPage implements ListAdapter, Dialog_SearchArticle_Listener, Dialog_SelectArticle_Listener, SendMailPage_Listener, View.OnClickListener, View.OnLongClickListener {
    private Button _back_button = null;
    private TelnetHeaderItemView _header_view = null;
    private View _list_empty_view = null;
    private Button _page_down_button = null;
    private Button _page_up_button = null;

    public int getPageType() {
        return 9;
    }

    public int getPageLayout() {
        return R.layout.mail_box_page;
    }

    public void onPageDidLoad() {
        super.onPageDidLoad();
        ListView list_view = (ListView) findViewById(R.id.MailBoxPage_ListView);
        this._list_empty_view = findViewById(R.id.MailBoxPage_ListEmptyView);
        list_view.setEmptyView(this._list_empty_view);
        setListView(list_view);
        this._back_button = (Button) findViewById(R.id.Mail_BackButton);
        this._back_button.setOnClickListener(this);
        this._back_button.setOnLongClickListener(this);
        this._page_up_button = (Button) findViewById(R.id.Mail_PageUpButton);
        this._page_up_button.setOnClickListener(this);
        this._page_up_button.setOnLongClickListener(this);
        this._page_down_button = (Button) findViewById(R.id.Mail_PageDownButton);
        this._page_down_button.setOnClickListener(this);
        this._page_down_button.setOnLongClickListener(this);
        ((Button) findViewById(R.id.Mail_SearchButton)).setOnClickListener(this);
        this._header_view = (TelnetHeaderItemView) findViewById(R.id.MailBox_HeaderView);
    }

    public void onPageDidDisappear() {
        this._back_button = null;
        this._page_up_button = null;
        this._page_down_button = null;
        this._header_view = null;
        this._list_empty_view = null;
        super.onPageDidDisappear();
    }

    public TelnetListPageBlock loadPage() {
        return MailBoxPageHandler.getInstance().load();
    }

    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        this._header_view.setData("我的信箱", "您有 " + getItemSize() + " 封信在信箱內", "");
    }

    public void clear() {
        super.clear();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        clear();
        getNavigationController().popViewController();
        TelnetOutputBuilder.create().pushKey(256).pushKey(256).sendToServerInBackground(1);
        return true;
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long ID) {
        onDeleteArticle(index + 1);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean onSearchButtonClicked() {
        showSelectArticleDialog();
        return true;
    }

    private void showSelectArticleDialog() {
        Dialog_SelectArticle dialog = new Dialog_SelectArticle();
        dialog.setListener(this);
        dialog.show();
    }

    public void onSearchDialogSearchButtonClickedWithValues(Vector<String> values) {
        pushCommand(new BahamutCommandSearchArticle(values.get(0), values.get(1), values.get(2) == "YES" ? "y" : "n", values.get(3)));
    }

    public void onSelectDialogDismissWIthIndex(String aIndexString) {
        int item_index = -1;
        try {
            item_index = Integer.parseInt(aIndexString) - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (item_index >= 0) {
            setListViewSelection(item_index);
        }
    }

    public void onSendMailDialogSendButtonClicked(SendMailPage aDialog, String receiver, String title, String content) {
        pushCommand(new BahamutCommandSendMail(receiver, title, content));
    }

    private void onDeleteArticle(final int itemIndex) {
        ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此信件?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() {
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                switch (index) {
                    case 1:
                        MailBoxPage.this.pushCommand(new BahamutCommandDeleteArticle(itemIndex));
                        return;
                    default:
                        return;
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    public boolean onLongClick(View aView) {
        switch (aView.getId()) {
            case R.id.Mail_PageDownButton /*2131230887*/:
                return true;
            case R.id.Mail_PageUpButton /*2131230888*/:
                return true;
            default:
                return false;
        }
    }

    public void onClick(View aView) {
        switch (aView.getId()) {
            case R.id.Mail_BackButton /*2131230881*/:
                onPostButtonClicked();
                return;
            case R.id.Mail_PageDownButton /*2131230887*/:
                setManualLoadPage();
                moveToLastPosition();
                return;
            case R.id.Mail_PageUpButton /*2131230888*/:
                moveToFirstPosition();
                return;
            case R.id.Mail_SearchButton /*2131230889*/:
                showSelectArticleDialog();
                return;
            default:
                return;
        }
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }

    public void goodLoadingArticle() {
        goodArticle(getLoadingItemNumber());
    }

    public void goodArticle(final int articleIndex) {
        ASAlertDialog.createDialog().setTitle("推薦").setMessage("是否要推薦此文章?").addButton("取消").addButton("推薦").setListener(new ASAlertDialogListener() {
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                switch (index) {
                    case 1:
                        MailBoxPage.this.pushCommand(new BahamutCommandGoodArticle(articleIndex));
                        return;
                    default:
                        return;
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    private void onPostButtonClicked() {
        SendMailPage send_main_page = new SendMailPage();
        send_main_page.setListener(this);
        getNavigationController().pushViewController(send_main_page);
    }

    public void loadFirstArticle() {
        if (getLoadingItemNumber() == 1) {
            ASToast.showShortToast("已讀至列首");
        } else {
            loadItemAtNumber(1);
        }
    }

    public void loadPreviousArticle() {
        int target_number = getLoadingItemNumber() - 1;
        if (target_number < 1) {
            ASToast.showShortToast("已讀至列首");
        } else {
            loadItemAtNumber(target_number);
        }
    }

    public void loadNextArticle() {
        int target_index = getLoadingItemNumber() + 1;
        if (target_index > getItemSize()) {
            ASToast.showShortToast("已讀至列尾");
        } else {
            loadItemAtNumber(target_index);
        }
    }

    public void loadLastestArticle() {
        if (getLoadingItemNumber() == getItemSize()) {
            ASToast.showShortToast("已讀至列尾");
        } else {
            loadItemAtNumber(getItemSize());
        }
    }

    public boolean isAutoLoadEnable() {
        return false;
    }

    public String getListName() {
        return "[MailBox]";
    }

    public View getView(int index, View itemView, ViewGroup parentView) {
        int item_index = index + 1;
        int item_block = ItemUtils.getBlock(item_index);
        MailBoxPageItem item = (MailBoxPageItem) getItem(index);
        if (item == null && getCurrentBlock() != item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block);
        }
        if (itemView == null) {
            itemView = new MailBoxPage_ItemView(getContext());
            itemView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        }
        MailBoxPage_ItemView item_view = (MailBoxPage_ItemView) itemView;
        item_view.setItem(item);
        item_view.setIndex(index + 1);
        return itemView;
    }

    public void recycleBlock(TelnetListPageBlock aBlock) {
        MailBoxPageBlock.recycle((MailBoxPageBlock) aBlock);
    }

    public void recycleItem(TelnetListPageItem aItem) {
        MailBoxPageItem.recycle((MailBoxPageItem) aItem);
    }

    public void recoverPost() {
        new ASRunner() {
            public void run() {
            }
        }.runInMainThread();
    }
}
