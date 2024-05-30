package com.kota.Bahamut.Pages.ArticlePage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASScrollView;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kota.Bahamut.Command.TelnetCommand;
import com.kota.Bahamut.DataModels.BookmarkList;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.Dialogs.DialogShortenUrl;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage;
import com.kota.Bahamut.Pages.Model.ToolBarFloating;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

public class ArticlePage extends TelnetPage {
    RelativeLayout mainLayout;
    private TelnetArticle _article = null;
    private TelnetView _telnet_view = null;
    private BoardMainPage _board_page = null;
    private boolean _full_screen = false;
    private boolean _i_have_sign = false; // 本篇文章有沒有出現簽名檔
    private ArticlePage_HeaderItemView articlePageHeaderItemView;
    long _action_delay = 500;
    Runnable _top_action = null;
    Runnable _bottom_action = null;
    BaseAdapter _list_adapter = new BaseAdapter() {
        @Override // android.widget.Adapter
        public int getCount() {
            if (_article != null) {
                return _article.getItemSize() + 2;
            }
            return 0;
        }

        @Override // android.widget.Adapter
        public TelnetArticleItem getItem(int itemIndex) {
            if (_article == null) {
                return null;
            }
            return _article.getItem(itemIndex - 1);
        }

        @Override // android.widget.Adapter
        public long getItemId(int itemIndex) {
            return itemIndex;
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public int getItemViewType(int itemIndex) {
            if (itemIndex == 0) {
                return 2;
            }
            if (itemIndex == getCount() - 1) {
                return 3;
            }
            return Objects.requireNonNull(getItem(itemIndex)).getType();
        }

        @Override // android.widget.Adapter
        public View getView(int itemIndex, View itemView, ViewGroup parentView) {
            int type = getItemViewType(itemIndex);
            // 2-標題 0-本文 1-簽名檔 3-發文時間
            if (itemView == null) {
                switch (type) {
                    case ArticlePageItemType.Content ->
                            itemView = new ArticlePage_TextItemView(getContext());
                    case ArticlePageItemType.Sign -> {
                        itemView = new ArticlePage_TelnetItemView(getContext());
                        _i_have_sign = true;
                    }
                    case ArticlePageItemType.Header -> {
                        itemView = new ArticlePage_HeaderItemView(getContext());
                        articlePageHeaderItemView = (ArticlePage_HeaderItemView) itemView;
                    }
                    case ArticlePageItemType.PostTime ->
                            itemView = new ArticlePage_TimeTimeView(getContext());
                }
            } else {
                if (type == ArticlePageItemType.Content) {
                    itemView = new ArticlePage_TextItemView(getContext());
                }
            }
            switch (type) {
                case ArticlePageItemType.Content:
                    ArticlePage_TextItemView item_view = (ArticlePage_TextItemView) itemView;
                    TelnetArticleItem item = getItem(itemIndex);
                    if (item!=null) {
                        item_view.setAuthor(item.getAuthor(), item.getNickname());
                        item_view.setQuote(item.getQuoteLevel());
                        item_view.setContent(item.getContent(), item.getFrame().rows);
                    }
                    // 分隔線
                    if (itemIndex < getCount() - 2) {
                        item_view.setDividerHidden(false);
                    } else {
                        item_view.setDividerHidden(true);
                    }
                    // 黑名單檢查
                    if (UserSettings.getPropertiesBlockListEnable() && UserSettings.isBlockListContains(Objects.requireNonNull(item).getAuthor())) {
                        item_view.setVisible(false);
                    } else {
                        item_view.setVisible(true);
                    }
                    break;
                case ArticlePageItemType.Sign:
                    ArticlePage_TelnetItemView item_view2 = (ArticlePage_TelnetItemView) itemView;
                    item_view2.setFrame(Objects.requireNonNull(getItem(itemIndex)).getFrame());
                    // 分隔線
                    if (itemIndex < getCount() - 2) {
                        item_view2.setDividerhidden(false);
                    } else {
                        item_view2.setDividerhidden(true);
                    }
                    break;
                case ArticlePageItemType.Header:
                    ArticlePage_HeaderItemView item_view3 = (ArticlePage_HeaderItemView) itemView;
                    String author = null;
                    String title = null;
                    String board_name = null;
                    if (_article != null) {
                        author = _article.Author;
                        title = _article.Title;
                        board_name = _article.BoardName;
                        if (_article.Nickname != null) {
                            author = author + "(" + _article.Nickname + ")";
                        }
                    }
                    item_view3.setData(title, author, board_name);
                    item_view3.setMenuButton(mMenuListener);
                    break;
                case ArticlePageItemType.PostTime:
                    if (_article != null) {
                        ((ArticlePage_TimeTimeView) itemView).setTime("《" + _article.DateTime + "》");
                        break;
                    }
                    break;
            }
            return itemView;
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public int getViewTypeCount() {
            return 4;
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public boolean hasStableIds() {
            return false;
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public boolean isEmpty() {
            return getCount() == 0;
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean isEnabled(int itemIndex) {
            int type = getItemViewType(itemIndex);
            return type == 0 || type == 1;
        }
    };

    // 長按內文
    AdapterView.OnItemLongClickListener _list_long_click_listener = (arg0, arg1, itemIndex, arg3) -> {
        // 沒有簽名檔直接往下走
        if (!_i_have_sign) {
            return true;
        }
        // 切換模式只適用於簽名檔
        // 簽名檔一定是最後一個
        int signIndex = _article.getItemSize();
        if (itemIndex != signIndex){
            return true;
        }
        // 開啟切換模式
        TelnetArticleItem item;
        if (_article == null || (item = _article.getItem(itemIndex - 1)) == null) {
            return true;
        }
        int type = item.getType();
        if (type == 0) {
            item.setType(1);
            _list_adapter.notifyDataSetChanged();
            return true;
        } else if (type == 1) {
            item.setType(0);
            _list_adapter.notifyDataSetChanged();
            return true;
        } else {
            return true;
        }
    };

    // 最前篇
    View.OnLongClickListener _page_top_listener = v -> {
        if (UserSettings.getPropertiesArticleMoveEnable()) {
            if (_top_action != null) {
                v.removeCallbacks(_top_action);
            }

            _top_action = () -> {
                _top_action = null;
                moveToTopArticle();
            };
            v.postDelayed(_top_action, _action_delay);
        }
        return true;
    };

    // 上一篇
    View.OnClickListener _page_up_listener = v -> {
        if (_top_action != null) {
            v.removeCallbacks(_top_action);
            _top_action = null;
        }
        if (!TelnetClient.getConnector().isConnecting() || _board_page == null) {
            showConnectionClosedToast();
        } else {
            _board_page.loadTheSameTitleUp();
        }
    };

    // 最後篇
    View.OnLongClickListener _page_bottom_listener = v -> {
        if (UserSettings.getPropertiesArticleMoveEnable()) {
            if (_bottom_action != null) {
                v.removeCallbacks(_bottom_action);
            }

            _bottom_action = () -> {
                _bottom_action = null;
                moveToBottomArticle();
            };
            v.postDelayed(_bottom_action, _action_delay);
        }
        return true;
    };

    // 下一篇
    View.OnClickListener _page_down_listener = v -> {
        if (_bottom_action != null) {
            v.removeCallbacks(_bottom_action);
            _bottom_action = null;
        }
        if (!TelnetClient.getConnector().isConnecting() || _board_page == null) {
            showConnectionClosedToast();
        } else {
            _board_page.loadTheSameTitleDown();
        }
    };

    // 回覆文章
    View.OnClickListener _back_listener = v -> {
        if (TelnetClient.getConnector().isConnecting()) {
            if (_article != null) {
                PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
                String reply_title = _article.generateReplyTitle();
                String reply_content = _article.generateReplyContent();
                page.setBoardPage(_board_page);
                page.setOperationMode(PostArticlePage.OperationMode.Reply);
                page.setArticleNumber(String.valueOf(_article.Number));
                page.setPostTitle(reply_title);
                page.setPostContent(reply_content + "\n\n\n");
                page.setListener(_board_page);
                page.setHeaderHidden(true);
                getNavigationController().pushViewController(page);
                return;
            }
            return;
        }
        showConnectionClosedToast();
    };

    // 選單
    private final View.OnClickListener mMenuListener = v -> onMenuClicked();

    // 推薦
    private final View.OnClickListener mDoGyListener = v -> onGYButtonClicked();

    // 切換模式
    private final View.OnClickListener mChangeModeListener = v -> {
        changeViewMode();
        refreshExternalToolbar();
    };

    // 開啟連結
    private final View.OnClickListener mShowLinkListener = v -> onOpenLinkClicked();

    // 靠左對其
    View.OnClickListener _btnLL_listener = view -> {
        UserSettings.setPropertiesToolbarLocation(1);
        ArticlePage.this.changeToolbarLocation();
    };
    // 靠右對其
    View.OnClickListener _btnRR_listener = view -> {
        UserSettings.setPropertiesToolbarLocation(2);
        ArticlePage.this.changeToolbarLocation();
    };

    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return BahamutPage.BAHAMUT_ARTICLE;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageLayout() {
        return R.layout.article_page;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public boolean isPopupPage() {
        return true;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageDidLoad() {
        mainLayout = (RelativeLayout) findViewById(R.id.content_view);

        this._telnet_view = mainLayout.findViewById(R.id.Article_contentTelnetView);
        reloadTelnetLayout();
        ASListView list_view = mainLayout.findViewById(R.id.Article_contentList);
        list_view.setAdapter(this._list_adapter);
        list_view.setOnItemLongClickListener(this._list_long_click_listener);

        Button back_button = mainLayout.findViewById(R.id.Article_backButton);
        back_button.setOnClickListener(this._back_listener);

        Button page_up_button = mainLayout.findViewById(R.id.Article_pageUpButton);
        page_up_button.setOnClickListener(this._page_up_listener);
        page_up_button.setOnLongClickListener(this._page_top_listener);

        Button page_down_button = mainLayout.findViewById(R.id.Article_pageDownButton);
        page_down_button.setOnClickListener(this._page_down_listener);
        page_down_button.setOnLongClickListener(this._page_bottom_listener);

        Button do_gy_button = mainLayout.findViewById(R.id.do_gy);
        if (do_gy_button != null) {
            do_gy_button.setOnClickListener(this.mDoGyListener);
        }
        Button change_mode_button = mainLayout.findViewById(R.id.change_mode);
        if (change_mode_button != null) {
            change_mode_button.setOnClickListener(this.mChangeModeListener);
        }
        Button show_link_button = mainLayout.findViewById(R.id.show_link);
        if (show_link_button != null) {
            show_link_button.setOnClickListener(this.mShowLinkListener);
        }

        mainLayout.findViewById(R.id.BoardPageLLButton).setOnClickListener(_btnLL_listener);
        mainLayout.findViewById(R.id.BoardPageRRButton).setOnClickListener(_btnRR_listener);

        if (this._telnet_view.getFrame() == null && this._article != null) {
            this._telnet_view.setFrame(this._article.getFrame());
        }
        refreshExternalToolbar();
        showNotification();

        // 工具列位置
        changeToolbarLocation();
        changeToolbarOrder();
    }

    // 變更工具列位置
    private void changeToolbarLocation() {
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
                toolBarFloating.setOnClickListenerSetting(_back_listener);
                toolBarFloating.setTextSetting(getContextString(R.string.reply));
                // button 1
                toolBarFloating.setOnClickListener1(_page_up_listener);
                toolBarFloating.setOnLongClickListener1(_page_top_listener);
                toolBarFloating.setText1(getContextString(R.string.prev_article));
                // button 2
                toolBarFloating.setOnClickListener2(_page_down_listener);
                toolBarFloating.setOnLongClickListener2(_page_bottom_listener);
                toolBarFloating.setText2(getContextString(R.string.next_article));
            }
            default -> {
                // 底部-中間
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }

        toolbar.setLayoutParams(layoutParams);
    }

    // 反轉按鈕順序
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

    // 第一次進入的提示訊息
    void showNotification() {
        boolean show_top_bottom_function = NotificationSettings.getShowTopBottomButton();
        if (!show_top_bottom_function) {
            ASToast.showLongToast(getContextString(R.string.notification_article_top_bottom_function));
            NotificationSettings.setShowTopBottomButton(true);
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageWillAppear() {
        reloadViewMode();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageDidDisappear() {
        this._telnet_view = null;
        super.onPageDidDisappear();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onBackPressed() {
        getNavigationController().popViewController();
        PageContainer.getInstance().cleanArticlePage();
        return true;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    protected boolean onMenuButtonClicked() {
        onMenuClicked();
        return true;
    }

    void onMenuClicked() {
        if (this._article != null && this._article.Author != null) {
            String author = this._article.Author.toLowerCase();
            String logon_user = UserSettings.getPropertiesUsername().trim().toLowerCase();
            boolean is_board = this._board_page.getPageType() == BahamutPage.BAHAMUT_BOARD;
            boolean ext_toolbar_enable = UserSettings.getPropertiesExternalToolbarEnable();
            String external_toolbar_enable_title = ext_toolbar_enable ? getContextString(R.string.hide_toolbar) : getContextString(R.string.open_toolbar);
            ASListDialog.createDialog()
                    .addItem(getContextString(R.string.do_gy))
                    .addItem(getContextString(R.string.change_mode))
                    .addItem((is_board && author.equals(logon_user)) ? getContextString(R.string.edit_article) : null)
                    .addItem(author.equals(logon_user) ? getContextString(R.string.delete_article) : null)
                    .addItem(external_toolbar_enable_title)
                    .addItem(getContextString(R.string.insert)+getContextString(R.string.system_setting_page_chapter_blocklist))
                    .addItem(getContextString(R.string.open_url))
                    .addItem(getContextString(R.string.board_page_item_long_click_1))
                    .addItem(getContextString(R.string.board_page_item_load_all_image))
                    .setListener(new ASListDialogItemClickListener() {
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    switch (index) {
                        case 0 -> onGYButtonClicked();
                        case 1 -> {
                            changeViewMode();
                            refreshExternalToolbar();
                        }
                        case 2 -> onEditButtonClicked();
                        case 3 -> onDeleteButtonClicked();
                        case 4 -> onExternalToolbarClicked();
                        case 5 -> onAddBlockListClicked();
                        case 6 -> onOpenLinkClicked();
                        case 7 -> _board_page.FSendMail();
                        case 8 -> onLoadAllImageClicked();
                        default -> {
                        }
                    }
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return true;
                }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    // 載入全部圖片
    private void onLoadAllImageClicked() {
        // TODO: not yet
        ASListView list_view = mainLayout.findViewById(R.id.Article_contentList);
        int childCount = list_view.getChildCount();
        for (int childIndex = 0; childIndex<childCount; childIndex++) {
            View view = list_view.getChildAt(childIndex);
            if (view.getClass().equals(ArticlePage_TextItemView.class)) {
                LinearLayout firstLLayout = (LinearLayout) ((ArticlePage_TextItemView) view).getChildAt(0);
                LinearLayout secondLLayout = (LinearLayout) firstLLayout.getChildAt(0);
                int childCount2 = secondLLayout.getChildCount();
                for (int childIndex2 = 0; childIndex2<childCount2;childIndex2++) {
                    View view1 = secondLLayout.getChildAt(childIndex2);
                    if (view1.getClass().equals(Thumbnail_ItemView.class)){
                        ((Thumbnail_ItemView) view1).prepare_load_image();
                    }
                }
            }
        }
    }

    public void setArticle(TelnetArticle aArticle) {
        this._article = aArticle;
        if (this._article != null) {
            String board_name = this._board_page.getListName();
            BookmarkStore store = TempSettings.getBookmarkStore();
            BookmarkList bookmark_list = store.getBookmarkList(board_name);
            bookmark_list.addHistoryBookmark(this._article.Title);
            store.store();
            this._telnet_view.setFrame(this._article.getFrame());
            reloadTelnetLayout();
            ASScrollView telnet_content_view = mainLayout.findViewById(R.id.Article_contentTelnetViewBlock);
            if (telnet_content_view != null) {
                telnet_content_view.scrollTo(0, 0);
            }
            this._list_adapter.notifyDataSetChanged();
        }
        ASProcessingDialog.dismissProcessingDialog();
    }

    private void reloadTelnetLayout() {
        int screen_width;
        int text_width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20.0f, getContext().getResources().getDisplayMetrics());
        int telnet_view_width = (text_width / 2) * 80;
        if (getNavigationController().getCurrentOrientation() == 2) {
            screen_width = getNavigationController().getScreenHeight();
        } else {
            screen_width = getNavigationController().getScreenWidth();
        }
        if (telnet_view_width <= screen_width) {
            telnet_view_width = -1;
            this._full_screen = true;
        } else {
            this._full_screen = false;
        }
        ViewGroup.LayoutParams telnet_layout = this._telnet_view.getLayoutParams();
        telnet_layout.width = telnet_view_width;
        telnet_layout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        this._telnet_view.setLayoutParams(telnet_layout);
    }

    void moveToTopArticle() {
        if (TelnetClient.getConnector().isConnecting() && this._board_page != null) {
            this._board_page.loadTheSameTitleTop();
        } else {
            showConnectionClosedToast();
        }
    }

    void moveToBottomArticle() {
        if (TelnetClient.getConnector().isConnecting() && this._board_page != null) {
            this._board_page.loadTheSameTitleBottom();
        } else {
            showConnectionClosedToast();
        }
    }

    private void showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷");
    }

    private void onGYButtonClicked() {
        if (this._board_page != null) {
            this._board_page.goodLoadingArticle();
        }
    }

    // 刪除文章
    public void onDeleteButtonClicked() {
        if (this._article != null && this._board_page != null) {
            final int item_number = this._article.Number;
            ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.string.delete))
                    .setMessage(getContextString(R.string.del_this_article))
                    .addButton(getContextString(R.string.cancel))
                    .addButton(getContextString(R.string.delete))
                    .setListener((aDialog, index) -> {
                switch (index) {
                    case 0:
                    default:
                        return;
                    case 1:
                        TelnetCommand command = new BahamutCommandDeleteArticle(item_number);
                        _board_page.pushCommand(command);
                        onBackPressed();
                }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    // 修改文章
    public void onEditButtonClicked() {
        if (this._article != null) {
            PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
            String edit_title = this._article.generateEditTitle();
            String edit_content = this._article.generateEditContent();
            String edit_format = this._article.generateEditFormat();
            page.setBoardPage(this._board_page);
            page.setArticleNumber(String.valueOf(this._article.Number));
            page.setOperationMode(PostArticlePage.OperationMode.Edit);
            page.setPostTitle(edit_title);
            page.setPostContent(edit_content);
            page.setEditFormat(edit_format);
            page.setListener(this._board_page);
            page.setHeaderHidden(true);
            getNavigationController().pushViewController(page);
        }
    }

    public void changeViewMode() {
        UserSettings.exchangeArticleViewMode();
        UserSettings.notifyDataUpdated();
        reloadViewMode();
    }

    private void reloadViewMode() {
        ViewGroup text_content_view = mainLayout.findViewById(R.id.Article_TextContentView);
        ASScrollView telnet_content_view = mainLayout.findViewById(R.id.Article_contentTelnetViewBlock);
        // 文字模式
        if (UserSettings.getPropertiesArticleViewMode() == 0) {
            if (text_content_view != null) {
                text_content_view.setVisibility(View.VISIBLE);
            }
            if (telnet_content_view != null) {
                telnet_content_view.setVisibility(View.GONE);
                return;
            }
            return;
        }

        // telnet模式
        if (text_content_view != null) {
            text_content_view.setVisibility(View.GONE);
        }
        if (telnet_content_view != null) {
            telnet_content_view.setVisibility(View.VISIBLE);
            telnet_content_view.invalidate();
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public boolean onReceivedGestureRight() {
        if (UserSettings.getPropertiesArticleViewMode() == 0 || this._full_screen) {
            if (UserSettings.getPropertiesGestureOnBoardEnable())
                onBackPressed();
            return true;
        }
        return true;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public boolean isKeepOnOffline() {
        return true;
    }

    public void setBoardPage(BoardMainPage aBoardMainPage) {
        this._board_page = aBoardMainPage;
    }

    public void onExternalToolbarClicked() {
        boolean enable = UserSettings.getPropertiesExternalToolbarEnable();
        UserSettings.setPropertiesExternalToolbarEnable(!enable);
        refreshExternalToolbar();
    }

    private void refreshExternalToolbar() {
        boolean enable = UserSettings.getPropertiesExternalToolbarEnable();
        int article_mode = UserSettings.getPropertiesArticleViewMode();
        if (article_mode == 1) {
            enable = true;
        }
        System.out.println("enable:" + enable);
        System.out.println("article_mode:" + article_mode);
        View toolbar_view = mainLayout.findViewById(R.id.ext_toolbar);
        if (toolbar_view != null) {
            toolbar_view.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    private void onOpenLinkClicked() {
        if (this._article != null) {
            // 擷取文章內的所有連結
            TextView textView = new TextView(getContext());
            textView.setText(this._article.getFullText());
            Linkify.addLinks(textView, Linkify.WEB_URLS);

            final URLSpan[] urls = textView.getUrls();
            if (urls.length == 0) {
                ASToast.showShortToast(getContextString(R.string.no_url));
                return;
            }
            ASListDialog list_dialog = ASListDialog.createDialog();
            for (URLSpan urlspan : urls) {
                list_dialog.addItem(urlspan.getURL());
            }
            list_dialog.setListener(new ASListDialogItemClickListener() {
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return true;
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    String url2 = urls[index].getURL();
                    Context context2 = getContext();
                    if (context2 != null) {
                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url2));
                        context2.startActivity(intent);
                    }
                }
            });
            list_dialog.show();
        }
    }

    // 加入黑名單
    public void onAddBlockListClicked() {
        TelnetArticle article = this._article;
        if (article != null) {
            Set<String> buffer = new HashSet<>();
            // 作者黑名單
            buffer.add(article.getAuthor());
            // 內文黑名單
            int len = article.getItemSize();
            for (int i = 0; i < len; i++) {
                TelnetArticleItem item = article.getItem(i);
                String author = item.getAuthor();
                if (author != null && !UserSettings.isBlockListContains(author)) {
                    buffer.add(author);
                }
            }
            if (buffer.size() == 0) {
                ASToast.showShortToast("無可加入黑名單的ID");
                return;
            }
            final String[] names = buffer.toArray(new String[0]);
            ASListDialog.createDialog().addItems(names).setListener(new ASListDialogItemClickListener() {
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return true;
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    onBlockButtonClicked(names[index]);
                }
            }).show();
        }
    }

    public void onBlockButtonClicked(final String aBlockName) {
        ASAlertDialog.createDialog()
                .setTitle("加入黑名單")
                .setMessage("是否要將\"" + aBlockName + "\"加入黑名單?")
                .addButton("取消")
                .addButton("加入")
                .setListener((aDialog, index) -> {
                    if (index == 1) {

                        List<String> new_list = UserSettings.getBlockList();
                        if (new_list.contains(aBlockName)) {
                            ASToast.showShortToast(getContextString(R.string.already_have_item));
                        } else {
                            new_list.add(aBlockName);
                        }
                        UserSettings.setBlockList(new_list);

                        UserSettings.notifyDataUpdated();
                        if (UserSettings.getPropertiesBlockListEnable()) {
                            if (aBlockName.equals(_article.Author)) {
                                onBackPressed();
                            } else {
                                _list_adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }).scheduleDismissOnPageDisappear(this).show();
    }

    public void ctrlQUser(Vector<String> fromStrings) {
        System.out.println(fromStrings);
        try {
            new ASRunner() {
                @Override
                public void run() {
                    DialogShortenUrl temp = new DialogShortenUrl();
                    temp.show();
                }
            }.runInMainThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
