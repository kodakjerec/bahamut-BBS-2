package com.kota.Telnet

import android.util.Log
import com.kota.Telnet.Model.TelnetRow

class TelnetUtils {
    companion object {
        @JvmStatic
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
            return try {
                val temp = aRow.getSpaceString(from, to).trim()
                if (temp.isNotEmpty()) {
                    temp.toInt()
                } else {
                    0
                }
            } catch (e: Exception) {
                Log.e(TelnetUtils::class.java.simpleName, e.message ?: "")
                0
            }
        }

        @JvmStatic
        fun getHeader(source: String): String {
            val trimSource = source.replace(" ", "")
            return if (trimSource.length > 1) {
                trimSource.substring(0, 2)
            } else {
                ""
            }
        }
    }
}
