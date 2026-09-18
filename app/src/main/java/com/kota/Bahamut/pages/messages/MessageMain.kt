package com.kota.Bahamut.pages.messages

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.myContext
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaCheckbox
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage
import com.kota.textEncoder.B2UEncoder
import java.util.Vector

class MessageMain : TelnetPage() {
    private var listViewRef: ASListView? = null
    private var isPostDelayedSuccess = false
    private var isUnderList = false

    // Compose states
    var currentTabState by mutableIntStateOf(0) // 0-Chat, 1-List
    var isFloatCheckedState by mutableStateOf(NotificationSettings.getShowMessageFloating())
    var searchWordState by mutableStateOf("")

    override val pageLayout: Int
        get() = 0

    override val isPopupPage: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                MessageMainContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageDidLoad() {
        if (TempSettings.isSyncMessageMain) {
            loadMessageList()
        } else {
            sendSyncCommand()
        }
    }

    override fun onBackPressed(): Boolean {
        if (isUnderList) {
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
        }
        if (TempSettings.getMessageSmall() != null) {
            TempSettings.getMessageSmall()?.show()
        }
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    private fun handleSearchSubmit() {
        if (isUnderList) {
            TelnetClient.myInstance!!.sendDataToServer(
                TelnetOutputBuilder.create()
                    .pushString("/")
                    .pushKey(TelnetKeyboard.CTRL_Y)
                    .pushString(searchWordState.lowercase() + "\n")
                    .build()
            )
        } else {
            handleSearchChats(searchWordState.lowercase())
        }
    }

    private fun handleSearchChats(searchWord: String) {
        val lv = listViewRef ?: return
        for (i in 0 until lv.childCount) {
            val view = lv.getChildAt(i)
            if (view is MessageMainChatItem) {
                if (view.getContent().senderName.lowercase().contains(searchWord)) {
                    view.visibility = View.VISIBLE
                } else {
                    view.visibility = View.GONE
                }
            }
        }
    }

    private fun switchTab(tab: Int) {
        currentTabState = tab
        if (tab == 0) {
            isUnderList = false
            loadMessageList()
        } else {
            isUnderList = true
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.CTRL_U)
        }
    }

