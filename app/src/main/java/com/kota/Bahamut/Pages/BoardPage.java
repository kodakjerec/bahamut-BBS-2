package com.kota.Bahamut.Pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
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
import com.kota.Bahamut.Command.TelnetCommand;
import com.kota.Bahamut.DataModels.Bookmark;
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
import java.util.Vector;

/* loaded from: classes.dex */
public class BoardPage extends TelnetListPage implements Dialog_SearchArticle_Listener, Dialog_SelectArticle_Listener, PostArticlePage_Listener, BoardExtendOptionalPageListener, ASListViewExtentOptionalDelegate {
    UserSettings _settings;
    protected String _board_title = null;
    protected String _board_manager = null;
    private int _last_list_action = 0;
    private boolean _initialed = false;
    private boolean _refresh_header_view = false;
    private View.OnClickListener mOpenBookmarkListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.2
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BoardPage.this.onBookmarkButtonClicked();
        }
    };
    private View.OnClickListener mPostListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.5
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BoardPage.this.onPostButtonClicked();
        }
    };
    private View.OnClickListener mSearchByKeywordListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.6
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BoardPage.this.onSearchButtonClicked();
        }
    };
    private View.OnClickListener mSearchByNumberListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.7
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BoardPage.this.showSelectArticleDialog();
        }
    };
    private View.OnClickListener mFirstPageClicked = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.8
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BoardPage.this.moveToFirstPosition();
        }
    };
    private View.OnClickListener mLastPageClicked = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.9
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BoardPage.this.setManualLoadPage();
            BoardPage.this.moveToLastPosition();
        }
    };
    private View.OnClickListener mMenuButtonListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.11
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BoardPage.this.onMenuClicked();
        }
    };

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return 10;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public int getPageLayout() {
        return R.layout.board_page;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidLoad() {
        super.onPageDidLoad();
        this._settings = new UserSettings(getContext());
        ASListView list_view = (ASListView) findViewById(R.id.BoardPage_ListView);
        list_view.extendOptionalDelegate = this;
        View list_empty_view = findViewById(R.id.BoardPage_ListEmptyView);
        list_view.setEmptyView(list_empty_view);
        setListView(list_view);
        Button post_button = (Button) findViewById(R.id.BoardPage_PostButton);
        post_button.setOnClickListener(this.mPostListener);
        Button first_page_button = (Button) findViewById(R.id.BoardPage_FirstPageButton);
        first_page_button.setOnClickListener(this.mFirstPageClicked);
        Button last_page_button = (Button) findViewById(R.id.BoardPage_LastestPageButton);
        last_page_button.setOnClickListener(this.mLastPageClicked);
        Button select_by_keyword_button = (Button) findViewById(R.id.search_by_keyword);
        if (select_by_keyword_button != null) {
            select_by_keyword_button.setOnClickListener(this.mSearchByKeywordListener);
        }
        Button select_by_number_button = (Button) findViewById(R.id.search_by_number);
        if (select_by_number_button != null) {
            select_by_number_button.setOnClickListener(this.mSearchByNumberListener);
        }
        Button open_bookmark_button = (Button) findViewById(R.id.open_bookmark);
        if (open_bookmark_button != null) {
            open_bookmark_button.setOnClickListener(this.mOpenBookmarkListener);
        }
        TelnetHeaderItemView header_view = (TelnetHeaderItemView) findViewById(R.id.BoardPage_HeaderView);
        header_view.setMenuButton(this.mMenuButtonListener);
        refreshHeaderView();
        refreshExternalToolbar();
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

    protected boolean isBookmarkAvailable() {
        return true;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public String getListIdFromListName(String aName) {
        return aName + "[Board]";
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public TelnetListPageBlock loadPage() {
        BoardPageBlock loaded_data = BoardPageHandler.getInstance().load();
        if (!this._initialed) {
            clear();
            if (loaded_data.Type == 1) {
                pushRefreshCommand(0);
            }
            this._board_manager = loaded_data.BoardManager;
            this._board_title = loaded_data.BoardTitle;
            setListName(loaded_data.BoardName);
            this._refresh_header_view = true;
            this._initialed = true;
        }
        return loaded_data;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemCanLoadAtIndex(int index) {
        BoardPageItem item = (BoardPageItem) getItem(index);
        if (item == null || !item.isDeleted) {
            return true;
        }
        ASToast.showShortToast("此文章已被刪除");
        return false;
    }

    public void setBoardTitle(String aBoardTitle) {
        this._board_title = aBoardTitle;
    }

    public void setBoardManager(String aBoardManager) {
        this._board_manager = aBoardManager;
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
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(256, 1);
        PageContainer.getInstance().cleanBoardPage();
        return true;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    protected boolean onListViewItemLongClicked(View itemView, int index) {
        onListArticle(index + 1);
        return true;
    }

    public void onBlockButtonClicked(final String aBlockName) {
        ASAlertDialog.createDialog().setTitle("加入黑名單").setMessage("是否要將\"" + aBlockName + "\"加入黑名單?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.Pages.BoardPage.1
            @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                if (index == 1) {
                    BoardPage.this._settings.addBlockName(aBlockName);
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
        Dialog_SearchArticle dialog = new Dialog_SearchArticle();
        dialog.setListener(this);
        dialog.show();
    }

    protected void showSelectArticleDialog() {
        Dialog_SelectArticle dialog = new Dialog_SelectArticle();
        dialog.setListener(this);
        dialog.show();
    }

    @Override // com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener
    public void onSearchDialogSearchButtonClickedWithValues(Vector<String> values) {
        String keyword = values.get(0);
        String author = values.get(1);
        String mark = values.get(2).equals("YES") ? "y" : "n";
        String gy = values.get(3);
        searchArticle(keyword, author, mark, gy);
    }

    private void searchArticle(String keyword, String author, String mark, String gy) {
        this._last_list_action = 1;
        BoardSearchPage page = PageContainer.getInstance().getBoard_Search_Page();
        page.clear();
        getNavigationController().pushViewController(page);
        ListState state = ListStateStore.getInstance().getState(page.getListIdFromListName(getListName()));
        if (state != null) {
            state.Top = 0;
            state.Position = 0;
        }
        page.setKeyword(keyword);
        page.setAuthor(author);
        page.setMark(mark);
        page.setGy(gy);
        TelnetCommand command = new BahamutCommandSearchArticle(keyword, author, mark, gy);
        pushCommand(command);
    }

    @Override // com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener
    public void onSelectDialogDismissWIthIndex(String aIndexString) {
        int item_number = -1;
        try {
            item_number = Integer.parseInt(aIndexString) - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (item_number >= 0) {
            setListViewSelection(item_number);
        }
    }

    public void onBookmarkButtonClicked() {
        getNavigationController().pushViewController(new BoardExtendOptionalPage(getListName(), this));
    }

    void onMenuClicked() {
        String block_message;
        if (this._settings.isBlockListEnable()) {
            block_message = "停用黑名單";
        } else {
            block_message = "啟用黑名單";
        }
        boolean ext_toolbar_enable = this._settings.isExternalToolbarEnable();
        String external_toolbar_enable_title = ext_toolbar_enable ? "隱藏工具列" : "開啟工具列";
        ASListDialog.createDialog().addItem(isBookmarkAvailable() ? "開啟書籤" : null).addItem("搜尋文章").addItem("選擇文章").addItem(block_message).addItem("編輯黑名單").addItem(external_toolbar_enable_title).setListener(new ASListDialogItemClickListener() { // from class: com.kota.Bahamut.Pages.BoardPage.3
            @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
            public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                switch (index) {
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
                    case 5:
                        BoardPage.this.onExternalToolbarClicked();
                        return;
                    default:
                        return;
                }
            }

            @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
            public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                return false;
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    @Override // com.kota.Bahamut.Pages.PostArticlePage_Listener
    public void onPostDialogEditButtonClicked(PostArticlePage aDialog, String aArticleNumber, String title, String content) {
        BahamutCommandEditArticle command = new BahamutCommandEditArticle(aArticleNumber, title, content);
        pushCommand(command);
    }

    @Override // com.kota.Bahamut.Pages.PostArticlePage_Listener
    public void onPostDialogSendButtonClicked(PostArticlePage aDialog, String title, String content, String aTarget, String aReplyNumber, String aSign) {
        BahamutCommandPostArticle command = new BahamutCommandPostArticle(this, title, content, aTarget, aReplyNumber, aSign);
        pushCommand(command);
    }

    private void onListArticle(int itemIndex) {
        this._last_list_action = 0;
        BoardLinkPage page = PageContainer.getInstance().getBoard_Linked_Title_Page();
        page.clear();
        getNavigationController().pushViewController(page);
        ListState state = ListStateStore.getInstance().getState(page.getListIdFromListName(getListName()));
        if (state != null) {
            state.Top = 0;
            state.Position = 0;
        }
        TelnetCommand command = new BahamutCommandListArticle(itemIndex);
        pushCommand(command);
    }

    protected void onDeleteArticle(final int itemNumber) {
        ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此文章?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.Pages.BoardPage.4
            @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                switch (index) {
                    case 0:
                    default:
                        return;
                    case 1:
                        TelnetCommand command = new BahamutCommandDeleteArticle(itemNumber);
                        BoardPage.this.pushCommand(command);
                        return;
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public boolean onReceivedGestureLeft() {
        return super.onReceivedGestureLeft();
    }

    protected void onPostButtonClicked() {
        PostArticlePage page = new PostArticlePage();
        page.setBoardPage(this);
        page.setListener(this);
        getNavigationController().pushViewController(page);
    }

    public void goodLoadingArticle() {
        goodArticle(getLoadingItemNumber());
    }

    public void goodArticle(final int articleIndex) {
        ASAlertDialog.createDialog().setTitle("推薦").setMessage("是否要推薦此文章?").addButton("取消").addButton("推薦").setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.Pages.BoardPage.10
            @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                switch (index) {
                    case 0:
                    default:
                        return;
                    case 1:
                        BahamutCommandGoodArticle command = new BahamutCommandGoodArticle(articleIndex);
                        BoardPage.this.pushCommand(command);
                        return;
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    public void loadTheSameTitleTop() {
        onLoadItemStart();
        BahamutCommandTheSameTitleTop command = new BahamutCommandTheSameTitleTop(getLoadingItemNumber());
        pushCommand(command);
    }

    public void loadTheSameTitleBottom() {
        onLoadItemStart();
        BahamutCommandTheSameTitleBottom command = new BahamutCommandTheSameTitleBottom(getLoadingItemNumber());
        pushCommand(command);
    }

    public void loadTheSameTitleUp() {
        onLoadItemStart();
        BahamutCommandTheSameTitleUp command = new BahamutCommandTheSameTitleUp(getLoadingItemNumber());
        pushCommand(command);
    }

    public void loadTheSameTitleDown() {
        onLoadItemStart();
        BahamutCommandTheSameTitleDown command = new BahamutCommandTheSameTitleDown(getLoadingItemNumber());
        pushCommand(command);
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemBlocked(TelnetListPageItem aItem) {
        if (aItem != null) {
            BoardPageItem item = (BoardPageItem) aItem;
            return this._settings.isBlockListEnable() && this._settings.isBlockListContains(item.Author);
        }
        return false;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemBlockEnable() {
        return this._settings.isBlockListEnable();
    }

    public int getLastListAction() {
        return this._last_list_action;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isAutoLoadEnable() {
        return true;
    }

    private void onChangeBlockStateButtonClicked() {
        this._settings.setBlockListEnable(!this._settings.isBlockListEnable());
        reloadListView();
    }

    private void onEditBlockListButtonClicked() {
        BlockListPage page = new BlockListPage();
        getNavigationController().pushViewController(page);
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public void loadItemAtIndex(int index) {
        ArticlePage page = PageContainer.getInstance().getArticlePage();
        page.setBoardPage(this);
        page.clear();
        getNavigationController().pushViewController(page);
        super.loadItemAtIndex(index);
    }

    public void prepareInitial() {
        this._initialed = false;
    }

    @Override // com.kota.Bahamut.Pages.BoardExtendOptionalPageListener
    public void onBoardExtendOptionalPageDidSelectBookmark(Bookmark aBookmark) {
        if (aBookmark != null) {
            this._last_list_action = 1;
            TelnetCommand command = new BahamutCommandSearchArticle(aBookmark.getKeyword(), aBookmark.getAuthor(), aBookmark.getMark(), aBookmark.getGy());
            pushCommand(command);
        }
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, android.widget.Adapter
    public View getView(int index, View itemView, ViewGroup parentView) {
        int item_index = index + 1;
        int item_block = ItemUtils.getBlock(item_index);
        BoardPageItem item = (BoardPageItem) getItem(index);
        if (item == null && getCurrentBlock() != item_block && !isLoadingBlock(item_index)) {
            loadBoardBlock(item_block);
        }
        if (itemView == null) {
            itemView = new BoardPageItemView(getContext());
            itemView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        }
        BoardPageItemView item_view = (BoardPageItemView) itemView;
        item_view.setItem(item);
        item_view.setNumber(index + 1);
        if (item != null && this._settings.isBlockListEnable() && this._settings.isBlockListContains(item.Author)) {
            item_view.setVisible(false);
        } else {
            item_view.setVisible(true);
        }
        return itemView;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public void recycleBlock(TelnetListPageBlock aBlock) {
        BoardPageBlock.recycle((BoardPageBlock) aBlock);
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public void recycleItem(TelnetListPageItem aItem) {
        BoardPageItem.recycle((BoardPageItem) aItem);
    }

    @Override // com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate
    public boolean onASListViewHandleExtentOptional(ASListView aListView, int index) {
        return false;
    }

    public void onExternalToolbarClicked() {
        boolean enable = this._settings.isExternalToolbarEnable();
        this._settings.setExternalToolbarEnable(!enable);
        refreshExternalToolbar();
    }

    private void refreshExternalToolbar() {
        boolean enable = this._settings.isExternalToolbarEnable();
        View toolbar_view = findViewById(R.id.ext_toolbar);
        if (toolbar_view != null) {
            toolbar_view.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    public void recoverPost() {
        new ASRunner() { // from class: com.kota.Bahamut.Pages.BoardPage.12
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                PostArticlePage page = new PostArticlePage();
                page.recover = true;
                page.setBoardPage(BoardPage.this);
                page.setListener(BoardPage.this);
                BoardPage.this.getNavigationController().pushViewController(page);
            }
        }.runInMainThread();
    }
}
