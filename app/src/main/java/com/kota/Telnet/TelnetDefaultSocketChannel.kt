package com.kota.Telnet

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TelnetDefaultSocketChannel @Throws(IOException::class) constructor(
    address: String,
    port: Int
) : TelnetSocketChannel {
    
    private val _socket_channel: SocketChannel = SocketChannel.open(
        InetSocketAddress(InetAddress.getByName(address).hostAddress, port)
    )

    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int = _socket_channel.read(buffer)

    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int = _socket_channel.write(buffer)

    @Throws(IOException::class)
    override fun finishConnect(): Boolean {
        _socket_channel.close()
        return _socket_channel.finishConnect()
    }
}
