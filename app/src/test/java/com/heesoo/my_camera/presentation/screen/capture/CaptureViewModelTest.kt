package com.heesoo.my_camera.presentation.screen.capture

import com.heesoo.my_camera.R
import com.heesoo.my_camera.data.local.CaptureBitmapDecoder
import com.heesoo.my_camera.domain.repository.PhotoRepository
import com.heesoo.my_camera.domain.usecase.SavePhotoUseCase
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Effect
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Event
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Phase
import java.io.File
import java.io.IOException
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
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
    fun tappingSelectedSlotClearsSelectionAndRetakeShowsHint() = runTest {
        val viewModel = completedSession()

        viewModel.sendEvent(Event.ClickSlot(slotIndex = 0))
        viewModel.sendEvent(Event.ClickSlot(slotIndex = 1))
        assertEquals(1, viewModel.state.value.selectedSlotIndex)
        viewModel.sendEvent(Event.ClickSlot(slotIndex = 1))
        assertNull(viewModel.state.value.selectedSlotIndex)

        viewModel.sendEvent(Event.ClickStartRetake)
        assertEquals(Effect.ShowToast(messageResId = R.string.select_slot_to_retake), viewModel.effect.first())
        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(1, viewModel.state.value.retakesRemaining)
    }

    @Test
    fun failedRetakePreservesPhotosAndBudget() = runTest {
        val viewModel = completedSession()
        val originalPhotoList = viewModel.state.value.photoFileList
        val requestId = startRetake(viewModel)
        viewModel.sendEvent(Event.CaptureCompleted(requestId = requestId, photoFile = null))

        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(R.string.capture_failed, viewModel.state.value.messageResId)
        assertNull(withTimeoutOrNull(1) { viewModel.effect.first() })
        assertEquals(originalPhotoList, viewModel.state.value.photoFileList)
        assertTrue(originalPhotoList.all { photoFile -> photoFile.exists() })
        assertEquals(1, viewModel.state.value.retakesRemaining)
    }

    @Test
    fun stoppingCameraWhileRetakeWaitsPreservesResult() = runTest {
        val viewModel = completedSession()
        val originalPhotoList = viewModel.state.value.photoFileList
        viewModel.sendEvent(Event.ClickSlot(slotIndex = 1))
        viewModel.sendEvent(Event.ClickStartRetake)
        assertEquals(Phase.RETAKE_WAITING, viewModel.state.value.phase)

        viewModel.sendEvent(Event.CameraStopped)
        viewModel.sendEvent(Event.CameraReadyChanged(isReady = true))
        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(originalPhotoList, viewModel.state.value.photoFileList)
        assertEquals(1, viewModel.state.value.retakesRemaining)
        assertNull(withTimeoutOrNull(1) { viewModel.effect.first() })
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
        assertNull(withTimeoutOrNull(1) { viewModel.effect.first() })
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
        assertEquals(Effect.ShowCaptureFlash, viewModel.effect.first())

        viewModel.sendEvent(Event.ClickSlot(slotIndex = 0))
        viewModel.sendEvent(Event.ClickStartRetake)
        assertEquals(Phase.COMPLETE, viewModel.state.value.phase)
        assertEquals(1, createViewModel().state.value.retakesRemaining)
    }

    @Test
    fun failedSaveKeepsResultAndCanBeRetriedWithoutDuplicateWrites() = runTest {
        var attempts = 0
        val viewModel = completedSession(createViewModel { _, _ ->
            attempts++
            if (attempts == 1) {
                throw IOException("Storage unavailable")
            }
        })
        val originalPhotoList = viewModel.state.value.photoFileList

        viewModel.sendEvent(Event.ClickSave)
        viewModel.sendEvent(Event.SavePermissionResult(isGranted = true))
        runCurrent()

        assertEquals(R.string.save_photo_failed, viewModel.state.value.messageResId)
        assertEquals(originalPhotoList, viewModel.state.value.photoFileList)
        assertFalse(viewModel.state.value.isSaved)

        viewModel.sendEvent(Event.ClickSave)
        viewModel.sendEvent(Event.ClickSave)
        viewModel.sendEvent(Event.SavePermissionResult(isGranted = true))
        viewModel.sendEvent(Event.SavePermissionResult(isGranted = true))
        runCurrent()
        assertTrue(viewModel.state.value.isSaved)

        viewModel.sendEvent(Event.ClickSave)
        viewModel.sendEvent(Event.SavePermissionResult(isGranted = true))
        runCurrent()
        assertEquals(2, attempts)
    }

    private suspend fun TestScope.completedSession(viewModel: CaptureViewModel = createViewModel()): CaptureViewModel {
        viewModel.sendEvent(Event.CameraReadyChanged(isReady = true))
        repeat(PhotoFrame.STRIP.slotCount) {
            viewModel.sendEvent(
                Event.CaptureCompleted(requestId = finishCountdown(viewModel), photoFile = temporaryFolder.newFile()),
            )
            assertEquals(Effect.ShowCaptureFlash, viewModel.effect.first())
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

    private fun createViewModel(
        onSave: suspend (PhotoFrame, List<File>) -> Unit = { _, _ -> },
    ) = CaptureViewModel(
        frame = PhotoFrame.STRIP,
        thumbnailDecoder = CaptureThumbnailDecoder(ioDispatcher = dispatcher, decoder = CaptureBitmapDecoder()),
        savePhotoUseCase = SavePhotoUseCase(
            repository = object : PhotoRepository {
                override suspend fun clearStaleCaptures() = Unit

                override suspend fun save(frame: PhotoFrame, photoFileList: List<File>) =
                    onSave(frame, photoFileList)
            },
        ),
    )
}
