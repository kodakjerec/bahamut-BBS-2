package com.kota.Bahamut.pages.essencePage

import android.annotation.SuppressLint
import android.content.Intent
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.net.toUri
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandFSendMail
import com.kota.Bahamut.pages.SendMailPage
import com.kota.Bahamut.pages.SendMailPageListener
import com.kota.Bahamut.pages.articlePage.ArticlePageHeaderItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageItemType
import com.kota.Bahamut.pages.articlePage.ArticlePageTelnetItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTextItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTimeTimeView
import com.kota.Bahamut.pages.articlePage.ArticleViewMode
import com.kota.Bahamut.pages.articlePage.ThumbnailItemView
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASScrollView
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView

class ArticleEssencePage() : TelnetPage(), View.OnClickListener, SendMailPageListener {
    var mainLayout: RelativeLayout? = null
    private var telnetArticle: TelnetArticle? = null
    private var asListView: ASListView? = null
    private var changeModeButton: Button? = null
    private var pageDownButton: Button? = null
    private var pageUpButton: Button? = null
    private var listEmptyView: TextView? = null
    private var telnetView: TelnetView? = null
    private var telnetViewBlock: ASScrollView? = null
    private var viewMode = ArticleViewMode.MODE_TEXT
    private var isFullScreen = false
    private val mDataSetObservable = DataSetObservable()
    private var boardEssencePage: BoardEssencePage? = null

