package com.kumi.Bahamut.ListPage;

import android.annotation.SuppressLint;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.kumi.ASFramework.Dialog.ASProcessingDialog;
import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.Bahamut.Command.BahamutCommandLoadArticle;
import com.kumi.Bahamut.Command.BahamutCommandLoadBlock;
import com.kumi.Bahamut.Command.BahamutCommandLoadLastBlock;
import com.kumi.Bahamut.Command.BahamutCommandMoveToLastBlock;
import com.kumi.Bahamut.Command.TelnetCommand;
import com.kumi.Telnet.Logic.ItemUtils;
import com.kumi.TelnetUI.TelnetPage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

public abstract class TelnetListPage extends TelnetPage implements ListAdapter, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
  public static final int LIST_TYPE_BOARD = 0;
  
  public static final int LIST_TYPE_LINK = 1;
  
  public static final int LIST_TYPE_SEARCH = 2;
  
  private AutoLoadThread _auto_load_thread = null;
  
  @SuppressLint({"UseSparseArrays"})
  private Map<Integer, TelnetListPageBlock> _block_list = new HashMap<Integer, TelnetListPageBlock>();
  
  private int _current_block = 0;
  
  private TelnetCommand _executing_command = null;
  
  private boolean _initialed = false;
  
  private int _item_size = 0;
  
  private int _last_load_item_index = 0;
  
  private long _last_load_time = 0L;
  
  private long _last_send_time = 0L;
  
  private Integer _list_count = Integer.valueOf(0);
  
  private boolean _list_loaded = false;
  
  private String _list_name = null;
  
  private ListView _list_view = null;
  
  private Stack<TelnetCommand> _load_command_stack = new Stack<TelnetCommand>();
  
  private boolean _manual_load_page = false;
  
  private Vector<TelnetCommand> _operation_command_stack = new Vector<TelnetCommand>();
  
  private boolean[] _page_preload_command = new boolean[1];
  
  private boolean[] _page_refresh_command = new boolean[2];
  
  private int _selected_index = 0;
  
  private final DataSetObservable mDataSetObservable = new DataSetObservable();
  
  private void cleanPreloadCommand() {
    for (byte b = 0; b < this._page_preload_command.length; b++)
      this._page_preload_command[b] = false; 
  }
  
  private void cleanRefreshCommand() {
    for (byte b = 0; b < this._page_refresh_command.length; b++)
      this._page_refresh_command[b] = false; 
  }
  
  private boolean containsLoadLastBlock() {
    boolean bool1;
    boolean bool2 = false;
    Iterator<TelnetCommand> iterator = this._operation_command_stack.iterator();
    while (true) {
      bool1 = bool2;
      if (iterator.hasNext()) {
        if (((TelnetCommand)iterator.next()).Action == 1) {
          bool1 = true;
          break;
        } 
        continue;
      } 
      break;
    } 
    return bool1;
  }
  
  private void executePreloadCommand() {
    if (this._page_preload_command[0])
      loadLastBlock(); 
    cleanPreloadCommand();
  }
  
  private void executeRefreshCommand() {
    if (this._page_refresh_command[0])
      setListViewSelection(0); 
    if (this._page_refresh_command[1])
      setListViewSelection(getItemSize() - 1); 
    cleanRefreshCommand();
  }
  
  private void insertPageData(TelnetListPageBlock paramTelnetListPageBlock) {
    if (paramTelnetListPageBlock != null) {
      int i = getBlockIndex(paramTelnetListPageBlock.minimumItemNumber - 1);
      synchronized (this._block_list) {
        setBlock(i, paramTelnetListPageBlock);
        int k = getFirstVisibleBlockIndex();
        int j = getLastVisibleBlockIndex();
        if (k != 0 && j != 0 && k >= 0 && j >= 0) {
          HashSet hashSet = new HashSet();
          this((Collection)this._block_list.keySet());
          for (Integer integer : hashSet) {
            if (integer.intValue() != i && (integer.intValue() > j + 3 || integer.intValue() < k - 3))
              removeBlock(integer); 
          } 
        } 
      } 
      /* monitor exit ClassFileLocalVariableReferenceExpression{type=ObjectType{java/lang/Object}, name=SYNTHETIC_LOCAL_VARIABLE_5} */
      if (paramTelnetListPageBlock.selectedItemNumber > 0) {
        this._selected_index = paramTelnetListPageBlock.selectedItemNumber;
        this._current_block = ItemUtils.getBlock(this._selected_index);
      } 
      if (paramTelnetListPageBlock.maximumItemNumber > getItemSize())
        setItemSize(paramTelnetListPageBlock.maximumItemNumber); 
    } 
  }
  
  private void removeBlock(Integer paramInteger) {
    TelnetListPageBlock telnetListPageBlock = this._block_list.remove(paramInteger);
    if (telnetListPageBlock != null) {
      byte b = 0;
      while (true) {
        if (b < 20) {
          TelnetListPageItem telnetListPageItem = telnetListPageBlock.getItem(b);
          if (telnetListPageItem != null) {
            telnetListPageItem.clear();
            recycleItem(telnetListPageItem);
            b++;
            continue;
          } 
        } 
        telnetListPageBlock.clear();
        recycleBlock(telnetListPageBlock);
        return;
      } 
    } 
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
  
  public boolean areAllItemsEnabled() {
    return false;
  }
  
  public void cleanAllItem() {
    synchronized (this._block_list) {
      HashSet hashSet = new HashSet();
      this((Collection)this._block_list.keySet());
      Iterator<Integer> iterator = hashSet.iterator();
      while (iterator.hasNext())
        removeBlock(iterator.next()); 
    } 
    this._block_list.clear();
    /* monitor exit ClassFileLocalVariableReferenceExpression{type=ObjectType{java/lang/Object}, name=SYNTHETIC_LOCAL_VARIABLE_1} */
  }
  
  public void cleanCommand() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield _operation_command_stack : Ljava/util/Vector;
    //   6: invokevirtual clear : ()V
    //   9: aload_0
    //   10: getfield _load_command_stack : Ljava/util/Stack;
    //   13: invokevirtual clear : ()V
    //   16: aload_0
    //   17: aconst_null
    //   18: putfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   21: aload_0
    //   22: invokespecial cleanRefreshCommand : ()V
    //   25: aload_0
    //   26: invokespecial cleanPreloadCommand : ()V
    //   29: aload_0
    //   30: monitorexit
    //   31: return
    //   32: astore_1
    //   33: aload_0
    //   34: monitorexit
    //   35: aload_1
    //   36: athrow
    // Exception table:
    //   from	to	target	type
    //   2	29	32	finally
  }
  
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
  
  public void executeCommand() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   6: astore_1
    //   7: aload_1
    //   8: ifnull -> 14
    //   11: aload_0
    //   12: monitorexit
    //   13: return
    //   14: aload_0
    //   15: aload_0
    //   16: invokevirtual popCommand : ()Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   19: putfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   22: aload_0
    //   23: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   26: ifnull -> 11
    //   29: aload_0
    //   30: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   33: getfield recordTime : Z
    //   36: ifeq -> 46
    //   39: aload_0
    //   40: invokestatic currentTimeMillis : ()J
    //   43: putfield _last_load_time : J
    //   46: aload_0
    //   47: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   50: aload_0
    //   51: invokevirtual execute : (Lcom/kumi/Bahamut/ListPage/TelnetListPage;)V
    //   54: aload_0
    //   55: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   58: invokevirtual isDone : ()Z
    //   61: ifeq -> 11
    //   64: aload_0
    //   65: aconst_null
    //   66: putfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   69: aload_0
    //   70: invokevirtual executeCommand : ()V
    //   73: goto -> 11
    //   76: astore_1
    //   77: aload_0
    //   78: monitorexit
    //   79: aload_1
    //   80: athrow
    // Exception table:
    //   from	to	target	type
    //   2	7	76	finally
    //   14	46	76	finally
    //   46	73	76	finally
  }
  
  public void executeCommandFinished(TelnetListPageBlock paramTelnetListPageBlock) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   6: ifnull -> 41
    //   9: aload_0
    //   10: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   13: aload_0
    //   14: aload_1
    //   15: invokevirtual executeFinished : (Lcom/kumi/Bahamut/ListPage/TelnetListPage;Lcom/kumi/Bahamut/ListPage/TelnetListPageBlock;)V
    //   18: aload_0
    //   19: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   22: invokevirtual isDone : ()Z
    //   25: ifne -> 36
    //   28: aload_0
    //   29: aload_0
    //   30: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   33: invokevirtual repushCommand : (Lcom/kumi/Bahamut/Command/TelnetCommand;)V
    //   36: aload_0
    //   37: aconst_null
    //   38: putfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   41: aload_0
    //   42: monitorexit
    //   43: return
    //   44: astore_1
    //   45: aload_0
    //   46: monitorexit
    //   47: aload_1
    //   48: athrow
    // Exception table:
    //   from	to	target	type
    //   2	36	44	finally
    //   36	41	44	finally
  }
  
  public TelnetListPageBlock getBlock(int paramInt) {
    return this._block_list.get(Integer.valueOf(paramInt));
  }
  
  public int getBlockIndex(int paramInt) {
    return paramInt / 20;
  }
  
  public int getBlockSize() {
    return this._block_list.size();
  }
  
  public int getCount() {
    synchronized (this._list_count) {
      return this._list_count.intValue();
    } 
  }
  
  public int getCurrentBlock() {
    return this._current_block;
  }
  
  public int getFirstVisibleBlockIndex() {
    int i = -1;
    if (this._list_view != null)
      i = getBlockIndex(this._list_view.getFirstVisiblePosition()); 
    return i;
  }
  
  public int getIndexInBlock(int paramInt) {
    return paramInt % 20;
  }
  
  public TelnetListPageItem getItem(int paramInt) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aconst_null
    //   3: astore_2
    //   4: aload_0
    //   5: getfield _block_list : Ljava/util/Map;
    //   8: astore_3
    //   9: aload_3
    //   10: monitorenter
    //   11: aload_0
    //   12: aload_0
    //   13: iload_1
    //   14: invokevirtual getBlockIndex : (I)I
    //   17: invokevirtual getBlock : (I)Lcom/kumi/Bahamut/ListPage/TelnetListPageBlock;
    //   20: astore #4
    //   22: aload #4
    //   24: ifnull -> 38
    //   27: aload #4
    //   29: aload_0
    //   30: iload_1
    //   31: invokevirtual getIndexInBlock : (I)I
    //   34: invokevirtual getItem : (I)Lcom/kumi/Bahamut/ListPage/TelnetListPageItem;
    //   37: astore_2
    //   38: aload_3
    //   39: monitorexit
    //   40: aload_2
    //   41: ifnull -> 57
    //   44: aload_2
    //   45: iload_1
    //   46: iconst_1
    //   47: iadd
    //   48: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   51: invokevirtual intValue : ()I
    //   54: putfield Number : I
    //   57: aload_0
    //   58: monitorexit
    //   59: aload_2
    //   60: areturn
    //   61: astore_2
    //   62: aload_3
    //   63: monitorexit
    //   64: aload_2
    //   65: athrow
    //   66: astore_2
    //   67: aload_0
    //   68: monitorexit
    //   69: aload_2
    //   70: athrow
    // Exception table:
    //   from	to	target	type
    //   4	11	66	finally
    //   11	22	61	finally
    //   27	38	61	finally
    //   38	40	61	finally
    //   44	57	66	finally
    //   62	64	61	finally
    //   64	66	66	finally
  }
  
  public long getItemId(int paramInt) {
    return (paramInt + 1);
  }
  
  public int getItemSize() {
    return this._item_size;
  }
  
  public int getItemViewType(int paramInt) {
    return 0;
  }
  
  public int getLastLoadItemIndex() {
    return this._last_load_item_index;
  }
  
  public int getLastVisibleBlockIndex() {
    int i = -1;
    if (this._list_view != null)
      i = getBlockIndex(this._list_view.getLastVisiblePosition()); 
    return i;
  }
  
  public String getListId() {
    return getListIdFromListName(getListName());
  }
  
  public String getListIdFromListName(String paramString) {
    return paramString;
  }
  
  public String getListName() {
    return this._list_name;
  }
  
  public int getListType() {
    return 0;
  }
  
  public ListView getListView() {
    return this._list_view;
  }
  
  public int getLoadingItemNumber() {
    return getLastLoadItemIndex() + 1;
  }
  
  public int getPageLayout() {
    return 0;
  }
  
  public int getPageType() {
    return 0;
  }
  
  public int getSelectedIndex() {
    return this._selected_index;
  }
  
  public abstract View getView(int paramInt, View paramView, ViewGroup paramViewGroup);
  
  public int getViewTypeCount() {
    return 1;
  }
  
  public boolean hasStableIds() {
    return false;
  }
  
  public abstract boolean isAutoLoadEnable();
  
  public boolean isEmpty() {
    return (getCount() == 0);
  }
  
  public boolean isEnabled(int paramInt) {
    return true;
  }
  
  public boolean isItemBlockEnable() {
    return false;
  }
  
  public boolean isItemBlocked(TelnetListPageItem paramTelnetListPageItem) {
    return false;
  }
  
  public boolean isItemCanLoadAtIndex(int paramInt) {
    return true;
  }
  
  public boolean isItemLoadingByIndex(int paramInt) {
    return (paramInt == getLastLoadItemIndex());
  }
  
  public boolean isItemLoadingByNumber(int paramInt) {
    return (paramInt == getLoadingItemNumber());
  }
  
  public boolean isLoadingBlock(int paramInt) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: iconst_0
    //   3: istore_3
    //   4: iload_3
    //   5: istore_2
    //   6: aload_0
    //   7: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   10: ifnull -> 37
    //   13: iload_3
    //   14: istore_2
    //   15: aload_0
    //   16: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   19: getfield Action : I
    //   22: ifne -> 37
    //   25: aload_0
    //   26: getfield _executing_command : Lcom/kumi/Bahamut/Command/TelnetCommand;
    //   29: checkcast com/kumi/Bahamut/Command/BahamutCommandLoadBlock
    //   32: iload_1
    //   33: invokevirtual containsArticle : (I)Z
    //   36: istore_2
    //   37: iload_2
    //   38: istore_3
    //   39: iload_2
    //   40: ifne -> 105
    //   43: aload_0
    //   44: getfield _load_command_stack : Ljava/util/Stack;
    //   47: invokevirtual iterator : ()Ljava/util/Iterator;
    //   50: astore #4
    //   52: iload_2
    //   53: istore_3
    //   54: aload #4
    //   56: invokeinterface hasNext : ()Z
    //   61: ifeq -> 105
    //   64: aload #4
    //   66: invokeinterface next : ()Ljava/lang/Object;
    //   71: checkcast com/kumi/Bahamut/Command/TelnetCommand
    //   74: astore #5
    //   76: aload #5
    //   78: ifnull -> 52
    //   81: aload #5
    //   83: getfield Action : I
    //   86: ifne -> 52
    //   89: aload #5
    //   91: checkcast com/kumi/Bahamut/Command/BahamutCommandLoadBlock
    //   94: iload_1
    //   95: invokevirtual containsArticle : (I)Z
    //   98: istore_3
    //   99: iload_3
    //   100: ifeq -> 52
    //   103: iconst_1
    //   104: istore_3
    //   105: aload_0
    //   106: monitorexit
    //   107: iload_3
    //   108: ireturn
    //   109: astore #4
    //   111: aload_0
    //   112: monitorexit
    //   113: aload #4
    //   115: athrow
    // Exception table:
    //   from	to	target	type
    //   6	13	109	finally
    //   15	37	109	finally
    //   43	52	109	finally
    //   54	76	109	finally
    //   81	99	109	finally
  }
  
  public boolean isLoadingSize() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: iconst_0
    //   3: istore_3
    //   4: aload_0
    //   5: getfield _operation_command_stack : Ljava/util/Vector;
    //   8: invokevirtual iterator : ()Ljava/util/Iterator;
    //   11: astore #4
    //   13: iload_3
    //   14: istore_2
    //   15: aload #4
    //   17: invokeinterface hasNext : ()Z
    //   22: ifeq -> 46
    //   25: aload #4
    //   27: invokeinterface next : ()Ljava/lang/Object;
    //   32: checkcast com/kumi/Bahamut/Command/TelnetCommand
    //   35: getfield Action : I
    //   38: istore_1
    //   39: iload_1
    //   40: iconst_2
    //   41: if_icmpne -> 13
    //   44: iconst_1
    //   45: istore_2
    //   46: aload_0
    //   47: monitorexit
    //   48: iload_2
    //   49: ireturn
    //   50: astore #4
    //   52: aload_0
    //   53: monitorexit
    //   54: aload #4
    //   56: athrow
    // Exception table:
    //   from	to	target	type
    //   4	13	50	finally
    //   15	39	50	finally
  }
  
  public void loadBoardBlock(int paramInt) {
    pushCommand((TelnetCommand)new BahamutCommandLoadBlock(paramInt));
  }
  
  public void loadItemAtIndex(int paramInt) {
    if (isItemCanLoadAtIndex(paramInt)) {
      this._last_load_item_index = paramInt;
      pushCommand((TelnetCommand)new BahamutCommandLoadArticle(paramInt + 1));
    } 
  }
  
  public void loadItemAtNumber(int paramInt) {
    loadItemAtIndex(paramInt - 1);
  }
  
  public void loadLastBlock() {
    loadLastBlock(true);
  }
  
  public void loadLastBlock(boolean paramBoolean) {
    if (!containsLoadLastBlock()) {
      BahamutCommandLoadLastBlock bahamutCommandLoadLastBlock = new BahamutCommandLoadLastBlock();
      bahamutCommandLoadLastBlock.recordTime = paramBoolean;
      pushCommand((TelnetCommand)bahamutCommandLoadLastBlock);
      executeCommand();
    } 
  }
  
  protected void loadListState() {
    if (this._list_view != null) {
      ListState listState = ListStateStore.getInstance().getState(getListId());
      setListViewSelectionFromTop(listState.Position, listState.Top);
    } 
  }
  
  public abstract TelnetListPageBlock loadPage();
  
  public void moveToFirstPosition() {
    setListViewSelection(0);
  }
  
  public void moveToLastPosition() {
    pushCommand((TelnetCommand)new BahamutCommandMoveToLastBlock());
  }
  
  public void notifyDataSetChanged() {
    this.mDataSetObservable.notifyChanged();
  }
  
  public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
    loadItemAtIndex(paramInt);
  }
  
  public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
    return onListViewItemLongClicked(paramView, paramInt);
  }
  
  protected boolean onListViewItemLongClicked(View paramView, int paramInt) {
    return false;
  }
  
  public void onLoadItemFinished() {
    ASProcessingDialog.hideProcessingDialog();
  }
  
  public void onLoadItemStart() {
    ASProcessingDialog.showProcessingDialog("讀取中");
  }
  
  public void onPageDidDisappear() {
    saveListState();
  }
  
  public void onPageDidLoad() {
    this._load_command_stack.setSize(2);
  }
  
  public void onPageDidRemoveFromNavigationController() {
    this._initialed = false;
    cleanAllItem();
    stopAutoLoad();
  }
  
  public void onPageDidUnload() {
    stopAutoLoad();
    super.onPageDidUnload();
  }
  
  public boolean onPagePreload() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: invokevirtual loadPage : ()Lcom/kumi/Bahamut/ListPage/TelnetListPageBlock;
    //   6: astore_1
    //   7: aload_0
    //   8: getfield _initialed : Z
    //   11: ifne -> 24
    //   14: aload_0
    //   15: iconst_0
    //   16: invokevirtual pushPreloadCommand : (I)V
    //   19: aload_0
    //   20: iconst_1
    //   21: putfield _initialed : Z
    //   24: aload_0
    //   25: aload_1
    //   26: invokevirtual executeCommandFinished : (Lcom/kumi/Bahamut/ListPage/TelnetListPageBlock;)V
    //   29: aload_0
    //   30: aload_1
    //   31: invokespecial insertPageData : (Lcom/kumi/Bahamut/ListPage/TelnetListPageBlock;)V
    //   34: aload_0
    //   35: invokespecial executePreloadCommand : ()V
    //   38: aload_0
    //   39: invokevirtual executeCommand : ()V
    //   42: aload_0
    //   43: monitorexit
    //   44: iconst_1
    //   45: ireturn
    //   46: astore_1
    //   47: aload_0
    //   48: monitorexit
    //   49: aload_1
    //   50: athrow
    // Exception table:
    //   from	to	target	type
    //   2	24	46	finally
    //   24	42	46	finally
  }
  
  public void onPageRefresh() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield _list_count : Ljava/lang/Integer;
    //   6: astore_2
    //   7: aload_2
    //   8: monitorenter
    //   9: aload_0
    //   10: aload_0
    //   11: getfield _item_size : I
    //   14: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   17: putfield _list_count : Ljava/lang/Integer;
    //   20: aload_0
    //   21: invokevirtual reloadListView : ()V
    //   24: aload_2
    //   25: monitorexit
    //   26: aload_0
    //   27: invokespecial executeRefreshCommand : ()V
    //   30: aload_0
    //   31: monitorexit
    //   32: return
    //   33: astore_1
    //   34: aload_2
    //   35: monitorexit
    //   36: aload_1
    //   37: athrow
    //   38: astore_1
    //   39: aload_0
    //   40: monitorexit
    //   41: aload_1
    //   42: athrow
    // Exception table:
    //   from	to	target	type
    //   2	9	38	finally
    //   9	26	33	finally
    //   26	30	38	finally
    //   34	36	33	finally
    //   36	38	38	finally
  }
  
  public void onPageWillAppear() {
    loadListState();
    startAutoLoad();
  }
  
  public void onPageWillDisappear() {
    stopAutoLoad();
  }
  
  public TelnetCommand popCommand() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aconst_null
    //   3: astore_1
    //   4: aload_0
    //   5: getfield _operation_command_stack : Ljava/util/Vector;
    //   8: invokevirtual size : ()I
    //   11: ifle -> 30
    //   14: aload_0
    //   15: getfield _operation_command_stack : Ljava/util/Vector;
    //   18: iconst_0
    //   19: invokevirtual remove : (I)Ljava/lang/Object;
    //   22: checkcast com/kumi/Bahamut/Command/TelnetCommand
    //   25: astore_1
    //   26: aload_0
    //   27: monitorexit
    //   28: aload_1
    //   29: areturn
    //   30: aload_0
    //   31: getfield _load_command_stack : Ljava/util/Stack;
    //   34: invokevirtual isEmpty : ()Z
    //   37: ifne -> 26
    //   40: aload_0
    //   41: getfield _load_command_stack : Ljava/util/Stack;
    //   44: invokevirtual pop : ()Ljava/lang/Object;
    //   47: checkcast com/kumi/Bahamut/Command/TelnetCommand
    //   50: astore_1
    //   51: goto -> 26
    //   54: astore_1
    //   55: aload_0
    //   56: monitorexit
    //   57: aload_1
    //   58: athrow
    // Exception table:
    //   from	to	target	type
    //   4	26	54	finally
    //   30	51	54	finally
  }
  
  public void pushCommand(TelnetCommand paramTelnetCommand) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: aload_1
    //   4: iconst_1
    //   5: invokevirtual pushCommand : (Lcom/kumi/Bahamut/Command/TelnetCommand;Z)V
    //   8: aload_0
    //   9: monitorexit
    //   10: return
    //   11: astore_1
    //   12: aload_0
    //   13: monitorexit
    //   14: aload_1
    //   15: athrow
    // Exception table:
    //   from	to	target	type
    //   2	8	11	finally
  }
  
  public void pushCommand(TelnetCommand paramTelnetCommand, boolean paramBoolean) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_1
    //   3: ifnull -> 22
    //   6: aload_1
    //   7: invokevirtual isOperationCommand : ()Z
    //   10: ifne -> 33
    //   13: aload_0
    //   14: getfield _load_command_stack : Ljava/util/Stack;
    //   17: aload_1
    //   18: invokevirtual push : (Ljava/lang/Object;)Ljava/lang/Object;
    //   21: pop
    //   22: iload_2
    //   23: ifeq -> 30
    //   26: aload_0
    //   27: invokevirtual executeCommand : ()V
    //   30: aload_0
    //   31: monitorexit
    //   32: return
    //   33: aload_0
    //   34: getfield _operation_command_stack : Ljava/util/Vector;
    //   37: aload_1
    //   38: invokevirtual add : (Ljava/lang/Object;)Z
    //   41: pop
    //   42: goto -> 22
    //   45: astore_1
    //   46: aload_0
    //   47: monitorexit
    //   48: aload_1
    //   49: athrow
    // Exception table:
    //   from	to	target	type
    //   6	22	45	finally
    //   26	30	45	finally
    //   33	42	45	finally
  }
  
  public void pushPreloadCommand(int paramInt) {
    this._page_preload_command[paramInt] = true;
  }
  
  public void pushRefreshCommand(int paramInt) {
    this._page_refresh_command[paramInt] = true;
  }
  
  public abstract void recycleBlock(TelnetListPageBlock paramTelnetListPageBlock);
  
  public abstract void recycleItem(TelnetListPageItem paramTelnetListPageItem);
  
  public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.registerObserver(paramDataSetObserver);
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
  
  public void repushCommand(TelnetCommand paramTelnetCommand) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_1
    //   3: ifnull -> 22
    //   6: aload_1
    //   7: invokevirtual isOperationCommand : ()Z
    //   10: ifne -> 25
    //   13: aload_0
    //   14: getfield _load_command_stack : Ljava/util/Stack;
    //   17: aload_1
    //   18: invokevirtual push : (Ljava/lang/Object;)Ljava/lang/Object;
    //   21: pop
    //   22: aload_0
    //   23: monitorexit
    //   24: return
    //   25: aload_0
    //   26: getfield _operation_command_stack : Ljava/util/Vector;
    //   29: aload_1
    //   30: iconst_0
    //   31: invokevirtual insertElementAt : (Ljava/lang/Object;I)V
    //   34: goto -> 22
    //   37: astore_1
    //   38: aload_0
    //   39: monitorexit
    //   40: aload_1
    //   41: athrow
    // Exception table:
    //   from	to	target	type
    //   6	22	37	finally
    //   25	34	37	finally
  }
  
  protected void saveListState() {
    if (this._list_view != null) {
      ListState listState = ListStateStore.getInstance().getState(getListId());
      listState.Position = this._list_view.getFirstVisiblePosition();
      View view = this._list_view.getChildAt(0);
      if (view != null)
        listState.Top = view.getTop(); 
    } 
  }
  
  public void setBlock(int paramInt, TelnetListPageBlock paramTelnetListPageBlock) {
    this._block_list.put(Integer.valueOf(paramInt), paramTelnetListPageBlock);
  }
  
  public void setItemSize(int paramInt) {
    this._item_size = paramInt;
  }
  
  public void setListName(String paramString) {
    this._list_name = paramString;
  }
  
  public void setListView(ListView paramListView) {
    this._list_view = paramListView;
    if (this._list_view != null) {
      this._list_view.setOnItemClickListener(this);
      this._list_view.setOnItemLongClickListener(this);
      this._list_view.setAdapter(this);
    } 
  }
  
  public void setListViewSelection(final int selection) {
    (new ASRunner() {
        final TelnetListPage this$0;
        
        final int val$selection;
        
        public void run() {
          if (TelnetListPage.this._list_view != null) {
            if (selection == -1) {
              TelnetListPage.this._list_view.setSelection(TelnetListPage.this.getCount() - 1);
              return;
            } 
          } else {
            return;
          } 
          TelnetListPage.this._list_view.setSelection(selection);
        }
      }).runInMainThread();
  }
  
  public void setListViewSelectionFromTop(int paramInt1, int paramInt2) {
    if (this._list_view != null) {
      if (paramInt1 == -1) {
        this._list_view.setSelection(getCount() - 1);
        return;
      } 
    } else {
      return;
    } 
    this._list_view.setSelectionFromTop(paramInt1, paramInt2);
  }
  
  public void setManualLoadPage() {
    this._manual_load_page = true;
  }
  
  public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.unregisterObserver(paramDataSetObserver);
  }
  
  private class AutoLoadThread extends Thread {
    public boolean run = true;
    
    final TelnetListPage this$0;
    
    private AutoLoadThread() {}
    
    public void run() {
      try {
        sleep(10000L);
        while (true) {
          if (this.run) {
            boolean bool;
            long l1 = System.currentTimeMillis();
            long l2 = l1 - TelnetListPage.this._last_load_time;
            long l3 = l1 - TelnetListPage.this._last_send_time;
            if (l2 > 900000L) {
              if (l3 > 60000L) {
                bool = true;
              } else {
                bool = false;
              } 
            } else if (l2 > 180000L) {
              if (l3 > 30000L) {
                bool = true;
              } else {
                bool = false;
              } 
            } else if (l2 > 10000L && l2 > l3) {
              bool = true;
            } else {
              bool = false;
            } 
            if ((bool || TelnetListPage.this._manual_load_page) && this.run) {
              TelnetListPage.this.loadLastBlock(false);
              TelnetListPage.access$102(TelnetListPage.this, l1);
            } 
            TelnetListPage.access$202(TelnetListPage.this, false);
            sleep(1000L);
            continue;
          } 
          return;
        } 
      } catch (Exception exception) {
        exception.printStackTrace();
        this.run = false;
      } 
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\ListPage\TelnetListPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */