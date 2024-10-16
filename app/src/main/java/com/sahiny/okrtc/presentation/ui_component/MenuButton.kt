package com.sahiny.okrtc.presentation.ui_component

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MenuButton(
    imageVector: ImageVector,
    description: String,
    activeBackgroundColor: Color,
    passiveBackgroundColor: Color = Color.Gray,
    iconColor: Color = Color.Black,
    state: State<Boolean> = mutableStateOf(true),
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        backgroundColor = if (!state.value) activeBackgroundColor else passiveBackgroundColor
    ) {
        Icon(imageVector = imageVector, contentDescription = description, tint = iconColor)
    }
}

@Composable
@Preview
fun MenuPreview() {
    val isClicked = rememberSaveable { mutableStateOf(false) }
    MenuButton(
        imageVector = Icons.Default.Face,
        description = "Face",
        activeBackgroundColor = Color.Green,
        state = isClicked
    ) {

    }
}