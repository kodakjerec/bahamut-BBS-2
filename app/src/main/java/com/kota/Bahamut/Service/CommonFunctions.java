package com.kota.Bahamut.Service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.kota.Telnet.Reference.TelnetDefs;
import com.kota.TextEncoder.B2UEncoder;
import com.kota.TextEncoder.U2BEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonFunctions {
    @SuppressLint("StaticFieldLeak")
    private static Context myContext;
    @SuppressLint("StaticFieldLeak")
    private static Activity myActivity;

    public static void initialCFContext(Context fromContext) {
        myContext = fromContext;
    }
    public static void initialCFActivity(Activity activity) {
        myActivity = activity;
    }
    public static Activity getActivity() { return myActivity; }
    /** 輸入 color int 回傳 顏色內容字串(#FF123456)
     * @param intColor color int
     * @response string
     *  */
    public static String intToRGB(int intColor) {
        return String.format("#%08X", intColor);
    }
    /** 輸入 顏色內容字串(#FF123456) 回傳 color int
     * @param stringColor color string
     * @response int
     *  */
    public static int rgbToInt(String stringColor) {
        // 移除 # 號
        String colorString = stringColor.replace("#", "");

        // 將十六進位字串轉換為整數
        int colorA = Integer.parseInt(colorString.substring(0,2), 16);
        int colorR = Integer.parseInt(colorString.substring(2,4), 16);
        int colorG = Integer.parseInt(colorString.substring(4,6), 16);
        int colorB = Integer.parseInt(colorString.substring(6,8), 16);

        // 將 A、R、G、B 四個成分組合成一個 int 型的顏色值
        int colorInt = (colorA << 24) | (colorR << 16) | (colorG << 8) | colorB;

        return colorInt;
    }

    /** 輸入 R.color.XX 回傳 顏色內容(int)
    * @param r_color_item R.color.XX
    * @response int
    *  */
    public static int getContextColor(int r_color_item) {
        return myContext.getColor(r_color_item);
    }

    /** 輸入 R.string.XX 回傳 文字內容(string)
     * @param r_string_item R.string.XX
     * @response string
     *  */
    public static String getContextString(int r_string_item) { return myContext.getString(r_string_item); }

    /** 調整螢幕方向 */
    public static void changeScreenOrientation() {
        int val = UserSettings.getPropertiesScreenOrientation();
        switch (val) {
            case 0 ->
                    myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            case 1 -> myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            case 2 -> myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /** 傳入字串,依照第 maxColumn 個字元分割後, 再回傳之後的字串
     * @param fromContent 來源字串
     * @param maxLength 一行最多幾個字元
     * */
    public static String judgeDoubleWord(String fromContent, Integer maxLength) {
        List<String> returnArrays = new ArrayList<>();
        // 分割成字串陣列
        try {
            String[] arrays = fromContent.split("\n");
            for (String array : arrays) {
                byte[] data2 = array.getBytes(TelnetDefs.CHARSET);
                byte[] data1 = U2BEncoder.getInstance().encodeToBytes(data2, 0);

                while (data1.length >= maxLength) {
                    boolean isControlCode = false;
                    int column = 0; // 現在取得的字元index
                    int cutLength = 0; // 要截斷的長度
                    // 逐行判斷第 maxColumn 個字元
                    while (cutLength < maxLength && column < data1.length) {
                        int compareData = data1[column] & 255;
                        if (compareData > 127) {
                            column++;
                            cutLength++;
                        }
                        // 遇到 *[ 標記為控制碼區塊, 直到 m 都不列入字元參考
                        if (compareData == 42 && (column+1<data1.length) && data1[(column+1)]==91) {
                            isControlCode = true;
                        }

                        column++;
                        if (!isControlCode) { // 非控制碼區塊照常計算
                            if (compareData > 127 && (cutLength)>=maxLength ) { // 截斷的剛好是雙字元第一位, 則退位
                                column-=2;
                            }
                            cutLength++;
                        }

                        if (isControlCode){
                            // 遇到 m, 下一輪回復正常
                            if (compareData == 109)
                                isControlCode = false;
                            // 控制碼狀態下出現不該出現的文字, 下一輪回復正常
                        }
                    }

                    byte[] newCharArray = Arrays.copyOfRange(data1, 0, column);
                    returnArrays.add(B2UEncoder.getInstance().encodeToString(newCharArray));
                    data1 = Arrays.copyOfRange(data1, column, data1.length);
                }
                // 如果data2有資料, 而最後剩餘出來的data1無資料, 代表這是截斷字串後的餘料, 不插入
                // 如果data2無資料, data1無料, 代表這是空白行
                if (!(data2.length>0 && data1.length==0)) {
                    returnArrays.add(B2UEncoder.getInstance().encodeToString(data1));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return String.join("\n", returnArrays);
    }

}
