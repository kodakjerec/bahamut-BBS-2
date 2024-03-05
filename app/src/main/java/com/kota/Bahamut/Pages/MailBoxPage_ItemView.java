package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

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
    TextView _author = null;
    TextView _date = null;
    View _divider_bottom = null;
    TextView _mark = null;
    TextView _number = null;
    TextView _reply = null;
    TextView _status = null;
    TextView _title = null;

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

    void init() {
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

    public void setTitle(String title) {
        if (_title != null) {
            _title.setText(Objects.requireNonNullElse(title, getContextString(R.string.loading_)));
        }
    }

    public void setAuthor(String author) {
        if (_author != null) {
            _author.setText(Objects.requireNonNullElse(author, getContextString(R.string.loading)));
        }
    }

    public void setDate(String date) {
        if (_date != null) {
            _date.setText(Objects.requireNonNullElse(date, getContextString(R.string.loading)));
        }
    }

    @SuppressLint({"DefaultLocale"})
    public void setIndex(int number) {
        if (_number != null) {
            if (number > 0) {
                _number.setText(String.format("%1$05d", number));
                return;
            }
            _number.setText(getContextString(R.string.loading));
        }
    }

    public void setReply(boolean isReply) {
        if (isReply) {
            _reply.setVisibility(View.VISIBLE);
        } else {
            _reply.setVisibility(View.INVISIBLE);
        }
    }

    public void setMark(boolean isMarked) {
        if (isMarked) {
            _mark.setVisibility(View.VISIBLE);
        } else {
            _mark.setVisibility(View.INVISIBLE);
        }
    }

    public void setRead(boolean isRead) {
        if (isRead) {
            _status.setText("◇");
            _title.setTextColor(-8355712);
            return;
        }
        _status.setText("◆");
        _title.setTextColor(-1);
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
        if (_divider_bottom == null) {
            return;
        }
        if (visible) {
            if (_divider_bottom.getVisibility() != View.VISIBLE) {
                _divider_bottom.setVisibility(View.VISIBLE);
            }
        } else if (_divider_bottom.getVisibility() != View.GONE) {
            _divider_bottom.setVisibility(View.GONE);
        }
    }
}
