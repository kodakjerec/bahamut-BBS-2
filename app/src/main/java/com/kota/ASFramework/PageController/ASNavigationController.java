// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.kumi.ASFramework.Thread.ASRunner;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASViewController, ASAnimation, ASPageView, ASNavigationControllerView, 
//            ASDeviceController, ASNavigationControllerPopAnimation, ASNavigationControllerPushAnimation

public class ASNavigationController extends Activity
{
    private abstract class PageCommand
    {

        public boolean animated;
        final ASNavigationController this$0;

        public abstract void run();

        private PageCommand()
        {
            this$0 = ASNavigationController.this;
            super();
            animated = true;
        }

    }


    private static ASNavigationController _current_controller = null;
    private Vector _add_list;
    private boolean _animation_enable;
    private Vector _controllers;
    private ASDeviceController _device_controller;
    private DisplayMetrics _display_metrics;
    private boolean _in_background;
    private boolean _is_animating;
    private Vector _page_commands;
    private Vector _remove_list;
    private ASNavigationControllerView _root_view;
    private Vector _temp_controllers;

    public ASNavigationController()
    {
        _device_controller = null;
        _display_metrics = new DisplayMetrics();
        _root_view = null;
        _controllers = new Vector();
        _temp_controllers = new Vector();
        _remove_list = new Vector();
        _add_list = new Vector();
        _animation_enable = true;
        _in_background = false;
        _page_commands = new Vector();
        _is_animating = false;
    }

    private void animatePopViewController(final ASViewController final_asviewcontroller, final ASViewController final_asviewcontroller1, boolean flag)
    {
        if (final_asviewcontroller != null)
        {
            final_asviewcontroller.notifyPageWillDisappear();
            if (flag)
            {
                removePageView(final_asviewcontroller, ASAnimation.getFadeOutToRightAnimation());
            } else
            {
                removePageView(final_asviewcontroller);
            }
        }
        if (final_asviewcontroller1 != null)
        {
            buildPageView(final_asviewcontroller1);
            final_asviewcontroller1.onPageDidLoad();
            final_asviewcontroller1.onPageRefresh();
            final_asviewcontroller1.notifyPageWillAppear();
        }
        (new ASNavigationControllerPopAnimation(final_asviewcontroller, final_asviewcontroller1) {

            final ASNavigationController this$0;
            final ASViewController val$aAddPage;
            final ASViewController val$aRemovePage;

            public void onAnimationFinished()
            {
                if (aRemovePage != null)
                {
                    aRemovePage.onPageDidDisappear();
                }
                Object obj = new Vector();
                for (int i = 0; i < _root_view.getContentView().getChildCount(); i++)
                {
                    View view = _root_view.getContentView().getChildAt(i);
                    if (view != aAddPage.getPageView())
                    {
                        ((Vector) (obj)).add(view);
                    }
                }

                View view1;
                for (obj = ((Vector) (obj)).iterator(); ((Iterator) (obj)).hasNext(); _root_view.getContentView().removeView(view1))
                {
                    view1 = (View)((Iterator) (obj)).next();
                }

                cleanPageView(aRemovePage);
                obj = _controllers.iterator();
                do
                {
                    if (!((Iterator) (obj)).hasNext())
                    {
                        break;
                    }
                    ASViewController asviewcontroller = (ASViewController)((Iterator) (obj)).next();
                    if (asviewcontroller != aAddPage && asviewcontroller.getPageView() != null)
                    {
                        cleanPageView(asviewcontroller);
                    }
                } while (true);
                if (aAddPage != null)
                {
                    aAddPage.notifyPageDidAppear();
                }
                onPageCommandExecuteFinished();
            }

            
            {
                this$0 = ASNavigationController.this;
                aRemovePage = asviewcontroller2;
                aAddPage = asviewcontroller3;
                super(final_asviewcontroller, final_asviewcontroller1);
            }
        }).start(flag);
    }

