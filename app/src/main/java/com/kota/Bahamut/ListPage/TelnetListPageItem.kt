package com.kota.Bahamut.ListPage

open class TelnetListPageItem {
    var Number = 0
    var isBlocked = false
    var isDeleted = false
    var isLoading = false

    fun set(aData: TelnetListPageItem?) {
        if (aData != null) {
            isDeleted = aData.isDeleted
            isLoading = aData.isLoading
        }
    }

    fun clear() {
        Number = 0
        isDeleted = false
        isLoading = false
        isBlocked = false
    }
}
