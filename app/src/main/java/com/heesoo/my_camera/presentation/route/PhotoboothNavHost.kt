package com.heesoo.my_camera.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.screen.capture.CaptureRoute
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionListener
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionScreen

@Composable
fun PhotoboothNavHost() {
    val navController = rememberNavController()
    val frameSelectionListener = remember(navController) {
        object : FrameSelectionListener {
            override fun goToCapture(frame: PhotoFrame) {
                navController.navigate(Route.Capture(frame = frame)) {
                    launchSingleTop = true
                }
            }
        }
    }
    NavHost(navController = navController, startDestination = Route.FrameSelection) {
        composable<Route.FrameSelection> {
            FrameSelectionScreen(viewModel = hiltViewModel(), listener = frameSelectionListener)
        }
        composable<Route.Capture> {
            CaptureRoute(
                viewModel = hiltViewModel(),
                onBackToFrames = { navController.popBackStack() },
                onNextGuest = { frame ->
                    navController.navigate(Route.Capture(frame = frame)) {
                        popUpTo<Route.Capture> { inclusive = true }
                    }
                },
            )
        }
    }
}
