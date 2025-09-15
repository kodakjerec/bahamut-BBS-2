package com.kota.Bahamut.Pages.BoardPage;

import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.CommonFunctions.rgbToInt

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.annotation.NonNull
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASListDialog
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASListView
import com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.Command.BahamutCommandEditArticle
import com.kota.Bahamut.Command.BahamutCommandFSendMail
import com.kota.Bahamut.Command.BahamutCommandGoodArticle
import com.kota.Bahamut.Command.BahamutCommandListArticle
import com.kota.Bahamut.Command.BahamutCommandLoadArticle
import com.kota.Bahamut.Command.BahamutCommandPostArticle
import com.kota.Bahamut.Command.BahamutCommandPushArticle
import com.kota.Bahamut.Command.BahamutCommandSearchArticle
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleBottom
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleDown
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleTop
import com.kota.Bahamut.Command.BahamutCommandTheSameTitleUp
import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.DataModels.BookmarkStore
import com.kota.Bahamut.Dialogs.DialogPushArticle
import com.kota.Bahamut.Dialogs.DialogSearchArticle
import com.kota.Bahamut.Dialogs.DialogSearchArticleListener
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle
import com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener
import com.kota.Bahamut.ListPage.ListState
import com.kota.Bahamut.ListPage.ListStateStore
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.ListPage.TelnetListPageItem
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage
import com.kota.Bahamut.Pages.BlockListPage.BlockListPage
import com.kota.Bahamut.Pages.BookmarkPage.BoardExtendOptionalPageListener
import com.kota.Bahamut.Pages.BookmarkPage.BookmarkManagePage
import com.kota.Bahamut.Pages.Model.BoardPageBlock
import com.kota.Bahamut.Pages.Model.BoardPageHandler
import com.kota.Bahamut.Pages.Model.BoardPageItem
import com.kota.Bahamut.Pages.Model.ToolBarFloating
import com.kota.Bahamut.Pages.PostArticlePage
import com.kota.Bahamut.Pages.PostArticlePage_Listener
import com.kota.Bahamut.Pages.Theme.Theme
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.Pages.Theme.ThemeStore
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Logic.ItemUtils
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TextView.TelnetTextViewLarge

import java.util.ArrayList
import java.util.List
import java.util.Objects
import java.util.Timer
import java.util.TimerTask
import java.util.Vector

class BoardMainPage : TelnetListPage()() implements DialogSearchArticleListener, Dialog_SelectArticle_Listener, PostArticlePage_Listener, BoardExtendOptionalPageListener, ASListViewExtentOptionalDelegate {
    var mainDrawerLayout: DrawerLayout
    var mainLayout: RelativeLayout
    protected var _board_title: String = null;
    protected var _board_manager: String = null;
    var _last_list_action: Int = BoardPageAction.LIST;
    var _initialed: Boolean = false;
    var _refresh_header_view: Boolean = false; // 正在更新標題列
    var blockListEnable: Boolean = false; // 是否啟用黑名單
    var blockListForTitle: Boolean = false; // 是否啟用黑名單套用至標題
    var _isDrawerOpening: Boolean = false; // 側邊選單正在開啟中
    val var List<Bookmark>: _bookmarkList: = ArrayList<>();
    var _drawerListView: ListView
    var _drawerListViewNone: TextView
    var _mode: Int = 0; // 現在開啟的是 0-書籤 1-紀錄
    Array<Button> _tab_buttons;
    var _show_bookmark_button: Button // 顯示書籤按鈕
    var _show_history_button: Button // 顯示記錄按鈕
    var drawerLocation: Int = GravityCompat.END; // 抽屜最後位置
    private var isPostDelayedSuccess: Boolean = false;

    /** 發文 */
    val var View.OnClickListener: mPostListener: = view -> BoardMainPage.onPostButtonClicked();

    /** 最前頁 */
    val var View.OnLongClickListener: mFirstPageClickListener: = view -> {
        BoardMainPage.moveToFirstPosition();
        var true: return
    }
    /** 前一頁 */
    val var View.OnClickListener: mPrevPageClickListener: = view -> {
        var firstIndex: Int = _list_view.getFirstVisiblePosition();
        var endIndex: Int = _list_view.getLastVisiblePosition();
        var moveIndex: Int = Math.abs(endIndex-firstIndex);
        var -: firstIndex = moveIndex;
        if (firstIndex < 0)
            firstIndex = 0;
        setListViewSelection(firstIndex);
    };
    /** 下一頁 */
    private val var Array<Int>: lastEndIndexes: = Int[3]; // 最後頁的結束位置
    private var endIndexCheckCount: Int = 0; // 結束位置檢查次數
    val var View.OnClickListener: mNextPageClickListener: = view -> {
        var firstIndex: Int = _list_view.getFirstVisiblePosition();
        var endIndex: Int = _list_view.getLastVisiblePosition();
        var moveIndex: Int = Math.abs(endIndex-firstIndex);
        var +: firstIndex = moveIndex;

        if (endIndexCheckCount > 0 var endIndex: && == lastEndIndexes[endIndexCheckCount - 1]) {
            lastEndIndexes[endIndexCheckCount] = endIndex;
            endIndexCheckCount++;

            if var >: (endIndexCheckCount = 3) {
                // Reset counter
                endIndexCheckCount = 0;
                // Move to last position
                BoardMainPage.setManualLoadPage();
                BoardMainPage.moveToLastPosition();
                return;
            }
        } else {
            // Reset counter if endIndex changed
            endIndexCheckCount = 1;
        }

        // Store current endIndex
        lastEndIndexes[0] = endIndex;

        setListViewSelection(firstIndex);
    };

    /** 最後頁 */
    val var View.OnClickListener: mLastPageClickListener: = view -> {
        BoardMainPage.setManualLoadPage();
        BoardMainPage.moveToLastPosition();
    };
    val var View.OnLongClickListener: mLastPageLongClickListener: = view -> {
        BoardMainPage.setManualLoadPage();
        BoardMainPage.moveToLastPosition();
        var true: return
    }

    /** 彈出側邊選單 */
    val var View.OnClickListener: mMenuButtonListener: = view -> {
        var drawerLayout: DrawerLayout = (DrawerLayout) BoardMainPage.findViewById(R.id.drawer_layout);
        if var !: (drawerLayout = null) {
            if (drawerLayout.isDrawerOpen(drawerLocation)) {
                drawerLayout.closeDrawer(drawerLocation);
            } else {
                var (UserSettings.getPropertiesDrawerLocation(): if == 0) {
                    drawerLocation = GravityCompat.END;
                } else {
                    drawerLocation = GravityCompat.START;
                }
                var menu_view: LinearLayout = mainDrawerLayout.findViewById(R.id.menu_view);
                var layoutParams_drawer: DrawerLayout.LayoutParams = (DrawerLayout.LayoutParams) menu_view.getLayoutParams();
                layoutParams_drawer.gravity = drawerLocation;
                menu_view.setLayoutParams(layoutParams_drawer);
                drawerLayout.openDrawer(drawerLocation, UserSettings.getPropertiesAnimationEnable());
            }
        }
    };
    /** 跳出小視窗 全部已讀/全部未讀 */
    val var View.OnClickListener: mReadAllListener: = view -> {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.String._article))
                .addItem(getContextString(R.String.board_main_read_all))
                .addItem(getContextString(R.String.board_main_unread_all))
                .setListener(ASListDialogItemClickListener() {
                    onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                        var true: return
                    }

                    onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                        if (Objects == aTitle, getContextString(R.String.board_main_read_all)) {
                            var data: Array<Byte> = TelnetOutputBuilder.create()
                                    .pushString("vV\n")
                                    .build();
                            TelnetClient.getClient().sendDataToServer(data);
                            ASToast.showShortToast(getContextString(R.String.board_main_read_all_msg01));
                        } else if (Objects == aTitle, getContextString(R.String.board_main_unread_all)) {
                            var data: Array<Byte> = TelnetOutputBuilder.create()
                                    .pushString("vU\n")
                                    .build();
                            TelnetClient.getClient().sendDataToServer(data);
                            ASToast.showShortToast(getContextString(R.String.board_main_unread_all_msg01));
                        }
                    }
                }).show();
    };

    var _bookmark_adapter: BaseAdapter = BaseAdapter() { 
        @Override // android.widget.Adapter
        getItemId(Int i): Long {
            var i: return
        }

        @Override // android.widget.Adapter
        getCount(): Int {
            return BoardMainPage._bookmarkList.size()
        }

        @Override // android.widget.Adapter
        getItem(Int i): Bookmark {
            return BoardMainPage._bookmarkList.get(i);
        }

        /** 顯示側邊選單書籤 */
        @Override // android.widget.Adapter
        getView(Int i, View view, ViewGroup viewGroup): View {
            var (view: if == null) {
                view = BoardExtendBookmarkItemView(BoardMainPage.getContext());
            }
            var boardExtendBookmarkItemView: BoardExtendBookmarkItemView = (BoardExtendBookmarkItemView) view;
            boardExtendBookmarkItemView.setBookmark(getItem(i));
            boardExtendBookmarkItemView.setDividerTopVisible(i == 0);
            var view: return
        }
    }

    var _history_adapter: BaseAdapter = BaseAdapter() {
        @Override // android.widget.Adapter
        getItemId(Int i): Long {
            var i: return
        }

        @Override // android.widget.Adapter
        getCount(): Int {
            return BoardMainPage._bookmarkList.size()
        }

        @Override // android.widget.Adapter
        getItem(Int i): Bookmark {
            return BoardMainPage._bookmarkList.get(i);
        }

        /** 顯示側邊選單書籤 */
        @Override // android.widget.Adapter
        getView(Int i, View view, ViewGroup viewGroup): View {
            var (view: if == null) {
                view = BoardExtendHistoryItemView(BoardMainPage.getContext());
            }
            var boardExtendHistoryItemView: BoardExtendHistoryItemView = (BoardExtendHistoryItemView) view;
            boardExtendHistoryItemView.setBookmark(getItem(i));
            boardExtendHistoryItemView.setDividerTopVisible(i == 0);
            var view: return
        }
    }

    /** 側邊選單 mListener */
    var _drawer_listener: DrawerLayout.DrawerListener = DrawerLayout.DrawerListener() {
        @Override
        onDrawerSlide(@NonNull View drawerView, Float slideOffset): Unit { }

        @Override
        onDrawerOpened(@NonNull View drawerView): Unit { }

        @Override
        onDrawerClosed(@NonNull View drawerView): Unit { }

        @Override
        onDrawerStateChanged(Int newState): Unit {
            _isDrawerOpening = var !: newState = DrawerLayout.STATE_IDLE;

            // 側邊選單未完全開啟 or 正要啟動狀態
            if ( !isDrawerOpen() || _isDrawerOpening) {
                BoardMainPage.reloadBookmark();
            }
        }
    };

    /** 點書籤 */
    var _bookmark_listener: AdapterView.OnItemClickListener = (adapterView, view, i, j) -> {
        BoardMainPage.closeDrawer();
        var bookmark: Bookmark = BoardMainPage._bookmarkList.get(i);
        searchArticle(bookmark.getKeyword(), bookmark.getAuthor(), bookmark.getMark(), bookmark.getGy());
    };

    /** 切換成書籤清單 */
    var buttonClickListener: View.OnClickListener = View.OnClickListener() {

        @Override
        onClick(View aView): Unit {
            var (aView: if == _show_bookmark_button) {
                _mode = 0;
            } else {
                _mode = 1;
            }
            if var !: (_drawerListView = null) {
                var (_mode: if == 0)
                    _drawerListView.setAdapter(_bookmark_adapter);
                else
                    _drawerListView.setAdapter(_history_adapter);
                var (_drawerListView.getOnItemClickListener(): if == null)
                    _drawerListView.setOnItemClickListener(_bookmark_listener);
            }
            reloadBookmark();

            // 切換頁籤
            var theme: Theme = ThemeStore.INSTANCE.getSelectTheme();
            for (Button tab_button : BoardMainPage._tab_buttons) {
                var (tab_button: if == aView) {
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
    var _search_listener: View.OnClickListener = view -> {
        BoardMainPage.closeDrawer();
        BoardMainPage.showSearchArticleDialog();
    };

    /** 選擇文章 */
    var _select_listener: View.OnClickListener = view -> {
        BoardMainPage.closeDrawer();
        BoardMainPage.showSelectArticleDialog();
    };

    /** 啟用/停用 黑名單 */
    var _enable_block_listener: View.OnClickListener = view -> BoardMainPage.onChangeBlockStateButtonClicked();

    /** 修改黑名單 */
    var _edit_block_listener: View.OnClickListener = view -> {
        BoardMainPage.closeDrawer();
        BoardMainPage.onEditBlockListButtonClicked();
    };

    /** 開啟書籤管理 */
    var _edit_bookmark_listener: View.OnClickListener = view -> {
        BoardMainPage.closeDrawer();
        BoardMainPage.onBookmarkButtonClicked();
    };
    /** 靠左對其 */
    var _btnLL_listener: View.OnClickListener = view -> {
        UserSettings.setPropertiesToolbarLocation(1);
        BoardMainPage.changeToolbarLocation();
    };
    /** 靠右對其 */
    var _btnRR_listener: View.OnClickListener = view -> {
        UserSettings.setPropertiesToolbarLocation(2);
        BoardMainPage.changeToolbarLocation();
    };

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    getPageLayout(): Int {
        return R.layout.board_page;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    getPageType(): Int {
        return BahamutPage.BAHAMUT_BOARD;
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    isAutoLoadEnable(): Boolean {
        var true: return
    }

    @Override // com.kota.ASFramework.UI.ASListViewExtentOptionalDelegate
    onASListViewHandleExtentOptional(ASListView aSListView, Int i): Boolean {
        var false: return
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    onPageDidLoad(): Unit {
        super.onPageDidLoad()

        mainDrawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout);
        mainLayout = findViewById<RelativeLayout>(R.id.content_view); 

        var aSListView: ASListView = mainLayout.findViewById(R.id.BoardPageListView);
        aSListView.extendOptionalDelegate = this;
        aSListView.setEmptyView(mainLayout.findViewById(R.id.BoardPageListEmptyView));
        setListView(aSListView);

        mainLayout.findViewById(R.id.BoardPagePostButton).setOnClickListener(mPostListener);
        mainLayout.findViewById(R.id.BoardPageFirstPageButton).setOnClickListener(mPrevPageClickListener);
        mainLayout.findViewById(R.id.BoardPageFirstPageButton).setOnLongClickListener(mFirstPageClickListener);
        // 下一頁
        var boardPageLatestPageButton: Button = mainLayout.findViewById(R.id.BoardPageLatestPageButton);
        if (UserSettings.getPropertiesBoardMoveEnable()>0) {
            boardPageLatestPageButton.setText(getContextString(R.String.next_page));
            boardPageLatestPageButton.setOnClickListener(mNextPageClickListener);
            boardPageLatestPageButton.setOnLongClickListener(mLastPageLongClickListener);
        } else {
            boardPageLatestPageButton.setOnClickListener(mLastPageClickListener);
        }
        mainLayout.findViewById(R.id.BoardPageLLButton).setOnClickListener(_btnLL_listener);
        mainLayout.findViewById(R.id.BoardPageRRButton).setOnClickListener(_btnRR_listener);

        // 側邊選單
        var (mainDrawerLayout!: if =null) {
            // 替換外觀
            ThemeFunctions().layoutReplaceTheme(mainDrawerLayout.findViewById(R.id.menu_view));

            var drawerLayout: DrawerLayout = mainDrawerLayout.findViewById(R.id.drawer_layout);
            if var !: (drawerLayout = null) {
                var menu_view: LinearLayout = mainDrawerLayout.findViewById(R.id.menu_view);
                var layoutParams_drawer: DrawerLayout.LayoutParams = (DrawerLayout.LayoutParams) menu_view.getLayoutParams();
                layoutParams_drawer.gravity = drawerLocation;
                menu_view.setLayoutParams(layoutParams_drawer);
                drawerLayout.addDrawerListener(_drawer_listener);
                // 根據手指位置設定側邊選單位置
                aSListView.setOnTouchListener((view, motionEvent) -> {
                    if (_isDrawerOpening)
                        var false: return
                    var screenWidth: Int = getContext().getResources().getDisplayMetrics().widthPixels/2;
                    if (motionEvent.getX()<screenWidth)
                        layoutParams_drawer.gravity = GravityCompat.START;
                    else
                        layoutParams_drawer.gravity = GravityCompat.END;
                    drawerLocation = layoutParams_drawer.gravity;
                    menu_view.setLayoutParams(layoutParams_drawer);
                    var false: return
                })
            }
            var search_article_button: View = mainDrawerLayout.findViewById(R.id.search_article_button);
            if var !: (search_article_button = null) {
                search_article_button.setOnClickListener(_search_listener);
            }
            var select_article_button: View = mainDrawerLayout.findViewById(R.id.select_article_button);
            if var !: (select_article_button = null) {
                select_article_button.setOnClickListener(_select_listener);
            }
            var block_enable_checkbox: CheckBox = mainDrawerLayout.findViewById(R.id.block_enable_button_checkbox);
            if var !: (block_enable_checkbox = null) {
                block_enable_checkbox.setChecked(UserSettings.getPropertiesBlockListEnable());
                block_enable_checkbox.setOnClickListener(_enable_block_listener);

                var text_checkbox: TelnetTextViewLarge = mainDrawerLayout.findViewById(R.id.block_enable_button_checkbox_label);
                text_checkbox.setOnClickListener(_enable_block_listener);
            }
            var block_setting_button: View = mainDrawerLayout.findViewById(R.id.block_setting_button);
            if var !: (block_setting_button = null) {
                block_setting_button.setOnClickListener(_edit_block_listener);
            }
            var bookmark_edit_button: View = mainDrawerLayout.findViewById(R.id.bookmark_edit_button);
            if var !: (bookmark_edit_button = null) {
                bookmark_edit_button.setOnClickListener(_edit_bookmark_listener);
            }
            // 側邊選單內的書籤
            _drawerListView = mainDrawerLayout.findViewById(R.id.bookmark_list_view);
            _drawerListViewNone = mainDrawerLayout.findViewById(R.id.bookmark_list_view_none);
            if var !: (_drawerListView = null) {
                _show_bookmark_button = mainDrawerLayout.findViewById(R.id.show_bookmark_button);
                _show_history_button = mainDrawerLayout.findViewById(R.id.show_history_button);
                _tab_buttons = Array<Button>{_show_bookmark_button, _show_history_button};
                _show_bookmark_button.setOnClickListener(buttonClickListener);
                _show_history_button.setOnClickListener(buttonClickListener);
                var (_mode: if == 0)
                    _show_bookmark_button.performClick();
                else
                    _show_history_button.performClick();
            }
            mainDrawerLayout.findViewById(R.id.bookmark_tab_button).setOnClickListener( toEssencePageClickListener );
        }

        // 標題
        var boardPageHeaderView: BoardHeaderView = mainLayout.findViewById(R.id.BoardPage_HeaderView);
        var (getPageType(): if ==BahamutPage.BAHAMUT_BOARD) {
            boardPageHeaderView.setMenuButtonClickListener(mMenuButtonListener);
            boardPageHeaderView.setDetail1ClickListener(mReadAllListener);
        } else {
            boardPageHeaderView.setMenuButtonClickListener(null);
        }
        refreshHeaderView();

        blockListEnable = UserSettings.getPropertiesBlockListEnable();
        blockListForTitle = UserSettings.getPropertiesBlockListForTitle();

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));

        // 解決android 14跳出軟鍵盤
        // 先把 focus 設定到其他目標物, 避免系統在回收過程一個個去 focus
        // keyword: clearFocusInternal
        if var !: (mainDrawerLayout = null)
            mainDrawerLayout.requestFocus();

        // 工具列位置
        changeToolbarLocation();
        changeToolbarOrder();

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {

            // 任務完成
            // 關閉"正在自動登入"
            TempSettings.isUnderAutoToChat = false;

            var timer: Timer = Timer();
            var task1: TimerTask = TimerTask() {
                @Override
                run(): Unit {
                    ASProcessingDialog.dismissProcessingDialog();
                }
            };
            var task2: TimerTask = TimerTask() {
                @Override
                run(): Unit {
                    // 跳到指定文章編號
                    if (TempSettings.lastVisitArticleNumber>0) {
                        onSelectDialogDismissWIthIndex(String.valueOf(TempSettings.lastVisitArticleNumber));
                    }
                }
            };
            timer.schedule(task1, 500);
            timer.schedule(task2, 500);
        }
    }

    /** 按下精華區 */
    var toEssencePageClickListener: View.OnClickListener = view -> {
        _last_list_action = BoardPageAction.ESSENCE;
        PageContainer.getInstance().pushBoardEssencePage(getListName(),_board_title);
        getNavigationController().pushViewController(PageContainer.getInstance().getBoardEssencePage());
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.TAB);
    };

    /** 變更工具列位置 */
    Unit changeToolbarLocation() {
        var toolbar: LinearLayout = mainLayout.findViewById(R.id.toolbar);
        var toolbarBlock: LinearLayout = mainLayout.findViewById(R.id.toolbar_block);
        var toolBarFloating: ToolBarFloating = mainLayout.findViewById(R.id.ToolbarFloatingComponent);
        toolBarFloating.setVisibility(View.GONE);

        // 最左邊最右邊
        var _btnLL: Button = toolbar.findViewById(R.id.BoardPageLLButton);
        var _btnLLDivider: View = toolbar.findViewById(R.id.toolbar_divider_0);
        var _btnRR: Button = toolbar.findViewById(R.id.BoardPageRRButton);
        var _btnRRDivider: View = toolbar.findViewById(R.id.toolbar_divider_3);
        _btnLL.setVisibility(View.GONE);
        _btnLLDivider.setVisibility(View.GONE);
        _btnRR.setVisibility(View.GONE);
        _btnRRDivider.setVisibility(View.GONE);

        var layoutParams: RelativeLayout.LayoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        var choice_toolbar_location: Int = UserSettings.getPropertiesToolbarLocation(); // 0-中間 1-靠左 2-靠右 3-浮動
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
                var OriginalBtn: Button = mainLayout.findViewById(R.id.BoardPagePostButton);
                toolBarFloating.setTextSetting(OriginalBtn.getText().toString());
                // button 1
                toolBarFloating.setOnClickListener1(mPrevPageClickListener);
                toolBarFloating.setOnLongClickListener1(mFirstPageClickListener);
                toolBarFloating.setText1(getContextString(R.String.prev_page));
                // button 2
                if (UserSettings.getPropertiesBoardMoveEnable()>0) {
                    toolBarFloating.setOnClickListener2(mNextPageClickListener);
                    toolBarFloating.setOnLongClickListener2(mLastPageLongClickListener);
                    toolBarFloating.setText2(getContextString(R.String.next_page));
                } else {
                    toolBarFloating.setOnClickListener2(mLastPageClickListener);
                    toolBarFloating.setText2(getContextString(R.String.last_page));
                }
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
    Unit changeToolbarOrder() {
        var toolbar: LinearLayout = mainLayout.findViewById(R.id.toolbar);

        var choice_toolbar_order: Int = UserSettings.getPropertiesToolbarOrder();
        var (choice_toolbar_order: if == 1) {
            // 最左邊最右邊
            var _btnLL: Button = toolbar.findViewById(R.id.BoardPageLLButton);
            var _btnLLDivider: View = toolbar.findViewById(R.id.toolbar_divider_0);
            var _btnRR: Button = toolbar.findViewById(R.id.BoardPageRRButton);
            var _btnRRDivider: View = toolbar.findViewById(R.id.toolbar_divider_3);

            // 擷取中間的元素
            var allViews: ArrayList<View> = ArrayList<>();
            for var i: (Int = toolbar.getChildCount() - 3; var >: i = 2; i--) {
                var view: View = toolbar.getChildAt(i);
                allViews.add(view);
            }

            // 清空
            toolbar.removeAllViews();

            // 插入
            toolbar.addView(_btnLL);
            toolbar.addView(_btnLLDivider);
            for var j: (Int = 0; j < allViews.size(); j++) {
                toolbar.addView(allViews.get(j));
            }
            toolbar.addView(_btnRRDivider);
            toolbar.addView(_btnRR);
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected fun onMenuButtonClicked(): Boolean {
        mMenuButtonListener.onClick(null);
        var true: return
    }

    /** 更新headerView */
    Unit refreshHeaderView() {
        var board_title: String = _board_title;
        board_title var (board_title: = == null || board_title.isEmpty()) ? getContextString(R.String.loading) : board_title;
        var board_manager: String = _board_manager;
        board_manager var (board_manager: = == null || board_manager.isEmpty()) ? getContextString(R.String.loading) : board_manager;
        var board_name: String = getListName();
        var header_view: BoardHeaderView = mainLayout.findViewById(R.id.BoardPage_HeaderView);
        if var !: (header_view = null) {
            header_view.setData(board_title, board_name, board_manager);
        }
    }

    @Override
    getListIdFromListName(String str): String {
        return str + "[Board]";
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    loadPage(): TelnetListPageBlock {
        var load: BoardPageBlock = BoardPageHandler.getInstance().load();
        if (!_initialed) {
            var _lastVisitBoard: String = TempSettings.lastVisitBoard;
            if (!_lastVisitBoard == load.BoardName) {
                // 紀錄最後瀏覽的看板
                TempSettings.lastVisitBoard = load.BoardName;
                clear();
                var (load.Type: if == BoardPageAction.SEARCH) {
                    pushRefreshCommand(0);
                }
            }
            _board_manager = load.BoardManager;
            _board_title = load.BoardTitle;
            setListName(load.BoardName);
            _refresh_header_view = true;
            _initialed = true;
        }
        var load: return
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    isItemCanLoadAtIndex(Int i): Boolean {
        var boardPageItem: BoardPageItem = (BoardPageItem) getItem(i);
        if var !: (boardPageItem = null){
            // 紀錄正在看的討論串標題
            TempSettings.boardFollowTitle = boardPageItem.Title;
            TempSettings.lastVisitArticleNumber = boardPageItem.Number;
        }
        var (boardPageItem: if == null || !boardPageItem.isDeleted) {
            var true: return
        }
        ASToast.showShortToast("此文章已被刪除")
        var false: return
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    public synchronized Unit onPageRefresh() {
        super.onPageRefresh()
        if (_refresh_header_view) {
            refreshHeaderView();
            _refresh_header_view = false;
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected fun onBackPressed(): Boolean {
        var drawerLayout: DrawerLayout = mainLayout.findViewById(R.id.drawer_layout);
        if var !: (drawerLayout = null && drawerLayout.isDrawerOpen(drawerLocation)) {
            drawerLayout.closeDrawer(drawerLocation);
            var true: return
        }
        clear()
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1);
        PageContainer.getInstance().cleanBoardPage();
        var true: return
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    protected fun onListViewItemLongClicked(View view, Int i): Boolean {
        onListArticle(i + 1)
        var true: return
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected fun onSearchButtonClicked(): Boolean {
        showSearchArticleDialog()
        var true: return
    }

    Unit showSearchArticleDialog() {
        var dialog_SearchArticle: DialogSearchArticle = DialogSearchArticle();
        dialog_SearchArticle.setListener(this);
        dialog_SearchArticle.show();
    }

    protected fun showSelectArticleDialog(): Unit {
        var dialog_SelectArticle: Dialog_SelectArticle = Dialog_SelectArticle();
        dialog_SelectArticle.setListener(this);
        dialog_SelectArticle.show();
    }

    @Override // com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener
    onSearchDialogSearchButtonClickedWithValues(Vector<String> vector): Unit {
        searchArticle(vector.get(0), vector.get(1), Objects == vector.get(2, "YES") ? "y" : "n", vector.get(3));
    }

    /** 搜尋文章 */
    Unit searchArticle(String _keyword, String _author, String _mark, String _gy) {
        _last_list_action = BoardPageAction.SEARCH;
        var board_Search_Page: BoardSearchPage = PageContainer.getInstance().getBoardSearchPage();
        board_Search_Page.clear();
        getNavigationController().pushViewController(board_Search_Page);
        var state: ListState = ListStateStore.getInstance().getState(board_Search_Page.getListIdFromListName(getListName()));
        if var !: (state = null) {
            state.Top = 0;
            state.Position = 0;
        }
        board_Search_Page.setKeyword(_keyword);
        board_Search_Page.setAuthor(_author);
        board_Search_Page.setMark(_mark);
        board_Search_Page.setGy(_gy);
        pushCommand(BahamutCommandSearchArticle(_keyword, _author, _mark, _gy));
    }

    /** 選擇文章 */
    @Override // com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener
    onSelectDialogDismissWIthIndex(String str): Unit {
        var i: Int
        try {
            i = Integer.parseInt(str) - 1;
        } catch (Exception e) {
            var e.getMessage()!: Log.e(getClass().getSimpleName(), =null?e.getMessage():"");
            i = -1;
        }
        if var >: (i = 0) {
            setListViewSelection(i);
        }
    }

    onBookmarkButtonClicked(): Unit {
        getNavigationController().pushViewController(BookmarkManagePage(getListName(), this));
    }

    /** 長按串接文章 */
    Unit onListArticle(Int i) {
        _last_list_action = BoardPageAction.LINK_TITLE;
        var board_Linked_Title_Page: BoardLinkPage = PageContainer.getInstance().getBoardLinkedTitlePage();
        board_Linked_Title_Page.clear();
        getNavigationController().pushViewController(board_Linked_Title_Page);
        var state: ListState = ListStateStore.getInstance().getState(board_Linked_Title_Page.getListIdFromListName(getListName()));
        if var !: (state = null) {
            state.Top = 0;
            state.Position = 0;
        }
        pushCommand(BahamutCommandListArticle(i));
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    onReceivedGestureRight(): Boolean {
        if (UserSettings.getPropertiesGestureOnBoardEnable()) {
            if (isDrawerOpen() || _isDrawerOpening) {
                var false: return
            }
            onBackPressed()
            var true: return
        }
        var true: return
    }

    /** 發文 */
    protected fun onPostButtonClicked(): Unit {
        var postArticlePage: PostArticlePage = PageContainer.getInstance().getPostArticlePage();
        postArticlePage.setBoardPage(this);
        postArticlePage.setListener(this);
        getNavigationController().pushViewController(postArticlePage);
    }

    /** 按下推薦文章 */
    goodLoadingArticle(): Unit {
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.String.do_gy))
                .setMessage(getContextString(R.String.gy_this_article))
                .addButton(getContextString(R.String.cancel))
                .addButton(getContextString(R.String.do_gy))
                .setListener((aSAlertDialog, i2) -> {
                    var (i2: if == 1) {
                        BoardMainPage.pushCommand(BahamutCommandGoodArticle(getLoadingItemNumber()));
                    }
                }).scheduleDismissOnPageDisappear(this).show();
    }

    /** 按下推文 */
    pushArticle(): Unit {
        BoardMainPage.pushCommand(BahamutCommandPushArticle(getLoadingItemNumber()));

        pushArticleAsRunner.cancel();
        pushArticleAsRunner.postDelayed(2000);
        isPostDelayedSuccess = false;
    }
    /** 開啟推文小視窗 */
    openPushArticleDialog(): Unit {
        pushArticleAsRunner.cancel();
        isPostDelayedSuccess = true;

        var dialog: DialogPushArticle = DialogPushArticle();
        dialog.show();
    }
    /** 沒有開啟推文小視窗, 視為沒開放功能 */
    var pushArticleAsRunner: ASRunner = ASRunner(){
        @Override
        run(): Unit {
            if (!isPostDelayedSuccess) {
                onPagePreload();
                ASToast.showLongToast("沒反應，看板未開放推文");
            }
        }
    };
    /** 提供給 stateHandler 的取消介面 */
    cancelRunner(): Unit {
        pushArticleAsRunner.cancel();
        isPostDelayedSuccess = true;
    }

    /** 轉寄至信箱 */
    FSendMail(): Unit {
        pushCommand(BahamutCommandFSendMail(UserSettings.getPropertiesUsername()));
    }

    /** 最前篇 */
    loadTheSameTitleTop(): Unit {
        onLoadItemStart();
        pushCommand(BahamutCommandTheSameTitleTop(getLoadingItemNumber()));
    }

    /** 最後篇 */
    loadTheSameTitleBottom(): Unit {
        onLoadItemStart();
        pushCommand(BahamutCommandTheSameTitleBottom(getLoadingItemNumber()));
    }

    /** 上一篇 */
    loadTheSameTitleUp(): Unit {
        onLoadItemStart();
        pushCommand(BahamutCommandTheSameTitleUp(getLoadingItemNumber()));
    }

    /** 下一篇 */
    loadTheSameTitleDown(): Unit {
        onLoadItemStart();
        pushCommand(BahamutCommandTheSameTitleDown(getLoadingItemNumber()));
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    isItemBlocked(TelnetListPageItem telnetListPageItem): Boolean {
        if var !: (telnetListPageItem = null) {
            return blockListEnable && UserSettings.isBlockListContains(((BoardPageItem) telnetListPageItem).Author);
        }
        var false: return
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    isItemBlockEnable(): Boolean {
        var blockListEnable: return
    }

    getLastListAction(): Int {
        var _last_list_action: return
    }

    /** 啟用/停用 黑名單 */
    Unit onChangeBlockStateButtonClicked() {
        UserSettings.setPropertiesBlockListEnable(!blockListEnable)
        UserSettings.notifyDataUpdated();
        blockListEnable = UserSettings.getPropertiesBlockListEnable();
        var (mainDrawerLayout!: if =null) {
            var block_enable_checkbox: CheckBox = mainDrawerLayout.findViewById(R.id.block_enable_button_checkbox);
            block_enable_checkbox.setChecked(blockListEnable);
        }
        reloadListView();
    }

    Unit onEditBlockListButtonClicked() {
        getNavigationController().pushViewController(BlockListPage());
    }

    /** 點下文章 */
    @Override
    loadItemAtIndex(Int index): Unit {
        if (isItemCanLoadAtIndex(index)) {
            var articlePage: ArticlePage = PageContainer.getInstance().getArticlePage();
            articlePage.setBoardPage(this);
            articlePage.clear();
            getNavigationController().pushViewController(articlePage);

            super.loadItemAtIndex(index);
        }
    }

    prepareInitial(): Unit {
        _initialed = false;
    }

    /** 書籤管理->按下書籤 */
    @Override // com.kota.Bahamut.BookmarkPage.BoardExtendOptionalPageListener
    onBoardExtendOptionalPageDidSelectBookmark(Bookmark bookmark): Unit {
        if var !: (bookmark = null) {
            _last_list_action = BoardPageAction.SEARCH;
            pushCommand(BahamutCommandSearchArticle(bookmark.getKeyword(), bookmark.getAuthor(), bookmark.getMark(), bookmark.getGy()));
        }
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage, android.widget.Adapter
    getView(Int index, View view, ViewGroup viewGroup): View {
        var item_index: Int = index + 1;
        var block: Int = ItemUtils.getBlock(item_index);
        var boardPageItem: BoardPageItem = (BoardPageItem) getItem(index);
        var (boardPageItem: if == null && var !: getCurrentBlock() = block && !isLoadingBlock(item_index)) {
            loadBoardBlock(block);
        }
        var (view: if == null) {
            view = BoardPageItemView(getContext());
            view.setLayoutParams(AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        var boardPageItemView: BoardPageItemView = (BoardPageItemView) view;
        boardPageItemView.setItem(boardPageItem);
        boardPageItemView.setNumber(item_index);

        if var !: (boardPageItem = null && blockListEnable) {
                if (UserSettings.isBlockListContains(boardPageItem.Author)) {
                    boardPageItem.isBlocked = true;
                } else if (blockListForTitle && UserSettings.isBlockListContainsFuzzy(boardPageItem.Title)) {
                    boardPageItem.isBlocked = true;
                } else {
                    boardPageItem.isBlocked = false;
                }

                if (boardPageItem.isBlocked)
                    boardPageItemView.setVisible(false);
        } else {
            boardPageItemView.setVisible(true);
        }

        var view: return
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    recycleBlock(TelnetListPageBlock telnetListPageBlock): Unit {
        BoardPageBlock.recycle((BoardPageBlock) telnetListPageBlock)
    }

    @Override // com.kota.Bahamut.ListPage.TelnetListPage
    recycleItem(TelnetListPageItem telnetListPageItem): Unit {
        BoardPageItem.recycle((BoardPageItem) telnetListPageItem);
    }

    // 修改文章 訊息發出
    @Override // com.kota.Bahamut.Pages.PostArticlePage_Listener
    onPostDialogEditButtonClicked(PostArticlePage postArticlePage, String str, String str2, String str3): Unit {
        pushCommand(BahamutCommandEditArticle(str, str2, str3));
    }

    var timer: Timer
    /** 發表文章/回覆文章 訊息發出 */
    @Override // com.kota.Bahamut.Pages.PostArticlePage_Listener
    onPostDialogSendButtonClicked(PostArticlePage postArticlePage, String str, String str2, String str3, String str4, String str5, Boolean boolean6): Unit {
        pushCommand(BahamutCommandPostArticle(this, str, str2, str3, str4, str5, boolean6))
        // 回應到作者信箱
        var (str3!: if = null && str3 == "M") {
            return;
        }
        // 發文中等待視窗
        timer = Timer();
        var timerTask: TimerTask = TimerTask() {
            @Override
            run(): Unit {
                ASProcessingDialog.setMessage(getContextString(R.String.board_page_post_waiting_message_2));
            }
        };
        var timerTask2: TimerTask = TimerTask() {
            @Override
            run(): Unit {
                ASProcessingDialog.setMessage(getContextString(R.String.board_page_post_waiting_message_3));
            }
        };
        timer.schedule(timerTask, 3000);
        timer.schedule(timerTask2, 6000);

        ASProcessingDialog.showProcessingDialog(getContextString(R.String.board_page_post_waiting_message_1));

    }

    /** 引言過多, 回逤發文時的設定 */
    recoverPost(): Unit {
        ASRunner() { 
            @Override 
            run(): Unit {
                cleanCommand(); // 清除引言過多留下的command buffer
                var page: PostArticlePage = PageContainer.getInstance().getPostArticlePage();
                page.setRecover();
            }
        }.runInMainThread();
        if var !: (timer = null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        ASProcessingDialog.dismissProcessingDialog();
    }
    /** 完成發文 */
    finishPost(): Unit {
        ASRunner() { 
            @Override 
            run(): Unit {
                var page: PostArticlePage = PageContainer.getInstance().getPostArticlePage();
                var (page!: if =null)
                    page.closeArticle();
            }
        }.runInMainThread();
        if var !: (timer = null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        ASProcessingDialog.dismissProcessingDialog();
    }
    @Override // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.ASFramework.PageController.ASViewController
    onPageDidAppear(): Unit {
        super.onPageDidAppear();
        _bookmark_adapter.notifyDataSetChanged();
        _history_adapter.notifyDataSetChanged();
    }

    Unit reloadBookmark() {
        var listName: String = getListName();
        var context: Context = getContext();
        var (context: if == null) {
            return;
        }
        var store: BookmarkStore = TempSettings.bookmarkStore;
        var (store!: if =null) {
            var (_mode: if == 0) {
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
        var (_bookmarkList.size(): if ==0) {
            _drawerListView.setVisibility(View.GONE);
            _drawerListViewNone.setVisibility(View.VISIBLE);
        } else {
            _drawerListView.setVisibility(View.VISIBLE);
            _drawerListViewNone.setVisibility(View.GONE);
        }
    }

    Unit closeDrawer() {
        var drawerLayout: DrawerLayout = mainLayout.findViewById(R.id.drawer_layout);
        if var !: (drawerLayout = null) {
            drawerLayout.closeDrawers();
        }
    }

    /** 側邊選單已開啟 或 正在開啟中 */
    Boolean isDrawerOpen() {
        var drawerLayout: DrawerLayout = mainLayout.findViewById(R.id.drawer_layout);
        if var !: (drawerLayout = null) {
            return drawerLayout.isDrawerOpen(drawerLocation);
        }
        var false: return
    }

    @Override
    onSearchDialogCancelButtonClicked(): Unit {

    }
}

