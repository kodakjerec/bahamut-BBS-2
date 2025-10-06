package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.kota.Bahamut.R
import com.kota.asFramework.dialog.ASDialog

class DialogSearchBoard : ASDialog(), View.OnClickListener {
    var cancelButton: Button
    var keywordLabel: EditText
    var dialogSearchBoardListener: DialogSearchBoardListener? = null
    var searchButton: Button

    override val name: String?
        get() = "BahamutBoardsSearchDialog"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_search_board)
        if (window != null) window?.setBackgroundDrawable(null)
        setTitle("搜尋看板")
        this.keywordLabel = findViewById<EditText>(R.id.Bahamut_Dialog_Search_board_keyword)
        this.searchButton = findViewById<Button>(R.id.Bahamut_Dialog_Search_board_Search_Button)
        this.cancelButton = findViewById<Button>(R.id.Bahamut_Dialog_Search_board_Cancel_Button)
        this.searchButton.setOnClickListener(this)
        this.cancelButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view === this.searchButton && this.dialogSearchBoardListener != null) {
            this.dialogSearchBoardListener?.onSearchButtonClickedWithKeyword(
                this.keywordLabel.text.toString().replace("\n", "")
            )
        }
        dismiss()
    }

    fun setListener(listener: DialogSearchBoardListener?) {
        this.dialogSearchBoardListener = listener
    }
}
