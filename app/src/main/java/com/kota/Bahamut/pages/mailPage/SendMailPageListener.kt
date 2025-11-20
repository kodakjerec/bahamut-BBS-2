package com.kota.Bahamut.pages.mailPage

interface SendMailPageListener {
    fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    )
}
