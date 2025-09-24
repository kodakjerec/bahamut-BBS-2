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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASProcessingDialog;
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
import com.kota.Bahamut.Pages.ArticlePage.ArticleViewMode;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItem;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

public class MailPage extends TelnetPage implements ListAdapter, View.OnClickListener, SendMailPage_Listener {
    RelativeLayout mainLayout = null;
    TelnetArticle telnetArticle = null;
    Button _back_button = null;
    ASListView listView = null;
    Button _page_down_button = null;
    Button _page_up_button = null;
    TextView listEmptyView = null;
    TelnetView telnetView = null;
    ASScrollView telnetViewBlock = null;
    int viewMode = ArticleViewMode.MODE_TEXT;

    boolean isFullScreen = false;
    final DataSetObservable mDataSetObservable = new DataSetObservable();

    public int getPageLayout() {
        return R.layout.mail_page;
    }

    public int getPageType() {
        return BahamutPage.BAHAMUT_MAIL;
    }

    @Override
    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        mainLayout = (RelativeLayout) findViewById(R.id.content_view);

        telnetViewBlock = mainLayout.findViewById(R.id.Mail_contentTelnetViewBlock);
        telnetView = mainLayout.findViewById(R.id.Mail_contentTelnetView);
        reloadTelnetLayout();
        listView = mainLayout.findViewById(R.id.Mail_contentList);
        listEmptyView = mainLayout.findViewById(R.id.Mail_listEmptyView);
        listView.setEmptyView(listEmptyView);

        _back_button = mainLayout.findViewById(R.id.Mail_backButton);
        _page_up_button = mainLayout.findViewById(R.id.Mail_pageUpButton);
        _page_down_button = mainLayout.findViewById(R.id.Mail_pageDownButton);
        _back_button.setOnClickListener(this);
        _page_up_button.setOnClickListener(this);
        _page_down_button.setOnClickListener(this);
        mainLayout.findViewById(R.id.Mail_changeModeButton).setOnClickListener(this);
        resetAdapter();

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));
    }

    protected boolean onBackPressed() {
        clear();
        return super.onBackPressed();
    }

    public void onPageDidDisappear() {
        _back_button = null;
        _page_up_button = null;
        _page_down_button = null;
        listView = null;
        telnetViewBlock = null;
        telnetView = null;
        super.onPageDidDisappear();
    }

    protected boolean onMenuButtonClicked() {
        reloadViewMode();
        return true;
    }

    public void setArticle(TelnetArticle aArticle) {
        clear();
        telnetArticle = aArticle;
        telnetView.setFrame(telnetArticle.getFrame());
        telnetView.setLayoutParams(telnetView.getLayoutParams());
        telnetViewBlock.scrollTo(0, 0);
        ASProcessingDialog.dismissProcessingDialog();
        resetAdapter();
    }

    public void resetAdapter() {
        if (telnetArticle != null) {
            listView.setAdapter(this);
        }
    }

    public int getCount() {
        if (telnetArticle != null) {
            return telnetArticle.getItemSize() + 2;
        }
        return 0;
    }

    public TelnetArticleItem getItem(int itemIndex) {
        return telnetArticle.getItem(itemIndex - 1);
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

    public View getView(int itemIndex, View itemViewFrom, ViewGroup parentView) {
        int type = getItemViewType(itemIndex);
        TelnetArticleItem item = getItem(itemIndex);
        View itemViewOrigin = itemViewFrom;

        if (itemViewOrigin == null) {
            switch (type) {
                case ArticlePageItemType.Sign -> itemViewOrigin = new ArticlePage_TelnetItemView(getContext());
                case ArticlePageItemType.Header -> itemViewOrigin = new ArticlePage_HeaderItemView(getContext());
                case ArticlePageItemType.PostTime -> itemViewOrigin = new ArticlePage_TimeTimeView(getContext());
                default -> itemViewOrigin = new ArticlePage_TextItemView(getContext());
            }
        }  else if (type == ArticlePageItemType.Content) {
            itemViewOrigin = new ArticlePage_TextItemView(getContext());
        }

        if (itemViewOrigin instanceof ArticlePage_TextItemView itemView1) {
            itemView1.setAuthor(item.getAuthor(), item.getNickname());
            itemView1.setQuote(item.getQuoteLevel());
            itemView1.setContent(item.getContent(), item.getFrame().rows);
            if (itemIndex >= getCount() - 2) {
                itemView1.setDividerHidden(true);
            } else {
                itemView1.setDividerHidden(false);
            }
        }
        else if (itemViewOrigin instanceof ArticlePage_TelnetItemView itemView2) {
            if (item!=null)
                itemView2.setFrame(item.getFrame());
            if (itemIndex >= getCount() - 2) {
                itemView2.setDividerHidden(true);
            } else {
                itemView2.setDividerHidden(false);
            }
        }
        else if (itemViewOrigin instanceof ArticlePage_HeaderItemView itemView3) {
            String author = telnetArticle.Author;
            if (telnetArticle.Nickname != null) {
                author = author + "(" + telnetArticle.Nickname + ")";
            }
            itemView3.setData(telnetArticle.Title, author, telnetArticle.BoardName);
        }
        else if (itemViewOrigin instanceof ArticlePage_TimeTimeView itemView4) {
            itemView4.setTime("《" + telnetArticle.DateTime + "》");
            itemView4.setIP(telnetArticle.fromIP);
        }
        return itemViewOrigin;
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
        telnetArticle = null;
    }

    public void onClick(View aView) {
        if (aView == _back_button) {
            onReplyButtonClicked();
        } else if (aView == _page_up_button) {
            onPageUpButtonClicked();
        } else if (aView == _page_down_button) {
            onPageDownButtonClicked();
        } else if (aView.getId() == R.id.Mail_changeModeButton) {
            reloadViewMode();
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
        String reply_title = telnetArticle.generateReplyTitle();
        String reply_content = telnetArticle.generateReplyContent();
        send_mail_page.setPostTitle(reply_title);
        send_mail_page.setPostContent(reply_content);
        send_mail_page.setReceiver(telnetArticle.Author);
        send_mail_page.setListener(this);
        getNavigationController().pushViewController(send_mail_page);
    }

    public void reloadViewMode() {
        if (viewMode == ArticleViewMode.MODE_TEXT) {
            viewMode = ArticleViewMode.MODE_TELNET;
        } else {
            viewMode = ArticleViewMode.MODE_TEXT;
        }
        if (viewMode == ArticleViewMode.MODE_TEXT) {
            listView.setVisibility(View.VISIBLE);
            telnetViewBlock.setVisibility(View.GONE);
            return;
        }
        listView.setVisibility(View.GONE);
        telnetViewBlock.setVisibility(View.VISIBLE);
        telnetViewBlock.invalidate();
    }

    public boolean onReceivedGestureRight() {
        if (viewMode != ArticleViewMode.MODE_TEXT || isFullScreen) {
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

    /** 給 state handler 更改讀取進度 */
    @SuppressLint("SetTextI18n")
    public void changeLoadingPercentage(String percentage) {
        ASProcessingDialog.showProcessingDialog(getContextString(R.string.loading_)+"\n"+percentage);
    }

    // 變更telnetView大小
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
}
