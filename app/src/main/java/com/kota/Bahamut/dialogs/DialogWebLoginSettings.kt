package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.login.WebAutoSignInManager
import com.kota.Bahamut.pages.model.PostEditText
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast

class DialogWebLoginSettings(private val onSaved: (() -> Unit)? = null) : ASDialog(), View.OnClickListener {
    private var mainLayout: FrameLayout
    private var usernameEdit: PostEditText
    private var passwordEdit: PostEditText
    private var debugViewCheckbox: CheckBox
    private var btnCancel: Button
    private var btnSave: Button

    override val name: String?
        get() = "BahamutWebLoginSettingsDialog"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_web_login_settings)
        window?.setBackgroundDrawable(null)
        setTitle(context.getString(R.string.login_web_settings_title))

        mainLayout = findViewById(R.id.dialog_web_login_settings_layout)
        usernameEdit = mainLayout.findViewById(R.id.dialog_web_login_username)
        passwordEdit = mainLayout.findViewById(R.id.dialog_web_login_password)
        debugViewCheckbox = mainLayout.findViewById(R.id.dialog_web_login_debug_view_checkbox)
        btnCancel = mainLayout.findViewById(R.id.dialog_web_login_btn_cancel)
        btnSave = mainLayout.findViewById(R.id.dialog_web_login_btn_save)

        // 載入當前設定
        usernameEdit.setText(UserSettings.propertiesWebUsername)
        passwordEdit.setText(UserSettings.propertiesWebPassword)
        debugViewCheckbox.isChecked = WebAutoSignInManager.showDebugView

        // 點擊文字切換 CheckBox
        mainLayout.findViewById<View>(R.id.dialog_web_login_debug_view_label)?.setOnClickListener {
            debugViewCheckbox.isChecked = !debugViewCheckbox.isChecked
        }

        btnCancel.setOnClickListener(this)
        btnSave.setOnClickListener(this)

        setDialogWidth(mainLayout)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialog_web_login_btn_save -> {
                val newUsername = usernameEdit.text.toString().trim()
                val newPassword = passwordEdit.text.toString()

                UserSettings.propertiesWebUsername = newUsername
                UserSettings.propertiesWebPassword = newPassword
                WebAutoSignInManager.showDebugView = debugViewCheckbox.isChecked

                ASToast.showShortToast(context.getString(R.string.login_web_settings_saved))
                dismiss()
                onSaved?.invoke()
            }
            R.id.dialog_web_login_btn_cancel -> {
                dismiss()
            }
        }
    }
}

