package com.kumi.Bahamut;

import android.content.Context;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.DataModels.AppDatabase;
import com.kumi.Bahamut.Pages.BoardLinkPage;
import com.kumi.Bahamut.Pages.BoardPage;
import com.kumi.Bahamut.Pages.BoardSearchPage;
import com.kumi.Bahamut.Pages.ClassPage;
import com.kumi.Bahamut.Pages.LoginPage;
import com.kumi.Bahamut.Pages.MailBoxPage;
import com.kumi.Bahamut.Pages.MainPage;
import com.kumi.Telnet.Logic.Article_Handler;
import com.kumi.Telnet.Logic.SearchBoard_Handler;
import com.kumi.Telnet.Model.TelnetRow;
import com.kumi.Telnet.TelnetArticle;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetCursor;
import com.kumi.Telnet.TelnetOutputBuilder;
import com.kumi.Telnet.TelnetStateHandler;
import com.kumi.Telnet.TelnetUtils;
import com.kumi.TelnetUI.TelnetPage;
import com.kumi.TextEncoder.B2UEncoder;
import java.io.ByteArrayOutputStream;

public class BahamutStateHandler extends TelnetStateHandler {
  private static final int STEP_CONNECTING = 0;
  
  private static final int STEP_WORKING = 1;
  
  public static final int UNKNOWN = -1;
  
  private static BahamutStateHandler _instance = null;
  
  private Article_Handler _article_handler = new Article_Handler();
  
  public String _article_number;
  
  private TelnetCursor _cursor = null;
  
  private boolean _reading_article = false;
  
  private int _step = 0;
  
  private String first_header = "";
  
  private String last_header = "";
  
  private String row_string_00 = "";
  
  private String row_string_01 = "";
  
  private String row_string_02 = "";
  
  private String row_string_23 = "";
  
  private String cutOffContinueMessage(String paramString) {
    // Byte code:
    //   0: ldc ''
    //   2: astore #5
    //   4: iconst_0
    //   5: istore_2
    //   6: aload_1
    //   7: invokevirtual length : ()I
    //   10: iconst_1
    //   11: isub
    //   12: istore #4
    //   14: aload_1
    //   15: invokevirtual toCharArray : ()[C
    //   18: astore #6
    //   20: aload #6
    //   22: iload_2
    //   23: caload
    //   24: sipush #9733
    //   27: if_icmpeq -> 42
    //   30: iload #4
    //   32: istore_3
    //   33: aload #6
    //   35: iload_2
    //   36: caload
    //   37: bipush #32
    //   39: if_icmpne -> 58
    //   42: iload #4
    //   44: istore_3
    //   45: iload_2
    //   46: aload #6
    //   48: arraylength
    //   49: if_icmpge -> 58
    //   52: iinc #2, 1
    //   55: goto -> 20
    //   58: aload #6
    //   60: iload_3
    //   61: caload
    //   62: bipush #91
    //   64: if_icmpeq -> 77
    //   67: iload_3
    //   68: iflt -> 77
    //   71: iinc #3, -1
    //   74: goto -> 58
    //   77: iload_3
    //   78: iload_2
    //   79: if_icmple -> 93
    //   82: aload_1
    //   83: iload_2
    //   84: iload_3
    //   85: invokevirtual substring : (II)Ljava/lang/String;
    //   88: invokevirtual trim : ()Ljava/lang/String;
    //   91: astore #5
    //   93: aload #5
    //   95: areturn
  }
  
  private boolean detectMessage() {
    byte b;
    int i;
    ByteArrayOutputStream byteArrayOutputStream;
    try {
      i = this._cursor.column;
      TelnetRow telnetRow = TelnetClient.getModel().getRow(23);
      byteArrayOutputStream = new ByteArrayOutputStream();
      this(80);
      ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
      this(80);
      byte b2 = -1;
      byte b1 = 0;
      while (true) {
        b = b2;
        if (b1 < telnetRow.data.length) {
          b = telnetRow.backgroundColor[b1];
          byte b3 = telnetRow.data[b1];
          if (b == 6) {
            byteArrayOutputStream.write(b3);
          } else if (b == 5) {
            byteArrayOutputStream1.write(b3);
          } else {
            b = b1;
            break;
          } 
          b1++;
          continue;
        } 
        break;
      } 
    } catch (Exception exception) {
      exception.printStackTrace();
      return false;
    } 
    if (byteArrayOutputStream.size() > 0 && exception.size() > 0) {
      String str2 = B2UEncoder.getInstance().encodeToString(byteArrayOutputStream.toByteArray());
      String str1 = B2UEncoder.getInstance().encodeToString(exception.toByteArray());
      if (b == i && str2.startsWith("★")) {
        String str = str2.substring(1, str2.length() - 1);
        str2 = str1.substring(1);
        AppDatabase appDatabase = new AppDatabase();
        this((Context)MyApplication.getInstance());
        appDatabase.saveMessage(str, str2);
        appDatabase.loadMessages();
        return true;
      } 
    } 
    return false;
  }
  
  public static BahamutStateHandler getInstance() {
    if (_instance == null)
      _instance = new BahamutStateHandler(); 
    return _instance;
  }
  
  private void loadState() {
    this.row_string_00 = getRowString(0).trim();
    this.row_string_01 = getRowString(1).trim();
    this.row_string_02 = getRowString(2).trim();
    this.row_string_23 = getRowString(23).trim();
    this.first_header = TelnetUtils.getHeader(this.row_string_00);
    this.last_header = TelnetUtils.getHeader(this.row_string_23);
  }
  
  private void onReadArticleFinished() {
    this._article_handler.loadLastPage(TelnetClient.getModel());
    this._article_handler.build();
    TelnetArticle telnetArticle = this._article_handler.getArticle();
    this._article_handler.newArticle();
    PageContainer.getInstance().getBoardPage();
    if (this._article_number != null)
      telnetArticle.Number = Integer.parseInt(this._article_number); 
    if (this.row_string_23.startsWith("魚雁往返")) {
      showMail(telnetArticle);
    } else {
      showArticle(telnetArticle);
    } 
    this._reading_article = false;
  }
  
  private void onReadArticlePage() {
    this._article_handler.loadPage(TelnetClient.getModel());
    cleanFrame();
  }
  
  private void onReadArticleStart() {
    this._reading_article = true;
    this._article_handler.clear();
  }
  
  private boolean pass_1() {
    boolean bool = true;
    if (this.row_string_23.startsWith("您有一篇文章尚未完成")) {
      TelnetClient.getClient().sendStringToServer("S\n1\n");
      bool = false;
    } 
    if (bool && this.row_string_23.endsWith("[請按任意鍵繼續]") && getCurrentPage() != 1) {
      String str = cutOffContinueMessage(this.row_string_23);
      if (str.length() > 0)
        ASToast.showShortToast(str); 
      if (this.row_string_23.startsWith("★ 引言太多")) {
        byte[] arrayOfByte = TelnetOutputBuilder.create().pushKey(32).pushKey(24).pushString("a\n\n").build();
        TelnetClient.getClient().sendDataToServer(arrayOfByte);
        TelnetPage telnetPage = (TelnetPage)ASNavigationController.getCurrentController().getTopController();
        if (telnetPage instanceof BoardPage) {
          PageContainer.getInstance().getBoardPage().recoverPost();
        } else if (telnetPage instanceof MailBoxPage) {
          PageContainer.getInstance().getMailBoxPage().recoverPost();
        } 
        return false;
      } 
      TelnetClient.getClient().sendStringToServer("");
      return false;
    } 
    if (this.row_string_23.equals("要新增資料嗎？(Y/N) [N]")) {
      ASToast.showShortToast("此看板無文章");
      TelnetClient.getClient().sendStringToServer("N");
      return false;
    } 
    if (this.row_string_23.equals("● 請按任意鍵繼續 ●")) {
      TelnetClient.getClient().sendKeyboardInputToServer(256);
      bool = false;
    } 
    return bool;
  }
  
