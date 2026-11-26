package com.heesoo.my_camera.presentation.screen.capture

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Event

@Composable
internal fun CaptureRoute(viewModel: CaptureViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCameraPermissionGranted by remember(context) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        isCameraPermissionGranted = isGranted
    }
    val previewView = remember(context) { PreviewView(context) }
    val camera = remember(previewView, lifecycleOwner) {
        TestCameraSession(previewView = previewView, lifecycleOwner = lifecycleOwner)
    }

    DisposableEffect(camera, isCameraPermissionGranted) {
        if (isCameraPermissionGranted) {
            camera.bind { isAvailable ->
                viewModel.sendEvent(Event.CameraAvailabilityChanged(isAvailable))
            }
        }
        onDispose { camera.close() }
    }

    val listener = remember(previewView, camera, permissionLauncher, viewModel) {
        object : CaptureListener {
            override val preview = previewView
            override val isPermissionGranted: Boolean
                get() = isCameraPermissionGranted

            override fun requestCameraPermission() {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            override fun takePhoto() {
                camera.takePhoto { isSuccessful ->
                    viewModel.sendEvent(Event.CaptureCompleted(isSuccessful))
                }
            }
        }
    }
    CaptureScreen(viewModel = viewModel, listener = listener)
}
