package com.sahiny.okrtc.domain.repository

import kotlinx.coroutines.flow.Flow

interface SignalRepository {
    fun connect(roomID: String) : Flow<Map<String, Any>>
}