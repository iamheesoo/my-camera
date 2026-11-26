package com.heesoo.my_camera.domain.model

enum class PhotoFrame(val rowCount: Int, val columnCount: Int) {
    STRIP(rowCount = 3, columnCount = 1),
    GRID(rowCount = 2, columnCount = 2);

    val slotCount: Int get() = rowCount * columnCount
}
