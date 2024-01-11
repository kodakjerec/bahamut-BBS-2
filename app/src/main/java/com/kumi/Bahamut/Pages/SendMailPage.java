package com.kumi.Bahamut.Pages;

import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.Dialog.ASListDialog;
import com.kumi.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.Bahamut.Dialogs.Dialog_InsertSymbol;
import com.kumi.Bahamut.Dialogs.Dialog_InsertSymbol_Listener;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;
import java.util.Vector;

public class SendMailPage extends TelnetPage implements View.OnClickListener, View.OnFocusChangeListener, Dialog_InsertSymbol_Listener {
  private String _content = null;
  
  private EditText _content_field = null;
  
  private Button _hide_title_button = null;
  
  private SendMailPage_Listener _listener = null;
  
  private Button _post_button = null;
  
  private String _receiver = null;
  
  private EditText _receiver_field = null;
  
  private TextView _receiver_field_background = null;
  
  private Button _symbol_button = null;
  
  private String _title = null;
  
  private View _title_block = null;
  
  private boolean _title_block_hidden = false;
  
  private EditText _title_field = null;
  
  private TextView _title_field_background = null;
  
  private void initial() {
    this._title_field = (EditText)findViewById(2131230923);
    this._title_field.setOnFocusChangeListener(this);
    this._title_field_background = (TextView)findViewById(2131230924);
    this._receiver_field = (EditText)findViewById(2131230918);
    this._receiver_field.setOnFocusChangeListener(this);
    this._receiver_field_background = (TextView)findViewById(2131230919);
    this._content_field = (EditText)findViewById(2131230914);
    this._post_button = (Button)findViewById(2131230915);
    this._post_button.setOnClickListener(this);
    this._symbol_button = (Button)findViewById(2131230916);
    this._symbol_button.setOnClickListener(this);
    this._hide_title_button = (Button)findViewById(2131230912);
    this._hide_title_button.setOnClickListener(this);
    ((Button)findViewById(2131230913)).setOnClickListener(this);
    this._title_block = findViewById(2131230922);
    refresh();
  }
  
  private void onInsertSymbolbuttonClicked() {
    Dialog_InsertSymbol dialog_InsertSymbol = new Dialog_InsertSymbol();
    dialog_InsertSymbol.setListsner(this);
    dialog_InsertSymbol.show();
  }
  
  private void refreshContentField() {
    if (this._content_field != null && this._content != null) {
      this._content_field.setText(this._content);
      if (this._content.length() > 0)
        Selection.setSelection((Spannable)this._content_field.getText(), this._content.length()); 
      this._content = null;
    } 
  }
  
  private void refreshReceiverField() {
    if (this._receiver_field != null && this._receiver != null) {
      this._receiver_field.setText(this._receiver);
      this._receiver = null;
    } 
  }
  
  private void refreshTitleField() {
    if (this._title_field != null && this._title != null) {
      this._title_field.setText(this._title);
      if (this._title.length() > 0)
        Selection.setSelection((Spannable)this._title_field.getText(), 1); 
      this._title = null;
    } 
  }
  
  void changeViewMode() {
    boolean bool;
    if (!this._title_block_hidden) {
      bool = true;
    } else {
      bool = false;
    } 
    this._title_block_hidden = bool;
    refresh();
  }
  
  public void clear() {
    if (this._receiver_field != null)
      this._receiver_field.setText(""); 
    if (this._title_field != null)
      this._title_field.setText(""); 
    if (this._content_field != null)
      this._content_field.setText(""); 
    this._listener = null;
  }
  
  public String getName() {
    return "BahamutSendMailDialog";
  }
  
  public int getPageLayout() {
    return 2131361867;
  }
  
  public int getPageType() {
    return 17;
  }
  
  public boolean isKeepOnOffline() {
    return true;
  }
  
  public boolean isPopupPage() {
    return true;
  }
  
