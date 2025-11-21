package com.kota.textEncoder

import java.nio.ByteBuffer

interface TextConverterBuffer {
    fun createByteBuffer(): ByteBuffer?

    fun recycleByteBuffer(byteBuffer: ByteBuffer?)
}
