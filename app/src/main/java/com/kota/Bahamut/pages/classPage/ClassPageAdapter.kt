package com.kota.Bahamut.pages.classPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.model.ClassPageItem

/**
 * 看板列表與我的最愛頁面的 RecyclerView Adapter。
 *
 * 職責說明：
 * 1. 管理並維護 [ClassPageItem] 看板項目清單。
 * 2. 依據 [isFavoriteMode] 切換每列項目的顯示樣式（我的最愛模式下顯示獨立刪除按鈕，一般模式下顯示右箭頭）。
 * 3. 管理 [ClassPageViewHolder] 的建立與資料綁定，並將點擊事件傳遞予 [ClassPageClickListener]。
 *
 * @property items 存放看板資料的動態清單
 */
class ClassPageAdapter(
    private val items: MutableList<ClassPageItem>
) : RecyclerView.Adapter<ClassPageViewHolder>() {

    /** 是否為「我的最愛」模式 */
    var isFavoriteMode: Boolean = false

    /** 項目與刪除按鈕點擊監聽器 */
    private var listener: ClassPageClickListener? = null

    /**
     * 設定事件監聽器代理
     *
     * @param listener 點擊事件回呼介面
     */
    fun setOnItemClickListener(listener: ClassPageClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.class_page_item_view, parent, false)
        return ClassPageViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ClassPageViewHolder, position: Int) {
        val item = getItem(position)
        holder.setItem(item, isFavoriteMode)
    }

    override fun getItemCount(): Int = items.size

    /**
     * 安全取得指定位置的看板項目
     *
     * @param position 清單索引位置
     * @return [ClassPageItem] 看板物件，若索引越界則傳回 null
     */
    fun getItem(position: Int): ClassPageItem? {
        return if (position in 0 until items.size) items[position] else null
    }
}
