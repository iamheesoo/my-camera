package com.heesoo.my_camera.presentation.screen.capture

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.StreamState
import androidx.lifecycle.Observer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Phase
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
internal fun CaptureRoute(viewModel: CaptureViewModel, onBackToFrames: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isComplete = state.phase == Phase.COMPLETE
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
    val savePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.sendEvent(Event.SavePermissionResult(isGranted = isGranted))
    }
    val previewView = remember(context, isComplete) {
        PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE }
    }
    val camera = remember(previewView, lifecycleOwner) {
        CameraCaptureSession(previewView = previewView, lifecycleOwner = lifecycleOwner)
    }

    DisposableEffect(camera, isCameraPermissionGranted, isComplete) {
        val streamObserver = Observer<StreamState> { streamState ->
            viewModel.sendEvent(Event.CameraReadyChanged(isReady = streamState == StreamState.STREAMING))
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.sendEvent(Event.CameraReadyChanged(isReady = false))
            }
        }
        if (isCameraPermissionGranted && !isComplete) {
            previewView.previewStreamState.observe(lifecycleOwner, streamObserver)
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            camera.bind { isAvailable ->
                if (!isAvailable) viewModel.sendEvent(Event.CameraUnavailable)
            }
        }
        onDispose {
            previewView.previewStreamState.removeObserver(streamObserver)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            camera.close()
            viewModel.sendEvent(Event.CameraReadyChanged(isReady = false))
        }
    }

    val listener = remember(previewView, camera, permissionLauncher, savePermissionLauncher, viewModel, onBackToFrames) {
        object : CaptureListener {
            override val preview = previewView
            override val isPermissionGranted: Boolean
                get() = isCameraPermissionGranted

            override fun requestCameraPermission() {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            override fun takePhoto(requestId: Long) {
                if (viewModel.isActiveRequest(requestId)) {
                    camera.takePhoto { photoFile ->
                        viewModel.sendEvent(
                            Event.CaptureCompleted(requestId = requestId, photoFile = photoFile),
                        )
                    }
                }
            }

            override fun checkSavePermission() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.sendEvent(Event.SavePermissionResult(isGranted = true))
                } else {
                    savePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            override fun goToFrames() = onBackToFrames()
        }
    }
    CaptureScreen(viewModel = viewModel, listener = listener)
}
