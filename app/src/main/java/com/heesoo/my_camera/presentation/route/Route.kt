package com.heesoo.my_camera.presentation.route

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object FrameSelection : Route

    @Serializable
    data object Capture : Route
}
