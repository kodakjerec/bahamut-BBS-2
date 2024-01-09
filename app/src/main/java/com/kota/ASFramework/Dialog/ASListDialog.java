package com.kota.ASFramework.Dialog;

import android.graphics.drawable.Drawable;
import androidx.core.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Vector;

public class ASListDialog extends ASDialog {
    public static final int SIZE_LARGE = 1;
    public static final int SIZE_NORMAL = 0;
    private LinearLayout _content_view = null;
    private float _dialog_width = 280.0f;
    private LinearLayout _item_block = null;
    private Vector<ASListDialogItem> _item_list = new Vector<>();
    private int _item_text_size = 1;
    private ASListDialogItemClickListener _listener = null;
    private ScrollView _scroll_view = null;
    private TextView _title_label = null;

    class ASListDialogItem {
        public Button button = null;
        public String title = null;

        ASListDialogItem() {
        }
    }

    public ASListDialog setItemTextSize(int size) {
        this._item_text_size = size;
        return this;
    }

    public String getName() {
        return "ListDialog";
    }

    public ASListDialog() {
        requestWindowFeature(1);
        setContentView(buildContentView());
        getWindow().setBackgroundDrawable((Drawable) null);
    }

    private View buildContentView() {
        int frame_padding = (int) TypedValue.applyDimension(1, 3.0f, getContext().getResources().getDisplayMetrics());
        int padding = (int) TypedValue.applyDimension(1, 5.0f, getContext().getResources().getDisplayMetrics());
        LinearLayout frame = new LinearLayout(getContext());
        frame.setBackgroundColor(-1);
        frame.setPadding(frame_padding, frame_padding, frame_padding, frame_padding);
        LinearLayout content_view = new LinearLayout(getContext());
        content_view.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        content_view.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        frame.addView(content_view);
        content_view.setOrientation(1);
        int dialog_width = (((int) TypedValue.applyDimension(1, this._dialog_width, getContext().getResources().getDisplayMetrics())) / 2) * 2;
        this._scroll_view = new ScrollView(getContext());
        this._scroll_view.setLayoutParams(new LinearLayout.LayoutParams(dialog_width, -2));
        content_view.addView(this._scroll_view);
        this._content_view = new LinearLayout(getContext());
        this._content_view.setLayoutParams(new FrameLayout.LayoutParams(dialog_width, -2));
        this._content_view.setOrientation(1);
        this._content_view.setGravity(17);
        this._scroll_view.addView(this._content_view);
        this._title_label = new TextView(getContext());
        this._title_label.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        this._title_label.setPadding(padding, padding, padding, padding);
        this._title_label.setTextColor(-1);
        this._title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        this._title_label.setText("選項");
        this._title_label.setBackgroundColor(-14671840);
        this._title_label.setGravity(17);
        this._content_view.addView(this._title_label);
        this._item_block = new LinearLayout(getContext());
        this._item_block.setLayoutParams(new FrameLayout.LayoutParams(dialog_width, -2));
        this._item_block.setOrientation(1);
        this._item_block.setGravity(17);
        this._content_view.addView(this._item_block);
        return frame;
    }

    public static ASListDialog createDialog() {
        return new ASListDialog();
    }

    public ASListDialog setListener(ASListDialogItemClickListener aListener) {
        this._listener = aListener;
        return this;
    }

    public ASListDialog setTitle(String aTitle) {
        if (this._title_label != null) {
            this._title_label.setText(aTitle);
        }
        return this;
    }

    public ASListDialog addItems(String[] aItemList) {
        for (String item_title : aItemList) {
            addItem(item_title);
        }
        return this;
    }

    public ASListDialog addItem(String aItemTitle) {
        Button button = createButton();
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ASListDialog.this.onItemClicked((Button) v);
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                return ASListDialog.this.onItemLongClicked((Button) v);
            }
        });
        if (aItemTitle == null) {
            button.setVisibility(8);
        } else {
            if (this._item_list.size() > 0) {
                this._item_block.addView(createDivider());
            }
            button.setText(aItemTitle);
        }
        this._item_block.addView(button);
        ASListDialogItem item = new ASListDialogItem();
        item.button = button;
        item.title = aItemTitle;
        this._item_list.add(item);
        return this;
    }

    public View createDivider() {
        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(-1, (int) Math.ceil((double) TypedValue.applyDimension(1, 1.0f, getContext().getResources().getDisplayMetrics()))));
        divider.setBackgroundColor(-2130706433);
        return divider;
    }

    private Button createButton() {
        Button button = new Button(getContext());
        button.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        button.setMinimumHeight((int) TypedValue.applyDimension(1, 60.0f, getContext().getResources().getDisplayMetrics()));
        button.setGravity(17);
        if (this._item_text_size == 0) {
            button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
        } else if (this._item_text_size == 1) {
            button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        } else if (this._item_text_size == 2) {
            button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge());
        }
        button.setBackgroundDrawable(ASLayoutParams.getInstance().getListItemBackgroundDrawable());
        button.setTextColor(ASLayoutParams.getInstance().getListItemTextColor());
        button.setSingleLine(true);
        return button;
    }

    private int indexOfButton(Button aButton) {
        for (int i = 0; i < this._item_list.size(); i++) {
            if (this._item_list.get(i).button == aButton) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void onItemClicked(Button button) {
        if (this._listener != null) {
            int index = indexOfButton(button);
            if (index != -1) {
                this._listener.onListDialogItemClicked(this, index, this._item_list.get(index).title);
            }
            dismiss();
        }
    }

    /* access modifiers changed from: private */
    public boolean onItemLongClicked(Button button) {
        int index;
        boolean result = false;
        if (!(this._listener == null || (index = indexOfButton(button)) == -1)) {
            result = this._listener.onListDialogItemLongClicked(this, index, this._item_list.get(index).title);
        }
        if (result) {
            dismiss();
        }
        return result;
    }

    public ASListDialog setDialogWidth(float width) {
        this._dialog_width = width;
        int dialog_width = (((int) TypedValue.applyDimension(1, this._dialog_width, getContext().getResources().getDisplayMetrics())) / 2) * 2;
        this._scroll_view.setLayoutParams(new LinearLayout.LayoutParams(dialog_width, -2));
        this._content_view.setLayoutParams(new FrameLayout.LayoutParams(dialog_width, -2));
        return this;
    }
}
