package com.kota.telnet.logic

object ItemUtils {
    @JvmStatic
    fun getBlock(aItemIndex: Int): Int {
        return (aItemIndex - 1) / 20
    }
}
