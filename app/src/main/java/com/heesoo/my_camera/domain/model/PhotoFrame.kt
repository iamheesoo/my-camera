package com.heesoo.my_camera.domain.model

enum class PhotoFrame(val rowCount: Int, val columnCount: Int) {
    STRIP(rowCount = 3, columnCount = 1),
    GRID(rowCount = 2, columnCount = 2);

    val slotCount: Int get() = rowCount * columnCount
    val width: Int get() = FRAME_PADDING * 2 + columnCount * SLOT_WIDTH + (columnCount - 1) * SLOT_GAP
    val height: Int get() = FRAME_PADDING * 2 + rowCount * SLOT_HEIGHT + (rowCount - 1) * SLOT_GAP

    fun slotLeft(slotIndex: Int): Int = FRAME_PADDING + slotIndex % columnCount * (SLOT_WIDTH + SLOT_GAP)

    fun slotTop(slotIndex: Int): Int = FRAME_PADDING + slotIndex / columnCount * (SLOT_HEIGHT + SLOT_GAP)

    companion object {
        const val FRAME_PADDING = 8
        const val SLOT_GAP = 6
        const val SLOT_WIDTH = 120
        const val SLOT_HEIGHT = 90
        const val SLOT_ASPECT_RATIO = 120f / 90f
    }
}
