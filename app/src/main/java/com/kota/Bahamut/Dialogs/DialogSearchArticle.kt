package com.kota.Bahamut.Dialogs

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions
import java.util.Objects
import java.util.Vector

class DialogSearchArticle : ASDialog(), View.OnClickListener {
    private var mainLayout: LinearLayout
    private var authorBlock: LinearLayout
    private var authorLabel: EditText
    private var cancelButton: Button
    private var searchButton: Button
    private var gyField: EditText
    private var headerBlock: LinearLayout
    private var keywordBlock: LinearLayout
    private var markBlock: LinearLayout
    private var keywordLabel: EditText
    private var markRadio: RadioGroup
    private lateinit var listener: DialogSearchArticleListener
    override fun getName(): String {
        return "BahamutBoardSearchDialog"
    }

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_search_article)
        Objects.requireNonNull(window)!!.setBackgroundDrawable(null)
        setTitle("搜尋文章")

        mainLayout = findViewById(R.id.dialog_search_article_layout)
        keywordLabel = mainLayout.findViewById(R.id.Bahamut_Dialog_Search_keyword)
        authorLabel = mainLayout.findViewById(R.id.Bahamut_Dialog_Search_Author)
        markRadio = mainLayout.findViewById(R.id.Bahamut_Dialog_Search_mark)
        gyField = mainLayout.findViewById(R.id.gy_number_field)
        searchButton = mainLayout.findViewById(R.id.Bahamut_Dialog_Search_Search_Button)
        cancelButton = mainLayout.findViewById(R.id.Bahamut_Dialog_Search_Cancel_Button)
        headerBlock = mainLayout.findViewById(R.id.SearchArticleDialog_headerBlock)
        keywordBlock = mainLayout.findViewById(R.id.SearchArticleDialog_keywordBlock)
        authorBlock = mainLayout.findViewById(R.id.SearchArticleDialog_AuthorBlock)
        markBlock = mainLayout.findViewById(R.id.SearchArticleDialog_markBlock)
        searchButton.setOnClickListener(this)
        cancelButton.setOnClickListener(this)

        setDialogWidth()
    }

    override fun onClick(view: View) {
        if (view === searchButton) {
            val searchOptions = Vector<String>()
            val keyword = keywordLabel.text.toString().replace("\n", "")
            val author = authorLabel.text.toString().replace("\n", "")
            var mark = "NO"
            if (markRadio.checkedRadioButtonId == R.id.Bahamut_Dialog_Search_mark_YES) {
                mark = "YES"
            }
            val gy = gyField.text.toString()
            searchOptions.add(keyword)
            searchOptions.add(author)
            searchOptions.add(mark)
            searchOptions.add(gy)
            if (keyword.isEmpty() && author.isEmpty() && gy.isEmpty()) {
                ASToast.showShortToast(CommonFunctions.getContextString(R.string.input_search_article))
                return
            }
            listener.onSearchDialogSearchButtonClickedWithValues(searchOptions)
        }
        dismiss()
        listener.onSearchDialogCancelButtonClicked()
    }

    fun setListener(listener: DialogSearchArticleListener) {
        this.listener = listener
    }

    fun editContent(searchOptions: Vector<String?>) {
        setTitle("修改搜尋內容")
        searchButton.text = "確定"
        keywordLabel.setText(searchOptions[0])
        authorLabel.setText(searchOptions[1])
        if (searchOptions[2] == "y") markRadio.check(R.id.Bahamut_Dialog_Search_mark_YES)
        gyField.setText(searchOptions[3])
    }

    /** 變更dialog寬度 */
    private fun setDialogWidth() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.7).toInt()
        val oldLayoutParams = mainLayout.layoutParams
        oldLayoutParams.width = dialogWidth
        mainLayout.layoutParams = oldLayoutParams
    }
}
