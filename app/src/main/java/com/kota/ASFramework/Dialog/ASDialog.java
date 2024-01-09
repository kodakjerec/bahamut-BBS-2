package com.kota.ASFramework.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.PageController.ASViewControllerDisappearListener;

public class ASDialog extends Dialog implements ASViewControllerDisappearListener {
    private ASViewController _controller = null;
    private ASDialogOnBackPressedDelegate _on_back_delegate = null;
    private boolean _showing = false;

    public ASDialog(int theme) {
        super(ASNavigationController.getCurrentController(), theme);
    }

    protected ASDialog(boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(ASNavigationController.getCurrentController(), cancelable, cancelListener);
    }

    public ASDialog() {
        super(ASNavigationController.getCurrentController());
    }

    public void dismiss() {
        if (this._controller != null) {
            this._controller.unregisterDisappearListener(this);
            this._controller = null;
        }
        this._showing = false;
        super.dismiss();
    }

    public void show() {
        super.show();
        this._showing = true;
    }

    public boolean isShowing() {
        return this._showing;
    }

    public int getCurrentOrientation() {
        ASNavigationController current_controller = ASNavigationController.getCurrentController();
        if (current_controller != null) {
            return current_controller.getCurrentOrientation();
        }
        return 1;
    }

    public String getName() {
        return "ASDialog";
    }

    public ASDialog setIsCancelable(boolean cancelable) {
        setCancelable(cancelable);
        return this;
    }

    public void onBackPressed() {
        if (this._on_back_delegate == null || !this._on_back_delegate.onASDialogBackPressed(this)) {
            super.onBackPressed();
        }
    }

    public ASDialog setOnBackDelegate(ASDialogOnBackPressedDelegate aDelegate) {
        this._on_back_delegate = aDelegate;
        return this;
    }

    public ASDialog scheduleDismissOnPageDisappear(ASViewController aController) {
        if (this._controller != null) {
            this._controller.unregisterDisappearListener(this);
        }
        this._controller = aController;
        if (this._controller != null) {
            this._controller.registerDisappearListener(this);
        }
        return this;
    }

    public void onASViewControllerWillDisappear(ASViewController aController) {
        if (isShowing()) {
            dismiss();
        }
    }

    public void onASViewControllerDidDisappear(ASViewController aController) {
    }
}
