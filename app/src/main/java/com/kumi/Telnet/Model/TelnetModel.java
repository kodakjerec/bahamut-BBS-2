package com.kumi.Telnet.Model;

import com.kumi.Telnet.Reference.TelnetAnsiCode;
import com.kumi.Telnet.TelnetAnsi;
import com.kumi.Telnet.TelnetCursor;
import java.nio.ByteBuffer;

public class TelnetModel {
  private static int _count = 0;
  
  protected TelnetAnsi _ansi = new TelnetAnsi();
  
  private ByteBuffer _ansi_buffer = ByteBuffer.allocate(1024);
  
  private TelnetCursor _cursor = new TelnetCursor();
  
  private TelnetFrame _frame = null;
  
  private int _pushed_data_size = 0;
  
  private int _row = 0;
  
  private TelnetCursor _saved_cursor = new TelnetCursor();
  
  public TelnetModel() {
    this._row = 24;
    initialDataModel();
  }
  
  public TelnetModel(int paramInt) {
    this._row = paramInt;
    initialDataModel();
  }
  
  private void cleanCursor(int paramInt1, int paramInt2) {
    if (paramInt1 >= 0 && paramInt1 < this._row && paramInt2 >= 0 && paramInt2 < 80)
      this._frame.cleanPositionData(paramInt1, paramInt2); 
  }
  
  private void initialDataModel() {
    this._frame = new TelnetFrame(this._row);
  }
  
  private void parseSGRState(int paramInt) {
    byte b;
    switch (paramInt) {
      default:
        this._ansi.resetToDefaultState();
        System.out.println("Unsupported SGR code : " + paramInt);
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
      case 0:
        this._ansi.resetToDefaultState();
      case 1:
        this._ansi.textBright = true;
      case 2:
        this._ansi.textBright = false;
      case 3:
        this._ansi.textItalic = true;
      case 5:
        this._ansi.textBlink = true;
      case 6:
        this._ansi.textBlink = true;
      case 7:
        b = this._ansi.textColor;
        this._ansi.textColor = this._ansi.backgroundColor;
        this._ansi.backgroundColor = b;
      case 25:
        this._ansi.textBlink = false;
      case 30:
        this._ansi.textColor = 0;
      case 31:
        this._ansi.textColor = 1;
      case 32:
        this._ansi.textColor = 2;
      case 33:
        this._ansi.textColor = 3;
      case 34:
        this._ansi.textColor = 4;
      case 35:
        this._ansi.textColor = 5;
      case 36:
        this._ansi.textColor = 6;
      case 37:
        this._ansi.textColor = 7;
      case 39:
        this._ansi.textColor = TelnetAnsi.DEFAULT_TEXT_COLOR;
      case 40:
        this._ansi.backgroundColor = 0;
      case 41:
        this._ansi.backgroundColor = 1;
      case 42:
        this._ansi.backgroundColor = 2;
      case 43:
        this._ansi.backgroundColor = 3;
      case 44:
        this._ansi.backgroundColor = 4;
      case 45:
        this._ansi.backgroundColor = 5;
      case 46:
        this._ansi.backgroundColor = 6;
      case 47:
        this._ansi.backgroundColor = 7;
      case 49:
        break;
    } 
    this._ansi.backgroundColor = TelnetAnsi.DEFAULT_BACKGROUND_COLOR;
  }
  
  private int readIntegerFromAnsiBuffer() {
    int i = 0;
    while (this._ansi_buffer.position() < this._ansi_buffer.limit()) {
      int j = this._ansi_buffer.get() & 0xFF;
      if (j >= 48 && j <= 57)
        i = i * 10 + j - 48; 
    } 
    return i;
  }
  
  private void setCursorData(byte paramByte, TelnetAnsi paramTelnetAnsi) {
    if (this._cursor.row >= 0 && this._cursor.row < this._row && this._cursor.column >= 0 && this._cursor.column < 80) {
      TelnetRow telnetRow = this._frame.getRow(this._cursor.row);
      telnetRow.cleanColumn(this._cursor.column);
      telnetRow.data[this._cursor.column] = paramByte;
      if (paramTelnetAnsi != null) {
        byte b = paramTelnetAnsi.textColor;
        paramByte = b;
        if (paramTelnetAnsi.textBright)
          paramByte = (byte)(b + 8); 
        telnetRow.textColor[this._cursor.column] = paramByte;
        telnetRow.backgroundColor[this._cursor.column] = paramTelnetAnsi.backgroundColor;
        telnetRow.blink[this._cursor.column] = paramTelnetAnsi.textBlink;
        telnetRow.italic[this._cursor.column] = paramTelnetAnsi.textItalic;
      } 
    } 
  }
  
  public void cleanAnsiBuffer() {
    this._ansi_buffer.clear();
  }
  
  public void cleanCahcedData() {
    this._frame.cleanCachedData();
  }
  
  public void cleanFrame() {
    for (byte b = 0; b < this._row; b++) {
      for (byte b1 = 0; b1 < 80; b1++)
        cleanCursor(b, b1); 
    } 
  }
  
  public void cleanFrameAll() {
    this._frame.clear();
  }
  
  public void cleanFrameToBeginning() {
    byte b;
    for (b = 0; b < this._cursor.row; b++) {
      for (byte b1 = 0; b1 < 80; b1++)
        cleanCursor(b, b1); 
    } 
    for (b = 0; b < this._cursor.column; b++)
      cleanCursor(this._cursor.row, b); 
  }
  
  public void cleanFrameToEnd() {
    int i;
    for (i = this._cursor.row + 1; i < this._row; i++) {
      for (byte b = 0; b < 80; b++)
        cleanCursor(i, b); 
    } 
    for (i = this._cursor.column; i < 80; i++)
      cleanCursor(this._cursor.row, i); 
  }
  
  public void cleanPushedDataSize() {
    this._pushed_data_size = 0;
  }
  
  public void cleanRow(int paramInt) {
    for (byte b = 0; b < 80; b++)
      cleanCursor(paramInt, b); 
  }
  
  public void cleanRowAll() {
    for (byte b = 0; b < 80; b++)
      cleanCursor(this._cursor.row, b); 
  }
  
  public void cleanRowToBeginning() {
    for (byte b = 0; b < this._cursor.column; b++)
      cleanCursor(this._cursor.row, b); 
  }
  
  public void cleanRowToEnd() {
    for (int i = this._cursor.column; i < 80; i++)
      cleanCursor(this._cursor.row, i); 
  }
  
