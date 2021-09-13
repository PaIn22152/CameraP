package com.af.camerap

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/09/06 - 15:20
 * Author     Payne.
 * About      类描述：
 */
class MyLifecycleEventObserver : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d(
            "16124",
            "MyLifecycleEventObserver  onStateChanged  LifecycleOwner=$source   Event=$event"
        )
    }
}