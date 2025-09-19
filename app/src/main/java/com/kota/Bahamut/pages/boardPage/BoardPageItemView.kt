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
    var _author_label: TextView? = null
    private var _content_view: ViewGroup? = null
    private var _date_label: TextView? = null
    private var _divider_bottom: View? = null
    private var _gy_label: TextView? = null
    private var _gy_title_label: TextView? = null
    private var _mark_label: TextView? = null
    private var _number_label: TextView? = null
    private var _status_label: TextView? = null
    private var _title_label: TextView? = null

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
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.board_page_item_view,
            this
        )
        _content_view = findViewById<ViewGroup?>(R.id.BoardPage_ItemView_contentView)
        _status_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_Status)
        _title_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_Title)
        _number_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_Number)
        _date_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_Date)
        _gy_title_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_GY_Title)
        _gy_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_GY)
        _mark_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_mark)
        _author_label = _content_view?.findViewById<TextView?>(R.id.BoardPage_ItemView_Author)
        _divider_bottom = _content_view?.findViewById<View>(R.id.BoardPage_ItemView_DividerBottom)
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (_divider_bottom == null) {
            return
        }
        if (visible) {
            if (_divider_bottom?.getVisibility() != VISIBLE) {
                _divider_bottom?.setVisibility(VISIBLE)
            }
        } else if (_divider_bottom?.getVisibility() != GONE) {
            _divider_bottom?.setVisibility(GONE)
        }
    }

    fun setTitle(title: String?) {
        if (_title_label != null) {
            _title_label?.setText(
                Objects.requireNonNullElse<String?>(
                    title,
                    getContextString(R.string.loading_)
                )
            )
        }
    }

    var author: String?
        get() = _author_label?.getText() as String?
        set(author) {
            if (_author_label != null) {
                _author_label?.setText(
                    Objects.requireNonNullElse<String?>(
                        author,
                        getContextString(R.string.loading)
                    )
                )
            }
        }

    fun setDate(date: String?) {
        if (_date_label != null) {
            _date_label?.setText(
                Objects.requireNonNullElse<String?>(
                    date,
                    getContextString(R.string.loading)
                )
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun setNumber(number: Int) {
        if (_number_label != null) {
            if (number > 0) {
                _number_label?.setText(String.format("%1$05d", number))
                return
            }
            _number_label?.setText(getContextString(R.string.loading))
        }
    }

    fun setGYNumber(number: Int) {
        if (_gy_label == null) {
            return
        }
        if (number == 0) {
            _gy_title_label?.setVisibility(GONE)
            _gy_label?.setVisibility(GONE)
            return
        }
        _gy_title_label?.setVisibility(VISIBLE)
        _gy_label?.setVisibility(VISIBLE)
        _gy_label?.setText(number.toString())
    }

    @SuppressLint("SetTextI18n")
    fun setReply(isReply: Boolean) {
        if (_status_label == null) {
            return
        }
        // 戰巴哈只要看到第一篇和回應就可
        if (isReply) {
            _status_label?.setText("Re")
        } else {
            _status_label?.setText("◆")
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            _mark_label?.setVisibility(VISIBLE)
        } else {
            _mark_label?.setVisibility(INVISIBLE)
        }
    }

    // 設定已讀/未讀
    fun setRead(isRead: Boolean) {
        if (TempSettings.isBoardFollowTitle((_title_label?.getText() as kotlin.String?)!!)) { // 關注的討論串
            if (_status_label?.getText() == "◆") { // 首篇文章
                if (isRead) {
                    _title_label?.setTextColor(getContextColor(R.color.board_item_follow_first_read))
                } else {
                    _title_label?.setTextColor(getContextColor(R.color.board_item_follow_first))
                }
            } else { // 回應文章
                if (isRead) {
                    _title_label?.setTextColor(getContextColor(R.color.board_item_follow_other_read))
                } else {
                    _title_label?.setTextColor(getContextColor(R.color.board_item_follow_other))
                }
            }
        } else { // 其他文章
            if (isRead) {
                _title_label?.setTextColor(getContextColor(R.color.board_item_normal_read))
            } else {
                _title_label?.setTextColor(getContextColor(R.color.board_item_normal))
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
        get() = _content_view?.getVisibility() == VISIBLE
        set(visible) {
            if (visible) {
                if (_content_view?.getVisibility() != VISIBLE) {
                    _content_view?.setVisibility(VISIBLE)
                }
            } else if (_content_view?.getVisibility() != GONE) {
                _content_view?.setVisibility(GONE)
            }
        }

    companion object {
        private const val _count = 0
    }
}
