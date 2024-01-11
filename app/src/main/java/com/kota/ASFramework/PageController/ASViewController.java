// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.View;
import java.util.Iterator;
import java.util.Vector;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASPageView, ASNavigationController, ASViewControllerOperationListener, ASViewControllerAppearListener, 
//            ASViewControllerDisappearListener

public abstract class ASViewController
{

    public static final int ALERT_LEFT_BUTTON = -1;
    public static final int ALERT_MIDDLE_BUTTON = -3;
    public static final int ALERT_RIGHT_BUTTON = -2;
    private Vector _appear_listeners;
    private boolean _contains_in_container;
    private ASNavigationController _controller;
    private Vector _disappear_listeners;
    private boolean _is_appeared;
    private boolean _is_disappeared;
    protected boolean _is_loaded;
    private boolean _is_request_refresh;
    private boolean _marked_added;
    private boolean _marked_removed;
    private Vector _operation_listeners;
    private ASPageView _page_view;

    public ASViewController()
    {
        _controller = null;
        _is_loaded = false;
        _page_view = null;
        _contains_in_container = false;
        _marked_removed = false;
        _marked_added = false;
        _is_appeared = false;
        _is_disappeared = true;
        _is_request_refresh = false;
        _operation_listeners = null;
        _appear_listeners = null;
        _disappear_listeners = null;
    }

    public boolean backToMainToolbarAfterExtendToolbarClicked()
    {
        return true;
    }

    protected void cleanMark()
    {
        _marked_removed = false;
        _marked_added = false;
    }

    public void clear()
    {
    }

    public boolean containsInContainer()
    {
        return _contains_in_container;
    }

    protected void finalize()
        throws Throwable
    {
        onPageDidUnload();
        super.finalize();
    }

    public View findViewById(int i)
    {
        if (_page_view != null)
        {
            return _page_view.findViewById(i);
        } else
        {
            return null;
        }
    }

    public AssetManager getAssets()
    {
        return _controller.getAssets();
    }

    public Context getContext()
    {
        return _controller;
    }

    public ASNavigationController getNavigationController()
    {
        return _controller;
    }

    public abstract int getPageLayout();

    public int getPageType()
    {
        return 0;
    }

    public ASPageView getPageView()
    {
        return _page_view;
    }

    public Resources getResource()
    {
        return _controller.getResources();
    }

    public Object getSystemService(String s)
    {
        if (_controller != null)
        {
            return _controller.getSystemService(s);
        } else
        {
            return null;
        }
    }

    protected boolean isMarkedAdded()
    {
        return _marked_added;
    }

    protected boolean isMarkedRemoved()
    {
        return _marked_removed;
    }

    public boolean isPageAppeared()
    {
        return _is_appeared;
    }

    public boolean isPageDisappeared()
    {
        return _is_disappeared;
    }

    public boolean isTopPage()
    {
        while (getNavigationController() == null || getNavigationController().getTopController() != this) 
        {
            return false;
        }
        return true;
    }

    public void notifyPageDidAddToNavigationController()
    {
        if (_operation_listeners != null)
        {
            Iterator iterator = (new Vector(_operation_listeners)).iterator();
            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }
                ASViewControllerOperationListener asviewcontrolleroperationlistener = (ASViewControllerOperationListener)iterator.next();
                if (asviewcontrolleroperationlistener != null)
                {
                    asviewcontrolleroperationlistener.onASViewControllerWillAddToNavigationController(this);
                }
            } while (true);
        }
        onPageDidAddToNavigationController();
    }

    public void notifyPageDidAppear()
    {
        _is_appeared = true;
        if (_appear_listeners != null)
        {
            Iterator iterator = (new Vector(_appear_listeners)).iterator();
            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }
                ASViewControllerAppearListener asviewcontrollerappearlistener = (ASViewControllerAppearListener)iterator.next();
                if (asviewcontrollerappearlistener != null)
                {
                    asviewcontrollerappearlistener.onASViewControllerDidAppear(this);
                }
            } while (true);
        }
        onPageDidAppear();
        if (_is_request_refresh)
        {
            onPageRefresh();
            _is_request_refresh = false;
        }
    }

    public void notifyPageDidDisappear()
    {
        _is_disappeared = true;
        if (_disappear_listeners != null)
        {
            Iterator iterator = (new Vector(_disappear_listeners)).iterator();
            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }
                ASViewControllerDisappearListener asviewcontrollerdisappearlistener = (ASViewControllerDisappearListener)iterator.next();
                if (asviewcontrollerdisappearlistener != null)
                {
                    asviewcontrollerdisappearlistener.onASViewControllerDidDisappear(this);
                }
            } while (true);
        }
        onPageDidDisappear();
    }

    public void notifyPageDidRemoveFromNavigationController()
    {
        if (_operation_listeners != null)
        {
            Iterator iterator = (new Vector(_operation_listeners)).iterator();
            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }
                ASViewControllerOperationListener asviewcontrolleroperationlistener = (ASViewControllerOperationListener)iterator.next();
                if (asviewcontrolleroperationlistener != null)
                {
                    asviewcontrolleroperationlistener.onASViewControllerWillRemoveFromNavigationController(this);
                }
            } while (true);
        }
        onPageDidRemoveFromNavigationController();
    }

    public void notifyPageWillAppear()
    {
        _is_disappeared = false;
        if (_appear_listeners != null)
        {
            Iterator iterator = (new Vector(_appear_listeners)).iterator();
            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }
                ASViewControllerAppearListener asviewcontrollerappearlistener = (ASViewControllerAppearListener)iterator.next();
                if (asviewcontrollerappearlistener != null)
                {
                    asviewcontrollerappearlistener.onASViewControllerWillAppear(this);
                }
            } while (true);
        }
        onPageWillAppear();
    }

    public void notifyPageWillDisappear()
    {
        _is_appeared = false;
        if (_disappear_listeners != null)
        {
            Iterator iterator = (new Vector(_disappear_listeners)).iterator();
            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }
                ASViewControllerDisappearListener asviewcontrollerdisappearlistener = (ASViewControllerDisappearListener)iterator.next();
                if (asviewcontrollerdisappearlistener != null)
                {
                    asviewcontrollerdisappearlistener.onASViewControllerWillDisappear(this);
                }
            } while (true);
        }
        onPageWillDisappear();
    }

    protected boolean onBackPressed()
    {
        getNavigationController().popViewController();
        return true;
    }

    protected boolean onMenuButtonClicked()
    {
        return false;
    }

    public void onPageDidAddToNavigationController()
    {
    }

    public void onPageDidAppear()
    {
    }

    public void onPageDidDisappear()
    {
    }

    public void onPageDidLoad()
    {
    }

    public void onPageDidRemoveFromNavigationController()
    {
    }

    public void onPageDidUnload()
    {
    }

    public void onPageFreeMemory()
    {
    }

    public void onPageRefresh()
    {
    }

    public void onPageWillAppear()
    {
    }

    public void onPageWillDisappear()
    {
    }

    public boolean onReceivedGestureDown()
    {
        return false;
    }

    public boolean onReceivedGestureLeft()
    {
        return false;
    }

    public boolean onReceivedGestureRight()
    {
        return false;
    }

    public boolean onReceivedGestureUp()
    {
        return false;
    }

    protected boolean onSearchButtonClicked()
    {
        return false;
    }

    protected void onSizeChanged(int i, int j, int k, int l)
    {
    }

    protected void prepareForAdd()
    {
        _marked_removed = false;
        _marked_added = true;
    }

    protected void prepareForRemove()
    {
        _marked_removed = true;
    }

    public void registerAppearListener(ASViewControllerAppearListener asviewcontrollerappearlistener)
    {
        if (_appear_listeners == null)
        {
            _appear_listeners = new Vector();
        }
        _appear_listeners.add(asviewcontrollerappearlistener);
    }

    public void registerDisappearListener(ASViewControllerDisappearListener asviewcontrollerdisappearlistener)
    {
        if (_disappear_listeners == null)
        {
            _disappear_listeners = new Vector();
        }
        _disappear_listeners.add(asviewcontrollerdisappearlistener);
    }

    public void registerPageOperationListener(ASViewControllerOperationListener asviewcontrolleroperationlistener)
    {
        if (_operation_listeners == null)
        {
            _operation_listeners = new Vector();
        }
        _operation_listeners.add(asviewcontrolleroperationlistener);
    }

    public void reloadLayout()
    {
        if (_controller != null)
        {
            _controller.reloadLayout();
        }
    }

    public void requestPageRefresh()
    {
        if (_is_appeared)
        {
            onPageRefresh();
            return;
        } else
        {
            _is_request_refresh = true;
            return;
        }
    }

    public void setContainsInContainer(boolean flag)
    {
        _contains_in_container = flag;
    }

    public void setNavigationController(ASNavigationController asnavigationcontroller)
    {
        _controller = asnavigationcontroller;
    }

    public void setPageView(ASPageView aspageview)
    {
        if (_page_view != null)
        {
            _page_view.setOwnerController(null);
        }
        _page_view = aspageview;
        if (_page_view != null)
        {
            _page_view.setOwnerController(this);
        }
    }

    public void startActivity(Intent intent)
    {
        if (_controller != null)
        {
            _controller.startActivity(intent);
        }
    }

    public String toString()
    {
        return (new StringBuilder()).append("ASViewController[Type:").append(getPageType()).append("]").toString();
    }

    public void unregisterAppearListener(ASViewControllerAppearListener asviewcontrollerappearlistener)
    {
        if (_appear_listeners != null)
        {
            _appear_listeners.remove(asviewcontrollerappearlistener);
        }
    }

    public void unregisterDisappearListener(ASViewControllerDisappearListener asviewcontrollerdisappearlistener)
    {
        if (_disappear_listeners != null)
        {
            _disappear_listeners.remove(asviewcontrollerdisappearlistener);
        }
    }

    public void unregisterPageOperationListener(ASViewControllerOperationListener asviewcontrolleroperationlistener)
    {
        if (_operation_listeners != null)
        {
            _operation_listeners.remove(asviewcontrolleroperationlistener);
        }
    }
}
