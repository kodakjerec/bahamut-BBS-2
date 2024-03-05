package com.kota.TelnetUI;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;

import java.util.ArrayList;

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
        this._title = findViewById(R.id.title);
        this._detail_1 = findViewById(R.id.detail_1);
        this._detail_2 = findViewById(R.id.detail_2);
        this.mMenuDivider = findViewById(R.id.menu_divider);
        this.mMenuButton = findViewById(R.id.menu_button);

        // 側邊選單
        int location = UserSettings.getPropertiesDrawerLocation();
        if (location == 1) {
            LinearLayout headerItemView = findViewById(R.id.header_item_view);
            // 備份現在的view
            ArrayList<View> alViews = new ArrayList<>();
            for (int i = headerItemView.getChildCount() - 1; i >= 0; i--) {
                View view = headerItemView.getChildAt(i);
                alViews.add(view);
            }
            // 刪除所有childview
            headerItemView.removeAllViews();
            // 回填
            for (int j = 0; j < alViews.size(); j++) {
                headerItemView.addView(alViews.get(j));
            }
        }
    }

    public void setMenuButton(View.OnClickListener aListener) {
        if (aListener == null) {
            this.mMenuDivider.setVisibility(View.GONE);
            this.mMenuButton.setVisibility(View.GONE);
            this.mMenuButton.setOnClickListener(null);
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
            if (aTitle!= null && aTitle.contains("系統精靈送信來了")) {
                this._title.setTextColor(getContextColor(R.color.white));
                this._title.setBackgroundColor(getContextColor(R.color.red));
                ViewGroup.LayoutParams layoutParams = this._title.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                this._title.setLayoutParams(layoutParams);
            }
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
