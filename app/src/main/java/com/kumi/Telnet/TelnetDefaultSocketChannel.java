package com.kumi.Telnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TelnetDefaultSocketChannel implements TelnetSocketChannel {
  SocketChannel _socket_channel;
  
  public TelnetDefaultSocketChannel(String paramString, int paramInt) throws IOException {
    this._socket_channel = SocketChannel.open(new InetSocketAddress(InetAddress.getByName(paramString).getHostAddress(), paramInt));
  }
  
  public boolean finishConnect() throws IOException {
    this._socket_channel.close();
    return this._socket_channel.finishConnect();
  }
  
  public int read(ByteBuffer paramByteBuffer) throws IOException {
    return this._socket_channel.read(paramByteBuffer);
  }
  
  public int write(ByteBuffer paramByteBuffer) throws IOException {
    return this._socket_channel.write(paramByteBuffer);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetDefaultSocketChannel.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */