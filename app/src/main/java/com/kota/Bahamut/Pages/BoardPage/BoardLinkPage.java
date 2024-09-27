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
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetHeaderItemView;

public class BoardLinkPage extends BoardMainPage {
    public int getPageType() {
        return BahamutPage.BAHAMUT_BOARD_LINK;
    }

    public int getPageLayout() {
        return R.layout.board_link_page;
    }

    @Override
    public int getListType() {
        return BoardPageAction.LINK_TITLE;
    }

    public synchronized void onPageRefresh() {
        super.onPageRefresh();
        TelnetHeaderItemView header_view = (TelnetHeaderItemView) findViewById(R.id.BoardPage_HeaderView);
        if (header_view != null) {
            String board_name = getListName();
            if (board_name == null) {
                board_name = getContextString(R.string.loading);
            }
            header_view.setData(this._board_title, "主題串列", board_name);
        }
    }

    protected boolean onMenuButtonClicked() {
        showSelectArticleDialog();
        return true;
    }

    // 長按串接到的item
    protected boolean onListViewItemLongClicked(View itemView, int selectedItemIndex) {
        BoardPageItem item = (BoardPageItem) BoardLinkPage.this.getItem(selectedItemIndex);

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
                            bookmark.setBoard(BoardLinkPage.this.getListName());
                            bookmark.setKeyword(item.Title);
                            bookmark.setTitle(bookmark.generateTitle());
                            BookmarkStore store = TempSettings.getBookmarkStore();
                            store.getBookmarkList(BoardLinkPage.this.getListName()).addBookmark(bookmark);
                            store.store();
                        }
                    }).scheduleDismissOnPageDisappear(this).show();
        }
        return true;
    }

    @Override
    public String getListIdFromListName(String aName) {
        return aName + "[Board][TitleLinked]";
    }

    protected void onPostButtonClicked() {
        if (BoardLinkPage.this.getCount() > 0) {
            BoardPageItem item = (BoardPageItem) BoardLinkPage.this.getItem(0);

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
                                bookmark.setBoard(BoardLinkPage.this.getListName());
                                bookmark.setKeyword(item.Title);
                                bookmark.setTitle(bookmark.generateTitle());
                                BookmarkStore store = TempSettings.getBookmarkStore();
                                store.getBookmarkList(BoardLinkPage.this.getListName()).addBookmark(bookmark);
                                store.store();
                            }
                        }).scheduleDismissOnPageDisappear(this).show();
            }
        }
    }

    protected boolean onBackPressed() {
        clear();
        getNavigationController().popViewController();
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1);
        PageContainer.getInstance().cleanBoardTitleLinkedPage();
        return true;
    }
}
