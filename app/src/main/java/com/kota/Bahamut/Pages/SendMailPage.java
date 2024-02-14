package com.kota.Bahamut.Pages;

import android.text.Selection;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol_Listener;
import com.kota.Bahamut.R;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import java.util.Vector;

public class SendMailPage extends TelnetPage implements View.OnClickListener, View.OnFocusChangeListener, Dialog_InsertSymbol_Listener {
    private String _content = null;
    /* access modifiers changed from: private */
    public EditText _content_field = null;
    private Button _hide_title_button = null;
    /* access modifiers changed from: private */
    public SendMailPage_Listener _listener = null;
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

    public String getName() {
        return "BahamutSendMailDialog";
    }

    public void setListener(SendMailPage_Listener aListener) {
        this._listener = aListener;
    }

    public int getPageLayout() {
        return R.layout.send_mail_page;
    }

    public int getPageType() {
        return 17;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        initial();
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

    private void refreshTitleField() {
        if (this._title_field != null && this._title != null) {
            this._title_field.setText(this._title);
            if (this._title.length() > 0) {
                Selection.setSelection(this._title_field.getText(), 1);
            }
            this._title = null;
        }
    }

    private void refreshReceiverField() {
        if (this._receiver_field != null && this._receiver != null) {
            this._receiver_field.setText(this._receiver);
            this._receiver = null;
        }
    }

    private void refreshContentField() {
        if (this._content_field != null && this._content != null) {
            this._content_field.setText(this._content);
            if (this._content.length() > 0) {
                Selection.setSelection(this._content_field.getText(), this._content.length());
            }
            this._content = null;
        }
    }

    public void onPageRefresh() {
        refreshTitleField();
        refreshContentField();
        refreshReceiverField();
    }

    private void initial() {
        this._title_field = (EditText) findViewById(R.id.SendMail_TitleField);
        this._title_field.setOnFocusChangeListener(this);
        this._title_field_background = (TextView) findViewById(R.id.SendMail_TitleFieldBackground);
        this._receiver_field = (EditText) findViewById(R.id.SendMail_ReceiverField);
        this._receiver_field.setOnFocusChangeListener(this);
        this._receiver_field_background = (TextView) findViewById(R.id.SendMail_ReceiverFieldBackground);
        this._content_field = (EditText) findViewById(R.id.SendMailDialog_EditField);
        this._post_button = (Button) findViewById(R.id.SendMailDialog_Post);
        this._post_button.setOnClickListener(this);
        this._symbol_button = (Button) findViewById(R.id.SendMailDialog_Symbol);
        this._symbol_button.setOnClickListener(this);
        this._hide_title_button = (Button) findViewById(R.id.SendMailDialog_Cancel);
        this._hide_title_button.setOnClickListener(this);
        findViewById(R.id.SendMailDialog_change).setOnClickListener(this);
        this._title_block = findViewById(R.id.SendMail_TitleBlock);
        refresh();
    }

    public void clear() {
        if (this._receiver_field != null) {
            this._receiver_field.setText("");
        }
        if (this._title_field != null) {
            this._title_field.setText("");
        }
        if (this._content_field != null) {
            this._content_field.setText("");
        }
        this._listener = null;
    }

    public void setPostTitle(String aTitle) {
        this._title = aTitle;
        refreshTitleField();
    }

    public void setPostContent(String aContent) {
        this._content = aContent;
        refreshContentField();
    }

    public void onClick(View view) {
        if (view == this._post_button) {
            if (this._listener != null) {
                String receiver = this._receiver_field.getText().toString().replace("\n", "");
                String title = this._title_field.getText().toString().replace("\n", "");
                String content = this._content_field.getText().toString();
                StringBuilder err_msg = new StringBuilder();
                Vector<String> empty = new Vector<>();
                if (receiver.length() == 0) {
                    empty.add("收件人");
                }
                if (title.length() == 0) {
                    empty.add("標題");
                }
                if (content.length() == 0) {
                    empty.add("內文");
                }
                if (empty.size() > 0) {
                    for (int i = 0; i < empty.size(); i++) {
                        err_msg.append(empty.get(i));
                        if (i == empty.size() - 2) {
                            err_msg.append("與");
                        } else if (i < empty.size() - 2) {
                            err_msg.append("、");
                        }
                    }
                    err_msg.append("不可為空");
                }
                if (err_msg.length() > 0) {
                    ASAlertDialog.createDialog().setTitle("錯誤").setMessage(err_msg.toString()).addButton("確定").show();
                    return;
                }
                final String send_receiver = receiver;
                final String send_title = title;
                final String send_content = content;
                ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認").setMessage("您是否確定要送出此信件?").setListener((aDialog, index) -> {
                    if (index == 1) {
                        SendMailPage.this._listener.onSendMailDialogSendButtonClicked(SendMailPage.this, send_receiver, send_title, send_content);
                        SendMailPage.this.getNavigationController().popViewController();
                        SendMailPage.this.clear();
                    }
                }).show();
            }
        } else if (view == this._symbol_button) {
            final String[] items = new UserSettings(getContext()).getSymbols();
            ASListDialog.createDialog().setDialogWidth(320.0f).setTitle("表情符號").addItems(items).setListener(new ASListDialogItemClickListener() {
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    String symbol = items[index];
                    SendMailPage.this._content_field.getEditableText().insert(SendMailPage.this._content_field.getSelectionStart(), symbol);
                }

                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }
            }).scheduleDismissOnPageDisappear(this).show();
        } else if (view == this._hide_title_button) {
            onInsertSymbolbuttonClicked();
        } else if (view.getId() == R.id.SendMailDialog_change) {
            changeViewMode();
        }
    }

    public void refresh() {
        if (this._title_block_hidden) {
            this._title_block.setVisibility(View.GONE);
        } else {
            this._title_block.setVisibility(View.VISIBLE);
        }
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this._receiver_field) {
            if (hasFocus) {
                this._receiver_field.setSingleLine(false);
                this._receiver_field_background.setTextColor(0);
                this._receiver_field.setTextColor(-1);
            } else {
                this._receiver_field.setSingleLine(true);
                this._receiver_field.setTextColor(0);
                this._receiver_field_background.setTextColor(-1);
                this._receiver_field_background.setText(this._receiver_field.getText().toString());
            }
        }
        if (v != this._title_field) {
            return;
        }
        if (hasFocus) {
            this._title_field.setSingleLine(false);
            this._title_field.setTextColor(-1);
            this._title_field_background.setTextColor(0);
            return;
        }
        this._title_field.setSingleLine(true);
        this._title_field.setTextColor(0);
        this._title_field_background.setTextColor(-1);
        this._title_field_background.setText(this._title_field.getText().toString());
    }

    /* access modifiers changed from: package-private */
    public void changeViewMode() {
        this._title_block_hidden = !this._title_block_hidden;
        refresh();
    }

    private void onInsertSymbolbuttonClicked() {
        Dialog_InsertSymbol dialog = new Dialog_InsertSymbol();
        dialog.setListsner(this);
        dialog.show();
    }

    public void onSymbolDialogDismissWithSymbol(String symbol) {
        this._content_field.getEditableText().insert(this._content_field.getSelectionStart(), symbol);
    }

    public void setReceiver(String aRecevier) {
        this._receiver = aRecevier;
        refreshReceiverField();
    }

    public boolean isKeepOnOffline() {
        return true;
    }
}
