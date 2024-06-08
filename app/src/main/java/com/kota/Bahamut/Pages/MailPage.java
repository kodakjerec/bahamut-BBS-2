package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASScrollView;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePageItemType;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_HeaderItemView;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_TelnetItemView;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_TextItemView;
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_TimeTimeView;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

public class MailPage extends TelnetPage implements ListAdapter, View.OnClickListener, SendMailPage_Listener {
    LinearLayout mainLayout = null;
    TelnetArticle _article = null;
    Button _back_button = null;
    ASListView _list = null;
    Button _page_down_button = null;
    Button _page_up_button = null;
    TextView _list_empty_view = null;
    TelnetView _telnet_view = null;
    ASScrollView _telnet_view_block = null;
    ArticleViewMode _view_mode = ArticleViewMode.MODE_TEXT;
    final DataSetObservable mDataSetObservable = new DataSetObservable();

    enum ArticleViewMode {
        MODE_TEXT,
        MODE_TELNET
    }

    public int getPageLayout() {
        return R.layout.mail_page;
    }

    public int getPageType() {
        return BahamutPage.BAHAMUT_MAIL;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        mainLayout = (LinearLayout) findViewById(R.id.Mail_contentView);

        _telnet_view_block = mainLayout.findViewById(R.id.Mail_contentTelnetViewBlock);
        _telnet_view = mainLayout.findViewById(R.id.Mail_contentTelnetView);
        int screen_width = (((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.0f, getContext().getResources().getDisplayMetrics())) / 2) * 80;
        ViewGroup.LayoutParams telnet_layout = _telnet_view.getLayoutParams();
        telnet_layout.width = screen_width;
        telnet_layout.height = -2;
        _telnet_view.setLayoutParams(telnet_layout);
        _list = mainLayout.findViewById(R.id.Mail_contentList);
        _list_empty_view = mainLayout.findViewById(R.id.Mail_listEmptyView);
        _list.setEmptyView(_list_empty_view);
        _back_button = mainLayout.findViewById(R.id.Mail_backButton);
        _page_up_button = mainLayout.findViewById(R.id.Mail_pageUpButton);
        _page_down_button = mainLayout.findViewById(R.id.Mail_pageDownButton);
        _back_button.setOnClickListener(this);
        _page_up_button.setOnClickListener(this);
        _page_down_button.setOnClickListener(this);
        mainLayout.findViewById(R.id.Mail_changeModeButton).setOnClickListener(this);
        resetAdapter();
    }

    protected boolean onBackPressed() {
        clear();
        return super.onBackPressed();
    }

    public void onPageDidDisappear() {
        _back_button = null;
        _page_up_button = null;
        _page_down_button = null;
        _list = null;
        _telnet_view_block = null;
        _telnet_view = null;
        super.onPageDidDisappear();
    }

    protected boolean onMenuButtonClicked() {
        changeViewMode();
        return true;
    }

    public void setArticle(TelnetArticle aArticle) {
        clear();
        _article = aArticle;
        _telnet_view.setFrame(_article.getFrame());
        _telnet_view.setLayoutParams(_telnet_view.getLayoutParams());
        _telnet_view_block.scrollTo(0, 0);
        resetAdapter();
    }

    public void resetAdapter() {
        if (_article != null) {
            _list.setAdapter(this);
        }
    }

    public int getCount() {
        if (_article != null) {
            return _article.getItemSize() + 2;
        }
        return 0;
    }

    public TelnetArticleItem getItem(int itemIndex) {
        return _article.getItem(itemIndex - 1);
    }

    public long getItemId(int itemIndex) {
        return itemIndex;
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
                case ArticlePageItemType.Content ->
                        itemView = new ArticlePage_TextItemView(getContext());
                case ArticlePageItemType.Sign ->
                        itemView = new ArticlePage_TelnetItemView(getContext());
                case ArticlePageItemType.Header ->
                        itemView = new ArticlePage_HeaderItemView(getContext());
                case ArticlePageItemType.PostTime ->
                        itemView = new ArticlePage_TimeTimeView(getContext());
            }
        }
        switch (type) {
            case ArticlePageItemType.Content -> {
                TelnetArticleItem item = getItem(itemIndex);
                ArticlePage_TextItemView item_view = (ArticlePage_TextItemView) itemView;
                item_view.setAuthor(item.getAuthor(), item.getNickname());
                item_view.setQuote(item.getQuoteLevel());
                item_view.setContent(item.getContent(), item.getFrame().rows);
                if (itemIndex >= getCount() - 2) {
                    item_view.setDividerHidden(true);
                } else {
                    item_view.setDividerHidden(false);
                }
            }
            case ArticlePageItemType.Sign -> {
                ArticlePage_TelnetItemView item_view2 = (ArticlePage_TelnetItemView) itemView;
                item_view2.setFrame(getItem(itemIndex).getFrame());
                if (itemIndex >= getCount() - 2) {
                    item_view2.setDividerhidden(true);
                } else {
                    item_view2.setDividerhidden(false);
                }
            }
            case ArticlePageItemType.Header -> {
                ArticlePage_HeaderItemView item_view3 = (ArticlePage_HeaderItemView) itemView;
                String author = _article.Author;
                if (_article.Nickname != null) {
                    author = author + "(" + _article.Nickname + ")";
                }
                item_view3.setData(_article.Title, author, _article.BoardName);
            }
            case ArticlePageItemType.PostTime ->
                    ((ArticlePage_TimeTimeView) itemView).setTime("《" + _article.DateTime + "》");
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
        return false;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int itemIndex) {
        return false;
    }

    public void clear() {
        _article = null;
    }

    public void onClick(View aView) {
        if (aView == _back_button) {
            onReplyButtonClicked();
        } else if (aView == _page_up_button) {
            onPageUpButtonClicked();
        } else if (aView == _page_down_button) {
            onPageDownButtonClicked();
        } else if (aView.getId() == R.id.Mail_changeModeButton) {
            changeViewMode();
        }
    }

    public void onSendMailDialogSendButtonClicked(SendMailPage aDialog, String receiver, String title, String content) {
        PageContainer.getInstance().getMailBoxPage().onSendMailDialogSendButtonClicked(aDialog, receiver, title, content);
        onBackPressed();
    }

    void onPageUpButtonClicked() {
        if (TelnetClient.getConnector().isConnecting()) {
            PageContainer.getInstance().getMailBoxPage().loadPreviousArticle();
        } else {
            showConnectionClosedToast();
        }
    }

    void onPageDownButtonClicked() {
        if (TelnetClient.getConnector().isConnecting()) {
            PageContainer.getInstance().getMailBoxPage().loadNextArticle();
        } else {
            showConnectionClosedToast();
        }
    }

    void showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷");
    }

    void onReplyButtonClicked() {
        SendMailPage send_mail_page = new SendMailPage();
        String reply_title = _article.generateReplyTitle();
        String reply_content = _article.generateReplyContent();
        send_mail_page.setPostTitle(reply_title);
        send_mail_page.setPostContent(reply_content);
        send_mail_page.setReceiver(_article.Author);
        send_mail_page.setListener(this);
        getNavigationController().pushViewController(send_mail_page);
    }

    public void changeViewMode() {
        if (_view_mode == ArticleViewMode.MODE_TEXT) {
            _view_mode = ArticleViewMode.MODE_TELNET;
        } else {
            _view_mode = ArticleViewMode.MODE_TEXT;
        }
        if (_view_mode == ArticleViewMode.MODE_TEXT) {
            _list.setVisibility(View.VISIBLE);
            _telnet_view_block.setVisibility(View.GONE);
            return;
        }
        _list.setVisibility(View.GONE);
        _telnet_view_block.setVisibility(View.VISIBLE);
        _telnet_view_block.invalidate();
    }

    public boolean onReceivedGestureRight() {
        if (_view_mode != ArticleViewMode.MODE_TEXT) {
            return true;
        }
        onBackPressed();
        return true;
    }

    public void refresh() {
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    // 給 state handler 更改讀取進度
    @SuppressLint("SetTextI18n")
    public void changeLoadingPercentage(String percentage) {
        _list_empty_view.setText(getContextString(R.string.loading_)+percentage);
    }
}
