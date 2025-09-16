package com.kota.ASFramework.PageController

import android.content.Intent
import android.content.res.AssetManager
import android.view.View
import java.util.Vector

/* loaded from: classes.dex */
abstract class ASViewController {
    var navigationController: ASNavigationController? = null
    protected var _is_loaded: Boolean = false
    private var _page_view: ASPageView? = null
    private var _contains_in_container = false
    var isMarkedRemoved: Boolean = false
        private set
    var isMarkedAdded: Boolean = false
        private set
    var isPageAppeared: Boolean = false
        private set
    var isPageDisappeared: Boolean = true
        private set
    private var _is_request_refresh = false
    private var _operation_listeners: Vector<ASViewControllerOperationListener?>? = null
    private var _appear_listeners: Vector<ASViewControllerAppearListener?>? = null
    private var _disappear_listeners: Vector<ASViewControllerDisappearListener?>? = null

    abstract val pageLayout: Int

    var pageView: ASPageView?
        get() = this._page_view
        set(aPageView) {
            if (this._page_view != null) {
                this._page_view!!.setOwnerController(null)
            }
            this._page_view = aPageView
            if (this._page_view != null) {
                this._page_view!!.setOwnerController(this)
            }
        }

    open val pageType: Int
        get() = 0

    open fun onPageDidLoad() {
    }

    open fun onPageWillAppear() {
    }

    open fun onPageDidAppear() {
    }

    open fun onPageRefresh() {
    }

    fun onPageFreeMemory() {
    }

    open fun onPageWillDisappear() {
    }

    open fun onPageDidDisappear() {
    }

    open fun onPageDidRemoveFromNavigationController() {
    }

    fun onPageDidAddToNavigationController() {
    }

    open fun onPageDidUnload() {
    }

    fun notifyPageDidRemoveFromNavigationController() {
        if (this._operation_listeners != null) {
            val listeners = Vector<ASViewControllerOperationListener?>(this._operation_listeners)
            for (listener in listeners) {
                if (listener != null) {
                    listener.onASViewControllerWillRemoveFromNavigationController(this)
                }
            }
        }
        onPageDidRemoveFromNavigationController()
    }

    fun notifyPageDidAddToNavigationController() {
        if (this._operation_listeners != null) {
            val listeners = Vector<ASViewControllerOperationListener?>(this._operation_listeners)
            for (listener in listeners) {
                if (listener != null) {
                    listener.onASViewControllerWillAddToNavigationController(this)
                }
            }
        }
        onPageDidAddToNavigationController()
    }

    fun notifyPageWillAppear() {
        this.isPageDisappeared = false
        if (this._appear_listeners != null) {
            val listeners = Vector<ASViewControllerAppearListener?>(this._appear_listeners)
            for (listener in listeners) {
                if (listener != null) {
                    listener.onASViewControllerWillAppear(this)
                }
            }
        }
        onPageWillAppear()
    }

    fun notifyPageDidAppear() {
        this.isPageAppeared = true
        if (this._appear_listeners != null) {
            val listeners = Vector<ASViewControllerAppearListener?>(this._appear_listeners)
            for (listener in listeners) {
                if (listener != null) {
                    listener.onASViewControllerDidAppear(this)
                }
            }
        }
        onPageDidAppear()
        if (this._is_request_refresh) {
            onPageRefresh()
            this._is_request_refresh = false
        }
    }

    fun notifyPageWillDisappear() {
        this.isPageAppeared = false
        if (this._disappear_listeners != null) {
            val listeners = Vector<ASViewControllerDisappearListener?>(this._disappear_listeners)
            for (listener in listeners) {
                if (listener != null) {
                    listener.onASViewControllerWillDisappear(this)
                }
            }
        }
        onPageWillDisappear()
    }

    fun notifyPageDidDisappear() {
        this.isPageDisappeared = true
        if (this._disappear_listeners != null) {
            val listeners = Vector<ASViewControllerDisappearListener?>(this._disappear_listeners)
            for (listener in listeners) {
                if (listener != null) {
                    listener.onASViewControllerDidDisappear(this)
                }
            }
        }
        onPageDidDisappear()
    }

    fun requestPageRefresh() {
        if (this.isPageAppeared) {
            onPageRefresh()
        } else {
            this._is_request_refresh = true
        }
    }

    fun backToMainToolbarAfterExtendToolbarClicked(): Boolean {
        return true
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        onPageDidUnload()
        super.finalize()
    }

    open fun clear() {
    }

    val context: Context?
        get() = this.navigationController

    val resource: Resources?
        get() = this.navigationController!!.getResources()

    fun findViewById(viewID: Int): View? {
        if (this._page_view != null) {
            return this._page_view!!.findViewById<View?>(viewID)
        }
        return null
    }

    fun getSystemService(name: String): Any? {
        if (this.navigationController != null) {
            return this.navigationController!!.getSystemService(name)
        }
        return null
    }

    fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
    }

    open fun onBackPressed(): Boolean {
        this.navigationController!!.popViewController()
        return true
    }

    open fun onSearchButtonClicked(): Boolean {
        return false
    }

    open fun onMenuButtonClicked(): Boolean {
        return false
    }

    fun startActivity(intent: Intent?) {
        if (this.navigationController != null) {
            this.navigationController!!.startActivity(intent)
        }
    }

    fun startActivityForResult(intent: Intent?, code: Int) {
        if (this.navigationController != null) {
            this.navigationController!!.startActivityForResult(intent, code)
        }
    }

    fun reloadLayout() {
        if (this.navigationController != null) {
            this.navigationController!!.reloadLayout()
        }
    }

    fun onReceivedGestureUp(): Boolean {
        return false
    }

    fun onReceivedGestureDown(): Boolean {
        return false
    }

    fun onReceivedGestureLeft(): Boolean {
        return false
    }

    open fun onReceivedGestureRight(): Boolean {
        return false
    }

    open val isTopPage: Boolean
        get() = this.navigationController != null && this.navigationController!!.getTopController() === this

    val assets: AssetManager?
        get() = this.navigationController!!.getAssets()

    fun containsInContainer(): Boolean {
        return this._contains_in_container
    }

    fun setContainsInContainer(contains: Boolean) {
        this._contains_in_container = contains
    }

    fun prepareForRemove() {
        this.isMarkedRemoved = true
    }

    fun prepareForAdd() {
        this.isMarkedRemoved = false
        this.isMarkedAdded = true
    }

    fun cleanMark() {
        this.isMarkedRemoved = false
        this.isMarkedAdded = false
    }

    override fun toString(): String {
        return "ASViewController[Type:" + this.pageType + "]"
    }

    fun registerPageOperationListener(aListener: ASViewControllerOperationListener?) {
        if (this._operation_listeners == null) {
            this._operation_listeners = Vector<ASViewControllerOperationListener?>()
        }
        this._operation_listeners!!.add(aListener)
    }

    fun unregisterPageOperationListener(aListener: ASViewControllerOperationListener?) {
        if (this._operation_listeners != null) {
            this._operation_listeners!!.remove(aListener)
        }
    }

    fun registerAppearListener(aListener: ASViewControllerAppearListener?) {
        if (this._appear_listeners == null) {
            this._appear_listeners = Vector<ASViewControllerAppearListener?>()
        }
        this._appear_listeners!!.add(aListener)
    }

    fun unregisterAppearListener(aListener: ASViewControllerAppearListener?) {
        if (this._appear_listeners != null) {
            this._appear_listeners!!.remove(aListener)
        }
    }

    fun registerDisappearListener(aListener: ASViewControllerDisappearListener?) {
        if (this._disappear_listeners == null) {
            this._disappear_listeners = Vector<ASViewControllerDisappearListener?>()
        }
        this._disappear_listeners!!.add(aListener)
    }

    fun unregisterDisappearListener(aListener: ASViewControllerDisappearListener?) {
        if (this._disappear_listeners != null) {
            this._disappear_listeners!!.remove(aListener)
        }
    }

    companion object {
        val ALERT_LEFT_BUTTON: Int = -1
        val ALERT_MIDDLE_BUTTON: Int = -3
        val ALERT_RIGHT_BUTTON: Int = -2
    }
}