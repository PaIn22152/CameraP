package com.af.camerap

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/04/29 - 17:19
 * Author     Payne.
 * About      类描述：
 */
abstract class BaseActivity : AppCompatActivity() {
    companion object {
        interface OnActivityResultListener {
            fun onResult(requestCode: Int, resultCode: Int, data: Intent?)
        }

        interface OnPermissionResultListener {
            fun onResult(
                requestCode: Int,
                permissions: Array<out String>,
                grantResults: IntArray
            )
        }
    }

    private var onActivityResultListener: OnActivityResultListener? = null
    private var onPermissionResultListener: OnPermissionResultListener? = null

    fun startActivityForResult(
        intent: Intent?,
        requestCode: Int,
        listener: OnActivityResultListener?
    ) {
        super.startActivityForResult(intent, requestCode)
        this.onActivityResultListener = listener
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermissions(
        permissions: Array<out String>,
        requestCode: Int,
        listener: OnPermissionResultListener
    ) {
        super.requestPermissions(permissions, requestCode)
        this.onPermissionResultListener = listener
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultListener?.onResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onPermissionResultListener?.onResult(requestCode, permissions, grantResults)
    }


}