    private void animatedPushViewController(final ASViewController final_asviewcontroller, final ASViewController final_asviewcontroller1, boolean flag)
    {
        if (final_asviewcontroller1 != null)
        {
            buildPageView(final_asviewcontroller1);
            final_asviewcontroller1.onPageDidLoad();
            final_asviewcontroller1.onPageRefresh();
        }
        if (final_asviewcontroller != null)
        {
            final_asviewcontroller.notifyPageWillDisappear();
        }
        if (final_asviewcontroller1 != null)
        {
            final_asviewcontroller1.notifyPageWillAppear();
        }
        (new ASNavigationControllerPushAnimation(final_asviewcontroller, final_asviewcontroller1) {

            final ASNavigationController this$0;
            final ASViewController val$sourceController;
            final ASViewController val$targetController;

            public void onAnimationFinished()
            {
                if (sourceController != null)
                {
                    sourceController.onPageDidDisappear();
                }
                if (targetController != null)
                {
                    Object obj = new Vector();
                    for (int i = 0; i < _root_view.getContentView().getChildCount(); i++)
                    {
                        View view = _root_view.getContentView().getChildAt(i);
                        if (view != targetController.getPageView())
                        {
                            ((Vector) (obj)).add(view);
                        }
                    }

                    View view1;
                    for (obj = ((Vector) (obj)).iterator(); ((Iterator) (obj)).hasNext(); _root_view.getContentView().removeView(view1))
                    {
                        view1 = (View)((Iterator) (obj)).next();
                    }

                }
                Iterator iterator = _controllers.iterator();
                do
                {
                    if (!iterator.hasNext())
                    {
                        break;
                    }
                    ASViewController asviewcontroller = (ASViewController)iterator.next();
                    if (asviewcontroller != targetController && asviewcontroller.getPageView() != null)
                    {
                        cleanPageView(asviewcontroller);
                    }
                } while (true);
                if (targetController != null)
                {
                    targetController.notifyPageDidAppear();
                }
                onPageCommandExecuteFinished();
            }

            
            {
                this$0 = ASNavigationController.this;
                sourceController = asviewcontroller2;
                targetController = asviewcontroller3;
                super(final_asviewcontroller, final_asviewcontroller1);
            }
        }).start(flag);
    }

    private void buildPageView(ASViewController asviewcontroller)
    {
        ASPageView aspageview = new ASPageView(this);
        aspageview.setLayoutParams(new android.widget.FrameLayout.LayoutParams(-1, -1));
        aspageview.setBackgroundColor(0xff000000);
        aspageview.setAnimationCacheEnabled(false);
        getLayoutInflater().inflate(asviewcontroller.getPageLayout(), aspageview);
        asviewcontroller.setPageView(aspageview);
        _root_view.getContentView().addView(aspageview);
    }

    private void cleanPageView(ASViewController asviewcontroller)
    {
        asviewcontroller.setPageView(null);
    }

    public static ASNavigationController getCurrentController()
    {
        return _current_controller;
    }

    private boolean onMenuPressed()
    {
        boolean flag1 = false;
        ASViewController asviewcontroller = getTopController();
        boolean flag = flag1;
        if (asviewcontroller != null)
        {
            flag = flag1;
            if (asviewcontroller.onMenuButtonClicked())
            {
                flag = true;
            }
        }
        return flag;
    }

    private void onPageCommandExecuteFinished()
    {
        this;
        JVM INSTR monitorenter ;
        _is_animating = false;
        this;
        JVM INSTR monitorexit ;
        _temp_controllers.clear();
        executePageCommand();
        return;
        Exception exception;
        exception;
        this;
        JVM INSTR monitorexit ;
        throw exception;
    }

    private boolean onSearchPressed()
    {
        boolean flag1 = false;
        ASViewController asviewcontroller = getTopController();
        boolean flag = flag1;
        if (asviewcontroller != null)
        {
            flag = flag1;
            if (asviewcontroller.onSearchButtonClicked())
            {
                flag = true;
            }
        }
        return flag;
    }

    private static void setNavigationController(ASNavigationController asnavigationcontroller)
    {
        _current_controller = asnavigationcontroller;
    }

    public void addPageView(final ASViewController aPage)
    {
        final ASPageView page_view = aPage.getPageView();
        _root_view.post(new Runnable() {

            final ASNavigationController this$0;
            final ASViewController val$aPage;
            final View val$page_view;

            public void run()
            {
                aPage.onPageDidDisappear();
                _root_view.removeView(page_view);
            }

            
            {
                this$0 = ASNavigationController.this;
                aPage = asviewcontroller;
                page_view = view;
                super();
            }
        });
    }

    public boolean containsViewController(ASViewController asviewcontroller)
    {
        return _controllers.contains(asviewcontroller);
    }

