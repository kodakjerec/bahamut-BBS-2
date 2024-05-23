package com.kota.Bahamut.ListPage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

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
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.Command.BahamutCommandLoadArticle;
import com.kota.Bahamut.Command.BahamutCommandLoadBlock;
import com.kota.Bahamut.Command.BahamutCommandLoadLastBlock;
import com.kota.Bahamut.Command.BahamutCommandMoveToLastBlock;
import com.kota.Bahamut.Command.TelnetCommand;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.TelnetUI.TelnetPage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

/* loaded from: classes.dex */
public abstract class TelnetListPage extends TelnetPage implements ListAdapter, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private final Vector<TelnetCommand> _operation_command_stack = new Vector<>();
    private final Stack<TelnetCommand> _load_command_stack = new Stack<>();
    private TelnetCommand _executing_command = null;
    private final boolean[] _page_preload_command = new boolean[1];
    private final boolean[] _page_refresh_command = new boolean[2];
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
    private final Map<Integer, TelnetListPageBlock> _block_list = new HashMap<>();
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override // android.widget.Adapter
    public abstract View getView(int i, View view, ViewGroup viewGroup);

    public abstract boolean isAutoLoadEnable();

    public abstract TelnetListPageBlock loadPage();

    public abstract void recycleBlock(TelnetListPageBlock telnetListPageBlock);

    public abstract void recycleItem(TelnetListPageItem telnetListPageItem);

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    private class AutoLoadThread extends Thread {
        public boolean run;

        private AutoLoadThread() {
            run = true;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            boolean send_command;
            try {
                sleep(10000L);
                while (run) {
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
                    if ((send_command || TelnetListPage.this._manual_load_page) && run) {
                        TelnetListPage.this.loadLastBlock(false);
                        TelnetListPage.this._last_send_time = current_time;
                    }
                    TelnetListPage.this._manual_load_page = false;
                    sleep(1000L);
                }
            } catch (Exception e) {
                e.printStackTrace();
                run = false;
            }
        }
    }

    public void setManualLoadPage() {
        _manual_load_page = true;
    }

    @Override // com.kota.TelnetUI.TelnetPage, com.kota.ASFramework.PageController.ASViewController
    public void onPageDidUnload() {
        stopAutoLoad();
        super.onPageDidUnload();
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageDidRemoveFromNavigationController() {
        _initialed = false;
        cleanAllItem();
        stopAutoLoad();
    }

    public void setListView(ListView aListView) {
        _list_view = aListView;
        if (_list_view != null) {
            _list_view.setOnItemClickListener(this);
            _list_view.setOnItemLongClickListener(this);
            _list_view.setAdapter(this);
        }
    }

    public int getLastLoadItemIndex() {
        return _last_load_item_index;
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
            _last_load_item_index = index;
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
        _page_refresh_command[aCommand] = true;
    }

    public void pushPreloadCommand(int aCommand) {
        _page_preload_command[aCommand] = true;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return 0;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void clear() {
        cleanCommand();
        cleanAllItem();
        _list_loaded = false;
        _selected_index = 0;
        _current_block = 0;
        _item_size = 0;
        _last_load_time = 0L;
        _last_send_time = 0L;
        _list_name = null;
    }

    public ListView getListView() {
        return _list_view;
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
        if (_list_view != null) {
            if (selection == -1) {
                _list_view.setSelection(getCount() - 1);
            } else {
                _list_view.setSelectionFromTop(selection, top);
            }
        }
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public void onPageDidLoad() {
        _load_command_stack.setSize(2);
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageLayout() {
        return 0;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public synchronized boolean onPagePreload() {
        TelnetListPageBlock page_data = loadPage();
        if (!_initialed) {
            pushPreloadCommand(0);
            _initialed = true;
        }
        executeCommandFinished(page_data);
        insertPageData(page_data);
        executePreloadCommand();
        executeCommand();
        return true;
    }

    @Override // com.kota.ASFramework.PageController.ASViewController
    public synchronized void onPageRefresh() {
        synchronized (_list_count) {
            _list_count = _item_size;
            reloadListView();
        }
        executeRefreshCommand();
    }

    private void executeRefreshCommand() {
        if (_page_refresh_command[0]) {
            setListViewSelection(0);
        }
        if (_page_refresh_command[1]) {
            setListViewSelection(getItemSize() - 1);
        }
        cleanRefreshCommand();
    }

    private void cleanRefreshCommand() {
        Arrays.fill(_page_refresh_command, false);
    }

    private void executePreloadCommand() {
        if (_page_preload_command[0]) {
            loadLastBlock();
        }
        cleanPreloadCommand();
    }

    private void cleanPreloadCommand() {
        Arrays.fill(_page_preload_command, false);
    }

    private void removeBlock(Integer key) {
        TelnetListPageItem item;
        TelnetListPageBlock block = _block_list.remove(key);
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
            synchronized (_block_list) {
                setBlock(block_index, aPageData);
                int first_block_index = getFirstVisibleBlockIndex();
                int last_block_index = getLastVisibleBlockIndex();
                if (first_block_index != 0 && last_block_index != 0 && first_block_index >= 0 && last_block_index >= 0) {
                    Set<Integer> keys = new HashSet<>(_block_list.keySet());
                    for (Integer key : keys) {
                        if (key != block_index && (key > last_block_index + 3 || key < first_block_index - 3)) {
                            removeBlock(key);
                        }
                    }
                }
            }
            if (aPageData.selectedItemNumber > 0) {
                _selected_index = aPageData.selectedItemNumber;
                _current_block = ItemUtils.getBlock(_selected_index);
            }
            if (aPageData.maximumItemNumber > getItemSize()) {
                setItemSize(aPageData.maximumItemNumber);
            }
        }
    }

    public int getFirstVisibleBlockIndex() {
        if (_list_view == null) {
            return -1;
        }
        return getBlockIndex(_list_view.getFirstVisiblePosition());
    }

    public int getLastVisibleBlockIndex() {
        if (_list_view == null) {
            return -1;
        }
        return getBlockIndex(_list_view.getLastVisiblePosition());
    }

    private void startAutoLoad() {
        if (isAutoLoadEnable() && _auto_load_thread == null) {
            _auto_load_thread = new AutoLoadThread();
            _auto_load_thread.start();
        }
    }

    private void stopAutoLoad() {
        if (_auto_load_thread != null) {
            _auto_load_thread.run = false;
            _auto_load_thread = null;
        }
    }

    public synchronized TelnetCommand popCommand() {
        TelnetCommand command;
        command = null;
        if (_operation_command_stack.size() > 0) {
            command = _operation_command_stack.remove(0);
        } else if (!_load_command_stack.isEmpty()) {
            command = _load_command_stack.pop();
        }
        return command;
    }

    public synchronized void rePushCommand(TelnetCommand aCommand) {
        if (aCommand != null) {
            if (!aCommand.isOperationCommand()) {
                _load_command_stack.push(aCommand);
            } else {
                _operation_command_stack.insertElementAt(aCommand, 0);
            }
        }
    }

    public synchronized void pushCommand(TelnetCommand aCommand) {
        pushCommand(aCommand, true);
    }

    public synchronized void pushCommand(TelnetCommand aCommand, boolean executeNow) {
        if (aCommand != null) {
            if (!aCommand.isOperationCommand()) {
                _load_command_stack.push(aCommand);
            } else {
                _operation_command_stack.add(aCommand);
            }
        }
        if (executeNow) {
            executeCommand();
        }
    }

    public synchronized void cleanCommand() {
        _operation_command_stack.clear();
        _load_command_stack.clear();
        _executing_command = null;
        cleanRefreshCommand();
        cleanPreloadCommand();
    }

    public synchronized void executeCommand() {
        if (_executing_command == null) {
            _executing_command = popCommand();
            if (_executing_command != null) {
                if (_executing_command.recordTime) {
                    _last_load_time = System.currentTimeMillis();
                }
                _executing_command.execute(this);
                if (_executing_command.isDone()) {
                    _executing_command = null;
                    executeCommand();
                }
            }
        }
    }

    public synchronized void executeCommandFinished(TelnetListPageBlock aPageData) {
        if (_executing_command != null) {
            _executing_command.executeFinished(this, aPageData);
            if (!_executing_command.isDone()) {
                rePushCommand(_executing_command);
            }
            _executing_command = null;
        }
    }

    public synchronized boolean isLoadingBlock(int itemIndex) {
        boolean result;
        result = false;
        if (_executing_command != null && _executing_command.Action == 0) {
            BahamutCommandLoadBlock load_block_command = (BahamutCommandLoadBlock) _executing_command;
            result = load_block_command.containsArticle(itemIndex);
        }
        if (!result) {
            Iterator<TelnetCommand> it = _load_command_stack.iterator();
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
        Iterator<TelnetCommand> it = _operation_command_stack.iterator();
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
        ASToast.showShortToast(getContextString(R.string.already_to_top_page));
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
        for (TelnetCommand command : _operation_command_stack) {
            if (command.Action == 1) {
                return true;
            }
        }
        return false;
    }

    public void moveToLastPosition() {
        TelnetCommand command = new BahamutCommandMoveToLastBlock();
        pushCommand(command);
        ASToast.showShortToast(getContextString(R.string.already_to_bottom_page));
    }

    public void reloadListView() {
        if (_list_view != null) {
            notifyDataSetChanged();
            if (!_list_loaded) {
                _list_loaded = true;
                setListViewSelection(getCount() - 1);
            }
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        int intValue;
        synchronized (_list_count) {
            intValue = _list_count;
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
        _block_list.put(blockIndex, aBlock);
    }

    public TelnetListPageBlock getBlock(int blockIndex) {
        return _block_list.get(blockIndex);
    }

    public int getBlockSize() {
        return _block_list.size();
    }

    @Override // android.widget.Adapter
    public synchronized TelnetListPageItem getItem(int index) {
        TelnetListPageItem item;
        int item_index = index + 1;
        synchronized (_block_list) {
            TelnetListPageBlock block = getBlock(getBlockIndex(index));
            item = block != null ? block.getItem(getIndexInBlock(index)) : null;
        }
        if (item != null) {
            item.Number = item_index;
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
        if (_list_view != null) {
            ListState state = ListStateStore.getInstance().getState(getListId());
            state.Position = _list_view.getFirstVisiblePosition();
            View first_visible_item_view = _list_view.getChildAt(0);
            if (first_visible_item_view != null) {
                state.Top = first_visible_item_view.getTop();
            }
        }
    }

    protected void loadListState() {
        if (_list_view != null) {
            ListState state = ListStateStore.getInstance().getState(getListId());
            setListViewSelectionFromTop(state.Position, state.Top);
        }
    }

    public int getSelectedIndex() {
        return _selected_index;
    }

    public int getListType() {
        return 0;
    }

    public void setItemSize(int size) {
        _item_size = size;
    }

    public int getItemSize() {
        return _item_size;
    }

    public void cleanAllItem() {
        synchronized (_block_list) {
            Set<Integer> keys = new HashSet<>(_block_list.keySet());
            for (Integer key : keys) {
                removeBlock(key);
            }
            _block_list.clear();
        }
    }

    public void setListName(String aListName) {
        _list_name = aListName;
    }

    public String getListName() {
        return _list_name;
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
        ASProcessingDialog.showProcessingDialog(getContextString(R.string.loading));
    }

    public void onLoadItemFinished() {
        ASProcessingDialog.dismissProcessingDialog();
    }

    public int getCurrentBlock() {
        return _current_block;
    }
}
