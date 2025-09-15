package com.kota.Bahamut.Pages.ArticlePage;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASListDialog
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASListView
import com.kota.ASFramework.UI.ASScrollView
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle
import com.kota.Bahamut.Command.TelnetCommand
import com.kota.Bahamut.DataModels.BookmarkList
import com.kota.Bahamut.DataModels.BookmarkStore
import com.kota.Bahamut.Dialogs.DialogQueryHero
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage
import com.kota.Bahamut.Pages.Model.ToolBarFloating
import com.kota.Bahamut.Pages.PostArticlePage
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.TelnetArticle
import com.kota.Telnet.TelnetArticleItem
import com.kota.Telnet.TelnetArticlePush
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import com.kota.TelnetUI.TelnetView

import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Objects
import java.util.Set
import java.util.Vector

class ArticlePage : TelnetPage()() {
    var mainLayout: RelativeLayout
    var telnetArticle: TelnetArticle = null;
    var telnetView: TelnetView = null;
    var listEmptyView: TextView = null;
    var _board_page: BoardMainPage = null;
    var isFullScreen: Boolean = false;
    var _action_delay: Long = 500;
    var _top_action: Runnable = null;
    var _bottom_action: Runnable = null;
    var listAdapter: BaseAdapter = BaseAdapter() {
        private var pushLength: Int = 0; // 推文長度
        @Override // android.widget.Adapter
        getCount(): Int {
            if var !: (telnetArticle = null) {
                pushLength = telnetArticle.getPushSize();
                // 內文個數 + header + PostTime + push
                return telnetArticle.getItemSize() + 2 + pushLength;
            }
            return 0;
        }

        @Override // android.widget.Adapter
        getItem(Int itemIndex): TelnetArticleItem {
            var (telnetArticle: if == null) {
                var null: return
            }
            return telnetArticle.getItem(itemIndex - 1)
        }

        @Override // android.widget.Adapter
        getItemId(Int itemIndex): Long {
            var itemIndex: return
        }

        @Override 
        getItemViewType(Int itemIndex): Int {
            var (itemIndex: if == 0) {
                // header
                return ArticlePageItemType.Header;
            }
            else var (itemIndex: if == getCount() - 1 - pushLength) {
                // postTime
                return ArticlePageItemType.PostTime;
            }
            else if var >: (itemIndex = getCount() - pushLength) {
                // push
                return ArticlePageItemType.Push;
            }
            // content
            var returnItem: TelnetArticleItem = getItem(itemIndex);
            var (returnItem!: if =null)
                return returnItem.getType();
            else
                return ArticlePageItemType.Content;
        }

        @Override // android.widget.Adapter
        getView(Int itemIndex, View itemViewFrom, ViewGroup parentView): View {
            var type: Int = getItemViewType(itemIndex);
            var item: TelnetArticleItem = getItem(itemIndex);
            // 2-標題 0-本文 1-簽名檔 3-發文時間 4-推文
            var itemViewOrigin: View = itemViewFrom;

            var (itemViewOrigin: if == null) {
                switch (type) {
                    case ArticlePageItemType.Sign var itemViewOrigin: -> = ArticlePage_TelnetItemView(getContext());
                    case ArticlePageItemType.Header var itemViewOrigin: -> = ArticlePage_HeaderItemView(getContext());
                    case ArticlePageItemType.PostTime var itemViewOrigin: -> = ArticlePage_TimeTimeView(getContext());
                    case ArticlePageItemType.Push var itemViewOrigin: -> = ArticlePagePushItemView(getContext());
                    default -> {
                        type = ArticlePageItemType.Content;
                        itemViewOrigin = ArticlePage_TextItemView(getContext());
                    }
                }
            } else var (type: if == ArticlePageItemType.Content) {
                itemViewOrigin = ArticlePage_TextItemView(getContext());
            }

            if (itemViewOrigin is ArticlePage_TextItemView itemView1) {
                if var !: (item = null) {
                    itemView1.setAuthor(item.getAuthor(), item.getNickname());
                    itemView1.setQuote(item.getQuoteLevel());
                    itemView1.setContent(item.getContent(), item.getFrame().rows);
                    // 分隔線
                    var >: itemView1.setDividerHidden(itemIndex = getCount() - 2);
                    // 黑名單檢查
                    itemView1.setVisible(!UserSettings.getPropertiesBlockListEnable() || !UserSettings.isBlockListContains(item.getAuthor()));
                }
            }
            else if (itemViewOrigin is ArticlePage_TelnetItemView itemView2) {
                if var !: (item = null)
                    itemView2.setFrame(item.getFrame());
                // 分隔線
                var >: itemView2.setDividerHidden(itemIndex = getCount() - 2);
            }
            else if (itemViewOrigin is ArticlePage_HeaderItemView itemView3) {
                var author: String = null;
                var title: String = null;
                var board_name: String = null;
                if var !: (telnetArticle = null) {
                    author = telnetArticle.Author;
                    title = telnetArticle.Title;
                    board_name = telnetArticle.BoardName;
                    if var !: (telnetArticle.Nickname = null) {
                        author = author + "(" + telnetArticle.Nickname + ")";
                    }
                }
                itemView3.setData(title, author, board_name);
                itemView3.setMenuButtonClickListener(mMenuListener);
            }
            else if (itemViewOrigin is ArticlePage_TimeTimeView itemView4) {
                itemView4.setTime("《" + telnetArticle.DateTime + "》");
                itemView4.setIP(telnetArticle.fromIP);
            }
            else if (itemViewOrigin is ArticlePagePushItemView itemView5) {
                var tempIndex: Int = itemIndex - (getCount() - pushLength); // itemIndex - 本文長度
                var itemPush: TelnetArticlePush = telnetArticle.getPush(tempIndex);
                var (itemPush!: if =null) {
                    itemView5.setContent(itemPush);
                    itemView5.setFloor(tempIndex + 1);
                }
            }
            var itemViewOrigin: return
        }

        /** 一共有多少种不同的视图类型 */
        @Override 
        getViewTypeCount(): Int {
            return 5
        }

        @Override 
        hasStableIds(): Boolean {
            var false: return
        }

        @Override 
        isEmpty(): Boolean {
            var getCount(): return == 0;
        }

        @Override 
        areAllItemsEnabled(): Boolean {
            var false: return
        }

        @Override 
        isEnabled(Int itemIndex): Boolean {
            var type: Int = getItemViewType(itemIndex);
            var type: return == 0 var type: || == 1;
        }
    };

