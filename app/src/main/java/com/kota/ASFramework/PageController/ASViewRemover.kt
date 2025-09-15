package com.kota.ASFramework.PageController

import android.view.View
import android.view.ViewGroup
import com.kota.ASFramework.Thread.ASRunner

class ASViewRemover private constructor(
    private val _parent_view: ViewGroup,
    private val _target_view: View
) {
    companion object {
        fun remove(parentView: ViewGroup, targetView: View) {
            val remover = ASViewRemover(parentView, targetView)
            remover.start()
        }
    }
    
    fun start() {
        ASRunner.runInNewThread {
            try {
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            remove()
        }
    }
    
    private fun remove() {
        object : ASRunner() {
            override fun run() {
                _parent_view.removeView(_target_view)
            }
        }.runInMainThread()
    }
}
