package com.kumi.Bahamut.Pages.Model;

import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import java.util.Stack;

public class BoardPageBlock extends TelnetListPageBlock {
  private static Stack<BoardPageBlock> _pool = new Stack<BoardPageBlock>();
  
  public String BoardManager = null;
  
  public String BoardName = null;
  
  public String BoardTitle = null;
  
  public int Type = 0;
  
  public int mode = 0;
  
  public static BoardPageBlock create() {
    Stack<BoardPageBlock> stack;
    BoardPageBlock boardPageBlock;
    null = null;
    synchronized (_pool) {
      if (_pool.size() > 0)
        null = _pool.pop(); 
      boardPageBlock = null;
      if (null == null)
        boardPageBlock = new BoardPageBlock(); 
      return boardPageBlock;
    } 
  }
  
  public static void recycle(BoardPageBlock paramBoardPageBlock) {
    synchronized (_pool) {
      _pool.push(paramBoardPageBlock);
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


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\BoardPageBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */