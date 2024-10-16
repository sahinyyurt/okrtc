package com.sahiny.okrtc.domain.usecase

import com.sahiny.okrtc.domain.repository.FireStoreRepository
import javax.inject.Inject

class GetRoomInfoUseCase @Inject constructor(private val fireStoreRepository: FireStoreRepository) {

    suspend operator fun invoke(roomId: String) = fireStoreRepository.getRoomInfo(roomId)
}