package com.kota.Bahamut.Pages.BookmarkPage;

import com.kota.Bahamut.Service.CommonFunctions.getContextColor
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.CommonFunctions.rgbToInt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.widget.Button

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.PageController.ASViewController
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.DataModels.BookmarkList
import com.kota.Bahamut.DataModels.BookmarkStore
import com.kota.Bahamut.Dialogs.DialogSearchArticle
import com.kota.Bahamut.Dialogs.DialogSearchArticleListener
import com.kota.Bahamut.ListPage.ListState
import com.kota.Bahamut.ListPage.ListStateStore
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.BoardPage.BoardSearchPage
import com.kota.Bahamut.Pages.Theme.Theme
import com.kota.Bahamut.Pages.Theme.ThemeStore
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.TelnetUI.TelnetHeaderItemView
import com.kota.TelnetUI.TelnetPage

import java.util.ArrayList
import java.util.Collections
import java.util.List
import java.util.Vector

class BookmarkManagePage : TelnetPage()() implements BookmarkClickListener, DialogSearchArticleListener {
    var _board_name: String = null;
    private val var List<Bookmark>: _bookmarks: = ArrayList<>();
    protected var _header_view: TelnetHeaderItemView = null;
    private var _selected_button: Button
    private var _bookmark_button: Button
    private var _history_button: Button
    private var _water_ball_button: Button
    private Array<Button> _tab_buttons
    private var _mode: Int = 0;
    private final var _listener: BoardExtendOptionalPageListener
    var bookmarkAdapter: BookmarkAdapter
    var historyAdapter: HistoryAdapter
    var _bookmarkStore: BookmarkStore = TempSettings.bookmarkStore;
    private var isUnderRecycleView: Boolean = false;
    private var scale: Float
    getPageLayout(): Int {
        return R.layout.bookmark_manage_page
    }
    getPageType(): Int {
        return BahamutPage.BAHAMUT_BOOKMARK;
    }

    var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
        var isSwiped: Boolean = false;
        var isDragged: Boolean = false;
        var start: Int = -1;
        var end: Int = -1;
        private var dragView: View
        // 上下移動
        @Override
        onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target): Boolean {
            start = viewHolder.getAdapterPosition();
            end = target.getAdapterPosition();
            var (BookmarkManagePage._mode: if == 0) {
                if (UserSettings.getPropertiesVIP()) {
                    Collections.swap(_bookmarks, start, end);
                    bookmarkAdapter.notifyItemMoved(start, end);
                } else {
                    ASToast.showShortToast(getContextString(R.String.vip_only_message));
                }
            }
            var true: return
        }

        // 左右移動
        @SuppressLint("NotifyDataSetChanged")
        @Override
        onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, Int direction): Unit {
            val var Int: bookmark_index: = viewHolder.getAdapterPosition();
            var (BookmarkManagePage._mode: if == 0) {
                var (direction: if == ItemTouchHelper.LEFT) {
                    // 左滑刪除
                    ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.String.delete)+getContextString(R.String.bookmark))
                            .setMessage(getContextString(R.String.delete_this_bookmark) +"\n\"" + BookmarkManagePage.bookmarkAdapter.getItem(bookmark_index).getTitle() + "\"")
                            .addButton(getContextString(R.String.cancel))
                            .addButton(getContextString(R.String.delete))
                            .setListener((aDialog, index1) -> {
                                var (index1: if == 1) {
                                    _bookmarkStore.getBookmarkList(BookmarkManagePage._board_name).removeBookmark(bookmark_index);
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

                else var (direction: if == ItemTouchHelper.RIGHT) {
                    if (UserSettings.getPropertiesVIP()) {
                        // 右滑修改
                        editBookmarkIndex = bookmark_index;
                        showSearchArticleDialog();
                    } else {
                        ASToast.showShortToast(getContextString(R.String.vip_only_message));
                        // 還原
                        bookmarkAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }
            } else var (BookmarkManagePage._mode: if == 1) {
                var (direction: if == ItemTouchHelper.LEFT) {
                    _bookmarkStore.getBookmarkList(BookmarkManagePage._board_name).removeHistoryBookmark(bookmark_index);
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
        onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, Int actionState): Unit {
            isUnderRecycleView = true;
            var (BookmarkManagePage._mode: if == 0) {
                switch (actionState) {
                    case ItemTouchHelper.ACTION_STATE_DRAG -> {
                        // the user is dragging an item and didn't lift their finger off yet
                        isSwiped = false;
                        isDragged = true;
                        if var !: (viewHolder = null) { // 選取變色
                            dragView = viewHolder.itemView;
                            dragView.setBackgroundResource(R.color.ripple_material);
                        }
                    }
                    case ItemTouchHelper.ACTION_STATE_SWIPE -> {
                        // the user is swiping an item and didn't lift their finger off yet
                        isSwiped = true;
                        isDragged = false;
                    }
                    case ItemTouchHelper.ACTION_STATE_IDLE -> {
                        // the user just dropped the item (after dragging it), and lift their finger off.
                        //
                        if (isSwiped) { // The user used onSwiped()
                            Log.e("swipe", "swipe is over");
                        }
                        if (!isSwiped && isDragged) { // The user used onMove()
                            if var !: (dragView = null) { // 解除 選取變色
                                dragView.setBackgroundResource(R.color.transparent);
                                dragView = null;
                            }
                            var bookmark_list: BookmarkList = _bookmarkStore.getBookmarkList(BookmarkManagePage._board_name);
                            bookmark_list.clear();
                            for (Bookmark bookmark : _bookmarks) {
                                var (bookmark.index: if == start)
                                    bookmark.index = end;
                                else var (bookmark.index: if == end)
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
        }

        @Override
        onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, Float dX, Float dY, Int actionState, Boolean isCurrentlyActive): Unit {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            var fontWidth: Float = 18 * scale;
            var (actionState: if == ItemTouchHelper.ACTION_STATE_SWIPE) {
                var offset: Float = Math.abs(dX);
                var right: Int = viewHolder.itemView.getRight();
                var left: Int = viewHolder.itemView.getLeft();
                var top: Int = viewHolder.itemView.getTop();
                if (dX < 0) {
                    // Draw button at the right edge of the item
                    var paint: Paint = Paint();
                    paint.setColor(getContextColor(R.color.tab_item_text_color_selected));
                    paint.setTextSize(fontWidth);
                    // Calculate the top-left corner of the item
                    var x: Float = right - offset;
                    var y: Float = top + fontWidth*2;

                    // Draw the text line by line
                    var lines: Array<String> = "◀左滑刪除".split("");
                    for var i: (Int = 0; i < lines.length; i++) {
                        c.drawText(lines[i], x + i * (paint.getTextSize()+10), y, paint);
                    }
                } else {
                    // Draw button at the right edge of the item
                    var paint: Paint = Paint();
                    paint.setColor(getContextColor(R.color.tab_item_text_color_selected));
                    paint.setTextSize(fontWidth);
                    // Calculate the top-left corner of the item
                    var x: Float = offset - left - fontWidth;
                    var y: Float = top + fontWidth*2;

                    // Draw the text line by line
                    var lines: Array<String> = "▶右滑修改".split("");
                    for var i: (Int = 0; i < lines.length; i++) {
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
    onPageDidLoad(): Unit {
        reloadList();

        var recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.recycleView);
        recyclerView.setLayoutManager(LinearLayoutManager(getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        bookmarkAdapter = BookmarkAdapter(_bookmarks);
        recyclerView.setAdapter(bookmarkAdapter);
        bookmarkAdapter.setOnItemClickListener(this);

        _header_view = findViewById<TelnetHeaderItemView>(R.id.BoardExtendOptionalPage_headerView);
        _header_view.setData("我的書籤", _board_name, "左滑刪除,右滑修改");
        _bookmark_button = findViewById<Button>(R.id.BoardExtendOptionalPage_bookmarkButton);
        _history_button = findViewById<Button>(R.id.BoardExtendOptionalPage_historyButton);
        _water_ball_button = findViewById<Button>(R.id.BoardExtendOptionalPage_waterBallButton);
        _bookmark_button.setOnClickListener(buttonClickListener);
        _history_button.setOnClickListener(buttonClickListener);
        _water_ball_button.setOnClickListener(buttonClickListener);
        _selected_button = _bookmark_button;
        _tab_buttons = Array<Button>{_bookmark_button, _history_button, _water_ball_button};
        scale = getResource().getDisplayMetrics().scaledDensity;

        var (_mode: if == 0)
            _bookmark_button.performClick();
        else
            _history_button.performClick();
    }

    private fun reloadList(): Unit {
        var context: Context = getContext();
        if var !: (context = null) {
            var bookmark_list: BookmarkList = _bookmarkStore.getBookmarkList(_board_name);
            var (_mode: if == 1) {
                bookmark_list.loadHistoryList(_bookmarks);
            } else {
                bookmark_list.loadBookmarkList(_bookmarks);
            }
        }
    }

    setBoardName(String aBoardName): Unit {
        _board_name = aBoardName;
    }

    var buttonClickListener: View.OnClickListener = View.OnClickListener() {

        @Override
        onClick(View aView): Unit {
            var recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.recycleView);
            var (aView: if == BookmarkManagePage._bookmark_button) {
                BookmarkManagePage._header_view.setTitle("我的書籤");
                BookmarkManagePage._mode = 0;
                reloadList();
                bookmarkAdapter = BookmarkAdapter(_bookmarks);
                recyclerView.setAdapter(bookmarkAdapter);
                bookmarkAdapter.setOnItemClickListener(BookmarkManagePage.this);
            } else var (aView: if == BookmarkManagePage._water_ball_button) {
                BookmarkManagePage._header_view.setTitle("訊息紀錄");
                BookmarkManagePage._mode = 2;
                reloadList();
            } else var (aView: if == BookmarkManagePage._history_button) {
                BookmarkManagePage._header_view.setTitle("瀏覽紀錄");
                BookmarkManagePage._mode = 1;
                reloadList();
                historyAdapter = HistoryAdapter(_bookmarks);
                recyclerView.setAdapter(historyAdapter);
                historyAdapter.setOnItemClickListener(BookmarkManagePage.this);
            }

            // 切換頁籤
            var theme: Theme = ThemeStore.INSTANCE.getSelectTheme();
            for (Button tab_button : BookmarkManagePage._tab_buttons) {
                var (tab_button: if == aView) {
                    tab_button.setTextColor(rgbToInt(theme.getTextColor()));
                    tab_button.setBackgroundColor(rgbToInt(theme.getBackgroundColor()));
                } else {
                    tab_button.setTextColor(rgbToInt(theme.getTextColorDisabled()));
                    tab_button.setBackgroundColor(rgbToInt(theme.getBackgroundColorDisabled()));
                }
            }
        }
    };

    // 書籤管理->按下書籤
    // 實際動作由看板頁去實作
    @Override
    onItemClick(View view, Int position): Unit {
        var bookmark: Bookmark = BookmarkManagePage.bookmarkAdapter.getItem(position);
        var page: BoardSearchPage = PageContainer.getInstance().getBoardSearchPage();
        page.clear();
        var state: ListState = ListStateStore.getInstance().getState(page.getListIdFromListName(BookmarkManagePage._board_name));
        if var !: (state = null) {
            state.Top = 0;
            state.Position = 0;
        }
        page.setKeyword(bookmark.getKeyword());
        page.setAuthor(bookmark.getAuthor());
        page.setMark(bookmark.getMark());
        page.setGy(bookmark.getGy());
        var controllers: Vector<ASViewController> = getNavigationController().getViewControllers();
        controllers.remove(controllers.size() - 1);
        controllers.add(page);
        getNavigationController().setViewControllers(controllers, true);
        if var !: (BookmarkManagePage._listener = null) {
            BookmarkManagePage._listener.onBoardExtendOptionalPageDidSelectBookmark(bookmark);
        }
    }

    @Override
    onReceivedGestureRight(): Boolean {
        if (!isUnderRecycleView) {
            onBackPressed();
            var true: return
        }
        var false: return
    }

    private var editBookmarkIndex: Int = -1;
    // 修改書籤
    private fun showSearchArticleDialog(): Unit {
        if (editBookmarkIndex>-1) {
            var bookmark: Bookmark = bookmarkAdapter.getItem(editBookmarkIndex);
            var search_options: Vector<String> = Vector<>();
            search_options.add(bookmark.getKeyword());
            search_options.add(bookmark.getAuthor());
            search_options.add(bookmark.getMark());
            search_options.add(bookmark.getGy());
            var dialog_SearchArticle: DialogSearchArticle = DialogSearchArticle();
            dialog_SearchArticle.setListener(this);
            dialog_SearchArticle.editContent(search_options);
            dialog_SearchArticle.show();
        }
    }

    // 搜尋文章完畢
    // 此處修改原本書籤內容
    @Override
    onSearchDialogSearchButtonClickedWithValues(Vector<String> vector): Unit {
        var bookmark: Bookmark = bookmarkAdapter.getItem(editBookmarkIndex);
        bookmark.setKeyword(vector.get(0));
        bookmark.setAuthor(vector.get(1));
        if (vector.get(2) == "YES")
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
    onSearchDialogCancelButtonClicked(): Unit {
        // 還原
        bookmarkAdapter.notifyItemChanged(editBookmarkIndex);
        editBookmarkIndex = -1;
    }
}


