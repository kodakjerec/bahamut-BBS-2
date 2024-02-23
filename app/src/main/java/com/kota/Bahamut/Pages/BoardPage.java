package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
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
import com.kota.Bahamut.Pages.BookmarkPage.BoardExtendOptionalPageListener;
import com.kota.Bahamut.Pages.BookmarkPage.BookmarkManagePage;
import com.kota.Bahamut.Pages.BookmarkPage.BoardExtendOptionalPageBookmarkItemView;
import com.kota.Bahamut.Pages.Model.BoardPageBlock;
import com.kota.Bahamut.Pages.Model.BoardPageHandler;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.Pages.Model.ToolBarFloating;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetHeaderItemView;
import com.kota.TelnetUI.TextView.TelnetTextViewLarge;
import com.kota.TelnetUI.TextView.TelnetTextViewNormal;

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
    private int _last_list_action = BoardPageAction.LIST;
    private boolean _initialed = false;
    private boolean _refresh_header_view = false; // 正在更新標題列
    private boolean blockListEnable = false; // 是否啟用黑名單
    private boolean _isDrawerOpening = false; // 側邊選單正在開啟中
    private final List<Bookmark> _list = new ArrayList<>();

    // 發文
    private final View.OnClickListener mPostListener = view -> BoardPage.this.onPostButtonClicked();

    // 前一頁
    private final View.OnClickListener mFirstPageClickListener = view -> BoardPage.this.moveToFirstPosition();

    // 下一頁
    private final View.OnClickListener mLastPageClickListener = view -> {
        BoardPage.this.setManualLoadPage();
        BoardPage.this.moveToLastPosition();
    };

    private final View.OnClickListener mMenuButtonListener = view -> {
        DrawerLayout drawerLayout = (DrawerLayout) BoardPage.this.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(getDrawerLayoutGravityLocation())) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(getDrawerLayoutGravityLocation(), true);
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

        // 顯示側邊選單書籤
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

    // 側邊選單 listener
    DrawerLayout.DrawerListener _drawer_listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) { }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) { }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (newState == DrawerLayout.STATE_IDLE) {
                _isDrawerOpening = false;
            } else {
                _isDrawerOpening = true;
            }

            // 側邊選單未完全開啟 or 正要啟動狀態
            if ( !isDrawerOpen() || _isDrawerOpening) {
                BoardPage.this.reloadBookmark();
            }
        }
    };

    // 點書籤
    AdapterView.OnItemClickListener _bookmark_listener = (adapterView, view, i, j) -> {
        BoardPage.this.closeDrawer();
        Bookmark bookmark = (Bookmark) BoardPage.this._list.get(i);
        searchArticle(bookmark.getKeyword(), bookmark.getAuthor(), bookmark.getMark(), bookmark.getGy());
    };

    // 搜尋文章
    View.OnClickListener _search_listener = view -> {
        BoardPage.this.closeDrawer();
        BoardPage.this.showSearchArticleDialog();
    };

    // 選擇文章
    View.OnClickListener _select_listener = view -> {
        BoardPage.this.closeDrawer();
        BoardPage.this.showSelectArticleDialog();
    };

    // 啟用/停用 黑名單
    View.OnClickListener _enable_block_listener = view -> BoardPage.this.onChangeBlockStateButtonClicked();

    // 修改黑名單
    View.OnClickListener _edit_block_listener = view -> {
        BoardPage.this.closeDrawer();
        BoardPage.this.onEditBlockListButtonClicked();
    };

    // 開啟書籤管理
    View.OnClickListener _edit_bookmark_listener = view -> {
        BoardPage.this.closeDrawer();
        BoardPage.this.onBookmarkButtonClicked();
    };

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public int getPageLayout() {
        return R.layout.board_page;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return BahamutPage.BAHAMUT_BOARD;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isAutoLoadEnable() {
        return true;
    }

    @Override // com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate
    public boolean onASListViewHandleExtentOptional(ASListView aSListView, int i) {
        return false;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidLoad() {
        super.onPageDidLoad();
        _settings = new UserSettings(getContext());

        ASListView aSListView = (ASListView) findViewById(R.id.BoardPageListView);
        aSListView.extendOptionalDelegate = this;
        aSListView.setEmptyView(findViewById(R.id.BoardPageListEmptyView));
        setListView(aSListView);
        findViewById(R.id.BoardPagePostButton).setOnClickListener(mPostListener);
        findViewById(R.id.BoardPageFirstPageButton).setOnClickListener(mFirstPageClickListener);
        findViewById(R.id.BoardPageLastestPageButton).setOnClickListener(mLastPageClickListener);
        View search_article_button = findViewById(R.id.search_article_button);
        if (search_article_button != null) {
            search_article_button.setOnClickListener(_search_listener);
        }
        View select_article_button = findViewById(R.id.select_article_button);
        if (select_article_button != null) {
            select_article_button.setOnClickListener(_select_listener);
        }
        CheckBox block_enable_checkbox = (CheckBox) findViewById(R.id.block_enable_button_checkbox);
        if (block_enable_checkbox != null) {
            block_enable_checkbox.setChecked(_settings.getPropertiesBlockListEnable());
            block_enable_checkbox.setOnClickListener(_enable_block_listener);

            TelnetTextViewLarge text_checkbox = (TelnetTextViewLarge) findViewById(R.id.block_enable_button_checkbox_label);
            text_checkbox.setOnClickListener(_enable_block_listener);
        }
        View block_setting_button = findViewById(R.id.block_setting_button);
        if (block_setting_button != null) {
            block_setting_button.setOnClickListener(_edit_block_listener);
        }
        View bookmark_edit_button = findViewById(R.id.bookmark_edit_button);
        if (bookmark_edit_button != null) {
            bookmark_edit_button.setOnClickListener(_edit_bookmark_listener);
        }
        // 側邊選單
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            LinearLayout menu_view = (LinearLayout)findViewById(R.id.menu_view);
            DrawerLayout.LayoutParams layoutParams_drawer = (DrawerLayout.LayoutParams) menu_view.getLayoutParams();
            layoutParams_drawer.gravity = getDrawerLayoutGravityLocation();
            menu_view.setLayoutParams(layoutParams_drawer);
            drawerLayout.addDrawerListener(_drawer_listener);
        }
        // 側邊選單內的書籤
        ListView listView = (ListView) findViewById(R.id.bookmark_list_view);
        if (listView != null) {
            listView.setAdapter(_bookmark_adapter);
            listView.setOnItemClickListener(_bookmark_listener);
        }
        ((TelnetHeaderItemView) findViewById(R.id.BoardPage_HeaderView)).setMenuButton(mMenuButtonListener);
        refreshHeaderView();

        blockListEnable = _settings.getPropertiesBlockListEnable();

        // 解決android 14跳出軟鍵盤
        // 先把 focus 設定到其他目標物, 避免系統在回收過程一個個去 focus
        // keyword: clearFocusInternal
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.content_view);
        if (relativeLayout != null)
            relativeLayout.requestFocus();

        // 工具列位置
        changeToolbarLocation();

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat()) {
            // 最後頁
            findViewById(R.id.BoardPageLastestPageButton).performClick();
            // 任務完成
            // 關閉"正在自動登入"
            TempSettings.setIsUnderAutoToChat(false);

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    ASProcessingDialog.hideProcessingDialog();
                }
            };
            timer.schedule(task, 500);
        }
    }

    // 側邊選單的位置
    private int getDrawerLayoutGravityLocation() {
        int location = _settings.getPropertiesDrawerLocation();
        if (location == 1) {
            return GravityCompat.START;
        }
        return GravityCompat.END;
    }
    // 變更工具列位置
    private void changeToolbarLocation() {
        LinearLayout toolbar = (LinearLayout) findViewById(R.id.toolbar);
        LinearLayout toolbarBlock = (LinearLayout)findViewById(R.id.toolbar_block);
        ToolBarFloating toolBarFloating = (ToolBarFloating) findViewById(R.id.ToolbarFloatingComponent);
        toolBarFloating.setVisibility(View.GONE);

        final float scale = getResource().getDisplayMetrics().density;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        int choice_toolbar_location = _settings.getPropertiesToolbarLocation(); // 0-中間 1-靠左 2-靠右 3-浮動
        switch (choice_toolbar_location) {
            case 1:
                // 底部-最左邊
                layoutParams.width = (int)(230 * scale);
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.addRule(RelativeLayout.ALIGN_START);
                break;
            case 2:
                // 底部-最右邊
                layoutParams.width = (int)(230 * scale);
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                break;
            case 3:
                // 浮動
                // 去除 原本工具列
                toolbar.setVisibility(View.GONE);
                // 去除底部卡位用view
                toolbarBlock.setVisibility(View.GONE);
                // 浮動工具列
                toolBarFloating.setVisibility(View.VISIBLE);
                // button setting
                toolBarFloating.setOnClickListenerSetting(mPostListener);
                Button OriginalBtn = ((Button)findViewById(R.id.BoardPagePostButton));
                toolBarFloating.setTextSetting(OriginalBtn.getText().toString());
                // button 1
                toolBarFloating.setOnClickListener1(mFirstPageClickListener);
                toolBarFloating.setText1(getContextString(R.string.first_page));
                // button 2
                toolBarFloating.setOnClickListener2(mLastPageClickListener);
                toolBarFloating.setText2(getContextString(R.string.last_page));
                break;
            default:
                // 底部-中間
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                break;
        }

        // 反轉按鈕順序
        int choice_toolbar_order = _settings.getPropertiesToolbarOrder();
        if (choice_toolbar_order == 1) {
            ArrayList<View> alViews = new ArrayList<>();
            for (int i = toolbar.getChildCount() - 1; i >= 0; i--) {
                View view = toolbar.getChildAt(i);
                alViews.add(view);
            }
            toolbar.removeAllViews();
            for (int j = 0; j < alViews.size(); j++) {
                toolbar.addView(alViews.get(j));
            }
        }
        toolbar.setLayoutParams(layoutParams);
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onMenuButtonClicked() {
        mMenuButtonListener.onClick(null);
        return true;
    }

    private void refreshHeaderView() {
        String board_title = _board_title;
        board_title = (board_title == null || board_title.length() == 0) ? getContextString(R.string.loading) : board_title;
        String board_manager = _board_manager;
        board_manager = (board_manager == null || board_manager.length() == 0) ? getContextString(R.string.loading) : board_manager;
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
        if (!_initialed) {
            clear();
            if (load.Type == 1) {
                pushRefreshCommand(0);
            }
            _board_manager = load.BoardManager;
            _board_title = load.BoardTitle;
            setListName(load.BoardName);
            _refresh_header_view = true;
            _initialed = true;
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

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        if (_refresh_header_view) {
            refreshHeaderView();
            _refresh_header_view = false;
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(getDrawerLayoutGravityLocation())) {
            drawerLayout.closeDrawer(getDrawerLayoutGravityLocation());
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
    private void searchArticle(String _keyword, String _author, String _mark, String _gy) {
        _last_list_action = BoardPageAction.SEARCH;
        BoardSearchPage board_Search_Page = PageContainer.getInstance().getBoardSearchPage();
        board_Search_Page.clear();
        getNavigationController().pushViewController(board_Search_Page);
        ListState state = ListStateStore.getInstance().getState(board_Search_Page.getListIdFromListName(getListName()));
        if (state != null) {
            state.Top = 0;
            state.Position = 0;
        }
        board_Search_Page.setKeyword(_keyword);
        board_Search_Page.setAuthor(_author);
        board_Search_Page.setMark(_mark);
        board_Search_Page.setGy(_gy);
        pushCommand(new BahamutCommandSearchArticle(_keyword, _author, _mark, _gy));
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
        getNavigationController().pushViewController(new BookmarkManagePage(getListName(), this));
    }

    private void onListArticle(int i) {
        _last_list_action = BoardPageAction.LIST;
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

    @Override // com.kota.ASFramework.PageController.ASViewController
    public boolean onReceivedGestureRight() {
            if (_settings.getPropertiesGestureOnBoardEnable()) {
                if (isDrawerOpen() || _isDrawerOpening) {
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
        // from class: com.kota.Bahamut.Pages.BoardPage.10
// com.kota.ASFramework.Dialog.ASAlertDialogListener
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.do_gy))
                .setMessage(getContextString(R.string.gy_this_article))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.do_gy))
                .setListener((aSAlertDialog, i2) -> {
            switch (i2) {
                case 0:
                default:
                    return;
                case 1:
                    BoardPage.this.pushCommand(new BahamutCommandGoodArticle(i));
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
            return blockListEnable && _settings.isBlockListContains(((BoardPageItem) telnetListPageItem).Author);
        }
        return false;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemBlockEnable() {
        return blockListEnable;
    }

    public int getLastListAction() {
        return _last_list_action;
    }

    private void onChangeBlockStateButtonClicked() {
        _settings.setPropertiesBlockListEnable(!blockListEnable);
        blockListEnable = _settings.getPropertiesBlockListEnable();
        CheckBox block_enable_checkbox = (CheckBox) findViewById(R.id.block_enable_button_checkbox);
        block_enable_checkbox.setChecked(blockListEnable);
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
        _initialed = false;
    }

    // 書籤管理->按下書籤
    @Override // com.kota.Bahamut.BookmarkPage.BoardExtendOptionalPageListener
    public void onBoardExtendOptionalPageDidSelectBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            _last_list_action = BoardPageAction.SEARCH;
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
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        BoardPageItemView boardPageItemView = (BoardPageItemView) view;
        boardPageItemView.setItem(boardPageItem);
        boardPageItemView.setNumber(i2);
        if (boardPageItem != null && blockListEnable && _settings.isBlockListContains(boardPageItem.Author)) {
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
        // 回應到作者信箱
        if (str3!= null && str3.equals("M")) {
            return;
        }
        // 發文中等待視窗
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        ASProcessingDialog.hideProcessingDialog();
    }
    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidAppear() {
        super.onPageDidAppear();
        _bookmark_adapter.notifyDataSetChanged();
    }

    private void reloadBookmark() {
        String listName = getListName();
        Context context = getContext();
        if (context == null) {
            return;
        }
        new BookmarkStore(context).getBookmarkList(listName).loadTitleList(_list);
        if (isPageAppeared()) {
            _bookmark_adapter.notifyDataSetChanged();
        }
    }

    void closeDrawer() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    // 側邊選單已開啟 或 正在開啟中
    boolean isDrawerOpen() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            return drawerLayout.isDrawerOpen(getDrawerLayoutGravityLocation());
        }
        return false;
    }

    @Override
    public void onSearchDialogCancelButtonClicked() {

    }
}