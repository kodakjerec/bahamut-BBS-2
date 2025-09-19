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

class MailBoxPageItemView : LinearLayout {
    var authorTextView: TextView? = null
    var dateTextView: TextView? = null
    var dividerBottom: View? = null
    var markTextView: TextView? = null
    var numberTextView: TextView? = null
    var replyTextView: TextView? = null
    var statusTextView: TextView? = null
    var titleTextView: TextView? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun setItem(aItem: MailBoxPageItem?) {
        if (aItem != null) {
            setTitle(aItem.title)
            setIndex(aItem.itemNumber)
            setDate(aItem.date)
            setAuthor(aItem.author)
            setReply(aItem.isReply)
            setRead(aItem.isRead)
            setMark(aItem.isMarked)
            return
        }
        clear()
    }

    fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.mail_box_page_item_view,
            this
        )
        statusTextView = findViewById(R.id.MailBoxPage_ItemView_Status)
        titleTextView = findViewById(R.id.MailBoxPage_ItemView_Title)
        numberTextView = findViewById(R.id.MailBoxPage_ItemView_Number)
        dateTextView = findViewById(R.id.MailBoxPage_ItemView_Date)
        markTextView = findViewById(R.id.MailBoxPage_ItemView_mark)
        authorTextView = findViewById(R.id.MailBoxPage_ItemView_Author)
        replyTextView = findViewById(R.id.MailBoxPage_ItemView_Reply)
        dividerBottom = findViewById(R.id.MailBoxPage_ItemView_DividerBottom)
    }

    fun setTitle(title: String?) {
        if (titleTextView != null) {
            titleTextView?.text = Objects.requireNonNullElse<String?>(
                title,
                getContextString(R.string.loading_)
            )
        }
    }

    fun setAuthor(author: String?) {
        if (this@MailBoxPageItemView.authorTextView != null) {
            this@MailBoxPageItemView.authorTextView?.text = Objects.requireNonNullElse<String?>(
                author,
                getContextString(R.string.loading)
            )
        }
    }

    fun setDate(date: String?) {
        if (dateTextView != null) {
            dateTextView?.text = Objects.requireNonNullElse<String?>(
                date,
                getContextString(R.string.loading)
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun setIndex(number: Int) {
        if (numberTextView != null) {
            if (number > 0) {
                numberTextView?.text = String.format("%1$05d", number)
                return
            }
            numberTextView?.text = getContextString(R.string.loading)
        }
    }

    fun setReply(isReply: Boolean) {
        if (isReply) {
            replyTextView?.visibility = VISIBLE
        } else {
            replyTextView?.visibility = INVISIBLE
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            markTextView?.visibility = VISIBLE
        } else {
            markTextView?.visibility = INVISIBLE
        }
    }

    fun setRead(isRead: Boolean) {
        // 戰巴哈信件只要看到有沒有讀取
        if (isRead) {
            statusTextView?.text = "◇"
            titleTextView?.setTextColor(getContextColor(R.color.board_item_normal_read))
            return
        }
        statusTextView?.text = "◆"
        titleTextView?.setTextColor(getContextColor(R.color.board_item_normal))
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
}
