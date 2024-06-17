package com.kota.ASFramework.Dialog;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kota.ASFramework.PageController.ASViewController;
import com.kota.Bahamut.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ASAlertDialog extends ASDialog implements View.OnClickListener {
  private static final Map<String, ASAlertDialog> _alerts = new HashMap<>();
  
  private String _alert_id = null;
  
  private final Vector<Button> _item_list = new Vector<>();
  
  private ASAlertDialogListener _listener = null;
  
  private TextView _message_label = null;
  
  private TextView _title_label = null;
  
  private LinearLayout _toolbar = null;
  private int _default_index = -1;
  
  public ASAlertDialog() {
    initial();
  }
  
  public ASAlertDialog(String paramString) {
    initial();
    _alert_id = paramString;
  }
  
  private View buildContentView() {
    int n = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ASLayoutParams.getInstance().getDialogWidthNormal(), getContext().getResources().getDisplayMetrics());
    int i = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0F, getContext().getResources().getDisplayMetrics());
    int k = (int)Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6.0F, getContext().getResources().getDisplayMetrics()));
    int j = (int)Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.0F, getContext().getResources().getDisplayMetrics()));
    int m = (int)Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0F, getContext().getResources().getDisplayMetrics()));
    LinearLayout linearLayout2 = new LinearLayout(getContext());
    linearLayout2.setOrientation(LinearLayout.VERTICAL);
    linearLayout2.setPadding(j, j, j, j);
    linearLayout2.setBackgroundResource(R.color.dialog_border_color);
    LinearLayout linearLayout1 = new LinearLayout(getContext());
    linearLayout1.setOrientation(LinearLayout.VERTICAL);
    linearLayout1.setPadding(m, m, m, m);
    linearLayout1.setBackgroundColor(-16777216);
    linearLayout2.addView((View)linearLayout1);
    _title_label = new TextView(getContext());
    _title_label.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(n, ViewGroup.LayoutParams.WRAP_CONTENT));
    _title_label.setPadding(k, k, k, k);
    _title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge());
    _title_label.setTextColor(-1);
    _title_label.setTypeface(_title_label.getTypeface(), Typeface.BOLD);
    _title_label.setVisibility(View.GONE);
    _title_label.setBackgroundColor(-15724528);
    _title_label.setSingleLine(true);
    linearLayout1.addView((View)_title_label);
    _message_label = new TextView(getContext());
    _message_label.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(n, ViewGroup.LayoutParams.WRAP_CONTENT));
    _message_label.setPadding(k, k, k, k);
    _message_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
    _message_label.setMinimumHeight(i);
    _message_label.setTextColor(-1);
    _message_label.setVisibility(View.GONE);
    _message_label.setBackgroundColor(-16777216);
    linearLayout1.addView((View)_message_label);
    _toolbar = new LinearLayout(getContext());
    _toolbar.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(n, ViewGroup.LayoutParams.WRAP_CONTENT));
    _toolbar.setGravity(17);
    _toolbar.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout1.addView((View)_toolbar);
    return (View)linearLayout2;
  }
  
  private void clear() {
    if (_message_label != null)
      _message_label.setText(""); 
    if (_title_label != null)
      _title_label.setText(""); 
    _toolbar.removeAllViews();
    _item_list.clear();
  }
  
  public static boolean containsAlert(String paramString) {
    boolean bool = false;
    if (paramString != null)
      bool = _alerts.containsKey(paramString); 
    return bool;
  }
  
  public static ASAlertDialog create(String paramString) {
    ASAlertDialog aSAlertDialog = _alerts.get(paramString);
    if (aSAlertDialog == null)
      return new ASAlertDialog(paramString); 
    aSAlertDialog.clear();
    return aSAlertDialog;
  }
  
  private Button createButton() {
    Button button = new Button(getContext());
    button.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0F));
    int j = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.0F, getContext().getResources().getDisplayMetrics());
    int i = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0F, getContext().getResources().getDisplayMetrics());
    button.setPadding(i, j, i, j);
    button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
    button.setMinimumHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ASLayoutParams.getInstance().getDefaultTouchBlockHeight(), getContext().getResources().getDisplayMetrics()));
    button.setGravity(17);
    button.setOnClickListener(this);
    button.setBackground(ASLayoutParams.getInstance().getAlertItemBackgroundDrawable());
    button.setSingleLine(false);
    button.setTextColor(ASLayoutParams.getInstance().getAlertItemTextColor());
    return button;
  }
  
  public static ASAlertDialog createDialog() {
    return new ASAlertDialog();
  }
  
  public static void hideAlert(String paramString) {
    ASAlertDialog aSAlertDialog = _alerts.get(paramString);
    if (aSAlertDialog != null)
      aSAlertDialog.dismiss(); 
  }
  
  private void initial() {
    requestWindowFeature(1);
    setContentView(buildContentView());
    getWindow().setBackgroundDrawable(null);
  }
  
  public ASAlertDialog addButton(String paramString) {
    if (paramString != null) {
      if (_item_list.size() > 0)
        _toolbar.addView(createDivider()); 
      Button button = createButton();
      _toolbar.addView((View)button);
      button.setText(paramString);
      if (paramString.length() < 4) {
        button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
      } else {
        button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
      }
      button.setOnClickListener(this);
      _item_list.add(button);
    } 
    return this;
  }
  
  public View createDivider() {
    View view = new View(getContext());
    view.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams((int)Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0F, getContext().getResources().getDisplayMetrics())), ViewGroup.LayoutParams.MATCH_PARENT));
    view.setBackgroundColor(-16777216);
    return view;
  }
  
  public void dismiss() {
    if (_alert_id != null)
      _alerts.remove(_alert_id); 
    super.dismiss();
  }
  
  public void onClick(View paramView) {
    if (_listener != null) {
      int i = _item_list.indexOf(paramView);
      _listener.onAlertDialogDismissWithButtonIndex(this, i);
    } 
    dismiss();
  }
  
  public ASAlertDialog setItemTitle(int paramInt, String paramString) {
    if (paramInt >= 0 && paramInt < _item_list.size())
      ((Button)_item_list.get(paramInt)).setText(paramString); 
    return this;
  }
  
  public ASAlertDialog setListener(ASAlertDialogListener paramASAlertDialogListener) {
    _listener = paramASAlertDialogListener;
    return this;
  }
  
  public ASAlertDialog setMessage(String paramString) {
    if (paramString == null) {
      _message_label.setVisibility(View.GONE);
      return this;
    } 
    _message_label.setVisibility(View.VISIBLE);
    _message_label.setText(paramString);
    return this;
  }
  
  public ASAlertDialog setTitle(String paramString) {
    if (paramString == null) {
      _title_label.setVisibility(View.GONE);
      return this;
    } 
    _title_label.setVisibility(View.VISIBLE);
    _title_label.setText(paramString);
    return this;
  }
  
  public void show() {
    if (_alert_id != null) {
      ASAlertDialog aSAlertDialog = _alerts.get(_alert_id);
      if (aSAlertDialog != null && aSAlertDialog.isShowing())
        aSAlertDialog.dismiss(); 
      _alerts.put(_alert_id, this);
    } 
    super.show();
  }

  // 設定都不按的時候, 是否傳回預設值
  // 預設不傳
  public ASAlertDialog setDefaultButtonIndex(int i) {
    _default_index = i;
    return this;
  }

  @Override
  public void cancel() {
    if (_default_index>-1) {
      _listener.onAlertDialogDismissWithButtonIndex(this, _default_index);
    }
    super.cancel();
  }

  public static void showErrorDialog(String err_message, ASViewController bahamutController) {
    ASAlertDialog.createDialog()
            .setTitle("錯誤")
            .setMessage(err_message)
            .addButton("確定")
            .setListener((aDialog, index) ->
                    aDialog.dismiss())
            .scheduleDismissOnPageDisappear(bahamutController)
            .show();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASAlertDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */