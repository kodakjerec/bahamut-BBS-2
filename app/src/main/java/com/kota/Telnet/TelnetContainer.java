package com.kota.Telnet;

import java.util.Map;
import java.util.TreeMap;

public class TelnetContainer<ObjectType> {
    Map<Integer, ObjectType> _container = new TreeMap<>();
    int _max_index = 0;

    public void add(Integer index, ObjectType object) {
        if (this._container != null) {
            this._container.put(index, object);
            if (index > this._max_index) {
                this._max_index = index;
            }
        }
    }

    public ObjectType get(Integer index) {
        if (index < 0 || index > this._max_index || this._container == null || !this._container.containsKey(index)) {
            return null;
        }
        return this._container.get(index);
    }

    public int size() {
        if (this._container != null) {
            return this._max_index + 1;
        }
        return 0;
    }

    public void clear() {
        this._max_index = 0;
        this._container.clear();
    }
}
