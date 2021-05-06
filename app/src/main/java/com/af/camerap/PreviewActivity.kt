package com.af.camerap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import com.af.camerap.databinding.ActivityPreviewBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera
    lateinit var currentPhotoPath: String
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

        binding.ivTake.setOnClickListener {
            takePicture()
            binding.vApFlash.visibility = View.VISIBLE
            MainScope().launch {
                delay(100)
                binding.vApFlash.visibility = View.GONE
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            d("currentPhotoPath =$currentPhotoPath")
        }
    }


    val REQUEST_TAKE_PHOTO = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile = File(currentPhotoPath)
                // Continue only if the File was successfully created
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.af.camerap.auth",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }


    private fun takePicture() {

        val outputFileOptions =
            ImageCapture.OutputFileOptions.Builder(createImageFile()).build()
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    d("take Picture  onError=$error")
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    d("take Picture  onImageSaved  " + outputFileResults.savedUri + "  currentPhotoPath =$currentPhotoPath")
                    Toast.makeText(this@PreviewActivity, "save success", Toast.LENGTH_LONG).show()
//                   dispatchTakePictureIntent()
//                    galleryAddPic()
                }
            })
    }

    fun notifyMedia() {

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
//            .setTargetResolution(Size(1280, 720))
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