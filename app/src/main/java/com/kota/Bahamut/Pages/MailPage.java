package com.kota.Bahamut.Pages;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import com.kota.ASFramework.UI.ASListView;
import com.kota.ASFramework.UI.ASScrollView;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Article.ArticlePage_HeaderItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TelnetItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TextItemView;
import com.kota.Bahamut.Pages.Article.ArticlePage_TimeTimeView;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

public class MailPage extends TelnetPage implements ListAdapter, View.OnClickListener, SendMailPage_Listener {
    private TelnetArticle _article = null;
    private Button _back_button = null;
    private ASListView _list = null;
    private Button _page_down_button = null;
    private Button _page_up_button = null;
    private TelnetView _telnet_view = null;
    private ASScrollView _telnet_view_block = null;
    private ArticleViewMode _view_mode = ArticleViewMode.MODE_TEXT;
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    enum ArticleViewMode {
        MODE_TEXT,
        MODE_TELNET
    }

    public int getPageLayout() {
        return R.layout.mail_page;
    }

    public int getPageType() {
        return 15;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        this._telnet_view_block = (ASScrollView) findViewById(R.id.Mail_ContentTelnetViewBlock);
        this._telnet_view = (TelnetView) findViewById(R.id.Mail_ContentTelnetView);
        int screen_width = (((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.0f, getContext().getResources().getDisplayMetrics())) / 2) * 80;
        ViewGroup.LayoutParams telnet_layout = this._telnet_view.getLayoutParams();
        telnet_layout.width = screen_width;
        telnet_layout.height = -2;
        this._telnet_view.setLayoutParams(telnet_layout);
        this._list = (ASListView) findViewById(R.id.Mail_ContentList);
        this._back_button = (Button) findViewById(R.id.Mail_BackButton);
        this._page_up_button = (Button) findViewById(R.id.Mail_PageUpButton);
        this._page_down_button = (Button) findViewById(R.id.Mail_PageDownButton);
        this._back_button.setOnClickListener(this);
        this._page_up_button.setOnClickListener(this);
        this._page_down_button.setOnClickListener(this);
        findViewById(R.id.Mail_ChangeModeButton).setOnClickListener(this);
        resetAdapter();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        clear();
        return super.onBackPressed();
    }

    public void onPageDidDisappear() {
        this._back_button = null;
        this._page_up_button = null;
        this._page_down_button = null;
        this._list = null;
        this._telnet_view_block = null;
        this._telnet_view = null;
        super.onPageDidDisappear();
    }

    /* access modifiers changed from: protected */
    public boolean onMenuButtonClicked() {
        changeViewMode();
        return true;
    }

    public void setArticle(TelnetArticle aArticle) {
        clear();
        this._article = aArticle;
        this._telnet_view.setFrame(this._article.getFrame());
        this._telnet_view.setLayoutParams(this._telnet_view.getLayoutParams());
        this._telnet_view_block.scrollTo(0, 0);
        resetAdapter();
    }

    public void resetAdapter() {
        if (this._article != null) {
            this._list.setAdapter(this);
        }
    }

    public int getCount() {
        if (this._article != null) {
            return this._article.getItemSize() + 2;
        }
        return 0;
    }

    public TelnetArticleItem getItem(int itemIndex) {
        return this._article.getItem(itemIndex - 1);
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
                case 0:
                    itemView = new ArticlePage_TextItemView(getContext());
                    break;
                case 1:
                    itemView = new ArticlePage_TelnetItemView(getContext());
                    break;
                case 2:
                    itemView = new ArticlePage_HeaderItemView(getContext());
                    break;
                case 3:
                    itemView = new ArticlePage_TimeTimeView(getContext());
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
                if (itemIndex >= getCount() - 2) {
                    item_view.setDividerhidden(true);
                } else {
                    item_view.setDividerhidden(false);
                }
                break;
            case 1:
                ArticlePage_TelnetItemView item_view2 = (ArticlePage_TelnetItemView) itemView;
                item_view2.setFrame(getItem(itemIndex).getFrame());
                if (itemIndex >= getCount() - 2) {
                    item_view2.setDividerhidden(true);
                } else {
                    item_view2.setDividerhidden(false);
                }
                break;
            case 2:
                ArticlePage_HeaderItemView item_view3 = (ArticlePage_HeaderItemView) itemView;
                String author = this._article.Author;
                if (this._article.Nickname != null) {
                    author = author + "(" + this._article.Nickname + ")";
                }
                item_view3.setData(this._article.Title, author, this._article.BoardName);
                break;
            case 3:
                ((ArticlePage_TimeTimeView) itemView).setTime("《" + this._article.DateTime + "》");
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
        return false;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int itemIndex) {
        return false;
    }

    public void clear() {
        this._article = null;
    }

    public void onClick(View aView) {
        if (aView == this._back_button) {
            onReplyButtonClicked();
        } else if (aView == this._page_up_button) {
            onPageUpButtonClicked();
        } else if (aView == this._page_down_button) {
            onPageDownButtonClicked();
        } else if (aView.getId() == R.id.Mail_ChangeModeButton) {
            changeViewMode();
        }
    }

    public void onSendMailDialogSendButtonClicked(SendMailPage aDialog, String receiver, String title, String content) {
        PageContainer.getInstance().getMailBoxPage().onSendMailDialogSendButtonClicked(aDialog, receiver, title, content);
        onBackPressed();
    }

    private void onPageUpButtonClicked() {
        if (TelnetClient.getConnector().isConnecting()) {
            PageContainer.getInstance().getMailBoxPage().loadPreviousArticle();
        } else {
            showConnectionClosedToast();
        }
    }

    private void onPageDownButtonClicked() {
        if (TelnetClient.getConnector().isConnecting()) {
            PageContainer.getInstance().getMailBoxPage().loadNextArticle();
        } else {
            showConnectionClosedToast();
        }
    }

    private void showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷");
    }

    private void onReplyButtonClicked() {
        SendMailPage send_mail_page = new SendMailPage();
        String reply_title = this._article.generateReplyTitle();
        String reply_content = this._article.generateReplyContent();
        send_mail_page.setPostTitle(reply_title);
        send_mail_page.setPostContent(reply_content);
        send_mail_page.setReceiver(this._article.Author);
        send_mail_page.setListener(this);
        getNavigationController().pushViewController(send_mail_page);
    }

    public void changeViewMode() {
        if (this._view_mode == ArticleViewMode.MODE_TEXT) {
            this._view_mode = ArticleViewMode.MODE_TELNET;
        } else {
            this._view_mode = ArticleViewMode.MODE_TEXT;
        }
        if (this._view_mode == ArticleViewMode.MODE_TEXT) {
            this._list.setVisibility(View.VISIBLE);
            this._telnet_view_block.setVisibility(View.GONE);
            return;
        }
        this._list.setVisibility(View.GONE);
        this._telnet_view_block.setVisibility(View.VISIBLE);
        this._telnet_view_block.invalidate();
    }

    public boolean onReceivedGestureRight() {
        if (this._view_mode != ArticleViewMode.MODE_TEXT) {
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
}
