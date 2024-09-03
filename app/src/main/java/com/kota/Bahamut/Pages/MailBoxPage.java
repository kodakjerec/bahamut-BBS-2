package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kota.Bahamut.Command.BahamutCommandSearchArticle;
import com.kota.Bahamut.Command.BahamutCommandSendMail;
import com.kota.Bahamut.Command.TelnetCommand;
import com.kota.Bahamut.Dialogs.DialogSearchArticleListener;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Bahamut.ListPage.TelnetListPageItem;
import com.kota.Bahamut.Pages.Model.MailBoxPageBlock;
import com.kota.Bahamut.Pages.Model.MailBoxPageHandler;
import com.kota.Bahamut.Pages.Model.MailBoxPageItem;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.TelnetUI.TelnetHeaderItemView;

import java.util.Objects;
import java.util.Vector;

public class MailBoxPage extends TelnetListPage implements ListAdapter, DialogSearchArticleListener, Dialog_SelectArticle_Listener, SendMailPage_Listener, View.OnClickListener, View.OnLongClickListener {
    Button _back_button = null;
    TelnetHeaderItemView _header_view = null;
    View _list_empty_view = null;
    ListView _list_view = null;
    Button _page_down_button = null;
    Button _page_up_button = null;

    public int getPageType() {
        return BahamutPage.BAHAMUT_MAIL_BOX;
    }

    public int getPageLayout() {
        return R.layout.mail_box_page;
    }

    public void onPageDidLoad() {
        super.onPageDidLoad();
        _list_view = (ListView) findViewById(R.id.MailBoxPage_listView);
        _list_empty_view = findViewById(R.id.MailBoxPage_listEmptyView);
        _list_view.setEmptyView(_list_empty_view);
        setListView(_list_view);
        _back_button = (Button) findViewById(R.id.Mail_backButton);
        _back_button.setOnClickListener(this);
        _back_button.setOnLongClickListener(this);
        _page_up_button = (Button) findViewById(R.id.Mail_pageUpButton);
        _page_up_button.setOnClickListener(this);
        _page_up_button.setOnLongClickListener(this);
        _page_down_button = (Button) findViewById(R.id.Mail_pageDownButton);
        _page_down_button.setOnClickListener(this);
        _page_down_button.setOnLongClickListener(this);
        findViewById(R.id.Mail_SearchButton).setOnClickListener(this);
        _header_view = (TelnetHeaderItemView) findViewById(R.id.MailBox_headerView);

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));
    }

    public void onPageDidDisappear() {
        _back_button = null;
        _page_up_button = null;
        _page_down_button = null;
        _header_view = null;
        _list_empty_view = null;
        super.onPageDidDisappear();
    }

    public TelnetListPageBlock loadPage() {
        return MailBoxPageHandler.getInstance().load();
    }

    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        _header_view.setData("我的信箱", "您有 " + getItemSize() + " 封信在信箱內", "");
    }

    public void clear() {
        super.clear();
    }

    protected boolean onBackPressed() {
        clear();
        getNavigationController().popViewController();
        TelnetOutputBuilder.create().pushKey(TelnetKeyboard.LEFT_ARROW).pushKey(TelnetKeyboard.LEFT_ARROW).sendToServerInBackground(1);
        return true;
    }

    @Override
    protected boolean onListViewItemLongClicked(View view, int index) {
        if (isItemCanLoadAtIndex(index)) {
            onDeleteArticle(view, index + 1);
            return true;
        }
        return false;
    }

    protected boolean onSearchButtonClicked() {
        showSelectArticleDialog();
        return true;
    }

    void showSelectArticleDialog() {
        Dialog_SelectArticle dialog = new Dialog_SelectArticle();
        dialog.setListener(this);
        dialog.show();
    }

    public void onSearchDialogSearchButtonClickedWithValues(Vector<String> values) {
        pushCommand(new BahamutCommandSearchArticle(values.get(0), values.get(1), Objects.equals(values.get(2), "YES") ? "y" : "n", values.get(3)));
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

    // 點下文章先做檢查
    @Override
    public boolean isItemCanLoadAtIndex(int index) {
        MailBoxPageItem mailBoxPageItem = (MailBoxPageItem)getItem(index);
        if (mailBoxPageItem == null || mailBoxPageItem.isDeleted) {
            ASToast.showShortToast("此信件已被刪除");
            return false;
        }
        return  true;
    }

    // 刪除文章
    void onDeleteArticle(View view, final int itemIndex) {
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.delete))
                .setMessage(getContextString(R.string.del_this_mail))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.delete))
                .setListener((aDialog, index) -> {
            if (index == 1) {
                // data
                MailBoxPageItem mailBoxPageItem = (MailBoxPageItem)getItem(itemIndex-1);
                mailBoxPageItem.isDeleted = true;

                _list_view.removeViewInLayout(view);

                // telnet
                TelnetCommand command = new BahamutCommandDeleteArticle(itemIndex);
                MailBoxPage.this.pushCommand(command);
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    public boolean onLongClick(View aView) {
        int get_id = aView.getId();
        if (get_id == R.id.Mail_pageDownButton) {
            return true;
        } else {
            return get_id == R.id.Mail_pageUpButton;
        }
    }

    public void onClick(View aView) {
        int get_id = aView.getId();
        if (get_id == R.id.Mail_backButton) {
            onPostButtonClicked();
        } else if (get_id == R.id.Mail_pageDownButton) {
            setManualLoadPage();
            moveToLastPosition();
            ASToast.showShortToast(getContextString(R.string.already_to_bottom));
        }else if (get_id == R.id.Mail_pageUpButton) {
            moveToFirstPosition();
            ASToast.showShortToast(getContextString(R.string.already_to_top));
        }else if (get_id == R.id.Mail_SearchButton) {
            showSelectArticleDialog();
        }
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast(getContextString(R.string._back));
        return true;
    }

    void onPostButtonClicked() {
        SendMailPage send_main_page = new SendMailPage();
        send_main_page.setListener(this);
        getNavigationController().pushViewController(send_main_page);
    }

    public void loadPreviousArticle() {
        int target_number = getLoadingItemNumber() - 1;
        if (target_number < 1) {
            ASToast.showShortToast(getContextString(R.string.already_to_top));
        } else {
            loadItemAtNumber(target_number);
        }
    }

    public void loadNextArticle() {
        int target_index = getLoadingItemNumber() + 1;
        if (target_index > getItemSize()) {
            ASToast.showShortToast(getContextString(R.string.already_to_bottom));
        } else {
            loadItemAtNumber(target_index);
        }
    }

    public boolean isAutoLoadEnable() {
        return false;
    }

    public String getListName() {
        return "[MailBox]";
    }

    public View getView(int index, View view, ViewGroup parentView) {
        int item_index = index + 1;
        int item_block = ItemUtils.getBlock(item_index);
        MailBoxPageItem item = (MailBoxPageItem) getItem(index);
        int currentBlock = getCurrentBlock();
        if (item == null && currentBlock != item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block);
        }

        if (view == null) {
            view = new MailBoxPage_ItemView(getContext());
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        MailBoxPage_ItemView item_view = (MailBoxPage_ItemView) view;
        item_view.setItem(item);
        item_view.setIndex(item_index);

        return view;
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

    public void finishPost() {
        new ASRunner() {
            public void run() {
            }
        }.runInMainThread();
    }

    @Override
    public void onSearchDialogCancelButtonClicked() {

    }
}
