package com.kumi.Bahamut.Pages;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.DataModels.Bookmark;
import com.kumi.Bahamut.DataModels.BookmarkList;
import com.kumi.Bahamut.DataModels.BookmarkStore;
import com.kumi.Bahamut.ListPage.ListState;
import com.kumi.Bahamut.ListPage.ListStateStore;
import com.kumi.Bahamut.PageContainer;
import com.kumi.TelnetUI.TelnetHeaderItemView;
import com.kumi.TelnetUI.TelnetPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class BoardExtendOptionalPage extends TelnetPage implements ListAdapter, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {
  public static final int MODE_BOOKMARK = 0;
  
  public static final int MODE_HISTORY = 1;
  
  public static final int MODE_WATER_BALL = 2;
  
  private String _board_name = null;
  
  private Button _bookmark_button = null;
  
  protected TelnetHeaderItemView _header_view = null;
  
  private Button _history_button = null;
  
  private List<Bookmark> _list = new ArrayList<Bookmark>();
  
  private View _list_empty_view = null;
  
  private ListView _list_view = null;
  
  private BoardExtendOptionalPageListener _listener = null;
  
  private int _mode = 0;
  
  private Button _selected_button = null;
  
  private Button[] _tab_buttons = null;
  
  private Button _water_ball_button = null;
  
  private final DataSetObservable mDataSetObservable = new DataSetObservable();
  
  public BoardExtendOptionalPage(String paramString, BoardExtendOptionalPageListener paramBoardExtendOptionalPageListener) {
    this._listener = paramBoardExtendOptionalPageListener;
    setBoardName(paramString);
  }
  
  private void reloadList() {
    Context context = getContext();
    if (context != null) {
      BookmarkList bookmarkList = (new BookmarkStore(context)).getBookmarkList(this._board_name);
      if (this._mode == 1) {
        bookmarkList.loadHistoryList(this._list);
        return;
      } 
      bookmarkList.loadTitleList(this._list);
    } 
  }
  
  public boolean areAllItemsEnabled() {
    return true;
  }
  
  public View getBookmarkView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    BoardExtendOptionalPageBookmarkItemView boardExtendOptionalPageBookmarkItemView2;
    View view = paramView;
    if (paramView == null)
      boardExtendOptionalPageBookmarkItemView2 = new BoardExtendOptionalPageBookmarkItemView(getContext()); 
    boardExtendOptionalPageBookmarkItemView2.setBookmark(getItem(paramInt));
    BoardExtendOptionalPageBookmarkItemView boardExtendOptionalPageBookmarkItemView1 = boardExtendOptionalPageBookmarkItemView2;
    if (paramInt == 0) {
      boolean bool1 = true;
      boardExtendOptionalPageBookmarkItemView1.setDividerTopVisible(bool1);
      return (View)boardExtendOptionalPageBookmarkItemView2;
    } 
    boolean bool = false;
    boardExtendOptionalPageBookmarkItemView1.setDividerTopVisible(bool);
    return (View)boardExtendOptionalPageBookmarkItemView2;
  }
  
  public int getCount() {
    reloadList();
    return this._list.size();
  }
  
  public View getHistoryView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    BoardExtendOptionalPageHistoryItemView boardExtendOptionalPageHistoryItemView2;
    View view = paramView;
    if (paramView == null)
      boardExtendOptionalPageHistoryItemView2 = new BoardExtendOptionalPageHistoryItemView(getContext()); 
    boardExtendOptionalPageHistoryItemView2.setBookmark(getItem(paramInt));
    BoardExtendOptionalPageHistoryItemView boardExtendOptionalPageHistoryItemView1 = boardExtendOptionalPageHistoryItemView2;
    if (paramInt == 0) {
      boolean bool1 = true;
      boardExtendOptionalPageHistoryItemView1.setDividerTopVisible(bool1);
      return (View)boardExtendOptionalPageHistoryItemView2;
    } 
    boolean bool = false;
    boardExtendOptionalPageHistoryItemView1.setDividerTopVisible(bool);
    return (View)boardExtendOptionalPageHistoryItemView2;
  }
  
  public Bookmark getItem(int paramInt) {
    return this._list.get(paramInt);
  }
  
  public long getItemId(int paramInt) {
    return paramInt;
  }
  
  public int getItemViewType(int paramInt) {
    return this._mode;
  }
  
  public int getPageLayout() {
    return 2131361826;
  }
  
  public int getPageType() {
    return 11;
  }
  
  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    return (this._mode == 1) ? getHistoryView(paramInt, paramView, paramViewGroup) : getBookmarkView(paramInt, paramView, paramViewGroup);
  }
  
  public int getViewTypeCount() {
    return 3;
  }
  
  public boolean hasStableIds() {
    return false;
  }
  
  public boolean isEmpty() {
    return (getCount() == 0);
  }
  
  public boolean isEnabled(int paramInt) {
    return true;
  }
  
  public void notifyDataSetChanged() {
    this.mDataSetObservable.notifyChanged();
  }
  
  public void onClick(View paramView) {
    byte b = 0;
    if (paramView != this._selected_button) {
      if (paramView == this._bookmark_button) {
        this._header_view.setTitle("我的書籤");
        this._mode = 0;
      } else if (paramView == this._water_ball_button) {
        this._header_view.setTitle("水球紀錄");
        this._mode = 2;
      } else if (paramView == this._history_button) {
        this._header_view.setTitle("瀏覽紀錄");
        this._mode = 1;
      } 
      this._selected_button = (Button)paramView;
      Button[] arrayOfButton = this._tab_buttons;
      int i = arrayOfButton.length;
      while (b < i) {
        Button button = arrayOfButton[b];
        if (button == this._selected_button) {
          button.setTextColor(getResource().getColorStateList(2131034245));
          button.setBackgroundResource(2131034243);
        } else {
          button.setTextColor(getResource().getColorStateList(2131034246));
          button.setBackgroundResource(2131034244);
        } 
        b++;
      } 
    } 
    notifyDataSetChanged();
  }
  
  public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
    Bookmark bookmark = getItem(paramInt);
    BoardSearchPage boardSearchPage = PageContainer.getInstance().getBoard_Search_Page();
    boardSearchPage.clear();
    ListState listState = ListStateStore.getInstance().getState(boardSearchPage.getListIdFromListName(this._board_name));
    if (listState != null) {
      listState.Top = 0;
      listState.Position = 0;
    } 
    boardSearchPage.setKeyword(bookmark.getKeyword());
    boardSearchPage.setAuthor(bookmark.getAuthor());
    boardSearchPage.setMark(bookmark.getMark());
    boardSearchPage.setGy(bookmark.getGy());
    Vector<BoardSearchPage> vector = getNavigationController().getViewControllers();
    vector.remove(vector.size() - 1);
    vector.add(boardSearchPage);
    getNavigationController().setViewControllers(vector, true);
    if (this._listener != null && bookmark != null)
      this._listener.onBoardExtendOptionalPageDidSelectBookmark(bookmark); 
  }
  
  public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, final int bookmark_index, long paramLong) {
    if (this._mode != 0)
      return false; 
    Bookmark bookmark = getItem(bookmark_index);
    ASAlertDialog.createDialog().setTitle("刪除書籤").setMessage("是否確定要刪除此書籤\"" + bookmark.getTitle() + "\"?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() {
          final BoardExtendOptionalPage this$0;
          
          final int val$bookmark_index;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            if (param1Int == 1) {
              BookmarkStore bookmarkStore = new BookmarkStore(BoardExtendOptionalPage.this.getContext());
              bookmarkStore.getBookmarkList(BoardExtendOptionalPage.this._board_name).removeBookmark(bookmark_index);
              bookmarkStore.store();
              BoardExtendOptionalPage.this.notifyDataSetChanged();
            } 
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
    return true;
  }
  
  public void onPageDidLoad() {
    super.onPageDidLoad();
    this._header_view = (TelnetHeaderItemView)findViewById(2131230805);
    this._header_view.setData("我的書籤", this._board_name, "");
    this._list_empty_view = findViewById(2131230814);
    this._list_view = (ListView)findViewById(2131230815);
    this._list_view.setAdapter(this);
    this._list_view.setOnItemClickListener(this);
    this._list_view.setOnItemLongClickListener(this);
    this._list_view.setEmptyView(this._list_empty_view);
    this._bookmark_button = (Button)findViewById(2131230792);
    this._history_button = (Button)findViewById(2131230806);
    this._water_ball_button = (Button)findViewById(2131230816);
    this._bookmark_button.setOnClickListener(this);
    this._history_button.setOnClickListener(this);
    this._water_ball_button.setOnClickListener(this);
    this._selected_button = this._bookmark_button;
    this._tab_buttons = new Button[] { this._bookmark_button, this._history_button, this._water_ball_button };
  }
  
  public boolean onReceivedGestureRight() {
    onBackPressed();
    ASToast.showShortToast("返回");
    return true;
  }
  
  public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.registerObserver(paramDataSetObserver);
  }
  
  public void setBoardName(String paramString) {
    this._board_name = paramString;
    notifyDataSetChanged();
  }
  
  public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.unregisterObserver(paramDataSetObserver);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BoardExtendOptionalPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */