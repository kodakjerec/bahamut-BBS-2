package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.Pages.Model.ClassPageItem;
import com.kota.Bahamut.R;

import java.util.Objects;

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
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.class_page_item_view, this);
        this._board_title_label = findViewById(R.id.ClassPage_ItemView_classTitle);
        this._board_name_label = findViewById(R.id.ClassPage_ItemView_className);
        this._board_manager_label = findViewById(R.id.ClassPage_ItemView_classManager);
        this._divider_bottom = findViewById(R.id.ClassPage_ItemView_DividerBottom);
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

    public void setBoardTitleText(String title) {
        if (this._board_title_label == null) {
            return;
        }
        this._board_title_label.setText(Objects.requireNonNullElse(title, getContextString(R.string.loading_)));
    }

    public void setBoardNameText(String boardName) {
        if (this._board_name_label == null) {
            return;
        }
        this._board_name_label.setText(Objects.requireNonNullElse(boardName, getContextString(R.string.loading)));
    }

    public void setBoardManagerText(String boardManager) {
        if (this._board_manager_label == null) {
            return;
        }
        this._board_manager_label.setText(Objects.requireNonNullElse(boardManager, getContextString(R.string.loading)));
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
        setBoardTitleText(null);
        setBoardNameText(null);
        setBoardManagerText(null);
    }
}
