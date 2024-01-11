// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Utils;

import java.io.UnsupportedEncodingException;

public class ASTypeConvertor
{

    public ASTypeConvertor()
    {
    }

    public static byte[] getData(char c)
    {
        return (new byte[] {
            (byte)(c >> 8), (byte)c
        });
    }

    public static byte[] getData(double d)
    {
        return getData(Double.doubleToLongBits(d));
    }

    public static byte[] getData(float f)
    {
        return getData(Float.floatToIntBits(f));
    }

    public static byte[] getData(int i)
    {
        return (new byte[] {
            (byte)(i >> 24), (byte)(i >> 16), (byte)(i >> 8), (byte)i
        });
    }

    public static byte[] getData(long l)
    {
        return (new byte[] {
            (byte)(int)(l >> 56), (byte)(int)(l >> 48), (byte)(int)(l >> 40), (byte)(int)(l >> 32), (byte)(int)(l >> 24), (byte)(int)(l >> 16), (byte)(int)(l >> 8), (byte)(int)l
        });
    }

    public static byte[] getData(String s)
    {
        byte abyte0[] = null;
        byte abyte1[];
        try
        {
            s = s.getBytes("unicode");
        }
        // Misplaced declaration of an exception variable
        catch (String s)
        {
            s.printStackTrace();
            s = abyte0;
        }
        abyte0 = getData(s.length);
        abyte1 = new byte[s.length + 4];
        for (int i = 0; i < 4; i++)
        {
            abyte1[i] = abyte0[i];
        }

        for (int j = 0; j < s.length; j++)
        {
            abyte1[j + 4] = s[j];
        }

        return abyte1;
    }

    public static byte[] getData(short word0)
    {
        return (new byte[] {
            (byte)(word0 >> 8), (byte)word0
        });
    }

    public static byte[] getData(boolean flag)
    {
        if (flag)
        {
            return (new byte[] {
                1
            });
        } else
        {
            return (new byte[] {
                0
            });
        }
    }
}
