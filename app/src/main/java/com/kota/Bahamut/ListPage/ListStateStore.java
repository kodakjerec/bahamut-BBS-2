package com.kota.Bahamut.ListPage;

import java.util.HashMap;
import java.util.Map;

public class ListStateStore {
    private static ListStateStore _instance = null;
    private final Map<String, ListState> _states = new HashMap();

    private ListStateStore() {
    }

    public static ListStateStore getInstance() {
        if (_instance == null) {
            _instance = new ListStateStore();
        }
        return _instance;
    }

    public ListState getState(String aBoardName) {
        ListState state = this._states.get(aBoardName);
        if (state != null) {
            return state;
        }
        ListState state2 = new ListState();
        this._states.put(aBoardName, state2);
        return state2;
    }
}
