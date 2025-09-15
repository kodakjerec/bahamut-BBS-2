package com.kota.ASFramework.PageController

import android.view.animation.Animation
import com.kota.ASFramework.Thread.ASRunner

/* loaded from: classes.dex */
open class ASNavigationControllerPopAnimation(
    private val _source_controller: ASViewController?,
    private val _target_controller: ASViewController?
) {
    private var _source_finished = false
    private var _target_finished = false
    private var _finished = false

    fun start(animated: Boolean) {
        if (!animated) {
            finish()
        } else {
            animate()
        }
    }

    private fun animate() {
        val animationDuration = 250

        if (_source_controller?.getPageView() != null) {
            val sourceAnimation = ASPageAnimation.getFadeOutTtRightAnimation()
            sourceAnimation.duration = animationDuration.toLong()
            sourceAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    synchronized(this@ASNavigationControllerPopAnimation) {
                        _source_controller.getPageView()?.onPageAnimationFinished()
                        _source_finished = true
                        checkFinished()
                    }
                }
            })
            _source_controller.getPageView()?.onPageAnimationStart()
            _source_controller.getPageView()?.startAnimation(sourceAnimation)
        } else {
            synchronized(this) {
                _source_finished = true
                checkFinished()
            }
        }

        if (_target_controller?.getPageView() != null) {
            val targetAnimation = ASPageAnimation.getFadeInFromLeftAnimation()
            targetAnimation.duration = animationDuration.toLong()
            targetAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    synchronized(this@ASNavigationControllerPopAnimation) {
                        _target_controller.getPageView()?.onPageAnimationFinished()
                        _target_finished = true
                        checkFinished()
                    }
                }
            })
            _target_controller.getPageView()?.onPageAnimationStart()
            _target_controller.getPageView()?.startAnimation(targetAnimation)
        } else {
            synchronized(this) {
                _target_finished = true
                checkFinished()
            }
        }
    }

    private fun checkFinished() {
        if (_source_finished && _target_finished) {
            finish()
        }
    }

    private fun finish() {
        if (!_finished) {
            object : ASRunner() {
                override fun run() {
                    onAnimationFinished()
                }
            }.runInMainThread()
            _finished = true
        }
    }

    open fun onAnimationFinished() {}
}
