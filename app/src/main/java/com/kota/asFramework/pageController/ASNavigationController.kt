package com.kota.asFramework.pageController

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.kota.Bahamut.pages.messages.MessageSmall
import com.kota.Bahamut.service.NotificationSettings.getShowMessageFloating
import com.kota.Bahamut.service.NotificationSettings.upgrade
import com.kota.Bahamut.service.TempSettings.getMessageSmall
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.thread.ASRunner.Companion.construct
import com.kota.telnetUI.TelnetPage
import java.util.Vector
import kotlin.math.max

open class ASNavigationController : Activity() {
    var deviceController: ASDeviceController? = null
        private set
    private val displayMetrics = DisplayMetrics()
    private var rootView: ASNavigationControllerView? = null
    private val controllers = Vector<ASViewController>()
    private val tempControllers = Vector<ASViewController>()
    private val removeList = Vector<ASViewController>()
    private val addList = Vector<ASViewController>()
    open var isAnimationEnable: Boolean = true
    var isInBackground: Boolean = false
        private set
    private val pageCommands: Vector<PageCommand?> = Vector<PageCommand?>()
    private var isAnimating = false

    abstract class PageCommand {
        var animated: Boolean = true

        abstract fun run()
    }

    protected open fun onControllerWillLoad() {
    }

    protected open fun onControllerDidLoad() {
    }

    protected fun onControllerWillFinish() {
    }

    fun setNavigationTitle(title: String?) {
        super.setTitle(title)
    }

    protected open val controllerName: String?
        get() = ""

    private fun onMenuPressed(): Boolean {
        val page = this.topController
        return !(page == null || !page.onMenuButtonClicked())
    }

    private fun onSearchPressed(): Boolean {
        val page = this.topController
        return !(page == null || !page.onSearchButtonClicked())
    }

