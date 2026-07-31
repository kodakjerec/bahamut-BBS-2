package com.kota.telnetUI

import com.kota.asFramework.pageController.ASViewController

abstract class TelnetPage : ASViewController() {
    override fun onPageDidUnload() {
        clear()
        super.onPageDidUnload()
    }

    open fun onPagePreload(): Boolean {
        return true
    }

    /** 告訴狀態機：這是一個彈出/本地頁面，收到 BBS 資料時不要自動切換掉我 */
    open val isPopupPage: Boolean
        get() = false

    /** 告訴系統：即使斷線了，也讓我留在這個頁面（因為這是本地管理功能） */
    open val isKeepOnOffline: Boolean
        get() = false
}
