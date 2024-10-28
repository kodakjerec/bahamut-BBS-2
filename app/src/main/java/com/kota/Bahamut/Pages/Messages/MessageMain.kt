package com.kota.Bahamut.Pages.Messages

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.ASFramework.Dialog.ASListDialog
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASListView
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.Pages.Model.PostEditText
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.Pages.Theme.ThemeStore.getSelectTheme
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.CommonFunctions.rgbToInt
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import java.util.Vector

class MessageMain:TelnetPage() {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var listView: ASListView
    private lateinit var searchWord: PostEditText
    private lateinit var tabButtons: Array<Button>
    private var isPostDelayedSuccess = false

    override fun getPageLayout(): Int {
        return R.layout.message_main
    }

    override fun isPopupPage(): Boolean {
        return true
    }

    /** 顯示聊天小視窗  */
    private val showHideFloating = CompoundButton.OnCheckedChangeListener {
        _: CompoundButton?, isChecked: Boolean ->
        NotificationSettings.setShowMessageFloating(isChecked)
    }

    /** 搜尋聊天 */
    private val handleSearchWatcher = TextView.OnEditorActionListener { textView, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            handleSearchChats(textView.text.toString().lowercase())
            return@OnEditorActionListener true
        }
        false
    }
    private fun handleSearchChats(searchWord: String) {
        for (i in 0 until listView.childCount) {
            val view = listView.getChildAt(i)
            if (view is MessageMainItem) {
                val subView:MessageMainItem = view
                if (subView.getContent().senderName.lowercase().contains(searchWord)) {
                    subView.visibility = VISIBLE
                } else {
                    subView.visibility = GONE
                }
            }
        }
    }

    /** 切換分頁 */
    private val tabClickListener = View.OnClickListener { aView ->
        // 切換頁籤
        val theme = getSelectTheme()
        for (tabButton in tabButtons) {
            if (tabButton == aView) {
                tabButton.setTextColor(rgbToInt(theme.textColor))
                tabButton.setBackgroundColor(rgbToInt(theme.backgroundColor))
            } else {
                tabButton.setTextColor(rgbToInt(theme.textColorDisabled))
                tabButton.setBackgroundColor(rgbToInt(theme.backgroundColorDisabled))
            }
        }
    }

    /** 設定選單 */
    private fun openSettings() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string.setting))
            .addItem(getContextString(R.string.message_main_setting01))
            .addItem(getContextString(R.string.message_main_setting02))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    aDialog: ASListDialog,
                    index: Int,
                    aTitle: String
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog,
                    index: Int,
                    aTitle: String
                ) {
                    if (aTitle == getContextString(R.string.message_main_setting01)) {
                        sendSyncCommand()
                    } else if (aTitle == getContextString(R.string.message_main_setting02)) {
                        val db = MessageDatabase(context)
                        try {
                            db.clearDb()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            db.close()
                        }
                        sendSyncCommand()
                    }
                }
            }).show()
    }

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(mainLayout)

        // 分頁
        val btnChat = mainLayout.findViewById<Button>(R.id.Message_Main_Button_Chat)
        val btnList = mainLayout.findViewById<Button>(R.id.Message_Main_Button_List)
        btnChat.setOnClickListener(tabClickListener)
        btnList.setOnClickListener(tabClickListener)
        tabButtons = arrayOf(btnChat, btnList)
        btnChat.performClick()

        // 查詢
        searchWord = mainLayout.findViewById(R.id.Message_Main_Search)
        searchWord.setOnEditorActionListener(handleSearchWatcher)
        // 清空查詢
        val searchWordClear:TextView = mainLayout.findViewById(R.id.Message_Main_Search_Clear)
        searchWordClear.setOnClickListener { _->
            searchWord.setText("")
            handleSearchChats("")
        }

        // 切換浮動隱藏
        val checkBox = mainLayout.findViewById<CheckBox>(R.id.Message_Main_Checkbox)
        checkBox.isChecked = NotificationSettings.getShowMessageFloating()
        checkBox.setOnCheckedChangeListener(showHideFloating)
        val checkLayout = mainLayout.findViewById<LinearLayout>(R.id.Message_Main_CheckboxLayout)
        checkLayout.setOnClickListener { _ -> checkBox.isChecked = !checkBox.isChecked }

        val txtEsc = mainLayout.findViewById<TextView>(R.id.Message_Main_Back)
        txtEsc.setOnClickListener{ _-> onBackPressed() }

        listView = mainLayout.findViewById(R.id.Message_Main_Scroll)

        // 重置
        val btnSettings: Button = mainLayout.findViewById(R.id.Message_Main_Settings)
        btnSettings.setOnClickListener { _-> openSettings() }

        // 每次登入開啟訊息主視窗先同步一次
        if (TempSettings.isSyncMessageMain) {
            loadMessageList()
        } else {
            sendSyncCommand()
        }
    }

    override fun onBackPressed(): Boolean {
        // 顯示小視窗
        if (TempSettings.getMessageSmall() != null)
            TempSettings.getMessageSmall()!!.show()

        return super.onBackPressed()
    }

    /** 顯示訊息清單 */
    fun loadMessageList() {
        messageAsRunner.cancel()
        isPostDelayedSuccess = true

        ASProcessingDialog.dismissProcessingDialog()
        TempSettings.isSyncMessageMain = true

        val db = MessageDatabase(context)
        try {
            listView.adapter = null

            // 紀錄訊息
            val messageList = db.getAllAndNewestMessage()
            val myAdapter = MessageMainAdapter(messageList)
            listView.adapter = myAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }
    /** 收到訊息, 只更新特定人物的最新訊息 */
    fun loadMessageList(item:BahaMessage) {
        var findSender = false
        var senderView = MessageMainItem(context)
        for (i in 0 until listView.childCount) {
            val view = listView.getChildAt(i)
            if (view is MessageMainItem) {
                if (view.getContent().senderName==item.senderName) {
                    findSender = true
                    senderView = view
                }
            }
        }

        val db = MessageDatabase(context)
        try {
            val itemSummary = db.getIdNewestMessage(item.senderName)
            object: ASRunner(){
                override fun run() {
                    // 找到同名人物
                    if (findSender) {
                        senderView.setContent(itemSummary)
                    } else {
                        val myAdapter:MessageMainAdapter = listView.adapter as MessageMainAdapter
                        myAdapter.addItem(itemSummary)
                    }
                }
            }.runInMainThread()
        } finally {
            db.close()
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    /** 同步BBS訊息到DB */
    private fun sendSyncCommand() {
        // 送出查詢指令
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.CTRL_R)

        ASProcessingDialog.showProcessingDialog(getContextString(R.string.message_small_sync_msg01))

        messageAsRunner.postDelayed(3000)
        isPostDelayedSuccess = false
    }
    fun receiveSyncCommand(rows: Vector<TelnetRow>) {
        messageAsRunner.cancel()
        messageAsRunner.postDelayed(3000)
        isPostDelayedSuccess = false

        val db = MessageDatabase(context)
        try {
            var senderName: String
            var message: String

            var startIndex: Int
            var endIndex: Int
            rows.forEach { row->
                val rawString = row.rawString

                if (rawString.startsWith("☆")) {
                    // send
                    startIndex = 1
                    endIndex = rawString.indexOf("(")
                    senderName = rawString.substring(startIndex, endIndex).trim()

                    startIndex = rawString.indexOf("：")+1
                    message = rawString.substring(startIndex).trim()

                    db.syncMessage(senderName, message, 1)
                } else if (rawString.startsWith("★")) {
                    // receive
                    startIndex = 1
                    endIndex = rawString.indexOf("(")
                    senderName = rawString.substring(startIndex, endIndex).trim()

                    startIndex = rawString.indexOf("：")+1
                    message = rawString.substring(startIndex).trim()

                    db.syncMessage(senderName, message, 0)
                }
            }
        } finally {
            db.close()
        }
    }

    // 強制讀取訊息進入讀取完畢
    private var messageAsRunner: ASRunner = object : ASRunner() {
        override fun run() {
            if (!isPostDelayedSuccess)
                loadMessageList()
        }
    }
}