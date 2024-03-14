package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
import android.text.Selection;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.DataModels.ArticleTemp;
import com.kota.Bahamut.DataModels.ArticleTempStore;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol_Listener;
import com.kota.Bahamut.Dialogs.Dialog_PaintColor;
import com.kota.Bahamut.Dialogs.Dialog_PaintColor_Listener;
import com.kota.Bahamut.Dialogs.Dialog_PostArticle;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.TelnetUI.TelnetPage;

import java.util.Objects;

public class PostArticlePage extends TelnetPage implements View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnFocusChangeListener, Dialog_InsertSymbol_Listener, Dialog_PaintColor_Listener {
    private String _article_number = null;
    private BoardPage _board_page = null;
    private String _content = null;
    private EditText _content_field = null;
    private String _edit_format = null;
    private boolean _header_hidden = false;
    private int _header_selected = 0;
    private Spinner _header_selector = null;
    private PostArticlePage_Listener _listener = null;
    private OperationMode _operation_mode = OperationMode.New;
    private String _ori_title = null;
    private Button _post_button = null;
    private Button _symbol_button = null;
    private Button _insert_symbol_button = null;
    private Button _paint_color_button = null;
    private View _title_block = null;
    private EditText _title_field = null;
    private TextView _title_field_background = null;
    public boolean recover = false;


    public enum OperationMode {
        New,
        Reply,
        Edit
    }

    public void setListener(PostArticlePage_Listener aListener) {
        _listener = aListener;
    }

    public int getPageLayout() {
        return R.layout.post_article_page;
    }

    public int getPageType() {
        return BahamutPage.BAHAMUT_POST_ARTICLE;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        initial();
    }

    public void onPageDidDisappear() {
        _header_selector = null;
        _title_field = null;
        _title_field_background = null;
        _content_field = null;
        _title_block = null;
        _symbol_button = null;
        _post_button = null;
        _insert_symbol_button = null;
        _paint_color_button = null;
        super.onPageDidDisappear();
    }

    private void refreshTitleField() {
        if (_title_field != null && _ori_title != null) {
            _title_field.setText(_ori_title);
            if (_ori_title.length() > 0) {
                Selection.setSelection(_title_field.getText(), 1);
            }
        }
    }

    private void refreshContentField() {
        if (_content_field != null && _content != null) {
            _content_field.setText(_content);
            if (_content.length() > 0) {
                Selection.setSelection(_content_field.getText(), _content.length());
            }
            _content = null;
        }
    }

    private void refreshHeaderSelector() {
        if (_header_selector == null) {
            return;
        }
        if (_header_hidden) {
            _header_selector.setVisibility(View.GONE);
        } else {
            _header_selector.setVisibility(View.VISIBLE);
        }
    }

    public void onPageRefresh() {
        refreshTitleField();
        refreshContentField();
        refreshHeaderSelector();
    }