  private void printState() {
    System.out.println("v********************************************************************************v");
    System.out.println("Current Page:" + getCurrentPage());
    String str = "\n";
    for (byte b = 0; b < 24; b++) {
      str = str + String.format("%1$02d.%2$s\n", new Object[] { Integer.valueOf(b + 1), TelnetClient.getModel().getRow(b).getRawString() });
    } 
    System.out.println("content:" + str);
    System.out.println("cursor:" + TelnetClient.getModel().getCursor().toString());
    System.out.println("^********************************************************************************^");
  }
  
  private void showArticle(final TelnetArticle aArticle) {
    (new ASRunner() {
        final BahamutStateHandler this$0;
        
        final TelnetArticle val$aArticle;
        
        public void run() {
          try {
            PageContainer.getInstance().getArticlePage().setArticle(aArticle);
          } catch (Exception exception) {
            exception.printStackTrace();
          } 
        }
      }).runInMainThread();
  }
  
  private void showMail(final TelnetArticle aArticle) {
    (new ASRunner() {
        final BahamutStateHandler this$0;
        
        final TelnetArticle val$aArticle;
        
        public void run() {
          // Byte code:
          //   0: invokestatic getCurrentController : ()Lcom/kumi/ASFramework/PageController/ASNavigationController;
          //   3: invokevirtual getViewControllers : ()Ljava/util/Vector;
          //   6: invokevirtual lastElement : ()Ljava/lang/Object;
          //   9: checkcast com/kumi/TelnetUI/TelnetPage
          //   12: astore_1
          //   13: aload_1
          //   14: ifnull -> 74
          //   17: aload_1
          //   18: invokevirtual getPageType : ()I
          //   21: bipush #15
          //   23: if_icmpne -> 74
          //   26: aload_1
          //   27: checkcast com/kumi/Bahamut/Pages/MailPage
          //   30: astore_1
          //   31: aload_1
          //   32: ifnonnull -> 71
          //   35: new com/kumi/Bahamut/Pages/MailPage
          //   38: astore_1
          //   39: aload_1
          //   40: invokespecial <init> : ()V
          //   43: invokestatic getCurrentController : ()Lcom/kumi/ASFramework/PageController/ASNavigationController;
          //   46: aload_1
          //   47: invokevirtual pushViewController : (Lcom/kumi/ASFramework/PageController/ASViewController;)V
          //   50: aload_1
          //   51: aload_0
          //   52: getfield val$aArticle : Lcom/kumi/Telnet/TelnetArticle;
          //   55: invokevirtual setArticle : (Lcom/kumi/Telnet/TelnetArticle;)V
          //   58: return
          //   59: astore_1
          //   60: aload_1
          //   61: invokevirtual printStackTrace : ()V
          //   64: goto -> 58
          //   67: astore_1
          //   68: goto -> 60
          //   71: goto -> 50
          //   74: aconst_null
          //   75: astore_1
          //   76: goto -> 31
          // Exception table:
          //   from	to	target	type
          //   0	13	59	java/lang/Exception
          //   17	31	59	java/lang/Exception
          //   35	43	67	java/lang/Exception
          //   43	50	59	java/lang/Exception
          //   50	58	59	java/lang/Exception
        }
      }).runInMainThread();
  }
  
  public void clear() {
    this._step = 0;
    setCurrentPage(-1);
  }
  
  public void handleArticle() {
    setCurrentPage(14);
    if (!this._reading_article)
      onReadArticleStart(); 
  }
  
  public void handleBoardPage() {
    setCurrentPage(10);
    if (this._cursor.column == 1) {
      BoardPage boardPage = PageContainer.getInstance().getBoardPage();
      if (boardPage.onPagePreload())
        showPage((TelnetPage)boardPage); 
    } 
  }
  
  public void handleBoardSearchPage() {
    setCurrentPage(13);
    if (this._cursor.column == 1) {
      BoardSearchPage boardSearchPage = PageContainer.getInstance().getBoard_Search_Page();
      if (boardSearchPage.onPagePreload())
        showPage((TelnetPage)boardSearchPage); 
    } 
  }
  
