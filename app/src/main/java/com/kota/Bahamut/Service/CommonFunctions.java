package com.kota.Bahamut.Service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.kota.Telnet.UserSettings;

public class CommonFunctions {
    @SuppressLint("StaticFieldLeak")
    private static Context myContext;
    @SuppressLint("StaticFieldLeak")
    private static Activity myActivity;
    static UserSettings _settings;

    public static void initialCFContext(Context fromContext) {
        myContext = fromContext;
        _settings = new UserSettings(myContext);
    }
    public static void initialCFActivity(Activity activity) {
        myActivity = activity;
    }

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
        int val = _settings.getPropertiesScreenOrientation();
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
}
