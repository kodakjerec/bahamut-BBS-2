package com.kota.Telnet

import java.io.IOException
import java.nio.ByteBuffer

interface TelnetSocketChannel {
    @Throws(IOException::class)
    fun finishConnect(): Boolean

    @Throws(IOException::class)
    fun read(byteBuffer: ByteBuffer?): Int

    @Throws(IOException::class)
    fun write(byteBuffer: ByteBuffer?): Int
}
