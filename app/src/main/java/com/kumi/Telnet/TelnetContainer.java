package com.kumi.Telnet;

import java.util.Map;
import java.util.TreeMap;

public class TelnetContainer<ObjectType> {
  Map<Integer, ObjectType> _container = new TreeMap<Integer, ObjectType>();
  
  int _max_index = 0;
  
  public void add(Integer paramInteger, ObjectType paramObjectType) {
    if (this._container != null) {
      this._container.put(paramInteger, paramObjectType);
      if (paramInteger.intValue() > this._max_index)
        this._max_index = paramInteger.intValue(); 
    } 
  }
  
  public void clear() {
    this._max_index = 0;
    this._container.clear();
  }
  
  public ObjectType get(Integer paramInteger) {
    return (paramInteger.intValue() >= 0 && paramInteger.intValue() <= this._max_index && this._container != null && this._container.containsKey(paramInteger)) ? this._container.get(paramInteger) : null;
  }
  
  public int size() {
    return (this._container != null) ? (this._max_index + 1) : 0;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetContainer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */