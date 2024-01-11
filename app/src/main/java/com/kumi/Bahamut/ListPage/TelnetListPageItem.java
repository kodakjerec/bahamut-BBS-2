package com.kumi.Bahamut.ListPage;

public class TelnetListPageItem {
  public int Number = 0;
  
  public boolean isBlocked = false;
  
  public boolean isDeleted = false;
  
  public boolean isLoading = false;
  
  public void clear() {
    this.Number = 0;
    this.isDeleted = false;
    this.isLoading = false;
    this.isBlocked = false;
  }
  
  public void set(TelnetListPageItem paramTelnetListPageItem) {
    if (paramTelnetListPageItem != null) {
      this.isDeleted = paramTelnetListPageItem.isDeleted;
      this.isLoading = paramTelnetListPageItem.isLoading;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\ListPage\TelnetListPageItem.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */