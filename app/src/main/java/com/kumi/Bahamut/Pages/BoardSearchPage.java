package com.kumi.Bahamut.Pages;

import android.view.View;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.Bahamut.DataModels.Bookmark;
import com.kumi.Bahamut.DataModels.BookmarkStore;
import com.kumi.Bahamut.PageContainer;
import com.kumi.Telnet.TelnetClient;

public class BoardSearchPage extends BoardPage {
  private String _author = null;
  
  private String _gy = null;
  
  private String _keyword = null;
  
  private String _mark = null;
  
  public String getListIdFromListName(String paramString) {
    return paramString + "[Board][Search]";
  }
  
  public int getListType() {
    return 2;
  }
  
  public int getPageLayout() {
    return 2131361832;
  }
  
  public int getPageType() {
    return 13;
  }
  
  public boolean isAutoLoadEnable() {
    return false;
  }
  
  protected boolean isBookmarkAvailable() {
    return false;
  }
  
  protected boolean onBackPressed() {
    clear();
    getNavigationController().popViewController();
    TelnetClient.getClient().sendKeyboardInputToServerInBackground(256, 1);
    PageContainer.getInstance().cleanBoardSearchPage();
    return true;
  }
  
  protected boolean onListViewItemLongClicked(View paramView, int paramInt) {
    return false;
  }
  
  protected boolean onMenuButtonClicked() {
    showSelectArticleDialog();
    return true;
  }
  
  public void onPageRefresh() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: invokespecial onPageRefresh : ()V
    //   6: aload_0
    //   7: ldc 2131230818
    //   9: invokevirtual findViewById : (I)Landroid/view/View;
    //   12: checkcast com/kumi/TelnetUI/TelnetHeaderItemView
    //   15: astore_3
    //   16: aload_3
    //   17: ifnull -> 45
    //   20: aload_0
    //   21: invokevirtual getListName : ()Ljava/lang/String;
    //   24: astore_2
    //   25: aload_2
    //   26: astore_1
    //   27: aload_2
    //   28: ifnonnull -> 34
    //   31: ldc '讀取中'
    //   33: astore_1
    //   34: aload_3
    //   35: aload_0
    //   36: getfield _board_title : Ljava/lang/String;
    //   39: ldc '文章搜尋'
    //   41: aload_1
    //   42: invokevirtual setData : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    //   45: aload_0
    //   46: monitorexit
    //   47: return
    //   48: astore_1
    //   49: aload_0
    //   50: monitorexit
    //   51: aload_1
    //   52: athrow
    // Exception table:
    //   from	to	target	type
    //   2	16	48	finally
    //   20	25	48	finally
    //   34	45	48	finally
  }
  
  protected void onPostButtonClicked() {
    ASAlertDialog.createDialog().setTitle("加入書籤").setMessage("是否要將此搜尋結果加入書籤?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() {
          final BoardSearchPage this$0;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            if (param1Int == 1) {
              Bookmark bookmark = new Bookmark();
              System.out.println("add bookmark:" + bookmark.getTitle());
              bookmark.setBoard(BoardSearchPage.this.getListName());
              bookmark.setKeyword(BoardSearchPage.this._keyword);
              bookmark.setAuthor(BoardSearchPage.this._author);
              bookmark.setMark(BoardSearchPage.this._mark);
              bookmark.setGy(BoardSearchPage.this._gy);
              bookmark.setTitle(bookmark.generateTitle());
              BookmarkStore bookmarkStore = new BookmarkStore(BoardSearchPage.this.getContext());
              bookmarkStore.getBookmarkList(BoardSearchPage.this.getListName()).addBookmark(bookmark);
              bookmarkStore.store();
            } 
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  public void setAuthor(String paramString) {
    this._author = paramString;
  }
  
  public void setGy(String paramString) {
    this._gy = paramString;
  }
  
  public void setKeyword(String paramString) {
    this._keyword = paramString;
  }
  
  public void setMark(String paramString) {
    this._mark = paramString;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BoardSearchPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */