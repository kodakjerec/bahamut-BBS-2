package com.kumi.Bahamut.DataModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class BookmarkList {
  private String _board = "";
  
  private List<Bookmark> _bookmarks = new ArrayList<Bookmark>();
  
  private List<Bookmark> _history_bookmarks = new ArrayList<Bookmark>();
  
  public int limit = 20;
  
  public BookmarkList(String paramString) {
    this._board = paramString;
  }
  
  public void addBookmark(Bookmark paramBookmark) {
    this._bookmarks.add(paramBookmark);
  }
  
  public void addHistoryBookmark(Bookmark paramBookmark) {
    for (Bookmark bookmark : this._history_bookmarks) {
      if (bookmark.getKeyword().equals(paramBookmark.getKeyword())) {
        this._history_bookmarks.remove(bookmark);
        break;
      } 
    } 
    this._history_bookmarks.add(0, paramBookmark);
    while (this._history_bookmarks.size() > this.limit)
      this._history_bookmarks.remove(this._history_bookmarks.get(this._history_bookmarks.size() - 1)); 
  }
  
  public void addHistoryBookmark(String paramString) {
    Bookmark bookmark1;
    Bookmark bookmark2 = null;
    Iterator<Bookmark> iterator = this._history_bookmarks.iterator();
    while (true) {
      bookmark1 = bookmark2;
      if (iterator.hasNext()) {
        Bookmark bookmark = iterator.next();
        if (bookmark.getKeyword().equals(paramString)) {
          bookmark1 = bookmark;
          this._history_bookmarks.remove(bookmark);
          break;
        } 
        continue;
      } 
      break;
    } 
    bookmark2 = bookmark1;
    if (bookmark1 == null) {
      bookmark2 = new Bookmark();
      bookmark2.setBoard(this._board);
      bookmark2.setKeyword(paramString);
    } 
    this._history_bookmarks.add(0, bookmark2);
    while (this._history_bookmarks.size() > this.limit)
      this._history_bookmarks.remove(this._history_bookmarks.get(this._history_bookmarks.size() - 1)); 
  }
  
  public void clear() {
    this._bookmarks.clear();
  }
  
  public void clearHistoryBookmark() {
    this._history_bookmarks.clear();
  }
  
  public Bookmark getBookmark(int paramInt) {
    return this._bookmarks.get(paramInt);
  }
  
  public int getBookmarkSize() {
    return this._bookmarks.size();
  }
  
  public Bookmark getHistoryBookmark(int paramInt) {
    return this._history_bookmarks.get(paramInt);
  }
  
  public int getHistoryBookmarkSize() {
    return this._history_bookmarks.size();
  }
  
  public void loadHistoryList(List<Bookmark> paramList) {
    paramList.clear();
    paramList.addAll(this._history_bookmarks);
  }
  
  public void loadTitleList(List<Bookmark> paramList) {
    paramList.clear();
    paramList.addAll(this._bookmarks);
  }
  
  public void printHistoryBookmark() {
    for (byte b = 0; b < this._history_bookmarks.size(); b++) {
      Bookmark bookmark = this._history_bookmarks.get(b);
      System.out.println((b + 1) + ". " + bookmark.getTitle());
    } 
  }
  
  public Bookmark removeBookmark(int paramInt) {
    return this._bookmarks.remove(paramInt);
  }
  
  public Bookmark removeHistoryBookmark(int paramInt) {
    return this._history_bookmarks.remove(paramInt);
  }
  
  void sort() {
    Collections.sort(this._bookmarks, new Comparator<Bookmark>() {
          final BookmarkList this$0;
          
          public int compare(Bookmark param1Bookmark1, Bookmark param1Bookmark2) {
            if (param1Bookmark1.index == 0 && param1Bookmark2.index == 0)
              System.out.println("error"); 
            return (param1Bookmark1.index < param1Bookmark2.index) ? -1 : ((param1Bookmark1.index > param1Bookmark2.index) ? 1 : 0);
          }
        });
    Collections.sort(this._history_bookmarks, new Comparator<Bookmark>() {
          final BookmarkList this$0;
          
          public int compare(Bookmark param1Bookmark1, Bookmark param1Bookmark2) {
            if (param1Bookmark1.index == 0 && param1Bookmark2.index == 0)
              System.out.println("error"); 
            return (param1Bookmark1.index < param1Bookmark2.index) ? -1 : ((param1Bookmark1.index > param1Bookmark2.index) ? 1 : 0);
          }
        });
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\DataModels\BookmarkList.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */