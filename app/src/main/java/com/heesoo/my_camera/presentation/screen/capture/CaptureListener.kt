package com.heesoo.my_camera.presentation.screen.capture

import androidx.camera.view.PreviewView

internal interface CaptureListener {
    val preview: PreviewView
    val isPermissionGranted: Boolean

    fun requestCameraPermission()

    fun takePhoto(requestId: Long)

    fun goToFrames()
}
