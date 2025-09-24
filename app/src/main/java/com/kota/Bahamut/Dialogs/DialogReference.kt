package com.kota.Bahamut.Dialogs

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.forEachIndexed
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.Bahamut.DataModels.ReferenceAuthor
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.NotificationSettings

class DialogReference : ASDialog(), View.OnClickListener {
    var mainLayout: LinearLayout
    private var cancelButton: Button
    private var sendButton: Button
    private var dialogReferenceListener: DialogReferenceListener? = null
    private lateinit var myAuthors: List<ReferenceAuthor>

    override fun getName(): String {
        return "BahamutSelectSignDialog"
    }

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_reference)
        window?.setBackgroundDrawable(null)
        mainLayout = findViewById(R.id.dialog_reference_layout)

        sendButton = mainLayout.findViewById(R.id.Dialog_reference_Send_Button)
        cancelButton = mainLayout.findViewById(R.id.Dialog_reference_Cancel_Button)
        sendButton.setOnClickListener(this)
        cancelButton.setOnClickListener(this)

        setDialogWidth()

        readOldSettings()
    }

    /**
     * 讀取暫存設定
     * - 讀取先前的 RemoveBlank 和 ReservedType 設定
     */
    private fun readOldSettings() {
        // 讀取先前設定
        val checkBoxAuthor0: CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author0_removeBlank_checkbox)
        val checkBoxAuthor1: CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author1_removeBlank_checkbox)
        val rbs0: RadioGroup = mainLayout.findViewById(R.id.Dialog_reference_author0_reservedType)
        val rbs1: RadioGroup = mainLayout.findViewById(R.id.Dialog_reference_author1_reservedType)

        checkBoxAuthor0.isChecked = NotificationSettings.getDialogReferenceAuthor0RemoveBlank()
        rbs0.check(rbs0.getChildAt(NotificationSettings.getDialogReferenceAuthor0ReservedType()).id)
        checkBoxAuthor1.isChecked = NotificationSettings.getDialogReferenceAuthor1RemoveBlank()
        rbs1.check(rbs1.getChildAt(NotificationSettings.getDialogReferenceAuthor1ReservedType()).id)
    }

    /**
     * 儲存暫存設定
     * - 儲存 RemoveBlank 和 ReservedType 設定
     */
    private fun saveSettings() {
        // 儲存設定
        val checkBoxAuthor0: CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author0_removeBlank_checkbox)
        val checkBoxAuthor1: CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author1_removeBlank_checkbox)
        val rbs0: RadioGroup = mainLayout.findViewById(R.id.Dialog_reference_author0_reservedType)
        val rbs1: RadioGroup = mainLayout.findViewById(R.id.Dialog_reference_author1_reservedType)

        NotificationSettings.setDialogReferenceAuthor0RemoveBlank(checkBoxAuthor0.isChecked)
        NotificationSettings.setDialogReferenceAuthor1RemoveBlank(checkBoxAuthor1.isChecked)

        // Get selected radio button index
        rbs0.forEachIndexed { index, view ->
            if (view is RadioButton && view.isChecked) {
                NotificationSettings.setDialogReferenceAuthor0ReservedType(index)
            }
        }
        rbs1.forEachIndexed { index, view ->
            if (view is RadioButton && view.isChecked) {
                NotificationSettings.setDialogReferenceAuthor1ReservedType(index)
            }
        }
    }

    // 變更dialog寬度
    private fun setDialogWidth() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.7).toInt()
        val oldLayoutParams: ViewGroup.LayoutParams = mainLayout.layoutParams
        oldLayoutParams.width = dialogWidth
        mainLayout.layoutParams = oldLayoutParams
    }

    override fun onClick(view: View) {
        if (view === sendButton && dialogReferenceListener != null) {
            // 回收資料
            val checkBoxAuthor0:CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author0)
            val checkBoxAuthor1:CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author1)

            myAuthors[0].enabled = checkBoxAuthor0.isChecked
            if (checkBoxAuthor0.isChecked) {
                val author = myAuthors[0]
                // 去除空白行
                val checkboxRemoveBlank:CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author0_removeBlank_checkbox)
                author.removeBlank = checkboxRemoveBlank.isChecked
                // 保留行數
                val rbs: RadioGroup = mainLayout.findViewById(R.id.Dialog_reference_author0_reservedType)
                rbs.forEachIndexed { index, rbsView ->
                    if (rbsView is RadioButton) {
                        if (rbsView.isChecked) {
                            author.reservedType = index
                        }
                    }
                }
            }

            myAuthors[1].enabled = checkBoxAuthor1.isChecked
            if (checkBoxAuthor1.isChecked) {
                val author = myAuthors[1]
                // 去除空白行
                val checkboxRemoveBlank:CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author1_removeBlank_checkbox)
                author.removeBlank = checkboxRemoveBlank.isChecked
                // 保留行數
                val rbs: RadioGroup = mainLayout.findViewById(R.id.Dialog_reference_author1_reservedType)
                rbs.forEachIndexed { index, rbsView ->
                    if (rbsView is RadioButton) {
                        if (rbsView.isChecked) {
                            author.reservedType = index
                        }
                    }
                }
            }

            dialogReferenceListener!!.onSelectAuthor(myAuthors)
        }
        saveSettings()
        dismiss()
    }

    @SuppressLint("SetTextI18n")
    fun setAuthors(authors:List<ReferenceAuthor>) {
        myAuthors = authors
        val checkBoxAuthor0:CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author0)
        val checkBoxAuthor0Layout:LinearLayout = mainLayout.findViewById(R.id.Dialog_reference_author0_subLayout)
        val checkBoxAuthor1:CheckBox = mainLayout.findViewById(R.id.Dialog_reference_author1)
        val checkBoxAuthor1Layout:LinearLayout = mainLayout.findViewById(R.id.Dialog_reference_author1_subLayout)
        val checkBoxAuthorNone:CheckBox = mainLayout.findViewById(R.id.Dialog_reference_authorNone)

        // reset
        checkBoxAuthor0.visibility = View.GONE
        checkBoxAuthor0Layout.visibility = View.GONE
        checkBoxAuthor1.visibility = View.GONE
        checkBoxAuthor1Layout.visibility = View.GONE

        // 前一
        if (myAuthors[0].enabled) {
            val author:ReferenceAuthor = myAuthors[0]
            checkBoxAuthor0.visibility = View.VISIBLE
            checkBoxAuthor0Layout.visibility = View.VISIBLE

            // 作者名稱
            checkBoxAuthor0.text = checkBoxAuthor0.text.toString() + author.authorName
            checkBoxAuthor0.isChecked = author.enabled
            checkBoxAuthor0.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxAuthor0Layout.visibility = View.VISIBLE
                    checkBoxAuthorNone.isChecked = false
                } else {
                    checkBoxAuthor0Layout.visibility = View.INVISIBLE
                    if (!checkBoxAuthor1.isChecked) {
                        checkBoxAuthorNone.isChecked = true
                    }
                }
            }
        }

        // 前二
        if (myAuthors[1].enabled) {
            val author:ReferenceAuthor = myAuthors[1]
            checkBoxAuthor1.visibility = View.VISIBLE
            checkBoxAuthor1Layout.visibility = View.VISIBLE

            // 作者名稱
            checkBoxAuthor1.text = checkBoxAuthor1.text.toString() + author.authorName
            checkBoxAuthor1.isChecked = author.enabled
            checkBoxAuthor1.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxAuthor1Layout.visibility = View.VISIBLE
                    checkBoxAuthorNone.isChecked = false
                } else {
                    checkBoxAuthor1Layout.visibility = View.INVISIBLE
                    if (!checkBoxAuthor0.isChecked) {
                        checkBoxAuthorNone.isChecked = true
                    }
                }
            }
        }
        // 無
        checkBoxAuthorNone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxAuthor0.isChecked = false
                checkBoxAuthor1.isChecked = false
            }
        }
    }

    fun setListener(listener: DialogReferenceListener?) {
        dialogReferenceListener = listener
    }
}
