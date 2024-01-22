package com.kota.Bahamut.Pages;

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
import com.kota.Bahamut.DataModels.ArticleTemp;
import com.kota.Bahamut.DataModels.ArticleTempStore;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol_Listener;
import com.kota.Bahamut.Dialogs.Dialog_PostArticle;
import com.kota.Bahamut.R;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;

import java.util.Objects;

public class PostArticlePage extends TelnetPage implements View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnFocusChangeListener, Dialog_InsertSymbol_Listener {
    /* access modifiers changed from: private */
    public String _article_number = null;
    /* access modifiers changed from: private */
    public BoardPage _board_page = null;
    private String _content = null;
    /* access modifiers changed from: private */
    public EditText _content_field = null;
    private String _edit_format = null;
    private boolean _header_hidden = false;
    private int _header_selected = 0;
    private Spinner _header_selector = null;
    private Button _hide_title_button = null;
    /* access modifiers changed from: private */
    public PostArticlePage_Listener _listener = null;
    private OperationMode _operation_mode = OperationMode.New;
    private String _ori_title = null;
    private Button _post_button = null;
    UserSettings _settings;
    private Button _symbol_button = null;
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
        this._listener = aListener;
    }

    public int getPageLayout() {
        return R.layout.post_article_page;
    }

    public int getPageType() {
        return 16;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        this._settings = new UserSettings(getContext());
        initial();
    }

    public void onPageWillAppear() {
        this._content_field.requestFocus();
    }

    public void onPageDidDisappear() {
        this._header_selector = null;
        this._title_field = null;
        this._title_field_background = null;
        this._content_field = null;
        this._title_block = null;
        this._symbol_button = null;
        this._post_button = null;
        this._hide_title_button = null;
        super.onPageDidDisappear();
    }

    private void refreshTitleField() {
        if (this._title_field != null && this._ori_title != null) {
            this._title_field.setText(this._ori_title);
            if (this._ori_title.length() > 0) {
                Selection.setSelection(this._title_field.getText(), 1);
            }
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

    private void refreshHeaderSelector() {
        if (this._header_selector == null) {
            return;
        }
        if (this._header_hidden) {
            this._header_selector.setVisibility(View.GONE);
        } else {
            this._header_selector.setVisibility(View.VISIBLE);
        }
    }

    public void onPageRefresh() {
        refreshTitleField();
        refreshContentField();
        refreshHeaderSelector();
    }

    @SuppressLint("ResourceType")
    private void initial() {
        this._title_field = (EditText) findViewById(R.id.ArticlePostDialog_TitleField);
        this._title_field.setOnFocusChangeListener(this);
        this._title_field_background = (TextView) findViewById(R.id.ArticlePostDialog_TitleFieldBackground);
        this._content_field = (EditText) findViewById(R.id.ArticlePostDialog_EditField);
        this._post_button = (Button) findViewById(R.id.ArticlePostDialog_Post);
        this._post_button.setOnClickListener(this);
        this._symbol_button = (Button) findViewById(R.id.ArticlePostDialog_Symbol);
        this._symbol_button.setOnClickListener(this);
        this._hide_title_button = (Button) findViewById(R.id.ArticlePostDialog_Cancel);
        this._hide_title_button.setOnClickListener(this);
        findViewById(R.id.ArticlePostDialog_File).setOnClickListener(this);
        this._header_selector = (Spinner) findViewById(R.id.Post_HeaderSelector);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, this._settings.getArticleHeaders());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this._header_selector.setAdapter(adapter);
        this._header_selector.setOnItemSelectedListener(this);
        this._title_block = findViewById(R.id.Post_TitleBlock);
        if (this.recover) {
            loadTempArticle(10);
            this.recover = false;
        }
    }

    public void clear() {
        if (this._title_field != null) {
            this._title_field.setText("");
        }
        if (this._content_field != null) {
            this._content_field.setText("");
        }
        this._listener = null;
    }

    public void setPostTitle(String aTitle) {
        this._ori_title = aTitle;
        refreshTitleField();
    }

    public void setPostContent(String aContent) {
        this._content = aContent;
        refreshContentField();
    }

    public void onClick(View view) {
        if (view == this._post_button) {
            if (this._listener != null) {
                String title = this._settings.getArticleHeader(this._header_selected) + this._title_field.getText().toString().replace("\n", "");
                String content = this._content_field.getText().toString();
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
                    ASAlertDialog.createDialog().setTitle("錯誤").setMessage(err_msg).addButton("確定").show();
                }
            }
        } else if (view == this._symbol_button) {
            final String[] items = this._settings.getSymbols();
            ASListDialog.createDialog().setItemTextSize(0).setTitle("表情符號").addItems(items).setListener(new ASListDialogItemClickListener() {
                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    String symbol = items[index];
                    PostArticlePage.this._content_field.getEditableText().insert(PostArticlePage.this._content_field.getSelectionStart(), symbol);
                }

                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return false;
                }
            }).scheduleDismissOnPageDisappear(this).show();
        } else if (view == this._hide_title_button) {
            onInsertSymbolbuttonClicked();
        } else if (view.getId() == R.id.ArticlePostDialog_File) {
            onFileClicked();
        }
    }

    /* access modifiers changed from: package-private */
    public void post(String title, String content) {
        final String send_title;
        saveTempArticle(10);
        if (this._article_number == null || !title.equals(this._ori_title)) {
            send_title = title;
        } else {
            send_title = null;
        }
        final String send_content = content;
        if (this._article_number != null && this._operation_mode == OperationMode.Reply) {
            Dialog_PostArticle dialog = new Dialog_PostArticle(1);
            dialog.setListener((aTarget, aSign) -> {
                if (PostArticlePage.this._listener != null) {
                    PostArticlePage.this._listener.onPostDialogSendButtonClicked(PostArticlePage.this, send_title, send_content, aTarget, PostArticlePage.this._article_number, aSign);
                }
                closeArticle();
            });
            dialog.show();
        } else if (this._article_number != null) {
            ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認").setMessage("您是否確定要編輯此文章?").setListener((aDialog, index) -> {
                if (index == 1) {
                    if (PostArticlePage.this._listener != null) {
                        PostArticlePage.this._listener.onPostDialogEditButtonClicked(PostArticlePage.this, PostArticlePage.this._article_number, send_title, PostArticlePage.this.getEditContent());
                    }
                }
                closeArticle();
            }).show();
        } else {
            Dialog_PostArticle dialog2 = new Dialog_PostArticle(0);
            dialog2.setListener((aTarget, aSign) -> {
                if (PostArticlePage.this._listener != null) {
                    PostArticlePage.this._listener.onPostDialogSendButtonClicked(PostArticlePage.this, send_title, send_content, null, null, aSign);
                }
                closeArticle();
            });
            dialog2.show();
        }
    }
    public void closeArticle() {
        this.clear();
        this.getNavigationController().popToViewController(this._board_page);
    }

    public void onItemSelected(AdapterView<?> adapterView, View aView, int index, long id) {
        this._header_selected = index;
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void refresh() {
        this._title_block.setVisibility(View.VISIBLE);
    }

    public void onFocusChange(View v, boolean hasFocus) {
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

    public void setHeaderHidden(boolean hidden) {
        this._header_hidden = hidden;
        refreshHeaderSelector();
    }

    public void onFileClicked() {
        ASListDialog.createDialog().setTitle("文章").addItem("讀取暫存檔").addItem("存入暫存檔").setListener(new ASListDialogItemClickListener() {
            public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                return false;
            }

            public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                if (Objects.equals(aTitle, "讀取暫存檔")) {
                    PostArticlePage.this.ontLoadArticleFromTempButtonClicked();
                } else if (Objects.equals(aTitle, "存入暫存檔")) {
                    PostArticlePage.this.ontSaveArticleToTempButtonClicked();
                }
            }
        }).show();
    }

    /* access modifiers changed from: private */
    public void ontLoadArticleFromTempButtonClicked() {
        ASListDialog.createDialog().setTitle("文章").addItem("讀取上次送出文章").addItem("讀取暫存檔.1").addItem("讀取暫存檔.2").addItem("讀取暫存檔.3").addItem("讀取暫存檔.4").addItem("讀取暫存檔.5").setListener(new ASListDialogItemClickListener() {
            public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                return false;
            }

            public void onListDialogItemClicked(ASListDialog aDialog, final int index, String aTitle) {
                if (index == 0) {
                    ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?").setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?").addButton("取消").addButton("確定").setListener((aDialog12, button_index) -> PostArticlePage.this.loadTempArticle(10)).show();
                } else {
                    ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以暫存檔." + index + "的內容取代您現在編輯的內容?").addButton("取消").addButton("確定").setListener((aDialog1, button_index) -> {
                        if (button_index == 1) {
                            PostArticlePage.this.loadTempArticle(index - 1);
                        }
                    }).show();
                }
            }
        }).show();
    }

    /* access modifiers changed from: private */
    public void loadTempArticle(int index) {
        ArticleTemp article_temp = new ArticleTempStore(getContext()).articles.get(index);
        this._header_selector.setSelection(this._settings.getIndexOfHeader(article_temp.header));
        this._title_field.setText(article_temp.title);
        this._content_field.setText(article_temp.content);
    }

    /* access modifiers changed from: private */
    public void ontSaveArticleToTempButtonClicked() {
        ASListDialog.createDialog().setTitle("文章").addItem("存入暫存檔.1").addItem("存入暫存檔.2").addItem("存入暫存檔.3").addItem("存入暫存檔.4").addItem("存入暫存檔.5").setListener(new ASListDialogItemClickListener() {
            public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                return false;
            }

            public void onListDialogItemClicked(ASListDialog aDialog, final int index, String aTitle) {
                ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?").addButton("取消").addButton("確定").setListener((aDialog1, button_index) -> {
                    if (button_index == 1) {
                        PostArticlePage.this.saveTempArticle(index);
                    }
                }).show();
            }
        }).show();
    }

    /* access modifiers changed from: private */
    public void saveTempArticle(int index) {
        ArticleTempStore store = new ArticleTempStore(getContext());
        ArticleTemp article_temp = store.articles.get(index);
        article_temp.header = "";
        if (this._header_selector.getSelectedItemPosition() > 0) {
            article_temp.header = this._settings.getArticleHeader(this._header_selector.getSelectedItemPosition());
        }
        article_temp.title = this._title_field.getText().toString();
        article_temp.content = this._content_field.getText().toString();
        store.store();
        if (index < 10) {
            ASAlertDialog.createDialog().setTitle("存檔").setMessage("存檔完成").addButton("確定").show();
        }
    }

    private void onInsertSymbolbuttonClicked() {
        Dialog_InsertSymbol dialog = new Dialog_InsertSymbol();
        dialog.setListsner(this);
        dialog.show();
    }

    public void onSymbolDialogDismissWithSymbol(String symbol) {
        if (this._content_field != null) {
            this._content_field.getEditableText().insert(this._content_field.getSelectionStart(), symbol);
        }
    }

    public void setOperationMode(OperationMode aMode) {
        this._operation_mode = aMode;
    }

    public OperationMode getOperationMode() {
        return this._operation_mode;
    }

    public void setArticleNumber(String aNumber) {
        this._article_number = aNumber;
    }

    public void setEditFormat(String aFormat) {
        this._edit_format = aFormat;
    }

    public String getEditContent() {
        if (this._edit_format == null) {
            return null;
        }
        return String.format(this._edit_format, this._title_field.getText().toString(), this._content_field.getText().toString());
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    public void setBoardPage(BoardPage aBoardPage) {
        this._board_page = aBoardPage;
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        if (this._title_field.getText().toString().length() == 0 && this._content_field.getText().toString().length() == 0) {
            return super.onBackPressed();
        }
        ASAlertDialog.createDialog().setTitle("文章").setMessage("是否要放棄此編輯內容?").addButton("取消").addButton("放棄").addButton("存檔").setListener((aDialog, index) -> {
            if (index == 1) {
                PostArticlePage.this.getNavigationController().popViewController();
            } else if (index == 2) {
                PostArticlePage.this.ontSaveArticleToTempAndLeaveButtonClicked();
            }
        }).show();
        return true;
    }

    /* access modifiers changed from: private */
    public void ontSaveArticleToTempAndLeaveButtonClicked() {
        ASListDialog.createDialog().setTitle("文章").addItem("存入暫存檔.1").addItem("存入暫存檔.2").addItem("存入暫存檔.3").addItem("存入暫存檔.4").addItem("存入暫存檔.5").setListener(new ASListDialogItemClickListener() {
            public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                return false;
            }

            public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?").addButton("取消").addButton("確定").setListener((aDialog1, index1) -> {
                    if (index1 == 0) {
                        PostArticlePage.this.onBackPressed();
                    } else if (index1 == 1) {
                        PostArticlePage.this.saveTempArticle(index1);
                        PostArticlePage.this.getNavigationController().popViewController();
                    }
                }).show();
            }
        }).show();
    }
}
