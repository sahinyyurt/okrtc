package com.sahiny.okrtc.domain.repository

import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.DocumentSnapshot

interface FireStoreRepository {
    fun getRoomInfo(roomID: String): Flow<DocumentSnapshot>
}