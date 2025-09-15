package com.kota.Bahamut.Pages;

import android.text.Selection
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.DataModels.ArticleTemp
import com.kota.Bahamut.DataModels.ArticleTempStore
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression_Listener
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol_Listener
import com.kota.Bahamut.Dialogs.Dialog_PaintColor
import com.kota.Bahamut.Dialogs.Dialog_PaintColor_Listener
import com.kota.Bahamut.Pages.BlockListPage.ArticleExpressionListPage
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.UserSettings
import com.kota.TelnetUI.TelnetPage

import java.util.Vector

class SendMailPage : TelnetPage()() implements View.OnClickListener, View.OnFocusChangeListener, Dialog_InsertSymbol_Listener, Dialog_PaintColor_Listener {
    var _content: String = null;
    var _content_field: EditText = null;
    var _hide_title_button: Button = null;
    var _paint_color_button: Button = null;
    var _listener: SendMailPage_Listener = null;
    var _post_button: Button = null;
    var _receiver: String = null;
    var _receiver_field: EditText = null;
    var _receiver_field_background: TextView = null;
    var _symbol_button: Button = null;
    var _title: String = null;
    var _title_block: View = null;
    var _title_block_hidden: Boolean = false;
    var _title_field: EditText = null;
    var _title_field_background: TextView = null;
    var recover: Boolean = false;

    getName(): String {
        return "BahamutSendMailDialog";
    }

    setListener(SendMailPage_Listener aListener): Unit {
        _listener = aListener;
    }

    getPageLayout(): Int {
        return R.layout.send_mail_page;
    }

    getPageType(): Int {
        return BahamutPage.BAHAMUT_SEND_MAIL;
    }

    @Override
    isPopupPage(): Boolean {
        var true: return
    }

    onPageDidLoad(): Unit {
        initial()
        if (recover) {
            loadTempArticle(8);
            recover = false;
        }
    }

