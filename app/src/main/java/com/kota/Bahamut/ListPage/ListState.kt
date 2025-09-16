package com.kota.Bahamut.ListPage

class ListState {
    @JvmField
    var Position: Int = -1
    @JvmField
    var Top: Int = -1

    fun clear() {
        this.Top = -1
        this.Position = -1
    }
}
