package com.kota.Bahamut.Pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASScrollView;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kota.Bahamut.Command.TelnetCommand;
import com.kota.Bahamut.DataModels.BookmarkList;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Article.ArticlePage_HeaderItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TelnetItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TextItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TimeTimeView;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class ArticlePage extends TelnetPage {
    UserSettings _settings;
    private TelnetArticle _article = null;
    private TelnetView _telnet_view = null;
    private BoardPage _board_page = null;
    private boolean _full_screen = false;
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
            return getItem(itemIndex).getType();
        }

        @Override // android.widget.Adapter
        public View getView(int itemIndex, View itemView, ViewGroup parentView) {
            int type = getItemViewType(itemIndex);
            if (itemView == null) {
                switch (type) {
                    case 0:
                        itemView = new ArticlePage_TextItemView(ArticlePage.this.getContext());
                        break;
                    case 1:
                        itemView = new ArticlePage_TelnetItemView(ArticlePage.this.getContext());
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
                    TelnetArticleItem item = getItem(itemIndex);
                    ArticlePage_TextItemView item_view = (ArticlePage_TextItemView) itemView;
                    item_view.setAuthor(item.getAuthor(), item.getNickname());
                    item_view.setQuote(item.getQuoteLevel());
                    item_view.setContent(item.getContent());
                    if (itemIndex < getCount() - 2) {
                        item_view.setDividerhidden(false);
                    } else {
                        item_view.setDividerhidden(true);
                    }
                    if (ArticlePage.this._settings.isBlockListEnable() && ArticlePage.this._settings.isBlockListContains(item.getAuthor())) {
                        item_view.setVisible(false);
                        break;
                    } else {
                        item_view.setVisible(true);
                        break;
                    }
                case 1:
                    ArticlePage_TelnetItemView item_view2 = (ArticlePage_TelnetItemView) itemView;
                    item_view2.setFrame(getItem(itemIndex).getFrame());
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
    AdapterView.OnItemLongClickListener _list_long_click_listener = new AdapterView.OnItemLongClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.3
        @Override // android.widget.AdapterView.OnItemLongClickListener
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int itemIndex, long arg3) {
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
        }
    };
    View.OnLongClickListener _page_top_listener = new View.OnLongClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.4
        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View v) {
            if (ArticlePage.this._settings.isArticleMoveEnsable()) {
                if (ArticlePage.this._top_action != null) {
                    v.removeCallbacks(ArticlePage.this._top_action);
                    ArticlePage.this._top_action = null;
                }
                ArticlePage.this._top_action = new Runnable() { // from class: com.kota.Bahamut.Pages.ArticlePage.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ArticlePage.this._top_action = null;
                        ArticlePage.this.moveToTopArticle();
                    }
                };
                v.postDelayed(ArticlePage.this._top_action, ArticlePage.this._action_delay);
            }
            return false;
        }
    };
    View.OnClickListener _page_up_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.5
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (ArticlePage.this._top_action != null) {
                v.removeCallbacks(ArticlePage.this._top_action);
                ArticlePage.this._top_action = null;
            }
            if (!TelnetClient.getConnector().isConnecting() || ArticlePage.this._board_page == null) {
                ArticlePage.this.showConnectionClosedToast();
            } else {
                ArticlePage.this._board_page.loadTheSameTitleUp();
            }
        }
    };
    View.OnLongClickListener _page_bottom_listener = new View.OnLongClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.6
        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View v) {
            if (ArticlePage.this._settings.isArticleMoveEnsable()) {
                if (ArticlePage.this._bottom_action != null) {
                    v.removeCallbacks(ArticlePage.this._bottom_action);
                    ArticlePage.this._bottom_action = null;
                }
                ArticlePage.this._bottom_action = new Runnable() { // from class: com.kota.Bahamut.Pages.ArticlePage.6.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ArticlePage.this._bottom_action = null;
                        ArticlePage.this.moveToBottomArticle();
                    }
                };
                v.postDelayed(ArticlePage.this._bottom_action, ArticlePage.this._action_delay);
            }
            return false;
        }
    };
    View.OnClickListener _page_down_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.7
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (ArticlePage.this._bottom_action != null) {
                v.removeCallbacks(ArticlePage.this._bottom_action);
                ArticlePage.this._bottom_action = null;
            }
            if (!TelnetClient.getConnector().isConnecting() || ArticlePage.this._board_page == null) {
                ArticlePage.this.showConnectionClosedToast();
            } else {
                ArticlePage.this._board_page.loadTheSameTitleDown();
            }
        }
    };
    View.OnClickListener _back_listener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.8
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (TelnetClient.getConnector().isConnecting()) {
                if (ArticlePage.this._article != null) {
                    PostArticlePage page = new PostArticlePage();
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
        }
    };
    private final View.OnClickListener mMenuListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.10
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            ArticlePage.this.onMenuClicked();
        }
    };
    private final View.OnClickListener mDoGyListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.11
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            ArticlePage.this.onGYButtonClicked();
        }
    };
    private final View.OnClickListener mChangeModeListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.12
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            ArticlePage.this.changeViewMode();
            ArticlePage.this.refreshExternalToolbar();
        }
    };
    private final View.OnClickListener mShowLinkListener = new View.OnClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.13
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            ArticlePage.this.onOpenLinkClicked();
        }
    };

    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return 14;
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
        list_view.setAdapter((ListAdapter) this._list_adapter);
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
    }

    void showNotification() {
        Activity activity = getNavigationController();
        if (activity != null) {
            SharedPreferences perf = activity.getSharedPreferences("notification", 0);
            boolean show_top_bottom_function = perf.getBoolean("show_top_bottom_function", false);
            if (!show_top_bottom_function) {
                Toast.makeText(activity, (int) R.string.article_top_bottom_function_notificaiton, Toast.LENGTH_LONG).show();
                perf.edit().putBoolean("show_top_bottom_function", true).apply();
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
            String logon_user = this._settings.getUsername().trim().toLowerCase();
            boolean is_board = this._board_page.getPageType() == 10;
            boolean ext_toolbar_enable = this._settings.isExternalToolbarEnable();
            String external_toolbar_enable_title = ext_toolbar_enable ? "隱藏工具列" : "開啟工具列";
            ASListDialog.createDialog().addItem("推薦").addItem("切換模式").addItem((is_board && author.equals(logon_user)) ? "編輯文章" : null).addItem(author.equals(logon_user) ? "刪除文章" : null).addItem(external_toolbar_enable_title).addItem("加入黑名單").addItem("開啟連結").setListener(new ASListDialogItemClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.1
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
        telnet_layout.height = -2;
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
            ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此文章?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.9
                @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
                public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                    switch (index) {
                        case 0:
                        default:
                            return;
                        case 1:
                            TelnetCommand command = new BahamutCommandDeleteArticle(item_number);
                            ArticlePage.this._board_page.pushCommand(command);
                            ArticlePage.this.onBackPressed();
                    }
                }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    public void onEditButtonClicked() {
        if (this._article != null) {
            PostArticlePage page = new PostArticlePage();
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
        if (this._settings.getArticleViewMode() == 0) {
            if (text_content_view != null) {
                text_content_view.setVisibility(View.VISIBLE);
            }
            if (telnet_content_view != null) {
                telnet_content_view.setVisibility(View.GONE);
                return;
            }
            return;
        }
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
        if (this._settings.getArticleViewMode() == 0 || this._full_screen) {
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
        boolean enable = this._settings.isExternalToolbarEnable();
        this._settings.setExternalToolbarEnable(!enable);
        refreshExternalToolbar();
    }

    private void refreshExternalToolbar() {
        boolean enable = this._settings.isExternalToolbarEnable();
        int article_mode = this._settings.getArticleViewMode();
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
            final String[] urls = this._article.getUrls();
            if (urls.length == 0) {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "此文章內容未包含連結", Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            }
            ASListDialog list_dialog = ASListDialog.createDialog();
            for (String url : urls) {
                list_dialog.addItem(url);
            }
            list_dialog.setListener(new ASListDialogItemClickListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.14
                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }

                @Override // com.kota.ASFramework.Dialog.ASListDialogItemClickListener
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    String url2 = urls[index];
                    if (!url2.startsWith("http://") && !url2.startsWith("https://") && !url2.startsWith("ftp://")) {
                        url2 = url2.matches("([a-zA-Z0-9\\-]+:[a-zA-Z0-9\\-]+@)([a-zA-Z0-9\\-]+)(\\.[a-zA-Z0-9\\-]+){1,9}([/\\\\]([a-zA-Z0-9\\-]+))?([a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+){0,1}){0,1}(\\?([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)|(([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)(&([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?))+)){0,1}") ? "ftp://" + url2 : url2.matches("([a-zA-Z0-9\\-]+@)([a-zA-Z0-9\\-]+)(\\.[a-zA-Z0-9\\-]+){1,9}([/\\\\]([a-zA-Z0-9\\-]+))?([a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+){0,1}){0,1}(\\?([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)|(([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)(&([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?))+)){0,1}") ? "mailto:" + url2 : "http://" + url2;
                    }
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
        ASAlertDialog.createDialog().setTitle("加入黑名單").setMessage("是否要將\"" + aBlockName + "\"加入黑名單?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.Pages.ArticlePage.16
            @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                if (index == 1) {
                    ArticlePage.this._settings.addBlockName(aBlockName);
                    ArticlePage.this._settings.notifyDataUpdated();
                    if (ArticlePage.this._settings.isBlockListEnable()) {
                        if (aBlockName.equals(ArticlePage.this._article.Author)) {
                            ArticlePage.this.onBackPressed();
                        } else {
                            ArticlePage.this._list_adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }
}
