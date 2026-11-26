package com.heesoo.my_camera.presentation.screen.frameselection

import com.heesoo.my_camera.core.mvi.BaseMviViewModel
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionContract.Effect
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionContract.Event
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FrameSelectionViewModel @Inject constructor() :
    BaseMviViewModel<State, Event, Effect>(initialState = State()) {

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.ClickFrame -> selectFrame(event.frame)
            Event.ClickStartCapture -> goToCapture()
        }
    }

    private fun selectFrame(frame: PhotoFrame) {
        setState { copy(selectedFrame = frame) }
    }

    private fun goToCapture() = sendEffect(Effect.GoToCapture(frame = state.value.selectedFrame))
}
