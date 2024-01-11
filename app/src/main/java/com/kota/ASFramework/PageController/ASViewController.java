package com.kota.ASFramework.PageController;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.View;
import java.util.Iterator;
import java.util.Vector;

/* loaded from: classes.dex */
public abstract class ASViewController {
  public static final int ALERT_LEFT_BUTTON = -1;
  public static final int ALERT_MIDDLE_BUTTON = -3;
  public static final int ALERT_RIGHT_BUTTON = -2;
  private ASNavigationController _controller = null;
  protected boolean _is_loaded = false;
  private ASPageView _page_view = null;
  private boolean _contains_in_container = false;
  private boolean _marked_removed = false;
  private boolean _marked_added = false;
  private boolean _is_appeared = false;
  private boolean _is_disappeared = true;
  private boolean _is_request_refresh = false;
  private Vector<ASViewControllerOperationListener> _operation_listeners = null;
  private Vector<ASViewControllerAppearListener> _appear_listeners = null;
  private Vector<ASViewControllerDisappearListener> _disappear_listeners = null;

  public abstract int getPageLayout();

  public void setPageView(ASPageView aPageView) {
    if (this._page_view != null) {
      this._page_view.setOwnerController(null);
    }
    this._page_view = aPageView;
    if (this._page_view != null) {
      this._page_view.setOwnerController(this);
    }
  }

  public ASPageView getPageView() {
    return this._page_view;
  }

  public int getPageType() {
    return 0;
  }

  public void onPageDidLoad() {
  }

  public void onPageWillAppear() {
  }

  public void onPageDidAppear() {
  }

  public void onPageRefresh() {
  }

  public void onPageFreeMemory() {
  }

  public void onPageWillDisappear() {
  }

  public void onPageDidDisappear() {
  }

  public void onPageDidRemoveFromNavigationController() {
  }

  public void onPageDidAddToNavigationController() {
  }

  public void onPageDidUnload() {
  }

  public void notifyPageDidRemoveFromNavigationController() {
    if (this._operation_listeners != null) {
      Vector<ASViewControllerOperationListener> listeners = new Vector<>(this._operation_listeners);
      Iterator<ASViewControllerOperationListener> it = listeners.iterator();
      while (it.hasNext()) {
        ASViewControllerOperationListener listener = it.next();
        if (listener != null) {
          listener.onASViewControllerWillRemoveFromNavigationController(this);
        }
      }
    }
    onPageDidRemoveFromNavigationController();
  }

  public void notifyPageDidAddToNavigationController() {
    if (this._operation_listeners != null) {
      Vector<ASViewControllerOperationListener> listeners = new Vector<>(this._operation_listeners);
      Iterator<ASViewControllerOperationListener> it = listeners.iterator();
      while (it.hasNext()) {
        ASViewControllerOperationListener listener = it.next();
        if (listener != null) {
          listener.onASViewControllerWillAddToNavigationController(this);
        }
      }
    }
    onPageDidAddToNavigationController();
  }

  public void notifyPageWillAppear() {
    this._is_disappeared = false;
    if (this._appear_listeners != null) {
      Vector<ASViewControllerAppearListener> listeners = new Vector<>(this._appear_listeners);
      Iterator<ASViewControllerAppearListener> it = listeners.iterator();
      while (it.hasNext()) {
        ASViewControllerAppearListener listener = it.next();
        if (listener != null) {
          listener.onASViewControllerWillAppear(this);
        }
      }
    }
    onPageWillAppear();
  }

  public void notifyPageDidAppear() {
    this._is_appeared = true;
    if (this._appear_listeners != null) {
      Vector<ASViewControllerAppearListener> listeners = new Vector<>(this._appear_listeners);
      Iterator<ASViewControllerAppearListener> it = listeners.iterator();
      while (it.hasNext()) {
        ASViewControllerAppearListener listener = it.next();
        if (listener != null) {
          listener.onASViewControllerDidAppear(this);
        }
      }
    }
    onPageDidAppear();
    if (this._is_request_refresh) {
      onPageRefresh();
      this._is_request_refresh = false;
    }
  }

  public void notifyPageWillDisappear() {
    this._is_appeared = false;
    if (this._disappear_listeners != null) {
      Vector<ASViewControllerDisappearListener> listeners = new Vector<>(this._disappear_listeners);
      Iterator<ASViewControllerDisappearListener> it = listeners.iterator();
      while (it.hasNext()) {
        ASViewControllerDisappearListener listener = it.next();
        if (listener != null) {
          listener.onASViewControllerWillDisappear(this);
        }
      }
    }
    onPageWillDisappear();
  }

