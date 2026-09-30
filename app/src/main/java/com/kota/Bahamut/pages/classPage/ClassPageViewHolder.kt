package com.kota.Bahamut.pages.classPage

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.model.ClassPageItem

/**
 * 看板列表單元格 (Item) 的 ViewHolder 快取。
 *
 * 職責：
 * 1. 快取單元格內部 TextView、ArrowView 與 DeleteButton View 元件。
 * 2. 依據 [setItem] 填入看板中文名稱、英文代碼、版主群，並根據 isFavoriteMode 控制右側按鈕顯隱。
 * 3. 處理點擊與長按事件，分發 [ClassPageClickListener] 相關回呼。
 *
 * @param itemView 單元格 View
 * @param mListener 點擊事件代理介面
 */
class ClassPageViewHolder(
    itemView: View,
    private val mListener: ClassPageClickListener?
) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    val classTitle: TextView = itemView.findViewById(R.id.ClassPage_ItemView_classTitle)
    val className: TextView = itemView.findViewById(R.id.ClassPage_ItemView_className)
    val classManager: TextView = itemView.findViewById(R.id.ClassPage_ItemView_classManager)
    val arrowView: View = itemView.findViewById(R.id.ListItem_ArrowView)
    val deleteButton: Button = itemView.findViewById(R.id.ClassPage_ItemView_DeleteButton)
    val backgroundView: View = itemView.findViewById(R.id.ClassPage_ItemView_backgroundView)

    init {
        backgroundView.setOnClickListener(this)
        backgroundView.setOnLongClickListener(this)
        deleteButton.setOnClickListener(this)
    }

    /**
     * 綁定看板項目資料並切換樣式
     *
     * @param item [ClassPageItem] 看板資料物件
     * @param isFavoriteMode 是否為「我的最愛」模式
     * @param isDeleteManageMode 是否為「刪除管理」模式（顯示刪除按鈕）
     */
    fun setItem(item: ClassPageItem?, isFavoriteMode: Boolean, isDeleteManageMode: Boolean = false) {
        if (item != null) {
            classTitle.text = item.title
            className.text = item.name
            classManager.text = item.manager
        } else {
            classTitle.text = ""
            className.text = ""
            classManager.text = ""
        }

        if (isFavoriteMode && isDeleteManageMode) {
            deleteButton.visibility = View.VISIBLE
            arrowView.visibility = View.GONE
        } else {
            deleteButton.visibility = View.GONE
            arrowView.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        val pos = bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) return

        if (v?.id == R.id.ClassPage_ItemView_DeleteButton) {
            mListener?.onDeleteClick(v, pos)
        } else {
            mListener?.onItemClick(v, pos)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        val pos = bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) return false
        return mListener?.onItemLongClick(v, pos) ?: false
    }
}
