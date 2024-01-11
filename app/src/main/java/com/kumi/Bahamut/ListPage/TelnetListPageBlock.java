package com.kumi.Bahamut.ListPage;

public abstract class TelnetListPageBlock {
  public static final int BLOCK_SIZE = 20;
  
  private TelnetListPageItem[] _items = new TelnetListPageItem[20];
  
  public int maximumItemNumber = 0;
  
  public int minimumItemNumber = 0;
  
  public TelnetListPageItem selectedItem = null;
  
  public int selectedItemNumber = 0;
  
  public void clear() {
    for (byte b = 0;; b++) {
      if (b >= this._items.length || this._items[b] == null)
        return; 
      this._items[b] = null;
    } 
  }
  
  public TelnetListPageItem getItem(int paramInt) {
    return this._items[paramInt];
  }
  
  public void setItem(int paramInt, TelnetListPageItem paramTelnetListPageItem) {
    this._items[paramInt] = paramTelnetListPageItem;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\ListPage\TelnetListPageBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */