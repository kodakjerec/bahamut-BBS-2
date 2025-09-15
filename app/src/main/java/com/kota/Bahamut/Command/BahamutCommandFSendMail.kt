package com.kota.Bahamut.Command

import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder

class BahamutCommandFSendMail(private val _receiver: String) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.SendMail
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (_receiver.isNotEmpty()) {
            TelnetClient.getClient().sendDataToServer(
                TelnetOutputBuilder.create()
                    .pushString("FA\n")
                    .pushKey(TelnetKeyboard.CTRL_Y)
                    .pushString("$_receiver\n")
                    .build()
            )
        }
        ASProcessingDialog.showProcessingDialog(getContextString(R.string.board_page_send_mail_ing))
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock) {
        setDone(true)
        ASProcessingDialog.dismissProcessingDialog()
        ASToast.showShortToast(getContextString(R.string.board_page_send_mail_finish))
    }

    override fun toString(): String {
        return "[FSendMail]"
    }
}
