package com.kota.TextEncoder;

import java.nio.ByteBuffer;

public interface TextConverterBuffer {
    ByteBuffer createByteBuffer();

    void recycleByteBuffer(ByteBuffer byteBuffer);
}
