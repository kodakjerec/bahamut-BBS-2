package com.kota.Bahamut.command

interface BahamutCommandDef {
    companion object {
        const val LOAD_BLOCK: Int = 0 // 讀取區塊
        const val LOAD_FIRST_BLOCK: Int = 1 // 讀取第一個區塊
        const val LOAD_LAST_BLOCK: Int = 2 // 讀取最後一個區塊
        const val MOVE_TO_LAST_BLOCK: Int = 3 // 移動到最後一個區塊
        const val SEARCH_ARTICLE: Int = 4 // 搜尋文章
        const val LOAD_ARTICLE: Int = 5 // 讀取文章
        const val LIST_ARTICLE: Int = 6 // 列出文章
        const val DELETE_ARTICLE: Int = 7 // 刪除文章
        const val POST_ARTICLE: Int = 8 // 發文
        const val GOOD_ARTICLE: Int = 9 // 推薦
        const val LOAD_FORWARD_SAME_ARTICLE: Int = 10 // 同標題上一篇
        const val THE_SAME_TITLE_DOWN: Int = 11 // 同標題下一篇
        const val SEND_MAIL: Int = 12 // 寄信
        const val PUSH_ARTICLE: Int = 13 // 推文
        const val LOCATE_ARTICLE: Int = 14 // 定位文章
    }
}
