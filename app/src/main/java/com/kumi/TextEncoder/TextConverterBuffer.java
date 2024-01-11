package com.kumi.TextEncoder;

import java.nio.ByteBuffer;

public interface TextConverterBuffer {
  ByteBuffer createByteBuffer();
  
  void recycleByteBuffer(ByteBuffer paramByteBuffer);
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TextEncoder\TextConverterBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */