package com.kota.Bahamut.listPage

abstract class TelnetListPageBlock {
    private val _items = arrayOfNulls<TelnetListPageItem>(BLOCK_SIZE)
    @JvmField
    var maximumItemNumber: Int = 0
    @JvmField
    var minimumItemNumber: Int = 0
    @JvmField
    var selectedItem: TelnetListPageItem? = null
    @JvmField
    var selectedItemNumber: Int = 0

    fun setItem(index: Int, aItem: TelnetListPageItem?) {
        this._items[index] = aItem
    }

    fun getItem(index: Int): TelnetListPageItem? {
        return this._items[index]
    }

    fun clear() {
        var i = 0
        while (i < this._items.size && this._items[i] != null) {
            this._items[i] = null
            i++
        }
    }

    companion object {
        const val BLOCK_SIZE: Int = 20
    }
}
