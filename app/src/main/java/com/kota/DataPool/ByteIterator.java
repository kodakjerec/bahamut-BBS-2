package com.kota.DataPool;

public interface ByteIterator {
    boolean hasNext();

    int next();

    void reset();
}
