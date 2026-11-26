package com.heesoo.my_camera.presentation.screen.frameselection

import com.heesoo.my_camera.domain.model.PhotoFrame

interface FrameSelectionListener {
    fun goToCapture(frame: PhotoFrame)
}
