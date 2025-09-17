package com.kota.Bahamut.pages

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
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASScrollView
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.articlePage.ArticlePageItemType
import com.kota.Bahamut.pages.articlePage.ArticlePage_HeaderItemView
import com.kota.Bahamut.pages.articlePage.ArticlePage_TelnetItemView
import com.kota.Bahamut.pages.articlePage.ArticlePage_TextItemView
import com.kota.Bahamut.pages.articlePage.ArticlePage_TimeTimeView
import com.kota.Bahamut.pages.articlePage.ArticleViewMode
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.connector
import com.kota.telnet.TelnetConnector.isConnecting
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView

class MailPage : TelnetPage(), ListAdapter, View.OnClickListener, SendMailPage_Listener {
    var mainLayout: RelativeLayout? = null
    var telnetArticle: TelnetArticle? = null
    var _back_button: Button? = null
    var listView: ASListView? = null
    var _page_down_button: Button? = null
    var _page_up_button: Button? = null
    var listEmptyView: TextView? = null
    var telnetView: TelnetView? = null
    var telnetViewBlock: ASScrollView? = null
    var viewMode: Int = ArticleViewMode.MODE_TEXT

    var isFullScreen: Boolean = false
    val mDataSetObservable: DataSetObservable = DataSetObservable()

    val pageLayout: Int
        get() = R.layout.mail_page

    val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIL

    val isPopupPage: Boolean
        get() = true

    public override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout?

        telnetViewBlock = mainLayout!!.findViewById<ASScrollView?>(R.id.Mail_contentTelnetViewBlock)
        telnetView = mainLayout!!.findViewById<TelnetView?>(R.id.Mail_contentTelnetView)
        reloadTelnetLayout()
        listView = mainLayout!!.findViewById<ASListView?>(R.id.Mail_contentList)
        listEmptyView = mainLayout!!.findViewById<TextView?>(R.id.Mail_listEmptyView)
        listView!!.setEmptyView(listEmptyView)

        _back_button = mainLayout!!.findViewById<Button?>(R.id.Mail_backButton)
        _page_up_button = mainLayout!!.findViewById<Button?>(R.id.Mail_pageUpButton)
        _page_down_button = mainLayout!!.findViewById<Button?>(R.id.Mail_pageDownButton)
        _back_button!!.setOnClickListener(this)
        _page_up_button!!.setOnClickListener(this)
        _page_down_button!!.setOnClickListener(this)
        mainLayout!!.findViewById<View?>(R.id.Mail_changeModeButton).setOnClickListener(this)
        resetAdapter()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    protected override fun onBackPressed(): Boolean {
        clear()
        return super.onBackPressed()
    }

    public override fun onPageDidDisappear() {
        _back_button = null
        _page_up_button = null
        _page_down_button = null
        listView = null
        telnetViewBlock = null
        telnetView = null
        super.onPageDidDisappear()
    }

    protected override fun onMenuButtonClicked(): Boolean {
        reloadViewMode()
        return true
    }

    fun setArticle(aArticle: TelnetArticle?) {
        clear()
        telnetArticle = aArticle
        telnetView!!.frame = telnetArticle!!.frame
        telnetView!!.setLayoutParams(telnetView!!.getLayoutParams())
        telnetViewBlock!!.scrollTo(0, 0)
        dismissProcessingDialog()
        resetAdapter()
    }

    fun resetAdapter() {
        if (telnetArticle != null) {
            listView!!.setAdapter(this)
        }
    }

    override fun getCount(): Int {
        if (telnetArticle != null) {
            return telnetArticle!!.itemSize + 2
        }
        return 0
    }

    override fun getItem(itemIndex: Int): TelnetArticleItem? {
        return telnetArticle!!.getItem(itemIndex - 1)
    }

    override fun getItemId(itemIndex: Int): Long {
        return itemIndex.toLong()
    }

    override fun getItemViewType(itemIndex: Int): Int {
        if (itemIndex == 0) {
            return 2
        }
        if (itemIndex == getCount() - 1) {
            return 3
        }
        return getItem(itemIndex)!!.type
    }

