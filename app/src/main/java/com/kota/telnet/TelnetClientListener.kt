package com.kota.telnet

interface TelnetClientListener {
    fun onTelnetClientConnectionClosed(telnetClient: TelnetClient?)

    fun onTelnetClientConnectionFail(telnetClient: TelnetClient?)

    fun onTelnetClientConnectionStart(telnetClient: TelnetClient?)

    fun onTelnetClientConnectionSuccess(telnetClient: TelnetClient?)
}
