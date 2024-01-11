package com.kota.TelnetUI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kota.bahamut_bbs_2.R;

public class TelnetHeaderItemView extends LinearLayout {
    private TextView _detail_1 = null;
    private TextView _detail_2 = null;
    private TextView _title = null;
    private ImageButton mMenuButton;
    private View mMenuDivider;

    public TelnetHeaderItemView(Context context) {
        super(context);
        init();
    }

    public TelnetHeaderItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.header_item_view, this);
        this._title = (TextView) findViewById(R.id.title);
        this._detail_1 = (TextView) findViewById(R.id.detail_1);
        this._detail_2 = (TextView) findViewById(R.id.detail_2);
        this.mMenuDivider = findViewById(R.id.menu_divider);
        this.mMenuButton = (ImageButton) findViewById(R.id.menu_button);
    }

    public void setMenuButton(View.OnClickListener aListener) {
        if (aListener == null) {
            this.mMenuDivider.setVisibility(View.GONE);
            this.mMenuButton.setVisibility(View.GONE);
            this.mMenuButton.setOnClickListener((View.OnClickListener) null);
            return;
        }
        this.mMenuDivider.setVisibility(View.VISIBLE);
        this.mMenuButton.setVisibility(View.VISIBLE);
        this.mMenuButton.setOnClickListener(aListener);
    }

    public void setData(String aTitle, String aDetail1, String aDetail2) {
        setTitle(aTitle);
        setDetail1(aDetail1);
        setDetail2(aDetail2);
    }

    public void setTitle(String aTitle) {
        if (this._title != null) {
            this._title.setText(aTitle);
        }
    }

    public void setDetail1(String aDetail1) {
        if (this._detail_1 != null) {
            this._detail_1.setText(aDetail1);
        }
    }

    public void setDetail2(String aDetail2) {
        if (this._detail_2 != null) {
            this._detail_2.setText(aDetail2);
        }
    }
}
