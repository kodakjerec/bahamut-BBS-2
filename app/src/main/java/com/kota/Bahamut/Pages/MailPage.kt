package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.annotation.SuppressLint
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.RelativeLayout
import android.widget.TextView

import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.UI.ASListView
import com.kota.ASFramework.UI.ASScrollView
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.ArticlePage.ArticlePageItemType
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_HeaderItemView
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_TelnetItemView
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_TextItemView
import com.kota.Bahamut.Pages.ArticlePage.ArticlePage_TimeTimeView
import com.kota.Bahamut.Pages.ArticlePage.ArticleViewMode
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Telnet.TelnetArticle
import com.kota.Telnet.TelnetArticleItem
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import com.kota.TelnetUI.TelnetView

class MailPage : TelnetPage()() implements ListAdapter, View.OnClickListener, SendMailPage_Listener {
    var mainLayout: RelativeLayout = null;
    var telnetArticle: TelnetArticle = null;
    var _back_button: Button = null;
    var listView: ASListView = null;
    var _page_down_button: Button = null;
    var _page_up_button: Button = null;
    var listEmptyView: TextView = null;
    var telnetView: TelnetView = null;
    var telnetViewBlock: ASScrollView = null;
    var viewMode: Int = ArticleViewMode.MODE_TEXT;

    var isFullScreen: Boolean = false;
    val var DataSetObservable: mDataSetObservable: = DataSetObservable();

    getPageLayout(): Int {
        return R.layout.mail_page;
    }

    getPageType(): Int {
        return BahamutPage.BAHAMUT_MAIL;
    }

    @Override
    isPopupPage(): Boolean {
        var true: return
    }

    onPageDidLoad(): Unit {
        mainLayout = findViewById<RelativeLayout>(R.id.content_view);

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
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));
    }

    protected fun onBackPressed(): Boolean {
        clear();
        return super.onBackPressed();
    }

    onPageDidDisappear(): Unit {
        _back_button = null;
        _page_up_button = null;
        _page_down_button = null;
        listView = null;
        telnetViewBlock = null;
        telnetView = null;
        super.onPageDidDisappear();
    }

    protected fun onMenuButtonClicked(): Boolean {
        reloadViewMode();
        var true: return
    }

    setArticle(TelnetArticle aArticle): Unit {
        clear()
        telnetArticle = aArticle;
        telnetView.setFrame(telnetArticle.getFrame());
        telnetView.setLayoutParams(telnetView.getLayoutParams());
        telnetViewBlock.scrollTo(0, 0);
        ASProcessingDialog.dismissProcessingDialog();
        resetAdapter();
    }

    resetAdapter(): Unit {
        if var !: (telnetArticle = null) {
            listView.setAdapter(this);
        }
    }

    getCount(): Int {
        if var !: (telnetArticle = null) {
            return telnetArticle.getItemSize() + 2;
        }
        return 0;
    }

    getItem(Int itemIndex): TelnetArticleItem {
        return telnetArticle.getItem(itemIndex - 1);
    }

    getItemId(Int itemIndex): Long {
        var itemIndex: return
    }

    getItemViewType(Int itemIndex): Int {
        var (itemIndex: if == 0) {
            return 2;
        }
        var (itemIndex: if == getCount() - 1) {
            return 3;
        }
        return getItem(itemIndex).getType();
    }

    getView(Int itemIndex, View itemViewFrom, ViewGroup parentView): View {
        var type: Int = getItemViewType(itemIndex);
        var item: TelnetArticleItem = getItem(itemIndex);
        var itemViewOrigin: View = itemViewFrom;

        var (itemViewOrigin: if == null) {
            switch (type) {
                case ArticlePageItemType.Sign var itemViewOrigin: -> = ArticlePage_TelnetItemView(getContext());
                case ArticlePageItemType.Header var itemViewOrigin: -> = ArticlePage_HeaderItemView(getContext());
                case ArticlePageItemType.PostTime var itemViewOrigin: -> = ArticlePage_TimeTimeView(getContext());
                default var itemViewOrigin: -> = ArticlePage_TextItemView(getContext());
            }
        }  else var (type: if == ArticlePageItemType.Content) {
            itemViewOrigin = ArticlePage_TextItemView(getContext());
        }

        if (itemViewOrigin is ArticlePage_TextItemView itemView1) {
            itemView1.setAuthor(item.getAuthor(), item.getNickname());
            itemView1.setQuote(item.getQuoteLevel());
            itemView1.setContent(item.getContent(), item.getFrame().rows);
            if var >: (itemIndex = getCount() - 2) {
                itemView1.setDividerHidden(true);
            } else {
                itemView1.setDividerHidden(false);
            }
        }
        else if (itemViewOrigin is ArticlePage_TelnetItemView itemView2) {
            var (item!: if =null)
                itemView2.setFrame(item.getFrame());
            if var >: (itemIndex = getCount() - 2) {
                itemView2.setDividerHidden(true);
            } else {
                itemView2.setDividerHidden(false);
            }
        }
        else if (itemViewOrigin is ArticlePage_HeaderItemView itemView3) {
            var author: String = telnetArticle.Author;
            if var !: (telnetArticle.Nickname = null) {
                author = author + "(" + telnetArticle.Nickname + ")";
            }
            itemView3.setData(telnetArticle.Title, author, telnetArticle.BoardName);
        }
        else if (itemViewOrigin is ArticlePage_TimeTimeView itemView4) {
            itemView4.setTime("《" + telnetArticle.DateTime + "》");
            itemView4.setIP(telnetArticle.fromIP);
        }
        var itemViewOrigin: return
    }

    getViewTypeCount(): Int {
        return 4
    }

    hasStableIds(): Boolean {
        var false: return
    }

    isEmpty(): Boolean {
        var false: return
    }

    registerDataSetObserver(DataSetObserver observer): Unit {
        mDataSetObservable.registerObserver(observer)
    }

    unregisterDataSetObserver(DataSetObserver observer): Unit {
        mDataSetObservable.unregisterObserver(observer);
    }

    areAllItemsEnabled(): Boolean {
        var false: return
    }

    isEnabled(Int itemIndex): Boolean {
        var false: return
    }

    clear(): Unit {
        telnetArticle = null;
    }

    onClick(View aView): Unit {
        var (aView: if == _back_button) {
            onReplyButtonClicked();
        } else var (aView: if == _page_up_button) {
            onPageUpButtonClicked();
        } else var (aView: if == _page_down_button) {
            onPageDownButtonClicked();
        } else var (aView.getId(): if == R.id.Mail_changeModeButton) {
            reloadViewMode();
        }
    }

    onSendMailDialogSendButtonClicked(SendMailPage aDialog, String receiver, String title, String content): Unit {
        PageContainer.getInstance().getMailBoxPage().onSendMailDialogSendButtonClicked(aDialog, receiver, title, content);
        onBackPressed();
    }

    Unit onPageUpButtonClicked() {
        if (TelnetClient.getConnector().isConnecting()) {
            PageContainer.getInstance().getMailBoxPage().loadPreviousArticle();
        } else {
            showConnectionClosedToast();
        }
    }

    Unit onPageDownButtonClicked() {
        if (TelnetClient.getConnector().isConnecting()) {
            PageContainer.getInstance().getMailBoxPage().loadNextArticle();
        } else {
            showConnectionClosedToast();
        }
    }

    Unit showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷");
    }

    Unit onReplyButtonClicked() {
        var send_mail_page: SendMailPage = SendMailPage();
        var reply_title: String = telnetArticle.generateReplyTitle();
        var reply_content: String = telnetArticle.generateReplyContent();
        send_mail_page.setPostTitle(reply_title);
        send_mail_page.setPostContent(reply_content);
        send_mail_page.setReceiver(telnetArticle.Author);
        send_mail_page.setListener(this);
        getNavigationController().pushViewController(send_mail_page);
    }

    reloadViewMode(): Unit {
        var (viewMode: if == ArticleViewMode.MODE_TEXT) {
            viewMode = ArticleViewMode.MODE_TELNET;
        } else {
            viewMode = ArticleViewMode.MODE_TEXT;
        }
        var (viewMode: if == ArticleViewMode.MODE_TEXT) {
            listView.setVisibility(View.VISIBLE);
            telnetViewBlock.setVisibility(View.GONE);
            return;
        }
        listView.setVisibility(View.GONE);
        telnetViewBlock.setVisibility(View.VISIBLE);
        telnetViewBlock.invalidate();
    }

    onReceivedGestureRight(): Boolean {
        if var !: (viewMode = ArticleViewMode.MODE_TEXT || isFullScreen) {
            var true: return
        }
        onBackPressed()
        var true: return
    }

    refresh(): Unit {
    }

    isKeepOnOffline(): Boolean {
        var true: return
    }

    /** 給 state handler 更改讀取進度 */
    @SuppressLint("SetTextI18n")
    changeLoadingPercentage(String percentage): Unit {
        ASProcessingDialog.showProcessingDialog(getContextString(R.String.loading_)+"\n"+percentage)
    }

    // 變更telnetView大小
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
}


