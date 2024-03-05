package com.kota.TextEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class U2BEncoder {
    public static final int CHAR_MAXIMUM = 65535;
    public static final int NULL_CHAR = 65533;
    private static U2BEncoder _instance = null;
    private int _offset = 0;
    private char[] _table = null;
    private int _table_size = 0;

    private U2BEncoder(InputStream inputStream) {
        readTableFromInputStream(inputStream);
    }

    public static U2BEncoder getInstance() {
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
            System.out.println("read U2B encode data success");
        }
    }

    public static void constructInstance(InputStream tableInputStream) {
        _instance = new U2BEncoder(tableInputStream);
    }

    public static void releaseInstance() {
        _instance = null;
    }

    public char encodeChar(char data) {
        int index = data - this._offset;
        return (index < 0 || index >= this._table_size) ? data : this._table[index];
    }

    public byte[] encodeToBytes(byte[] data, int start) {
        ByteBuffer string_buffer = ByteBuffer.allocate(data.length);
        string_buffer.clear();
        for (int i = start; i < data.length; i += 2) {
            int c = encodeChar((char) (((data[i + 1] & 255) << 8) + (data[i] & 255)));
            int upper = (c >> 8) & 255;
            int lower = c & 255;
            if (upper > 0) {
                string_buffer.put((byte) upper);
            }
            string_buffer.put((byte) lower);
        }
        string_buffer.flip();
        byte[] result_data = new byte[string_buffer.limit()];
        for (int i2 = 0; i2 < result_data.length; i2++) {
            result_data[i2] = string_buffer.get();
        }
        return result_data;
    }
}
