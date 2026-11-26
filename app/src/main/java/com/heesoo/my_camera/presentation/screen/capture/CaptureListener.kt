package com.heesoo.my_camera.presentation.screen.capture

import androidx.camera.view.PreviewView
import com.heesoo.my_camera.domain.model.PhotoFrame

internal interface CaptureListener {
    val preview: PreviewView
    val isPermissionGranted: Boolean

    fun requestCameraPermission()

    fun takePhoto(requestId: Long)

    fun checkSavePermission()

    fun goToNextGuest(frame: PhotoFrame)

    fun goToFrames()
}