    // android.app.Activity
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        var handleBackButton = false
        val page = this.topController
        if (page != null && page.onBackPressed()) {
            handleBackButton = true
        }
        if (!handleBackButton) {
            finish()
        }
    }

    open fun onBackLongPressed(): Boolean {
        return false
    }

    // android.app.Activity
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 啟用 edge-to-edge 支援
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.setDecorFitsSystemWindows(false)
        }
        // 對於 API 35+ (VANILLA_ICE_CREAM)，行為預設為 false 且無法更改

        setNavigationController(this)

        // 獲取顯示器指標
        initializeDisplayMetrics()
        construct()

        UserSettings(this)
        upgrade(this)

        this.deviceController = ASDeviceController(this)
        onControllerWillLoad()

        this.rootView = ASNavigationControllerView(this)
        this.rootView!!.setPageController(this)
        setContentView(this.rootView)


        // 設定 WindowInsets 處理
        setupWindowInsets()

        onControllerDidLoad()
    }

    // android.app.Activity, android.view.KeyEvent.Callback
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            return onBackLongPressed()
        }
        return super.onKeyLongPress(keyCode, event)
    }

    // android.app.Activity, android.view.KeyEvent.Callback
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            82 -> onMenuPressed()
            83 -> super.onKeyUp(keyCode, event)
            84 -> onSearchPressed()
            else -> super.onKeyUp(keyCode, event)
        }
    }

    val topController: ASViewController?
        get() {
            var controller: ASViewController? = null
            synchronized(this.controllers) {
                if (this.controllers.isNotEmpty()) {
                    controller = this.controllers.lastElement()
                }
            }
            return controller
        }
    val allController: Vector<ASViewController>
        get() {
            var controllers =
                Vector<ASViewController>()
            synchronized(this.controllers) {
                controllers = this.controllers
            }
            return controllers
        }

    private fun buildPageView(controller: ASViewController) {
        val pageView = ASPageView(this)
        pageView.layoutParams = FrameLayout.LayoutParams(-1, -1)
        pageView.setBackgroundColor(View.MEASURED_STATE_MASK)
        layoutInflater.inflate(controller.pageLayout, pageView)
        controller.pageView = pageView
        this.rootView!!.contentView?.addView(pageView)
    }

    private fun cleanPageView(controller: ASViewController) {
        controller.pageView = null
    }

    fun addPageView(aPage: ASViewController) {
        val pageView: ASPageView? = aPage.pageView
        this.rootView!!.post {
            aPage.onPageDidDisappear()
            this@ASNavigationController.rootView!!.removeView(pageView)
        }
    }

    fun removePageView(aPage: ASViewController) {
        val pageView: ASPageView? = aPage.pageView
        this.rootView!!.post {
            aPage.onPageDidDisappear()
            this@ASNavigationController.rootView!!.removeView(pageView)
        }
    }

    fun removePageView(aPage: ASViewController, aAnimation: Animation) {
        aAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                this@ASNavigationController.removePageView(aPage)
            }
        })
        val pageView: ASPageView? = aPage.pageView
        pageView?.startAnimation(aAnimation)
    }

    private fun animatePopViewController(
        aRemovePage: ASViewController?,
        aAddPage: ASViewController?,
        animated: Boolean
    ) {
        if (aRemovePage != null) {
            aRemovePage.notifyPageWillDisappear()
            if (animated) {
                removePageView(aRemovePage, ASAnimation.fadeOutToRightAnimation)
            } else {
                removePageView(aRemovePage)
            }
        }
        if (aAddPage != null) {
            buildPageView(aAddPage)
            aAddPage.onPageDidLoad()
            aAddPage.onPageRefresh()
            aAddPage.notifyPageWillAppear()
        }
        object : ASNavigationControllerPopAnimation(aRemovePage, aAddPage) {
            // from class: com.kota.ASFramework.PageController.ASNavigationController.4
            // com.kota.ASFramework.PageController.ASNavigationControllerPopAnimation
            override fun onAnimationFinished() {
                aRemovePage?.onPageDidDisappear()
                val removeList = Vector<View?>()
                val childCount = this@ASNavigationController.rootView!!.contentView?.childCount ?: 0
                for (i in 0..<childCount) {
                    val pageView =
                        this@ASNavigationController.rootView!!.contentView?.getChildAt(i)
                    if (pageView !== aAddPage!!.pageView) {
                        removeList.add(pageView)
                    }
                }
                for (view in removeList) {
                    this@ASNavigationController.rootView!!.contentView?.removeView(view)
                }
                this@ASNavigationController.cleanPageView(aRemovePage!!)
                for (controller in this@ASNavigationController.controllers) {
                    if (controller !== aAddPage && controller.pageView != null) {
                        this@ASNavigationController.cleanPageView(controller)
                    }
                }
                aAddPage?.notifyPageDidAppear()
                this@ASNavigationController.onPageCommandExecuteFinished()
            }
        }.start(animated)
    }

    private fun animatedPushViewController(
        sourceController: ASViewController?,
        targetController: ASViewController?,
        animated: Boolean
    ) {
        if (targetController != null) {
            buildPageView(targetController)
            targetController.onPageDidLoad()
            targetController.onPageRefresh()
        }
        sourceController?.notifyPageWillDisappear()
        targetController?.notifyPageWillAppear()
        object : ASNavigationControllerPushAnimation(sourceController, targetController) {
            override fun onAnimationFinished() {
                sourceController?.onPageDidDisappear()
                if (targetController != null) {
                    val removeList = Vector<View?>()
                    val childCount = this@ASNavigationController.rootView!!.contentView?.childCount ?: 0
                    for (i in 0..<childCount) {
                        val pageView =
                            this@ASNavigationController.rootView!!.contentView?.getChildAt(i)
                        if (pageView !== targetController.pageView) {
                            removeList.add(pageView)
                        }
                    }
                    for (view in removeList) {
                        this@ASNavigationController.rootView!!.contentView?.removeView(view)
                    }
                }
                for (controller in this@ASNavigationController.controllers) {
                    if (controller !== targetController && controller.pageView != null) {
                        this@ASNavigationController.cleanPageView(controller)
                    }
                }
                targetController?.notifyPageDidAppear()
                this@ASNavigationController.onPageCommandExecuteFinished()
            }
        }.start(animated)
    }

    @JvmOverloads
    fun pushViewController(
        aController: ASViewController?,
        animated: Boolean = this.isAnimationEnable
    ) {
        val command: PageCommand = object : PageCommand() {
            override fun run() {
                if (aController != null) {
                    if (this@ASNavigationController.tempControllers.isEmpty() || aController !== this@ASNavigationController.tempControllers.lastElement()) {
                        this@ASNavigationController.tempControllers.add(aController)
                    }
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    @JvmOverloads
    fun popViewController(animated: Boolean = this.isAnimationEnable) {
        val command: PageCommand = object : PageCommand() {
            override fun run() {
                if (this@ASNavigationController.tempControllers.isNotEmpty()) {
                    this@ASNavigationController.tempControllers.removeAt(this@ASNavigationController.tempControllers.size - 1)
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    @JvmOverloads
    fun popToViewController(
        aController: ASViewController?,
        animated: Boolean = this.isAnimationEnable
    ) {
        val command: PageCommand = object : PageCommand() {
            override fun run() {
                if (aController != null) {
                    if (this@ASNavigationController.tempControllers.isEmpty() || aController !== this@ASNavigationController.tempControllers.lastElement()) {
                        while (this@ASNavigationController.tempControllers.isNotEmpty() && this@ASNavigationController.tempControllers.lastElement() !== aController) {
                            this@ASNavigationController.tempControllers.removeAt(this@ASNavigationController.tempControllers.size - 1)
                        }
                    }
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    fun setViewControllers(aControllerList: Vector<ASViewController>?) {
        setViewControllers(aControllerList, this.isAnimationEnable)
    }

    fun setViewControllers(aControllerList: Vector<ASViewController>?, animated: Boolean) {
        val command: PageCommand = object : PageCommand() {
            override fun run() {
                if (aControllerList != null) {
                    this@ASNavigationController.tempControllers.removeAllElements()
                    this@ASNavigationController.tempControllers.addAll(aControllerList)
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    fun exchangeViewControllers(animated: Boolean) {
        object : ASRunner() {
            override fun run() {
                val sourceController =
                    if (this@ASNavigationController.controllers.isNotEmpty()) this@ASNavigationController.controllers.lastElement() else null
                val targetController =
                    if (this@ASNavigationController.tempControllers.isNotEmpty()) this@ASNavigationController.tempControllers.lastElement() else null
                val pop = this@ASNavigationController.controllers.contains(targetController)
                for (controller in this@ASNavigationController.controllers) {
                    controller.prepareForRemove()
                }
                for (controller2 in this@ASNavigationController.tempControllers) {
                    controller2.prepareForAdd()
                }
                for (controller3 in this@ASNavigationController.controllers) {
                    if (controller3.isMarkedRemoved) {
                        this@ASNavigationController.removeList.add(controller3)
                    }
                    controller3.cleanMark()
                }
                for (controller4 in this@ASNavigationController.tempControllers) {
                    if (controller4.isMarkedAdded) {
                        controller4.navigationController = this@ASNavigationController
                        this@ASNavigationController.addList.add(controller4)
                    }
                    controller4.cleanMark()
                }
                this@ASNavigationController.controllers.removeAllElements()
                this@ASNavigationController.controllers.addAll(this@ASNavigationController.tempControllers)
                for (controller5 in this@ASNavigationController.addList) {
                    controller5.notifyPageDidAddToNavigationController()
                }
                for (controller6 in this@ASNavigationController.removeList) {
                    controller6.notifyPageDidRemoveFromNavigationController()
                }
                this@ASNavigationController.addList.clear()
                this@ASNavigationController.removeList.clear()
                if (sourceController === targetController) {
                    this@ASNavigationController.onPageCommandExecuteFinished()
                } else if (pop) {
                    this@ASNavigationController.animatePopViewController(
                        sourceController,
                        targetController,
                        animated
                    )
                } else {
                    this@ASNavigationController.animatedPushViewController(
                        sourceController,
                        targetController,
                        animated
                    )
                }
                // 檢查顯示訊息小視窗
                checkMessageFloatingShow()
            }
        }.runInMainThread()
    }

    val viewControllers: Vector<ASViewController?>
        get() = Vector<ASViewController?>(this.controllers)

    fun containsViewController(aController: ASViewController?): Boolean {
        return this.controllers.contains(aController)
    }

    fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        val page = this.topController
        page?.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)
    }

    fun reloadLayout() {
        if (this.rootView != null) {
            this.rootView!!.requestLayout()
        }
    }

    fun onReceivedGestureUp(): Boolean {
        val page = this.topController
        if (page != null) {
            return page.onReceivedGestureUp()
        }
        return false
    }

    fun onReceivedGestureDown(): Boolean {
        val page = this.topController
        if (page != null) {
            return page.onReceivedGestureDown()
        }
        return false
    }

    fun onReceivedGestureLeft(): Boolean {
        val page = this.topController
        if (page != null) {
            return page.onReceivedGestureLeft()
        }
        return false
    }

    fun onReceivedGestureRight(): Boolean {
        val page = this.topController
        if (page != null) {
            return page.onReceivedGestureRight()
        }
        return false
    }

    val currentOrientation: Int
        get() {
            val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display?.rotation ?: 0
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.rotation
            }
            if (rotation != 1 && rotation != 3) {
                return 1
            }
            return 2
        }

    val screenWidth: Int
        get() = this.displayMetrics.widthPixels

    val screenHeight: Int
        get() = this.displayMetrics.heightPixels

    // android.app.Activity
    override fun finish() {
        onControllerWillFinish()
        // 改善設備控制器的清理
        if (this.deviceController != null) {
            deviceController!!.cleanup()
        }
        super.finish()
    }

    // android.app.Activity
    override fun onPause() {
        super.onPause()
        this.isInBackground = true
        // 當應用進入背景時，確保連線保持
        // 這裡不釋放 WiFi 和 CPU lock，讓 telnet 連線保持
    }

    // android.app.Activity
    override fun onResume() {
        super.onResume()
        this.isInBackground = false
        // 當應用恢復前景時，檢查網路狀態
        if (this.deviceController != null) {
            val networkType = deviceController!!.isNetworkAvailable
            if (networkType == -1) {
                // 網路已斷開，可能需要重連
                println("Network disconnected while in background")
            }
        }
    }

    @JvmOverloads
    fun printControllers(controllers: Vector<ASViewController> = this.controllers) {
        for (controller in controllers) {
            print(controller.pageType.toString() + " ")
        }
        print("\n")
    }

    override fun onLowMemory() {
        println("on low memory")
        super.onLowMemory()
    }

    fun pushPageCommand(aCommand: PageCommand?) {
        synchronized(this.pageCommands) {
            this.pageCommands.add(aCommand)
        }
        executePageCommand()
    }

    fun executePageCommand() {
        synchronized(this) {
            if (!this.isAnimating) {
                synchronized(this.pageCommands) {
                    if (this.pageCommands.isNotEmpty()) {
                        synchronized(this) {
                            this.isAnimating = true
                        }
                        this.tempControllers.addAll(this.controllers)
                        val animated = this.pageCommands.firstElement()!!.animated
                        while (this.pageCommands.isNotEmpty() && this.pageCommands.firstElement()!!.animated == animated) {
                            this.pageCommands.removeAt(0)!!.run()
                        }
                        exchangeViewControllers(animated)
                    }
                }
            }
        }
    }

    private fun onPageCommandExecuteFinished() {
        synchronized(this) {
            this.isAnimating = false
        }
        this.tempControllers.clear()
        executePageCommand()
    }

    /** 加入畫面上永遠存在的View  */
    fun addForeverView(view: View?) {
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addContentView(view, layoutParams)
    }

    /** 移除畫面上永遠存在的View  */
    fun removeForeverView(messageSmall: MessageSmall?) {
        val parentViewGroup = messageSmall?.parent as ViewGroup?
        parentViewGroup?.removeView(messageSmall)
    }

    /** 根據最上層頁面決定是否顯示訊息小視窗  */
    private fun checkMessageFloatingShow() {
        // 如果是PopupPage, 隱藏訊息
        val topPage = this.topController as TelnetPage?
        if (topPage != null) if (topPage.isPopupPage) {
            // 已經顯示就隱藏
            if (getMessageSmall() != null) {
                val messageSmall = getMessageSmall()
                if (messageSmall!!.isVisible) {
                    messageSmall.hide()
                }
            }
        } else {
            // 已經隱藏則看設定決定顯示
            if (getShowMessageFloating()) {
                if (getMessageSmall() != null) {
                    val messageSmall = getMessageSmall()
                    messageSmall!!.show()
                }
            }
        }
    }

    /**
     * 初始化顯示器指標，支援新舊 API
     */
    private fun initializeDisplayMetrics() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ 使用 WindowMetrics
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            displayMetrics.apply {
                widthPixels = bounds.width()
                heightPixels = bounds.height()
                density = resources.displayMetrics.density
                densityDpi = resources.displayMetrics.densityDpi
            }
        } else {
            // API 29 及以下使用傳統方法
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
    }

    /**
     * 設定 WindowInsets 處理以支援 edge-to-edge
     */
    private fun setupWindowInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.rootView!!.setOnApplyWindowInsetsListener { v: View?, windowInsets: WindowInsets? ->
                val windowInsetsCompat =
                    WindowInsetsCompat.toWindowInsetsCompat(windowInsets!!, v)
                // 獲取系統欄的insets
                val systemBars = windowInsetsCompat
                    .getInsets(WindowInsetsCompat.Type.systemBars())


                // 獲取軟鍵盤的insets
                val imeInsets = windowInsetsCompat
                    .getInsets(WindowInsetsCompat.Type.ime())


                // 計算總的底部間距（系統欄 + 軟鍵盤）
                val bottomPadding = max(systemBars.bottom, imeInsets.bottom)


                // 更新 ASWindowStateHandler 中的狀態
                ASWindowStateHandler.updateWindowInsets(
                    systemBars.top, bottomPadding,
                    systemBars.left, systemBars.right
                )


                // 設定內容區域的 padding 以避免與系統欄和軟鍵盤重疊
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
                windowInsets
            }
        }
    }

    companion object {
        var currentController: ASNavigationController? = null
            private set

        private fun setNavigationController(aController: ASNavigationController?) {
            currentController = aController
        }
    }
}
