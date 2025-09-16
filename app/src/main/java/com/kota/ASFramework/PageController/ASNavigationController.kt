package com.kota.ASFramework.PageController

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
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.Thread.ASRunner.Companion.construct
import com.kota.Bahamut.Service.NotificationSettings.getShowMessageFloating
import com.kota.Bahamut.Service.NotificationSettings.upgrade
import com.kota.Bahamut.Service.TempSettings.getMessageSmall
import com.kota.Bahamut.Service.UserSettings
import com.kota.TelnetUI.TelnetPage
import java.util.Vector
import kotlin.math.max

/* loaded from: classes.dex */
open class ASNavigationController : Activity() {
    var deviceController: ASDeviceController? = null
        private set
    private val _display_metrics = DisplayMetrics()
    private var _root_view: ASNavigationControllerView? = null
    private val _controllers = Vector<ASViewController>()
    private val _temp_controllers = Vector<ASViewController>()
    private val _remove_list = Vector<ASViewController>()
    private val _add_list = Vector<ASViewController>()
    open var isAnimationEnable: Boolean = true
    var isInBackground: Boolean = false
        private set
    private val _page_commands: Vector<PageCommand?> = Vector<PageCommand?>()
    private var _is_animating = false

    /* loaded from: classes.dex */
    private abstract class PageCommand {
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
        if (page == null || !page.onMenuButtonClicked()) {
            return false
        }
        return true
    }

    private fun onSearchPressed(): Boolean {
        val page = this.topController
        if (page == null || !page.onSearchButtonClicked()) {
            return false
        }
        return true
    }

    // android.app.Activity
    override fun onBackPressed() {
        var handle_back_button = false
        val page = this.topController
        if (page != null && page.onBackPressed()) {
            handle_back_button = true
        }
        if (!handle_back_button) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false)
        }

        setNavigationController(this)

        getWindowManager().getDefaultDisplay().getMetrics(this._display_metrics)
        construct()

        UserSettings(this)
        upgrade(this)

        this.deviceController = ASDeviceController(this)
        onControllerWillLoad()

        this._root_view = ASNavigationControllerView(this)
        this._root_view!!.setPageController(this)
        setContentView(this._root_view)


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
        when (keyCode) {
            82 -> return onMenuPressed()
            83 -> return super.onKeyUp(keyCode, event)
            84 -> return onSearchPressed()
            else -> return super.onKeyUp(keyCode, event)
        }
    }

    val topController: ASViewController?
        get() {
            var controller: ASViewController? = null
            synchronized(this._controllers) {
                if (this._controllers.size > 0) {
                    controller = this._controllers.lastElement()
                }
            }
            return controller
        }
    val allController: Vector<ASViewController>
        get() {
            var controllers =
                Vector<ASViewController>()
            synchronized(this._controllers) {
                controllers = this._controllers
            }
            return controllers
        }

    private fun buildPageView(controller: ASViewController) {
        val page_view = ASPageView(this)
        page_view.setLayoutParams(FrameLayout.LayoutParams(-1, -1))
        page_view.setBackgroundColor(View.MEASURED_STATE_MASK)
        getLayoutInflater().inflate(controller.getPageLayout(), page_view)
        controller.setPageView(page_view)
        this._root_view!!.getContentView().addView(page_view)
    }

    private fun cleanPageView(controller: ASViewController) {
        controller.setPageView(null)
    }

    fun addPageView(aPage: ASViewController) {
        val page_view: View = aPage.getPageView()
        this._root_view!!.post(object : Runnable {
            // from class: com.kota.ASFramework.PageController.ASNavigationController.1
            // java.lang.Runnable
            override fun run() {
                aPage.onPageDidDisappear()
                this@ASNavigationController._root_view!!.removeView(page_view)
            }
        })
    }

    fun removePageView(aPage: ASViewController) {
        val page_view: View = aPage.getPageView()
        this._root_view!!.post(object : Runnable {
            // from class: com.kota.ASFramework.PageController.ASNavigationController.2
            // java.lang.Runnable
            override fun run() {
                aPage.onPageDidDisappear()
                this@ASNavigationController._root_view!!.removeView(page_view)
            }
        })
    }

    fun removePageView(aPage: ASViewController, aAnimation: Animation) {
        aAnimation.setAnimationListener(object : Animation.AnimationListener {
            // from class: com.kota.ASFramework.PageController.ASNavigationController.3
            // android.view.animation.Animation.AnimationListener
            override fun onAnimationStart(animation: Animation?) {
            }

            // android.view.animation.Animation.AnimationListener
            override fun onAnimationRepeat(animation: Animation?) {
            }

            // android.view.animation.Animation.AnimationListener
            override fun onAnimationEnd(animation: Animation?) {
                this@ASNavigationController.removePageView(aPage)
            }
        })
        val page_view: View = aPage.getPageView()
        page_view.startAnimation(aAnimation)
    }

    private fun animatePopViewController(
        aRemovePage: ASViewController?,
        aAddPage: ASViewController?,
        animated: Boolean
    ) {
        if (aRemovePage != null) {
            aRemovePage.notifyPageWillDisappear()
            if (animated) {
                removePageView(aRemovePage, ASAnimation.getFadeOutToRightAnimation())
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
                if (aRemovePage != null) {
                    aRemovePage.onPageDidDisappear()
                }
                val remove_list = Vector<View?>()
                for (i in 0..<this@ASNavigationController._root_view!!.getContentView()
                    .getChildCount()) {
                    val page_view =
                        this@ASNavigationController._root_view!!.getContentView().getChildAt(i)
                    if (page_view !== aAddPage!!.getPageView()) {
                        remove_list.add(page_view)
                    }
                }
                for (view in remove_list) {
                    this@ASNavigationController._root_view!!.getContentView().removeView(view)
                }
                this@ASNavigationController.cleanPageView(aRemovePage!!)
                for (controller in this@ASNavigationController._controllers) {
                    if (controller !== aAddPage && controller.getPageView() != null) {
                        this@ASNavigationController.cleanPageView(controller)
                    }
                }
                if (aAddPage != null) {
                    aAddPage.notifyPageDidAppear()
                }
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
        if (sourceController != null) {
            sourceController.notifyPageWillDisappear()
        }
        if (targetController != null) {
            targetController.notifyPageWillAppear()
        }
        object : ASNavigationControllerPushAnimation(sourceController, targetController) {
            // from class: com.kota.ASFramework.PageController.ASNavigationController.5
            // com.kota.ASFramework.PageController.ASNavigationControllerPushAnimation
            override fun onAnimationFinished() {
                if (sourceController != null) {
                    sourceController.onPageDidDisappear()
                }
                if (targetController != null) {
                    val remove_list = Vector<View?>()
                    for (i in 0..<this@ASNavigationController._root_view!!.getContentView()
                        .getChildCount()) {
                        val page_view =
                            this@ASNavigationController._root_view!!.getContentView().getChildAt(i)
                        if (page_view !== targetController.getPageView()) {
                            remove_list.add(page_view)
                        }
                    }
                    for (view in remove_list) {
                        this@ASNavigationController._root_view!!.getContentView().removeView(view)
                    }
                }
                for (controller in this@ASNavigationController._controllers) {
                    if (controller !== targetController && controller.getPageView() != null) {
                        this@ASNavigationController.cleanPageView(controller)
                    }
                }
                if (targetController != null) {
                    targetController.notifyPageDidAppear()
                }
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
            // from class: com.kota.ASFramework.PageController.ASNavigationController.6
            // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
            override fun run() {
                if (aController != null) {
                    if (this@ASNavigationController._temp_controllers.size <= 0 || aController !== this@ASNavigationController._temp_controllers.lastElement()) {
                        this@ASNavigationController._temp_controllers.add(aController)
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
            // from class: com.kota.ASFramework.PageController.ASNavigationController.7
            // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
            override fun run() {
                if (this@ASNavigationController._temp_controllers.size > 0) {
                    this@ASNavigationController._temp_controllers.removeAt(this@ASNavigationController._temp_controllers.size - 1)
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
            // from class: com.kota.ASFramework.PageController.ASNavigationController.8
            // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
            override fun run() {
                if (aController != null) {
                    if (this@ASNavigationController._temp_controllers.size <= 0 || aController !== this@ASNavigationController._temp_controllers.lastElement()) {
                        while (this@ASNavigationController._temp_controllers.size > 0 && this@ASNavigationController._temp_controllers.lastElement() !== aController) {
                            this@ASNavigationController._temp_controllers.removeAt(this@ASNavigationController._temp_controllers.size - 1)
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
            // from class: com.kota.ASFramework.PageController.ASNavigationController.9
            // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
            override fun run() {
                if (aControllerList != null) {
                    this@ASNavigationController._temp_controllers.removeAllElements()
                    this@ASNavigationController._temp_controllers.addAll(aControllerList)
                }
            }
        }
        command.animated = animated
        pushPageCommand(command)
    }

    fun exchangeViewControllers(animated: Boolean) {
        object : ASRunner() {
            // from class: com.kota.ASFramework.PageController.ASNavigationController.10
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                val source_controller =
                    if (this@ASNavigationController._controllers.size > 0) this@ASNavigationController._controllers.lastElement() else null
                val target_controller =
                    if (this@ASNavigationController._temp_controllers.size > 0) this@ASNavigationController._temp_controllers.lastElement() else null
                val pop = this@ASNavigationController._controllers.contains(target_controller)
                for (controller in this@ASNavigationController._controllers) {
                    controller.prepareForRemove()
                }
                for (controller2 in this@ASNavigationController._temp_controllers) {
                    controller2.prepareForAdd()
                }
                for (controller3 in this@ASNavigationController._controllers) {
                    if (controller3.isMarkedRemoved()) {
                        this@ASNavigationController._remove_list.add(controller3)
                    }
                    controller3.cleanMark()
                }
                for (controller4 in this@ASNavigationController._temp_controllers) {
                    if (controller4.isMarkedAdded()) {
                        controller4.setNavigationController(this@ASNavigationController)
                        this@ASNavigationController._add_list.add(controller4)
                    }
                    controller4.cleanMark()
                }
                this@ASNavigationController._controllers.removeAllElements()
                this@ASNavigationController._controllers.addAll(this@ASNavigationController._temp_controllers)
                for (controller5 in this@ASNavigationController._add_list) {
                    controller5.notifyPageDidAddToNavigationController()
                }
                for (controller6 in this@ASNavigationController._remove_list) {
                    controller6.notifyPageDidRemoveFromNavigationController()
                }
                this@ASNavigationController._add_list.clear()
                this@ASNavigationController._remove_list.clear()
                if (source_controller === target_controller) {
                    this@ASNavigationController.onPageCommandExecuteFinished()
                } else if (pop) {
                    this@ASNavigationController.animatePopViewController(
                        source_controller,
                        target_controller,
                        animated
                    )
                } else {
                    this@ASNavigationController.animatedPushViewController(
                        source_controller,
                        target_controller,
                        animated
                    )
                }
                // 檢查顯示訊息小視窗
                checkMessageFloatingShow()
            }
        }.runInMainThread()
    }

    val viewControllers: Vector<ASViewController?>
        get() = Vector<ASViewController?>(this._controllers)

    fun containsViewController(aController: ASViewController?): Boolean {
        return this._controllers.contains(aController)
    }

    fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        val page = this.topController
        if (page != null) {
            page.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)
        }
    }

    fun reloadLayout() {
        if (this._root_view != null) {
            this._root_view!!.requestLayout()
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
            val rotation = getWindowManager().getDefaultDisplay().getRotation()
            if (rotation != 1 && rotation != 3) {
                return 1
            }
            return 2
        }

    val screenWidth: Int
        get() = this._display_metrics.widthPixels

    val screenHeight: Int
        get() = this._display_metrics.heightPixels

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
            val networkType = deviceController!!.isNetworkAvailable()
            if (networkType == -1) {
                // 網路已斷開，可能需要重連
                println("Network disconnected while in background")
            }
        }
    }

    @JvmOverloads
    fun printControllers(controllers: Vector<ASViewController> = this._controllers) {
        for (controller in controllers) {
            print(controller.getPageType().toString() + " ")
        }
        print("\n")
    }

    // android.app.Activity, android.content.ComponentCallbacks
    override fun onLowMemory() {
        println("on low memory")
        super.onLowMemory()
    }

    fun pushPageCommand(aCommand: PageCommand?) {
        synchronized(this._page_commands) {
            this._page_commands.add(aCommand)
        }
        executePageCommand()
    }

    fun executePageCommand() {
        synchronized(this) {
            if (!this._is_animating) {
                synchronized(this._page_commands) {
                    if (this._page_commands.size > 0) {
                        synchronized(this) {
                            this._is_animating = true
                        }
                        this._temp_controllers.addAll(this._controllers)
                        val animated = this._page_commands.firstElement()!!.animated
                        while (this._page_commands.size > 0 && this._page_commands.firstElement()!!.animated == animated) {
                            this._page_commands.removeAt(0)!!.run()
                        }
                        exchangeViewControllers(animated)
                    }
                }
            }
        }
    }

    private fun onPageCommandExecuteFinished() {
        synchronized(this) {
            this._is_animating = false
        }
        this._temp_controllers.clear()
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
    fun removeForeverView(view: View) {
        val parentViewGroup = view.getParent() as ViewGroup?
        if (parentViewGroup != null) parentViewGroup.removeView(view)
    }

    /** 根據最上層頁面決定是否顯示訊息小視窗  */
    private fun checkMessageFloatingShow() {
        // 如果是PopupPage, 隱藏訊息
        val top_page = this.topController as TelnetPage?
        if (top_page != null) if (top_page.isPopupPage) {
            // 已經顯示就隱藏
            if (getMessageSmall() != null) {
                val messageSmall = getMessageSmall()
                if (messageSmall!!.getVisibility() == View.VISIBLE) {
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
     * 設定 WindowInsets 處理以支援 edge-to-edge
     */
    private fun setupWindowInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this._root_view!!.setOnApplyWindowInsetsListener(View.OnApplyWindowInsetsListener { v: View?, windowInsets: WindowInsets? ->
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
            })
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
