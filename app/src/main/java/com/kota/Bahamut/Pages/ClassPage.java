package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.Dialogs.Dialog_SearchBoard;
import com.kota.Bahamut.Dialogs.Dialog_SearchBoard_Listener;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Bahamut.ListPage.TelnetListPageItem;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage;
import com.kota.Bahamut.Pages.Model.ClassPageBlock;
import com.kota.Bahamut.Pages.Model.ClassPageHandler;
import com.kota.Bahamut.Pages.Model.ClassPageItem;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.Logic.SearchBoard_Handler;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.TelnetUI.TelnetHeaderItemView;

import java.util.Timer;
import java.util.TimerTask;

public class ClassPage extends TelnetListPage implements View.OnClickListener, Dialog_SearchBoard_Listener {
    private String _detail = "看板列表";
    private String _title = "";

    public int getPageType() {
        return BahamutPage.BAHAMUT_CLASS;
    }

    public int getPageLayout() {
        return R.layout.class_page;
    }

    public void onPageDidLoad() {
        super.onPageDidLoad();

        ListView list_view = (ListView) findViewById(R.id.ClassPage_listView);
        list_view.setEmptyView(findViewById(R.id.ClassPage_listEmptyView));
        setListView(list_view);
        findViewById(R.id.ClassPage_SearchButton).setOnClickListener(this);
        findViewById(R.id.ClassPage_FirstPageButton).setOnClickListener(this);
        findViewById(R.id.ClassPage_LastestPageButton).setOnClickListener(this);

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat()) {
            // 進入洽特
            // 查詢看板 => Chat => 定位到Chat:Enter
            Timer timer = new Timer();
            TimerTask task1 = new TimerTask() {
                @Override
                public void run() {
                TelnetClient.getClient().sendStringToServerInBackground("sChat");
                }
            };
            timer.schedule(task1, 0);
        }
    }

    @Override
    public void onPageDidDisappear() {
        super.onPageDidDisappear();
    }

    @Override
    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        String title = this._title;
        if (title == null || title.length() == 0) {
            title = getContextString(R.string.loading);
        }
        String detail = this._detail;
        if (detail == null || detail.length() == 0) {
            detail = getContextString(R.string.loading);
        }
        TelnetHeaderItemView header_view = (TelnetHeaderItemView) findViewById(R.id.ClassPage_headerView);
        if (header_view != null) {
            if  (!TempSettings.getLastVisitBoard().isEmpty()) {
                String finalLastVisitBoard = TempSettings.getLastVisitBoard();
                String lastVisitBoard = finalLastVisitBoard + getContextString(R.string.toolbar_item_rr);

                TextView _detail_2 = (TextView) findViewById(R.id.ClassPage_lastVisit);
                _detail_2.setVisibility(View.VISIBLE);
                _detail_2.bringToFront();
                _detail_2.setText(lastVisitBoard);
                _detail_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TelnetClient.getClient().sendStringToServer("s"+ finalLastVisitBoard);
                    }
                });
            }
            header_view.setData(title, detail, "");
        }
    }

    protected boolean onBackPressed() {
        clear();
        PageContainer.getInstance().popClassPage();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1);
        return true;
    }

    protected boolean onSearchButtonClicked() {
        showSearchBoardDialog();
        return true;
    }

    private void showSearchBoardDialog() {
        Dialog_SearchBoard dialog = new Dialog_SearchBoard();
        dialog.setListener(this);
        dialog.show();
    }

    public void onSearchButtonClickedWithKeyword(String keyword) {
        SearchBoard_Handler.getInstance().clear();
        ASProcessingDialog.showProcessingDialog("搜尋中");
        TelnetOutputBuilder.create().pushString("s" + keyword + " ").sendToServerInBackground();
    }

    public void onMenuItemClicked(int itemIndex) {
        switch (itemIndex) {
            case 0:
                showSearchBoardDialog();
                return;
            case 1:
                TelnetClient.getClient().sendKeyboardInputToServerInBackground(99);
                return;
            default:
        }
    }

    public void setClassTitle(String aTitle) {
        this._title = aTitle;
    }

    public void setDetail(String aDetail) {
        this._detail = aDetail;
    }

    public void onClick(View aView) {
        int get_id = aView.getId();
        if (get_id == R.id.ClassPage_FirstPageButton) {
            moveToFirstPosition();
        } else if (get_id == R.id.ClassPage_LastestPageButton) {
            moveToLastPosition();
        } else if (get_id == R.id.ClassPage_SearchButton){
            onSearchButtonClicked();
        }
    }

    protected boolean onListViewItemLongClicked(View itemView, int index) {
        if (getListName() != null && getListName().equals("Favorite")) {
            final int item_index = index + 1;
            ASAlertDialog.createDialog().setMessage("確定要將此看板移出我的最愛?").addButton("取消").addButton("確定").setListener((aDialog, index1) -> {
                if (index1 == 1) {
                    TelnetClient.getClient().sendStringToServerInBackground(item_index + "\nd");
                    ClassPage.this.loadLastBlock();
                }
            }).scheduleDismissOnPageDisappear(this).show();
            return true;
        } else if (((ClassPageItem) getItem(index)).isDirectory) {
            return false;
        } else {
            final int item_index2 = index + 1;
            ASAlertDialog.createDialog().setMessage("確定要將此看板加入我的最愛?").addButton("取消").addButton("確定").setListener((aDialog, index12) -> {
                if (index12 == 1) {
                    TelnetClient.getClient().sendStringToServerInBackground(item_index2 + "\na");
                }
            }).show();
            return true;
        }
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }

    public void onSearchBoardFinished() {
        System.out.println("onSearchBoardFinished");
        ASProcessingDialog.dismissProcessingDialog();
        ASListDialog.createDialog().addItems(SearchBoard_Handler.getInstance().getBoards()).setListener(new ASListDialogItemClickListener() {
            public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                String board = SearchBoard_Handler.getInstance().getBoard(index);
                if (ClassPage.this.getListName().equals("Favorite")) {
                    ClassPage.this.showAddBoardToFavoriteDialog(board);
                    return;
                }
                TelnetClient.getClient().sendStringToServerInBackground("s" + board);

                SearchBoard_Handler.getInstance().clear();
            }

            public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                return false;
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    public void showAddBoardToFavoriteDialog(final String boardName) {
        ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?").addButton("取消").addButton("加入").setListener((aDialog, index) -> {
            if (index == 1) {
                TelnetOutputBuilder.create().pushKey(TelnetKeyboard.LEFT_ARROW).pushString("B\n").pushKey(TelnetKeyboard.HOME).pushString("/" + boardName + "\na ").pushKey(TelnetKeyboard.LEFT_ARROW).pushString("F\ns" + boardName + "\n").sendToServerInBackground();
                return;
            }
            TelnetClient.getClient().sendStringToServerInBackground("s" + boardName);
            SearchBoard_Handler.getInstance().clear();
        }).scheduleDismissOnPageDisappear(this).show();
    }

    public TelnetListPageBlock loadPage() {
        return ClassPageHandler.getInstance().load();
    }

    public boolean isAutoLoadEnable() {
        return false;
    }

    public String getListIdFromListName(String aName) {
        return aName + "[Class]";
    }

    @Override
    public void loadItemAtIndex(int index) {
        ClassPageItem item = (ClassPageItem) getItem(index);
        if (item == null) return;

        if (item.isDirectory) {
            PageContainer.getInstance().pushClassPage(item.Name, item.Title);
            getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        } else {
            BoardMainPage page = PageContainer.getInstance().getBoardPage();
            page.prepareInitial();
            getNavigationController().pushViewController(page);
        }
        super.loadItemAtIndex(index);
    }

    // 填入看板
    public View getView(int index, View itemView, ViewGroup parentView) {
        int item_index = index + 1;
        int item_block = ItemUtils.getBlock(item_index);
        ClassPageItem item = (ClassPageItem) getItem(index);
        if (item == null && getCurrentBlock() != item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block);
        }
        if (itemView == null) {
            itemView = new ClassPageItemView(getContext());
            itemView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        }
        ((ClassPageItemView) itemView).setItem(item);
        return itemView;
    }

    public void recycleBlock(TelnetListPageBlock aBlock) {
        ClassPageBlock.recycle((ClassPageBlock) aBlock);
    }

    public void recycleItem(TelnetListPageItem aItem) {
        ClassPageItem.recycle((ClassPageItem) aItem);
    }
}
