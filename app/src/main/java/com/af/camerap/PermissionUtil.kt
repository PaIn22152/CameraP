package com.af.camerap

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/04/29 - 16:23
 * Author     Payne.
 * About      类描述：
 */

//所有的普通类型权限（通过requestPermissions方法申请）
val allNormalPermissions = arrayOf(
    android.Manifest.permission.CAMERA,//相机
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,//写入
    android.Manifest.permission.CALL_PHONE,//拨打电话
)

const val REQUEST_CODE_NORMAL = 0x12

fun requestAllPermissions(activity: BaseActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activity.requestPermissions(
            allNormalPermissions,
            REQUEST_CODE_NORMAL,
            object : BaseActivity.Companion.OnPermissionResultListener {
                override fun onResult(
                    requestCode: Int,
                    permissions: Array<out String>,
                    grantResults: IntArray
                ) {
                    if (requestCode == REQUEST_CODE_NORMAL) {
                        requestAllSpecialPermissions(activity)
                    }
                }

            })
    } else {
        requestAllSpecialPermissions(activity)
    }
}

fun requestAllSpecialPermissions(activity: BaseActivity) {
    //todo 跳转到特殊权限界面之前，可以先弹一个说明dialog，用户同意后再到权限申请界面
    if (!checkSpecialPermission(SPECIAL_OVERLAY)) {
        requestSpecialPermission(
            activity,
            SPECIAL_OVERLAY,
            object : BaseActivity.Companion.OnActivityResultListener {
                override fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
                    if (requestCode == SPECIAL_OVERLAY) {
                        if (!checkSpecialPermission(SPECIAL_NOTIFICATION_LISTENER)) {
                            requestSpecialPermission(
                                activity,
                                SPECIAL_NOTIFICATION_LISTENER,
                                null
                            )
                        }
                    }
                }
            })
    } else {
        if (!checkSpecialPermission(SPECIAL_NOTIFICATION_LISTENER)) {
            requestSpecialPermission(
                activity,
                SPECIAL_NOTIFICATION_LISTENER,
                null
            )
        }
    }
}


//一些特殊权限的申请
const val SPECIAL_OVERLAY = 0xab//悬浮窗权限
const val SPECIAL_NOTIFICATION_LISTENER = 0xcd//通知使用权权限
fun checkSpecialPermission(special: Int): Boolean {
    val ctx = MyApp.instance
    when (special) {
        SPECIAL_OVERLAY -> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(ctx)
            } else {
                true
            }
        }
        SPECIAL_NOTIFICATION_LISTENER -> {
            val pkg = ctx.packageName
            val flat: String =
                Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners")
            return if (!TextUtils.isEmpty(flat)) {
                flat.contains(pkg)
            } else false

        }
    }
    return false
}

fun requestSpecialPermission(
    activity: BaseActivity,
    special: Int,
    listener: BaseActivity.Companion.OnActivityResultListener?
) {
    when (special) {
        SPECIAL_OVERLAY -> {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            } else {
                Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
            }
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivityForResult(intent, SPECIAL_OVERLAY, listener)
        }
        SPECIAL_NOTIFICATION_LISTENER -> {
            val intent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                } else {
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                }
            activity.startActivityForResult(
                intent,
                SPECIAL_NOTIFICATION_LISTENER,
                listener
            )
        }
    }
}