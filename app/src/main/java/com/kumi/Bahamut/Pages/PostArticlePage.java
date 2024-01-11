package com.kumi.Bahamut.Pages;

import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.Dialog.ASListDialog;
import com.kumi.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.Bahamut.DataModels.ArticleTemp;
import com.kumi.Bahamut.DataModels.ArticleTempStore;
import com.kumi.Bahamut.Dialogs.Dialog_InsertSymbol;
import com.kumi.Bahamut.Dialogs.Dialog_InsertSymbol_Listener;
import com.kumi.Bahamut.Dialogs.Dialog_PostArticle;
import com.kumi.Bahamut.Dialogs.Dialog_PostArticle_Listener;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;

public class PostArticlePage extends TelnetPage implements View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnFocusChangeListener, Dialog_InsertSymbol_Listener {
  private String _article_number = null;
  
  private BoardPage _board_page = null;
  
  private String _content = null;
  
  private EditText _content_field = null;
  
  private String _edit_format = null;
  
  private boolean _header_hidden = false;
  
  private int _header_selected = 0;
  
  private Spinner _header_selector = null;
  
  private Button _hide_title_button = null;
  
  private PostArticlePage_Listener _listener = null;
  
  private OperationMode _operation_mode = OperationMode.New;
  
  private String _ori_title = null;
  
  private Button _post_button = null;
  
  UserSettings _settings;
  
  private Button _symbol_button = null;
  
  private View _title_block = null;
  
  private boolean _title_block_hidden = false;
  
  private EditText _title_field = null;
  
  private TextView _title_field_background = null;
  
  public boolean recover = false;
  
  private void initial() {
    this._title_field = (EditText)findViewById(2131230728);
    this._title_field.setOnFocusChangeListener(this);
    this._title_field_background = (TextView)findViewById(2131230729);
    this._content_field = (EditText)findViewById(2131230724);
    this._post_button = (Button)findViewById(2131230726);
    this._post_button.setOnClickListener(this);
    this._symbol_button = (Button)findViewById(2131230727);
    this._symbol_button.setOnClickListener(this);
    this._hide_title_button = (Button)findViewById(2131230723);
    this._hide_title_button.setOnClickListener(this);
    ((Button)findViewById(2131230725)).setOnClickListener(this);
    this._header_selector = (Spinner)findViewById(2131230899);
    ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), 2131361869, (Object[])this._settings.getArticleHeaders());
    arrayAdapter.setDropDownViewResource(17367049);
    this._header_selector.setAdapter((SpinnerAdapter)arrayAdapter);
    this._header_selector.setOnItemSelectedListener(this);
    this._title_block = findViewById(2131230903);
    if (this.recover) {
      loadTempArticle(10);
      this.recover = false;
    } 
  }
  
  private void loadTempArticle(int paramInt) {
    ArticleTemp articleTemp = (new ArticleTempStore(getContext())).articles.get(paramInt);
    paramInt = this._settings.getIndexOfHeader(articleTemp.header);
    this._header_selector.setSelection(paramInt);
    this._title_field.setText(articleTemp.title);
    this._content_field.setText(articleTemp.content);
  }
  
  private void onInsertSymbolbuttonClicked() {
    Dialog_InsertSymbol dialog_InsertSymbol = new Dialog_InsertSymbol();
    dialog_InsertSymbol.setListsner(this);
    dialog_InsertSymbol.show();
  }
  
  private void ontLoadArticleFromTempButtonClicked() {
    ASListDialog.createDialog().setTitle("文章").addItem("讀取上次送出文章").addItem("讀取暫存檔.1").addItem("讀取暫存檔.2").addItem("讀取暫存檔.3").addItem("讀取暫存檔.4").addItem("讀取暫存檔.5").setListener(new ASListDialogItemClickListener() {
          final PostArticlePage this$0;
          
          public void onListDialogItemClicked(ASListDialog param1ASListDialog, final int index, String param1String) {
            if (index == 0) {
              ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?").setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?").addButton("取消").addButton("確定").setListener(new ASAlertDialogListener() {
                    final PostArticlePage.null this$1;
                    
                    public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param2ASAlertDialog, int param2Int) {
                      PostArticlePage.this.loadTempArticle(10);
                    }
                  }).show();
              return;
            } 
            ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以暫存檔." + index + "的內容取代您現在編輯的內容?").addButton("取消").addButton("確定").setListener(new ASAlertDialogListener() {
                  final PostArticlePage.null this$1;
                  
                  final int val$index;
                  
                  public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param2ASAlertDialog, int param2Int) {
                    if (param2Int == 1)
                      PostArticlePage.this.loadTempArticle(index - 1); 
                  }
                }).show();
          }
          
          public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            return false;
          }
        }).show();
  }
  
  private void ontSaveArticleToTempAndLeaveButtonClicked() {
    ASListDialog.createDialog().setTitle("文章").addItem("存入暫存檔.1").addItem("存入暫存檔.2").addItem("存入暫存檔.3").addItem("存入暫存檔.4").addItem("存入暫存檔.5").setListener(new ASListDialogItemClickListener() {
          final PostArticlePage this$0;
          
          public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (param1Int + 1) + "的內容?").addButton("取消").addButton("確定").setListener(new ASAlertDialogListener() {
                  final PostArticlePage.null this$1;
                  
                  public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param2ASAlertDialog, int param2Int) {
                    if (param2Int == 0) {
                      PostArticlePage.this.onBackPressed();
                      return;
                    } 
                    if (param2Int == 1) {
                      PostArticlePage.this.saveTempArticle(param2Int);
                      PostArticlePage.this.getNavigationController().popViewController();
                    } 
                  }
                }).show();
          }
          
          public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            return false;
          }
        }).show();
  }
  
  private void ontSaveArticleToTempButtonClicked() {
    ASListDialog.createDialog().setTitle("文章").addItem("存入暫存檔.1").addItem("存入暫存檔.2").addItem("存入暫存檔.3").addItem("存入暫存檔.4").addItem("存入暫存檔.5").setListener(new ASListDialogItemClickListener() {
          final PostArticlePage this$0;
          
          public void onListDialogItemClicked(ASListDialog param1ASListDialog, final int index, String param1String) {
            ASAlertDialog.createDialog().setTitle("讀取暫存檔").setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?").addButton("取消").addButton("確定").setListener(new ASAlertDialogListener() {
                  final PostArticlePage.null this$1;
                  
                  final int val$index;
                  
                  public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param2ASAlertDialog, int param2Int) {
                    if (param2Int == 1)
                      PostArticlePage.this.saveTempArticle(index); 
                  }
                }).show();
          }
          
          public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            return false;
          }
        }).show();
  }
  
  private void refreshContentField() {
    if (this._content_field != null && this._content != null) {
      this._content_field.setText(this._content);
      if (this._content.length() > 0)
        Selection.setSelection((Spannable)this._content_field.getText(), this._content.length()); 
      this._content = null;
    } 
  }
  
  private void refreshHeaderSelector() {
    if (this._header_selector != null) {
      if (this._header_hidden) {
        this._header_selector.setVisibility(8);
        return;
      } 
    } else {
      return;
    } 
    this._header_selector.setVisibility(0);
  }
  
  private void refreshTitleField() {
    if (this._title_field != null && this._ori_title != null) {
      this._title_field.setText(this._ori_title);
      if (this._ori_title.length() > 0)
        Selection.setSelection((Spannable)this._title_field.getText(), 1); 
    } 
  }
  
  private void saveTempArticle(int paramInt) {
    ArticleTempStore articleTempStore = new ArticleTempStore(getContext());
    ArticleTemp articleTemp = articleTempStore.articles.get(paramInt);
    articleTemp.header = "";
    if (this._header_selector.getSelectedItemPosition() > 0)
      articleTemp.header = this._settings.getArticleHeader(this._header_selector.getSelectedItemPosition()); 
    articleTemp.title = this._title_field.getText().toString();
    articleTemp.content = this._content_field.getText().toString();
    articleTempStore.store();
    if (paramInt < 10)
      ASAlertDialog.createDialog().setTitle("存檔").setMessage("存檔完成").addButton("確定").show(); 
  }
  
  public void clear() {
    if (this._title_field != null)
      this._title_field.setText(""); 
    if (this._content_field != null)
      this._content_field.setText(""); 
    this._listener = null;
  }
  
  public String getEditContent() {
    String str = null;
    if (this._edit_format != null)
      str = String.format(this._edit_format, new Object[] { this._title_field.getText().toString(), this._content_field.getText().toString() }); 
    return str;
  }
  
  public OperationMode getOperationMode() {
    return this._operation_mode;
  }
  
  public int getPageLayout() {
    return 2131361863;
  }
  
  public int getPageType() {
    return 16;
  }
  
  public boolean isKeepOnOffline() {
    return true;
  }
  
  public boolean isPopupPage() {
    return true;
  }
  
  protected boolean onBackPressed() {
    if (this._title_field.getText().toString().length() > 0 || this._content_field.getText().toString().length() > 0) {
      ASAlertDialog.createDialog().setTitle("文章").setMessage("是否要放棄此編輯內容?").addButton("取消").addButton("放棄").addButton("存檔").setListener(new ASAlertDialogListener() {
            final PostArticlePage this$0;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              if (param1Int == 1) {
                PostArticlePage.this.getNavigationController().popViewController();
                return;
              } 
              if (param1Int == 2)
                PostArticlePage.this.ontSaveArticleToTempAndLeaveButtonClicked(); 
            }
          }).show();
      return true;
    } 
    return super.onBackPressed();
  }
  
  public void onClick(View paramView) {
    String str;
    final String[] items;
    if (paramView == this._post_button) {
      if (this._listener != null) {
        String str1 = this._settings.getArticleHeader(this._header_selected) + this._title_field.getText().toString().replace("\n", "");
        String str2 = this._content_field.getText().toString();
        paramView = null;
        if (str1.length() == 0 && str2.length() == 0) {
          str = "標題與內文不可為空";
        } else if (str1.length() == 0) {
          str = "標題不可為空";
        } else if (str2.length() == 0) {
          str = "內文不可為空";
        } 
        if (str == null) {
          post(str1, str2);
          return;
        } 
      } else {
        return;
      } 
      ASAlertDialog.createDialog().setTitle("錯誤").setMessage(str).addButton("確定").show();
      return;
    } 
    if (str == this._symbol_button) {
      arrayOfString = this._settings.getSymbols();
      ASListDialog.createDialog().setItemTextSize(0).setTitle("表情符號").addItems(arrayOfString).setListener(new ASListDialogItemClickListener() {
            final PostArticlePage this$0;
            
            final String[] val$items;
            
            public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
              String str = items[param1Int];
              param1Int = PostArticlePage.this._content_field.getSelectionStart();
              PostArticlePage.this._content_field.getEditableText().insert(param1Int, str);
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
    if (arrayOfString.getId() == 2131230725)
      onFileClicked(); 
  }
  
  public void onFileClicked() {
    ASListDialog.createDialog().setTitle("文章").addItem("讀取暫存檔").addItem("存入暫存檔").setListener(new ASListDialogItemClickListener() {
          final PostArticlePage this$0;
          
          public void onListDialogItemClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            if (param1String == "讀取暫存檔") {
              PostArticlePage.this.ontLoadArticleFromTempButtonClicked();
              return;
            } 
            if (param1String == "存入暫存檔")
              PostArticlePage.this.ontSaveArticleToTempButtonClicked(); 
          }
          
          public boolean onListDialogItemLongClicked(ASListDialog param1ASListDialog, int param1Int, String param1String) {
            return false;
          }
        }).show();
  }
  
  public void onFocusChange(View paramView, boolean paramBoolean) {
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
  
  public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
    this._header_selected = paramInt;
  }
  
  public void onNothingSelected(AdapterView<?> paramAdapterView) {}
  
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
  
  public void onPageDidLoad() {
    this._settings = new UserSettings(getContext());
    initial();
  }
  
  public void onPageRefresh() {
    refreshTitleField();
    refreshContentField();
    refreshHeaderSelector();
  }
  
  public void onPageWillAppear() {
    this._content_field.requestFocus();
  }
  
  public void onSymbolDialogDismissWithSymbol(String paramString) {
    if (this._content_field != null) {
      int i = this._content_field.getSelectionStart();
      this._content_field.getEditableText().insert(i, paramString);
    } 
  }
  
  void post(final String send_title, final String send_content) {
    saveTempArticle(10);
    if (this._article_number != null && send_title.equals(this._ori_title))
      send_title = null; 
    if (this._article_number != null && this._operation_mode == OperationMode.Reply) {
      Dialog_PostArticle dialog_PostArticle1 = new Dialog_PostArticle(1);
      dialog_PostArticle1.setListener(new Dialog_PostArticle_Listener() {
            final PostArticlePage this$0;
            
            final PostArticlePage val$post_dialog;
            
            final String val$send_content;
            
            final String val$send_title;
            
            public void onPostArticleDoneWithTarger(String param1String1, String param1String2) {
              if (PostArticlePage.this._listener != null)
                PostArticlePage.this._listener.onPostDialogSendButtonClicked(post_dialog, send_title, send_content, param1String1, PostArticlePage.this._article_number, param1String2); 
              PostArticlePage.this.getNavigationController().popToViewController((ASViewController)PostArticlePage.this._board_page);
              PostArticlePage.this.clear();
            }
          });
      dialog_PostArticle1.show();
      return;
    } 
    if (this._article_number != null) {
      ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認").setMessage("您是否確定要編輯此文章?").setListener(new ASAlertDialogListener() {
            final PostArticlePage this$0;
            
            final PostArticlePage val$post_dialog;
            
            final String val$send_title;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              if (param1Int == 1) {
                if (PostArticlePage.this._listener != null)
                  PostArticlePage.this._listener.onPostDialogEditButtonClicked(post_dialog, PostArticlePage.this._article_number, send_title, PostArticlePage.this.getEditContent()); 
                PostArticlePage.this.getNavigationController().popToViewController((ASViewController)PostArticlePage.this._board_page);
                PostArticlePage.this.clear();
              } 
            }
          }).show();
      return;
    } 
    Dialog_PostArticle dialog_PostArticle = new Dialog_PostArticle(0);
    dialog_PostArticle.setListener(new Dialog_PostArticle_Listener() {
          final PostArticlePage this$0;
          
          final PostArticlePage val$post_dialog;
          
          final String val$send_content;
          
          final String val$send_title;
          
          public void onPostArticleDoneWithTarger(String param1String1, String param1String2) {
            if (PostArticlePage.this._listener != null)
              PostArticlePage.this._listener.onPostDialogSendButtonClicked(post_dialog, send_title, send_content, null, null, param1String2); 
            PostArticlePage.this.getNavigationController().popToViewController((ASViewController)PostArticlePage.this._board_page);
            PostArticlePage.this.clear();
          }
        });
    dialog_PostArticle.show();
  }
  
  public void refresh() {
    if (this._title_block_hidden) {
      this._title_block.setVisibility(8);
      return;
    } 
    this._title_block.setVisibility(0);
  }
  
  public void setArticleNumber(String paramString) {
    this._article_number = paramString;
  }
  
  public void setBoardPage(BoardPage paramBoardPage) {
    this._board_page = paramBoardPage;
  }
  
  public void setEditFormat(String paramString) {
    this._edit_format = paramString;
  }
  
  public void setHeaderHidden(boolean paramBoolean) {
    this._header_hidden = paramBoolean;
    refreshHeaderSelector();
  }
  
  public void setListener(PostArticlePage_Listener paramPostArticlePage_Listener) {
    this._listener = paramPostArticlePage_Listener;
  }
  
  public void setOperationMode(OperationMode paramOperationMode) {
    this._operation_mode = paramOperationMode;
  }
  
  public void setPostContent(String paramString) {
    this._content = paramString;
    refreshContentField();
  }
  
  public void setPostTitle(String paramString) {
    this._ori_title = paramString;
    refreshTitleField();
  }
  
  public enum OperationMode {
    Edit, New, Reply;
    
    private static final OperationMode[] $VALUES;
    
    static {
      $VALUES = new OperationMode[] { New, Reply, Edit };
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\PostArticlePage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */