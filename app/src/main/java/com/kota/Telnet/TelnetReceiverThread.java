package com.kota.Telnet;

import android.util.Log;

import com.kota.Telnet.Model.TelnetModel;
import java.io.IOException;

public class TelnetReceiverThread extends Thread {
    public static final int UNSET = -1;
    private TelnetCommand _command = new TelnetCommand();
    private TelnetConnector _connector = null;
    private TelnetModel _model = null;
    private boolean _receiving = true;

    public TelnetReceiverThread(TelnetConnector aConnector, TelnetModel aModel) {
        this._connector = aConnector;
        this._model = aModel;
    }

    public void close() {
        this._receiving = false;
        this._connector = null;
        this._model = null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP:0: B:0:0x0000->B:3:0x0008, LOOP_START, MTH_ENTER_BLOCK] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        do {

        } while (this._receiving && receiveData());
    }

    private byte readData() throws TelnetConnectionClosedException, IOException {
        return this._connector.readData(0);
    }

    private boolean receiveData() {
        boolean result = true;
        try {
            byte data = readData();
            if (data == -1) {
                byte action = readData();
                byte option = readData();
                this._command.header = data;
                this._command.action = action;
                this._command.option = option;
                handleCommand();
            } else if (data == 13) {
                this._model.moveCursorColumnToBegin();
            } else if (data == 10) {
                this._model.moveCursorToNextLine();
            } else if (data == 7) {
                System.out.println("get BEL");
            } else if (data == 8) {
                this._model.moveCursorColumnLeft();
            } else if (data != 27) {
                this._model.pushData(data);
            } else if (readData() == 91) {
                this._model.cleanAnsiBuffer();
                byte data2 = readData();
                this._model.pushAnsiBuffer(data2);
                while (true) {
                    if ((data2 < 48 || data2 > 57) && data2 != 59) {
                        break;
                    }
                    data2 = readData();
                    this._model.pushAnsiBuffer(data2);
                }
                this._model.parseAnsiBuffer();
            }

        } catch (Exception e) {
            Log.v("SocketChannel", "receiveData Exception");
            result = false;
            if (this._connector != null && this._connector.isConnecting()) {
                this._connector.close();
            }
        }
        return result;
    }

    public void handleCommand() throws TelnetConnectionClosedException, IOException {
        if (this._command.isEqualTo(-1, -3, 37)) {
            sendCommandToServer(-1, -4, 37);
        } else if (this._command.isEqualTo(-1, -5, 1)) {
            sendCommandToServer(-1, -2, 1);
        } else if (this._command.isEqualTo(-1, -3, 1)) {
            sendCommandToServer(-1, -4, 1);
        } else if (this._command.isEqualTo(-1, -5, 3)) {
            sendCommandToServer(-1, -2, 3);
        } else if (this._command.isEqualTo(-1, -3, 39)) {
            sendCommandToServer(-1, -4, 39);
        } else if (this._command.isEqualTo(-1, -3, 31)) {
            sendCommandToServer(-1, -4, 31);
        } else if (this._command.isEqualTo(-1, -3, 0)) {
            sendCommandToServer(-1, -4, 0);
        } else if (this._command.isEqualTo(-1, -5, 0)) {
            sendCommandToServer(-1, -2, 0);
        } else if (this._command.isEqualTo(-1, -3, 24)) {
            sendCommandToServer(-1, -5, 24);
        } else if (this._command.isEqualTo(-1, -3, 0)) {
            sendCommandToServer(-1, -4, 0);
        } else if (this._command.isEqualTo(-1, -6, 24)) {
            TelnetClient.getConnector().readData(0);
            TelnetClient.getConnector().readData(0);
            TelnetClient.getConnector().readData(0);
            TelnetOutputBuilder.create().pushData((byte) -1).pushData((byte) -6).pushData((byte) TelnetCommand.TERMINAL_TYPE).pushData((byte) 0).pushData((byte) 65).pushData((byte) 78).pushData((byte) 83).pushData((byte) 73).pushData((byte) -1).pushData((byte) TelnetCommand.SE).sendToServer();
        } else {
            System.out.println("[MuTelnetCommandHandler]Unimplement command : " + this._command.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void sendCommandToServer(int aHeader, int aAction, int aOption) {
        TelnetOutputBuilder.create().pushData((byte) aHeader).pushData((byte) aAction).pushData((byte) aOption).sendToServer();
    }
}
