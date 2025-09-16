package com.kota.Telnet

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TelnetDefaultSocketChannel(address: String?, port: Int) : TelnetSocketChannel {
    var _socket_channel: SocketChannel

    init {
        this._socket_channel = SocketChannel.open(
            InetSocketAddress(
                InetAddress.getByName(address).getHostAddress(),
                port
            )
        )
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer?): Int {
        return this._socket_channel.read(buffer)
    }

    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer?): Int {
        return this._socket_channel.write(buffer)
    }

    @Throws(IOException::class)
    override fun finishConnect(): Boolean {
        this._socket_channel.close()
        return this._socket_channel.finishConnect()
    }
}
