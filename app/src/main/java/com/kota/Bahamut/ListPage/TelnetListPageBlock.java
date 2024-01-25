package com.kota.Bahamut.ListPage;

public abstract class TelnetListPageBlock {
    public static final int BLOCK_SIZE = 20;
    private final TelnetListPageItem[] _items = new TelnetListPageItem[20];
    public int maximumItemNumber = 0;
    public int minimumItemNumber = 0;
    public TelnetListPageItem selectedItem = null;
    public int selectedItemNumber = 0;

    public void setItem(int index, TelnetListPageItem aItem) {
        this._items[index] = aItem;
    }

    public TelnetListPageItem getItem(int index) {
        return this._items[index];
    }

    public void clear() {
        int i = 0;
        while (i < this._items.length && this._items[i] != null) {
            this._items[i] = null;
            i++;
        }
    }
}
