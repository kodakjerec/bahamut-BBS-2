package com.kota.Bahamut.pages.model

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import java.util.Objects

class BoardEssencePageItemView : LinearLayout {
    private var authorLabel: TextView? = null
    private var contentView: ViewGroup? = null
    private var dateLabel: TextView? = null
    private var dividerBottom: View? = null
    private var numberLabel: TextView? = null
    private var statusLabel: TextView? = null
    private var titleLabel: TextView? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun setItem(aItem: BoardEssencePageItem?) {
        if (aItem != null) {
            setTitle(aItem.title)
            setNumber(aItem.itemNumber)
            setDate(aItem.date)
            author = aItem.author
            aItem.isBBSClickable = aItem.isBBSClickable
            setDirectory(aItem.isDirectory, aItem.isBBSClickable)
            return
        }
        clear()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.board_page_item_view,
            this
        )
        contentView = findViewById(R.id.BoardPage_ItemView_contentView)
        if (contentView!=null) {
            statusLabel = contentView!!.findViewById(R.id.BoardPage_ItemView_Status)
            titleLabel = contentView!!.findViewById(R.id.BoardPage_ItemView_Title)
            numberLabel = contentView!!.findViewById(R.id.BoardPage_ItemView_Number)
            dateLabel = contentView!!.findViewById(R.id.BoardPage_ItemView_Date)
            authorLabel = contentView!!.findViewById(R.id.BoardPage_ItemView_Author)
            dividerBottom = contentView!!.findViewById(R.id.BoardPage_ItemView_DividerBottom)
            contentView!!.findViewById<TextView>(R.id.BoardPage_ItemView_mark).visibility = View.INVISIBLE
            contentView!!.findViewById<TextView>(R.id.BoardPage_ItemView_GY_Title).visibility = View.INVISIBLE
            contentView!!.findViewById<TextView>(R.id.BoardPage_ItemView_GY).visibility = View.INVISIBLE
        }
    }

    fun setTitle(title: String?) {
        if (titleLabel != null) {
            titleLabel!!.text = Objects.requireNonNullElse(
                title,
                CommonFunctions.getContextString(R.string.loading_)
            )
        }
    }

    var author: String?
        get() = authorLabel!!.text as String
        set(author) {
            if (authorLabel != null) {
                authorLabel!!.text = Objects.requireNonNullElse(
                    author,
                    CommonFunctions.getContextString(R.string.loading)
                )
            }
        }

    fun setDate(date: String?) {
        if (dateLabel != null) {
            dateLabel!!.text = Objects.requireNonNullElse(
                date,
                CommonFunctions.getContextString(R.string.loading)
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun setNumber(number: Int) {
        if (numberLabel != null) {
            if (number > 0) {
                numberLabel!!.text = String.format("%1$05d", number)
                return
            }
            numberLabel!!.text = CommonFunctions.getContextString(R.string.loading)
        }
    }

    fun clear() {
        setTitle(null)
        setDate(null)
        author = null
        setNumber(0)
        setDirectory(false, false)
    }

    private fun setDirectory(isDirectory: Boolean, isBBSClickable: Boolean) {
        var statusText: String
        if (isDirectory) {
            statusText = "◆"
        } else {
            statusText = "◇"
        }
        if (!isBBSClickable) {
            statusText += "("
            isClickable = true
        }

        statusLabel!!.text = statusText
    }
}
