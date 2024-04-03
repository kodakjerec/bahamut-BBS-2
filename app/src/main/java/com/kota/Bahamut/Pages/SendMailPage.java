package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.text.Selection;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.DataModels.ArticleTemp;
import com.kota.Bahamut.DataModels.ArticleTempStore;
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression;
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression_Listener;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol_Listener;
import com.kota.Bahamut.Dialogs.Dialog_PaintColor;
import com.kota.Bahamut.Dialogs.Dialog_PaintColor_Listener;
import com.kota.Bahamut.Pages.BlockListPage.ArticleExpressionListPage;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import java.util.Vector;

public class SendMailPage extends TelnetPage implements View.OnClickListener, View.OnFocusChangeListener, Dialog_InsertSymbol_Listener, Dialog_PaintColor_Listener {
    String _content = null;
    EditText _content_field = null;
    Button _hide_title_button = null;
    Button _paint_color_button = null;
    SendMailPage_Listener _listener = null;
    Button _post_button = null;
    String _receiver = null;
    EditText _receiver_field = null;
    TextView _receiver_field_background = null;
    Button _symbol_button = null;
    String _title = null;
    View _title_block = null;
    boolean _title_block_hidden = false;
    EditText _title_field = null;
    TextView _title_field_background = null;
    public boolean recover = false;

    public String getName() {
        return "BahamutSendMailDialog";
    }

    public void setListener(SendMailPage_Listener aListener) {
        _listener = aListener;
    }

    public int getPageLayout() {
        return R.layout.send_mail_page;
    }

    public int getPageType() {
        return BahamutPage.BAHAMUT_SEND_MAIL;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        initial();
        if (recover) {
            loadTempArticle(8);
            recover = false;
        }
    }

    public void onPageDidDisappear() {
        _title_field = null;
        _title_field_background = null;
        _receiver_field = null;
        _receiver_field_background = null;
        _content_field = null;
        _post_button = null;
        _symbol_button = null;
        _hide_title_button = null;
        _title_block = null;
        super.onPageDidDisappear();
    }

    void refreshTitleField() {
        if (_title_field != null && _title != null) {
            _title_field.setText(_title);
            if (_title.length() > 0) {
                Selection.setSelection(_title_field.getText(), 1);
            }
            _title = null;
        }
    }

    void refreshReceiverField() {
        if (_receiver_field != null && _receiver != null) {
            _receiver_field.setText(_receiver);
            _receiver = null;
        }
    }

    void refreshContentField() {
        if (_content_field != null && _content != null) {
            _content_field.setText(_content);
            if (_content.length() > 0) {
                Selection.setSelection(_content_field.getText(), _content.length());
            }
            _content = null;
        }
    }

    public void onPageRefresh() {
        refreshTitleField();
        refreshContentField();
        refreshReceiverField();
    }

    void initial() {
        _title_field = (EditText) findViewById(R.id.SendMail_TitleField);
        _title_field.setOnFocusChangeListener(this);
        _title_field_background = (TextView) findViewById(R.id.SendMail_TitleFieldBackground);
        
        _receiver_field = (EditText) findViewById(R.id.SendMail_ReceiverField);
        _receiver_field.setOnFocusChangeListener(this);
        _receiver_field_background = (TextView) findViewById(R.id.SendMail_ReceiverFieldBackground);
        
        _content_field = (EditText) findViewById(R.id.SendMailDialog_EditField);
        
        _post_button = (Button) findViewById(R.id.SendMailDialog_Post);
        _post_button.setOnClickListener(this);
        
        _symbol_button = (Button) findViewById(R.id.SendMailDialog_Symbol);
        _symbol_button.setOnClickListener(this);
        
        _hide_title_button = (Button) findViewById(R.id.SendMailDialog_Cancel);
        _hide_title_button.setOnClickListener(this);
        
        _paint_color_button = (Button) findViewById(R.id.ArticlePostDialog_Color);
        _paint_color_button.setOnClickListener(this);
        
        findViewById(R.id.SendMailDialog_change).setOnClickListener(this);
        _title_block = findViewById(R.id.SendMail_TitleBlock);
        refresh();
    }

    public void clear() {
        if (_receiver_field != null) {
            _receiver_field.setText("");
        }
        if (_title_field != null) {
            _title_field.setText("");
        }
        if (_content_field != null) {
            _content_field.setText("");
        }
        _listener = null;
    }

