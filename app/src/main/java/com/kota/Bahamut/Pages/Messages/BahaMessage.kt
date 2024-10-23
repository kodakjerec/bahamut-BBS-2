package com.kota.Bahamut.Pages.Messages

class BahaMessage {
    var senderName = ""
    var message = ""
    var receivedDate: Long = 0
    var readDate: Long = 0
    var type = 0 // 0-receive 1-send
    var status = 0 // 0-Fail 1-Success
}