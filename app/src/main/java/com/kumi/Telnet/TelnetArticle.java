package com.kumi.Telnet;

import android.annotation.SuppressLint;
import com.kumi.Telnet.Model.TelnetFrame;
import com.kumi.Telnet.Model.TelnetRow;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelnetArticle {
  public static final int MINIMUM_REMOVE_QUOTE = 1;
  
  public static final int NORMAL = 0;
  
  public static final int REPLY = 0;
  
  public String Author = "";
  
  public String BoardName = "";
  
  public String DateTime = "";
  
  public String Nickname = "";
  
  public int Number = 0;
  
  public String Title = "";
  
  public int Type = 0;
  
  private String _block_list = null;
  
  private Vector<TelnetArticleItem> _extend_items = new Vector<TelnetArticleItem>();
  
  private TelnetFrame _frame = null;
  
  private Vector<TelnetArticleItemInfo> _infos = new Vector<TelnetArticleItemInfo>();
  
  private Vector<TelnetArticleItem> _items = new Vector<TelnetArticleItem>();
  
  private Vector<TelnetArticleItem> _main_items = new Vector<TelnetArticleItem>();
  
  private Vector<TelnetArticlePush> _pushs = new Vector<TelnetArticlePush>();
  
  public void addExtendItem(TelnetArticleItem paramTelnetArticleItem) {
    this._extend_items.add(paramTelnetArticleItem);
  }
  
  public void addInfo(TelnetArticleItemInfo paramTelnetArticleItemInfo) {
    this._infos.add(paramTelnetArticleItemInfo);
  }
  
  public void addMainItem(TelnetArticleItem paramTelnetArticleItem) {
    this._main_items.add(paramTelnetArticleItem);
  }
  
  public void addPush(TelnetArticlePush paramTelnetArticlePush) {
    this._pushs.add(paramTelnetArticlePush);
  }
  
  public void build() {
    Iterator<TelnetArticleItem> iterator = this._main_items.iterator();
    while (iterator.hasNext())
      ((TelnetArticleItem)iterator.next()).build(); 
    iterator = this._extend_items.iterator();
    while (iterator.hasNext())
      ((TelnetArticleItem)iterator.next()).build(); 
    Vector<TelnetArticleItem> vector = new Vector();
    for (TelnetArticleItem telnetArticleItem : this._main_items) {
      if (telnetArticleItem.isEmpty())
        vector.add(telnetArticleItem); 
    } 
    for (TelnetArticleItem telnetArticleItem : vector)
      this._main_items.remove(telnetArticleItem); 
    vector.clear();
    for (TelnetArticleItem telnetArticleItem : this._extend_items) {
      if (telnetArticleItem.isEmpty())
        vector.add(telnetArticleItem); 
    } 
    for (TelnetArticleItem telnetArticleItem : vector)
      this._extend_items.remove(telnetArticleItem); 
    vector.clear();
    if (this._extend_items.size() > 0)
      ((TelnetArticleItem)this._extend_items.lastElement()).setType(1); 
    this._items.clear();
    this._items.addAll(this._main_items);
    this._items.addAll(this._extend_items);
  }
  
  public void clear() {
    this.Title = "";
    this.Author = "";
    this.BoardName = "";
    this.DateTime = "";
    this._main_items.clear();
    this._items.clear();
    this._pushs.clear();
    this._infos.clear();
    this._frame = null;
  }
  
  public String generateEditContent() {
    StringBuffer stringBuffer = new StringBuffer();
    for (byte b = 5; b < this._frame.getRowSize(); b++)
      stringBuffer.append(this._frame.getRow(b).getRawString() + "\n"); 
    return stringBuffer.toString();
  }
  
  public String generateEditTitle() {
    return this._frame.getRow(1).toString().substring(4);
  }
  
  public String generateReplyContent() {
    int i;
    StringBuilder stringBuilder = new StringBuilder();
    HashSet<Integer> hashSet = new HashSet();
    hashSet.add(Integer.valueOf(0));
    null = this._infos.iterator();
    while (null.hasNext())
      hashSet.add(Integer.valueOf(((TelnetArticleItemInfo)null.next()).quoteLevel)); 
    Integer[] arrayOfInteger = hashSet.<Integer>toArray(new Integer[hashSet.size()]);
    Arrays.sort((Object[])arrayOfInteger);
    if (arrayOfInteger.length < 2) {
      i = arrayOfInteger[arrayOfInteger.length - 1].intValue();
    } else {
      i = arrayOfInteger[1].intValue();
    } 
    stringBuilder.append(String.format("※ 引述《%s (%s)》之銘言：", new Object[] { this.Author, this.Nickname }));
    stringBuilder.append("\n");
    for (TelnetArticleItemInfo telnetArticleItemInfo : this._infos) {
      if (!isBlocked(telnetArticleItemInfo.author) && telnetArticleItemInfo.quoteLevel <= i) {
        for (byte b = 0; b < telnetArticleItemInfo.quoteLevel; b++)
          stringBuilder.append("> "); 
        stringBuilder.append(String.format("※ 引述《%s (%s)》之銘言：\n", new Object[] { telnetArticleItemInfo.author, telnetArticleItemInfo.nickname }));
      } 
    } 
    for (TelnetArticleItem telnetArticleItem : this._main_items) {
      if (!isBlocked(telnetArticleItem.getAuthor()) && telnetArticleItem.getQuoteLevel() <= i) {
        String[] arrayOfString = telnetArticleItem.getContent().split("\n");
        for (byte b = 0; b < arrayOfString.length; b++) {
          for (byte b1 = 0; b1 <= telnetArticleItem.getQuoteLevel(); b1++)
            stringBuilder.append("> "); 
          stringBuilder.append(arrayOfString[b]);
          stringBuilder.append("\n");
        } 
      } 
    } 
    return stringBuilder.toString();
  }
  
  public String generateReplyTitle() {
    return "Re: " + this.Title;
  }
  
  public String generatrEditFormat() {
    StringBuffer stringBuffer = new StringBuffer();
    String str = this._frame.getRow(2).toString().substring(4);
    stringBuffer.append("作者: " + this.Author);
    if (this.Nickname != null && this.Nickname.length() > 0)
      stringBuffer.append("(" + this.Nickname + ")"); 
    stringBuffer.append(" 看板: " + this.BoardName + "\n");
    stringBuffer.append("標題: %s\n");
    stringBuffer.append("時間: " + str + "\n");
    stringBuffer.append("\n%s");
    return stringBuffer.toString();
  }
  
  public TelnetFrame getFrame() {
    return this._frame;
  }
  
  public String getFullText() {
    if (this._frame == null)
      return ""; 
    StringBuilder stringBuilder = new StringBuilder();
    byte b = 0;
    int i = this._frame.getRowSize();
    while (b < i) {
      TelnetRow telnetRow = this._frame.getRow(b);
      if (b > 0)
        stringBuilder.append("\n"); 
      stringBuilder.append(telnetRow.getRawString());
      b++;
    } 
    return stringBuilder.toString();
  }
  
  public TelnetArticleItemInfo getInfo(int paramInt) {
    return this._infos.get(paramInt);
  }
  
  public int getInfoSize() {
    return this._infos.size();
  }
  
  public TelnetArticleItem getItem(int paramInt) {
    TelnetArticleItem telnetArticleItem2 = null;
    TelnetArticleItem telnetArticleItem1 = telnetArticleItem2;
    if (paramInt >= 0) {
      telnetArticleItem1 = telnetArticleItem2;
      if (paramInt < this._items.size())
        telnetArticleItem1 = this._items.get(paramInt); 
    } 
    return telnetArticleItem1;
  }
  
  public int getItemSize() {
    return this._items.size();
  }
  
  public String[] getUrls() {
    String str = getFullText();
    System.out.println("article_text:" + str);
    Matcher matcher = Pattern.compile("(ftp://|http://|https://)?([a-zA-Z0-9_-~]+(:[a-zA-Z0-9_-~]+)?@)?([a-zA-Z0-9_-~]+(\\.[a-zA-Z0-9_-~]+){1,})((((/[a-zA-Z0-9_-~]+){0,})?((/[a-zA-Z0-9_-~*]+(\\.[a-zA-Z0-9_-~]+)?(\\?([a-zA-Z0-9_-~]+=([a-zA-Z0-9_-~%#*]+)?)(&[a-zA-Z0-9_-~]+=([a-zA-Z0-9_-~%#*]+)?){0,})?)|/))|(((/[a-zA-Z0-9_-~]+){0,})?((/[a-zA-Z0-9_-~*]+(\\.[a-zA-Z0-9_-~]+)?))))?").matcher(str);
    Vector<String> vector = new Vector();
    while (matcher.find()) {
      str = matcher.group();
      System.out.println("url_string:" + str);
      vector.add(str);
    } 
    return vector.<String>toArray(new String[vector.size()]);
  }
  
  @SuppressLint({"DefaultLocale"})
  public boolean isBlocked(String paramString) {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (this._block_list != null) {
      bool1 = bool2;
      bool1 = bool2;
      if (paramString != null && this._block_list.contains("," + paramString.trim().toLowerCase() + ","))
        bool1 = true; 
    } 
    return bool1;
  }
  
  public void setBlockList(String paramString) {
    this._block_list = paramString;
  }
  
  public void setFrameData(Vector<TelnetRow> paramVector) {
    this._frame = new TelnetFrame(paramVector.size());
    for (byte b = 0; b < paramVector.size(); b++)
      this._frame.setRow(b, ((TelnetRow)paramVector.get(b)).clone()); 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */