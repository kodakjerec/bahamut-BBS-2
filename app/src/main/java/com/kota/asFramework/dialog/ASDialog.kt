package com.kota.asFramework.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASViewController
import com.kota.asFramework.pageController.ASViewControllerDisappearListener
import java.lang.ref.WeakReference

/**
 * [ASDialog] - 對話框基類 (抽象化 Dialog)。
 *
 * 職責：
 * 1. 繼承 Android 原生 [Dialog]，為所有自訂對話框 (如 [ASAlertDialog], [ASListDialog]) 提供基底。
 * 2. 實作 [ASViewControllerDisappearListener]，當關聯的 [ASViewController] 離開畫面時自動排程關閉對話框 ([scheduleDismissOnPageDisappear])。
 * 3. 追蹤全域對話框實例，提供 [dismissAllDialogs] 一鍵清理所有排隊中的對話框。
 */
open class ASDialog : Dialog, ASViewControllerDisappearListener {
    private var aSViewController: ASViewController? = null
    private var isShowing: Boolean = false

    constructor(theme: Int) : super(ASNavigationController.currentController!!, theme) {
        trackDialog(this)
    }

    protected constructor(
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(ASNavigationController.currentController!!, cancelable, cancelListener) {
        trackDialog(this)
    }

    constructor() : super(ASNavigationController.currentController!!) {
        trackDialog(this)
    }

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

    override fun isShowing(): Boolean {
        return this.isShowing
    }

    /** 取得當前螢幕方向 (1: 豎屏, 2: 橫屏) */
    val currentOrientation: Int
        get() {
            val currentController: ASNavigationController? =
                ASNavigationController.currentController
            if (currentController == null) {
                return 1
            }
            return currentController.currentOrientation
        }

    open val name: String?
        get() = "ASDialog"

    /** 設定是否可透過點擊外部或返回鍵取消對話框 */
    fun setIsCancelable(cancelable: Boolean): ASDialog {
        setCancelable(cancelable)
        return this
    }

    /**
     * 排程：當指定的 [ASViewController] 頁面離開/消失時自動關閉此對話框
     *
     * @param aController 關聯的頁面控制器
     */
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

    override fun onASViewControllerWillDisappear(paramASViewController: ASViewController?) {
        if (this@ASDialog.isShowing) {
            dismiss()
        }
    }

    override fun onASViewControllerDidDisappear(paramASViewController: ASViewController?) {
    }

    /** 變更對話框寬度 (依據螢幕比例適應) */
    fun setDialogWidth() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels

        val dialogWidth: Int = if (currentOrientation == 2) {
            (screenHeight * 0.7).toInt()
        } else {
            (screenWidth * 0.8).toInt()
        }

        window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /** 變更對話框寬度與高度 (依據螢幕方向適應) */
    fun setDialogWidthHeight() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels

        val dialogWidth: Int
        val dialogHeight: Int

        if (currentOrientation == 2) {
            dialogWidth = (screenWidth * 0.7).toInt()
            dialogHeight = (screenHeight * 0.8).toInt()
        } else {
            dialogWidth = (screenWidth * 0.8).toInt()
            dialogHeight = (screenHeight * 0.7).toInt()
        }

        window?.setLayout(dialogWidth, dialogHeight)
    }

    companion object {
        private val _allDialogs = ArrayList<WeakReference<ASDialog>>()

        private fun trackDialog(dialog: ASDialog) {
            synchronized(_allDialogs) {
                _allDialogs.add(WeakReference(dialog))
            }
        }

        /**
         * 關閉並銷毀所有追蹤中的對話框
         */
        @JvmStatic
        fun dismissAllDialogs() {
            synchronized(_allDialogs) {
                val iterator = _allDialogs.iterator()
                while (iterator.hasNext()) {
                    val ref = iterator.next()
                    val dialog = ref.get()
                    if (dialog != null) {
                        try {
                            if (dialog.isShowing) {
                                dialog.dismiss()
                            }
                        } catch (_: Exception) {
                        }
                    }
                    iterator.remove()
                }
            }
        }
    }
}
