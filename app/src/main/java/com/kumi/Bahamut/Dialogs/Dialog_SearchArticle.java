package com.kumi.Bahamut.Dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import com.kumi.ASFramework.Dialog.ASDialog;
import java.util.Vector;

public class Dialog_SearchArticle extends ASDialog implements View.OnClickListener {
  LinearLayout _author_block = null;
  
  EditText _author_label = null;
  
  Button _cancel_button = null;
  
  EditText _gy_field = null;
  
  LinearLayout _header_block = null;
  
  LinearLayout _keyword_block = null;
  
  EditText _keyword_label = null;
  
  Dialog_SearchArticle_Listener _listener = null;
  
  LinearLayout _mark_block = null;
  
  RadioGroup _mark_radio = null;
  
  Button _search_button = null;
  
  public Dialog_SearchArticle() {
    requestWindowFeature(1);
    setContentView(2131361836);
    getWindow().setBackgroundDrawable(null);
    setTitle("搜尋文章");
    this._keyword_label = (EditText)findViewById(2131230749);
    this._author_label = (EditText)findViewById(2131230744);
    this._mark_radio = (RadioGroup)findViewById(2131230750);
    this._gy_field = (EditText)findViewById(2131230990);
    this._search_button = (Button)findViewById(2131230753);
    this._cancel_button = (Button)findViewById(2131230748);
    this._header_block = (LinearLayout)findViewById(2131230909);
    this._keyword_block = (LinearLayout)findViewById(2131230910);
    this._author_block = (LinearLayout)findViewById(2131230907);
    this._mark_block = (LinearLayout)findViewById(2131230911);
    this._search_button.setOnClickListener(this);
    this._cancel_button.setOnClickListener(this);
  }
  
  public String getName() {
    return "BahamutBoardSearchDialog";
  }
  
  public void onClick(View paramView) {
    if (paramView == this._search_button && this._listener != null) {
      Vector<String> vector = new Vector();
      String str3 = this._keyword_label.getText().toString().replace("\n", "");
      String str2 = this._author_label.getText().toString().replace("\n", "");
      String str1 = "NO";
      if (this._mark_radio.getCheckedRadioButtonId() == 2131230752)
        str1 = "YES"; 
      String str4 = this._gy_field.getText().toString();
      vector.add(str3);
      vector.add(str2);
      vector.add(str1);
      vector.add(str4);
      this._listener.onSearchDialogSearchButtonClickedWithValues(vector);
    } 
    dismiss();
  }
  
  public void setListener(Dialog_SearchArticle_Listener paramDialog_SearchArticle_Listener) {
    this._listener = paramDialog_SearchArticle_Listener;
  }
  
  public void show() {
    super.show();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Dialogs\Dialog_SearchArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */