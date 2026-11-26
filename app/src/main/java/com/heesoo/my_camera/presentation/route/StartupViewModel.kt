package com.heesoo.my_camera.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heesoo.my_camera.domain.usecase.ClearStaleCapturesUseCase
import com.heesoo.my_camera.domain.usecase.GetTutorialCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
internal class StartupViewModel @Inject constructor(
    private val clearStaleCapturesUseCase: ClearStaleCapturesUseCase,
    private val getTutorialCompletionUseCase: GetTutorialCompletionUseCase,
) : ViewModel() {
    private val mutableStartDestination = MutableStateFlow<Route?>(null)
    val startDestination = mutableStartDestination.asStateFlow()

    init {
        viewModelScope.launch {
            clearStaleCapturesUseCase()
            val isTutorialCompleted = try {
                getTutorialCompletionUseCase()
            } catch (_: IOException) {
                false
            }
            mutableStartDestination.value = if (isTutorialCompleted) Route.FrameSelection else Route.Tutorial
        }
    }
}
