package com.kumi.Bahamut.Pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kumi.Bahamut.Command.BahamutCommandGoodArticle;
import com.kumi.Bahamut.Command.BahamutCommandSearchArticle;
import com.kumi.Bahamut.Command.BahamutCommandSendMail;
import com.kumi.Bahamut.Command.TelnetCommand;
import com.kumi.Bahamut.Dialogs.Dialog_SearchArticle_Listener;
import com.kumi.Bahamut.Dialogs.Dialog_SelectArticle;
import com.kumi.Bahamut.Dialogs.Dialog_SelectArticle_Listener;
import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Bahamut.ListPage.TelnetListPageItem;
import com.kumi.Bahamut.Pages.Model.MailBoxPageBlock;
import com.kumi.Bahamut.Pages.Model.MailBoxPageHandler;
import com.kumi.Bahamut.Pages.Model.MailBoxPageItem;
import com.kumi.Telnet.Logic.ItemUtils;
import com.kumi.Telnet.TelnetOutputBuilder;
import com.kumi.TelnetUI.TelnetHeaderItemView;
import java.util.Vector;

public class MailBoxPage extends TelnetListPage implements ListAdapter, Dialog_SearchArticle_Listener, Dialog_SelectArticle_Listener, SendMailPage_Listener, View.OnClickListener, View.OnLongClickListener {
  private Button _back_button = null;
  
  private TelnetHeaderItemView _header_view = null;
  
  private View _list_empty_view = null;
  
  private Button _page_down_button = null;
  
  private Button _page_up_button = null;
  
  private void onDeleteArticle(final int itemIndex) {
    ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此信件?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() {
          final MailBoxPage this$0;
          
          final int val$itemIndex;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            switch (param1Int) {
              default:
                return;
              case 1:
                break;
            } 
            BahamutCommandDeleteArticle bahamutCommandDeleteArticle = new BahamutCommandDeleteArticle(itemIndex);
            MailBoxPage.this.pushCommand((TelnetCommand)bahamutCommandDeleteArticle);
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  private void onPostButtonClicked() {
    SendMailPage sendMailPage = new SendMailPage();
    sendMailPage.setListener(this);
    getNavigationController().pushViewController((ASViewController)sendMailPage);
  }
  
  private void showSelectArticleDialog() {
    Dialog_SelectArticle dialog_SelectArticle = new Dialog_SelectArticle();
    dialog_SelectArticle.setListener(this);
    dialog_SelectArticle.show();
  }
  
  public void clear() {
    super.clear();
  }
  
  public String getListName() {
    return "[MailBox]";
  }
  
  public int getPageLayout() {
    return 2131361844;
  }
  
  public int getPageType() {
    return 9;
  }
  
  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    MailBoxPage_ItemView mailBoxPage_ItemView2;
    int i = paramInt + 1;
    int j = ItemUtils.getBlock(i);
    MailBoxPageItem mailBoxPageItem = (MailBoxPageItem)getItem(paramInt);
    if (mailBoxPageItem == null && getCurrentBlock() != j && !isLoadingBlock(i))
      loadBoardBlock(j); 
    View view = paramView;
    if (paramView == null) {
      mailBoxPage_ItemView2 = new MailBoxPage_ItemView(getContext());
      mailBoxPage_ItemView2.setLayoutParams((ViewGroup.LayoutParams)new AbsListView.LayoutParams(-1, -2));
    } 
    MailBoxPage_ItemView mailBoxPage_ItemView1 = mailBoxPage_ItemView2;
    mailBoxPage_ItemView1.setItem(mailBoxPageItem);
    mailBoxPage_ItemView1.setIndex(paramInt + 1);
    return (View)mailBoxPage_ItemView2;
  }
  
  public void goodArticle(final int articleIndex) {
    ASAlertDialog.createDialog().setTitle("推薦").setMessage("是否要推薦此文章?").addButton("取消").addButton("推薦").setListener(new ASAlertDialogListener() {
          final MailBoxPage this$0;
          
          final int val$articleIndex;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            switch (param1Int) {
              default:
                return;
              case 1:
                break;
            } 
            BahamutCommandGoodArticle bahamutCommandGoodArticle = new BahamutCommandGoodArticle(articleIndex);
            MailBoxPage.this.pushCommand((TelnetCommand)bahamutCommandGoodArticle);
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  public void goodLoadingArticle() {
    goodArticle(getLoadingItemNumber());
  }
  
  public boolean isAutoLoadEnable() {
    return false;
  }
  
  public void loadFirstArticle() {
    if (getLoadingItemNumber() == 1) {
      ASToast.showShortToast("已讀至列首");
      return;
    } 
    loadItemAtNumber(1);
  }
  
  public void loadLastestArticle() {
    if (getLoadingItemNumber() == getItemSize()) {
      ASToast.showShortToast("已讀至列尾");
      return;
    } 
    loadItemAtNumber(getItemSize());
  }
  
  public void loadNextArticle() {
    int i = getLoadingItemNumber() + 1;
    if (i > getItemSize()) {
      ASToast.showShortToast("已讀至列尾");
      return;
    } 
    loadItemAtNumber(i);
  }
  
  public TelnetListPageBlock loadPage() {
    return (TelnetListPageBlock)MailBoxPageHandler.getInstance().load();
  }
  
  public void loadPreviousArticle() {
    int i = getLoadingItemNumber() - 1;
    if (i < 1) {
      ASToast.showShortToast("已讀至列首");
      return;
    } 
    loadItemAtNumber(i);
  }
  
  protected boolean onBackPressed() {
    clear();
    getNavigationController().popViewController();
    TelnetOutputBuilder.create().pushKey(256).pushKey(256).sendToServerInBackground(1);
    return true;
  }
  
  public void onClick(View paramView) {
    switch (paramView.getId()) {
      default:
        return;
      case 2131230881:
        onPostButtonClicked();
      case 2131230888:
        moveToFirstPosition();
      case 2131230887:
        setManualLoadPage();
        moveToLastPosition();
      case 2131230889:
        break;
    } 
    showSelectArticleDialog();
  }
  
  public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
    onDeleteArticle(paramInt + 1);
    return true;
  }
  
  public boolean onLongClick(View paramView) {
    boolean bool2 = false;
    boolean bool1 = bool2;
    switch (paramView.getId()) {
      default:
        bool1 = bool2;
      case 2131230881:
        return bool1;
      case 2131230888:
        bool1 = true;
      case 2131230887:
        break;
    } 
    bool1 = true;
  }
  
  public void onPageDidDisappear() {
    this._back_button = null;
    this._page_up_button = null;
    this._page_down_button = null;
    this._header_view = null;
    this._list_empty_view = null;
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    super.onPageDidLoad();
    ListView listView = (ListView)findViewById(2131230878);
    this._list_empty_view = findViewById(2131230877);
    listView.setEmptyView(this._list_empty_view);
    setListView(listView);
    this._back_button = (Button)findViewById(2131230881);
    this._back_button.setOnClickListener(this);
    this._back_button.setOnLongClickListener(this);
    this._page_up_button = (Button)findViewById(2131230888);
    this._page_up_button.setOnClickListener(this);
    this._page_up_button.setOnLongClickListener(this);
    this._page_down_button = (Button)findViewById(2131230887);
    this._page_down_button.setOnClickListener(this);
    this._page_down_button.setOnLongClickListener(this);
    ((Button)findViewById(2131230889)).setOnClickListener(this);
    this._header_view = (TelnetHeaderItemView)findViewById(2131230880);
  }
  
  public void onPageRefresh() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: invokespecial onPageRefresh : ()V
    //   6: aload_0
    //   7: getfield _header_view : Lcom/kumi/TelnetUI/TelnetHeaderItemView;
    //   10: astore_1
    //   11: new java/lang/StringBuilder
    //   14: astore_2
    //   15: aload_2
    //   16: invokespecial <init> : ()V
    //   19: aload_1
    //   20: ldc_w '我的信箱'
    //   23: aload_2
    //   24: ldc_w '您有 '
    //   27: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   30: aload_0
    //   31: invokevirtual getItemSize : ()I
    //   34: invokevirtual append : (I)Ljava/lang/StringBuilder;
    //   37: ldc_w ' 封信在信箱內'
    //   40: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   43: invokevirtual toString : ()Ljava/lang/String;
    //   46: ldc_w ''
    //   49: invokevirtual setData : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    //   52: aload_0
    //   53: monitorexit
    //   54: return
    //   55: astore_1
    //   56: aload_0
    //   57: monitorexit
    //   58: aload_1
    //   59: athrow
    // Exception table:
    //   from	to	target	type
    //   2	52	55	finally
  }
  
  public boolean onReceivedGestureRight() {
    onBackPressed();
    ASToast.showShortToast("返回");
    return true;
  }
  
  protected boolean onSearchButtonClicked() {
    showSelectArticleDialog();
    return true;
  }
  
  public void onSearchDialogSearchButtonClickedWithValues(Vector<String> paramVector) {
    String str1;
    String str3 = paramVector.get(0);
    String str2 = paramVector.get(1);
    if (paramVector.get(2) == "YES") {
      str1 = "y";
    } else {
      str1 = "n";
    } 
    pushCommand((TelnetCommand)new BahamutCommandSearchArticle(str3, str2, str1, paramVector.get(3)));
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
  
  public void onSendMailDialogSendButtonClicked(SendMailPage paramSendMailPage, String paramString1, String paramString2, String paramString3) {
    pushCommand((TelnetCommand)new BahamutCommandSendMail(paramString1, paramString2, paramString3));
  }
  
  public void recoverPost() {
    (new ASRunner() {
        final MailBoxPage this$0;
        
        public void run() {}
      }).runInMainThread();
  }
  
  public void recycleBlock(TelnetListPageBlock paramTelnetListPageBlock) {
    MailBoxPageBlock.recycle((MailBoxPageBlock)paramTelnetListPageBlock);
  }
  
  public void recycleItem(TelnetListPageItem paramTelnetListPageItem) {
    MailBoxPageItem.recycle((MailBoxPageItem)paramTelnetListPageItem);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\MailBoxPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */