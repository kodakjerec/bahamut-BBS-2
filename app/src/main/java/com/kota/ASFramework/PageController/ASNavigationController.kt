package com.kota.ASFramework.PageController

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.Pages.Messages.MessageSmall
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.TelnetUI.TelnetPage
import java.util.*

/* loaded from: classes.dex */
open class ASNavigationController : Activity() {
    companion object {
        private var _current_controller: ASNavigationController? = null

        private fun setNavigationController(aController: ASNavigationController?) {
            _current_controller = aController
        }

        fun getCurrentController(): ASNavigationController? = _current_controller
    }

    private var _device_controller: ASDeviceController? = null
    private val _display_metrics = DisplayMetrics()
    private var _root_view: ASNavigationControllerView? = null
    private val _controllers = Vector<ASViewController>()
    private val _temp_controllers = Vector<ASViewController>()
    private val _remove_list = Vector<ASViewController>()
    private val _add_list = Vector<ASViewController>()
    private var _animation_enable = true
    private var _in_background = false
    private val _page_commands = Vector<PageCommand>()
    private var _is_animating = false

    /* loaded from: classes.dex */
    private abstract class PageCommand {
        var animated = true

        abstract fun run()
    }

    protected open fun onControllerWillLoad() {}

    protected open fun onControllerDidLoad() {}

    protected open fun onControllerWillFinish() {}

    fun setNavigationTitle(title: String) {
        super.setTitle(title)
    }

    protected open fun getControllerName(): String = ""

    private fun onMenuPressed(): Boolean {
        val page = getTopController()
        return page?.onMenuButtonClicked() == true
    }

    private fun onSearchPressed(): Boolean {
        val page = getTopController()
        return page?.onSearchButtonClicked() == true
    }

    override fun onBackPressed() {
        var handleBackButton = false
        val page = getTopController()
        if (page?.onBackPressed() == true) {
            handleBackButton = true
        }
        if (!handleBackButton) {
            finish()
        }
    }

