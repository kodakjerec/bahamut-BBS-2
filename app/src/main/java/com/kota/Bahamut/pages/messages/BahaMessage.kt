package com.kota.Bahamut.pages.messages

enum class MessageStatus {
    Unknown,
    Default,
    Success,
    CloseBBCall,
    Escape,
    Offline
}
class BahaMessage {
    var id: Long = -1
    var senderName = ""
    var message = ""
    var receivedDate: Long = 0
    var readDate: Long = 0
    var type = 0 // 0-receive 1-send
    var status = MessageStatus.Default
}