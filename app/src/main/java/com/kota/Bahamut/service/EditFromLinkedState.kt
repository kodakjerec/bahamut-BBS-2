package com.kota.Bahamut.service

import com.kota.telnet.TelnetArticle

/**
 * 從串接頁編輯文章的狀態機
 *
 * 用於在 BoardLinkPage 或 BoardSearchPage 中編輯文章時，
 * 追蹤整個流程的狀態。
 *
 * @param targetArticle 目標文章（包含特徵：title, author, dateTime）
 */
data class EditFromLinkedState(
    val targetArticle: TelnetArticle
) {
    /** 從 "t" 取得的版面文章編號 */
    var boardNumber: Int = 0

    /** 當前步驟 */
    var step: EditFromLinkedStep = EditFromLinkedStep.INIT

    /** 重試次數 */
    var retryCount: Int = 0

    /** 在 LinkPage/SearchPage 中的位置 */
    val articleNumber: Int get() = targetArticle.articleNumber

    /** 是否為區塊邊界（20的倍數） */
    val isBlockBoundary: Boolean get() = articleNumber % 20 == 0

    /** 是否為最後一篇文章（boardNumber == 1 代表繞回第一篇） */
    var isLastArticle: Boolean = false

    /**
     * 驗證文章特徵是否一致
     *
     * 比較 title 和 author，dateTime 則檢查是否包含 targetArticle 的日期部分
     *
     * @param article 要驗證的文章
     * @return true 表示特徵一致
     */
    fun matchesTarget(article: TelnetArticle): Boolean {
        if (article.title != targetArticle.title) return false
        if (article.author != targetArticle.author) return false
        // dateTime 可能格式不同，檢查是否包含日期部分
        if (targetArticle.dateTime.isNotEmpty() &&
            !article.dateTime.contains(targetArticle.dateTime)) return false
        return true
    }
}

/**
 * 編輯流程的步驟定義
 */
enum class EditFromLinkedStep {
    /** 初始狀態 */
    INIT,

    /** (例外1) 已送出 Up，等待移到上一筆 */
    MOVE_UP_FOR_BOUNDARY,

    /** 已送出 "t"，等待解析 boardNumber */
    SENT_T,

    /** 已送出 "left"，等待進入 BoardMainPage */
    LEAVING_LINKED_PAGE,

    /** 已在 BoardMainPage，準備選擇文章 */
    ON_BOARD_PAGE,

    /** 正在讀取文章 */
    READING_ARTICLE,

    /** 驗證特徵中 */
    VERIFYING,

    /** (例外1) 送出 "]" 找下一篇 */
    SEARCH_NEXT,

    /** (例外2) 送出 "[" 找上一篇 */
    SEARCH_PREV,

    /** (例外2) 移到版面最後 */
    GOTO_LAST,

    /** 完成，進入編輯 */
    DONE,

    /** 失敗 */
    FAILED
}
