package com.kota.Bahamut.listPage

open class TelnetListPageItem {
    @JvmField
    var itemNumber: Int = 0
    @JvmField
    var isBlocked: Boolean = false
    @JvmField
    var isDeleted: Boolean = false
    var isLoading: Boolean = false

    open fun set(aData: TelnetListPageItem?) {
        if (aData != null) {
            this.isDeleted = aData.isDeleted
            this.isLoading = aData.isLoading
        }
    }

    open fun clear() {
        this.itemNumber = 0
        this.isDeleted = false
        this.isLoading = false
        this.isBlocked = false
    }
}
