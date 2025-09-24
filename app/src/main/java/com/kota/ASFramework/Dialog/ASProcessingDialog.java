package com.kota.ASFramework.Dialog;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
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

import java.util.Objects;

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

  public static void dismissProcessingDialog() {
    if (!ASNavigationController.getCurrentController().isInBackground()) {
      new ASRunner() {
        @Override
        public void run() {
          if (_instance != null) {
            _instance.dismiss();
            releaseInstance();
          }
        }
      }.runInMainThread();
    }
  }

  public ASProcessingDialog() {
    requestWindowFeature(1);
    setContentView(R.layout.as_processing_dialog);
    if (getWindow()!=null)
            getWindow().setBackgroundDrawable(null);
    buildContentView();
  }

  void buildContentView() {
    // frame_view
    LinearLayout frame_view = findViewById(R.id.as_processing_dialog_frame_view);
    _message_label = frame_view.findViewById(R.id.as_processing_dialog_text);
    _message_label.setText(R.string.zero_word);
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