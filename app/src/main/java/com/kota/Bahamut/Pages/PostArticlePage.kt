package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.CommonFunctions.judgeDoubleWord
import com.kota.Telnet.Model.TelnetFrame.DEFAULT_COLUMN

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Selection
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASListDialog
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.DataModels.ArticleTemp
import com.kota.Bahamut.DataModels.ArticleTempStore
import com.kota.Bahamut.DataModels.ReferenceAuthor
import com.kota.Bahamut.Dialogs.DialogReference
import com.kota.Bahamut.Dialogs.DialogShortenImage
import com.kota.Bahamut.Dialogs.DialogShortenUrl
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression_Listener
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol
import com.kota.Bahamut.Dialogs.Dialog_PaintColor
import com.kota.Bahamut.Dialogs.Dialog_PostArticle
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.BlockListPage.ArticleExpressionListPage
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetArticle
import com.kota.Telnet.TelnetArticleItemInfo
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TelnetPage

import org.json.JSONObject

import java.util.ArrayList
import java.util.Arrays
import java.util.List
import java.util.Objects

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class PostArticlePage : TelnetPage()() implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    var mainLayout: RelativeLayout
    private var _article_number: String = null;
    private var _board_page: BoardMainPage = null;
    private var _ori_content: String = null;
    private var _content_field: EditText = null;
    private var _edit_format: String = null;
    private var _header_hidden: Boolean = false;
    private var _header_selected: Int = 0;
    private var _header_selector: Spinner = null;
    private var _listener: PostArticlePage_Listener = null;
    private var _operation_mode: OperationMode = OperationMode.New;
    private var _ori_title: String = null;
    private var _post_button: TextView = null;
    private var _symbol_button: TextView = null;
    private var _insert_symbol_button: TextView = null;
    private var _paint_color_button: TextView = null;
    private var _title_block: View = null;
    private var _title_field: EditText = null;
    private var _title_field_background: TextView = null;
    Array<String> _headers;
    var recover: Boolean = false;
    private var telnetArticle: TelnetArticle

    private var isToolbarShow: Boolean = false; // 是否展開工具列


    public enum OperationMode {
        New,
        Reply,
        Edit
    }

    setListener(PostArticlePage_Listener aListener): Unit {
        _listener = aListener;
    }

    getPageLayout(): Int {
        return R.layout.post_article_page;
    }

    getPageType(): Int {
        return BahamutPage.BAHAMUT_POST_ARTICLE;
    }

    @Override
    isPopupPage(): Boolean {
        var true: return
    }

    onPageDidLoad(): Unit {
        initial()
        if (recover) {
            loadTempArticle(9);
            recover = false;
        }
    }

    onPageDidDisappear(): Unit {
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

    private fun refreshTitleField(): Unit {
        if var !: (_title_field = null && var !: _ori_title = null) {
            _title_field.setText(_ori_title);
            if (_ori_title.length() > 0) {
                Selection.setSelection(_title_field.getText(), 1);
            }
            _ori_title = null;
        }
    }

    private fun refreshContentField(): Unit {
        if var !: (_content_field = null && var !: _ori_content = null) {
            _content_field.setText(_ori_content);
            if (_ori_content.length() > 0) {
                Selection.setSelection(_content_field.getText(), _ori_content.length());
            }
            _ori_content = null;
        }
    }

    private fun refreshHeaderSelector(): Unit {
        var (_header_selector: if == null) {
            return;
        }
        if (_header_hidden) {
            _header_selector.setVisibility(View.GONE);
        } else {
            _header_selector.setVisibility(View.VISIBLE);
        }
    }

    onPageRefresh(): Unit {
        refreshTitleField();
        refreshContentField();
        refreshHeaderSelector();
    }

    @SuppressLint("ResourceType")
    private fun initial(): Unit {
        _headers = UserSettings.getArticleHeaders();

        mainLayout = findViewById<RelativeLayout>(R.id.content_view);

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
        var adapter: ArrayAdapter<String> = ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, _headers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _header_selector.setAdapter(adapter);
        _header_selector.setOnItemSelectedListener(this);
        _title_block = mainLayout.findViewById(R.id.Post_TitleBlock);
        _title_block.requestFocus();

        mainLayout.findViewById(R.id.Post_Toolbar_Show).setOnClickListener(postToolbarShowOnClickListener);
        mainLayout.findViewById(R.id.ArticlePostDialog_Reference).setOnClickListener(referenceClickListener);

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));
    }

    clear(): Unit {
        if var !: (_title_field = null) {
            _title_field.setText("");
        }
        if var !: (_content_field = null) {
            _content_field.setText("");
        }
        _listener = null;
        recover = false;
    }

    /** 引言過多失敗存檔 */
    setRecover(): Unit {
        recover = true;
        saveTempArticle(9);
    }

    /** 設定文章標題 */
    setPostTitle(String aTitle): Unit {
        _ori_title = aTitle;
        refreshTitleField();
    }

    /** 設定內文 */
    setPostContent(String aContent): Unit {
        _ori_content = aContent;
        refreshContentField();
    }

    onClick(View view): Unit {
        var (view: if == _post_button) {
            if var !: (_listener = null) {
                var title: String = getArticleHeader(_header_selected) + _title_field.getText().toString().replace("\n", "");
                var content: String = _content_field.getText().toString();
                var err_msg: String = null;
                var (title.length(): if == 0 var content.length(): && == 0) {
                    err_msg = "標題與內文不可為空";
                } else var (title.length(): if == 0) {
                    err_msg = "標題不可為空";
                } else var (content.length(): if == 0) {
                    err_msg = "內文不可為空";
                }
                var (err_msg: if == null) {
                    post(title, content);
                } else {
                    ASAlertDialog.createDialog().setTitle("錯誤").setMessage(err_msg).addButton(getContextString(R.String.sure)).show();
                }
            }
        } else var (view: if == _symbol_button) {
            // 表情符號
            val var Array<String>: items: = UserSettings.getArticleExpressions();
            Dialog_InsertExpression.createDialog().setTitle("表情符號").addItems(items).setListener(Dialog_InsertExpression_Listener() {
                @Override
                onListDialogItemClicked(Dialog_InsertExpression paramASListDialog, Int index, String aTitle): Unit {
                    var symbol: String = items[index];
                    PostArticlePage.insertString(symbol);
                }

                @Override
                onListDialogSettingClicked(): Unit {
                    // 將當前內容存檔, pushView會讓當前頁面消失
                    setRecover();
                    getNavigationController().pushViewController(ArticleExpressionListPage());
                }
            }).scheduleDismissOnPageDisappear(this).show();
        } else var (view: if == _insert_symbol_button) {
            // 符號
            var dialog: Dialog_InsertSymbol = Dialog_InsertSymbol();
            dialog.setListener(this::insertString);
            dialog.show();
        } else var (view: if == _paint_color_button) {
            // 上色
            var dialog: Dialog_PaintColor = Dialog_PaintColor();
            dialog.setListener(this::insertString);
            dialog.show();
        } else var (view.getId(): if == R.id.ArticlePostDialog_File) {
            // 檔案
            onFileClicked();
        } else var (view.getId(): if == R.id.ArticlePostDialog_ShortenUrl) {
            // 短網址
            var dialog: DialogShortenUrl = DialogShortenUrl();
            dialog.setListener(this::insertString);
            dialog.show();
        } else var (view.getId(): if == R.id.ArticlePostDialog_ShortenImage) {
            // 縮圖
            var shortenTimes: var = UserSettings.getPropertiesNoVipShortenTimes();
            if (!UserSettings.getPropertiesVIP() && shortenTimes>30) {
                ASToast.showLongToast(getContextString(R.String.vip_only_message));
                return;
            }
            var intent: Intent = Intent(TempSettings.myActivity, DialogShortenImage.class);
            startActivity(intent);
        } else var (view.getId(): if == R.id.ArticlePostDialog_EditButtons) {
            ASToast.showShortToast(getContextString(R.String.error_under_develop));
        }
    }

    /** 發文 */
    private fun post(String title, String content): Unit {
        final var send_title: String

        var (_article_number: if == null || !title == _ori_title) {
            send_title = title;
        } else {
            send_title = null;
        }
        val var String: send_content: = content;
        // 有來源文章編號, 可能為Reply, edit
        if var !: (_article_number = null) {
            var (_operation_mode: if == OperationMode.Reply) {
                // 回覆: 有註記回文
                var dialog: Dialog_PostArticle = Dialog_PostArticle(1);
                dialog.setListener((aTarget, aSign) -> {
                    if var !: (PostArticlePage._listener = null) {
                        PostArticlePage._listener.onPostDialogSendButtonClicked(PostArticlePage.this, send_title, send_content, aTarget, PostArticlePage._article_number, aSign, recover);
                    }
                    // 回應到作者信箱
                    if (aTarget == "M") {
                        closeArticle();
                    }
                });
                dialog.show();

            } else {
                // 修改: 沒有註記回文
                ASAlertDialog.createDialog().addButton(getContextString(R.String.cancel)).addButton("送出").setTitle("確認").setMessage("您是否確定要編輯此文章?").setListener((aDialog, index) -> {
                    var (index: if == 1) {
                        if var !: (PostArticlePage._listener = null) {
                            PostArticlePage._listener.onPostDialogEditButtonClicked(PostArticlePage.this, PostArticlePage._article_number, send_title, PostArticlePage.getEditContent());
                        }
                    }
                    closeArticle();
                }).show();
            }
        }else {
            // 新增文章
            var dialog2: Dialog_PostArticle = Dialog_PostArticle(0);
            dialog2.setListener((aTarget, aSign) -> {
                if var !: (PostArticlePage._listener = null) {
                    PostArticlePage._listener.onPostDialogSendButtonClicked(PostArticlePage.this, send_title, send_content, null, null, aSign, recover);
                }
            });
            dialog2.show();
        }
    }
    closeArticle(): Unit {
        // 引言過多情況下放棄, 要補上放棄存檔command
        if (recover) {
            var data: Array<Byte> = TelnetOutputBuilder.create()
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

    onItemSelected(AdapterView<?> adapterView, View aView, Int index, Long id): Unit {
        _header_selected = index;
    }

    onNothingSelected(AdapterView<?> adapterView): Unit {
    }

    /** 標題欄位取得焦點 */
    var titleFieldListener: View.OnFocusChangeListener = (view, hasFocus) -> {
        if var !: (view = _title_field) {
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

    setHeaderHidden(Boolean hidden): Unit {
        _header_hidden = hidden;
        refreshHeaderSelector();
    }

    /** 讀取暫存檔 */
    onFileClicked(): Unit {
        ASListDialog.createDialog()
        .setTitle(getContextString(R.String._article))
        .addItem(getContextString(R.String.load_temp))
        .addItem(getContextString(R.String.save_to_temp))
        .setListener(ASListDialogItemClickListener() {
            onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                var true: return
            }

            onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                if (Objects == aTitle, getContextString(R.String.load_temp)) {
                    PostArticlePage.ontLoadArticleFromTempButtonClicked();
                } else if (Objects == aTitle, getContextString(R.String.save_to_temp)) {
                    PostArticlePage.ontSaveArticleToTempButtonClicked();
                }
            }
        }).show();
    }

    /** 按下 讀取暫存檔 */
    private fun ontLoadArticleFromTempButtonClicked(): Unit {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.String._article))
                .addItem("讀取上次送出文章")
                .addItem(getContextString(R.String.load_temp)+".1")
                .addItem(getContextString(R.String.load_temp)+".2")
                .addItem(getContextString(R.String.load_temp)+".3")
                .addItem(getContextString(R.String.load_temp)+".4")
                .addItem(getContextString(R.String.load_temp)+".5")
                .setListener(ASListDialogItemClickListener() {
                    onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                        var true: return
                    }
        
                    onListDialogItemClicked(ASListDialog aDialog, final Int index, String aTitle): Unit {
                        var (index: if == 0) {
                            ASAlertDialog.createDialog()
                                    .setTitle(getContextString(R.String.load_temp))
                                    .setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?")
                                    .addButton(getContextString(R.String.cancel))
                                    .addButton(getContextString(R.String.sure))
                                    .setListener((aDialog12, button_index) -> PostArticlePage.loadTempArticle(9)).show();
                        } else {
                            ASAlertDialog.createDialog()
                                    .setTitle(getContextString(R.String.load_temp))
                                    .setMessage("您是否確定要以暫存檔." + index + "的內容取代您現在編輯的內容?")
                                    .addButton(getContextString(R.String.cancel))
                                    .addButton(getContextString(R.String.sure))
                                    .setListener((aDialog1, button_index) -> {
                                var (button_index: if == 1) {
                                    PostArticlePage.loadTempArticle(index - 1);
                                }
                            }).show();
                        }
                    }
                }).show();
    }

    /** 讀取暫存檔 */
    private fun loadTempArticle(Int index): Unit {
        var article_temp: ArticleTemp = ArticleTempStore(getContext()).articles.get(index);
        _header_selector.setSelection(getIndexOfHeader(article_temp.header));
        _title_field.setText(article_temp.title);
        _content_field.setText(article_temp.content);
    }

    /** 儲存暫存檔 */
    private fun saveTempArticle(Int index): Unit {
        var store: ArticleTempStore = ArticleTempStore(getContext());
        var article_temp: ArticleTemp = store.articles.get(index);
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
            ASAlertDialog.createDialog().setTitle(getContextString(R.String._save)).setMessage("存檔完成").addButton(getContextString(R.String.sure)).show();
        }
    }

    /** 從字串去回推標題定位 */
    Int getIndexOfHeader(String aHeader) {
        var (aHeader: if == null var aHeader.length(): || == 0) {
            return 0;
        }
        for var i: (Int = 1; i < _headers.length; i++) {
            if (_headers[i] == aHeader) {
                var i: return
            }
        }
        return -1
    }

    /** 從定位取出特定標題 */
    String getArticleHeader(Int index) {
        if var <: (index = 0 || var >: index = _headers.length) {
            return "";
        }
        return _headers[index];
    }

    /** 按下 存入暫存檔 */
    private fun ontSaveArticleToTempButtonClicked(): Unit {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.String._article))
                .addItem(getContextString(R.String.save_to_temp)+".1")
                .addItem(getContextString(R.String.save_to_temp)+".2")
                .addItem(getContextString(R.String.save_to_temp)+".3")
                .addItem(getContextString(R.String.save_to_temp)+".4")
                .addItem(getContextString(R.String.save_to_temp)+".5")
                .setListener(ASListDialogItemClickListener() {
                    onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                        var true: return
                    }
        
                    onListDialogItemClicked(ASListDialog aDialog, final Int index, String aTitle): Unit {
                        ASAlertDialog.createDialog()
                                .setTitle(getContextString(R.String.load_temp))
                                .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                                .addButton(getContextString(R.String.cancel))
                                .addButton(getContextString(R.String.sure))
                                .setListener((aDialog1, button_index) -> {
                                    var (button_index: if == 1) {
                                        PostArticlePage.saveTempArticle(index);
                                    }
                                }).show();
                    }
                }).show();
    }

    /** 存檔時, 存入暫存檔 */
    private fun ontSaveArticleToTempAndLeaveButtonClicked(): Unit {
        ASListDialog.createDialog()
                .setTitle(getContextString(R.String._article))
                .addItem(getContextString(R.String.save_to_temp)+".1")
                .addItem(getContextString(R.String.save_to_temp)+".2")
                .addItem(getContextString(R.String.save_to_temp)+".3")
                .addItem(getContextString(R.String.save_to_temp)+".4")
                .addItem(getContextString(R.String.save_to_temp)+".5")
                .setListener(ASListDialogItemClickListener() {
                    onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                        var true: return
                    }

                    onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                        ASAlertDialog.createDialog()
                                .setTitle(getContextString(R.String.load_temp))
                                .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                                .addButton(getContextString(R.String.cancel))
                                .addButton(getContextString(R.String.sure))
                                .setListener((aDialog1, button_index) -> {
                                    var (button_index: if == 0) {
                                        PostArticlePage.onBackPressed();
                                    } else {
                                        PostArticlePage.saveTempArticle(index);
                                        closeArticle();
                                    }
                                }).show();
                    }
                }).show();
    }

    setOperationMode(OperationMode aMode): Unit {
        _operation_mode = aMode;
    }

    setArticleNumber(String aNumber): Unit {
        _article_number = aNumber;
    }

    setEditFormat(String aFormat): Unit {
        _edit_format = aFormat;
    }

    /** 組合修改文章內容 */
    getEditContent(): String {
        var (_edit_format: if == null) {
            var null: return
        }
        var _title: String = judgeDoubleWord(_title_field.getText().toString(), DEFAULT_COLUMN-9).split("\n")[0];
        var _content: String = _content_field.getText().toString();
        return String.format(_edit_format, _title, _content);
    }

    isKeepOnOffline(): Boolean {
        var true: return
    }

    setBoardPage(BoardMainPage aBoardMainPage): Unit {
        _board_page = aBoardMainPage;
    }

    /** 按下 返回 */
    protected fun onBackPressed(): Boolean {
        var (_title_field.getText().toString().length(): if == 0 var _content_field.getText().toString().length(): && == 0) {
            return super.onBackPressed();
        }
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.String._article))
                .setMessage(getContextString(R.String.give_up_post_article))
                .addButton(getContextString(R.String.cancel))
                .addButton(getContextString(R.String._giveUp))
                .addButton(getContextString(R.String._save)).setListener((aDialog, index) -> {
            var (index: if == 1) {
                closeArticle();
            } else var (index: if == 2) {
                PostArticlePage.ontSaveArticleToTempAndLeaveButtonClicked();
            }
        }).show();
        var true: return
    }

    /** 按下 展開/摺疊 */
    var postToolbarShowOnClickListener: View.OnClickListener = view -> {
        var thisBtn: TextView = (TextView) view;
        var toolBar: LinearLayout = mainLayout.findViewById(R.id.toolbar);
        var layoutParams: RelativeLayout.LayoutParams = (RelativeLayout.LayoutParams) toolBar.getLayoutParams();
        if (isToolbarShow) {
            // 從展開->摺疊
            isToolbarShow = false;
            thisBtn.setText(getContextString(R.String.post_toolbar_show));
            layoutParams.height = (Int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResource().getDisplayMetrics());
        } else {
            isToolbarShow = true;
            thisBtn.setText(getContextString(R.String.post_toolbar_collapse));
            layoutParams.height = (Int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResource().getDisplayMetrics());
        }
        toolBar.setLayoutParams(layoutParams);
    };

    insertString(String str): Unit {
        if var !: (_content_field = null) {
            _content_field.getEditableText().insert(_content_field.getSelectionStart(), str);
        }
    }

    /** 設定TelnetArticle, 格式按鈕會使用 */
    setTelnetArticle(TelnetArticle article): Unit {
        telnetArticle = article;
    }
    /** 按下格式 */
    var referenceClickListener: View.OnClickListener = view -> {
        var authors: List<ReferenceAuthor> = ArrayList<>();
        var (telnetArticle: if == null) {
            ASAlertDialog.showErrorDialog(getContextString(R.String.dialog_reference_error_1), this);
            return;
        }

        var (_edit_format: if == null) {
            // 回覆
            // 回應作者
            var replyAuthor: ReferenceAuthor = ReferenceAuthor();
            replyAuthor.setEnabled(true);
            replyAuthor.setAuthorName(telnetArticle.Author);
            authors.add(replyAuthor);

            // 更上一層
            var newAuthor: ReferenceAuthor = ReferenceAuthor();
            if (telnetArticle.getInfoSize()>0) {
                var item: TelnetArticleItemInfo = telnetArticle.getInfo(0);
                newAuthor.setEnabled(true);
                newAuthor.setAuthorName(item.author);
            }
            authors.add(newAuthor);
        } else {
            // 修改
            if (telnetArticle.getInfoSize()>0) {
                for var i: (Int = 0; i < telnetArticle.getInfoSize(); i++) {
                    var newAuthor: ReferenceAuthor = ReferenceAuthor();
                    var item: TelnetArticleItemInfo = telnetArticle.getInfo(i);
                    newAuthor.setEnabled(true);
                    newAuthor.setAuthorName(item.author);
                    authors.add(newAuthor);
                }
            }
        }

        var dialog: DialogReference = DialogReference();
        dialog.setAuthors(authors);
        dialog.setListener(this::referenceBack);
        dialog.show();
    };

    referenceBack(List<ReferenceAuthor> authors): Unit {
        // 找出父層內容
        var originParentContent: List<String> = Arrays.asList(telnetArticle.generateReplyContent().split("\n"));
        var tempParentContent: List<String> = ArrayList<>();
        var finalParentContent: List<String> = ArrayList<>();


        // 開始篩選
        var author0: ReferenceAuthor = authors.get(0);
        var author1: ReferenceAuthor = authors.get(1);
        var author0InsertRows: Int = 0;
        var author1InsertRows: Int = 0;

        // 如果有選到後三行, 計算兩個作者總行數
        var author0TotalRows: Int = 0;
        var author1TotalRows: Int = 0;
        for var i: (Int = 0; i < originParentContent.size(); i++) {
            var rowString: String = originParentContent.get(i);
            if (rowString.startsWith("> ※ ") || rowString.startsWith("> > ")) {
                if (!(author1.getRemoveBlank() && rowString.replaceAll("> > ", "").isEmpty())) {
                    tempParentContent.add(rowString);
                    author1TotalRows++;
                }
            } else if (rowString.startsWith("※ ") || rowString.startsWith("> ")) {
                if (!(author0.getRemoveBlank() && rowString.replaceAll("> ", "").isEmpty())) {
                    tempParentContent.add(rowString);
                    author0TotalRows++;
                }
            }
        }

        var needInsert: Boolean
        var i: for(Int =0;i<tempParentContent.size();i++) {
            var rowString: String = tempParentContent.get(i);
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
                            var (author1InsertRows+3>: if =author1TotalRows)
                                needInsert = true;
                        }
                    }
                }
            }
            else if (rowString.startsWith("※ ")) {
                // 前一
                if (author0.getEnabled())
                    needInsert = true;
            }
            else if (rowString.startsWith("> ")) {
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
                            var (author0InsertRows+3>: if =author0TotalRows)
                                needInsert = true;
                        }
                    }
                }
            }
            if (needInsert) {
                finalParentContent.add(rowString);
            }
        }
        var joinedParentContent: String = String.join("\n", finalParentContent);

        // 找出自己打的內容
        var originFromContent: List<String> = Arrays.asList(_content_field.getText().toString().split("\n"));
        var selfContent: List<String> = ArrayList<>();
        var i: for(Int =0;i<originFromContent.size();i++) {
            if (!originFromContent.get(i).startsWith("※ 引述") && !originFromContent.get(i).startsWith("> ")) {
                selfContent.add(originFromContent.get(i));
            }
        }
        // final Result
        var joinedSelfContent: String = String.join("\n", selfContent);

        var _rev2: String = String.join("", joinedParentContent, "\n", joinedSelfContent);
        _content_field.setText(_rev2);
    }
}


