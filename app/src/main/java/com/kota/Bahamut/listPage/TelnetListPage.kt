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
    open var name: String? = ""
        get() = "TelnetListPage"
    @JvmField
    protected var listViewWidget: ListView? = null
    private var isListLoaded = false
    private var lastLoadTime: Long = 0
    private var lastSendTime: Long = 0
    private var autoLoadThread: AutoLoadThread? = null
    var listCount = 0
    var itemSize: Int = 0
    var selectedIndex: Int = 0
        private set
    var currentBlock: Int = 0
        private set
    var lastLoadItemIndex: Int = 0
        private set
    private var isInitialed = false
    private var isManualLoadPending = false

    @SuppressLint("UseSparseArrays")
    private val blockList: MutableMap<Int?, TelnetListPageBlock?> =
        HashMap()
    private val mDataSetObservable = DataSetObservable()

    // android.widget.Adapter
    abstract override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View?

    abstract val isAutoLoadEnable: Boolean

    abstract fun loadPage(): TelnetListPageBlock?

    abstract fun recycleBlock(telnetListPageBlock: TelnetListPageBlock?)

    abstract fun recycleItem(telnetListPageItem: TelnetListPageItem?)

    // android.widget.Adapter
    override fun registerDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.registerObserver(observer)
    }

    // android.widget.Adapter
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        mDataSetObservable.unregisterObserver(observer)
    }

    fun notifyDataSetChanged() {
        mDataSetObservable.notifyChanged()
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
        this.itemSize = 0
        lastLoadTime = 0L
        lastSendTime = 0L
    }

    var listView: ListView?
        get() = listViewWidget
        set(aListView) {
            listViewWidget = aListView
            if (listViewWidget != null) {
                listViewWidget?.onItemClickListener = this
                listViewWidget?.onItemLongClickListener = this
                listViewWidget?.setAdapter(this)
            }
        }

    fun setListViewSelection(selection: Int) {
        object : ASRunner() {
            // from class: com.kota.Bahamut.ListPage.TelnetListPage.1
            // com.kota.asFramework.thread.ASRunner
            override fun run() {
                if (this@TelnetListPage.listViewWidget != null) {
                    if (selection == -1) {
                        this@TelnetListPage.listViewWidget?.setSelection(this@TelnetListPage.getCount() - 1)
                    } else {
                        this@TelnetListPage.listViewWidget?.setSelection(selection)
                    }
                }
            }
        }.runInMainThread()
    }

    fun setListViewSelectionFromTop(selection: Int, top: Int) {
        if (listViewWidget != null) {
            if (selection == -1) {
                listViewWidget?.setSelection(getCount() - 1)
            } else {
                listViewWidget?.setSelectionFromTop(selection, top)
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
        executePreloadCommand()
        executeCommand()
        return true
    }

    @Synchronized  // com.kota.asFramework.pageController.ASViewController
    override fun onPageRefresh() {
        listCount = this.itemSize
        reloadListView()
        executeRefreshCommand()
    }

    private fun executeRefreshCommand() {
        if (pageRefreshCommand[0]) {
            setListViewSelection(0)
        }
        if (pageRefreshCommand[1]) {
            setListViewSelection(this.itemSize - 1)
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
        var item: TelnetListPageItem? = null
        val block = blockList.remove(key)
        if (block != null) {
            var i = 0
            while (i < 20 && (block.getItem(i).also { item = it }) != null) {
                item?.clear()
                recycleItem(item)
                i++
            }
            block.clear()
            recycleBlock(block)
        }
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
        if (telnetListPageBlock.maximumItemNumber > this.itemSize) {
            this.itemSize = telnetListPageBlock.maximumItemNumber
        }
    }

    val firstVisibleBlockIndex: Int
        get() {
            if (listViewWidget == null) {
                return -1
            }
            return getBlockIndex(listViewWidget?.firstVisiblePosition!!)
        }

    val lastVisibleBlockIndex: Int
        get() {
            if (listViewWidget == null) {
                return -1
            }
            return getBlockIndex(listViewWidget?.lastVisiblePosition!!)
        }

    private fun startAutoLoad() {
        if (this.isAutoLoadEnable && autoLoadThread == null) {
            autoLoadThread = AutoLoadThread()
            autoLoadThread?.start()
        }
    }

    private fun stopAutoLoad() {
        if (autoLoadThread != null) {
            autoLoadThread?.run = false
            autoLoadThread = null
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
        if (listViewWidget != null) {
            notifyDataSetChanged()
            if (!isListLoaded) {
                isListLoaded = true
                setListViewSelection(getCount() - 1)
            }
        }
    }

    // android.widget.Adapter
    override fun getCount(): Int {
        val intValue: Int = listCount
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
        if (listViewWidget != null) {
            val state: ListState = ListStateStore.instance.getState(this.listId)
            state.position = listViewWidget?.firstVisiblePosition!!
            val firstVisibleItemView = listViewWidget?.getChildAt(0)
            if (firstVisibleItemView != null) {
                state.top = firstVisibleItemView.top
            }
        }
    }

    protected fun loadListState() {
        if (listViewWidget != null) {
            val state: ListState = ListStateStore.instance.getState(this.listId)
            setListViewSelectionFromTop(state.position, state.top)
        }
    }

    open val listType: Int
        get() = BoardPageAction.LIST

    fun cleanAllItem() {
        synchronized(blockList) {
            val keys: MutableSet<Int?> = HashSet(blockList.keys)
            for (key in keys) {
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
        get() = getListIdFromListName(this.name)

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