    private fun openSettings() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string.setting))
            .addItem(getContextString(R.string.message_main_setting01))
            .addItem(getContextString(R.string.message_main_setting02), true)
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(paramASListDialog: ASListDialog?, index: Int, title: String?): Boolean = true
                override fun onListDialogItemClicked(paramASListDialog: ASListDialog?, index: Int, title: String?) {
                    if (title == getContextString(R.string.message_main_setting01)) {
                        sendSyncCommand()
                    } else if (title == getContextString(R.string.message_main_setting02)) {
                        val db = MessageDatabase(myContext!!)
                        try {
                            db.clearDb()
                        } catch (e: Exception) {
                            Log.e(javaClass.simpleName, e.message.toString())
                        } finally {
                            db.close()
                        }
                        sendSyncCommand()
                    }
                }
            }).show()
    }

    fun loadMessageList() {
        if (isUnderList) {
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
            isUnderList = false
        }
        messageASCoroutine.cancel()
        isPostDelayedSuccess = true
        ASProcessingDialog.dismissProcessingDialog()
        TempSettings.isSyncMessageMain = true

        val db = MessageDatabase(myContext ?: return)
        try {
            listViewRef?.adapter = null
            val messageList = db.getAllAndNewestMessage()
            val myAdapter = MessageMainChatAdapter(messageList)
            listViewRef?.adapter = myAdapter
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message.toString())
        } finally {
            db.close()
        }
    }

    fun loadMessageList(item: BahaMessage) {
        val lv = listViewRef ?: return
        var findSender = false
        var senderView = MessageMainChatItem(myContext ?: return)
        for (i in 0 until lv.childCount) {
            val view = lv.getChildAt(i)
            if (view is MessageMainChatItem) {
                if (view.getContent().senderName == item.senderName) {
                    findSender = true
                    senderView = view
                }
            }
        }

        val db = MessageDatabase(myContext ?: return)
        try {
            val itemSummary = db.getIdNewestMessage(item.senderName)
            ASCoroutine.ensureMainThread {
                if (findSender) {
                    senderView.setContent(itemSummary)
                } else {
                    val myAdapter = lv.adapter as? MessageMainChatAdapter
                    myAdapter?.addItem(itemSummary)
                }
            }
        } finally {
            db.close()
        }
    }

    fun loadUserList(fromRows: Vector<TelnetRow>) {
        isUnderList = true
        val userList: MutableList<MessageMainListItemStructure> = ArrayList()
        val rows: Vector<TelnetRow> = Vector(fromRows)

        for (i in 3 until rows.size step 1) {
            val row = rows[i]
            val item = MessageMainListItemStructure()
            val bytes = row.data
            item.index = B2UEncoder.instance!!.encodeToString(bytes.copyOfRange(1, 5))
            item.senderName = B2UEncoder.instance!!.encodeToString(bytes.copyOfRange(8, 19))
            item.nickname = B2UEncoder.instance!!.encodeToString(bytes.copyOfRange(21, 37))
            item.ip = B2UEncoder.instance!!.encodeToString(bytes.copyOfRange(39, 56))
            item.status = B2UEncoder.instance!!.encodeToString(bytes.copyOfRange(58, 70))
            userList.add(item)
        }

        val myAdapter = MessageMainListAdapter(userList)
        listViewRef?.adapter = myAdapter
    }

    private fun sendSyncCommand() {
        TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.CTRL_R)
        ASProcessingDialog.showProcessingDialog(getContextString(R.string.message_small_sync_msg01))
        messageASCoroutine.postDelayed(3000L)
        isPostDelayedSuccess = false
    }

    fun receiveSyncCommand(rows: Vector<TelnetRow>) {
        messageASCoroutine.cancel()
        messageASCoroutine.postDelayed(3000L)
        isPostDelayedSuccess = false

        val db = MessageDatabase(myContext ?: return)
        try {
            rows.forEach { row ->
                val rawString = row.rawString
                if (rawString.startsWith("☆")) {
                    val startIndex = 1
                    val endIndex = rawString.indexOf("(")
                    val senderName = rawString.substring(startIndex, endIndex).trim()
                    val msg = rawString.substring(rawString.indexOf("：") + 1).trim()
                    db.syncMessage(senderName, msg, 1)
                } else if (rawString.startsWith("★")) {
                    val startIndex = 1
                    val endIndex = rawString.indexOf("(")
                    val senderName = rawString.substring(startIndex, endIndex).trim()
                    val msg = rawString.substring(rawString.indexOf("：") + 1).trim()
                    db.syncMessage(senderName, msg, 0)
                }
            }
        } finally {
            db.close()
        }
    }

    private var messageASCoroutine: ASCoroutine = object : ASCoroutine() {
        override suspend fun run() {
            if (!isPostDelayedSuccess) loadMessageList()
        }
    }

    @Composable
    fun MessageMainContent() {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // Header: Back button + Float checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaText(
                    text = stringResource(R.string._back),
                    color = colors.textPrimary,
                    fontSize = BahaTextSize.BODY,
                    modifier = Modifier.clickable { onBackPressed() }
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val newVal = !isFloatCheckedState
                        isFloatCheckedState = newVal
                        NotificationSettings.setShowMessageFloating(newVal)
                    }
                ) {
                    BahaText(
                        text = stringResource(R.string.message_small_show_float),
                        color = colors.textSecondary,
                        fontSize = BahaTextSize.TINY,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    BahaCheckbox(
                        checked = isFloatCheckedState,
                        onCheckedChange = { newVal ->
                            isFloatCheckedState = newVal
                            NotificationSettings.setShowMessageFloating(newVal)
                        }
                    )
                }
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaInputField(
                    value = searchWordState,
                    onValueChange = { searchWordState = it },
                    placeholder = stringResource(R.string.search_id),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { handleSearchSubmit() }),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BahaText(
                    text = stringResource(R.string.message_main_search_clear),
                    color = colors.titleBarDetail,
                    fontSize = BahaTextSize.CAPTION,
                    modifier = Modifier
                        .clickable {
                            searchWordState = ""
                            handleSearchChats("")
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // ListView
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        ASListView(ctx).apply {
                            listViewRef = this
                            if (currentTabState == 0) loadMessageList()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // List mode pagination toolbar (Prev, Next)
            if (currentTabState == 1) {
                HorizontalDivider(color = colors.toolbarDivider, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(colors.toolbarBackground)
                ) {
                    BahaButton(
                        text = stringResource(R.string.prev_page),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.PAGE_UP) }
                    )
                    BahaButton(
                        text = stringResource(R.string.next_page),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.PAGE_DOWN) }
                    )
                }
            }

            // Bottom tabs: 聊天, 名單, 設定
            HorizontalDivider(color = colors.toolbarDivider, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.toolbarBackground)
            ) {
                BahaButton(
                    text = stringResource(R.string.message_main_tab_chat),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { switchTab(0) }
                )
                BahaButton(
                    text = stringResource(R.string.message_main_tab_list),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { switchTab(1) }
                )
                if (currentTabState == 0) {
                    BahaButton(
                        text = stringResource(R.string.setting),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { openSettings() }
                    )
                }
            }
        }
    }
}