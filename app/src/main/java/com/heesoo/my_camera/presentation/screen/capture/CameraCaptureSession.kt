package com.heesoo.my_camera.presentation.screen.capture

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import com.heesoo.my_camera.data.local.CaptureCache
import java.io.File

internal class CameraCaptureSession(
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

    fun bind(onAvailabilityChanged: (Boolean) -> Unit) {
        val currentBindingId = ++bindingId
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            if (bindingId == currentBindingId) {
                previewView.doOnLayout {
                    if (bindingId == currentBindingId) {
                        val isAvailable = runCatching {
                            val provider = providerFuture.get()
                            bindUseCases(provider)
                            cameraProvider = provider
                        }.isSuccess
                        onAvailabilityChanged(isAvailable)
                    }
                }
            }
        }, mainExecutor)
    }

    private fun bindUseCases(provider: ProcessCameraProvider) {
        preview.surfaceProvider = previewView.surfaceProvider
        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageCapture)
        previewView.viewPort?.let { viewPort -> useCaseGroup.setViewPort(viewPort) }
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            useCaseGroup.build(),
        )
    }

    fun takePhoto(onCompleted: (File?) -> Unit) {
        if (cameraProvider != null) {
            val currentBindingId = bindingId
            val outputFile = CaptureCache.newPhotoFile(cacheDirectory = context.cacheDir)
            previewView.display?.let { imageCapture.targetRotation = it.rotation }
            val metadata = ImageCapture.Metadata().apply { isReversedHorizontal = true }
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile)
                .setMetadata(metadata)
                .build()
            imageCapture.takePicture(
                outputOptions,
                mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        if (bindingId == currentBindingId) {
                            onCompleted(outputFile)
                        } else {
                            outputFile.delete()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        outputFile.delete()
                        if (bindingId == currentBindingId) {
                            onCompleted(null)
                        }
                    }
                },
            )
        } else {
            onCompleted(null)
        }
    }

    fun close() {
        bindingId++
        cameraProvider?.unbind(preview, imageCapture)
        cameraProvider = null
    }
}
