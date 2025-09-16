package com.kota.ASFramework.Dialog

import android.annotation.SuppressLint
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.R

/* loaded from: classes.dex */
class ASProcessingDialog : ASDialog() {
    private var _message_label: TextView? = null
    private var _on_back_delegate: ASProcessingDialogOnBackDelegate? = null

    init {
        requestWindowFeature(1)
        setContentView(R.layout.as_processing_dialog)
        if (getWindow() != null) getWindow()!!.setBackgroundDrawable(null)
        buildContentView()
    }

    fun buildContentView() {
        // frame_view
        val frame_view = findViewById<LinearLayout>(R.id.as_processing_dialog_frame_view)
        _message_label = frame_view.findViewById<TextView?>(R.id.as_processing_dialog_text)
        _message_label!!.setText(R.string.zero_word)
    }

    // com.kota.ASFramework.Dialog.ASDialog, android.app.Dialog
    override fun show() {
        super.show()
    }

    // com.kota.ASFramework.Dialog.ASDialog, android.app.Dialog, android.content.DialogInterface
    override fun dismiss() {
        super.dismiss()
    }

    fun setOnBackDelegate(onBackDelegate: ASProcessingDialogOnBackDelegate?) {
        this._on_back_delegate = onBackDelegate
    }

    // com.kota.ASFramework.Dialog.ASDialog, android.app.Dialog
    override fun onBackPressed() {
        if (this._on_back_delegate == null || !this._on_back_delegate!!.onASProcessingDialogOnBackDetected(
                this
            )
        ) {
            super.onBackPressed()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _instance: ASProcessingDialog? = null
        private fun construceInstance() {
            _instance = ASProcessingDialog()
        }

        private fun releaseInstance() {
            _instance = null
        }

        @JvmStatic
        @JvmOverloads
        fun showProcessingDialog(
            aMessage: String?,
            onBackDelegate: ASProcessingDialogOnBackDelegate? = null
        ) {
            if (!ASNavigationController.getCurrentController().isInBackground()) {
                object : ASRunner() {
                    // from class: com.kota.ASFramework.Dialog.ASProcessingDialog.1
                    // com.kota.ASFramework.Thread.ASRunner
                    public override fun run() {
                        if (_instance == null) {
                            construceInstance()
                        }
                        setMessage(aMessage)
                        _instance!!.setOnBackDelegate(onBackDelegate)
                        if (!_instance!!.isShowing()) {
                            _instance!!.show()
                        }
                    }
                }.runInMainThread()
            }
        }

        @JvmStatic
        fun dismissProcessingDialog() {
            if (!ASNavigationController.getCurrentController().isInBackground()) {
                object : ASRunner() {
                    public override fun run() {
                        if (_instance != null) {
                            _instance!!.dismiss()
                            releaseInstance()
                        }
                    }
                }.runInMainThread()
            }
        }

        @JvmStatic
        fun setMessage(message: String?) {
            object : ASRunner() {
                // from class: com.kota.ASFramework.Dialog.ASProcessingDialog.2
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    if (_instance != null) {
                        _instance!!._message_label!!.setText(message)
                    }
                }
            }.runInMainThread()
        }
    }
}