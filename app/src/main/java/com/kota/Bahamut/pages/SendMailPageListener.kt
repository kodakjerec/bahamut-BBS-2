package com.kota.Bahamut.pages

interface SendMailPageListener {
    fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    )
}