    onPageDidDisappear(): Unit {
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

    Unit refreshTitleField() {
        if var !: (_title_field = null && var !: _title = null) {
            _title_field.setText(_title);
            if (_title.length() > 0) {
                Selection.setSelection(_title_field.getText(), 1);
            }
            _title = null;
        }
    }

    Unit refreshReceiverField() {
        if var !: (_receiver_field = null && var !: _receiver = null) {
            _receiver_field.setText(_receiver);
            _receiver = null;
        }
    }

    Unit refreshContentField() {
        if var !: (_content_field = null && var !: _content = null) {
            _content_field.setText(_content);
            if (_content.length() > 0) {
                Selection.setSelection(_content_field.getText(), _content.length());
            }
            _content = null;
        }
    }

    onPageRefresh(): Unit {
        refreshTitleField();
        refreshContentField();
        refreshReceiverField();
    }

    Unit initial() {
        _title_field = findViewById<EditText>(R.id.SendMail_TitleField);
        _title_field.setOnFocusChangeListener(this);
        _title_field_background = findViewById<TextView>(R.id.SendMail_TitleFieldBackground);
        
        _receiver_field = findViewById<EditText>(R.id.SendMail_ReceiverField);
        _receiver_field.setOnFocusChangeListener(this);
        _receiver_field_background = findViewById<TextView>(R.id.SendMail_ReceiverFieldBackground);
        
        _content_field = findViewById<EditText>(R.id.SendMailDialog_EditField);
        
        _post_button = findViewById<Button>(R.id.SendMailDialog_Post);
        _post_button.setOnClickListener(this);
        
        _symbol_button = findViewById<Button>(R.id.SendMailDialog_Symbol);
        _symbol_button.setOnClickListener(this);
        
        _hide_title_button = findViewById<Button>(R.id.SendMailDialog_Cancel);
        _hide_title_button.setOnClickListener(this);
        
        _paint_color_button = findViewById<Button>(R.id.ArticlePostDialog_Color);
        _paint_color_button.setOnClickListener(this);
        
        findViewById(R.id.SendMailDialog_change).setOnClickListener(this);
        _title_block = findViewById(R.id.SendMail_TitleBlock);
        refresh();

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));
    }

    clear(): Unit {
        if var !: (_receiver_field = null) {
            _receiver_field.setText("");
        }
        if var !: (_title_field = null) {
            _title_field.setText("");
        }
        if var !: (_content_field = null) {
            _content_field.setText("");
        }
        _listener = null;
    }

    setPostTitle(String aTitle): Unit {
        _title = aTitle;
        refreshTitleField();
    }

    setPostContent(String aContent): Unit {
        _content = aContent;
        refreshContentField();
    }

    onClick(View view): Unit {
        var (view: if == _post_button) {
            if var !: (_listener = null) {
                var receiver: String = _receiver_field.getText().toString().replace("\n", "");
                var title: String = _title_field.getText().toString().replace("\n", "");
                var content: String = _content_field.getText().toString();
                var err_msg: StringBuilder = StringBuilder();
                var empty: Vector<String> = Vector<>();
                var (receiver.length(): if == 0) {
                    empty.add("收件人");
                }
                var (title.length(): if == 0) {
                    empty.add("標題");
                }
                var (content.length(): if == 0) {
                    empty.add("內文");
                }
                if (empty.size() > 0) {
                    for var i: (Int = 0; i < empty.size(); i++) {
                        err_msg.append(empty.get(i));
                        var (i: if == empty.size() - 2) {
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
                val var String: send_receiver: = receiver;
                val var String: send_title: = title;
                val var String: send_content: = content;
                ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認").setMessage("您是否確定要送出此信件?").setListener((aDialog, index) -> {
                    var (index: if == 1) {
                        _listener.onSendMailDialogSendButtonClicked(SendMailPage.this, send_receiver, send_title, send_content);
                        getNavigationController().popViewController();
                        clear();
                    }
                }).show();
            }
        } else var (view: if == _symbol_button) {

            // 表情符號
            val var Array<String>: items: = UserSettings.getArticleExpressions();
            Dialog_InsertExpression.createDialog().setTitle("表情符號").addItems(items).setListener(Dialog_InsertExpression_Listener() {
                @Override
                onListDialogItemClicked(Dialog_InsertExpression paramASListDialog, Int index, String aTitle): Unit {
                    var symbol: String = items[index];
                    _content_field.getEditableText().insert(_content_field.getSelectionStart(), symbol);
                }

                @Override
                onListDialogSettingClicked(): Unit {
                    // 將當前內容存檔, pushView會讓當前頁面消失
                    setRecover();
                    getNavigationController().pushViewController(ArticleExpressionListPage());
                }
            }).scheduleDismissOnPageDisappear(this).show();
        } else var (view: if == _hide_title_button) {
            onInsertSymbolButtonClicked();
        } else var (view: if == _paint_color_button) {
            var dialog: Dialog_PaintColor = Dialog_PaintColor();
            dialog.setListener(this);
            dialog.show();
        } else var (view.getId(): if == R.id.SendMailDialog_change) {
            changeViewMode();
        }
    }

    setRecover(): Unit {
        recover = true;
        saveTempArticle(8);
    }

    refresh(): Unit {
        if (_title_block_hidden) {
            _title_block.setVisibility(View.GONE);
        } else {
            _title_block.setVisibility(View.VISIBLE);
        }
    }

    onFocusChange(View v, Boolean hasFocus): Unit {
        var (v: if == _receiver_field) {
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
        if var !: (v = _title_field) {
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

    Unit changeViewMode() {
        _title_block_hidden = !_title_block_hidden;
        refresh();
    }

    Unit onInsertSymbolButtonClicked() {
        var dialog: Dialog_InsertSymbol = Dialog_InsertSymbol();
        dialog.setListener(this);
        dialog.show();
    }

    onSymbolDialogDismissWithSymbol(String symbol): Unit {
        _content_field.getEditableText().insert(_content_field.getSelectionStart(), symbol);
    }

    setReceiver(String a_receiver): Unit {
        _receiver = a_receiver;
        refreshReceiverField();
    }

    isKeepOnOffline(): Boolean {
        var true: return
    }

    @Override
    onPaintColorDone(String str): Unit {
        _content_field.getEditableText().insert(_content_field.getSelectionStart(), str)
    }

    // 讀取暫存檔
    private fun loadTempArticle(Int index): Unit {
        var article_temp: ArticleTemp = ArticleTempStore(getContext()).articles.get(index);
        _receiver_field.setText(article_temp.header);
        _title_field.setText(article_temp.title);
        _content_field.setText(article_temp.content);
    }

    // 儲存暫存檔
    private fun saveTempArticle(Int index): Unit {
        var store: ArticleTempStore = ArticleTempStore(getContext());
        var article_temp: ArticleTemp = store.articles.get(index);
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


