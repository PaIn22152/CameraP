package com.af.camerap

import android.annotation.TargetApi
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/04/29 - 16:49
 * Author     Payne.
 * About      类描述：
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class MyNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        d(" onNotificationPosted")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        d(" onNotificationRemoved")
    }
}
