package com.kota.Telnet

interface TelnetChannelListener {
    fun onTelnetChannelReceiveDataFinished(telnetChannel: TelnetChannel?)

    fun onTelnetChannelReceiveDataStart(telnetChannel: TelnetChannel?)
}
