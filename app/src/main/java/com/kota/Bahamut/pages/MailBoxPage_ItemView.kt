package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.pages.model.MailBoxPageItem
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.CommonFunctions.getContextString
import java.util.Objects

class MailBoxPage_ItemView : LinearLayout {
    var _author: TextView? = null
    var _date: TextView? = null
    var _divider_bottom: View? = null
    var _mark: TextView? = null
    var _number: TextView? = null
    var _reply: TextView? = null
    var _status: TextView? = null
    var _title: TextView? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun setItem(aItem: MailBoxPageItem?) {
        if (aItem != null) {
            setTitle(aItem.Title)
            setIndex(aItem.itemNumber)
            setDate(aItem.Date)
            setAuthor(aItem.Author)
            setReply(aItem.isReply)
            setRead(aItem.isRead)
            setMark(aItem.isMarked)
            return
        }
        clear()
    }

    fun init() {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.mail_box_page_item_view,
            this
        )
        _status = findViewById<TextView?>(R.id.MailBoxPage_ItemView_Status)
        _title = findViewById<TextView?>(R.id.MailBoxPage_ItemView_Title)
        _number = findViewById<TextView?>(R.id.MailBoxPage_ItemView_Number)
        _date = findViewById<TextView?>(R.id.MailBoxPage_ItemView_Date)
        _mark = findViewById<TextView?>(R.id.MailBoxPage_ItemView_mark)
        _author = findViewById<TextView?>(R.id.MailBoxPage_ItemView_Author)
        _reply = findViewById<TextView?>(R.id.MailBoxPage_ItemView_Reply)
        _divider_bottom = findViewById<View?>(R.id.MailBoxPage_ItemView_DividerBottom)
    }

    fun setTitle(title: String?) {
        if (_title != null) {
            _title!!.setText(
                Objects.requireNonNullElse<String?>(
                    title,
                    getContextString(R.string.loading_)
                )
            )
        }
    }

    fun setAuthor(author: String?) {
        if (_author != null) {
            _author!!.setText(
                Objects.requireNonNullElse<String?>(
                    author,
                    getContextString(R.string.loading)
                )
            )
        }
    }

    fun setDate(date: String?) {
        if (_date != null) {
            _date!!.setText(
                Objects.requireNonNullElse<String?>(
                    date,
                    getContextString(R.string.loading)
                )
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun setIndex(number: Int) {
        if (_number != null) {
            if (number > 0) {
                _number!!.setText(String.format("%1$05d", number))
                return
            }
            _number!!.setText(getContextString(R.string.loading))
        }
    }

    fun setReply(isReply: Boolean) {
        if (isReply) {
            _reply!!.setVisibility(VISIBLE)
        } else {
            _reply!!.setVisibility(INVISIBLE)
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            _mark!!.setVisibility(VISIBLE)
        } else {
            _mark!!.setVisibility(INVISIBLE)
        }
    }

    fun setRead(isRead: Boolean) {
        // 戰巴哈信件只要看到有沒有讀取
        if (isRead) {
            _status!!.setText("◇")
            _title!!.setTextColor(getContextColor(R.color.board_item_normal_read))
            return
        }
        _status!!.setText("◆")
        _title!!.setTextColor(getContextColor(R.color.board_item_normal))
    }

    fun clear() {
        setTitle(null)
        setDate(null)
        setAuthor(null)
        setIndex(0)
        setRead(true)
        setReply(false)
        setMark(false)
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (_divider_bottom == null) {
            return
        }
        if (visible) {
            if (_divider_bottom!!.getVisibility() != VISIBLE) {
                _divider_bottom!!.setVisibility(VISIBLE)
            }
        } else if (_divider_bottom!!.getVisibility() != GONE) {
            _divider_bottom!!.setVisibility(GONE)
        }
    }
}
