package com.kota.Telnet;

import com.kota.DataPool.MutableByteBuffer;
import com.kota.TextEncoder.B2UEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TelnetChannel {
    public static int BUFFER_SIZE = 2048;
    private final ByteBuffer _input_buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private TelnetChannelListener _listener = null;
    private boolean _lock = false;
    private final MutableByteBuffer _output_buffer = MutableByteBuffer.createMutableByteBuffer();
    private int _read_data_size = 0;
    private TelnetSocketChannel _socket_channel = null;

    public TelnetChannel(TelnetSocketChannel aSocketChannel) {
        this._socket_channel = aSocketChannel;
    }

    public void setListener(TelnetChannelListener aListener) {
        this._listener = aListener;
    }

    public void clear() {
        this._input_buffer.clear();
        this._output_buffer.clear();
    }

    public void lock() {
        this._lock = true;
    }

    public void unlock() {
        this._lock = false;
    }

    public void writeData(byte[] data) {
        for (byte b : data) {
            writeData(b);
        }
    }

    public void writeData(byte data) {
        this._output_buffer.put(data);
    }

    public boolean sendData() {
        if (this._lock || this._output_buffer.size() <= 0) {
            return false;
        }
        try {
            this._output_buffer.close();
            for (ByteBuffer byteBuffer : this._output_buffer) {
                this._socket_channel.write(byteBuffer);
            }
//            printOutputBuffer();
            this._output_buffer.clear();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte readData() throws TelnetConnectionClosedException, IOException {
        if (!this._input_buffer.hasRemaining()) {
            onReceiveDataStart();
            receiveData();
            onReceiveDataFinished();
        }
        this._input_buffer.mark();
        byte data = this._input_buffer.get();
        this._read_data_size++;
        return data;
    }

    public void undoReadData() {
        this._input_buffer.reset();
        this._read_data_size--;
    }

    private void receiveData() throws TelnetConnectionClosedException, IOException {
        this._input_buffer.clear();
        int read = this._socket_channel.read(this._input_buffer);
        if (this._input_buffer.position() != 0) {
            this._input_buffer.flip();
            return;
        }
        throw new TelnetConnectionClosedException();
    }

    private void onReceiveDataStart() {
        if (this._listener != null) {
            this._listener.onTelnetChannelReceiveDataStart(this);
        }
    }

    private void onReceiveDataFinished() {
        if (this._listener != null) {
            this._listener.onTelnetChannelReceiveDataFinished(this);
//            printInputBuffer();
        }
    }

    /** 列印收到的資料 */
    private void printInputBuffer() {
        byte[] data = new byte[this._input_buffer.limit()];
        for (int i = 0; i < this._input_buffer.limit(); i++) {
            data[i] = this._input_buffer.array()[i];
        }
        try {
            System.out.println("receive data:" + B2UEncoder.getInstance().encodeToString(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 列印送出的資料 */
    private void printOutputBuffer() {
        byte[] data = new byte[this._output_buffer.size()];
        int position = 0;
        for (ByteBuffer output_buffer : this._output_buffer) {
            for (int i = 0; i < output_buffer.limit(); i++) {
                data[position] = output_buffer.array()[i];
                position++;
            }
        }
        try {
            System.out.println("send data:\n" + B2UEncoder.getInstance().encodeToString(data));
            String hex_data = "";
            for (byte datum : data) {
                hex_data = hex_data + String.format(" %1$02x", datum);
            }
            System.out.println("send hex data:\n" + hex_data.substring(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanReadDataSize() {
        this._read_data_size = 0;
    }

    public int getReadDataSize() {
        return this._read_data_size;
    }
}
