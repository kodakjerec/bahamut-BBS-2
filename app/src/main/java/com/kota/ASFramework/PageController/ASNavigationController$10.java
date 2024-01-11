// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import com.kumi.ASFramework.Thread.ASRunner;
import java.util.Iterator;
import java.util.Vector;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationController, ASViewController

class val.animated extends ASRunner
{

    final ASNavigationController this$0;
    final boolean val$animated;

    public void run()
    {
        ASViewController asviewcontroller;
        ASViewController asviewcontroller1;
        boolean flag;
        if (ASNavigationController.access$200(ASNavigationController.this).size() > 0)
        {
            asviewcontroller = (ASViewController)ASNavigationController.access$200(ASNavigationController.this).lastElement();
        } else
        {
            asviewcontroller = null;
        }
        if (ASNavigationController.access$500(ASNavigationController.this).size() > 0)
        {
            asviewcontroller1 = (ASViewController)ASNavigationController.access$500(ASNavigationController.this).lastElement();
        } else
        {
            asviewcontroller1 = null;
        }
        flag = ASNavigationController.access$200(ASNavigationController.this).contains(asviewcontroller1);
        for (Iterator iterator = ASNavigationController.access$200(ASNavigationController.this).iterator(); iterator.hasNext(); ((ASViewController)iterator.next()).prepareForRemove()) { }
        for (Iterator iterator1 = ASNavigationController.access$500(ASNavigationController.this).iterator(); iterator1.hasNext(); ((ASViewController)iterator1.next()).prepareForAdd()) { }
        ASViewController asviewcontroller2;
        for (Iterator iterator2 = ASNavigationController.access$200(ASNavigationController.this).iterator(); iterator2.hasNext(); asviewcontroller2.cleanMark())
        {
            asviewcontroller2 = (ASViewController)iterator2.next();
            if (asviewcontroller2.isMarkedRemoved())
            {
                ASNavigationController.access$600(ASNavigationController.this).add(asviewcontroller2);
            }
        }

        ASViewController asviewcontroller3;
        for (Iterator iterator3 = ASNavigationController.access$500(ASNavigationController.this).iterator(); iterator3.hasNext(); asviewcontroller3.cleanMark())
        {
            asviewcontroller3 = (ASViewController)iterator3.next();
            if (asviewcontroller3.isMarkedAdded())
            {
                asviewcontroller3.setNavigationController(ASNavigationController.this);
                ASNavigationController.access$700(ASNavigationController.this).add(asviewcontroller3);
            }
        }

        ASNavigationController.access$200(ASNavigationController.this).removeAllElements();
        ASNavigationController.access$200(ASNavigationController.this).addAll(ASNavigationController.access$500(ASNavigationController.this));
        for (Iterator iterator4 = ASNavigationController.access$700(ASNavigationController.this).iterator(); iterator4.hasNext(); ((ASViewController)iterator4.next()).notifyPageDidAddToNavigationController()) { }
        for (Iterator iterator5 = ASNavigationController.access$600(ASNavigationController.this).iterator(); iterator5.hasNext(); ((ASViewController)iterator5.next()).notifyPageDidRemoveFromNavigationController()) { }
        ASNavigationController.access$700(ASNavigationController.this).clear();
        ASNavigationController.access$600(ASNavigationController.this).clear();
        if (asviewcontroller != asviewcontroller1)
        {
            if (flag)
            {
                ASNavigationController.access$800(ASNavigationController.this, asviewcontroller, asviewcontroller1, val$animated);
                return;
            } else
            {
                ASNavigationController.access$900(ASNavigationController.this, asviewcontroller, asviewcontroller1, val$animated);
                return;
            }
        } else
        {
            ASNavigationController.access$300(ASNavigationController.this);
            return;
        }
    }

    ()
    {
        this$0 = final_asnavigationcontroller;
        val$animated = Z.this;
        super();
    }
}
