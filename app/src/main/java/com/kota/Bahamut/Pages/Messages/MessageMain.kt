package com.kota.Bahamut.Pages.Messages

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.TelnetUI.TelnetPage

class MessageMain:TelnetPage() {
    private var mainLayout: LinearLayout
    override fun getPageLayout(): Int {
        return R.layout.message_main
    }

    val showHideFloating = CompoundButton.OnCheckedChangeListener {
        _: CompoundButton?, isChecked: Boolean ->
            NotificationSettings.setShowMessageFloating(isChecked)
        }

    init {
        mainLayout = findViewById(R.id.content_view) as LinearLayout
        // 切換浮動隱藏
        val checkBox = mainLayout.findViewById<CheckBox>(R.id.Message_Main_Checkbox)
        checkBox.setOnCheckedChangeListener(showHideFloating)
        val checkLayout = mainLayout.findViewById<LinearLayout>(R.id.Message_Main_CheckboxLayout)
        checkLayout.setOnClickListener { _ -> checkBox.isChecked = !checkBox.isChecked }

        val escTxt = mainLayout.findViewById<TextView>(R.id.Message_Main_Back)
        escTxt.setOnClickListener{ _-> onBackPressed() }
    }

    override fun onPageDidLoad() {
        super.onPageDidLoad()


    }

    override fun onBackPressed(): Boolean {
        return super.onBackPressed()
    }
}