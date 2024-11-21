package com.kota.Bahamut.Service

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.util.Log
import com.kota.Telnet.Reference.TelnetDefs
import com.kota.TextEncoder.B2UEncoder
import com.kota.TextEncoder.U2BEncoder
import java.util.Arrays
import java.util.Objects

@SuppressLint("StaticFieldLeak")
object CommonFunctions {
    /** 輸入 color int 回傳 顏色內容字串(#FF123456)
     * @param intColor color int
     * @response string
     */
    @JvmStatic
    fun intToRGB(intColor: Int): String {
        return String.format("#%08X", intColor)
    }

    /** 輸入 顏色內容字串(#FF123456) 回傳 color int
     * @param stringColor color string
     * @response int
     */
    @JvmStatic
    fun rgbToInt(stringColor: String): Int {
        // 移除 # 號
        val colorString = stringColor.replace("#", "")

        // 將十六進位字串轉換為整數
        val colorA = colorString.substring(0, 2).toInt(16)
        val colorR = colorString.substring(2, 4).toInt(16)
        val colorG = colorString.substring(4, 6).toInt(16)
        val colorB = colorString.substring(6, 8).toInt(16)

        // 將 A、R、G、B 四個成分組合成一個 int 型的顏色值
        return colorA shl 24 or (colorR shl 16) or (colorG shl 8) or colorB
    }

    /** 輸入 R.color.XX 回傳 顏色內容(int)
     * @param rColorItem R.color.XX
     * @response int
     */
    @JvmStatic
    fun getContextColor(rColorItem: Int): Int {
        return TempSettings.myContext?.getColor(rColorItem) ?:0
    }

    /** 輸入 R.string.XX 回傳 文字內容(string)
     * @param rStringItem R.string.XX
     * @response string
     */
    @JvmStatic
    fun getContextString(rStringItem: Int): String {
        return TempSettings.myContext?.getString(rStringItem) ?:""
    }

    /** 調整螢幕方向  */
    @JvmStatic
    fun changeScreenOrientation() {
        when (UserSettings.getPropertiesScreenOrientation()) {
            0 -> TempSettings.myActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            1 -> TempSettings.myActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            2 -> TempSettings.myActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    /** 傳入字串,依照第 maxColumn 個字元分割後, 再回傳之後的字串
     * @param fromContent 來源字串
     * @param maxLength 一行最多幾個字元
     */
    @JvmStatic
    fun judgeDoubleWord(fromContent: String, fromMaxLength: Int): String {
        var oldLineAuthorChar = "" // 記錄前一行開頭是不是引用, 如果本行不是引用擇要加分行
        val returnArrays: MutableList<String> = ArrayList()
        // 分割成字串陣列
        try {
            val arrays = fromContent.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (array in arrays) {
                var maxLength = fromMaxLength
                // 遇到回應文章, 拉寬限制
                if (array.startsWith("> > "))
                    maxLength+=4
                else if (array.startsWith("> "))
                    maxLength+=2
                val data2 = array.toByteArray(charset(TelnetDefs.CHARSET))
                var data1 = U2BEncoder.getInstance().encodeToBytes(data2, 0)
                while (data1.size >= maxLength) {
                    var isControlCode = false
                    var column = 0 // 現在取得的字元index
                    var cutLength = 0 // 要截斷的長度
                    // 逐行判斷第 maxColumn 個字元
                    while (cutLength < maxLength && column < data1.size) {
                        val compareData = data1[column].toInt() and 255
                        if (compareData > 127) {
                            column++
                            cutLength++
                        }
                        // 遇到 *[ 標記為控制碼區塊, 直到 m 都不列入字元參考
                        if (compareData == 42 && column + 1 < data1.size && data1[column + 1].toInt() == 91) {
                            isControlCode = true
                        }
                        column++
                        if (!isControlCode) { // 非控制碼區塊照常計算
                            if (compareData > 127 && cutLength >= maxLength) { // 截斷的剛好是雙字元第一位, 則退位
                                column -= 2
                            }
                            cutLength++
                        }
                        if (isControlCode) {
                            // 遇到 m, 下一輪回復正常
                            if (compareData == 109) isControlCode = false
                            // 控制碼狀態下出現不該出現的文字, 下一輪回復正常
                        }
                    }
                    val newCharArray = Arrays.copyOfRange(data1, 0, column)
                    var inputString = B2UEncoder.getInstance().encodeToString(newCharArray)

                    inputString+="\n"
                    returnArrays.add(inputString)

                    data1 = Arrays.copyOfRange(data1, column, data1.size)
                }
                // 如果data2有資料, 而最後剩餘出來的data1無資料, 代表這是截斷字串後的餘料, 不插入
                // 如果data2無資料, data1無料, 代表這是空白行
                if (!(data2.isNotEmpty() && data1.isEmpty())) {
                    val inputString = B2UEncoder.getInstance().encodeToString(data1)
                    returnArrays.add(inputString+"\n")
                }
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message.toString())
        }
        return java.lang.String.join("", returnArrays)
    }
}
