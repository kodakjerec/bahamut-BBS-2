package com.kota.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.bahamut_bbs_2.R;;

public class BlockListPage_ItemView extends LinearLayout implements View.OnClickListener {
    private Button _delete_button = null;
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
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.block_list_item_view, this);
        this._name_label = (TextView) findViewById(R.id.BlockListItemView_Name);
        this._delete_button = (Button) findViewById(R.id.BlockListItemView_Delete);
        this._delete_button.setOnClickListener(this);
        this._divider_top = findViewById(R.id.BlockListPage_ItemView_DividerTop);
        this._divider_bottom = findViewById(R.id.BlockListPage_ItemView_DividerBottom);
    }

    public void setName(String aName) {
        if (this._name_label != null && this._name_label != null) {
            this._name_label.setText(aName);
        }
    }

    public void onClick(View v) {
        if (this.listener != null) {
            this.listener.onBlockListPage_ItemView_Clicked(this);
        }
    }

    public void setDividerTopVisible(boolean visible) {
        if (this._divider_top == null) {
            return;
        }
        if (visible) {
            if (this._divider_top.getVisibility() != 0) {
                this._divider_top.setVisibility(0);
            }
        } else if (this._divider_top.getVisibility() != 8) {
            this._divider_top.setVisibility(8);
        }
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
}
