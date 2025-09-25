package com.kota.Bahamut.pages.boardPage

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import java.util.Objects

class BoardPageItemView : LinearLayout {
    var authorLabel: TextView? = null
    private var contentView: ViewGroup? = null
    private var dateLabel: TextView? = null
    private var dividerBottom: View? = null
    private var gyLabel: TextView? = null
    private var gyTitleLabel: TextView? = null
    private var markLabel: TextView? = null
    private var numberLabel: TextView? = null
    private var statusLabel: TextView? = null
    private var titleLabel: TextView? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun setItem(aItem: BoardPageItem?) {
        if (aItem != null) {
            setTitle(aItem.title)
            setNumber(aItem.itemNumber)
            setDate(aItem.date)
            this.author = aItem.author
            setMark(aItem.isMarked)
            setGYNumber(aItem.gy)
            setReply(aItem.isReply)
            setRead(aItem.isDeleted || aItem.isRead)
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
        statusLabel = contentView?.findViewById(R.id.BoardPage_ItemView_Status)
        titleLabel = contentView?.findViewById(R.id.BoardPage_ItemView_Title)
        numberLabel = contentView?.findViewById(R.id.BoardPage_ItemView_Number)
        dateLabel = contentView?.findViewById(R.id.BoardPage_ItemView_Date)
        gyTitleLabel = contentView?.findViewById(R.id.BoardPage_ItemView_GY_Title)
        gyLabel = contentView?.findViewById(R.id.BoardPage_ItemView_GY)
        markLabel = contentView?.findViewById(R.id.BoardPage_ItemView_mark)
        authorLabel = contentView?.findViewById(R.id.BoardPage_ItemView_Author)
        dividerBottom = contentView?.findViewById(R.id.BoardPage_ItemView_DividerBottom)
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (dividerBottom == null) {
            return
        }
        if (visible) {
            if (dividerBottom?.visibility != VISIBLE) {
                dividerBottom?.visibility = VISIBLE
            }
        } else if (dividerBottom?.visibility != GONE) {
            dividerBottom?.visibility = GONE
        }
    }

    fun setTitle(title: String?) {
        if (titleLabel != null) {
            titleLabel?.text = Objects.requireNonNullElse<String?>(
                title,
                getContextString(R.string.loading_)
            )
        }
    }

    var author: String?
        get() = authorLabel?.text as String?
        set(author) {
            if (authorLabel != null) {
                authorLabel?.setText(
                    Objects.requireNonNullElse<String?>(
                        author,
                        getContextString(R.string.loading)
                    )
                )
            }
        }

    fun setDate(date: String?) {
        if (dateLabel != null) {
            dateLabel?.text = Objects.requireNonNullElse<String?>(
                date,
                getContextString(R.string.loading)
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun setNumber(number: Int) {
        if (numberLabel != null) {
            if (number > 0) {
                numberLabel?.text = String.format("%1$05d", number)
                return
            }
            numberLabel?.text = getContextString(R.string.loading)
        }
    }

    fun setGYNumber(number: Int) {
        if (gyLabel == null) {
            return
        }
        if (number == 0) {
            gyTitleLabel?.visibility = GONE
            gyLabel?.visibility = GONE
            return
        }
        gyTitleLabel?.visibility = VISIBLE
        gyLabel?.visibility = VISIBLE
        gyLabel?.text = number.toString()
    }

    @SuppressLint("SetTextI18n")
    fun setReply(isReply: Boolean) {
        if (statusLabel == null) {
            return
        }
        // 戰巴哈只要看到第一篇和回應就可
        if (isReply) {
            statusLabel?.text = "Re"
        } else {
            statusLabel?.text = "◆"
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            markLabel?.visibility = VISIBLE
        } else {
            markLabel?.visibility = INVISIBLE
        }
    }

    // 設定已讀/未讀
    fun setRead(isRead: Boolean) {
        if (TempSettings.isBoardFollowTitle((titleLabel?.text as String?)!!)) { // 關注的討論串
            if (statusLabel?.text == "◆") { // 首篇文章
                if (isRead) {
                    titleLabel?.setTextColor(getContextColor(R.color.board_item_follow_first_read))
                } else {
                    titleLabel?.setTextColor(getContextColor(R.color.board_item_follow_first))
                }
            } else { // 回應文章
                if (isRead) {
                    titleLabel?.setTextColor(getContextColor(R.color.board_item_follow_other_read))
                } else {
                    titleLabel?.setTextColor(getContextColor(R.color.board_item_follow_other))
                }
            }
        } else { // 其他文章
            if (isRead) {
                titleLabel?.setTextColor(getContextColor(R.color.board_item_normal_read))
            } else {
                titleLabel?.setTextColor(getContextColor(R.color.board_item_normal))
            }
        }
    }

    fun clear() {
        setTitle(null)
        setDate(null)
        this.author = null
        setNumber(0)
        setGYNumber(0)
        setRead(true)
        setReply(false)
        setMark(false)
    }

    var visible: Boolean
        get() = contentView?.visibility == VISIBLE
        set(visible) {
            if (visible) {
                if (contentView?.visibility != VISIBLE) {
                    contentView?.setVisibility(VISIBLE)
                }
            } else if (contentView?.visibility != GONE) {
                contentView?.setVisibility(GONE)
            }
        }

    companion object {
        private const val COUNT = 0
    }
}
