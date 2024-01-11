// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;

import android.app.Dialog;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.PageController.ASViewControllerDisappearListener;

// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASDialogOnBackPressedDelegate

public class ASDialog extends Dialog
    implements ASViewControllerDisappearListener
{

    private ASViewController _controller;
    private ASDialogOnBackPressedDelegate _on_back_delegate;
    private boolean _showing;

    public ASDialog()
    {
        super(ASNavigationController.getCurrentController());
        _on_back_delegate = null;
        _showing = false;
        _controller = null;
    }

    public ASDialog(int i)
    {
        super(ASNavigationController.getCurrentController(), i);
        _on_back_delegate = null;
        _showing = false;
        _controller = null;
    }

    protected ASDialog(boolean flag, OnCancelListener oncancellistener)
    {
        super(ASNavigationController.getCurrentController(), flag, oncancellistener);
        _on_back_delegate = null;
        _showing = false;
        _controller = null;
    }

    public void dismiss()
    {
        if (_controller != null)
        {
            _controller.unregisterDisappearListener(this);
            _controller = null;
        }
        _showing = false;
        super.dismiss();
    }

    public int getCurrentOrientation()
    {
        int i = 1;
        ASNavigationController asnavigationcontroller = ASNavigationController.getCurrentController();
        if (asnavigationcontroller != null)
        {
            i = asnavigationcontroller.getCurrentOrientation();
        }
        return i;
    }

    public String getName()
    {
        return "ASDialog";
    }

    public boolean isShowing()
    {
        return _showing;
    }

    public void onASViewControllerDidDisappear(ASViewController asviewcontroller)
    {
    }

    public void onASViewControllerWillDisappear(ASViewController asviewcontroller)
    {
        if (isShowing())
        {
            dismiss();
        }
    }

    public void onBackPressed()
    {
        if (_on_back_delegate == null || !_on_back_delegate.onASDialogBackPressed(this))
        {
            super.onBackPressed();
        }
    }

    public ASDialog scheduleDismissOnPageDisappear(ASViewController asviewcontroller)
    {
        if (_controller != null)
        {
            _controller.unregisterDisappearListener(this);
        }
        _controller = asviewcontroller;
        if (_controller != null)
        {
            _controller.registerDisappearListener(this);
        }
        return this;
    }

    public ASDialog setIsCancelable(boolean flag)
    {
        setCancelable(flag);
        return this;
    }

    public ASDialog setOnBackDelegate(ASDialogOnBackPressedDelegate asdialogonbackpresseddelegate)
    {
        _on_back_delegate = asdialogonbackpresseddelegate;
        return this;
    }

    public void show()
    {
        super.show();
        _showing = true;
    }
}