  public void clear() {
    this._ansi = new TelnetAnsi();
    this._frame.clear();
    this._cursor = new TelnetCursor();
    this._saved_cursor = new TelnetCursor();
    this._pushed_data_size = 0;
    this._ansi_buffer.clear();
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public String getAnsiBufferString() {
    String str = "";
    for (byte b = 0; b < this._ansi_buffer.limit(); b++) {
      byte b1 = this._ansi_buffer.get(b);
      str = str + String.valueOf((char)(b1 & 0xFF));
    } 
    return str;
  }
  
  public int getBackgroundColor(int paramInt1, int paramInt2) {
    return TelnetAnsiCode.getBackgroundColor((this._frame.getRow(paramInt1)).backgroundColor[paramInt2]);
  }
  
  public boolean getBlink(int paramInt1, int paramInt2) {
    return (this._frame.getRow(paramInt1)).blink[paramInt2];
  }
  
  public TelnetCursor getCursor() {
    return this._cursor;
  }
  
  public int getData(int paramInt1, int paramInt2) {
    return (this._frame.getRow(paramInt1)).data[paramInt2] & 0xFF;
  }
  
  public TelnetRow getFirstRow() {
    return this._frame.getFirstRow();
  }
  
  public TelnetFrame getFrame() {
    return this._frame;
  }
  
  public TelnetRow getLastRow() {
    return this._frame.getLastestRow();
  }
  
  public int getPushedDataSize() {
    return this._pushed_data_size;
  }
  
  public TelnetRow getRow(int paramInt) {
    return (paramInt >= 0 && paramInt < this._row) ? this._frame.getRow(paramInt) : null;
  }
  
  public int getRowSize() {
    return this._row;
  }
  
  public String getRowString(int paramInt) {
    return this._frame.getRow(paramInt).toString();
  }
  
  public int getTextColor(int paramInt1, int paramInt2) {
    return TelnetAnsiCode.getTextColor((this._frame.getRow(paramInt1)).textColor[paramInt2]);
  }
  
  public void moveCursorColumnLeft() {
    if (this._cursor.column > 0) {
      TelnetCursor telnetCursor = this._cursor;
      telnetCursor.column--;
    } 
  }
  
  public void moveCursorColumnLeft(int paramInt) {
    int i = paramInt;
    if (paramInt < 1)
      i = 1; 
    for (paramInt = 0; paramInt < i; paramInt++)
      moveCursorColumnLeft(); 
  }
  
  public void moveCursorColumnRight() {
    if (this._cursor.column < 79) {
      TelnetCursor telnetCursor = this._cursor;
      telnetCursor.column++;
    } 
  }
  
  public void moveCursorColumnRight(int paramInt) {
    int i = paramInt;
    if (paramInt < 1)
      i = 1; 
    for (paramInt = 0; paramInt < i; paramInt++)
      moveCursorColumnRight(); 
  }
  
  public void moveCursorColumnToBegin() {
    this._cursor.column = 0;
  }
  
  public void moveCursorColumnToEnd() {
    this._cursor.column = 79;
  }
  
  public void moveCursorRowDown() {
    if (this._cursor.row < this._row - 1) {
      TelnetCursor telnetCursor = this._cursor;
      telnetCursor.row++;
    } 
  }
  
  public void moveCursorRowDown(int paramInt) {
    int i = paramInt;
    if (paramInt < 1)
      i = 1; 
    for (paramInt = 0; paramInt < i; paramInt++)
      moveCursorRowDown(); 
  }
  
  public void moveCursorRowToBegin() {
    this._cursor.row = 0;
  }
  
  public void moveCursorRowToEnd() {
    this._cursor.row = 79;
  }
  
  public void moveCursorRowUp() {
    if (this._cursor.column > 0) {
      TelnetCursor telnetCursor = this._cursor;
      telnetCursor.column--;
    } 
  }
  
  public void moveCursorRowUp(int paramInt) {
    int i = paramInt;
    if (paramInt < 1)
      i = 1; 
    for (paramInt = 0; paramInt < i; paramInt++)
      moveCursorRowUp(); 
  }
  
  public void moveCursorToNextLine() {
    moveCursorColumnToBegin();
    if (this._cursor.row == this._row - 1) {
      for (byte b = 0; b < this._row - 1; b++)
        this._frame.switchRow(b, b + 1); 
      this._frame.getLastestRow().clear();
      return;
    } 
    if (this._cursor.row < this._row - 1) {
      TelnetCursor telnetCursor = this._cursor;
      telnetCursor.row++;
    } 
  }
  
  public void onReceivedAnsiControlCHA() {
    if (this._ansi_buffer.limit() > 1) {
      setCursorColumn(readIntegerFromAnsiBuffer() - 1);
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlCNL() {
    if (this._ansi_buffer.limit() == 1) {
      moveCursorRowDown(1);
      return;
    } 
    if (this._ansi_buffer.limit() > 1) {
      moveCursorRowDown(readIntegerFromAnsiBuffer());
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlCPL() {
    if (this._ansi_buffer.limit() == 1) {
      moveCursorRowUp(1);
      moveCursorColumnToBegin();
      return;
    } 
    if (this._ansi_buffer.limit() > 1) {
      moveCursorRowUp(readIntegerFromAnsiBuffer());
      moveCursorColumnToBegin();
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlCUB() {
    if (this._ansi_buffer.limit() == 1) {
      moveCursorColumnLeft(1);
      return;
    } 
    if (this._ansi_buffer.limit() > 1) {
      moveCursorColumnLeft(readIntegerFromAnsiBuffer());
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlCUD() {
    if (this._ansi_buffer.limit() == 1) {
      moveCursorRowDown(1);
      return;
    } 
    if (this._ansi_buffer.limit() > 1) {
      moveCursorRowDown(readIntegerFromAnsiBuffer());
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlCUF() {
    if (this._ansi_buffer.limit() == 1) {
      moveCursorColumnRight(1);
      return;
    } 
    if (this._ansi_buffer.limit() > 1) {
      moveCursorColumnRight(readIntegerFromAnsiBuffer());
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlCUP() {
    if (this._ansi_buffer.limit() > 1) {
      setCursor(readIntegerFromAnsiBuffer() - 1, readIntegerFromAnsiBuffer() - 1);
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlCUU() {
    if (this._ansi_buffer.limit() == 1) {
      moveCursorRowUp(1);
      return;
    } 
    if (this._ansi_buffer.limit() > 1) {
      moveCursorRowUp(readIntegerFromAnsiBuffer());
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlDSR() {
    if (this._ansi_buffer.limit() != 1 || this._ansi_buffer.get(0) != 6)
      onReceivedUnknownAnsiControl(); 
  }
  
  public void onReceivedAnsiControlED() {
    int i = 0;
    if (this._ansi_buffer.limit() == 1) {
      i = 0;
    } else if (this._ansi_buffer.limit() > 1) {
      i = readIntegerFromAnsiBuffer();
    } else {
      onReceivedUnknownAnsiControl();
    } 
    switch (i) {
      default:
        cleanFrameToEnd();
        return;
      case 1:
        cleanFrameToBeginning();
        return;
      case 2:
        break;
    } 
    cleanFrameAll();
    setCursor(0, 0);
  }
  
  public void onReceivedAnsiControlEL() {
    int i = 0;
    if (this._ansi_buffer.limit() == 1) {
      i = 0;
    } else if (this._ansi_buffer.limit() > 1) {
      i = readIntegerFromAnsiBuffer();
    } else {
      onReceivedUnknownAnsiControl();
    } 
    switch (i) {
      default:
        cleanRowToEnd();
        return;
      case 1:
        cleanRowToBeginning();
        return;
      case 2:
        break;
    } 
    cleanRowAll();
  }
  
  public void onReceivedAnsiControlHVP() {
    onReceivedAnsiControlCUP();
  }
  
  public void onReceivedAnsiControlRCP() {
    if (this._ansi_buffer.limit() == 1) {
      restoreCursor();
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlSCP() {
    if (this._ansi_buffer.limit() == 1) {
      saveCursor();
      return;
    } 
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlSD() {
    if (this._ansi_buffer.limit() != 1)
      onReceivedUnknownAnsiControl(); 
  }
  
  public void onReceivedAnsiControlSGR() {
    if (this._ansi_buffer.limit() == 1) {
      parseSGRState(0);
      return;
    } 
    if (this._ansi_buffer.limit() > 1)
      while (true) {
        if (this._ansi_buffer.position() < this._ansi_buffer.limit()) {
          parseSGRState(readIntegerFromAnsiBuffer());
          continue;
        } 
        return;
      }  
    onReceivedUnknownAnsiControl();
  }
  
  public void onReceivedAnsiControlSU() {
    if (this._ansi_buffer.limit() != 1)
      onReceivedUnknownAnsiControl(); 
  }
  
  public void onReceivedUnknownAnsiControl() {
    System.out.println("get unsupport ansi control : " + getAnsiBufferString());
  }
  
  public void parseAnsiBuffer() {
    this._ansi_buffer.flip();
    switch (this._ansi_buffer.get(this._ansi_buffer.limit() - 1) & 0xFF) {
      default:
        onReceivedUnknownAnsiControl();
        return;
      case 109:
        onReceivedAnsiControlSGR();
        return;
      case 115:
        onReceivedAnsiControlSCP();
        return;
      case 117:
        onReceivedAnsiControlRCP();
        return;
      case 110:
        onReceivedAnsiControlDSR();
        return;
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
      case 102:
        onReceivedAnsiControlHVP();
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
        break;
    } 
    onReceivedAnsiControlSD();
  }
  
  public void pushAnsiBuffer(byte paramByte) {
    this._ansi_buffer.put(paramByte);
  }
  
  public void pushData(byte paramByte) {
    if (this._cursor.column < 80) {
      this._pushed_data_size++;
      setCursorData(paramByte, this._ansi);
      TelnetCursor telnetCursor = this._cursor;
      telnetCursor.column++;
    } 
  }
  
  public void restoreCursor() {
    this._cursor.set(this._saved_cursor);
  }
  
  public void saveCursor() {
    this._saved_cursor.set(this._cursor);
  }
  
  public void setCursor(int paramInt1, int paramInt2) {
    setCursorRow(paramInt1);
    setCursorColumn(paramInt2);
  }
  
  public void setCursorColumn(int paramInt) {
    if (paramInt < 0) {
      this._cursor.column = 0;
      return;
    } 
    if (paramInt > 79) {
      this._cursor.column = 79;
      return;
    } 
    this._cursor.column = paramInt;
  }
  
  public void setCursorRow(int paramInt) {
    if (paramInt < 0) {
      this._cursor.row = 0;
      return;
    } 
    if (paramInt > this._row - 1) {
      this._cursor.row = this._row - 1;
      return;
    } 
    this._cursor.row = paramInt;
  }
  
  public void setFrame(TelnetFrame paramTelnetFrame) {
    this._frame.set(paramTelnetFrame);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Model\TelnetModel.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */