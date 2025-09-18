package com.kota.Bahamut.listPage

abstract class TelnetListPageBlock {
    private val pageItems = arrayOfNulls<TelnetListPageItem>(BLOCK_SIZE)
    @JvmField
    var maximumItemNumber: Int = 0
    @JvmField
    var minimumItemNumber: Int = 0
    @JvmField
    var selectedItem: TelnetListPageItem? = null
    @JvmField
    var selectedItemNumber: Int = 0

    fun setItem(index: Int, aItem: TelnetListPageItem?) {
        this.pageItems[index] = aItem
    }

    fun getItem(index: Int): TelnetListPageItem? {
        return this.pageItems[index]
    }

    fun clear() {
        var i = 0
        while (i < this.pageItems.size && this.pageItems[i] != null) {
            this.pageItems[i] = null
            i++
        }
    }

    companion object {
        const val BLOCK_SIZE: Int = 20
    }
}
