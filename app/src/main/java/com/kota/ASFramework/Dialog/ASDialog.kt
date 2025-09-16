package com.kota.ASFramework.Dialog

import android.app.Dialog
import android.content.DialogInterface
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.PageController.ASViewController
import com.kota.ASFramework.PageController.ASViewControllerDisappearListener

open class ASDialog : Dialog, ASViewControllerDisappearListener {
    private var _controller: ASViewController?
    private var _on_back_delegate: ASDialogOnBackPressedDelegate?
    private var _showing: Boolean

    constructor(theme: Int) : super(ASNavigationController.getCurrentController(), theme) {
        this._on_back_delegate = null
        this._showing = false
        this._controller = null
    }

    protected constructor(
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(ASNavigationController.getCurrentController(), cancelable, cancelListener) {
        this._on_back_delegate = null
        this._showing = false
        this._controller = null
    }

    constructor() : super(ASNavigationController.getCurrentController()) {
        this._on_back_delegate = null
        this._showing = false
        this._controller = null
    }

    // android.app.Dialog, android.content.DialogInterface
    override fun dismiss() {
        try {
            if (this._controller != null) {
                this._controller!!.unregisterDisappearListener(this)
                this._controller = null
            }
            this._showing = false
            super.dismiss()
        } catch (ignored: Exception) {
        }
    }

    // android.app.Dialog
    override fun show() {
        try {
            super.show()
            this._showing = true
        } catch (ignored: Exception) {
        }
    }

    override fun hide() {
        try {
            super.hide()
            this._showing = false
        } catch (ignored: Exception) {
        }
    }

    // android.app.Dialog
    override fun isShowing(): Boolean {
        return this._showing
    }

    val currentOrientation: Int
        get() {
            val current_controller: ASNavigationController? =
                ASNavigationController.getCurrentController()
            if (current_controller == null) {
                return 1
            }
            return current_controller.currentOrientation
        }

    open val name: String?
        get() = "ASDialog"

    fun setIsCancelable(cancelable: Boolean): ASDialog {
        setCancelable(cancelable)
        return this
    }

    // android.app.Dialog
    override fun onBackPressed() {
        if (this._on_back_delegate == null || !this._on_back_delegate!!.onASDialogBackPressed(this)) {
            super.onBackPressed()
        }
    }

    fun setOnBackDelegate(aDelegate: ASDialogOnBackPressedDelegate?): ASDialog {
        this._on_back_delegate = aDelegate
        return this
    }

    fun scheduleDismissOnPageDisappear(aController: ASViewController?): ASDialog {
        if (this._controller != null) {
            this._controller!!.unregisterDisappearListener(this)
        }
        this._controller = aController
        if (this._controller != null) {
            this._controller!!.registerDisappearListener(this)
        }
        return this
    }

    // com.kota.ASFramework.PageController.ASViewControllerDisappearListener
    override fun onASViewControllerWillDisappear(aController: ASViewController?) {
        if (isShowing()) {
            dismiss()
        }
    }

    // com.kota.ASFramework.PageController.ASViewControllerDisappearListener
    override fun onASViewControllerDidDisappear(aController: ASViewController?) {
    }
}
