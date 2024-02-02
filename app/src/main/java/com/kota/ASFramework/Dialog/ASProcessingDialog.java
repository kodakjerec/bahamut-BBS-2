package com.kota.ASFramework.Dialog;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;

import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.R;

/* loaded from: classes.dex */
public class ASProcessingDialog extends ASDialog {
  @SuppressLint("StaticFieldLeak")
  private static ASProcessingDialog _instance = null;
  private TextView _message_label = null;
  private ASProcessingDialogOnBackDelegate _on_back_delegate = null;

  private static void construceInstance() {
    _instance = new ASProcessingDialog();
  }

  private static void releaseInstance() {
    _instance = null;
  }

  public static void showProcessingDialog(String aMessage) {
    showProcessingDialog(aMessage, null);
  }

  public static void showProcessingDialog(final String aMessage, final ASProcessingDialogOnBackDelegate onBackDelegate) {
    if (!ASNavigationController.getCurrentController().isInBackground()) {
      new ASRunner() { // from class: com.kota.ASFramework.Dialog.ASProcessingDialog.1
        @Override // com.kota.ASFramework.Thread.ASRunner
        public void run() {
          if (ASProcessingDialog._instance == null) {
            ASProcessingDialog.construceInstance();
          }
          setMessage(aMessage);
          ASProcessingDialog._instance.setOnBackDelegate(onBackDelegate);
          if (!ASProcessingDialog._instance.isShowing()) {
            ASProcessingDialog._instance.show();
          }
        }
      }.runInMainThread();
    }
  }

  public static void hideProcessingDialog() {
    if (!ASNavigationController.getCurrentController().isInBackground()) {
      new ASRunner() { // from class: com.kota.ASFramework.Dialog.ASProcessingDialog.2
        @Override // com.kota.ASFramework.Thread.ASRunner
        public void run() {
          if (ASProcessingDialog._instance != null) {
            ASProcessingDialog._instance.dismiss();
            ASProcessingDialog.releaseInstance();
          }
        }
      }.runInMainThread();
    }
  }

  public ASProcessingDialog() {
    requestWindowFeature(1);
    setContentView(buildContentView());
    getWindow().setBackgroundDrawable(null);
  }

  private View buildContentView() {
    // frame_view
    LinearLayout frame_view = new LinearLayout(getContext());
    frame_view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    int frame_padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.0f, getContext().getResources().getDisplayMetrics());
    frame_view.setPadding(frame_padding, frame_padding, frame_padding, frame_padding);
    frame_view.setBackgroundResource(R.color.dialog_border_color);
    // content_view
    LinearLayout content_view = new LinearLayout(getContext());
    content_view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0f, getContext().getResources().getDisplayMetrics());
    int padding_bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, getContext().getResources().getDisplayMetrics());
    content_view.setPadding(padding, padding, padding, padding_bottom);
    content_view.setOrientation(LinearLayout.VERTICAL);
    content_view.setGravity(Gravity.CENTER_HORIZONTAL);
    content_view.setMinimumWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0f, getContext().getResources().getDisplayMetrics()));
    content_view.setBackgroundColor(View.MEASURED_STATE_MASK);
    frame_view.addView(content_view);
    // progress bar
    ProgressBar _progress_bar = new ProgressBar(getContext());
    _progress_bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    int progress_padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, getContext().getResources().getDisplayMetrics());
    _progress_bar.setPadding(0, 0, 0, progress_padding);
    content_view.addView(_progress_bar);
    // 訊息
    this._message_label = new TextView(getContext());
    this._message_label.setTextSize(2, 24.0f);
    this._message_label.setTextColor(getContextColor(R.color.white));
    this._message_label.setGravity(Gravity.CENTER_HORIZONTAL);
    this._message_label.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
    content_view.addView(this._message_label);
    return frame_view;
  }

  public static void setMessage(String message) {
    new ASRunner() { // from class: com.kota.ASFramework.Dialog.ASProcessingDialog.2
      @Override // com.kota.ASFramework.Thread.ASRunner
      public void run() {
        if (ASProcessingDialog._instance != null) {
          ASProcessingDialog._instance._message_label.setText(message);
        }
      }
    }.runInMainThread();
  }

  @Override // com.kota.ASFramework.Dialog.ASDialog, android.app.Dialog
  public void show() {
    super.show();
  }

  @Override // com.kota.ASFramework.Dialog.ASDialog, android.app.Dialog, android.content.DialogInterface
  public void dismiss() {
    super.dismiss();
  }

  public void setOnBackDelegate(ASProcessingDialogOnBackDelegate onBackDelegate) {
    this._on_back_delegate = onBackDelegate;
  }

  @Override // com.kota.ASFramework.Dialog.ASDialog, android.app.Dialog
  public void onBackPressed() {
    if (this._on_back_delegate == null || !this._on_back_delegate.onASProcessingDialogOnBackDetected(this)) {
      super.onBackPressed();
    }
  }
}