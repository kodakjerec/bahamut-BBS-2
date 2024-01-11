package com.kumi.Bahamut.Pages;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.UI.ASListView;
import com.kumi.ASFramework.UI.ASScrollView;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.PageContainer;
import com.kumi.Telnet.TelnetArticle;
import com.kumi.Telnet.TelnetArticleItem;
import com.kumi.Telnet.TelnetClient;
import com.kumi.TelnetUI.TelnetPage;
import com.kumi.TelnetUI.TelnetView;

public class MailPage extends TelnetPage implements ListAdapter, View.OnClickListener, SendMailPage_Listener {
  private TelnetArticle _article = null;
  
  private Button _back_button = null;
  
  private ASListView _list = null;
  
  private Button _page_down_button = null;
  
  private Button _page_up_button = null;
  
  private TelnetView _telnet_view = null;
  
  private ASScrollView _telnet_view_block = null;
  
  private ArticleViewMode _view_mode = ArticleViewMode.MODE_TEXT;
  
  private final DataSetObservable mDataSetObservable = new DataSetObservable();
  
  private void onPageDownButtonClicked() {
    if (TelnetClient.getConnector().isConnecting()) {
      PageContainer.getInstance().getMailBoxPage().loadNextArticle();
      return;
    } 
    showConnectionClosedToast();
  }
  
  private void onPageUpButtonClicked() {
    if (TelnetClient.getConnector().isConnecting()) {
      PageContainer.getInstance().getMailBoxPage().loadPreviousArticle();
      return;
    } 
    showConnectionClosedToast();
  }
  
  private void onReplyButtonClicked() {
    SendMailPage sendMailPage = new SendMailPage();
    String str2 = this._article.generateReplyTitle();
    String str1 = this._article.generateReplyContent();
    sendMailPage.setPostTitle(str2);
    sendMailPage.setPostContent(str1);
    sendMailPage.setReceiver(this._article.Author);
    sendMailPage.setListener(this);
    getNavigationController().pushViewController((ASViewController)sendMailPage);
  }
  
  private void showConnectionClosedToast() {
    ASToast.showShortToast("連線已中斷");
  }
  
  public boolean areAllItemsEnabled() {
    return false;
  }
  
  public void changeViewMode() {
    if (this._view_mode == ArticleViewMode.MODE_TEXT) {
      this._view_mode = ArticleViewMode.MODE_TELNET;
    } else {
      this._view_mode = ArticleViewMode.MODE_TEXT;
    } 
    if (this._view_mode == ArticleViewMode.MODE_TEXT) {
      this._list.setVisibility(0);
      this._telnet_view_block.setVisibility(8);
      return;
    } 
    this._list.setVisibility(8);
    this._telnet_view_block.setVisibility(0);
    this._telnet_view_block.invalidate();
  }
  
  public void clear() {
    this._article = null;
  }
  
  public int getCount() {
    int i = 0;
    if (this._article != null)
      i = this._article.getItemSize() + 2; 
    return i;
  }
  
  public TelnetArticleItem getItem(int paramInt) {
    return this._article.getItem(paramInt - 1);
  }
  
  public long getItemId(int paramInt) {
    return paramInt;
  }
  
  public int getItemViewType(int paramInt) {
    return (paramInt == 0) ? 2 : ((paramInt == getCount() - 1) ? 3 : getItem(paramInt).getType());
  }
  
  public int getPageLayout() {
    return 2131361846;
  }
  
