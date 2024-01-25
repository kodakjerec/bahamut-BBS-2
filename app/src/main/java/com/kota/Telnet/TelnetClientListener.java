package com.kota.Telnet;

public interface TelnetClientListener {
    void onTelnetClientConnectionClosed(TelnetClient telnetClient);

    void onTelnetClientConnectionFail(TelnetClient telnetClient);

    void onTelnetClientConnectionStart(TelnetClient telnetClient);

    void onTelnetClientConnectionSuccess(TelnetClient telnetClient);
}
