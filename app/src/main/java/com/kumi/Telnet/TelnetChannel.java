package com.kumi.Telnet;

import com.kumi.DataPool.MutableByteBuffer;
import com.kumi.TextEncoder.B2UEncoder;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

public class TelnetChannel {
  public static int BUFFER_SIZE = 2048;
  
  private ByteBuffer _input_buffer = ByteBuffer.allocate(BUFFER_SIZE);
  
  private TelnetChannelListener _listener = null;
  
  private boolean _lock = false;
  
  private MutableByteBuffer _output_buffer = MutableByteBuffer.createMutableByteBuffer();
  
  private int _read_data_size = 0;
  
  private TelnetSocketChannel _socket_channel = null;
  
  public TelnetChannel(TelnetSocketChannel paramTelnetSocketChannel) {
    this._socket_channel = paramTelnetSocketChannel;
  }
  
  private void onReceiveDataFinished() {
    if (this._listener != null)
      this._listener.onTelnetChannelReceiveDataFinished(this); 
  }
  
  private void onReceiveDataStart() {
    if (this._listener != null)
      this._listener.onTelnetChannelReceiveDataStart(this); 
  }
  
  private void printInputBuffer() {
    byte[] arrayOfByte = new byte[this._input_buffer.limit()];
    for (byte b = 0; b < this._input_buffer.limit(); b++)
      arrayOfByte[b] = this._input_buffer.array()[b]; 
    try {
      String str = B2UEncoder.getInstance().encodeToString(arrayOfByte);
      PrintStream printStream = System.out;
      StringBuilder stringBuilder = new StringBuilder();
      this();
      printStream.println(stringBuilder.append("receive data:").append(str).toString());
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
  }
  
  private void printOutputBuffer() {
    byte[] arrayOfByte = new byte[this._output_buffer.size()];
    byte b = 0;
    label20: for (ByteBuffer byteBuffer : this._output_buffer) {
      byte b1 = 0;
      byte b2 = b;
      while (true) {
        b = b2;
        if (b1 < byteBuffer.limit()) {
          arrayOfByte[b2] = byteBuffer.array()[b1];
          b2++;
          b1++;
          continue;
        } 
        continue label20;
      } 
    } 
    try {
      String str1 = B2UEncoder.getInstance().encodeToString(arrayOfByte);
      PrintStream printStream = System.out;
      StringBuilder stringBuilder2 = new StringBuilder();
      this();
      printStream.println(stringBuilder2.append("send data:\n").append(str1).toString());
      str1 = "";
      for (byte b1 = 0; b1 < arrayOfByte.length; b1++) {
        StringBuilder stringBuilder = new StringBuilder();
        this();
        str1 = stringBuilder.append(str1).append(String.format(" %1$02x", new Object[] { Byte.valueOf(arrayOfByte[b1]) })).toString();
      } 
      String str2 = str1.substring(1);
      printStream = System.out;
      StringBuilder stringBuilder1 = new StringBuilder();
      this();
      printStream.println(stringBuilder1.append("send hex data:\n").append(str2).toString());
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
  }
  
  private void receiveData() throws TelnetConnectionClosedException, IOException {
    this._input_buffer.clear();
    this._socket_channel.read(this._input_buffer);
    if (this._input_buffer.position() != 0) {
      this._input_buffer.flip();
      return;
    } 
    throw new TelnetConnectionClosedException();
  }
  
  public void cleanReadDataSize() {
    this._read_data_size = 0;
  }
  
  public void clear() {
    this._input_buffer.clear();
    this._output_buffer.clear();
  }
  
  public int getReadDataSize() {
    return this._read_data_size;
  }
  
  public void lock() {
    this._lock = true;
  }
  
  public byte readData() throws TelnetConnectionClosedException, IOException {
    if (!this._input_buffer.hasRemaining()) {
      onReceiveDataStart();
      receiveData();
      onReceiveDataFinished();
    } 
    this._input_buffer.mark();
    byte b = this._input_buffer.get();
    this._read_data_size++;
    return b;
  }
  
  public boolean sendData() {
    boolean bool = false;
    null = bool;
    if (!this._lock) {
      null = bool;
      if (this._output_buffer.size() > 0) {
        try {
          this._output_buffer.close();
          for (ByteBuffer byteBuffer : this._output_buffer)
            this._socket_channel.write(byteBuffer); 
        } catch (Exception exception) {
          exception.printStackTrace();
          return bool;
        } 
      } else {
        return null;
      } 
    } else {
      return null;
    } 
    this._output_buffer.clear();
    return true;
  }
  
  public void setListener(TelnetChannelListener paramTelnetChannelListener) {
    this._listener = paramTelnetChannelListener;
  }
  
  public void undoReadData() {
    this._input_buffer.reset();
    this._read_data_size--;
  }
  
  public void unlock() {
    this._lock = false;
  }
  
  public void writeData(byte paramByte) {
    this._output_buffer.put(paramByte);
  }
  
  public void writeData(byte[] paramArrayOfbyte) {
    int i = paramArrayOfbyte.length;
    for (byte b = 0; b < i; b++)
      writeData(paramArrayOfbyte[b]); 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetChannel.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */