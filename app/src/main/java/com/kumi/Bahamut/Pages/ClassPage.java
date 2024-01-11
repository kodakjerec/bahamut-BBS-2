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
import com.kumi.ASFramework.Dialog.ASProcessingDialog;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.Dialogs.Dialog_SearchBoard;
import com.kumi.Bahamut.Dialogs.Dialog_SearchBoard_Listener;
import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Bahamut.ListPage.TelnetListPageItem;
import com.kumi.Bahamut.PageContainer;
import com.kumi.Bahamut.Pages.Model.ClassPageBlock;
import com.kumi.Bahamut.Pages.Model.ClassPageHandler;
import com.kumi.Bahamut.Pages.Model.ClassPageItem;
import com.kumi.Telnet.Logic.ItemUtils;
import com.kumi.Telnet.Logic.SearchBoard_Handler;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetOutputBuilder;

public class ClassPage extends TelnetListPage implements View.OnClickListener, Dialog_SearchBoard_Listener {
  private String _detail = "看板列表";
  
  private String _title = "";
  
  private void showSearchBoardDialog() {
    Dialog_SearchBoard dialog_SearchBoard = new Dialog_SearchBoard();
    dialog_SearchBoard.setListener(this);
    dialog_SearchBoard.show();
  }
  
  public String getListIdFromListName(String paramString) {
    return paramString + "[Class]";
  }
  
  public int getPageLayout() {
    return 2131361833;
  }
  
  public int getPageType() {
    return 6;
  }
  
  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    ClassPageItemView classPageItemView;
    int j = paramInt + 1;
    int i = ItemUtils.getBlock(j);
    ClassPageItem classPageItem = (ClassPageItem)getItem(paramInt);
    if (classPageItem == null && getCurrentBlock() != i && !isLoadingBlock(j))
      loadBoardBlock(i); 
    View view = paramView;
    if (paramView == null) {
      classPageItemView = new ClassPageItemView(getContext());
      classPageItemView.setLayoutParams((ViewGroup.LayoutParams)new AbsListView.LayoutParams(-1, -2));
    } 
    classPageItemView.setItem(classPageItem);
    return (View)classPageItemView;
  }
  
  public boolean isAutoLoadEnable() {
    return false;
  }
  
  public void loadItemAtIndex(int paramInt) {
    ClassPageItem classPageItem = (ClassPageItem)getItem(paramInt);
    if (classPageItem.isDirectory) {
      PageContainer.getInstance().pushClassPage(classPageItem.Name, classPageItem.Title);
      getNavigationController().pushViewController((ASViewController)PageContainer.getInstance().getClassPage());
    } else {
      BoardPage boardPage = PageContainer.getInstance().getBoardPage();
      boardPage.prepareInitial();
      getNavigationController().pushViewController((ASViewController)boardPage);
    } 
    super.loadItemAtIndex(paramInt);
  }
  
  public TelnetListPageBlock loadPage() {
    return (TelnetListPageBlock)ClassPageHandler.getInstance().load();
  }
  
  protected boolean onBackPressed() {
    clear();
    PageContainer.getInstance().popClassPage();
    getNavigationController().popViewController();
    TelnetClient.getClient().sendKeyboardInputToServerInBackground(256, 1);
    return true;
  }
  
  public void onClick(View paramView) {
    switch (paramView.getId()) {
      default:
        return;
      case 2131230856:
        onSearchButtonClicked();
      case 2131230843:
        moveToFirstPosition();
      case 2131230852:
        break;
    } 
    moveToLastPosition();
  }
  
  protected boolean onListViewItemLongClicked(View paramView, int paramInt) {
    null = true;
    if (getListName() != null && getListName().equals("Favorite")) {
      ASAlertDialog.createDialog().setMessage("確定要將此看板移出我的最愛?").addButton("取消").addButton("確定").setListener(new ASAlertDialogListener() {
            final ClassPage this$0;
            
            final int val$item_index;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              if (param1Int == 1) {
                String str = String.valueOf(item_index) + "\nd";
                TelnetClient.getClient().sendStringToServerInBackground(str);
                ClassPage.this.loadLastBlock();
              } 
            }
          }).scheduleDismissOnPageDisappear((ASViewController)this).show();
      return null;
    } 
    if (!((ClassPageItem)getItem(paramInt)).isDirectory) {
      ASAlertDialog.createDialog().setMessage("確定要將此看板加入我的最愛?").addButton("取消").addButton("確定").setListener(new ASAlertDialogListener() {
            final ClassPage this$0;
            
            final int val$item_index;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              if (param1Int == 1) {
                String str = String.valueOf(item_index) + "\na";
                TelnetClient.getClient().sendStringToServerInBackground(str);
              } 
            }
          }).show();
      return null;
    } 
    return false;
  }
  
  public void onMenuItemClicked(int paramInt) {
    switch (paramInt) {
      default:
        return;
      case 0:
        showSearchBoardDialog();
      case 1:
        break;
    } 
    TelnetClient.getClient().sendKeyboardInputToServerInBackground(99);
  }
  
  public void onPageDidDisappear() {
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    super.onPageDidLoad();
    ListView listView = (ListView)findViewById(2131230854);
    listView.setEmptyView(findViewById(2131230853));
    setListView(listView);
    ((Button)findViewById(2131230856)).setOnClickListener(this);
    ((Button)findViewById(2131230843)).setOnClickListener(this);
    ((Button)findViewById(2131230852)).setOnClickListener(this);
  }
  
  public void onPageRefresh() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: invokespecial onPageRefresh : ()V
    //   6: aload_0
    //   7: getfield _title : Ljava/lang/String;
    //   10: astore_2
    //   11: aload_2
    //   12: ifnull -> 24
    //   15: aload_2
    //   16: astore_1
    //   17: aload_2
    //   18: invokevirtual length : ()I
    //   21: ifne -> 28
    //   24: ldc_w '讀取中'
    //   27: astore_1
    //   28: aload_0
    //   29: getfield _detail : Ljava/lang/String;
    //   32: astore_3
    //   33: aload_3
    //   34: ifnull -> 46
    //   37: aload_3
    //   38: astore_2
    //   39: aload_3
    //   40: invokevirtual length : ()I
    //   43: ifne -> 50
    //   46: ldc_w '讀取中'
    //   49: astore_2
    //   50: aload_0
    //   51: ldc_w 2131230844
    //   54: invokevirtual findViewById : (I)Landroid/view/View;
    //   57: checkcast com/kumi/TelnetUI/TelnetHeaderItemView
    //   60: astore_3
    //   61: aload_3
    //   62: ifnull -> 73
    //   65: aload_3
    //   66: aload_1
    //   67: aload_2
    //   68: ldc ''
    //   70: invokevirtual setData : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    //   73: aload_0
    //   74: monitorexit
    //   75: return
    //   76: astore_1
    //   77: aload_0
    //   78: monitorexit
    //   79: aload_1
    //   80: athrow
    // Exception table:
    //   from	to	target	type
    //   2	11	76	finally
    //   17	24	76	finally
    //   28	33	76	finally
    //   39	46	76	finally
    //   50	61	76	finally
    //   65	73	76	finally
  }
  
  public boolean onReceivedGestureRight() {
    onBackPressed();
    ASToast.showShortToast("返回");
    return true;
  }
  
  public void onSearchBoardFinished() {
    System.out.println("onSearchBoardFinished");
    ASProcessingDialog.hideProcessingDialog();
    String[] arrayOfString = SearchBoard_Handler.getInstance().getBoards();
    ASListDialog.createDialog().addItems(arrayOfString).setListener(new ASListDialogItemClickListener() {
          final ClassPage this$0;
          
          public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            String str = SearchBoard_Handler.getInstance().getBoard(param1Int);
            if (ClassPage.this.getListName() == "Favorite") {
              ClassPage.this.showAddBoardToFavoriteDialog(str);
              return;
            } 
            TelnetClient.getClient().sendStringToServerInBackground("s" + str);
            SearchBoard_Handler.getInstance().clear();
          }
          
          public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            return false;
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  protected boolean onSearchButtonClicked() {
    showSearchBoardDialog();
    return true;
  }
  
  public void onSearchButtonClickedWithKeyword(String paramString) {
    SearchBoard_Handler.getInstance().clear();
    ASProcessingDialog.showProcessingDialog("搜尋中");
    TelnetOutputBuilder.create().pushString("s" + paramString + " ").sendToServerInBackground();
  }
  
  public void recycleBlock(TelnetListPageBlock paramTelnetListPageBlock) {
    ClassPageBlock.recycle((ClassPageBlock)paramTelnetListPageBlock);
  }
  
  public void recycleItem(TelnetListPageItem paramTelnetListPageItem) {
    ClassPageItem.recycle((ClassPageItem)paramTelnetListPageItem);
  }
  
  public void setClassTitle(String paramString) {
    this._title = paramString;
  }
  
  public void setDetail(String paramString) {
    this._detail = paramString;
  }
  
  public void showAddBoardToFavoriteDialog(final String boardName) {
    ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() {
          final ClassPage this$0;
          
          final String val$boardName;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            if (param1Int == 1) {
              TelnetOutputBuilder.create().pushKey(256).pushString("B\n").pushKey(262).pushString("/" + boardName + "\na ").pushKey(256).pushString("F\ns" + boardName + "\n").sendToServerInBackground();
              return;
            } 
            TelnetClient.getClient().sendStringToServerInBackground("s" + boardName);
            SearchBoard_Handler.getInstance().clear();
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\ClassPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */