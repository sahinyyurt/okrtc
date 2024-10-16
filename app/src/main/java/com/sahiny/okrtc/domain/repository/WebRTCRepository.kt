package com.sahiny.okrtc.domain.repository

import org.webrtc.IceCandidate

interface WebRTCRepository {
    fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean, roomId: String)
}