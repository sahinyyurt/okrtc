package com.sahiny.okrtc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahiny.okrtc.domain.state.FireStoreState
import com.sahiny.okrtc.domain.usecase.GetRoomInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireStoreViewModel @Inject constructor(
    private val getRoomInfoUseCase: GetRoomInfoUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<FireStoreState>(FireStoreState.Idle)
    val state = _state.asStateFlow()
    fun getRoomInfo(roomId: String, isJoin: Boolean) = viewModelScope.launch {
        if(roomId.isEmpty()) {
            _state.emit(FireStoreState.RoomIdEmpty)
        } else {
            getRoomInfoUseCase(roomId).collect { snapshot ->
                if (snapshot["type"] == "END_CALL") {
                    _state.emit(FireStoreState.RoomAlreadyEnded)
                }
                else if(!snapshot.exists() && isJoin) {
                    _state.emit((FireStoreState.RoomNotFound))
                }
                else {
                    _state.emit(FireStoreState.EnterRoom(roomId, isJoin))
                }
            }
        }

    }
}