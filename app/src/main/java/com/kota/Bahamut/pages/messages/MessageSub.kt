package com.kota.Bahamut.pages.messages

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.DialogInsertExpression
import com.kota.Bahamut.dialogs.DialogInsertExpressionListener
import com.kota.Bahamut.dialogs.DialogInsertSymbol
import com.kota.Bahamut.dialogs.DialogShortenImage
import com.kota.Bahamut.dialogs.DialogShortenUrl
import com.kota.Bahamut.dialogs.DialogShortenUrlListener
import com.kota.Bahamut.pages.blockListPage.ArticleExpressionListPage
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage

class MessageSub : TelnetPage() {
    private var listViewRef: ASListView? = null
    var tempMessage: BahaMessage? = null
    private var isPostDelayedSuccess = false

    // Compose states
    var senderNameState by mutableStateOf("")
    var contentState by mutableStateOf(TextFieldValue(""))

    override val pageLayout: Int
        get() = 0

    override val isTopPage: Boolean
        get() = true

    override val isPopupPage: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                MessageSubContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    /** 設定內容 */
    fun setSenderName(senderName: String) {
        senderNameState = senderName

        val db = MessageDatabase(context)
        try {
            listViewRef?.adapter = null
            val messageList = db.getIdMessage(senderName)
            val myAdapter = MessageSubAdapter(messageList)
            listViewRef?.adapter = myAdapter
            listViewRef?.setSelection(myAdapter.count - 1)
        } finally {
            db.close()
        }
    }

    /** 臨時插入一個訊息 */
    fun insertMessage(item: BahaMessage) {
        if (item.senderName == senderNameState) {
            val db = MessageDatabase(context)
            try {
                db.updateReceiveMessage(item.senderName)
            } finally {
                db.close()
            }
            ASCoroutine.ensureMainThread {
                val myAdapter = listViewRef?.adapter as? MessageSubAdapter
                myAdapter?.addItem(item)
            }
        }
    }

    fun insertString(str: String) {
        val current = contentState
        val start = current.selection.start.coerceIn(0, current.text.length)
        val end = current.selection.end.coerceIn(0, current.text.length)
        val newText = current.text.replaceRange(start, end, str).take(59)
        val newCursor = (start + str.length).coerceAtMost(59)
        contentState = TextFieldValue(newText, TextRange(newCursor))
    }

    private fun onPostClicked() {
        if (contentState.text.isNotEmpty()) {
            sendMessagePart1()
            contentState = TextFieldValue("")
        }
    }

    /** 送出訊息-1 試著啟動訊息 */
    private fun sendMessagePart1() {
        val aSenderName = senderNameState.trim()
        val aMessage = contentState.text.trim()
        TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.CTRL_S)

        val db = MessageDatabase(context)
        try {
            val bahaMessage = db.sendMessage(aSenderName, aMessage)
            if (bahaMessage != null) {
                tempMessage = bahaMessage
                insertMessage(bahaMessage)
            }
        } finally {
            db.close()
        }

        messageASCoroutine?.cancel()
        messageASCoroutine?.postDelayed(3000L)
        isPostDelayedSuccess = false
    }

    /** 送出訊息-2 送出對方id */
    fun sendMessagePart2() {
        messageASCoroutine?.cancel()

        if (tempMessage != null) {
            val aSenderName = tempMessage?.senderName
            val builder = TelnetOutputBuilder.create()
                .pushString("$aSenderName\n")
                .build()
            TelnetClient.myInstance!!.sendDataToServer(builder)

            messageASCoroutine?.cancel()
            messageASCoroutine?.postDelayed(3000L)
            isPostDelayedSuccess = false
        }
    }

    /** 送出訊息-3 送出內容 */
    fun sendMessagePart3() {
        messageASCoroutine?.cancel()
        isPostDelayedSuccess = true

        if (tempMessage != null) {
            tempMessage?.status = MessageStatus.Success
            val aMessage = tempMessage?.message
            val builder = TelnetOutputBuilder.create()
                .pushString("$aMessage\n")
                .build()
            TelnetClient.myInstance!!.sendDataToServer(builder)

            val db = MessageDatabase(context)
            try {
                db.updateSendMessage(tempMessage!!)
                val lv = listViewRef
                if (lv != null) {
                    for (i in lv.childCount - 1 downTo 0) {
                        val view = lv.getChildAt(i)
                        if (view is MessageSubSend) {
                            if (view.myBahaMessage.id == tempMessage?.id) {
                                view.setStatus(tempMessage?.status)
                                break
                            }
                        }
                    }
                }
            } finally {
                db.close()
            }
            tempMessage = null
        }
    }

    fun sendMessageFail(status: MessageStatus) {
        messageASCoroutine?.cancel()
        isPostDelayedSuccess = false

        if (tempMessage != null) {
            tempMessage?.status = status
            val db = MessageDatabase(context)
            try {
                db.updateSendMessage(tempMessage!!)
                val lv = listViewRef
                if (lv != null) {
                    for (i in lv.childCount - 1 downTo 0) {
                        val view = lv.getChildAt(i)
                        if (view is MessageSubSend) {
                            if (view.myBahaMessage.id == tempMessage?.id) {
                                view.setStatus(tempMessage?.status)
                                break
                            }
                        }
                    }
                }
            } finally {
                db.close()
            }
            tempMessage = null
        }
    }

    private var messageASCoroutine: ASCoroutine? = object : ASCoroutine() {
        override suspend fun run() {
            if (!isPostDelayedSuccess) {
                sendMessageFail(MessageStatus.Offline)
                ASToast.showLongToast("私訊無反應，對方可能不在線上")
            }
        }
    }

    @Composable
    fun MessageSubContent() {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // Header: Back button + Sender Name
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
                Spacer(modifier = Modifier.width(16.dp))
                BahaText(
                    text = senderNameState,
                    color = colors.titleBarTitle,
                    fontSize = BahaTextSize.SUBTITLE
                )
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // Chat Message List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        ASListView(ctx).apply {
                            divider = null
                            listViewRef = this
                            if (senderNameState.isNotEmpty()) {
                                setSenderName(senderNameState)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Edit Field
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            BahaInputField(
                value = contentState,
                onValueChange = { if (it.text.length <= 59) contentState = it },
                placeholder = stringResource(R.string.input_content_here),
                maxLength = 59,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onPostClicked() }),
                modifier = Modifier.fillMaxWidth()
            )

            // 底部工具列 (符號, 表情, 短網址, 縮圖, 發送)
            HorizontalDivider(color = colors.toolbarDivider, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.toolbarBackground)
            ) {
                BahaButton(
                    text = stringResource(R.string.symbol),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        val dialog = DialogInsertSymbol()
                        dialog.setListener { str -> insertString(str) }
                        dialog.show()
                    }
                )
                BahaButton(
                    text = stringResource(R.string.face),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        val items = UserSettings.articleExpressions
                        DialogInsertExpression.createDialog().setTitle("表情符號").addItems(items)
                            .setListener(object : DialogInsertExpressionListener {
                                override fun onListDialogItemClicked(paramASListDialog: DialogInsertExpression, paramInt: Int, paramString: String) {
                                    insertString(items[paramInt])
                                }
                                override fun onListDialogSettingClicked() {
                                    navigationController.pushViewController(ArticleExpressionListPage())
                                }
                            }).scheduleDismissOnPageDisappear(this@MessageSub).show()
                    }
                )
                BahaButton(
                    text = stringResource(R.string.dialog_shorten_url_title),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        val dialog = DialogShortenUrl()
                        dialog.setListener(object : DialogShortenUrlListener {
                            override fun onShortenUrlDone(str: String?) {
                                if (str != null) insertString(str)
                            }
                        })
                        dialog.show()
                    }
                )
                BahaButton(
                    text = stringResource(R.string.dialog_shorten_img_title),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        val shortenTimes = UserSettings.propertiesNoVipShortenTimes
                        if (!UserSettings.propertiesVIP && shortenTimes > 30) {
                            ASToast.showLongToast(getContextString(R.string.vip_only_message))
                        } else {
                            val intent = Intent(TempSettings.myActivity, DialogShortenImage::class.java)
                            startActivity(intent)
                        }
                    }
                )
                BahaButton(
                    text = stringResource(R.string.post),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onPostClicked() }
                )
            }
        }
    }
}