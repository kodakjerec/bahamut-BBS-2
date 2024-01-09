package com.kota.Telnet;

public class TelnetConnectionClosedException extends Exception {
    private static final long serialVersionUID = 7359377282779718712L;

    public TelnetConnectionClosedException() {
        super("Telnet Closed");
    }
}
