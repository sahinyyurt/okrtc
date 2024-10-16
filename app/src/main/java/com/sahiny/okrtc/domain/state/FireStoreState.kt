package com.sahiny.okrtc.domain.state

sealed class FireStoreState {
    data object Idle : FireStoreState()
    data class EnterRoom(val roomId: String, val isJoin: Boolean) : FireStoreState()
    data object RoomAlreadyEnded : FireStoreState()
    data object RoomNotFound : FireStoreState()
    data object RoomIdEmpty : FireStoreState()
}