    @SuppressLint("ResourceType")
    private void initial() {
        _title_field = (EditText) findViewById(R.id.ArticlePostDialog_TitleField);
        _title_field_background = (TextView) findViewById(R.id.ArticlePostDialog_TitleFieldBackground);
        _content_field = (EditText) findViewById(R.id.ArticlePostDialog_EditField);

        _post_button = (Button) findViewById(R.id.ArticlePostDialog_Post);
        _post_button.setOnClickListener(this);

        _symbol_button = (Button) findViewById(R.id.ArticlePostDialog_Symbol);
        _symbol_button.setOnClickListener(this);

        _insert_symbol_button = (Button) findViewById(R.id.ArticlePostDialog_Cancel);
        _insert_symbol_button.setOnClickListener(this);

        _paint_color_button = (Button) findViewById(R.id.ArticlePostDialog_Color);
        _paint_color_button.setOnClickListener(this);

        findViewById(R.id.ArticlePostDialog_File).setOnClickListener(this);
        _header_selector = (Spinner) findViewById(R.id.Post_headerSelector);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, UserSettings.getArticleHeaders());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _header_selector.setAdapter(adapter);
        _header_selector.setOnItemSelectedListener(this);
        _title_block = findViewById(R.id.Post_TitleBlock);
        _title_block.requestFocus();
    }

    public void clear() {
        if (_title_field != null) {
            _title_field.setText("");
        }
        if (_content_field != null) {
            _content_field.setText("");
        }
        _listener = null;
        recover = false;
    }

    // 引言過多失敗存檔
    public void setRecover() {
        recover = true;
        saveTempArticle(9);
    }

    // 設定文章標題
    public void setPostTitle(String aTitle) {
        _ori_title = aTitle;
        refreshTitleField();
    }

    // 設定內文
    public void setPostContent(String aContent) {
        _content = aContent;
        refreshContentField();
    }

    public void onClick(View view) {
        if (view == _post_button) {
            if (_listener != null) {
                String title = UserSettings.getArticleHeader(_header_selected) + _title_field.getText().toString().replace("\n", "");
                String content = _content_field.getText().toString();
                String err_msg = null;
                if (title.length() == 0 && content.length() == 0) {
                    err_msg = "標題與內文不可為空";
                } else if (title.length() == 0) {
                    err_msg = "標題不可為空";
                } else if (content.length() == 0) {
                    err_msg = "內文不可為空";
                }
                if (err_msg == null) {
                    post(title, content);
                } else {
                    ASAlertDialog.createDialog().setTitle("錯誤").setMessage(err_msg).addButton(getContextString(R.string.sure)).show();
                }
            }
        } else if (view == _symbol_button) {
            final String[] items = UserSettings.getSymbols();
            ASListDialog.createDialog().setItemTextSize(0).setTitle("表情符號").addItems(items).setListener(new ASListDialogItemClickListener() {
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    String symbol = items[index];
                    PostArticlePage.this._content_field.getEditableText().insert(PostArticlePage.this._content_field.getSelectionStart(), symbol);
                }

                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }
            }).scheduleDismissOnPageDisappear(this).show();
        } else if (view == _insert_symbol_button) {
            Dialog_InsertSymbol dialog = new Dialog_InsertSymbol();
            dialog.setListener(this);
            dialog.show();
        } else if (view == _paint_color_button) {
            Dialog_PaintColor dialog = new Dialog_PaintColor();
            dialog.setListener(this);
            dialog.show();
        } else if (view.getId() == R.id.ArticlePostDialog_File) {
            onFileClicked();
        }
    }

    // 發文
    private void post(String title, String content) {
        final String send_title;

        if (_article_number == null || !title.equals(_ori_title)) {
            send_title = title;
        } else {
            send_title = null;
        }
        final String send_content = content;
        // 有來源文章編號, 可能為Reply, edit
        if (_article_number != null) {
            if (_operation_mode == OperationMode.Reply) {
                // 回覆: 有註記回文
                Dialog_PostArticle dialog = new Dialog_PostArticle(1);
                dialog.setListener((aTarget, aSign) -> {
                    if (PostArticlePage.this._listener != null) {
                        PostArticlePage.this._listener.onPostDialogSendButtonClicked(PostArticlePage.this, send_title, send_content, aTarget, PostArticlePage.this._article_number, aSign, recover);
                    }
                    // 回應到作者信箱
                    if (aTarget.equals("M")) {
                        closeArticle();
                    }
                });
                dialog.show();

            } else {
                // 修改: 沒有註記回文
                ASAlertDialog.createDialog().addButton(getContextString(R.string.cancel)).addButton("送出").setTitle("確認").setMessage("您是否確定要編輯此文章?").setListener((aDialog, index) -> {
                    if (index == 1) {
                        if (PostArticlePage.this._listener != null) {
                            PostArticlePage.this._listener.onPostDialogEditButtonClicked(PostArticlePage.this, PostArticlePage.this._article_number, send_title, PostArticlePage.this.getEditContent());
                        }
                    }
                    closeArticle();
                }).show();
            }
        }else {
            // 新增文章
            Dialog_PostArticle dialog2 = new Dialog_PostArticle(0);
            dialog2.setListener((aTarget, aSign) -> {
                if (PostArticlePage.this._listener != null) {
                    PostArticlePage.this._listener.onPostDialogSendButtonClicked(PostArticlePage.this, send_title, send_content, null, null, aSign, recover);
                }
            });
            dialog2.show();
        }
    }
    public void closeArticle() {
        // 引言過多情況下放棄, 要補上放棄存檔command
        if (recover) {
            byte[] data = TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.CTRL_X)
                    .pushString("a\n")
                    .build();
            TelnetClient.getClient().sendDataToServer(data);
        }
        clear();
        getNavigationController().popToViewController(_board_page);
        PageContainer.getInstance().cleanPostArticlePage();
    }

    public void onItemSelected(AdapterView<?> adapterView, View aView, int index, long id) {
        _header_selected = index;
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void refresh() {
        _title_block.setVisibility(View.VISIBLE);
    }

    public void onFocusChange(View v, boolean hasFocus) {
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

    public void setHeaderHidden(boolean hidden) {
        _header_hidden = hidden;
        refreshHeaderSelector();
    }

    public void onFileClicked() {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.string._article))
                .addItem(getContextString(R.string.load_temp))
                .addItem(getContextString(R.string.save_to_temp))
                .setListener(new ASListDialogItemClickListener() {
                    public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                        return false;
                    }
        
                    public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                        if (Objects.equals(aTitle, getContextString(R.string.load_temp))) {
                            PostArticlePage.this.ontLoadArticleFromTempButtonClicked();
                        } else if (Objects.equals(aTitle, getContextString(R.string.save_to_temp))) {
                            PostArticlePage.this.ontSaveArticleToTempButtonClicked();
                        }
                    }
                }).show();
    }

    private void ontLoadArticleFromTempButtonClicked() {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.string._article))
                .addItem("讀取上次送出文章")
                .addItem(getContextString(R.string.load_temp)+".1")
                .addItem(getContextString(R.string.load_temp)+".2")
                .addItem(getContextString(R.string.load_temp)+".3")
                .addItem(getContextString(R.string.load_temp)+".4")
                .addItem(getContextString(R.string.load_temp)+".5")
                .setListener(new ASListDialogItemClickListener() {
                    public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                        return false;
                    }
        
                    public void onListDialogItemClicked(ASListDialog aDialog, final int index, String aTitle) {
                        if (index == 0) {
                            ASAlertDialog.createDialog()
                                    .setTitle(getContextString(R.string.load_temp))
                                    .setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?")
                                    .addButton(getContextString(R.string.cancel))
                                    .addButton(getContextString(R.string.sure))
                                    .setListener((aDialog12, button_index) -> PostArticlePage.this.loadTempArticle(9)).show();
                        } else {
                            ASAlertDialog.createDialog()
                                    .setTitle(getContextString(R.string.load_temp))
                                    .setMessage("您是否確定要以暫存檔." + index + "的內容取代您現在編輯的內容?")
                                    .addButton(getContextString(R.string.cancel))
                                    .addButton(getContextString(R.string.sure))
                                    .setListener((aDialog1, button_index) -> {
                                if (button_index == 1) {
                                    PostArticlePage.this.loadTempArticle(index - 1);
                                }
                            }).show();
                        }
                    }
                }).show();
    }

    private void loadTempArticle(int index) {
        ArticleTemp article_temp = new ArticleTempStore(getContext()).articles.get(index);
        _header_selector.setSelection(UserSettings.getIndexOfHeader(article_temp.header));
        _title_field.setText(article_temp.title);
        _content_field.setText(article_temp.content);
    }

    private void ontSaveArticleToTempButtonClicked() {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.string._article))
                .addItem(getContextString(R.string.save_to_temp)+".1")
                .addItem(getContextString(R.string.save_to_temp)+".2")
                .addItem(getContextString(R.string.save_to_temp)+".3")
                .addItem(getContextString(R.string.save_to_temp)+".4")
                .addItem(getContextString(R.string.save_to_temp)+".5")
                .setListener(new ASListDialogItemClickListener() {
                    public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                        return false;
                    }
        
                    public void onListDialogItemClicked(ASListDialog aDialog, final int index, String aTitle) {
                        ASAlertDialog.createDialog()
                                .setTitle(getContextString(R.string.load_temp))
                                .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                                .addButton(getContextString(R.string.cancel))
                                .addButton(getContextString(R.string.sure))
                                .setListener((aDialog1, button_index) -> {
                                    if (button_index == 1) {
                                        PostArticlePage.this.saveTempArticle(index);
                                    }
                                }).show();
                    }
                }).show();
    }

    // 儲存暫存檔
    private void saveTempArticle(int index) {
        ArticleTempStore store = new ArticleTempStore(getContext());
        ArticleTemp article_temp = store.articles.get(index);
        // 類別
        article_temp.header = "";
        if (_header_selector.getSelectedItemPosition() > 0) {
            article_temp.header = UserSettings.getArticleHeader(_header_selector.getSelectedItemPosition());
        }
        // 標題
        article_temp.title = _title_field.getText().toString();
        // 內文
        article_temp.content = _content_field.getText().toString();

        // 存檔
        store.store();
        if (index < 9) {
            ASAlertDialog.createDialog().setTitle(getContextString(R.string._save)).setMessage("存檔完成").addButton(getContextString(R.string.sure)).show();
        }
    }

    // 帶回符號
    public void onSymbolDialogDismissWithSymbol(String symbol) {
        if (_content_field != null) {
            _content_field.getEditableText().insert(_content_field.getSelectionStart(), symbol);
        }
    }

    // 取回上色內容
    @Override
    public void onPaintColorDone(String str) {
        if (_content_field != null) {
            _content_field.getEditableText().insert(_content_field.getSelectionStart(), str);
        }
    }

    public void setOperationMode(OperationMode aMode) {
        _operation_mode = aMode;
    }

    public OperationMode getOperationMode() {
        return _operation_mode;
    }

    public void setArticleNumber(String aNumber) {
        _article_number = aNumber;
    }

    public void setEditFormat(String aFormat) {
        _edit_format = aFormat;
    }

    public String getEditContent() {
        if (_edit_format == null) {
            return null;
        }
        return String.format(_edit_format, _title_field.getText().toString(), _content_field.getText().toString());
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    public void setBoardPage(BoardPage aBoardPage) {
        _board_page = aBoardPage;
    }

    protected boolean onBackPressed() {
        if (_title_field.getText().toString().length() == 0 && _content_field.getText().toString().length() == 0) {
            return super.onBackPressed();
        }
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string._article))
                .setMessage(getContextString(R.string.give_up_post_article))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string._giveUp))
                .addButton(getContextString(R.string._save)).setListener((aDialog, index) -> {
            if (index == 1) {
                closeArticle();
            } else if (index == 2) {
                PostArticlePage.this.ontSaveArticleToTempAndLeaveButtonClicked();
            }
        }).show();
        return true;
    }

    private void ontSaveArticleToTempAndLeaveButtonClicked() {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.string._article))
                .addItem(getContextString(R.string.save_to_temp)+".1")
                .addItem(getContextString(R.string.save_to_temp)+".2")
                .addItem(getContextString(R.string.save_to_temp)+".3")
                .addItem(getContextString(R.string.save_to_temp)+".4")
                .addItem(getContextString(R.string.save_to_temp)+".5")
                .setListener(new ASListDialogItemClickListener() {
                    public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                        return false;
                    }
        
                    public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                        ASAlertDialog.createDialog()
                                .setTitle(getContextString(R.string.load_temp))
                                .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                                .addButton(getContextString(R.string.cancel))
                                .addButton(getContextString(R.string.sure))
                                .setListener((aDialog1, button_index) -> {
                                    if (button_index == 0) {
                                        PostArticlePage.this.onBackPressed();
                                    } else {
                                        PostArticlePage.this.saveTempArticle(index);
                                        closeArticle();
                                    }
                                }).show();
                    }
                }).show();
    }
}
