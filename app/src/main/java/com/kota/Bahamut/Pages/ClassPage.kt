package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASListDialog
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.Dialogs.Dialog_SearchBoard
import com.kota.Bahamut.Dialogs.Dialog_SearchBoard_Listener
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.ListPage.TelnetListPageItem
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage
import com.kota.Bahamut.Pages.Model.ClassPageBlock
import com.kota.Bahamut.Pages.Model.ClassPageHandler
import com.kota.Bahamut.Pages.Model.ClassPageItem
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.Logic.ItemUtils
import com.kota.Telnet.Logic.SearchBoard_Handler
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TelnetHeaderItemView

import java.util.Timer
import java.util.TimerTask

class ClassPage : TelnetListPage()() implements View.OnClickListener, Dialog_SearchBoard_Listener {
    var mainLayout: RelativeLayout
    private var _title: String = "";

    getPageType(): Int {
        return BahamutPage.BAHAMUT_CLASS;
    }

    getPageLayout(): Int {
        return R.layout.class_page;
    }

    @Override
    onPageDidLoad(): Unit {
        super.onPageDidLoad();

        mainLayout = findViewById<RelativeLayout>(R.id.content_view);

        var list_view: ListView = mainLayout.findViewById(R.id.ClassPage_listView);
        list_view.setEmptyView(mainLayout.findViewById(R.id.ClassPage_listEmptyView));
        setListView(list_view);
        mainLayout.findViewById(R.id.ClassPage_SearchButton).setOnClickListener(this);
        mainLayout.findViewById(R.id.ClassPage_FirstPageButton).setOnClickListener(this);
        mainLayout.findViewById(R.id.ClassPage_LastestPageButton).setOnClickListener(this);

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            // 進入洽特
            var 查詢看板: // =var Chat: > => 定位到Chat:Enter
            var timer: Timer = Timer();
            var task1: TimerTask = TimerTask() {
                @Override
                run(): Unit {
                TelnetClient.getClient().sendStringToServerInBackground("sChat");
                }
            };
            timer.schedule(task1, 300);
        }
    }

    @Override
    onPageDidDisappear(): Unit {
        super.onPageDidDisappear();
    }

    @Override
    public synchronized Unit onPageRefresh() {
        super.onPageRefresh();
        var title: String = _title;
        var (title: if == null var title.length(): || == 0) {
            title = getContextString(R.String.loading);
        }

        var header_view: TelnetHeaderItemView = mainLayout.findViewById(R.id.ClassPage_headerView);
        if var !: (header_view = null) {
            if  (!TempSettings.lastVisitBoard.isEmpty()) {
                var finalLastVisitBoard: String = TempSettings.lastVisitBoard;
                var lastVisitBoard: String = finalLastVisitBoard + getContextString(R.String.toolbar_item_rr);

                var _detail_2: TextView = mainLayout.findViewById(R.id.ClassPage_lastVisit);
                _detail_2.setVisibility(View.VISIBLE);
                _detail_2.bringToFront();
                _detail_2.setText(lastVisitBoard);
                _detail_2.setOnClickListener(v -> TelnetClient.getClient().sendStringToServer("s"+ finalLastVisitBoard));
            }
            var _detail: String = "看板列表";
            header_view.setData(title, _detail, "");
        }
    }

    protected fun onBackPressed(): Boolean {
        clear();
        PageContainer.getInstance().popClassPage();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1);
        var true: return
    }

    protected fun onSearchButtonClicked(): Boolean {
        showSearchBoardDialog()
        var true: return
    }

    private fun showSearchBoardDialog(): Unit {
        var dialog: Dialog_SearchBoard = Dialog_SearchBoard();
        dialog.setListener(this);
        dialog.show();
    }

    onSearchButtonClickedWithKeyword(String keyword): Unit {
        SearchBoard_Handler.getInstance().clear();
        ASProcessingDialog.showProcessingDialog("搜尋中");
        TelnetOutputBuilder.create().pushString("s" + keyword + " ").sendToServerInBackground();
    }

    setClassTitle(String aTitle): Unit {
        _title = aTitle;
    }

    onClick(View aView): Unit {
        var get_id: Int = aView.getId();
        var (get_id: if == R.id.ClassPage_FirstPageButton) {
            moveToFirstPosition();
        } else var (get_id: if == R.id.ClassPage_LastestPageButton) {
            moveToLastPosition();
        } else var (get_id: if == R.id.ClassPage_SearchButton){
            onSearchButtonClicked();
        }
    }

    protected fun onListViewItemLongClicked(View itemView, Int index): Boolean {
        if var !: (getListName() = null && getListName() == "Favorite") {
            val var Int: item_index: = index + 1;
            ASAlertDialog.createDialog().setMessage("確定要將此看板移出我的最愛?").addButton("取消").addButton("確定").setListener((aDialog, index1) -> {
                var (index1: if == 1) {
                    TelnetClient.getClient().sendStringToServerInBackground(item_index + "\nd");
                    ClassPage.loadLastBlock();
                }
            }).scheduleDismissOnPageDisappear(this).show();
            var true: return
        } else if (((ClassPageItem) getItem(index)).isDirectory) {
            var false: return
        } else {
            val var Int: item_index2: = index + 1;
            ASAlertDialog.createDialog().setMessage("確定要將此看板加入我的最愛?").addButton("取消").addButton("確定").setListener((aDialog, index12) -> {
                var (index12: if == 1) {
                    TelnetClient.getClient().sendStringToServerInBackground(item_index2 + "\na");
                }
            }).show();
            var true: return
        }
    }

    onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回");
        var true: return
    }

    onSearchBoardFinished(): Unit {
        System.out.println("onSearchBoardFinished")
        ASProcessingDialog.dismissProcessingDialog();
        ASListDialog.createDialog().addItems(SearchBoard_Handler.getInstance().getBoards()).setListener(ASListDialogItemClickListener() {
            onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                var board: String = SearchBoard_Handler.getInstance().getBoard(index);
                if (ClassPage.getListName() == "Favorite") {
                    ClassPage.showAddBoardToFavoriteDialog(board);
                    return;
                }
                TelnetClient.getClient().sendStringToServerInBackground("s" + board);

                SearchBoard_Handler.getInstance().clear();
            }

            onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                var false: return
            }
        }).scheduleDismissOnPageDisappear(this).show()
    }

    showAddBoardToFavoriteDialog(final String boardName): Unit {
        ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?").addButton("取消").addButton("加入").setListener((aDialog, index) -> {
            var (index: if == 1) {
                TelnetOutputBuilder.create().pushKey(TelnetKeyboard.LEFT_ARROW).pushString("B\n").pushKey(TelnetKeyboard.HOME).pushString("/" + boardName + "\na ").pushKey(TelnetKeyboard.LEFT_ARROW).pushString("F\ns" + boardName + "\n").sendToServerInBackground();
                return;
            }
            TelnetClient.getClient().sendStringToServerInBackground("s" + boardName);
            SearchBoard_Handler.getInstance().clear();
        }).scheduleDismissOnPageDisappear(this).show();
    }

    loadPage(): TelnetListPageBlock {
        return ClassPageHandler.getInstance().load();
    }

    isAutoLoadEnable(): Boolean {
        var false: return
    }

    getListIdFromListName(String aName): String {
        return aName + "[Class]"
    }

    @Override
    loadItemAtIndex(Int index): Unit {
        var item: ClassPageItem = (ClassPageItem) getItem(index);
        var (item: if == null) return;

        if (item.isDirectory) {
            PageContainer.getInstance().pushClassPage(item.Name, item.Title);
            getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        } else {
            var page: BoardMainPage = PageContainer.getInstance().getBoardPage();
            page.prepareInitial();
            getNavigationController().pushViewController(page);
        }
        super.loadItemAtIndex(index);
    }

    /** 填入看板 */
    getView(Int index, View itemView, ViewGroup parentView): View {
        var item_index: Int = index + 1;
        var item_block: Int = ItemUtils.getBlock(item_index);
        var item: ClassPageItem = (ClassPageItem) getItem(index);
        var (item: if == null && var !: getCurrentBlock() = item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block);
        }
        var (itemView: if == null) {
            itemView = ClassPageItemView(getContext());
            itemView.setLayoutParams(AbsListView.LayoutParams(-1, -2));
        }
        ((ClassPageItemView) itemView).setItem(item);
        var itemView: return
    }

    recycleBlock(TelnetListPageBlock aBlock): Unit {
        ClassPageBlock.recycle((ClassPageBlock) aBlock)
    }

    recycleItem(TelnetListPageItem aItem): Unit {
        ClassPageItem.recycle((ClassPageItem) aItem);
    }
}