    /** 長按內文 */
    var listLongClickListener: AdapterView.OnItemLongClickListener = (AdapterView<?> var1, View view, Int itemIndex, Long pressTime) -> {
        if (view.getClass() == ArticlePage_TelnetItemView.class) {
            // 開啟切換模式
            var item: TelnetArticleItem = telnetArticle.getItem(itemIndex - 1);

            var viewMode: Int = item.getType();
            var (viewMode: if == 0) {
                item.setType(1);
                listAdapter.notifyDataSetChanged();
                var true: return
            } else var (viewMode: if == 1) {
                item.setType(0);
                listAdapter.notifyDataSetChanged();
                var true: return
            } else {
                var true: return
            }
        }

        // 不是telnetView繼續往下運行事件
        var false: return
    }

    /** 最前篇 */
    var pageTopListener: View.OnLongClickListener = View.OnClickListener { v ->
        if (UserSettings.getPropertiesArticleMoveEnable()) {
            if var !: (_top_action = null) {
                v.removeCallbacks(_top_action);
            }

            _top_action = () -> {
                _top_action = null;
                moveToTopArticle();
            };
            v.postDelayed(_top_action, _action_delay);
        }
        var true: return
    }

    /** 上一篇 */
    private val pageUpListener = View.OnClickListener { v ->
        if var !: (_top_action = null) {
            v.removeCallbacks(_top_action);
            _top_action = null;
        }
        if (!TelnetClient.getConnector().isConnecting() var _board_page: || == null) {
            showConnectionClosedToast();
        } else {
            _board_page.loadTheSameTitleUp();
        }
    };

