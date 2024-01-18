package com.kota.ASFramework.Dialog;

import androidx.core.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASProcessingDialog extends ASDialog {
  private static ASProcessingDialog _instance = null;
  private ProgressBar _progress_bar = null;
  private TextView _message_label = null;
  private ASProcessingDialogOnBackDelegate _on_back_delegate = null;

  /* JADX INFO: Access modifiers changed from: private */
  public static void construceInstance() {
    _instance = new ASProcessingDialog();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public static void releaseInstance() {
    _instance = null;
  }

  public static void showProcessingDialog(String aMessage) {
    showProcessingDialog(aMessage, null);
  }

  public static void showProcessingDialog(final String aMessage, final ASProcessingDialogOnBackDelegate onBackDelegate) {
    if (!ASNavigationController.getCurrentController().isInBackground()) {
      new ASRunner() { // from class: com.kumi.ASFramework.Dialog.ASProcessingDialog.1
        @Override // com.kumi.ASFramework.Thread.ASRunner
        public void run() {
          if (ASProcessingDialog._instance == null) {
            ASProcessingDialog.construceInstance();
          }
          ASProcessingDialog._instance.setMessage(aMessage);
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
      new ASRunner() { // from class: com.kumi.ASFramework.Dialog.ASProcessingDialog.2
        @Override // com.kumi.ASFramework.Thread.ASRunner
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
    LinearLayout frame_view = new LinearLayout(getContext());
    frame_view.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
    int frame_padding = (int) TypedValue.applyDimension(1, 3.0f, getContext().getResources().getDisplayMetrics());
    frame_view.setPadding(frame_padding, frame_padding, frame_padding, frame_padding);
    frame_view.setBackgroundColor(-1);
    LinearLayout content_view = new LinearLayout(getContext());
    content_view.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
    int padding = (int) TypedValue.applyDimension(1, 15.0f, getContext().getResources().getDisplayMetrics());
    int padding_bottom = (int) TypedValue.applyDimension(1, 10.0f, getContext().getResources().getDisplayMetrics());
    content_view.setPadding(padding, padding, padding, padding_bottom);
    content_view.setOrientation(1);
    content_view.setGravity(1);
    content_view.setMinimumWidth((int) TypedValue.applyDimension(1, 100.0f, getContext().getResources().getDisplayMetrics()));
    content_view.setBackgroundColor(View.MEASURED_STATE_MASK);
    frame_view.addView(content_view);
    this._progress_bar = new ProgressBar(getContext());
    this._progress_bar.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
    int progress_padding = (int) TypedValue.applyDimension(1, 10.0f, getContext().getResources().getDisplayMetrics());
    this._progress_bar.setPadding(0, 0, 0, progress_padding);
    content_view.addView(this._progress_bar);
    this._message_label = new TextView(getContext());
    this._message_label.setTextSize(2, 24.0f);
    this._message_label.setTextColor(-1);
    this._message_label.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 1.0f));
    content_view.addView(this._message_label);
    return frame_view;
  }

  public void setMessage(String message) {
    this._message_label.setText(message);
  }

  @Override // com.kumi.ASFramework.Dialog.ASDialog, android.app.Dialog
  public void show() {
    super.show();
  }

  @Override // com.kumi.ASFramework.Dialog.ASDialog, android.app.Dialog, android.content.DialogInterface
  public void dismiss() {
    super.dismiss();
  }

  public void setOnBackDelegate(ASProcessingDialogOnBackDelegate onBackDelegate) {
    this._on_back_delegate = onBackDelegate;
  }

  @Override // com.kumi.ASFramework.Dialog.ASDialog, android.app.Dialog
  public void onBackPressed() {
    if (this._on_back_delegate == null || !this._on_back_delegate.onASProcessingDialogOnBackDetected(this)) {
      super.onBackPressed();
    }
  }
}