  public void notifyPageDidDisappear() {
    this._is_disappeared = true;
    if (this._disappear_listeners != null) {
      Vector<ASViewControllerDisappearListener> listeners = new Vector<>(this._disappear_listeners);
      Iterator<ASViewControllerDisappearListener> it = listeners.iterator();
      while (it.hasNext()) {
        ASViewControllerDisappearListener listener = it.next();
        if (listener != null) {
          listener.onASViewControllerDidDisappear(this);
        }
      }
    }
    onPageDidDisappear();
  }

  public void requestPageRefresh() {
    if (this._is_appeared) {
      onPageRefresh();
    } else {
      this._is_request_refresh = true;
    }
  }

  public boolean isPageAppeared() {
    return this._is_appeared;
  }

  public boolean isPageDisappeared() {
    return this._is_disappeared;
  }

  public boolean backToMainToolbarAfterExtendToolbarClicked() {
    return true;
  }

  protected void finalize() throws Throwable {
    onPageDidUnload();
    super.finalize();
  }

  public void clear() {
  }

  public Context getContext() {
    return this._controller;
  }

  public Resources getResource() {
    return this._controller.getResources();
  }

  public ASNavigationController getNavigationController() {
    return this._controller;
  }

  public View findViewById(int viewID) {
    if (this._page_view != null) {
      return this._page_view.findViewById(viewID);
    }
    return null;
  }

  public Object getSystemService(String name) {
    if (this._controller != null) {
      return this._controller.getSystemService(name);
    }
    return null;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public boolean onBackPressed() {
    getNavigationController().popViewController();
    return true;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public boolean onSearchButtonClicked() {
    return false;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public boolean onMenuButtonClicked() {
    return false;
  }

  public void startActivity(Intent intent) {
    if (this._controller != null) {
      this._controller.startActivity(intent);
    }
  }

  public void setNavigationController(ASNavigationController controller) {
    this._controller = controller;
  }

  public void reloadLayout() {
    if (this._controller != null) {
      this._controller.reloadLayout();
    }
  }

  public boolean onReceivedGestureUp() {
    return false;
  }

  public boolean onReceivedGestureDown() {
    return false;
  }

  public boolean onReceivedGestureLeft() {
    return false;
  }

  public boolean onReceivedGestureRight() {
    return false;
  }

  public boolean isTopPage() {
    return getNavigationController() != null && getNavigationController().getTopController() == this;
  }

  public AssetManager getAssets() {
    return this._controller.getAssets();
  }

  public boolean containsInContainer() {
    return this._contains_in_container;
  }

  public void setContainsInContainer(boolean contains) {
    this._contains_in_container = contains;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public boolean isMarkedRemoved() {
    return this._marked_removed;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public boolean isMarkedAdded() {
    return this._marked_added;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public void prepareForRemove() {
    this._marked_removed = true;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public void prepareForAdd() {
    this._marked_removed = false;
    this._marked_added = true;
  }

  /* JADX INFO: Access modifiers changed from: protected */
  public void cleanMark() {
    this._marked_removed = false;
    this._marked_added = false;
  }

  public String toString() {
    return "ASViewController[Type:" + getPageType() + "]";
  }

  public void registerPageOperationListener(ASViewControllerOperationListener aListener) {
    if (this._operation_listeners == null) {
      this._operation_listeners = new Vector<>();
    }
    this._operation_listeners.add(aListener);
  }

  public void unregisterPageOperationListener(ASViewControllerOperationListener aListener) {
    if (this._operation_listeners != null) {
      this._operation_listeners.remove(aListener);
    }
  }

  public void registerAppearListener(ASViewControllerAppearListener aListener) {
    if (this._appear_listeners == null) {
      this._appear_listeners = new Vector<>();
    }
    this._appear_listeners.add(aListener);
  }

  public void unregisterAppearListener(ASViewControllerAppearListener aListener) {
    if (this._appear_listeners != null) {
      this._appear_listeners.remove(aListener);
    }
  }

  public void registerDisappearListener(ASViewControllerDisappearListener aListener) {
    if (this._disappear_listeners == null) {
      this._disappear_listeners = new Vector<>();
    }
    this._disappear_listeners.add(aListener);
  }

  public void unregisterDisappearListener(ASViewControllerDisappearListener aListener) {
    if (this._disappear_listeners != null) {
      this._disappear_listeners.remove(aListener);
    }
  }
}