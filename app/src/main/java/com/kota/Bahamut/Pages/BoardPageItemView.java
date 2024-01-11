package com.kota.Bahamut.Pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.bahamut_bbs_2.R;;

public class BoardPageItemView extends LinearLayout {
    private static final int _count = 0;
    private TextView _author_label = null;
    private ViewGroup _content_view = null;
    private TextView _date_label = null;
    private View _divider_bottom = null;
    private TextView _gy_label = null;
    private TextView _gy_title_label = null;
    private TextView _mark_label = null;
    private TextView _number_label = null;
    private TextView _status_label = null;
    private TextView _title_label = null;

    public BoardPageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardPageItemView(Context context) {
        super(context);
        init();
    }

    /* access modifiers changed from: protected */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void setItem(BoardPageItem aItem) {
        if (aItem != null) {
            setTitle(aItem.Title);
            setNumber(aItem.Number);
            setDate(aItem.Date);
            setAuthor(aItem.Author);
            setMark(aItem.isMarked);
            setGYNumber(aItem.GY);
            setReply(aItem.isReply);
            setRead(aItem.isDeleted || aItem.isRead);
            return;
        }
        clear();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.board_page_item_view, this);
        this._status_label = (TextView) findViewById(R.id.BoardPage_ItemView_Status);
        this._title_label = (TextView) findViewById(R.id.BoardPage_ItemView_Title);
        this._number_label = (TextView) findViewById(R.id.BoardPage_ItemView_Number);
        this._date_label = (TextView) findViewById(R.id.BoardPage_ItemView_Date);
        this._gy_title_label = (TextView) findViewById(R.id.BoardPage_ItemView_GY_Title);
        this._gy_label = (TextView) findViewById(R.id.BoardPage_ItemView_GY);
        this._mark_label = (TextView) findViewById(R.id.BoardPage_ItemView_Mark);
        this._author_label = (TextView) findViewById(R.id.BoardPage_ItemView_Author);
        this._content_view = (ViewGroup) findViewById(R.id.BoardPage_ItemView_ContentView);
        this._divider_bottom = findViewById(R.id.BoardPage_ItemView_DividerBottom);
    }

    public void setDividerBottomVisible(boolean visible) {
        if (this._divider_bottom == null) {
            return;
        }
        if (visible) {
            if (this._divider_bottom.getVisibility() != 0) {
                this._divider_bottom.setVisibility(0);
            }
        } else if (this._divider_bottom.getVisibility() != 8) {
            this._divider_bottom.setVisibility(8);
        }
    }

    public void setTitle(String title) {
        if (this._title_label != null) {
            if (title != null) {
                this._title_label.setText(title);
            } else {
                this._title_label.setText("讀取中...");
            }
        }
    }

    public void setAuthor(String author) {
        if (this._author_label != null) {
            if (author != null) {
                this._author_label.setText(author);
            } else {
                this._author_label.setText("讀取中");
            }
        }
    }

    public void setDate(String date) {
        if (this._date_label != null) {
            if (date != null) {
                this._date_label.setText(date);
            } else {
                this._date_label.setText("讀取中");
            }
        }
    }

    @SuppressLint({"DefaultLocale"})
    public void setNumber(int number) {
        if (this._number_label != null) {
            if (number > 0) {
                this._number_label.setText(String.format("%1$05d", Integer.valueOf(number)));
                return;
            }
            this._number_label.setText("讀取中");
        }
    }

    public void setGYNumber(int number) {
        if (this._gy_label == null) {
            return;
        }
        if (number == 0) {
            this._gy_title_label.setVisibility(8);
            this._gy_label.setVisibility(8);
            return;
        }
        this._gy_title_label.setVisibility(0);
        this._gy_label.setVisibility(0);
        this._gy_label.setText(String.valueOf(number));
    }

    public void setReply(boolean isReply) {
        if (this._status_label == null) {
            return;
        }
        if (isReply) {
            this._status_label.setText("Re");
        } else {
            this._status_label.setText("◆");
        }
    }

    public void setMark(boolean isMarked) {
        if (isMarked) {
            this._mark_label.setVisibility(0);
        } else {
            this._mark_label.setVisibility(4);
        }
    }

    public void setRead(boolean isRead) {
        if (isRead) {
            this._title_label.setTextColor(-8355712);
        } else {
            this._title_label.setTextColor(-1);
        }
    }

    public void clear() {
        setTitle((String) null);
        setDate((String) null);
        setAuthor((String) null);
        setNumber(0);
        setGYNumber(0);
        setRead(true);
        setReply(false);
        setMark(false);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (this._content_view.getVisibility() != 0) {
                this._content_view.setVisibility(0);
            }
        } else if (this._content_view.getVisibility() != 8) {
            this._content_view.setVisibility(8);
        }
    }
}
