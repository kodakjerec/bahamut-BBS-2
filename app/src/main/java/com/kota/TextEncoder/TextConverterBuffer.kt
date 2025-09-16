package com.kota.TextEncoder

import java.nio.ByteBuffer

interface TextConverterBuffer {
    fun createByteBuffer(): ByteBuffer?

    fun recycleByteBuffer(byteBuffer: ByteBuffer?)
}
