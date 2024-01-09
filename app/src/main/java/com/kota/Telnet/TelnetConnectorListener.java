package com.kota.Telnet;

public interface TelnetConnectorListener {
    void onTelnetConnectorClosed(TelnetConnector telnetConnector);

    void onTelnetConnectorConnectFail(TelnetConnector telnetConnector);

    void onTelnetConnectorConnectStart(TelnetConnector telnetConnector);

    void onTelnetConnectorConnectSuccess(TelnetConnector telnetConnector);

    void onTelnetConnectorReceiveDataFinished(TelnetConnector telnetConnector);

    void onTelnetConnectorReceiveDataStart(TelnetConnector telnetConnector);
}
