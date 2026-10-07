package com.kota.Bahamut.service

import com.kota.telnet.TelnetArticle

/**
 * 從串接頁 (BoardLinkPage / BoardSearchPage) 編輯文章的定位狀態機資料類別。
 *
 * 用於在串接頁面中觸發「編輯文章」時，追蹤由串接頁面切換回主看板、定位版面編號 (boardNumber)、
 * 驗證文章特徵並最終進入編輯視窗的完整步驟流程。
 *
 * @property targetArticle 目標文章 (包含特徵：title, author, dateTime)
 */
class EditFromLinkedState(
    rawArticle: TelnetArticle,
) {
    /** 獨立複製一份目標文章副本，避免原串接頁面清空 TelnetArticle 時連帶清空此處的特徵 */
    val targetArticle: TelnetArticle = rawArticle.clone()
    /** 從 BBS "t" 鍵查詢解析所得之真正的版面文章編號 */
    var boardNumber: Int = 0

    /** 當前定位步驟 */
    var step: EditFromLinkedStep = EditFromLinkedStep.INIT

    /** 驗證不一致時的重試次數 */
    var retryCount: Int = 0

    /** 目標文章在串接頁清單中的序號 */
    val articleNumber: Int get() = targetArticle.articleNumber

    /** 是否為 BBS 畫面區塊邊界 (20 的倍數) */
    val isBlockBoundary: Boolean get() = (articleNumber % 20 == 0)

    /** 是否為當前分頁第一篇 */
    var isFirstInPage: Boolean = false

    /** 是否為全看板的第一篇文章 (articleNumber == 1) */
    var isFirst: Boolean = false

    /** 是否為全串接清單的最後一篇文章 */
    var isLast: Boolean = false

    /**
     * 驗證載入的文章特徵是否與目標文章一致
     *
     * 比對內容包括：文章標題 (title)、作者 (author) 以及日期時間 (dateTime)
     *
     * @param article BBS 實際載入的文章物件
     * @return 若標題、作者與日期皆吻合則傳回 true
     */
    fun matchesTarget(article: TelnetArticle): Boolean {
        // 1. 比對作者 (不區分大小寫)
        if (!article.author.equals(targetArticle.author, ignoreCase = true)) return false

        // 2. 比對標題 (自動移除 "Re: " 前綴與前後空白後比對)
        val targetCleanTitle = cleanTitle(targetArticle.title)
        val articleCleanTitle = cleanTitle(article.title)
        if (targetCleanTitle != articleCleanTitle) return false

        // 3. dateTime 可能存在格式微異，檢查是否互相包含
        if (targetArticle.dateTime.isNotEmpty() && !article.dateTime.contains(targetArticle.dateTime)) {
            return false
        }
        return true
    }

    /** 輔助方法：清理標題字串 (去除 "Re: " 前綴與前後空白) */
    private fun cleanTitle(title: String): String {
        var t = title.trim()
        if (t.startsWith("Re: ", ignoreCase = true)) {
            t = t.substring(4).trim()
        }
        return t
    }
}

/**
 * 從串接頁編輯文章定位流程的步驟枚舉
 */
enum class EditFromLinkedStep {
    /** 初始狀態 */
    INIT,

    /** 例外處理：已送出 UP 鍵移往上一筆，等待到達目標行 */
    MOVE_UP_FOR_BOUNDARY,

    /** 已送出 "t" 鍵，等待解析游標所在行的版面文章編號 (boardNumber) */
    SENT_T,

    /** 已送出 Left 鍵離開串接頁，等待返回主看板頁面 (BoardMainPage) */
    LEAVING_LINKED_PAGE,

    /** 已回到主看板頁面，準備發送跳轉或搜尋指令 */
    ON_BOARD_PAGE,

    /** 正在載入並讀取目標文章內文 */
    READING_ARTICLE,

    /** 特徵驗證中 */
    VERIFYING,

    /** 例外處理：送出 "]" 鍵搜尋下一篇同標題文章 */
    SEARCH_NEXT,

    /** 例外處理：送出 "[" 鍵搜尋上一篇同標題文章 */
    SEARCH_PREV,

    /** 例外處理：定位至版面最後項目 */
    GOTO_LAST,

    /** 定位驗證成功，進入文章編輯模式 */
    DONE,

    /** 定位驗證失敗，提示錯誤並傳回原頁面 */
    FAILED
}
