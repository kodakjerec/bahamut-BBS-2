package com.kota.ASFramework.PageController;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.widget.ListAdapter;

/* loaded from: classes.dex */
public abstract class ASListViewController implements ListAdapter {
  private final DataSetObservable mDataSetObservable = new DataSetObservable();

  @Override // android.widget.Adapter
  public void registerDataSetObserver(DataSetObserver observer) {
    this.mDataSetObservable.registerObserver(observer);
  }

  @Override // android.widget.Adapter
  public void unregisterDataSetObserver(DataSetObserver observer) {
    this.mDataSetObservable.unregisterObserver(observer);
  }

  public void notifyDataSetChanged() {
    this.mDataSetObservable.notifyChanged();
  }
}
