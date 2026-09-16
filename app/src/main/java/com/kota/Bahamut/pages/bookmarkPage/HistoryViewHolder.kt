package com.kota.Bahamut.pages.bookmarkPage

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.getThemeColor
import com.kota.telnetUI.textView.TelnetTextViewNormal

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

    companion object {
        fun createView(context: Context): View {
            val density = context.resources.displayMetrics.density
            fun dp(value: Float): Int = (value * density + 0.5f).toInt()

            val root = LinearLayout(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_contentView
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
            }

            val dividerTop = View(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_DividerTop
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1f))
                setBackgroundColor(getThemeColor(R.attr.bahamut_dividerColor))
                visibility = View.GONE
            }
            root.addView(dividerTop)

            val rowLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            val bgView = LinearLayout(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_backgroundView
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(getThemeColor(R.attr.bahamut_pageBackground))
                setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
            }

            val status = TelnetTextViewNormal(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_Status
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = getContextString(R.string.title_)
                setTextColor(getThemeColor(R.attr.bahamut_boardItemStatusColor))
            }
            bgView.addView(status)

            val titleView = TelnetTextViewNormal(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_Title
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                ellipsize = android.text.TextUtils.TruncateAt.END
                isSingleLine = true
                text = getContextString(R.string.un_input)
                setTextColor(getThemeColor(R.attr.bahamut_defaultTextColor))
            }
            bgView.addView(titleView)

            rowLayout.addView(bgView)

            val buttonBlock = LinearLayout(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_ButtonBlock
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL
                visibility = View.GONE
            }

            val btnEdit = Button(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_Edit
                layoutParams = LinearLayout.LayoutParams(dp(45f), LinearLayout.LayoutParams.MATCH_PARENT)
                text = getContextString(R.string.edit_short)
                textSize = 14f
            }
            buttonBlock.addView(btnEdit)

            val dividerBtn = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(1f), LinearLayout.LayoutParams.MATCH_PARENT)
                setBackgroundColor(getThemeColor(R.attr.bahamut_dividerColor))
            }
            buttonBlock.addView(dividerBtn)

            val btnDelete = Button(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_Delete
                layoutParams = LinearLayout.LayoutParams(dp(45f), LinearLayout.LayoutParams.MATCH_PARENT)
                text = getContextString(R.string.delete_short)
                textSize = 14f
                setTextColor(Color.RED)
            }
            buttonBlock.addView(btnDelete)

            rowLayout.addView(buttonBlock)
            root.addView(rowLayout)

            val dividerBottom = View(context).apply {
                id = R.id.BoardExtendOptionalPage_historyItemView_DividerBottom
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1f))
                setBackgroundColor(getThemeColor(R.attr.bahamut_dividerColor))
            }
            root.addView(dividerBottom)

            return root
        }
    }
}