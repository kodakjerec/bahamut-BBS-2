package com.kota.ASFramework.Dialog;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ASAlertDialog extends ASDialog implements View.OnClickListener {
    private static final Map<String, ASAlertDialog> _alerts = new HashMap();
    private String _alert_id = null;
    private final Vector<Button> _item_list = new Vector<>();
    private ASAlertDialogListener _listener = null;
    private TextView _message_label = null;
    private TextView _title_label = null;
    private LinearLayout _toolbar = null;

    public static ASAlertDialog create(String alertID) {
        ASAlertDialog dialog = _alerts.get(alertID);
        if (dialog == null) {
            return new ASAlertDialog(alertID);
        }
        dialog.clear();
        return dialog;
    }

    public static ASAlertDialog createDialog() {
        return new ASAlertDialog();
    }

    public static boolean containsAlert(String alertID) {
        if (alertID != null) {
            return _alerts.containsKey(alertID);
        }
        return false;
    }

    public static void hideAlert(String alertID) {
        ASAlertDialog exists_alert = _alerts.get(alertID);
        if (exists_alert != null) {
            exists_alert.dismiss();
        }
    }

    public ASAlertDialog(String alertID) {
        initial();
        this._alert_id = alertID;
    }

    public ASAlertDialog() {
        initial();
    }

    private void initial() {
        requestWindowFeature(1);
        setContentView(buildContentView());
        getWindow().setBackgroundDrawable((Drawable) null);
    }

    private View buildContentView() {
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ASLayoutParams.getInstance().getDialogWidthNormal(), getContext().getResources().getDisplayMetrics());
        int message_minimum_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0f, getContext().getResources().getDisplayMetrics());
        int header_padding = (int) Math.ceil((double) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6.0f, getContext().getResources().getDisplayMetrics()));
        int padding_1 = (int) Math.ceil((double) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.0f, getContext().getResources().getDisplayMetrics()));
        int padding_2 = (int) Math.ceil((double) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getContext().getResources().getDisplayMetrics()));
        LinearLayout frame_view = new LinearLayout(getContext());
        frame_view.setOrientation(LinearLayout.VERTICAL);
        frame_view.setPadding(padding_1, padding_1, padding_1, padding_1);
        frame_view.setBackgroundColor(-1);
        LinearLayout content_view = new LinearLayout(getContext());
        content_view.setOrientation(LinearLayout.VERTICAL);
        content_view.setPadding(padding_2, padding_2, padding_2, padding_2);
        content_view.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        frame_view.addView(content_view);
        this._title_label = new TextView(getContext());
        this._title_label.setLayoutParams(new LinearLayout.LayoutParams(width, -2));
        this._title_label.setPadding(header_padding, header_padding, header_padding, header_padding);
        this._title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge());
        this._title_label.setTextColor(-1);
        this._title_label.setTypeface(this._title_label.getTypeface(), Typeface.BOLD);
        this._title_label.setVisibility(View.GONE);
        this._title_label.setBackgroundColor(-15724528);
        this._title_label.setSingleLine(true);
        content_view.addView(this._title_label);
        this._message_label = new TextView(getContext());
        this._message_label.setLayoutParams(new LinearLayout.LayoutParams(width, -2));
        this._message_label.setPadding(header_padding, header_padding, header_padding, header_padding);
        this._message_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        this._message_label.setMinimumHeight(message_minimum_height);
        this._message_label.setTextColor(-1);
        this._message_label.setVisibility(View.GONE);
        this._message_label.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        content_view.addView(this._message_label);
        this._toolbar = new LinearLayout(getContext());
        this._toolbar.setLayoutParams(new LinearLayout.LayoutParams(width, -2));
        this._toolbar.setGravity(17);
        this._toolbar.setOrientation(LinearLayout.HORIZONTAL);
        content_view.addView(this._toolbar);
        return frame_view;
    }

    public View createDivider() {
        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams((int) Math.ceil((double) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getContext().getResources().getDisplayMetrics())), -1));
        divider.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        return divider;
    }

    private Button createButton() {
        Button button = new Button(getContext());
        button.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        int padding_v = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.0f, getContext().getResources().getDisplayMetrics());
        int padding_h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f, getContext().getResources().getDisplayMetrics());
        button.setPadding(padding_h, padding_v, padding_h, padding_v);
        button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        button.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ASLayoutParams.getInstance().getDefaultTouchBlockHeight(), getContext().getResources().getDisplayMetrics()));
        button.setGravity(17);
        button.setOnClickListener(this);
        button.setBackgroundDrawable(ASLayoutParams.getInstance().getAlertItemBackgroundDrawable());
        button.setSingleLine(false);
        button.setTextColor(ASLayoutParams.getInstance().getAlertItemTextColor());
        return button;
    }

    public void onClick(View aView) {
        if (this._listener != null) {
            this._listener.onAlertDialogDismissWithButtonIndex(this, this._item_list.indexOf(aView));
        }
        dismiss();
    }

    public ASAlertDialog setListener(ASAlertDialogListener aListener) {
        this._listener = aListener;
        return this;
    }

    public ASAlertDialog setMessage(String aMessage) {
        if (aMessage == null) {
            this._message_label.setVisibility(View.GONE);
        } else {
            this._message_label.setVisibility(View.VISIBLE);
            this._message_label.setText(aMessage);
        }
        return this;
    }

    public ASAlertDialog setTitle(String aTitle) {
        if (aTitle == null) {
            this._title_label.setVisibility(View.GONE);
        } else {
            this._title_label.setVisibility(View.VISIBLE);
            this._title_label.setText(aTitle);
        }
        return this;
    }

    public ASAlertDialog addButton(String aTitle) {
        if (aTitle != null) {
            if (this._item_list.size() > 0) {
                this._toolbar.addView(createDivider());
            }
            Button button = createButton();
            this._toolbar.addView(button);
            button.setText(aTitle);
            if (aTitle != null) {
                if (aTitle.length() < 4) {
                    button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
                } else {
                    button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
                }
            }
            button.setOnClickListener(this);
            this._item_list.add(button);
        }
        return this;
    }

    public ASAlertDialog setItemTitle(int index, String aTitle) {
        if (index >= 0 && index < this._item_list.size()) {
            this._item_list.get(index).setText(aTitle);
        }
        return this;
    }

    public void dismiss() {
        if (this._alert_id != null) {
            _alerts.remove(this._alert_id);
        }
        super.dismiss();
    }

    public void show() {
        if (this._alert_id != null) {
            ASAlertDialog exists_alert = _alerts.get(this._alert_id);
            if (exists_alert != null && exists_alert.isShowing()) {
                exists_alert.dismiss();
            }
            _alerts.put(this._alert_id, this);
        }
        super.show();
    }

    private void clear() {
        if (this._message_label != null) {
            this._message_label.setText("");
        }
        if (this._title_label != null) {
            this._title_label.setText("");
        }
        this._toolbar.removeAllViews();
        this._item_list.clear();
    }
}
