package com.sahiny.okrtc.domain.event

import com.sahiny.okrtc.domain.client.WebRTCClient

sealed class WebRTCEvent {
    data class Initialize(val webRTCClient: WebRTCClient) : WebRTCEvent()

    data class CloseSession(val webRTCClient: WebRTCClient)  : WebRTCEvent()

}
