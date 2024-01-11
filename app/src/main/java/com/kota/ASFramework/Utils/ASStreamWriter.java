// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class ASStreamWriter
{

    public ASStreamWriter()
    {
    }

    public static void writeData(OutputStream outputstream, byte byte0)
        throws IOException
    {
        outputstream.write(byte0);
    }

    public static void writeData(OutputStream outputstream, char c)
        throws IOException
    {
        outputstream.write(c >> 8 & 0xff);
        outputstream.write(c & 0xff);
    }

    public static void writeData(OutputStream outputstream, double d)
        throws IOException
    {
        writeData(outputstream, Double.doubleToLongBits(d));
    }

    public static void writeData(OutputStream outputstream, float f)
        throws IOException
    {
        writeData(outputstream, Float.floatToIntBits(f));
    }

    public static void writeData(OutputStream outputstream, int i)
        throws IOException
    {
        outputstream.write(i >> 24 & 0xff);
        outputstream.write(i >> 16 & 0xff);
        outputstream.write(i >> 8 & 0xff);
        outputstream.write(i & 0xff);
    }

    public static void writeData(OutputStream outputstream, long l)
        throws IOException
    {
        outputstream.write((int)(l >> 56) & 0xff);
        outputstream.write((int)(l >> 48) & 0xff);
        outputstream.write((int)(l >> 40) & 0xff);
        outputstream.write((int)(l >> 32) & 0xff);
        outputstream.write((int)(l >> 24) & 0xff);
        outputstream.write((int)(l >> 16) & 0xff);
        outputstream.write((int)(l >> 8) & 0xff);
        outputstream.write((int)l & 0xff);
    }

    public static void writeData(OutputStream outputstream, String s)
        throws IOException
    {
        writeData(outputstream, s.getBytes("unicode"));
    }

    public static void writeData(OutputStream outputstream, short word0)
        throws IOException
    {
        outputstream.write(word0 >> 8 & 0xff);
        outputstream.write(word0 & 0xff);
    }

    public static void writeData(OutputStream outputstream, boolean flag)
        throws IOException
    {
        if (flag)
        {
            outputstream.write(1);
            return;
        } else
        {
            outputstream.write(0);
            return;
        }
    }

    public static void writeData(OutputStream outputstream, byte abyte0[])
        throws IOException
    {
        writeData(outputstream, abyte0.length);
        outputstream.write(abyte0);
    }
}
