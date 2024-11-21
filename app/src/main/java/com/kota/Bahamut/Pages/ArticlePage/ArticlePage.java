package com.kota.Bahamut.Pages.ArticlePage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
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
import com.kota.Bahamut.Dialogs.DialogQueryHero;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage;
import com.kota.Bahamut.Pages.Model.ToolBarFloating;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetArticlePush;
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
    TelnetArticle telnetArticle = null;
    TelnetView telnetView = null;
    TextView listEmptyView = null;
    BoardMainPage _board_page = null;
    boolean isFullScreen = false;
    long _action_delay = 500;
    Runnable _top_action = null;
    Runnable _bottom_action = null;
    BaseAdapter listAdapter = new BaseAdapter() {
        private int pushLength = 0; // 推文長度
        @Override // android.widget.Adapter
        public int getCount() {
            if (telnetArticle != null) {
                pushLength = telnetArticle.getPushSize();
                // 內文個數 + header + PostTime + push
                return telnetArticle.getItemSize() + 2 + pushLength;
            }
            return 0;
        }

        @Override // android.widget.Adapter
        public TelnetArticleItem getItem(int itemIndex) {
            if (telnetArticle == null) {
                return null;
            }
            return telnetArticle.getItem(itemIndex - 1);
        }

        @Override // android.widget.Adapter
        public long getItemId(int itemIndex) {
            return itemIndex;
        }

        @Override 
        public int getItemViewType(int itemIndex) {
            if (itemIndex == 0) {
                // header
                return ArticlePageItemType.Header;
            }
            else if (itemIndex == getCount() - 1 - pushLength) {
                // postTime
                return ArticlePageItemType.PostTime;
            }
            else if (itemIndex >= getCount() - pushLength) {
                // push
                return ArticlePageItemType.Push;
            }
            // content
            return Objects.requireNonNull(getItem(itemIndex)).getType();
        }

        @Override // android.widget.Adapter
        public View getView(int itemIndex, View itemViewFrom, ViewGroup parentView) {
            int type = getItemViewType(itemIndex);
            TelnetArticleItem item = getItem(itemIndex);
            // 2-標題 0-本文 1-簽名檔 3-發文時間 4-推文
            View itemViewOrigin = itemViewFrom;

            if (itemViewOrigin == null) {
                switch (type) {
                    case ArticlePageItemType.Sign -> itemViewOrigin = new ArticlePage_TelnetItemView(getContext());
                    case ArticlePageItemType.Header -> itemViewOrigin = new ArticlePage_HeaderItemView(getContext());
                    case ArticlePageItemType.PostTime -> itemViewOrigin = new ArticlePage_TimeTimeView(getContext());
                    case ArticlePageItemType.Push -> itemViewOrigin = new ArticlePagePushItemView(getContext());
                    default -> {
                        type = ArticlePageItemType.Content;
                        itemViewOrigin = new ArticlePage_TextItemView(getContext());
                    }
                }
            } else if (type == ArticlePageItemType.Content) {
                itemViewOrigin = new ArticlePage_TextItemView(getContext());
            }

            if (itemViewOrigin instanceof ArticlePage_TextItemView itemView1) {
                if (item != null) {
                    itemView1.setAuthor(item.getAuthor(), item.getNickname());
                    itemView1.setQuote(item.getQuoteLevel());
                    itemView1.setContent(item.getContent(), item.getFrame().rows);
                }
                // 分隔線
                itemView1.setDividerHidden(itemIndex >= getCount() - 2);
                // 黑名單檢查
                itemView1.setVisible(!UserSettings.getPropertiesBlockListEnable() || !UserSettings.isBlockListContains(Objects.requireNonNull(item).getAuthor()));
            }
            else if (itemViewOrigin instanceof ArticlePage_TelnetItemView itemView2) {
                if (item != null)
                    itemView2.setFrame(item.getFrame());
                // 分隔線
                itemView2.setDividerHidden(itemIndex >= getCount() - 2);
            }
            else if (itemViewOrigin instanceof ArticlePage_HeaderItemView itemView3) {
                String author = null;
                String title = null;
                String board_name = null;
                if (telnetArticle != null) {
                    author = telnetArticle.Author;
                    title = telnetArticle.Title;
                    board_name = telnetArticle.BoardName;
                    if (telnetArticle.Nickname != null) {
                        author = author + "(" + telnetArticle.Nickname + ")";
                    }
                }
                itemView3.setData(title, author, board_name);
                itemView3.setMenuButtonClickListener(mMenuListener);
            }
            else if (itemViewOrigin instanceof ArticlePage_TimeTimeView itemView4) {
                itemView4.setTime("《" + telnetArticle.DateTime + "》");
                itemView4.setIP(telnetArticle.fromIP);
            }
            else if (itemViewOrigin instanceof ArticlePagePushItemView itemView5) {
                int tempIndex = itemIndex - (getCount() - pushLength); // itemIndex - 本文長度
                TelnetArticlePush itemPush = telnetArticle.getPush(tempIndex);
                if (itemPush!=null) {
                    itemView5.setContent(itemPush);
                    itemView5.setFloor(tempIndex + 1);
                }
            }
            return itemViewOrigin;
        }

        /** 一共有多少种不同的视图类型 */
        @Override 
        public int getViewTypeCount() {
            return 5;
        }

        @Override 
        public boolean hasStableIds() {
            return false;
        }

        @Override 
        public boolean isEmpty() {
            return getCount() == 0;
        }

        @Override 
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override 
        public boolean isEnabled(int itemIndex) {
            int type = getItemViewType(itemIndex);
            return type == 0 || type == 1;
        }
    };

    /** 長按內文 */
    AdapterView.OnItemLongClickListener listLongClickListener = (AdapterView<?> var1, View view, int itemIndex, long pressTime) -> {
        if (view.getClass().equals(ArticlePage_TelnetItemView.class)) {
            // 開啟切換模式
            TelnetArticleItem item = telnetArticle.getItem(itemIndex - 1);

            int viewMode = item.getType();
            if (viewMode == 0) {
                item.setType(1);
                listAdapter.notifyDataSetChanged();
                return true;
            } else if (viewMode == 1) {
                item.setType(0);
                listAdapter.notifyDataSetChanged();
                return true;
            } else {
                return true;
            }
        }

        // 不是telnetView繼續往下運行事件
        return false;
    };

    /** 最前篇 */
    View.OnLongClickListener pageTopListener = v -> {
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

    /** 上一篇 */
    View.OnClickListener pageUpListener = v -> {
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

    /** 最後篇 */
    View.OnLongClickListener pageBottomListener = v -> {
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

    /** 下一篇 */
    View.OnClickListener pageDownListener = v -> {
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

    /** 選單 */
    final View.OnClickListener mMenuListener = v -> onMenuClicked();

    /** 推薦 */
    final View.OnClickListener mDoGyListener = v -> onGYButtonClicked();

    /** 切換模式 */
    final View.OnClickListener mChangeModeListener = v -> {
        changeViewMode();
        refreshExternalToolbar();
    };

    /** 開啟連結 */
    final View.OnClickListener mShowLinkListener = v -> onOpenLinkClicked();

    /** 靠左對其 */
    View.OnClickListener _btnLL_listener = view -> {
        UserSettings.setPropertiesToolbarLocation(1);
        ArticlePage.this.changeToolbarLocation();
    };
    /** 靠右對其 */
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

        telnetView = mainLayout.findViewById(R.id.Article_contentTelnetView);
        reloadTelnetLayout();
        ASListView listView = mainLayout.findViewById(R.id.Article_contentList);
        listEmptyView = mainLayout.findViewById(R.id.Article_listEmptyView);
        listView.setAdapter(listAdapter);
        listView.setEmptyView(listEmptyView);
        listView.setOnItemLongClickListener(listLongClickListener);

        Button back_button = mainLayout.findViewById(R.id.Article_backButton);
        back_button.setOnClickListener(replyListener);

        Button page_up_button = mainLayout.findViewById(R.id.Article_pageUpButton);
        page_up_button.setOnClickListener(pageUpListener);
        page_up_button.setOnLongClickListener(pageTopListener);

        Button page_down_button = mainLayout.findViewById(R.id.Article_pageDownButton);
        page_down_button.setOnClickListener(pageDownListener);
        page_down_button.setOnLongClickListener(pageBottomListener);

        Button do_gy_button = mainLayout.findViewById(R.id.do_gy);
        if (do_gy_button != null) {
            do_gy_button.setOnClickListener(mDoGyListener);
        }
        Button change_mode_button = mainLayout.findViewById(R.id.change_mode);
        if (change_mode_button != null) {
            change_mode_button.setOnClickListener(mChangeModeListener);
        }
        Button show_link_button = mainLayout.findViewById(R.id.show_link);
        if (show_link_button != null) {
            show_link_button.setOnClickListener(mShowLinkListener);
        }

        mainLayout.findViewById(R.id.BoardPageLLButton).setOnClickListener(_btnLL_listener);
        mainLayout.findViewById(R.id.BoardPageRRButton).setOnClickListener(_btnRR_listener);

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.ext_toolbar));
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));

        if (telnetView.getFrame() == null && telnetArticle != null) {
            telnetView.setFrame(telnetArticle.getFrame());
        }
        refreshExternalToolbar();
        showNotification();

        // 工具列位置
        changeToolbarLocation();
        changeToolbarOrder();
    }

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
                toolBarFloating.setOnClickListenerSetting(replyListener);
                toolBarFloating.setTextSetting(getContextString(R.string.reply));
                // button 1
                toolBarFloating.setOnClickListener1(pageUpListener);
                toolBarFloating.setOnLongClickListener1(pageTopListener);
                toolBarFloating.setText1(getContextString(R.string.prev_article));
                // button 2
                toolBarFloating.setOnClickListener2(pageDownListener);
                toolBarFloating.setOnLongClickListener2(pageBottomListener);
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

    /** 第一次進入的提示訊息 */
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
        telnetView = null;
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
        if (telnetArticle != null && telnetArticle.Author != null) {
            String author = telnetArticle.Author.toLowerCase();
            String logon_user = UserSettings.getPropertiesUsername().trim().toLowerCase();
            boolean is_board = _board_page.getPageType() == BahamutPage.BAHAMUT_BOARD;
            boolean ext_toolbar_enable = UserSettings.getPropertiesExternalToolbarEnable();
            String external_toolbar_enable_title = ext_toolbar_enable ? getContextString(R.string.hide_toolbar) : getContextString(R.string.open_toolbar);
            ASListDialog.createDialog()
                    .addItem(getContextString(R.string.do_gy))
                    .addItem(getContextString(R.string.do_push))
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
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return true;
                }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    /** 載入全部圖片 */
    void onLoadAllImageClicked() {
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

    /** 變更telnetView大小 */
    void reloadTelnetLayout() {
        int screenWidth;
        int textWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20.0f, getContext().getResources().getDisplayMetrics());
        int telnetViewWidth = (textWidth / 2) * 80;
        if (getNavigationController().getCurrentOrientation() == 2) {
            screenWidth = getNavigationController().getScreenHeight();
        } else {
            screenWidth = getNavigationController().getScreenWidth();
        }
        if (telnetViewWidth <= screenWidth) {
            telnetViewWidth = -1;
            isFullScreen = true;
        } else {
            isFullScreen = false;
        }
        ViewGroup.LayoutParams layoutParams = telnetView.getLayoutParams();
        layoutParams.width = telnetViewWidth;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        telnetView.setLayoutParams(layoutParams);
    }

    void moveToTopArticle() {
        if (TelnetClient.getConnector().isConnecting() && _board_page != null) {
            _board_page.loadTheSameTitleTop();
        } else {
            showConnectionClosedToast();
        }
    }

    void moveToBottomArticle() {
        if (TelnetClient.getConnector().isConnecting() && _board_page != null) {
            _board_page.loadTheSameTitleBottom();
        } else {
            showConnectionClosedToast();
        }
    }

    void showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷");
    }

    /** 推薦 */
    void onGYButtonClicked() {
        if (_board_page != null) {
            _board_page.goodLoadingArticle();
        }
    }
    /** 推文 */
    void onPushArticleButtonClicked() {
        if (_board_page != null) {
            _board_page.pushArticle();
        }
    }

    /** 刪除文章 */
    public void onDeleteButtonClicked() {
        if (telnetArticle != null && _board_page != null) {
            final int item_number = telnetArticle.Number;
            ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.string.delete))
                    .setMessage(getContextString(R.string.del_this_article))
                    .addButton(getContextString(R.string.cancel))
                    .addButton(getContextString(R.string.delete))
                    .setListener((aDialog, index) -> {
                        if (index == 1) {
                            TelnetCommand command = new BahamutCommandDeleteArticle(item_number);
                            _board_page.pushCommand(command);
                            onBackPressed();
                        }
            }).scheduleDismissOnPageDisappear(this).show();
        }
    }

    /** 回覆文章 */
    View.OnClickListener replyListener = v -> {
        if (TelnetClient.getConnector().isConnecting()) {
            if (telnetArticle != null) {
                PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
                String reply_title = telnetArticle.generateReplyTitle();
                String reply_content = telnetArticle.generateReplyContent();
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
    public void onEditButtonClicked() {
        if (telnetArticle != null) {
            PostArticlePage page = PageContainer.getInstance().getPostArticlePage();
            String edit_title = telnetArticle.generateEditTitle();
            String edit_content = telnetArticle.generateEditContent();
            String edit_format = telnetArticle.generateEditFormat();
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
    public void changeViewMode() {
        UserSettings.exchangeArticleViewMode();
        UserSettings.notifyDataUpdated();
        reloadViewMode();
    }

    void reloadViewMode() {
        ViewGroup text_content_view = mainLayout.findViewById(R.id.Article_TextContentView);
        ASScrollView telnetViewBlock = mainLayout.findViewById(R.id.Article_contentTelnetViewBlock);
        // 文字模式
        if (UserSettings.getPropertiesArticleViewMode() == ArticleViewMode.MODE_TEXT) {
            if (text_content_view != null) {
                text_content_view.setVisibility(View.VISIBLE);
            }
            if (telnetViewBlock != null) {
                telnetViewBlock.setVisibility(View.GONE);
                return;
            }
            return;
        }

        // telnet模式
        if (text_content_view != null) {
            text_content_view.setVisibility(View.GONE);
        }
        if (telnetViewBlock != null) {
            telnetViewBlock.setVisibility(View.VISIBLE);
            telnetViewBlock.invalidate();
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public boolean onReceivedGestureRight() {
        if (UserSettings.getPropertiesArticleViewMode() == ArticleViewMode.MODE_TEXT || isFullScreen) {
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

    void onExternalToolbarClicked() {
        boolean enable = UserSettings.getPropertiesExternalToolbarEnable();
        UserSettings.setPropertiesExternalToolbarEnable(!enable);
        refreshExternalToolbar();
    }

    void refreshExternalToolbar() {
        boolean enable = UserSettings.getPropertiesExternalToolbarEnable();
        int article_mode = UserSettings.getPropertiesArticleViewMode();
        if (article_mode == ArticleViewMode.MODE_TELNET) {
            enable = true;
        }
        System.out.println("enable:" + enable);
        System.out.println("article_mode:" + article_mode);
        View toolbar_view = mainLayout.findViewById(R.id.ext_toolbar);
        if (toolbar_view != null) {
            toolbar_view.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    void onOpenLinkClicked() {
        if (telnetArticle != null) {
            // 擷取文章內的所有連結
            TextView textView = new TextView(getContext());
            textView.setText(telnetArticle.getFullText());
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
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url2));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
            list_dialog.show();
        }
    }

    /** 加入黑名單 */
    void onAddBlockListClicked() {
        TelnetArticle article = telnetArticle;
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

    void onBlockButtonClicked(final String aBlockName) {
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
                            if (aBlockName.equals(telnetArticle.Author)) {
                                onBackPressed();
                            } else {
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }).scheduleDismissOnPageDisappear(this).show();
    }

    /** 給其他頁面託付使用 */
    public void setBoardPage(BoardMainPage aBoardMainPage) {
        _board_page = aBoardMainPage;
    }

    /** 給其他網頁顯示文章使用 */
    public void setArticle(TelnetArticle aArticle) {
        telnetArticle = aArticle;
        if (telnetArticle != null) {
            String board_name = _board_page.getListName();
            // 加入歷史紀錄
            BookmarkStore store = TempSettings.bookmarkStore;
            if (store!=null) {
                BookmarkList bookmark_list = store.getBookmarkList(board_name);
                bookmark_list.addHistoryBookmark(telnetArticle.Title);
                store.storeWithoutCloud();
            }
            if (telnetView.getFrame() == null && telnetArticle != null) {
                telnetView.setFrame(telnetArticle.getFrame());
            }
            reloadTelnetLayout();
            ASScrollView telnet_content_view = mainLayout.findViewById(R.id.Article_contentTelnetViewBlock);
            if (telnet_content_view != null) {
                telnet_content_view.scrollTo(0, 0);
            }
            listAdapter.notifyDataSetChanged();
        }
        ASProcessingDialog.dismissProcessingDialog();
    }

    /** 給 state handler 更改讀取進度 */
    @SuppressLint("SetTextI18n")
    public void changeLoadingPercentage(String percentage) {
        ASProcessingDialog.showProcessingDialog(getContextString(R.string.loading_)+"\n"+percentage);
    }

    /** 查詢勇者 */
    public void ctrlQUser(Vector<String> fromStrings) {
        try {
            new ASRunner() {
                @Override
                public void run() {
                    DialogQueryHero dialogQueryHero = new DialogQueryHero();
                    dialogQueryHero.show();
                    dialogQueryHero.getData(fromStrings);
                }
            }.runInMainThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
