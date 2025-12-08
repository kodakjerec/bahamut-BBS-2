package com.kota.Bahamut.listPage

import android.annotation.SuppressLint
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListAdapter
import android.widget.ListView
import com.kota.Bahamut.BuildConfig
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandLoadArticle
import com.kota.Bahamut.command.BahamutCommandLoadBlock
import com.kota.Bahamut.command.BahamutCommandLoadLastBlock
import com.kota.Bahamut.command.BahamutCommandMoveToLastBlock
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.telnet.logic.ItemUtils
import com.kota.telnetUI.TelnetPage
import java.util.Arrays
import java.util.Stack
import java.util.Vector

abstract class TelnetListPage : TelnetPage(), ListAdapter, OnItemClickListener,
    OnItemLongClickListener {
    // Short-term tracing control: enabled only for debug builds
    val TRACE_LOG_ENABLE: Boolean = try { BuildConfig.DEBUG } catch (_: Throwable) { false }

    fun traceCaller(): String {
        try {
            val st = Throwable().stackTrace
            for (f in st) {
                val cn = f.className
                if (!cn.startsWith("android.") && !cn.startsWith("java.") && !cn.startsWith("kotlin.")) {
                    return "${f.className}.${f.methodName}:${f.lineNumber}"
                }
            }
        } catch (_: Exception) {
            // ignore
        }
        return "unknown"
    }

    private val operationCommandStack = Vector<TelnetCommand>()
    private val loadCommandStack = Stack<TelnetCommand?>()
    private var executingCommand: TelnetCommand? = null
    private val pagePreloadCommand = BooleanArray(1)
    private val pageRefreshCommand = BooleanArray(2)
    open var listName: String = ""
    var listView: ListView? = null
    fun bindListView(aListView: ListView) {
        listView = aListView
        if (listView != null) {
            listView!!.onItemClickListener = this
            listView!!.onItemLongClickListener = this
            listView!!.adapter = this
        }
    }
    private var isListLoaded = false
    private var lastLoadTime: Long = 0
    private var lastSendTime: Long = 0
    var listCount: Int = 0 // 信件量
    var selectedIndex: Int = 0
        private set
    var currentBlock: Int = 0
        private set
    var lastLoadItemIndex: Int = 0
        private set
    var isInitialed = false
    private var isManualLoadPending = false

    @SuppressLint("UseSparseArrays")
    private val blockList: MutableMap<Int?, TelnetListPageBlock?> =
        HashMap()
    private val mDataSetObservable = DataSetObservable()

    // android.widget.Adapter
    abstract override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View?

    abstract val isAutoLoadEnable: Boolean

    abstract fun loadPage(): TelnetListPageBlock?

    /** 回收block */
    abstract fun recycleBlock(telnetListPageBlock: TelnetListPageBlock)

    /** 回收item */
    abstract fun recycleItem(telnetListPageItem: TelnetListPageItem)

    // android.widget.Adapter
    override fun registerDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.registerObserver(observer)
    }

    // android.widget.Adapter
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.unregisterObserver(observer)
    }

    /** 自動加載執行緒 */
    private var autoLoadJob: ASCoroutine? = null

    fun setManualLoadPage() {
        isManualLoadPending = true
    }

    // com.kota.telnetUI.TelnetPage, com.kota.asFramework.pageController.ASViewController
    override fun onPageDidUnload() {
        stopAutoLoad()
        autoLoadJob?.cancel() // 清理协程作用域
        super.onPageDidUnload()
    }

    override fun onPageDidRemoveFromNavigationController() {
        isInitialed = false
        cleanAllItem()
        stopAutoLoad()
    }

    /** android.widget.Adapter
     * 安全的在主執行緒中更新列表
     */
    fun safeNotifyDataSetChanged() {
        if (TRACE_LOG_ENABLE) {
            try {
                Log.i("TelnetListPageTrace", "time=${java.time.Instant.now()} thread=${Thread.currentThread().name} isMain=${ASCoroutine.isMainThread} caller=${traceCaller()} action=safeNotify listCount=${this.listCount} hasListView=${listView!=null}")
                if (ASCoroutine.isMainThread) {
                    Log.i("TelnetListPageTrace", "blockKeys=${blockList.keys}")
                }
            } catch (_: Exception) { /* ignore logging errors */ }
        }
        ASCoroutine.ensureMainThread {
            mDataSetObservable.notifyChanged()

            // 如果 ListView 還沒設定 adapter，則手動刷新視圖
            if (listView?.adapter == null) {
                listView?.invalidateViews()
            }
        }
    }

    val loadingItemNumber: Int
        get() = this.lastLoadItemIndex + 1

    fun isItemLoadingByNumber(number: Int): Boolean {
        return number == this.loadingItemNumber
    }

    open fun isItemCanLoadAtIndex(index: Int): Boolean {
        return true
    }

    fun loadItemAtNumber(number: Int) {
        loadItemAtIndex(number - 1)
    }

    open fun loadItemAtIndex(index: Int) {
        if (isItemCanLoadAtIndex(index)) {
            this.lastLoadItemIndex = index
            val command: TelnetCommand = BahamutCommandLoadArticle(index + 1)
            pushCommand(command)
        }
    }

    override fun onPageWillAppear() {
        loadListState()
        startAutoLoad()
    }

    override fun onPageWillDisappear() {
        stopAutoLoad()
    }

    override fun onPageDidDisappear() {
        saveListState()
    }

    fun pushRefreshCommand(aCommand: Int) {
        pageRefreshCommand[aCommand] = true
    }

    fun pushPreloadCommand(aCommand: Int) {
        pagePreloadCommand[aCommand] = true
    }

    override val pageType: Int
        get() = 0

    override fun clear() {
        cleanCommand()
        cleanAllItem()
        isListLoaded = false
        this.selectedIndex = 0
        this.currentBlock = 0
        this.listCount = 0
        lastLoadTime = 0L
        lastSendTime = 0L
        listName = ""
    }

    fun setListViewSelection(selection: Int) {
        ASCoroutine.ensureMainThread {
            if (this@TelnetListPage.listView != null) {
                if (selection == -1) {
                    this@TelnetListPage.listView?.setSelection(this@TelnetListPage.getCount() - 1)
                } else {
                    this@TelnetListPage.listView?.setSelection(selection)
                }
            }
        }
    }

    fun setListViewSelectionFromTop(selection: Int, top: Int) {
        if (listView != null) {
            if (selection == -1) {
                listView?.setSelection(getCount() - 1)
            } else {
                listView?.setSelectionFromTop(selection, top)
            }
        }
    }

    override fun onPageDidLoad() {
        loadCommandStack.setSize(2)
        isLoaded = true
    }

    override val pageLayout: Int
        get() = 0

    @Synchronized
    override fun onPagePreload(): Boolean {
        val pageData = loadPage()
        if (!isInitialed) {
            pushPreloadCommand(0)
            isInitialed = true
        }
        if (pageData == null) return false
        executeCommandFinished(pageData)
        insertPageData(pageData)
        executePreloadCommand()
        executeCommand()
        return true
    }

    @Synchronized  // com.kota.asFramework.pageController.ASViewController
    override fun onPageRefresh() {
        reloadListView()
        executeRefreshCommand()
    }

    private fun executeRefreshCommand() {
        if (pageRefreshCommand[0]) {
            setListViewSelection(0)
        }
        if (pageRefreshCommand[1]) {
            setListViewSelection(this.listCount - 1)
        }
        cleanRefreshCommand()
    }

    private fun cleanRefreshCommand() {
        Arrays.fill(pageRefreshCommand, false)
    }

    private fun executePreloadCommand() {
        if (pagePreloadCommand[0]) {
            loadLastBlock()
        }
        cleanPreloadCommand()
    }

    private fun cleanPreloadCommand() {
        Arrays.fill(pagePreloadCommand, false)
    }

    private fun removeBlock(key: Int) {
        if (TRACE_LOG_ENABLE) {
            try {
                Log.i("TelnetListPageTrace", "time=${java.time.Instant.now()} thread=${Thread.currentThread().name} isMain=${ASCoroutine.isMainThread} caller=${traceCaller()} action=removeBlock key=$key blockListSizeBefore=${blockList.size}")
            } catch (_: Exception) { }
        }
        var item: TelnetListPageItem? = null
        val block = blockList.remove(key)
        if (block != null) {
            var i = 0
            while (i < 20 && (block.getItem(i).also { item = it }) != null) {
                item!!.clear()
                recycleItem(item)
                i++
            }
            block.clear()
            recycleBlock(block)
        }
    }

    private fun insertPageData(telnetListPageBlock: TelnetListPageBlock) {
        if (TRACE_LOG_ENABLE) {
            try {
                val min = telnetListPageBlock.minimumItemNumber
                val max = telnetListPageBlock.maximumItemNumber
                Log.i("TelnetListPageTrace", "time=${java.time.Instant.now()} thread=${Thread.currentThread().name} isMain=${ASCoroutine.isMainThread} caller=${traceCaller()} action=insertPageData min=$min max=$max")
            } catch (_: Exception) { }
        }
        val blockIndex = getBlockIndex(telnetListPageBlock.minimumItemNumber - 1)
        synchronized(blockList) {
            setBlock(blockIndex, telnetListPageBlock)
            val firstBlockIndex = this.firstVisibleBlockIndex
            val lastBlockIndex = this.lastVisibleBlockIndex
            if (firstBlockIndex != 0 && lastBlockIndex != 0 && firstBlockIndex >= 0 && lastBlockIndex >= 0) {
                val keys: MutableSet<Int?> = HashSet(blockList.keys)
                for (key in keys) {
                    if (key != null && key != blockIndex && (key > lastBlockIndex + 3 || key < firstBlockIndex - 3)) {
                        removeBlock(key)
                    }
                }
            }
        }
        if (telnetListPageBlock.selectedItemNumber > 0) {
            this.selectedIndex = telnetListPageBlock.selectedItemNumber
            this.currentBlock = ItemUtils.getBlock(this.selectedIndex)
        }
        if (telnetListPageBlock.maximumItemNumber > this.listCount) {
            this.listCount = telnetListPageBlock.maximumItemNumber
        }
    }

    val firstVisibleBlockIndex: Int
        get() {
            if (listView == null) {
                return -1
            }
            return getBlockIndex(listView?.firstVisiblePosition!!)
        }

    val lastVisibleBlockIndex: Int
        get() {
            if (listView == null) {
                return -1
            }
            return getBlockIndex(listView?.lastVisiblePosition!!)
        }

    /** 開始自動加載最後頁 */
    private fun startAutoLoad() {
        if (!isAutoLoadEnable) return

        autoLoadJob?.cancel() // 取消之前的任务
        autoLoadJob = object : ASCoroutine() {
            override suspend fun run() {
                postDelayed(10000L) // 初始延迟

                try {
                    val currentTime = System.currentTimeMillis()
                    val totalOffset = currentTime - lastLoadTime
                    val spanOffset = currentTime - lastSendTime

                    // 根據距離上次載入/送出時間決定是否要自動發送載入最後一個區塊的命令
                    // 規則：
                    // - 超過 15 分鐘 (900000ms)：若距離上次送出超過 1 分鐘則送出
                    // - 超過 3 分鐘 (180000ms)：若距離上次送出超過 30 秒則送出
                    // - 超過 10 秒，且自上次載入的時間大於自上次送出的時間，表示有可能需要更新（避免頻繁重複送出）
                    val shouldSend = when {
                        totalOffset > 900000 -> spanOffset > 60000
                        totalOffset > 180000 -> spanOffset > 30000
                        totalOffset > 10000 && totalOffset > spanOffset -> true
                        else -> false
                    }

                    if (shouldSend || isManualLoadPending) {
                        loadLastBlock()
                        lastSendTime = currentTime
                        isManualLoadPending = false
                    }
                } catch (_: Exception) {
                    // 忽略錯誤
                } finally {
                    postDelayed(1000L) // 1秒间隔
                }
            }
        }
    }

    /** 停止自動加載最後頁 */
    private fun stopAutoLoad() {
        autoLoadJob?.cancel()
        autoLoadJob = null
    }

    @Synchronized
    fun popCommand(): TelnetCommand? {
        var command: TelnetCommand?
        command = null
        if (operationCommandStack.isNotEmpty()) {
            command = operationCommandStack.removeAt(0)
        } else if (!loadCommandStack.isEmpty()) {
            command = loadCommandStack.pop()
        }
        return command
    }

    @Synchronized
    fun rePushCommand(aCommand: TelnetCommand?) {
        if (aCommand != null) {
            if (!aCommand.isOperationCommand) {
                loadCommandStack.push(aCommand)
            } else {
                operationCommandStack.insertElementAt(aCommand, 0)
            }
        }
    }

    @Synchronized
    fun pushCommand(aCommand: TelnetCommand?) {
        pushCommand(aCommand, true)
    }

    @Synchronized
    fun pushCommand(aCommand: TelnetCommand?, executeNow: Boolean) {
        if (aCommand != null) {
            if (!aCommand.isOperationCommand) {
                loadCommandStack.push(aCommand)
            } else {
                operationCommandStack.add(aCommand)
            }
        }
        if (executeNow) {
            executeCommand()
        }
    }

    @Synchronized
    fun cleanCommand() {
        operationCommandStack.clear()
        loadCommandStack.clear()
        executingCommand = null
        cleanRefreshCommand()
        cleanPreloadCommand()
    }

    @Synchronized
    fun executeCommand() {
        if (executingCommand == null) {
            executingCommand = popCommand()
            if (executingCommand != null) {
                if (executingCommand?.recordTime!!) {
                    lastLoadTime = System.currentTimeMillis()
                }
                executingCommand?.execute(this)
                if (executingCommand?.isDone!!) {
                    executingCommand = null
                    executeCommand()
                }
            }
        }
    }

    @Synchronized
    fun executeCommandFinished(telnetListPageBlock: TelnetListPageBlock) {
        if (executingCommand != null) {
            executingCommand?.executeFinished(this, telnetListPageBlock)
            if (!executingCommand?.isDone!!) {
                rePushCommand(executingCommand)
            }
            executingCommand = null
        }
    }

    @Synchronized
    fun isLoadingBlock(itemIndex: Int): Boolean {
        var result: Boolean
        result = false
        if (executingCommand != null && executingCommand?.action == 0) {
            val loadBlockCommand = executingCommand as BahamutCommandLoadBlock
            result = loadBlockCommand.containsArticle(itemIndex)
        }
        if (!result) {
            val it = loadCommandStack.iterator()
            while (true) {
                if (!it.hasNext()) {
                    break
                }
                val command = it.next()
                if (command != null && command.action == 0) {
                    val loadBlockCommand2 = command as BahamutCommandLoadBlock
                    if (loadBlockCommand2.containsArticle(itemIndex)) {
                        result = true
                        break
                    }
                }
            }
        }
        return result
    }

    fun loadBoardBlock(block: Int) {
        val command: TelnetCommand = BahamutCommandLoadBlock(block)
        pushCommand(command)
    }

    fun moveToFirstPosition() {
        setListViewSelection(0)
    }

    @JvmOverloads
    fun loadLastBlock(isRecordTime: Boolean = true) {
        if (!containsLoadLastBlock()) {
            val command = BahamutCommandLoadLastBlock()
            command.recordTime = isRecordTime
            pushCommand(command)
            executeCommand()
        }
    }

    private fun containsLoadLastBlock(): Boolean {
        for (command in operationCommandStack) {
            if (command.action == 1) {
                return true
            }
        }
        return false
    }

    fun moveToLastPosition() {
        val command: TelnetCommand = BahamutCommandMoveToLastBlock()
        pushCommand(command)
        // 記錄最後瀏覽文章編號
        if (this::class == BoardMainPage::class)
            TempSettings.lastVisitArticleNumber = listCount
    }

    fun reloadListView() {
        if (listView != null) {
            safeNotifyDataSetChanged()
            if (!isListLoaded) {
                isListLoaded = true
                setListViewSelection(getCount() - 1)
            }
        }
    }

    // android.widget.Adapter
    override fun getCount(): Int {
        val intValue: Int = this.listCount
        return intValue
    }

    fun getIndexInBlock(itemIndex: Int): Int {
        return itemIndex % 20
    }

    fun getBlockIndex(itemIndex: Int): Int {
        return itemIndex / 20
    }

    fun setBlock(blockIndex: Int, aBlock: TelnetListPageBlock) {
        blockList.put(blockIndex, aBlock)
    }

    fun getBlock(blockIndex: Int): TelnetListPageBlock? {
        return blockList[blockIndex]
    }

    @Synchronized  // android.widget.Adapter
    override fun getItem(index: Int): TelnetListPageItem? {
        val item: TelnetListPageItem?
        val itemIndex = index + 1
        synchronized(blockList) {
            val block = getBlock(getBlockIndex(index))
            item = block?.getItem(getIndexInBlock(index))
        }
        if (item != null) {
            item.itemNumber = itemIndex
        }
        return item
    }

    override fun getItemId(index: Int): Long {
        return (index + 1).toLong()
    }

    override fun getItemViewType(index: Int): Int {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isEmpty(): Boolean {
        return getCount() == 0
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(index: Int): Boolean {
        return true
    }

    protected open fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        return false
    }

    override fun onItemLongClick(
        parentView: AdapterView<*>?,
        view: View?,
        index: Int,
        id: Long
    ): Boolean {
        return onListViewItemLongClicked(view, index)
    }

    override fun onItemClick(parentView: AdapterView<*>?, itemView: View?, index: Int, id: Long) {
        loadItemAtIndex(index)
    }

    protected fun saveListState() {
        if (listView != null) {
            val state: ListState = ListStateStore.instance.getState(this.listId)
            state.position = listView?.firstVisiblePosition!!
            val firstVisibleItemView = listView?.getChildAt(0)
            if (firstVisibleItemView != null) {
                state.top = firstVisibleItemView.top
            }
        }
    }

    protected fun loadListState() {
        if (listView != null) {
            val state: ListState = ListStateStore.instance.getState(this.listId)
            setListViewSelectionFromTop(state.position, state.top)
        }
    }

    open val listType: Int
        get() = BoardPageAction.LIST

    fun cleanAllItem() {
        if (TRACE_LOG_ENABLE) {
            try {
                Log.i("TelnetListPageTrace", "time=${java.time.Instant.now()} thread=${Thread.currentThread().name} isMain=${ASCoroutine.isMainThread} caller=${traceCaller()} action=cleanAllItem blockListSize=${blockList.size}")
            } catch (_: Exception) { }
        }
        synchronized(blockList) {
            val keys: HashSet<Int?> = HashSet(blockList.keys)
            for (key in keys) {
                if (key != null)
                    removeBlock(key)
            }
            blockList.clear()
        }
    }

    open fun isItemBlocked(aItem: TelnetListPageItem?): Boolean {
        return false
    }

    open val isItemBlockEnable: Boolean
        get() = false

    val listId: String?
        get() = getListIdFromListName(this.listName)

    open fun getListIdFromListName(aName: String?): String? {
        return aName
    }

    fun onLoadItemStart() {
        showProcessingDialog(getContextString(R.string.loading))
    }

    fun onLoadItemFinished() {
        dismissProcessingDialog()
    }
}
