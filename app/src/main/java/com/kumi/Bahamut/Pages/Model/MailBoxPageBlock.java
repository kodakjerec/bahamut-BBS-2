package com.kumi.Bahamut.Pages.Model;

import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import java.util.Stack;

public class MailBoxPageBlock extends TelnetListPageBlock {
  private static Stack<MailBoxPageBlock> _pool = new Stack<MailBoxPageBlock>();
  
  public static MailBoxPageBlock create() {
    Stack<MailBoxPageBlock> stack;
    MailBoxPageBlock mailBoxPageBlock;
    null = null;
    synchronized (_pool) {
      if (_pool.size() > 0)
        null = _pool.pop(); 
      mailBoxPageBlock = null;
      if (null == null)
        mailBoxPageBlock = new MailBoxPageBlock(); 
      return mailBoxPageBlock;
    } 
  }
  
  public static void recycle(MailBoxPageBlock paramMailBoxPageBlock) {
    synchronized (_pool) {
      _pool.push(paramMailBoxPageBlock);
      return;
    } 
  }
  
  public static void release() {
    synchronized (_pool) {
      _pool.clear();
      return;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\MailBoxPageBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */