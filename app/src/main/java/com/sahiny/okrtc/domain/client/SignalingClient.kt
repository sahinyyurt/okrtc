package com.sahiny.okrtc.domain.client

import com.sahiny.okrtc.domain.event.SignalEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow

interface SignalingClient {
    fun initialize(roomID: String)

    fun connect(): Job

    fun handleIceCandidateReceived(data: Map<String, Any>)

    fun handleAnswerReceived(data: Map<String, Any>)

    fun handleOfferReceived(data: Map<String, Any>)

    fun handleEndCallReceived()

    fun getEvent(): SharedFlow<SignalEvent>
}