    public void setPostTitle(String aTitle) {
        _title = aTitle;
        refreshTitleField();
    }

    public void setPostContent(String aContent) {
        _content = aContent;
        refreshContentField();
    }

    public void onClick(View view) {
        if (view == _post_button) {
            if (_listener != null) {
                String receiver = _receiver_field.getText().toString().replace("\n", "");
                String title = _title_field.getText().toString().replace("\n", "");
                String content = _content_field.getText().toString();
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
                        _listener.onSendMailDialogSendButtonClicked(SendMailPage.this, send_receiver, send_title, send_content);
                        getNavigationController().popViewController();
                        clear();
                    }
                }).show();
            }
        } else if (view == _symbol_button) {

            // 表情符號
            final String[] items = UserSettings.getArticleExpressions();
            Dialog_InsertExpression.createDialog().setTitle("表情符號").addItems(items).setListener(new Dialog_InsertExpression_Listener() {
                @Override
                public void onListDialogItemClicked(Dialog_InsertExpression paramASListDialog, int index, String aTitle) {
                    String symbol = items[index];
                    _content_field.getEditableText().insert(_content_field.getSelectionStart(), symbol);
                }

                @Override
                public void onListDialogSettingClicked() {
                    // 將當前內容存檔, pushView會讓當前頁面消失
                    setRecover();
                    getNavigationController().pushViewController(new ArticleExpressionListPage());
                }
            }).scheduleDismissOnPageDisappear(this).show();
        } else if (view == _hide_title_button) {
            onInsertSymbolButtonClicked();
        } else if (view == _paint_color_button) {
            Dialog_PaintColor dialog = new Dialog_PaintColor();
            dialog.setListener(this);
            dialog.show();
        } else if (view.getId() == R.id.SendMailDialog_change) {
            changeViewMode();
        }
    }

    public void setRecover() {
        recover = true;
        saveTempArticle(8);
    }

    public void refresh() {
        if (_title_block_hidden) {
            _title_block.setVisibility(View.GONE);
        } else {
            _title_block.setVisibility(View.VISIBLE);
        }
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (v == _receiver_field) {
            if (hasFocus) {
                _receiver_field.setSingleLine(false);
                _receiver_field_background.setTextColor(0);
                _receiver_field.setTextColor(-1);
            } else {
                _receiver_field.setSingleLine(true);
                _receiver_field.setTextColor(0);
                _receiver_field_background.setTextColor(-1);
                _receiver_field_background.setText(_receiver_field.getText().toString());
            }
        }
        if (v != _title_field) {
            return;
        }
        if (hasFocus) {
            _title_field.setSingleLine(false);
            _title_field.setTextColor(-1);
            _title_field_background.setTextColor(0);
            return;
        }
        _title_field.setSingleLine(true);
        _title_field.setTextColor(0);
        _title_field_background.setTextColor(-1);
        _title_field_background.setText(_title_field.getText().toString());
    }

    void changeViewMode() {
        _title_block_hidden = !_title_block_hidden;
        refresh();
    }

    void onInsertSymbolButtonClicked() {
        Dialog_InsertSymbol dialog = new Dialog_InsertSymbol();
        dialog.setListener(this);
        dialog.show();
    }

    public void onSymbolDialogDismissWithSymbol(String symbol) {
        _content_field.getEditableText().insert(_content_field.getSelectionStart(), symbol);
    }

    public void setReceiver(String a_receiver) {
        _receiver = a_receiver;
        refreshReceiverField();
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    @Override
    public void onPaintColorDone(String str) {
        _content_field.getEditableText().insert(_content_field.getSelectionStart(), str);
    }

    // 讀取暫存檔
    private void loadTempArticle(int index) {
        ArticleTemp article_temp = new ArticleTempStore(getContext()).articles.get(index);
        _receiver_field.setText(article_temp.header);
        _title_field.setText(article_temp.title);
        _content_field.setText(article_temp.content);
    }

    // 儲存暫存檔
    private void saveTempArticle(int index) {
        ArticleTempStore store = new ArticleTempStore(getContext());
        ArticleTemp article_temp = store.articles.get(index);
        // 收信者
        article_temp.header = _receiver_field.getText().toString();
        // 標題
        article_temp.title = _title_field.getText().toString();
        // 內文
        article_temp.content = _content_field.getText().toString();

        // 存檔
        store.store();
    }
}
