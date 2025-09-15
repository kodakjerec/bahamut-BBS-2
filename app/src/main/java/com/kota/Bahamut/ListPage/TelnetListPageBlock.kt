package com.kota.Bahamut.ListPage

abstract class TelnetListPageBlock {
    private val _items = arrayOfNulls<TelnetListPageItem>(BLOCK_SIZE)
    var maximumItemNumber = 0
    var minimumItemNumber = 0
    var selectedItem: TelnetListPageItem? = null
    var selectedItemNumber = 0

    fun setItem(index: Int, aItem: TelnetListPageItem?) {
        _items[index] = aItem
    }

    fun getItem(index: Int): TelnetListPageItem? {
        return _items[index]
    }

    fun clear() {
        var i = 0
        while (i < _items.size && _items[i] != null) {
            _items[i] = null
            i++
        }
    }

    companion object {
        const val BLOCK_SIZE = 20
    }
}
