package com.kumi.Bahamut.Dialogs;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.kumi.ASFramework.Dialog.ASDialog;

public class Dialog_PostArticle extends ASDialog implements View.OnClickListener {
  public static final int NEW = 0;
  
  public static final int REPLY = 1;
  
  Button _cancel_button = null;
  
  Dialog_PostArticle_Listener _listener = null;
  
  RadioGroup _post_target_group;
  
  Button _send_button = null;
  
  Spinner _sign_spinner = null;
  
  int _target = 0;
  
  public Dialog_PostArticle(int paramInt) {
    requestWindowFeature(1);
    setContentView(2131361835);
    getWindow().setBackgroundDrawable(null);
    this._target = paramInt;
    this._post_target_group = (RadioGroup)findViewById(2131231019);
    this._send_button = (Button)findViewById(2131231046);
    this._cancel_button = (Button)findViewById(2131230967);
    View view = findViewById(2131231026);
    findViewById(2131231053);
    if (this._target == 0) {
      view.setVisibility(8);
    } else {
      view.setVisibility(0);
    } 
    this._sign_spinner = (Spinner)findViewById(2131231052);
    ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getContext(), 2130837504, 17367048);
    arrayAdapter.setDropDownViewResource(17367049);
    this._sign_spinner.setAdapter((SpinnerAdapter)arrayAdapter);
    this._send_button.setOnClickListener(this);
    this._cancel_button.setOnClickListener(this);
  }
  
  public String getName() {
    return "BahamutBoardsPostArticle";
  }
  
  public void onClick(View paramView) {
    if (paramView == this._send_button && this._listener != null) {
      String str1;
      int i = this._post_target_group.getCheckedRadioButtonId();
      int j = this._sign_spinner.getSelectedItemPosition();
      String str2 = "";
      if (j > 0)
        str2 = "" + (j - 1); 
      if (i == 2131231022) {
        str1 = "M";
      } else if (i == 2131231021) {
        str1 = "B";
      } else {
        str1 = "F";
      } 
      this._listener.onPostArticleDoneWithTarger(str1, str2);
    } 
    dismiss();
  }
  
  public void setListener(Dialog_PostArticle_Listener paramDialog_PostArticle_Listener) {
    this._listener = paramDialog_PostArticle_Listener;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Dialogs\Dialog_PostArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */