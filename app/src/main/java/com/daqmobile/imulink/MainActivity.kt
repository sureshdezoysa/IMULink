package com.daqmobile.imulink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.daqmobile.imulink.ui.IMULinkApp
import com.daqmobile.imulink.ui.theme.IMULinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IMULinkTheme {
                IMULinkApp()
            }
        }
    }
}
