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
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.Bahamut.command.BahamutCommandLoadArticle
import com.kota.Bahamut.command.BahamutCommandLoadBlock
import com.kota.Bahamut.command.BahamutCommandLoadLastBlock
import com.kota.Bahamut.command.BahamutCommandMoveToLastBlock
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.telnet.logic.ItemUtils
import com.kota.telnetUI.TelnetPage
import java.util.Arrays
import java.util.Stack
import java.util.Vector
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet

abstract class TelnetListPage : TelnetPage(), ListAdapter, OnItemClickListener,
    OnItemLongClickListener {
    private val _operation_command_stack = Vector<TelnetCommand>()
    private val _load_command_stack = Stack<TelnetCommand?>()
    private var _executing_command: TelnetCommand? = null
    private val _page_preload_command = BooleanArray(1)
    private val _page_refresh_command = BooleanArray(2)
    open var listName: String? = null
    @JvmField
    protected var _list_view: ListView? = null
    private var _list_loaded = false
    private var _last_load_time: Long = 0
    private var _last_send_time: Long = 0
    private var _auto_load_thread: AutoLoadThread? = null
    private var _list_count = 0
    var itemSize: Int = 0
    var selectedIndex: Int = 0
        private set
    var currentBlock: Int = 0
        private set
    var lastLoadItemIndex: Int = 0
        private set
    private var _initialed = false
    private var _manual_load_page = false

    @SuppressLint("UseSparseArrays")
    private val _block_list: MutableMap<Int?, TelnetListPageBlock?> =
        HashMap<Int?, TelnetListPageBlock?>()
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
            var send_command: Boolean
            try {
                sleep(10000L)
                while (run) {
                    val current_time = System.currentTimeMillis()
                    val total_offset = current_time - this@TelnetListPage._last_load_time
                    val span_offset = current_time - this@TelnetListPage._last_send_time
                    if (total_offset > 900000) {
                        send_command = span_offset > 60000
                    } else if (total_offset > 180000) {
                        send_command = span_offset > 30000
                    } else if (total_offset > 10000 && total_offset > span_offset) {
                        send_command = true
                    } else {
                        send_command = false
                    }
                    if ((send_command || this@TelnetListPage._manual_load_page) && run) {
                        this@TelnetListPage.loadLastBlock(false)
                        this@TelnetListPage._last_send_time = current_time
                    }
                    this@TelnetListPage._manual_load_page = false
                    sleep(1000L)
                }
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                run = false
            }
        }
    }

    fun setManualLoadPage() {
        _manual_load_page = true
    }

    // com.kota.TelnetUI.TelnetPage, com.kota.ASFramework.PageController.ASViewController
    public override fun onPageDidUnload() {
        stopAutoLoad()
        super.onPageDidUnload()
    }

    // com.kota.ASFramework.PageController.ASViewController
    public override fun onPageDidRemoveFromNavigationController() {
        _initialed = false
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

    // com.kota.ASFramework.PageController.ASViewController
    public override fun onPageWillAppear() {
        loadListState()
        startAutoLoad()
    }

    // com.kota.ASFramework.PageController.ASViewController
    public override fun onPageWillDisappear() {
        stopAutoLoad()
    }

    // com.kota.ASFramework.PageController.ASViewController
    public override fun onPageDidDisappear() {
        saveListState()
    }

    fun pushRefreshCommand(aCommand: Int) {
        _page_refresh_command[aCommand] = true
    }

    fun pushPreloadCommand(aCommand: Int) {
        _page_preload_command[aCommand] = true
    }

    val pageType: Int
        // com.kota.ASFramework.PageController.ASViewController
        get() = 0

    // com.kota.ASFramework.PageController.ASViewController
    public override fun clear() {
        cleanCommand()
        cleanAllItem()
        _list_loaded = false
        this.selectedIndex = 0
        this.currentBlock = 0
        this.itemSize = 0
        _last_load_time = 0L
        _last_send_time = 0L
        this.listName = null
    }

    var listView: ListView?
        get() = _list_view
        set(aListView) {
            _list_view = aListView
            if (_list_view != null) {
                _list_view!!.setOnItemClickListener(this)
                _list_view!!.setOnItemLongClickListener(this)
                _list_view!!.setAdapter(this)
            }
        }

    fun setListViewSelection(selection: Int) {
        object : ASRunner() {
            // from class: com.kota.Bahamut.ListPage.TelnetListPage.1
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                if (this@TelnetListPage._list_view != null) {
                    if (selection == -1) {
                        this@TelnetListPage._list_view!!.setSelection(this@TelnetListPage.getCount() - 1)
                    } else {
                        this@TelnetListPage._list_view!!.setSelection(selection)
                    }
                }
            }
        }.runInMainThread()
    }

    fun setListViewSelectionFromTop(selection: Int, top: Int) {
        if (_list_view != null) {
            if (selection == -1) {
                _list_view!!.setSelection(getCount() - 1)
            } else {
                _list_view!!.setSelectionFromTop(selection, top)
            }
        }
    }

    // com.kota.ASFramework.PageController.ASViewController
    public override fun onPageDidLoad() {
        _load_command_stack.setSize(2)
    }

    val pageLayout: Int
        // com.kota.ASFramework.PageController.ASViewController
        get() = 0

    @Synchronized  // com.kota.TelnetUI.TelnetPage
    public override fun onPagePreload(): Boolean {
        val page_data = loadPage()
        if (!_initialed) {
            pushPreloadCommand(0)
            _initialed = true
        }
        executeCommandFinished(page_data)
        insertPageData(page_data)
        executePreloadCommand()
        executeCommand()
        return true
    }

    @Synchronized  // com.kota.ASFramework.PageController.ASViewController
    public override fun onPageRefresh() {
        synchronized(_list_count) {
            _list_count = this.itemSize
            reloadListView()
        }
        executeRefreshCommand()
    }

    private fun executeRefreshCommand() {
        if (_page_refresh_command[0]) {
            setListViewSelection(0)
        }
        if (_page_refresh_command[1]) {
            setListViewSelection(this.itemSize - 1)
        }
        cleanRefreshCommand()
    }

    private fun cleanRefreshCommand() {
        Arrays.fill(_page_refresh_command, false)
    }

    private fun executePreloadCommand() {
        if (_page_preload_command[0]) {
            loadLastBlock()
        }
        cleanPreloadCommand()
    }

    private fun cleanPreloadCommand() {
        Arrays.fill(_page_preload_command, false)
    }

    private fun removeBlock(key: Int?) {
        var item: TelnetListPageItem?
        val block = _block_list.remove(key)
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

    private fun insertPageData(aPageData: TelnetListPageBlock?) {
        if (aPageData != null) {
            val block_index = getBlockIndex(aPageData.minimumItemNumber - 1)
            synchronized(_block_list) {
                setBlock(block_index, aPageData)
                val first_block_index = this.firstVisibleBlockIndex
                val last_block_index = this.lastVisibleBlockIndex
                if (first_block_index != 0 && last_block_index != 0 && first_block_index >= 0 && last_block_index >= 0) {
                    val keys: MutableSet<Int> = HashSet<Int>(_block_list.keys)
                    for (key in keys) {
                        if (key != block_index && (key > last_block_index + 3 || key < first_block_index - 3)) {
                            removeBlock(key)
                        }
                    }
                }
            }
            if (aPageData.selectedItemNumber > 0) {
                this.selectedIndex = aPageData.selectedItemNumber
                this.currentBlock = ItemUtils.getBlock(this.selectedIndex)
            }
            if (aPageData.maximumItemNumber > this.itemSize) {
                this.itemSize = aPageData.maximumItemNumber
            }
        }
    }

    val firstVisibleBlockIndex: Int
        get() {
            if (_list_view == null) {
                return -1
            }
            return getBlockIndex(_list_view!!.getFirstVisiblePosition())
        }

    val lastVisibleBlockIndex: Int
        get() {
            if (_list_view == null) {
                return -1
            }
            return getBlockIndex(_list_view!!.getLastVisiblePosition())
        }

    private fun startAutoLoad() {
        if (this.isAutoLoadEnable && _auto_load_thread == null) {
            _auto_load_thread = AutoLoadThread()
            _auto_load_thread!!.start()
        }
    }

    private fun stopAutoLoad() {
        if (_auto_load_thread != null) {
            _auto_load_thread!!.run = false
            _auto_load_thread = null
        }
    }

    @Synchronized
    fun popCommand(): TelnetCommand? {
        var command: TelnetCommand?
        command = null
        if (_operation_command_stack.size > 0) {
            command = _operation_command_stack.removeAt(0)
        } else if (!_load_command_stack.isEmpty()) {
            command = _load_command_stack.pop()
        }
        return command
    }

    @Synchronized
    fun rePushCommand(aCommand: TelnetCommand?) {
        if (aCommand != null) {
            if (!aCommand.isOperationCommand) {
                _load_command_stack.push(aCommand)
            } else {
                _operation_command_stack.insertElementAt(aCommand, 0)
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
                _load_command_stack.push(aCommand)
            } else {
                _operation_command_stack.add(aCommand)
            }
        }
        if (executeNow) {
            executeCommand()
        }
    }

    @Synchronized
    fun cleanCommand() {
        _operation_command_stack.clear()
        _load_command_stack.clear()
        _executing_command = null
        cleanRefreshCommand()
        cleanPreloadCommand()
    }

    @Synchronized
    fun executeCommand() {
        if (_executing_command == null) {
            _executing_command = popCommand()
            if (_executing_command != null) {
                if (_executing_command!!.recordTime) {
                    _last_load_time = System.currentTimeMillis()
                }
                _executing_command!!.execute(this)
                if (_executing_command!!.isDone) {
                    _executing_command = null
                    executeCommand()
                }
            }
        }
    }

    @Synchronized
    fun executeCommandFinished(aPageData: TelnetListPageBlock?) {
        if (_executing_command != null) {
            _executing_command!!.executeFinished(this, aPageData)
            if (!_executing_command!!.isDone) {
                rePushCommand(_executing_command)
            }
            _executing_command = null
        }
    }

    @Synchronized
    fun isLoadingBlock(itemIndex: Int): Boolean {
        var result: Boolean
        result = false
        if (_executing_command != null && _executing_command!!.Action == 0) {
            val load_block_command = _executing_command as BahamutCommandLoadBlock
            result = load_block_command.containsArticle(itemIndex)
        }
        if (!result) {
            val it = _load_command_stack.iterator()
            while (true) {
                if (!it.hasNext()) {
                    break
                }
                val command = it.next()
                if (command != null && command.Action == 0) {
                    val load_block_command2 = command as BahamutCommandLoadBlock
                    if (load_block_command2.containsArticle(itemIndex)) {
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
            var load_size_command_exists: Boolean
            load_size_command_exists = false
            val it =
                _operation_command_stack.iterator()
            while (true) {
                if (!it.hasNext()) {
                    break
                }
                val command = it.next()
                if (command.Action == 2) {
                    load_size_command_exists = true
                    break
                }
            }
            return load_size_command_exists
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
        for (command in _operation_command_stack) {
            if (command.Action == 1) {
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
        if (_list_view != null) {
            notifyDataSetChanged()
            if (!_list_loaded) {
                _list_loaded = true
                setListViewSelection(getCount() - 1)
            }
        }
    }

    // android.widget.Adapter
    override fun getCount(): Int {
        val intValue: Int
        synchronized(_list_count) {
            intValue = _list_count
        }
        return intValue
    }

    fun getIndexInBlock(itemIndex: Int): Int {
        return itemIndex % 20
    }

    fun getBlockIndex(itemIndex: Int): Int {
        return itemIndex / 20
    }

    fun setBlock(blockIndex: Int, aBlock: TelnetListPageBlock?) {
        _block_list.put(blockIndex, aBlock)
    }

    fun getBlock(blockIndex: Int): TelnetListPageBlock? {
        return _block_list.get(blockIndex)
    }

    val blockSize: Int
        get() = _block_list.size

    @Synchronized  // android.widget.Adapter
    override fun getItem(index: Int): TelnetListPageItem? {
        val item: TelnetListPageItem?
        val item_index = index + 1
        synchronized(_block_list) {
            val block = getBlock(getBlockIndex(index))
            item = if (block != null) block.getItem(getIndexInBlock(index)) else null
        }
        if (item != null) {
            item.Number = item_index
        }
        return item
    }

    // android.widget.Adapter
    override fun getItemId(index: Int): Long {
        return (index + 1).toLong()
    }

    // android.widget.Adapter
    override fun getItemViewType(index: Int): Int {
        return 0
    }

    // android.widget.Adapter
    override fun getViewTypeCount(): Int {
        return 1
    }

    // android.widget.Adapter
    override fun hasStableIds(): Boolean {
        return false
    }

    // android.widget.Adapter
    override fun isEmpty(): Boolean {
        return getCount() == 0
    }

    // android.widget.ListAdapter
    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    // android.widget.ListAdapter
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
        ID: Long
    ): Boolean {
        return onListViewItemLongClicked(view, index)
    }

    override fun onItemClick(parentView: AdapterView<*>?, itemView: View?, index: Int, id: Long) {
        loadItemAtIndex(index)
    }

    protected fun saveListState() {
        if (_list_view != null) {
            val state: ListState = ListStateStore.Companion.getInstance().getState(this.listId)
            state.Position = _list_view!!.getFirstVisiblePosition()
            val first_visible_item_view = _list_view!!.getChildAt(0)
            if (first_visible_item_view != null) {
                state.Top = first_visible_item_view.getTop()
            }
        }
    }

    protected fun loadListState() {
        if (_list_view != null) {
            val state: ListState = ListStateStore.Companion.getInstance().getState(this.listId)
            setListViewSelectionFromTop(state.Position, state.Top)
        }
    }

    open val listType: Int
        get() = BoardPageAction.LIST

    fun cleanAllItem() {
        synchronized(_block_list) {
            val keys: MutableSet<Int?> = HashSet<Int?>(_block_list.keys)
            for (key in keys) {
                removeBlock(key)
            }
            _block_list.clear()
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
