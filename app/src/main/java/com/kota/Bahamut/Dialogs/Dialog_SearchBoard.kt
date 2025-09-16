package com.kota.Bahamut.Dialogs

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.Bahamut.R

class Dialog_SearchBoard : ASDialog(), View.OnClickListener {
    var _cancel_button: Button
    var _keyword_label: EditText
    var _listener: Dialog_SearchBoard_Listener? = null
    var _search_button: Button

    val name: String?
        get() = "BahamutBoardsSearchDialog"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_search_board)
        if (getWindow() != null) getWindow()!!.setBackgroundDrawable(null)
        setTitle("搜尋看板")
        this._keyword_label = findViewById<EditText>(R.id.Bahamut_Dialog_Search_board_keyword)
        this._search_button = findViewById<Button>(R.id.Bahamut_Dialog_Search_board_Search_Button)
        this._cancel_button = findViewById<Button>(R.id.Bahamut_Dialog_Search_board_Cancel_Button)
        this._search_button.setOnClickListener(this)
        this._cancel_button.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view === this._search_button && this._listener != null) {
            this._listener!!.onSearchButtonClickedWithKeyword(
                this._keyword_label.getText().toString().replace("\n", "")
            )
        }
        dismiss()
    }

    fun setListener(listener: Dialog_SearchBoard_Listener?) {
        this._listener = listener
    }
}