  public void onClick(View paramView) {
    String str;
    final String[] items;
    if (paramView == this._post_button) {
      final String send_receiver;
      final String send_title;
      final String send_content;
      if (this._listener != null) {
        str1 = this._receiver_field.getText().toString().replace("\n", "");
        str2 = this._title_field.getText().toString().replace("\n", "");
        str3 = this._content_field.getText().toString();
        String str4 = "";
        Vector<String> vector = new Vector();
        if (str1.length() == 0)
          vector.add("收件人"); 
        if (str2.length() == 0)
          vector.add("標題"); 
        if (str3.length() == 0)
          vector.add("內文"); 
        str = str4;
        if (vector.size() > 0) {
          byte b = 0;
          str = str4;
          while (b < vector.size()) {
            str4 = str + (String)vector.get(b);
            if (b == vector.size() - 2) {
              str = str4 + "與";
            } else {
              str = str4;
              if (b < vector.size() - 2)
                str = str4 + "、"; 
            } 
            b++;
          } 
          str = str + "不可為空";
        } 
        if (str.length() > 0) {
          ASAlertDialog.createDialog().setTitle("錯誤").setMessage(str).addButton("確定").show();
          return;
        } 
      } else {
        return;
      } 
      ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認").setMessage("您是否確定要送出此信件?").setListener(new ASAlertDialogListener() {
            final SendMailPage this$0;
            
            final String val$send_content;
            
            final SendMailPage val$send_dialog;
            
            final String val$send_receiver;
            
            final String val$send_title;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              if (param1Int == 1) {
                SendMailPage.this._listener.onSendMailDialogSendButtonClicked(send_dialog, send_receiver, send_title, send_content);
                SendMailPage.this.getNavigationController().popViewController();
                SendMailPage.this.clear();
              } 
            }
          }).show();
      return;
    } 
    if (str == this._symbol_button) {
      arrayOfString = (new UserSettings(getContext())).getSymbols();
      ASListDialog.createDialog().setDialogWidth(320.0F).setTitle("表情符號").addItems(arrayOfString).setListener(new ASListDialogItemClickListener() {
            final SendMailPage this$0;
            
            final String[] val$items;
            
            public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
              String str = items[param1Int];
              param1Int = SendMailPage.this._content_field.getSelectionStart();
              SendMailPage.this._content_field.getEditableText().insert(param1Int, str);
            }
            
            public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
              return false;
            }
          }).scheduleDismissOnPageDisappear((ASViewController)this).show();
      return;
    } 
    if (arrayOfString == this._hide_title_button) {
      onInsertSymbolbuttonClicked();
      return;
    } 
    if (arrayOfString.getId() == 2131230913)
      changeViewMode(); 
  }
  
  public void onFocusChange(View paramView, boolean paramBoolean) {
    if (paramView == this._receiver_field)
      if (paramBoolean) {
        this._receiver_field.setSingleLine(false);
        this._receiver_field_background.setTextColor(0);
        this._receiver_field.setTextColor(-1);
      } else {
        this._receiver_field.setSingleLine(true);
        this._receiver_field.setTextColor(0);
        this._receiver_field_background.setTextColor(-1);
        this._receiver_field_background.setText(this._receiver_field.getText().toString());
      }  
    if (paramView == this._title_field) {
      if (paramBoolean) {
        this._title_field.setSingleLine(false);
        this._title_field.setTextColor(-1);
        this._title_field_background.setTextColor(0);
        return;
      } 
    } else {
      return;
    } 
    this._title_field.setSingleLine(true);
    this._title_field.setTextColor(0);
    this._title_field_background.setTextColor(-1);
    this._title_field_background.setText(this._title_field.getText().toString());
  }
  
  public void onPageDidDisappear() {
    this._title_field = null;
    this._title_field_background = null;
    this._receiver_field = null;
    this._receiver_field_background = null;
    this._content_field = null;
    this._post_button = null;
    this._symbol_button = null;
    this._hide_title_button = null;
    this._title_block = null;
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    initial();
  }
  
  public void onPageRefresh() {
    refreshTitleField();
    refreshContentField();
    refreshReceiverField();
  }
  
  public void onPageWillAppear() {
    this._content_field.requestFocus();
  }
  
  public void onSymbolDialogDismissWithSymbol(String paramString) {
    int i = this._content_field.getSelectionStart();
    this._content_field.getEditableText().insert(i, paramString);
  }
  
  public void refresh() {
    if (this._title_block_hidden) {
      this._title_block.setVisibility(8);
      return;
    } 
    this._title_block.setVisibility(0);
  }
  
  public void setListener(SendMailPage_Listener paramSendMailPage_Listener) {
    this._listener = paramSendMailPage_Listener;
  }
  
  public void setPostContent(String paramString) {
    this._content = paramString;
    refreshContentField();
  }
  
  public void setPostTitle(String paramString) {
    this._title = paramString;
    refreshTitleField();
  }
  
  public void setReceiver(String paramString) {
    this._receiver = paramString;
    refreshReceiverField();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\SendMailPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */