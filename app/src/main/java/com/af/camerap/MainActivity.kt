package com.af.camerap

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.*
import android.widget.CompoundButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.databinding.DataBindingUtil
import com.af.camerap.databinding.ActivityMainBinding
import com.af.camerap.databinding.ViewFloatBinding
import com.google.common.util.concurrent.ListenableFuture

private const val long_click_time = 100//长按时间后，view可以拖动

class MainActivity : BaseActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var customLifecycle: CustomLifecycle


    private lateinit var binding: ActivityMainBinding
    private lateinit var data: MainUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        lifecycle.addObserver(MyLifecycleObserver())
        lifecycle.addObserver(MyLifecycleEventObserver())

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        data = MainUI()
        binding.data = data

        binding.checkboxBack.setOnCheckedChangeListener { _: CompoundButton, c: Boolean ->
            data.front = !c
            binding.data = data
        }
        binding.checkboxFront.setOnCheckedChangeListener { _: CompoundButton, c: Boolean ->
            data.front = c
            binding.data = data
        }


        binding.previewTake.setOnClickListener {
            val intent = Intent(this, PreviewActivity::class.java)
            intent.putExtra("front", data.front)
            startActivity(intent)
        }
        binding.floatPreview.setOnClickListener {
            showFloat()
        }



        cameraProviderFuture = ProcessCameraProvider.getInstance(this)


        requestAllPermissions(this)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun showFloat() {
        val w = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            windowManager.defaultDisplay.width
        }
        val h = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            windowManager.defaultDisplay.height
        }
        val lp = WindowManager.LayoutParams()
        lp.gravity = Gravity.TOP or Gravity.START
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        lp.format = PixelFormat.RGBA_8888
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        var x = 0
        var y = 0
        lp.x = x
        lp.y = y
        // 设置悬浮窗口长宽数据
        lp.width = (0.3 * w).toInt()
        lp.height = (0.3 * h).toInt()

//        val view=LayoutInflater.from(this).inflate(R.layout.view_float,null)
        val binding = ViewFloatBinding.inflate(layoutInflater, null, false)
        val view = binding.root


        binding.tvVfClose.setOnClickListener {
            windowManager.removeView(view)
            customLifecycle.doOnDestroyed()
        }
        var downTime = 0L
        var canDrag = false
        var touch = false
        var downX = 0f
        var downY = 0f


        binding.tvVfDrag.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    d(" downnnnnnnn   x=${event.x}  y=${event.y}    rx=${event.rawX}  ry=${event.rawY}")
                    touch = false
                    downTime = System.currentTimeMillis()
                    downX = event.rawX
                    downY = event.rawY
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    d(" move  eeee ee e   x=${event.x}  y=${event.y}    rx=${event.rawX}  ry=${event.rawY}")

                    if (canDrag) {
                        touch = true

                        lp.x = x + (event.rawX - downX).toInt()
                        lp.y = y + (event.rawY - downY).toInt()

                        windowManager.updateViewLayout(view, lp)
                    } else {
                        val cur = System.currentTimeMillis()
                        canDrag = cur - downTime >= long_click_time
                    }

                }
                MotionEvent.ACTION_UP -> {
                    d("up up up up")
                    if (canDrag) {
                        x = (event.rawX - event.x).toInt()
                        y =
                            (event.rawY - event.y).toInt() - getStatusBarHeight() - binding.pvVfPre.height
                    }
                    canDrag = false

                }
            }
            touch
        }
        windowManager.addView(view, lp)

        bindView(binding.pvVfPre)

    }

    private fun getStatusBarHeight(): Int {
        val rectangle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        return rectangle.top
    }

    private fun bindView(previewView: PreviewView) {
        // Camera provider is now guaranteed to be available
        val cameraProvider = cameraProviderFuture.get()

        // Set up the preview use case to display camera preview.
        val preview = Preview.Builder().build()

        // Set up the capture use case to allow users to take photos.
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val lens = if (data.front) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        // Choose the camera by requiring a lens facing
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lens)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        customLifecycle = CustomLifecycle()
        cameraProvider.bindToLifecycle(
            customLifecycle, cameraSelector, preview, imageAnalysis, imageCapture
        )
        customLifecycle.doOnResume()

        // Connect the preview use case to the previewView
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }


}