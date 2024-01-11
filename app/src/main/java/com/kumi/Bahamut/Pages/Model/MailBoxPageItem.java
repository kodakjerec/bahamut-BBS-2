package com.kumi.Bahamut.Pages.Model;

import com.kumi.Bahamut.ListPage.TelnetListPageItem;
import java.util.Stack;

public class MailBoxPageItem extends TelnetListPageItem {
  private static int _count = 0;
  
  private static Stack<MailBoxPageItem> _pool = new Stack<MailBoxPageItem>();
  
  public String Author = null;
  
  public String Date = null;
  
  public int Size = 0;
  
  public String Title = null;
  
  public boolean isMarked = false;
  
  public boolean isOrigin = false;
  
  public boolean isRead = false;
  
  public boolean isReply = false;
  
  public static MailBoxPageItem create() {
    Stack<MailBoxPageItem> stack;
    MailBoxPageItem mailBoxPageItem;
    null = null;
    synchronized (_pool) {
      if (_pool.size() > 0)
        null = _pool.pop(); 
      mailBoxPageItem = null;
      if (null == null)
        mailBoxPageItem = new MailBoxPageItem(); 
      return mailBoxPageItem;
    } 
  }
  
  public static void recycle(MailBoxPageItem paramMailBoxPageItem) {
    synchronized (_pool) {
      _pool.push(paramMailBoxPageItem);
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
    this.isReply = false;
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
      this.Size = ((MailBoxPageItem)paramTelnetListPageItem).Size;
      this.Date = ((MailBoxPageItem)paramTelnetListPageItem).Date;
      this.Author = ((MailBoxPageItem)paramTelnetListPageItem).Author;
      this.isRead = ((MailBoxPageItem)paramTelnetListPageItem).isRead;
      this.isReply = ((MailBoxPageItem)paramTelnetListPageItem).isReply;
      this.isMarked = ((MailBoxPageItem)paramTelnetListPageItem).isMarked;
      this.Title = ((MailBoxPageItem)paramTelnetListPageItem).Title;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\MailBoxPageItem.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */