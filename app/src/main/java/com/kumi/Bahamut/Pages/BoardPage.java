package com.kumi.Bahamut.Pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.Dialog.ASListDialog;
import com.kumi.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.ASFramework.UI.ASListView;
import com.kumi.ASFramework.UI.ASListViewExtentOptionalDelegate;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kumi.Bahamut.Command.BahamutCommandEditArticle;
import com.kumi.Bahamut.Command.BahamutCommandGoodArticle;
import com.kumi.Bahamut.Command.BahamutCommandListArticle;
import com.kumi.Bahamut.Command.BahamutCommandPostArticle;
import com.kumi.Bahamut.Command.BahamutCommandSearchArticle;
import com.kumi.Bahamut.Command.BahamutCommandTheSameTitleBottom;
import com.kumi.Bahamut.Command.BahamutCommandTheSameTitleDown;
import com.kumi.Bahamut.Command.BahamutCommandTheSameTitleTop;
import com.kumi.Bahamut.Command.BahamutCommandTheSameTitleUp;
import com.kumi.Bahamut.Command.TelnetCommand;
import com.kumi.Bahamut.DataModels.Bookmark;
import com.kumi.Bahamut.Dialogs.Dialog_SearchArticle;
import com.kumi.Bahamut.Dialogs.Dialog_SearchArticle_Listener;
import com.kumi.Bahamut.Dialogs.Dialog_SelectArticle;
import com.kumi.Bahamut.Dialogs.Dialog_SelectArticle_Listener;
import com.kumi.Bahamut.ListPage.ListState;
import com.kumi.Bahamut.ListPage.ListStateStore;
import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Bahamut.ListPage.TelnetListPageItem;
import com.kumi.Bahamut.PageContainer;
import com.kumi.Bahamut.Pages.Model.BoardPageBlock;
import com.kumi.Bahamut.Pages.Model.BoardPageHandler;
import com.kumi.Bahamut.Pages.Model.BoardPageItem;
import com.kumi.Telnet.Logic.ItemUtils;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetHeaderItemView;
import java.util.Vector;

public class BoardPage extends TelnetListPage implements Dialog_SearchArticle_Listener, Dialog_SelectArticle_Listener, PostArticlePage_Listener, BoardExtendOptionalPageListener, ASListViewExtentOptionalDelegate {
  protected String _board_manager = null;
  
  protected String _board_title = null;
  
  private boolean _initialed = false;
  
  private int _last_list_action = 0;
  
  private boolean _refresh_header_view = false;
  
  UserSettings _settings;
  
  private View.OnClickListener mFirstPageClicked = new View.OnClickListener() {
      final BoardPage this$0;
      
      public void onClick(View param1View) {
        BoardPage.this.moveToFirstPosition();
      }
    };
  
  private View.OnClickListener mLastPageClicked = new View.OnClickListener() {
      final BoardPage this$0;
      
      public void onClick(View param1View) {
        BoardPage.this.setManualLoadPage();
        BoardPage.this.moveToLastPosition();
      }
    };
  
  private View.OnClickListener mMenuButtonListener = new View.OnClickListener() {
      final BoardPage this$0;
      
      public void onClick(View param1View) {
        BoardPage.this.onMenuClicked();
      }
    };
  
  private View.OnClickListener mOpenBookmarkListener = new View.OnClickListener() {
      final BoardPage this$0;
      
      public void onClick(View param1View) {
        BoardPage.this.onBookmarkButtonClicked();
      }
    };
  
  private View.OnClickListener mPostListener = new View.OnClickListener() {
      final BoardPage this$0;
      
      public void onClick(View param1View) {
        BoardPage.this.onPostButtonClicked();
      }
    };
  
  private View.OnClickListener mSearchByKeywordListener = new View.OnClickListener() {
      final BoardPage this$0;
      
      public void onClick(View param1View) {
        BoardPage.this.onSearchButtonClicked();
      }
    };
  
  private View.OnClickListener mSearchByNumberListener = new View.OnClickListener() {
      final BoardPage this$0;
      
      public void onClick(View param1View) {
        BoardPage.this.showSelectArticleDialog();
      }
    };
  
  private void onChangeBlockStateButtonClicked() {
    boolean bool;
    UserSettings userSettings = this._settings;
    if (!this._settings.isBlockListEnable()) {
      bool = true;
    } else {
      bool = false;
    } 
    userSettings.setBlockListEnable(bool);
    reloadListView();
  }
  
  private void onEditBlockListButtonClicked() {
    BlockListPage blockListPage = new BlockListPage();
    getNavigationController().pushViewController((ASViewController)blockListPage);
  }
  
  private void onListArticle(int paramInt) {
    this._last_list_action = 0;
    BoardLinkPage boardLinkPage = PageContainer.getInstance().getBoard_Linked_Title_Page();
    boardLinkPage.clear();
    getNavigationController().pushViewController((ASViewController)boardLinkPage);
    ListState listState = ListStateStore.getInstance().getState(boardLinkPage.getListIdFromListName(getListName()));
    if (listState != null) {
      listState.Top = 0;
      listState.Position = 0;
    } 
    pushCommand((TelnetCommand)new BahamutCommandListArticle(paramInt));
  }
  
  private void refreshExternalToolbar() {
    boolean bool = this._settings.isExternalToolbarEnable();
    View view = findViewById(2131230988);
    if (view != null) {
      byte b;
      if (bool) {
        b = 0;
      } else {
        b = 8;
      } 
      view.setVisibility(b);
    } 
  }
  
  private void refreshHeaderView() {
    // Byte code:
    //   0: aload_0
    //   1: getfield _board_title : Ljava/lang/String;
    //   4: astore_2
    //   5: aload_2
    //   6: ifnull -> 18
    //   9: aload_2
    //   10: astore_1
    //   11: aload_2
    //   12: invokevirtual length : ()I
    //   15: ifne -> 21
    //   18: ldc '讀取中'
    //   20: astore_1
    //   21: aload_0
    //   22: getfield _board_manager : Ljava/lang/String;
    //   25: astore_3
    //   26: aload_3
    //   27: ifnull -> 39
    //   30: aload_3
    //   31: astore_2
    //   32: aload_3
    //   33: invokevirtual length : ()I
    //   36: ifne -> 42
    //   39: ldc '讀取中'
    //   41: astore_2
    //   42: aload_0
    //   43: ldc 2131230818
    //   45: invokevirtual findViewById : (I)Landroid/view/View;
    //   48: checkcast com/kumi/TelnetUI/TelnetHeaderItemView
    //   51: astore_3
    //   52: aload_3
    //   53: ifnull -> 66
    //   56: aload_3
    //   57: aload_1
    //   58: aload_0
    //   59: invokevirtual getListName : ()Ljava/lang/String;
    //   62: aload_2
    //   63: invokevirtual setData : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    //   66: return
  }
  
  private void searchArticle(String paramString1, String paramString2, String paramString3, String paramString4) {
    this._last_list_action = 1;
    BoardSearchPage boardSearchPage = PageContainer.getInstance().getBoard_Search_Page();
    boardSearchPage.clear();
    getNavigationController().pushViewController((ASViewController)boardSearchPage);
    ListState listState = ListStateStore.getInstance().getState(boardSearchPage.getListIdFromListName(getListName()));
    if (listState != null) {
      listState.Top = 0;
      listState.Position = 0;
    } 
    boardSearchPage.setKeyword(paramString1);
    boardSearchPage.setAuthor(paramString2);
    boardSearchPage.setMark(paramString3);
    boardSearchPage.setGy(paramString4);
    pushCommand((TelnetCommand)new BahamutCommandSearchArticle(paramString1, paramString2, paramString3, paramString4));
  }
  
  private void showSearchArticleDialog() {
    Dialog_SearchArticle dialog_SearchArticle = new Dialog_SearchArticle();
    dialog_SearchArticle.setListener(this);
    dialog_SearchArticle.show();
  }
  
  public int getLastListAction() {
    return this._last_list_action;
  }
  
  public String getListIdFromListName(String paramString) {
    return paramString + "[Board]";
  }
  
  public int getPageLayout() {
    return 2131361830;
  }
  
  public int getPageType() {
    return 10;
  }
  
  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    BoardPageItemView boardPageItemView2;
    int i = paramInt + 1;
    int j = ItemUtils.getBlock(i);
    BoardPageItem boardPageItem = (BoardPageItem)getItem(paramInt);
    if (boardPageItem == null && getCurrentBlock() != j && !isLoadingBlock(i))
      loadBoardBlock(j); 
    View view = paramView;
    if (paramView == null) {
      boardPageItemView2 = new BoardPageItemView(getContext());
      boardPageItemView2.setLayoutParams((ViewGroup.LayoutParams)new AbsListView.LayoutParams(-1, -2));
    } 
    BoardPageItemView boardPageItemView1 = boardPageItemView2;
    boardPageItemView1.setItem(boardPageItem);
    boardPageItemView1.setNumber(paramInt + 1);
    if (boardPageItem != null && this._settings.isBlockListEnable() && this._settings.isBlockListContains(boardPageItem.Author)) {
      boardPageItemView1.setVisible(false);
      return (View)boardPageItemView2;
    } 
    boardPageItemView1.setVisible(true);
    return (View)boardPageItemView2;
  }
  
  public void goodArticle(final int articleIndex) {
    ASAlertDialog.createDialog().setTitle("推薦").setMessage("是否要推薦此文章?").addButton("取消").addButton("推薦").setListener(new ASAlertDialogListener() {
          final BoardPage this$0;
          
          final int val$articleIndex;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            switch (param1Int) {
              default:
                return;
              case 1:
                break;
            } 
            BahamutCommandGoodArticle bahamutCommandGoodArticle = new BahamutCommandGoodArticle(articleIndex);
            BoardPage.this.pushCommand((TelnetCommand)bahamutCommandGoodArticle);
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  public void goodLoadingArticle() {
    goodArticle(getLoadingItemNumber());
  }
  
  public boolean isAutoLoadEnable() {
    return true;
  }
  
  protected boolean isBookmarkAvailable() {
    return true;
  }
  
  public boolean isItemBlockEnable() {
    return this._settings.isBlockListEnable();
  }
  
  public boolean isItemBlocked(TelnetListPageItem paramTelnetListPageItem) {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (paramTelnetListPageItem != null) {
      BoardPageItem boardPageItem = (BoardPageItem)paramTelnetListPageItem;
      bool1 = bool2;
      if (this._settings.isBlockListEnable()) {
        bool1 = bool2;
        if (this._settings.isBlockListContains(boardPageItem.Author))
          bool1 = true; 
      } 
    } 
    return bool1;
  }
  
  public boolean isItemCanLoadAtIndex(int paramInt) {
    boolean bool2 = true;
    BoardPageItem boardPageItem = (BoardPageItem)getItem(paramInt);
    boolean bool1 = bool2;
    if (boardPageItem != null) {
      bool1 = bool2;
      if (boardPageItem.isDeleted) {
        ASToast.showShortToast("此文章已被刪除");
        bool1 = false;
      } 
    } 
    return bool1;
  }
  
  public void loadItemAtIndex(int paramInt) {
    ArticlePage articlePage = PageContainer.getInstance().getArticlePage();
    articlePage.setBoardPage(this);
    articlePage.clear();
    getNavigationController().pushViewController((ASViewController)articlePage);
    super.loadItemAtIndex(paramInt);
  }
  
  public TelnetListPageBlock loadPage() {
    BoardPageBlock boardPageBlock = BoardPageHandler.getInstance().load();
    if (!this._initialed) {
      clear();
      if (boardPageBlock.Type == 1)
        pushRefreshCommand(0); 
      this._board_manager = boardPageBlock.BoardManager;
      this._board_title = boardPageBlock.BoardTitle;
      setListName(boardPageBlock.BoardName);
      this._refresh_header_view = true;
      this._initialed = true;
    } 
    return (TelnetListPageBlock)boardPageBlock;
  }
  
  public void loadTheSameTitleBottom() {
    onLoadItemStart();
    pushCommand((TelnetCommand)new BahamutCommandTheSameTitleBottom(getLoadingItemNumber()));
  }
  
  public void loadTheSameTitleDown() {
    onLoadItemStart();
    pushCommand((TelnetCommand)new BahamutCommandTheSameTitleDown(getLoadingItemNumber()));
  }
  
  public void loadTheSameTitleTop() {
    onLoadItemStart();
    pushCommand((TelnetCommand)new BahamutCommandTheSameTitleTop(getLoadingItemNumber()));
  }
  
  public void loadTheSameTitleUp() {
    onLoadItemStart();
    pushCommand((TelnetCommand)new BahamutCommandTheSameTitleUp(getLoadingItemNumber()));
  }
  
  public boolean onASListViewHandleExtentOptional(ASListView paramASListView, int paramInt) {
    return false;
  }
  
  protected boolean onBackPressed() {
    clear();
    getNavigationController().popViewController();
    TelnetClient.getClient().sendKeyboardInputToServerInBackground(256, 1);
    PageContainer.getInstance().cleanBoardPage();
    return true;
  }
  
  public void onBlockButtonClicked(final String aBlockName) {
    ASAlertDialog.createDialog().setTitle("加入黑名單").setMessage("是否要將\"" + aBlockName + "\"加入黑名單?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() {
          final BoardPage this$0;
          
          final String val$aBlockName;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            if (param1Int == 1) {
              BoardPage.this._settings.addBlockName(aBlockName);
              BoardPage.this._settings.notifyDataUpdated();
              BoardPage.this.reloadListView();
            } 
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  public void onBoardExtendOptionalPageDidSelectBookmark(Bookmark paramBookmark) {
    if (paramBookmark != null) {
      this._last_list_action = 1;
      pushCommand((TelnetCommand)new BahamutCommandSearchArticle(paramBookmark.getKeyword(), paramBookmark.getAuthor(), paramBookmark.getMark(), paramBookmark.getGy()));
    } 
  }
  
  public void onBookmarkButtonClicked() {
    getNavigationController().pushViewController((ASViewController)new BoardExtendOptionalPage(getListName(), this));
  }
  
  protected void onDeleteArticle(final int item_number) {
    ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此文章?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() {
          final BoardPage this$0;
          
          final int val$item_number;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            switch (param1Int) {
              default:
                return;
              case 1:
                break;
            } 
            BahamutCommandDeleteArticle bahamutCommandDeleteArticle = new BahamutCommandDeleteArticle(item_number);
            BoardPage.this.pushCommand((TelnetCommand)bahamutCommandDeleteArticle);
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  public void onExternalToolbarClicked() {
    boolean bool = this._settings.isExternalToolbarEnable();
    UserSettings userSettings = this._settings;
    if (!bool) {
      bool = true;
    } else {
      bool = false;
    } 
    userSettings.setExternalToolbarEnable(bool);
    refreshExternalToolbar();
  }
  
  protected boolean onListViewItemLongClicked(View paramView, int paramInt) {
    onListArticle(paramInt + 1);
    return true;
  }
  
  void onMenuClicked() {
    String str1;
    String str2;
    String str3;
    if (this._settings.isBlockListEnable()) {
      str1 = "停用黑名單";
    } else {
      str1 = "啟用黑名單";
    } 
    if (this._settings.isExternalToolbarEnable()) {
      str2 = "隱藏工具列";
    } else {
      str2 = "開啟工具列";
    } 
    ASListDialog aSListDialog = ASListDialog.createDialog();
    if (isBookmarkAvailable()) {
      str3 = "開啟書籤";
    } else {
      str3 = null;
    } 
    aSListDialog.addItem(str3).addItem("搜尋文章").addItem("選擇文章").addItem(str1).addItem("編輯黑名單").addItem(str2).setListener(new ASListDialogItemClickListener() {
          final BoardPage this$0;
          
          public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            switch (param1Int) {
              default:
                return;
              case 0:
                BoardPage.this.onBookmarkButtonClicked();
              case 1:
                BoardPage.this.showSearchArticleDialog();
              case 2:
                BoardPage.this.showSelectArticleDialog();
              case 3:
                BoardPage.this.onChangeBlockStateButtonClicked();
              case 4:
                BoardPage.this.onEditBlockListButtonClicked();
              case 5:
                break;
            } 
            BoardPage.this.onExternalToolbarClicked();
          }
          
          public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            return false;
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  public void onPageDidLoad() {
    super.onPageDidLoad();
    this._settings = new UserSettings(getContext());
    ASListView aSListView = (ASListView)findViewById(2131230835);
    aSListView.extendOptionalDelegate = this;
    aSListView.setEmptyView(findViewById(2131230834));
    setListView((ListView)aSListView);
    ((Button)findViewById(2131230837)).setOnClickListener(this.mPostListener);
    ((Button)findViewById(2131230817)).setOnClickListener(this.mFirstPageClicked);
    ((Button)findViewById(2131230833)).setOnClickListener(this.mLastPageClicked);
    Button button = (Button)findViewById(2131231036);
    if (button != null)
      button.setOnClickListener(this.mSearchByKeywordListener); 
    button = (Button)findViewById(2131231037);
    if (button != null)
      button.setOnClickListener(this.mSearchByNumberListener); 
    button = (Button)findViewById(2131231015);
    if (button != null)
      button.setOnClickListener(this.mOpenBookmarkListener); 
    ((TelnetHeaderItemView)findViewById(2131230818)).setMenuButton(this.mMenuButtonListener);
    refreshHeaderView();
    refreshExternalToolbar();
  }
  
  public void onPageRefresh() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: invokespecial onPageRefresh : ()V
    //   6: aload_0
    //   7: getfield _refresh_header_view : Z
    //   10: ifeq -> 22
    //   13: aload_0
    //   14: invokespecial refreshHeaderView : ()V
    //   17: aload_0
    //   18: iconst_0
    //   19: putfield _refresh_header_view : Z
    //   22: aload_0
    //   23: monitorexit
    //   24: return
    //   25: astore_1
    //   26: aload_0
    //   27: monitorexit
    //   28: aload_1
    //   29: athrow
    // Exception table:
    //   from	to	target	type
    //   2	22	25	finally
  }
  
  protected void onPostButtonClicked() {
    PostArticlePage postArticlePage = new PostArticlePage();
    postArticlePage.setBoardPage(this);
    postArticlePage.setListener(this);
    getNavigationController().pushViewController((ASViewController)postArticlePage);
  }
  
  public void onPostDialogEditButtonClicked(PostArticlePage paramPostArticlePage, String paramString1, String paramString2, String paramString3) {
    pushCommand((TelnetCommand)new BahamutCommandEditArticle(paramString1, paramString2, paramString3));
  }
  
  public void onPostDialogSendButtonClicked(PostArticlePage paramPostArticlePage, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5) {
    pushCommand((TelnetCommand)new BahamutCommandPostArticle(this, paramString1, paramString2, paramString3, paramString4, paramString5));
  }
  
  public boolean onReceivedGestureLeft() {
    return super.onReceivedGestureLeft();
  }
  
  public boolean onReceivedGestureRight() {
    onBackPressed();
    ASToast.showShortToast("返回");
    return true;
  }
  
  protected boolean onSearchButtonClicked() {
    showSearchArticleDialog();
    return true;
  }
  
  public void onSearchDialogSearchButtonClickedWithValues(Vector<String> paramVector) {
    String str1;
    String str2 = paramVector.get(0);
    String str3 = paramVector.get(1);
    if (paramVector.get(2) == "YES") {
      str1 = "y";
    } else {
      str1 = "n";
    } 
    searchArticle(str2, str3, str1, paramVector.get(3));
  }
  
  public void onSelectDialogDismissWIthIndex(String paramString) {
    int i = -1;
    try {
      int j = Integer.parseInt(paramString);
      i = j - 1;
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
    if (i >= 0)
      setListViewSelection(i); 
  }
  
  public void prepareInitial() {
    this._initialed = false;
  }
  
  public void recoverPost() {
    (new ASRunner() {
        final BoardPage this$0;
        
        public void run() {
          PostArticlePage postArticlePage = new PostArticlePage();
          postArticlePage.recover = true;
          postArticlePage.setBoardPage(BoardPage.this);
          postArticlePage.setListener(BoardPage.this);
          BoardPage.this.getNavigationController().pushViewController((ASViewController)postArticlePage);
        }
      }).runInMainThread();
  }
  
  public void recycleBlock(TelnetListPageBlock paramTelnetListPageBlock) {
    BoardPageBlock.recycle((BoardPageBlock)paramTelnetListPageBlock);
  }
  
  public void recycleItem(TelnetListPageItem paramTelnetListPageItem) {
    BoardPageItem.recycle((BoardPageItem)paramTelnetListPageItem);
  }
  
  public void setBoardManager(String paramString) {
    this._board_manager = paramString;
  }
  
  public void setBoardTitle(String paramString) {
    this._board_title = paramString;
  }
  
  protected void showSelectArticleDialog() {
    Dialog_SelectArticle dialog_SelectArticle = new Dialog_SelectArticle();
    dialog_SelectArticle.setListener(this);
    dialog_SelectArticle.show();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BoardPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */