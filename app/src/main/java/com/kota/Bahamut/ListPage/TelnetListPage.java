package com.kota.Bahamut.ListPage;

import android.annotation.SuppressLint;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.Command.BahamutCommandLoadArticle;
import com.kota.Bahamut.Command.BahamutCommandLoadBlock;
import com.kota.Bahamut.Command.BahamutCommandLoadLastBlock;
import com.kota.Bahamut.Command.BahamutCommandMoveToLastBlock;
import com.kota.Bahamut.Command.TelnetCommand;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.TelnetUI.TelnetPage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

/* loaded from: classes.dex */
public abstract class TelnetListPage extends TelnetPage implements ListAdapter, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static final int LIST_TYPE_BOARD = 0;
    public static final int LIST_TYPE_LINK = 1;
    public static final int LIST_TYPE_SEARCH = 2;
    private Vector<TelnetCommand> _operation_command_stack = new Vector<>();
    private Stack<TelnetCommand> _load_command_stack = new Stack<>();
    private TelnetCommand _executing_command = null;
    private boolean[] _page_preload_command = new boolean[1];
    private boolean[] _page_refresh_command = new boolean[2];
    private String _list_name = null;
    private ListView _list_view = null;
    private boolean _list_loaded = false;
    private long _last_load_time = 0;
    private long _last_send_time = 0;
    private AutoLoadThread _auto_load_thread = null;
    private Integer _list_count = 0;
    private int _item_size = 0;
    private int _selected_index = 0;
    private int _current_block = 0;
    private int _last_load_item_index = 0;
    private boolean _initialed = false;
    private boolean _manual_load_page = false;
    @SuppressLint({"UseSparseArrays"})
    private Map<Integer, TelnetListPageBlock> _block_list = new HashMap();
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override // android.widget.Adapter
    public abstract View getView(int i, View view, ViewGroup viewGroup);

    public abstract boolean isAutoLoadEnable();

    public abstract TelnetListPageBlock loadPage();

    public abstract void recycleBlock(TelnetListPageBlock telnetListPageBlock);

    public abstract void recycleItem(TelnetListPageItem telnetListPageItem);

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        this.mDataSetObservable.notifyChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AutoLoadThread extends Thread {
        public boolean run;

        private AutoLoadThread() {
            this.run = true;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            boolean send_command;
            try {
                sleep(10000L);
                while (this.run) {
                    long current_time = System.currentTimeMillis();
                    long total_offset = current_time - TelnetListPage.this._last_load_time;
                    long span_offset = current_time - TelnetListPage.this._last_send_time;
                    if (total_offset > 900000) {
                        send_command = span_offset > 60000;
                    } else if (total_offset > 180000) {
                        send_command = span_offset > 30000;
                    } else if (total_offset > 10000 && total_offset > span_offset) {
                        send_command = true;
                    } else {
                        send_command = false;
                    }
                    if ((send_command || TelnetListPage.this._manual_load_page) && this.run) {
                        TelnetListPage.this.loadLastBlock(false);
                        TelnetListPage.this._last_send_time = current_time;
                    }
                    TelnetListPage.this._manual_load_page = false;
                    sleep(1000L);
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.run = false;
            }
        }
    }

    public void setManualLoadPage() {
        this._manual_load_page = true;
    }

    @Override // com.kota.TelnetUI.TelnetPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidUnload() {
        stopAutoLoad();
        super.onPageDidUnload();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageDidRemoveFromNavigationController() {
        this._initialed = false;
        cleanAllItem();
        stopAutoLoad();
    }

    public void setListView(ListView aListView) {
        this._list_view = aListView;
        if (this._list_view != null) {
            this._list_view.setOnItemClickListener(this);
            this._list_view.setOnItemLongClickListener(this);
            this._list_view.setAdapter((ListAdapter) this);
        }
    }

    public int getLastLoadItemIndex() {
        return this._last_load_item_index;
    }

    public int getLoadingItemNumber() {
        return getLastLoadItemIndex() + 1;
    }

    public boolean isItemLoadingByIndex(int index) {
        return index == getLastLoadItemIndex();
    }

    public boolean isItemLoadingByNumber(int number) {
        return number == getLoadingItemNumber();
    }

    public boolean isItemCanLoadAtIndex(int index) {
        return true;
    }

    public void loadItemAtNumber(int number) {
        loadItemAtIndex(number - 1);
    }

    public void loadItemAtIndex(int index) {
        if (isItemCanLoadAtIndex(index)) {
            this._last_load_item_index = index;
            TelnetCommand command = new BahamutCommandLoadArticle(index + 1);
            pushCommand(command);
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageWillAppear() {
        loadListState();
        startAutoLoad();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageWillDisappear() {
        stopAutoLoad();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageDidDisappear() {
        saveListState();
    }

    public void pushRefreshCommand(int aCommand) {
        this._page_refresh_command[aCommand] = true;
    }

    public void pushPreloadCommand(int aCommand) {
        this._page_preload_command[aCommand] = true;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return 0;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void clear() {
        cleanCommand();
        cleanAllItem();
        this._list_loaded = false;
        this._selected_index = 0;
        this._current_block = 0;
        this._item_size = 0;
        this._last_load_time = 0L;
        this._last_send_time = 0L;
        this._list_name = null;
    }

    public ListView getListView() {
        return this._list_view;
    }

    public void setListViewSelection(final int selection) {
        new ASRunner() { // from class: com.kota.Bahamut.ListPage.TelnetListPage.1
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                if (TelnetListPage.this._list_view != null) {
                    if (selection == -1) {
                        TelnetListPage.this._list_view.setSelection(TelnetListPage.this.getCount() - 1);
                    } else {
                        TelnetListPage.this._list_view.setSelection(selection);
                    }
                }
            }
        }.runInMainThread();
    }

    public void setListViewSelectionFromTop(int selection, int top) {
        if (this._list_view != null) {
            if (selection == -1) {
                this._list_view.setSelection(getCount() - 1);
            } else {
                this._list_view.setSelectionFromTop(selection, top);
            }
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageDidLoad() {
        this._load_command_stack.setSize(2);
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageLayout() {
        return 0;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public synchronized boolean onPagePreload() {
        TelnetListPageBlock page_data = loadPage();
        if (!this._initialed) {
            pushPreloadCommand(0);
            this._initialed = true;
        }
        executeCommandFinished(page_data);
        insertPageData(page_data);
        executePreloadCommand();
        executeCommand();
        return true;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public synchronized void onPageRefresh() {
        synchronized (this._list_count) {
            this._list_count = Integer.valueOf(this._item_size);
            reloadListView();
        }
        executeRefreshCommand();
    }

    private void executeRefreshCommand() {
        if (this._page_refresh_command[0]) {
            setListViewSelection(0);
        }
        if (this._page_refresh_command[1]) {
            setListViewSelection(getItemSize() - 1);
        }
        cleanRefreshCommand();
    }

    private void cleanRefreshCommand() {
        for (int i = 0; i < this._page_refresh_command.length; i++) {
            this._page_refresh_command[i] = false;
        }
    }

    private void executePreloadCommand() {
        if (this._page_preload_command[0]) {
            loadLastBlock();
        }
        cleanPreloadCommand();
    }

    private void cleanPreloadCommand() {
        for (int i = 0; i < this._page_preload_command.length; i++) {
            this._page_preload_command[i] = false;
        }
    }

    private void removeBlock(Integer key) {
        TelnetListPageItem item;
        TelnetListPageBlock block = this._block_list.remove(key);
        if (block != null) {
            for (int i = 0; i < 20 && (item = block.getItem(i)) != null; i++) {
                item.clear();
                recycleItem(item);
            }
            block.clear();
            recycleBlock(block);
        }
    }

    private void insertPageData(TelnetListPageBlock aPageData) {
        if (aPageData != null) {
            int block_index = getBlockIndex(aPageData.minimumItemNumber - 1);
            synchronized (this._block_list) {
                setBlock(block_index, aPageData);
                int first_block_index = getFirstVisibleBlockIndex();
                int last_block_index = getLastVisibleBlockIndex();
                if (first_block_index != 0 && last_block_index != 0 && first_block_index >= 0 && last_block_index >= 0) {
                    Set<Integer> keys = new HashSet<>(this._block_list.keySet());
                    for (Integer key : keys) {
                        if (key.intValue() != block_index && (key.intValue() > last_block_index + 3 || key.intValue() < first_block_index - 3)) {
                            removeBlock(key);
                        }
                    }
                }
            }
            if (aPageData.selectedItemNumber > 0) {
                this._selected_index = aPageData.selectedItemNumber;
                this._current_block = ItemUtils.getBlock(this._selected_index);
            }
            if (aPageData.maximumItemNumber > getItemSize()) {
                setItemSize(aPageData.maximumItemNumber);
            }
        }
    }

    public int getFirstVisibleBlockIndex() {
        if (this._list_view == null) {
            return -1;
        }
        int index = getBlockIndex(this._list_view.getFirstVisiblePosition());
        return index;
    }

    public int getLastVisibleBlockIndex() {
        if (this._list_view == null) {
            return -1;
        }
        int index = getBlockIndex(this._list_view.getLastVisiblePosition());
        return index;
    }

    private void startAutoLoad() {
        if (isAutoLoadEnable() && this._auto_load_thread == null) {
            this._auto_load_thread = new AutoLoadThread();
            this._auto_load_thread.start();
        }
    }

    private void stopAutoLoad() {
        if (this._auto_load_thread != null) {
            this._auto_load_thread.run = false;
            this._auto_load_thread = null;
        }
    }

    public synchronized TelnetCommand popCommand() {
        TelnetCommand command;
        command = null;
        if (this._operation_command_stack.size() > 0) {
            TelnetCommand command2 = this._operation_command_stack.remove(0);
            command = command2;
        } else if (!this._load_command_stack.isEmpty()) {
            TelnetCommand command3 = this._load_command_stack.pop();
            command = command3;
        }
        return command;
    }

    public synchronized void repushCommand(TelnetCommand aCommand) {
        if (aCommand != null) {
            if (!aCommand.isOperationCommand()) {
                this._load_command_stack.push(aCommand);
            } else {
                this._operation_command_stack.insertElementAt(aCommand, 0);
            }
        }
    }

    public synchronized void pushCommand(TelnetCommand aCommand) {
        pushCommand(aCommand, true);
    }

    public synchronized void pushCommand(TelnetCommand aCommand, boolean executeNow) {
        if (aCommand != null) {
            if (!aCommand.isOperationCommand()) {
                this._load_command_stack.push(aCommand);
            } else {
                this._operation_command_stack.add(aCommand);
            }
        }
        if (executeNow) {
            executeCommand();
        }
    }

    public synchronized void cleanCommand() {
        this._operation_command_stack.clear();
        this._load_command_stack.clear();
        this._executing_command = null;
        cleanRefreshCommand();
        cleanPreloadCommand();
    }

    public synchronized void executeCommand() {
        if (this._executing_command == null) {
            this._executing_command = popCommand();
            if (this._executing_command != null) {
                if (this._executing_command.recordTime) {
                    this._last_load_time = System.currentTimeMillis();
                }
                this._executing_command.execute(this);
                if (this._executing_command.isDone()) {
                    this._executing_command = null;
                    executeCommand();
                }
            }
        }
    }

    public synchronized void executeCommandFinished(TelnetListPageBlock aPageData) {
        if (this._executing_command != null) {
            this._executing_command.executeFinished(this, aPageData);
            if (!this._executing_command.isDone()) {
                repushCommand(this._executing_command);
            }
            this._executing_command = null;
        }
    }

    public synchronized boolean isLoadingBlock(int itemIndex) {
        boolean result;
        result = false;
        if (this._executing_command != null && this._executing_command.Action == 0) {
            BahamutCommandLoadBlock load_block_command = (BahamutCommandLoadBlock) this._executing_command;
            result = load_block_command.containsArticle(itemIndex);
        }
        if (!result) {
            Iterator<TelnetCommand> it = this._load_command_stack.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                TelnetCommand command = it.next();
                if (command != null && command.Action == 0) {
                    BahamutCommandLoadBlock load_block_command2 = (BahamutCommandLoadBlock) command;
                    if (load_block_command2.containsArticle(itemIndex)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public synchronized boolean isLoadingSize() {
        boolean load_size_command_exists;
        load_size_command_exists = false;
        Iterator<TelnetCommand> it = this._operation_command_stack.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            TelnetCommand command = it.next();
            if (command.Action == 2) {
                load_size_command_exists = true;
                break;
            }
        }
        return load_size_command_exists;
    }

    public void loadBoardBlock(int block) {
        TelnetCommand command = new BahamutCommandLoadBlock(block);
        pushCommand(command);
    }

    public void moveToFirstPosition() {
        setListViewSelection(0);
    }

    public void loadLastBlock() {
        loadLastBlock(true);
    }

    public void loadLastBlock(boolean isRecordTime) {
        if (!containsLoadLastBlock()) {
            BahamutCommandLoadLastBlock command = new BahamutCommandLoadLastBlock();
            command.recordTime = isRecordTime;
            pushCommand(command);
            executeCommand();
        }
    }

    private boolean containsLoadLastBlock() {
        Iterator<TelnetCommand> it = this._operation_command_stack.iterator();
        while (it.hasNext()) {
            TelnetCommand command = it.next();
            if (command.Action == 1) {
                return true;
            }
        }
        return false;
    }

    public void moveToLastPosition() {
        TelnetCommand command = new BahamutCommandMoveToLastBlock();
        pushCommand(command);
    }

    public void reloadListView() {
        if (this._list_view != null) {
            notifyDataSetChanged();
            if (!this._list_loaded) {
                this._list_loaded = true;
                setListViewSelection(getCount() - 1);
            }
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        int intValue;
        synchronized (this._list_count) {
            intValue = this._list_count.intValue();
        }
        return intValue;
    }

    public int getIndexInBlock(int itemIndex) {
        return itemIndex % 20;
    }

    public int getBlockIndex(int itemIndex) {
        return itemIndex / 20;
    }

    public void setBlock(int blockIndex, TelnetListPageBlock aBlock) {
        this._block_list.put(Integer.valueOf(blockIndex), aBlock);
    }

    public TelnetListPageBlock getBlock(int blockIndex) {
        return this._block_list.get(Integer.valueOf(blockIndex));
    }

    public int getBlockSize() {
        return this._block_list.size();
    }

    @Override // android.widget.Adapter
    public synchronized TelnetListPageItem getItem(int index) {
        TelnetListPageItem item;
        Integer item_index = Integer.valueOf(index + 1);
        synchronized (this._block_list) {
            TelnetListPageBlock block = getBlock(getBlockIndex(index));
            item = block != null ? block.getItem(getIndexInBlock(index)) : null;
        }
        if (item != null) {
            item.Number = item_index.intValue();
        }
        return item;
    }

    @Override // android.widget.Adapter
    public long getItemId(int index) {
        return index + 1;
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int index) {
        return 0;
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        return 1;
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override // android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override // android.widget.ListAdapter
    public boolean isEnabled(int index) {
        return true;
    }

    protected boolean onListViewItemLongClicked(View itemView, int index) {
        return false;
    }

    @Override // android.widget.AdapterView.OnItemLongClickListener
    public boolean onItemLongClick(AdapterView<?> parentView, View view, int index, long ID) {
        return onListViewItemLongClicked(view, index);
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parentView, View itemView, int index, long id) {
        loadItemAtIndex(index);
    }

    protected void saveListState() {
        if (this._list_view != null) {
            ListState state = ListStateStore.getInstance().getState(getListId());
            state.Position = this._list_view.getFirstVisiblePosition();
            View first_visible_item_view = this._list_view.getChildAt(0);
            if (first_visible_item_view != null) {
                state.Top = first_visible_item_view.getTop();
            }
        }
    }

    protected void loadListState() {
        if (this._list_view != null) {
            ListState state = ListStateStore.getInstance().getState(getListId());
            setListViewSelectionFromTop(state.Position, state.Top);
        }
    }

    public int getSelectedIndex() {
        return this._selected_index;
    }

    public int getListType() {
        return 0;
    }

    public void setItemSize(int size) {
        this._item_size = size;
    }

    public int getItemSize() {
        return this._item_size;
    }

    public void cleanAllItem() {
        synchronized (this._block_list) {
            Set<Integer> keys = new HashSet<>(this._block_list.keySet());
            for (Integer key : keys) {
                removeBlock(key);
            }
            this._block_list.clear();
        }
    }

    public void setListName(String aListName) {
        this._list_name = aListName;
    }

    public String getListName() {
        return this._list_name;
    }

    public boolean isItemBlocked(TelnetListPageItem aItem) {
        return false;
    }

    public boolean isItemBlockEnable() {
        return false;
    }

    public String getListId() {
        return getListIdFromListName(getListName());
    }

    public String getListIdFromListName(String aName) {
        return aName;
    }

    public void onLoadItemStart() {
        ASProcessingDialog.showProcessingDialog("讀取中");
    }

    public void onLoadItemFinished() {
        ASProcessingDialog.hideProcessingDialog();
    }

    public int getCurrentBlock() {
        return this._current_block;
    }
}