    open fun onBackLongPressed(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 啟用 edge-to-edge 支援
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setNavigationController(this)

        windowManager.defaultDisplay.getMetrics(_display_metrics)
        ASRunner.construct()

        UserSettings(this)
        NotificationSettings.upgrade(this)

        _device_controller = ASDeviceController(this)
        onControllerWillLoad()

        _root_view = ASNavigationControllerView(this)
        _root_view?.setPageController(this)
        setContentView(_root_view)

        // 設定 WindowInsets 處理
        setupWindowInsets()

        onControllerDidLoad()
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return onBackLongPressed()
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> onMenuPressed()
            KeyEvent.KEYCODE_SEARCH -> onSearchPressed()
            else -> super.onKeyUp(keyCode, event)
        }
    }

    fun getTopController(): ASViewController? {
        var controller: ASViewController? = null
        synchronized(_controllers) {
            if (_controllers.size > 0) {
                controller = _controllers.lastElement()
            }
        }
        return controller
    }

    fun getAllController(): Vector<ASViewController> {
        val controllers: Vector<ASViewController>
        synchronized(_controllers) {
            controllers = _controllers
        }
        return controllers
    }

    private fun buildPageView(controller: ASViewController) {
        val pageView = ASPageView(this)
        pageView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        pageView.setBackgroundColor(View.MEASURED_STATE_MASK)
        layoutInflater.inflate(controller.getPageLayout(), pageView)
        controller.setPageView(pageView)
        _root_view?.getContentView()?.addView(pageView)
    }

    private fun cleanPageView(controller: ASViewController) {
        controller.setPageView(null)
    }

    fun addPageView(aPage: ASViewController) {
        val pageView = aPage.getPageView()
        _root_view?.post {
            aPage.onPageDidDisappear()
            _root_view?.removeView(pageView)
        }
    }

    fun removePageView(aPage: ASViewController) {
        val pageView = aPage.getPageView()
        _root_view?.post {
            aPage.onPageDidDisappear()
            _root_view?.removeView(pageView)
        }
    }

    fun removePageView(aPage: ASViewController, aAnimation: Animation) {
        aAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                removePageView(aPage)
            }
        })
        val pageView = aPage.getPageView()
        pageView?.startAnimation(aAnimation)
    }

    private fun animatePopViewController(
        aRemovePage: ASViewController?,
        aAddPage: ASViewController?, 
        animated: Boolean
    ) {
        aRemovePage?.let { removePage ->
            removePage.notifyPageWillDisappear()
            if (animated) {
                removePageView(removePage, ASAnimation.getFadeOutToRightAnimation())
            } else {
                removePageView(removePage)
            }
        }

        aAddPage?.let { addPage ->
            buildPageView(addPage)
            addPage.onPageDidLoad()
            addPage.onPageRefresh()
            addPage.notifyPageWillAppear()
        }

        object : ASNavigationControllerPopAnimation(aRemovePage, aAddPage) {
            override fun onAnimationFinished() {
                aRemovePage?.onPageDidDisappear()

                val removeList = Vector<View>()
                val contentView = _root_view?.getContentView()
                contentView?.let { cv ->
                    for (i in 0 until cv.childCount) {
                        val pageView = cv.getChildAt(i)
                        if (pageView != aAddPage?.getPageView()) {
                            removeList.add(pageView)
                        }
                    }
                }

                for (view in removeList) {
                    _root_view?.getContentView()?.removeView(view)
                }

                aRemovePage?.let { cleanPageView(it) }

                for (controller in _controllers) {
                    if (controller != aAddPage && controller.getPageView() != null) {
                        cleanPageView(controller)
                    }
                }

                aAddPage?.notifyPageDidAppear()
                onPageCommandExecuteFinished()
            }
        }.start(animated)
    }

    private fun animatedPushViewController(
        sourceController: ASViewController?,
        targetController: ASViewController?,
        animated: Boolean
    ) {
        targetController?.let { target ->
            buildPageView(target)
            target.onPageDidLoad()
            target.onPageRefresh()
        }

        sourceController?.notifyPageWillDisappear()
        targetController?.notifyPageWillAppear()

        object : ASNavigationControllerPushAnimation(sourceController, targetController) {
            override fun onAnimationFinished() {
                sourceController?.onPageDidDisappear()

                targetController?.let { target ->
                    val removeList = Vector<View>()
                    val contentView = _root_view?.getContentView()
                    contentView?.let { cv ->
                        for (i in 0 until cv.childCount) {
                            val pageView = cv.getChildAt(i)
                            if (pageView != target.getPageView()) {
                                removeList.add(pageView)
                            }
                        }
                    }

                    for (view in removeList) {
                        _root_view?.getContentView()?.removeView(view)
                    }
                }

                for (controller in _controllers) {
                    if (controller != targetController && controller.getPageView() != null) {
                        cleanPageView(controller)
                    }
                }

                targetController?.notifyPageDidAppear()
                onPageCommandExecuteFinished()
            }
        }.start(animated)
    }

    fun pushViewController(aController: ASViewController) {
        pushViewController(aController, _animation_enable)
    }

    fun pushViewController(aController: ASViewController, animated: Boolean) {
        val command = object : PageCommand() {
            override fun run() {
                if (_temp_controllers.size <= 0 || aController != _temp_controllers.lastElement()) {
                    _temp_controllers.add(aController)
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    fun popViewController() {
        popViewController(_animation_enable)
    }

    fun popViewController(animated: Boolean) {
        val command = object : PageCommand() {
            override fun run() {
                if (_temp_controllers.size > 0) {
                    _temp_controllers.removeAt(_temp_controllers.size - 1)
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    fun popToViewController(aController: ASViewController) {
        popToViewController(aController, _animation_enable)
    }

    fun popToViewController(aController: ASViewController, animated: Boolean) {
        val command = object : PageCommand() {
            override fun run() {
                if (_temp_controllers.size <= 0 || aController != _temp_controllers.lastElement()) {
                    while (_temp_controllers.size > 0 && _temp_controllers.lastElement() != aController) {
                        _temp_controllers.removeAt(_temp_controllers.size - 1)
                    }
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    fun setViewControllers(aControllerList: Vector<ASViewController>) {
        setViewControllers(aControllerList, _animation_enable)
    }

    fun setViewControllers(aControllerList: Vector<ASViewController>, animated: Boolean) {
        val command = object : PageCommand() {
            override fun run() {
                _temp_controllers.removeAllElements()
                _temp_controllers.addAll(aControllerList)
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    fun exchangeViewControllers(animated: Boolean) {
        object : ASRunner() {
            override fun run() {
                val sourceController = if (_controllers.size > 0) _controllers.lastElement() else null
                val targetController = if (_temp_controllers.size > 0) _temp_controllers.lastElement() else null
                val pop = _controllers.contains(targetController)

                for (controller in _controllers) {
                    controller.prepareForRemove()
                }
                for (controller in _temp_controllers) {
                    controller.prepareForAdd()
                }
                for (controller in _controllers) {
                    if (controller.isMarkedRemoved()) {
                        _remove_list.add(controller)
                    }
                    controller.cleanMark()
                }
                for (controller in _temp_controllers) {
                    if (controller.isMarkedAdded()) {
                        controller.setNavigationController(this@ASNavigationController)
                        _add_list.add(controller)
                    }
                    controller.cleanMark()
                }

                _controllers.removeAllElements()
                _controllers.addAll(_temp_controllers)

                for (controller in _add_list) {
                    controller.notifyPageDidAddToNavigationController()
                }
                for (controller in _remove_list) {
                    controller.notifyPageDidRemoveFromNavigationController()
                }

                _add_list.clear()
                _remove_list.clear()

                when {
                    sourceController == targetController -> onPageCommandExecuteFinished()
                    pop -> animatePopViewController(sourceController, targetController, animated)
                    else -> animatedPushViewController(sourceController, targetController, animated)
                }

                // 檢查顯示訊息小視窗
                checkMessageFloatingShow()
            }
        }.runInMainThread()
    }

    fun getViewControllers(): Vector<ASViewController> = Vector(_controllers)

    fun containsViewController(aController: ASViewController): Boolean = _controllers.contains(aController)

    protected open fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        getTopController()?.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)
    }

    fun reloadLayout() {
        _root_view?.requestLayout()
    }

    fun onReceivedGestureUp(): Boolean = getTopController()?.onReceivedGestureUp() ?: false

    fun onReceivedGestureDown(): Boolean = getTopController()?.onReceivedGestureDown() ?: false

    fun onReceivedGestureLeft(): Boolean = getTopController()?.onReceivedGestureLeft() ?: false

    fun onReceivedGestureRight(): Boolean = getTopController()?.onReceivedGestureRight() ?: false

    fun getCurrentOrientation(): Int {
        val rotation = windowManager.defaultDisplay.rotation
        return if (rotation != 1 && rotation != 3) 1 else 2
    }

    fun getScreenWidth(): Int = _display_metrics.widthPixels

    fun getScreenHeight(): Int = _display_metrics.heightPixels

    fun getDeviceController(): ASDeviceController? = _device_controller

    override fun finish() {
        onControllerWillFinish()
        // 改善設備控制器的清理
        _device_controller?.cleanup()
        super.finish()
    }

    fun isAnimationEnable(): Boolean = _animation_enable

    fun setAnimationEnable(enable: Boolean) {
        _animation_enable = enable
    }

    override fun onPause() {
        super.onPause()
        _in_background = true
        // 當應用進入背景時，確保連線保持
        // 這裡不釋放 WiFi 和 CPU lock，讓 telnet 連線保持
    }

    override fun onResume() {
        super.onResume()
        _in_background = false
        // 當應用恢復前景時，檢查網路狀態
        _device_controller?.let { deviceController ->
            val networkType = deviceController.isNetworkAvailable()
            if (networkType == -1) {
                // 網路已斷開，可能需要重連
                println("Network disconnected while in background")
            }
        }
    }

    fun isInBackground(): Boolean = _in_background

    fun printControllers() {
        printControllers(_controllers)
    }

    fun printControllers(controllers: Vector<ASViewController>) {
        for (controller in controllers) {
            print("${controller.getPageType()} ")
        }
        print("\n")
    }

    override fun onLowMemory() {
        println("on low memory")
        super.onLowMemory()
    }

    fun pushPageCommand(aCommand: PageCommand) {
        synchronized(_page_commands) {
            _page_commands.add(aCommand)
        }
        executePageCommand()
    }

    fun executePageCommand() {
        synchronized(this) {
            if (!_is_animating) {
                synchronized(_page_commands) {
                    if (_page_commands.size > 0) {
                        synchronized(this) {
                            _is_animating = true
                        }
                        _temp_controllers.addAll(_controllers)
                        val animated = _page_commands.firstElement().animated
                        while (_page_commands.size > 0 && _page_commands.firstElement().animated == animated) {
                            _page_commands.removeAt(0).run()
                        }
                        exchangeViewControllers(animated)
                    }
                }
            }
        }
    }

    private fun onPageCommandExecuteFinished() {
        synchronized(this) {
            _is_animating = false
        }
        _temp_controllers.clear()
        executePageCommand()
    }

    /** 加入畫面上永遠存在的View */
    fun addForeverView(view: View) {
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addContentView(view, layoutParams)
    }

    /** 移除畫面上永遠存在的View */
    fun removeForeverView(view: View) {
        val parentViewGroup = view.parent as? ViewGroup
        parentViewGroup?.removeView(view)
    }

    /** 根據最上層頁面決定是否顯示訊息小視窗 */
    private fun checkMessageFloatingShow() {
        // 如果是PopupPage, 隱藏訊息
        val topPage = getTopController() as? TelnetPage
        topPage?.let { page ->
            if (page.isPopupPage()) {
                // 已經顯示就隱藏
                TempSettings.getMessageSmall()?.let { messageSmall ->
                    if (messageSmall.visibility == View.VISIBLE) {
                        messageSmall.hide()
                    }
                }
            } else {
                // 已經隱藏則看設定決定顯示
                if (NotificationSettings.getShowMessageFloating()) {
                    TempSettings.getMessageSmall()?.show()
                }
            }
        }
    }

    /**
     * 設定 WindowInsets 處理以支援 edge-to-edge
     */
    private fun setupWindowInsets() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            _root_view?.setOnApplyWindowInsetsListener { v, windowInsets ->
                val windowInsetsCompat = androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat(windowInsets, v)

                // 獲取系統欄的insets
                val systemBars = windowInsetsCompat.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())

                // 獲取軟鍵盤的insets
                val imeInsets = windowInsetsCompat.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())

                // 計算總的底部間距（系統欄 + 軟鍵盤）
                val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)

                // 更新 ASWindowStateHandler 中的狀態
                ASWindowStateHandler.updateWindowInsets(
                    systemBars.top, bottomPadding,
                    systemBars.left, systemBars.right
                )

                // 設定內容區域的 padding 以避免與系統欄和軟鍵盤重疊
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)

                windowInsets
            }
        }
    }
}
