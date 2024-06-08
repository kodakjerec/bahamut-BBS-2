package com.kota.Bahamut.Pages.BoardPage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;
import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;

import java.util.Objects;

public class BoardPageItemView extends LinearLayout {
    private static final int _count = 0;
    public TextView _author_label = null;
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

    public void setTitle(String title) {
        if (_title_label != null) {
            _title_label.setText(Objects.requireNonNullElse(title, getContextString(R.string.loading_)));
        }
    }

    public void setAuthor(String author) {
        if (_author_label != null) {
            _author_label.setText(Objects.requireNonNullElse(author, getContextString(R.string.loading)));
        }
    }
    public String getAuthor() {
        return (String) _author_label.getText();
    }

    public void setDate(String date) {
        if (_date_label != null) {
            _date_label.setText(Objects.requireNonNullElse(date, getContextString(R.string.loading)));
        }
    }

    @SuppressLint({"DefaultLocale"})
    public void setNumber(int number) {
        if (_number_label != null) {
            if (number > 0) {
                _number_label.setText(String.format("%1$05d", number));
                return;
            }
            _number_label.setText(getContextString(R.string.loading));
        }
    }

    public void setGYNumber(int number) {
        if (_gy_label == null) {
            return;
        }
        if (number == 0) {
            _gy_title_label.setVisibility(View.GONE);
            _gy_label.setVisibility(View.GONE);
            return;
        }
        _gy_title_label.setVisibility(View.VISIBLE);
        _gy_label.setVisibility(View.VISIBLE);
        _gy_label.setText(String.valueOf(number));
    }

    @SuppressLint("SetTextI18n")
    public void setReply(boolean isReply) {
        if (_status_label == null) {
            return;
        }
        // 戰巴哈只要看到第一篇和回應就可
        if (isReply) {
            _status_label.setText("Re");
        } else {
            _status_label.setText("◆");
        }
    }

    public void setMark(boolean isMarked) {
        if (isMarked) {
            _mark_label.setVisibility(View.VISIBLE);
        } else {
            _mark_label.setVisibility(View.INVISIBLE);
        }
    }

    // 設定已讀/未讀
    public void setRead(boolean isRead) {
        if (TempSettings.isBoardFollowTitle((String) _title_label.getText())) { // 關注的討論串
            if (_status_label.getText().equals("◆")) { // 首篇文章
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

    public void clear() {
        setTitle(null);
        setDate(null);
        setAuthor(null);
        setNumber(0);
        setGYNumber(0);
        setRead(true);
        setReply(false);
        setMark(false);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (_content_view.getVisibility() != View.VISIBLE) {
                _content_view.setVisibility(View.VISIBLE);
            }
        } else if (_content_view.getVisibility() != View.GONE) {
            _content_view.setVisibility(View.GONE);
        }
    }
}
