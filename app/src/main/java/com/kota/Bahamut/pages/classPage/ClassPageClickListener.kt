package com.kota.Bahamut.pages.classPage

import android.view.View

/**
 * [ClassPage] 項目與按鈕點擊事件回呼介面
 */
interface ClassPageClickListener {
    /**
     * 點擊看板列項目時觸發 (進入看板或子目錄)
     *
     * @param view 被點擊的 View
     * @param position 項目在 Adapter 中的位置
     */
    fun onItemClick(view: View?, position: Int)

    /**
     * 點擊「移出我的最愛」刪除按鈕時觸發
     *
     * @param view 被點擊的 View
     * @param position 項目在 Adapter 中的位置
     */
    fun onDeleteClick(view: View?, position: Int)

    /**
     * 長按看板列項目時觸發 (加入我的最愛)
     *
     * @param view 被點擊的 View
     * @param position 項目在 Adapter 中的位置
     * @return 若已處理長按事件傳回 true
     */
    fun onItemLongClick(view: View?, position: Int): Boolean
}
