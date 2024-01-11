package com.kumi.Telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TelnetDefaultSocketChannel2 implements TelnetSocketChannel {
  byte[] _input_buffer = new byte[1024];
  
  int _input_length;
  
  InputStream _is;
  
  OutputStream _os;
  
  int _output_length;
  
  Socket _socket;
  
  public TelnetDefaultSocketChannel2(String paramString, int paramInt) throws IOException {
    InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(paramString).getHostAddress(), paramInt);
    this._socket = new Socket();
    this._socket.connect(inetSocketAddress);
    this._is = this._socket.getInputStream();
    this._os = this._socket.getOutputStream();
  }
  
  public boolean finishConnect() throws IOException {
    if (this._is != null) {
      this._is.close();
      this._is = null;
    } 
    if (this._os != null) {
      this._os.close();
      this._os = null;
    } 
    if (this._socket != null) {
      this._socket.close();
      this._socket = null;
    } 
    return true;
  }
  
  public int read(ByteBuffer paramByteBuffer) throws IOException {
    this._input_length = 0;
    try {
      while (this._input_length == 0) {
        this._input_length = this._is.read(this._input_buffer);
        if (this._input_length == 0)
          Thread.sleep(100L); 
      } 
    } catch (Exception exception) {
      exception.printStackTrace();
      throw new IOException("read failed.");
    } 
    exception.put(this._input_buffer, 0, this._input_length);
    return this._input_length;
  }
  
  public int write(ByteBuffer paramByteBuffer) throws IOException {
    this._output_length = 0;
    if (paramByteBuffer.position() != 0)
      paramByteBuffer.flip(); 
    byte[] arrayOfByte = paramByteBuffer.array();
    int i = paramByteBuffer.limit();
    this._os.write(arrayOfByte, 0, i);
    this._os.flush();
    this._output_length = arrayOfByte.length;
    return this._output_length;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetDefaultSocketChannel2.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */