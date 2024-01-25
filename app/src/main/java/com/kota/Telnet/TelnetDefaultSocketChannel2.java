package com.kota.Telnet;

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

    public TelnetDefaultSocketChannel2(String address, int port) throws IOException {
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(address).getHostAddress(), port);
        this._socket = new Socket();
        this._socket.connect(addr);
        this._is = this._socket.getInputStream();
        this._os = this._socket.getOutputStream();
    }

    public int read(ByteBuffer buffer) throws IOException {
        this._input_length = 0;
        while (this._input_length == 0) {
            try {
                this._input_length = this._is.read(this._input_buffer);
                if (this._input_length == 0) {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("read failed.");
            }
        }
        buffer.put(this._input_buffer, 0, this._input_length);
        return this._input_length;
    }

    public int write(ByteBuffer buffer) throws IOException {
        this._output_length = 0;
        if (buffer.position() != 0) {
            buffer.flip();
        }
        byte[] output = buffer.array();
        this._os.write(output, 0, buffer.limit());
        this._os.flush();
        this._output_length = output.length;
        return this._output_length;
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
        if (this._socket == null) {
            return true;
        }
        this._socket.close();
        this._socket = null;
        return true;
    }
}
