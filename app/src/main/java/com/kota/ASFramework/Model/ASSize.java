// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Model;


public class ASSize
{

    public int height;
    public int width;

    public ASSize()
    {
        width = 0;
        height = 0;
    }

    public ASSize(int i, int j)
    {
        width = 0;
        height = 0;
        width = i;
        height = j;
    }

    public boolean isZero()
    {
        return width == 0 && height == 0;
    }

    public void set(int i, int j)
    {
        width = i;
        height = j;
    }

    public void set(ASSize assize)
    {
        set(assize.width, assize.height);
    }

    public String toString()
    {
        return (new StringBuilder()).append("(").append(width).append(",").append(height).append(")").toString();
    }
}
