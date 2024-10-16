package com.sahiny.okrtc.data.di

import com.sahiny.okrtc.data.client.SignalingClientImpl
import com.sahiny.okrtc.data.client.WebRTCClientImpl
import com.sahiny.okrtc.domain.client.SignalingClient
import com.sahiny.okrtc.domain.client.WebRTCClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ClientModule {

    @Binds
    abstract fun providesSignalingClient(signalingClient: SignalingClientImpl): SignalingClient

    @Binds
    abstract fun provideWebRTCClient(webRTCClient: WebRTCClientImpl): WebRTCClient
}