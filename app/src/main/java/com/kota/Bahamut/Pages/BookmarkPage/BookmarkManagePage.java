package com.kota.Bahamut.Pages.BookmarkPage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;
import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.DataModels.BookmarkList;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.Dialogs.Dialog_SearchArticle;
import com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener;
import com.kota.Bahamut.ListPage.ListState;
import com.kota.Bahamut.ListPage.ListStateStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.BoardPage.BoardSearchPage;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.TelnetUI.TelnetHeaderItemView;
import com.kota.TelnetUI.TelnetPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class BookmarkManagePage extends TelnetPage implements BookmarkClickListener, Dialog_SearchArticle_Listener {
    public String _board_name = null;
    private final List<Bookmark> _bookmarks = new ArrayList<>();
    protected TelnetHeaderItemView _header_view = null;
    private Button _selected_button;
    private Button _bookmark_button;
    private Button _history_button;
    private Button _water_ball_button;
    private Button[] _tab_buttons;
    private int _mode = 0;
    private final BoardExtendOptionalPageListener _listener;
    BookmarkAdapter bookmarkAdapter;
    HistoryAdapter historyAdapter;
    BookmarkStore _bookmarkStore = TempSettings.getBookmarkStore();
    private boolean isUnderRecycleView = false;
    private float scale;
    public int getPageLayout() {
        return R.layout.bookmark_manage_page;
    }
    public int getPageType() {
        return BahamutPage.BAHAMUT_BOOKMARK;
    }

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
        public boolean isSwiped = false;
        public boolean isDragged = false;
        int start = -1;
        int end = -1;
        private View dragView;
        // 上下移動
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            start = viewHolder.getAdapterPosition();
            end = target.getAdapterPosition();
            if (BookmarkManagePage.this._mode == 0) {
                if (UserSettings.getPropertiesVIP()) {
                    Collections.swap(_bookmarks, start, end);
                    bookmarkAdapter.notifyItemMoved(start, end);
                } else {
                    ASToast.showShortToast(getContextString(R.string.vip_only_message));
                }
            }
            return true;
        }

        // 左右移動
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int bookmark_index = viewHolder.getAdapterPosition();
            if (BookmarkManagePage.this._mode == 0) {
                if (direction == ItemTouchHelper.LEFT) {
                    // 左滑刪除
                    ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.string.delete)+getContextString(R.string.bookmark))
                            .setMessage(getContextString(R.string.delete_this_bookmark) +"\n\"" + BookmarkManagePage.this.bookmarkAdapter.getItem(bookmark_index).getTitle() + "\"")
                            .addButton(getContextString(R.string.cancel))
                            .addButton(getContextString(R.string.delete))
                            .setListener((aDialog, index1) -> {
                                if (index1 == 1) {
                                    _bookmarkStore.getBookmarkList(BookmarkManagePage.this._board_name).removeBookmark(bookmark_index);
                                    _bookmarkStore.store();
                                    reloadList();
                                    bookmarkAdapter.notifyDataSetChanged();
                                } else {
                                    // 還原
                                    bookmarkAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                }
                            })
                            .scheduleDismissOnPageDisappear(BookmarkManagePage.this).show();
                }

                else if (direction == ItemTouchHelper.RIGHT) {
                    if (UserSettings.getPropertiesVIP()) {
                        // 右滑修改
                        editBookmarkIndex = bookmark_index;
                        showSearchArticleDialog();
                    } else {
                        ASToast.showShortToast(getContextString(R.string.vip_only_message));
                        // 還原
                        bookmarkAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }
            } else if (BookmarkManagePage.this._mode == 1) {
                if (direction == ItemTouchHelper.LEFT) {
                    _bookmarkStore.getBookmarkList(BookmarkManagePage.this._board_name).removeHistoryBookmark(bookmark_index);
                    _bookmarkStore.store();
                    reloadList();
                    historyAdapter.notifyDataSetChanged();
                } else {
                    // 還原
                    historyAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            }
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            isUnderRecycleView = true;
            if (BookmarkManagePage.this._mode == 0) {
                switch (actionState) {
                    case ItemTouchHelper.ACTION_STATE_DRAG:
                        // the user is dragging an item and didn't lift their finger off yet
                        isSwiped = false;
                        isDragged = true;
                        if (viewHolder != null) { // 選取變色
                            dragView = viewHolder.itemView;
                            dragView.setBackgroundResource(R.color.ripple_material);
                        }
                        break;

                    case ItemTouchHelper.ACTION_STATE_SWIPE:
                        // the user is swiping an item and didn't lift their finger off yet
                        isSwiped = true;
                        isDragged = false;
                        break;

                    case ItemTouchHelper.ACTION_STATE_IDLE:
                        // the user just dropped the item (after dragging it), and lift their finger off.
                        //
                        if (isSwiped) { // The user used onSwiped()
                            Log.e("swipe", "swipe is over");
                        }
                        if (!isSwiped && isDragged) { // The user used onMove()
                            if (dragView != null) { // 解除 選取變色
                                dragView.setBackgroundResource(R.color.transparent);
                                dragView = null;
                            }
                            BookmarkList bookmark_list = _bookmarkStore.getBookmarkList(BookmarkManagePage.this._board_name);
                            bookmark_list.clear();
                            for (Bookmark bookmark : _bookmarks) {
                                if (bookmark.index == start)
                                    bookmark.index = end;
                                else if (bookmark.index == end)
                                    bookmark.index = start;
                                bookmark_list.addBookmark(bookmark);
                            }
                            _bookmarkStore.store();
                        }

                        isSwiped = false;
                        isDragged = false;
                        isUnderRecycleView = false;
                }
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            float fontWidth = 18 * scale;
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                float offset = Math.abs(dX);
                int right = viewHolder.itemView.getRight();
                int left = viewHolder.itemView.getLeft();
                int top = viewHolder.itemView.getTop();
                if (dX < 0) {
                    // Draw button at the right edge of the item
                    Paint paint = new Paint();
                    paint.setColor(getContextColor(R.color.tab_item_text_color_selected));
                    paint.setTextSize(fontWidth);
                    // Calculate the top-left corner of the item
                    float x = right - offset;
                    float y = top + fontWidth*2;

                    // Draw the text line by line
                    String[] lines = "◀左滑刪除".split("");
                    for (int i = 0; i < lines.length; i++) {
                        c.drawText(lines[i], x + i * (paint.getTextSize()+10), y, paint);
                    }
                } else {
                    // Draw button at the right edge of the item
                    Paint paint = new Paint();
                    paint.setColor(getContextColor(R.color.tab_item_text_color_selected));
                    paint.setTextSize(fontWidth);
                    // Calculate the top-left corner of the item
                    float x = offset - left - fontWidth;
                    float y = top + fontWidth*2;

                    // Draw the text line by line
                    String[] lines = "▶右滑修改".split("");
                    for (int i = 0; i < lines.length; i++) {
                        c.drawText(lines[i], x - i * (paint.getTextSize()+10), y, paint);
                    }
                }
            }
        }
    });

    public BookmarkManagePage(String aBoardName, BoardExtendOptionalPageListener aListener) {
        _listener = aListener;
        setBoardName(aBoardName);
    }

    @Override
    public void onPageDidLoad() {
        reloadList();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        bookmarkAdapter = new BookmarkAdapter(_bookmarks);
        recyclerView.setAdapter(bookmarkAdapter);
        bookmarkAdapter.setOnItemClickListener(this);

        _header_view = (TelnetHeaderItemView) findViewById(R.id.BoardExtendOptionalPage_headerView);
        _header_view.setData("我的書籤", _board_name, "左滑刪除,右滑修改");
        _bookmark_button = (Button) findViewById(R.id.BoardExtendOptionalPage_bookmarkButton);
        _history_button = (Button) findViewById(R.id.BoardExtendOptionalPage_historyButton);
        _water_ball_button = (Button) findViewById(R.id.BoardExtendOptionalPage_waterBallButton);
        _bookmark_button.setOnClickListener(buttonClickListener);
        _history_button.setOnClickListener(buttonClickListener);
        _water_ball_button.setOnClickListener(buttonClickListener);
        _selected_button = _bookmark_button;
        _tab_buttons = new Button[]{_bookmark_button, _history_button, _water_ball_button};
        scale = getResource().getDisplayMetrics().scaledDensity;
    }

    private void reloadList() {
        Context context = getContext();
        if (context != null) {
            BookmarkList bookmark_list = _bookmarkStore.getBookmarkList(_board_name);
            if (_mode == 1) {
                bookmark_list.loadHistoryList(_bookmarks);
            } else {
                bookmark_list.loadBookmarkList(_bookmarks);
            }
        }
    }

    public void setBoardName(String aBoardName) {
        _board_name = aBoardName;
    }

    View.OnClickListener buttonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View aView) {
            if (aView != BookmarkManagePage.this._selected_button) {
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
                if (aView == BookmarkManagePage.this._bookmark_button) {
                    BookmarkManagePage.this._header_view.setTitle("我的書籤");
                    BookmarkManagePage.this._mode = 0;
                    reloadList();
                    bookmarkAdapter = new BookmarkAdapter(_bookmarks);
                    recyclerView.setAdapter(bookmarkAdapter);
                    bookmarkAdapter.setOnItemClickListener(BookmarkManagePage.this);
                } else if (aView == BookmarkManagePage.this._water_ball_button) {
                    BookmarkManagePage.this._header_view.setTitle("水球紀錄");
                    BookmarkManagePage.this._mode = 2;
                    reloadList();
                } else if (aView == BookmarkManagePage.this._history_button) {
                    BookmarkManagePage.this._header_view.setTitle("瀏覽紀錄");
                    BookmarkManagePage.this._mode = 1;
                    reloadList();
                    historyAdapter = new HistoryAdapter(_bookmarks);
                    recyclerView.setAdapter(historyAdapter);
                    historyAdapter.setOnItemClickListener(BookmarkManagePage.this);
                }
                BookmarkManagePage.this._selected_button = (Button) aView;
                for (Button tab_button : BookmarkManagePage.this._tab_buttons) {
                    if (tab_button == BookmarkManagePage.this._selected_button) {
                        tab_button.setTextColor(getContextColor(R.color.tab_item_text_color_selected));
                        tab_button.setBackgroundResource(R.drawable.tab_item_background_color_selected);
                    } else {
                        tab_button.setTextColor(getContextColor(R.color.tab_item_text_color_unselected));
                        tab_button.setBackgroundResource(R.drawable.tab_item_background_color_unselected);
                    }
                }
            }
        }
    };

    // 書籤管理->按下書籤
    // 實際動作由看板頁去實作
    @Override
    public void onItemClick(View view, int position) {
        Bookmark bookmark = BookmarkManagePage.this.bookmarkAdapter.getItem(position);
        BoardSearchPage page = PageContainer.getInstance().getBoardSearchPage();
        page.clear();
        ListState state = ListStateStore.getInstance().getState(page.getListIdFromListName(BookmarkManagePage.this._board_name));
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
        if (BookmarkManagePage.this._listener != null) {
            BookmarkManagePage.this._listener.onBoardExtendOptionalPageDidSelectBookmark(bookmark);
        }
    }

    @Override
    public boolean onReceivedGestureRight() {
        if (!isUnderRecycleView) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private int editBookmarkIndex = -1;
    // 修改書籤
    private void showSearchArticleDialog() {
        if (editBookmarkIndex>-1) {
            Bookmark bookmark = bookmarkAdapter.getItem(editBookmarkIndex);
            Vector<String> search_options = new Vector<>();
            search_options.add(bookmark.getKeyword());
            search_options.add(bookmark.getAuthor());
            search_options.add(bookmark.getMark());
            search_options.add(bookmark.getGy());
            Dialog_SearchArticle dialog_SearchArticle = new Dialog_SearchArticle();
            dialog_SearchArticle.setListener(this);
            dialog_SearchArticle.editContent(search_options);
            dialog_SearchArticle.show();
        }
    }

    // 搜尋文章完畢
    // 此處修改原本書籤內容
    @Override
    public void onSearchDialogSearchButtonClickedWithValues(Vector<String> vector) {
        Bookmark bookmark = bookmarkAdapter.getItem(editBookmarkIndex);
        bookmark.setKeyword(vector.get(0));
        bookmark.setAuthor(vector.get(1));
        if (vector.get(2).equals("YES"))
            bookmark.setMark("y");
        else
            bookmark.setMark("n");
        bookmark.setGy(vector.get(3));
        bookmark.setTitle(bookmark.generateTitle());
        _bookmarkStore.getBookmarkList(_board_name).updateBookmark(editBookmarkIndex, bookmark);
        _bookmarkStore.store();
        reloadList();
        // 還原
        bookmarkAdapter.notifyItemChanged(editBookmarkIndex);
        editBookmarkIndex = -1;
    }

    @Override
    public void onSearchDialogCancelButtonClicked() {
        // 還原
        bookmarkAdapter.notifyItemChanged(editBookmarkIndex);
        editBookmarkIndex = -1;
    }
}
