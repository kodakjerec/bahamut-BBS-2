package com.kota.asFramework.ui

/**
 * [ASListView] 橫向快速滑動 (Fling) 選項觸發的回呼代理介面
 */
interface ASListViewExtentOptionalDelegate {
    /**
     * 當使用者在清單項目上執行橫向快速滑動時觸發
     *
     * @param paramASListView 觸發事件的 [ASListView] 實例
     * @param paramInt 被滑動項目的索引位置
     * @return 傳回 true 表示已處理該滑動手勢
     */
    fun onASListViewHandleExtentOptional(paramASListView: ASListView?, paramInt: Int): Boolean
}
