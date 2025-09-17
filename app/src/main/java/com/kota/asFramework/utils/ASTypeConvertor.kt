package com.kota.asFramework.utils

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

object ASTypeConvertor {
    
    // 泛型整合方法 - 統一處理不同類型轉換
    inline fun <reified T> getData(value: T): ByteArray {
        return when (value) {
            is Boolean -> if (value) byteArrayOf(1) else byteArrayOf(0)
            is Short -> byteArrayOf((value.toInt() shr 8).toByte(), value.toByte())
            is Char -> byteArrayOf((value.code shr 8).toByte(), value.code.toByte())
            is Int -> byteArrayOf(
                (value shr 24).toByte(),
                (value shr 16).toByte(),
                (value shr 8).toByte(),
                value.toByte()
            )
            is Long -> byteArrayOf(
                (value shr 56).toByte(),
                (value shr 48).toByte(),
                (value shr 40).toByte(),
                (value shr 32).toByte(),
                (value shr 24).toByte(),
                (value shr 16).toByte(),
                (value shr 8).toByte(),
                value.toByte()
            )
            is Float -> getData(value.toInt())
            is Double -> getData(value.toLong()) 
            is String -> {
                try {
                    val stringData = value.toByteArray(charset("unicode"))
                    val sizeData = getData(stringData.size)
                    val data = ByteArray(stringData.size + 4)
                    sizeData.copyInto(data, 0)
                    stringData.copyInto(data, 4)
                    data
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    byteArrayOf()
                }
            }
            else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
        }
    }

    // 為了向後兼容，保留原有的方法名（可選）
    fun getData(aBooleanValue: Boolean): ByteArray = getData<Boolean>(aBooleanValue)
    fun getData(aShortValue: Short): ByteArray = getData<Short>(aShortValue)
    fun getData(aCharValue: Char): ByteArray = getData<Char>(aCharValue)
    fun getData(aIntValue: Int): ByteArray = getData<Int>(aIntValue)
    fun getData(aLongValue: Long): ByteArray = getData<Long>(aLongValue)
    fun getData(aFloatValue: Float): ByteArray = getData<Float>(aFloatValue)
    fun getData(aDoubleValue: Double): ByteArray = getData<Double>(aDoubleValue)
    fun getData(aString: String): ByteArray = getData<String>(aString)
}