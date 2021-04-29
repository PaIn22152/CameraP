package com.af.camerap

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/04/27 - 18:32
 * Author     Payne.
 * About      类描述：
 */
class CustomLifecycle : LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun doOnResume() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun doOnDestroyed() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

}