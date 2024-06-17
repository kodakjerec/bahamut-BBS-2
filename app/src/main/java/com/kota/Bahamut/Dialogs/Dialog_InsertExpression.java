package com.kota.Bahamut.Dialogs;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.ASFramework.Dialog.ASLayoutParams;
import com.kota.Bahamut.R;

import java.util.Objects;
import java.util.Vector;

public class Dialog_InsertExpression extends ASDialog {
    ScrollView _scroll_view;
    TextView _title_label;
    LinearLayout _item_block;
    Dialog_InsertExpression_Listener _listener;
    final Vector<DialogItem> _item_list = new Vector<>();

    private static class DialogItem {
        public Button button = null;
        public String title = null;

        DialogItem() {
        }
    }

    View.OnClickListener _setting_listener = view -> {
        this._listener.onListDialogSettingClicked();
        dismiss();
    };

    public Dialog_InsertExpression() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_insert_expressions);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(null);

        _scroll_view = findViewById(R.id.dialog_insert_expressions_scrollView);
        _title_label = findViewById(R.id.dialog_insert_expressions_title);
        _item_block = findViewById(R.id.dialog_insert_expressions_content);

        findViewById(R.id.dialog_insert_expressions_setting).setOnClickListener(_setting_listener);
        setDialogWidth();
    }

    public static Dialog_InsertExpression createDialog() {
        return new Dialog_InsertExpression();
    }

    public Dialog_InsertExpression setListener(Dialog_InsertExpression_Listener aListener) {
        _listener = aListener;
        return this;
    }

    public Dialog_InsertExpression setTitle(String aTitle) {
        if (this._title_label != null) {
            this._title_label.setText(aTitle);
        }
        return this;
    }

    public Dialog_InsertExpression addItems(String[] aItemList) {
        for (String item_title : aItemList) {
            addItem(item_title);
        }
        return this;
    }

    public Dialog_InsertExpression addItem(String aItemTitle) {
        Button button = createButton();

        button.setOnClickListener(v -> Dialog_InsertExpression.this.onItemClicked((Button) v));

        if (aItemTitle == null) {
            button.setVisibility(View.GONE);
        } else {
            if (this._item_list.size() > 0) {
                this._item_block.addView(createDivider());
            }
            button.setText(aItemTitle);
        }
        this._item_block.addView(button);
        DialogItem item = new DialogItem();
        item.button = button;
        item.title = aItemTitle;
        this._item_list.add(item);
        return this;
    }

    public View createDivider() {
        View divider = new View(getContext());
        int divider_height = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getContext().getResources().getDisplayMetrics()));
        divider.setLayoutParams(new LinearLayout.LayoutParams(-1, divider_height));
        divider.setBackgroundColor(-2130706433);
        return divider;
    }

    private Button createButton() {
        Button button = new Button(getContext());
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60.0f, getContext().getResources().getDisplayMetrics()));
        button.setGravity(Gravity.CENTER);
        button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
        button.setBackground(ASLayoutParams.getInstance().getListItemBackgroundDrawable());
        button.setTextColor(ASLayoutParams.getInstance().getListItemTextColor());
        button.setSingleLine(true);
        return button;
    }

    private int indexOfButton(Button aButton) {
        for (int i = 0; i < this._item_list.size(); i++) {
            DialogItem item = this._item_list.get(i);
            if (item.button == aButton) {
                return i;
            }
        }
        return -1;
    }

    private void onItemClicked(Button button) {
        if (this._listener != null) {
            int index = indexOfButton(button);
            if (index != -1) {
                this._listener.onListDialogItemClicked(this, index, this._item_list.get(index).title);
            }
            dismiss();
        }
    }

    // 變更dialog寬度
    void setDialogWidth() {
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int dialog_height = (int) (screenHeight*0.7);
        int dialog_width = (int) (screenWidth*0.7);
        ViewGroup.LayoutParams oldLayoutParams = _scroll_view.getLayoutParams();
        oldLayoutParams.width = dialog_width;
        oldLayoutParams.height = dialog_height;
        _scroll_view.setLayoutParams(oldLayoutParams);
    }
}
