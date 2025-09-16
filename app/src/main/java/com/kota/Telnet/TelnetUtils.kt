package com.kota.Telnet

import android.util.Log
import com.kota.Telnet.Model.TelnetRow

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
            if (temp.length > 0) {
                return temp.toInt()
            }
            return 0
        } catch (e: Exception) {
            Log.e(
                TelnetUtils::class.java.getSimpleName(),
                (if (e.message != null) e.message else "")!!
            )
            return 0
        }
    }

    @JvmStatic
    fun getHeader(source: String): String {
        val trim_source = source.replace(" ", "")
        if (trim_source.length > 1) {
            return trim_source.substring(0, 2)
        }
        return ""
    }
}
