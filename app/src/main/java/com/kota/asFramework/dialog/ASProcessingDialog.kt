package com.kota.asFramework.dialog

import android.annotation.SuppressLint
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine

class ASProcessingDialog : ASDialog() {
    private var messageLabel: TextView? = null
    private var onBackDelegate: ASProcessingDialogOnBackDelegate? = null

    init {
        requestWindowFeature(1)
        setContentView(R.layout.as_processing_dialog)
        if (window != null) window?.setBackgroundDrawable(null)
        buildContentView()
    }

    fun buildContentView() {
        // frame_view
        val frameView = findViewById<LinearLayout>(R.id.as_processing_dialog_frame_view)
        messageLabel = frameView.findViewById(R.id.as_processing_dialog_text)
        messageLabel?.setText(R.string.zero_word)
    }

    // com.kota.asFramework.dialog.ASDialog, android.app.Dialog
    override fun show() {
        super.show()
    }

    // com.kota.asFramework.dialog.ASDialog, android.app.Dialog, android.content.DialogInterface
    override fun dismiss() {
        super.dismiss()
    }

    fun setOnBackDelegate(onBackDelegate: ASProcessingDialogOnBackDelegate?) {
        this.onBackDelegate = onBackDelegate
    }

    // com.kota.asFramework.dialog.ASDialog, android.app.Dialog
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this.onBackDelegate == null || !this.onBackDelegate!!.onASProcessingDialogOnBackDetected(
                this
            )
        ) {
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
            ASNavigationController.currentController?.isInBackground?.let {
                if (!it) {
                    ASCoroutine.ensureMainThread {
                        if (aSProcessingDialog != null) {
                            aSProcessingDialog?.dismiss()
                            releaseInstance()
                        }
                    }
                }
            }
        }

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