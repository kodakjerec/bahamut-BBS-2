package com.kota.Bahamut.Pages.BoardPage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.view.View;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetHeaderItemView;

public class BoardSearchPage extends BoardMainPage {
    private String _author = null;
    private String _gy = null;
    private String _keyword = null;
    private String _mark = null;

    public int getPageType() {
        return BahamutPage.BAHAMUT_BOARD_SEARCH;
    }

    public int getPageLayout() {
        return R.layout.board_search_page;
    }

    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        TelnetHeaderItemView header_view = (TelnetHeaderItemView) findViewById(R.id.BoardPage_HeaderView);
        if (header_view != null) {
            String board_name = getListName();
            if (board_name == null) {
                board_name = getContextString(R.string.loading);
            }
            header_view.setData(this._board_title, "文章搜尋", board_name);
        }
    }

    protected boolean onMenuButtonClicked() {
        showSelectArticleDialog();
        return true;
    }

    protected boolean onListViewItemLongClicked(View itemView, int selectedIndex) {
        BoardPageItem item = (BoardPageItem) BoardSearchPage.this.getItem(selectedIndex);

        if (item!=null) {
            ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.string.insert) + getContextString(R.string.bookmark))
                    .setMessage(getContextString(R.string.insert_this_bookmark) + "\n\"" + item.Title + "\"")
                    .addButton(getContextString(R.string.cancel))
                    .addButton(getContextString(R.string.insert))
                    .setListener((aDialog, index) -> {
                        if (index == 1) {
                            Bookmark bookmark = new Bookmark();
                            System.out.println("add bookmark:" + bookmark.getTitle());
                            bookmark.setBoard(BoardSearchPage.this.getListName());
                            bookmark.setKeyword(item.Title);
                            bookmark.setTitle(bookmark.generateTitle());
                            BookmarkStore store = TempSettings.getBookmarkStore();
                            store.getBookmarkList(BoardSearchPage.this.getListName()).addBookmark(bookmark);
                            store.store();
                        }
                    }).scheduleDismissOnPageDisappear(this).show();
        }
        return true;
    }

    public int getListType() {
        return 2;
    }

    public String getListIdFromListName(String aName) {
        return aName + "[Board][Search]";
    }

    protected void onPostButtonClicked() {
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.insert) + getContextString(R.string.bookmark))
                .setMessage(getContextString(R.string.insert_this_bookmark_search))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.insert))
                .setListener((aDialog, index) -> {
            if (index == 1) {
                Bookmark bookmark = new Bookmark();
                System.out.println("add bookmark:" + bookmark.getTitle());
                bookmark.setBoard(BoardSearchPage.this.getListName());
                bookmark.setKeyword(BoardSearchPage.this._keyword);
                bookmark.setAuthor(BoardSearchPage.this._author);
                bookmark.setMark(BoardSearchPage.this._mark);
                bookmark.setGy(BoardSearchPage.this._gy);
                bookmark.setTitle(bookmark.generateTitle());
                BookmarkStore store = TempSettings.getBookmarkStore();
                store.getBookmarkList(BoardSearchPage.this.getListName()).addBookmark(bookmark);
                store.store();
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    public void setKeyword(String keyword) {
        this._keyword = keyword;
    }

    public void setAuthor(String author) {
        this._author = author;
    }

    public void setMark(String mark) {
        this._mark = mark;
    }

    public void setGy(String gy) {
        this._gy = gy;
    }

    protected boolean onBackPressed() {
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(256, 1);
        PageContainer.getInstance().cleanBoardSearchPage();
        return true;
    }

    public boolean isAutoLoadEnable() {
        return false;
    }
}
