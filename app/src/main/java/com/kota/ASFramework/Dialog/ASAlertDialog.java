package com.kota.ASFramework.Dialog;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
  
  public ASAlertDialog() {
    initial();
  }
  
  public ASAlertDialog(String paramString) {
    initial();
    this._alert_id = paramString;
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
    this._title_label = new TextView(getContext());
    this._title_label.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(n, ViewGroup.LayoutParams.WRAP_CONTENT));
    this._title_label.setPadding(k, k, k, k);
    this._title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge());
    this._title_label.setTextColor(-1);
    this._title_label.setTypeface(this._title_label.getTypeface(), Typeface.BOLD);
    this._title_label.setVisibility(View.GONE);
    this._title_label.setBackgroundColor(-15724528);
    this._title_label.setSingleLine(true);
    linearLayout1.addView((View)this._title_label);
    this._message_label = new TextView(getContext());
    this._message_label.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(n, ViewGroup.LayoutParams.WRAP_CONTENT));
    this._message_label.setPadding(k, k, k, k);
    this._message_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
    this._message_label.setMinimumHeight(i);
    this._message_label.setTextColor(-1);
    this._message_label.setVisibility(View.GONE);
    this._message_label.setBackgroundColor(-16777216);
    linearLayout1.addView((View)this._message_label);
    this._toolbar = new LinearLayout(getContext());
    this._toolbar.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(n, ViewGroup.LayoutParams.WRAP_CONTENT));
    this._toolbar.setGravity(17);
    this._toolbar.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout1.addView((View)this._toolbar);
    return (View)linearLayout2;
  }
  
  private void clear() {
    if (this._message_label != null)
      this._message_label.setText(""); 
    if (this._title_label != null)
      this._title_label.setText(""); 
    this._toolbar.removeAllViews();
    this._item_list.clear();
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
      if (this._item_list.size() > 0)
        this._toolbar.addView(createDivider()); 
      Button button = createButton();
      this._toolbar.addView((View)button);
      button.setText(paramString);
      if (paramString != null)
        if (paramString.length() < 4) {
          button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
        } else {
          button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
        }  
      button.setOnClickListener(this);
      this._item_list.add(button);
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
    if (this._alert_id != null)
      _alerts.remove(this._alert_id); 
    super.dismiss();
  }
  
  public void onClick(View paramView) {
    if (this._listener != null) {
      int i = this._item_list.indexOf(paramView);
      this._listener.onAlertDialogDismissWithButtonIndex(this, i);
    } 
    dismiss();
  }
  
  public ASAlertDialog setItemTitle(int paramInt, String paramString) {
    if (paramInt >= 0 && paramInt < this._item_list.size())
      ((Button)this._item_list.get(paramInt)).setText(paramString); 
    return this;
  }
  
  public ASAlertDialog setListener(ASAlertDialogListener paramASAlertDialogListener) {
    this._listener = paramASAlertDialogListener;
    return this;
  }
  
  public ASAlertDialog setMessage(String paramString) {
    if (paramString == null) {
      this._message_label.setVisibility(View.GONE);
      return this;
    } 
    this._message_label.setVisibility(View.VISIBLE);
    this._message_label.setText(paramString);
    return this;
  }
  
  public ASAlertDialog setTitle(String paramString) {
    if (paramString == null) {
      this._title_label.setVisibility(View.GONE);
      return this;
    } 
    this._title_label.setVisibility(View.VISIBLE);
    this._title_label.setText(paramString);
    return this;
  }
  
  public void show() {
    if (this._alert_id != null) {
      ASAlertDialog aSAlertDialog = _alerts.get(this._alert_id);
      if (aSAlertDialog != null && aSAlertDialog.isShowing())
        aSAlertDialog.dismiss(); 
      _alerts.put(this._alert_id, this);
    } 
    super.show();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASAlertDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */