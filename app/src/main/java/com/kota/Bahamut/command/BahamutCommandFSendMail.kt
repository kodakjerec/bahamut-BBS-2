package com.kota.Bahamut.command

import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandFSendMail(var receiver: String) : TelnetCommand() {
    init {
        this.action = BahamutCommandDef.Companion.SEND_MAIL
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (receiver.isNotEmpty()) {
            TelnetClient.myInstance!!.sendDataToServer(
                create()
                    .pushString("FA\n")
                    .pushKey(TelnetKeyboard.CTRL_Y)
                    .pushString(receiver + "\n")
                    .build()
            )
        }
        showProcessingDialog(getContextString(R.string.board_page_send_mail_ing))
    }

    override fun executeFinished(
        telnetListPage: TelnetListPage,
        telnetListPageBlock: TelnetListPageBlock?
    ) {
        isDone = true
        dismissProcessingDialog()
        showShortToast(getContextString(R.string.board_page_send_mail_finish))
    }

    override fun toString(): String {
        return "[FSendMail]"
    }
}
