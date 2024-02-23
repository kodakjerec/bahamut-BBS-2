package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASScrollView;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kota.Bahamut.Command.TelnetCommand;
import com.kota.Bahamut.DataModels.BookmarkList;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Article.ArticlePage_HeaderItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TelnetItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TextItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TimeTimeView;
import com.kota.Bahamut.Pages.Model.ToolBarFloating;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/* loaded from: classes.dex */
public class ArticlePage extends TelnetPage {
    UserSettings _settings;
    private TelnetArticle _article = null;
    private TelnetView _telnet_view = null;
    private BoardPage _board_page = null;
    private boolean _full_screen = false;
    private boolean _i_have_sign = false; // 本篇文章有沒有出現簽名檔
    long _action_delay = 500;
    Runnable _top_action = null;
    Runnable _bottom_action = null;
    BaseAdapter _list_adapter = new BaseAdapter() { // from class: com.kota.Bahamut.Pages.ArticlePage.2
        @Override // android.widget.Adapter
        public int getCount() {
            if (ArticlePage.this._article != null) {
                return ArticlePage.this._article.getItemSize() + 2;
            }
            return 0;
        }

        @Override // android.widget.Adapter
        public TelnetArticleItem getItem(int itemIndex) {
            if (ArticlePage.this._article == null) {
                return null;
            }
            return ArticlePage.this._article.getItem(itemIndex - 1);
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
                    case 0:
                        itemView = new ArticlePage_TextItemView(ArticlePage.this.getContext());
                        break;
                    case 1:
                        itemView = new ArticlePage_TelnetItemView(ArticlePage.this.getContext());
                        _i_have_sign = true;
                        break;
                    case 2:
                        itemView = new ArticlePage_HeaderItemView(ArticlePage.this.getContext());
                        itemView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
                        break;
                    case 3:
                        itemView = new ArticlePage_TimeTimeView(ArticlePage.this.getContext());
                        break;
                }
            }
            switch (type) {
                case 0:
                    ArticlePage_TextItemView item_view = (ArticlePage_TextItemView) itemView;
                    TelnetArticleItem item = getItem(itemIndex);
                    if (item!=null) {
                        item_view.setAuthor(item.getAuthor(), item.getNickname());
                        item_view.setQuote(item.getQuoteLevel());
                        item_view.setContent(item.getContent());
                    }
                    if (itemIndex < getCount() - 2) {
                        item_view.setDividerhidden(false);
                    } else {
                        item_view.setDividerhidden(true);
                    }
                    if (ArticlePage.this._settings.getPropertiesBlockListEnable() && ArticlePage.this._settings.isBlockListContains(Objects.requireNonNull(item).getAuthor())) {
                        item_view.setVisible(false);
                    } else {
                        item_view.setVisible(true);
                    }
                    break;
                case 1:
                    ArticlePage_TelnetItemView item_view2 = (ArticlePage_TelnetItemView) itemView;
                    item_view2.setFrame(Objects.requireNonNull(getItem(itemIndex)).getFrame());
                    if (itemIndex < getCount() - 2) {
                        item_view2.setDividerhidden(false);
                        break;
                    } else {
                        item_view2.setDividerhidden(true);
                        break;
                    }
                case 2:
                    ArticlePage_HeaderItemView item_view3 = (ArticlePage_HeaderItemView) itemView;
                    String author = null;
                    String title = null;
                    String board_name = null;
                    if (ArticlePage.this._article != null) {
                        author = ArticlePage.this._article.Author;
                        title = ArticlePage.this._article.Title;
                        board_name = ArticlePage.this._article.BoardName;
                        if (ArticlePage.this._article.Nickname != null) {
                            author = author + "(" + ArticlePage.this._article.Nickname + ")";
                        }
                    }
                    item_view3.setData(title, author, board_name);
                    item_view3.setMenuButton(ArticlePage.this.mMenuListener);
                    break;
                case 3:
                    if (ArticlePage.this._article != null) {
                        ((ArticlePage_TimeTimeView) itemView).setTime("《" + ArticlePage.this._article.DateTime + "》");
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
            return false;
        }
        // 切換模式只適用於簽名檔
        // 簽名檔一定是最後一個
        int signIndex = ArticlePage.this._article.getItemSize();
        if (itemIndex != signIndex){
            return false;
        }
        // 開啟切換模式
        TelnetArticleItem item;
        if (ArticlePage.this._article == null || (item = ArticlePage.this._article.getItem(itemIndex - 1)) == null) {
            return false;
        }
        int type = item.getType();
        if (type == 0) {
            item.setType(1);
            ArticlePage.this._list_adapter.notifyDataSetChanged();
            return true;
        } else if (type == 1) {
            item.setType(0);
            ArticlePage.this._list_adapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    };

    // 最前篇
    View.OnLongClickListener _page_top_listener = v -> {
        if (ArticlePage.this._settings.getPropertiesArticleMoveEnable()) {
            if (ArticlePage.this._top_action != null) {
                v.removeCallbacks(ArticlePage.this._top_action);
            }
            // from class: com.kota.Bahamut.Pages.ArticlePage.4.1
// java.lang.Runnable
            ArticlePage.this._top_action = () -> {
                ArticlePage.this._top_action = null;
                ArticlePage.this.moveToTopArticle();
            };
            v.postDelayed(ArticlePage.this._top_action, ArticlePage.this._action_delay);
        }
        return false;
    };

    // 上一篇
    View.OnClickListener _page_up_listener = v -> {
        if (ArticlePage.this._top_action != null) {
            v.removeCallbacks(ArticlePage.this._top_action);
            ArticlePage.this._top_action = null;
        }
        if (!TelnetClient.getConnector().isConnecting() || ArticlePage.this._board_page == null) {
            ArticlePage.this.showConnectionClosedToast();
        } else {
            ArticlePage.this._board_page.loadTheSameTitleUp();
        }
    };

    // 最後篇
    View.OnLongClickListener _page_bottom_listener = v -> {
        if (ArticlePage.this._settings.getPropertiesArticleMoveEnable()) {
            if (ArticlePage.this._bottom_action != null) {
                v.removeCallbacks(ArticlePage.this._bottom_action);
            }
            // from class: com.kota.Bahamut.Pages.ArticlePage.6.1
// java.lang.Runnable
            ArticlePage.this._bottom_action = () -> {
                ArticlePage.this._bottom_action = null;
                ArticlePage.this.moveToBottomArticle();
            };
            v.postDelayed(ArticlePage.this._bottom_action, ArticlePage.this._action_delay);
        }
        return false;
    };

    // 下一篇
    View.OnClickListener _page_down_listener = v -> {
        if (ArticlePage.this._bottom_action != null) {
            v.removeCallbacks(ArticlePage.this._bottom_action);
            ArticlePage.this._bottom_action = null;
        }
        if (!TelnetClient.getConnector().isConnecting() || ArticlePage.this._board_page == null) {
            ArticlePage.this.showConnectionClosedToast();
        } else {
            ArticlePage.this._board_page.loadTheSameTitleDown();
        }
    };

    // 回復
    View.OnClickListener _back_listener = v -> {
        if (TelnetClient.getConnector().isConnecting()) {
            if (ArticlePage.this._article != null) {
                PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
                String reply_title = ArticlePage.this._article.generateReplyTitle();
                ArticlePage.this._article.setBlockList(ArticlePage.this._settings.getBlockListLowCasedString());
                String reply_content = ArticlePage.this._article.generateReplyContent();
                page.setBoardPage(ArticlePage.this._board_page);
                page.setOperationMode(PostArticlePage.OperationMode.Reply);
                page.setArticleNumber(String.valueOf(ArticlePage.this._article.Number));
                page.setPostTitle(reply_title);
                page.setPostContent(reply_content + "\n\n\n");
                page.setListener(ArticlePage.this._board_page);
                page.setHeaderHidden(true);
                ArticlePage.this.getNavigationController().pushViewController(page);
                return;
            }
            return;
        }
        ArticlePage.this.showConnectionClosedToast();
    };

    // 選單
    private final View.OnClickListener mMenuListener = v -> ArticlePage.this.onMenuClicked();

    // 推薦
    private final View.OnClickListener mDoGyListener = v -> ArticlePage.this.onGYButtonClicked();

    // 切換模式
    private final View.OnClickListener mChangeModeListener = v -> {
        ArticlePage.this.changeViewMode();
        ArticlePage.this.refreshExternalToolbar();
    };

    // 開啟連結
    private final View.OnClickListener mShowLinkListener = v -> ArticlePage.this.onOpenLinkClicked();

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
        this._settings = new UserSettings(getContext());
        this._telnet_view = (TelnetView) findViewById(R.id.Article_contentTelnetView);
        reloadTelnetLayout();
        View empty_view = findViewById(R.id.Article_contentEmptyView);
        ASListView list_view = (ASListView) findViewById(R.id.Article_contentList);
        list_view.setEmptyView(empty_view);
        list_view.setAdapter(this._list_adapter);
        list_view.setOnItemLongClickListener(this._list_long_click_listener);
        Button back_button = (Button) findViewById(R.id.Article_backButton);
        Button page_up_button = (Button) findViewById(R.id.Article_pageUpButton);
        Button page_down_button = (Button) findViewById(R.id.Article_pageDownButton);
        back_button.setOnClickListener(this._back_listener);
        page_up_button.setOnClickListener(this._page_up_listener);
        page_up_button.setOnLongClickListener(this._page_top_listener);
        page_down_button.setOnClickListener(this._page_down_listener);
        page_down_button.setOnLongClickListener(this._page_bottom_listener);
        Button do_gy_button = (Button) findViewById(R.id.do_gy);
        if (do_gy_button != null) {
            do_gy_button.setOnClickListener(this.mDoGyListener);
        }
        Button change_mode_button = (Button) findViewById(R.id.change_mode);
        if (change_mode_button != null) {
            change_mode_button.setOnClickListener(this.mChangeModeListener);
        }
        Button show_link_button = (Button) findViewById(R.id.show_link);
        if (show_link_button != null) {
            show_link_button.setOnClickListener(this.mShowLinkListener);
        }
        if (this._telnet_view.getFrame() == null && this._article != null) {
            this._telnet_view.setFrame(this._article.getFrame());
        }
        refreshExternalToolbar();
        showNotification();

        // 工具列位置
        changeToolbarLocation();
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

    // 第一次進入的提示訊息
    void showNotification() {
        Activity activity = getNavigationController();
        if (activity != null) {
            SharedPreferences perf = activity.getSharedPreferences("notification", 0);
            boolean show_top_bottom_function = perf.getBoolean("show_top_bottom_function", false);
            if (!show_top_bottom_function) {
                Toast.makeText(activity, (int) R.string.article_top_bottom_function_notificaiton, Toast.LENGTH_LONG).show();
                perf.edit().putBoolean("show_top_bottom_function", true).commit();
            }
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
            String logon_user = this._settings.getPropertiesUsername().trim().toLowerCase();
            boolean is_board = this._board_page.getPageType() == BahamutPage.BAHAMUT_BOARD;
            boolean ext_toolbar_enable = this._settings.getPropertiesExternalToolbarEnable();
            String external_toolbar_enable_title = ext_toolbar_enable ? "隱藏工具列" : "開啟工具列";
            ASListDialog.createDialog().addItem(getContextString(R.string.do_gy)).addItem("切換模式").addItem((is_board && author.equals(logon_user)) ? "編輯文章" : null).addItem(author.equals(logon_user) ? "刪除文章" : null).addItem(external_toolbar_enable_title).addItem("加入黑名單").addItem("開啟連結").setListener(new ASListDialogItemClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.1
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    switch (index) {
                        case 0:
                            ArticlePage.this.onGYButtonClicked();
                            return;
                        case 1:
                            ArticlePage.this.changeViewMode();
                            ArticlePage.this.refreshExternalToolbar();
                            return;
                        case 2:
                            ArticlePage.this.onEditButtonClicked();
                            return;
                        case 3:
                            ArticlePage.this.onDeleteButtonClicked();
                            return;
                        case 4:
                            ArticlePage.this.onExternalToolbarClicked();
                            return;
                        case 5:
                            ArticlePage.this.onAddBlackListClicked();
                            return;
                        case 6:
                            ArticlePage.this.onOpenLinkClicked();
                            return;
                        default:
                    }
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    public void setArticle(TelnetArticle aArticle) {
        this._article = aArticle;
        if (this._article != null) {
            String board_name = this._board_page.getListName();
            BookmarkStore store = new BookmarkStore(getContext());
            BookmarkList bookmark_list = store.getBookmarkList(board_name);
            bookmark_list.addHistoryBookmark(this._article.Title);
            store.store();
            this._telnet_view.setFrame(this._article.getFrame());
            reloadTelnetLayout();
            ASScrollView telnet_content_view = (ASScrollView) findViewById(R.id.Article_contentTelnetViewBlock);
            if (telnet_content_view != null) {
                telnet_content_view.scrollTo(0, 0);
            }
            this._list_adapter.notifyDataSetChanged();
        }
        ASProcessingDialog.hideProcessingDialog();
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

    public void onDeleteButtonClicked() {
        if (this._article != null && this._board_page != null) {
            final int item_number = this._article.Number;
            // from class: com.kota.Bahamut.Pages.ArticlePage.9
// com.kota.ASFramework.Dialog.ASAlertDialogListener
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
                        ArticlePage.this._board_page.pushCommand(command);
                        ArticlePage.this.onBackPressed();
                }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    public void onEditButtonClicked() {
        if (this._article != null) {
            PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
            String edit_title = this._article.generateEditTitle();
            String edit_content = this._article.generateEditContent();
            String edit_format = this._article.generatrEditFormat();
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
        this._settings.exchangeArticleViewMode();
        this._settings.notifyDataUpdated();
        reloadViewMode();
    }

    private void reloadViewMode() {
        ViewGroup text_content_view = (ViewGroup) findViewById(R.id.Article_TextContentView);
        ASScrollView telnet_content_view = (ASScrollView) findViewById(R.id.Article_contentTelnetViewBlock);
        // 文字模式
        if (this._settings.getPropertiesArticleViewMode() == 0) {
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
        if (this._settings.getPropertiesArticleViewMode() == 0 || this._full_screen) {
            if (this._settings.getPropertiesGestureOnBoardEnable())
                onBackPressed();
            return true;
        }
        return true;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public boolean isKeepOnOffline() {
        return true;
    }

    public void setBoardPage(BoardPage aBoardPage) {
        this._board_page = aBoardPage;
    }

    public void onExternalToolbarClicked() {
        boolean enable = this._settings.getPropertiesExternalToolbarEnable();
        this._settings.setPropertiesExternalToolbarEnable(!enable);
        refreshExternalToolbar();
    }

    private void refreshExternalToolbar() {
        boolean enable = this._settings.getPropertiesExternalToolbarEnable();
        int article_mode = this._settings.getPropertiesArticleViewMode();
        if (article_mode == 1) {
            enable = true;
        }
        System.out.println("enable:" + enable);
        System.out.println("article_mode:" + article_mode);
        View toolbar_view = findViewById(R.id.ext_toolbar);
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
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "此文章內容未包含連結", Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            }
            ASListDialog list_dialog = ASListDialog.createDialog();
            for (URLSpan urlspan : urls) {
                list_dialog.addItem(urlspan.getURL());
            }
            list_dialog.setListener(new ASListDialogItemClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.14
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    String url2 = urls[index].getURL();
                    Context context2 = ArticlePage.this.getContext();
                    if (context2 != null) {
                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url2));
                        context2.startActivity(intent);
                    }
                }
            });
            list_dialog.show();
        }
    }

    public void onAddBlackListClicked() {
        TelnetArticle article = this._article;
        if (article != null) {
            Set<String> buffer = new HashSet<>();
            int len = article.getItemSize();
            for (int i = 0; i < len; i++) {
                TelnetArticleItem item = article.getItem(i);
                String author = item.getAuthor();
                if (author != null && !this._settings.isBlockListContains(author)) {
                    buffer.add(author);
                }
            }
            if (buffer.size() == 0) {
                ASAlertDialog.createDialog().setMessage("無可加入黑名單的ID").show();
                return;
            }
            final String[] names = (String[]) buffer.toArray(new String[buffer.size()]);
            ASListDialog.createDialog().addItems(names).setListener(new ASListDialogItemClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.15
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    ArticlePage.this.onBlockButtonClicked(names[index]);
                }
            }).show();
        }
    }

    public void onBlockButtonClicked(final String aBlockName) {
        // from class: com.kota.Bahamut.Pages.ArticlePage.16
// com.kota.ASFramework.Dialog.ASAlertDialogListener
        ASAlertDialog.createDialog().setTitle("加入黑名單").setMessage("是否要將\"" + aBlockName + "\"加入黑名單?").addButton("取消").addButton("加入").setListener((aDialog, index) -> {
            if (index == 1) {
                ArticlePage.this._settings.addBlockName(aBlockName);
                ArticlePage.this._settings.notifyDataUpdated();
                if (ArticlePage.this._settings.getPropertiesBlockListEnable()) {
                    if (aBlockName.equals(ArticlePage.this._article.Author)) {
                        ArticlePage.this.onBackPressed();
                    } else {
                        ArticlePage.this._list_adapter.notifyDataSetChanged();
                    }
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }
}
