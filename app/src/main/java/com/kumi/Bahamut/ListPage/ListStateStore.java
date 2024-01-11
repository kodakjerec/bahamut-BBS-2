package com.kumi.Bahamut.ListPage;

import java.util.HashMap;
import java.util.Map;

public class ListStateStore {
  private static ListStateStore _instance = null;
  
  private Map<String, ListState> _states = new HashMap<String, ListState>();
  
  public static ListStateStore getInstance() {
    if (_instance == null)
      _instance = new ListStateStore(); 
    return _instance;
  }
  
  public ListState getState(String paramString) {
    ListState listState2 = this._states.get(paramString);
    ListState listState1 = listState2;
    if (listState2 == null) {
      listState1 = new ListState();
      this._states.put(paramString, listState1);
    } 
    return listState1;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\ListPage\ListStateStore.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */