package com.kumi.Bahamut.Pages.Model;

import com.kumi.Bahamut.ListPage.TelnetListPageItem;
import java.util.Stack;

public class BoardPageItem extends TelnetListPageItem {
  private static int _count = 0;
  
  private static Stack<BoardPageItem> _pool = new Stack<BoardPageItem>();
  
  public String Author = null;
  
  public String Date = null;
  
  public int GY = 0;
  
  public int Size = 0;
  
  public String Title = null;
  
  public boolean isMarked = false;
  
  public boolean isRead = false;
  
  public boolean isReply = false;
  
  public static BoardPageItem create() {
    Stack<BoardPageItem> stack;
    BoardPageItem boardPageItem;
    null = null;
    synchronized (_pool) {
      if (_pool.size() > 0)
        null = _pool.pop(); 
      boardPageItem = null;
      if (null == null)
        boardPageItem = new BoardPageItem(); 
      return boardPageItem;
    } 
  }
  
  public static void recycle(BoardPageItem paramBoardPageItem) {
    synchronized (_pool) {
      _pool.push(paramBoardPageItem);
      return;
    } 
  }
  
  public static void release() {
    synchronized (_pool) {
      _pool.clear();
      return;
    } 
  }
  
  public void clear() {
    super.clear();
    this.Size = 0;
    this.isRead = false;
    this.isMarked = false;
    this.isReply = false;
    this.GY = 0;
    this.Date = null;
    this.Author = null;
    this.Title = null;
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public void set(TelnetListPageItem paramTelnetListPageItem) {
    super.set(paramTelnetListPageItem);
    if (paramTelnetListPageItem != null) {
      paramTelnetListPageItem = paramTelnetListPageItem;
      this.Size = ((BoardPageItem)paramTelnetListPageItem).Size;
      this.Date = ((BoardPageItem)paramTelnetListPageItem).Date;
      this.Author = ((BoardPageItem)paramTelnetListPageItem).Author;
      this.isRead = ((BoardPageItem)paramTelnetListPageItem).isRead;
      this.isMarked = ((BoardPageItem)paramTelnetListPageItem).isMarked;
      this.isReply = ((BoardPageItem)paramTelnetListPageItem).isReply;
      this.GY = ((BoardPageItem)paramTelnetListPageItem).GY;
      this.Title = ((BoardPageItem)paramTelnetListPageItem).Title;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\BoardPageItem.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */