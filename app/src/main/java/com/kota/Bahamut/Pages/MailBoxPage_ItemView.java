package com.kota.Bahamut.Pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.Pages.Model.MailBoxPageItem;
import com.kota.Bahamut.R;
import java.util.Objects;

public class MailBoxPage_ItemView extends LinearLayout {
    private static final int _count = 0;
    private TextView _author = null;
    private TextView _date = null;
    private View _divider_bottom = null;
    private TextView _mark = null;
    private TextView _number = null;
    private TextView _reply = null;
    private TextView _status = null;
    private TextView _title = null;

    public MailBoxPage_ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MailBoxPage_ItemView(Context context) {
        super(context);
        init();
    }

    /* access modifiers changed from: protected */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void setItem(MailBoxPageItem aItem) {
        if (aItem != null) {
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

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mail_box_page_item_view, this);
        this._status = findViewById(R.id.MailBoxPage_ItemView_Status);
        this._title = findViewById(R.id.MailBoxPage_ItemView_Title);
        this._number = findViewById(R.id.MailBoxPage_ItemView_Number);
        this._date = findViewById(R.id.MailBoxPage_ItemView_Date);
        this._mark = findViewById(R.id.MailBoxPage_ItemView_Mark);
        this._author = findViewById(R.id.MailBoxPage_ItemView_Author);
        this._reply = findViewById(R.id.MailBoxPage_ItemView_Reply);
        this._divider_bottom = findViewById(R.id.MailBoxPage_ItemView_DividerBottom);
    }

    public void setTitle(String title) {
        if (this._title != null) {
            this._title.setText(Objects.requireNonNullElse(title, "讀取中..."));
        }
    }

    public void setAuthor(String author) {
        if (this._author != null) {
            this._author.setText(Objects.requireNonNullElse(author, "讀取中"));
        }
    }

    public void setDate(String date) {
        if (this._date != null) {
            this._date.setText(Objects.requireNonNullElse(date, "讀取中"));
        }
    }

    @SuppressLint({"DefaultLocale"})
    public void setIndex(int number) {
        if (this._number != null) {
            if (number > 0) {
                this._number.setText(String.format("%1$05d", number));
                return;
            }
            this._number.setText("讀取中");
        }
    }

    public void setReply(boolean isReply) {
        if (isReply) {
            this._reply.setVisibility(View.VISIBLE);
        } else {
            this._reply.setVisibility(View.INVISIBLE);
        }
    }

    public void setMark(boolean isMarked) {
        if (isMarked) {
            this._mark.setVisibility(View.VISIBLE);
        } else {
            this._mark.setVisibility(View.INVISIBLE);
        }
    }

    public void setRead(boolean isRead) {
        if (isRead) {
            this._status.setText("◇");
            this._title.setTextColor(-8355712);
            return;
        }
        this._status.setText("◆");
        this._title.setTextColor(-1);
    }

    public void clear() {
        setTitle(null);
        setDate(null);
        setAuthor(null);
        setIndex(0);
        setRead(true);
        setReply(false);
        setMark(false);
    }

    public void setDividerBottomVisible(boolean visible) {
        if (this._divider_bottom == null) {
            return;
        }
        if (visible) {
            if (this._divider_bottom.getVisibility() != View.VISIBLE) {
                this._divider_bottom.setVisibility(View.VISIBLE);
            }
        } else if (this._divider_bottom.getVisibility() != View.GONE) {
            this._divider_bottom.setVisibility(View.GONE);
        }
    }
}
