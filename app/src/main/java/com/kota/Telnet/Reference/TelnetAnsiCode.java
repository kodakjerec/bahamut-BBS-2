package com.kota.Telnet.Reference;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.core.internal.view.SupportMenu;
import androidx.core.view.InputDeviceCompat;

import com.kota.Telnet.TelnetAnsi;

public class TelnetAnsiCode {
    public static final int[] BACKGROUND_COLOR_NORMAL = {View.MEASURED_STATE_MASK, -8388608, -16744448, -8355840, -16777088, -8388480, -16744320, -4144960};
    @SuppressLint("RestrictedApi")
    public static final int[] COLOR_BRIGHT = {-8355712, SupportMenu.CATEGORY_MASK, -16711936, InputDeviceCompat.SOURCE_ANY, -16776961, -65281, -16711681, -1};
    public static final int[] TEXT_COLOR_NORMAL = {View.MEASURED_STATE_MASK, -8388608, -16744448, -8355840, -16777088, -8388480, -16744320, -4144960};

    public static class Code {
        public static final int CHA = 6;
        public static final int CNL = 4;
        public static final int CPL = 5;
        public static final int CUB = 3;
        public static final int CUD = 1;
        public static final int CUF = 2;
        public static final int CUP = 7;
        public static final int CUU = 0;
        public static final int DSR = 14;
        public static final int ED = 8;
        public static final int EL = 9;
        public static final int HC = 17;
        public static final int HVP = 12;
        public static final int RCP = 16;
        public static final int SC = 18;
        public static final int SCP = 15;
        public static final int SD = 11;
        public static final int SGR = 13;
        public static final int SU = 10;
    }

    public static class Color {
        public static final byte BLACK = 0;
        public static final byte RED = 1;
        public static final byte GREEN = 2;
        public static final byte YELLOW = 3;
        public static final byte BLUE = 4;
        public static final byte MAGENTA = 5;
        public static final byte CYAN = 6;
        public static final byte WHITE = 7;
    }

    /**
     * 瀏覽文章, 返回前景色
     * @param colorIndex index
     * @return int (color code)
     */
    public static int getTextColor(byte colorIndex) {
        int color_index = colorIndex & 255;
        if (colorIndex < 8) {
            return TEXT_COLOR_NORMAL[color_index];
        }
        try {
            return COLOR_BRIGHT[color_index - 8];
        } catch (Exception e) {
            e.printStackTrace();
            return -4144960;
        }
    }

    /**
     * 瀏覽文章, 返回背景色
     * @param colorIndex index
     * @return int (color code)
     */
    public static int getBackgroundColor(byte colorIndex) {
        int color_index = colorIndex & 255;
        if (colorIndex < 8) {
            return BACKGROUND_COLOR_NORMAL[color_index];
        }
        try {
            return COLOR_BRIGHT[color_index - 8];
        } catch (Exception e) {
            e.printStackTrace();
            return -4144960;
        }
    }

    /**
     * 修改文章, 返回前景字碼
     * @param colorIndex  index
     * @return string
     */
    public static String getTextAsciiCode(byte colorIndex) {
        int color_index = colorIndex & 255;
        if (color_index == TelnetAnsi.getDefaultTextColor()) {
            return "";
        }
        if (colorIndex < 8) {
            return "3" + colorIndex;
        }
        try {
            return "1;3" + (color_index - 8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 修改文章, 返回背景字碼
     * @param colorIndex  index
     * @return string
     */
    public static String getBackAsciiCode(byte colorIndex) {
        int color_index = colorIndex & 255;
        if (color_index == TelnetAnsi.getDefaultBackgroundColor()) {
            return "";
        }
        if (colorIndex < 8) {
            return "4" + colorIndex;
        }
        return "";
    }
}
