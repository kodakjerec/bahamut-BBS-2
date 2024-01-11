// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.widget.ListAdapter;

public abstract class ASListViewController
    implements ListAdapter
{

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    public ASListViewController()
    {
    }

    public void notifyDataSetChanged()
    {
        mDataSetObservable.notifyChanged();
    }

    public void registerDataSetObserver(DataSetObserver datasetobserver)
    {
        mDataSetObservable.registerObserver(datasetobserver);
    }

    public void unregisterDataSetObserver(DataSetObserver datasetobserver)
    {
        mDataSetObservable.unregisterObserver(datasetobserver);
    }
}
