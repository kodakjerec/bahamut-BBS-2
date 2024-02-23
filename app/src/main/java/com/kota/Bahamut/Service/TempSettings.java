package com.kota.Bahamut.Service;

/*
* 此處存放app執行階段使用的變數, 存放於記憶體, 關閉就消失
* */

public class TempSettings {
    private static Boolean isUnderAutoToChat = false; // 正在自動登入ing
    private static Boolean isFloatingInvisible = false; // 浮動工具列是否正處於隱藏狀態
    public static void setIsUnderAutoToChat(boolean isEnable) {
        isUnderAutoToChat = isEnable;
    }
    public static boolean isUnderAutoToChat() {
        return isUnderAutoToChat;
    }

    public static void setIsFloatingInvisible(boolean isEnable) {
        isFloatingInvisible = isEnable;
    }
    public static boolean isFloatingInvisible() {
        return isFloatingInvisible;
    }
}
