package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.DataModels.BookmarkList;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.ListPage.ListState;
import com.kota.Bahamut.ListPage.ListStateStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.TelnetUI.TelnetHeaderItemView;
import com.kota.TelnetUI.TelnetPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class BoardExtendOptionalPage extends TelnetPage implements ListAdapter, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {
    public static final int MODE_BOOKMARK = 0;
    public static final int MODE_HISTORY = 1;
    public static final int MODE_WATER_BALL = 2;
    /* access modifiers changed from: private */
    public String _board_name = null;
    private Button _bookmark_button = null;
    protected TelnetHeaderItemView _header_view = null;
    private Button _history_button = null;
    private final List<Bookmark> _list = new ArrayList<>();
    private final BoardExtendOptionalPageListener _listener;
    private int _mode = 0;
    private Button _selected_button = null;
    private Button[] _tab_buttons = null;
    private Button _water_ball_button = null;
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    public BoardExtendOptionalPage(String aBoardName, BoardExtendOptionalPageListener aListener) {
        this._listener = aListener;
        setBoardName(aBoardName);
    }

    public int getPageType() {
        return 11;
    }

    public int getPageLayout() {
        return R.layout.board_extend_optional_page;
    }

    public void onPageDidLoad() {
        super.onPageDidLoad();
        this._header_view = (TelnetHeaderItemView) findViewById(R.id.BoardExtendOptionalPage_headerView);
        this._header_view.setData("我的書籤", this._board_name, "");
        View _list_empty_view = findViewById(R.id.BoardExtendOptionalPage_listEmptyView);
        ListView _list_view = (ListView) findViewById(R.id.BoardExtendOptionalPage_listView);
        _list_view.setAdapter(this);
        _list_view.setOnItemClickListener(this);
        _list_view.setOnItemLongClickListener(this);
        _list_view.setEmptyView(_list_empty_view);
        this._bookmark_button = (Button) findViewById(R.id.BoardExtendOptionalPage_bookmarkButton);
        this._history_button = (Button) findViewById(R.id.BoardExtendOptionalPage_historyButton);
        this._water_ball_button = (Button) findViewById(R.id.BoardExtendOptionalPage_waterBallButton);
        this._bookmark_button.setOnClickListener(this);
        this._history_button.setOnClickListener(this);
        this._water_ball_button.setOnClickListener(this);
        this._selected_button = this._bookmark_button;
        this._tab_buttons = new Button[]{this._bookmark_button, this._history_button, this._water_ball_button};
    }

    public int getCount() {
        reloadList();
        return this._list.size();
    }

    public Bookmark getItem(int position) {
        return this._list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        return this._mode;
    }

    public View getView(int position, View convertView, ViewGroup parentView) {
        if (this._mode == 1) {
            return getHistoryView(position, convertView, parentView);
        }
        return getBookmarkView(position, convertView, parentView);
    }

    public View getBookmarkView(int position, View convertView, ViewGroup parentView) {
        if (convertView == null) {
            convertView = new BoardExtendOptionalPageBookmarkItemView(getContext());
        }
        ((BoardExtendOptionalPageBookmarkItemView) convertView).setBookmark(getItem(position));
        ((BoardExtendOptionalPageBookmarkItemView) convertView).setDividerTopVisible(position == 0);
        return convertView;
    }

    public View getHistoryView(int position, View convertView, ViewGroup parentView) {
        if (convertView == null) {
            convertView = new BoardExtendOptionalPageHistoryItemView(getContext());
        }
        ((BoardExtendOptionalPageHistoryItemView) convertView).setBookmark(getItem(position));
        ((BoardExtendOptionalPageHistoryItemView) convertView).setDividerTopVisible(position == 0);
        return convertView;
    }

    public int getViewTypeCount() {
        return 3;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;
    }

    public void setBoardName(String aBoardName) {
        this._board_name = aBoardName;
        notifyDataSetChanged();
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View aView, int index, long id) {
        if (this._mode != 0) {
            return false;
        }
        final int bookmark_index = index;
        ASAlertDialog.createDialog().setTitle("刪除書籤").setMessage("是否確定要刪除此書籤\"" + getItem(index).getTitle() + "\"?").addButton("取消").addButton("刪除").setListener((aDialog, index1) -> {
            if (index1 == 1) {
                BookmarkStore store = new BookmarkStore(BoardExtendOptionalPage.this.getContext());
                store.getBookmarkList(BoardExtendOptionalPage.this._board_name).removeBookmark(bookmark_index);
                store.store();
                BoardExtendOptionalPage.this.notifyDataSetChanged();
            }
        }).scheduleDismissOnPageDisappear(this).show();
        return true;
    }

    public void onItemClick(AdapterView<?> adapterView, View aView, int index, long id) {
        Bookmark bookmark = getItem(index);
        BoardSearchPage page = PageContainer.getInstance().getBoardSearchPage();
        page.clear();
        ListState state = ListStateStore.getInstance().getState(page.getListIdFromListName(this._board_name));
        if (state != null) {
            state.Top = 0;
            state.Position = 0;
        }
        page.setKeyword(bookmark.getKeyword());
        page.setAuthor(bookmark.getAuthor());
        page.setMark(bookmark.getMark());
        page.setGy(bookmark.getGy());
        Vector<ASViewController> controllers = getNavigationController().getViewControllers();
        controllers.remove(controllers.size() - 1);
        controllers.add(page);
        getNavigationController().setViewControllers(controllers, true);
        if (this._listener != null) {
            this._listener.onBoardExtendOptionalPageDidSelectBookmark(bookmark);
        }
    }

    public void onClick(View aView) {
        if (aView != this._selected_button) {
            if (aView == this._bookmark_button) {
                this._header_view.setTitle("我的書籤");
                this._mode = 0;
            } else if (aView == this._water_ball_button) {
                this._header_view.setTitle("水球紀錄");
                this._mode = 2;
            } else if (aView == this._history_button) {
                this._header_view.setTitle("瀏覽紀錄");
                this._mode = 1;
            }
            this._selected_button = (Button) aView;
            for (Button tab_button : this._tab_buttons) {
                if (tab_button == this._selected_button) {
                    tab_button.setTextColor(getContextColor(R.color.tab_item_text_color_selected));
                    tab_button.setBackgroundResource(R.drawable.tab_item_background_color_selected);
                } else {
                    tab_button.setTextColor(getContextColor(R.color.tab_item_text_color_unselected));
                    tab_button.setBackgroundResource(R.drawable.tab_item_background_color_unselected);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void reloadList() {
        Context context = getContext();
        if (context != null) {
            BookmarkList bookmark_list = new BookmarkStore(context).getBookmarkList(this._board_name);
            if (this._mode == 1) {
                bookmark_list.loadHistoryList(this._list);
            } else {
                bookmark_list.loadTitleList(this._list);
            }
        }
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        this.mDataSetObservable.notifyChanged();
    }
}
