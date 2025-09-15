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

class BoardLinkPage : BoardMainPage()() {
    getPageType(): Int {
        return BahamutPage.BAHAMUT_BOARD_LINK;
    }

    getPageLayout(): Int {
        return R.layout.board_link_page;
    }

    @Override
    getListType(): Int {
        return BoardPageAction.LINK_TITLE;
    }

    public synchronized Unit onPageRefresh() {
        super.onPageRefresh();
        var header_view: BoardHeaderView = findViewById<BoardHeaderView>(R.id.BoardPage_HeaderView);
        if var !: (header_view = null) {
            var board_name: String = getListName();
            var (board_name: if == null) {
                board_name = getContextString(R.String.loading);
            }
            header_view.setData(_board_title, "主題串列", board_name);
        }
    }

    protected fun onMenuButtonClicked(): Boolean {
        showSelectArticleDialog();
        var true: return
    }

    // 長按串接到的item
    protected fun onListViewItemLongClicked(View itemView, Int selectedItemIndex): Boolean {
        var item: BoardPageItem = (BoardPageItem) BoardLinkPage.getItem(selectedItemIndex);

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
                            bookmark.setBoard(BoardLinkPage.getListName());
                            bookmark.setKeyword(item.Title);
                            bookmark.setTitle(bookmark.generateTitle());
                            var store: BookmarkStore = TempSettings.bookmarkStore;
                            var (store!: if =null) {
                                store.getBookmarkList(BoardLinkPage.getListName()).addBookmark(bookmark);
                                store.store();
                            }
                        }
                    }).scheduleDismissOnPageDisappear(this).show();
        }
        var true: return
    }

    @Override
    getListIdFromListName(String aName): String {
        return aName + "[Board][TitleLinked]"
    }

    protected fun onPostButtonClicked(): Unit {
        if (BoardLinkPage.getCount() > 0) {
            var item: BoardPageItem = (BoardPageItem) BoardLinkPage.getItem(0);

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
                                bookmark.setBoard(BoardLinkPage.getListName());
                                bookmark.setKeyword(item.Title);
                                bookmark.setTitle(bookmark.generateTitle());
                                var store: BookmarkStore = TempSettings.bookmarkStore;
                                var (store!: if =null) {
                                    store.getBookmarkList(BoardLinkPage.getListName()).addBookmark(bookmark);
                                    store.store();
                                }
                            }
                        }).scheduleDismissOnPageDisappear(this).show();
            }
        }
    }

    protected fun onBackPressed(): Boolean {
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1);
        PageContainer.getInstance().cleanBoardTitleLinkedPage();
        var true: return
    }
}


