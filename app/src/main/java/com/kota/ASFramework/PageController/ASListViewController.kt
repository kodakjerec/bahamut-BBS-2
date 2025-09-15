package com.kota.ASFramework.PageController

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.widget.ListAdapter

/* loaded from: classes.dex */
abstract class ASListViewController : ListAdapter {
    private val mDataSetObservable = DataSetObservable()

    override fun registerDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.unregisterObserver(observer)
    }

    fun notifyDataSetChanged() {
        mDataSetObservable.notifyChanged()
    }
}
