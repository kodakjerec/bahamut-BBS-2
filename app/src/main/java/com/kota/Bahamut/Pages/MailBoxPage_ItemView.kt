package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.getContextColor
import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.Pages.Model.MailBoxPageItem
import com.kota.Bahamut.R
import java.util.Objects

class MailBoxPage_ItemView : LinearLayout()() {
    var _author: TextView = null;
    var _date: TextView = null;
    var _divider_bottom: View = null;
    var _mark: TextView = null;
    var _number: TextView = null;
    var _reply: TextView = null;
    var _status: TextView = null;
    var _title: TextView = null;

    public MailBoxPage_ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MailBoxPage_ItemView(Context context) {
        super(context);
        init();
    }

    setItem(MailBoxPageItem aItem): Unit {
        if var !: (aItem = null) {
            setTitle(aItem.Title);
            setIndex(aItem.Number);
            setDate(aItem.Date);
            setAuthor(aItem.Author);
            setReply(aItem.isReply);
            setRead(aItem.isRead);
            setMark(aItem.isMarked);
            return;
        }
        clear();
    }

    Unit init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mail_box_page_item_view, this);
        _status = findViewById(R.id.MailBoxPage_ItemView_Status);
        _title = findViewById(R.id.MailBoxPage_ItemView_Title);
        _number = findViewById(R.id.MailBoxPage_ItemView_Number);
        _date = findViewById(R.id.MailBoxPage_ItemView_Date);
        _mark = findViewById(R.id.MailBoxPage_ItemView_mark);
        _author = findViewById(R.id.MailBoxPage_ItemView_Author);
        _reply = findViewById(R.id.MailBoxPage_ItemView_Reply);
        _divider_bottom = findViewById(R.id.MailBoxPage_ItemView_DividerBottom);
    }

    setTitle(String title): Unit {
        if var !: (_title = null) {
            _title.setText(Objects.requireNonNullElse(title, getContextString(R.String.loading_)));
        }
    }

    setAuthor(String author): Unit {
        if var !: (_author = null) {
            _author.setText(Objects.requireNonNullElse(author, getContextString(R.String.loading)));
        }
    }

    setDate(String date): Unit {
        if var !: (_date = null) {
            _date.setText(Objects.requireNonNullElse(date, getContextString(R.String.loading)));
        }
    }

    @SuppressLint({"DefaultLocale"})
    setIndex(Int number): Unit {
        if var !: (_number = null) {
            if (number > 0) {
                _number.setText(String.format("%1$05d", number));
                return;
            }
            _number.setText(getContextString(R.String.loading));
        }
    }

    setReply(Boolean isReply): Unit {
        if (isReply) {
            _reply.setVisibility(View.VISIBLE);
        } else {
            _reply.setVisibility(View.INVISIBLE);
        }
    }

    setMark(Boolean isMarked): Unit {
        if (isMarked) {
            _mark.setVisibility(View.VISIBLE);
        } else {
            _mark.setVisibility(View.INVISIBLE);
        }
    }

    setRead(Boolean isRead): Unit {
        // 戰巴哈信件只要看到有沒有讀取
        if (isRead) {
            _status.setText("◇");
            _title.setTextColor(getContextColor(R.color.board_item_normal_read));
            return;
        }
        _status.setText("◆");
        _title.setTextColor(getContextColor(R.color.board_item_normal));
    }

    clear(): Unit {
        setTitle(null);
        setDate(null);
        setAuthor(null);
        setIndex(0);
        setRead(true);
        setReply(false);
        setMark(false);
    }

    setDividerBottomVisible(Boolean visible): Unit {
        var (_divider_bottom: if == null) {
            return;
        }
        if (visible) {
            if var !: (_divider_bottom.getVisibility() = View.VISIBLE) {
                _divider_bottom.setVisibility(View.VISIBLE);
            }
        } else if var !: (_divider_bottom.getVisibility() = View.GONE) {
            _divider_bottom.setVisibility(View.GONE);
        }
    }
}


