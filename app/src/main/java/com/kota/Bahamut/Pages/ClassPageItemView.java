package com.kota.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.Pages.Model.ClassPageItem;
import com.kota.Bahamut.R;;

public class ClassPageItemView extends LinearLayout {
    private static final int _count = 0;
    private TextView _board_manager_label = null;
    private TextView _board_name_label = null;
    private TextView _board_title_label = null;
    private View _divider_bottom = null;

    public ClassPageItemView(Context context) {
        super(context);
        init();
    }

    public ClassPageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /* access modifiers changed from: protected */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.class_page_item_view, this);
        this._board_title_label = (TextView) findViewById(R.id.ClassPage_ItemView_ClassTitle);
        this._board_name_label = (TextView) findViewById(R.id.ClassPage_ItemView_ClassName);
        this._board_manager_label = (TextView) findViewById(R.id.ClassPage_ItemView_ClassManager);
        this._divider_bottom = findViewById(R.id.ClassPage_ItemView_DividerBottom);
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

    public void setBoardTitleText(String title) {
        if (this._board_title_label == null) {
            return;
        }
        if (title != null) {
            this._board_title_label.setText(title);
        } else {
            this._board_title_label.setText("讀取中...");
        }
    }

    public void setBoardNameText(String boardName) {
        if (this._board_name_label == null) {
            return;
        }
        if (boardName != null) {
            this._board_name_label.setText(boardName);
        } else {
            this._board_name_label.setText("讀取中");
        }
    }

    public void setBoardManagerText(String boardManager) {
        if (this._board_manager_label == null) {
            return;
        }
        if (boardManager != null) {
            this._board_manager_label.setText(boardManager);
        } else {
            this._board_manager_label.setText("讀取中");
        }
    }

    public void setItem(ClassPageItem aItem) {
        if (aItem != null) {
            setBoardTitleText(aItem.Title);
            setBoardNameText(aItem.Name);
            setBoardManagerText(aItem.Manager);
            return;
        }
        clear();
    }

    public void clear() {
        setBoardTitleText((String) null);
        setBoardNameText((String) null);
        setBoardManagerText((String) null);
    }
}
