package com.kumi.Telnet;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface TelnetSocketChannel {
  boolean finishConnect() throws IOException;
  
  int read(ByteBuffer paramByteBuffer) throws IOException;
  
  int write(ByteBuffer paramByteBuffer) throws IOException;
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetSocketChannel.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */