package com.kota.Telnet;

import androidx.annotation.NonNull;

public class TelnetCommand {
    public static final byte AUTH = 37;
    public static final byte DO = -3;
    public static final byte DONT = -2;
    public static final byte ECHO = 1;
    public static final byte IAC = -1;
    public static final byte IS = 0;
    public static final byte NAWS = 31;
    public static final byte NEW_ENV = 39;
    public static final byte SB = -6;
    public static final byte SE = -16;
    public static final byte SG = 3;
    public static final byte TERMINAL_TYPE = 24;
    public static final byte WILL = -5;
    public static final byte WONT = -4;
    public byte action = 0;
    public byte header = 0;
    public byte option = 0;

    public TelnetCommand() {
    }

    public TelnetCommand(byte aHeader, byte aAction, byte aOption) {
        this.header = aHeader;
        this.action = aAction;
        this.option = aOption;
    }

    public boolean isEqualTo(TelnetCommand aCommand) {
        return aCommand.header == this.header && aCommand.action == this.action && aCommand.option == this.option;
    }

    public boolean isEqualTo(int aHeader, int aAction, int aOption) {
        return aHeader == this.header && aAction == this.action && aOption == this.option;
    }

    public String getCommandNameString(int aCommand) {
        switch (aCommand) {
            case -16:
                return "SE";
            case -6:
                return "SB";
            case -5:
                return "WILL";
            case -4:
                return "WONT";
            case -3:
                return "DO";
            case -2:
                return "DONT";
            case -1:
                return "IAC";
            case 0:
                return "IS";
            case 1:
                return "ECHO";
            case 3:
                return "SG";
            case 24:
                return "TERMINAL_TYPE";
            case 31:
                return "NAWS";
            case 37:
                return "AUTH";
            case 39:
                return "NEW_ENV";
            default:
                return "UNKNOW(" + aCommand + ")";
        }
    }

    @NonNull
    public String toString() {
        return getCommandNameString(this.header) + "," + getCommandNameString(this.action) + "," + getCommandNameString(this.option);
    }
}
