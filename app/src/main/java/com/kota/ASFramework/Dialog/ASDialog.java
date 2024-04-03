package com.kota.ASFramework.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.PageController.ASViewControllerDisappearListener;

public class ASDialog extends Dialog implements ASViewControllerDisappearListener {
  private ASViewController _controller;
  private ASDialogOnBackPressedDelegate _on_back_delegate;
  private boolean _showing;

  public ASDialog(int theme) {
    super(ASNavigationController.getCurrentController(), theme);
    this._on_back_delegate = null;
    this._showing = false;
    this._controller = null;
  }

  protected ASDialog(boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
    super(ASNavigationController.getCurrentController(), cancelable, cancelListener);
    this._on_back_delegate = null;
    this._showing = false;
    this._controller = null;
  }

  public ASDialog() {
    super(ASNavigationController.getCurrentController());
    this._on_back_delegate = null;
    this._showing = false;
    this._controller = null;
  }

  @Override // android.app.Dialog, android.content.DialogInterface
  public void dismiss() {
    if (this._controller != null) {
      this._controller.unregisterDisappearListener(this);
      this._controller = null;
    }
    this._showing = false;
    super.dismiss();
  }

  @Override // android.app.Dialog
  public void show() {
    super.show();
    this._showing = true;
  }

  @Override
  public void hide() {
    super.hide();
    this._showing = false;
  }

  @Override // android.app.Dialog
  public boolean isShowing() {
    return this._showing;
  }

  public int getCurrentOrientation() {
    ASNavigationController current_controller = ASNavigationController.getCurrentController();
    if (current_controller == null) {
      return 1;
    }
    int orientation = current_controller.getCurrentOrientation();
    return orientation;
  }

  public String getName() {
    return "ASDialog";
  }

  public ASDialog setIsCancelable(boolean cancelable) {
    setCancelable(cancelable);
    return this;
  }

  @Override // android.app.Dialog
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

  @Override // com.kota.ASFramework.PageController.ASViewControllerDisappearListener
  public void onASViewControllerWillDisappear(ASViewController aController) {
    if (isShowing()) {
      dismiss();
    }
  }

  @Override // com.kota.ASFramework.PageController.ASViewControllerDisappearListener
  public void onASViewControllerDidDisappear(ASViewController aController) {
  }
}
