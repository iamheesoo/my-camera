package com.heesoo.my_camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.heesoo.my_camera.presentation.route.PhotoboothNavHost
import com.heesoo.my_camera.ui.theme.MycameraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MycameraTheme {
                PhotoboothNavHost()
            }
        }
    }
}
