package com.kota.telnet

interface TelnetChannelListener {
    fun onTelnetChannelReceiveDataFinished(telnetChannel: TelnetChannel)

    fun onTelnetChannelReceiveDataStart(telnetChannel: TelnetChannel)
}
