package com.kumi.Bahamut.Dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.kumi.ASFramework.Dialog.ASDialog;

public class Dialog_SelectSign extends ASDialog implements View.OnClickListener {
  Button _cancel_button = null;
  
  Button _confirm_button = null;
  
  Dialog_SelectSign_Listener _listener = null;
  
  EditText _sign_field = null;
  
  public Dialog_SelectSign() {
    requestWindowFeature(1);
    setContentView(2131361839);
    getWindow().setBackgroundDrawable(null);
    setTitle(getContext().getString(2131558516));
    this._sign_field = (EditText)findViewById(2131230784);
    this._confirm_button = (Button)findViewById(2131230783);
    this._cancel_button = (Button)findViewById(2131230782);
    this._confirm_button.setOnClickListener(this);
    this._cancel_button.setOnClickListener(this);
  }
  
  public String getName() {
    return "BahamutSelectSignDialog";
  }
  
  public void onClick(View paramView) {
    if (paramView == this._confirm_button && this._listener != null) {
      String str = this._sign_field.getText().toString().replace("\n", "");
      this._listener.onSelectSign(str);
    } 
    dismiss();
  }
  
  public void setListener(Dialog_SelectSign_Listener paramDialog_SelectSign_Listener) {
    this._listener = paramDialog_SelectSign_Listener;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Dialogs\Dialog_SelectSign.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */