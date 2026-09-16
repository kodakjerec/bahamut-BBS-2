package com.kota.Bahamut.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kota.Bahamut.ui.components.ButtonType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 全域 Compose 對話框管理器
 * 負責集中管理與響應 Processing (Loading) 與 Alert 對話框的顯示狀態
 */
object BahaDialogManager {

    data class ProcessingState(
        val isShowing: Boolean = false,
        val message: String = "處理中...",
        val isCancelable: Boolean = false,
        val onDismiss: (() -> Unit)? = null
    )

    data class AlertState(
        val isShowing: Boolean = false,
        val title: String? = null,
        val message: String? = null,
        val buttons: List<BahaDialogButton> = emptyList(),
        val onDismiss: (() -> Unit)? = null
    )

    private val _processingState = MutableStateFlow(ProcessingState())
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private val _alertState = MutableStateFlow(AlertState())
    val alertState: StateFlow<AlertState> = _alertState.asStateFlow()

    /**
     * 顯示處理中 / 載入中對話框
     */
    fun showProcessing(
        message: String = "處理中...",
        isCancelable: Boolean = false,
        onDismiss: (() -> Unit)? = null
    ) {
        _processingState.value = ProcessingState(
            isShowing = true,
            message = message,
            isCancelable = isCancelable,
            onDismiss = onDismiss
        )
    }

    /**
     * 關閉處理中對話框
     */
    fun dismissProcessing() {
        _processingState.value = _processingState.value.copy(isShowing = false)
    }

    /**
     * 更新處理中對話框文字 (例如百分比進度)
     */
    fun updateProcessingMessage(message: String) {
        if (_processingState.value.isShowing) {
            _processingState.value = _processingState.value.copy(message = message)
        }
    }

    /**
     * 顯示提示對話框
     */
    fun showAlert(
        title: String? = null,
        message: String? = null,
        buttons: List<BahaDialogButton> = emptyList(),
        onDismiss: (() -> Unit)? = null
    ) {
        _alertState.value = AlertState(
            isShowing = true,
            title = title,
            message = message,
            buttons = buttons,
            onDismiss = onDismiss
        )
    }

    /**
     * 顯示標準確認 / 取消對話框
     */
    fun showConfirm(
        title: String,
        message: String,
        confirmText: String = "確定",
        dismissText: String = "取消",
        confirmButtonType: ButtonType = ButtonType.NORMAL,
        onConfirm: () -> Unit,
        onDismiss: (() -> Unit)? = null
    ) {
        val buttons = listOf(
            BahaDialogButton(
                text = dismissText,
                type = ButtonType.SECONDARY,
                onClick = {
                    dismissAlert()
                    onDismiss?.invoke()
                }
            ),
            BahaDialogButton(
                text = confirmText,
                type = confirmButtonType,
                onClick = {
                    dismissAlert()
                    onConfirm()
                }
            )
        )
        showAlert(
            title = title,
            message = message,
            buttons = buttons,
            onDismiss = onDismiss
        )
    }

    /**
     * 關閉提示對話框
     */
    fun dismissAlert() {
        _alertState.value = _alertState.value.copy(isShowing = false)
    }
}

/**
 * 全域對話框宿主 Composable
 * 掛載在頁面根層級 (如 TelnetComposePage / ASComposeViewController)，響應全域對話框狀態
 */
@Composable
fun BahaGlobalDialogHost() {
    val processingState by BahaDialogManager.processingState.collectAsState()
    val alertState by BahaDialogManager.alertState.collectAsState()

    if (processingState.isShowing) {
        BahaProcessingDialog(
            message = processingState.message,
            isCancelable = processingState.isCancelable,
            onDismissRequest = {
                processingState.onDismiss?.invoke()
                BahaDialogManager.dismissProcessing()
            }
        )
    }

    if (alertState.isShowing) {
        BahaAlertDialog(
            title = alertState.title,
            message = alertState.message,
            buttons = alertState.buttons,
            onDismissRequest = {
                alertState.onDismiss?.invoke()
                BahaDialogManager.dismissAlert()
            }
        )
    }
}