    public void exchangeViewControllers(final boolean animated)
    {
        (new ASRunner() {

            final ASNavigationController this$0;
            final boolean val$animated;

            public void run()
            {
                ASViewController asviewcontroller;
                ASViewController asviewcontroller1;
                boolean flag;
                if (_controllers.size() > 0)
                {
                    asviewcontroller = (ASViewController)_controllers.lastElement();
                } else
                {
                    asviewcontroller = null;
                }
                if (_temp_controllers.size() > 0)
                {
                    asviewcontroller1 = (ASViewController)_temp_controllers.lastElement();
                } else
                {
                    asviewcontroller1 = null;
                }
                flag = _controllers.contains(asviewcontroller1);
                for (Iterator iterator = _controllers.iterator(); iterator.hasNext(); ((ASViewController)iterator.next()).prepareForRemove()) { }
                for (Iterator iterator1 = _temp_controllers.iterator(); iterator1.hasNext(); ((ASViewController)iterator1.next()).prepareForAdd()) { }
                ASViewController asviewcontroller2;
                for (Iterator iterator2 = _controllers.iterator(); iterator2.hasNext(); asviewcontroller2.cleanMark())
                {
                    asviewcontroller2 = (ASViewController)iterator2.next();
                    if (asviewcontroller2.isMarkedRemoved())
                    {
                        _remove_list.add(asviewcontroller2);
                    }
                }

                ASViewController asviewcontroller3;
                for (Iterator iterator3 = _temp_controllers.iterator(); iterator3.hasNext(); asviewcontroller3.cleanMark())
                {
                    asviewcontroller3 = (ASViewController)iterator3.next();
                    if (asviewcontroller3.isMarkedAdded())
                    {
                        asviewcontroller3.setNavigationController(ASNavigationController.this);
                        _add_list.add(asviewcontroller3);
                    }
                }

                _controllers.removeAllElements();
                _controllers.addAll(_temp_controllers);
                for (Iterator iterator4 = _add_list.iterator(); iterator4.hasNext(); ((ASViewController)iterator4.next()).notifyPageDidAddToNavigationController()) { }
                for (Iterator iterator5 = _remove_list.iterator(); iterator5.hasNext(); ((ASViewController)iterator5.next()).notifyPageDidRemoveFromNavigationController()) { }
                _add_list.clear();
                _remove_list.clear();
                if (asviewcontroller != asviewcontroller1)
                {
                    if (flag)
                    {
                        animatePopViewController(asviewcontroller, asviewcontroller1, animated);
                        return;
                    } else
                    {
                        animatedPushViewController(asviewcontroller, asviewcontroller1, animated);
                        return;
                    }
                } else
                {
                    onPageCommandExecuteFinished();
                    return;
                }
            }

            
            {
                this$0 = ASNavigationController.this;
                animated = flag;
                super();
            }
        }).runInMainThread();
    }

    public void executePageCommand()
    {
        this;
        JVM INSTR monitorenter ;
        if (!_is_animating)
        {
            break MISSING_BLOCK_LABEL_12;
        }
        this;
        JVM INSTR monitorexit ;
        return;
        this;
        JVM INSTR monitorexit ;
        Vector vector = _page_commands;
        vector;
        JVM INSTR monitorenter ;
        if (_page_commands.size() <= 0)
        {
            break MISSING_BLOCK_LABEL_98;
        }
        this;
        JVM INSTR monitorenter ;
        _is_animating = true;
        this;
        JVM INSTR monitorexit ;
        _temp_controllers.addAll(_controllers);
        Exception exception;
        Exception exception1;
        for (boolean flag = ((PageCommand)_page_commands.firstElement()).animated; _page_commands.size() > 0 && ((PageCommand)_page_commands.firstElement()).animated == flag; ((PageCommand)_page_commands.remove(0)).run())
        {
            break MISSING_BLOCK_LABEL_116;
        }

        exchangeViewControllers(flag);
        vector;
        JVM INSTR monitorexit ;
        return;
        exception1;
        vector;
        JVM INSTR monitorexit ;
        throw exception1;
        exception;
        this;
        JVM INSTR monitorexit ;
        throw exception;
        exception1;
        this;
        JVM INSTR monitorexit ;
        throw exception1;
    }

    public void finish()
    {
        onControllerWillFinish();
        getDeviceController().unlockWifi();
        super.finish();
    }

    protected String getControllerName()
    {
        return "";
    }

    public int getCurrentOrientation()
    {
        byte byte0 = 1;
        int i = getWindowManager().getDefaultDisplay().getRotation();
        if (i == 1 || i == 3)
        {
            byte0 = 2;
        }
        return byte0;
    }

    public ASDeviceController getDeviceController()
    {
        return _device_controller;
    }

    public int getScreenHeight()
    {
        return _display_metrics.heightPixels;
    }

    public int getScreenWidth()
    {
        return _display_metrics.widthPixels;
    }

    public ASViewController getTopController()
    {
        ASViewController asviewcontroller = null;
        synchronized (_controllers)
        {
            if (_controllers.size() > 0)
            {
                asviewcontroller = (ASViewController)_controllers.lastElement();
            }
        }
        return asviewcontroller;
        exception;
        vector;
        JVM INSTR monitorexit ;
        throw exception;
    }

    public Vector getViewControllers()
    {
        return new Vector(_controllers);
    }

    public boolean isAnimationEnable()
    {
        return _animation_enable;
    }

    public boolean isInBackground()
    {
        return _in_background;
    }

    public boolean onBackLongPressed()
    {
        return false;
    }

    public void onBackPressed()
    {
        boolean flag1 = false;
        ASViewController asviewcontroller = getTopController();
        boolean flag = flag1;
        if (asviewcontroller != null)
        {
            flag = flag1;
            if (asviewcontroller.onBackPressed())
            {
                flag = true;
            }
        }
        if (!flag)
        {
            finish();
        }
    }

    protected void onControllerDidLoad()
    {
    }

    protected void onControllerWillFinish()
    {
    }

