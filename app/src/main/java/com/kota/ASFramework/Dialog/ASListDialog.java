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
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Vector;

// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASDialog, ASLayoutParams, ASListDialogItemClickListener

public class ASListDialog extends ASDialog
{
    class ASListDialogItem
    {

        public Button button;
        final ASListDialog this$0;
        public String title;

        ASListDialogItem()
        {
            this$0 = ASListDialog.this;
            super();
            button = null;
            title = null;
        }
    }


    public static final int SIZE_LARGE = 1;
    public static final int SIZE_NORMAL = 0;
    private LinearLayout _content_view;
    private float _dialog_width;
    private LinearLayout _item_block;
    private Vector _item_list;
    private int _item_text_size;
    private ASListDialogItemClickListener _listener;
    private ScrollView _scroll_view;
    private TextView _title_label;

    public ASListDialog()
    {
        _content_view = null;
        _item_block = null;
        _listener = null;
        _item_list = new Vector();
        _title_label = null;
        _dialog_width = 280F;
        _scroll_view = null;
        _item_text_size = 1;
        requestWindowFeature(1);
        setContentView(buildContentView());
        getWindow().setBackgroundDrawable(null);
    }

    private View buildContentView()
    {
        int j = (int)TypedValue.applyDimension(1, 3F, getContext().getResources().getDisplayMetrics());
        int i = (int)TypedValue.applyDimension(1, 5F, getContext().getResources().getDisplayMetrics());
        LinearLayout linearlayout = new LinearLayout(getContext());
        linearlayout.setBackgroundColor(-1);
        linearlayout.setPadding(j, j, j, j);
        LinearLayout linearlayout1 = new LinearLayout(getContext());
        linearlayout1.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        linearlayout1.setBackgroundColor(0xff000000);
        linearlayout.addView(linearlayout1);
        linearlayout1.setOrientation(1);
        j = ((int)TypedValue.applyDimension(1, _dialog_width, getContext().getResources().getDisplayMetrics()) / 2) * 2;
        _scroll_view = new ScrollView(getContext());
        _scroll_view.setLayoutParams(new LinearLayout.LayoutParams(j, -2));
        linearlayout1.addView(_scroll_view);
        _content_view = new LinearLayout(getContext());
        _content_view.setLayoutParams(new android.widget.FrameLayout.LayoutParams(j, -2));
        _content_view.setOrientation(1);
        _content_view.setGravity(17);
        _scroll_view.addView(_content_view);
        _title_label = new TextView(getContext());
        _title_label.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        _title_label.setPadding(i, i, i, i);
        _title_label.setTextColor(-1);
        _title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        _title_label.setText("\u9078\u9805");
        _title_label.setBackgroundColor(0xff202020);
        _title_label.setGravity(17);
        _content_view.addView(_title_label);
        _item_block = new LinearLayout(getContext());
        _item_block.setLayoutParams(new android.widget.FrameLayout.LayoutParams(j, -2));
        _item_block.setOrientation(1);
        _item_block.setGravity(17);
        _content_view.addView(_item_block);
        return linearlayout;
    }

    private Button createButton()
    {
        Button button;
        button = new Button(getContext());
        button.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        button.setMinimumHeight((int)TypedValue.applyDimension(1, 60F, getContext().getResources().getDisplayMetrics()));
        button.setGravity(17);
        if (_item_text_size != 0) goto _L2; else goto _L1
_L1:
        button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
_L4:
        button.setBackgroundDrawable(ASLayoutParams.getInstance().getListItemBackgroundDrawable());
        button.setTextColor(ASLayoutParams.getInstance().getListItemTextColor());
        button.setSingleLine(true);
        return button;
_L2:
        if (_item_text_size == 1)
        {
            button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        } else
        if (_item_text_size == 2)
        {
            button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge());
        }
        if (true) goto _L4; else goto _L3
_L3:
    }

    public static ASListDialog createDialog()
    {
        return new ASListDialog();
    }

    private int indexOfButton(Button button)
    {
        byte byte0 = -1;
        int i = 0;
        do
        {
label0:
            {
                int j = byte0;
                if (i < _item_list.size())
                {
                    if (((ASListDialogItem)_item_list.get(i)).button != button)
                    {
                        break label0;
                    }
                    j = i;
                }
                return j;
            }
            i++;
        } while (true);
    }

    private void onItemClicked(Button button)
    {
        if (_listener != null)
        {
            int i = indexOfButton(button);
            if (i != -1)
            {
                _listener.onListDialogItemClicked(this, i, ((ASListDialogItem)_item_list.get(i)).title);
            }
            dismiss();
        }
    }

    private boolean onItemLongClicked(Button button)
    {
        boolean flag1 = false;
        boolean flag = flag1;
        if (_listener != null)
        {
            int i = indexOfButton(button);
            flag = flag1;
            if (i != -1)
            {
                flag = _listener.onListDialogItemLongClicked(this, i, ((ASListDialogItem)_item_list.get(i)).title);
            }
        }
        if (flag)
        {
            dismiss();
        }
        return flag;
    }

    public ASListDialog addItem(String s)
    {
        Button button = createButton();
        button.setOnClickListener(new View.OnClickListener() {

            final ASListDialog this$0;

            public void onClick(View view)
            {
                onItemClicked((Button)view);
            }

            
            {
                this$0 = ASListDialog.this;
                super();
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {

            final ASListDialog this$0;

            public boolean onLongClick(View view)
            {
                return onItemLongClicked((Button)view);
            }

            
            {
                this$0 = ASListDialog.this;
                super();
            }
        });
        ASListDialogItem aslistdialogitem;
        if (s == null)
        {
            button.setVisibility(8);
        } else
        {
            if (_item_list.size() > 0)
            {
                _item_block.addView(createDivider());
            }
            button.setText(s);
        }
        _item_block.addView(button);
        aslistdialogitem = new ASListDialogItem();
        aslistdialogitem.button = button;
        aslistdialogitem.title = s;
        _item_list.add(aslistdialogitem);
        return this;
    }

    public ASListDialog addItems(String as[])
    {
        int j = as.length;
        for (int i = 0; i < j; i++)
        {
            addItem(as[i]);
        }

        return this;
    }

    public View createDivider()
    {
        View view = new View(getContext());
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)Math.ceil(TypedValue.applyDimension(1, 1.0F, getContext().getResources().getDisplayMetrics()))));
        view.setBackgroundColor(0x80ffffff);
        return view;
    }

    public String getName()
    {
        return "ListDialog";
    }

    public ASListDialog setDialogWidth(float f)
    {
        _dialog_width = f;
        int i = ((int)TypedValue.applyDimension(1, _dialog_width, getContext().getResources().getDisplayMetrics()) / 2) * 2;
        _scroll_view.setLayoutParams(new LinearLayout.LayoutParams(i, -2));
        _content_view.setLayoutParams(new android.widget.FrameLayout.LayoutParams(i, -2));
        return this;
    }

    public ASListDialog setItemTextSize(int i)
    {
        _item_text_size = i;
        return this;
    }

    public ASListDialog setListener(ASListDialogItemClickListener aslistdialogitemclicklistener)
    {
        _listener = aslistdialogitemclicklistener;
        return this;
    }

    public ASListDialog setTitle(String s)
    {
        if (_title_label != null)
        {
            _title_label.setText(s);
        }
        return this;
    }


}
