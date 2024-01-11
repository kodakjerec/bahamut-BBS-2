package com.kumi.Telnet.Logic;

import com.kumi.Telnet.Model.TelnetModel;
import com.kumi.Telnet.Model.TelnetRow;
import com.kumi.Telnet.TelnetArticle;
import com.kumi.Telnet.TelnetArticlePage;
import java.util.Vector;

public class Article_Handler {
  TelnetArticle _article = new TelnetArticle();
  
  TelnetArticlePage _last_page = null;
  
  Vector<TelnetArticlePage> _pages = new Vector<TelnetArticlePage>();
  
  private void addPage(TelnetArticlePage paramTelnetArticlePage, Vector<TelnetRow> paramVector) {
    if (paramTelnetArticlePage != null)
      for (byte b = 0; b < paramTelnetArticlePage.getRowCount(); b++)
        addRow(paramTelnetArticlePage.getRow(b), paramVector);  
  }
  
  private void addRow(TelnetRow paramTelnetRow, Vector<TelnetRow> paramVector) {
    paramVector.add(paramTelnetRow);
  }
  
  private void buildRows(Vector<TelnetRow> paramVector) {
    synchronized (this._pages) {
      if (this._pages != null && this._pages.size() > 0) {
        int i = this._pages.size();
        for (byte b = 0; b < i; b++)
          addPage(this._pages.get(b), paramVector); 
      } 
      if (this._last_page != null)
        addPage(this._last_page, paramVector); 
      return;
    } 
  }
  
  private boolean loadHeader(Vector<TelnetRow> paramVector) {
    // Byte code:
    //   0: iconst_0
    //   1: istore #6
    //   3: aload_1
    //   4: invokevirtual size : ()I
    //   7: iconst_3
    //   8: if_icmple -> 297
    //   11: aload_1
    //   12: iconst_0
    //   13: invokevirtual get : (I)Ljava/lang/Object;
    //   16: checkcast com/kumi/Telnet/Model/TelnetRow
    //   19: astore #10
    //   21: aload_1
    //   22: iconst_1
    //   23: invokevirtual get : (I)Ljava/lang/Object;
    //   26: checkcast com/kumi/Telnet/Model/TelnetRow
    //   29: astore #11
    //   31: aload_1
    //   32: iconst_2
    //   33: invokevirtual get : (I)Ljava/lang/Object;
    //   36: checkcast com/kumi/Telnet/Model/TelnetRow
    //   39: astore #12
    //   41: aload #10
    //   43: bipush #7
    //   45: bipush #58
    //   47: invokevirtual getSpaceString : (II)Ljava/lang/String;
    //   50: invokevirtual trim : ()Ljava/lang/String;
    //   53: astore #13
    //   55: ldc ''
    //   57: astore #8
    //   59: ldc ''
    //   61: astore #9
    //   63: aload #8
    //   65: astore #7
    //   67: aload #13
    //   69: invokevirtual toCharArray : ()[C
    //   72: astore #14
    //   74: iconst_0
    //   75: istore #4
    //   77: iconst_0
    //   78: istore_2
    //   79: iload #4
    //   81: istore_3
    //   82: aload #8
    //   84: astore #7
    //   86: iload_2
    //   87: aload #14
    //   89: arraylength
    //   90: if_icmpge -> 104
    //   93: aload #14
    //   95: iload_2
    //   96: caload
    //   97: bipush #40
    //   99: if_icmpne -> 300
    //   102: iload_2
    //   103: istore_3
    //   104: aload #8
    //   106: astore_1
    //   107: iload_3
    //   108: ifle -> 126
    //   111: aload #8
    //   113: astore #7
    //   115: aload #13
    //   117: iconst_0
    //   118: iload_3
    //   119: invokevirtual substring : (II)Ljava/lang/String;
    //   122: invokevirtual trim : ()Ljava/lang/String;
    //   125: astore_1
    //   126: iload_3
    //   127: iconst_1
    //   128: iadd
    //   129: istore #5
    //   131: iload #5
    //   133: istore_3
    //   134: aload_1
    //   135: astore #7
    //   137: aload #14
    //   139: arraylength
    //   140: iconst_1
    //   141: isub
    //   142: istore_2
    //   143: iload_3
    //   144: istore #4
    //   146: iload_2
    //   147: iflt -> 162
    //   150: aload #14
    //   152: iload_2
    //   153: caload
    //   154: bipush #41
    //   156: if_icmpne -> 306
    //   159: iload_2
    //   160: istore #4
    //   162: aload_1
    //   163: astore #7
    //   165: aload #9
    //   167: astore #8
    //   169: iload #4
    //   171: iload #5
    //   173: if_icmple -> 196
    //   176: aload_1
    //   177: astore #7
    //   179: aload #13
    //   181: iload #5
    //   183: iload #4
    //   185: invokevirtual substring : (II)Ljava/lang/String;
    //   188: invokevirtual trim : ()Ljava/lang/String;
    //   191: astore #8
    //   193: aload_1
    //   194: astore #7
    //   196: aload_0
    //   197: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   200: aload #7
    //   202: putfield Author : Ljava/lang/String;
    //   205: aload_0
    //   206: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   209: aload #8
    //   211: putfield Nickname : Ljava/lang/String;
    //   214: aload_0
    //   215: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   218: aload #10
    //   220: bipush #66
    //   222: bipush #78
    //   224: invokevirtual getSpaceString : (II)Ljava/lang/String;
    //   227: invokevirtual trim : ()Ljava/lang/String;
    //   230: putfield BoardName : Ljava/lang/String;
    //   233: aload #11
    //   235: bipush #7
    //   237: bipush #78
    //   239: invokevirtual getSpaceString : (II)Ljava/lang/String;
    //   242: invokevirtual trim : ()Ljava/lang/String;
    //   245: astore_1
    //   246: aload_1
    //   247: ldc 'Re: '
    //   249: invokevirtual startsWith : (Ljava/lang/String;)Z
    //   252: ifeq -> 324
    //   255: aload_0
    //   256: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   259: aload_1
    //   260: iconst_4
    //   261: invokevirtual substring : (I)Ljava/lang/String;
    //   264: putfield Title : Ljava/lang/String;
    //   267: aload_0
    //   268: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   271: iconst_0
    //   272: putfield Type : I
    //   275: aload_0
    //   276: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   279: aload #12
    //   281: bipush #7
    //   283: bipush #30
    //   285: invokevirtual getSpaceString : (II)Ljava/lang/String;
    //   288: invokevirtual trim : ()Ljava/lang/String;
    //   291: putfield DateTime : Ljava/lang/String;
    //   294: iconst_1
    //   295: istore #6
    //   297: iload #6
    //   299: ireturn
    //   300: iinc #2, 1
    //   303: goto -> 79
    //   306: iinc #2, -1
    //   309: goto -> 143
    //   312: astore_1
    //   313: aload_1
    //   314: invokevirtual printStackTrace : ()V
    //   317: aload #9
    //   319: astore #8
    //   321: goto -> 196
    //   324: aload_0
    //   325: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   328: aload_1
    //   329: putfield Title : Ljava/lang/String;
    //   332: aload_0
    //   333: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   336: iconst_0
    //   337: putfield Type : I
    //   340: goto -> 275
    // Exception table:
    //   from	to	target	type
    //   67	74	312	java/lang/Exception
    //   86	93	312	java/lang/Exception
    //   115	126	312	java/lang/Exception
    //   137	143	312	java/lang/Exception
    //   179	193	312	java/lang/Exception
  }
  
  private void trimRows(Vector<TelnetRow> paramVector) {
    for (byte b = 0; b < paramVector.size(); b++) {
      TelnetRow telnetRow = paramVector.get(b);
      if (telnetRow.data[79] != 0 && b < paramVector.size() - 1) {
        TelnetRow telnetRow1 = paramVector.get(b + 1);
        if (telnetRow1.getQuoteSpace() == 0 && telnetRow1.getDataSpace() < telnetRow.getQuoteSpace()) {
          telnetRow.append(telnetRow1);
          paramVector.remove(b + 1);
        } 
      } 
    } 
  }
  
  public void build() {
    // Byte code:
    //   0: aload_0
    //   1: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   4: invokevirtual clear : ()V
    //   7: new java/util/Vector
    //   10: dup
    //   11: invokespecial <init> : ()V
    //   14: astore #9
    //   16: aload_0
    //   17: aload #9
    //   19: invokespecial buildRows : (Ljava/util/Vector;)V
    //   22: aload_0
    //   23: aload #9
    //   25: invokespecial trimRows : (Ljava/util/Vector;)V
    //   28: aload_0
    //   29: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   32: aload #9
    //   34: invokevirtual setFrameData : (Ljava/util/Vector;)V
    //   37: aload_0
    //   38: aload #9
    //   40: invokespecial loadHeader : (Ljava/util/Vector;)Z
    //   43: ifeq -> 66
    //   46: iconst_0
    //   47: istore_1
    //   48: iload_1
    //   49: iconst_5
    //   50: if_icmpge -> 66
    //   53: aload #9
    //   55: iconst_0
    //   56: invokevirtual remove : (I)Ljava/lang/Object;
    //   59: pop
    //   60: iinc #1, 1
    //   63: goto -> 48
    //   66: iconst_0
    //   67: istore_3
    //   68: iconst_0
    //   69: istore #4
    //   71: aconst_null
    //   72: astore #8
    //   74: aload #9
    //   76: invokevirtual iterator : ()Ljava/util/Iterator;
    //   79: astore #14
    //   81: aload #14
    //   83: invokeinterface hasNext : ()Z
    //   88: ifeq -> 904
    //   91: aload #14
    //   93: invokeinterface next : ()Ljava/lang/Object;
    //   98: checkcast com/kumi/Telnet/Model/TelnetRow
    //   101: astore #10
    //   103: aload #10
    //   105: invokevirtual toString : ()Ljava/lang/String;
    //   108: astore #15
    //   110: aload #10
    //   112: invokevirtual getQuoteLevel : ()I
    //   115: istore #7
    //   117: aload #15
    //   119: ldc '※( *)引述( *)《(.+)( *)(\((.+)\))?》之銘言：'
    //   121: invokevirtual matches : (Ljava/lang/String;)Z
    //   124: ifeq -> 387
    //   127: ldc ''
    //   129: astore #9
    //   131: ldc ''
    //   133: astore #10
    //   135: aload #15
    //   137: invokevirtual toCharArray : ()[C
    //   140: astore #11
    //   142: iconst_0
    //   143: istore #5
    //   145: iconst_0
    //   146: istore #6
    //   148: iconst_0
    //   149: istore_2
    //   150: iload #5
    //   152: istore_1
    //   153: iload_2
    //   154: aload #11
    //   156: arraylength
    //   157: if_icmpge -> 174
    //   160: aload #11
    //   162: iload_2
    //   163: caload
    //   164: sipush #12298
    //   167: if_icmpne -> 263
    //   170: iload_2
    //   171: iconst_1
    //   172: iadd
    //   173: istore_1
    //   174: iload_1
    //   175: istore_2
    //   176: iload #6
    //   178: istore #5
    //   180: iload_2
    //   181: aload #11
    //   183: arraylength
    //   184: if_icmpge -> 200
    //   187: aload #11
    //   189: iload_2
    //   190: caload
    //   191: sipush #12299
    //   194: if_icmpne -> 269
    //   197: iload_2
    //   198: istore #5
    //   200: iload #5
    //   202: iload_1
    //   203: if_icmple -> 219
    //   206: aload #15
    //   208: iload_1
    //   209: iload #5
    //   211: invokevirtual substring : (II)Ljava/lang/String;
    //   214: invokevirtual trim : ()Ljava/lang/String;
    //   217: astore #9
    //   219: aload #9
    //   221: invokevirtual toCharArray : ()[C
    //   224: astore #11
    //   226: iconst_0
    //   227: istore_1
    //   228: aload #11
    //   230: arraylength
    //   231: iconst_1
    //   232: isub
    //   233: istore #5
    //   235: iload #5
    //   237: istore_2
    //   238: iload_1
    //   239: aload #11
    //   241: arraylength
    //   242: if_icmpge -> 275
    //   245: iload #5
    //   247: istore_2
    //   248: aload #11
    //   250: iload_1
    //   251: caload
    //   252: bipush #40
    //   254: if_icmpeq -> 275
    //   257: iinc #1, 1
    //   260: goto -> 235
    //   263: iinc #2, 1
    //   266: goto -> 150
    //   269: iinc #2, 1
    //   272: goto -> 176
    //   275: iload_2
    //   276: iflt -> 294
    //   279: aload #11
    //   281: iload_2
    //   282: caload
    //   283: bipush #41
    //   285: if_icmpeq -> 294
    //   288: iinc #2, -1
    //   291: goto -> 275
    //   294: iload_2
    //   295: iload_1
    //   296: iconst_1
    //   297: iadd
    //   298: if_icmple -> 315
    //   301: aload #9
    //   303: iload_1
    //   304: iconst_1
    //   305: iadd
    //   306: iload_2
    //   307: invokevirtual substring : (II)Ljava/lang/String;
    //   310: invokevirtual trim : ()Ljava/lang/String;
    //   313: astore #10
    //   315: aload #9
    //   317: astore #11
    //   319: aload #10
    //   321: invokevirtual length : ()I
    //   324: ifle -> 336
    //   327: aload #9
    //   329: iconst_0
    //   330: iload_1
    //   331: invokevirtual substring : (II)Ljava/lang/String;
    //   334: astore #11
    //   336: aload #11
    //   338: invokevirtual trim : ()Ljava/lang/String;
    //   341: astore #9
    //   343: new com/kumi/Telnet/TelnetArticleItemInfo
    //   346: dup
    //   347: invokespecial <init> : ()V
    //   350: astore #11
    //   352: aload #11
    //   354: aload #9
    //   356: putfield author : Ljava/lang/String;
    //   359: aload #11
    //   361: aload #10
    //   363: putfield nickname : Ljava/lang/String;
    //   366: aload #11
    //   368: iload #7
    //   370: iconst_1
    //   371: iadd
    //   372: putfield quoteLevel : I
    //   375: aload_0
    //   376: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   379: aload #11
    //   381: invokevirtual addInfo : (Lcom/kumi/Telnet/TelnetArticleItemInfo;)V
    //   384: goto -> 81
    //   387: aload #15
    //   389: ldc '※ 修改:.*'
    //   391: invokevirtual matches : (Ljava/lang/String;)Z
    //   394: ifne -> 81
    //   397: aload #15
    //   399: ldc '--'
    //   401: invokevirtual equals : (Ljava/lang/Object;)Z
    //   404: ifeq -> 415
    //   407: iconst_1
    //   408: istore_3
    //   409: aconst_null
    //   410: astore #8
    //   412: goto -> 81
    //   415: aload #15
    //   417: ldc '※ Origin: 巴哈姆特<((gamer\.com\.tw)|(www\.gamer\.com\.tw)|(bbs\.gamer\.com\.tw)|(BAHAMUT\.ORG))> ◆ From: (.+)'
    //   419: invokevirtual matches : (Ljava/lang/String;)Z
    //   422: ifeq -> 434
    //   425: iconst_1
    //   426: istore #4
    //   428: aconst_null
    //   429: astore #8
    //   431: goto -> 81
    //   434: iload #4
    //   436: ifeq -> 717
    //   439: aload #15
    //   441: ldc '.+：.+\(.+\)'
    //   443: invokevirtual matches : (Ljava/lang/String;)Z
    //   446: ifeq -> 717
    //   449: ldc ''
    //   451: astore #9
    //   453: ldc ''
    //   455: astore #12
    //   457: ldc ''
    //   459: astore #10
    //   461: ldc ''
    //   463: astore #13
    //   465: ldc ''
    //   467: astore #11
    //   469: aload #15
    //   471: invokevirtual toCharArray : ()[C
    //   474: astore #16
    //   476: iconst_0
    //   477: istore #5
    //   479: iconst_0
    //   480: istore_1
    //   481: iload #5
    //   483: istore_2
    //   484: iload_1
    //   485: aload #16
    //   487: arraylength
    //   488: if_icmpge -> 502
    //   491: aload #16
    //   493: iload_1
    //   494: caload
    //   495: ldc 65306
    //   497: if_icmpne -> 699
    //   500: iload_1
    //   501: istore_2
    //   502: iload_2
    //   503: ifle -> 518
    //   506: aload #15
    //   508: iconst_0
    //   509: iload_2
    //   510: invokevirtual substring : (II)Ljava/lang/String;
    //   513: invokevirtual trim : ()Ljava/lang/String;
    //   516: astore #9
    //   518: iconst_0
    //   519: istore #7
    //   521: iconst_0
    //   522: istore #6
    //   524: aload #16
    //   526: arraylength
    //   527: iconst_1
    //   528: isub
    //   529: istore_1
    //   530: iload #6
    //   532: istore #5
    //   534: iload_1
    //   535: iflt -> 550
    //   538: aload #16
    //   540: iload_1
    //   541: caload
    //   542: bipush #41
    //   544: if_icmpne -> 705
    //   547: iload_1
    //   548: istore #5
    //   550: iload #5
    //   552: iconst_1
    //   553: isub
    //   554: istore #6
    //   556: iload #7
    //   558: istore_1
    //   559: iload #6
    //   561: iflt -> 579
    //   564: aload #16
    //   566: iload #6
    //   568: caload
    //   569: bipush #40
    //   571: if_icmpne -> 711
    //   574: iload #6
    //   576: iconst_1
    //   577: iadd
    //   578: istore_1
    //   579: iload #5
    //   581: iload_1
    //   582: if_icmple -> 595
    //   585: aload #15
    //   587: iload_1
    //   588: iload #5
    //   590: invokevirtual substring : (II)Ljava/lang/String;
    //   593: astore #10
    //   595: aload #10
    //   597: ldc ' +'
    //   599: invokevirtual split : (Ljava/lang/String;)[Ljava/lang/String;
    //   602: astore #16
    //   604: aload #13
    //   606: astore #10
    //   608: aload #16
    //   610: arraylength
    //   611: iconst_2
    //   612: if_icmpne -> 627
    //   615: aload #16
    //   617: iconst_0
    //   618: aaload
    //   619: astore #10
    //   621: aload #16
    //   623: iconst_1
    //   624: aaload
    //   625: astore #11
    //   627: iinc #2, 1
    //   630: iinc #1, -1
    //   633: iload_1
    //   634: iload_2
    //   635: if_icmple -> 650
    //   638: aload #15
    //   640: iload_2
    //   641: iload_1
    //   642: invokevirtual substring : (II)Ljava/lang/String;
    //   645: invokevirtual trim : ()Ljava/lang/String;
    //   648: astore #12
    //   650: new com/kumi/Telnet/TelnetArticlePush
    //   653: dup
    //   654: invokespecial <init> : ()V
    //   657: astore #13
    //   659: aload #13
    //   661: aload #9
    //   663: putfield author : Ljava/lang/String;
    //   666: aload #13
    //   668: aload #12
    //   670: putfield content : Ljava/lang/String;
    //   673: aload #13
    //   675: aload #10
    //   677: putfield date : Ljava/lang/String;
    //   680: aload #13
    //   682: aload #11
    //   684: putfield time : Ljava/lang/String;
    //   687: aload_0
    //   688: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   691: aload #13
    //   693: invokevirtual addPush : (Lcom/kumi/Telnet/TelnetArticlePush;)V
    //   696: goto -> 81
    //   699: iinc #1, 1
    //   702: goto -> 481
    //   705: iinc #1, -1
    //   708: goto -> 530
    //   711: iinc #6, -1
    //   714: goto -> 556
    //   717: aload #8
    //   719: ifnull -> 736
    //   722: aload #8
    //   724: astore #9
    //   726: aload #8
    //   728: invokevirtual getQuoteLevel : ()I
    //   731: iload #7
    //   733: if_icmpeq -> 798
    //   736: new com/kumi/Telnet/TelnetArticleItem
    //   739: dup
    //   740: invokespecial <init> : ()V
    //   743: astore #8
    //   745: iload_3
    //   746: ifeq -> 821
    //   749: aload_0
    //   750: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   753: aload #8
    //   755: invokevirtual addExtendItem : (Lcom/kumi/Telnet/TelnetArticleItem;)V
    //   758: aload #8
    //   760: iload #7
    //   762: invokevirtual setQuoteLevel : (I)V
    //   765: iload #7
    //   767: ifne -> 833
    //   770: aload #8
    //   772: aload_0
    //   773: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   776: getfield Author : Ljava/lang/String;
    //   779: invokevirtual setAuthor : (Ljava/lang/String;)V
    //   782: aload #8
    //   784: aload_0
    //   785: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   788: getfield Nickname : Ljava/lang/String;
    //   791: invokevirtual setNickname : (Ljava/lang/String;)V
    //   794: aload #8
    //   796: astore #9
    //   798: aload #9
    //   800: astore #8
    //   802: aload #9
    //   804: ifnull -> 81
    //   807: aload #9
    //   809: aload #10
    //   811: invokevirtual addRow : (Lcom/kumi/Telnet/Model/TelnetRow;)V
    //   814: aload #9
    //   816: astore #8
    //   818: goto -> 81
    //   821: aload_0
    //   822: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   825: aload #8
    //   827: invokevirtual addMainItem : (Lcom/kumi/Telnet/TelnetArticleItem;)V
    //   830: goto -> 758
    //   833: aload_0
    //   834: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   837: invokevirtual getInfoSize : ()I
    //   840: iconst_1
    //   841: isub
    //   842: istore_1
    //   843: aload #8
    //   845: astore #9
    //   847: iload_1
    //   848: iflt -> 798
    //   851: aload_0
    //   852: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   855: iload_1
    //   856: invokevirtual getInfo : (I)Lcom/kumi/Telnet/TelnetArticleItemInfo;
    //   859: astore #9
    //   861: aload #9
    //   863: getfield quoteLevel : I
    //   866: iload #7
    //   868: if_icmpne -> 898
    //   871: aload #8
    //   873: aload #9
    //   875: getfield author : Ljava/lang/String;
    //   878: invokevirtual setAuthor : (Ljava/lang/String;)V
    //   881: aload #8
    //   883: aload #9
    //   885: getfield nickname : Ljava/lang/String;
    //   888: invokevirtual setNickname : (Ljava/lang/String;)V
    //   891: aload #8
    //   893: astore #9
    //   895: goto -> 798
    //   898: iinc #1, -1
    //   901: goto -> 843
    //   904: aload_0
    //   905: getfield _article : Lcom/kumi/Telnet/TelnetArticle;
    //   908: invokevirtual build : ()V
    //   911: return
  }
  
  public void clear() {
    this._article.clear();
    this._pages.clear();
    this._last_page = null;
  }
  
  public TelnetArticle getArticle() {
    return this._article;
  }
  
  public void loadLastPage(TelnetModel paramTelnetModel) {
    byte b1;
    byte b2;
    if (this._pages.size() > 0) {
      b1 = 1;
    } else {
      b1 = 0;
    } 
    byte b3 = 23;
    while (true) {
      b2 = b3;
      if (b1 < 23) {
        b2 = b3;
        if (paramTelnetModel.getRow(b1).isEmpty()) {
          b1++;
          continue;
        } 
      } 
      break;
    } 
    while (b2 > b1 && paramTelnetModel.getRow(b2).isEmpty())
      b2--; 
    TelnetArticlePage telnetArticlePage2 = this._last_page;
    TelnetArticlePage telnetArticlePage1 = telnetArticlePage2;
    if (telnetArticlePage2 == null)
      telnetArticlePage1 = new TelnetArticlePage(); 
    telnetArticlePage1.clear();
    while (b1 < b2) {
      telnetArticlePage1.addRow(paramTelnetModel.getRow(b1));
      b1++;
    } 
    this._last_page = telnetArticlePage1;
  }
  
  public void loadPage(TelnetModel paramTelnetModel) {
    byte b = 1;
    if (paramTelnetModel != null) {
      int i = parsePageIndex(paramTelnetModel.getLastRow());
      if (i > 0) {
        TelnetArticlePage telnetArticlePage = new TelnetArticlePage();
        if (i == 1)
          b = 0; 
        while (b < 23) {
          telnetArticlePage.addRow(paramTelnetModel.getRow(b));
          b++;
        } 
        this._pages.add(telnetArticlePage);
      } 
    } 
  }
  
  public void newArticle() {
    this._article = new TelnetArticle();
  }
  
  public int parsePageIndex(TelnetRow paramTelnetRow) {
    int i = 0;
    byte b = 8;
    while (b < 14) {
      byte b1 = paramTelnetRow.data[b];
      if (b1 >= 48 && b1 <= 57) {
        i = i * 10 + (b1 - 48 & 0xFF);
        b++;
      } 
    } 
    return i;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Logic\Article_Handler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */