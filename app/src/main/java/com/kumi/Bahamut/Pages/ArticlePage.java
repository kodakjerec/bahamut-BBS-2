package com.kumi.Bahamut.Pages;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.Dialog.ASListDialog;
import com.kumi.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kumi.ASFramework.Dialog.ASProcessingDialog;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.UI.ASListView;
import com.kumi.ASFramework.UI.ASScrollView;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.Command.BahamutCommandDeleteArticle;
import com.kumi.Bahamut.Command.TelnetCommand;
import com.kumi.Bahamut.DataModels.BookmarkStore;
import com.kumi.Bahamut.PageContainer;
import com.kumi.Telnet.TelnetArticle;
import com.kumi.Telnet.TelnetArticleItem;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;
import com.kumi.TelnetUI.TelnetView;
import java.util.HashSet;

public class ArticlePage extends TelnetPage {
  long _action_delay = 500L;
  
  private TelnetArticle _article = null;
  
  View.OnClickListener _back_listener = new View.OnClickListener() {
      final ArticlePage this$0;
      
      public void onClick(View param1View) {
        if (TelnetClient.getConnector().isConnecting()) {
          if (ArticlePage.this._article != null) {
            PostArticlePage postArticlePage = new PostArticlePage();
            String str1 = ArticlePage.this._article.generateReplyTitle();
            ArticlePage.this._article.setBlockList(ArticlePage.this._settings.getBlockListLowCasedString());
            String str2 = ArticlePage.this._article.generateReplyContent();
            postArticlePage.setBoardPage(ArticlePage.this._board_page);
            postArticlePage.setOperationMode(PostArticlePage.OperationMode.Reply);
            postArticlePage.setArticleNumber(String.valueOf(ArticlePage.this._article.Number));
            postArticlePage.setPostTitle(str1);
            postArticlePage.setPostContent(str2 + "\n\n\n");
            postArticlePage.setListener(ArticlePage.this._board_page);
            postArticlePage.setHeaderHidden(true);
            ArticlePage.this.getNavigationController().pushViewController((ASViewController)postArticlePage);
          } 
          return;
        } 
        ArticlePage.this.showConnectionClosedToast();
      }
    };
  
  private BoardPage _board_page = null;
  
  Runnable _bottom_action = null;
  
  private boolean _full_screen = false;
  