    protected void onControllerWillLoad()
    {
    }

    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setNavigationController(this);
        getWindowManager().getDefaultDisplay().getMetrics(_display_metrics);
        ASRunner.construct();
        _device_controller = new ASDeviceController(this);
        onControllerWillLoad();
        _root_view = new ASNavigationControllerView(this);
        _root_view.setPageController(this);
        setContentView(_root_view);
        onControllerDidLoad();
    }

    public boolean onKeyLongPress(int i, KeyEvent keyevent)
    {
        if (i == 4)
        {
            return onBackLongPressed();
        } else
        {
            return super.onKeyLongPress(i, keyevent);
        }
    }

    public boolean onKeyUp(int i, KeyEvent keyevent)
    {
        switch (i)
        {
        case 83: // 'S'
        default:
            return super.onKeyUp(i, keyevent);

        case 84: // 'T'
            return onSearchPressed();

        case 82: // 'R'
            return onMenuPressed();
        }
    }

    public void onLowMemory()
    {
        System.out.println("on low memory");
        super.onLowMemory();
    }

    protected void onPause()
    {
        super.onPause();
        _in_background = true;
    }

    public boolean onReceivedGestureDown()
    {
        ASViewController asviewcontroller = getTopController();
        if (asviewcontroller != null)
        {
            return asviewcontroller.onReceivedGestureDown();
        } else
        {
            return false;
        }
    }

    public boolean onReceivedGestureLeft()
    {
        ASViewController asviewcontroller = getTopController();
        if (asviewcontroller != null)
        {
            return asviewcontroller.onReceivedGestureLeft();
        } else
        {
            return false;
        }
    }

    public boolean onReceivedGestureRight()
    {
        ASViewController asviewcontroller = getTopController();
        if (asviewcontroller != null)
        {
            return asviewcontroller.onReceivedGestureRight();
        } else
        {
            return false;
        }
    }

    public boolean onReceivedGestureUp()
    {
        ASViewController asviewcontroller = getTopController();
        if (asviewcontroller != null)
        {
            return asviewcontroller.onReceivedGestureUp();
        } else
        {
            return false;
        }
    }

    protected void onResume()
    {
        super.onResume();
        _in_background = false;
    }

    protected void onSizeChanged(int i, int j, int k, int l)
    {
        ASViewController asviewcontroller = getTopController();
        if (asviewcontroller != null)
        {
            asviewcontroller.onSizeChanged(i, j, k, l);
        }
    }

    public void popToViewController(ASViewController asviewcontroller)
    {
        popToViewController(asviewcontroller, _animation_enable);
    }

    public void popToViewController(final ASViewController aController, boolean flag)
    {
        aController = new PageCommand() {

            final ASNavigationController this$0;
            final ASViewController val$aController;

            public void run()
            {
                if (aController != null && (_temp_controllers.size() <= 0 || aController != _temp_controllers.lastElement()))
                {
                    for (; _temp_controllers.size() > 0 && _temp_controllers.lastElement() != aController; _temp_controllers.remove(_temp_controllers.size() - 1)) { }
                }
            }

            
            {
                this$0 = ASNavigationController.this;
                aController = asviewcontroller;
                super();
            }
        };
        aController.animated = flag;
        pushPageCommand(aController);
    }

    public void popViewController()
    {
        popViewController(_animation_enable);
    }

    public void popViewController(boolean flag)
    {
        PageCommand pagecommand = new PageCommand() {

            final ASNavigationController this$0;

            public void run()
            {
                if (_temp_controllers.size() > 0)
                {
                    _temp_controllers.remove(_temp_controllers.size() - 1);
                }
            }

            
            {
                this$0 = ASNavigationController.this;
                super();
            }
        };
        pagecommand.animated = flag;
        pushPageCommand(pagecommand);
    }

    public void printControllers()
    {
        printControllers(_controllers);
    }

    public void printControllers(Vector vector)
    {
        ASViewController asviewcontroller;
        for (vector = vector.iterator(); vector.hasNext(); System.out.print((new StringBuilder()).append(asviewcontroller.getPageType()).append(" ").toString()))
        {
            asviewcontroller = (ASViewController)vector.next();
        }

        System.out.print("\n");
    }

    public void pushPageCommand(PageCommand pagecommand)
    {
        synchronized (_page_commands)
        {
            _page_commands.add(pagecommand);
        }
        executePageCommand();
        return;
        pagecommand;
        vector;
        JVM INSTR monitorexit ;
        throw pagecommand;
    }

    public void pushViewController(ASViewController asviewcontroller)
    {
        pushViewController(asviewcontroller, _animation_enable);
    }

    public void pushViewController(final ASViewController aController, boolean flag)
    {
        aController = new PageCommand() {

            final ASNavigationController this$0;
            final ASViewController val$aController;

            public void run()
            {
                if (aController != null && (_temp_controllers.size() <= 0 || aController != _temp_controllers.lastElement()))
                {
                    _temp_controllers.add(aController);
                }
            }

            
            {
                this$0 = ASNavigationController.this;
                aController = asviewcontroller;
                super();
            }
        };
        aController.animated = flag;
        pushPageCommand(aController);
    }

    public void reloadLayout()
    {
        if (_root_view != null)
        {
            _root_view.requestLayout();
        }
    }

    public void removePageView(final ASViewController aPage)
    {
        final ASPageView page_view = aPage.getPageView();
        _root_view.post(new Runnable() {

            final ASNavigationController this$0;
            final ASViewController val$aPage;
            final View val$page_view;

            public void run()
            {
                aPage.onPageDidDisappear();
                _root_view.removeView(page_view);
            }

            
            {
                this$0 = ASNavigationController.this;
                aPage = asviewcontroller;
                page_view = view;
                super();
            }
        });
    }

    public void removePageView(final ASViewController aPage, Animation animation)
    {
        animation.setAnimationListener(new Animation.AnimationListener() {

            final ASNavigationController this$0;
            final ASViewController val$aPage;

            public void onAnimationEnd(Animation animation1)
            {
                removePageView(aPage);
            }

            public void onAnimationRepeat(Animation animation1)
            {
            }

            public void onAnimationStart(Animation animation1)
            {
            }

            
            {
                this$0 = ASNavigationController.this;
                aPage = asviewcontroller;
                super();
            }
        });
        aPage.getPageView().startAnimation(animation);
    }

    public void setAnimationEnable(boolean flag)
    {
        _animation_enable = flag;
    }

    public void setNavigationTitle(String s)
    {
        super.setTitle(s);
    }

    public void setViewControllers(Vector vector)
    {
        setViewControllers(vector, _animation_enable);
    }

    public void setViewControllers(final Vector aControllerList, boolean flag)
    {
        aControllerList = new PageCommand() {

            final ASNavigationController this$0;
            final Vector val$aControllerList;

            public void run()
            {
                if (aControllerList != null)
                {
                    _temp_controllers.removeAllElements();
                    _temp_controllers.addAll(aControllerList);
                }
            }

            
            {
                this$0 = ASNavigationController.this;
                aControllerList = vector;
                super();
            }
        };
        aControllerList.animated = flag;
        pushPageCommand(aControllerList);
    }










}
