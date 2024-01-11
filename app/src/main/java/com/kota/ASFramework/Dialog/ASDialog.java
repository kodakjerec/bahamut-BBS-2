package com.kota.ASFramework.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.PageController.ASViewControllerDisappearListener;

public class ASDialog extends Dialog implements ASViewControllerDisappearListener {
  private ASViewController _controller = null;
  
  private ASDialogOnBackPressedDelegate _on_back_delegate = null;
  
  private boolean _showing = false;
  
  public ASDialog() {
    super((Context)ASNavigationController.getCurrentController());
  }
  
  public ASDialog(int paramInt) {
    super((Context)ASNavigationController.getCurrentController(), paramInt);
  }
  
  protected ASDialog(boolean paramBoolean, OnCancelListener paramOnCancelListener) {
    super((Context)ASNavigationController.getCurrentController(), paramBoolean, paramOnCancelListener);
  }
  
  public void dismiss() {
    if (this._controller != null) {
      this._controller.unregisterDisappearListener(this);
      this._controller = null;
    } 
    this._showing = false;
    super.dismiss();
  }
  
  public int getCurrentOrientation() {
    int i = 1;
    ASNavigationController aSNavigationController = ASNavigationController.getCurrentController();
    if (aSNavigationController != null)
      i = aSNavigationController.getCurrentOrientation(); 
    return i;
  }
  
  public String getName() {
    return "ASDialog";
  }
  
  public boolean isShowing() {
    return this._showing;
  }
  
  public void onASViewControllerDidDisappear(ASViewController paramASViewController) {}
  
  public void onASViewControllerWillDisappear(ASViewController paramASViewController) {
    if (isShowing())
      dismiss(); 
  }
  
  public void onBackPressed() {
    if (this._on_back_delegate == null || !this._on_back_delegate.onASDialogBackPressed(this))
      super.onBackPressed(); 
  }
  
  public ASDialog scheduleDismissOnPageDisappear(ASViewController paramASViewController) {
    if (this._controller != null)
      this._controller.unregisterDisappearListener(this); 
    this._controller = paramASViewController;
    if (this._controller != null)
      this._controller.registerDisappearListener(this); 
    return this;
  }
  
  public ASDialog setIsCancelable(boolean paramBoolean) {
    setCancelable(paramBoolean);
    return this;
  }
  
  public ASDialog setOnBackDelegate(ASDialogOnBackPressedDelegate paramASDialogOnBackPressedDelegate) {
    this._on_back_delegate = paramASDialogOnBackPressedDelegate;
    return this;
  }
  
  public void show() {
    super.show();
    this._showing = true;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */