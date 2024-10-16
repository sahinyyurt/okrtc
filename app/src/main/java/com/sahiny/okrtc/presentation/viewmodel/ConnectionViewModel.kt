package com.sahiny.okrtc.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahiny.okrtc.domain.client.SignalingClient
import com.sahiny.okrtc.domain.client.WebRTCClient
import com.sahiny.okrtc.domain.event.SignalEvent
import com.sahiny.okrtc.domain.event.WebRTCEvent
import com.sahiny.okrtc.domain.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val signalingClient: SignalingClient,
    private val webRTCClient: WebRTCClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId = MutableStateFlow(savedStateHandle["roomID"] ?: "")
    private val isJoin = MutableStateFlow(savedStateHandle["isJoin"] ?: false)

    private val _webRTCEvent = MutableSharedFlow<WebRTCEvent>()
    val webRTCEvent = _webRTCEvent

    private val signalEvent = signalingClient.getEvent()

    val peerConnectionEvent = webRTCClient.getEvent()

    val uiState = MutableStateFlow<UiState>(UiState.UnInitialized)

    init {
        viewModelScope.launch {
            initSignal()
            receiveSignal()
        }
    }

    private suspend fun receiveSignal() {
        signalEvent.collect {
            when (it) {
                is SignalEvent.OfferReceived -> {
                    if (isJoin.value) {
                        onRemoteSessionReceived(it.data)
                        answer()
                    }
                }

                is SignalEvent.AnswerReceived -> if (isJoin.value.not()) onRemoteSessionReceived(it.data)
                is SignalEvent.IceCandidateReceived -> addCandidate(it.data)
                is SignalEvent.EndCallReceived -> closeSession()
            }
        }
    }

    private fun initSignal() = viewModelScope.launch {
        signalingClient.initialize(roomId.value)
    }

    fun initRTC() = viewModelScope.launch {
        _webRTCEvent.emit(WebRTCEvent.Initialize(webRTCClient))
    }

    fun connect() = viewModelScope.launch {
        signalingClient.connect()
    }

    fun sendIceCandidate(candidate: IceCandidate?) =
        viewModelScope.launch {
            webRTCClient.sendIceCandidate(candidate, isJoin.value, roomId.value)
        }

    fun addCandidate(candidate: IceCandidate?) = viewModelScope.launch {
        webRTCClient.addCandidate(candidate)
    }

    private fun onRemoteSessionReceived(sessionDescription: SessionDescription) =
        viewModelScope.launch {
            webRTCClient.onRemoteSessionReceived(sessionDescription)
        }

    private fun answer() = viewModelScope.launch {
        webRTCClient.answer(roomId.value)
    }

    fun call() = viewModelScope.launch {
        if (!isJoin.value) webRTCClient.call(roomId.value)
    }

    fun toggleVoice() = viewModelScope.launch {
        webRTCClient.toggleVoice()
    }

    fun toggleVideo() = viewModelScope.launch {
        webRTCClient.toggleVideo()
    }

    fun closeSession() = viewModelScope.launch {
        try {
            webRTCClient.closeSession(roomID = roomId.value)
            _webRTCEvent.emit(WebRTCEvent.CloseSession(webRTCClient))
        } catch (e: Exception) {
            Log.e("error", "${e.message}")
        }
    }
}