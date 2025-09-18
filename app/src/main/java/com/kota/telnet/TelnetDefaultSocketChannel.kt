package com.kota.telnet

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TelnetDefaultSocketChannel(address: String, port: Int) : TelnetSocketChannel {
    var socketChannel: SocketChannel = SocketChannel.open(
        InetSocketAddress(
            InetAddress.getByName(address).hostAddress,
            port
        )
    )

    @Throws(IOException::class)
    override fun read(byteBuffer: ByteBuffer?): Int {
        return this.socketChannel.read(byteBuffer)
    }

    @Throws(IOException::class)
    override fun write(byteBuffer: ByteBuffer?): Int {
        return this.socketChannel.write(byteBuffer)
    }

    @Throws(IOException::class)
    override fun finishConnect(): Boolean {
        this.socketChannel.close()
        return this.socketChannel.finishConnect()
    }
}
