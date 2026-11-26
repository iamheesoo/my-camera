package com.heesoo.my_camera.presentation.screen.capture

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.UUID

internal class TestCameraSession(
    private val previewView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
) {
    private val context = previewView.context
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private val preview = Preview.Builder().build()
    private val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
    private var cameraProvider: ProcessCameraProvider? = null
    private var bindingId = 0
    private var photoFile: File? = null

    fun bind(onAvailabilityChanged: (Boolean) -> Unit) {
        val currentBindingId = ++bindingId
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            if (bindingId == currentBindingId) {
                val isAvailable = runCatching {
                    val provider = providerFuture.get()
                    preview.surfaceProvider = previewView.surfaceProvider
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageCapture,
                    )
                    cameraProvider = provider
                }.isSuccess
                onAvailabilityChanged(isAvailable)
            }
        }, mainExecutor)
    }

    fun takePhoto(onCompleted: (Boolean) -> Unit) {
        if (cameraProvider != null) {
            val currentBindingId = bindingId
            val outputFile = File(context.cacheDir, "camera-check-${UUID.randomUUID()}.jpg")
            previewView.display?.let { imageCapture.targetRotation = it.rotation }
            imageCapture.takePicture(
                ImageCapture.OutputFileOptions.Builder(outputFile).build(),
                mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        if (bindingId == currentBindingId) {
                            photoFile?.delete()
                            photoFile = outputFile
                            onCompleted(true)
                        } else {
                            outputFile.delete()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        outputFile.delete()
                        if (bindingId == currentBindingId) {
                            onCompleted(false)
                        }
                    }
                },
            )
        } else {
            onCompleted(false)
        }
    }

    fun close() {
        bindingId++
        cameraProvider?.unbind(preview, imageCapture)
        cameraProvider = null
        photoFile?.delete()
        photoFile = null
    }
}
