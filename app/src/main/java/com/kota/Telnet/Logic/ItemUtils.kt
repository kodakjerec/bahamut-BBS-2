package com.kota.Telnet.Logic

object ItemUtils {
    @JvmStatic
    fun getBlock(aItemIndex: Int): Int {
        return (aItemIndex - 1) / 20
    }
}
