package com.kota.Bahamut.pages.theme

import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.articlePage.ArticlePageTextItemView
import com.kota.Bahamut.pages.boardPage.BoardPageItemView
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.telnet.TelnetClient
import com.kota.telnet.model.TelnetRow
import com.kota.telnetUI.TelnetHeaderItemView
import com.kota.telnetUI.TelnetPage
import java.util.Vector

class ThemeManagerPage : TelnetPage() {
    private lateinit var mainLayout: LinearLayout
    private lateinit var previewContainer: LinearLayout
    private lateinit var themeButtons: List<Button>
    private var previewIndex: Int = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_THEME_MANAGER_PAGE

    override val pageLayout: Int
        get() = R.layout.theme_manager_page

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as LinearLayout
        previewContainer = mainLayout.findViewById(R.id.Theme_Manager_Page_PreviewContainer)

        previewIndex = ThemeStore.getSelectIndex()

        val themeNames = listOf("預設", "粉紅", "EInk")
        themeButtons = listOf(
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_0),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_1),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_2)
        )

        // 隱藏預留按鈕 3 與 4
        mainLayout.findViewById<View>(R.id.Theme_Manager_Page_Button_3)?.visibility = View.GONE
        mainLayout.findViewById<View>(R.id.Theme_Manager_Page_Button_4)?.visibility = View.GONE

        themeNames.forEachIndexed { index, name ->
            if (index < themeButtons.size) {
                val button: Button = themeButtons[index]
                button.text = name
                button.setOnClickListener {
                    previewIndex = index
                    updatePreview(previewIndex)
                }
            }
        }

        // 底部工具列：返回與套用
        findViewById(R.id.Theme_Manager_Page_Toolbar_Back)?.setOnClickListener {
            onBackPressed()
        }

        findViewById(R.id.Theme_Manager_Page_Toolbar_Apply)?.setOnClickListener {
            if (previewIndex == ThemeStore.getSelectIndex()) {
                Toast.makeText(context, "此外觀已是目前套用的外觀", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dialog = ASAlertDialog("THEME_CHANGE_CONFIRM")
            dialog.setTitle("更換外觀")
                .setMessage("更換外觀將會中斷目前的連線並重新啟動應用程式，是否確定更換?")
                .addButton("取消")
                .addButton("確定")
                .setListener(object : ASAlertDialogListener {
                    override fun onAlertDialogDismissWithButtonIndex(
                        paramASAlertDialog: ASAlertDialog,
                        paramInt: Int
                    ) {
                        if (paramInt == 1) {
                            ThemeStore.setSelectIndex(previewIndex)
                            Toast.makeText(
                                context,
                                getContextString(R.string.theme_manager_page_msg01),
                                Toast.LENGTH_SHORT
                            ).show()

                            // 執行斷線流程
                            TelnetClient.myInstance?.close()
                            TempSettings.lastVisitArticleNumber = 0

                            // 立即重啟 Activity 以套用原生主題
                            context?.recreate()
                        }
                    }
                }).show()
        }

        // 初始渲染預覽範例
        updatePreview(previewIndex)
    }

    /** 動態渲染選擇的主題預覽範例 (包含 TelnetHeaderItemView, BoardPageItemView, ArticlePageTextItemView) */
    private fun updatePreview(index: Int) {
        // 更新頂端按鈕選取狀態 α 視覺標示
        themeButtons.forEachIndexed { btnIndex, button ->
            button.alpha = if (btnIndex == index) 1.0f else 0.5f
        }

        previewContainer.removeAllViews()

        // 建立對應試閱主題的 Context
        val themeResId = ThemeStore.getThemeResIdForIndex(index)
        val themedContext = ContextThemeWrapper(context, themeResId)

        // 1. TelnetHeaderItemView 範例
        val headerView = TelnetHeaderItemView(themedContext)
        headerView.setData("【看板標題】Chat (洽特)", "看板: Chat (洽特)", "線上: 1234 人")
        previewContainer.addView(headerView)

        // 分隔線
        val divider1 = View(themedContext)
        divider1.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        ).apply { setMargins(0, 8, 0, 8) }
        divider1.setBackgroundResource(R.color.divider_color)
        previewContainer.addView(divider1)

        // 2. BoardPageItemView 範例
        val boardItemView = BoardPageItemView(themedContext)
        val sampleBoardItem = BoardPageItem.create().apply {
            title = "【試閱】這是看板文章標題範例"
            itemNumber = 12345
            date = "09/26"
            author = "bahaUser"
            isMarked = true
            gy = 10
            isReply = true
            isRead = false
        }
        boardItemView.setItem(sampleBoardItem)
        previewContainer.addView(boardItemView)

        // 分隔線
        val divider2 = View(themedContext)
        divider2.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        ).apply { setMargins(0, 8, 0, 8) }
        divider2.setBackgroundResource(R.color.divider_color)
        previewContainer.addView(divider2)

        // 3. ArticlePageTextItemView 範例
        val articleItemView = ArticlePageTextItemView(themedContext)
        articleItemView.setAuthor("bahaUser", "勇者")
        val sampleText = "這是文章內文與超連結試閱範例：\nhttps://user.gamer.com.tw/"
        val sampleRows = Vector<TelnetRow>()
        val row1 = TelnetRow()
        val bytes = try {
            sampleText.toByteArray(charset("Big5"))
        } catch (_: Exception) {
            sampleText.toByteArray(Charsets.UTF_8)
        }
        val len = minOf(bytes.size, 80)
        System.arraycopy(bytes, 0, row1.data, 0, len)
        for (i in 0 until len) {
            row1.myTextColorArray[i] = 7.toByte()
        }
        sampleRows.add(row1)
        articleItemView.setContent(sampleText, sampleRows)
        previewContainer.addView(articleItemView)

        // 分隔線
        val divider3 = View(themedContext)
        divider3.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        ).apply { setMargins(0, 8, 0, 8) }
        divider3.setBackgroundResource(R.color.divider_color)
        previewContainer.addView(divider3)

        // 4. 按鈕範例 (一般按鈕 & 危險按鈕)
        val buttonContainer = LinearLayout(themedContext).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        val normalBtn = Button(themedContext, null, 0, R.style.ToolbarItem).apply {
            text = "一般按鈕"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 0, 4, 0)
            }
        }

        val dangerBtn = Button(themedContext, null, 0, R.style.ToolbarItem_Danger).apply {
            text = "危險按鈕"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 0, 4, 0)
            }
        }

        buttonContainer.addView(normalBtn)
        buttonContainer.addView(dangerBtn)
        previewContainer.addView(buttonContainer)
    }

    override fun onPageWillAppear() {
        super.onPageWillAppear()
        navigationController.setNavigationTitle(getContextString(R.string.theme_manager_page))
    }

    override fun onBackPressed(): Boolean {
        PageContainer.instance!!.cleanThemeManagerPage()
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        return true
    }
}