  BaseAdapter _list_adapter = new BaseAdapter() {
      final ArticlePage this$0;
      
      public boolean areAllItemsEnabled() {
        return false;
      }
      
      public int getCount() {
        int i = 0;
        if (ArticlePage.this._article != null)
          i = ArticlePage.this._article.getItemSize() + 2; 
        return i;
      }
      
      public TelnetArticleItem getItem(int param1Int) {
        return (ArticlePage.this._article == null) ? null : ArticlePage.this._article.getItem(param1Int - 1);
      }
      
      public long getItemId(int param1Int) {
        return param1Int;
      }
      
      public int getItemViewType(int param1Int) {
        return (param1Int == 0) ? 2 : ((param1Int == getCount() - 1) ? 3 : getItem(param1Int).getType());
      }
      
      public View getView(int param1Int, View param1View, ViewGroup param1ViewGroup) {
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
        //   15: tableswitch default -> 44, 0 -> 82, 1 -> 100, 2 -> 118, 3 -> 150
        //   44: aload_2
        //   45: astore_3
        //   46: iload #4
        //   48: tableswitch default -> 80, 0 -> 168, 1 -> 284, 2 -> 333, 3 -> 489
        //   80: aload_3
        //   81: areturn
        //   82: new com/kumi/Bahamut/Pages/Article/ArticlePage_TextItemView
        //   85: dup
        //   86: aload_0
        //   87: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   90: invokevirtual getContext : ()Landroid/content/Context;
        //   93: invokespecial <init> : (Landroid/content/Context;)V
        //   96: astore_3
        //   97: goto -> 46
        //   100: new com/kumi/Bahamut/Pages/Article/ArticlePage_TelnetItemView
        //   103: dup
        //   104: aload_0
        //   105: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   108: invokevirtual getContext : ()Landroid/content/Context;
        //   111: invokespecial <init> : (Landroid/content/Context;)V
        //   114: astore_3
        //   115: goto -> 46
        //   118: new com/kumi/Bahamut/Pages/Article/ArticlePage_HeaderItemView
        //   121: dup
        //   122: aload_0
        //   123: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   126: invokevirtual getContext : ()Landroid/content/Context;
        //   129: invokespecial <init> : (Landroid/content/Context;)V
        //   132: astore_3
        //   133: aload_3
        //   134: new android/widget/AbsListView$LayoutParams
        //   137: dup
        //   138: iconst_m1
        //   139: bipush #-2
        //   141: invokespecial <init> : (II)V
        //   144: invokevirtual setLayoutParams : (Landroid/view/ViewGroup$LayoutParams;)V
        //   147: goto -> 46
        //   150: new com/kumi/Bahamut/Pages/Article/ArticlePage_TimeTimeView
        //   153: dup
        //   154: aload_0
        //   155: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   158: invokevirtual getContext : ()Landroid/content/Context;
        //   161: invokespecial <init> : (Landroid/content/Context;)V
        //   164: astore_3
        //   165: goto -> 46
        //   168: aload_0
        //   169: iload_1
        //   170: invokevirtual getItem : (I)Lcom/kumi/Telnet/TelnetArticleItem;
        //   173: astore_2
        //   174: aload_3
        //   175: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_TextItemView
        //   178: astore #5
        //   180: aload #5
        //   182: aload_2
        //   183: invokevirtual getAuthor : ()Ljava/lang/String;
        //   186: aload_2
        //   187: invokevirtual getNickname : ()Ljava/lang/String;
        //   190: invokevirtual setAuthor : (Ljava/lang/String;Ljava/lang/String;)V
        //   193: aload #5
        //   195: aload_2
        //   196: invokevirtual getQuoteLevel : ()I
        //   199: invokevirtual setQuote : (I)V
        //   202: aload #5
        //   204: aload_2
        //   205: invokevirtual getContent : ()Ljava/lang/String;
        //   208: invokevirtual setContent : (Ljava/lang/String;)V
        //   211: iload_1
        //   212: aload_0
        //   213: invokevirtual getCount : ()I
        //   216: iconst_2
        //   217: isub
        //   218: if_icmpge -> 266
        //   221: aload #5
        //   223: iconst_0
        //   224: invokevirtual setDividerhidden : (Z)V
        //   227: aload_0
        //   228: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   231: getfield _settings : Lcom/kumi/Telnet/UserSettings;
        //   234: invokevirtual isBlockListEnable : ()Z
        //   237: ifeq -> 275
        //   240: aload_0
        //   241: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   244: getfield _settings : Lcom/kumi/Telnet/UserSettings;
        //   247: aload_2
        //   248: invokevirtual getAuthor : ()Ljava/lang/String;
        //   251: invokevirtual isBlockListContains : (Ljava/lang/String;)Z
        //   254: ifeq -> 275
        //   257: aload #5
        //   259: iconst_0
        //   260: invokevirtual setVisible : (Z)V
        //   263: goto -> 80
        //   266: aload #5
        //   268: iconst_1
        //   269: invokevirtual setDividerhidden : (Z)V
        //   272: goto -> 227
        //   275: aload #5
        //   277: iconst_1
        //   278: invokevirtual setVisible : (Z)V
        //   281: goto -> 80
        //   284: aload_0
        //   285: iload_1
        //   286: invokevirtual getItem : (I)Lcom/kumi/Telnet/TelnetArticleItem;
        //   289: astore_2
        //   290: aload_3
        //   291: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_TelnetItemView
        //   294: astore #5
        //   296: aload #5
        //   298: aload_2
        //   299: invokevirtual getFrame : ()Lcom/kumi/Telnet/Model/TelnetFrame;
        //   302: invokevirtual setFrame : (Lcom/kumi/Telnet/Model/TelnetFrame;)V
        //   305: iload_1
        //   306: aload_0
        //   307: invokevirtual getCount : ()I
        //   310: iconst_2
        //   311: isub
        //   312: if_icmpge -> 324
        //   315: aload #5
        //   317: iconst_0
        //   318: invokevirtual setDividerhidden : (Z)V
        //   321: goto -> 80
        //   324: aload #5
        //   326: iconst_1
        //   327: invokevirtual setDividerhidden : (Z)V
        //   330: goto -> 80
        //   333: aload_3
        //   334: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_HeaderItemView
        //   337: astore #10
        //   339: aconst_null
        //   340: astore_2
        //   341: aconst_null
        //   342: astore #6
        //   344: aconst_null
        //   345: astore #5
        //   347: aload_0
        //   348: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   351: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   354: ifnull -> 464
        //   357: aload_0
        //   358: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   361: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   364: getfield Author : Ljava/lang/String;
        //   367: astore #9
        //   369: aload_0
        //   370: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   373: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   376: getfield Title : Ljava/lang/String;
        //   379: astore #8
        //   381: aload_0
        //   382: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   385: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   388: getfield BoardName : Ljava/lang/String;
        //   391: astore #7
        //   393: aload #9
        //   395: astore_2
        //   396: aload #7
        //   398: astore #5
        //   400: aload #8
        //   402: astore #6
        //   404: aload_0
        //   405: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   408: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   411: getfield Nickname : Ljava/lang/String;
        //   414: ifnull -> 464
        //   417: new java/lang/StringBuilder
        //   420: dup
        //   421: invokespecial <init> : ()V
        //   424: aload #9
        //   426: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   429: ldc '('
        //   431: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   434: aload_0
        //   435: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   438: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   441: getfield Nickname : Ljava/lang/String;
        //   444: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   447: ldc ')'
        //   449: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   452: invokevirtual toString : ()Ljava/lang/String;
        //   455: astore_2
        //   456: aload #8
        //   458: astore #6
        //   460: aload #7
        //   462: astore #5
        //   464: aload #10
        //   466: aload #6
        //   468: aload_2
        //   469: aload #5
        //   471: invokevirtual setData : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
        //   474: aload #10
        //   476: aload_0
        //   477: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   480: invokestatic access$400 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Landroid/view/View$OnClickListener;
        //   483: invokevirtual setMenuButton : (Landroid/view/View$OnClickListener;)V
        //   486: goto -> 80
        //   489: aload_0
        //   490: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   493: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   496: ifnull -> 80
        //   499: aload_3
        //   500: checkcast com/kumi/Bahamut/Pages/Article/ArticlePage_TimeTimeView
        //   503: new java/lang/StringBuilder
        //   506: dup
        //   507: invokespecial <init> : ()V
        //   510: ldc '《'
        //   512: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   515: aload_0
        //   516: getfield this$0 : Lcom/kumi/Bahamut/Pages/ArticlePage;
        //   519: invokestatic access$300 : (Lcom/kumi/Bahamut/Pages/ArticlePage;)Lcom/kumi/Telnet/TelnetArticle;
        //   522: getfield DateTime : Ljava/lang/String;
        //   525: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   528: ldc '》'
        //   530: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   533: invokevirtual toString : ()Ljava/lang/String;
        //   536: invokevirtual setTime : (Ljava/lang/String;)V
        //   539: goto -> 80
      }
      
      public int getViewTypeCount() {
        return 4;
      }
      
      public boolean hasStableIds() {
        return false;
      }
      
      public boolean isEmpty() {
        return (getCount() == 0);
      }
      
      public boolean isEnabled(int param1Int) {
        boolean bool = true;
        param1Int = getItemViewType(param1Int);
        null = bool;
        if (param1Int != 0) {
          if (param1Int == 1)
            return bool; 
        } else {
          return null;
        } 
        return false;
      }
    };
  
  AdapterView.OnItemLongClickListener _list_long_click_listener = new AdapterView.OnItemLongClickListener() {
      final ArticlePage this$0;
      
      public boolean onItemLongClick(AdapterView<?> param1AdapterView, View param1View, int param1Int, long param1Long) {
        boolean bool2 = false;
        if (ArticlePage.this._article == null)
          return bool2; 
        TelnetArticleItem telnetArticleItem = ArticlePage.this._article.getItem(param1Int - 1);
        boolean bool1 = bool2;
        if (telnetArticleItem != null) {
          param1Int = telnetArticleItem.getType();
          if (param1Int == 0) {
            telnetArticleItem.setType(1);
            ArticlePage.this._list_adapter.notifyDataSetChanged();
            return true;
          } 
          bool1 = bool2;
          if (param1Int == 1) {
            telnetArticleItem.setType(0);
            ArticlePage.this._list_adapter.notifyDataSetChanged();
            bool1 = true;
          } 
        } 
        return bool1;
      }
    };
  
  View.OnLongClickListener _page_bottom_listener = new View.OnLongClickListener() {
      final ArticlePage this$0;
      
      public boolean onLongClick(View param1View) {
        if (!ArticlePage.this._settings.isArticleMoveDisable()) {
          if (ArticlePage.this._bottom_action != null) {
            param1View.removeCallbacks(ArticlePage.this._bottom_action);
            ArticlePage.this._bottom_action = null;
          } 
          ArticlePage.this._bottom_action = new Runnable() {
              final ArticlePage.null this$1;
              
              public void run() {
                ArticlePage.this._bottom_action = null;
                ArticlePage.this.moveToBottomArticle();
              }
            };
          param1View.postDelayed(ArticlePage.this._bottom_action, ArticlePage.this._action_delay);
        } 
        return false;
      }
    };
  
  View.OnClickListener _page_down_listener = new View.OnClickListener() {
      final ArticlePage this$0;
      
      public void onClick(View param1View) {
        if (ArticlePage.this._bottom_action != null) {
          param1View.removeCallbacks(ArticlePage.this._bottom_action);
          ArticlePage.this._bottom_action = null;
        } 
        if (TelnetClient.getConnector().isConnecting() && ArticlePage.this._board_page != null) {
          ArticlePage.this._board_page.loadTheSameTitleDown();
          return;
        } 
        ArticlePage.this.showConnectionClosedToast();
      }
    };
  
  View.OnLongClickListener _page_top_listener = new View.OnLongClickListener() {
      final ArticlePage this$0;
      
      public boolean onLongClick(View param1View) {
        if (!ArticlePage.this._settings.isArticleMoveDisable()) {
          if (ArticlePage.this._top_action != null) {
            param1View.removeCallbacks(ArticlePage.this._top_action);
            ArticlePage.this._top_action = null;
          } 
          ArticlePage.this._top_action = new Runnable() {
              final ArticlePage.null this$1;
              
              public void run() {
                ArticlePage.this._top_action = null;
                ArticlePage.this.moveToTopArticle();
              }
            };
          param1View.postDelayed(ArticlePage.this._top_action, ArticlePage.this._action_delay);
        } 
        return false;
      }
    };
  
  View.OnClickListener _page_up_listener = new View.OnClickListener() {
      final ArticlePage this$0;
      
      public void onClick(View param1View) {
        if (ArticlePage.this._top_action != null) {
          param1View.removeCallbacks(ArticlePage.this._top_action);
          ArticlePage.this._top_action = null;
        } 
        if (TelnetClient.getConnector().isConnecting() && ArticlePage.this._board_page != null) {
          ArticlePage.this._board_page.loadTheSameTitleUp();
          return;
        } 
        ArticlePage.this.showConnectionClosedToast();
      }
    };
  
  UserSettings _settings;
  
  private TelnetView _telnet_view = null;
  
  Runnable _top_action = null;
  
  private View.OnClickListener mChangeModeListener = new View.OnClickListener() {
      final ArticlePage this$0;
      
      public void onClick(View param1View) {
        ArticlePage.this.changeViewMode();
        ArticlePage.this.refreshExternalToolbar();
      }
    };
  
  private View.OnClickListener mDoGyListener = new View.OnClickListener() {
      final ArticlePage this$0;
      
      public void onClick(View param1View) {
        ArticlePage.this.onGYButtonClicked();
      }
    };
  
  private View.OnClickListener mMenuListener = new View.OnClickListener() {
      final ArticlePage this$0;
      
      public void onClick(View param1View) {
        ArticlePage.this.onMenuClicked();
      }
    };
  
  private View.OnClickListener mShowLinkListener = new View.OnClickListener() {
      final ArticlePage this$0;
      
      public void onClick(View param1View) {
        ArticlePage.this.onOpenLinkClicked();
      }
    };
  
  private void onGYButtonClicked() {
    if (this._board_page != null)
      this._board_page.goodLoadingArticle(); 
  }
  
