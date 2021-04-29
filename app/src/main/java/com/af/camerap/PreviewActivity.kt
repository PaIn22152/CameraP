package com.af.camerap

import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.af.camerap.databinding.ActivityPreviewBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera
    private lateinit var path: String
    private var front = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewView = binding.previewView
        front = intent.getBooleanExtra("front", true)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
        cameraProviderFuture.addListener(Runnable {
            bindView(previewView)

        }, ContextCompat.getMainExecutor(this))

        path = filesDir.absolutePath + "/pic.jpg"
//        path = cacheDir.absolutePath + "/name123.jpg"
        d("path=$path")
        binding.ivTake.setOnClickListener {
            takePicture()
        }
    }

    fun takePicture() {
        val outputFileOptions =
            ImageCapture.OutputFileOptions.Builder(File(path)).build()
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    d("take Picture  onError=$error")
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    d("take Picture  onImageSaved  " + outputFileResults.savedUri)
                    notifyMedia()
                }
            })
    }

    fun notifyMedia() {

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

        val lens = if (front) {
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


        // Attach use cases to the camera with the same lifecycle owner
        camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner, cameraSelector, preview, imageAnalysis, imageCapture
        )
        //开启闪光
//        camera.cameraControl.enableTorch(true)

        // Connect the preview use case to the previewView
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
}