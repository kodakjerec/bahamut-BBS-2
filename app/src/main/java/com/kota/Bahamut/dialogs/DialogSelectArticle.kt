package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast.showShortToast

class DialogSelectArticle : ASDialog(), View.OnClickListener {
    var button0p: Button
    var button1p: Button
    var button2p: Button
    var button3p: Button
    var button4p: Button
    var button5p: Button
    var button6p: Button
    var button7p: Button
    var button8p: Button
    var button9p: Button
    var buttonBackSpaceP: Button
    var cancelButton: Button
    var content: TextView
    var contentString: String = ""
    var dialogSelectArticleListener: DialogSelectArticleListener? = null
    var searchButton: Button

    override val name: String?
        get() = "BahamutBoardSelectDialog"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_select_article)
        if (window != null) window?.setBackgroundDrawable(null)
        this.content = findViewById<TextView>(R.id.Bahamut_Dialog_Select_content_Label)
        this.button0p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_0_p)
        this.button1p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_1_p)
        this.button2p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_2_p)
        this.button3p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_3_p)
        this.button4p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_4_p)
        this.button5p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_5_p)
        this.button6p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_6_p)
        this.button7p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_7_p)
        this.button8p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_8_p)
        this.button9p = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_9_p)
        this.buttonBackSpaceP =
            findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_backSpace_p)
        this.searchButton = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_Search)
        this.cancelButton = findViewById<Button>(R.id.Bahamut_Dialog_Select_Button_Cancel)
        this.button0p.setOnClickListener(this)
        this.button1p.setOnClickListener(this)
        this.button2p.setOnClickListener(this)
        this.button3p.setOnClickListener(this)
        this.button4p.setOnClickListener(this)
        this.button5p.setOnClickListener(this)
        this.button6p.setOnClickListener(this)
        this.button7p.setOnClickListener(this)
        this.button8p.setOnClickListener(this)
        this.button9p.setOnClickListener(this)
        this.buttonBackSpaceP.setOnClickListener(this)
        this.searchButton.setOnClickListener(this)
        this.cancelButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view === this.button0p) {
            this.contentString += 0
        } else if (view === this.button1p) {
            this.contentString += 1
        } else if (view === this.button2p) {
            this.contentString += 2
        } else if (view === this.button3p) {
            this.contentString += 3
        } else if (view === this.button4p) {
            this.contentString += 4
        } else if (view === this.button5p) {
            this.contentString += 5
        } else if (view === this.button6p) {
            this.contentString += 6
        } else if (view === this.button7p) {
            this.contentString += 7
        } else if (view === this.button8p) {
            this.contentString += 8
        } else if (view === this.button9p) {
            this.contentString += 9
        } else if (view === this.buttonBackSpaceP) {
            if (this.contentString.length > 0) {
                this.contentString =
                    this.contentString.substring(0, this.contentString.length - 1)
            }
        } else if (view === this.searchButton) {
            if (this.contentString.isEmpty()) {
                showShortToast(getContextString(R.string.please_input_article_number))
                return
            }
            if (this.dialogSelectArticleListener != null) {
                this.dialogSelectArticleListener?.onSelectDialogDismissWIthIndex(this.contentString)
            }
            dismiss()
        } else if (view === this.cancelButton) {
            dismiss()
        }
        if (this.contentString.length > 5) {
            this.contentString = this.contentString.substring(0, 5)
        }
        this.content.text = this.contentString
    }

    fun setListener(listener: DialogSelectArticleListener?) {
        this.dialogSelectArticleListener = listener
    }

    public override fun show() {
        super.show()
    }
}
