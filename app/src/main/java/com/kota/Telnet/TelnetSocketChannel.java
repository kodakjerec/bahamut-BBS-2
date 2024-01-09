package com.kota.Telnet;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface TelnetSocketChannel {
    boolean finishConnect() throws IOException;

    int read(ByteBuffer byteBuffer) throws IOException;

    int write(ByteBuffer byteBuffer) throws IOException;
}
