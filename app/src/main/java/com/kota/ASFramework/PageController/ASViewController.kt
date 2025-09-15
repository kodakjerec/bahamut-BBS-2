package com.kota.ASFramework.PageController

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.view.View
import java.util.*

/* loaded from: classes.dex */
abstract class ASViewController {
    companion object {
        const val ALERT_LEFT_BUTTON = -1
        const val ALERT_MIDDLE_BUTTON = -3
        const val ALERT_RIGHT_BUTTON = -2
    }

    private var _controller: ASNavigationController? = null
    protected var _is_loaded = false
    private var _page_view: ASPageView? = null
    private var _contains_in_container = false
    private var _marked_removed = false
    private var _marked_added = false
    private var _is_appeared = false
    private var _is_disappeared = true
    private var _is_request_refresh = false
    private var _operation_listeners: Vector<ASViewControllerOperationListener>? = null
    private var _appear_listeners: Vector<ASViewControllerAppearListener>? = null
    private var _disappear_listeners: Vector<ASViewControllerDisappearListener>? = null

    abstract fun getPageLayout(): Int

    fun setPageView(aPageView: ASPageView?) {
        _page_view?.setOwnerController(null)
        _page_view = aPageView
        _page_view?.setOwnerController(this)
    }

    fun getPageView(): ASPageView? = _page_view

    open fun getPageType(): Int = 0

    open fun onPageDidLoad() {}

    open fun onPageWillAppear() {}

    open fun onPageDidAppear() {}

    open fun onPageRefresh() {}

    open fun onPageFreeMemory() {}

    open fun onPageWillDisappear() {}

    open fun onPageDidDisappear() {}

    open fun onPageDidRemoveFromNavigationController() {}

    open fun onPageDidAddToNavigationController() {}

    open fun onPageDidUnload() {}

    fun notifyPageDidRemoveFromNavigationController() {
        _operation_listeners?.let { listeners ->
            val listenersCopy = Vector(listeners)
            for (listener in listenersCopy) {
                listener?.onASViewControllerWillRemoveFromNavigationController(this)
            }
        }
        onPageDidRemoveFromNavigationController()
    }

    fun notifyPageDidAddToNavigationController() {
        _operation_listeners?.let { listeners ->
            val listenersCopy = Vector(listeners)
            for (listener in listenersCopy) {
                listener?.onASViewControllerWillAddToNavigationController(this)
            }
        }
        onPageDidAddToNavigationController()
    }

    fun notifyPageWillAppear() {
        _is_disappeared = false
        _appear_listeners?.let { listeners ->
            val listenersCopy = Vector(listeners)
            for (listener in listenersCopy) {
                listener?.onASViewControllerWillAppear(this)
            }
        }
        onPageWillAppear()
    }

    fun notifyPageDidAppear() {
        _is_appeared = true
        _appear_listeners?.let { listeners ->
            val listenersCopy = Vector(listeners)
            for (listener in listenersCopy) {
                listener?.onASViewControllerDidAppear(this)
            }
        }
        onPageDidAppear()
        if (_is_request_refresh) {
            onPageRefresh()
            _is_request_refresh = false
        }
    }

    fun notifyPageWillDisappear() {
        _is_appeared = false
        _disappear_listeners?.let { listeners ->
            val listenersCopy = Vector(listeners)
            for (listener in listenersCopy) {
                listener?.onASViewControllerWillDisappear(this)
            }
        }
        onPageWillDisappear()
    }

    fun notifyPageDidDisappear() {
        _is_disappeared = true
        _disappear_listeners?.let { listeners ->
            val listenersCopy = Vector(listeners)
            for (listener in listenersCopy) {
                listener?.onASViewControllerDidDisappear(this)
            }
        }
        onPageDidDisappear()
    }

    fun requestPageRefresh() {
        if (_is_appeared) {
            onPageRefresh()
        } else {
            _is_request_refresh = true
        }
    }

    fun isPageAppeared(): Boolean = _is_appeared

    fun isPageDisappeared(): Boolean = _is_disappeared

    open fun backToMainToolbarAfterExtendToolbarClicked(): Boolean = true

    @Throws(Throwable::class)
    protected fun finalize() {
        onPageDidUnload()
    }

    open fun clear() {}

    fun getContext(): Context? = _controller

    fun getResource(): Resources? = _controller?.resources

    fun getNavigationController(): ASNavigationController? = _controller

    fun findViewById(viewID: Int): View? = _page_view?.findViewById(viewID)

    fun getSystemService(name: String): Any? = _controller?.getSystemService(name)

    protected open fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {}

    protected open fun onBackPressed(): Boolean {
        getNavigationController()?.popViewController()
        return true
    }

    protected open fun onSearchButtonClicked(): Boolean = false

    protected open fun onMenuButtonClicked(): Boolean = false

    fun startActivity(intent: Intent) {
        _controller?.startActivity(intent)
    }

    fun startActivityForResult(intent: Intent, code: Int) {
        _controller?.startActivityForResult(intent, code)
    }

    fun setNavigationController(controller: ASNavigationController?) {
        _controller = controller
    }

    fun reloadLayout() {
        _controller?.reloadLayout()
    }

    open fun onReceivedGestureUp(): Boolean = false

    open fun onReceivedGestureDown(): Boolean = false

    open fun onReceivedGestureLeft(): Boolean = false

    open fun onReceivedGestureRight(): Boolean = false

    fun isTopPage(): Boolean = 
        getNavigationController()?.getTopController() == this

    fun getAssets(): AssetManager? = _controller?.assets

    fun containsInContainer(): Boolean = _contains_in_container

    fun setContainsInContainer(contains: Boolean) {
        _contains_in_container = contains
    }

    protected fun isMarkedRemoved(): Boolean = _marked_removed

    protected fun isMarkedAdded(): Boolean = _marked_added

    protected fun prepareForRemove() {
        _marked_removed = true
    }

    protected fun prepareForAdd() {
        _marked_removed = false
        _marked_added = true
    }

    protected fun cleanMark() {
        _marked_removed = false
        _marked_added = false
    }

    override fun toString(): String = "ASViewController[Type:${getPageType()}]"

    fun registerPageOperationListener(aListener: ASViewControllerOperationListener) {
        if (_operation_listeners == null) {
            _operation_listeners = Vector()
        }
        _operation_listeners?.add(aListener)
    }

    fun unregisterPageOperationListener(aListener: ASViewControllerOperationListener) {
        _operation_listeners?.remove(aListener)
    }

    fun registerAppearListener(aListener: ASViewControllerAppearListener) {
        if (_appear_listeners == null) {
            _appear_listeners = Vector()
        }
        _appear_listeners?.add(aListener)
    }

    fun unregisterAppearListener(aListener: ASViewControllerAppearListener) {
        _appear_listeners?.remove(aListener)
    }

    fun registerDisappearListener(aListener: ASViewControllerDisappearListener) {
        if (_disappear_listeners == null) {
            _disappear_listeners = Vector()
        }
        _disappear_listeners?.add(aListener)
    }

    fun unregisterDisappearListener(aListener: ASViewControllerDisappearListener) {
        _disappear_listeners?.remove(aListener)
    }
}