  public void handleBoardTitleLinkedPage() {
    setCurrentPage(12);
    if (this._cursor.column == 1) {
      BoardLinkPage boardLinkPage = PageContainer.getInstance().getBoard_Linked_Title_Page();
      if (boardLinkPage.onPagePreload())
        showPage((TelnetPage)boardLinkPage); 
    } 
  }
  
  public void handleClassPage() {
    setCurrentPage(6);
    if (this._cursor.column == 1) {
      ClassPage classPage = PageContainer.getInstance().getClassPage();
      if (classPage != null && classPage.onPagePreload())
        showPage((TelnetPage)classPage); 
    } 
  }
  
  public void handleLoginPage() {
    setCurrentPage(1);
    LoginPage loginPage = PageContainer.getInstance().getLoginPage();
    if (loginPage.onPagePreload())
      showPage((TelnetPage)loginPage); 
  }
  
  public void handleMailBoxPage() {
    setCurrentPage(9);
    if (this._cursor.column == 1) {
      MailBoxPage mailBoxPage = PageContainer.getInstance().getMailBoxPage();
      if (mailBoxPage.onPagePreload())
        showPage((TelnetPage)mailBoxPage); 
    } 
  }
  
  public void handleMainPage() {
    this._step = 1;
    if (getCurrentPage() < 5)
      PageContainer.getInstance().getLoginPage().onLoginSuccess(); 
    setCurrentPage(5);
    MainPage mainPage = PageContainer.getInstance().getMainPage();
    if (mainPage.onPagePreload())
      showPage((TelnetPage)mainPage); 
    if (this.last_header.equals("本次")) {
      (new ASRunner() {
          final BahamutStateHandler this$0;
          
          public void run() {
            MainPage mainPage = PageContainer.getInstance().getMainPage();
            if (mainPage.isTopPage())
              mainPage.onProcessHotMessage(); 
          }
        }).runInMainThread();
      return;
    } 
    if (this.last_header.equals("G)"))
      (new ASRunner() {
          final BahamutStateHandler this$0;
          
          public void run() {
            MainPage mainPage = PageContainer.getInstance().getMainPage();
            if (mainPage.isTopPage())
              mainPage.onCheckGoodbye(); 
          }
        }).runInMainThread(); 
  }
  
  public void handleSearchBoard() {
    if (this.row_string_23.startsWith("★ 列表") && this._cursor.equals(23, 29)) {
      SearchBoard_Handler.getInstance().read();
      TelnetClient.getClient().sendKeyboardInputToServer(67);
      return;
    } 
    if (this._cursor.row == 1) {
      SearchBoard_Handler.getInstance().read();
      byte[] arrayOfByte = TelnetOutputBuilder.create().pushData((byte)25).pushString("\n\n").build();
      TelnetClient.getClient().sendDataToServer(arrayOfByte);
      (new ASRunner() {
          final BahamutStateHandler this$0;
          
          public void run() {
            PageContainer.getInstance().getClassPage().onSearchBoardFinished();
          }
        }).runInMainThread();
    } 
  }
  
