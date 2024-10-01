package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;
import static com.kota.Bahamut.Service.CommonFunctions.judgeDoubleWord;
import static com.kota.Telnet.Model.TelnetFrame.DEFAULT_COLUMN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.Selection;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.DataModels.ArticleTemp;
import com.kota.Bahamut.DataModels.ArticleTempStore;
import com.kota.Bahamut.DataModels.ReferenceAuthor;
import com.kota.Bahamut.Dialogs.DialogReference;
import com.kota.Bahamut.Dialogs.DialogShortenImage;
import com.kota.Bahamut.Dialogs.DialogShortenUrl;
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression;
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression_Listener;
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol;
import com.kota.Bahamut.Dialogs.Dialog_PaintColor;
import com.kota.Bahamut.Dialogs.Dialog_PostArticle;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.BlockListPage.ArticleExpressionListPage;
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.CommonFunctions;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetArticle;
import com.kota.Telnet.TelnetArticleItemInfo;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.TelnetUI.TelnetPage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostArticlePage extends TelnetPage implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    RelativeLayout mainLayout;
    private String _article_number = null;
    private BoardMainPage _board_page = null;
    private String _ori_content = null;
    private EditText _content_field = null;
    private String _edit_format = null;
    private boolean _header_hidden = false;
    private int _header_selected = 0;
    private Spinner _header_selector = null;
    private PostArticlePage_Listener _listener = null;
    private OperationMode _operation_mode = OperationMode.New;
    private String _ori_title = null;
    private TextView _post_button = null;
    private TextView _symbol_button = null;
    private TextView _insert_symbol_button = null;
    private TextView _paint_color_button = null;
    private View _title_block = null;
    private EditText _title_field = null;
    private TextView _title_field_background = null;
    String[] _headers;
    public boolean recover = false;
    private TelnetArticle telnetArticle;

    private boolean isToolbarShow = false; // 是否展開工具列


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
        if (recover) {
            loadTempArticle(9);
            recover = false;
        }
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
            _ori_title = null;
        }
    }

    private void refreshContentField() {
        if (_content_field != null && _ori_content != null) {
            _content_field.setText(_ori_content);
            if (_ori_content.length() > 0) {
                Selection.setSelection(_content_field.getText(), _ori_content.length());
            }
            _ori_content = null;
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
        _headers = UserSettings.getArticleHeaders();

        mainLayout = (RelativeLayout) findViewById(R.id.content_view);

        _title_field = mainLayout.findViewById(R.id.ArticlePostDialog_TitleField);
        // 點標題的時候拉大編輯框
        _title_field.setOnFocusChangeListener(titleFieldListener);
        _title_field_background = mainLayout.findViewById(R.id.ArticlePostDialog_TitleFieldBackground);
        _content_field = mainLayout.findViewById(R.id.ArticlePostDialog_EditField);

        _post_button = mainLayout.findViewById(R.id.ArticlePostDialog_Post);
        _post_button.setOnClickListener(this);

        _symbol_button = mainLayout.findViewById(R.id.ArticlePostDialog_Symbol);
        _symbol_button.setOnClickListener(this);

        _insert_symbol_button = mainLayout.findViewById(R.id.ArticlePostDialog_Cancel);
        _insert_symbol_button.setOnClickListener(this);

        _paint_color_button = mainLayout.findViewById(R.id.ArticlePostDialog_Color);
        _paint_color_button.setOnClickListener(this);

        mainLayout.findViewById(R.id.ArticlePostDialog_File).setOnClickListener(this);
        mainLayout.findViewById(R.id.ArticlePostDialog_ShortenUrl).setOnClickListener(this);
        mainLayout.findViewById(R.id.ArticlePostDialog_ShortenImage).setOnClickListener(this);
        mainLayout.findViewById(R.id.ArticlePostDialog_EditButtons).setOnClickListener(this);

        _header_selector = mainLayout.findViewById(R.id.Post_headerSelector);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, _headers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _header_selector.setAdapter(adapter);
        _header_selector.setOnItemSelectedListener(this);
        _title_block = mainLayout.findViewById(R.id.Post_TitleBlock);
        _title_block.requestFocus();

        mainLayout.findViewById(R.id.Post_Toolbar_Show).setOnClickListener(postToolbarShowOnClickListener);
        mainLayout.findViewById(R.id.ArticlePostDialog_Reference).setOnClickListener(referenceClickListener);

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));
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

    /** 引言過多失敗存檔 */
    public void setRecover() {
        recover = true;
        saveTempArticle(9);
    }

    /** 設定文章標題 */
    public void setPostTitle(String aTitle) {
        _ori_title = aTitle;
        refreshTitleField();
    }

    /** 設定內文 */
    public void setPostContent(String aContent) {
        _ori_content = aContent;
        refreshContentField();
    }

    public void onClick(View view) {
        if (view == _post_button) {
            if (_listener != null) {
                String title = getArticleHeader(_header_selected) + _title_field.getText().toString().replace("\n", "");
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
            // 表情符號
            final String[] items = UserSettings.getArticleExpressions();
            Dialog_InsertExpression.createDialog().setTitle("表情符號").addItems(items).setListener(new Dialog_InsertExpression_Listener() {
                @Override
                public void onListDialogItemClicked(Dialog_InsertExpression paramASListDialog, int index, String aTitle) {
                    String symbol = items[index];
                    PostArticlePage.this.insertString(symbol);
                }

                @Override
                public void onListDialogSettingClicked() {
                    // 將當前內容存檔, pushView會讓當前頁面消失
                    setRecover();
                    getNavigationController().pushViewController(new ArticleExpressionListPage());
                }
            }).scheduleDismissOnPageDisappear(this).show();
        } else if (view == _insert_symbol_button) {
            // 符號
            Dialog_InsertSymbol dialog = new Dialog_InsertSymbol();
            dialog.setListener(this::insertString);
            dialog.show();
        } else if (view == _paint_color_button) {
            // 上色
            Dialog_PaintColor dialog = new Dialog_PaintColor();
            dialog.setListener(this::insertString);
            dialog.show();
        } else if (view.getId() == R.id.ArticlePostDialog_File) {
            // 檔案
            onFileClicked();
        } else if (view.getId() == R.id.ArticlePostDialog_ShortenUrl) {
            // 短網址
            DialogShortenUrl dialog = new DialogShortenUrl();
            dialog.setListener(this::insertString);
            dialog.show();
        } else if (view.getId() == R.id.ArticlePostDialog_ShortenImage) {
            // 縮圖
            getUrlToken();
            Intent intent = new Intent(TempSettings.getActivity(), DialogShortenImage.class);
            startActivity(intent);
        } else if (view.getId() == R.id.ArticlePostDialog_EditButtons) {
            ASToast.showShortToast(getContextString(R.string.error_under_develop));
        }
    }

    /** 發文 */
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
            recover = false;
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

    /** 標題欄位取得焦點 */
    View.OnFocusChangeListener titleFieldListener = (view, hasFocus) -> {
        if (view != _title_field) {
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
    };

    public void setHeaderHidden(boolean hidden) {
        _header_hidden = hidden;
        refreshHeaderSelector();
    }

    /** 讀取暫存檔 */
    public void onFileClicked() {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.string._article))
                .addItem(getContextString(R.string.load_temp))
                .addItem(getContextString(R.string.save_to_temp))
                .setListener(new ASListDialogItemClickListener() {
                    public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                        return true;
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

    /** 按下 讀取暫存檔 */
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
                        return true;
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

    /** 讀取暫存檔 */
    private void loadTempArticle(int index) {
        ArticleTemp article_temp = new ArticleTempStore(getContext()).articles.get(index);
        _header_selector.setSelection(getIndexOfHeader(article_temp.header));
        _title_field.setText(article_temp.title);
        _content_field.setText(article_temp.content);
    }

    /** 儲存暫存檔 */
    private void saveTempArticle(int index) {
        ArticleTempStore store = new ArticleTempStore(getContext());
        ArticleTemp article_temp = store.articles.get(index);
        // 類別
        article_temp.header = "";
        if (_header_selector.getSelectedItemPosition() > 0) {
            article_temp.header = getArticleHeader(_header_selector.getSelectedItemPosition());
        }
        // 標題
        article_temp.title = _title_field.getText().toString();
        // 內文
        article_temp.content = _content_field.getText().toString();

        // 存檔
        store.store();
        // ArticleTempStore 有暫存檔定義
        if (index < 8) {
            ASAlertDialog.createDialog().setTitle(getContextString(R.string._save)).setMessage("存檔完成").addButton(getContextString(R.string.sure)).show();
        }
    }

    /** 從字串去回推標題定位 */
    int getIndexOfHeader(String aHeader) {
        if (aHeader == null || aHeader.length() == 0) {
            return 0;
        }
        for (int i = 1; i < _headers.length; i++) {
            if (_headers[i].equals(aHeader)) {
                return i;
            }
        }
        return -1;
    }

    /** 從定位取出特定標題 */
    String getArticleHeader(int index) {
        if (index <= 0 || index >= _headers.length) {
            return "";
        }
        return _headers[index];
    }

    /** 按下 存入暫存檔 */
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
                        return true;
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

    /** 存檔時, 存入暫存檔 */
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
                        return true;
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

    public void setOperationMode(OperationMode aMode) {
        _operation_mode = aMode;
    }

    public void setArticleNumber(String aNumber) {
        _article_number = aNumber;
    }

    public void setEditFormat(String aFormat) {
        _edit_format = aFormat;
    }

    /** 組合修改文章內容 */
    public String getEditContent() {
        if (_edit_format == null) {
            return null;
        }
        String _title = judgeDoubleWord(_title_field.getText().toString(), DEFAULT_COLUMN-9).split("\n")[0];
        String _content = _content_field.getText().toString();
        return String.format(_edit_format, _title, _content);
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    public void setBoardPage(BoardMainPage aBoardMainPage) {
        _board_page = aBoardMainPage;
    }

    /** 按下 返回 */
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

    /** 按下 展開/摺疊 */
    View.OnClickListener postToolbarShowOnClickListener = view -> {
        TextView thisBtn = (TextView) view;
        LinearLayout toolBar = mainLayout.findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolBar.getLayoutParams();
        if (isToolbarShow) {
            // 從展開->摺疊
            isToolbarShow = false;
            thisBtn.setText(getContextString(R.string.post_toolbar_show));
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResource().getDisplayMetrics());
        } else {
            isToolbarShow = true;
            thisBtn.setText(getContextString(R.string.post_toolbar_collapse));
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResource().getDisplayMetrics());
        }
        toolBar.setLayoutParams(layoutParams);
    };

    public void insertString(String str) {
        if (_content_field != null) {
            _content_field.getEditableText().insert(_content_field.getSelectionStart(), str);
        }
    }

    /** 取得imgur token */
    void getUrlToken() {
        String apiUrl = "https://worker-get-imgur-token.kodakjerec.workers.dev/";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();
        ASRunner.runInNewThread(()->{
            try{
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String data = response.body().string();
                JSONObject jsonObject = new JSONObject(data);
                String accessToken = jsonObject.getString("accessToken");
                String albumHash = jsonObject.getString("albumHash");
                if (!accessToken.isEmpty()) {
                    TempSettings.setImgurToken(accessToken);
                    TempSettings.setImgurAlbum(albumHash);
                }
            } catch (Exception e) {
//                ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error01));
                Log.e("ShortenImage", e.toString());
            }
        });
    }

    /** 設定TelnetArticle, 格式按鈕會使用 */
    public void setTelnetArticle(TelnetArticle article) {
        telnetArticle = article;
    }
    /** 按下格式 */
    View.OnClickListener referenceClickListener = view -> {
        List<ReferenceAuthor> authors = new ArrayList<>();
        if (telnetArticle == null) {
            ASAlertDialog.showErrorDialog(getContextString(R.string.dialog_reference_error_1), this);
            return;
        }

        if (_edit_format == null) {
            // 回覆
            // 回應作者
            ReferenceAuthor replyAuthor = new ReferenceAuthor();
            replyAuthor.setEnabled(true);
            replyAuthor.setAuthorName(telnetArticle.Author);
            authors.add(replyAuthor);

            // 更上一層
            ReferenceAuthor newAuthor = new ReferenceAuthor();
            if (telnetArticle.getInfoSize()>0) {
                TelnetArticleItemInfo item = telnetArticle.getInfo(0);
                newAuthor.setEnabled(true);
                newAuthor.setAuthorName(item.author);
            }
            authors.add(newAuthor);
        } else {
            // 修改
            if (telnetArticle.getInfoSize()>0) {
                for (int i = 0; i < telnetArticle.getInfoSize(); i++) {
                    ReferenceAuthor newAuthor = new ReferenceAuthor();
                    TelnetArticleItemInfo item = telnetArticle.getInfo(i);
                    newAuthor.setEnabled(true);
                    newAuthor.setAuthorName(item.author);
                    authors.add(newAuthor);
                }
            }
        }

        DialogReference dialog = new DialogReference();
        dialog.setAuthors(authors);
        dialog.setListener(this::referenceBack);
        dialog.show();
    };

    public void referenceBack(List<ReferenceAuthor> authors) {
        // 找出父層內容
        List<String> originParentContent = Arrays.asList(telnetArticle.generateReplyContent().split("\n"));
        List<String> parentContent = new ArrayList<>();


        // 開始篩選
        ReferenceAuthor author0 = authors.get(0);
        ReferenceAuthor author1 = authors.get(1);
        int author0InsertRows = 0;
        int author1InsertRows = 0;

        // 如果有選到後三行, 計算兩個作者總行數
        int author0TotalRows = 0;
        int author1TotalRows = 0;
        if (author0.getReservedType()==2 || author1.getReservedType()==2) {
            for (int i = 0; i < originParentContent.size(); i++) {
                String rowString = originParentContent.get(i);
                if (rowString.startsWith("> ※ ") || rowString.startsWith("> > ")) {
                    author1TotalRows++;
                } else if (rowString.startsWith("※ ") || rowString.startsWith("> ")) {
                    author0TotalRows++;
                }
            }
        }

        boolean needInsert;
        for(int i=0;i<originParentContent.size();i++) {
            String rowString = originParentContent.get(i);
            needInsert = false;
            if (rowString.startsWith("> ※ ")) {
                // 前二
                if (author1.getEnabled())
                    needInsert = true;
            }
            else if (rowString.startsWith("> > ")) {
                // 前二
                if (author1.getEnabled()) {
                    switch (author1.getReservedType()) {
                        case 0 -> // 全部
                                needInsert = true;
                        case 1 -> { // 前三
                            if (author1InsertRows < 3) {
                                author1InsertRows++;
                                needInsert = true;
                            }
                        }
                        case 2 -> {  // 後三
                            author1InsertRows++;
                            if (author1InsertRows+3>=author1TotalRows)
                                needInsert = true;
                        }
                    }

                    if (needInsert && rowString.replaceAll("> > ", "").isEmpty()) {
                        if (author1.getRemoveBlank()) {
                            needInsert = false;
                        }
                    }
                }
            } else if (rowString.startsWith("※ ")) {
                // 前一
                if (author0.getEnabled())
                    needInsert = true;
            } else if (rowString.startsWith("> ")) {
                // 前一
                if (author0.getEnabled()) {
                    switch (author0.getReservedType()) {
                        case 0 -> // 保留
                                needInsert = true;
                        case 1 -> { // 前三
                            if (author0InsertRows < 3) {
                                author0InsertRows++;
                                needInsert = true;
                            }
                        }
                        case 2 -> { // 後三
                            author0InsertRows++;
                            if (author0InsertRows+3>=author0TotalRows)
                                needInsert = true;
                        }
                    }
                    if (needInsert && rowString.replaceAll("> ", "").isEmpty()) {
                        if (author0.getRemoveBlank()) {
                            needInsert = false;
                        }
                    }
                }
            }
            if (needInsert) {
                parentContent.add(originParentContent.get(i));
            }
        }
        String joinedParentContent = String.join("\n", parentContent);

        // 找出自己打的內容
        List<String> originFromContent = Arrays.asList(_content_field.getText().toString().split("\n"));
        List<String> selfContent = new ArrayList<>();
        for(int i=0;i<originFromContent.size();i++) {
            if (!originFromContent.get(i).startsWith("※ 引述") && !originFromContent.get(i).startsWith("> ")) {
                selfContent.add(originFromContent.get(i));
            }
        }
        // final Result
        String joinedSelfContent = String.join("\n", selfContent);

        String _rev2 = String.join("", joinedParentContent, "\n", joinedSelfContent);
        _content_field.setText(_rev2);
    }
}
