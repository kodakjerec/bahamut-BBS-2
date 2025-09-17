package com.kota.telnet

import android.util.Log
import com.kota.telnet.model.TelnetRow

object TelnetUtils {
    fun hashCode(aData: ByteArray): Int {
        var hash = 0
        var multiplier = 1
        for (i in aData.indices.reversed()) {
            hash += (aData[i].toInt() and 255) * multiplier
            multiplier = (multiplier shl 5) - multiplier
        }
        return hash
    }

    @JvmStatic
    fun getIntegerFromData(aRow: TelnetRow, from: Int, to: Int): Int {
        try {
            val temp = aRow.getSpaceString(from, to).trim { it <= ' ' }
            if (temp.isNotEmpty()) {
                return temp.toInt()
            }
            return 0
        } catch (e: Exception) {
            Log.e(
                TelnetUtils::class.java.simpleName,
                (if (e.message != null) e.message else "")!!
            )
            return 0
        }
    }

    @JvmStatic
    fun getHeader(source: String): String {
        val trimSource = source.replace(" ", "")
        if (trimSource.length > 1) {
            return trimSource.substring(0, 2)
        }
        return ""
    }
}
