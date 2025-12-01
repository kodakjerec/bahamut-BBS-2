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
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandLoadArticle
import com.kota.Bahamut.command.BahamutCommandLoadBlock
import com.kota.Bahamut.command.BahamutCommandLoadLastBlock
import com.kota.Bahamut.command.BahamutCommandMoveToLastBlock
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.telnet.logic.ItemUtils
import com.kota.telnetUI.TelnetPage
import java.util.Arrays
import java.util.Stack
import java.util.Vector

abstract class TelnetListPage : TelnetPage(), ListAdapter, OnItemClickListener,
    OnItemLongClickListener {
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
    private var autoLoadThread: AutoLoadThread? = null
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

    abstract fun recycleBlock(telnetListPageBlock: TelnetListPageBlock)

    abstract fun recycleItem(telnetListPageItem: TelnetListPageItem)

    // android.widget.Adapter
    override fun registerDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.registerObserver(observer)
    }

    // android.widget.Adapter
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.unregisterObserver(observer)
    }

    private inner class AutoLoadThread : Thread() {
        var run: Boolean = true

        // java.lang.Thread, java.lang.Runnable
        override fun run() {
            var sendCommand: Boolean
            try {
                sleep(10000L)
                while (run) {
                    val currentTime = System.currentTimeMillis()
                    val totalOffset = currentTime - this@TelnetListPage.lastLoadTime
                    val spanOffset = currentTime - this@TelnetListPage.lastSendTime
                    sendCommand = if (totalOffset > 900000) {
                        spanOffset > 60000
                    } else if (totalOffset > 180000) {
                        spanOffset > 30000
                    } else if (totalOffset > 10000 && totalOffset > spanOffset) {
                        true
                    } else {
                        false
                    }
                    if ((sendCommand || this@TelnetListPage.isManualLoadPending) && run) {
                        this@TelnetListPage.loadLastBlock(false)
                        this@TelnetListPage.lastSendTime = currentTime
                    }
                    this@TelnetListPage.isManualLoadPending = false
                    sleep(1000L)
                }
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                run = false
            }
        }
    }
    fun setManualLoadPage() {
        isManualLoadPending = true
    }

    // com.kota.telnetUI.TelnetPage, com.kota.asFramework.pageController.ASViewController
    override fun onPageDidUnload() {
        stopAutoLoad()
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
        object : ASRunner() {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                mDataSetObservable.notifyChanged()

                // 若 listView 的 adapter 是 BaseAdapter，呼叫其 notifyDataSetChanged
                val adapter = listView?.adapter
                if (adapter is android.widget.BaseAdapter) {
                    adapter.notifyDataSetChanged()
                } else if (adapter is androidx.recyclerview.widget.RecyclerView.Adapter<*>) {
                    adapter.notifyDataSetChanged()
                }
            }
        }.runInMainThread()
    }

    val loadingItemNumber: Int
        get() = this.lastLoadItemIndex + 1

    fun isItemLoadingByIndex(index: Int): Boolean {
        return index == this.lastLoadItemIndex
    }

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
        safeNotifyDataSetChanged()
    }

    fun setListViewSelection(selection: Int) {
        object : ASRunner() {
            override fun run() {
                if (this@TelnetListPage.listView != null) {
                    if (selection == -1) {
                        this@TelnetListPage.listView?.setSelection(this@TelnetListPage.getCount() - 1)
                    } else {
                        this@TelnetListPage.listView?.setSelection(selection)
                    }
                }
            }
        }.runInMainThread()
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
        safeNotifyDataSetChanged()
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

    private fun removeBlock(key: Int?) {
        if (key == null) {
            return
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
        // 確保在移除 block 後通知 UI（在主執行緒）
        safeNotifyDataSetChanged()
    }

    private fun insertPageData(telnetListPageBlock: TelnetListPageBlock) {
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
        // 插入/更新資料結構後，確保在主執行緒通知 ListView 更新
        safeNotifyDataSetChanged()
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

    private fun startAutoLoad() {
        synchronized(this) {
            if (!this.isAutoLoadEnable) return
            if (autoLoadThread?.isAlive == true) return
            val thread = AutoLoadThread()
            autoLoadThread = thread
            thread.start()
        }
    }

    private fun stopAutoLoad() {
        val threadToStop: AutoLoadThread?
        synchronized(this) {
            threadToStop = autoLoadThread
            autoLoadThread = null
        }
        threadToStop?.interrupt()
        try {
            threadToStop?.join(2000L)
        } catch (ie: InterruptedException) {
            Thread.currentThread().interrupt()
        }
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

    @get:Synchronized
    val isLoadingSize: Boolean
        get() {
            var loadSizeCommandExists: Boolean
            loadSizeCommandExists = false
            val it =
                operationCommandStack.iterator()
            while (true) {
                if (!it.hasNext()) {
                    break
                }
                val command = it.next()
                if (command.action == 2) {
                    loadSizeCommandExists = true
                    break
                }
            }
            return loadSizeCommandExists
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
        synchronized(blockList) {
            val keys: HashSet<Int?> = HashSet(blockList.keys)
            for (key in keys) {
                removeBlock(key)
            }
            blockList.clear()
        }
        // 清空所有項目後通知 UI 更新（在主執行緒）
        safeNotifyDataSetChanged()
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
