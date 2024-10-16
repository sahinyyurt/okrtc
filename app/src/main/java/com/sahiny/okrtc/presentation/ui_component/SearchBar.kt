package com.sahiny.okrtc.presentation.ui_component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: State<String>,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = state.value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        label = label
    )
}