package com.kumi.Bahamut.Dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.kumi.ASFramework.Dialog.ASDialog;

public class Dialog_SearchBoard extends ASDialog implements View.OnClickListener {
  Button _cancel_button = null;
  
  EditText _keyword_label = null;
  
  Dialog_SearchBoard_Listener _listener = null;
  
  Button _search_button = null;
  
  public Dialog_SearchBoard() {
    requestWindowFeature(1);
    setContentView(2131361837);
    getWindow().setBackgroundDrawable(null);
    setTitle("搜尋看板");
    this._keyword_label = (EditText)findViewById(2131230746);
    this._search_button = (Button)findViewById(2131230747);
    this._cancel_button = (Button)findViewById(2131230745);
    this._search_button.setOnClickListener(this);
    this._cancel_button.setOnClickListener(this);
  }
  
  public String getName() {
    return "BahamutBoardsSearchDialog";
  }
  
  public void onClick(View paramView) {
    if (paramView == this._search_button && this._listener != null) {
      String str = this._keyword_label.getText().toString().replace("\n", "");
      this._listener.onSearchButtonClickedWithKeyword(str);
    } 
    dismiss();
  }
  
  public void setListener(Dialog_SearchBoard_Listener paramDialog_SearchBoard_Listener) {
    this._listener = paramDialog_SearchBoard_Listener;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Dialogs\Dialog_SearchBoard.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */