package com.kota.ASFramework.Utils

import java.io.UnsupportedEncodingException
import java.lang.Double
import java.lang.Float
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Char
import kotlin.Int
import kotlin.Long
import kotlin.Short
import kotlin.String
import kotlin.byteArrayOf
import kotlin.code

/* loaded from: classes.dex */
object ASTypeConvertor {
    fun getData(aBooleanValue: Boolean): ByteArray {
        return if (aBooleanValue) byteArrayOf(1) else byteArrayOf(0)
    }

    fun getData(aShortValue: Short): ByteArray {
        return byteArrayOf((aShortValue.toInt() shr 8).toByte(), aShortValue.toByte())
    }

    fun getData(aCharValue: Char): ByteArray {
        return byteArrayOf((aCharValue.code shr '\b'.code).toByte(), aCharValue.code.toByte())
    }

    fun getData(aIntValue: Int): ByteArray {
        return byteArrayOf(
            (aIntValue shr 24).toByte(),
            (aIntValue shr 16).toByte(),
            (aIntValue shr 8).toByte(),
            aIntValue.toByte()
        )
    }

    fun getData(aLongValue: Long): ByteArray {
        return byteArrayOf(
            (aLongValue shr 56).toByte(),
            (aLongValue shr 48).toByte(),
            (aLongValue shr 40).toByte(),
            (aLongValue shr 32).toByte(),
            (aLongValue shr 24).toByte(),
            (aLongValue shr 16).toByte(),
            (aLongValue shr 8).toByte(),
            aLongValue.toByte()
        )
    }

    fun getData(aFloatValue: Float): ByteArray {
        return getData(Float.floatToIntBits(aFloatValue))
    }

    fun getData(aDoubleValue: Double): ByteArray {
        return getData(Double.doubleToLongBits(aDoubleValue))
    }

    fun getData(aString: String): ByteArray {
        var string_data: ByteArray? = null
        try {
            string_data = aString.toByteArray(charset("unicode"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        val size_data = getData(string_data!!.size)
        val data = ByteArray(string_data.size + 4)
        for (i in 0..3) {
            data[i] = size_data[i]
        }
        for (i2 in string_data.indices) {
            data[i2 + 4] = string_data[i2]
        }
        return data
    }
}