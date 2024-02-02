package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kota.Bahamut.Command.BahamutCommandEditArticle;
import com.kota.Bahamut.Command.BahamutCommandGoodArticle;
import com.kota.Bahamut.Command.BahamutCommandListArticle;
import com.kota.Bahamut.Command.BahamutCommandPostArticle;
import com.kota.Bahamut.Command.BahamutCommandSearchArticle;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleBottom;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleDown;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleTop;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleUp;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.Dialogs.Dialog_SearchArticle;
import com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener;
import com.kota.Bahamut.ListPage.ListState;
import com.kota.Bahamut.ListPage.ListStateStore;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Bahamut.ListPage.TelnetListPageItem;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Model.BoardPageBlock;
import com.kota.Bahamut.Pages.Model.BoardPageHandler;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.R;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetHeaderItemView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/* loaded from: classes.dex */
public class BoardPage extends TelnetListPage implements Dialog_SearchArticle_Listener, Dialog_SelectArticle_Listener, PostArticlePage_Listener, BoardExtendOptionalPageListener, ASListViewExtentOptionalDelegate {
    UserSettings _settings;
    protected String _board_title = null;
    protected String _board_manager = null;
    private int _last_list_action = 0;
    private boolean _initialed = false;
    private boolean _refresh_header_view = false;
    private final List<Bookmark> _list = new ArrayList<>();
    private final View.OnClickListener mOpenBookmarkListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.2
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.onBookmarkButtonClicked();
        }
    };
    private final View.OnClickListener mPostListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.5
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.onPostButtonClicked();
        }
    };
    private final View.OnClickListener mSearchByKeywordListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.6
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.onSearchButtonClicked();
        }
    };
    private final View.OnClickListener mSearchByNumberListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.7
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.showSelectArticleDialog();
        }
    };
    private final View.OnClickListener mFirstPageClicked = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.8
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.moveToFirstPosition();
        }
    };
    private final View.OnClickListener mLastPageClicked = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.9
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.setManualLoadPage();
            BoardPage.this.moveToLastPosition();
        }
    };
    private final View.OnClickListener mMenuButtonListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.11
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            DrawerLayout drawerLayout = (DrawerLayout) BoardPage.this.findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(GravityCompat.END, true);
                }
            }
        }
    };
    BaseAdapter _bookmark_adapter = new BaseAdapter() { // from class: com.kota.Bahamut.Pages.BoardPage.13
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return BoardPage.this._list.size();
        }

        @Override // android.widget.Adapter
        public Bookmark getItem(int i) {
            return (Bookmark) BoardPage.this._list.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new BoardExtendOptionalPageBookmarkItemView(BoardPage.this.getContext());
            }
            BoardExtendOptionalPageBookmarkItemView boardExtendOptionalPageBookmarkItemView = (BoardExtendOptionalPageBookmarkItemView) view;
            boardExtendOptionalPageBookmarkItemView.setBookmark(getItem(i));
            boardExtendOptionalPageBookmarkItemView.setDividerTopVisible(i == 0);
            return view;
        }
    };

    DrawerLayout.DrawerListener _drawer_listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            Log.i("BahaBBS", "slideOffset:" + slideOffset);
            BoardPage.this.reloadBlockEnableButton();
            BoardPage.this.reloadBookmark();
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };
    AdapterView.OnItemClickListener _bookmark_listener = new AdapterView.OnItemClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.15
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            BoardPage.this.closeDrawer();
            Bookmark bookmark = (Bookmark) BoardPage.this._list.get(i);
            BoardPage.this._last_list_action = 1;
            BoardPage.this.pushCommand(new BahamutCommandSearchArticle(bookmark.getKeyword(), bookmark.getAuthor(), bookmark.getMark(), bookmark.getGy()));
        }
    };
    View.OnClickListener _search_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.16
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.closeDrawer();
            BoardPage.this.showSearchArticleDialog();
        }
    };
    View.OnClickListener _select_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.17
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.closeDrawer();
            BoardPage.this.showSelectArticleDialog();
        }
    };
    View.OnClickListener _enable_block_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.18
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.onChangeBlockStateButtonClicked();
        }
    };
    View.OnClickListener _edit_block_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.19
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.closeDrawer();
            BoardPage.this.onEditBlockListButtonClicked();
        }
    };
    View.OnClickListener _edit_bookmark_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.20
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BoardPage.this.closeDrawer();
            BoardPage.this.onBookmarkButtonClicked();
        }
    };

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public int getPageLayout() {
        return R.layout.board_page;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return 10;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isAutoLoadEnable() {
        return true;
    }

    protected boolean isBookmarkAvailable() {
        return true;
    }

    @Override // com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate
    public boolean onASListViewHandleExtentOptional(ASListView aSListView, int i) {
        return false;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidLoad() {
        super.onPageDidLoad();
        this._settings = new UserSettings(getContext());

        ASListView aSListView = (ASListView) findViewById(R.id.BoardPage_ListView);
        aSListView.extendOptionalDelegate = this;
        aSListView.setEmptyView(findViewById(R.id.BoardPage_ListEmptyView));
        setListView(aSListView);
        findViewById(R.id.BoardPage_PostButton).setOnClickListener(this.mPostListener);
        findViewById(R.id.BoardPage_FirstPageButton).setOnClickListener(this.mFirstPageClicked);
        findViewById(R.id.BoardPage_LastestPageButton).setOnClickListener(this.mLastPageClicked);
        View findViewById = findViewById(R.id.search_article_button);
        if (findViewById != null) {
            findViewById.setOnClickListener(this._search_listener);
        }
        View findViewById2 = findViewById(R.id.select_article_button);
        if (findViewById2 != null) {
            findViewById2.setOnClickListener(this._select_listener);
        }
        View findViewById3 = findViewById(R.id.block_enable_button);
        if (findViewById3 != null) {
            findViewById3.setOnClickListener(this._enable_block_listener);
        }
        View findViewById4 = findViewById(R.id.block_setting_button);
        if (findViewById4 != null) {
            findViewById4.setOnClickListener(this._edit_block_listener);
        }
        View findViewById5 = findViewById(R.id.bookmark_edit_button);
        if (findViewById5 != null) {
            findViewById5.setOnClickListener(this._edit_bookmark_listener);
        }
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(this._drawer_listener);
        }
        ListView listView = (ListView) findViewById(R.id.bookmark_list_view);
        if (listView != null) {
            listView.setAdapter(this._bookmark_adapter);
            listView.setOnItemClickListener(this._bookmark_listener);
        }
        ((TelnetHeaderItemView) findViewById(R.id.BoardPage_HeaderView)).setMenuButton(this.mMenuButtonListener);
        refreshHeaderView();

        // 自動登入洽特
        if (this._settings.isUnderAutoToChat()) {
            // 最後頁
            findViewById(R.id.BoardPage_LastestPageButton).performClick();
            // 任務完成
            // 關閉"正在自動登入"
            this._settings.setIsUnderAutoToChat(false);
        }

        // 解決android 14跳出軟鍵盤
        // 先把 focus 設定到其他目標物, 避免系統在回收過程一個個去 focus
        // keyword: clearFocusInternal
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.content_view);
        if (linearLayout != null)
            linearLayout.requestFocus();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onMenuButtonClicked() {
        this.mMenuButtonListener.onClick(null);
        return true;
    }

    private void refreshHeaderView() {
        String board_title = this._board_title;
        board_title = (board_title == null || board_title.length() == 0) ? "讀取中" : board_title;
        String board_manager = this._board_manager;
        board_manager = (board_manager == null || board_manager.length() == 0) ? "讀取中" : board_manager;
        TelnetHeaderItemView header_view = (TelnetHeaderItemView) findViewById(R.id.BoardPage_HeaderView);
        if (header_view != null) {
            header_view.setData(board_title, getListName(), board_manager);
        }
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public String getListIdFromListName(String str) {
        return str + "[Board]";
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public TelnetListPageBlock loadPage() {
        BoardPageBlock load = BoardPageHandler.getInstance().load();
        if (!this._initialed) {
            clear();
            if (load.Type == 1) {
                pushRefreshCommand(0);
            }
            this._board_manager = load.BoardManager;
            this._board_title = load.BoardTitle;
            setListName(load.BoardName);
            this._refresh_header_view = true;
            this._initialed = true;
        }
        return load;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemCanLoadAtIndex(int i) {
        BoardPageItem boardPageItem = (BoardPageItem) getItem(i);
        if (boardPageItem == null || !boardPageItem.isDeleted) {
            return true;
        }
        ASToast.showShortToast("此文章已被刪除");
        return false;
    }

    public void setBoardTitle(String str) {
        this._board_title = str;
    }

    public void setBoardManager(String str) {
        this._board_manager = str;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        if (this._refresh_header_view) {
            refreshHeaderView();
            this._refresh_header_view = false;
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        }
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(256, 1);
        PageContainer.getInstance().cleanBoardPage();
        return true;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    protected boolean onListViewItemLongClicked(View view, int i) {
        onListArticle(i + 1);
        return true;
    }

    public void onBlockButtonClicked(final String str) {
        ASAlertDialog title = ASAlertDialog.createDialog().setTitle("加入黑名單");
        title.setMessage("是否要將\"" + str + "\"加入黑名單?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.Pages.BoardPage.1
            @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aSAlertDialog, int i) {
                if (i == 1) {
                    BoardPage.this._settings.addBlockName(str);
                    BoardPage.this._settings.notifyDataUpdated();
                    BoardPage.this.reloadListView();
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onSearchButtonClicked() {
        showSearchArticleDialog();
        return true;
    }

    private void showSearchArticleDialog() {
        Dialog_SearchArticle dialog_SearchArticle = new Dialog_SearchArticle();
        dialog_SearchArticle.setListener(this);
        dialog_SearchArticle.show();
    }

    protected void showSelectArticleDialog() {
        Dialog_SelectArticle dialog_SelectArticle = new Dialog_SelectArticle();
        dialog_SelectArticle.setListener(this);
        dialog_SelectArticle.show();
    }

    @Override // com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener
    public void onSearchDialogSearchButtonClickedWithValues(Vector<String> vector) {
        searchArticle(vector.get(0), vector.get(1), Objects.equals(vector.get(2), "YES") ? "y" : "n", vector.get(3));
    }

    // 搜尋文章
    private void searchArticle(String str, String str2, String str3, String str4) {
        this._last_list_action = 1;
        BoardSearchPage board_Search_Page = PageContainer.getInstance().getBoardSearchPage();
        board_Search_Page.clear();
        getNavigationController().pushViewController(board_Search_Page);
        ListState state = ListStateStore.getInstance().getState(board_Search_Page.getListIdFromListName(getListName()));
        if (state != null) {
            state.Top = 0;
            state.Position = 0;
        }
        board_Search_Page.setKeyword(str);
        board_Search_Page.setAuthor(str2);
        board_Search_Page.setMark(str3);
        board_Search_Page.setGy(str4);
        pushCommand(new BahamutCommandSearchArticle(str, str2, str3, str4));
    }

    @Override // com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener
    public void onSelectDialogDismissWIthIndex(String str) {
        int i;
        try {
            i = Integer.parseInt(str) - 1;
        } catch (Exception e) {
            e.printStackTrace();
            i = -1;
        }
        if (i >= 0) {
            setListViewSelection(i);
        }
    }

    public void onBookmarkButtonClicked() {
        getNavigationController().pushViewController(new BoardExtendOptionalPage(getListName(), this));
    }

    void onMenuClicked() {
        ASListDialog.createDialog().addItem(isBookmarkAvailable() ? "開啟書籤" : null).addItem("搜尋文章").addItem("選擇文章").addItem(this._settings.getPropertiesBlockListEnable() ? "停用黑名單" : "啟用黑名單").addItem("編輯黑名單").setListener(new ASListDialogItemClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.3
            @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
            public boolean onListDialogItemLongClicked(ASListDialog aSListDialog, int i, String str) {
                return false;
            }

            @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
            public void onListDialogItemClicked(ASListDialog aSListDialog, int i, String str) {
                switch (i) {
                    case 0:
                        BoardPage.this.onBookmarkButtonClicked();
                        return;
                    case 1:
                        BoardPage.this.showSearchArticleDialog();
                        return;
                    case 2:
                        BoardPage.this.showSelectArticleDialog();
                        return;
                    case 3:
                        BoardPage.this.onChangeBlockStateButtonClicked();
                        return;
                    case 4:
                        BoardPage.this.onEditBlockListButtonClicked();
                        return;
                    default:
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    private void onListArticle(int i) {
        this._last_list_action = 0;
        BoardLinkPage board_Linked_Title_Page = PageContainer.getInstance().getBoardLinkedTitlePage();
        board_Linked_Title_Page.clear();
        getNavigationController().pushViewController(board_Linked_Title_Page);
        ListState state = ListStateStore.getInstance().getState(board_Linked_Title_Page.getListIdFromListName(getListName()));
        if (state != null) {
            state.Top = 0;
            state.Position = 0;
        }
        pushCommand(new BahamutCommandListArticle(i));
    }

    protected void onDeleteArticle(final int i) {
        ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此文章?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.Pages.BoardPage.4
            @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aSAlertDialog, int i2) {
                switch (i2) {
                    case 0:
                    default:
                        return;
                    case 1:
                        BoardPage.this.pushCommand(new BahamutCommandDeleteArticle(i));
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public boolean onReceivedGestureRight() {
            if (this._settings.getPropertiesGetsureOnBoardEnable()) {
                if (isDrawerOpen()) {
                    return false;
                }
                onBackPressed();
                return true;
            }
            return true;
    }

    protected void onPostButtonClicked() {
        PostArticlePage postArticlePage = PageContainer.getInstance().getPostArticlePage();
        postArticlePage.setBoardPage(this);
        postArticlePage.setListener(this);
        getNavigationController().pushViewController(postArticlePage);
    }

    public void goodLoadingArticle() {
        goodArticle(getLoadingItemNumber());
    }

    public void goodArticle(final int i) {
        ASAlertDialog.createDialog()
                .setTitle("推薦")
                .setMessage("是否要推薦此文章?")
                .addButton("取消")
                .addButton("推薦")
                .setListener(new ASAlertDialogListener() {
                    // from class: com.kota.Bahamut.Pages.BoardPage.10
            @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aSAlertDialog, int i2) {
                switch (i2) {
                    case 0:
                    default:
                        return;
                    case 1:
                        BoardPage.this.pushCommand(new BahamutCommandGoodArticle(i));
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    public void loadTheSameTitleTop() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleTop(getLoadingItemNumber()));
    }

    public void loadTheSameTitleBottom() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleBottom(getLoadingItemNumber()));
    }

    public void loadTheSameTitleUp() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleUp(getLoadingItemNumber()));
    }

    public void loadTheSameTitleDown() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleDown(getLoadingItemNumber()));
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemBlocked(TelnetListPageItem telnetListPageItem) {
        if (telnetListPageItem != null) {
            return this._settings.getPropertiesBlockListEnable() && this._settings.isBlockListContains(((BoardPageItem) telnetListPageItem).Author);
        }
        return false;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemBlockEnable() {
        return this._settings.getPropertiesBlockListEnable();
    }

    public int getLastListAction() {
        return this._last_list_action;
    }

    private void onChangeBlockStateButtonClicked() {
        this._settings.setPropertiesBlockListEnable(!this._settings.getPropertiesBlockListEnable());
        reloadBlockEnableButton();
        reloadListView();
    }

    private void onEditBlockListButtonClicked() {
        getNavigationController().pushViewController(new BlockListPage());
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public void loadItemAtIndex(int index) {
        if (isItemCanLoadAtIndex(index)) {
            ArticlePage articlePage = PageContainer.getInstance().getArticlePage();
            articlePage.setBoardPage(this);
            articlePage.clear();
            getNavigationController().pushViewController(articlePage);

            super.loadItemAtIndex(index);
        }
    }

    public void prepareInitial() {
        this._initialed = false;
    }

    @Override // com.kota.Bahamut.Pages.BoardExtendOptionalPageListener
    public void onBoardExtendOptionalPageDidSelectBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            this._last_list_action = 1;
            pushCommand(new BahamutCommandSearchArticle(bookmark.getKeyword(), bookmark.getAuthor(), bookmark.getMark(), bookmark.getGy()));
        }
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        int i2 = i + 1;
        int block = ItemUtils.getBlock(i2);
        BoardPageItem boardPageItem = (BoardPageItem) getItem(i);
        if (boardPageItem == null && getCurrentBlock() != block && !isLoadingBlock(i2)) {
            loadBoardBlock(block);
        }
        if (view == null) {
            view = new BoardPageItemView(getContext());
            view.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        }
        BoardPageItemView boardPageItemView = (BoardPageItemView) view;
        boardPageItemView.setItem(boardPageItem);
        boardPageItemView.setNumber(i2);
        if (boardPageItem != null && this._settings.getPropertiesBlockListEnable() && this._settings.isBlockListContains(boardPageItem.Author)) {
            boardPageItemView.setVisible(false);
        } else {
            boardPageItemView.setVisible(true);
        }
        return view;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public void recycleBlock(TelnetListPageBlock telnetListPageBlock) {
        BoardPageBlock.recycle((BoardPageBlock) telnetListPageBlock);
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public void recycleItem(TelnetListPageItem telnetListPageItem) {
        BoardPageItem.recycle((BoardPageItem) telnetListPageItem);
    }

    // 修改文章 訊息發出
    @Override // com.kota.Bahamut.Pages.PostArticlePage_Listener
    public void onPostDialogEditButtonClicked(PostArticlePage postArticlePage, String str, String str2, String str3) {
        pushCommand(new BahamutCommandEditArticle(str, str2, str3));
    }

    Timer timer;
    // 發表文章/回覆文章 訊息發出
    @Override // com.kota.Bahamut.Pages.PostArticlePage_Listener
    public void onPostDialogSendButtonClicked(PostArticlePage postArticlePage, String str, String str2, String str3, String str4, String str5, Boolean boolean6) {
        pushCommand(new BahamutCommandPostArticle(this, str, str2, str3, str4, str5, boolean6));
        ASProcessingDialog.showProcessingDialog(getContextString(R.string.board_page_post_waiting_message_1));

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ASProcessingDialog.setMessage(getContextString(R.string.board_page_post_waiting_message_2));
            }
        };
        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run() {
                ASProcessingDialog.setMessage(getContextString(R.string.board_page_post_waiting_message_3));
            }
        };
        timer.schedule(timerTask, 3000);
        timer.schedule(timerTask2, 6000);
    }

    // 引言過多, 回逤發文時的設定
    public void recoverPost() {
        new ASRunner() { // from class: com.kota.Bahamut.Pages.BoardPage.12
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                cleanCommand(); // 清除引言過多留下的command buffer
                PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
                page.recover = true;
            }
        }.runInMainThread();
        if (timer!=null)
            timer.cancel();
        ASProcessingDialog.hideProcessingDialog();
    }
    // 完成發文
    public void finishPost() {
        new ASRunner() { // from class: com.kota.Bahamut.Pages.BoardPage.12
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
                page.closeArticle();
            }
        }.runInMainThread();
        if (timer!=null)
            timer.cancel();
        ASProcessingDialog.hideProcessingDialog();
    }
    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidAppear() {
        super.onPageDidAppear();
        this._bookmark_adapter.notifyDataSetChanged();
    }

    private void reloadBookmark() {
        String listName = getListName();
        Context context = getContext();
        if (context == null) {
            return;
        }
        new BookmarkStore(context).getBookmarkList(listName).loadTitleList(this._list);
        if (isPageAppeared()) {
            this._bookmark_adapter.notifyDataSetChanged();
        }
    }

    void closeDrawer() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    void reloadBlockEnableButton() {
        Button button = (Button) findViewById(R.id.block_enable_button);
        if (button != null) {
            if (this._settings.getPropertiesBlockListEnable()) {
                button.setText(R.string.Drawer_button_text_enable);
                return;
            }
            button.setText(R.string.Drawer_button_text_disable);
        }
    }

    boolean isDrawerOpen() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            return drawerLayout.isDrawerOpen(GravityCompat.END);
        }
        return false;
    }
}