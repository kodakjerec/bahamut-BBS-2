package com.kota.ASFramework.Dialog

import android.app.Dialog
import android.content.DialogInterface
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.PageController.ASViewController
import com.kota.ASFramework.PageController.ASViewControllerDisappearListener

open class ASDialog : Dialog, ASViewControllerDisappearListener {
    private var _controller: ASViewController? = null
    private var _on_back_delegate: ASDialogOnBackPressedDelegate? = null
    private var _showing: Boolean = false

    constructor(theme: Int) : super(ASNavigationController.getCurrentController(), theme) {
        initializeFields()
    }

    protected constructor(cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) 
        : super(ASNavigationController.getCurrentController(), cancelable, cancelListener) {
        initializeFields()
    }

    constructor() : super(ASNavigationController.getCurrentController()) {
        initializeFields()
    }

    private fun initializeFields() {
        _on_back_delegate = null
        _showing = false
        _controller = null
    }

    override fun dismiss() {
        try {
            _controller?.let { controller ->
                controller.unregisterDisappearListener(this)
                _controller = null
            }
            _showing = false
            super.dismiss()
        } catch (ignored: Exception) {
            // 忽略異常
        }
    }

    override fun show() {
        try {
            super.show()
            _showing = true
        } catch (ignored: Exception) {
            // 忽略異常
        }
    }

    override fun hide() {
        try {
            super.hide()
            _showing = false
        } catch (ignored: Exception) {
            // 忽略異常
        }
    }

    override fun isShowing(): Boolean {
        return _showing
    }

    fun getCurrentOrientation(): Int {
        val currentController = ASNavigationController.getCurrentController()
        return currentController?.getCurrentOrientation() ?: 1
    }

    open fun getName(): String {
        return "ASDialog"
    }

    fun setIsCancelable(cancelable: Boolean): ASDialog {
        isCancelable = cancelable
        return this
    }

    override fun onBackPressed() {
        if (_on_back_delegate?.onASDialogBackPressed(this) != true) {
            super.onBackPressed()
        }
    }

    fun setOnBackDelegate(aDelegate: ASDialogOnBackPressedDelegate?): ASDialog {
        _on_back_delegate = aDelegate
        return this
    }

    fun scheduleDismissOnPageDisappear(aController: ASViewController?): ASDialog {
        _controller?.unregisterDisappearListener(this)
        _controller = aController
        _controller?.registerDisappearListener(this)
        return this
    }

    override fun onASViewControllerWillDisappear(aController: ASViewController) {
        if (isShowing) {
            dismiss()
        }
    }

    override fun onASViewControllerDidDisappear(aController: ASViewController) {
        // 空實現
    }
}