    private val listAdapter: BaseAdapter = object :BaseAdapter() {
        override fun getCount(): Int {
            return if (telnetArticle != null) {
                telnetArticle?.itemSize!! + 2
            } else 0
        }

        override fun getItem(itemIndex: Int): TelnetArticleItem? {
            return if (telnetArticle != null) {
                telnetArticle?.getItem(itemIndex - 1)
            } else null
        }

        override fun getItemId(itemIndex: Int): Long {
            return itemIndex.toLong()
        }

        override fun getView(itemIndex: Int, itemView: View?, parentView: ViewGroup): View {
            val type: Int = getItemViewType(itemIndex)
            val item = getItem(itemIndex)
            var itemViewOrigin: View? = itemView

            // 2-標題 0-本文 1-簽名檔 3-發文時間
            if (itemViewOrigin == null) {
                itemViewOrigin = when (type) {
                    ArticlePageItemType.HEADER ->
                        ArticlePageHeaderItemView(context)
                    ArticlePageItemType.SIGN ->
                        ArticlePageTelnetItemView(context)
                    ArticlePageItemType.POST_TIME ->
                        ArticlePageTimeTimeView(context)
                    else ->
                        ArticlePageTextItemView(context)
                }
            } else if (type == ArticlePageItemType.CONTENT) {
                itemViewOrigin = ArticlePageTextItemView(context)
            }

            when (getItemViewType(itemIndex)) {
                ArticlePageItemType.HEADER -> {
                    val itemView1 = itemViewOrigin as ArticlePageHeaderItemView
                    var author = ""
                    var title = ""
                    var boardName = ""
                    if (telnetArticle!=null) {
                        author = telnetArticle?.author!!
                        title = telnetArticle?.title!!
                        if (telnetArticle?.nickName != null) {
                            author = author + "(" + telnetArticle?.nickName + ")"
                        }
                        boardName = telnetArticle?.boardName!!
                    }
                    itemView1.setData(title, author, boardName)
                    itemView1.setMenuButtonClickListener(mMenuListener)
                }

                ArticlePageItemType.CONTENT -> {
                    val itemView2 = itemViewOrigin as ArticlePageTextItemView
                    if (item!=null) {
                        itemView2.setAuthor(item.author, item.nickname)
                        itemView2.setQuote(item.quoteLevel)
                        itemView2.setContent(item.content, item.frame?.rows!!)
                    }
                    if (itemIndex >= count - 2) {
                        itemView2.setDividerHidden(true)
                    } else {
                        itemView2.setDividerHidden(false)
                    }
                }

                ArticlePageItemType.SIGN -> {
                    val itemView3 = itemViewOrigin as ArticlePageTelnetItemView
                    if (item!=null) {
                        itemView3.setFrame(item.frame!!)
                    }
                    if (itemIndex >= count - 2) {
                        itemView3.setDividerHidden(true)
                    } else {
                        itemView3.setDividerHidden(false)
                    }
                }

                ArticlePageItemType.POST_TIME -> {
                    val itemView4 = itemViewOrigin as ArticlePageTimeTimeView
                    itemView4.setTime("《" + telnetArticle?.dateTime + "》")
                    itemView4.setIP(telnetArticle?.fromIP!!)
                }
            }
            return itemViewOrigin
        }


        override fun getItemViewType(itemIndex: Int): Int {
            if (itemIndex == 0) {
                return ArticlePageItemType.HEADER
            }
            return if (itemIndex == count - 1) {
                ArticlePageItemType.POST_TIME
            } else getItem(itemIndex)?.type!!
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

        override fun registerDataSetObserver(observer: DataSetObserver) {
            mDataSetObservable.registerObserver(observer)
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver) {
            mDataSetObservable.unregisterObserver(observer)
        }

        override fun areAllItemsEnabled(): Boolean {
            return false
        }

        override fun isEnabled(itemIndex: Int): Boolean {
            return false
        }
    }

    override val pageLayout: Int
        get() =  R.layout.article_essence_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_ARTICLE_ESSENCE

    override val isPopupPage: Boolean
        get() = true

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout
        telnetViewBlock = mainLayout?.findViewById(R.id.Essence_contentTelnetViewBlock)
        telnetView = mainLayout?.findViewById(R.id.Essence_contentTelnetView)
        reloadTelnetLayout()
        asListView = mainLayout?.findViewById(R.id.Essence_contentList)
        listEmptyView = mainLayout?.findViewById(R.id.Essence_listEmptyView)
        asListView?.adapter = listAdapter
        asListView?.emptyView = listEmptyView
        asListView?.onItemLongClickListener = listLongClickListener
        pageUpButton = mainLayout?.findViewById(R.id.Essence_pageUpButton)
        pageDownButton = mainLayout?.findViewById(R.id.Essence_pageDownButton)
        changeModeButton = mainLayout?.findViewById(R.id.Essence_changeModeButton)
        changeModeButton?.setOnClickListener(this)
        pageUpButton?.setOnClickListener(this)
        pageDownButton?.setOnClickListener(this)
        resetAdapter()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout)
    }

    override fun onBackPressed(): Boolean {
        clear()
        return super.onBackPressed()
    }

    override fun onPageDidDisappear() {
        pageUpButton = null
        pageDownButton = null
        asListView = null
        telnetViewBlock = null
        telnetView = null
        super.onPageDidDisappear()
    }

    override fun onMenuButtonClicked(): Boolean {
        reloadViewMode()
        return true
    }

    fun setArticle(aArticle: TelnetArticle?) {
        clear()
        telnetArticle = aArticle
        telnetView!!.frame = telnetArticle!!.frame!!
        telnetView!!.layoutParams = telnetView?.layoutParams
        telnetViewBlock?.scrollTo(0, 0)
        ASProcessingDialog.dismissProcessingDialog()
        resetAdapter()
    }

    private fun resetAdapter() {
        if (telnetArticle != null) {
            asListView?.adapter = listAdapter
        }
    }

    override fun clear() {
        telnetArticle = null
    }

    override fun onClick(aView: View) {
        if (aView === changeModeButton) {
            reloadViewMode()
        } else if (aView === pageUpButton) {
            onPageUpButtonClicked()
        } else if (aView === pageDownButton) {
            onPageDownButtonClicked()
        }
    }

    override fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    ) {
        PageContainer.instance?.mailBoxPage!!.onSendMailDialogSendButtonClicked(
            sendMailPage,
            receiver,
            title,
            content
        )
        onBackPressed()
    }

    private fun onPageUpButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector!!.isConnecting) {
            PageContainer.instance?.boardEssencePage!!.loadPreviousArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    private fun onPageDownButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector!!.isConnecting) {
            PageContainer.instance?.boardEssencePage!!.loadNextArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    private fun showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷")
    }

    private fun reloadViewMode() {
        viewMode =
            if (viewMode == ArticleViewMode.MODE_TEXT) {
                ArticleViewMode.MODE_TELNET
            } else {
                ArticleViewMode.MODE_TEXT
            }
        if (viewMode == ArticleViewMode.MODE_TEXT) {
            asListView?.visibility = View.VISIBLE
            telnetViewBlock?.visibility = View.GONE
            return
        }
        asListView?.visibility = View.GONE
        telnetViewBlock?.visibility = View.VISIBLE
        telnetViewBlock?.invalidate()
    }

    override fun onReceivedGestureRight(): Boolean {
        if (viewMode != ArticleViewMode.MODE_TEXT) {
            return true
        }
        onBackPressed()
        return true
    }

    override val isKeepOnOffline: Boolean
        get() = true

    /** 給 state handler 更改讀取進度 */
    @SuppressLint("SetTextI18n")
    fun changeLoadingPercentage(percentage: String) {
        ASProcessingDialog.showProcessingDialog(CommonFunctions.getContextString(R.string.loading_) +"\n"+ percentage)
    }


    // 長按內文
    private var listLongClickListener =
        OnItemLongClickListener { _: AdapterView<*>?, view: View, itemIndex: Int, _: Long ->
            if (view.javaClass == ArticlePageTelnetItemView::class.java) {
                // 開啟切換模式
                val item: TelnetArticleItem? = telnetArticle?.getItem(itemIndex - 1)
                if (item!==null)
                    when (item.type) {
                        0 -> {
                            item.type = 1
                            listAdapter.notifyDataSetChanged()
                            return@OnItemLongClickListener true
                        }
                        1 -> {
                            item.type = 0
                            listAdapter.notifyDataSetChanged()
                            return@OnItemLongClickListener true
                        }
                        else -> {
                            return@OnItemLongClickListener true
                        }
                    }
            }

            // 不是telnetView繼續往下運行事件
            return@OnItemLongClickListener false
        }


    private fun onMenuClicked() {
        if (telnetArticle != null) {
            ASListDialog.createDialog()
                .addItem(CommonFunctions.getContextString(R.string.change_mode))
                .addItem(
                    CommonFunctions.getContextString(R.string.insert) + CommonFunctions.getContextString(
                        R.string.system_setting_page_chapter_blocklist
                    )
                )
                .addItem(CommonFunctions.getContextString(R.string.open_url))
                .addItem(CommonFunctions.getContextString(R.string.board_page_item_long_click_1))
                .addItem(CommonFunctions.getContextString(R.string.board_page_item_load_all_image))
                .setListener(object : ASListDialogItemClickListener {
                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
                    ) {
                        when (index) {
                            0 -> reloadViewMode()
                            1 -> onAddBlockListClicked()
                            2 -> onOpenLinkClicked()
                            3 -> fSendMail()
                            4 -> onLoadAllImageClicked()
                            else -> {}
                        }
                    }

                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemLongClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
                    ): Boolean {
                        return true
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        }
    }

    fun onOpenLinkClicked() {
        if (telnetArticle != null) {
            // 擷取文章內的所有連結
            val textView = TextView(context)
            textView.text = telnetArticle?.fullText
            Linkify.addLinks(textView, Linkify.WEB_URLS)
            val urls = textView.urls
            if (urls.isEmpty()) {
                ASToast.showShortToast(CommonFunctions.getContextString(R.string.no_url))
                return
            }
            val listDialog = ASListDialog.createDialog()
            for (urlSpan in urls) {
                listDialog.addItem(urlSpan.url)
            }
            listDialog.setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    val url2 = urls[index].url
                    val context2 = context
                    if (context2 != null) {
                        val intent = Intent(Intent.ACTION_VIEW, url2.toUri())
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context2.startActivity(intent)
                    }
                }
            })
            listDialog.show()
        }
    }

    // 加入黑名單
    fun onAddBlockListClicked() {
        val article: TelnetArticle = telnetArticle!!
        val buffer: MutableSet<String> = HashSet()
        // 作者黑名單
        buffer.add(article.author)
        // 內文黑名單
        val len = article.itemSize
        for (i in 0 until len) {
            val item = article.getItem(i)
            if (item!==null) {
                val author = item.author
                if (author != null && !UserSettings.isBlockListContains(author)) {
                    buffer.add(author)
                }
            }
        }
        if (buffer.isEmpty()) {
            ASToast.showShortToast("無可加入黑名單的ID")
            return
        }
        val names: Array<String> = buffer.toTypedArray<String>()
        ASListDialog.createDialog().addItems(names)
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    onBlockButtonClicked(names[index])
                }
            }).show()
    }

    fun onBlockButtonClicked(aBlockName: String) {
        ASAlertDialog.createDialog()
            .setTitle("加入黑名單")
            .setMessage("是否要將\"$aBlockName\"加入黑名單?")
            .addButton("取消")
            .addButton("加入")
            .setListener { _: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    val newList =
                        UserSettings.blockList!!
                    if (newList.contains(aBlockName)) {
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.already_have_item))
                    } else {
                        newList.add(aBlockName)
                    }
                    UserSettings.blockList = newList
                    UserSettings.notifyDataUpdated()
                    if (UserSettings.propertiesBlockListEnable) {
                        if (aBlockName == telnetArticle?.author) {
                            onBackPressed()
                        } else {
                            listAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    // 載入全部圖片
    fun onLoadAllImageClicked() {
        // TODO: not yet
        val listView = asListView!!
        val childCount = listView.childCount
        for (childIndex in 0 until childCount) {
            val view = listView.getChildAt(childIndex)
            if (view.javaClass == ArticlePageTextItemView::class.java) {
                val firstLLayout = (view as ArticlePageTextItemView).getChildAt(0) as LinearLayout
                val secondLLayout = firstLLayout.getChildAt(0) as LinearLayout
                val childCount2 = secondLLayout.childCount
                for (childIndex2 in 0 until childCount2) {
                    val view1 = secondLLayout.getChildAt(childIndex2)
                    if (view1.javaClass == ThumbnailItemView::class.java) {
                        (view1 as ThumbnailItemView).prepareLoadImage()
                    }
                }
            }
        }
    }

    // 選單
    val mMenuListener =
        View.OnClickListener { _: View? -> onMenuClicked() }

    // 給其他頁面託付使用
    fun setBoardEssencePage(aboardEssencePage: BoardEssencePage) {
        boardEssencePage = aboardEssencePage
    }
    // 轉寄至信箱
    fun fSendMail() {
        boardEssencePage?.pushCommand(BahamutCommandFSendMail(UserSettings.propertiesUsername!!))
    }

    // 變更telnetView大小
    private fun reloadTelnetLayout() {
        val textWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            20.0f,
            context!!.resources.displayMetrics
        ).toInt()
        var telnetViewWidth = textWidth / 2 * 80
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
