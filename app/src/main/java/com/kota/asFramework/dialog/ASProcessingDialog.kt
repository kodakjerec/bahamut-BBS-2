package com.kota.asFramework.dialog

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kota.Bahamut.ui.dialogs.BahaProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine

class ASProcessingDialog : ASDialog() {
    private var onBackDelegate: ASProcessingDialogOnBackDelegate? = null
    var messageText by mutableStateOf("處理中...")

    init {
        setComposeContent {
            BahaProcessingDialog(message = messageText)
        }
    }

    fun setOnBackDelegate(onBackDelegate: ASProcessingDialogOnBackDelegate?) {
        this.onBackDelegate = onBackDelegate
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this.onBackDelegate == null || !this.onBackDelegate!!.onASProcessingDialogOnBackDetected(this)) {
            super.onBackPressed()
        }
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

        @JvmStatic
        @JvmOverloads
        fun showProcessingDialog(
            aMessage: String?,
            onBackDelegate: ASProcessingDialogOnBackDelegate? = null
        ) {
            ASNavigationController.currentController?.isInBackground?.let {
                if (!it) {
                    ASCoroutine.ensureMainThread {
                        if (aSProcessingDialog == null) {
                            constructInstance()
                        }
                        setMessage(aMessage)
                        aSProcessingDialog!!.setOnBackDelegate(onBackDelegate)
                        if (!aSProcessingDialog!!.isShowing) {
                            aSProcessingDialog?.show()
                        }
                    }
                }
            }
        }

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

        @JvmStatic
        fun setMessage(message: String?) {
            ASCoroutine.ensureMainThread {
                if (aSProcessingDialog != null && message != null) {
                    aSProcessingDialog?.messageText = message
                }
            }
        }
    }
}