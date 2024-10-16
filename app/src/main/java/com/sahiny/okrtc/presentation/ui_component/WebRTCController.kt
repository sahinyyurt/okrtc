package com.sahiny.okrtc.presentation.ui_component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahiny.okrtc.presentation.viewmodel.ConnectionViewModel

@Composable
fun WebRTCController(viewModel: ConnectionViewModel) {
    val isCallClicked = rememberSaveable { mutableStateOf(false) }
    val isVideoClicked = rememberSaveable { mutableStateOf(false) }
    Row(
        Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MenuButton(
            imageVector = if(!isCallClicked.value) Icons.Default.MicNone else Icons.Default.MicOff,
            description = "Mic",
            activeBackgroundColor = Color.White,
            state = isCallClicked
        ) {
            isCallClicked.value = !isCallClicked.value
            viewModel.toggleVoice()
        }
        MenuButton(
            imageVector = if(!isVideoClicked.value) Icons.Default.Videocam else Icons.Default.VideocamOff,
            description = "Cam",
            activeBackgroundColor = Color.White,
            state = isVideoClicked
        ) {
            isVideoClicked.value = !isVideoClicked.value
            viewModel.toggleVideo()
        }
        MenuButton(
            imageVector = Icons.Default.Close,
            description = "Close",
            activeBackgroundColor = Color.White,
            passiveBackgroundColor = Color.Red,
            iconColor = Color.White
        ) {
            viewModel.closeSession()
        }
    }
}

@Composable
@Preview
fun ControllerPreview() {
    WebRTCController(viewModel = viewModel())
}