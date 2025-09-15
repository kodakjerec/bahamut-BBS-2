package com.kota.Telnet.Logic

class ItemUtils {
    companion object {
        @JvmStatic
        fun getBlock(aItemIndex: Int): Int {
            return (aItemIndex - 1) / 20
        }
    }
}
