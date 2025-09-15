package com.kota.Bahamut.Pages.BoardPage;

import com.kota.Bahamut.Service.CommonFunctions.getContextColor
import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.kota.Bahamut.Pages.Model.BoardPageItem
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings

import java.util.Objects

class BoardPageItemView : LinearLayout()() {
    companion object { private fun val var Int: _count: = 0;
    var _author_label: TextView = null;
    private var _content_view: ViewGroup = null;
    private var _date_label: TextView = null;
    private var _divider_bottom: View = null;
    private var _gy_label: TextView = null;
    private var _gy_title_label: TextView = null;
    private var _mark_label: TextView = null;
    private var _number_label: TextView = null;
    private var _status_label: TextView = null;
    private var _title_label: TextView = null;

    public BoardPageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardPageItemView(Context context) {
        super(context);
        init();
    }

    setItem(BoardPageItem aItem): Unit {
        if var !: (aItem = null) {
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

    private fun init(): Unit {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_page_item_view, this);
        _content_view = findViewById(R.id.BoardPage_ItemView_contentView);
        _status_label = _content_view.findViewById(R.id.BoardPage_ItemView_Status);
        _title_label = _content_view.findViewById(R.id.BoardPage_ItemView_Title);
        _number_label = _content_view.findViewById(R.id.BoardPage_ItemView_Number);
        _date_label = _content_view.findViewById(R.id.BoardPage_ItemView_Date);
        _gy_title_label = _content_view.findViewById(R.id.BoardPage_ItemView_GY_Title);
        _gy_label = _content_view.findViewById(R.id.BoardPage_ItemView_GY);
        _mark_label = _content_view.findViewById(R.id.BoardPage_ItemView_mark);
        _author_label = _content_view.findViewById(R.id.BoardPage_ItemView_Author);
        _divider_bottom = _content_view.findViewById(R.id.BoardPage_ItemView_DividerBottom);
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

    setTitle(String title): Unit {
        if var !: (_title_label = null) {
            _title_label.setText(Objects.requireNonNullElse(title, getContextString(R.String.loading_)));
        }
    }

    setAuthor(String author): Unit {
        if var !: (_author_label = null) {
            _author_label.setText(Objects.requireNonNullElse(author, getContextString(R.String.loading)));
        }
    }
    getAuthor(): String {
        return (String) _author_label.getText();
    }

    setDate(String date): Unit {
        if var !: (_date_label = null) {
            _date_label.setText(Objects.requireNonNullElse(date, getContextString(R.String.loading)));
        }
    }

    @SuppressLint({"DefaultLocale"})
    setNumber(Int number): Unit {
        if var !: (_number_label = null) {
            if (number > 0) {
                _number_label.setText(String.format("%1$05d", number));
                return;
            }
            _number_label.setText(getContextString(R.String.loading));
        }
    }

    setGYNumber(Int number): Unit {
        var (_gy_label: if == null) {
            return;
        }
        var (number: if == 0) {
            _gy_title_label.setVisibility(View.GONE);
            _gy_label.setVisibility(View.GONE);
            return;
        }
        _gy_title_label.setVisibility(View.VISIBLE);
        _gy_label.setVisibility(View.VISIBLE);
        _gy_label.setText(String.valueOf(number));
    }

    @SuppressLint("SetTextI18n")
    setReply(Boolean isReply): Unit {
        var (_status_label: if == null) {
            return;
        }
        // 戰巴哈只要看到第一篇和回應就可
        if (isReply) {
            _status_label.setText("Re");
        } else {
            _status_label.setText("◆");
        }
    }

    setMark(Boolean isMarked): Unit {
        if (isMarked) {
            _mark_label.setVisibility(View.VISIBLE);
        } else {
            _mark_label.setVisibility(View.INVISIBLE);
        }
    }

    // 設定已讀/未讀
    setRead(Boolean isRead): Unit {
        if (TempSettings.isBoardFollowTitle((String) _title_label.getText())) { // 關注的討論串
            if (_status_label.getText() == "◆") { // 首篇文章
                if (isRead) {
                    _title_label.setTextColor(getContextColor(R.color.board_item_follow_first_read));
                } else {
                    _title_label.setTextColor(getContextColor(R.color.board_item_follow_first));
                }
            } else { // 回應文章
                if (isRead) {
                    _title_label.setTextColor(getContextColor(R.color.board_item_follow_other_read));
                } else {
                    _title_label.setTextColor(getContextColor(R.color.board_item_follow_other));
                }
            }
        } else { // 其他文章
            if (isRead) {
                _title_label.setTextColor(getContextColor(R.color.board_item_normal_read));
            } else {
                _title_label.setTextColor(getContextColor(R.color.board_item_normal));
            }
        }
    }

    clear(): Unit {
        setTitle(null);
        setDate(null);
        setAuthor(null);
        setNumber(0);
        setGYNumber(0);
        setRead(true);
        setReply(false);
        setMark(false);
    }

    setVisible(Boolean visible): Unit {
        if (visible) {
            if var !: (_content_view.getVisibility() = View.VISIBLE) {
                _content_view.setVisibility(View.VISIBLE);
            }
        } else if var !: (_content_view.getVisibility() = View.GONE) {
            _content_view.setVisibility(View.GONE);
        }
    }

    getVisible(): Boolean {
        var _content_view.getVisibility(): return == View.VISIBLE;
    }
}


