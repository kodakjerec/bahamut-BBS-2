package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import com.kota.asFramework.dialog.ASDialog
import com.kota.Bahamut.R
import com.kota.telnet.TelnetArticle

class Dialog_PostArticle(aTarget: Int) : ASDialog(), View.OnClickListener {
    var _cancel_button: Button
    var _listener: Dialog_PostArticle_Listener? = null
    var _post_target_group: RadioGroup
    var _send_button: Button
    var _sign_spinner: Spinner
    var _target: Int

    val name: String?
        get() = "BahamutBoardsPostArticle"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_post_article)
        if (getWindow() != null) getWindow()!!.setBackgroundDrawable(null)
        this._target = aTarget
        this._post_target_group = findViewById<RadioGroup>(R.id.post_target)
        this._send_button = findViewById<Button>(R.id.send)
        this._cancel_button = findViewById<Button>(R.id.cancel)
        val reply_target_view = findViewById<View>(R.id.reply_target_view)
        findViewById<View?>(R.id.sign_view)
        if (this._target == TelnetArticle.Companion.NEW) {
            reply_target_view.setVisibility(View.GONE)
        } else {
            reply_target_view.setVisibility(View.VISIBLE)
        }
        this._sign_spinner = findViewById<Spinner>(R.id.sign_spinner)
        val adapter = ArrayAdapter.createFromResource(
            getContext(),
            R.array.reply_target_list,
            R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this._sign_spinner.setAdapter(adapter)
        this._send_button.setOnClickListener(this)
        this._cancel_button.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val target: String?
        if (view === this._send_button && this._listener != null) {
            val checked_id = this._post_target_group.getCheckedRadioButtonId()
            val selected_sign = this._sign_spinner.getSelectedItemPosition()
            var sign = ""
            if (selected_sign > 0) {
                sign = (selected_sign - 1).toString()
            }
            if (checked_id == R.id.post_to_mail) {
                target = "M"
            } else if (checked_id == R.id.post_to_both) {
                target = "B"
            } else {
                target = "F"
            }
            this._listener!!.onPostArticleDoneWithTarger(target, sign)
        }
        dismiss()
    }

    fun setListener(listener: Dialog_PostArticle_Listener?) {
        this._listener = listener
    }
}
