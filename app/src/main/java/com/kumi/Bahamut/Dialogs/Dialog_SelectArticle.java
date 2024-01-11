package com.kumi.Bahamut.Dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.kumi.ASFramework.Dialog.ASDialog;

public class Dialog_SelectArticle extends ASDialog implements View.OnClickListener {
  Button _button_0_p = null;
  
  Button _button_1_p = null;
  
  Button _button_2_p = null;
  
  Button _button_3_p = null;
  
  Button _button_4_p = null;
  
  Button _button_5_p = null;
  
  Button _button_6_p = null;
  
  Button _button_7_p = null;
  
  Button _button_8_p = null;
  
  Button _button_9_p = null;
  
  Button _button_back_space_p = null;
  
  Button _cancel_button = null;
  
  TextView _content = null;
  
  String _content_string = "";
  
  Dialog_SelectArticle_Listener _listener = null;
  
  Button _search_button = null;
  
  public Dialog_SelectArticle() {
    requestWindowFeature(1);
    setContentView(2131361838);
    getWindow().setBackgroundDrawable(null);
    this._content = (TextView)findViewById(2131230781);
    this._button_0_p = (Button)findViewById(2131230758);
    this._button_1_p = (Button)findViewById(2131230760);
    this._button_2_p = (Button)findViewById(2131230762);
    this._button_3_p = (Button)findViewById(2131230764);
    this._button_4_p = (Button)findViewById(2131230766);
    this._button_5_p = (Button)findViewById(2131230768);
    this._button_6_p = (Button)findViewById(2131230770);
    this._button_7_p = (Button)findViewById(2131230772);
    this._button_8_p = (Button)findViewById(2131230774);
    this._button_9_p = (Button)findViewById(2131230776);
    this._button_back_space_p = (Button)findViewById(2131230778);
    this._search_button = (Button)findViewById(2131230780);
    this._cancel_button = (Button)findViewById(2131230779);
    this._button_0_p.setOnClickListener(this);
    this._button_1_p.setOnClickListener(this);
    this._button_2_p.setOnClickListener(this);
    this._button_3_p.setOnClickListener(this);
    this._button_4_p.setOnClickListener(this);
    this._button_5_p.setOnClickListener(this);
    this._button_6_p.setOnClickListener(this);
    this._button_7_p.setOnClickListener(this);
    this._button_8_p.setOnClickListener(this);
    this._button_9_p.setOnClickListener(this);
    this._button_back_space_p.setOnClickListener(this);
    this._search_button.setOnClickListener(this);
    this._cancel_button.setOnClickListener(this);
  }
  
  public String getName() {
    return "BahamutBoardSelectDialog";
  }
  
  public void onClick(View paramView) {
    if (paramView == this._button_0_p) {
      this._content_string += Character.MIN_VALUE;
    } else if (paramView == this._button_1_p) {
      this._content_string++;
    } else if (paramView == this._button_2_p) {
      this._content_string += '\002';
    } else if (paramView == this._button_3_p) {
      this._content_string += '\003';
    } else if (paramView == this._button_4_p) {
      this._content_string += '\004';
    } else if (paramView == this._button_5_p) {
      this._content_string += '\005';
    } else if (paramView == this._button_6_p) {
      this._content_string += '\006';
    } else if (paramView == this._button_7_p) {
      this._content_string += '\007';
    } else if (paramView == this._button_8_p) {
      this._content_string += '\b';
    } else if (paramView == this._button_9_p) {
      this._content_string += '\t';
    } else if (paramView == this._button_back_space_p) {
      if (this._content_string.length() > 0)
        this._content_string = this._content_string.substring(0, this._content_string.length() - 1); 
    } else if (paramView == this._search_button) {
      if (this._listener != null)
        this._listener.onSelectDialogDismissWIthIndex(this._content_string); 
      dismiss();
    } else if (paramView == this._cancel_button) {
      dismiss();
    } 
    if (this._content_string.length() > 5)
      this._content_string = this._content_string.substring(0, 5); 
    this._content.setText(this._content_string);
  }
  
  public void setListener(Dialog_SelectArticle_Listener paramDialog_SelectArticle_Listener) {
    this._listener = paramDialog_SelectArticle_Listener;
  }
  
  public void show() {
    super.show();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Dialogs\Dialog_SelectArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */