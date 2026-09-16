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
import com.kota.telnetUI.textView.TelnetTextViewSmall

class BookmarkViewHolder(view: View, private val mListener: BookmarkClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val authorLabel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)
    private val gyLabel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)
    private val markLevel: TextView = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)
    private val titleLevel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)
    private val btnEdit: Button? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Edit)
    private val btnDelete: Button? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Delete)
    private val buttonBlock: View? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_ButtonBlock)

    init {
        view.setOnClickListener(this)
        btnEdit?.setOnClickListener(this)
        btnDelete?.setOnClickListener(this)
        buttonBlock?.visibility = View.VISIBLE
    }

    fun setBookmark(bookmark: Bookmark?) {
        if (bookmark != null) {
            setTitle(bookmark.keyword)
            setAuthor(bookmark.author)
            setMark(bookmark.mark == "y")
            setGYNumber(bookmark.gy)
            return
        }
        clear()
    }

    fun setTitle(title: String?) {
        if (this.titleLevel != null) {
            this.titleLevel.text = if (title.isNullOrEmpty()) "未輸入" else title
        }
    }

    fun setAuthor(author: String?) {
        if (this.authorLabel != null) {
            this.authorLabel.text = if (author.isNullOrEmpty()) "未輸入" else author
        }
    }

    fun setGYNumber(number: String?) {
        if (this.gyLabel != null) {
            this.gyLabel.text = if (number.isNullOrEmpty()) Bookmark.OPTIONAL_BOOKMARK else number
        }
    }

    fun setMark(isMarked: Boolean) {
        this.markLevel.visibility = if (isMarked) View.VISIBLE else View.INVISIBLE
    }

    fun clear() {
        setTitle(null)
        setAuthor(null)
        setGYNumber(null)
        setMark(false)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.BoardExtendOptionalPage_bookmarkItemView_Edit -> mListener?.onEditClick(view, bindingAdapterPosition)
            R.id.BoardExtendOptionalPage_bookmarkItemView_Delete -> mListener?.onDeleteClick(view, bindingAdapterPosition)
            else -> mListener?.onItemClick(view, bindingAdapterPosition)
        }
    }

    companion object {
        fun createView(context: Context): View {
            val density = context.resources.displayMetrics.density
            fun dp(value: Float): Int = (value * density + 0.5f).toInt()

            val root = LinearLayout(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_ContentView
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
            }

            val dividerTop = View(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop
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
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_BackgroundView
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(getThemeColor(R.attr.bahamut_pageBackground))
                setPadding(dp(10f), dp(6f), dp(10f), dp(6f))
            }

            val titleView = TelnetTextViewNormal(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_Title
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                ellipsize = android.text.TextUtils.TruncateAt.END
                maxLines = 2
                text = getContextString(R.string.un_input)
                setTextColor(getThemeColor(R.attr.bahamut_defaultTextColor))
            }
            bgView.addView(titleView)

            val subRow = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            val authorTitle = TelnetTextViewSmall(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_Author_Title
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isSingleLine = true
                text = getContextString(R.string.author_)
                setTextColor(getThemeColor(R.attr.bahamut_boardExAuthorTitleColor))
            }
            subRow.addView(authorTitle)

            val author = TelnetTextViewSmall(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_Author
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                isSingleLine = true
                text = getContextString(R.string.un_input)
                setTextColor(getThemeColor(R.attr.bahamut_boardExAuthorColor))
            }
            subRow.addView(author)

            val mark = TelnetTextViewSmall(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_Mark
                layoutParams = LinearLayout.LayoutParams(
                    dp(30f),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = getContextString(R.string.word_m)
                setTextColor(getThemeColor(R.attr.bahamut_boardItemMarkColor))
                visibility = View.INVISIBLE
            }
            subRow.addView(mark)

            val gyTitle = TelnetTextViewSmall(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_GY_Title
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = dp(12f)
                }
                text = getContextString(R.string.gy_)
                setTextColor(getThemeColor(R.attr.bahamut_articleContentColor0))
            }
            subRow.addView(gyTitle)

            val gy = TelnetTextViewSmall(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_GY
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = getContextString(R.string.number_0)
                setTextColor(getThemeColor(R.attr.bahamut_boardItemGyColor))
            }
            subRow.addView(gy)

            bgView.addView(subRow)
            rowLayout.addView(bgView)

            val buttonBlock = LinearLayout(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_ButtonBlock
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL
                visibility = View.GONE
            }

            val btnEdit = Button(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_Edit
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
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_Delete
                layoutParams = LinearLayout.LayoutParams(dp(45f), LinearLayout.LayoutParams.MATCH_PARENT)
                text = getContextString(R.string.delete_short)
                textSize = 14f
                setTextColor(Color.RED)
            }
            buttonBlock.addView(btnDelete)

            rowLayout.addView(buttonBlock)
            root.addView(rowLayout)

            val dividerBottom = View(context).apply {
                id = R.id.BoardExtendOptionalPage_bookmarkItemView_DividerBottom
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1f))
                setBackgroundColor(getThemeColor(R.attr.bahamut_dividerColor))
            }
            root.addView(dividerBottom)

            return root
        }
    }
}