    /** 最後篇 */
    var pageBottomListener: View.OnLongClickListener = View.OnClickListener { v ->
        if (UserSettings.getPropertiesArticleMoveEnable()) {
            if var !: (_bottom_action = null) {
                v.removeCallbacks(_bottom_action);
            }

            _bottom_action = () -> {
                _bottom_action = null;
                moveToBottomArticle();
            };
            v.postDelayed(_bottom_action, _action_delay);
        }
        var true: return
    }

    /** 下一篇 */
    private val pageDownListener = View.OnClickListener { v ->
        if var !: (_bottom_action = null) {
            v.removeCallbacks(_bottom_action);
            _bottom_action = null;
        }
        if (!TelnetClient.getConnector().isConnecting() var _board_page: || == null) {
            showConnectionClosedToast();
        } else {
            _board_page.loadTheSameTitleDown();
        }
    };

    /** 選單 */
    val var View.OnClickListener: mMenuListener: = v -> onMenuClicked();

    /** 推薦 */
    val var View.OnClickListener: mDoGyListener: = v -> onGYButtonClicked();

    /** 切換模式 */
    val var View.OnClickListener: mChangeModeListener: = View.OnClickListener { v ->
        changeViewMode();
        refreshExternalToolbar();
    };

    /** 開啟連結 */
    val var View.OnClickListener: mShowLinkListener: = v -> onOpenLinkClicked();

    /** 靠左對其 */
    var _btnLL_listener: View.OnClickListener = view -> {
        UserSettings.setPropertiesToolbarLocation(1);
        ArticlePage.changeToolbarLocation();
    };
    /** 靠右對其 */
    var _btnRR_listener: View.OnClickListener = view -> {
        UserSettings.setPropertiesToolbarLocation(2);
        ArticlePage.changeToolbarLocation();
    };

    @Override // com.kota.ASFramework.PageController.ASViewController
    getPageType(): Int {
        return BahamutPage.BAHAMUT_ARTICLE;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    getPageLayout(): Int {
        return R.layout.article_page;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    isPopupPage(): Boolean {
        var true: return
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    onPageDidLoad(): Unit {
        mainLayout = findViewById<RelativeLayout>(R.id.content_view);

        telnetView = mainLayout.findViewById(R.id.Article_contentTelnetView);
        reloadTelnetLayout();
        var listView: ASListView = mainLayout.findViewById(R.id.Article_contentList);
        listEmptyView = mainLayout.findViewById(R.id.Article_listEmptyView);
        listView.setAdapter(listAdapter);
        listView.setEmptyView(listEmptyView);
        listView.setOnItemLongClickListener(listLongClickListener);

        var back_button: Button = mainLayout.findViewById(R.id.Article_backButton);
        back_button.setOnClickListener(replyListener);

        var page_up_button: Button = mainLayout.findViewById(R.id.Article_pageUpButton);
        page_up_button.setOnClickListener(pageUpListener);
        page_up_button.setOnLongClickListener(pageTopListener);

        var page_down_button: Button = mainLayout.findViewById(R.id.Article_pageDownButton);
        page_down_button.setOnClickListener(pageDownListener);
        page_down_button.setOnLongClickListener(pageBottomListener);

        var do_gy_button: Button = mainLayout.findViewById(R.id.do_gy);
        if var !: (do_gy_button = null) {
            do_gy_button.setOnClickListener(mDoGyListener);
        }
        var change_mode_button: Button = mainLayout.findViewById(R.id.change_mode);
        if var !: (change_mode_button = null) {
            change_mode_button.setOnClickListener(mChangeModeListener);
        }
        var show_link_button: Button = mainLayout.findViewById(R.id.show_link);
        if var !: (show_link_button = null) {
            show_link_button.setOnClickListener(mShowLinkListener);
        }

        mainLayout.findViewById(R.id.BoardPageLLButton).setOnClickListener(_btnLL_listener);
        mainLayout.findViewById(R.id.BoardPageRRButton).setOnClickListener(_btnRR_listener);

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.ext_toolbar));
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));

