package com.kota.Bahamut.pages.bookmarkPage

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.theme.ThemeFunctions

class HistoryViewHolder(view: View, private val mListener: BookmarkClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val titleLabel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_historyItemView_Title)
    private val btnEdit: Button? = view.findViewById(R.id.BoardExtendOptionalPage_historyItemView_Edit)
    private val btnDelete: Button? = view.findViewById(R.id.BoardExtendOptionalPage_historyItemView_Delete)
    private val buttonBlock: View? = view.findViewById(R.id.BoardExtendOptionalPage_historyItemView_ButtonBlock)

    init {
        view.setOnClickListener(this)
        btnEdit?.visibility = View.GONE // 歷史紀錄不需要修改
        btnDelete?.setOnClickListener(this)
        buttonBlock?.visibility = View.VISIBLE
    }

    fun setBookmark(bookmark: Bookmark?) {
        if (bookmark != null) {
            setTitle(bookmark.keyword)

            return
        }
        clear()
    }

    fun setTitle(title: String?) {
        if (this.titleLabel != null) {
            this.titleLabel.text = if (title.isNullOrEmpty()) "未輸入" else title
        }
    }

    fun clear() {
        setTitle(null)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.BoardExtendOptionalPage_historyItemView_Delete -> mListener?.onDeleteClick(view, bindingAdapterPosition)
            else -> mListener?.onItemClick(view, bindingAdapterPosition)
        }
    }
}