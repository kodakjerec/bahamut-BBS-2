package com.kota.asFramework.dialog

import android.annotation.SuppressLint
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine

/**
 * [ASProcessingDialog] - 系統「載入中/處理中」的全域浮動對話框。
 *
 * 職責：
 * 1. 於背景處理網路通訊、BBS 載入或長耗時作業時呈送載入動畫與說明文字。
 * 2. 採用單例 (Singleton) 管理模式，確保全域同時僅存在一個處理中對話框。
 */
class ASProcessingDialog : ASDialog() {
    private var messageLabel: TextView? = null

    init {
        requestWindowFeature(1)
        setContentView(R.layout.as_processing_dialog)
        if (window != null) window?.setBackgroundDrawable(null)
        buildContentView()
    }

    /**
     * 建立對話框視圖內容
     */
    fun buildContentView() {
        val frameView = findViewById<LinearLayout>(R.id.as_processing_dialog_frame_view)
        messageLabel = frameView.findViewById(R.id.as_processing_dialog_text)
        messageLabel?.setText(R.string.zero_word)
    }

    override fun show() {
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var aSProcessingDialog: ASProcessingDialog? = null

        private fun constructInstance() {
            aSProcessingDialog = ASProcessingDialog()
        }

        private fun releaseInstance() {
            aSProcessingDialog = null
        }

        /**
         * 顯示處理中對話框
         *
         * @param aMessage 顯示說明文字 (如 "載入中")
         */
        @JvmStatic
        fun showProcessingDialog(aMessage: String?) {
            ASNavigationController.currentController?.isInBackground?.let {
                if (!it) {
                    ASCoroutine.ensureMainThread {
                        if (aSProcessingDialog == null) {
                            constructInstance()
                        }
                        setMessage(aMessage)
                        if (!aSProcessingDialog!!.isShowing) {
                            aSProcessingDialog?.show()
                        }
                    }
                }
            }
        }

        /**
         * 關閉並銷毀處理中對話框
         */
        @JvmStatic
        fun dismissProcessingDialog() {
            ASCoroutine.ensureMainThread {
                if (aSProcessingDialog != null) {
                    if (aSProcessingDialog!!.isShowing) {
                        aSProcessingDialog?.dismiss()
                    }
                    releaseInstance()
                }
            }
        }

        /**
         * 更新處理中對話框之文字訊息
         *
         * @param message 最新說明文字
         */
        @JvmStatic
        fun setMessage(message: String?) {
            ASCoroutine.ensureMainThread {
                if (aSProcessingDialog != null) {
                    aSProcessingDialog?.messageLabel?.text = message
                }
            }
        }
    }
}
