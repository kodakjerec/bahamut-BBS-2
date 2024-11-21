package com.kota.Bahamut.Pages.BoardPage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;
import static com.kota.Bahamut.Service.CommonFunctions.rgbToInt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.Command.BahamutCommandEditArticle;
import com.kota.Bahamut.Command.BahamutCommandFSendMail;
import com.kota.Bahamut.Command.BahamutCommandGoodArticle;
import com.kota.Bahamut.Command.BahamutCommandListArticle;
import com.kota.Bahamut.Command.BahamutCommandPostArticle;
import com.kota.Bahamut.Command.BahamutCommandPushArticle;
import com.kota.Bahamut.Command.BahamutCommandSearchArticle;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleBottom;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleDown;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleTop;
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleUp;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.Dialogs.DialogPushArticle;
import com.kota.Bahamut.Dialogs.DialogSearchArticle;
import com.kota.Bahamut.Dialogs.DialogSearchArticleListener;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle;
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener;
import com.kota.Bahamut.ListPage.ListState;
import com.kota.Bahamut.ListPage.ListStateStore;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Bahamut.ListPage.TelnetListPageItem;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage;
import com.kota.Bahamut.Pages.BlockListPage.BlockListPage;
import com.kota.Bahamut.Pages.BookmarkPage.BoardExtendOptionalPageListener;
import com.kota.Bahamut.Pages.BookmarkPage.BookmarkManagePage;
import com.kota.Bahamut.Pages.Model.BoardPageBlock;
import com.kota.Bahamut.Pages.Model.BoardPageHandler;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.Pages.Model.ToolBarFloating;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.Pages.PostArticlePage_Listener;
import com.kota.Bahamut.Pages.Theme.Theme;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.Pages.Theme.ThemeStore;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.TelnetUI.TextView.TelnetTextViewLarge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class BoardMainPage extends TelnetListPage implements DialogSearchArticleListener, Dialog_SelectArticle_Listener, PostArticlePage_Listener, BoardExtendOptionalPageListener, ASListViewExtentOptionalDelegate {
    DrawerLayout mainDrawerLayout;
    RelativeLayout mainLayout;
    protected String _board_title = null;
    protected String _board_manager = null;
    int _last_list_action = BoardPageAction.LIST;
    boolean _initialed = false;
    boolean _refresh_header_view = false; // 正在更新標題列
    boolean blockListEnable = false; // 是否啟用黑名單
    boolean blockListForTitle = false; // 是否啟用黑名單套用至標題
    boolean _isDrawerOpening = false; // 側邊選單正在開啟中
    final List<Bookmark> _bookmarkList = new ArrayList<>();
    ListView _drawerListView;
    TextView _drawerListViewNone;
    int _mode = 0; // 現在開啟的是 0-書籤 1-紀錄
    Button[] _tab_buttons;
    Button _show_bookmark_button; // 顯示書籤按鈕
    Button _show_history_button; // 顯示記錄按鈕
    int drawerLocation = GravityCompat.END; // 抽屜最後位置
    private boolean isPostDelayedSuccess = false;

    /** 發文 */
    final View.OnClickListener mPostListener = view -> BoardMainPage.this.onPostButtonClicked();

    /** 最前頁 */
    final View.OnLongClickListener mFirstPageClickListener = view -> {
        BoardMainPage.this.moveToFirstPosition();
        return true;
    };
    /** 前一頁 */
    final View.OnClickListener mPrevPageClickListener = view -> {
        int firstIndex = _list_view.getFirstVisiblePosition();
        int endIndex = _list_view.getLastVisiblePosition();
        int moveIndex = Math.abs(endIndex-firstIndex);
        firstIndex -= moveIndex;
        if (firstIndex < 0)
            firstIndex = 0;
        setListViewSelection(firstIndex);
    };

    /** 下一頁 */
    final View.OnClickListener mLastPageClickListener = view -> {
        BoardMainPage.this.setManualLoadPage();
        BoardMainPage.this.moveToLastPosition();
    };

    /** 彈出側邊選單 */
    final View.OnClickListener mMenuButtonListener = view -> {
        DrawerLayout drawerLayout = (DrawerLayout) BoardMainPage.this.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(drawerLocation)) {
                drawerLayout.closeDrawer(drawerLocation);
            } else {
                if (UserSettings.getPropertiesDrawerLocation() == 0) {
                    drawerLocation = GravityCompat.END;
                } else {
                    drawerLocation = GravityCompat.START;
                }
                LinearLayout menu_view = mainDrawerLayout.findViewById(R.id.menu_view);
                DrawerLayout.LayoutParams layoutParams_drawer = (DrawerLayout.LayoutParams) menu_view.getLayoutParams();
                layoutParams_drawer.gravity = drawerLocation;
                menu_view.setLayoutParams(layoutParams_drawer);
                drawerLayout.openDrawer(drawerLocation, UserSettings.getPropertiesAnimationEnable());
            }
        }
    };
    /** 跳出小視窗 全部已讀/全部未讀 */
    final View.OnClickListener mReadAllListener = view -> {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.string._article))
                .addItem(getContextString(R.string.board_main_read_all))
                .addItem(getContextString(R.string.board_main_unread_all))
                .setListener(new ASListDialogItemClickListener() {
                    public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                        return true;
                    }

                    public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                        if (Objects.equals(aTitle, getContextString(R.string.board_main_read_all))) {
                            byte[] data = TelnetOutputBuilder.create()
                                    .pushString("vV\n")
                                    .build();
                            TelnetClient.getClient().sendDataToServer(data);
                            ASToast.showShortToast(getContextString(R.string.board_main_read_all_msg01));
                        } else if (Objects.equals(aTitle, getContextString(R.string.board_main_unread_all))) {
                            byte[] data = TelnetOutputBuilder.create()
                                    .pushString("vU\n")
                                    .build();
                            TelnetClient.getClient().sendDataToServer(data);
                            ASToast.showShortToast(getContextString(R.string.board_main_unread_all_msg01));
                        }
                    }
                }).show();
    };

    BaseAdapter _bookmark_adapter = new BaseAdapter() { 
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return BoardMainPage.this._bookmarkList.size();
        }

        @Override // android.widget.Adapter
        public Bookmark getItem(int i) {
            return BoardMainPage.this._bookmarkList.get(i);
        }

        /** 顯示側邊選單書籤 */
        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new BoardExtendBookmarkItemView(BoardMainPage.this.getContext());
            }
            BoardExtendBookmarkItemView boardExtendBookmarkItemView = (BoardExtendBookmarkItemView) view;
            boardExtendBookmarkItemView.setBookmark(getItem(i));
            boardExtendBookmarkItemView.setDividerTopVisible(i == 0);
            return view;
        }
    };

    BaseAdapter _history_adapter = new BaseAdapter() {
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return BoardMainPage.this._bookmarkList.size();
        }

        @Override // android.widget.Adapter
        public Bookmark getItem(int i) {
            return BoardMainPage.this._bookmarkList.get(i);
        }

        /** 顯示側邊選單書籤 */
        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new BoardExtendHistoryItemView(BoardMainPage.this.getContext());
            }
            BoardExtendHistoryItemView boardExtendHistoryItemView = (BoardExtendHistoryItemView) view;
            boardExtendHistoryItemView.setBookmark(getItem(i));
            boardExtendHistoryItemView.setDividerTopVisible(i == 0);
            return view;
        }
    };

    /** 側邊選單 mListener */
    DrawerLayout.DrawerListener _drawer_listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) { }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) { }

        @Override
        public void onDrawerStateChanged(int newState) {
            _isDrawerOpening = newState != DrawerLayout.STATE_IDLE;

            // 側邊選單未完全開啟 or 正要啟動狀態
            if ( !isDrawerOpen() || _isDrawerOpening) {
                BoardMainPage.this.reloadBookmark();
            }
        }
    };

    /** 點書籤 */
    AdapterView.OnItemClickListener _bookmark_listener = (adapterView, view, i, j) -> {
        BoardMainPage.this.closeDrawer();
        Bookmark bookmark = BoardMainPage.this._bookmarkList.get(i);
        searchArticle(bookmark.getKeyword(), bookmark.getAuthor(), bookmark.getMark(), bookmark.getGy());
    };

    /** 切換成書籤清單 */
    View.OnClickListener buttonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View aView) {
            if (aView == _show_bookmark_button) {
                _mode = 0;
            } else {
                _mode = 1;
            }
            if (_drawerListView != null) {
                if (_mode == 0)
                    _drawerListView.setAdapter(_bookmark_adapter);
                else
                    _drawerListView.setAdapter(_history_adapter);
                if (_drawerListView.getOnItemClickListener() == null)
                    _drawerListView.setOnItemClickListener(_bookmark_listener);
            }
            reloadBookmark();

            // 切換頁籤
            Theme theme = ThemeStore.INSTANCE.getSelectTheme();
            for (Button tab_button : BoardMainPage.this._tab_buttons) {
                if (tab_button == aView) {
                    tab_button.setTextColor(rgbToInt(theme.getTextColor()));
                    tab_button.setBackgroundColor(rgbToInt(theme.getBackgroundColor()));
                } else {
                    tab_button.setTextColor(rgbToInt(theme.getTextColorDisabled()));
                    tab_button.setBackgroundColor(rgbToInt(theme.getBackgroundColorDisabled()));
                }
            }
        }
    };

    /** 搜尋文章 */
    View.OnClickListener _search_listener = view -> {
        BoardMainPage.this.closeDrawer();
        BoardMainPage.this.showSearchArticleDialog();
    };

    /** 選擇文章 */
    View.OnClickListener _select_listener = view -> {
        BoardMainPage.this.closeDrawer();
        BoardMainPage.this.showSelectArticleDialog();
    };

    /** 啟用/停用 黑名單 */
    View.OnClickListener _enable_block_listener = view -> BoardMainPage.this.onChangeBlockStateButtonClicked();

    /** 修改黑名單 */
    View.OnClickListener _edit_block_listener = view -> {
        BoardMainPage.this.closeDrawer();
        BoardMainPage.this.onEditBlockListButtonClicked();
    };

    /** 開啟書籤管理 */
    View.OnClickListener _edit_bookmark_listener = view -> {
        BoardMainPage.this.closeDrawer();
        BoardMainPage.this.onBookmarkButtonClicked();
    };
    /** 靠左對其 */
    View.OnClickListener _btnLL_listener = view -> {
        UserSettings.setPropertiesToolbarLocation(1);
        BoardMainPage.this.changeToolbarLocation();
    };
    /** 靠右對其 */
    View.OnClickListener _btnRR_listener = view -> {
        UserSettings.setPropertiesToolbarLocation(2);
        BoardMainPage.this.changeToolbarLocation();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidLoad() {
        super.onPageDidLoad();

        mainDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mainLayout = (RelativeLayout) findViewById(R.id.content_view); 

        ASListView aSListView = mainLayout.findViewById(R.id.BoardPageListView);
        aSListView.extendOptionalDelegate = this;
        aSListView.setEmptyView(mainLayout.findViewById(R.id.BoardPageListEmptyView));
        setListView(aSListView);

        mainLayout.findViewById(R.id.BoardPagePostButton).setOnClickListener(mPostListener);
        mainLayout.findViewById(R.id.BoardPageFirstPageButton).setOnClickListener(mPrevPageClickListener);
        mainLayout.findViewById(R.id.BoardPageFirstPageButton).setOnLongClickListener(mFirstPageClickListener);
        mainLayout.findViewById(R.id.BoardPageLatestPageButton).setOnClickListener(mLastPageClickListener);
        mainLayout.findViewById(R.id.BoardPageLLButton).setOnClickListener(_btnLL_listener);
        mainLayout.findViewById(R.id.BoardPageRRButton).setOnClickListener(_btnRR_listener);

        // 側邊選單
        if (mainDrawerLayout!=null) {
            // 替換外觀
            new ThemeFunctions().layoutReplaceTheme(mainDrawerLayout.findViewById(R.id.menu_view));

            DrawerLayout drawerLayout = mainDrawerLayout.findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                LinearLayout menu_view = mainDrawerLayout.findViewById(R.id.menu_view);
                DrawerLayout.LayoutParams layoutParams_drawer = (DrawerLayout.LayoutParams) menu_view.getLayoutParams();
                layoutParams_drawer.gravity = drawerLocation;
                menu_view.setLayoutParams(layoutParams_drawer);
                drawerLayout.addDrawerListener(_drawer_listener);
                // 根據手指位置設定側邊選單位置
                aSListView.setOnTouchListener((view, motionEvent) -> {
                    if (_isDrawerOpening)
                        return false;
                    int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels/2;
                    if (motionEvent.getX()<screenWidth)
                        layoutParams_drawer.gravity = GravityCompat.START;
                    else
                        layoutParams_drawer.gravity = GravityCompat.END;
                    drawerLocation = layoutParams_drawer.gravity;
                    menu_view.setLayoutParams(layoutParams_drawer);
                    return false;
                });
            }
            View search_article_button = mainDrawerLayout.findViewById(R.id.search_article_button);
            if (search_article_button != null) {
                search_article_button.setOnClickListener(_search_listener);
            }
            View select_article_button = mainDrawerLayout.findViewById(R.id.select_article_button);
            if (select_article_button != null) {
                select_article_button.setOnClickListener(_select_listener);
            }
            CheckBox block_enable_checkbox = mainDrawerLayout.findViewById(R.id.block_enable_button_checkbox);
            if (block_enable_checkbox != null) {
                block_enable_checkbox.setChecked(UserSettings.getPropertiesBlockListEnable());
                block_enable_checkbox.setOnClickListener(_enable_block_listener);

                TelnetTextViewLarge text_checkbox = mainDrawerLayout.findViewById(R.id.block_enable_button_checkbox_label);
                text_checkbox.setOnClickListener(_enable_block_listener);
            }
            View block_setting_button = mainDrawerLayout.findViewById(R.id.block_setting_button);
            if (block_setting_button != null) {
                block_setting_button.setOnClickListener(_edit_block_listener);
            }
            View bookmark_edit_button = mainDrawerLayout.findViewById(R.id.bookmark_edit_button);
            if (bookmark_edit_button != null) {
                bookmark_edit_button.setOnClickListener(_edit_bookmark_listener);
            }
            // 側邊選單內的書籤
            _drawerListView = mainDrawerLayout.findViewById(R.id.bookmark_list_view);
            _drawerListViewNone = mainDrawerLayout.findViewById(R.id.bookmark_list_view_none);
            if (_drawerListView != null) {
                _show_bookmark_button = mainDrawerLayout.findViewById(R.id.show_bookmark_button);
                _show_history_button = mainDrawerLayout.findViewById(R.id.show_history_button);
                _tab_buttons = new Button[]{_show_bookmark_button, _show_history_button};
                _show_bookmark_button.setOnClickListener(buttonClickListener);
                _show_history_button.setOnClickListener(buttonClickListener);
                if (_mode == 0)
                    _show_bookmark_button.performClick();
                else
                    _show_history_button.performClick();
            }
            mainDrawerLayout.findViewById(R.id.bookmark_tab_button).setOnClickListener( toEssencePageClickListener );
        }

        // 標題
        BoardHeaderView boardPageHeaderView = mainLayout.findViewById(R.id.BoardPage_HeaderView);
        if (getPageType()==BahamutPage.BAHAMUT_BOARD) {
            boardPageHeaderView.setMenuButtonClickListener(mMenuButtonListener);
            boardPageHeaderView.setDetail1ClickListener(mReadAllListener);
        } else {
            boardPageHeaderView.setMenuButtonClickListener(null);
        }
        refreshHeaderView();

        blockListEnable = UserSettings.getPropertiesBlockListEnable();
        blockListForTitle = UserSettings.getPropertiesBlockListForTitle();

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));

        // 解決android 14跳出軟鍵盤
        // 先把 focus 設定到其他目標物, 避免系統在回收過程一個個去 focus
        // keyword: clearFocusInternal
        if (mainDrawerLayout != null)
            mainDrawerLayout.requestFocus();

        // 工具列位置
        changeToolbarLocation();
        changeToolbarOrder();

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            // 任務完成
            // 關閉"正在自動登入"
            TempSettings.isUnderAutoToChat = false;

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    ASProcessingDialog.dismissProcessingDialog();
                }
            };
            timer.schedule(task, 500);
        }
    }

    /** 按下精華區 */
    View.OnClickListener toEssencePageClickListener = view -> {
        _last_list_action = BoardPageAction.ESSENCE;
        PageContainer.getInstance().pushBoardEssencePage(getListName(),_board_title);
        getNavigationController().pushViewController(PageContainer.getInstance().getBoardEssencePage());
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.TAB);
    };

    /** 變更工具列位置 */
    void changeToolbarLocation() {
        LinearLayout toolbar = mainLayout.findViewById(R.id.toolbar);
        LinearLayout toolbarBlock = mainLayout.findViewById(R.id.toolbar_block);
        ToolBarFloating toolBarFloating = mainLayout.findViewById(R.id.ToolbarFloatingComponent);
        toolBarFloating.setVisibility(View.GONE);

        // 最左邊最右邊
        Button _btnLL = toolbar.findViewById(R.id.BoardPageLLButton);
        View _btnLLDivider = toolbar.findViewById(R.id.toolbar_divider_0);
        Button _btnRR = toolbar.findViewById(R.id.BoardPageRRButton);
        View _btnRRDivider = toolbar.findViewById(R.id.toolbar_divider_3);
        _btnLL.setVisibility(View.GONE);
        _btnLLDivider.setVisibility(View.GONE);
        _btnRR.setVisibility(View.GONE);
        _btnRRDivider.setVisibility(View.GONE);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        int choice_toolbar_location = UserSettings.getPropertiesToolbarLocation(); // 0-中間 1-靠左 2-靠右 3-浮動
        switch (choice_toolbar_location) {
            case 1 -> {
                // 底部-最左邊
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
                layoutParams.addRule(RelativeLayout.ALIGN_START);
                _btnRR.setVisibility(View.VISIBLE);
                _btnRRDivider.setVisibility(View.VISIBLE);
            }
            case 2 -> {
                // 底部-最右邊
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.removeRule(RelativeLayout.ALIGN_START);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                _btnLL.setVisibility(View.VISIBLE);
                _btnLLDivider.setVisibility(View.VISIBLE);
            }
            case 3 -> {
                // 浮動
                // 去除 原本工具列
                toolbar.setVisibility(View.GONE);
                // 去除底部卡位用view
                toolbarBlock.setVisibility(View.GONE);
                // 浮動工具列
                toolBarFloating.setVisibility(View.VISIBLE);
                // button setting
                toolBarFloating.setOnClickListenerSetting(mPostListener);
                Button OriginalBtn = mainLayout.findViewById(R.id.BoardPagePostButton);
                toolBarFloating.setTextSetting(OriginalBtn.getText().toString());
                // button 1
                toolBarFloating.setOnClickListener1(mPrevPageClickListener);
                toolBarFloating.setOnLongClickListener1(mFirstPageClickListener);
                toolBarFloating.setText1(getContextString(R.string.prev_page));
                // button 2
                toolBarFloating.setOnClickListener2(mLastPageClickListener);
                toolBarFloating.setText2(getContextString(R.string.last_page));
            }
            default -> {
                // 底部-中間
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }

        toolbar.setLayoutParams(layoutParams);
    }

    /** 反轉按鈕順序 */
    void changeToolbarOrder() {
        LinearLayout toolbar = mainLayout.findViewById(R.id.toolbar);

        int choice_toolbar_order = UserSettings.getPropertiesToolbarOrder();
        if (choice_toolbar_order == 1) {
            // 最左邊最右邊
            Button _btnLL = toolbar.findViewById(R.id.BoardPageLLButton);
            View _btnLLDivider = toolbar.findViewById(R.id.toolbar_divider_0);
            Button _btnRR = toolbar.findViewById(R.id.BoardPageRRButton);
            View _btnRRDivider = toolbar.findViewById(R.id.toolbar_divider_3);

            // 擷取中間的元素
            ArrayList<View> allViews = new ArrayList<>();
            for (int i = toolbar.getChildCount() - 3; i >= 2; i--) {
                View view = toolbar.getChildAt(i);
                allViews.add(view);
            }

            // 清空
            toolbar.removeAllViews();

            // 插入
            toolbar.addView(_btnLL);
            toolbar.addView(_btnLLDivider);
            for (int j = 0; j < allViews.size(); j++) {
                toolbar.addView(allViews.get(j));
            }
            toolbar.addView(_btnRRDivider);
            toolbar.addView(_btnRR);
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onMenuButtonClicked() {
        mMenuButtonListener.onClick(null);
        return true;
    }

    /** 更新headerView */
    void refreshHeaderView() {
        String board_title = _board_title;
        board_title = (board_title == null || board_title.isEmpty()) ? getContextString(R.string.loading) : board_title;
        String board_manager = _board_manager;
        board_manager = (board_manager == null || board_manager.isEmpty()) ? getContextString(R.string.loading) : board_manager;
        String board_name = getListName();
        BoardHeaderView header_view = mainLayout.findViewById(R.id.BoardPage_HeaderView);
        if (header_view != null) {
            header_view.setData(board_title, board_name, board_manager);
        }
    }

    @Override
    public String getListIdFromListName(String str) {
        return str + "[Board]";
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public TelnetListPageBlock loadPage() {
        BoardPageBlock load = BoardPageHandler.getInstance().load();
        if (!_initialed) {
            String _lastVisitBoard = TempSettings.lastVisitBoard;
            if (!_lastVisitBoard.equals(load.BoardName)) {
                // 紀錄最後瀏覽的看板
                TempSettings.lastVisitBoard = load.BoardName;
                clear();
                if (load.Type == BoardPageAction.SEARCH) {
                    pushRefreshCommand(0);
                }
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
        if (boardPageItem != null){
            // 紀錄正在看的討論串標題
            TempSettings.boardFollowTitle = boardPageItem.Title;
        }
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
        DrawerLayout drawerLayout = mainLayout.findViewById(R.id.drawer_layout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(drawerLocation)) {
            drawerLayout.closeDrawer(drawerLocation);
            return true;
        }
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1);
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

    void showSearchArticleDialog() {
        DialogSearchArticle dialog_SearchArticle = new DialogSearchArticle();
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

    /** 搜尋文章 */
    void searchArticle(String _keyword, String _author, String _mark, String _gy) {
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

    /** 選擇文章 */
    @Override // com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener
    public void onSelectDialogDismissWIthIndex(String str) {
        int i;
        try {
            i = Integer.parseInt(str) - 1;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
            i = -1;
        }
        if (i >= 0) {
            setListViewSelection(i);
        }
    }

    public void onBookmarkButtonClicked() {
        getNavigationController().pushViewController(new BookmarkManagePage(getListName(), this));
    }

    /** 長按串接文章 */
    void onListArticle(int i) {
        _last_list_action = BoardPageAction.LINK_TITLE;
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
        if (UserSettings.getPropertiesGestureOnBoardEnable()) {
            if (isDrawerOpen() || _isDrawerOpening) {
                return false;
            }
            onBackPressed();
            return true;
        }
        return true;
    }

    /** 發文 */
    protected void onPostButtonClicked() {
        PostArticlePage postArticlePage = PageContainer.getInstance().getPostArticlePage();
        postArticlePage.setBoardPage(this);
        postArticlePage.setListener(this);
        getNavigationController().pushViewController(postArticlePage);
    }

    /** 按下推薦文章 */
    public void goodLoadingArticle() {
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.do_gy))
                .setMessage(getContextString(R.string.gy_this_article))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.do_gy))
                .setListener((aSAlertDialog, i2) -> {
                    if (i2 == 1) {
                        BoardMainPage.this.pushCommand(new BahamutCommandGoodArticle(getLoadingItemNumber()));
                    }
                }).scheduleDismissOnPageDisappear(this).show();
    }

    /** 按下推文 */
    public void pushArticle() {
        BoardMainPage.this.pushCommand(new BahamutCommandPushArticle(getLoadingItemNumber()));

        pushArticleAsRunner.cancel();
        pushArticleAsRunner.postDelayed(2000);
        isPostDelayedSuccess = false;
    }
    /** 開啟推文小視窗 */
    public void openPushArticleDialog() {
        pushArticleAsRunner.cancel();
        isPostDelayedSuccess = true;

        DialogPushArticle dialog = new DialogPushArticle();
        dialog.show();
    }
    /** 沒有開啟推文小視窗, 視為沒開放功能 */
    ASRunner pushArticleAsRunner = new ASRunner(){
        @Override
        public void run() {
            if (!isPostDelayedSuccess) {
                onPagePreload();
                ASToast.showLongToast("沒反應，看板未開放推文");
            }
        }
    };
    /** 提供給 stateHandler 的取消介面 */
    public void cancelRunner() {
        pushArticleAsRunner.cancel();
        isPostDelayedSuccess = true;
    }

    /** 轉寄至信箱 */
    public void FSendMail() {
        pushCommand(new BahamutCommandFSendMail(UserSettings.getPropertiesUsername()));
    }

    /** 最前篇 */
    public void loadTheSameTitleTop() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleTop(getLoadingItemNumber()));
    }

    /** 最後篇 */
    public void loadTheSameTitleBottom() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleBottom(getLoadingItemNumber()));
    }

    /** 上一篇 */
    public void loadTheSameTitleUp() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleUp(getLoadingItemNumber()));
    }

    /** 下一篇 */
    public void loadTheSameTitleDown() {
        onLoadItemStart();
        pushCommand(new BahamutCommandTheSameTitleDown(getLoadingItemNumber()));
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    public boolean isItemBlocked(TelnetListPageItem telnetListPageItem) {
        if (telnetListPageItem != null) {
            return blockListEnable && UserSettings.isBlockListContains(((BoardPageItem) telnetListPageItem).Author);
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

    /** 啟用/停用 黑名單 */
    void onChangeBlockStateButtonClicked() {
        UserSettings.setPropertiesBlockListEnable(!blockListEnable);
        UserSettings.notifyDataUpdated();
        blockListEnable = UserSettings.getPropertiesBlockListEnable();
        if (mainDrawerLayout!=null) {
            CheckBox block_enable_checkbox = mainDrawerLayout.findViewById(R.id.block_enable_button_checkbox);
            block_enable_checkbox.setChecked(blockListEnable);
        }
        reloadListView();
    }

    void onEditBlockListButtonClicked() {
        getNavigationController().pushViewController(new BlockListPage());
    }

    /** 點下文章 */
    @Override
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

    /** 書籤管理->按下書籤 */
    @Override // com.kota.Bahamut.BookmarkPage.BoardExtendOptionalPageListener
    public void onBoardExtendOptionalPageDidSelectBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            _last_list_action = BoardPageAction.SEARCH;
            pushCommand(new BahamutCommandSearchArticle(bookmark.getKeyword(), bookmark.getAuthor(), bookmark.getMark(), bookmark.getGy()));
        }
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, android.widget.Adapter
    public View getView(int index, View view, ViewGroup viewGroup) {
        int item_index = index + 1;
        int block = ItemUtils.getBlock(item_index);
        BoardPageItem boardPageItem = (BoardPageItem) getItem(index);
        if (boardPageItem == null && getCurrentBlock() != block && !isLoadingBlock(item_index)) {
            loadBoardBlock(block);
        }
        if (view == null) {
            view = new BoardPageItemView(getContext());
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        BoardPageItemView boardPageItemView = (BoardPageItemView) view;
        boardPageItemView.setItem(boardPageItem);
        boardPageItemView.setNumber(item_index);
        if (boardPageItem != null && blockListEnable) {
            if (UserSettings.isBlockListContains(boardPageItem.Author)) {
                boardPageItemView.setVisible(false);
            } else if (blockListForTitle && UserSettings.isBlockListContains(boardPageItem.Title)) {
                boardPageItemView.setVisible(false);
            } else {
                boardPageItemView.setVisible(true);
            }
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
    /** 發表文章/回覆文章 訊息發出 */
    @Override // com.kota.Bahamut.Pages.PostArticlePage_Listener
    public void onPostDialogSendButtonClicked(PostArticlePage postArticlePage, String str, String str2, String str3, String str4, String str5, Boolean boolean6) {
        pushCommand(new BahamutCommandPostArticle(this, str, str2, str3, str4, str5, boolean6));
        // 回應到作者信箱
        if (str3!= null && str3.equals("M")) {
            return;
        }
        // 發文中等待視窗
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

        ASProcessingDialog.showProcessingDialog(getContextString(R.string.board_page_post_waiting_message_1));

    }

    /** 引言過多, 回逤發文時的設定 */
    public void recoverPost() {
        new ASRunner() { 
            @Override 
            public void run() {
                cleanCommand(); // 清除引言過多留下的command buffer
                PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
                page.setRecover();
            }
        }.runInMainThread();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        ASProcessingDialog.dismissProcessingDialog();
    }
    /** 完成發文 */
    public void finishPost() {
        new ASRunner() { 
            @Override 
            public void run() {
                PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
                if (page!=null)
                    page.closeArticle();
            }
        }.runInMainThread();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        ASProcessingDialog.dismissProcessingDialog();
    }
    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidAppear() {
        super.onPageDidAppear();
        _bookmark_adapter.notifyDataSetChanged();
        _history_adapter.notifyDataSetChanged();
    }

    void reloadBookmark() {
        String listName = getListName();
        Context context = getContext();
        if (context == null) {
            return;
        }
        BookmarkStore store = TempSettings.bookmarkStore;
        if (store!=null) {
            if (_mode == 0) {
                store.getBookmarkList(listName).loadBookmarkList(_bookmarkList);
                if (isPageAppeared()) {
                    _bookmark_adapter.notifyDataSetChanged();
                }
            } else {
                store.getBookmarkList(listName).loadHistoryList(_bookmarkList);
                if (isPageAppeared()) {
                    _history_adapter.notifyDataSetChanged();
                }
            }
        }
        if (_bookmarkList.size()==0) {
            _drawerListView.setVisibility(View.GONE);
            _drawerListViewNone.setVisibility(View.VISIBLE);
        } else {
            _drawerListView.setVisibility(View.VISIBLE);
            _drawerListViewNone.setVisibility(View.GONE);
        }
    }

    void closeDrawer() {
        DrawerLayout drawerLayout = mainLayout.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    /** 側邊選單已開啟 或 正在開啟中 */
    boolean isDrawerOpen() {
        DrawerLayout drawerLayout = mainLayout.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            return drawerLayout.isDrawerOpen(drawerLocation);
        }
        return false;
    }

    @Override
    public void onSearchDialogCancelButtonClicked() {

    }
}