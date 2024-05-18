package com.kota.Bahamut.Service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.util.Log;

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

    public static String intToRGB(int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    /* 輸入 R.color.XX 回傳 顏色內容(int)
    * @param R.color.XX
    * @response int
    *  */
    public static int getContextColor(int r_color_item) {
        return myContext.getColor(r_color_item);
    }

    /* 輸入 R.string.XX 回傳 文字內容(string)
     * @param R.string.XX
     * @response string
     *  */
    public static String getContextString(int r_string_item) { return myContext.getString(r_string_item); }

    // 調整螢幕方向
    public static void changeScreenOrientation() {
        int val = UserSettings.getPropertiesScreenOrientation();
        switch (val) {
            case 0:
                myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case 1:
                myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 2:
                myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }
    
    public static void getColorARGB(int colorCode) {
        int red = Color.red(colorCode);
        int green = Color.green(colorCode);
        int blue = Color.blue(colorCode);
        int alpha = Color.alpha(colorCode);
        Log.v("ColorCode","A:"+ alpha + " R:"+red+" G:"+green+" B"+blue);
    }
}
