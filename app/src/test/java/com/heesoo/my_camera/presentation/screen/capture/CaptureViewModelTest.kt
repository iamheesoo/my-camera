package com.heesoo.my_camera.presentation.screen.capture

import com.heesoo.my_camera.R
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Effect
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Event
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Phase
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun failedRetakePreservesPhotosAndBudget() = runTest {
        val viewModel = completedSession()
        val originalPhotoList = viewModel.state.value.photoFileList
        val requestId = startRetake(viewModel)
        viewModel.sendEvent(Event.CaptureCompleted(requestId = requestId, photoFile = null))

        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(R.string.capture_failed, viewModel.state.value.messageResId)
        assertEquals(originalPhotoList, viewModel.state.value.photoFileList)
        assertTrue(originalPhotoList.all { photoFile -> photoFile.exists() })
        assertEquals(1, viewModel.state.value.retakesRemaining)
    }

    @Test
    fun cancelledRetakeRejectsLateSuccess() = runTest {
        val viewModel = completedSession()
        val originalPhotoList = viewModel.state.value.photoFileList
        val requestId = startRetake(viewModel)
        viewModel.sendEvent(Event.ClickCancelRetake)

        val latePhoto = temporaryFolder.newFile()
        viewModel.sendEvent(Event.CaptureCompleted(requestId = requestId, photoFile = latePhoto))

        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(originalPhotoList, viewModel.state.value.photoFileList)
        assertEquals(1, viewModel.state.value.retakesRemaining)
        assertFalse(latePhoto.exists())
    }

    @Test
    fun successfulRetakeReplacesOnlySelectedSlotAndConsumesBudget() = runTest {
        val viewModel = completedSession()
        val originalPhotoList = viewModel.state.value.photoFileList
        val requestId = startRetake(viewModel)
        val replacementPhoto = temporaryFolder.newFile()
        viewModel.sendEvent(Event.CaptureCompleted(requestId = requestId, photoFile = replacementPhoto))

        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(listOf(originalPhotoList[0], replacementPhoto, originalPhotoList[2]), viewModel.state.value.photoFileList)
        assertFalse(originalPhotoList[1].exists())
        assertEquals(0, viewModel.state.value.retakesRemaining)

        viewModel.sendEvent(Event.ClickSlot(slotIndex = 0))
        viewModel.sendEvent(Event.ClickStartRetake)
        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(1, createViewModel().state.value.retakesRemaining)
    }

    private suspend fun TestScope.completedSession(): CaptureViewModel {
        val viewModel = createViewModel()
        viewModel.sendEvent(Event.CameraReadyChanged(isReady = true))
        repeat(PhotoFrame.STRIP.slotCount) {
            viewModel.sendEvent(
                Event.CaptureCompleted(requestId = finishCountdown(viewModel), photoFile = temporaryFolder.newFile()),
            )
        }
        return viewModel
    }

    private suspend fun TestScope.startRetake(viewModel: CaptureViewModel): Long {
        viewModel.sendEvent(Event.ClickSlot(slotIndex = 1))
        viewModel.sendEvent(Event.ClickStartRetake)
        viewModel.sendEvent(Event.CameraReadyChanged(isReady = true))
        return finishCountdown(viewModel)
    }

    private suspend fun TestScope.finishCountdown(viewModel: CaptureViewModel): Long {
        advanceTimeBy(5_000)
        runCurrent()
        return (viewModel.effect.first() as Effect.TakePhoto).requestId
    }

    private fun createViewModel() = CaptureViewModel(
        frame = PhotoFrame.STRIP,
        thumbnailDecoder = CaptureThumbnailDecoder(ioDispatcher = dispatcher),
    )
}
