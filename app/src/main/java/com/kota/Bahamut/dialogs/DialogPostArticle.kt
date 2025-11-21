package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import com.kota.Bahamut.R
import com.kota.asFramework.dialog.ASDialog
import com.kota.telnet.TelnetArticle

class DialogPostArticle(aTarget: Int) : ASDialog(), View.OnClickListener {
    var cancelButton: Button
    var dialogPostArticleListener: DialogPostArticleListener? = null
    var postTargetRadioGroup: RadioGroup
    var sendButton: Button
    var signSpinner: Spinner
    var myTarget: Int

    override val name: String?
        get() = "BahamutBoardsPostArticle"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_post_article)
        if (window != null) window?.setBackgroundDrawable(null)
        this.myTarget = aTarget
        this.postTargetRadioGroup = findViewById<RadioGroup>(R.id.post_target)
        this.sendButton = findViewById<Button>(R.id.send)
        this.cancelButton = findViewById<Button>(R.id.cancel)
        val replyTargetView = findViewById<View>(R.id.reply_target_view)
        findViewById<View?>(R.id.sign_view)
        if (this.myTarget == TelnetArticle.Companion.NEW) {
            replyTargetView.visibility = View.GONE
        } else {
            replyTargetView.visibility = View.VISIBLE
        }
        this.signSpinner = findViewById<Spinner>(R.id.sign_spinner)
        val adapter = ArrayAdapter.createFromResource(
            context,
            R.array.reply_target_list,
            R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.signSpinner.adapter = adapter
        this.sendButton.setOnClickListener(this)
        this.cancelButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val target: String?
        if (view === this.sendButton && this.dialogPostArticleListener != null) {
            val checkedId = this.postTargetRadioGroup.checkedRadioButtonId
            val selectedSign = this.signSpinner.selectedItemPosition
            var sign = ""
            if (selectedSign > 0) {
                sign = (selectedSign - 1).toString()
            }
            target = when (checkedId) {
                R.id.post_to_mail -> {
                    "M"
                }
                R.id.post_to_both -> {
                    "B"
                }
                else -> {
                    "F"
                }
            }
            this.dialogPostArticleListener?.onPostArticleDoneWithTarget(target, sign)
        }
        dismiss()
    }

    fun setListener(listener: DialogPostArticleListener?) {
        this.dialogPostArticleListener = listener
    }
}
