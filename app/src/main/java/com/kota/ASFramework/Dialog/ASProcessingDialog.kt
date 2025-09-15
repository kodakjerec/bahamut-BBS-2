package com.kota.ASFramework.Dialog

import android.annotation.SuppressLint
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.R

class ASProcessingDialog : ASDialog() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var _instance: ASProcessingDialog? = null
        
        private fun constructInstance() {
            _instance = ASProcessingDialog()
        }
        
        private fun releaseInstance() {
            _instance = null
        }
        
        fun showProcessingDialog(message: String) {
            showProcessingDialog(message, null)
        }
        
        fun showProcessingDialog(message: String, onBackDelegate: ASProcessingDialogOnBackDelegate?) {
            if (!ASNavigationController.getCurrentController().isInBackground()) {
                object : ASRunner() {
                    override fun run() {
                        if (_instance == null) {
                            constructInstance()
                        }
                        setMessage(message)
                        _instance?.setOnBackDelegate(onBackDelegate)
                        if (_instance?.isShowing != true) {
                            _instance?.show()
                        }
                    }
                }.runInMainThread()
            }
        }
        
        fun dismissProcessingDialog() {
            if (!ASNavigationController.getCurrentController().isInBackground()) {
                object : ASRunner() {
                    override fun run() {
                        _instance?.let { instance ->
                            instance.dismiss()
                            releaseInstance()
                        }
                    }
                }.runInMainThread()
            }
        }
        
        fun setMessage(message: String) {
            object : ASRunner() {
                override fun run() {
                    _instance?._message_label?.text = message
                }
            }.runInMainThread()
        }
    }
    
    private var _message_label: TextView? = null
    private var _on_back_delegate: ASProcessingDialogOnBackDelegate? = null
    
    init {
        requestWindowFeature(1)
        setContentView(R.layout.as_processing_dialog)
        window?.setBackgroundDrawable(null)
        buildContentView()
    }
    
    private fun buildContentView() {
        // frame_view
        val frameView = findViewById<LinearLayout>(R.id.as_processing_dialog_frame_view)
        _message_label = frameView.findViewById(R.id.as_processing_dialog_text)
        _message_label?.setText(R.string.zero_word)
    }
    
    override fun show() {
        super.show()
    }
    
    override fun dismiss() {
        super.dismiss()
    }
    
    fun setOnBackDelegate(onBackDelegate: ASProcessingDialogOnBackDelegate?) {
        _on_back_delegate = onBackDelegate
    }
    
    override fun onBackPressed() {
        if (_on_back_delegate?.onASProcessingDialogOnBackDetected(this) != true) {
            super.onBackPressed()
        }
    }
}
