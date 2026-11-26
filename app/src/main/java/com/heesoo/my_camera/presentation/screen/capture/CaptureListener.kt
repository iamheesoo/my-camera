package com.heesoo.my_camera.presentation.screen.capture

import com.heesoo.my_camera.domain.model.PhotoFrame

internal interface CaptureListener {
    fun goToNextGuest(frame: PhotoFrame)

    fun goToFrames()
}
