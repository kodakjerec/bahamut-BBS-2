package com.kumi.ASFramework.PageController;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.widget.ListAdapter;

public abstract class ASListViewController implements ListAdapter {
  private final DataSetObservable mDataSetObservable = new DataSetObservable();
  
  public void notifyDataSetChanged() {
    this.mDataSetObservable.notifyChanged();
  }
  
  public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.registerObserver(paramDataSetObserver);
  }
  
  public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
    this.mDataSetObservable.unregisterObserver(paramDataSetObserver);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASListViewController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */