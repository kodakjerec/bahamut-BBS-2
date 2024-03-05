package com.kota.TextEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Vector;

public class B2UEncoder {
    private static final int BUFFER_SIZE = 1024;
    public static final int CHAR_MAXIMUM = 65535;
    public static final int NULL_CHAR = 65533;
    private static B2UEncoder _instance = null;
    private TextConverterBuffer _buffer = null;
    private int _offset = 0;
    private char[] _table = null;
    private int _table_size = 0;

    private B2UEncoder(InputStream inputStream) {
        readTableFromInputStream(inputStream);
    }

    public static B2UEncoder getInstance() {
        return _instance;
    }

    private char readCharFromStream(InputStream fis) throws IOException {
        return (char) ((fis.read() << 8) + fis.read());
    }

    private void readTableFromInputStream(InputStream inputStream) {
        boolean result = false;
        try {
            char total = readCharFromStream(inputStream);
            this._offset = readCharFromStream(inputStream);
            this._table_size = readCharFromStream(inputStream);
            this._table = new char[this._table_size];
            for (int i = 0; i < this._table.length; i++) {
                this._table[i] = NULL_CHAR;
            }
            for (int i2 = 0; i2 < total; i2++) {
                char index = readCharFromStream(inputStream);
                this._table[index - this._offset] = readCharFromStream(inputStream);
            }
            inputStream.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result) {
            System.out.println("read B2U encode data success");
        }
    }

    public static void constructInstance(InputStream inputStream) {
        _instance = new B2UEncoder(inputStream);
    }

    public static void releaseInstance() {
        _instance = null;
    }

    public char encodeChar(char data) {
        int index = data - this._offset;
        return (index < 0 || index >= this._table_size) ? data : this._table[index];
    }

    public String encodeToString(byte[] stringData) {
        try {
            return new String(encodeToBytes(stringData), "unicode");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private byte[] encodeToBytes(byte[] stringData) {
        int upper;
        int upper2;
        Vector<ByteBuffer> buffers = new Vector<>();
        buffers.add(getBuffer().createByteBuffer());
        buffers.firstElement().put((byte) -2);
        buffers.firstElement().put((byte) -1);
        int i = 0;
        while (i < stringData.length && (upper = stringData[i] & 255) != 0) {
            int lower = upper;
            if (upper <= 127 || i >= stringData.length - 1) {
                upper2 = 0;
            } else {
                i++;
                int c = encodeChar((char) ((upper << 8) + (stringData[i] & 255)));
                upper2 = (c >> 8) & 255;
                lower = c & 255;
            }
            if (!buffers.lastElement().hasRemaining()) {
                buffers.lastElement().flip();
                buffers.add(getBuffer().createByteBuffer());
            }
            buffers.lastElement().put((byte) upper2);
            if (!buffers.lastElement().hasRemaining()) {
                buffers.lastElement().flip();
                buffers.add(getBuffer().createByteBuffer());
            }
            buffers.lastElement().put((byte) lower);
            i++;
        }
        buffers.lastElement().flip();
        int buffer_size = 0;
        for (int i2 = 0; i2 < buffers.size() - 1; i2++) {
            buffer_size += BUFFER_SIZE;
        }
        byte[] result_data = new byte[(buffer_size + buffers.lastElement().limit())];
        int count = 0;
        for (ByteBuffer buffer : buffers) {
            for (int i3 = 0; i3 < buffer.limit(); i3++) {
                result_data[count] = buffer.get(i3);
                count++;
            }
        }
        return result_data;
    }

    private TextConverterBuffer getBuffer() {
        if (this._buffer == null) {
            this._buffer = new TextConverterBuffer() {
                public void recycleByteBuffer(ByteBuffer aBuffer) {
                }

                public ByteBuffer createByteBuffer() {
                    return ByteBuffer.allocate(BUFFER_SIZE);
                }
            };
        }
        return this._buffer;
    }
}
