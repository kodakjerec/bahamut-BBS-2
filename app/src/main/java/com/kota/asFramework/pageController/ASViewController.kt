package com.kota.asFramework.pageController

import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.view.View
import java.util.Vector

abstract class ASViewController {
    lateinit var navigationController: ASNavigationController
    protected var isLoaded: Boolean = false
    private var aSPageView: ASPageView? = null
    private var isContainedInContainer = false
    var isMarkedRemoved: Boolean = false
        private set
    var isMarkedAdded: Boolean = false
        private set
    var isPageAppeared: Boolean = false
        private set
    var isPageDisappeared: Boolean = true
        private set
    private var isRequestRefresh = false
    private var operationListeners: Vector<ASViewControllerOperationListener?>? = null
    private var appearListeners: Vector<ASViewControllerAppearListener?>? = null
    private var disappearListeners: Vector<ASViewControllerDisappearListener?>? = null

    abstract val pageLayout: Int

    var pageView: ASPageView?
        get() = this.aSPageView
        set(aPageView) {
            if (this.aSPageView != null) {
                this.aSPageView!!.ownerController = null
            }
            this.aSPageView = aPageView
            if (this.aSPageView != null) {
                this.aSPageView!!.ownerController = this
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
        if (this.operationListeners != null) {
            val listeners = Vector<ASViewControllerOperationListener?>(this.operationListeners!!)
            for (listener in listeners) {
                listener?.onASViewControllerWillRemoveFromNavigationController(this)
            }
        }
        onPageDidRemoveFromNavigationController()
    }

    fun notifyPageDidAddToNavigationController() {
        if (this.operationListeners != null) {
            val listeners = Vector<ASViewControllerOperationListener?>(this.operationListeners!!)
            for (listener in listeners) {
                listener?.onASViewControllerWillAddToNavigationController(this)
            }
        }
        onPageDidAddToNavigationController()
    }

    fun notifyPageWillAppear() {
        this.isPageDisappeared = false
        if (this.appearListeners != null) {
            val listeners = Vector<ASViewControllerAppearListener?>(this.appearListeners!!)
            for (listener in listeners) {
                listener?.onASViewControllerWillAppear(this)
            }
        }
        onPageWillAppear()
    }

    fun notifyPageDidAppear() {
        this.isPageAppeared = true
        if (this.appearListeners != null) {
            val listeners = Vector<ASViewControllerAppearListener?>(this.appearListeners!!)
            for (listener in listeners) {
                listener?.onASViewControllerDidAppear(this)
            }
        }
        onPageDidAppear()
        if (this.isRequestRefresh) {
            onPageRefresh()
            this.isRequestRefresh = false
        }
    }

    fun notifyPageWillDisappear() {
        this.isPageAppeared = false
        if (this.disappearListeners != null) {
            val listeners = Vector<ASViewControllerDisappearListener?>(this.disappearListeners!!)
            for (listener in listeners) {
                listener?.onASViewControllerWillDisappear(this)
            }
        }
        onPageWillDisappear()
    }

    fun notifyPageDidDisappear() {
        this.isPageDisappeared = true
        if (this.disappearListeners != null) {
            val listeners = Vector<ASViewControllerDisappearListener?>(this.disappearListeners!!)
            for (listener in listeners) {
                listener?.onASViewControllerDidDisappear(this)
            }
        }
        onPageDidDisappear()
    }

    fun requestPageRefresh() {
        if (this.isPageAppeared) {
            onPageRefresh()
        } else {
            this.isRequestRefresh = true
        }
    }

    fun backToMainToolbarAfterExtendToolbarClicked(): Boolean {
        return true
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        onPageDidUnload()
    }

    open fun clear() {
    }

    val context: ASNavigationController?
        get() = this.navigationController

    val resource: Resources?
        get() = this.navigationController.resources

    fun findViewById(viewID: Int): View? {
        if (this.aSPageView != null) {
            return this.aSPageView!!.findViewById(viewID)
        }
        return null
    }

    fun getSystemService(name: String): Any? {
        return this.navigationController.getSystemService(name)
    }

    fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
    }

    open fun onBackPressed(): Boolean {
        this.navigationController.popViewController()
        return true
    }

    open fun onSearchButtonClicked(): Boolean {
        return false
    }

    open fun onMenuButtonClicked(): Boolean {
        return false
    }

    fun startActivity(intent: Intent?) {
        this.navigationController.startActivity(intent)
    }

    fun startActivityForResult(intent: Intent?, code: Int) {
        this.navigationController.startActivityForResult(intent, code)
    }

    fun reloadLayout() {
        this.navigationController.reloadLayout()
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
        get() = this.navigationController.topController === this

    val assets: AssetManager?
        get() = this.navigationController.assets

    fun containsInContainer(): Boolean {
        return this.isContainedInContainer
    }

    fun setContainsInContainer(contains: Boolean) {
        this.isContainedInContainer = contains
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

    fun registerPageOperationListener(aListener: ASViewControllerOperationListener) {
        if (this.operationListeners == null) {
            this.operationListeners = Vector<ASViewControllerOperationListener?>()
        }
        this.operationListeners!!.add(aListener)
    }

    fun unregisterPageOperationListener(aListener: ASViewControllerOperationListener) {
        if (this.operationListeners != null) {
            this.operationListeners!!.remove(aListener)
        }
    }

    fun registerAppearListener(aListener: ASViewControllerAppearListener) {
        if (this.appearListeners == null) {
            this.appearListeners = Vector<ASViewControllerAppearListener?>()
        }
        this.appearListeners!!.add(aListener)
    }

    fun unregisterAppearListener(aListener: ASViewControllerAppearListener) {
        if (this.appearListeners != null) {
            this.appearListeners!!.remove(aListener)
        }
    }

    fun registerDisappearListener(aListener: ASViewControllerDisappearListener?) {
        if (this.disappearListeners == null) {
            this.disappearListeners = Vector<ASViewControllerDisappearListener?>()
        }
        this.disappearListeners!!.add(aListener)
    }

    fun unregisterDisappearListener(aListener: ASViewControllerDisappearListener?) {
        if (this.disappearListeners != null) {
            this.disappearListeners!!.remove(aListener)
        }
    }

    companion object {
        const val ALERT_LEFT_BUTTON: Int = -1
        const val ALERT_MIDDLE_BUTTON: Int = -3
        const val ALERT_RIGHT_BUTTON: Int = -2
    }
}