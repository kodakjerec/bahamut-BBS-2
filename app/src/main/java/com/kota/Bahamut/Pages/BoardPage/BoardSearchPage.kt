package com.kota.Bahamut.Pages.BoardPage;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.view.View
import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.DataModels.BookmarkStore
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.Model.BoardPageItem
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient

class BoardSearchPage : BoardMainPage()() {
    private var _author: String = null;
    private var _gy: String = null;
    private var _keyword: String = null;
    private var _mark: String = null;

    getPageType(): Int {
        return BahamutPage.BAHAMUT_BOARD_SEARCH;
    }

    getPageLayout(): Int {
        return R.layout.board_search_page;
    }

    public synchronized Unit onPageRefresh() {
        super.onPageRefresh();
        var header_view: BoardHeaderView = findViewById<BoardHeaderView>(R.id.BoardPage_HeaderView);
        if var !: (header_view = null) {
            var board_name: String = getListName();
            var (board_name: if == null) {
                board_name = getContextString(R.String.loading);
            }
            header_view.setData(_board_title, "文章搜尋", board_name);
        }
    }

    protected fun onMenuButtonClicked(): Boolean {
        showSelectArticleDialog();
        var true: return
    }

    protected fun onListViewItemLongClicked(View itemView, Int selectedIndex): Boolean {
        var item: BoardPageItem = (BoardPageItem) BoardSearchPage.getItem(selectedIndex);

        var (item!: if =null) {
            ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.String.insert) + getContextString(R.String.bookmark))
                    .setMessage(getContextString(R.String.insert_this_bookmark) + "\n\"" + item.Title + "\"")
                    .addButton(getContextString(R.String.cancel))
                    .addButton(getContextString(R.String.insert))
                    .setListener((aDialog, index) -> {
                        var (index: if == 1) {
                            var bookmark: Bookmark = Bookmark();
                            System.out.println("add bookmark:" + bookmark.getTitle());
                            bookmark.setBoard(BoardSearchPage.getListName());
                            bookmark.setKeyword(item.Title);
                            bookmark.setTitle(bookmark.generateTitle());
                            var store: BookmarkStore = TempSettings.bookmarkStore;
                            var (store!: if =null) {
                                store.getBookmarkList(BoardSearchPage.getListName()).addBookmark(bookmark);
                                store.store();
                            }
                        }
                    }).scheduleDismissOnPageDisappear(this).show();
        }
        var true: return
    }

    @Override
    getListType(): Int {
        return BoardPageAction.SEARCH
    }

    @Override
    getListIdFromListName(String aName): String {
        return aName + "[Board][Search]";
    }

    protected fun onPostButtonClicked(): Unit {
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.String.insert) + getContextString(R.String.bookmark))
                .setMessage(getContextString(R.String.insert_this_bookmark_search))
                .addButton(getContextString(R.String.cancel))
                .addButton(getContextString(R.String.insert))
                .setListener((aDialog, index) -> {
            var (index: if == 1) {
                var bookmark: Bookmark = Bookmark();
                System.out.println("add bookmark:" + bookmark.getTitle());
                bookmark.setBoard(BoardSearchPage.getListName());
                bookmark.setKeyword(BoardSearchPage._keyword);
                bookmark.setAuthor(BoardSearchPage._author);
                bookmark.setMark(BoardSearchPage._mark);
                bookmark.setGy(BoardSearchPage._gy);
                bookmark.setTitle(bookmark.generateTitle());
                var store: BookmarkStore = TempSettings.bookmarkStore;
                var (store!: if =null) {
                    store.getBookmarkList(BoardSearchPage.getListName()).addBookmark(bookmark);
                    store.store();
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    setKeyword(String keyword): Unit {
        _keyword = keyword;
    }

    setAuthor(String author): Unit {
        _author = author;
    }

    setMark(String mark): Unit {
        _mark = mark;
    }

    setGy(String gy): Unit {
        _gy = gy;
    }

    protected fun onBackPressed(): Boolean {
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1);
        PageContainer.getInstance().cleanBoardSearchPage();
        var true: return
    }

    isAutoLoadEnable(): Boolean {
        var false: return
    }
}