  public int getPageType() {
    return 15;
  }
  
  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    // Byte code:
    //   0: aload_0
    //   1: iload_1
    //   2: invokevirtual getItemViewType : (I)I
    //   5: istore #4
    //   7: aload_2
    //   8: astore_3
    //   9: aload_2
    //   10: ifnonnull -> 46
    //   13: iload #4
    //   15: tableswitch default -> 44, 0 -> 82, 1 -> 97, 2 -> 112, 3 -> 127
    //   44: aload_2
    //   45: astore_3
    //   46: iload #4
    //   48: tableswitch default -> 80, 0 -> 142, 1 -> 212, 2 -> 259, 3 -> 346
    //   80: aload_3
    //   81: areturn
    //   82: new com/kumi/Bahamut/Pages/Article/ArticlePage_TextItemView
    //   85: dup
    //   86: aload_0
    //   87: invokevirtual getContext : ()Landroid/content/Context;
    //   90: invokespecial <init> : (Landroid/content/Context;)V
    //   93: astore_3
    //   94: goto -> 46
    //   97: new com/kumi/Bahamut/Pages/Article/ArticlePage_TelnetItemView
    //   100: dup
    //   101: aload_0
    //   102: invokevirtual getContext : ()Landroid/content/Context;
    //   105: invokespecial <init> : (Landroid/content/Context;)V
    //   108: astore_3
    //   109: goto -> 46
    //   112: new com/kumi/Bahamut/Pages/Article/ArticlePage_HeaderItemView
    //   115: dup
    //   116: aload_0
    //   117: invokevirtual getContext : ()Landroid/content/Context;
    //   120: invokespecial <init> : (Landroid/content/Context;)V
    //   123: astore_3
    //   124: goto -> 46
    //   127: new com/kumi/Bahamut/Pages/Article/ArticlePage_TimeTimeView
    //   130: dup
    //   131: aload_0
    //   132: invokevirtual getContext : ()Landroid/content/Context;
    //   135: invokespecial <init> : (Landroid/content/Context;)V
    //   138: astore_3
    //   139: goto -> 46
    //   142: aload_0
    //   143: iload_1
    //   144: invokevirtual getItem : (I)Lcom/kumi/Telnet/TelnetArticleItem;
    //   147: astore #5
    //   149: aload_3
    //   150: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_TextItemView
    //   153: astore_2
    //   154: aload_2
    //   155: aload #5
    //   157: invokevirtual getAuthor : ()Ljava/lang/String;
    //   160: aload #5
    //   162: invokevirtual getNickname : ()Ljava/lang/String;
    //   165: invokevirtual setAuthor : (Ljava/lang/String;Ljava/lang/String;)V
    //   168: aload_2
    //   169: aload #5
    //   171: invokevirtual getQuoteLevel : ()I
    //   174: invokevirtual setQuote : (I)V
    //   177: aload_2
    //   178: aload #5
    //   180: invokevirtual getContent : ()Ljava/lang/String;
    //   183: invokevirtual setContent : (Ljava/lang/String;)V
    //   186: iload_1
    //   187: aload_0
    //   188: invokevirtual getCount : ()I
    //   191: iconst_2
    //   192: isub
    //   193: if_icmpge -> 204
    //   196: aload_2
    //   197: iconst_0
    //   198: invokevirtual setDividerhidden : (Z)V
    //   201: goto -> 80
    //   204: aload_2
    //   205: iconst_1
    //   206: invokevirtual setDividerhidden : (Z)V
    //   209: goto -> 80
    //   212: aload_0
    //   213: iload_1
    //   214: invokevirtual getItem : (I)Lcom/kumi/Telnet/TelnetArticleItem;
    //   217: astore #5
    //   219: aload_3
    //   220: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_TelnetItemView
    //   223: astore_2
    //   224: aload_2
    //   225: aload #5
    //   227: invokevirtual getFrame : ()Lcom/kumi/Telnet/Model/TelnetFrame;
    //   230: invokevirtual setFrame : (Lcom/kumi/Telnet/Model/TelnetFrame;)V
    //   233: iload_1
    //   234: aload_0
    //   235: invokevirtual getCount : ()I
    //   238: iconst_2
    //   239: isub
    //   240: if_icmpge -> 251
    //   243: aload_2
    //   244: iconst_0
    //   245: invokevirtual setDividerhidden : (Z)V
    //   248: goto -> 80
    //   251: aload_2
    //   252: iconst_1
    //   253: invokevirtual setDividerhidden : (Z)V
    //   256: goto -> 80
    //   259: aload_3
    //   260: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_HeaderItemView
    //   263: astore #6
    //   265: aload_0
    //   266: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   269: getfield Author : Ljava/lang/String;
    //   272: astore #5
    //   274: aload #5
    //   276: astore_2
    //   277: aload_0
    //   278: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   281: getfield Nickname : Ljava/lang/String;
    //   284: ifnull -> 323
    //   287: new java/lang/StringBuilder
    //   290: dup
    //   291: invokespecial <init> : ()V
    //   294: aload #5
    //   296: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   299: ldc '('
    //   301: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   304: aload_0
    //   305: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   308: getfield Nickname : Ljava/lang/String;
    //   311: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   314: ldc ')'
    //   316: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   319: invokevirtual toString : ()Ljava/lang/String;
    //   322: astore_2
    //   323: aload #6
    //   325: aload_0
    //   326: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   329: getfield Title : Ljava/lang/String;
    //   332: aload_2
    //   333: aload_0
    //   334: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   337: getfield BoardName : Ljava/lang/String;
    //   340: invokevirtual setData : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    //   343: goto -> 80
    //   346: aload_3
    //   347: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_TimeTimeView
    //   350: new java/lang/StringBuilder
    //   353: dup
    //   354: invokespecial <init> : ()V
    //   357: ldc_w '《'
    //   360: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   363: aload_0
    //   364: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   367: getfield DateTime : Ljava/lang/String;
    //   370: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   373: ldc_w '》'
    //   376: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   379: invokevirtual toString : ()Ljava/lang/String;
    //   382: invokevirtual setTime : (Ljava/lang/String;)V
    //   385: goto -> 80
  }
  
  public int getViewTypeCount() {
    return 4;
  }
  
  public boolean hasStableIds() {
    return false;
  }
  
  public boolean isEmpty() {
    return false;
  }
  
  public boolean isEnabled(int paramInt) {
    return false;
  }
  
  public boolean isKeepOnOffline() {
    return true;
  }
  
  public boolean isPopupPage() {
    return true;
  }
  
  protected boolean onBackPressed() {
    clear();
    return super.onBackPressed();
  }
  
  public void onClick(View paramView) {
    if (paramView == this._back_button) {
      onReplyButtonClicked();
      return;
    } 
    if (paramView == this._page_up_button) {
      onPageUpButtonClicked();
      return;
    } 
    if (paramView == this._page_down_button) {
      onPageDownButtonClicked();
      return;
    } 
    if (paramView.getId() == 2131230882)
      changeViewMode(); 
  }
  
  protected boolean onMenuButtonClicked() {
    changeViewMode();
    return true;
  }
  
  public void onPageDidDisappear() {
    this._back_button = null;
    this._page_up_button = null;
    this._page_down_button = null;
    this._list = null;
    this._telnet_view_block = null;
    this._telnet_view = null;
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    this._telnet_view_block = (ASScrollView)findViewById(2131230885);
    this._telnet_view = (TelnetView)findViewById(2131230884);
    int i = (int)TypedValue.applyDimension(1, 20.0F, getContext().getResources().getDisplayMetrics()) / 2;
    ViewGroup.LayoutParams layoutParams = this._telnet_view.getLayoutParams();
    layoutParams.width = i * 80;
    layoutParams.height = -2;
    this._telnet_view.setLayoutParams(layoutParams);
    this._list = (ASListView)findViewById(2131230883);
    this._back_button = (Button)findViewById(2131230881);
    this._page_up_button = (Button)findViewById(2131230888);
    this._page_down_button = (Button)findViewById(2131230887);
    Button button = (Button)findViewById(2131230882);
    this._back_button.setOnClickListener(this);
    this._page_up_button.setOnClickListener(this);
    this._page_down_button.setOnClickListener(this);
    button.setOnClickListener(this);
    resetAdapter();
  }
  
  public boolean onReceivedGestureRight() {
    if (this._view_mode == ArticleViewMode.MODE_TEXT)
      onBackPressed(); 
    return true;
  }
  
  public void onSendMailDialogSendButtonClicked(SendMailPage paramSendMailPage, String paramString1, String paramString2, String paramString3) {
    PageContainer.getInstance().getMailBoxPage().onSendMailDialogSendButtonClicked(paramSendMailPage, paramString1, paramString2, paramString3);
    onBackPressed();
  }
  
  public void refresh() {}
  
  public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.registerObserver(paramDataSetObserver);
  }
  
  public void resetAdapter() {
    if (this._article != null)
      this._list.setAdapter(this); 
  }
  
  public void setArticle(TelnetArticle paramTelnetArticle) {
    clear();
    this._article = paramTelnetArticle;
    this._telnet_view.setFrame(this._article.getFrame());
    this._telnet_view.setLayoutParams(this._telnet_view.getLayoutParams());
    this._telnet_view_block.scrollTo(0, 0);
    resetAdapter();
  }
  
  public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.unregisterObserver(paramDataSetObserver);
  }
  
  enum ArticleViewMode {
    MODE_TELNET, MODE_TEXT;
    
    private static final ArticleViewMode[] $VALUES;
    
    static {
      $VALUES = new ArticleViewMode[] { MODE_TEXT, MODE_TELNET };
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\MailPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */