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
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Article.ArticlePage_HeaderItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TelnetItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TextItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TimeTimeView;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;
import java.util.HashSet;
import java.util.Set;

public class ArticlePage extends TelnetPage {
    long _action_delay = 500;
    /* access modifiers changed from: private */
    public TelnetArticle _article = null;
    View.OnClickListener _back_listener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!TelnetClient.getConnector().isConnecting()) {
                ArticlePage.this.showConnectionClosedToast();
            } else if (ArticlePage.this._article != null) {
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
            }
        }
    };
    /* access modifiers changed from: private */
    public BoardPage _board_page = null;
    Runnable _bottom_action = null;
    private boolean _full_screen = false;
    BaseAdapter _list_adapter = new BaseAdapter() {
        public int getCount() {
            if (ArticlePage.this._article != null) {
                return ArticlePage.this._article.getItemSize() + 2;
            }
            return 0;
        }

        public TelnetArticleItem getItem(int itemIndex) {
            if (ArticlePage.this._article == null) {
                return null;
            }
            return ArticlePage.this._article.getItem(itemIndex - 1);
        }

        public long getItemId(int itemIndex) {
            return (long) itemIndex;
        }

        public int getItemViewType(int itemIndex) {
            if (itemIndex == 0) {
                return 2;
            }
            if (itemIndex == getCount() - 1) {
                return 3;
            }
            return getItem(itemIndex).getType();
        }

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
                    if (itemIndex >= getCount() - 2) {
                        item_view2.setDividerhidden(true);
                        break;
                    } else {
                        item_view2.setDividerhidden(false);
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

        public int getViewTypeCount() {
            return 4;
        }

        public boolean hasStableIds() {
            return false;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int itemIndex) {
            int type = getItemViewType(itemIndex);
            if (type == 0 || type == 1) {
                return true;
            }
            return false;
        }
    };
    AdapterView.OnItemLongClickListener _list_long_click_listener = new AdapterView.OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int itemIndex, long arg3) {
            TelnetArticleItem item;
            if (ArticlePage.this._article == null || (item = ArticlePage.this._article.getItem(itemIndex - 1)) == null) {
                return false;
            }
            int type = item.getType();
            if (type == 0) {
                item.setType(1);
                ArticlePage.this._list_adapter.notifyDataSetChanged();
                return true;
            } else if (type != 1) {
                return false;
            } else {
                item.setType(0);
                ArticlePage.this._list_adapter.notifyDataSetChanged();
                return true;
            }
        }
    };
    View.OnLongClickListener _page_bottom_listener = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            if (!ArticlePage.this._settings.isArticleMoveDisable()) {
                if (ArticlePage.this._bottom_action != null) {
                    v.removeCallbacks(ArticlePage.this._bottom_action);
                    ArticlePage.this._bottom_action = null;
                }
                ArticlePage.this._bottom_action = new Runnable() {
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
    View.OnClickListener _page_down_listener = new View.OnClickListener() {
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
    View.OnLongClickListener _page_top_listener = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            if (!ArticlePage.this._settings.isArticleMoveDisable()) {
                if (ArticlePage.this._top_action != null) {
                    v.removeCallbacks(ArticlePage.this._top_action);
                    ArticlePage.this._top_action = null;
                }
                ArticlePage.this._top_action = new Runnable() {
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
    View.OnClickListener _page_up_listener = new View.OnClickListener() {
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
    UserSettings _settings;
    private TelnetView _telnet_view = null;
    Runnable _top_action = null;
    private View.OnClickListener mChangeModeListener = new View.OnClickListener() {
        public void onClick(View v) {
            ArticlePage.this.changeViewMode();
            ArticlePage.this.refreshExternalToolbar();
        }
    };
    private View.OnClickListener mDoGyListener = new View.OnClickListener() {
        public void onClick(View v) {
            ArticlePage.this.onGYButtonClicked();
        }
    };
    /* access modifiers changed from: private */
    public View.OnClickListener mMenuListener = new View.OnClickListener() {
        public void onClick(View v) {
            ArticlePage.this.onMenuClicked();
        }
    };
    private View.OnClickListener mShowLinkListener = new View.OnClickListener() {
        public void onClick(View v) {
            ArticlePage.this.onOpenLinkClicked();
        }
    };

    public int getPageType() {
        return 14;
    }

    public int getPageLayout() {
        return R.layout.article_page;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        this._settings = new UserSettings(getContext());
        this._telnet_view = (TelnetView) findViewById(R.id.Article_ContentTelnetView);
        reloadTelnetLayout();
        View empty_view = findViewById(R.id.Article_ContentEmptyView);
        ASListView list_view = (ASListView) findViewById(R.id.Article_ContentList);
        list_view.setEmptyView(empty_view);
        list_view.setAdapter(this._list_adapter);
        list_view.setOnItemLongClickListener(this._list_long_click_listener);
        Button page_up_button = (Button) findViewById(R.id.Article_PageUpButton);
        Button page_down_button = (Button) findViewById(R.id.Article_PageDownButton);
        ((Button) findViewById(R.id.Article_BackButton)).setOnClickListener(this._back_listener);
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

    /* access modifiers changed from: package-private */
    public void showNotification() {
        Activity activity = getNavigationController();
        if (activity != null) {
            SharedPreferences perf = activity.getSharedPreferences("notification", 0);
            if (!perf.getBoolean("show_top_bottom_function", false)) {
                Toast.makeText(activity, R.string.article_top_bottom_function_notificaiton, 1).show();
                perf.edit().putBoolean("show_top_bottom_function", true).commit();
            }
        }
    }

    public void onPageWillAppear() {
        reloadViewMode();
    }

    public void onPageDidDisappear() {
        this._telnet_view = null;
        super.onPageDidDisappear();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        getNavigationController().popViewController();
        PageContainer.getInstance().cleanArticlePage();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean onMenuButtonClicked() {
        onMenuClicked();
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onMenuClicked() {
        String str;
        String str2 = null;
        if (this._article != null && this._article.Author != null) {
            String author = this._article.Author.toLowerCase();
            String logon_user = this._settings.getUsername().trim().toLowerCase();
            boolean is_board = this._board_page.getPageType() == 10;
            String external_toolbar_enable_title = this._settings.isExternalToolbarEnable() ? "隱藏工具列" : "開啟工具列";
            ASListDialog addItem = ASListDialog.createDialog().addItem("推薦").addItem("切換模式");
            if (!is_board || !author.equals(logon_user)) {
                str = null;
            } else {
                str = "編輯文章";
            }
            ASListDialog addItem2 = addItem.addItem(str);
            if (author.equals(logon_user)) {
                str2 = "刪除文章";
            }
            addItem2.addItem(str2).addItem(external_toolbar_enable_title).addItem("加入黑名單").addItem("開啟連結").setListener(new ASListDialogItemClickListener() {
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
                            return;
                    }
                }

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
            store.getBookmarkList(board_name).addHistoryBookmark(this._article.Title);
            store.store();
            this._telnet_view.setFrame(this._article.getFrame());
            reloadTelnetLayout();
            ASScrollView telnet_content_view = (ASScrollView) findViewById(R.id.Article_ContentTelnetViewBlock);
            if (telnet_content_view != null) {
                telnet_content_view.scrollTo(0, 0);
            }
            this._list_adapter.notifyDataSetChanged();
        }
        ASProcessingDialog.hideProcessingDialog();
    }

    private void reloadTelnetLayout() {
        int screen_width;
        int telnet_view_width = (((int) TypedValue.applyDimension(2, 20.0f, getContext().getResources().getDisplayMetrics())) / 2) * 80;
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

    /* access modifiers changed from: package-private */
    public void moveToTopArticle() {
        if (!TelnetClient.getConnector().isConnecting() || this._board_page == null) {
            showConnectionClosedToast();
        } else {
            this._board_page.loadTheSameTitleTop();
        }
    }

    /* access modifiers changed from: package-private */
    public void moveToBottomArticle() {
        if (!TelnetClient.getConnector().isConnecting() || this._board_page == null) {
            showConnectionClosedToast();
        } else {
            this._board_page.loadTheSameTitleBottom();
        }
    }

    /* access modifiers changed from: private */
    public void showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷");
    }

    /* access modifiers changed from: private */
    public void onGYButtonClicked() {
        if (this._board_page != null) {
            this._board_page.goodLoadingArticle();
        }
    }

    public void onDeleteButtonClicked() {
        if (this._article != null && this._board_page != null) {
            final int item_number = this._article.Number;
            ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此文章?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() {
                public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                    switch (index) {
                        case 1:
                            ArticlePage.this._board_page.pushCommand(new BahamutCommandDeleteArticle(item_number));
                            ArticlePage.this.onBackPressed();
                            return;
                        default:
                            return;
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
        ASScrollView telnet_content_view = (ASScrollView) findViewById(R.id.Article_ContentTelnetViewBlock);
        if (this._settings.getArticleViewMode() == 0) {
            if (text_content_view != null) {
                text_content_view.setVisibility(0);
            }
            if (telnet_content_view != null) {
                telnet_content_view.setVisibility(8);
                return;
            }
            return;
        }
        if (text_content_view != null) {
            text_content_view.setVisibility(8);
        }
        if (telnet_content_view != null) {
            telnet_content_view.setVisibility(0);
            telnet_content_view.invalidate();
        }
    }

    public boolean onReceivedGestureRight() {
        if (this._settings.getArticleViewMode() != 0 && !this._full_screen) {
            return true;
        }
        onBackPressed();
        return true;
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    public void setBoardPage(BoardPage aBoardPage) {
        this._board_page = aBoardPage;
    }

    public void onExternalToolbarClicked() {
        this._settings.setExternalToolbarEnable(!this._settings.isExternalToolbarEnable());
        refreshExternalToolbar();
    }

    /* access modifiers changed from: private */
    public void refreshExternalToolbar() {
        boolean enable = this._settings.isExternalToolbarEnable();
        int article_mode = this._settings.getArticleViewMode();
        if (article_mode == 1) {
            enable = true;
        }
        System.out.println("enable:" + enable);
        System.out.println("article_mode:" + article_mode);
        View toolbar_view = findViewById(R.id.ext_toolbar);
        if (toolbar_view != null) {
            toolbar_view.setVisibility(enable ? 0 : 8);
        }
    }

    /* access modifiers changed from: private */
    public void onOpenLinkClicked() {
        if (this._article != null) {
            final String[] urls = this._article.getUrls();
            if (urls.length == 0) {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "此文章內容未包含連結", 0).show();
                    return;
                }
                return;
            }
            ASListDialog list_dialog = ASListDialog.createDialog();
            for (String url : urls) {
                list_dialog.addItem(url);
            }
            list_dialog.setListener(new ASListDialogItemClickListener() {
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }

                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    String url = urls[index];
                    if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://")) {
                        url = url.matches("([a-zA-Z0-9\\-]+:[a-zA-Z0-9\\-]+@)([a-zA-Z0-9\\-]+)(\\.[a-zA-Z0-9\\-]+){1,9}([/\\\\]([a-zA-Z0-9\\-]+))?([a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+){0,1}){0,1}(\\?([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)|(([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)(&([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?))+)){0,1}") ? "ftp://" + url : url.matches("([a-zA-Z0-9\\-]+@)([a-zA-Z0-9\\-]+)(\\.[a-zA-Z0-9\\-]+){1,9}([/\\\\]([a-zA-Z0-9\\-]+))?([a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+){0,1}){0,1}(\\?([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)|(([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)(&([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?))+)){0,1}") ? "mailto:" + url : "http://" + url;
                    }
                    Context context = ArticlePage.this.getContext();
                    if (context != null) {
                        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
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
                String author = article.getItem(i).getAuthor();
                if (author != null && !this._settings.isBlockListContains(author)) {
                    buffer.add(author);
                }
            }
            if (buffer.size() == 0) {
                ASAlertDialog.createDialog().setMessage("無可加入黑名單的ID").show();
                return;
            }
            final String[] names = (String[]) buffer.toArray(new String[buffer.size()]);
            ASListDialog.createDialog().addItems(names).setListener(new ASListDialogItemClickListener() {
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }

                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    ArticlePage.this.onBlockButtonClicked(names[index]);
                }
            }).show();
        }
    }

    public void onBlockButtonClicked(final String aBlockName) {
        ASAlertDialog.createDialog().setTitle("加入黑名單").setMessage("是否要將\"" + aBlockName + "\"加入黑名單?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() {
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                if (index == 1) {
                    ArticlePage.this._settings.addBlockName(aBlockName);
                    ArticlePage.this._settings.notifyDataUpdated();
                    if (!ArticlePage.this._settings.isBlockListEnable()) {
                        return;
                    }
                    if (aBlockName == ArticlePage.this._article.Author) {
                        ArticlePage.this.onBackPressed();
                    } else {
                        ArticlePage.this._list_adapter.notifyDataSetChanged();
                    }
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }
}
