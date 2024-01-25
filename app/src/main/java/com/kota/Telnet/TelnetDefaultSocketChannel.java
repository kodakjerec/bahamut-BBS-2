package com.kota.Telnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TelnetDefaultSocketChannel implements TelnetSocketChannel {
    SocketChannel _socket_channel;

    public TelnetDefaultSocketChannel(String address, int port) throws IOException {
        this._socket_channel = SocketChannel.open(new InetSocketAddress(InetAddress.getByName(address).getHostAddress(), port));
    }

    public int read(ByteBuffer buffer) throws IOException {
        return this._socket_channel.read(buffer);
    }

    public int write(ByteBuffer buffer) throws IOException {
        return this._socket_channel.write(buffer);
    }

    public boolean finishConnect() throws IOException {
        this._socket_channel.close();
        return this._socket_channel.finishConnect();
    }
}
