package com.kumi.Bahamut.Pages.Model;

import com.kumi.Bahamut.ListPage.TelnetListPageItem;
import java.util.Stack;

public class ClassPageItem extends TelnetListPageItem {
  private static int _count = 0;
  
  private static Stack<ClassPageItem> _pool = new Stack<ClassPageItem>();
  
  public String Manager = null;
  
  public int Mode = 0;
  
  public String Name = null;
  
  public String Title = null;
  
  public boolean isDirectory = false;
  
  public static ClassPageItem create() {
    Stack<ClassPageItem> stack;
    ClassPageItem classPageItem;
    null = null;
    synchronized (_pool) {
      if (_pool.size() > 0)
        null = _pool.pop(); 
      classPageItem = null;
      if (null == null)
        classPageItem = new ClassPageItem(); 
      return classPageItem;
    } 
  }
  
  public static void recycle(ClassPageItem paramClassPageItem) {
    synchronized (_pool) {
      _pool.push(paramClassPageItem);
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
    this.Manager = null;
    this.Name = null;
    this.Title = null;
    this.isDirectory = false;
    this.Mode = 0;
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\ClassPageItem.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */