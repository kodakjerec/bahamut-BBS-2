package com.kota.Bahamut.pages.login

import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast

import android.util.Log

class LoginWebDebugView(private val context: Context) : ASDialog() {
    private val webView = WebView(context)
    private var loginWeb: LoginWeb? = null
    var onDismissCallback: (() -> Unit)? = null

    override val name: String?
        get() = "BahamutWebDebugDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.login_web_sign_in))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        BahaAlertDialogContent(
            modifier = Modifier.widthIn(min = 300.dp, max = 380.dp),
            title = CommonFunctions.getContextString(R.string.login_web_sign_in),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.exit),
                    type = ButtonType.SECONDARY,
                    onClick = { dismiss() }
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                AndroidView(
                    factory = { webView },
                    onRelease = { view ->
                        try {
                            (view.parent as? android.view.ViewGroup)?.removeView(view)
                        } catch (e: Exception) {
                            Log.e("LoginWebDebugView", "Error in onRelease", e)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    /**
     * 初始化並開始自動登入流程
     */
    fun startAutoLogin(
        onLoginSuccess: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ): LoginWebDebugView {
        this.onDismissCallback = onComplete
        loginWeb = LoginWeb(context, webView)

        loginWeb?.init(
            onSignDetected = {
                // 檢測到簽到對話框的處理
                ASToast.showShortToast(context.getString(R.string.login_web_sign_in_msg03))
                // 通知外部成功
                onLoginSuccess?.invoke()
                // 關閉對話框
                dismiss()
            },
            onFail = {
                // 簽到失敗的處理
                ASToast.showShortToast(context.getString(R.string.login_web_sign_in_msg02))
                // 關閉對話框
                dismiss()
            },
            onManual = { message ->
                // 需要手動驗證的處理
                ASToast.showShortToast(message)
                this.show()
            }
        )

        return this
    }

    override fun dismiss() {
        super.dismiss()
        // 清理 LoginWeb 資源
        loginWeb?.cleanup()
        loginWeb = null
        val callback = onDismissCallback
        onDismissCallback = null
        callback?.invoke()
    }
}