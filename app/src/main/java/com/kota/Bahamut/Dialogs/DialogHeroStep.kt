package com.kota.Bahamut.Dialogs

import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.Pages.Model.PostEditText
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import java.util.Objects

class DialogHeroStep:ASDialog() {
    private var mainLayout: LinearLayout
    private var content1: PostEditText
    private var content2: PostEditText
    private var content3: PostEditText
    private var btnCancel: Button
    private var btnSend: Button
    private var isClickButton = false

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_hero_step)
        Objects.requireNonNull(window)!!.setBackgroundDrawable(null)

        mainLayout = findViewById(R.id.content_view)
        content1 = mainLayout.findViewById(R.id.Dialog_hero_step_content1)
        content2 = mainLayout.findViewById(R.id.Dialog_hero_step_content2)
        content3 = mainLayout.findViewById(R.id.Dialog_hero_step_content3)
        btnCancel = mainLayout.findViewById(R.id.Dialog_hero_step_cancel)
        btnSend = mainLayout.findViewById(R.id.Dialog_hero_step_send)
    }

    override fun show() {
        btnCancel.setOnClickListener(cancelOnClickListener)
        btnSend.setOnClickListener(sendOnClickListener)
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
        // 不是正常的按按鈕消失
        if (!isClickButton) {
            val builder = TelnetOutputBuilder.create()
                .pushString("\n") // 按[Enter]結束
                .pushString("\n") // (S)存檔觀賞 (E)重新來過 (Q)算了？[S]
                .build()
            TelnetClient.getClient().sendDataToServer(builder)
        }
    }

    /** 關閉勇者足跡 */
    private val cancelOnClickListener = OnClickListener {_->
        isClickButton = true
        val builder = TelnetOutputBuilder.create()
            .pushString("\n") // 按[Enter]結束
            .pushString("\n") // (S)存檔觀賞 (E)重新來過 (Q)算了？[S]
            .build()
        TelnetClient.getClient().sendDataToServer(builder)
        dismiss()
    }

    /** 送出留言 */
    private val sendOnClickListener = OnClickListener {_->
        isClickButton = true
        var sendContent = ""
        if (content1.text!!.isNotEmpty())
            sendContent += content1.text.toString()+"\n"
        if (content2.text!!.isNotEmpty())
            sendContent += content2.text.toString()+"\n"
        if (content3.text!!.isNotEmpty())
            sendContent += content3.text.toString()+"\n"
        if (sendContent.isNotEmpty()) {
            val builder = TelnetOutputBuilder.create()
                .pushString(sendContent)
                .pushString("\n") // 按[Enter]結束
                .pushString("\n") // (S)存檔觀賞 (E)重新來過 (Q)算了？[S]
                .build()
            TelnetClient.getClient().sendDataToServer(builder)
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.main_hero_success01))
        } else {
            val builder = TelnetOutputBuilder.create()
                .pushString("\n") // 按[Enter]結束
                .pushString("\n") // (S)存檔觀賞 (E)重新來過 (Q)算了？[S]
                .build()
            TelnetClient.getClient().sendDataToServer(builder)
        }
        dismiss()
    }

}