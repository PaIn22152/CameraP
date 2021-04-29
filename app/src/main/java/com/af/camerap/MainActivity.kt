package com.af.camerap

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.Gravity
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.databinding.DataBindingUtil
import com.af.camerap.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture


class MainActivity : BaseActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var customLifecycle: CustomLifecycle


    private lateinit var binding: ActivityMainBinding
    private lateinit var data: MainUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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
        lp.gravity = Gravity.TOP or Gravity.END
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        lp.format = PixelFormat.RGBA_8888
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        lp.x = 0
        lp.y = 0
        // 设置悬浮窗口长宽数据
        lp.width = (0.3 * w).toInt()
        lp.height = (0.3 * h).toInt()
        val ll = LinearLayout(this)
        val pv = PreviewView(this)
        ll.addView(pv)
        ll.setOnClickListener {
            windowManager.removeView(ll)
            customLifecycle.doOnDestroyed()
        }
        windowManager.addView(ll, lp)

        bindView(pv)

    }

    fun bindView(previewView: PreviewView) {
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