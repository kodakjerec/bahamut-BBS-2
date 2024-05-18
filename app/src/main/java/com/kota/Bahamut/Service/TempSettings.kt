package com.kota.Bahamut.Service

import android.annotation.SuppressLint
import com.kota.Bahamut.DataModels.BookmarkStore

/*
* 此處存放app執行階段使用的變數, 存放於記憶體, 關閉就消失
* */
object TempSettings {
    @JvmField
    var isUnderAutoToChat = false // 正在自動登入ing
    @JvmField
    var isFloatingInvisible = false // 浮動工具列是否正處於隱藏狀態
    private var boardFollowTitle = "" // 正在看的討論串標題
    private var _transportType = -1 // 網路狀況
    @JvmField
    var lastVisitBoard = "" // 最後離開的看板
    @SuppressLint("StaticFieldLeak")
    var _bookmarkStore: BookmarkStore? = null // 公用的bookmarkStore

    // 清空數據但不包含 網路狀況
    @JvmStatic
    fun clearTempSettings() {
        isUnderAutoToChat = false
        isFloatingInvisible = false
        setBoardFollowTitle("")
    }

    @JvmStatic
    fun setBoardFollowTitle(title: String) {
        boardFollowTitle = title
    }

    @JvmStatic
    fun isBoardFollowTitle(readTitle: String): Boolean {
        return boardFollowTitle == readTitle
    }

    @JvmStatic
    fun setTransportType(fromTransportType: Int) {
        _transportType = fromTransportType
    }

    @JvmStatic
    fun getTransportType(): Int {
        return _transportType
    }

    @JvmStatic
    fun setBookmarkStore(fromBookmarkStore: BookmarkStore?) {
        _bookmarkStore = fromBookmarkStore
    }

    @JvmStatic
    fun getBookmarkStore(): BookmarkStore? {
        return _bookmarkStore
    }
}
