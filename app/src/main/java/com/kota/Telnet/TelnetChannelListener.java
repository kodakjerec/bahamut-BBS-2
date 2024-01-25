package com.kota.Telnet;

public interface TelnetChannelListener {
    void onTelnetChannelReceiveDataFinished(TelnetChannel telnetChannel);

    void onTelnetChannelReceiveDataStart(TelnetChannel telnetChannel);
}
