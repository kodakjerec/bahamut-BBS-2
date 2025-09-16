package com.kota.asFramework.pageController

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.widget.ListAdapter

/* loaded from: classes.dex */
abstract class ASListViewController : ListAdapter {
    private val mDataSetObservable = DataSetObservable()

    // android.widget.Adapter
    override fun registerDataSetObserver(observer: DataSetObserver?) {
        this.mDataSetObservable.registerObserver(observer)
    }

    // android.widget.Adapter
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        this.mDataSetObservable.unregisterObserver(observer)
    }

    fun notifyDataSetChanged() {
        this.mDataSetObservable.notifyChanged()
    }
}
