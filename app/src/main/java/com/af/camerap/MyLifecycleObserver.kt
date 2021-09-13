package com.af.camerap

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/09/03 - 17:59
 * Author     Payne.
 * About      类描述：
 */
class MyLifecycleObserver : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(lifecycleOwner: LifecycleOwner) {
        Log.d(
            "16124",
            "MyLifecycleObserver  onResume  LifecycleOwner=$lifecycleOwner"
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(lifecycleOwner: LifecycleOwner) {
        Log.d(
            "16124",
            "MyLifecycleObserver  onPause  LifecycleOwner=$lifecycleOwner"
        )
    }
}