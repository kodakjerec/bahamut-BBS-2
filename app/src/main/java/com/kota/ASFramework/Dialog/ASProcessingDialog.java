// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASDialog, ASProcessingDialogOnBackDelegate

public class ASProcessingDialog extends ASDialog
{

    private static ASProcessingDialog _instance = null;
    private TextView _message_label;
    private ASProcessingDialogOnBackDelegate _on_back_delegate;
    private ProgressBar _progress_bar;

    public ASProcessingDialog()
    {
        _progress_bar = null;
        _message_label = null;
        _on_back_delegate = null;
        requestWindowFeature(1);
        setContentView(buildContentView());
        getWindow().setBackgroundDrawable(null);
    }

    private View buildContentView()
    {
        LinearLayout linearlayout = new LinearLayout(getContext());
        linearlayout.setLayoutParams(new android.view.ViewGroup.LayoutParams(-2, -2));
        int i = (int)TypedValue.applyDimension(1, 3F, getContext().getResources().getDisplayMetrics());
        linearlayout.setPadding(i, i, i, i);
        linearlayout.setBackgroundColor(-1);
        LinearLayout linearlayout1 = new LinearLayout(getContext());
        linearlayout1.setLayoutParams(new android.view.ViewGroup.LayoutParams(-2, -2));
        i = (int)TypedValue.applyDimension(1, 15F, getContext().getResources().getDisplayMetrics());
        linearlayout1.setPadding(i, i, i, (int)TypedValue.applyDimension(1, 10F, getContext().getResources().getDisplayMetrics()));
        linearlayout1.setOrientation(1);
        linearlayout1.setGravity(1);
        linearlayout1.setMinimumWidth((int)TypedValue.applyDimension(1, 100F, getContext().getResources().getDisplayMetrics()));
        linearlayout1.setBackgroundColor(0xff000000);
        linearlayout.addView(linearlayout1);
        _progress_bar = new ProgressBar(getContext());
        _progress_bar.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        i = (int)TypedValue.applyDimension(1, 10F, getContext().getResources().getDisplayMetrics());
        _progress_bar.setPadding(0, 0, 0, i);
        linearlayout1.addView(_progress_bar);
        _message_label = new TextView(getContext());
        _message_label.setTextSize(2, 24F);
        _message_label.setTextColor(-1);
        _message_label.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 1.0F));
        linearlayout1.addView(_message_label);
        return linearlayout;
    }

    private static void construceInstance()
    {
        _instance = new ASProcessingDialog();
    }

    public static void hideProcessingDialog()
    {
        if (ASNavigationController.getCurrentController().isInBackground())
        {
            return;
        } else
        {
            (new ASRunner() {

                public void run()
                {
                    if (ASProcessingDialog._instance != null)
                    {
                        ASProcessingDialog._instance.dismiss();
                        ASProcessingDialog.releaseInstance();
                    }
                }

            }).runInMainThread();
            return;
        }
    }

    private static void releaseInstance()
    {
        _instance = null;
    }

    public static void showProcessingDialog(String s)
    {
        showProcessingDialog(s, null);
    }

    public static void showProcessingDialog(String s, ASProcessingDialogOnBackDelegate asprocessingdialogonbackdelegate)
    {
        if (ASNavigationController.getCurrentController().isInBackground())
        {
            return;
        } else
        {
            (new ASRunner(s, asprocessingdialogonbackdelegate) {

                final String val$aMessage;
                final ASProcessingDialogOnBackDelegate val$onBackDelegate;

                public void run()
                {
                    if (ASProcessingDialog._instance == null)
                    {
                        ASProcessingDialog.construceInstance();
                    }
                    ASProcessingDialog._instance.setMessage(aMessage);
                    ASProcessingDialog._instance.setOnBackDelegate(onBackDelegate);
                    if (!ASProcessingDialog._instance.isShowing())
                    {
                        ASProcessingDialog._instance.show();
                    }
                }

            
            {
                aMessage = s;
                onBackDelegate = asprocessingdialogonbackdelegate;
                super();
            }
            }).runInMainThread();
            return;
        }
    }

    public void dismiss()
    {
        super.dismiss();
    }

    public void onBackPressed()
    {
        if (_on_back_delegate == null || !_on_back_delegate.onASProcessingDialogOnBackDetected(this))
        {
            super.onBackPressed();
        }
    }

    public void setMessage(String s)
    {
        _message_label.setText(s);
    }

    public void setOnBackDelegate(ASProcessingDialogOnBackDelegate asprocessingdialogonbackdelegate)
    {
        _on_back_delegate = asprocessingdialogonbackdelegate;
    }

    public void show()
    {
        super.show();
    }




}
