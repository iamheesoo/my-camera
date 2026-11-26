package com.heesoo.my_camera.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.screen.capture.CaptureListener
import com.heesoo.my_camera.presentation.screen.capture.CaptureRoute
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionListener
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionScreen
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialListener
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialScreen

@Composable
fun PhotoboothNavHost(startDestination: Route) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Route.Tutorial> {
            val listener = object : TutorialListener {
                override fun goToFrameSelection() {
                    navController.navigate(Route.FrameSelection) {
                        popUpTo<Route.Tutorial> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            if (startDestination == Route.Tutorial) {
                TutorialScreen(viewModel = hiltViewModel(), listener = listener)
            } else {
                LaunchedEffect(navController) { listener.goToFrameSelection() }
            }
        }
        composable<Route.FrameSelection> {
            FrameSelectionScreen(
                viewModel = hiltViewModel(),
                listener = object : FrameSelectionListener {
                    override fun goToCapture(frame: PhotoFrame) {
                        navController.navigate(Route.Capture(frame = frame)) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable<Route.Capture> {
            CaptureRoute(
                viewModel = hiltViewModel(),
                listener = object : CaptureListener {
                    override fun goToNextGuest(frame: PhotoFrame) {
                        navController.navigate(Route.Capture(frame = frame)) {
                            popUpTo<Route.Capture> { inclusive = true }
                        }
                    }

                    override fun goToFrames() {
                        navController.popBackStack()
                    }
                },
            )
        }
    }
}
