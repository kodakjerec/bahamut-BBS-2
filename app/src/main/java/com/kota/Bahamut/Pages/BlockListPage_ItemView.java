package com.kota.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.R;

public class BlockListPage_ItemView extends LinearLayout implements View.OnClickListener {
    private View _divider_bottom = null;
    private View _divider_top = null;
    private TextView _name_label = null;
    public int index = 0;
    public BlockListPage_ItemView_Listener listener = null;

    public BlockListPage_ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlockListPage_ItemView(Context context) {
        super(context);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.block_list_item_view, this);
        this._name_label = findViewById(R.id.BlockListItemView_Name);
        Button _delete_button = findViewById(R.id.BlockListItemView_Delete);
        _delete_button.setOnClickListener(this);
        this._divider_top = findViewById(R.id.BlockListPage_ItemView_DividerTop);
        this._divider_bottom = findViewById(R.id.BlockListPage_ItemView_DividerBottom);
    }

    public void setName(String aName) {
        if (this._name_label != null) {
            this._name_label.setText(aName);
        }
    }

    public void onClick(View v) {
        if (this.listener != null) {
            this.listener.onBlockListPage_ItemView_clicked(this);
        }
    }

    public void setDividerTopVisible(boolean visible) {
        if (this._divider_top == null) {
            return;
        }
        if (visible) {
            if (this._divider_top.getVisibility() != View.VISIBLE) {
                this._divider_top.setVisibility(View.VISIBLE);
            }
        } else if (this._divider_top.getVisibility() != View.GONE) {
            this._divider_top.setVisibility(View.GONE);
        }
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