  private void onOpenLinkClicked() {
    final String[] urls;
    byte b = 0;
    if (this._article != null) {
      arrayOfString = this._article.getUrls();
      if (arrayOfString.length == 0) {
        Context context = getContext();
        if (context != null)
          Toast.makeText(context, "此文章內容未包含連結", 0).show(); 
        return;
      } 
    } else {
      return;
    } 
    ASListDialog aSListDialog = ASListDialog.createDialog();
    int i = arrayOfString.length;
    while (b < i) {
      aSListDialog.addItem(arrayOfString[b]);
      b++;
    } 
    aSListDialog.setListener(new ASListDialogItemClickListener() {
          final ArticlePage this$0;
          
          final String[] val$urls;
          
          public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            param1String = urls[param1Int];
            String str = param1String;
            if (!param1String.startsWith("http://")) {
              str = param1String;
              if (!param1String.startsWith("https://")) {
                str = param1String;
                if (!param1String.startsWith("ftp://"))
                  if (param1String.matches("([a-zA-Z0-9\\-]+:[a-zA-Z0-9\\-]+@)([a-zA-Z0-9\\-]+)(\\.[a-zA-Z0-9\\-]+){1,9}([/\\\\]([a-zA-Z0-9\\-]+))?([a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+){0,1}){0,1}(\\?([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)|(([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)(&([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?))+)){0,1}")) {
                    str = "ftp://" + param1String;
                  } else if (param1String.matches("([a-zA-Z0-9\\-]+@)([a-zA-Z0-9\\-]+)(\\.[a-zA-Z0-9\\-]+){1,9}([/\\\\]([a-zA-Z0-9\\-]+))?([a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+){0,1}){0,1}(\\?([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)|(([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?)(&([a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-%&;#]?))+)){0,1}")) {
                    str = "mailto:" + param1String;
                  } else {
                    str = "http://" + param1String;
                  }  
              } 
            } 
            Context context = ArticlePage.this.getContext();
            if (context != null)
              context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str))); 
          }
          
          public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            return false;
          }
        });
    aSListDialog.show();
  }
  
  private void refreshExternalToolbar() {
    boolean bool = this._settings.isExternalToolbarEnable();
    int i = this._settings.getArticleViewMode();
    if (i == 1)
      bool = true; 
    System.out.println("enable:" + bool);
    System.out.println("article_mode:" + i);
    View view = findViewById(2131230988);
    if (view != null) {
      if (bool) {
        i = 0;
      } else {
        i = 8;
      } 
      view.setVisibility(i);
    } 
  }
  
  private void reloadTelnetLayout() {
    int i;
    int j = (int)TypedValue.applyDimension(2, 20.0F, getContext().getResources().getDisplayMetrics()) / 2 * 80;
    if (getNavigationController().getCurrentOrientation() == 2) {
      i = getNavigationController().getScreenHeight();
    } else {
      i = getNavigationController().getScreenWidth();
    } 
    if (j <= i) {
      i = -1;
      this._full_screen = true;
    } else {
      this._full_screen = false;
      i = j;
    } 
    ViewGroup.LayoutParams layoutParams = this._telnet_view.getLayoutParams();
    layoutParams.width = i;
    layoutParams.height = -2;
    this._telnet_view.setLayoutParams(layoutParams);
  }
  
  private void reloadViewMode() {
    ViewGroup viewGroup = (ViewGroup)findViewById(2131230743);
    ASScrollView aSScrollView = (ASScrollView)findViewById(2131230739);
    if (this._settings.getArticleViewMode() == 0) {
      if (viewGroup != null)
        viewGroup.setVisibility(0); 
      if (aSScrollView != null)
        aSScrollView.setVisibility(8); 
      return;
    } 
    if (viewGroup != null)
      viewGroup.setVisibility(8); 
    if (aSScrollView != null) {
      aSScrollView.setVisibility(0);
      aSScrollView.invalidate();
    } 
  }
  
  private void showConnectionClosedToast() {
    ASToast.showShortToast("連線已中斷");
  }
  
  public void changeViewMode() {
    this._settings.exchangeArticleViewMode();
    this._settings.notifyDataUpdated();
    reloadViewMode();
  }
  
  public int getPageLayout() {
    return 2131361820;
  }
  
  public int getPageType() {
    return 14;
  }
  
  public boolean isKeepOnOffline() {
    return true;
  }
  
  public boolean isPopupPage() {
    return true;
  }
  
  void moveToBottomArticle() {
    if (TelnetClient.getConnector().isConnecting() && this._board_page != null) {
      this._board_page.loadTheSameTitleBottom();
      return;
    } 
    showConnectionClosedToast();
  }
  
  void moveToTopArticle() {
    if (TelnetClient.getConnector().isConnecting() && this._board_page != null) {
      this._board_page.loadTheSameTitleTop();
      return;
    } 
    showConnectionClosedToast();
  }
  
  public void onAddBlackListClicked() {
    TelnetArticle telnetArticle = this._article;
    if (telnetArticle != null) {
      HashSet<String> hashSet = new HashSet();
      byte b = 0;
      int i = telnetArticle.getItemSize();
      while (b < i) {
        String str = telnetArticle.getItem(b).getAuthor();
        if (str != null && !this._settings.isBlockListContains(str))
          hashSet.add(str); 
        b++;
      } 
      if (hashSet.size() == 0) {
        ASAlertDialog.createDialog().setMessage("無可加入黑名單的ID").show();
        return;
      } 
      final String[] names = hashSet.<String>toArray(new String[hashSet.size()]);
      ASListDialog.createDialog().addItems(arrayOfString).setListener(new ASListDialogItemClickListener() {
            final ArticlePage this$0;
            
            final String[] val$names;
            
            public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
              ArticlePage.this.onBlockButtonClicked(names[param1Int]);
            }
            
            public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
              return false;
            }
          }).show();
    } 
  }
  
  protected boolean onBackPressed() {
    getNavigationController().popViewController();
    PageContainer.getInstance().cleanArticlePage();
    return true;
  }
  
  public void onBlockButtonClicked(final String aBlockName) {
    ASAlertDialog.createDialog().setTitle("加入黑名單").setMessage("是否要將\"" + aBlockName + "\"加入黑名單?").addButton("取消").addButton("加入").setListener(new ASAlertDialogListener() {
          final ArticlePage this$0;
          
          final String val$aBlockName;
          
          public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
            if (param1Int == 1) {
              ArticlePage.this._settings.addBlockName(aBlockName);
              ArticlePage.this._settings.notifyDataUpdated();
              if (ArticlePage.this._settings.isBlockListEnable()) {
                if (aBlockName == ArticlePage.this._article.Author) {
                  ArticlePage.this.onBackPressed();
                  return;
                } 
              } else {
                return;
              } 
            } else {
              return;
            } 
            ArticlePage.this._list_adapter.notifyDataSetChanged();
          }
        }).scheduleDismissOnPageDisappear((ASViewController)this).show();
  }
  
  public void onDeleteButtonClicked() {
    if (this._article != null && this._board_page != null) {
      final int item_number = this._article.Number;
      ASAlertDialog.createDialog().setTitle("刪除").setMessage("是否確定要刪除此文章?").addButton("取消").addButton("刪除").setListener(new ASAlertDialogListener() {
            final ArticlePage this$0;
            
            final int val$item_number;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              switch (param1Int) {
                default:
                  return;
                case 1:
                  break;
              } 
              BahamutCommandDeleteArticle bahamutCommandDeleteArticle = new BahamutCommandDeleteArticle(item_number);
              ArticlePage.this._board_page.pushCommand((TelnetCommand)bahamutCommandDeleteArticle);
              ArticlePage.this.onBackPressed();
            }
          }).scheduleDismissOnPageDisappear((ASViewController)this).show();
    } 
  }
  
  public void onEditButtonClicked() {
    if (this._article != null) {
      PostArticlePage postArticlePage = new PostArticlePage();
      String str2 = this._article.generateEditTitle();
      String str3 = this._article.generateEditContent();
      String str1 = this._article.generatrEditFormat();
      postArticlePage.setBoardPage(this._board_page);
      postArticlePage.setArticleNumber(String.valueOf(this._article.Number));
      postArticlePage.setOperationMode(PostArticlePage.OperationMode.Edit);
      postArticlePage.setPostTitle(str2);
      postArticlePage.setPostContent(str3);
      postArticlePage.setEditFormat(str1);
      postArticlePage.setListener(this._board_page);
      postArticlePage.setHeaderHidden(true);
      getNavigationController().pushViewController((ASViewController)postArticlePage);
    } 
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
  
  protected boolean onMenuButtonClicked() {
    onMenuClicked();
    return true;
  }
  
  void onMenuClicked() {
    String str = null;
    if (this._article != null && this._article.Author != null) {
      boolean bool;
      String str1;
      String str3 = this._article.Author.toLowerCase();
      String str4 = this._settings.getUsername().trim().toLowerCase();
      if (this._board_page.getPageType() == 10) {
        bool = true;
      } else {
        bool = false;
      } 
      if (this._settings.isExternalToolbarEnable()) {
        str1 = "隱藏工具列";
      } else {
        str1 = "開啟工具列";
      } 
      ASListDialog aSListDialog = ASListDialog.createDialog().addItem("推薦").addItem("切換模式");
      if (bool && str3.equals(str4)) {
        str2 = "編輯文章";
      } else {
        str2 = null;
      } 
      aSListDialog = aSListDialog.addItem(str2);
      String str2 = str;
      if (str3.equals(str4))
        str2 = "刪除文章"; 
      aSListDialog.addItem(str2).addItem(str1).addItem("加入黑名單").addItem("開啟連結").setListener(new ASListDialogItemClickListener() {
            final ArticlePage this$0;
            
            public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
              switch (param1Int) {
                default:
                  return;
                case 0:
                  ArticlePage.this.onGYButtonClicked();
                case 1:
                  ArticlePage.this.changeViewMode();
                  ArticlePage.this.refreshExternalToolbar();
                case 2:
                  ArticlePage.this.onEditButtonClicked();
                case 3:
                  ArticlePage.this.onDeleteButtonClicked();
                case 4:
                  ArticlePage.this.onExternalToolbarClicked();
                case 5:
                  ArticlePage.this.onAddBlackListClicked();
                case 6:
                  break;
              } 
              ArticlePage.this.onOpenLinkClicked();
            }
            
            public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
              return false;
            }
          }).scheduleDismissOnPageDisappear((ASViewController)this).show();
    } 
  }
  
  public void onPageDidDisappear() {
    this._telnet_view = null;
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    this._settings = new UserSettings(getContext());
    this._telnet_view = (TelnetView)findViewById(2131230738);
    reloadTelnetLayout();
    View view = findViewById(2131230736);
    ASListView aSListView = (ASListView)findViewById(2131230737);
    aSListView.setEmptyView(view);
    aSListView.setAdapter((ListAdapter)this._list_adapter);
    aSListView.setOnItemLongClickListener(this._list_long_click_listener);
    Button button1 = (Button)findViewById(2131230735);
    Button button3 = (Button)findViewById(2131230742);
    Button button2 = (Button)findViewById(2131230741);
    button1.setOnClickListener(this._back_listener);
    button3.setOnClickListener(this._page_up_listener);
    button3.setOnLongClickListener(this._page_top_listener);
    button2.setOnClickListener(this._page_down_listener);
    button2.setOnLongClickListener(this._page_bottom_listener);
    button1 = (Button)findViewById(2131230982);
    if (button1 != null)
      button1.setOnClickListener(this.mDoGyListener); 
    button1 = (Button)findViewById(2131230970);
    if (button1 != null)
      button1.setOnClickListener(this.mChangeModeListener); 
    button1 = (Button)findViewById(2131231051);
    if (button1 != null)
      button1.setOnClickListener(this.mShowLinkListener); 
    if (this._telnet_view.getFrame() == null && this._article != null)
      this._telnet_view.setFrame(this._article.getFrame()); 
    refreshExternalToolbar();
    showNotification();
  }
  
  public void onPageWillAppear() {
    reloadViewMode();
  }
  
  public boolean onReceivedGestureRight() {
    if (this._settings.getArticleViewMode() == 0 || this._full_screen)
      onBackPressed(); 
    return true;
  }
  
  public void setArticle(TelnetArticle paramTelnetArticle) {
    this._article = paramTelnetArticle;
    if (this._article != null) {
      String str = this._board_page.getListName();
      BookmarkStore bookmarkStore = new BookmarkStore(getContext());
      bookmarkStore.getBookmarkList(str).addHistoryBookmark(this._article.Title);
      bookmarkStore.store();
      this._telnet_view.setFrame(this._article.getFrame());
      reloadTelnetLayout();
      ASScrollView aSScrollView = (ASScrollView)findViewById(2131230739);
      if (aSScrollView != null)
        aSScrollView.scrollTo(0, 0); 
      this._list_adapter.notifyDataSetChanged();
    } 
    ASProcessingDialog.hideProcessingDialog();
  }
  
  public void setBoardPage(BoardPage paramBoardPage) {
    this._board_page = paramBoardPage;
  }
  
  void showNotification() {
    ASNavigationController aSNavigationController = getNavigationController();
    if (aSNavigationController != null) {
      SharedPreferences sharedPreferences = aSNavigationController.getSharedPreferences("notification", 0);
      if (!sharedPreferences.getBoolean("show_top_bottom_function", false)) {
        Toast.makeText((Context)aSNavigationController, 2131558438, 1).show();
        sharedPreferences.edit().putBoolean("show_top_bottom_function", true).commit();
      } 
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\ArticlePage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */