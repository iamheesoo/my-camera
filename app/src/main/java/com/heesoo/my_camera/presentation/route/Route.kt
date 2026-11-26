package com.heesoo.my_camera.presentation.route

import com.heesoo.my_camera.domain.model.PhotoFrame
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object FrameSelection : Route

    @Serializable
    data class Capture(val frame: PhotoFrame) : Route
}
