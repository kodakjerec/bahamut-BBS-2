package com.kota.Bahamut.listPage

abstract class TelnetListPageBlock {
    private val pageItems = arrayOfNulls<TelnetListPageItem>(BLOCK_SIZE)
    @JvmField
    var maximumItemNumber: Int = 0 // 實際上頁面最大的顯示數量(編號/總數)，可能會小於 BLOCK_SIZE
    @JvmField
    var minimumItemNumber: Int = 0 // 實際上頁面最小的顯示數量(編號/總數)，可能會小於 BLOCK_SIZE
    @JvmField
    var selectedItem: TelnetListPageItem? = null // 當前選中的項目，可能為 null
    @JvmField
    var selectedItemNumber: Int = 0 // 當前選中的項目編號，從 1 開始，0 表示未選中

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
