// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASDialog, ASLayoutParams, ASAlertDialogListener

public class ASAlertDialog extends ASDialog
    implements View.OnClickListener
{

    private static Map _alerts = new HashMap();
    private String _alert_id;
    private Vector _item_list;
    private ASAlertDialogListener _listener;
    private TextView _message_label;
    private TextView _title_label;
    private LinearLayout _toolbar;

    public ASAlertDialog()
    {
        _message_label = null;
        _title_label = null;
        _listener = null;
        _alert_id = null;
        _toolbar = null;
        _item_list = new Vector();
        initial();
    }

    public ASAlertDialog(String s)
    {
        _message_label = null;
        _title_label = null;
        _listener = null;
        _alert_id = null;
        _toolbar = null;
        _item_list = new Vector();
        initial();
        _alert_id = s;
    }

    private View buildContentView()
    {
        int i = (int)TypedValue.applyDimension(1, ASLayoutParams.getInstance().getDialogWidthNormal(), getContext().getResources().getDisplayMetrics());
        int j = (int)TypedValue.applyDimension(1, 100F, getContext().getResources().getDisplayMetrics());
        int k = (int)Math.ceil(TypedValue.applyDimension(1, 6F, getContext().getResources().getDisplayMetrics()));
        int l = (int)Math.ceil(TypedValue.applyDimension(1, 3F, getContext().getResources().getDisplayMetrics()));
        int i1 = (int)Math.ceil(TypedValue.applyDimension(1, 1.0F, getContext().getResources().getDisplayMetrics()));
        LinearLayout linearlayout = new LinearLayout(getContext());
        linearlayout.setOrientation(1);
        linearlayout.setPadding(l, l, l, l);
        linearlayout.setBackgroundColor(-1);
        LinearLayout linearlayout1 = new LinearLayout(getContext());
        linearlayout1.setOrientation(1);
        linearlayout1.setPadding(i1, i1, i1, i1);
        linearlayout1.setBackgroundColor(0xff000000);
        linearlayout.addView(linearlayout1);
        _title_label = new TextView(getContext());
        _title_label.setLayoutParams(new LinearLayout.LayoutParams(i, -2));
        _title_label.setPadding(k, k, k, k);
        _title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge());
        _title_label.setTextColor(-1);
        _title_label.setTypeface(_title_label.getTypeface(), 1);
        _title_label.setVisibility(8);
        _title_label.setBackgroundColor(0xff101010);
        _title_label.setSingleLine(true);
        linearlayout1.addView(_title_label);
        _message_label = new TextView(getContext());
        _message_label.setLayoutParams(new LinearLayout.LayoutParams(i, -2));
        _message_label.setPadding(k, k, k, k);
        _message_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        _message_label.setMinimumHeight(j);
        _message_label.setTextColor(-1);
        _message_label.setVisibility(8);
        _message_label.setBackgroundColor(0xff000000);
        linearlayout1.addView(_message_label);
        _toolbar = new LinearLayout(getContext());
        _toolbar.setLayoutParams(new LinearLayout.LayoutParams(i, -2));
        _toolbar.setGravity(17);
        _toolbar.setOrientation(0);
        linearlayout1.addView(_toolbar);
        return linearlayout;
    }

    private void clear()
    {
        if (_message_label != null)
        {
            _message_label.setText("");
        }
        if (_title_label != null)
        {
            _title_label.setText("");
        }
        _toolbar.removeAllViews();
        _item_list.clear();
    }

    public static boolean containsAlert(String s)
    {
        boolean flag = false;
        if (s != null)
        {
            flag = _alerts.containsKey(s);
        }
        return flag;
    }

    public static ASAlertDialog create(String s)
    {
        ASAlertDialog asalertdialog = (ASAlertDialog)_alerts.get(s);
        if (asalertdialog == null)
        {
            return new ASAlertDialog(s);
        } else
        {
            asalertdialog.clear();
            return asalertdialog;
        }
    }

    private Button createButton()
    {
        Button button = new Button(getContext());
        button.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0F));
        int i = (int)TypedValue.applyDimension(1, 3F, getContext().getResources().getDisplayMetrics());
        int j = (int)TypedValue.applyDimension(1, 5F, getContext().getResources().getDisplayMetrics());
        button.setPadding(j, i, j, i);
        button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        button.setMinimumHeight((int)TypedValue.applyDimension(1, ASLayoutParams.getInstance().getDefaultTouchBlockHeight(), getContext().getResources().getDisplayMetrics()));
        button.setGravity(17);
        button.setOnClickListener(this);
        button.setBackgroundDrawable(ASLayoutParams.getInstance().getAlertItemBackgroundDrawable());
        button.setSingleLine(false);
        button.setTextColor(ASLayoutParams.getInstance().getAlertItemTextColor());
        return button;
    }

    public static ASAlertDialog createDialog()
    {
        return new ASAlertDialog();
    }

    public static void hideAlert(String s)
    {
        s = (ASAlertDialog)_alerts.get(s);
        if (s != null)
        {
            s.dismiss();
        }
    }

    private void initial()
    {
        requestWindowFeature(1);
        setContentView(buildContentView());
        getWindow().setBackgroundDrawable(null);
    }

    public ASAlertDialog addButton(String s)
    {
        if (s == null)
        {
            return this;
        }
        if (_item_list.size() > 0)
        {
            _toolbar.addView(createDivider());
        }
        Button button = createButton();
        _toolbar.addView(button);
        button.setText(s);
        if (s != null)
        {
            if (s.length() < 4)
            {
                button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
            } else
            {
                button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
            }
        }
        button.setOnClickListener(this);
        _item_list.add(button);
        return this;
    }

    public View createDivider()
    {
        View view = new View(getContext());
        view.setLayoutParams(new LinearLayout.LayoutParams((int)Math.ceil(TypedValue.applyDimension(1, 1.0F, getContext().getResources().getDisplayMetrics())), -1));
        view.setBackgroundColor(0xff000000);
        return view;
    }

    public void dismiss()
    {
        if (_alert_id != null)
        {
            _alerts.remove(_alert_id);
        }
        super.dismiss();
    }

    public void onClick(View view)
    {
        if (_listener != null)
        {
            int i = _item_list.indexOf(view);
            _listener.onAlertDialogDismissWithButtonIndex(this, i);
        }
        dismiss();
    }

    public ASAlertDialog setItemTitle(int i, String s)
    {
        if (i >= 0 && i < _item_list.size())
        {
            ((Button)_item_list.get(i)).setText(s);
        }
        return this;
    }

    public ASAlertDialog setListener(ASAlertDialogListener asalertdialoglistener)
    {
        _listener = asalertdialoglistener;
        return this;
    }

    public ASAlertDialog setMessage(String s)
    {
        if (s == null)
        {
            _message_label.setVisibility(8);
            return this;
        } else
        {
            _message_label.setVisibility(0);
            _message_label.setText(s);
            return this;
        }
    }

    public ASAlertDialog setTitle(String s)
    {
        if (s == null)
        {
            _title_label.setVisibility(8);
            return this;
        } else
        {
            _title_label.setVisibility(0);
            _title_label.setText(s);
            return this;
        }
    }

    public void show()
    {
        if (_alert_id != null)
        {
            ASAlertDialog asalertdialog = (ASAlertDialog)_alerts.get(_alert_id);
            if (asalertdialog != null && asalertdialog.isShowing())
            {
                asalertdialog.dismiss();
            }
            _alerts.put(_alert_id, this);
        }
        super.show();
    }

}
