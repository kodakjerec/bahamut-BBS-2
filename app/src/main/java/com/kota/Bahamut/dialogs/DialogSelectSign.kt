package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.kota.asFramework.dialog.ASDialog
import com.kota.Bahamut.R

class DialogSelectSign : ASDialog(), View.OnClickListener {
    var cancelButton: Button
    var confirmButton: Button
    var listener: DialogSelectSignListener? = null
    var signField: EditText

    override val name: String?
        get() = "BahamutSelectSignDialog"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_select_sign)
        if (window != null) window?.setBackgroundDrawable(null)
        setTitle(context.getString(R.string.select_sign))
        this.signField = findViewById<EditText>(R.id.Bahamut_Dialog_Select_Sign_Input_Field)
        this.confirmButton = findViewById<Button>(R.id.Bahamut_Dialog_Select_Sign_confirm_Button)
        this.cancelButton = findViewById<Button>(R.id.Bahamut_Dialog_Select_Sign_Cancel_Button)
        this.confirmButton.setOnClickListener(this)
        this.cancelButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view === this.confirmButton && this.listener != null) {
            this.listener?.onSelectSign(this.signField.text.toString().replace("\n", ""))
        }
        dismiss()
    }

    fun setListener(listener: DialogSelectSignListener?) {
        this.listener = listener
    }
}
