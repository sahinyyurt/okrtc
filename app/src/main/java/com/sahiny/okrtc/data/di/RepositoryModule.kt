package com.sahiny.okrtc.data.di

import com.sahiny.okrtc.domain.repository.FireStoreRepository
import com.sahiny.okrtc.domain.repository.SignalRepository
import com.sahiny.okrtc.domain.repository.WebRTCRepository

import com.sahiny.okrtc.data.repository.FireStoreRepositoryImpl
import com.sahiny.okrtc.data.repository.SignalRepositoryImpl
import com.sahiny.okrtc.data.repository.WebRTCRepositoryImpl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    abstract fun bindsSignalRepository(signalRepositoryImpl: SignalRepositoryImpl): SignalRepository

    @Binds
    abstract fun bindsWebRTCRepository(webRTCRepositoryImpl: WebRTCRepositoryImpl): WebRTCRepository

    @Binds
    abstract fun bindsFireStoreRepository(fireStoreRepositoryImpl: FireStoreRepositoryImpl): FireStoreRepository
}