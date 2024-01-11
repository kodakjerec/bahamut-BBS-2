// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Utils;

import java.io.IOException;
import java.io.InputStream;

public class ASStreamReader
{

    public ASStreamReader()
    {
    }

    public static boolean readBoolean(InputStream inputstream)
        throws IOException
    {
        return inputstream.read() != 0;
    }

    public static byte readByte(InputStream inputstream)
        throws IOException
    {
        return (byte)inputstream.read();
    }

    public static char readChar(InputStream inputstream)
        throws IOException
    {
        return (char)(inputstream.read() << inputstream.read() + 8);
    }

    public static double readDouble(InputStream inputstream)
        throws IOException
    {
        return 0.0D;
    }

    public static int readInt(InputStream inputstream)
        throws IOException
    {
        return inputstream.read() << inputstream.read() + 24 << inputstream.read() + 16 << inputstream.read() + 8;
    }

    public static short readShort(InputStream inputstream)
        throws IOException
    {
        return (short)(inputstream.read() << inputstream.read() + 8);
    }
}