  public void handleState() {
    loadState();
    this._cursor = TelnetClient.getModel().getCursor();
    if (pass_1()) {
      if (getCurrentPage() == 6 && this.row_string_23.startsWith("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
        TelnetClient.getClient().sendKeyboardInputToServer(256);
        return;
      } 
    } else {
      return;
    } 
    if (getCurrentPage() > 9 && this.row_string_23.startsWith("文章選讀") && this.row_string_23.endsWith("搜尋作者")) {
      handleArticle();
      onReadArticleFinished();
      TelnetClient.getClient().sendKeyboardInputToServer(256);
      return;
    } 
    if (getCurrentPage() > 6 && this.row_string_23.startsWith("魚雁往返") && this.row_string_23.endsWith("標記")) {
      handleArticle();
      onReadArticleFinished();
      TelnetClient.getClient().sendKeyboardInputToServer(256);
      return;
    } 
    if (getCurrentPage() > 6 && this.row_string_23.startsWith("瀏覽 P.") && this.row_string_23.endsWith("結束")) {
      handleArticle();
      onReadArticlePage();
      TelnetClient.getClient().sendKeyboardInputToServerInBackground(261, 1);
      return;
    } 
    if (this.first_header.equals("對戰") && getCurrentPage() < 5) {
      handleLoginPage();
      return;
    } 
    if (this.row_string_00.startsWith("【主功能表】")) {
      handleMainPage();
      return;
    } 
    if (this.row_string_00.startsWith("【郵件選單】")) {
      handleMailBoxPage();
      return;
    } 
    if (this.row_string_00.startsWith("【看板列表】")) {
      if (this.row_string_01.startsWith("請輸入看板名稱")) {
        handleSearchBoard();
        return;
      } 
      if (this.row_string_02.startsWith("總數")) {
        TelnetClient.getClient().sendKeyboardInputToServer(99);
        return;
      } 
      handleClassPage();
      return;
    } 
    if (this.row_string_00.startsWith("【主題串列】")) {
      if (PageContainer.getInstance().getBoardPage().getLastListAction() == 1) {
        handleBoardSearchPage();
        return;
      } 
      handleBoardTitleLinkedPage();
      return;
    } 
    if (this.row_string_00.startsWith("【板主：")) {
      handleBoardPage();
      return;
    } 
    if (this.row_string_23.startsWith("您要刪除上述記錄嗎")) {
      TelnetClient.getClient().sendStringToServer("n");
      return;
    } 
    if (this.row_string_23.equals("● 請按任意鍵繼續 ●")) {
      TelnetClient.getClient().sendKeyboardInputToServer(256);
      return;
    } 
    if (this.last_header.equals("您想")) {
      (new ASRunner() {
          final BahamutStateHandler this$0;
          
          public void run() {
            LoginPage loginPage = PageContainer.getInstance().getLoginPage();
            if (loginPage.isTopPage())
              loginPage.onSaveArticle(); 
          }
        }).runInMainThread();
      return;
    } 
    if (this.row_string_23.startsWith("★ 請閱讀最新公告")) {
      TelnetClient.getClient().sendStringToServer("");
      return;
    } 
    if (this._step == 0 && this.first_header.equals("--")) {
      setCurrentPage(3);
      if (this.last_header.equals("●請") || this.last_header.equals("請按"))
        TelnetClient.getClient().sendStringToServer(""); 
      return;
    } 
    if (this._step == 0 && this.first_header.equals("□□")) {
      setCurrentPage(2);
      if (this.last_header.equals("●請") || this.last_header.equals("請按"))
        TelnetClient.getClient().sendStringToServer(""); 
      return;
    } 
    if (this.first_header.equals("【過")) {
      setCurrentPage(4);
      if (this.last_header.equals("●請") || this.last_header.equals("請按"))
        TelnetClient.getClient().sendStringToServer(""); 
    } 
  }
  
  public void setArticleNumber(String paramString) {
    this._article_number = paramString;
  }
  
  public void showPage(TelnetPage paramTelnetPage) {
    final TelnetPage top_page = (TelnetPage)ASNavigationController.getCurrentController().getTopController();
    if (paramTelnetPage == telnetPage) {
      (new ASRunner() {
          final BahamutStateHandler this$0;
          
          final TelnetPage val$top_page;
          
          public void run() {
            top_page.requestPageRefresh();
          }
        }).runInMainThread();
      return;
    } 
    if (!telnetPage.isPopupPage() && paramTelnetPage != null) {
      if (ASNavigationController.getCurrentController().containsViewController((ASViewController)paramTelnetPage)) {
        ASNavigationController.getCurrentController().popToViewController((ASViewController)paramTelnetPage);
        return;
      } 
      ASNavigationController.getCurrentController().pushViewController((ASViewController)paramTelnetPage);
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\BahamutStateHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */