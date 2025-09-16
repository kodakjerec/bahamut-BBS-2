package com.kota.telnet

interface TelnetConnectorListener {
    fun onTelnetConnectorClosed(telnetConnector: TelnetConnector?)

    fun onTelnetConnectorConnectFail(telnetConnector: TelnetConnector?)

    fun onTelnetConnectorConnectStart(telnetConnector: TelnetConnector?)

    fun onTelnetConnectorConnectSuccess(telnetConnector: TelnetConnector?)

    fun onTelnetConnectorReceiveDataFinished(telnetConnector: TelnetConnector?)

    fun onTelnetConnectorReceiveDataStart(telnetConnector: TelnetConnector?)
}
