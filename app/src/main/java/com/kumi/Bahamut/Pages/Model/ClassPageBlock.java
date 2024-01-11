package com.kumi.Bahamut.Pages.Model;

import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import java.util.Stack;

public class ClassPageBlock extends TelnetListPageBlock {
  private static Stack<ClassPageBlock> _pool = new Stack<ClassPageBlock>();
  
  public int mode = 0;
  
  public static ClassPageBlock create() {
    Stack<ClassPageBlock> stack;
    ClassPageBlock classPageBlock;
    null = null;
    synchronized (_pool) {
      if (_pool.size() > 0)
        null = _pool.pop(); 
      classPageBlock = null;
      if (null == null)
        classPageBlock = new ClassPageBlock(); 
      return classPageBlock;
    } 
  }
  
  public static void recycle(ClassPageBlock paramClassPageBlock) {
    synchronized (_pool) {
      _pool.push(paramClassPageBlock);
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


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\ClassPageBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */