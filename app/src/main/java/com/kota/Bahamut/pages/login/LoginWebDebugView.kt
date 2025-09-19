package com.kota.Bahamut.pages.login

import android.content.Context
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast
import com.kota.Bahamut.R

class LoginWebDebugView(private val context: Context) : ASDialog() {
    private var mainLayout: LinearLayout
    private var webView: WebView
    private var btnClose: Button
    private var isClickButton = false
    private var loginWeb: LoginWeb? = null

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_login_signin)
        window?.setBackgroundDrawable(null)

        mainLayout = findViewById(R.id.dialog_login_signin_layout)
        webView = findViewById(R.id.debug_webview)
        btnClose = findViewById(R.id.debug_btn_close)

        // 初始設為小尺寸顯示在左上角
        setDialogWidthHeight()
    }

    /**
     * 初始化並開始自動登入流程
     */
    fun startAutoLogin(
        onLoginSuccess: (() -> Unit)? = null
    ): LoginWebDebugView {
        loginWeb = LoginWeb(context, webView) // 直接傳遞 WebView 給 LoginWeb

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

    override fun show() {
        btnClose.setOnClickListener(closeOnClickListener)
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
        // 清理 LoginWeb 資源
        loginWeb?.cleanup()
        loginWeb = null
    }

    /** 關閉按鈕 */
    private val closeOnClickListener = OnClickListener { _ ->
        isClickButton = true
        dismiss()
    }

    // 變更dialog寬度和高度
    private fun setDialogWidthHeight() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        val dialogWidth = (screenWidth * 0.7).toInt()
        val dialogHeight = (screenHeight * 0.8).toInt()
        val oldLayoutParams: ViewGroup.LayoutParams = mainLayout.layoutParams
        oldLayoutParams.width = dialogWidth
        oldLayoutParams.height = dialogHeight
        mainLayout.layoutParams = oldLayoutParams
    }
}