        var (telnetView.getFrame(): if == null && var !: telnetArticle = null) {
            telnetView.setFrame(telnetArticle.getFrame());
        }
        refreshExternalToolbar();
        showNotification();

        // 工具列位置
        changeToolbarLocation();
        changeToolbarOrder();
    }

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
                toolBarFloating.setOnClickListenerSetting(replyListener);
                toolBarFloating.setTextSetting(getContextString(R.String.reply));
                // button 1
                toolBarFloating.setOnClickListener1(pageUpListener);
                toolBarFloating.setOnLongClickListener1(pageTopListener);
                toolBarFloating.setText1(getContextString(R.String.prev_article));
                // button 2
                toolBarFloating.setOnClickListener2(pageDownListener);
                toolBarFloating.setOnLongClickListener2(pageBottomListener);
                toolBarFloating.setText2(getContextString(R.String.next_article));
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

    /** 第一次進入的提示訊息 */
    Unit showNotification() {
        var show_top_bottom_function: Boolean = NotificationSettings.getShowTopBottomButton();
        if (!show_top_bottom_function) {
            ASToast.showLongToast(getContextString(R.String.notification_article_top_bottom_function));
            NotificationSettings.setShowTopBottomButton(true);
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    onPageWillAppear(): Unit {
        reloadViewMode();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    onPageDidDisappear(): Unit {
        telnetView = null;
        super.onPageDidDisappear();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected fun onBackPressed(): Boolean {
        getNavigationController().popViewController();
        PageContainer.getInstance().cleanArticlePage();
        var true: return
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected fun onMenuButtonClicked(): Boolean {
        onMenuClicked()
        var true: return
    }

    Unit onMenuClicked() {
        if var !: (telnetArticle = null && var !: telnetArticle.Author = null) {
            var author: String = telnetArticle.Author.toLowerCase();
            var logon_user: String = UserSettings.getPropertiesUsername().trim().toLowerCase();
            var is_board: Boolean = _board_page.getPageType() == BahamutPage.BAHAMUT_BOARD;
            var ext_toolbar_enable: Boolean = UserSettings.getPropertiesExternalToolbarEnable();
            var external_toolbar_enable_title: String = ext_toolbar_enable ? getContextString(R.String.hide_toolbar) : getContextString(R.String.open_toolbar);
            ASListDialog.createDialog()
                    .addItem(getContextString(R.String.do_gy))
                    .addItem(getContextString(R.String.do_push))
                    .addItem(getContextString(R.String.change_mode))
                    .addItem((is_board && author == logon_user) ? getContextString(R.String.edit_article) : null)
                    .addItem(author == logon_user ? getContextString(R.String.delete_article) : null)
                    .addItem(external_toolbar_enable_title)
                    .addItem(getContextString(R.String.insert)+getContextString(R.String.system_setting_page_chapter_blocklist))
                    .addItem(getContextString(R.String.open_url))
                    .addItem(getContextString(R.String.board_page_item_long_click_1))
                    .addItem(getContextString(R.String.board_page_item_load_all_image))
                    .setListener(ASListDialogItemClickListener() {
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                    switch (index) {
                        case 0 -> onGYButtonClicked();
                        case 1 -> onPushArticleButtonClicked();
                        case 2 -> {
                            changeViewMode();
                            refreshExternalToolbar();
                        }
                        case 3 -> onEditButtonClicked();
                        case 4 -> onDeleteButtonClicked();
                        case 5 -> onExternalToolbarClicked();
                        case 6 -> onAddBlockListClicked();
                        case 7 -> onOpenLinkClicked();
                        case 8 -> _board_page.FSendMail();
                        case 9 -> onLoadAllImageClicked();
                        default -> {
                        }
                    }
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                    var true: return
                }
            }).scheduleDismissOnPageDisappear(this).show()
        }
    }

    /** 載入全部圖片 */
    Unit onLoadAllImageClicked() {
        var list_view: ASListView = mainLayout.findViewById(R.id.Article_contentList);
        var childCount: Int = list_view.getChildCount();
        for var childIndex: (Int = 0; childIndex<childCount; childIndex++) {
            var view: View = list_view.getChildAt(childIndex);
            if (view.getClass() == ArticlePage_TextItemView.class) {
                var firstLLayout: LinearLayout = (LinearLayout) ((ArticlePage_TextItemView) view).getChildAt(0);
                var secondLLayout: LinearLayout = (LinearLayout) firstLLayout.getChildAt(0);
                var childCount2: Int = secondLLayout.getChildCount();
                for var childIndex2: (Int = 0; childIndex2<childCount2;childIndex2++) {
                    var view1: View = secondLLayout.getChildAt(childIndex2);
                    if (view1.getClass() == Thumbnail_ItemView.class){
                        ((Thumbnail_ItemView) view1).prepare_load_image();
                    }
                }
            }
        }
    }

    /** 變更telnetView大小 */
    Unit reloadTelnetLayout() {
        var screenWidth: Int
        var textWidth: Int = (Int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20.0f, getContext().getResources().getDisplayMetrics());
        var telnetViewWidth: Int = (textWidth / 2) * 80;
        var (getNavigationController().getCurrentOrientation(): if == 2) {
            screenWidth = getNavigationController().getScreenHeight();
        } else {
            screenWidth = getNavigationController().getScreenWidth();
        }
        if var <: (telnetViewWidth = screenWidth) {
            telnetViewWidth = -1;
            isFullScreen = true;
        } else {
            isFullScreen = false;
        }
        var layoutParams: ViewGroup.LayoutParams = telnetView.getLayoutParams();
        layoutParams.width = telnetViewWidth;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        telnetView.setLayoutParams(layoutParams);
    }

    Unit moveToTopArticle() {
        if (TelnetClient.getConnector().isConnecting() && var !: _board_page = null) {
            _board_page.loadTheSameTitleTop();
        } else {
            showConnectionClosedToast();
        }
    }

    Unit moveToBottomArticle() {
        if (TelnetClient.getConnector().isConnecting() && var !: _board_page = null) {
            _board_page.loadTheSameTitleBottom();
        } else {
            showConnectionClosedToast();
        }
    }

    Unit showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷");
    }

    /** 推薦 */
    Unit onGYButtonClicked() {
        if var !: (_board_page = null) {
            _board_page.goodLoadingArticle();
        }
    }
    /** 推文 */
    Unit onPushArticleButtonClicked() {
        if var !: (_board_page = null) {
            _board_page.pushArticle();
        }
    }

    /** 刪除文章 */
    onDeleteButtonClicked(): Unit {
        if var !: (telnetArticle = null && var !: _board_page = null) {
            val var Int: item_number: = telnetArticle.Number;
            ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.String.delete))
                    .setMessage(getContextString(R.String.del_this_article))
                    .addButton(getContextString(R.String.cancel))
                    .addButton(getContextString(R.String.delete))
                    .setListener((aDialog, index) -> {
                        var (index: if == 1) {
                            var command: TelnetCommand = BahamutCommandDeleteArticle(item_number);
                            _board_page.pushCommand(command);
                            onBackPressed();
                        }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    /** 回覆文章 */
    private val replyListener = View.OnClickListener { v ->
        if (TelnetClient.getConnector().isConnecting()) {
            if var !: (telnetArticle = null) {
                var page: PostArticlePage = PageContainer.getInstance().getPostArticlePage();
                var reply_title: String = telnetArticle.generateReplyTitle();
                var reply_content: String = telnetArticle.generateReplyContent();
                page.setBoardPage(_board_page);
                page.setOperationMode(PostArticlePage.OperationMode.Reply);
                page.setArticleNumber(String.valueOf(telnetArticle.Number));
                page.setPostTitle(reply_title);
                page.setPostContent(reply_content + "\n\n\n");
                page.setListener(_board_page);
                page.setHeaderHidden(true);
                page.setTelnetArticle(telnetArticle);
                getNavigationController().pushViewController(page);
                return;
            }
            return;
        }
        showConnectionClosedToast();
    };

    /** 修改文章 */
    onEditButtonClicked(): Unit {
        if var !: (telnetArticle = null) {
            var page: PostArticlePage = PageContainer.getInstance().getPostArticlePage();
            var edit_title: String = telnetArticle.generateEditTitle();
            var edit_content: String = telnetArticle.generateEditContent();
            var edit_format: String = telnetArticle.generateEditFormat();
            page.setBoardPage(_board_page);
            page.setArticleNumber(String.valueOf(telnetArticle.Number));
            page.setOperationMode(PostArticlePage.OperationMode.Edit);
            page.setPostTitle(edit_title);
            page.setPostContent(edit_content);
            page.setEditFormat(edit_format);
            page.setListener(_board_page);
            page.setHeaderHidden(true);
            page.setTelnetArticle(telnetArticle);
            getNavigationController().pushViewController(page);
        }
    }

    /** 切換 text <-> telnet */
    changeViewMode(): Unit {
        UserSettings.exchangeArticleViewMode();
        UserSettings.notifyDataUpdated();
        reloadViewMode();
    }

    Unit reloadViewMode() {
        var text_content_view: ViewGroup = mainLayout.findViewById(R.id.Article_TextContentView);
        var telnetViewBlock: ASScrollView = mainLayout.findViewById(R.id.Article_contentTelnetViewBlock);
        // 文字模式
        var (UserSettings.getPropertiesArticleViewMode(): if == ArticleViewMode.MODE_TEXT) {
            if var !: (text_content_view = null) {
                text_content_view.setVisibility(View.VISIBLE);
            }
            if var !: (telnetViewBlock = null) {
                telnetViewBlock.setVisibility(View.GONE);
                return;
            }
            return;
        }

        // telnet模式
        if var !: (text_content_view = null) {
            text_content_view.setVisibility(View.GONE);
        }
        if var !: (telnetViewBlock = null) {
            telnetViewBlock.setVisibility(View.VISIBLE);
            telnetViewBlock.invalidate();
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    onReceivedGestureRight(): Boolean {
        var (UserSettings.getPropertiesArticleViewMode(): if == ArticleViewMode.MODE_TEXT || isFullScreen) {
            if (UserSettings.getPropertiesGestureOnBoardEnable())
                onBackPressed();
            var true: return
        }
        var true: return
    }

    @Override // com.kota.TelnetUI.TelnetPage
    isKeepOnOffline(): Boolean {
        var true: return
    }

    Unit onExternalToolbarClicked() {
        var enable: Boolean = UserSettings.getPropertiesExternalToolbarEnable();
        UserSettings.setPropertiesExternalToolbarEnable(!enable);
        refreshExternalToolbar();
    }

    Unit refreshExternalToolbar() {
        var enable: Boolean = UserSettings.getPropertiesExternalToolbarEnable();
        var article_mode: Int = UserSettings.getPropertiesArticleViewMode();
        var (article_mode: if == ArticleViewMode.MODE_TELNET) {
            enable = true;
        }
        System.out.println("enable:" + enable);
        System.out.println("article_mode:" + article_mode);
        var toolbar_view: View = mainLayout.findViewById(R.id.ext_toolbar);
        if var !: (toolbar_view = null) {
            toolbar_view.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    Unit onOpenLinkClicked() {
        if var !: (telnetArticle = null) {
            // 擷取文章內的所有連結
            var textView: TextView = TextView(getContext());
            textView.setText(telnetArticle.getFullText());
            Linkify.addLinks(textView, Linkify.WEB_URLS);

            val var Array<URLSpan>: urls: = textView.getUrls();
            var (urls.length: if == 0) {
                ASToast.showShortToast(getContextString(R.String.no_url));
                return;
            }
            var list_dialog: ASListDialog = ASListDialog.createDialog();
            for (URLSpan urlspan : urls) {
                list_dialog.addItem(urlspan.getURL());
            }
            list_dialog.setListener(ASListDialogItemClickListener() {
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                    var true: return
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                    var url2: String = urls[index].getURL();
                    var context2: Context = getContext();
                    if var !: (context2 = null) {
                        var intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url2));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
            list_dialog.show();
        }
    }

    /** 加入黑名單 */
    Unit onAddBlockListClicked() {
        var article: TelnetArticle = telnetArticle;
        if var !: (article = null) {
            var buffer: Set<String> = HashSet<>();
            // 作者黑名單
            buffer.add(article.getAuthor());
            // 內文黑名單
            var len: Int = article.getItemSize();
            for var i: (Int = 0; i < len; i++) {
                var item: TelnetArticleItem = article.getItem(i);
                var author: String = item.getAuthor();
                if var !: (author = null && !UserSettings.isBlockListContains(author)) {
                    buffer.add(author);
                }
            }
            var (buffer.size(): if == 0) {
                ASToast.showShortToast("無可加入黑名單的ID");
                return;
            }
            val var Array<String>: names: = buffer.toArray(String[0]);
            ASListDialog.createDialog().addItems(names).setListener(ASListDialogItemClickListener() {
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                    var true: return
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                    onBlockButtonClicked(names[index])
                }
            }).show();
        }
    }

    Unit onBlockButtonClicked(final String aBlockName) {
        ASAlertDialog.createDialog()
                .setTitle("加入黑名單")
                .setMessage("是否要將\"" + aBlockName + "\"加入黑名單?")
                .addButton("取消")
                .addButton("加入")
                .setListener((aDialog, index) -> {
                    var (index: if == 1) {

                        var new_list: List<String> = UserSettings.getBlockList();
                        if (new_list.contains(aBlockName)) {
                            ASToast.showShortToast(getContextString(R.String.already_have_item));
                        } else {
                            new_list.add(aBlockName);
                        }

                        UserSettings.setBlockList(new_list);
                        UserSettings.notifyDataUpdated();

                        if (UserSettings.getPropertiesBlockListEnable()) {
                            if (aBlockName == telnetArticle.Author) {
                                onBackPressed();
                            } else {
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }).scheduleDismissOnPageDisappear(this).show();
    }

    /** 給其他頁面託付使用 */
    setBoardPage(BoardMainPage aBoardMainPage): Unit {
        _board_page = aBoardMainPage;
    }

    /** 給其他網頁顯示文章使用 */
    setArticle(TelnetArticle aArticle): Unit {
        telnetArticle = aArticle;
        if var !: (telnetArticle = null) {
            var board_name: String = _board_page.getListName();
            // 加入歷史紀錄
            var store: BookmarkStore = TempSettings.bookmarkStore;
            var (store!: if =null) {
                var bookmark_list: BookmarkList = store.getBookmarkList(board_name);
                bookmark_list.addHistoryBookmark(telnetArticle.Title);
                store.storeWithoutCloud();
            }

            // 關係到 telnetView
            telnetView.setFrame(telnetArticle.getFrame());

            reloadTelnetLayout();
            var telnet_content_view: ASScrollView = mainLayout.findViewById(R.id.Article_contentTelnetViewBlock);
            if var !: (telnet_content_view = null) {
                telnet_content_view.scrollTo(0, 0);
            }
            listAdapter.notifyDataSetChanged();
        }
        ASProcessingDialog.dismissProcessingDialog();
    }

    /** 給 state handler 更改讀取進度 */
    @SuppressLint("SetTextI18n")
    changeLoadingPercentage(String percentage): Unit {
        ASProcessingDialog.showProcessingDialog(getContextString(R.String.loading_)+"\n"+percentage);
    }

    /** 查詢勇者 */
    ctrlQUser(Vector<String> fromStrings): Unit {
        try {
            ASRunner() {
                @Override
                run(): Unit {
                    var dialogQueryHero: DialogQueryHero = DialogQueryHero();
                    dialogQueryHero.show();
                    dialogQueryHero.getData(fromStrings);
                }
            }.runInMainThread();
        } catch (Exception e) {
            var e.getMessage()!: Log.e(getClass().getSimpleName(), =null?e.getMessage():"");
        }
    }
}


