package com.kota.Bahamut.pages.blockListPage

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getShowHeader
import com.kota.Bahamut.service.NotificationSettings.setShowHeader
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.service.UserSettings.Companion.articleHeaders
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.resetArticleHeaders
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetPage
import java.util.Collections

class ArticleHeaderListPage : TelnetPage() {

    var inputTextState by mutableStateOf("")

    private val articleHeadersList = mutableStateListOf<String>()

    override val isPopupPage: Boolean
        get() = true

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BLOCK_LIST

    override val pageLayout: Int
        get() = 0

    override val isKeepOnOffline: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        showNotification()
        reload()
        return ComposeView(context).apply {
            setBahamutContent {
                ArticleHeaderListPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageWillDisappear() {
        notifyDataUpdated()
    }

    override fun onPageDidDisappear() {
        articleHeadersList.clear()
        super.onPageDidDisappear()
    }

    fun reload() {
        val elements = listOf(*articleHeaders)
        articleHeadersList.clear()
        articleHeadersList.addAll(elements)
    }

    private fun showNotification() {
        if (!getShowHeader()) {
            showLongToast(getContextString(R.string.notification_header))
            setShowHeader(true)
        }
    }

    private fun onAddClicked() {
        val name = inputTextState.trim()
        inputTextState = ""
        if (name.isNotEmpty()) {
            val newList: MutableList<String> = ArrayList(listOf(*articleHeaders))
            if (newList.contains(name)) {
                showErrorDialog(getContextString(R.string.already_have_item), this)
            } else {
                newList.add(name)
            }
            UserSettings.setArticleHeaders(newList)
            notifyDataUpdated()
            reload()
        } else {
            showErrorDialog(getContextString(R.string.please_input_id), this)
        }
    }

    private fun onDeleteClicked(index: Int) {
        if (index == 0) {
            showShortToast(getContextString(R.string.article_header_zero_error_message))
            return
        }
        if (index in articleHeadersList.indices) {
            articleHeadersList.removeAt(index)
            UserSettings.setArticleHeaders(ArrayList(articleHeadersList))
            notifyDataUpdated()
            reload()
        }
    }

    private fun moveArticleHeaderListItem(fromIndex: Int, toIndex: Int) {
        if (propertiesVIP) {
            if (fromIndex == 0 || toIndex == 0) {
                showShortToast(getContextString(R.string.article_header_zero_error_message))
                return
            }
            if (fromIndex in articleHeadersList.indices && toIndex in articleHeadersList.indices) {
                Collections.swap(articleHeadersList, fromIndex, toIndex)
                UserSettings.setArticleHeaders(ArrayList(articleHeadersList))
                notifyDataUpdated()
                reload()
            }
        } else {
            showShortToast(getContextString(R.string.vip_only_message))
        }
    }

    private fun onResetClicked() {
        createDialog()
            .setTitle(getContextString(R.string.reset))
            .setMessage(getContextString(R.string.reset_message))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.sure))
            .setListener { _: ASAlertDialog?, buttonIndex: Int ->
                if (buttonIndex > 0) {
                    showShortToast(getContextString(R.string.reset_ok))
                    inputTextState = ""
                    resetArticleHeaders()
                    reload()
                }
            }.show()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    // ---------------------------------------------------------------
    // Compose UI
    // ---------------------------------------------------------------

    @Composable
    fun ArticleHeaderListPageContent() {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(colors.pageBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaButton(
                    text = stringResource(R.string.reset),
                    type = ButtonType.DANGER,
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = { onResetClicked() }
                )
                BahaInputField(
                    value = inputTextState,
                    onValueChange = { inputTextState = it },
                    placeholder = stringResource(R.string.please_input_id),
                    modifier = Modifier
                        .weight(3f)
                        .padding(horizontal = 8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddClicked() })
                )
                BahaButton(
                    text = stringResource(R.string.add),
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = { onAddClicked() }
                )
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // 標題列表 (LazyColumn)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(
                    items = articleHeadersList,
                    key = { index, item -> "${index}_$item" }
                ) { index, item ->
                    ArticleHeaderItemRow(
                        name = item,
                        isVip = propertiesVIP,
                        canMoveUp = index > 1,
                        canMoveDown = index > 0 && index < articleHeadersList.size - 1,
                        onDelete = { onDeleteClicked(index) },
                        onMoveUp = { moveArticleHeaderListItem(index, index - 1) },
                        onMoveDown = { moveArticleHeaderListItem(index, index + 1) }
                    )
                    HorizontalDivider(color = colors.divider, thickness = 1.dp)
                }
            }

            // 底部工具列 (滿版無縫)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string._back),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                onClick = { onBackPressed() }
            )
        }
    }

    @Composable
    private fun ArticleHeaderItemRow(
        name: String,
        isVip: Boolean,
        canMoveUp: Boolean,
        canMoveDown: Boolean,
        onDelete: () -> Unit,
        onMoveUp: () -> Unit,
        onMoveDown: () -> Unit
    ) {
        val colors = AppTheme.colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(colors.pageBackground)
                .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BahaText(
                text = name,
                color = colors.textPrimary,
                fontSize = AppTheme.fontSize.title,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "刪除$name"
                    }
            )

            if (isVip) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = if (canMoveUp) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = if (canMoveDown) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            BahaButton(
                text = stringResource(R.string.delete),
                type = ButtonType.DANGER,
                fontSize = AppTheme.fontSize.body,
                modifier = Modifier
                    .width(80.dp)
                    .height(36.dp),
                onClick = onDelete
            )
        }
    }
}
