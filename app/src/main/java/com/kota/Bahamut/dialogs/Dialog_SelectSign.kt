package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.kota.asFramework.dialog.ASDialog
import com.kota.Bahamut.R

class Dialog_SelectSign : ASDialog(), View.OnClickListener {
    var _cancel_button: Button
    var _confirm_button: Button
    var _listener: Dialog_SelectSign_Listener? = null
    var _sign_field: EditText

    val name: String?
        get() = "BahamutSelectSignDialog"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_select_sign)
        if (getWindow() != null) getWindow()!!.setBackgroundDrawable(null)
        setTitle(getContext().getString(R.string.select_sign))
        this._sign_field = findViewById<EditText>(R.id.Bahamut_Dialog_Select_Sign_Input_Field)
        this._confirm_button = findViewById<Button>(R.id.Bahamut_Dialog_Select_Sign_confirm_Button)
        this._cancel_button = findViewById<Button>(R.id.Bahamut_Dialog_Select_Sign_Cancel_Button)
        this._confirm_button.setOnClickListener(this)
        this._cancel_button.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view === this._confirm_button && this._listener != null) {
            this._listener!!.onSelectSign(this._sign_field.getText().toString().replace("\n", ""))
        }
        dismiss()
    }

    fun setListener(listener: Dialog_SelectSign_Listener?) {
        this._listener = listener
    }
}
