package com.kota.Bahamut.Pages;

import android.view.View;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetHeaderItemView;

public class BoardLinkPage extends BoardPage {
    public int getPageType() {
        return 12;
    }

    public int getPageLayout() {
        return R.layout.board_link_page;
    }

    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        TelnetHeaderItemView header_view = (TelnetHeaderItemView) findViewById(R.id.BoardPage_HeaderView);
        if (header_view != null) {
            String board_name = getListName();
            if (board_name == null) {
                board_name = "讀取中";
            }
            header_view.setData(this._board_title, "主題串列", board_name);
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
        return 1;
    }

    public String getListIdFromListName(String aName) {
        return aName + "[Board][TitleLinked]";
    }

    protected void onPostButtonClicked() {
        ASAlertDialog.createDialog().setTitle("加入書籤").setMessage("是否要將此標題加入書籤?").addButton("取消").addButton("加入").setListener((aDialog, index) -> {
            if (index == 1) {
                BoardPageItem item = null;
                if (BoardLinkPage.this.getCount() > 0) {
                    item = (BoardPageItem) BoardLinkPage.this.getItem(0);
                }
                if (item != null) {
                    Bookmark bookmark = new Bookmark();
                    System.out.println("add bookmark:" + bookmark.getTitle());
                    bookmark.setBoard(BoardLinkPage.this.getListName());
                    bookmark.setKeyword(item.Title);
                    bookmark.setTitle(bookmark.generateTitle());
                    BookmarkStore store = new BookmarkStore(BoardLinkPage.this.getContext());
                    store.getBookmarkList(BoardLinkPage.this.getListName()).addBookmark(bookmark);
                    store.store();
                }
            }
        }).scheduleDismissOnPageDisappear(this).show();
    }

    protected boolean onBackPressed() {
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(256, 1);
        PageContainer.getInstance().cleanBoardTitleLinkedPage();
        return true;
    }

    protected boolean isBookmarkAvailable() {
        return false;
    }
}
