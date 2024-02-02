package com.kota.Bahamut.Pages;

import android.view.View;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetHeaderItemView;

public class BoardSearchPage extends BoardPage {
    private String _author = null;
    private String _gy = null;
    private String _keyword = null;
    private String _mark = null;

    public int getPageType() {
        return 13;
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
                board_name = "讀取中";
            }
            header_view.setData(this._board_title, "文章搜尋", board_name);
        }
    }

    protected boolean onMenuButtonClicked() {
        showSelectArticleDialog();
        return true;
    }

    protected boolean onListViewItemLongClicked(View itemView, int index) {
        return false;
    }

    public int getListType() {
        return 2;
    }

    public String getListIdFromListName(String aName) {
        return aName + "[Board][Search]";
    }

    protected void onPostButtonClicked() {
        ASAlertDialog.createDialog().setTitle("加入書籤").setMessage("是否要將此搜尋結果加入書籤?").addButton("取消").addButton("加入").setListener((aDialog, index) -> {
            if (index == 1) {
                Bookmark bookmark = new Bookmark();
                System.out.println("add bookmark:" + bookmark.getTitle());
                bookmark.setBoard(BoardSearchPage.this.getListName());
                bookmark.setKeyword(BoardSearchPage.this._keyword);
                bookmark.setAuthor(BoardSearchPage.this._author);
                bookmark.setMark(BoardSearchPage.this._mark);
                bookmark.setGy(BoardSearchPage.this._gy);
                bookmark.setTitle(bookmark.generateTitle());
                BookmarkStore store = new BookmarkStore(BoardSearchPage.this.getContext());
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

    protected boolean isBookmarkAvailable() {
        return false;
    }

    public boolean isAutoLoadEnable() {
        return false;
    }
}
