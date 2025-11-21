package com.kota.Bahamut.pages.mailPage

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
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.articlePage.ArticlePageHeaderItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageItemType
import com.kota.Bahamut.pages.articlePage.ArticlePageTelnetItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTextItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTimeTimeView
import com.kota.Bahamut.pages.articlePage.ArticleViewMode
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASScrollView
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView

class MailPage : TelnetPage(), ListAdapter, View.OnClickListener, SendMailPageListener {
    lateinit var mainLayout: RelativeLayout
    var telnetArticle: TelnetArticle? = null
    var backButton: Button? = null
    var listView: ASListView? = null
    var pageDownButton: Button? = null
    var pageUpButton: Button? = null
    var listEmptyView: TextView? = null
    var telnetView: TelnetView? = null
    var telnetViewBlock: ASScrollView? = null
    var viewMode: Int = ArticleViewMode.MODE_TEXT

    var isFullScreen: Boolean = false
    val mDataSetObservable: DataSetObservable = DataSetObservable()

    override val pageLayout: Int
        get() = R.layout.mail_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIL

    override val isPopupPage: Boolean
        get() = true

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        telnetViewBlock = mainLayout.findViewById(R.id.Mail_contentTelnetViewBlock)
        telnetView = mainLayout.findViewById(R.id.Mail_contentTelnetView)
        reloadTelnetLayout()
        listView = mainLayout.findViewById(R.id.Mail_contentList)
        listEmptyView = mainLayout.findViewById(R.id.Mail_listEmptyView)
        listView?.emptyView = listEmptyView

        backButton = mainLayout.findViewById(R.id.Mail_backButton)
        pageUpButton = mainLayout.findViewById(R.id.Mail_pageUpButton)
        pageDownButton = mainLayout.findViewById(R.id.Mail_pageDownButton)
        backButton?.setOnClickListener(this)
        pageUpButton?.setOnClickListener(this)
        pageDownButton?.setOnClickListener(this)
        mainLayout.findViewById<View>(R.id.Mail_changeModeButton)!!.setOnClickListener(this)
        resetAdapter()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    override fun onBackPressed(): Boolean {
        clear()
        return super.onBackPressed()
    }

    override fun onPageDidDisappear() {
        backButton = null
        pageUpButton = null
        pageDownButton = null
        listView = null
        telnetViewBlock = null
        telnetView = null
        super.onPageDidDisappear()
    }

    override fun onMenuButtonClicked(): Boolean {
        reloadViewMode()
        return true
    }

    fun setArticle(aArticle: TelnetArticle) {
        clear()
        telnetArticle = aArticle
        telnetView!!.frame = telnetArticle!!.frame!!
        telnetView!!.layoutParams = telnetView?.layoutParams
        telnetViewBlock?.scrollTo(0, 0)
        dismissProcessingDialog()
        resetAdapter()
    }

    fun resetAdapter() {
        if (telnetArticle != null) {
            listView?.adapter = this
        }
    }

    override fun getCount(): Int {
        if (telnetArticle != null) {
            return telnetArticle?.itemSize!! + 2
        }
        return 0
    }

    override fun getItem(itemIndex: Int): TelnetArticleItem? {
        return telnetArticle?.getItem(itemIndex - 1)
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
            itemViewOrigin = when (type) {
                ArticlePageItemType.SIGN -> ArticlePageTelnetItemView(context)
                ArticlePageItemType.HEADER -> ArticlePageHeaderItemView(context)
                ArticlePageItemType.POST_TIME -> ArticlePageTimeTimeView(context)
                else -> ArticlePageTextItemView(context)
            }
        } else if (type == ArticlePageItemType.CONTENT) {
            itemViewOrigin = ArticlePageTextItemView(context)
        }

        if (itemViewOrigin is ArticlePageTextItemView && item!==null) {
            itemViewOrigin.setAuthor(item.author, item.nickname)
            itemViewOrigin.setQuote(item.quoteLevel)
            itemViewOrigin.setContent(item.content, item.frame?.rows!!)
            if (itemIndex >= getCount() - 2) {
                itemViewOrigin.setDividerHidden(true)
            } else {
                itemViewOrigin.setDividerHidden(false)
            }
        } else if (itemViewOrigin is ArticlePageTelnetItemView && item!==null) {
            itemViewOrigin.setFrame(item.frame!!)
            if (itemIndex >= getCount() - 2) {
                itemViewOrigin.setDividerHidden(true)
            } else {
                itemViewOrigin.setDividerHidden(false)
            }
        } else if (itemViewOrigin is ArticlePageHeaderItemView) {
            var author = telnetArticle?.author
            if (telnetArticle?.nickName != null) {
                author = author + "(" + telnetArticle?.nickName + ")"
            }
            itemViewOrigin.setData(telnetArticle?.title, author, telnetArticle?.boardName)
        } else if (itemViewOrigin is ArticlePageTimeTimeView) {
            itemViewOrigin.setTime("《" + telnetArticle?.dateTime + "》")
            itemViewOrigin.setIP(telnetArticle?.fromIP!!)
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

    override fun clear() {
        telnetArticle = null
    }

    override fun onClick(aView: View) {
        if (aView === backButton) {
            onReplyButtonClicked()
        } else if (aView === pageUpButton) {
            onPageUpButtonClicked()
        } else if (aView === pageDownButton) {
            onPageDownButtonClicked()
        } else if (aView.id == R.id.Mail_changeModeButton) {
            reloadViewMode()
        }
    }

    /**
     * 按下寄信
     */
    override fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    ) {
        PageContainer.instance!!.mailBoxPage
            .onSendMailDialogSendButtonClicked(sendMailPage, receiver, title, content)
        onBackPressed()
    }

    fun onPageUpButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector?.isConnecting == true) {
            PageContainer.instance!!.mailBoxPage.loadPreviousArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    fun onPageDownButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector?.isConnecting!!) {
            PageContainer.instance!!.mailBoxPage!!.loadNextArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    fun showConnectionClosedToast() {
        showShortToast("連線已中斷")
    }

    fun onReplyButtonClicked() {
        val sendMailPage = SendMailPage()
        navigationController.pushViewController(sendMailPage)
        if (telnetArticle != null) {
            val replyTitle = telnetArticle!!.generateReplyTitle()
            val replyContent = telnetArticle!!.generateReplyContent()
            sendMailPage.setPostTitle(replyTitle)
            sendMailPage.setPostContent(replyContent)
            sendMailPage.setReceiver(telnetArticle!!.author)
            sendMailPage.setListener(this)
        }
    }

    fun reloadViewMode() {
        viewMode = if (viewMode == ArticleViewMode.MODE_TEXT) {
            ArticleViewMode.MODE_TELNET
        } else {
            ArticleViewMode.MODE_TEXT
        }
        if (viewMode == ArticleViewMode.MODE_TEXT) {
            listView?.visibility = View.VISIBLE
            telnetViewBlock?.visibility = View.GONE
            return
        }
        listView?.visibility = View.GONE
        telnetViewBlock?.visibility = View.VISIBLE
        telnetViewBlock?.invalidate()
    }

    override fun onReceivedGestureRight(): Boolean {
        if (viewMode != ArticleViewMode.MODE_TEXT || isFullScreen) {
            return true
        }
        onBackPressed()
        return true
    }

    override val isKeepOnOffline: Boolean
        get() = true

    /** 給 state handler 更改讀取進度  */
    @SuppressLint("SetTextI18n")
    fun changeLoadingPercentage(percentage: String?) {
        showProcessingDialog(getContextString(R.string.loading_) + "\n" + percentage)
    }

    // 變更telnetView大小
    fun reloadTelnetLayout() {
        val textWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            20.0f,
            context?.resources!!.displayMetrics
        ).toInt()
        var telnetViewWidth = (textWidth / 2) * 80
        val screenWidth: Int = if (navigationController.currentOrientation == 2) {
            navigationController.screenHeight
        } else {
            navigationController.screenWidth
        }
        if (telnetViewWidth <= screenWidth) {
            telnetViewWidth = -1
            isFullScreen = true
        } else {
            isFullScreen = false
        }
        val layoutParams = telnetView?.layoutParams!!
        layoutParams.width = telnetViewWidth
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        telnetView?.layoutParams = layoutParams
    }
}
