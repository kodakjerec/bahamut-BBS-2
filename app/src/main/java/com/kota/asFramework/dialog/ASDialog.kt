package com.kota.asFramework.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASViewController
import com.kota.asFramework.pageController.ASViewControllerDisappearListener

open class ASDialog : Dialog, ASViewControllerDisappearListener {
    private var aSViewController: ASViewController?
    private var backPressedHandler: ASDialogOnBackPressedDelegate?
    private var isShowing: Boolean

    constructor(theme: Int) : super(ASNavigationController.currentController!!, theme) {
        this.backPressedHandler = null
        this.isShowing = false
        this.aSViewController = null
    }

    protected constructor(
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(ASNavigationController.currentController!!, cancelable, cancelListener) {
        this.backPressedHandler = null
        this.isShowing = false
        this.aSViewController = null
    }

    constructor() : super(ASNavigationController.currentController!!) {
        this.backPressedHandler = null
        this.isShowing = false
        this.aSViewController = null
    }

    // android.app.Dialog, android.content.DialogInterface
    override fun dismiss() {
        try {
            if (this.aSViewController != null) {
                this.aSViewController?.unregisterDisappearListener(this)
                this.aSViewController = null
            }
            this.isShowing = false
            super.dismiss()
        } catch (_: Exception) {
        }
    }

    // android.app.Dialog
    override fun show() {
        try {
            super.show()
            this.isShowing = true
        } catch (_: Exception) {
        }
    }

    override fun hide() {
        try {
            super.hide()
            this.isShowing = false
        } catch (_: Exception) {
        }
    }

    // android.app.Dialog
    override fun isShowing(): Boolean {
        return this.isShowing
    }

    val currentOrientation: Int
        get() {
            val currentController: ASNavigationController? =
                ASNavigationController.currentController!!
            if (currentController == null) {
                return 1
            }
            return currentController.currentOrientation
        }

    open val name: String?
        get() = "ASDialog"

    fun setIsCancelable(cancelable: Boolean): ASDialog {
        setCancelable(cancelable)
        return this
    }

    // android.app.Dialog
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this.backPressedHandler == null || !this.backPressedHandler?.onASDialogBackPressed(this)) {
            super.onBackPressed()
        }
    }

    fun setOnBackDelegate(aDelegate: ASDialogOnBackPressedDelegate?): ASDialog {
        this.backPressedHandler = aDelegate
        return this
    }

    fun scheduleDismissOnPageDisappear(aController: ASViewController?): ASDialog {
        if (this.aSViewController != null) {
            this.aSViewController?.unregisterDisappearListener(this)
        }
        this.aSViewController = aController
        if (this.aSViewController != null) {
            this.aSViewController?.registerDisappearListener(this)
        }
        return this
    }

    // com.kota.ASFramework.PageController.ASViewControllerDisappearListener
    override fun onASViewControllerWillDisappear(paramASViewController: ASViewController?) {
        if (this@ASDialog.isShowing) {
            dismiss()
        }
    }

    // com.kota.ASFramework.PageController.ASViewControllerDisappearListener
    override fun onASViewControllerDidDisappear(paramASViewController: ASViewController?) {
    }

    // 變更dialog寬度
    fun setDialogWidth(targetView: View) {
        val screenHeight = context.resources.displayMetrics.heightPixels
        val screenWidth = context.resources.displayMetrics.widthPixels
        val dialogHeight = (screenHeight * 0.7).toInt()
        val dialogWidth = (screenWidth * 0.7).toInt()
        val oldLayoutParams = targetView.layoutParams
        oldLayoutParams.width = dialogWidth
        oldLayoutParams.height = dialogHeight
        targetView.layoutParams = oldLayoutParams
    }
}
