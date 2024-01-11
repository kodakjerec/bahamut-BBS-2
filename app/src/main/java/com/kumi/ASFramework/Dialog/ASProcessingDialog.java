package com.kumi.ASFramework.Dialog;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.Thread.ASRunner;

public class ASProcessingDialog extends ASDialog {
  private static ASProcessingDialog _instance = null;
  
  private TextView _message_label = null;
  
  private ASProcessingDialogOnBackDelegate _on_back_delegate = null;
  
  private ProgressBar _progress_bar = null;
  
  public ASProcessingDialog() {
    requestWindowFeature(1);
    setContentView(buildContentView());
    getWindow().setBackgroundDrawable(null);
  }
  
  private View buildContentView() {
    LinearLayout linearLayout1 = new LinearLayout(getContext());
    linearLayout1.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
    int i = (int)TypedValue.applyDimension(1, 3.0F, getContext().getResources().getDisplayMetrics());
    linearLayout1.setPadding(i, i, i, i);
    linearLayout1.setBackgroundColor(-1);
    LinearLayout linearLayout2 = new LinearLayout(getContext());
    linearLayout2.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
    i = (int)TypedValue.applyDimension(1, 15.0F, getContext().getResources().getDisplayMetrics());
    linearLayout2.setPadding(i, i, i, (int)TypedValue.applyDimension(1, 10.0F, getContext().getResources().getDisplayMetrics()));
    linearLayout2.setOrientation(1);
    linearLayout2.setGravity(1);
    linearLayout2.setMinimumWidth((int)TypedValue.applyDimension(1, 100.0F, getContext().getResources().getDisplayMetrics()));
    linearLayout2.setBackgroundColor(-16777216);
    linearLayout1.addView((View)linearLayout2);
    this._progress_bar = new ProgressBar(getContext());
    this._progress_bar.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(-2, -2));
    i = (int)TypedValue.applyDimension(1, 10.0F, getContext().getResources().getDisplayMetrics());
    this._progress_bar.setPadding(0, 0, 0, i);
    linearLayout2.addView((View)this._progress_bar);
    this._message_label = new TextView(getContext());
    this._message_label.setTextSize(2, 24.0F);
    this._message_label.setTextColor(-1);
    this._message_label.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(-2, -2, 1.0F));
    linearLayout2.addView((View)this._message_label);
    return (View)linearLayout1;
  }
  
  private static void construceInstance() {
    _instance = new ASProcessingDialog();
  }
  
  public static void hideProcessingDialog() {
    if (!ASNavigationController.getCurrentController().isInBackground())
      (new ASRunner() {
          public void run() {
            if (ASProcessingDialog._instance != null) {
              ASProcessingDialog._instance.dismiss();
              ASProcessingDialog.releaseInstance();
            } 
          }
        }).runInMainThread(); 
  }
  
  private static void releaseInstance() {
    _instance = null;
  }
  
  public static void showProcessingDialog(String paramString) {
    showProcessingDialog(paramString, (ASProcessingDialogOnBackDelegate)null);
  }
  
  public static void showProcessingDialog(final String aMessage, final ASProcessingDialogOnBackDelegate onBackDelegate) {
    if (!ASNavigationController.getCurrentController().isInBackground())
      (new ASRunner() {
          final String val$aMessage;
          
          final ASProcessingDialogOnBackDelegate val$onBackDelegate;
          
          public void run() {
            if (ASProcessingDialog._instance == null)
              ASProcessingDialog.construceInstance(); 
            ASProcessingDialog._instance.setMessage(aMessage);
            ASProcessingDialog._instance.setOnBackDelegate(onBackDelegate);
            if (!ASProcessingDialog._instance.isShowing())
              ASProcessingDialog._instance.show(); 
          }
        }).runInMainThread(); 
  }
  
  public void dismiss() {
    super.dismiss();
  }
  
  public void onBackPressed() {
    if (this._on_back_delegate == null || !this._on_back_delegate.onASProcessingDialogOnBackDetected(this))
      super.onBackPressed(); 
  }
  
  public void setMessage(String paramString) {
    this._message_label.setText(paramString);
  }
  
  public void setOnBackDelegate(ASProcessingDialogOnBackDelegate paramASProcessingDialogOnBackDelegate) {
    this._on_back_delegate = paramASProcessingDialogOnBackDelegate;
  }
  
  public void show() {
    super.show();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASProcessingDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */