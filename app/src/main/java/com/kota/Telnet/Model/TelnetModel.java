package com.kota.Telnet.Model;

import com.kota.Telnet.Reference.TelnetAnsiCode;
import com.kota.Telnet.TelnetAnsi;
import com.kota.Telnet.TelnetCommand;
import com.kota.Telnet.TelnetCursor;
import java.nio.ByteBuffer;

public class TelnetModel {
    protected TelnetAnsi _ansi;
    private final ByteBuffer _ansi_buffer;
    private TelnetCursor _cursor;
    private TelnetFrame _frame;
    private int _pushed_data_size;
    private int _row;
    private TelnetCursor _saved_cursor;

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public TelnetModel(int row) {
        this._ansi = new TelnetAnsi();
        this._frame = null;
        this._row = 0;
        this._cursor = new TelnetCursor();
        this._saved_cursor = new TelnetCursor();
        this._pushed_data_size = 0;
        this._ansi_buffer = ByteBuffer.allocate(1024);
        this._row = row;
        initialDataModel();
    }

    public TelnetModel() {
        this._ansi = new TelnetAnsi();
        this._frame = null;
        this._row = 0;
        this._cursor = new TelnetCursor();
        this._saved_cursor = new TelnetCursor();
        this._pushed_data_size = 0;
        this._ansi_buffer = ByteBuffer.allocate(1024);
        this._row = 24;
        initialDataModel();
    }

    public void cleanCahcedData() {
        this._frame.cleanCachedData();
    }

    public void clear() {
        this._ansi = new TelnetAnsi();
        this._frame.clear();
        this._cursor = new TelnetCursor();
        this._saved_cursor = new TelnetCursor();
        this._pushed_data_size = 0;
        this._ansi_buffer.clear();
    }

    private void initialDataModel() {
        this._frame = new TelnetFrame(this._row);
    }

    public int getRowSize() {
        return this._row;
    }

    public void cleanFrame() {
        for (int row = 0; row < this._row; row++) {
            for (int column = 0; column < 80; column++) {
                cleanCursor(row, column);
            }
        }
    }

    public void setFrame(TelnetFrame aFrame) {
        this._frame.set(aFrame);
    }

    public boolean getBlink(int row, int column) {
        return this._frame.getRow(row).blink[column];
    }

    public int getData(int row, int column) {
        return this._frame.getRow(row).data[column] & 0xFF;
    }

    public int getTextColor(int row, int column) {
        return TelnetAnsiCode.getTextColor(this._frame.getRow(row).textColor[column]);
    }

    public int getBackgroundColor(int row, int column) {
        return TelnetAnsiCode.getBackgroundColor(this._frame.getRow(row).backgroundColor[column]);
    }

    public TelnetCursor getCursor() {
        return this._cursor;
    }

    private void cleanCursor(int row, int column) {
        if (row >= 0 && row < this._row && column >= 0 && column < 80) {
            this._frame.cleanPositionData(row, column);
        }
    }

    public void saveCursor() {
        this._saved_cursor.set(this._cursor);
    }

    public void restoreCursor() {
        this._cursor.set(this._saved_cursor);
    }

    public void setCursor(int row, int column) {
        setCursorRow(row);
        setCursorColumn(column);
    }

    public void setCursorRow(int aRow) {
        if (aRow < 0) {
            this._cursor.row = 0;
        } else {
            this._cursor.row = Math.min(aRow, this._row - 1);
        }
    }

    public void setCursorColumn(int aColumn) {
        if (aColumn < 0) {
            this._cursor.column = 0;
        } else {
            this._cursor.column = Math.min(aColumn, 79);
        }
    }

    private void setCursorData(byte data, TelnetAnsi ansiState) {
        if (this._cursor.row >= 0 && this._cursor.row < this._row && this._cursor.column >= 0 && this._cursor.column < 80) {
            TelnetRow row = this._frame.getRow(this._cursor.row);
            row.cleanColumn(this._cursor.column);
            row.data[this._cursor.column] = data;
            if (ansiState != null) {
                byte text_color = ansiState.textColor;
                if (ansiState.textBright) {
                    text_color = (byte) (text_color + 8);
                }
                row.textColor[this._cursor.column] = text_color;
                row.backgroundColor[this._cursor.column] = ansiState.backgroundColor;
                row.blink[this._cursor.column] = ansiState.textBlink;
                row.italic[this._cursor.column] = ansiState.textItalic;
            }
        }
    }

    public void pushData(byte data) {
        if (this._cursor.column < 80) {
            this._pushed_data_size++;
            setCursorData(data, this._ansi);
            this._cursor.column++;
        }
    }

    public String getRowString(int row) {
        return this._frame.getRow(row).toString();
    }

    public TelnetRow getRow(int row) {
        if (row < 0 || row >= this._row) {
            return null;
        }
        return this._frame.getRow(row);
    }

    public TelnetRow getLastRow() {
        return this._frame.getLastestRow();
    }

    public TelnetRow getFirstRow() {
        return this._frame.getFirstRow();
    }

    public TelnetFrame getFrame() {
        return this._frame;
    }

    public void cleanFrameAll() {
        this._frame.clear();
    }

    public void cleanFrameToEnd() {
        for (int row = this._cursor.row + 1; row < this._row; row++) {
            for (int column = 0; column < 80; column++) {
                cleanCursor(row, column);
            }
        }
        for (int column2 = this._cursor.column; column2 < 80; column2++) {
            cleanCursor(this._cursor.row, column2);
        }
    }

    public void cleanFrameToBeginning() {
        for (int row = 0; row < this._cursor.row; row++) {
            for (int column = 0; column < 80; column++) {
                cleanCursor(row, column);
            }
        }
        for (int column2 = 0; column2 < this._cursor.column; column2++) {
            cleanCursor(this._cursor.row, column2);
        }
    }

    public void cleanRow(int row) {
        for (int column = 0; column < 80; column++) {
            cleanCursor(row, column);
        }
    }

    public void cleanRowAll() {
        for (int column = 0; column < 80; column++) {
            cleanCursor(this._cursor.row, column);
        }
    }

    public void cleanRowToBeginning() {
        for (int column = 0; column < this._cursor.column; column++) {
            cleanCursor(this._cursor.row, column);
        }
    }

    public void cleanRowToEnd() {
        for (int column = this._cursor.column; column < 80; column++) {
            cleanCursor(this._cursor.row, column);
        }
    }

    public void moveCursorColumnToBegin() {
        this._cursor.column = 0;
    }

    public void moveCursorColumnToEnd() {
        this._cursor.column = 79;
    }

    public void moveCursorRowToBegin() {
        this._cursor.row = 0;
    }

    public void moveCursorRowToEnd() {
        this._cursor.row = 79;
    }

    public void moveCursorColumnLeft() {
        if (this._cursor.column > 0) {
            TelnetCursor telnetCursor = this._cursor;
            telnetCursor.column--;
        }
    }

    public void moveCursorColumnLeft(int n) {
        if (n < 1) {
            n = 1;
        }
        for (int i = 0; i < n; i++) {
            moveCursorColumnLeft();
        }
    }

    public void moveCursorColumnRight() {
        if (this._cursor.column < 79) {
            this._cursor.column++;
        }
    }

    public void moveCursorColumnRight(int n) {
        if (n < 1) {
            n = 1;
        }
        for (int i = 0; i < n; i++) {
            moveCursorColumnRight();
        }
    }

    public void moveCursorToNextLine() {
        moveCursorColumnToBegin();
        if (this._cursor.row == this._row - 1) {
            for (int i = 0; i < this._row - 1; i++) {
                this._frame.switchRow(i, i + 1);
            }
            this._frame.getLastestRow().clear();
        } else if (this._cursor.row < this._row - 1) {
            this._cursor.row++;
        }
    }

    public void moveCursorRowDown() {
        if (this._cursor.row < this._row - 1) {
            this._cursor.row++;
        }
    }

    public void moveCursorRowDown(int n) {
        if (n < 1) {
            n = 1;
        }
        for (int i = 0; i < n; i++) {
            moveCursorRowDown();
        }
    }

    public void moveCursorRowUp() {
        if (this._cursor.column > 0) {
            TelnetCursor telnetCursor = this._cursor;
            telnetCursor.column--;
        }
    }

    public void moveCursorRowUp(int n) {
        if (n < 1) {
            n = 1;
        }
        for (int i = 0; i < n; i++) {
            moveCursorRowUp();
        }
    }

    public void cleanPushedDataSize() {
        this._pushed_data_size = 0;
    }

    public int getPushedDataSize() {
        return this._pushed_data_size;
    }

    public void pushAnsiBuffer(byte data) {
        this._ansi_buffer.put(data);
    }

    public void cleanAnsiBuffer() {
        this._ansi_buffer.clear();
    }

    public void parseAnsiBuffer() {
        this._ansi_buffer.flip();
        switch (this._ansi_buffer.get(this._ansi_buffer.limit() - 1) & 255) {
            case 65:
                onReceivedAnsiControlCUU();
                return;
            case 66:
                onReceivedAnsiControlCUD();
                return;
            case 67:
                onReceivedAnsiControlCUF();
                return;
            case 68:
                onReceivedAnsiControlCUB();
                return;
            case 69:
                onReceivedAnsiControlCNL();
                return;
            case 70:
                onReceivedAnsiControlCPL();
                return;
            case 71:
                onReceivedAnsiControlCHA();
                return;
            case 72:
                onReceivedAnsiControlCUP();
                return;
            case 74:
                onReceivedAnsiControlED();
                return;
            case 75:
                onReceivedAnsiControlEL();
                return;
            case 83:
                onReceivedAnsiControlSU();
                return;
            case 84:
                onReceivedAnsiControlSD();
                return;
            case 102:
                onReceivedAnsiControlHVP();
                return;
            case 109:
                onReceivedAnsiControlSGR();
                return;
            case 110:
                onReceivedAnsiControlDSR();
                return;
            case 115:
                onReceivedAnsiControlSCP();
                return;
            case 117:
                onReceivedAnsiControlRCP();
                return;
            default:
                onReceivedUnknownAnsiControl();
        }
    }

    private int readIntegerFromAnsiBuffer() {
        int data;
        int state = 0;
        while (this._ansi_buffer.position() < this._ansi_buffer.limit() && (data = this._ansi_buffer.get() & 255) >= 48 && data <= 57) {
            state = (state * 10) + (data - 48);
        }
        return state;
    }

    public void onReceivedAnsiControlSGR() {
        if (this._ansi_buffer.limit() == 1) {
            parseSGRState(0);
        } else if (this._ansi_buffer.limit() > 1) {
            while (this._ansi_buffer.position() < this._ansi_buffer.limit()) {
                parseSGRState(readIntegerFromAnsiBuffer());
            }
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    private void parseSGRState(int state) {
        switch (state) {
            case 0:
                this._ansi.resetToDefaultState();
                return;
            case 1:
                this._ansi.textBright = true;
                return;
            case 2:
                this._ansi.textBright = false;
                return;
            case 3:
                this._ansi.textItalic = true;
                return;
            case 4:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 27:
            case 28:
            case 29:
            case 38:
            case 48:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
                return;
            case 5:
            case 6:
                this._ansi.textBlink = true;
                return;
            case 7:
                byte text_color = this._ansi.textColor;
                this._ansi.textColor = this._ansi.backgroundColor;
                this._ansi.backgroundColor = text_color;
                return;
            case 25:
                this._ansi.textBlink = false;
                return;
            case 30:
                this._ansi.textColor = 0;
                return;
            case 31:
                this._ansi.textColor = 1;
                return;
            case 32:
                this._ansi.textColor = 2;
                return;
            case 33:
                this._ansi.textColor = 3;
                return;
            case 34:
                this._ansi.textColor = 4;
                return;
            case 35:
                this._ansi.textColor = 5;
                return;
            case 36:
                this._ansi.textColor = 6;
                return;
            case 37:
                this._ansi.textColor = 7;
                return;
            case 39:
                this._ansi.textColor = TelnetAnsi.DEFAULT_TEXT_COLOR;
                return;
            case 40:
                this._ansi.backgroundColor = 0;
                return;
            case 41:
                this._ansi.backgroundColor = 1;
                return;
            case 42:
                this._ansi.backgroundColor = 2;
                return;
            case 43:
                this._ansi.backgroundColor = 3;
                return;
            case 44:
                this._ansi.backgroundColor = 4;
                return;
            case 45:
                this._ansi.backgroundColor = 5;
                return;
            case 46:
                this._ansi.backgroundColor = 6;
                return;
            case 47:
                this._ansi.backgroundColor = 7;
                return;
            case 49:
                this._ansi.backgroundColor = TelnetAnsi.DEFAULT_BACKGROUND_COLOR;
                return;
            default:
                this._ansi.resetToDefaultState();
                System.out.println("Unsupported SGR code : " + state);
        }
    }

    public void onReceivedAnsiControlSCP() {
        if (this._ansi_buffer.limit() == 1) {
            saveCursor();
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlRCP() {
        if (this._ansi_buffer.limit() == 1) {
            restoreCursor();
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlCUU() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowUp(1);
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowUp(readIntegerFromAnsiBuffer());
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlCUD() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowDown(1);
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowDown(readIntegerFromAnsiBuffer());
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlCUF() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorColumnRight(1);
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorColumnRight(readIntegerFromAnsiBuffer());
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlCUB() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorColumnLeft(1);
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorColumnLeft(readIntegerFromAnsiBuffer());
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlCNL() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowDown(1);
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowDown(readIntegerFromAnsiBuffer());
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlHVP() {
        onReceivedAnsiControlCUP();
    }

    public void onReceivedAnsiControlCPL() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowUp(1);
            moveCursorColumnToBegin();
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowUp(readIntegerFromAnsiBuffer());
            moveCursorColumnToBegin();
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlCHA() {
        if (this._ansi_buffer.limit() > 1) {
            setCursorColumn(readIntegerFromAnsiBuffer() - 1);
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlCUP() {
        if (this._ansi_buffer.limit() > 1) {
            setCursor(readIntegerFromAnsiBuffer() - 1, readIntegerFromAnsiBuffer() - 1);
        } else {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlED() {
        int state = 0;
        if (this._ansi_buffer.limit() == 1) {
            state = 0;
        } else if (this._ansi_buffer.limit() > 1) {
            state = readIntegerFromAnsiBuffer();
        } else {
            onReceivedUnknownAnsiControl();
        }
        switch (state) {
            case 1:
                cleanFrameToBeginning();
                return;
            case 2:
                cleanFrameAll();
                setCursor(0, 0);
                return;
            default:
                cleanFrameToEnd();
        }
    }

    public void onReceivedAnsiControlEL() {
        int state = 0;
        if (this._ansi_buffer.limit() == 1) {
            state = 0;
        } else if (this._ansi_buffer.limit() > 1) {
            state = readIntegerFromAnsiBuffer();
        } else {
            onReceivedUnknownAnsiControl();
        }
        switch (state) {
            case 1:
                cleanRowToBeginning();
                return;
            case 2:
                cleanRowAll();
                return;
            default:
                cleanRowToEnd();
        }
    }

    public void onReceivedAnsiControlSU() {
        if (this._ansi_buffer.limit() != 1) {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlSD() {
        if (this._ansi_buffer.limit() != 1) {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedAnsiControlDSR() {
        if (this._ansi_buffer.limit() != 1 || this._ansi_buffer.get(0) != 6) {
            onReceivedUnknownAnsiControl();
        }
    }

    public void onReceivedUnknownAnsiControl() {
        System.out.println("get unsupport ansi control : " + getAnsiBufferString());
    }

    public String getAnsiBufferString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < this._ansi_buffer.limit(); i++) {
            str.append((char) (this._ansi_buffer.get(i) & 255));
        }
        return str.toString();
    }
}
