package com.kota.Bahamut.listPage

class ListState {
    @JvmField
    var position: Int = -1
    @JvmField
    var top: Int = -1

    fun clear() {
        this.top = -1
        this.position = -1
    }
}