    override fun getView(itemIndex: Int, itemViewFrom: View?, parentView: ViewGroup?): View {
        val type = getItemViewType(itemIndex)
        val item = getItem(itemIndex)
        var itemViewOrigin = itemViewFrom

        if (itemViewOrigin == null) {
            when (type) {
                ArticlePageItemType.Sign -> itemViewOrigin = ArticlePage_TelnetItemView(context)
                ArticlePageItemType.Header -> itemViewOrigin = ArticlePage_HeaderItemView(context)
                ArticlePageItemType.PostTime -> itemViewOrigin = ArticlePage_TimeTimeView(context)
                else -> itemViewOrigin = ArticlePage_TextItemView(context)
            }
        } else if (type == ArticlePageItemType.Content) {
            itemViewOrigin = ArticlePage_TextItemView(context)
        }

        if (itemViewOrigin is ArticlePage_TextItemView) {
            itemViewOrigin.setAuthor(item!!.author, item.nickname)
            itemViewOrigin.setQuote(item.quoteLevel)
            itemViewOrigin.setContent(item.content, item.frame!!.rows)
            if (itemIndex >= getCount() - 2) {
                itemViewOrigin.setDividerHidden(true)
            } else {
                itemViewOrigin.setDividerHidden(false)
            }
        } else if (itemViewOrigin is ArticlePage_TelnetItemView) {
            if (item != null) itemViewOrigin.setFrame(item.frame)
            if (itemIndex >= getCount() - 2) {
                itemViewOrigin.setDividerHidden(true)
            } else {
                itemViewOrigin.setDividerHidden(false)
            }
        } else if (itemViewOrigin is ArticlePage_HeaderItemView) {
            var author = telnetArticle!!.author
            if (telnetArticle!!.nickName != null) {
                author = author + "(" + telnetArticle!!.nickName + ")"
            }
            itemViewOrigin.setData(telnetArticle!!.title, author, telnetArticle!!.boardName)
        } else if (itemViewOrigin is ArticlePage_TimeTimeView) {
            itemViewOrigin.setTime("《" + telnetArticle!!.dateTime + "》")
            itemViewOrigin.setIP(telnetArticle!!.fromIP)
        }
        return itemViewOrigin
    }

    override fun getViewTypeCount(): Int {
        return 4
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.unregisterObserver(observer)
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(itemIndex: Int): Boolean {
        return false
    }

    public override fun clear() {
        telnetArticle = null
    }

    override fun onClick(aView: View) {
        if (aView === _back_button) {
            onReplyButtonClicked()
        } else if (aView === _page_up_button) {
            onPageUpButtonClicked()
        } else if (aView === _page_down_button) {
            onPageDownButtonClicked()
        } else if (aView.getId() == R.id.Mail_changeModeButton) {
            reloadViewMode()
        }
    }

    override fun onSendMailDialogSendButtonClicked(
        aDialog: SendMailPage?,
        receiver: String?,
        title: String?,
        content: String?
    ) {
        PageContainer.getInstance().getMailBoxPage()
            .onSendMailDialogSendButtonClicked(aDialog, receiver, title, content)
        onBackPressed()
    }

    fun onPageUpButtonClicked() {
        if (TelnetClient.connector.isConnecting) {
            PageContainer.getInstance().getMailBoxPage().loadPreviousArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    fun onPageDownButtonClicked() {
        if (TelnetClient.connector.isConnecting) {
            PageContainer.getInstance().getMailBoxPage().loadNextArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    fun showConnectionClosedToast() {
        showShortToast("連線已中斷")
    }

    fun onReplyButtonClicked() {
        val send_mail_page = SendMailPage()
        val reply_title = telnetArticle!!.generateReplyTitle()
        val reply_content = telnetArticle!!.generateReplyContent()
        send_mail_page.setPostTitle(reply_title)
        send_mail_page.setPostContent(reply_content)
        send_mail_page.setReceiver(telnetArticle!!.author)
        send_mail_page.setListener(this)
        navigationController!!.pushViewController(send_mail_page)
    }

    fun reloadViewMode() {
        if (viewMode == ArticleViewMode.MODE_TEXT) {
            viewMode = ArticleViewMode.MODE_TELNET
        } else {
            viewMode = ArticleViewMode.MODE_TEXT
        }
        if (viewMode == ArticleViewMode.MODE_TEXT) {
            listView!!.setVisibility(View.VISIBLE)
            telnetViewBlock!!.setVisibility(View.GONE)
            return
        }
        listView!!.setVisibility(View.GONE)
        telnetViewBlock!!.setVisibility(View.VISIBLE)
        telnetViewBlock!!.invalidate()
    }

    public override fun onReceivedGestureRight(): Boolean {
        if (viewMode != ArticleViewMode.MODE_TEXT || isFullScreen) {
            return true
        }
        onBackPressed()
        return true
    }

    fun refresh() {
    }

    val isKeepOnOffline: Boolean
        get() = true

    /** 給 state handler 更改讀取進度  */
    @SuppressLint("SetTextI18n")
    fun changeLoadingPercentage(percentage: String?) {
        showProcessingDialog(getContextString(R.string.loading_) + "\n" + percentage)
    }

    // 變更telnetView大小
    fun reloadTelnetLayout() {
        val screenWidth: Int
        val textWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            20.0f,
            context.getResources().getDisplayMetrics()
        ).toInt()
        var telnetViewWidth = (textWidth / 2) * 80
        if (navigationController!!.currentOrientation == 2) {
            screenWidth = navigationController!!.screenHeight
        } else {
            screenWidth = navigationController!!.screenWidth
        }
        if (telnetViewWidth <= screenWidth) {
            telnetViewWidth = -1
            isFullScreen = true
        } else {
            isFullScreen = false
        }
        val layoutParams = telnetView!!.getLayoutParams()
        layoutParams.width = telnetViewWidth
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        telnetView!!.setLayoutParams(layoutParams)
    }
}
